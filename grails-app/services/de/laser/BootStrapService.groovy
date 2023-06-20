package de.laser

import com.opencsv.CSVParser
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import com.opencsv.ICSVParser
import de.laser.auth.*
import de.laser.config.ConfigDefaults
import de.laser.config.ConfigMapper
import de.laser.properties.PropertyDefinition
import de.laser.storage.RDConstants
import de.laser.system.SystemEvent
import de.laser.system.SystemSetting
import de.laser.utils.AppUtils
import de.laser.utils.DateUtils
import de.laser.utils.PasswordUtils
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Environment
import groovy.sql.Sql
import org.hibernate.SessionFactory
import org.hibernate.query.NativeQuery
import org.hibernate.type.TextType

import javax.sql.DataSource

/**
 * This service encapsulates methods called upon system startup; it defines system-wide constants, updates hard-coded translations and sets other globally relevant parameters
 */
@Transactional
class BootStrapService {

    CacheService cacheService
    DataSource dataSource
    GrailsApplication grailsApplication
    RefdataReorderService refdataReorderService
    SessionFactory sessionFactory

    static final BOOTSTRAP = true   // indicates this object is created via bootstrap (= is hard-coded in system, persists database resets and instances)

    /**
     * Runs initialisation and triggers other startup methods
     * @param servletContext unused
     */
    void init (boolean quickStart) {

        ConfigMapper.setConfig( ConfigMapper.QUARTZ_HEARTBEAT, null ) // config injection

        if (Environment.isDevelopmentMode() && ! quickStart) {
            ConfigMapper.checkCurrentConfig()
            SystemEvent.checkDefinedEvents()
        }

        log.info('--------------------------------------------------------------------------------')

        log.info("SystemId:      ${ConfigMapper.getLaserSystemId()}")
        log.info("Version:       ${AppUtils.getMeta('info.app.version')}")
        log.info("Server:        ${AppUtils.getCurrentServer()} @ ${ConfigMapper.getGrailsServerURL()}")
        log.info("Database #1:   ${ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_DEFAULT + '.url', String)} ;  dbCreate: ${ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_DEFAULT + '.dbCreate', String)}")
        log.info("Database #2:   ${ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_STORAGE + '.url', String)} ;  dbCreate: ${ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_STORAGE + '.dbCreate', String)}")
        log.info("Database migration plugin updateOnStart: ${ConfigMapper.getPluginConfig('databasemigration.updateOnStart', Boolean)}")
        log.info("Documents:     ${ConfigMapper.getDocumentStorageLocation()}")

        String dsp = cacheService.getDiskStorePath()
        if (dsp) {
            log.info("Cache: ${dsp}")
        }

        log.info('--------------------------------------------------------------------------------')
        SystemEvent.createEvent('BOOTSTRAP_STARTUP')

        if (quickStart) {
            log.info("Quick start performed - setup operations ignored .. ")
            log.info('--------------------------------------------------------------------------------')
        }
        else {
            // Reset harddata flag for given refdata and properties

            RefdataValue.executeUpdate('UPDATE RefdataValue rdv SET rdv.isHardData =:reset', [reset: false])
            RefdataCategory.executeUpdate('UPDATE RefdataCategory rdc SET rdc.isHardData =:reset', [reset: false])
            PropertyDefinition.executeUpdate('UPDATE PropertyDefinition pd SET pd.isHardData =:reset', [reset: false])

            // Here we go ..

            log.debug("updatePsqlRoutines ..")
            updatePsqlRoutines()

            log.debug("setupRefdata ..")
            setupRefdata()

            log.debug("reorderRefdata ..")
            refdataReorderService.reorderRefdata()

            log.debug("setupPropertyDefinitions ..")
            setupPropertyDefinitions()

            log.debug("setupRolesAndPermissions ..")
            setupRolesAndPermissions()

            log.debug("setupSystemUser ..")
            setupSystemUser()

            log.debug("setupSystemSettings ..")
            setupSystemSettings()

            log.debug("setOrgRoleGroups ..")
            setOrgRoleGroups()

            log.debug("setIdentifierNamespace ..")
            setIdentifierNamespace()

            log.debug("adjustDatabasePermissions ..")
            adjustDatabasePermissions()
        }

        log.debug("JSON.registerObjectMarshaller(Date) ..")

        JSON.registerObjectMarshaller(Date) {
            return it ? DateUtils.getSDF_yyyyMMddTHHmmssZ().format( it ) : null
            //return it?.format("yyyy-MM-dd'T'HH:mm:ss'Z'")
        }

        log.debug(".__                                .________ ")
        log.debug("|  |   _____    ______ ___________  \\_____  \\ ~ Grails_5")
        log.debug("|  |   \\__  \\  /  ___// __ \\_  __ \\   _(__  < ")
        log.debug("|  |___ / __ \\_\\___ \\\\  ___/|  | \\/  /       \\ ")
        log.debug("|_____ (____  /____  >\\___  >__|    /______  / ")
        log.debug("      \\/    \\/     \\/     \\/               \\/ ")
    }

    /**
     * Destructor method
     */
    void destroy() {}

    /**
     * Sets - if not exists - one or more system users with global roles and a fallback anonymous user if all users have been deleted. The system users are
     * defined in the server config (see laser2-config example) file which should be stored on each server instance / locale instance separately
     */
    void setupSystemUser() {

        // Create anonymousUser that serves as a replacement when users are deleted
        User au = User.findByUsername('anonymous')
        if (au) {
            log.debug("${au.username} exists .. skipped")
        }
        else {
            log.debug("creating user ..")

            au = new User(
                    username: 'anonymous',
                    password: PasswordUtils.getRandomUserPassword(),
                    display: 'Anonymous User',
                    email: 'laser@hbz-nrw.de',
                    enabled: false
            ).save(failOnError: true)

            Role role = Role.findByAuthority('ROLE_USER')

            if (role.roleType != 'user') {
                log.debug("  -> adding role: ${role}")
                UserRole.create au, role
            }
        }
    }

    /**
     * Sets up the global and institutional roles and grants the permissions to them
     * @see Role
     * @see Perm
     * @see PermGrant
     */
    void setupRolesAndPermissions() {

        PermGrant.executeUpdate('delete PermGrant pg')

        // Global User Roles - native spring support

        Closure updateRole = { String authority, String roleType, Map<String, String> translations ->

            Role role = Role.findByAuthority(authority) ?: new Role(authority: authority)
            role.setRoleType(roleType)
            role.setAuthority_de(translations['de'])
            role.setAuthority_en(translations['en'])
            role.save(failOnError: true)
            role
        }

        Role tmp = updateRole('ROLE_YODA',  'transcendent', [en: 'ROLE_YODA', de: 'ROLE_YODA'])
             tmp = updateRole('ROLE_ADMIN', 'global',       [en: 'ROLE_ADMIN', de: 'ROLE_ADMIN'])
             tmp = updateRole('ROLE_USER',  'global',       [en: 'ROLE_USER', de: 'ROLE_USER'])

        // Inst User Roles - not natively supported

        Role instAdmin  = updateRole('INST_ADM', 'user',    [en: 'INST_ADM', de: 'INST_ADM'])
        Role instEditor = updateRole('INST_EDITOR', 'user', [en: 'INST_EDITOR', de: 'INST_EDITOR'])
        Role instUser   = updateRole('INST_USER', 'user',   [en: 'INST_USER', de: 'INST_USER'])

//        Perm edit_permission = Perm.findByCode('edit') ?: new Perm(code: 'edit').save(failOnError: true)
//        Perm view_permission = Perm.findByCode('view') ?: new Perm(code: 'view').save(failOnError: true)
//
//        ensurePermGrant(instAdmin, edit_permission)
//        ensurePermGrant(instAdmin, view_permission)
//        ensurePermGrant(instEditor, edit_permission)
//        ensurePermGrant(instEditor, view_permission)
//        ensurePermGrant(instUser, view_permission)

        // Customer Types

        Closure updateRolePerms = { Role role, List<String> permList ->

            permList.each{ String code ->
                code = code.toLowerCase()
                Perm perm = Perm.findByCode(code) ?: new Perm(code: code).save(failOnError: true)
                ensurePermGrant(role, perm)
            }
        }

        Role fakeRole               = updateRole('FAKE',                                   'fake', [en: 'Fake', de: 'Fake'])
        Role orgInstRole            = updateRole(CustomerTypeService.ORG_INST_BASIC,        'org', [en: 'LAS:eR (Basic)', de: 'LAS:eR (Basic)'])
        Role orgInstProRole         = updateRole(CustomerTypeService.ORG_INST_PRO,          'org', [en: 'LAS:eR (Pro)', de: 'LAS:eR (Pro)'])
        Role orgConsortiumRole      = updateRole(CustomerTypeService.ORG_CONSORTIUM_BASIC,  'org', [en: 'Consortium Manager (Basic)', de: 'Konsortialmanager (Basic)'])
        Role orgConsortiumProRole   = updateRole(CustomerTypeService.ORG_CONSORTIUM_PRO,    'org', [en: 'Consortium Manager (Pro)',   de: 'Konsortialmanager (Pro)'])

        updateRolePerms(fakeRole,                ['FAKE'])
        updateRolePerms(orgInstRole,             [CustomerTypeService.ORG_INST_BASIC])
        updateRolePerms(orgInstProRole,          [CustomerTypeService.ORG_INST_PRO, CustomerTypeService.ORG_INST_BASIC])
        updateRolePerms(orgConsortiumRole,       [CustomerTypeService.ORG_CONSORTIUM_BASIC])
        updateRolePerms(orgConsortiumProRole,    [CustomerTypeService.ORG_CONSORTIUM_PRO, CustomerTypeService.ORG_CONSORTIUM_BASIC])
    }

    void setupSystemSettings() {

        SystemSetting mailSent = SystemSetting.findByName('MailSentDisabled')
        if (mailSent) {
            mailSent.delete()
        }

        SystemSetting.findByName('MaintenanceMode') ?: new SystemSetting(name: 'MaintenanceMode', tp: SystemSetting.CONTENT_TYPE_BOOLEAN, value: 'false').save()
        // SystemSetting.findByName('StatusUpdateInterval') ?: new SystemSetting(name: 'StatusUpdateInterval', tp: SystemSetting.CONTENT_TYPE_STRING, value: '300').save()
        // SystemSetting.findByName('AutoApproveMemberships') ?: new SystemSetting(name: 'AutoApproveMemberships', tp: SystemSetting.CONTENT_TYPE_BOOLEAN, value: 'true').save()
    }

    /**
     * Parses the given CSV file path according to the file header reference specified by objType
     * @param filePath the source file to parse
     * @param objType the object type reference; this is needed to read the definitions in the columns correctly and is one of RefdataCategory, RefdataValue or PropertyDefinition
     * @return the {@link List} of rows (each row parsed as {@link Map}) retrieved from the source file
     */
    List<Map> getParsedCsvData(String filePath) {

        List<Map> result = []
        File csvFile = grailsApplication.mainContext.getResource(filePath).file

        if (! (filePath.contains('RefdataCategory') || filePath.contains('RefdataValue') || filePath.contains('PropertyDefinition')) ) {
            log.warn("getParsedCsvData() - invalid object type ${filePath}!")
        }
        else if (! csvFile.exists()) {
            log.warn("getParsedCsvData() - ${filePath} not found!")
        }
        else {
            csvFile.withReader { reader ->
                //CSVReader csvrDepr = new CSVReader(reader, (char) ',', (char) '"', (char) '\\', (int) 1)
                ICSVParser csvp = new CSVParser() // csvp.DEFAULT_SEPARATOR, csvp.DEFAULT_QUOTE_CHARACTER, csvp.DEFAULT_ESCAPE_CHARACTER
                CSVReader csvr = new CSVReaderBuilder( reader ).withCSVParser( csvp ).withSkipLines( 1 ).build()
                String[] line

                while (line = csvr.readNext()) {
                    if (line[0]) {
                        if (filePath.contains('RefdataCategory')) {
                            // CSV: [token, value_de, value_en]
                            Map<String, Object> map = [
                                    token   : line[0].trim(),
                                    hardData: BOOTSTRAP,
                                    i10n    : [
                                            desc_de: line[1].trim(),
                                            desc_en: line[2].trim()
                                    ]
                            ]
                            result.add(map)
                        }
                        if (filePath.contains('RefdataValue')) {
                            // CSV: [rdc, token, value_de, value_en]
                            Map<String, Object> map = [
                                    token   : line[1].trim(),
                                    rdc     : line[0].trim(),
                                    hardData: BOOTSTRAP,
                                    i10n    : [
                                            value_de: line[2].trim(),
                                            value_en: line[3].trim(),
                                            expl_de:  line[4].trim(),
                                            expl_en:  line[5].trim()
                                    ]
                            ]
                            result.add(map)
                        }
                        if (filePath.contains('PropertyDefinition')) {
                            Map<String, Object> map = [
                                    token       : line[1].trim(),
                                    category    : line[0].trim(),
                                    type        : line[4].trim(),
                                    rdc         : line[5].trim(),
                                    mandatory   : new Boolean( line[6].trim() ),
                                    multiple    : new Boolean( line[7].trim() ),
                                    logic       : new Boolean( line[8].trim() ),
                                    tenant      : line[11].trim(),
                                    hardData    : BOOTSTRAP,
                                    i10n        : [
                                            name_de: line[2].trim(),
                                            name_en: line[3].trim(),
                                            expl_de: line[9].trim(),
                                            expl_en: line[10].trim()
                                    ]
                            ]
                            result.add(map)
                        }
                    }
                }
            }
        }
        result
    }

    /**
     * Creates or updates stored database functions. They are located in the /grails-app/migrations/functions folder
     */
    void updatePsqlRoutines() {

        try {
            URL folder = this.class.classLoader.getResource('functions')
            File dir = new File(folder.file)

            if (dir.exists()) {
                log.debug('scanning ' + dir.getAbsolutePath())

                dir.listFiles().each { file ->
                    String fileName = file.getName()
                    if (fileName.endsWith('.sql')) {
                        String fileSql     = file.readLines().join(System.getProperty("line.separator")).trim()
                        String validateSql = "SELECT proname, regexp_matches(prosrc, 'VERSION CONSTANT NUMERIC = [0-9]*') FROM pg_proc WHERE proname = '" +
                                fileName.replace('.sql', '') + "'"

                        if (fileSql.take(26).equalsIgnoreCase('CREATE OR REPLACE FUNCTION')) {

                            try {
                                NativeQuery query    = sessionFactory.currentSession.createSQLQuery(fileSql)
                                NativeQuery validate = sessionFactory.currentSession
                                        .createSQLQuery(validateSql)
                                        .addScalar("regexp_matches", new TextType())

                                query.executeUpdate()
                                log.debug("  -> ${fileName} : " + validate.list()?.get(0))
                            }
                            catch(Exception e) {
                                log.error("  -> ${fileName} : " + e)
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            log.warn('.. failed: ' + e.getMessage())
        }
    }

    /**
     * Ensures database permissions for the backup and readonly users
     */
    void adjustDatabasePermissions() {

        Sql sql = new Sql(dataSource)
        sql.rows("SELECT * FROM grants_for_maintenance()")
    }

    /**
     * Assigns to the given role the given permission
     * @param role the {@link Role} whose permissions should be granted
     * @param perm the {@link Perm} permission to be granted
     */
    void ensurePermGrant(Role role, Perm perm) {

        PermGrant existingPermGrant = PermGrant.findByRoleAndPerm(role,perm)
        if (! existingPermGrant) {
            new PermGrant(role:role, perm:perm).save()
        }
    }

    /**
     * RefdataValue.group is used only for OrgRole to filter the types of role available in 'Add Role' action
     * This is done by providing 'linkType' (using instance class) to the '_orgLinksModal' template.
     * This method sets those (actually never used) reference groups
     */
    void setOrgRoleGroups() {

        String lic = License.name
        String sub = Subscription.name
        String pkg = Package.name

        List entries = [
                ['Licensor', lic],
                ['Licensee', lic],
                ['Licensing Consortium', lic],
                ['Negotiator', lic],
                ['Subscriber', sub],
                ['Provider', sub],
                ['Subscription Agent', sub],
                ['Subscription Consortia', sub],
                ['Content Provider', pkg],
                ['Package Consortia', pkg],
                ['Publisher', null],
                ['Agency', sub]
        ]

        entries.each{ List<String> rdv ->
            String token = rdv[0]
            String group = rdv[1]

            RefdataValue val = RefdataValue.getByValueAndCategory(token, RDConstants.ORGANISATIONAL_ROLE)
            if (group) {
                val.setGroup(group)
            }
            val.save()
        }
    }

    /**
     * Processes the hard coded reference value sources and updates the reference values and their categories
     * @see RefdataValue
     * @see RefdataCategory
     */
    void setupRefdata() {

        List rdcList = getParsedCsvData( ConfigDefaults.SETUP_REFDATA_CATEGORY_CSV )
        rdcList.each { map ->
            RefdataCategory.construct(map)
        }

        List rdvList = getParsedCsvData( ConfigDefaults.SETUP_REFDATA_VALUE_CSV )
        rdvList.each { map ->
            RefdataValue.construct(map)
        }
    }

    /**
     * Processes the hard coded property definition source and updates the property definitions
     * @see PropertyDefinition
     */
    void setupPropertyDefinitions() {

        List pdList = getParsedCsvData( ConfigDefaults.SETUP_PROPERTY_DEFINITION_CSV )
        pdList.each { map ->
            PropertyDefinition.construct(map)
        }
    }

    /**
     * This is the hard coded list of identifier namespace definitions; the method creates or updates the namespaces according to those entries.
     * Beware: description_de and description_en are nullable but not blank!
     * @see IdentifierNamespace
     */
    void setIdentifierNamespace() {

        //TODO isUnique/isHidden flags are set provisorically to "false", adaptations may be necessary
        List<Map<String,Object>> namespaces = [
            [ns: "Anbieter_Produkt_ID", name_de: "Anbieter-Produkt-ID", description_de: "Interne, eindeutige ID der Anbieter für die eigenen Pakete.", name_en: "Provider-product-ID", description_en: "Internal unique ID of provider for the own packages.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "Anbieter_Produkt_ID", name_de: "Anbieter-Produkt-ID", description_de: "Interne, eindeutige ID der Anbieter für die eigenen Pakete.", name_en: "Provider-product-ID", description_en: "Internal unique ID of provider for the own packages.", nsType: IdentifierNamespace.NS_PACKAGE, isUnique: false, isHidden: false],
            [ns: "crossref funder id", name_de: "Crossref Funder ID", description_de: null, name_en: "Crossref Funder ID", description_en: null, nsType: IdentifierNamespace.NS_ORGANISATION, isUnique: false, isHidden: false],
            [ns: "DBS-ID", name_de: "DBS-ID", description_de: "ID in der Deutschen und Österreichischen Bibliotheksstatistik (DBS/ÖBS) (https://www.bibliotheksstatistik.de/).", name_en: "DBS-ID", description_en: "ID in the German and Austrian library statistic (DBS/ÖBS) (https://www.bibliotheksstatistik.de/).", nsType: IdentifierNamespace.NS_ORGANISATION, urlPrefix: null, isUnique: false, isHidden: false],
            [ns: "dbis_org_id", name_de: "DBIS-Organisations-ID", description_de: "ID Ihrer Bibliothek oder Einrichtung im DBIS System, typischerweise ein Kürzel mit mehreren Buchstaben, z.B. 'ub_r'. Derzeit z.B. über die URL der eigenen Einrichtung auslesbar.", name_en: "DBIS organisation ID", description_en: "ID of your library or organisation in the DBIS system, typically an abbreviation with several letters, e.g. 'ub_r'. It may be read off currently from the URL of your own institution for example.", nsType: IdentifierNamespace.NS_ORGANISATION, urlPrefix: "https://dbis.ur.de//fachliste.php?bib_id=", isUnique: true, isHidden: false],
            [ns: "dbis_res_id", name_de: "DBIS-Ressourcen-ID", description_de: "ID für eine Datenbank oder allgemein Ressource im DBIS-System, die Sie z.B. mit einer Lizenz verknüpfen können.", name_en: "DBIS resource ID", description_en: "ID for a database or generally a resource in the DBIS system what you may link to a subscription for example.",  nsType: IdentifierNamespace.NS_SUBSCRIPTION, urlPrefix: "https://dbis.uni-regensburg.de/frontdoor.php?titel_id=", isUnique: false, isHidden: false],
            [ns: "dbpedia", name_de: "DBpedia", description_de: null, name_en: "DBpedia", description_en: null, nsType: IdentifierNamespace.NS_ORGANISATION, isUnique: false, isHidden: false],
            [ns: "DNB_ID", name_de: "DNB-ID", description_de: "Identifikator der Deutschen Nationalbibliothek.", name_en: "DNB-ID", description_en: "Identifier of the German National Library (DNB).", nsType: IdentifierNamespace.NS_SUBSCRIPTION, urlPrefix: "http://d-nb.info/", isUnique: false, isHidden: false],
            [ns: "eissn", name_de: "E-ISSN", description_de: "Internationale Standardnummer(n) für fortlaufende Sammelwerke.", name_en: "E-ISSN", description_en: "International standard number(s) for continued series.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "eduPersonEntitlement", name_de: "Entitlement KfL-Portal", description_de: "das Shibboleth-Entitlement", name_en: "Entitlement KfL portal", description_en: "the Shibboleth entitlement", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "ezb_anchor", name_de: "EZB-Anker", description_de: "EZB-Anker werden von Konsortialverwaltern für deren jeweilige angelegte Kollektionen vergeben, um so die genaue Lizenzeinheit abbilden zu können.", name_en: "EZB anchor", description_en: "EZB anchors are distributed by consortia managers for their respective collections in order to represent the subscription unit.", nsType:null, isUnique: false, isHidden: false],
            [ns: "ezb_collection_id", name_de: "EZB-Kollektions-ID", description_de: "Automatisch vergebene ID. Beim Abruf frei verfügbarer Titellisten der jeweiligen Kollektionen dient die Kollektions-ID als eindeutiger Identifikator.", name_en: "EZB collection id", description_en: "Automatically distributed ID. The collection ID serves as unique identifier upon a call of the entitlement lists of the collections.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, urlPrefix: "http://ezb.ur.de/api/collections/", isUnique: false, isHidden: false],
            [ns: "ezb_org_id", name_de: "EZB-ID", description_de: "Identifikator der Elektronischen Zeitschriftendatenbank (EZB). Mehrfachangabe möglich.", name_en: "EZB-ID", description_en: "Identifier of Electronic Journals Library (EZB). Multiple insertion possible.", nsType: IdentifierNamespace.NS_ORGANISATION, urlPrefix: "https://ezb.uni-regensburg.de/fl.phtml?bibid=", isUnique: false, isHidden: false],
            [ns: "ezb_sub_id", name_de: "EZB-ID", description_de: "Identnummer, die einen bestimmten Eintrag in der Elektronischen Zeitschriftenbibliothek EZB auszeichnet.", name_en: "EZB-ID", description_en: "Identification number which indicates a certain entry in the Electronic Journals Library (EZB).", nsType: IdentifierNamespace.NS_SUBSCRIPTION, urlPrefix: "http://rzbvm017.uni-regensburg.de/ezeit/detail.phtml?bibid=AAAAA&colors=7&lang=de&jour_id=", isUnique: false, isHidden: false],
            [ns: "gnd_org_nr", name_de: "GND-NR", description_de: "Eindeutiger und stabiler Bezeichner für jede einzelne Entität in der GND (Gemeinsame Normdatei). https://www.dnb.de/DE/Professionell/Standardisierung/GND/gnd_node.html", name_en: "GND-NR", description_en: "Unique and stable identifier for every entity in the GND (Integrated Authority File). https://www.dnb.de/EN/Professionell/Standardisierung/GND/gnd_node.html", nsType: IdentifierNamespace.NS_ORGANISATION, urlPrefix: "https://d-nb.info/gnd/", isUnique: false, isHidden: false],
            [ns: "ISBN", name_de: "ISBN", description_de: "Internationale Standardbuchnummer.", name_en: "ISIL", description_en: "International standard book number.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "ISIL", name_de: "ISIL", description_de: "International Standard Identifier for Libraries and Related Organizations, entspricht dem Bibliothekssigel der Sigelstelle. Sind der Einrichtung mehrere ISIL zugeordnet, ist eine Mehrfachangabe möglich.", name_en: "ISIL", description_en: "International Standard Identifier for Libraries and Related Organizations, corresponds to the library identifier of the identifier authority. Are several ISILs assigned to the organisation, multiple distribution is possible.", nsType: IdentifierNamespace.NS_ORGANISATION, urlPrefix: "https://sigel.staatsbibliothek-berlin.de/suche/?isil=", isUnique: false, isHidden: false],
            [ns: "ISIL Paketsigel", name_de: "ZDB-Paketsigel", description_de: "ZDB-Produktkennzeichnung für (Gesamt)pakete, vergeben von der ISIL-Agentur.", name_en: "ISIL package identifier", description_en: "ZDB product marking for (whole) packages, distributed by the ISIL agency.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, urlPrefix: "https://sigel.staatsbibliothek-berlin.de/suche/?isil=", isUnique: false, isHidden: false],
            [ns: "isil_product", name_de: "ZDB-Produktsigel", description_de: "ZDB-Produktsigel für Teilpakete, vergeben von der ISIL-Agentur.", name_en: "ISIL product identifier", description_en: "ZDB product marking for partial packages, distributed by the ISIL agency.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, urlPrefix: "https://sigel.staatsbibliothek-berlin.de/suche/?isil=", isUnique: false, isHidden: false],
            [ns: "isni", name_de: "ISNI", description_de: null, name_en: "ISNI", description_en: null, nsType: IdentifierNamespace.NS_ORGANISATION, isUnique: false, isHidden: false],
            [ns: "leibniz", name_de: "Leibniz-ID", description_de: "Kürzel einer Einrichtung der Leibniz-Gemeinschaft", name_en: "Leibniz-ID", description_en: "Abbreviation of an institution of the Leibniz association", nsType: IdentifierNamespace.NS_ORGANISATION, isUnique: true, isHidden: false],
            [ns: "loc id", name_de: "LOC ID", description_de: null, name_en: "LOC ID", description_en: null, nsType: IdentifierNamespace.NS_ORGANISATION, isUnique: false, isHidden: false],
            [ns: "pissn", name_de: "P-ISSN", description_de: "Internationale Standardnummer(n) für fortlaufende Sammelwerke.", name_en: "P-ISSN", description_en: "International standard number(s) for continued series.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "Rechnungssystem_Nummer", name_de: "Rechnungssystem-Nr.", description_de: "Individuelle, interne Rechnungsnummer, vergeben von der einzelnen Einrichtung.", name_en: "Invoice system number", description_en: "Unique internal invoice system number, assigned by the respective institution.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "ROR ID", name_de: "ROR-ID", description_de: "Identifikator einer Forschungseinrichtung im Research Organization Registry (ROR)", name_en: "ROR-ID", description_en: "Identifier of a research organization in the Research Organization Registry (ROR).", nsType: IdentifierNamespace.NS_ORGANISATION, urlPrefix: "https://ror.org/", isUnique: false, isHidden: false],
            [ns: "SFX-Anker", name_de: "SFX-Anker", description_de: "Eintrag im Linkresolver SFX (Ex Libris Group).", name_en: "SFX anchor", description_en: "Entry in the Linkresolver SFX (Ex Libris Group).", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "sis_kfl_proof", name_de: "Nachweis im KfL-Portal", description_de: "Nachweis im KfL-Portal", name_en: "Proof in KfL portal", description_en: "Proof in KfL portal", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "fid_negotiator_id", name_de: "FID-Verhandlungs-ID", "description_de": null, "name_en": "SIS negotiator ID", description_en: null, nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false], //when creating the namespace, I did not knew that DFG provided a translation for German Fachonformationsdienste (Specialised Information Services)
            [ns: "sis_nl_proof", name_de: "Nachweis im NL-Portal", description_de: "Nachweis im NL-Portal", name_en: "Proof in NL portal", description_en: "Proof in NL portal", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "sis_product_id", name_de: "FID-Produkt-ID", description_de: "eindeutige FID-Produktkennung", name_en: "SIS product ID", description_en: "unique SIS product marking", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: true, isHidden: false],
            [ns: "VAT", name_de: "VAT", description_de: "Internationale Bezeichnung für Umsatzsteuer-ID bzw. Steuer-Ident-Nummer. Zur eindeutigen Identifikation eines Unternehmen im steuerrechtlichen Sinne.", name_en: "VAT", description_en: null, nsType: IdentifierNamespace.NS_ORGANISATION, isUnique: true, isHidden: false],
            [ns: "viaf", name_de: "VIAF", description_de: null, name_en: "VIAF", description_en: null, nsType: IdentifierNamespace.NS_ORGANISATION, isUnique: false, isHidden: false],
            [ns: "wibid", name_de: "WIB-ID", description_de: "Identifikator, den Sie bei der Registrierung auf nationallizenzen.de erhalten.", name_en: "WIB-ID", description_en: "The identifier you received upon registration on nationallizenzen.de.", nsType: IdentifierNamespace.NS_ORGANISATION, isUnique: true, isHidden: false], //, validationRegex: "WIB\\d{4}" needed to be crossed out because of initial dummy values
            [ns: "wikidata id", name_de: "Wikidata ID", description_de: null, name_en: "Wikidata ID", description_en: null, nsType: IdentifierNamespace.NS_ORGANISATION, isUnique: false, isHidden: false],
            [ns: "zdb", name_de: "ZDB-ID", description_de: "ID der Ressource in der ZDB.", name_en: "ZDB-ID", description_en: "ID of resource in ZDB.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, urlPrefix: "https://ld.zdb-services.de/resource/", isUnique: false, isHidden: false]
        ]

        List<IdentifierNamespace> hardCodedIDNS = IdentifierNamespace.findAllByIsHardData(true)

        hardCodedIDNS.each { IdentifierNamespace current ->
            Map<String, Object> hardCodedNamespaceProps = namespaces.find { Map<String, Object> hardCoded -> hardCoded.ns == current.ns && hardCoded.nsType == current.nsType }
            boolean updated = false
            if(hardCodedNamespaceProps) {
                hardCodedNamespaceProps.each { String prop, Object value ->
                    if(current[prop] != value) {
                        current[prop] = value
                        updated = true
                    }
                }
                if(!hardCodedNamespaceProps.containsKey('validationRegex') && current.validationRegex) {
                    current.validationRegex = null
                    updated = true
                }
                if(!current.isFromLaser) {
                    current.isFromLaser = true
                    updated = true
                }
            }
            else {
                current.isHardData = false
                updated = true
            }
            if(updated)
                current.save()
        }
        namespaces.each { Map<String, Object> namespaceProperties ->
            if(!hardCodedIDNS.find { IdentifierNamespace existing -> existing.ns == namespaceProperties.ns && existing.nsType == namespaceProperties.nsType }) {
                namespaceProperties.isFromLaser = true
                namespaceProperties.isHardData = true
                IdentifierNamespace.construct(namespaceProperties)
            }
        }
    }
}