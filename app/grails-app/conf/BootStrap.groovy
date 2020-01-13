import au.com.bytecode.opencsv.CSVReader
import com.k_int.kbplus.*

import com.k_int.kbplus.auth.*
import com.k_int.properties.PropertyDefinition
import de.laser.ContextService
import de.laser.SystemEvent
import de.laser.domain.I10nTranslation
import grails.converters.JSON
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import org.hibernate.type.TextType
import groovy.sql.Sql

class BootStrap {

    def grailsApplication
    def dataloadService
    def apiService
    def refdataReorderService
    def sessionFactory
    def organisationService
    def dataSource
    def userService

    //  indicates this object is created via current bootstrap
    final static BOOTSTRAP = true

    final static OT = [
            Date:   Date.toString(),
            Int:    Integer.toString(),
            RdC:    RefdataCategory.toString(),
            Rdv:    RefdataValue.toString(),
            String: String.toString(),
            URL:    URL.toString()
    ]

    def init = { servletContext ->

        log.info("SystemId: ${grailsApplication.config.laserSystemId}")
        log.info("Database: ${grailsApplication.config.dataSource.url}")
        log.info("Database datasource dbCreate: ${grailsApplication.config.dataSource.dbCreate}")
        log.info("Database migration plugin updateOnStart: ${grailsApplication.config.grails.plugin.databasemigration.updateOnStart}")
        log.info("Documents: ${grailsApplication.config.documentStorageLocation}")

        log.info("--------------------------------------------------------------------------------")

        if (grailsApplication.config.laserSystemId != null) {
            def system_object = SystemObject.findBySysId(grailsApplication.config.laserSystemId) ?: new SystemObject(sysId: grailsApplication.config.laserSystemId).save(flush: true)
        }

        SystemEvent.createEvent('BOOTSTRAP_STARTUP')

        // Reset harddata flag for given refdata and properties

        RefdataValue.executeUpdate('UPDATE RefdataValue rdv SET rdv.isHardData =:reset', [reset: false])
        RefdataCategory.executeUpdate('UPDATE RefdataCategory rdc SET rdc.isHardData =:reset', [reset: false])
        PropertyDefinition.executeUpdate('UPDATE PropertyDefinition pd SET pd.isHardData =:reset', [reset: false])

        // Here we go ..

        log.debug("updatePsqlRoutines ..")
        updatePsqlRoutines()

        // --

        log.debug("setupRefdata ..")
        setupRefdata()

        log.debug("reorderRefdata ..")
        refdataReorderService.reorderRefdata()

        log.debug("setupRolesAndPermissions ..")
        setupRolesAndPermissions()

        log.debug("setupSystemUsers ..")
        setupSystemUsers()

        log.debug("setupAdminUsers ..")
        setupAdminUsers()

        if (UserOrg.findAllByFormalRoleIsNull()?.size() > 0) {
            log.warn("there are user org rows with no role set. Please update the table to add role FKs")
        }

        log.debug("setupTransforms ..")
        setupTransforms()

        // def auto_approve_memberships = Setting.findByName('AutoApproveMemberships') ?: new Setting(name: 'AutoApproveMemberships', tp: Setting.CONTENT_TYPE_BOOLEAN, defvalue: 'true', value: 'true').save()

        def mailSent = Setting.findByName('MailSentDisabled') ?: new Setting(name: 'MailSentDisabled', tp: Setting.CONTENT_TYPE_BOOLEAN, defvalue: 'false', value: (grailsApplication.config.grails.mail.disabled ?: "false")).save()

        //def maintenance_mode = Setting.findByName('MaintenanceMode') ?: new Setting(name: 'MaintenanceMode', tp: Setting.CONTENT_TYPE_BOOLEAN, defvalue: 'false', value: 'false').save()

        def systemMessage = SystemMessage.findByText('Das System wird in den nächsten Minuten aktualisiert. Bitte pflegen Sie keine Daten mehr ein!') ?: new SystemMessage(text: 'Das System wird in den nächsten Minuten aktualisiert. Bitte pflegen Sie keine Daten mehr ein!', showNow: false).save()

        // SpringSecurityUtils.clientRegisterFilter( 'oracleSSOFilter', SecurityFilterPosition.PRE_AUTH_FILTER.order)
        // SpringSecurityUtils.clientRegisterFilter('securityContextPersistenceFilter', SecurityFilterPosition.PRE_AUTH_FILTER)
        //SpringSecurityUtils.clientRegisterFilter('ediauthFilter', SecurityFilterPosition.PRE_AUTH_FILTER)
        //SpringSecurityUtils.clientRegisterFilter('apiauthFilter', SecurityFilterPosition.SECURITY_CONTEXT_FILTER.order + 10)
        SpringSecurityUtils.clientRegisterFilter('apiFilter', SecurityFilterPosition.BASIC_AUTH_FILTER)

        log.debug("setOrgRoleGroups ..")
        setOrgRoleGroups()

        log.debug("setupOnixPlRefdata ..")
        setupOnixPlRefdata()

        log.debug("setupContentItems ..")
        setupContentItems()

        //log.debug("createOrgConfig ..")
        //createOrgConfig()

        log.debug("createOrgProperties ..")
        createOrgProperties()

        log.debug("createLicenseProperties ..")
        createLicenseProperties()

        log.debug("createPlatformProperties ..")
        createPlatformProperties()

        log.debug("createSubscriptionProperties ..")
        createSubscriptionProperties()

        log.debug("createSurveyProperties ..")
        createSurveyProperties()

        //log.debug("createPrivateProperties ..")
        //createPrivateProperties()

        log.debug("setIdentifierNamespace ..")
        setIdentifierNamespace()

        log.debug("checking database ..")

        if (!Org.findAll() && !Person.findAll() && !Address.findAll() && !Contact.findAll()) {
            log.debug("database is probably empty; setting up essential data ..")
            File f = new File("${grailsApplication.config.basicDataPath}${grailsApplication.config.basicDataFileName}")
            if(f.exists())
                apiService.setupBasicData(f)
            else {
                organisationService.createOrgsFromScratch()
            }
        }

        //log.debug("initializeDefaultSettings ..")
        //initializeDefaultSettings()

//        log.debug("setESGOKB ..")
//        setESGOKB()

        log.debug("setJSONFormatDate ..")

        JSON.registerObjectMarshaller(Date) {
            return it?.format("yyyy-MM-dd'T'HH:mm:ss'Z'")
        }

        log.debug("adjustDatabasePermissions ..")
        adjustDatabasePermissions()

        log.debug(" _                                               ")
        log.debug("| |_ ___ ___ ___    _ _ _ ___    ___ ___         ")
        log.debug("|   | -_|  _| -_|  | | | | -_|  | . | . |  _ _   ")
        log.debug("|_|_|___|_| |___|  |_____|___|  |_  |___| |_|_|  ")
        log.debug("                                |___|            ")
    }

    def destroy = {
    }

    def setupSystemUsers = {

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
                    email: 'laser_support@hbz-nrw.de',
                    enabled: false
            ).save(failOnError: true)

                def role = Role.findByAuthority('ROLE_USER')
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
                        def role = Role.findByAuthority(r)
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
                                UserOrg userOrg = new UserOrg(
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

    def setupAdminUsers = {

        if (grailsApplication.config.getCurrentServer() == ContextService.SERVER_QA) {
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

    def setupRolesAndPermissions = {

        // seting perm grants to current state

        PermGrant.findAll().each { it.delete(flush:true) }

        // Permissions

        def edit_permission = Perm.findByCode('edit') ?: new Perm(code: 'edit').save(failOnError: true)
        def view_permission = Perm.findByCode('view') ?: new Perm(code: 'view').save(failOnError: true)

        // TODO: refactoring: partOf

        // Global System Roles

        Role yodaRole    = Role.findByAuthority('ROLE_YODA')        ?: new Role(authority: 'ROLE_YODA', roleType: 'transcendent').save(failOnError: true)
        Role adminRole   = Role.findByAuthority('ROLE_ADMIN')       ?: new Role(authority: 'ROLE_ADMIN', roleType: 'global').save(failOnError: true)
        //Role dmRole      = Role.findByAuthority('ROLE_DATAMANAGER') ?: new Role(authority: 'ROLE_DATAMANAGER', roleType: 'global').save(failOnError: true)
        Role userRole    = Role.findByAuthority('ROLE_USER')        ?: new Role(authority: 'ROLE_USER', roleType: 'global').save(failOnError: true)
        Role apiRole     = Role.findByAuthority('ROLE_API')         ?: new Role(authority: 'ROLE_API', roleType: 'global').save(failOnError: true)

        Role globalDataRole    = Role.findByAuthority('ROLE_GLOBAL_DATA')        ?: new Role(authority: 'ROLE_GLOBAL_DATA', roleType: 'global').save(failOnError: true)
        Role orgEditorRole     = Role.findByAuthority('ROLE_ORG_EDITOR')         ?: new Role(authority: 'ROLE_ORG_EDITOR', roleType: 'global').save(failOnError: true)
        //Role orgComRole        = Role.findByAuthority('ROLE_ORG_COM_EDITOR')     ?: new Role(authority: 'ROLE_ORG_COM_EDITOR', roleType: 'global').save(failOnError: true)
        Role packageEditorRole = Role.findByAuthority('ROLE_PACKAGE_EDITOR')     ?: new Role(authority: 'ROLE_PACKAGE_EDITOR', roleType: 'global').save(failOnError: true)
        Role statsEditorRole   = Role.findByAuthority('ROLE_STATISTICS_EDITOR')  ?: new Role(authority: 'ROLE_STATISTICS_EDITOR', roleType: 'global').save(failOnError: true)
        Role ticketEditorRole  = Role.findByAuthority('ROLE_TICKET_EDITOR')      ?: new Role(authority: 'ROLE_TICKET_EDITOR', roleType: 'global').save(failOnError: true)

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

            permList.each{ code ->
                code = code.toLowerCase()
                Perm perm = Perm.findByCode(code) ?: new Perm(code: code).save(failOnError: true)
                ensurePermGrant(role, perm)
            }
        }

        Role fakeRole                = locOrgRole('FAKE',                   'fake', [en: 'Fake', de: 'Fake'])
        Role orgMemberRole           = locOrgRole('ORG_BASIC_MEMBER',       'org', [en: 'Institution consortium member', de: 'Konsorte'])
        Role orgSingleRole           = locOrgRole('ORG_INST',               'org', [en: 'Institution basic', de: 'Singlenutzer'])
        Role orgCollectiveRole       = locOrgRole('ORG_INST_COLLECTIVE',    'org', [en: 'Institution collective', de: 'Kollektivnutzer'])
        Role orgConsortiumRole       = locOrgRole('ORG_CONSORTIUM',         'org', [en: 'Consortium basic', de: 'Konsortium ohne Umfragefunktion'])
        Role orgConsortiumSurveyRole = locOrgRole('ORG_CONSORTIUM_SURVEY',  'org', [en: 'Consortium survey', de: 'Konsortium mit Umfragefunktion'])

        createOrgPerms(fakeRole,                    ['FAKE'])
        createOrgPerms(orgMemberRole,               ['ORG_BASIC_MEMBER'])
        createOrgPerms(orgSingleRole,               ['ORG_INST', 'ORG_BASIC_MEMBER'])
        createOrgPerms(orgCollectiveRole,           ['ORG_INST_COLLECTIVE', 'ORG_INST', 'ORG_BASIC_MEMBER'])
        createOrgPerms(orgConsortiumRole,           ['ORG_CONSORTIUM'])
        createOrgPerms(orgConsortiumSurveyRole,     ['ORG_CONSORTIUM_SURVEY', 'ORG_CONSORTIUM'])
    }

    List getParsedCsvData(String filePath, String objType) {

        List result = []
        File csvFile = grailsApplication.mainContext.getResource(filePath).file

        if (! ['RefdataCategory', 'RefdataValue' /*, 'PropertyDefinition' */].contains(objType)) {
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
                                    token   : "${line[0].trim()}",
                                    hardData: BOOTSTRAP,
                                    i10n    : [de: "${line[1].trim()}", en: "${line[2].trim()}"]
                            ]
                            result.add(map)
                        }
                        if (objType == 'RefdataValue') {
                            // CSV: [rdc, token, value_de, value_en]
                            Map<String, Object> map = [
                                    token   : "${line[1].trim()}",
                                    rdc     : "${line[0].trim()}",
                                    hardData: BOOTSTRAP,
                                    i10n    : [de: "${line[2].trim()}", en: "${line[3].trim()}"]
                            ]
                            result.add(map)
                        }
                        /*
                        if (objType == 'PropertyDefinition') {
                            Map<String, Object> map = [
                                    token   : "${line[0].trim()}",
                                    hardData: true,
                                    i10n    : [en: "${line[2].trim()}", de: "${line[1].trim()}"]
                            ]
                            result.add(map)
                        }*/
                    }
                }
            }
        }

        result
    }


    def setupTransforms = {

        // Transforms types and formats Refdata
        // !!! HAS TO BE BEFORE the script adding the Transformers as it is used by those tables !!!

        // Add Transformers and Transforms defined in local config (laser-config.groovy)
        grailsApplication.config.systransforms.each { tr ->
            def transformName = tr.transforms_name //"${tr.name}-${tr.format}-${tr.type}"

            def transforms = Transforms.findByName("${transformName}")
            def transformer = Transformer.findByName("${tr.transformer_name}")
            if (transformer) {
                if (transformer.url != tr.url) {
                    log.debug("change transformer [${tr.transformer_name}] url to ${tr.url}")
                    transformer.url = tr.url;
                    transformer.save(failOnError: true, flush: true)
                } else {
                    log.debug("${tr.transformer_name} present and correct")
                }
            } else {
                log.debug("create transformer ${tr.transformer_name} ..")
                transformer = new Transformer(
                        name: tr.transformer_name,
                        url: tr.url).save(failOnError: true, flush: true)
            }

            log.debug("create transform ${transformName} ..")
            def types = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('Transform Type'))
            def formats = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('Transform Format'))

            if (transforms) {

                if (tr.type) {
                    // split values
                    def type_list = tr.type.split(",")
                    type_list.each { new_type ->
                        if (!transforms.accepts_types.any { f -> f.value == new_type }) {
                            log.debug("add transformer [${transformName}] type: ${new_type}")
                            def type = types.find { t -> t.value == new_type }
                            transforms.addToAccepts_types(type)
                        }
                    }
                }
                if (transforms.accepts_format.value != tr.format) {
                    log.debug("change transformer [${transformName}] format to ${tr.format}")
                    def format = formats.findAll { t -> t.value == tr.format }
                    transforms.accepts_format = format[0]
                }
                if (transforms.return_mime != tr.return_mime) {
                    log.debug("change transformer [${transformName}] return format to ${tr.'mime'}")
                    transforms.return_mime = tr.return_mime;
                }
                if (transforms.return_file_extention != tr.return_file_extension) {
                    log.debug("change transformer [${transformName}] return format to ${tr.'return'}")
                    transforms.return_file_extention = tr.return_file_extension;
                }
                if (transforms.path_to_stylesheet != tr.path_to_stylesheet) {
                    log.debug("change transformer [${transformName}] return format to ${tr.'path'}")
                    transforms.path_to_stylesheet = tr.path_to_stylesheet;
                }
                transforms.save(failOnError: true, flush: true)
            } else {
                def format = formats.findAll { t -> t.value == tr.format }

                assert format.size() == 1

                transforms = new Transforms(
                        name: transformName,
                        accepts_format: format[0],
                        return_mime: tr.return_mime,
                        return_file_extention: tr.return_file_extension,
                        path_to_stylesheet: tr.path_to_stylesheet,
                        transformer: transformer).save(failOnError: true, flush: true)

                def type_list = tr.type.split(",")
                type_list.each { new_type ->
                    def type = types.find { t -> t.value == new_type }
                    transforms.addToAccepts_types(type)
                }
            }
        }
    }

    def updatePsqlRoutines = {

        File dir

        try {
            def folder = this.class.classLoader.getResource('./migrations/functions')
            dir = new File(folder.file)
        }
        catch (Exception e) {
            log.warn(e)
            log.warn('fallback ..')

            String dirPath = grailsApplication.config.grails.plugin.databasemigration.changelogLocation + '/functions'
            dir = new File(dirPath)
        }

        if (dir.exists()) {
            log.debug('scanning ' + dir.getAbsolutePath())

            dir.listFiles().each { file ->
                String fileName = file.getName()
                if (fileName.endsWith('.sql')) {
                    String fileSql     = file.readLines().join(System.getProperty("line.separator")).trim()
                    String validateSql = "SELECT proname, REGEXP_MATCH(prosrc, 'VERSION CONSTANT NUMERIC = [0-9]*') FROM pg_proc WHERE proname = '" +
                            fileName.replace('.sql', '') + "'"

                    if (fileSql.take(26).equalsIgnoreCase('CREATE OR REPLACE FUNCTION')) {

                        try {
                            org.hibernate.SQLQuery query    = sessionFactory.currentSession.createSQLQuery(fileSql)
                            org.hibernate.SQLQuery validate = sessionFactory.currentSession
                                    .createSQLQuery(validateSql)
                                    .addScalar("REGEXP_MATCH", new TextType())

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

    def adjustDatabasePermissions = {

        Sql sql = new Sql(dataSource)
        sql.rows("SELECT * FROM grants_for_maintenance()")
    }

    @Deprecated
    def initializeDefaultSettings(){
        /*
        def admObj = SystemAdmin.list()
        if (! admObj) {
            log.debug("no SystemAdmin object found, creating new")
            admObj = new SystemAdmin(name:"demo").save()
        } else {
            admObj = admObj.first()
        }
        //Will not overwrite any existing database properties.
        createDefaultSysProps(admObj)
        admObj.refresh()
        log.debug("finished updating config from SystemAdmin")
        */
    }

    @Deprecated
    def createDefaultSysProps(admObj){
        /*
        def allDescr = [en: PropertyDefinition.SYS_CONF, de: PropertyDefinition.SYS_CONF]

        def requiredProps = [
                [name: [en: "onix_ghost_license"],
                    descr:allDescr, type: OT.String, val:"Jisc Collections Model Journals License 2015", note:"Default license used for comparison when viewing a single onix license."],
                [name: [en: "net.sf.jasperreports.export.csv.exclude.origin.keep.first.band.1"],
                    descr:allDescr, type: OT.String, val:"columnHeader", note:"Only show 1 column header for csv"],
                [name: [en: "net.sf.jasperreports.export.xls.exclude.origin.keep.first.band.1"],
                    descr:allDescr, type: OT.String, val:"columnHeader", note:"Only show 1 column header for xls"],
                [name: [en: "net.sf.jasperreports.export.xls.exclude.origin.band.1"],
                    descr:allDescr, type: OT.String, val:"pageHeader", note:" Remove header/footer from csv/xls"],
                [name: [en: "net.sf.jasperreports.export.xls.exclude.origin.band.2"],
                    descr:allDescr, type: OT.String, val:"pageFooter", note:" Remove header/footer from csv/xls"],
                [name: [en: "net.sf.jasperreports.export.csv.exclude.origin.band.1"],
                    descr:allDescr, type: OT.String, val:"pageHeader", note: " Remove header/footer from csv/xls"],
                [name: [en: "net.sf.jasperreports.export.csv.exclude.origin.band.2"],
                    descr:allDescr, type: OT.String, val:"pageFooter", note: " Remove header/footer from csv/xls"]
        ]

        requiredProps.each { prop ->
            def name = prop.name['en']
            def pd   = PropertyDefinition.findWhere(name: name, tenant: null)

            if (! pd) {
                log.debug("unable to locate property definition for ${name} .. creating")
                pd = new PropertyDefinition(name: name)
            }

            pd.type  = prop.type
            pd.descr = prop.descr['en']
            //pd.softData = false
            pd.isHardData = BOOTSTRAP
            pd.save(failOnError: true)

            if (! SystemAdminCustomProperty.findByType(pd)) {
                def newProp = new SystemAdminCustomProperty(type: pd, owner: admObj, stringValue: prop.val, note: prop.note)
                newProp.save()
            }
        }
        */
    }

    @Deprecated
    def createOrgConfig() {

        def allDescr = [en: PropertyDefinition.ORG_CONF, de: PropertyDefinition.ORG_CONF]

        def requiredProps = [
                [name: [en: "API Key", de: "API Key"],         descr:allDescr, type: OT.String],
                [name: [en: "RequestorID", de: "RequestorID"], descr:allDescr, type: OT.String],
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredProps)
    }

    def createOrgProperties() {

        def allDescr = [en: PropertyDefinition.ORG_PROP, de: PropertyDefinition.ORG_PROP]

        def requiredOrgProps = [
            [
                    name: [key: "TaxExemption", en: "TaxExemption", de: "Steuerbefreiung"],
                    expl : [en: "", de: "Liegt eine Steuerbefreiung für den Anbieter vor?"],
                    descr:allDescr, type: OT.Rdv, cat:'YN'
            ],
            [
                    name: [key: "Shibboleth Usage", en: "Shibboleth Usage", de: "Shibboleth: Nutzung für Bibliotheksangebote"],
                    expl : [en: "", de: "Nutzt die Organisation Shibboleth für Bibliotheksangebote?"],
                    descr:allDescr, type: OT.Rdv, cat:'YNU'
            ],
            [
                    name: [key: "Shibboleth Identity Provider Entity-ID", en: "Shibboleth Identity Provider Entity-ID", de: "Shibboleth: Identity Provider Entity-ID"],
                    expl : [en: "", de: "Wie lautet die Entity-ID der Organisation?"],
                    descr:allDescr, type: OT.String, multiple: true, isUsedForLogic: true
            ],

        ]
        createPropertyDefinitionsWithI10nTranslations(requiredOrgProps)
    }

    def createLicenseProperties() {

        def allDescr = [en: PropertyDefinition.LIC_PROP, de: PropertyDefinition.LIC_PROP]

        def requiredProps = [
            /*[
                    name: [en: "Agreement Date", de: "Abschlussdatum"],
                    expl: [en: "Date, when the agreement has been signed", de: "Abschlussdatum des Vertrags."],
                    descr:allDescr, type: OT.Date  // TODO: Should be deleted, but is in use
            ],*/
            [
                    name: [en: "All rights reserved indicator", de: "Hinweis auf 'Alle Rechte vorbehalten'"],
                    expl: [en: "A clause stating that all intellectual property rights not explicitly granted to the licensee are retained by the licensor.", de: ""],
                    descr:allDescr, type: OT.Rdv, cat:'YNO'
            ],
            [
                    name: [en: "Applicable copyright law", de: "Urheberrechtsgesetz"],
                    expl: [en: "A clause that specifies the national copyright law agreed to in the contract.", de: "Das anzuwendende Urheberrecht."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Authorized Users", de: "Autorisierte Nutzer"],
                    expl: [en: "The language in the contract that defines the group of users allowed to use the Electronic Product.", de: "Definition, welche Personenkreise Zugriff erhalten."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            /*[
                    name: [en: "Authorized user definition", de: "Autorisierte Nutzer, Definition"],
                    expl: [en: "The language in the contract that defines the group of users allowed to use the Electronic Product.", de: "Definition, welche Personenkreise Zugriff erhalten."],
                    descr:allDescr, type: OT.String // TODO: Should be deleted, but is in use.
            ],*/
            [
                    name: [en: "Local authorized user defintion", de: "Autorisierte Nutzer, lokale Defintion"],
                    expl: [en: "The inclusion of an institution-specific preferred authorized user definition", de: "Definition, welcher Personenkreis vor Ort zu den berechtigten Nutzern gezählt wird."],
                    descr:allDescr, type: OT.String
            ],
            [
                    name: [en: "Walk-in Access", de: "Walk-In User"],
                    expl: [en: "A group of people who have access to information whithout being e.g. a student at this University Library.", de: "Nutzergruppe, die zwar Zugang zu Informationen erhält, jedoch bspw. nicht Universitätsmitglied ist."],
                    descr:allDescr, type: OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [key: "walkinusertermnote", en: "Walk-in User Term Note", de: "Walk-In User Bedingungen"],
                    expl: [en: "Information which qualifies the status or permitted actions of Walk-In Users", de: "Nutzergruppe, die zwar Zugang zu Informationen erhält, jedoch bspw. nicht Universitätsmitglied ist."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Alumni Access", de: "Alumni Access"],
                    expl: [en: "Decision whether people who have completed their studies, have access.", de: "Aussage darüber, ob der Zugriff auf die lizenzierten Produkte für Alumni ermöglicht wird."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false
            ],
            [
                    name: [en: "Cancellation Allowance", de: "Außerordentliche Kündigung"],
                    expl: [en: "Defines under which extraordenary circumstances an angreement can be stopped.", de: "Definition, unter welchen besonderen Voraussetzungen eine Kündigung ausgesprochen werden kann."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Change to licensed material", de: "Änderung am Vertragsgegenstand"],
                    expl: [en: "Defines changes which are allowed or forbidden to be made to the licensed material itself.", de: "Definition über erlaubte oder verbotenen Veränderungen, welche am lizenzierten Material vorgenommen werden dürfen."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Citation requirement detail", de: "Zitier-Regeln Details"],
                    expl: [en: "A specification of the required or recommended form of citation.", de: ""],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Clickwrap modification", de: "Clickthrough"],
                    expl: [en: "A clause indicating that the negotiated agreement supersedes any click-through, click-wrap, other user agreement, or terms of use residing on the provider's server that might otherwise function as a contract of adhesion.", de: ""],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false
            ],
            [
                    name: [en: "Completeness of content clause", de: "Klausel zur Vollständigkeit der Inhalte"],
                    expl: [en: "The presence of a provision in the contract stating that the licensed electronic materials shall include all content found in the print equivalent.", de: "Aussage darüber, ob das lizenzierte Material alle Inhalte einer gedruckten Fassung beinhaltet, z. B. auch Werbeanzeigen."],
                    descr:allDescr, type: OT.Rdv, cat:'Existence', isUsedForLogic: false
            ],
            [
                    name: [en: "Concurrency with print version", de: "Übereinstimmung mit Druckversion"],
                    expl: [en: "The presence of a provision in the contract which states that the licensed materials will be available before, or no later than the print equivalent, and/or will be kept current.", de: "Aussage, ob das lizenzierte Material früher als oder zeitgleich mit dem  gedruckten Material zugänglich ist  bzw. ob es aktuell gehalten wird."],
                    descr:allDescr, type: OT.Rdv, cat:'Existence', isUsedForLogic: false
            ],
            [
                    name: [en: "Concurrent Users", de: "Gleichzeitige Nutzer"],
                    expl: [en: "1. The licensed number of concurrent users for a resource. 2. The number of concurrent users if shared across an interface rather than for a specific resource.", de: "Anzahl der gleichzeitigen Zugriffe auf ein lizenziertes Produkt."],
                    descr:allDescr, type: OT.Int, isUsedForLogic: false //TODO: OT.String because of 'unlimited'
            ],
            [
                    name: [en: "Concurrent Access", de: "Gleichzeitige Nutzer (Regelung)"],
                    expl: [en: "The allowance to give access to more than one user at the same time to use s. th.", de: "Erlaubnis, mehrere, zeitlich parallele Zugriffe zu ermöglichen."],
                    descr:allDescr, type: OT.Rdv, cat:'ConcurrentAccess', isUsedForLogic: false
            ],
            [
                    name: [en: "Confidentiality of agreement", de: "Vertraulichkeit der Vereinbarung"],
                    expl: [en: "The presence or absence of clauses that specify or detail restrictions on the sharing of the terms of the license agreement. The clause may specify terms to be held confidential, or may refer to the entire agreement. This clause may be limited by state law for U.S. public institutions.", de: "Festlegung zur Vertraulichkeit der Vereinbarung."],
                    descr:allDescr, type: OT.Rdv, cat:'Confidentiality', isUsedForLogic: false
            ],
            [
                    name: [en: "Content warranty", de: "Gewährleistung über den Inhalt"],
                    expl: [en: "A clause that guarantees a remedy to the licensee if the quantity or quality of material contained within the resource is materially diminished. The clause is usually in the form of a pro-rata refund or termination right.", de: "Aussage darüber, inwieweit sich die Qualität und Menge des lizenzierten Materials verändern darf und welche Kompensationszahlungen und Sonderkündigungsrechte möglich sind."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Course pack electronic", de: "Semesterapparat elektronisch"],
                    expl: [en: "The right to use licensed materials in collections or compilations of materials assembled in an electronic format by faculty members for use by students in a class for purposes of instruction.", de: "Das Recht elektronische Kopien des lizensierten Materials für Semesterapparate zur Verfügung zu stellen."],
                    descr:allDescr, type: OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [en: "Course pack print", de: "Semesterapparat gedruckt"],
                    expl: [en: "The right to use licensed materials in collections or compilations of materials assembled in a print format by faculty members for use by students in a class for purposes of instruction, e.g., book chapters, journal articles.", de: "Das Recht lizensiertes Materials in ausgedruckter Form für Semesterapparate zur Verfügung zu stellen."],
                    descr:allDescr, type: OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [en: "Course pack term note", de: "Semesterapparat Bedingungen zur Nutzung elektronischer Skripte"],
                    expl: [en: "Information which qualifies a permissions statement on Course Packs.", de: "Bedingungen zur Nutzung elektronischer Skripte."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Course reserve electronic/cached", de: "Seminarapparat mit Zugangsbeschränkung elektronisch"],
                    expl: [en: "The right to make electronic copies of the licensed materials and store them on a secure network, e.g., book chapters, journal articles stored on a secure network, for course reserves and online course websites.", de: ""],
                    descr:allDescr, type: OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [en: "Course reserve print", de: "Seminarapparat mit Zugangsbeschränkung gedruckt"],
                    expl: [en: "The right to make print copies of the licensed materials and place them in a controlled circulation area of the library for reserve reading in conjunction with specific courses of instruction.", de: ""],
                    descr:allDescr, type: OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [en: "Course reserve term note", de: "Seminarapparat mit Zugangsbeschränkung Bedingungen der Nutzung"],
                    expl: [en: "Information which qualifies a permissions statement on Course Reserves.", de: ""],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Cure period for breach", de: "Zeitraum der Behebung bei Vertragsbruch"],
                    expl: [en: "The cure period for an alleged material breach.", de: "Definition der Zeitspanne, in welcher Mängel behoben werden."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false //TODO: OT.Int
            ],
            [
                    name: [en: "Data protection override", de: "Datenschutz aufgehoben"],
                    expl: [en: "A clause that provides fair use protections within the context of assertions of database protection or additional proprietary rights related to database content not currently covered by U.S. copyright law. Applicable for U.S. libraries but may be of interest for other countries when recording terms for products licensed by U.S. businesses.", de: ""],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false // TODO: cat:'YNU'
            ],
            [
                    name: [en: "General Terms note", de: "Allgemeine Bedingungen"],
                    expl: [en: "Notes about the terms in the business agreement of the license as a whole.", de: ""],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Governing jurisdiction", de: "Gerichtsstand"],
                    expl: [en: "The venue or jurisdiction to be used in the event of an alleged breach of the agreement.", de: "Ort des zuständigen Gerichts."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Governing law", de: "Anzuwendendes Recht"],
                    expl: [en: "A clause specifying the governing law to be used in the event of an alleged breach of the agreement.", de: "Definition über anzuwendendes Recht."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "ILL electronic", de: "Fernleihe elektronisch"],
                    expl: [en: "The right to provide the licensed materials via interlibrary loan by way of electronic copies.", de: "Aussage darüber, ob die Fernleihe mittels einer elektron. Kopie bedient werden darf."],
                    descr:allDescr, type: OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [en: "ILL print or fax", de: "Fernleihe per Papier oder Fax"],
                    expl: [en: "The right to provide the licensed materials via interlibrary loan by way of print copies or facsimile transmission.", de: "Aussage darüber, ob die Fernleihe mittels  Fax oder gedruckter Kopien bedient werden darf."],
                    descr:allDescr, type: OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [en: "ILL secure electronic transmission", de: "Fernleihe über sichere elektronische Übermittlung"],
                    expl: [en: "The right to provide the licensed materials via interlibrary loan by way of secure electronic transmission.", de: "Aussage darüber, ob die Fernleihe über sichere, elektron. Übermittlungswege bedient werden darf."],
                    descr:allDescr, type: OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [en: "ILL record keeping required", de: "Fernleihdatensatz muss gespeichert werden"],
                    expl: [en: "The requirement to keep records of interlibrary loan activity and provide reports to the licensor at periodic intervals or upon request.", de: "Aussage darüber, ob der Fernleihdatensatz aufbewahrt werden muss."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false // TODO: cat:'YNU'
            ],
            [
                    name: [en: "ILL term note", de: "Fernleihe Weitere Bedingungen"],
                    expl: [en: "Additional information related to interlibrary loan.", de: "Weitere Bedingungen zur Fernleihe."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [key: "Ill ZETA code", en: "Ill ZETA code", de: "Fernleihindikator: Lieferweg"],
                    expl: [en: "", de: "Fernleihindikator gemäß ZETA/RDA, 1. Position: Bestellaufgabe und Lieferweg"],
                    descr:allDescr, type: OT.Rdv, cat:'Ill code', isUsedForLogic: false
            ],
            [
                    name: [key: "Ill ZETA inland only", en: "Ill ZETA inland only", de: "Fernleihindikator: Inland"],
                    expl: [en: "", de: "Fernleihindikator gemäß ZETA/RDA, 2. Position: Nur Inland"],
                    descr:allDescr, type: OT.Rdv, cat:'YN', isUsedForLogic: false
            ],
            [
                    name: [key: "Ill ZETA electronic fobidden", en: "Ill ZETA electronic fobidden", de: "Fernleihindikator: Elektronische Übertragung zwischen Bibliotheken ausgeschlossen"],
                    expl: [en: "", de:"Fernleihindikator gemäß ZETA/RDA, 3. Position: Elektronische Übertragung zwischen Bibliotheken ausgeschlossen"],
                    descr:allDescr,type: OT.Rdv, cat:'YN', isUsedForLogic: false
            ],
            [
                    name: [en: "Indemnification by licensor", de: "Entschädigung durch den Lizenzgeber"],
                    expl: [en: "A clause by which the licensor agrees to indemnify the licensee against a legal claim. This may specifically include intellectual property claims (third party rights), or may be broader in application.", de: "Aussage darüber, inwiefern der Lizenzgeber den Lizenznehmer von juristischen Ansprüchen schadlos hält."],
                    descr:allDescr, type: OT.Rdv, cat:'Indemnification', isUsedForLogic: false
            ],
            [
                    name: [en: "Indemnification by licensee clause indicator", de: "Entschädigung durch den Lizenznehmer"],
                    expl: [en: "A clause by which the licensee agrees to indemnify the licensor against a legal claim, usually for a breach of agreement by the licensee.", de: "Aussage darüber, inwieweit der Lizenznehmer den Lizenzgeber von juristischen Ansprüchen schadlos hält."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false
            ],
            [
                    name: [en: "Invoicing", de: "Rechnungsstellung"],
                    expl: [en: "Defining who creates and sends out invoices to the participants.", de: "Benennung des Rechnungsstellers."],
                    descr:allDescr, type: OT.Date, isUsedForLogic: false //TODO: OT.Rdv, cat:'Invoicing'
            ],
            [
                    name: [en: "Licensee termination right", de: "Kündigungsrecht des Lizenznehmers"],
                    expl: [en: "The ability of the licensee to terminate an acquisition during a contract period.", de: "Aussagen über die Kündigungsrechte des Lizenznehmers."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false
            ],
            [
                    name: [en: "Licensee termination condition", de: "Kündigungsrecht des Lizenznehmers Voraussetzung"],
                    expl: [en: "The conditions that would allow a licensee to terminate acquisition during a contract period.", de: "Aussagen über die notwendigen Voraussetzungen einer Kündigung seitens des Lizenznehmers."],
                    descr:allDescr, type: OT.Rdv, cat:'Termination Condition', isUsedForLogic: false
            ],
            [
                    name: [en: "Licensee termination notice period", de: "Kündigungsfrist des Lizenznehmers"],
                    expl: [en: "The amount of advance notice required prior to contract termination by the Licensee.", de: "Definition der Kündigungsfrist des Lizenznehmers."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false //TODO: OT.Int
            ],
            [
                    name: [en: "Licensor termination right", de: "Kündigungsrecht des Lizenzgebers"],
                    expl: [en: "The ability of a Licensor to terminate an acquisition during a contract period.", de: "Aussagen über die Kündigungsrechte des Lizenzgebers."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false
            ],
            [
                    name: [en: "Licensor termination condition", de: "Kündigungsrecht des Lizenzgebers Voraussetzung"],
                    expl: [en: "The conditions that would allow a licensor to terminate acquisition during a contract period.", de: "Aussagen über die notwendigen Voraussetzungen einer Kündigung seitens des Lizenzgebers."],
                    descr:allDescr, type: OT.Rdv, cat:'Termination Condition', isUsedForLogic: false
            ],
            [
                    name: [en: "Licensor termination notice period", de: "Kündigungsfrist des Lizenzgebers"],
                    expl: [en: "The amount of advance notice required prior to contract termination by the licensor.", de: "Definition der Kündigungsfrist des Lizenzgebers."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false  //TODO: OT.Int
            ],
            [
                    name: [en: "Maintenance window", de: "Wartungsfenster"],
                    expl: [en: "The recurring period of time reserved by the product provider for technical maintenance activities, during which online access may be unavailable.", de: "Angaben über die Häufigkeit, Dauer und geschaltete Vorabinformationen bei Produktwartungen seitens des Lizenzgebers."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Metadata delivery"],
                    expl: [en: "Description to whom metadata will be delivered, what kind of standard will be provided.", de: "Aussagen darüber, von wem Metadaten geliefert werden und zur Art des Formats."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Method of Authentication", de: "Authentifizierungsverfahren"],
                    expl: [en: "", de: "Aussagen darüber, mit welchem Authentifizierungsverfahren Nutzer Zugriff erhalten."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Multi Site Access"],
                    expl: [en: "", de: "Aussagen darüber, ob mehrere Standorte auf das gleiche Produkt zugreifen dürfen."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false //TODO: cat:'YNU'
            ],
            [
                    name: [en: "New Underwriter", de: "Aufnahme neuer Teilnehmer"],
                    expl: [en: "New participant.", de: "Zeitpunkt für die Aufnahme neuer Teilnehmer (z. B. relevant für Rabattermittlung)."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false
            ],
            [
                    name: [en: "Partners Access", de:"Partners Access"],
                    expl: [en: "", de: "Partners Access."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false //TODO: cat:'YNU'
            ],
            [
                    name: [en: "Uptime guarantee", de: "Gewährleistung einer verfügbaren Betriebszeit"],
                    expl: [en: "", de: ""],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Digitial copy", de: "Digitalkopie"],
                    expl: [en: "The right of the licensee and authorized users to download and digitally copy a reasonable portion of the licensed materials.", de: "Aussagen darüber, inwiefern der Lizenznehmer und autorisierte Nutzer lizenziertes Material herunterladen und abspeichern darf."],
                    descr:allDescr, type: OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [en: "Digitial copy term note", de: "Digitalkopie Bedingungen"],
                    expl: [en: "Information which qualifies a permissions statement on Digitally Copy", de: "Bedingungen der dititalen Kopie."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            // change contents in DB per Script
            /*
            [
                    name: [en: "Datamining", de: "Datamining"],
                    expl: [en: "", de: "Aussagen darüber, ob bzw. wie das Material im Kontext des Datamining zu Verfügung steht, ferner Informationen über weiter zu berücksichtigende Aspekte wie Nutzungsbedingungen, Sicherheitserklärungen und Datenvernichtung."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            */
            [
                    name: [key: "TextDataMining", en: "Text- and Datamining", de: "Text- and Datamining"],
                    expl: [en: "", de: "Aussagen darüber, ob das Material im Kontext des Datamining zu Verfügung steht."],
                    descr:allDescr, type:  OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [key: "TextDataMiningCharacters", en: "Text- and Datamining Character Count", de: "Text- and Datamining Zeichenzahl"],
                    expl: [en: "", de: "Zahl der Zeichen, die im Rahmen eines Text- und Dataminings erlaubt sind."],
                    descr:allDescr, type: OT.Int, isUsedForLogic: false
            ],
            [
                    name: [key: "TextDataMiningRestrictions", en: "Text- and Datamining Restrictions", de: "Text- and Datamining Einschränkungen."],
                    expl: [en: "", de: "Aussagen darüber, welchen Einschränkungen das Text- und Datamining unterliegt, sowie Informationen über weiter zu berücksichtigende Aspekte wie Nutzungsbedingungen, Sicherheitserklärungen und Datenvernichtung."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Electronic link", de: "Elektronischer Link"],
                    expl: [en: "The right to link to the licensed material.", de: "Aussagen darüber, inwiefern auf das lizenzierte Material verlinkt werden darf."],
                    descr:allDescr, type: OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [en: "Electronic link term note", de: "Elektronischer Link Bedingungen"],
                    expl: [en: "Information which qualifies a permissions statement on Electronic Links.", de: "Bedingungen für die Nutzung des elektronischen Link"],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Fair use clause indicator", de: "Hinweis auf Klausel über die \'faire Nutzung\'"],
                    expl: [en: "A clause that affirms statutory fair use rights under U.S. copyright law (17 USC Section 107), or that the agreement does not restrict or abrogate the rights of the licensee or its user community under copyright law. Fair use rights include, but are not limited to, printing, downloading, and copying.", de: "Hinweis auf Klausel über die \'faire Nutzung\'."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false
            ],
            [
                    name: [en: "Distance Education", de: "Fernstudium"],
                    expl: [en: "The right to use licensed materials in distance education.", de: "Aussagen darüber, inwieweit das lizenzierte Material für das Fernstudium genutzt werden darf."],
                    descr:allDescr, type: OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [en: "Distance education term note", de: "Fernstudium Bedingungen zur Nutzung"],
                    expl: [en: "Information which qualifies a permissions statement on distance education.", de: "Bedingungen der Nutzung für das Fernstudium."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Print copy", de: "Druckkopie"],
                    expl: [en: "The right of the licensee and authorized users to print a portion of the licensed materials.", de: "Aussagen darüber, inwiefern der Lizenznehmer und autorisierte Nutzer lizenziertes Material ausdrucken darf."],
                    descr:allDescr, type: OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [en: "Print copy term note", de: "Druckkopie Bedingungen zur Nutzung"],
                    expl: [en: "Information which qualifies a permissions statement on Print Copy.", de: "Bedingungen der Druckkopie."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Scholarly sharing", de: "Weitergabe im Rahmen der Lehre"],
                    expl: [en: "The right of authorized users and/or the licensee to transmit hard copy or an electronic copy of a portion of the licensed materials to a third party for personal, scholarly, educational, scientific or professional use.", de: "Aussagen darüber, inwiefern der Lizenznehmer und autorisierte Nutzer Ausdrucke oder elektron. Kopien an Dritte im Kontext der u. a. privaten oder wissenschaftlichen Nutzung und im Rahmen der Lehre ausgeben darf."],
                    descr:allDescr, type: OT.Rdv, cat:'Permissions', isUsedForLogic: false
            ],
            [
                    name: [en: "Wifi Access", de: "WLAN-Zugriff"],
                    expl: [en: "Internet is provided without using wires.", de: "Aussagen darüber, inwieweit über WLAN auf das lizenzierte Material zugegriffen werden darf."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false
            ],
            [
                    name: [en: "Remote Access", de: "Remote-Zugriff"],
                    expl: [en: "The right of an authorized user to gain access to an Electronic Product from an offsite location.", de: "Aussagen darüber, inwieweit autorisierte Nutzer über Remote Access auf lizenziertes Material zugreifen dürfen."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false //TODO: cat:'License.RemoteAccess'
            ],
            [
                    name: [en: "Scholarly sharing term note", de: "Weitergabe im Rahmen der Lehre Bedingungen"],
                    expl: [en: "Information which qualifies a permissions statement on Scholarly Sharing.", de: "Bedingungen der Weitergabe im Rahmen der Lehre."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Signed"],
                    expl: [en: "a document has been signed by s.o.", de: "Aussage darüber, ob das Vertragsdokument unterzeichnet worden ist."],
                    descr:allDescr, type: OT.Rdv, cat:'YN', isUsedForLogic: false //Todo: type: OT.Rdv, cat:'YNU'
            ],
            [
                    name: [en: "Termination requirement note", de: "Kündigungsrecht besondere Anforderung"],
                    expl: [en: "A clarification of the termination requirements and what certification of the requirement activities is necessary.", de: ""],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [key: "Usage Statistics", en: "Usage Statistics Availability Indicator", de: "Statistik  Lieferung"],
                    expl: [en: "The availability of usage statistics for the product.", de: "Aussagen über die Lieferung von Nutzungsstatistiken."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false
            ],
            [
                    name: [key: "statisticsstandardcompliance", en: "Usage Statistics Standard Compliance", de: "Statistikstandard"],
                    expl: [en: "The official standard to which the statistics conform for more information, see guidelines set forth at http://www.projectcounter.org and http://www.library.yale.edu/consortia/2001webstats.htm.", de: "Aussagen über den Standard, in welchem die Statistiken geliefert werden."],
                    descr:allDescr, type: OT.Rdv, cat:'License.Statistics.Standards', isUsedForLogic: false
            ],
            [
                    name: [key: "usagestatsdelivery", en: "Usage Statistics Delivery", de: "Statistik Liefermethode"],
                    expl: [en: "The manner in which statistics are made available.", de: "Aussagen über den Versandweg der Statistiken."],
                    descr:allDescr, type: OT.Rdv, cat:'License.Statistics.Delivery', isUsedForLogic: false
            ],
            [
                    name: [key: "usagestatsformat", en: "Usage Statistics Format", de: "Statistik Auslieferungsformat"],
                    expl: [en: "The format(s) in which statistics are made available.", de: "Aussagen über Auslieferformat der Statistiken."],
                    descr:allDescr, type: OT.Rdv, cat:'License.Statistics.Format', isUsedForLogic: false
            ],
            [
                    name: [key: "usagestatsfrequency", en: "Usage Statistics Frequency", de: "Statistik Auslieferungsfrequenz"],
                    expl: [en: "The frequency with which statistics are made available.", de: "Aussagen über die Frequenz der Auslieferung der Statistiken."],
                    descr:allDescr, type: OT.Rdv, cat:'License.Statistics.Frequency', isUsedForLogic: false
            ],
            [
                    name: [key: "usagestatsonlinelocation", en: "Usage Statistics Online Location", de: "Statistik Host"],
                    expl: [en: "The online location at which statistics can be accessed e.g., URL or file path.", de: "Aussagen über den Ort, an welchem die Statistiken online zu Verfügung stehen."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [key: "usagestatsuserid", en: "Usage Statistics User ID", de: "Statistik Nutzername"],
                    expl: [en: "The identifier used for online access to the statistics management site or dataset may be same as administrative identifier.", de: "Zugangsdaten."],
                    descr:allDescr, type: OT.Rdv, cat:'License.Statistics.UserCreds', isUsedForLogic: false
            ],
            [
                    name: [key: "usagestatspassword", en: "Usage Statistics Password", de: "Statistik Passwort"],
                    expl: [en: "The password used for online access to the statistics management site or dataset may be same as administrative password.", de: "Zugangsdaten."],
                    descr:allDescr, type: OT.Rdv, cat:'License.Statistics.UserCreds', isUsedForLogic: false
            ],
            [
                    name: [key: "usagestatsaddressee", en: "Usage Statistics Addressee", de: "Statistik Hauptverantwortlicher"],
                    expl: [en: "The local person to whom statistics are sent.", de: "Aussagen über den Verantwortlichen, welcher die Statistiken zugesandt bekommt."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [key: "usagestatslocallystored", en: "Usage Statistics Online Location", de: "Statistik Links und lokale Daten"],
                    expl: [en: "Information about and/or links to locally stored data.", de: "Aussagen über bzw. Links zu den lokal gespeicherten Statistiken."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            /*[
                    name: [en: "User restriction note", de: "Benutzungsbeschränkungen"],
                    expl: [en: "", de: ""],
                    descr:allDescr, type: OT.String // TODO: should be deleted, because no ERMI, no ONIX-PL. In use at the moment.
            ],*/
            [
                    name: [key: "accessibilitycompliance", en: "Accessibility compliance", de: "Zugriff für Personen mit Einschränkungen"],
                    expl: [en: "An agreement that the data is provided in a form compliant with relevant accessibility (disabilities) legislation. For more information, see guidelines set forth by the World Wide Web Consortium at http://www.w3.org/wai/.", de: "Aussagen über Zugriffmöglichkeiten für Personen mit Einschränkungen."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false
            ],
            [
                    name: [en: "Single-site license", de: "Einzelplatzlizenz"],
                    expl: [en: "", de: "Angaben über die Nutzung des Produktes über eine Einzelplatzlizenz."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Document delivery service (commercial)", de: "Dokumentenlieferdienst kommerziell"],
                    expl: [en: "", de: "Angabe über die Nutzungsmöglichkeit von kommerziellen Dokumentenlieferdiensten wie Subito."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Licensee obligations", de: "Pflichten des Lizenznehmers"],
                    expl: [en: "", de: "Angaben über die Pflichten des Lizenznehmers, wie Informationspflichten über IP-Rangeänderungen."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Severability Clause", de: "Salvatorische Klausel"],
                    expl: [en: "", de: "Angaben über die salvatorische Klausel."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            /*[
                    name: [en: "Payment target", de: "Zahlungsziel"],
                    expl: [en: "A date until which the payment will be expected.", de: "Definition des spätestens Datums, an welchem die Rechnung beglichen sein muss, z. B. ein festes Datum oder x Tage nach Rechnungseingang."],
                    descr:allDescr, type: OT.Date // TODO: should be deleted, because no ERMI, no ONIX-PL and not a licence term. In use at the moment.
            ],*/
            [
                    name: [en: "Performance warranty", de: "Gewährleistung einer Systemleistung/Performanz"],
                    expl: [en: "The specific percentage of up-time guaranteed for the product being licensed, and the context for that percentage, e.g., routine maintenance, excluding routine maintenance, etc.", de: "Zusage der Verfügbarkeit und Leistung des lizenzierten Produktes in Prozent."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false //TODO: OT.String
            ],
            [
                    name: [en: "Permitted Uses", de: "Permitted Uses"],
                    expl: [en: "The allowance to use s. th. several times.", de: ""],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [key: "otheruserestriction", en: "Other Use Restriction Note", de: "Zusätzliche Einschränkungen"],
                    expl: [en: "Additional information about other use restrictions not adequately described elsewhere, e.g., time of day restrictions on Concurrent Users.", de: "Zusätzliche Einschränkungen, z.B. Tageszeit."],
                    descr:allDescr, type: OT.String, isUsedForLogic: false
            ],
            [
                    name: [en: "Regional Restriction", de: "Regionale Einschränkung"],
                    expl: [en: "", de: "Aussagen darüber, welche regionalen Einschränkungen zur Teilnahme am Vertrag gelten."],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false
            ],
            /*[
                    name: [en: "Service regulations", de: "Servicestandards"],
                    expl: [en: "", de: "Aussagen über Servicestandards, die einzuhalten sind"],
                    descr:allDescr, type: OT.String  // TODO: should be deleted, because no ERMI, no ONIX-PL and doublet with "Performance warranty". In use at the moment.
            ],*/
            [
                    name: [en: "User information confidentiality", de: "Vertraulichkeit der Nutzerdaten"],
                    expl: [en: "", de: "Aussagen über Servicestandards, die einzuhalten sind"],
                    descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false //TODO: cat:'YNU'
            ],


        ]
        createPropertyDefinitionsWithI10nTranslations(requiredProps)

        //def allOADescr = [en: PropertyDefinition.LIC_OA_PROP, de: PropertyDefinition.LIC_OA_PROP]
        def allOADescr = [en: PropertyDefinition.LIC_PROP, de: PropertyDefinition.LIC_PROP]

        def requiredOAProps
        requiredOAProps = [
                [
                        name : [en: "APC Discount", de: "Sonderkonditionen für Autoren"],
                        expl : [en: "", de: "Sonderkonditionen für Autoren aus teilnehmenden Einrichtungen.z. B. APC-Rabatt."],
                        descr: allOADescr, type: OT.String, isUsedForLogic: false
                ],
                [
                        name : [en: "Authority", de: "Zweitveröffentlichungsrecht Verfügungsberechtigter"],
                        expl : [en: "", de: "Aussage darüber, wer über das Recht zur Zweitveröffentlichung verfügt."],
                        descr: allOADescr, type: OT.Rdv, cat: 'Authority', isUsedForLogic: false //Todo: neu
                ],
                [
                        name : [en: "Branding", de: "Branding"],
                        expl : [en: "The availability of a branding feature.", de: "Aussagen über das Wasserzeichen, mit welchem das lizenzierte Material gekennzeichnet wird."],
                        descr: allOADescr, type: OT.String, isUsedForLogic: false //Todo: OT.Rdv, cat: 'YNU' (DLF-ERMI)
                ],
                [
                        name : [en: "Corresponding Author Identification", de: "Autorenidentifikation"],
                        expl : [en: "", de: "Aussagen über die Vorgaben zur Autorenidentifikation."],
                        descr: allOADescr, type: OT.Rdv, cat: 'License.OA.CorrespondingAuthorIdentification', multiple: true, isUsedForLogic: false
                ],
                [
                        name : [en: "Electronically Archivable Version", de: "Zweitveröffentlichungsrecht archivierbare Version"],
                        expl : [en: "Describes which version can be used for e.g. long-term preservation.", de: "Aussagen über die Möglichkeiten, eine archivierbare Version auf einem Repositorium zu hinterlegen."],
                        descr: allOADescr, type: OT.Rdv, cat: 'License.OA.eArcVersion', isUsedForLogic: false
                ],
                [
                        name : [en: "Embargo Period", de: "Zweitveröffentlichungsrecht Embargo"],
                        expl : [en: "The amount of time by which content is intentionally delayed. Refer to developing standards (e.g. ONIX for Serials) for values.", de: "Aussagen über die Zeitspanne, nach welcher auf ein elektron. Dokument ein Zugriff möglich wird."],
                        descr: allOADescr, type: OT.Int, isUsedForLogic: false
                ],
                [
                        name : [en: "Embargo Period", de: "Zweitveröffentlichungsrecht Embargo"],
                        expl : [en: "The amount of time by which content is intentionally delayed. Refer to developing standards (e.g. ONIX for Serials) for values.", de: "Aussagen über die Zeitspanne, nach welcher auf ein elektron. Dokument ein Zugriff möglich wird."],
                        descr: allOADescr, type: OT.Int, isUsedForLogic: false
                ],
                [
                        name : [en: "Funder", de: "Publikationsförderer"],
                        expl : [en: "S.o. who provides money for s.th.", de: "Aussage über Geldgeber."],
                        descr: allOADescr, type: OT.String, isUsedForLogic: false
                ],
                [
                        name : [en: "License to Publish", de: "Publikationslizenz"],
                        expl : [en: "An official sign by the CC which permitts different kind of usage.", de: "Aussagen über die Nutzung der Publikationslizenz (Creative Commons Attribution)."],
                        descr: allOADescr, type: OT.Rdv, cat: 'License.OA.LicenseToPublish', multiple: true, isUsedForLogic: false
                ],
                [
                        name : [en: "OA First Date", de: "OA Startdatum"],
                        expl : [en: "Time span start", de: "Aussagen über den zeitlichen Beginn, für welchen Institution/Autor Open Access-Rechte haben."],
                        descr: allOADescr, type: OT.Date, isUsedForLogic: false
                ],
                [
                        name : [en: "OA Last Date", de: "OA Enddatum"],
                        expl : [en: "Time span end", de: "Aussagen über den zeitlichen Endpunkt, für welchen Institution/Autor Open Access-Rechte haben.."],
                        descr: allOADescr, type: OT.Date, isUsedForLogic: false
                ],
                [
                        name : [en: "OA Note", de: "OA Bemerkung"],
                        expl : [en: "", de: "Weitere Aussagen und Bemerkungen zu Open Access."],
                        descr: allOADescr, type: OT.String, isUsedForLogic: false
                ],
                [
                        name : [en: "Offsetting", de: "Offsetting Berechnungsmodell"],
                        expl : [en: "", de: "Aussagen über das Berechnungsmodell."],
                        descr: allOADescr, type: OT.String, isUsedForLogic: false
                ],
                [
                        name : [key: "Type", en: "Open Access", de: "Open Access (Variante)"],
                        expl : [en: "", de: "Grundlegendes Merkmal, ob eine Lizenz bzw. ein Titel eine Open Access-Komponente enthält. (gold: OA Publikation; grün: Archivierung von pre-print und post-print; blau: Archiverung von post-print; gelb: Archivierung von pre-print; weiß: Archivierung wird nicht formell unterstützt)."],
                        descr: allOADescr, type: OT.Rdv, cat: 'License.OA.Type', isUsedForLogic: false
                ],
                // [name: [en: "Open Access", de: "Open Access", descr: allOADescr, type: OT.Rdv, cat: 'YN'], // Todo: delete in DB after swapping with "Type"
                [
                        name : [en: "Publishing Fee", de: "Publikationsgebür"],
                        expl : [en: "Share of money which has to be payed by e.g. the author.", de: "Aussagen über die Publikationsgebühren."],
                        descr: allOADescr, type: OT.String, isUsedForLogic: false
                ],
                [
                        name : [en: "Reading Fee", de: "Reading Fee"],
                        expl : [en: "Share of money which has to be payed by e.g. the user", de: "Aussagen über die Zugriffsgebühren."],
                        descr: allOADescr, type: OT.String, isUsedForLogic: false
                ],
                [
                        name : [en: "Receiving Modalities", de: "Zweitveröffentlichungsrecht Bezugsmodalitäten"],
                        expl : [en: "Defines the way s.th. is delivered.", de: "Aussagen über die Bezugsmodalitäten der archivierbaren Version."],
                        descr: allOADescr, type: OT.Rdv, cat: 'License.OA.ReceivingModalities', multiple: true, isUsedForLogic: false
                ],
                [
                        name : [en: "Repository", de: "Zweitveröffentlichungsrecht Repositorium"],
                        expl : [en: "A place where s.th. can be stored and found.", de: "Aussagen über die Art des Repositoriums."],
                        descr: allOADescr, type: OT.Rdv, cat: 'License.OA.Repository', multiple: true, isUsedForLogic: false
                ],
                [
                        name : [en: "Vouchers Free OA Articles", de: "Vouchers"],
                        expl : [en: "A piece of e.g. paper that can be used instead of e.g. money.", de: "Aussagen über Vouchers, die der Anbieter der teiln. Einrichtung zur Verfügung stellt und die zur kostenlosen Publikation in einer Hybrid-Zeitschrift berechtigen."],
                        descr: allOADescr, type: OT.String, isUsedForLogic: false
                ],
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredOAProps)

        //def allArcDescr = [en: PropertyDefinition.LIC_ARC_PROP, de: PropertyDefinition.LIC_ARC_PROP]
        def allArcDescr = [en: PropertyDefinition.LIC_PROP, de: PropertyDefinition.LIC_PROP]

        def requiredARCProps = [
                [
                        name: [en: "Perpetual coverage note", de: "Perpetual coverage note"],
                        expl: [en: "", de: ""],
                        descr:allDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false //TODO: cat:'YNU'
                ],
                [
                        name : [en: "Perpetual coverage from", de: "Dauerhafter Zugang Zeitraum von"],
                        expl : [en: "Time span start.", de: "Definition des Zeitpunktes, ab welchem ein dauerhafter Zugriff ermöglicht wird."],
                        descr: allArcDescr, type: OT.Date, isUsedForLogic: false
                ],
                [
                        name : [en: "Perpetual coverage to", de: "Dauerhafter Zugang Zeitraum bis"],
                        expl : [en: "Time span end.", de: "Definition des Zeitpunktes, bis zu welchem ein dauerhafter Zugriff ermöglicht wird."],
                        descr: allArcDescr, type: OT.Date, isUsedForLogic: false
                ],
                [
                        name : [key: "archivingright", en: "Archiving rights", de: "Archivrechte"],
                        expl : [en: "The right to permanently retain an electronic copy of the licensed materials.", de: "Aussage, ob ein Archivrecht besteht."],
                        descr: allArcDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false //TODO: cat:'YNU'  or Permissions
                ],
                [
                        name : [key: "archivingformat", en: "Archiving format", de: "Archivdaten Übermittlungsformat"],
                        expl : [en: "The format of the archival content. Values should be site-definable (e.g. remote, CD-ROM).", de: ""],
                        descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.ArchivalCopyTransmissionFormat', isUsedForLogic: false
                ],
                [
                        name : [en: "Archival Copy Content", de: "Archivkopie Form"],
                        expl : [en: "", de: "Erklärung: Aussagen über den zu erhaltenen Datenträger mit Archivmaterial, z. B. Rohdaten, inkl. Struktur, ohne DRM, inkl. Metadaten, inkl. Software."],
                        descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.ArchivalCopyContent', isUsedForLogic: false //TODO: String
                ],
                [
                        name : [en: "Archival Copy: Cost", de: "Archivkopie Kosten"],
                        expl : [en: "", de: "Aussagen über die Kosten der Archivkopieerstellung."],
                        descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.ArchivalCopyCost', isUsedForLogic: false
                ],
                [
                        name : [en: "Archival Copy: Permission", de: "Archivkopie Recht"],
                        expl : [en: "", de: "Aussage, ob ein Recht auf eine Archivkopie besteht."],
                        descr: allArcDescr, type: OT.Rdv, cat: 'YNO', isUsedForLogic: false
                ],
                [
                        name : [en: "Archival Copy: Time", de: "Archivkopie Zeitpunkt"],
                        expl : [en: "", de: "Aussagen über den Zeitpunkt, an welchem die Archivkopie angefertigt wird."],
                        descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.ArchivalCopyTime', multiple:true, isUsedForLogic: false
                ],
                [
                        name : [en: "Continuing Access: Payment Note", de: "Zugriffsrechte Kosten"],
                        expl : [en: "", de: "Aussagen über mögliche laufende Kosten bei fortgesetztem Hosting durch den Anbieter."],
                        descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.PaymentNote', isUsedForLogic: false
                ],
                [
                        name : [en: "Continuing Access: Restrictions", de: "Zugriffsrechte Einschränkungen"],
                        expl : [en: "", de: "Aussagen über Einschränkungen des Zugriffs, wenn z. B. im Post-Cancellation-Access nur ein Teil der vorher lizensierten Inhalte verfügbar ist."],
                        descr: allArcDescr, type: OT.Rdv, cat: 'YNO', isUsedForLogic: false //TODO: cat:'YNU'
                ],
                [
                        name : [en: "Post Cancellation Online Access", de: "Zugriffsrechte dauerhaft"],
                        expl : [en: "", de: "Aussagen über das dauerhafte Zugriffsrecht nach Kündigung."],
                        descr: allArcDescr, type: OT.Rdv, cat: 'YNO', isUsedForLogic: false //TODO: cat:'YNU'
                ],
                [
                        name: [en: "Continuing Access: Title Transfer", de: "Zugriffsrechte Titeltransferregeln"],
                        expl : [en: "", de: "Aussagen über Regeln des Titeltransfers, z. B. Regelung vorhanden, keine Regelung, Transfer Code of Practice."],
                        descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.TitleTransferRegulation', isUsedForLogic: false //TODO: OT.String
                ],
                [
                        name: [en: "Hosting Time", de: "Hostingrecht Zeitpunkt"],
                        expl : [en: "", de: "Aussagen über den Zeitpunkt, an welchem das Hostingrecht beginnt."],
                        descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.HostingTime', multiple:true, isUsedForLogic: false
                ],
                [
                        name: [en: "Hosting: Authorized", de: "Hostingrecht Berechtigte"],
                        expl : [en: "", de: "Aussage über die zum Hosting Berechtigten."],
                        descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.Authorized', multiple:true, isUsedForLogic: false
                ],
                [
                        name: [en: "Hosting: Obligation", de: "Hostingpflicht"],
                        expl : [en: "", de: ""],
                        descr: allArcDescr, type: OT.Rdv, cat: 'YN', isUsedForLogic: false //TODO: cat:'YNU'
                ],
                [
                        name : [en: "Hosting: Permission", de: "Hostingrecht"],
                        expl : [en: "", de: ""],
                        descr: allArcDescr, type: OT.Rdv, cat: 'YNO', isUsedForLogic: false //TODO: cat:'YNU'
                ],
                [
                        name: [en: "Hosting: Restriction", de: "Hostingrecht Einschränkung"],
                        expl : [en: "", de: ""],
                        descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.HostingRestriction', multiple:true, isUsedForLogic: false
                ],
                [
                        name: [en: "Hosting: Solution", de: "Hostingrecht: Lösung"],
                        expl : [en: "", de: "Aussagen, über welche technische Lösung das Material angeboten wird."],
                        descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.HostingSolution', multiple:true, isUsedForLogic: false
                ],
                [
                        name: [key: "intellectualpropertywarranty", en: "Intellectual property warranty", de: "Gewährleistung des/zum geistigen Eigentum"],
                        expl : [en: "A clause in which the licensor warrants that making the licensed materials available does not infringe upon the intellectual property rights of any third parties. This clause, in the form of a warranty, may or may not include an indemnification of licensee.", de: "Aussagen darüber, dass durch die Freigabe des Materials durch den Lizenzgeber keine Rechte Dritter und deren geistiges Eigentum verletzt werden."],
                        descr: allArcDescr, type: OT.Rdv, cat:'YNO', isUsedForLogic: false //TODO: cat:'YNU'
                ],
                //[name: [en: "Hosting: Additonal Agreement Necessary", de: "Hostingrecht: Zusatzvereinbarung notwendig"],descr: allArcDescr, type: OT.Rdv, cat: 'YN'],  //TODO: To be deleted, but in use

        ]
        createPropertyDefinitionsWithI10nTranslations(requiredARCProps)
    }

    def createPlatformProperties(){

        def allDescr = [en: PropertyDefinition.PLA_PROP, de: PropertyDefinition.PLA_PROP]

        def requiredPlatformProperties = [
            [
                name: [key: "Access Method: IPv4: Supported", en: "IPv4: Supported", de: "IPv4: Unterstützt"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.Rdv, cat:'YN', multiple: false, isUsedForLogic: true
            ],
            [
                name: [key: "Access Method: IPv6: Supported", en: "IPv6: Supported", de: "IPv6: Unterstützt"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.Rdv, cat:'YN', multiple: false, isUsedForLogic: true
            ],
            [
                name: [key: "Access Method: Proxy: Supported", en: "Proxy: Supported", de: "Proxy: Unterstützt"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.Rdv, cat:'YN', multiple: false, isUsedForLogic: true
            ],
            [
                name: [key: "Access Method: Shibboleth: Supported", en: "Shibboleth: Supported", de: "Shibboleth: Unterstützt"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.Rdv, cat:'YN', multiple: false, isUsedForLogic: true
            ],
            [
                name: [key: "Access Method: Shibboleth: SP entityID", en: "Shibboleth: SP entityID", de: "Shibboleth: SP entityID"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.String, multiple: false, isUsedForLogic: true
            ],
            [
                name: [key: "Usage Reporting: COUNTER R3: Reports supported", en: "COUNTER R3: Reports supported", de: "COUNTER R3: Reports unterstützt"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.Rdv, cat:'YN', multiple: false, isUsedForLogic: true
            ],
            [
                name: [key: "Usage Reporting: COUNTER R4: Reports supported", en: "COUNTER R4: Reports supported", de: "COUNTER R4: Reports unterstützt"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.Rdv, cat:'YN', multiple: false, isUsedForLogic: true
            ],
            [
                name: [key: "Usage Reporting: COUNTER R4: COUNTER_SUSHI API supported", en: "COUNTER R4: COUNTER_SUSHI API supported", de: "COUNTER R4: COUNTER_SUSHI API unterstützt"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.Rdv, cat:'YN', multiple: false, isUsedForLogic: false
            ],
            [
                name: [key: "Usage Reporting: COUNTER R4: SUSHI Server URL", en: "COUNTER R4: SUSHI Server URL", de: "COUNTER R4: SUSHI Server URL"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.URL, multiple: false, isUsedForLogic: false
            ],
            [
                name: [key: "Usage Reporting: COUNTER R4: Usage Statistics URL", en: "COUNTER R4: Usage Statistics URL", de: "COUNTER R4: Webzugang Statistik"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.URL, multiple: false, isUsedForLogic: false
            ],
            [
                name: [key: "Usage Reporting: COUNTER R5: Reports supported", en: "COUNTER R5: Reports supported", de: "COUNTER R5: Reports unterstützt"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.Rdv, cat:'YN', multiple: false, isUsedForLogic: true
            ],
            [
                name: [key: "Usage Reporting: COUNTER R5: COUNTER_SUSHI API supported", en: "COUNTER R5: COUNTER_SUSHI API supported", de: "COUNTER R5: COUNTER_SUSHI API unterstützt"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.Rdv, cat:'YN', multiple: false, isUsedForLogic: false
            ],
            [
                name: [key: "Usage Reporting: COUNTER R5: SUSHI Server URL", en: "COUNTER R5: SUSHI Server URL", de: "COUNTER R5: SUSHI Server URL"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.URL, multiple: false, isUsedForLogic: false
            ],
            [
                name: [key: "Usage Reporting: COUNTER R5: Usage Statistics URL", en: "COUNTER R5: Usage Statistics URL", de: "COUNTER R5: Webzugang Statistik"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.URL, multiple: false, isUsedForLogic: false
            ],
            [
                name: [key: "Usage Reporting: NatStat Supplier ID", en: "NatStat Supplier ID", de: "NatStat Anbietername"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.String, multiple: false, isUsedForLogic: true
            ],
            [
                name: [key: "Usage Reporting: COUNTER Registry URL", en: "COUNTER Registry URL", de: "COUNTER Registry URL"],
                expl: [en: "", de: ""],
                descr:allDescr, type: OT.URL, multiple: false, isUsedForLogic: false
            ],
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredPlatformProperties)
    }

    def createSubscriptionProperties() {

        def allDescr = [en: PropertyDefinition.SUB_PROP, de: PropertyDefinition.SUB_PROP]

        def requiredProps = [
                [
                        name: [en: "GASCO Entry", de: "GASCO-Eintrag"],
                        expl : [en: "", de: "Dieses Merkmal als Konsortialstelle auf \"ja\" setzen, um eine Lizenz im GASCO-Monitor aufzunehmen."],
                        descr:allDescr, type: OT.Rdv, cat:'YN', isUsedForLogic: true
                ],
                [
                        name: [en: "GASCO display name", de: "GASCO-Anzeigename"],
                        expl : [en: "", de: "Dieses Merkmal setzen, um bei Bedarf im GASCO-Monitor einen anderen Lizenznamen anzugeben als in LAS:eR aufgenommen."],
                        descr:allDescr, type: OT.String, isUsedForLogic: true
                ],
                [
                        name: [en: "GASCO negotiator name", de: "GASCO-Verhandlername"],
                        expl : [en: "", de: "Dieses Merkmal als Konsortialstelle verwenden, um im GASCO-Monitor einen anderen Verhandlungsführer-Namen anzugeben."],
                        descr:allDescr, type: OT.String, isUsedForLogic: true
                ],
                [
                        name: [en: "GASCO information link", de: "GASCO-Informations-Link"],
                        expl : [en: "", de: "Unter welchem Link finden sich Informationen zum Produkt?"],
                        descr:allDescr, type: OT.URL, isUsedForLogic: true
                ],
                [
                        name: [en: "EZB tagging (yellow)", de: "EZB Gelbschaltung"],
                        expl : [en: "", de: "Wird eine Gelbschaltung in der EZB vorgenommen?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "EZB Collection Transfer", de: "EZB Kollektionsdatenweitergabe an Drittsysteme erlaubt?"],
                        expl : [en: "", de: "Ist die Kollektionsdatenweitergabe an Drittsysteme erlaubt?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Metadata Delivery", de: "Metadatenlieferung"],
                        expl : [en: "", de: "Ist eine automatische Metadatenlieferung vorhanden?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Metadata Source", de: "Metadaten Quelle"],
                        expl : [en: "", de: "Über welche Quelle können die Metadaten bezogen werden?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Pricing advantage by licensing of another product", de: "Preisvorteil durch weitere Produktteilnahme"],
                        expl : [en: "", de: "Kann durch die Lizenzierung eines weiteren Produktes ein Preisvorteil gesichert werden?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Product dependency", de: "Produktabhängigkeit"],
                        expl : [en: "", de: "Ist die Lizenz von einem anderen Produkt abhängig?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Open country-wide", de: "Bundesweit offen"],
                        expl : [en: "", de: "Hat die Lizenz eine überregionale Ausrichtung?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Billing done by provider", de: "Rechnungsstellung durch Anbieter"],
                        expl: [en: "", de: "Erfolgt die Rechnungsstellung direkt über den Anbieter?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Due date for volume discount", de: "Mengenrabatt Stichtag"],
                        expl: [en: "", de: "Wann ist der Stichtag für die Findung der erreichten Rabattstufe?"],
                        descr:allDescr, type: OT.Date, cat:'YN'
                ],
                [
                        name: [en: "Time span for testing", de: "Testzeitraum"],
                        expl: [en: "", de: "Wie lange ermöglicht der Anbieter einen kostenfreien Testzugriff?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Joining during the period", de: "Unterjähriger Einstieg"],
                        expl: [en: "", de: "Ist ein unterjähriger Einstieg in die Lizenz möglich?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Newcomer discount", de: "Neueinsteigerrabatt"],
                        expl : [en: "", de: "Existiert ein zusätzlicher Neueinsteigerpreis?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Simuser", de: "Simuser"],
                        expl : [en: "", de: "Sind die simuser relevant für die Preisbestimmung?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Sim-User Number", de: "Sim-User Zahl"],
                        expl : [en: "", de: "Wieviele gleichzeitige Nutzerzugriffe umfasst die Lizenz?"],
                        descr:allDescr, type: OT.Rdv, cat:'Sim-User Number'
                ],
                [
                        name: [en: "Access choice remote", de: "Zugangswahl Remote"],
                        expl: [en: "Please indicate here whether you want 2FA, access for scientists or no remote access?", de: "Bitte geben Sie hier an, ob Sie 2FA, Zugang für Wissenschaftler oder kein remote Zugang wünschen?"],
                        descr:allDescr, type: OT.Rdv, cat:'Access choice remote'
                ],
                [
                        name: [en: "Beck Price Category A-F", de: "Beck Preiskategorie A-F"],
                        expl: [en: "Please indicate which price category your facility falls into. These can be found in the price tables. A-C each Uni with and without lawyers; D-F FH with and without law and other facilities.", de: "Bitte geben Sie an, in welche Preis-Kategorie Ihre Einrichtung fällt. Diese können Sie den Preistabellen entnehmen. A-C jeweils Uni mit und ohne Jurastutenten; D-F FH mit und ohne Jura und sonstige Einrichtungen."],
                        descr:allDescr, type: OT.Rdv, cat:'Category A-F'
                ],
                [
                        name: [en: "Time of billing", de: "Rechnungszeitpunkt"],
                        expl : [en: "Time of billing.", de: "Zeitpunkt der Rechnung."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Payment target", de: "Zahlungsziel"],
                        expl : [en: "", de: "Zahlungsziel"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Price rounded", de: "Preis gerundet"],
                        expl : [en: "Rounded price.", de: "Gerundeter Preis."],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Partial payment", de: "Teilzahlung"],
                        expl : [en: "Is a partial payment of bills intended in this subscription?", de: "Ist bei der Lizenz eine Teilzahlung der Rechnung vorgesehen?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Statistic", de: "Statistik"],
                        expl : [en: "", de: ""],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Statistic access", de: "Statistikzugang"],
                        expl : [en: "With which credentials may the statistics be fetched?", de: "Mit welchen Zugangsdaten können die Statistiken abgerufen werden?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Statistics Link", de: "Statistik-Link"],
                        expl : [en: "", de: "Über welchen Link können die Statistiken abgerufen werden?"],
                        descr:allDescr, type: OT.URL
                ],
                [
                        name: [en: "Admin Access", de: "Adminzugang"],
                        expl : [en: "", de: "Mit welchen Zugangsdaten gelangt in den Admin-Bereich auf der Anbieterplattform?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Admin Link", de: "Admin-Link"],
                        expl : [en: "", de: "Über welchen Link gelangt in den Admin-Bereich auf der Anbieterplattform?"],
                        descr:allDescr, type: OT.URL
                ],
                [
                        name: [en: "KBART", de: "KBART"],
                        expl : [en: "", de: "Existiert ein KBART-Lieferung?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "reverse charge", de: "reverse charge"],
                        expl: [en: "", de: "Unterliegt die Lizenz dem reverse charge?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Private institutions", de: "Private Einrichtungen"],
                        expl: [en: "", de: "Dürfen auch private Einrichtungen an der Lizenz teilnehmen?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Perennial term", de: "Mehrjahreslaufzeit"],
                        expl: [en: "", de: "Ist für die Lizenze eine Mehrjahreslaufzeit möglich?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Perennial term checked", de: "Mehrjahreslaufzeit ausgewählt"],
                        expl: [en: "", de: "Hat der Teilnehmer sich für eine Mehrjahreslaufzeit entschieden?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN', isUsedForLogic: true
                ],
                [
                        name: [en: "Discount", de: "Rabatt"],
                        expl: [en: "", de: "Höhe des Rabattes."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Scale of discount", de: "Rabattstaffel"],
                        expl: [en: "", de: "Wie sieht die Rabattstaffel für die Lizenz aus?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Calculation of discount", de: "Rabatt Zählung"],
                        expl: [en: "", de: "Wie wird die Rabatthöhe errechnet? Z.B. durch Zählung aller Teilnehmer bei dem Anbieter…"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Term of notice", de: "Kündigungsfrist"],
                        expl: [en: "", de: "Kündigungsfrist."],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Additional software necessary?", de: "Zusätzliche Software erforderlich?"],
                        expl : [en: "", de: "Wird für die Lizenzierung eine zusätzliche Software benötigt?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Price increase", de: "Preissteigerung"],
                        expl: [en: "", de: "Preissteigerung."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Price depending on", de: "Preis abhängig von"],
                        expl : [en: "", de: "Ist der Preis von etwas abhängig?"],
                        descr:allDescr, type: OT.String, multiple:true
                ],
                [
                        name: [en: "Cancellation rate", de: "Abbestellquote"],
                        expl: [en: "Cancellation rate.", de: "Abbestellquote."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Order number in purchasing system", de: "Bestellnummer im Erwerbungssystem"],
                        expl: [en: "", de: "Bestellnummer im Erwerbungssystem."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Credentials for users (per journal)", de: "Zugangskennungen für Nutzer (pro Zeitschrift)"],
                        expl: [en: "", de: "Zugangskennungen für Nutzer (pro Zeitschrift)."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Tax exemption", de: "Steuerbefreiung"],
                        expl : [en: "", de: "Liegt eine Steuerbefreiung für die Lizenz vor?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Subscription number of editor", de: "Subskriptionsnummer des Verlags"],
                        expl: [en: "", de: "Subskriptionsnummer des Verlags."],
                        descr:allDescr, type: OT.String, multiple:true
                ],
                [
                        name: [en: "Subscription number of provider", de: "Subskriptionsnummer des Lieferanten"],
                        expl: [en: "", de: "Subskriptionsnummer des Lieferanten."],
                        descr:allDescr, type: OT.String, multiple:true
                ],
                [
                        name: [en: "DBIS entry", de: "DBIS-Eintrag"],
                        expl: [en: "", de: "Existiert ein DBIS-Eintrag?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "DBIS link", de: "DBIS-Link"],
                        expl: [en: "", de: "Link zum DBIS-Eintrag."],
                        descr:allDescr, type: OT.URL
                ],
                [
                        name: [en: "Cancellation reason", de: "Abbestellgrund"],
                        expl: [en: "", de: "Welchen Grund gab es für die Abbestellung?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Hosting fee", de: "Hosting-Gebühr"],
                        expl: [en: "", de: "Ist eine Hosting-Gebühr zu entrichten?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Pick&Choose package", de: "Pick&Choose-Paket"],
                        expl: [en: "", de: "Handelt es sich um ein Paket mit Einzeltitelauswahl (Pick & Choose)?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "PDA/EBS model", de: "PDA/EBS-Programm"],
                        expl: [en: "", de: "Basiert die Lizenz auf einem PDA-, EBS- oder EBA-Modell?"],
                        descr:allDescr, type: OT.String
                ],
                                //[name: [en: "Produktsigel beantragt", de: "Produktsigel beantragt"],                    descr:allDescr, type: OT.String],
                [
                        name: [en: "Specialised statistics / classification", de: "Fachstatistik / Klassifikation"],
                        expl: [en: "", de: "Fachstatistik / Klassifikation"],
                        descr:allDescr, type: OT.Int, multiple:true
                ],
                [
                        name: [en: "Perpetual access", de: "Archivzugriff"],
                        expl: [en: "", de: "Gibt es einen Archivzugriff?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Restricted user group", de: "Eingeschränkter Benutzerkreis"],
                        expl: [en: "", de: "Welche Einschränkung des Benutzerkreises gibt es?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "SFX entry", de: "SFX-Eintrag"],
                        expl: [en: "", de: "Gibt es einen SFX-Eintrag?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Take Over Titles", de: "Take-Over-Titel"],
                        expl: [en: "", de: "Bedingungen für während der Vertragslaufzeit vom Verlag übernommene oder neu veröffentlichte Titel."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Deep Discount Price", de: "Deep-Discount-Preis"],
                        expl: [en: "", de: "Bietet der Verlag einen Deep-Discount-Preis für Printabonnements an?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Central funding possible", de: "Zentralmittelfähig"],
                        expl: [en: "", de: "Ist es möglich, diese Lizenz aus Zentralmitteln zu finanzieren?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ]
		
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredProps)
    }

    def createSurveyProperties() {

        def requiredProps = [
                [
                        name: [en: "Participation", de: "Teilnahme"],
                        expl: [en: "Do you still want to license the license?", de: "Wollen Sie weiterhin an der Lizenz teilnehmen?"],
                        type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Access choice remote", de: "Zugangswahl Remote"],
                        expl: [en: "Please indicate here whether you want 2FA, access for scientists or no remote access?", de: "Bitte geben Sie hier an, ob Sie 2FA, Zugang für Wissenschaftler oder kein remote Zugang wünschen?"],
                        type: OT.Rdv, cat:'Access choice remote'
                ],
                [
                        name: [en: "Beck Price Category A-F", de: "Beck Preiskategorie A-F"],
                        expl: [en: "Please indicate which price category your facility falls into. These can be found in the price tables. A-C each Uni with and without lawyers; D-F FH with and without law and other facilities.", de: "Bitte geben Sie an, in welche Preis-Kategorie Ihre Einrichtung fällt. Diese können Sie den Preistabellen entnehmen. A-C jeweils Uni mit und ohne Jurastutenten; D-F FH mit und ohne Jura und sonstige Einrichtungen."],
                        type: OT.Rdv, cat:'Category A-F'
                ],
                [
                        name: [en: "Multi-year term 2 years", de: "Mehrjahreslaufzeit 2 Jahre"],
                        expl: [en: "Please indicate here, if you wish a licensing directly for two years.", de: "Bitte geben Sie hier an, ob Sie eine Lizenzierung direkt für zwei Jahre wünschen."],
                        type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Multi-year term 3 years", de: "Mehrjahreslaufzeit 3 Jahre"],
                        expl: [en: "Please indicate here, if you wish a licensing directly for three years.", de: "Bitte geben Sie hier an, ob Sie eine Lizenzierung direkt für drei Jahre wünschen."],
                        type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Sim-User Number", de: "Sim-User Zahl"],
                        expl: [en: "Please indicate which number of Sim users should be licensed.", de: "Bitte geben sie an, welche Anzahl an Sim-Usern lizenziert werden soll."],
                        type: OT.Rdv, cat:'Sim-User Number'
                ],

        ]
        createSurveyPropertiesWithI10nTranslations(requiredProps)
    }

    @Deprecated
    def createPrivateProperties() {

        def allOrgDescr = [en: PropertyDefinition.ORG_PROP, de: PropertyDefinition.ORG_PROP]

        // TODO - remove HOTFIX: hardcoded hbz properties
        def requiredOrgProps = [
                [name: [en: "Note", de: "Anmerkung"], tenant: 'hbz', descr: allOrgDescr, type: OT.String],
                [name: [en: "promotionsrecht", de: "Promotionsrecht"], tenant: 'hbz', descr: allOrgDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "privatrechtlich", de: "Privatrechtlich"], tenant: 'hbz', descr: allOrgDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "ezb teilnehmer", de: "EZB-Teilnehmer"], tenant: 'hbz', descr: allOrgDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "nationallizenz teilnehmer", de: "Nationallizenz-Teilnehmer"], tenant: 'hbz', descr: allOrgDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "discovery system", de: "Discovery-System"], tenant: 'hbz', descr: allOrgDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "verwendete discovery systeme", de: "Verwendete Discovery-Systeme"], tenant: 'hbz', descr: allOrgDescr, type: OT.String]
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredOrgProps)

        def allPrsDescr = [en: PropertyDefinition.PRS_PROP, de: PropertyDefinition.PRS_PROP]

        def requiredPrsProps = [
                [name: [en: "Note", de: "Anmerkung"], descr: allPrsDescr, type: OT.String]
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredPrsProps)
    }

    def createPropertyDefinitionsWithI10nTranslations(requiredProps) {

        requiredProps.each { default_prop ->
            def prop   = null
            def tenant = null

            if (default_prop.tenant) {
                tenant = Org.findByShortname(default_prop.tenant)

                if (tenant) {
                    prop = PropertyDefinition.findByNameAndDescrAndTenant(default_prop.name['en'], default_prop.descr['en'], tenant)
                } else {
                    log.debug("unable to locate tenant: ${default_prop.tenant} .. skipped")
                    return
                }
            } else {
                prop = PropertyDefinition.findWhere(name: default_prop.name['en'], descr: default_prop.descr['en'], tenant: null)
            }

            if (! prop) {
                if (tenant) {
                    log.debug("unable to locate private property definition for ${default_prop.name['en']} / ${default_prop.descr['en']} for tenant: ${tenant} .. creating")
                    prop = new PropertyDefinition(name: default_prop.name['en'], descr: default_prop.descr['en'], tenant: tenant)
                } else {
                    log.debug("unable to locate property definition for ${default_prop.name['en']} / ${default_prop.descr['en']} .. creating")
                    prop = new PropertyDefinition(name: default_prop.name['en'], descr: default_prop.descr['en'])
                }
            }

            if (default_prop.cat != null) {
                prop.setRefdataCategory(default_prop.cat)
            }

            if (default_prop.multiple) {
                prop.multipleOccurrence = default_prop.multiple
            }

            if (default_prop.isUsedForLogic) {
                prop.isUsedForLogic = default_prop.isUsedForLogic
            }

            prop.type  = default_prop.type
            //prop.softData = false
            prop.isHardData = BOOTSTRAP
            prop.save(failOnError: true)

            I10nTranslation.createOrUpdateI10n(prop, 'name', default_prop.name)
            I10nTranslation.createOrUpdateI10n(prop, 'descr', default_prop.descr)

            if (default_prop.expl) {
                I10nTranslation.createOrUpdateI10n(prop, 'expl', default_prop.expl)
            }
        }
    }

    def createSurveyPropertiesWithI10nTranslations(requiredProps) {

        requiredProps.each { default_prop ->
            def surveyProperty   = null
            def owner = null

            if (default_prop.owner) {
                owner = Org.findByShortname(default_prop.owner)

                if (owner) {
                    surveyProperty = SurveyProperty.findByNameAndOwner(default_prop.name['en'], owner)
                } else {
                    log.debug("unable to locate owner: ${default_prop.owner} .. skipped")
                    return
                }
            } else {
                surveyProperty = SurveyProperty.findWhere(name: default_prop.name['en'], owner: null)
            }

            if (! surveyProperty) {
                if (owner) {
                    log.debug("unable to locate private survey property definition for ${default_prop.name['en']} for owner: ${owner} .. creating")
                    surveyProperty = new SurveyProperty(name: default_prop.name['en'], owner: owner)
                } else {
                    log.debug("unable to locate survey property definition for ${default_prop.name['en']} .. creating")
                    surveyProperty = new SurveyProperty(name: default_prop.name['en'])
                }
            }

            if (default_prop.cat != null) {
                surveyProperty.setRefdataCategory(default_prop.cat)
            }

            surveyProperty.type  = default_prop.type
            //prop.softData = false
            surveyProperty.isHardData = BOOTSTRAP
            surveyProperty.save(failOnError: true)

            I10nTranslation.createOrUpdateI10n(surveyProperty, 'name', default_prop.name)

            if (default_prop.expl) {
                I10nTranslation.createOrUpdateI10n(surveyProperty, 'expl', default_prop.expl)
            }

            if (default_prop.introduction) {
                I10nTranslation.createOrUpdateI10n(surveyProperty, 'introduction', default_prop.introduction)
            }
        }
    }

    @Deprecated
    def addDefaultJasperReports() {
        //Add default Jasper reports, if there are currently no reports in DB

        /*
        def reportsFound = JasperReportFile.findAll()
        def defaultReports = ["floating_titles", "match_coverage", "no_identifiers", "title_no_url",
                              "previous_expected_sub", "previous_expected_pkg", "duplicate_titles"]
        defaultReports.each { reportName ->

            def path = "resources/jasper_reports/"
            def filePath = path + reportName + ".jrxml"
            def inputStreamBytes = grailsApplication.parentContext.getResource("classpath:$filePath").inputStream.bytes
            def newReport = reportsFound.find { it.name == reportName }
            if (newReport) {
                newReport.setReportFile(inputStreamBytes)
                newReport.save()
            } else {
                newReport = new JasperReportFile(name: reportName, reportFile: inputStreamBytes).save()
            }
            if (newReport.hasErrors()) {
                log.error("jasper Report creation for " + reportName + ".jrxml failed with errors: \n")
                newReport.errors.each {
                    log.error(it + "\n")
                }
            }
        }
        */
    }

    def ensurePermGrant(role, perm) {
        def existingPermGrant = PermGrant.findByRoleAndPerm(role,perm)
        if (! existingPermGrant) {
            log.debug("create new perm grant for ${role}, ${perm}")
            def new_grant = new PermGrant(role:role, perm:perm).save()
        }
        else {
            log.debug("grant already exists ${role}, ${perm}")
        }
    }

    /**
    * RefdataValue.group is used only for OrgRole to filter the types of role available in 'Add Role' action
    * This is done by providing 'linkType' (using instance class) to the '_orgLinksModal' template.
    */
    def setOrgRoleGroups() {
        def lic = License.name
        def sub = Subscription.name
        def pkg = Package.name

        def entries = [
                [[en: 'Licensor', de: 'Lizenzgeber'], lic],
                [[en: 'Licensee', de: 'Lizenznehmer'], lic],
                [[en: 'Licensing Consortium', de: 'Lizenzkonsortium'], lic],
                [[en: 'Negotiator'], lic],
                [[en: 'Subscriber'], sub],
                [[en: 'Provider', de: 'Anbieter'], sub],
                [[en: 'Subscription Agent'], sub],
                [[en: 'Subscription Consortia'], sub],
                [[en: 'Content Provider', de: 'Anbieter'], pkg],
                [[en: 'Package Consortia'], pkg],
                [[en: 'Publisher', de: 'Verlag'], null],
                [[en: 'Agency', de: 'Lieferant'], sub]
        ]

        entries.each{ rdv ->
            def i10n = rdv[0]
            def group = rdv[1]

            def val = RefdataValue.loc("Organisational Role", i10n, BOOTSTRAP)
            if (group) {
                val.setGroup(group)
            }
            val.save()
        }
    }

    def setupRefdata = {

        // RefdataCategory.locCategory( category_name, EN, DE )     => RefdataCategory(desc:category_name            & I10nTranslation(EN, DE)
        // RefdataCategory.locRefdataValue( category_name, EN, DE ) => RefdataValue(value:EN, owner:@category_name)  & I10nTranslation(EN, DE)

        // because legacy logic is hardcoded against RefdataCategory.desc & RefdataValue.value

        List rdcList = getParsedCsvData('setup/RefdataCategory.csv', 'RefdataCategory')

        rdcList.each { map ->
            RefdataCategory.construct(map)
        }

        List rdvList = getParsedCsvData('setup/RefdataValue.csv', 'RefdataValue')

        rdvList.each { map ->
            RefdataValue.construct(map)
        }

        // refdata values

        // RefdataValue.loc('Country',   [en: 'Germany', de: 'Deutschland'], BOOTSTRAP)
        // RefdataValue.loc('Country',   [en: 'Switzerland', de: 'Schweiz'], BOOTSTRAP)
        // RefdataValue.loc('Country',   [en: 'Austria', de: 'Österreich'], BOOTSTRAP)
        // RefdataValue.loc('Country',   [en: 'France', de: 'Frankreich'], BOOTSTRAP)
        // RefdataValue.loc('Country',   [en: 'Great Britain', de: 'Großbritannien'], BOOTSTRAP)
        // RefdataValue.loc('Country',   [en: 'United States of America', de: 'Vereinigte Staaten von Amerika'], BOOTSTRAP)
        // RefdataValue.loc('Country',   [en: 'Belgium', de: 'Belgien'], BOOTSTRAP)
        // RefdataValue.loc('Country',   [en: 'Italy', de: 'Italien'], BOOTSTRAP)
        // RefdataValue.loc('Country',   [en: 'Netherlands', de: 'Niederlande'], BOOTSTRAP)
        // RefdataValue.loc('Country',   [en: 'Italy', de: 'Italien'], BOOTSTRAP)

        // RefdataValue.loc('Person Function',     [en: 'Bestandsaufbau', de: 'Bestandsaufbau'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Direktion', de: 'Direktion'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Direktionsassistenz', de: 'Direktionsassistenz'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Erwerbungsabteilung', de: 'Erwerbungsabteilung'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Erwerbungsleitung', de: 'Erwerbungsleitung'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Medienbearbeitung', de: 'Medienbearbeitung'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Zeitschriftenabteilung', de: 'Zeitschriftenabteilung'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Fachreferat', de: 'Fachreferat'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Bereichsbibliotheksleitung', de: 'Bereichsbibliotheksleitung'], BOOTSTRAP) //Position

        //deactivated as March 21st, 2019 - the feature has been postponed into quartal II at least
        //RefdataValue.loc('Share Configuration', [key: 'only for consortia members',en:'only for my consortia members',de:'nur für meine Konsorten'], BOOTSTRAP)
        //RefdataValue.loc('Share Configuration', [en: 'everyone',de:'alle'], BOOTSTRAP)
         //RefdataValue.loc('Subscription Type',      [en: 'Collective Subscription', de: 'Kollektivlizenz'], BOOTSTRAP)

        createRefdataWithI10nExplanation()
    }

    void createRefdataWithI10nExplanation() {


        I10nTranslation.createOrUpdateI10n(RefdataValue.loc('Number Type',[en: 'Students', de: 'Studierende'], BOOTSTRAP),'expl',[en:'',de:'Gesamtzahl aller immatrikulierten Studierenden'])
        I10nTranslation.createOrUpdateI10n(RefdataValue.loc('Number Type',[en: 'Scientific staff', de: 'wissenschaftliches Personal'], BOOTSTRAP),'expl',[en:'',de:'zugehöriges wissenschaftliches Personal'])
        I10nTranslation.createOrUpdateI10n(RefdataValue.loc('Number Type',[en: 'User', de: 'Nutzer'], BOOTSTRAP),'expl',[en:'',de:'Nutzer der Einrichtung'])
        I10nTranslation.createOrUpdateI10n(RefdataValue.loc('Number Type',[en: 'Population', de: 'Einwohner'], BOOTSTRAP),'expl',[en:'',de:'Einwohner der Stadt'])
    }

    def setupOnixPlRefdata = {

        // Refdata values that need to be added to the database to allow ONIX-PL licenses to be compared properly. The code will
        // add them to the DB if they don't already exist.
        def refdatavalues = [
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

        refdatavalues.each { rdc, rdvList ->
            rdvList.each { rdv ->
                RefdataValue.loc(rdc, [en: rdv], BOOTSTRAP)
            }
        }

        // copied from Config.groovy .. END

        // -------------------------------------------------------------------
        // ONIX-PL Additions
        // -------------------------------------------------------------------

        //RefdataValue.loc('CostItemCategory', [en: 'Price', de: 'Preis'], BOOTSTRAP)
        //RefdataValue.loc('CostItemCategory', [en: 'Bank Charge', de: 'Bank Charge'], BOOTSTRAP)
        //RefdataValue.loc('CostItemCategory', [en: 'Refund', de: 'Erstattung'], BOOTSTRAP)
        //RefdataValue.loc('CostItemCategory', [en: 'Other', de: 'Andere'], BOOTSTRAP)

        /*
        RefdataCategory.loc('Entitlement Issue Status',
                [en: 'Entitlement Issue Status', de: 'Entitlement Issue Status'], BOOTSTRAP)

        RefdataValue.loc('Entitlement Issue Status', [en: 'Live', de: 'Live'], BOOTSTRAP)
        RefdataValue.loc('Entitlement Issue Status', [en: 'Current', de: 'Current'], BOOTSTRAP)
        RefdataValue.loc('Entitlement Issue Status', [en: 'Deleted', de: 'Deleted'], BOOTSTRAP)
        */

        RefdataValue.loc(RefdataCategory.IE_ACCEPT_STATUS, [en: 'Fixed', de: 'Feststehend'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.IE_ACCEPT_STATUS, [en: 'Under Negotiation', de: 'In Verhandlung'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.IE_ACCEPT_STATUS, [en: 'Under Consideration', de: 'Entscheidung steht aus'], BOOTSTRAP)

        RefdataValue.loc(RefdataCategory.LIC_TYPE, [en: 'Actual', de: 'Konkrete Instanz'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.LIC_TYPE, [en: 'Template', de: 'Vorlage'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.LIC_TYPE, [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)

        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Current', de: 'Aktiv'], BOOTSTRAP)
        //RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Deleted', de: 'Gelöscht'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'In Progress', de:'In Bearbeitung'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Retired', de: 'Abgelaufen'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Status not defined', de: 'Status nicht festgelegt'], BOOTSTRAP)

        RefdataValue.loc(RefdataCategory.PKG_LIST_STAT,  [en: 'Checked', de: 'Überprüft'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_LIST_STAT,  [en: 'In Progress', de: 'In Bearbeitung'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_LIST_STAT,  [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_BREAKABLE,  [en: 'No', de: 'Nein'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_BREAKABLE,  [en: 'Yes', de: 'Ja'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_BREAKABLE,  [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_CONSISTENT, [en: 'No', de: 'Nein'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_CONSISTENT, [en: 'Yes', de: 'Ja'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_CONSISTENT, [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_FIXED,      [en: 'No', de: 'Nein'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_FIXED,      [en: 'Yes', de: 'Ja'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_FIXED,      [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_SCOPE,      [en: 'Aggregator', de: 'Aggregator'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_SCOPE,      [en: 'Front File', de: 'Front File'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_SCOPE,      [en: 'Back File', de: 'Back File'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_SCOPE,      [en: 'Master File', de: 'Master File'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_SCOPE,      [en: 'Scope Undefined', de: 'Scope Undefined'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.PKG_SCOPE,      [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)

        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Current', de: 'Aktuell'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Deleted', de: 'Gelöscht'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'In Progress', de:'In Bearbeitung'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)

        RefdataValue.loc(RefdataCategory.TI_TYPE, [en: 'Journal', de: 'Journal'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TI_TYPE, [en: 'EBook', de: 'EBook'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TI_TYPE, [en: 'Database', de:'Datenbank'], BOOTSTRAP)

        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Current', de: 'Aktuell'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Expected', de: 'Expected'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Deleted', de: 'Gelöscht'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Transferred', de: 'Transferred'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Retired', de: 'im Ruhestand'], BOOTSTRAP)

        // Controlled values from the <UsageType> element.

        RefdataCategory.lookupOrCreate('UsageStatus', 'greenTick',      'UseForDataMining')
        RefdataCategory.lookupOrCreate('UsageStatus', 'greenTick',      'InterpretedAsPermitted')
        RefdataCategory.lookupOrCreate('UsageStatus', 'redCross',       'InterpretedAsProhibited')
        RefdataCategory.lookupOrCreate('UsageStatus', 'greenTick',      'Permitted')
        RefdataCategory.lookupOrCreate('UsageStatus', 'redCross',       'Prohibited')
        RefdataCategory.lookupOrCreate('UsageStatus', 'purpleQuestion', 'SilentUninterpreted')
        RefdataCategory.lookupOrCreate('UsageStatus', 'purpleQuestion', 'NotApplicable')

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

    def setupContentItems = {

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

    def setIdentifierNamespace() {

        def namespaces = [
                            [ns: "GND", typ: "com.k_int.kbplus.Creator"],
                            [ns: "ISIL"],
                            [ns: "ISIL_Paketsigel"],
                            [ns: "uri"],
                            [ns: "zdb"],
                            [ns: "zdb_ppn"],
                            [ns: "VIAF"],
                            [ns: "issn"],
                            [ns: "eissn"]
        ]

        namespaces.each { namespaceproperties ->
            def namespace = namespaceproperties["ns"]
            def typ = namespaceproperties["typ"]?:null
            //TODO isUnique/isHidden flags are set provisorically to "false", adaptations may be necessary
            IdentifierNamespace.findByNsIlike(namespace) ?: new IdentifierNamespace(ns: namespace, nsType: typ, isUnique: false, isHidden: false).save(flush: true);

        }

    }

    /*def setESGOKB() {
         ElasticsearchSource.findByIdentifier("gokb") ?: new ElasticsearchSource(name: 'GOKB ES', identifier: 'gokb', cluster: 'elasticsearch', index: 'gokb', host: '127.0.0.1', gokb_es: true)
    }*/
}
