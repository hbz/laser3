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

//            log.debug("clearCaches ..")
//            clearCaches()

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
        log.debug("|  |   _____    ______ ___________  \\_____  \\ ~ Grails_6")
        log.debug("|  |   \\__  \\  /  ___// __ \\_  __ \\   _(__  < ")
        log.debug("|  |___ / __ \\_\\___ \\\\  ___/|  | \\/  /       \\ ")
        log.debug("|_____ (____  /____  >\\___  >__|    /______  / ")
        log.debug("      \\/    \\/     \\/     \\/               \\/ ")
    }

    /**
     * Destructor method
     */
    void destroy() {}

//    void clearCaches() {
//    }

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
        Role orgSupportRole         = updateRole(CustomerTypeService.ORG_SUPPORT,           'org', [en: 'Interne Verwaltung (HBZ)', de: 'Interne Verwaltung (HBZ)'])

        updateRolePerms(fakeRole,                ['FAKE'])
        updateRolePerms(orgInstRole,             [CustomerTypeService.ORG_INST_BASIC])
        updateRolePerms(orgInstProRole,          [CustomerTypeService.ORG_INST_PRO, CustomerTypeService.ORG_INST_BASIC])
        updateRolePerms(orgConsortiumRole,       [CustomerTypeService.ORG_CONSORTIUM_BASIC])
        updateRolePerms(orgConsortiumProRole,    [CustomerTypeService.ORG_CONSORTIUM_PRO, CustomerTypeService.ORG_CONSORTIUM_BASIC])
        updateRolePerms(orgSupportRole,          [CustomerTypeService.ORG_SUPPORT, CustomerTypeService.ORG_CONSORTIUM_PRO, CustomerTypeService.ORG_CONSORTIUM_BASIC])
    }

    /**
     * Removes the MailSentDisabled system settings and ensures the existence of MaintenanceMode and SystemInsight system settings.
     * @see SystemSetting
     */
    void setupSystemSettings() {

        SystemSetting mailSent = SystemSetting.findByName('MailSentDisabled')
        if (mailSent) {
            mailSent.delete()
        }

        SystemSetting.findByName('MaintenanceMode') ?: new SystemSetting(name: 'MaintenanceMode', tp: SystemSetting.CONTENT_TYPE_BOOLEAN, value: 'false').save()
        SystemSetting.findByName('SystemInsight') ?: new SystemSetting(name: 'SystemInsight', tp: SystemSetting.CONTENT_TYPE_BOOLEAN, value: 'false').save()
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

        if (! (filePath.contains('RefdataCategory') || filePath.contains('RefdataValue') || filePath.contains('PropertyDefinition')|| filePath.contains('IdentifierNamespace')) ) {
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
                                    mandatory   : Boolean.parseBoolean( line[6].trim() ),
                                    multiple    : Boolean.parseBoolean( line[7].trim() ),
                                    logic       : Boolean.parseBoolean( line[8].trim() ),
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
                        if(filePath.contains('IdentifierNamespace')) {
                            Map<String, Object> map = [
                                    ns     : line[0].trim(),
                                    name_de: line[1].trim(),
                                    name_en: line[3].trim(),
                                    description_de: line[2].trim() ?: null,
                                    description_en: line[4].trim() ?: null,
                                    nsType        : line[5].trim() ?: null,
                                    urlPrefix     : line[6].trim() ?: null,
                                    isUnique      : Boolean.parseBoolean(line[7].trim()),
                                    isHidden      : Boolean.parseBoolean(line[8].trim())
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

        List namespaces = getParsedCsvData( ConfigDefaults.SETUP_IDENTIFIER_NAMESPACE_CSV )

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