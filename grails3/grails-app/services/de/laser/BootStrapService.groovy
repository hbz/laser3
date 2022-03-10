package de.laser

import com.opencsv.CSVReader
import de.laser.auth.*
import de.laser.helper.ConfigUtils
import de.laser.helper.RDConstants
import de.laser.helper.ServerUtils
import de.laser.properties.PropertyDefinition
import de.laser.system.SystemEvent
import de.laser.system.SystemSetting
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.jdbc.Work
import org.hibernate.type.TextType

import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.text.SimpleDateFormat

/**
 * This service encapsulates methods called upon system startup; it defines system-wide constants, updates hard-coded translations and sets other globally relevant parameters
 */
@Transactional
class BootStrapService {

    def apiService
    def cacheService
    def dataSource
    def grailsApplication
    def organisationService
    def refdataReorderService
    def sessionFactory
    def userService

    final static BOOTSTRAP = true   // indicates this object is created via bootstrap (= is hard-coded in system, persists database resets and instances)

    /**
     * Runs initialisation and triggers other startup methods
     * @param servletContext unused
     */
    void init(def servletContext) {

        ConfigUtils.checkConfig()

        log.info("--------------------------------------------------------------------------------")

        log.info("SystemId: ${ConfigUtils.getLaserSystemId()}")
        log.info("Server: ${ServerUtils.getCurrentServer()}")
        log.info("Database: ${grailsApplication.config.dataSource.url}")
        log.info("Database datasource dbCreate: ${grailsApplication.config.dataSource.dbCreate}")
        log.info("Database migration plugin updateOnStart: ${grailsApplication.config.grails.plugin.databasemigration.updateOnStart}")
        log.info("Documents: ${ConfigUtils.getDocumentStorageLocation()}")

        String dsp = cacheService.getDiskStorePath()
        if (dsp) {
            log.info("Cache: ${dsp}")
        }

        log.info("--------------------------------------------------------------------------------")

        SystemEvent.createEvent('BOOTSTRAP_STARTUP')

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

        log.debug("setupSystemUsers ..")
        setupSystemUsers()

        log.debug("setupAdminUsers ..")
        setupAdminUsers()

        if (UserOrg.findAllByFormalRoleIsNull()?.size() > 0) {
            log.warn("there are user org rows with no role set. Please update the table to add role FKs")
        }

        // def auto_approve_memberships = SystemSetting.findByName('AutoApproveMemberships') ?: new SystemSetting(name: 'AutoApproveMemberships', tp: SystemSetting.CONTENT_TYPE_BOOLEAN, value: 'true').save()

        SystemSetting mailSent = SystemSetting.findByName('MailSentDisabled')

        if(mailSent){
            mailSent.delete()
        }

        SystemSetting.findByName('MaintenanceMode') ?: new SystemSetting(name: 'MaintenanceMode', tp: SystemSetting.CONTENT_TYPE_BOOLEAN, value: 'false').save()
        SystemSetting.findByName('StatusUpdateInterval') ?: new SystemSetting(name: 'StatusUpdateInterval', tp: SystemSetting.CONTENT_TYPE_STRING, value: '300').save()

        // SpringSecurityUtils.clientRegisterFilter('securityContextPersistenceFilter', SecurityFilterPosition.PRE_AUTH_FILTER)

        log.debug("setOrgRoleGroups ..")
        setOrgRoleGroups()

        log.debug("setupOnixPlRefdata ..")
        setupOnixPlRefdata()

        log.debug("setupContentItems ..")
        setupContentItems()

        log.debug("setIdentifierNamespace ..")
        setIdentifierNamespace()

        log.debug("checking database ..")

        if (!Org.findAll() && !Person.findAll() && !Address.findAll() && !Contact.findAll()) {
            log.debug("database is probably empty; setting up essential data ..")
            File f = new File("${ConfigUtils.getBasicDataPath()}${ConfigUtils.getBasicDataFileName()}")
            if(f.exists())
                apiService.setupBasicData(f)
            else {
                organisationService.createOrgsFromScratch()
            }
        }

        log.debug("setJSONFormatDate ..")

        JSON.registerObjectMarshaller(Date) {
            return it ? (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")).format( it ) : null
            //return it?.format("yyyy-MM-dd'T'HH:mm:ss'Z'")
        }

        log.debug("adjustDatabasePermissions ..")
        adjustDatabasePermissions()

        /*
        only for local usage
        log.debug("vacuumAndAnalyzeTables ..")
        vacuumAndAnalyseTables()
         */

        log.debug(" .__                            .________ ")
        log.debug(" |  | _____    ______ ___________\\_____  \\ ~ grails3")
        log.debug(" |  | \\__  \\  /  ___// __ \\_  __ \\/  ____/ ")
        log.debug(" |  |__/ __ \\_\\___ \\\\  ___/|  | \\/       \\ ")
        log.debug(" |____(____  /____  >\\___  >__|  \\_______ \\  ")
        log.debug("           \\/     \\/     \\/              \\/ ")
    }

    /**
     * Destructor method
     */
    void destroy() {}

    /**
     * Sets - if not exists - one or more system users with global roles and a fallback anonymous user if all users have been deleted. The system users are
     * defined in the server config (see laser2-config example) file which should be stored on each server instance / locale instance separately
     */
    void setupSystemUsers() {

        // Create anonymousUser that serves as a replacement when users are deleted
        User anonymousUser = User.findByUsername('anonymous')
        if (anonymousUser) {
            log.debug("${anonymousUser.username} exists .. skipped")
        }
        else {
            log.debug("creating user ..")

            anonymousUser = new User(
                    username: 'anonymous',
                    password: "laser@514@2019",
                    display: 'Anonymous User',
                    email: 'laser@hbz-nrw.de',
                    enabled: false
            ).save(failOnError: true)

            Role role = Role.findByAuthority('ROLE_USER')

            if (role.roleType != 'user') {
                log.debug("  -> adding role: ${role}")
                UserRole.create anonymousUser, role
            }
        }

        if (grailsApplication.config.systemUsers) {
            log.debug("found systemUsers in local config file ..")

            grailsApplication.config.systemUsers.each { su ->
                log.debug("checking: [${su.name}, ${su.display}, ${su.roles}, ${su.affils}]")

                User user = User.findByUsername(su.name)
                if (user) {
                    log.debug("${su.name} exists .. skipped")
                }
                else {
                    log.debug("creating user ..")

                    user = new User(
                            username: su.name,
                            password: su.pass,
                            display:  su.display,
                            email:    su.email,
                            enabled:  true
                    ).save(failOnError: true)

                    su.roles.each { r ->
                        Role role = Role.findByAuthority(r)
                        if (role.roleType != 'user') {
                            log.debug("  -> adding role: ${role}")
                            UserRole.create user, role
                        }
                    }

                    su.affils.each { key, values ->
                        Org org = Org.findByShortname(key)
                        values.each { affil ->
                            Role role = Role.findByAuthorityAndRoleType(affil, 'user')
                            if (org && role) {
                                log.debug("  -> adding affiliation: ${role} for ${org.shortname} ")
                                new UserOrg(
                                        user: user,
                                        org: org,
                                        formalRole: role
                                ).save(failOnError: true)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This setup should hold only for the QA environment and is disused as everyone should set up its own environment. Was used to create demo organisations and to assign each
     * hbz group member to them for demonstration and testing purposes.
     * @see UserService#setupAdminAccounts()
     * @see OrganisationService#createOrgsFromScratch()
     */
    void setupAdminUsers() {

        if (ServerUtils.getCurrentServer() == ServerUtils.SERVER_QA) {
            log.debug("check if all user accounts are existing on QA ...")

            Map<String,Org> modelOrgs = [konsorte: Org.findByName('Musterkonsorte'),
                                         vollnutzer: Org.findByName('Mustereinrichtung'),
                                         konsortium: Org.findByName('Musterkonsortium')]

            Map<String,Org> testOrgs = [konsorte: Org.findByName('Testkonsorte'),
                                        vollnutzer: Org.findByName('Testeinrichtung'),
                                        konsortium: Org.findByName('Testkonsortium')]

            Map<String,Org> QAOrgs = [konsorte: Org.findByName('QA-Konsorte'),
                                      vollnutzer: Org.findByName('QA-Einrichtung'),
                                      konsortium: Org.findByName('QA-Konsortium')]

            userService.setupAdminAccounts(modelOrgs)
            userService.setupAdminAccounts(testOrgs)
            userService.setupAdminAccounts(QAOrgs)
        }
        else {
            log.debug('.. skipped')
        }
    }

    /**
     * Sets up the global and institutional roles and grants the permissions to them
     * @see Role
     * @see Perm
     * @see PermGrant
     * @see UserOrg
     */
    void setupRolesAndPermissions() {

        PermGrant.executeUpdate('delete PermGrant pg')

        // Permissions

        Perm edit_permission = Perm.findByCode('edit') ?: new Perm(code: 'edit').save(failOnError: true)
        Perm view_permission = Perm.findByCode('view') ?: new Perm(code: 'view').save(failOnError: true)

        // TODO: refactoring: partOf

        // Global System Roles

        Role tmp = Role.findByAuthority('ROLE_YODA')    ?: new Role(authority: 'ROLE_YODA', roleType: 'transcendent').save(failOnError: true)
             tmp = Role.findByAuthority('ROLE_ADMIN')   ?: new Role(authority: 'ROLE_ADMIN', roleType: 'global').save(failOnError: true)
             tmp = Role.findByAuthority('ROLE_USER')    ?: new Role(authority: 'ROLE_USER', roleType: 'global').save(failOnError: true)
             tmp = Role.findByAuthority('ROLE_API')     ?: new Role(authority: 'ROLE_API', roleType: 'global').save(failOnError: true)

             tmp = Role.findByAuthority('ROLE_GLOBAL_DATA')        ?: new Role(authority: 'ROLE_GLOBAL_DATA', roleType: 'global').save(failOnError: true)
             tmp = Role.findByAuthority('ROLE_ORG_EDITOR')         ?: new Role(authority: 'ROLE_ORG_EDITOR', roleType: 'global').save(failOnError: true)
             tmp = Role.findByAuthority('ROLE_PACKAGE_EDITOR')     ?: new Role(authority: 'ROLE_PACKAGE_EDITOR', roleType: 'global').save(failOnError: true)
             tmp = Role.findByAuthority('ROLE_STATISTICS_EDITOR')  ?: new Role(authority: 'ROLE_STATISTICS_EDITOR', roleType: 'global').save(failOnError: true)
             tmp = Role.findByAuthority('ROLE_TICKET_EDITOR')      ?: new Role(authority: 'ROLE_TICKET_EDITOR', roleType: 'global').save(failOnError: true)

        // Institutional Roles

        Role instAdmin = Role.findByAuthority('INST_ADM')
        if (! instAdmin) {
            instAdmin = new Role(authority: 'INST_ADM', roleType: 'user').save(failOnError: true)
        }
        ensurePermGrant(instAdmin, edit_permission)
        ensurePermGrant(instAdmin, view_permission)

        Role instEditor = Role.findByAuthority('INST_EDITOR')
        if (! instEditor) {
            instEditor = new Role(authority: 'INST_EDITOR', roleType: 'user').save(failOnError: true)
        }
        ensurePermGrant(instEditor, edit_permission)
        ensurePermGrant(instEditor, view_permission)

        Role instUser = Role.findByAuthority('INST_USER')
        if (! instUser) {
            instUser = new Role(authority: 'INST_USER', roleType: 'user').save(failOnError: true)
        }
        ensurePermGrant(instUser, view_permission)

        // Customer Type Toles

        Closure locOrgRole = { String authority, String roleType, Map<String, String> translations ->

            Role role = Role.findByAuthority(authority) ?: new Role(authority: authority, roleType: roleType).save(failOnError: true)
            I10nTranslation.createOrUpdateI10n(role, 'authority', translations)

            role
        }
        Closure createOrgPerms = { Role role, List<String> permList ->
            // TODO PermGrant.executeQuery('DELETE ALL')

            permList.each{ String code ->
                code = code.toLowerCase()
                Perm perm = Perm.findByCode(code) ?: new Perm(code: code).save(failOnError: true)
                ensurePermGrant(role, perm)
            }
        }

        Role fakeRole                = locOrgRole('FAKE',                   'fake', [en: 'Fake', de: 'Fake'])
        Role orgMemberRole           = locOrgRole('ORG_BASIC_MEMBER',       'org', [en: 'Institution consortium member', de: 'Konsorte'])
        Role orgSingleRole           = locOrgRole('ORG_INST',               'org', [en: 'Institution basic', de: 'Vollnutzer'])
        Role orgConsortiumRole       = locOrgRole('ORG_CONSORTIUM',         'org', [en: 'Consortium basic', de: 'Konsortium mit Umfragefunktion'])

        createOrgPerms(fakeRole,                    ['FAKE'])
        createOrgPerms(orgMemberRole,               ['ORG_BASIC_MEMBER'])
        createOrgPerms(orgSingleRole,               ['ORG_INST', 'ORG_BASIC_MEMBER'])
        createOrgPerms(orgConsortiumRole,           ['ORG_CONSORTIUM'])
    }

    /**
     * Parses the given CSV file path according to the file header reference specified by objType
     * @param filePath the source file to parse
     * @param objType the object type reference; this is needed to read the definitions in the columns correctly and is one of RefdataCategory, RefdataValue or PropertyDefinition
     * @return the {@link List} of rows (each row parsed as {@link Map}) retrieved from the source file
     */
    List getParsedCsvData(String filePath, String objType) {

        List result = []
        File csvFile = grailsApplication.mainContext.getResource(filePath).file

        if (! ['RefdataCategory', 'RefdataValue', 'PropertyDefinition'].contains(objType)) {
            println "WARNING: invalid object type ${objType}!"
        }
        else if (! csvFile.exists()) {
            println "WARNING: ${filePath} not found!"
        }
        else {
            csvFile.withReader { reader ->
                CSVReader csvr = new CSVReader(reader, (char) ',', (char) '"', (char) '\\', (int) 1)
                String[] line

                while (line = csvr.readNext()) {
                    if (line[0]) {
                        if (objType == 'RefdataCategory') {
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
                        if (objType == 'RefdataValue') {
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
                        if (objType == 'PropertyDefinition') {
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
                                            //descr_de: line[0].trim(),
                                            //descr_en: line[0].trim(),
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
            def folder = this.class.classLoader.getResource('functions')
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
                                SQLQuery query    = sessionFactory.currentSession.createSQLQuery(fileSql)
                                SQLQuery validate = sessionFactory.currentSession
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
     * @deprecated is replaced by {@link #setupPropertyDefinitions}
     */
    @Deprecated
    void createPropertyDefinitionsWithI10nTranslations(requiredProps) {

        requiredProps.each { default_prop ->

            Map<String, Object> map = [
                    token   : default_prop.name['en'],
                    category: default_prop.descr['en'],
                    type    : default_prop.type,
                    hardData: BOOTSTRAP,
                    rdc     : default_prop.cat,
                    multiple: default_prop.multiple,
                    logic   : default_prop.isUsedForLogic,
                    tenant  : default_prop.tenant,
                    i10n    : [
                            name_de: default_prop.name?.trim(),
                            name_en: default_prop.name?.trim(),
                            expl_de: default_prop.expl?.trim(),
                            expl_en: default_prop.expl?.trim()
                    ]
            ]
            PropertyDefinition.construct(map)
        }
    }

    /**
     * Assigns to the given role the given permission
     * @param role the {@link Role} whose permissions should be granted
     * @param perm the {@link Perm} permission to be granted
     */
    void ensurePermGrant(Role role, Perm perm) {
        PermGrant existingPermGrant = PermGrant.findByRoleAndPerm(role,perm)
        if (! existingPermGrant) {
            //log.debug("create new perm grant for ${role}, ${perm}")
            new PermGrant(role:role, perm:perm).save()
        }
        else {
            //log.debug("grant already exists ${role}, ${perm}")
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

        List rdcList = getParsedCsvData('setup/RefdataCategory.csv', 'RefdataCategory')

        rdcList.each { map ->
            RefdataCategory.construct(map)
        }

        List rdvList = getParsedCsvData('setup/RefdataValue.csv', 'RefdataValue')

        rdvList.each { map ->
            RefdataValue.construct(map)
        }
    }

    /**
     * Processes the hard coded property definition source and updates the property definitions
     * @see PropertyDefinition
     */
    void setupPropertyDefinitions() {

        List pdList = getParsedCsvData('setup/PropertyDefinition.csv', 'PropertyDefinition')

        pdList.each { map ->
            PropertyDefinition.construct(map)
        }
    }

    @Deprecated
    void setupOnixPlRefdata() {

        // Refdata values that need to be added to the database to allow ONIX-PL licenses to be compared properly. The code will
        // add them to the DB if they don't already exist.
        Map<String, List> refdatavalues = [
                "User" : [ "Authorized User", "ExternalAcademic", "ExternalLibrarian", "ExternalStudent",
                           "ExternalTeacher", "ExternalTeacherInCountryOfLicensee", "LibraryUserUnaffiliated", "Licensee",
                           "LicenseeAlumnus", "LicenseeAuxiliary", "LicenseeContractor", "LicenseeContractorOrganization",
                           "LicenseeContractorStaff", "LicenseeDistanceLearningStudent", "LicenseeExternalStudent", "LicenseeFaculty",
                           "LicenseeInternalStudent", "LicenseeLibrary", "LicenseeLibraryStaff", "LicenseeNonFacultyStaff",
                           "LicenseeResearcher", "LicenseeRetiredStaff", "LicenseeStaff", "LicenseeStudent", "LoansomeDocUser",
                           "OtherTeacherOfAuthorizedUsers", "RegulatoryAuthority", "ResearchSponsor", "ThirdParty", "ThirdPartyLibrary",
                           "ThirdPartyNonCommercialLibrary", "ThirdPartyOrganization", "ThirdPartyPerson", "WalkInUser" ],

                "UsedResource" : ["AcademicPaper", "AcademicWork", "AcademicWorkIncludingLicensedContent",
                                  "AcknowledgmentOfSource", "AuthoredContent", "AuthoredContentPeerReviewedCopy", "AuthorizedUserOwnWork",
                                  "CatalogOrInformationSystem", "CombinedWorkIncludingLicensedContent", "CompleteArticle", "CompleteBook",
                                  "CompleteChapter", "CompleteIssue", "CopyrightNotice", "CopyrightNoticesOrDisclaimers",
                                  "CoursePackElectronic", "CoursePackPrinted", "CourseReserveElectronic", "CourseReservePrinted",
                                  "DataFromLicensedContent", "DerivedWork", "DigitalInstructionalMaterial",
                                  "DigitalInstructionalMaterialIncludingLicensedContent",
                                  "DigitalInstructionalMaterialWithLinkToLicensedContent", "DownloadedLicensedContent",
                                  "ImagesInLicensedContent", "LicensedContent", "LicensedContentBriefExcerpt", "LicensedContentMetadata",
                                  "LicensedContentPart", "LicensedContentPartDigital", "LicensedContentPartPrinted", "LicenseeContent",
                                  "LicenseeWebsite", "LinkToLicensedContent", "MaterialForPresentation", "PersonalPresentationMaterial",
                                  "PrintedInstructionalMaterial", "SpecialNeedsInstructionalMaterial", "ThirdPartyWebsite",
                                  "TrainingMaterial", "UserContent", "UserWebsite"]
        ]

        refdatavalues.each { String rdc, List<String> rdvList ->
            rdvList.each { String rdv ->

                Map<String, Object> map = [
                        token   : rdv,
                        rdc     : rdc,
                        hardData: BOOTSTRAP,
                        i10n    : [value_de: rdv, value_en: rdv]
                ]

                RefdataValue.construct(map)
            }
        }

        // copied from Config.groovy .. END

        // -------------------------------------------------------------------
        // ONIX-PL Additions
        // -------------------------------------------------------------------

        /*
        RefdataCategory.loc('Entitlement Issue Status',
                [en: 'Entitlement Issue Status', de: 'Entitlement Issue Status'], BOOTSTRAP)

        RefdataValue.loc('Entitlement Issue Status', [en: 'Live', de: 'Live'], BOOTSTRAP)
        RefdataValue.loc('Entitlement Issue Status', [en: 'Current', de: 'Current'], BOOTSTRAP)
        RefdataValue.loc('Entitlement Issue Status', [en: 'Deleted', de: 'Deleted'], BOOTSTRAP)
        */

        // Controlled values from the <UsageType> element.

        List<String> usageStatusList = [
                'UseForDataMining', 'InterpretedAsPermitted', 'InterpretedAsProhibited',
                'Permitted', 'Prohibited', 'SilentUninterpreted', 'NotApplicable'
        ]
        usageStatusList.each { String token ->
            RefdataValue.construct( [token: token, rdc: RDConstants.USAGE_STATUS, hardData: BOOTSTRAP, i10n:[value_de: token, value_en: token]] )
        }

//        RefdataCategory.lookupOrCreate(RDConstants.USAGE_STATUS, 'greenTick',      'UseForDataMining')
//        RefdataCategory.lookupOrCreate(RDConstants.USAGE_STATUS, 'greenTick',      'InterpretedAsPermitted')
//        RefdataCategory.lookupOrCreate(RDConstants.USAGE_STATUS, 'redCross',       'InterpretedAsProhibited')
//        RefdataCategory.lookupOrCreate(RDConstants.USAGE_STATUS, 'greenTick',      'Permitted')
//        RefdataCategory.lookupOrCreate(RDConstants.USAGE_STATUS, 'redCross',       'Prohibited')
//        RefdataCategory.lookupOrCreate(RDConstants.USAGE_STATUS, 'purpleQuestion', 'SilentUninterpreted')
//        RefdataCategory.lookupOrCreate(RDConstants.USAGE_STATUS, 'purpleQuestion', 'NotApplicable')

        // def gokb_record_source = GlobalRecordSource.findByIdentifier('gokbPackages') ?: new GlobalRecordSource(
        //                                                                                       identifier:'gokbPackages',
        //                                                                                       name:'GOKB',
        //                                                                                       type:'OAI',
        //                                                                                       haveUpTo:null,
        //                                                                                       uri:'https://gokb.kuali.org/gokb/oai/packages',
        //                                                                                       listPrefix:'oai_dc',
        //                                                                                       fullPrefix:'gokb',
        //                                                                                       principal:null,
        //                                                                                       credentials:null,
        //                                                                                       rectype:0)
        // gokb_record_source.save(flush:true, stopOnError:true)
        // log.debug("new gokb record source: ${gokb_record_source}")
    }

    @Deprecated
    void setupContentItems() {

        // The default template for a property change on a title
        ContentItem.lookupOrCreate ('ChangeNotification.TitleInstance.propertyChange','', '''
Title change - The <strong>${evt.prop}</strong> field was changed from  "<strong>${evt.oldLabel?:evt.old}</strong>" to "<strong>${evt.newLabel?:evt.new}</strong>".
''')

        ContentItem.lookupOrCreate ('ChangeNotification.TitleInstance.identifierAdded','', '''
An identifier was added to title ${OID?.title}.
''')

        ContentItem.lookupOrCreate ('ChangeNotification.TitleInstance.identifierRemoved','', '''
An identifier was removed from title ${OID?.title}.
''')

        ContentItem.lookupOrCreate ('ChangeNotification.TitleInstancePackagePlatform.updated','', '''
TIPP change for title ${OID?.title?.title} - The <strong>${evt.prop}</strong> field was changed from  "<strong>${evt.oldLabel?:evt.old}</strong>" to "<strong>${evt.newLabel?:evt.new}</strong>".
''')

        ContentItem.lookupOrCreate ('ChangeNotification.TitleInstancePackagePlatform.added','', '''
TIPP Added for title ${OID?.title?.title} ${evt.linkedTitle} on platform ${evt.linkedPlatform} .
''')

        ContentItem.lookupOrCreate ('ChangeNotification.TitleInstancePackagePlatform.deleted','', '''
TIPP Deleted for title ${OID?.title?.title} ${evt.linkedTitle} on platform ${evt.linkedPlatform} .
''')

        ContentItem.lookupOrCreate ('ChangeNotification.Package.created','', '''
New package added with id ${OID.id} - "${OID.name}".
''')

        ContentItem.lookupOrCreate ('kbplus.noHostPlatformURL','', '''
No Host Platform URL Content
''')
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
            [ns: "DBS-ID", name_de: "DBS-ID", description_de: "ID in der Deutschen und Österreichischen Bibliotheksstatistik (DBS/ÖBS) (https://www.bibliotheksstatistik.de/).", name_en: "DBS-ID", description_en: "ID in the German and Austrian library statistic (DBS/ÖBS) (https://www.bibliotheksstatistik.de/).", nsType: IdentifierNamespace.NS_ORGANISATION, urlPrefix: null, isUnique: false, isHidden: false],
            [ns: "dbis_org_id", name_de: "DBIS-Organisations-ID", description_de: "ID Ihrer Bibliothek oder Einrichtung im DBIS System, typischerweise ein Kürzel mit mehreren Buchstaben, z.B. 'ub_r'. Derzeit z.B. über die URL der eigenen Einrichtung auslesbar.", name_en: "DBIS organisation ID", description_en: "ID of your library or organisation in the DBIS system, typically an abbreviation with several letters, e.g. 'ub_r'. It may be read off currently from the URL of your own institution for example.", nsType: IdentifierNamespace.NS_ORGANISATION, urlPrefix: "https://dbis.ur.de//fachliste.php?bib_id=", isUnique: false, isHidden: false],
            [ns: "dbis_res_id", name_de: "DBIS-Ressourcen-ID", description_de: "ID für eine Datenbank oder allgemein Ressource im DBIS-System, die Sie z.B. mit einer Lizenz verknüpfen können.", name_en: "DBIS resource ID", description_en: "ID for a database or generally a resource in the DBIS system what you may link to a subscription for example.",  nsType: IdentifierNamespace.NS_SUBSCRIPTION, urlPrefix: "https://dbis.uni-regensburg.de/frontdoor.php?titel_id=", isUnique: false, isHidden: false],
            [ns: "DNB_ID", name_de: "DNB-ID", description_de: "Identifikator der Deutschen Nationalbibliothek.", name_en: "DNB-ID", description_en: "Identifier of the German National Library (DNB).", nsType: IdentifierNamespace.NS_SUBSCRIPTION, urlPrefix: "http://d-nb.info/", isUnique: false, isHidden: false],
            [ns: "eissn", name_de: "E-ISSN", description_de: "Internationale Standardnummer(n) für fortlaufende Sammelwerke.", name_en: "E-ISSN", description_en: "International standard number(s) for continued series.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "eduPersonEntitlement", name_de: "Entitlement KfL-Portal", description_de: "das Shibboleth-Entitlement", name_en: "Entitlement KfL portal", description_en: "the Shibboleth entitlement", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "EZB anchor", name_de: "EZB-Anker", description_de: "EZB-Anker werden von Konsortialverwaltern für deren jeweilige angelegte Kollektionen vergeben, um so die genaue Lizenzeinheit abbilden zu können.", name_en: "EZB anchor", description_en: "EZB anchors are distributed by consortia managers for their respective collections in order to represent the subscription unit.", nsType:null, isUnique: false, isHidden: false],
            [ns: "ezb_collection_id", name_de: "EZB-Kollektions-ID", description_de: "Automatisch vergebene ID. Beim Abruf frei verfügbarer Titellisten der jeweiligen Kollektionen dient die Kollektions-ID als eindeutiger Identifikator.", name_en: "EZB collection id", description_en: "Automatically distributed ID. The collection ID serves as unique identifier upon a call of the entitlement lists of the collections.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, urlPrefix: "http://ezb.ur.de/api/collections/", isUnique: false, isHidden: false],
            [ns: "ezb_org_id", name_de: "EZB-ID", description_de: "Identifkator der Elektronischen Zeitschriftendatenbank (EZB). Mehrfachangabe möglich.", name_en: "EZB-ID", description_en: "Identifier of Electronic Journals Library (EZB). Multiple insertion possible.", nsType: IdentifierNamespace.NS_ORGANISATION, isUnique: false, isHidden: false],
            [ns: "ezb_sub_id", name_de: "EZB-ID", description_de: "Identnummer, die einen bestimmten Eintrag in der Elektronischen Zeitschriftenbibliothek EZB auszeichnet.", name_en: "EZB-ID", description_en: "Identification number which indicates a certain entry in the Electronic Journals Library (EZB).", nsType: IdentifierNamespace.NS_SUBSCRIPTION, urlPrefix: "http://rzbvm017.uni-regensburg.de/ezeit/detail.phtml?bibid=AAAAA&colors=7&lang=de&jour_id=", isUnique: false, isHidden: false],
            [ns: "gnd_org_nr", name_de: "GND-NR", description_de: "Eindeutiger und stabiler Bezeichner für jede einzelne Entität in der GND (Gemeinsame Normdatei). https://www.dnb.de/DE/Professionell/Standardisierung/GND/gnd_node.html", name_en: "GND-NR", description_en: "Unique and stable identifier for every entity in the GND (Integrated Authority File). https://www.dnb.de/EN/Professionell/Standardisierung/GND/gnd_node.html", nsType: IdentifierNamespace.NS_ORGANISATION, urlPrefix: "https://d-nb.info/gnd/", isUnique: false, isHidden: false],
            [ns: "GRID ID", name_de: "GRID-ID", description_de: "Identifikator einer Forschungsinstitution in der Datenbank Global-Research-Identifier-Database (https://grid.ac).", name_en: "GRID-ID", description_en: "Identifier of a research institution in the Global-Research-Identifier-Database (https://www.grid.ac/).", nsType: IdentifierNamespace.NS_ORGANISATION, urlPrefix: "https://grid.ac/institutes/", isUnique: false, isHidden: false],
            [ns: "ISBN", name_de: "ISBN", description_de: "Internationale Standardbuchnummer.", name_en: "ISIL", description_en: "International standard book number.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "ISIL", name_de: "ISIL", description_de: null, name_en: "ISIL", description_en: null, nsType: IdentifierNamespace.NS_ORGANISATION, isUnique: false, isHidden: false],
            [ns: "ISIL Paketsigel", name_de: "ZDB-Paketsigel", description_de: "ZDB-Produktkennzeichnung für (Gesamt)pakete, vergeben von der ISIL-Agentur.", name_en: "ISIL package identifier", description_en: "ZDB product marking for (whole) packages, distributed by the ISIL agency.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, urlPrefix: "https://sigel.staatsbibliothek-berlin.de/suche/?isil=", isUnique: false, isHidden: false],
            [ns: "isil_product", name_de: "ZDB-Produktsigel", description_de: "ZDB-Produktsigel für Teilpakete, vergeben von der ISIL-Agentur.", name_en: "ISIL product identifier", description_en: "ZDB product marking for partial packages, distributed by the ISIL agency.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, urlPrefix: "https://sigel.staatsbibliothek-berlin.de/suche/?isil=", isUnique: false, isHidden: false],
            [ns: "pissn", name_de: "P-ISSN", description_de: "Internationale Standardnummer(n) für fortlaufende Sammelwerke.", name_en: "P-ISSN", description_en: "International standard number(s) for continued series.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "Rechnungssystem_Nummer", name_de: "Rechnungssystem-Nr.", description_de: "Individuelle, interne Rechnungsnummer, vergeben von der einzelnen Einrichtung.", name_en: "Invoice system number", description_en: "Unique internal invoice system number, assigned by the respective institution.", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "SFX-Anker", name_de: "SFX-Anker", description_de: "Eintrag im Linkresolver SFX (Ex Libris Group).", name_en: "SFX anchor", description_en: "Entry in the Linkresolver SFX (Ex Libris Group).", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "sis_kfl_proof", name_de: "Nachweis im KfL-Portal", description_de: "Nachweis im KfL-Portal", name_en: "Proof in KfL portal", description_en: "Proof in KfL portal", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "fid_negotiator_id", name_de: "FID-Verhandlungs-ID", "description_de": null, "name_en": "SIS negotiator ID", description_en: null, nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false], //when creating the namespace, I did not knew that DFG provided a translation for German Fachonformationsdienste (Specialised Information Services)
            [ns: "sis_nl_proof", name_de: "Nachweis im NL-Portal", description_de: "Nachweis im NL-Portal", name_en: "Proof in NL portal", description_en: "Proof in NL portal", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: false, isHidden: false],
            [ns: "sis_product_id", name_de: "FID-Produkt-ID", description_de: "eindeutige FID-Produktkennung", name_en: "SIS product ID", description_en: "unique SIS product marking", nsType: IdentifierNamespace.NS_SUBSCRIPTION, isUnique: true, isHidden: false],
            [ns: "wibid", name_de: "WIB-ID", description_de: "Identifikator, den Sie bei der Registrierung auf nationallizenzen.de erhalten.", name_en: "WIB-ID", description_en: "The identifier you received upon registration on nationallizenzen.de.", nsType: IdentifierNamespace.NS_ORGANISATION, isUnique: false, isHidden: false],
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

    /**
     * Analyses huge tables for better query execution planning.
     * Unused, may be activated for local testing
     */
    void vacuumAndAnalyseTables() {
        Session sess = sessionFactory.currentSession
        sess.doWork(new Work() {
            void execute(Connection connection) throws SQLException {
                Statement stmt = connection.createStatement()
                stmt.execute('analyze pending_change')
                stmt.execute('analyze issue_entitlement')
                stmt.execute('analyze issue_entitlement_coverage')
                stmt.execute('analyze title_instance_package_platform')
                stmt.execute('analyze tippcoverage')
                stmt.execute('analyze counter4report')
                stmt.execute('analyze counter5report')
            }
        })
    }
}