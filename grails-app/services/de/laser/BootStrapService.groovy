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
import org.hibernate.type.TextType

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

    final static BOOTSTRAP = true   // indicates this object is created via bootstrap

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

        // def auto_approve_memberships = SystemSetting.findByName('AutoApproveMemberships') ?: new SystemSetting(name: 'AutoApproveMemberships', tp: SystemSetting.CONTENT_TYPE_BOOLEAN, defvalue: 'true', value: 'true').save()

        SystemSetting mailSent = SystemSetting.findByName('MailSentDisabled')

        if(mailSent){
            mailSent.delete()
        }

        SystemSetting.findByName('MaintenanceMode') ?: new SystemSetting(name: 'MaintenanceMode', tp: SystemSetting.CONTENT_TYPE_BOOLEAN, defvalue: 'false', value: 'false').save()

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
            return it?.format("yyyy-MM-dd'T'HH:mm:ss'Z'")
        }

        log.debug("adjustDatabasePermissions ..")
        adjustDatabasePermissions()

        log.debug(" .__                            .________ ")
        log.debug(" |  | _____    ______ ___________\\_____  \\ ~ grails3")
        log.debug(" |  | \\__  \\  /  ___// __ \\_  __ \\/  ____/ ")
        log.debug(" |  |__/ __ \\_\\___ \\\\  ___/|  | \\/       \\ ")
        log.debug(" |____(____  /____  >\\___  >__|  \\_______ \\  ")
        log.debug("           \\/     \\/     \\/              \\/ ")
    }

    void destroy() {}

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
                                        formalRole: role,
                                        status: UserOrg.STATUS_APPROVED,
                                        dateRequested: System.currentTimeMillis(),
                                        dateActioned: System.currentTimeMillis()

                                ).save(failOnError: true)
                            }
                        }
                    }
                }
            }
        }
    }

    void setupAdminUsers() {

        if (ServerUtils.getCurrentServer() == ServerUtils.SERVER_QA) {
            log.debug("check if all user accounts are existing on QA ...")

            Map<String,Org> modelOrgs = [konsorte: Org.findByName('Musterkonsorte'),
                                         institut: Org.findByName('Musterinstitut'),
                                         singlenutzer: Org.findByName('Mustereinrichtung'),
                                         kollektivnutzer: Org.findByName('Mustereinrichtung Kollektiv'),
                                         konsortium: Org.findByName('Musterkonsortium')]

            Map<String,Org> testOrgs = [konsorte: Org.findByName('Testkonsorte'),
                                        institut: Org.findByName('Testinstitut'),
                                        singlenutzer: Org.findByName('Testeinrichtung'),
                                        kollektivnutzer: Org.findByName('Testeinrichtung Kollektiv'),
                                        konsortium: Org.findByName('Testkonsortium')]

            Map<String,Org> QAOrgs = [konsorte: Org.findByName('QA-Konsorte'),
                                      institut: Org.findByName('QA-Institut'),
                                      singlenutzer: Org.findByName('QA-Einrichtung'),
                                      kollektivnutzer: Org.findByName('QA-Einrichtung Kollektiv'),
                                      konsortium: Org.findByName('QA-Konsortium')]

            userService.setupAdminAccounts(modelOrgs)
            userService.setupAdminAccounts(testOrgs)
            userService.setupAdminAccounts(QAOrgs)
        }
        else {
            log.debug('.. skipped')
        }
    }

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
        Role orgSingleRole           = locOrgRole('ORG_INST',               'org', [en: 'Institution basic', de: 'Singlenutzer'])
        Role orgCollectiveRole       = locOrgRole('ORG_INST_COLLECTIVE',    'org', [en: 'Institution collective', de: 'Kollektivnutzer'])
        Role orgConsortiumRole       = locOrgRole('ORG_CONSORTIUM',         'org', [en: 'Consortium basic', de: 'Konsortium mit Umfragefunktion'])

        createOrgPerms(fakeRole,                    ['FAKE'])
        createOrgPerms(orgMemberRole,               ['ORG_BASIC_MEMBER'])
        createOrgPerms(orgSingleRole,               ['ORG_INST', 'ORG_BASIC_MEMBER'])
        createOrgPerms(orgCollectiveRole,           ['ORG_INST_COLLECTIVE', 'ORG_INST', 'ORG_BASIC_MEMBER'])
        createOrgPerms(orgConsortiumRole,           ['ORG_CONSORTIUM'])
    }

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

    void adjustDatabasePermissions() {

        Sql sql = new Sql(dataSource)
        sql.rows("SELECT * FROM grants_for_maintenance()")
    }

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

    void setupPropertyDefinitions() {

        List pdList = getParsedCsvData('setup/PropertyDefinition.csv', 'PropertyDefinition')

        pdList.each { map ->
            PropertyDefinition.construct(map)
        }
    }

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

    void setIdentifierNamespace() {

        List namespaces = [
                [ns: "ISIL"],
                [ns: "ISIL_Paketsigel"],
                [ns: "uri"],
                [ns: "zdb"],
                [ns: "zdb_ppn"],
                [ns: "VIAF"],
                [ns: "issn"],
                [ns: "eissn"]
        ]

        namespaces.each { Map<String, String> namespaceProperties ->
            String namespace = namespaceProperties["ns"]
            String typ = namespaceProperties["typ"] ?: null
            //TODO isUnique/isHidden flags are set provisorically to "false", adaptations may be necessary
            IdentifierNamespace.findByNsIlike(namespace) ?: new IdentifierNamespace(ns: namespace, nsType: typ, isUnique: false, isHidden: false, isFromLaser: true).save()
        }
    }
}