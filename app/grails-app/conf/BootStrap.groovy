import com.k_int.kbplus.*

import com.k_int.kbplus.auth.*
import com.k_int.properties.PropertyDefinition
import com.k_int.properties.PropertyDefinitionGroup
import de.laser.OrgTypeService
import de.laser.SystemEvent
import de.laser.domain.I10nTranslation
import grails.converters.JSON
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils

import java.text.SimpleDateFormat

class BootStrap {

    def grailsApplication
    def dataloadService
    def apiService
    def refdataReorderService

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

        if (grailsApplication.config.laserSystemId != null) {
            def system_object = SystemObject.findBySysId(grailsApplication.config.laserSystemId) ?: new SystemObject(sysId: grailsApplication.config.laserSystemId).save(flush: true)
        }

        // TODO: remove due SystemEvent
        def evt_startup   = new EventLog(event: 'kbplus.startup', message: 'Normal startup', tstp: new Date(System.currentTimeMillis())).save(flush: true)

        SystemEvent.createEvent('BOOTSTRAP_STARTUP')

        def so_filetype   = DataloadFileType.findByName('Subscription Offered File') ?: new DataloadFileType(name: 'Subscription Offered File')
        def plat_filetype = DataloadFileType.findByName('Platforms File') ?: new DataloadFileType(name: 'Platforms File')

        // Reset harddata flag for given refdata and properties

        RefdataValue.executeUpdate('UPDATE RefdataValue rdv SET rdv.hardData =:reset', [reset: false])
        RefdataCategory.executeUpdate('UPDATE RefdataCategory rdc SET rdc.hardData =:reset', [reset: false])
        PropertyDefinition.executeUpdate('UPDATE PropertyDefinition pd SET pd.hardData =:reset', [reset: false])

        // Here we go ..

        log.debug("setupRefdata ..")
        setupRefdata()

        log.debug("reorderRefdata ..")
        refdataReorderService.reorderRefdata()

        log.debug("setupRolesAndPermissions ..")
        setupRolesAndPermissions()

        // Transforms types and formats Refdata

        RefdataCategory.loc('Transform Format', [en: 'Transform Format', de: 'Transform Format'], BOOTSTRAP)
        RefdataCategory.loc('Transform Type',   [en: 'Transform Type', de: 'Transform Type'], BOOTSTRAP)

        // !!! HAS TO BE BEFORE the script adding the Transformers as it is used by those tables !!!

        RefdataValue.loc('Transform Format', [en: 'json'], BOOTSTRAP)
        RefdataValue.loc('Transform Format', [en: 'xml'], BOOTSTRAP)
        RefdataValue.loc('Transform Format', [en: 'url'], BOOTSTRAP)

        RefdataValue.loc('Transform Type', [en: 'subscription'], BOOTSTRAP)
        RefdataValue.loc('Transform Type', [en: 'license'], BOOTSTRAP)
        RefdataValue.loc('Transform Type', [en: 'title'], BOOTSTRAP)
        RefdataValue.loc('Transform Type', [en: 'package'], BOOTSTRAP)

        // Add Transformers and Transforms defined in local config (laser-config.groovy)
        grailsApplication.config.systransforms.each { tr ->
            def transformName = tr.transforms_name //"${tr.name}-${tr.format}-${tr.type}"

            def transforms = Transforms.findByName("${transformName}")
            def transformer = Transformer.findByName("${tr.transformer_name}")
            if (transformer) {
                if (transformer.url != tr.url) {
                    log.debug("Change transformer [${tr.transformer_name}] url to ${tr.url}")
                    transformer.url = tr.url;
                    transformer.save(failOnError: true, flush: true)
                } else {
                    log.debug("${tr.transformer_name} present and correct")
                }
            } else {
                log.debug("Create transformer ${tr.transformer_name} ..")
                transformer = new Transformer(
                        name: tr.transformer_name,
                        url: tr.url).save(failOnError: true, flush: true)
            }

            log.debug("Create transform ${transformName} ..")
            def types = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('Transform Type'))
            def formats = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('Transform Format'))

            if (transforms) {

                if (tr.type) {
                    // split values
                    def type_list = tr.type.split(",")
                    type_list.each { new_type ->
                        if (! transforms.accepts_types.any { f -> f.value == new_type }) {
                            log.debug("Add transformer [${transformName}] type: ${new_type}")
                            def type = types.find { t -> t.value == new_type }
                            transforms.addToAccepts_types(type)
                        }
                    }
                }
                if (transforms.accepts_format.value != tr.format) {
                    log.debug("Change transformer [${transformName}] format to ${tr.format}")
                    def format = formats.findAll { t -> t.value == tr.format }
                    transforms.accepts_format = format[0]
                }
                if (transforms.return_mime != tr.return_mime) {
                    log.debug("Change transformer [${transformName}] return format to ${tr.'mime'}")
                    transforms.return_mime = tr.return_mime;
                }
                if (transforms.return_file_extention != tr.return_file_extension) {
                    log.debug("Change transformer [${transformName}] return format to ${tr.'return'}")
                    transforms.return_file_extention = tr.return_file_extension;
                }
                if (transforms.path_to_stylesheet != tr.path_to_stylesheet) {
                    log.debug("Change transformer [${transformName}] return format to ${tr.'path'}")
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

        if (grailsApplication.config.localauth) {
            log.debug("localauth is set.. ensure user accounts present (From local config file) ${grailsApplication.config.sysusers}")

            grailsApplication.config.sysusers.each { su ->
                log.debug("test ${su.name} ${su.pass} ${su.display} ${su.roles}")
                def user = User.findByUsername(su.name)
                if (user) {
                    if (user.password != su.pass) {
                        log.debug("Hard change of user password from config ${user.password} -> ${su.pass}")
                        user.password = su.pass;
                        user.save(failOnError: true)
                    } else {
                        log.debug("${su.name} present and correct")
                    }
                } else {
                    log.debug("Create user ..")
                    user = new User(
                            username: su.name,
                            password: su.pass,
                            display: su.display,
                            email: su.email,
                            enabled: true).save(failOnError: true)
                }

                log.debug("Add roles for ${su.name}")
                su.roles.each { r ->
                    def role = Role.findByAuthority(r)
                    if (! (user.authorities.contains(role))) {
                        log.debug("  -> adding role ${role}")
                        UserRole.create user, role
                    } else {
                        log.debug("  -> ${role} already present")
                    }
                }
            }
        }

        // def auto_approve_memberships = Setting.findByName('AutoApproveMemberships') ?: new Setting(name: 'AutoApproveMemberships', tp: Setting.CONTENT_TYPE_BOOLEAN, defvalue: 'true', value: 'true').save()

        def mailSent = Setting.findByName('MailSentDisabled') ?: new Setting(name: 'MailSentDisabled', tp: Setting.CONTENT_TYPE_BOOLEAN, defvalue: 'false', value: (grailsApplication.config.grails.mail.disabled ?: "false")).save()

        //def maintenance_mode = Setting.findByName('MaintenanceMode') ?: new Setting(name: 'MaintenanceMode', tp: Setting.CONTENT_TYPE_BOOLEAN, defvalue: 'false', value: 'false').save()

        def systemMessage = SystemMessage.findByText('Das System wird in den nächsten Minuten aktualisiert. Bitte pflegen Sie keine Daten mehr ein!') ?: new SystemMessage(text: 'Das System wird in den nächsten Minuten aktualisiert. Bitte pflegen Sie keine Daten mehr ein!', showNow: false).save()

        // SpringSecurityUtils.clientRegisterFilter( 'oracleSSOFilter', SecurityFilterPosition.PRE_AUTH_FILTER.order)
        // SpringSecurityUtils.clientRegisterFilter('securityContextPersistenceFilter', SecurityFilterPosition.PRE_AUTH_FILTER)
        SpringSecurityUtils.clientRegisterFilter('ediauthFilter', SecurityFilterPosition.PRE_AUTH_FILTER)
        //SpringSecurityUtils.clientRegisterFilter('apiauthFilter', SecurityFilterPosition.SECURITY_CONTEXT_FILTER.order + 10)
        SpringSecurityUtils.clientRegisterFilter('apiFilter', SecurityFilterPosition.BASIC_AUTH_FILTER)

        if (UserOrg.findAllByFormalRoleIsNull()?.size() > 0) {
            log.warn("There are user org rows with no role set. Please update the table to add role FKs")
        }

        log.debug("setOrgRoleGroups ..")
        setOrgRoleGroups()

        log.debug("setupOnixPlRefdata ..")
        setupOnixPlRefdata()

        log.debug("setupCurrencies ..")
        setupCurrencies()

        log.debug("setupContentItems ..")
        setupContentItems()

        log.debug("addDefaultJasperReports ..")
        addDefaultJasperReports()

        log.debug("addDefaultPageMappings ..")
        addDefaultPageMappings()

        log.debug("createOrgConfig ..")
        createOrgConfig()

        log.debug("createOrgProperties ..")
        createOrgProperties()

        log.debug("createLicenseProperties ..")
        createLicenseProperties()

        log.debug("createSubscriptionProperties ..")
        createSubscriptionProperties()

        log.debug("createSurveyProperties ..")
        createSurveyProperties()

        //log.debug("createPrivateProperties ..")
        //createPrivateProperties()

        log.debug("setIdentifierNamespace ..")
        setIdentifierNamespace()

        log.debug("check if database needs to be set up ...")
        if (!Org.findAll() && !Person.findAll() && !Address.findAll() && !Contact.findAll()) {
            apiService.setupBasicData()
        }
        else {
            log.debug("Data available, skipping ...")
            //System.exit(42)
        }

        log.debug("initializeDefaultSettings ..")
        initializeDefaultSettings()

//        log.debug("setESGOKB ..")
//        setESGOKB()

        log.debug("setJSONFormatDate ..")
        JSON.registerObjectMarshaller(Date) {
            return it?.format("yyyy-MM-dd'T'HH:mm:ss'Z'")
        }

        log.debug("Here we go ..")
    }

    def destroy = {
    }

    def setupRolesAndPermissions = {

        // Permissions

        def edit_permission = Perm.findByCode('edit') ?: new Perm(code: 'edit').save(failOnError: true)
        def view_permission = Perm.findByCode('view') ?: new Perm(code: 'view').save(failOnError: true)

        // Roles

        def or_lc_role            = RefdataValue.loc('Organisational Role', [en: 'Licensing Consortium', de:'Konsortium'], BOOTSTRAP)
        def or_licensee_role      = RefdataValue.loc('Organisational Role', [en: 'Licensee', de: 'Lizenznehmer'], BOOTSTRAP)
        def or_licensee_cons_role = RefdataValue.loc('Organisational Role', [key: 'Licensee_Consortial', en: 'Consortial licensee', de: 'Konsortiallizenznehmer'], BOOTSTRAP)

        def or_sc_role          = RefdataValue.loc('Organisational Role', [en: 'Subscription Consortia', de:'Konsortium'], BOOTSTRAP)
        def or_subscr_role      = RefdataValue.loc('Organisational Role', [en: 'Subscriber', de: 'Teilnehmer'], BOOTSTRAP)
        def or_subscr_cons_role = RefdataValue.loc('Organisational Role', [key: 'Subscriber_Consortial', en: 'Consortial subscriber', de: 'Konsortialteilnehmer'], BOOTSTRAP)

        def cl_owner_role       = RefdataValue.loc('Cluster Role',   [en: 'Cluster Owner'], BOOTSTRAP)
        def cl_member_role      = RefdataValue.loc('Cluster Role',   [en: 'Cluster Member'], BOOTSTRAP)

        // TODO: refactoring: partOf

        def combo1 = RefdataValue.loc('Combo Type',     [en: 'Consortium', de: 'Konsortium'], BOOTSTRAP)
        def combo2 = RefdataValue.loc('Combo Type',     [en: 'Institution', de: 'Einrichtung'], BOOTSTRAP)
        def combo3 = RefdataValue.loc('Combo Type',     [en: 'Department', de: 'Abteilung'], BOOTSTRAP)

        // Global System Roles

        def yodaRole    = Role.findByAuthority('ROLE_YODA')        ?: new Role(authority: 'ROLE_YODA', roleType: 'transcendent').save(failOnError: true)
        def adminRole   = Role.findByAuthority('ROLE_ADMIN')       ?: new Role(authority: 'ROLE_ADMIN', roleType: 'global').save(failOnError: true)
        def dmRole      = Role.findByAuthority('ROLE_DATAMANAGER') ?: new Role(authority: 'ROLE_DATAMANAGER', roleType: 'global').save(failOnError: true)
        def userRole    = Role.findByAuthority('ROLE_USER')        ?: new Role(authority: 'ROLE_USER', roleType: 'global').save(failOnError: true)
        def apiRole     = Role.findByAuthority('ROLE_API')         ?: new Role(authority: 'ROLE_API', roleType: 'global').save(failOnError: true)

        def globalDataRole    = Role.findByAuthority('ROLE_GLOBAL_DATA')        ?: new Role(authority: 'ROLE_GLOBAL_DATA', roleType: 'global').save(failOnError: true)
        def orgEditorRole     = Role.findByAuthority('ROLE_ORG_EDITOR')         ?: new Role(authority: 'ROLE_ORG_EDITOR', roleType: 'global').save(failOnError: true)
        def orgComRole        = Role.findByAuthority('ROLE_ORG_COM_EDITOR')     ?: new Role(authority: 'ROLE_ORG_COM_EDITOR', roleType: 'global').save(failOnError: true)
        def packageEditorRole = Role.findByAuthority('ROLE_PACKAGE_EDITOR')     ?: new Role(authority: 'ROLE_PACKAGE_EDITOR', roleType: 'global').save(failOnError: true)
        def statsEditorRole   = Role.findByAuthority('ROLE_STATISTICS_EDITOR')  ?: new Role(authority: 'ROLE_STATISTICS_EDITOR', roleType: 'global').save(failOnError: true)
        def ticketEditorRole  = Role.findByAuthority('ROLE_TICKET_EDITOR')      ?: new Role(authority: 'ROLE_TICKET_EDITOR', roleType: 'global').save(failOnError: true)

        // Institutional Roles

        def instAdmin = Role.findByAuthority('INST_ADM')
        if (! instAdmin) {
            instAdmin = new Role(authority: 'INST_ADM', roleType: 'user').save(failOnError: true)
        }
        ensurePermGrant(instAdmin, edit_permission)
        ensurePermGrant(instAdmin, view_permission)

        def instEditor = Role.findByAuthority('INST_EDITOR')
        if (! instEditor) {
            instEditor = new Role(authority: 'INST_EDITOR', roleType: 'user').save(failOnError: true)
        }
        ensurePermGrant(instEditor, edit_permission)
        ensurePermGrant(instEditor, view_permission)

        def instUser = Role.findByAuthority('INST_USER')
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

        def fakeRole                = locOrgRole('FAKE',                  'fake', [de: 'Keine Zuweisung', en: 'Nothing'])
        def orgBasicRole            = locOrgRole('ORG_BASIC',              'org', [en: 'Institution basic', de: 'Singlenutzer'])
        def orgMemberRole           = locOrgRole('ORG_MEMBER',             'org', [en: 'Institution consortium member', de: 'Konsorte'])
        def orgConsortiumRole       = locOrgRole('ORG_CONSORTIUM',         'org', [en: 'Consortium basic', de: 'Konsortium ohne Umfragefunktion'])
        def orgConsortiumSurveyRole = locOrgRole('ORG_CONSORTIUM_SURVEY',  'org', [en: 'Consortium survey', de: 'Konsortium mit Umfragefunktion'])
        def orgCollectiveRole       = locOrgRole('ORG_COLLECTIVE',         'org', [en: 'Institution collective', de: 'Kollektivnutzer'])

        createOrgPerms(orgBasicRole, ['ORG_BASIC'])
        createOrgPerms(orgMemberRole, ['ORG_MEMBER'])
        createOrgPerms(orgConsortiumRole, ['ORG_CONSORTIUM', 'ORG_MEMBER'])
        createOrgPerms(orgConsortiumSurveyRole, ['ORG_CONSORTIUM_SURVEY', 'ORG_CONSORTIUM', 'ORG_MEMBER'])
        createOrgPerms(orgCollectiveRole, ['ORG_COLLECTIVE'])

    }

    def initializeDefaultSettings(){

        def admObj = SystemAdmin.list()
        if (! admObj) {
            log.debug("No SystemAdmin object found, creating new")
            admObj = new SystemAdmin(name:"demo").save()
        } else {
            admObj = admObj.first()
        }
        //Will not overwrite any existing database properties.
        createDefaultSysProps(admObj)
        admObj.refresh()
        log.debug("Finished updating config from SystemAdmin")
    }

    def createDefaultSysProps(admObj){

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
                log.debug("Unable to locate property definition for ${name} .. creating")
                pd = new PropertyDefinition(name: name)
            }

            pd.type  = prop.type
            pd.descr = prop.descr['en']
            //pd.softData = false
            pd.hardData = BOOTSTRAP
            pd.save(failOnError: true)

            if (! SystemAdminCustomProperty.findByType(pd)) {
                def newProp = new SystemAdminCustomProperty(type: pd, owner: admObj, stringValue: prop.val, note: prop.note)
                newProp.save()
            }
        }
    }

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
                    name: [en: "Course pack term note", de: "Notzifeld zu den Bedingungen zur Nutzung elektronischer Skripte"],
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
                    name: [en: "Course reserve term note", de: "Notizfeld zu den Bedingungen der Nutzung für Semesterapparate mit Zugangsbeschränkung"],
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
            [
                    name: [en: "Datamining", de: "Datamining"],
                    expl: [en: "", de: "Aussagen darüber, ob bzw. wie das Material im Kontext des Datamining zu Verfügung steht, ferner Informationen über weiter zu berücksichtigende Aspekte wie Nutzungsbedingungen, Sicherheitserklärungen und Datenvernichtung."],
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
                    name: [en: "Scholarly sharing term note", de: "Weitergabe im Rahmen der Lehre"],
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
                    descr:allDescr, type: OT.Rdv, cat:'YNU', isUsedForLogic: false
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

    def createSubscriptionProperties() {

        def allDescr = [en: PropertyDefinition.SUB_PROP, de: PropertyDefinition.SUB_PROP]

        def requiredProps = [
                [
                        name: [en: "GASCO Entry", de: "GASCO-Eintrag"],
                        expl : [en: "", de: "Dieses Merkmal als Konsortialstelle auf \"ja\" setzen, um eine Lizenz im GASCO-Monitor aufzunehmen."],
                        descr:allDescr, type: OT.Rdv, cat:'YN', isUsedForLogic: true
                ],
                [
                        name: [en: "GASCO-Anzeigename", de: "GASCO-Anzeigename"],
                        expl : [en: "", de: "Dieses Merkmal setzen, um bei Bedarf im GASCO-Monitor einen anderen Lizenznamen anzugeben als in LAS:eR aufgenommen."],
                        descr:allDescr, type: OT.String, isUsedForLogic: true
                ],
                [
                        name: [en: "GASCO-Verhandlername", de: "GASCO-Verhandlername"],
                        expl : [en: "", de: "Dieses Merkmal als Konsortialstelle verwenden, um im GASCO-Monitor einen anderen Verhandlungsführer-Namen anzugeben."],
                        descr:allDescr, type: OT.String, isUsedForLogic: true
                ],
                [
                        name: [en: "GASCO-Information-Link", de: "GASCO-Informations-Link"],
                        expl : [en: "", de: "Unter welchem Link finden sich Informationen zum Produkt?"],
                        descr:allDescr, type: OT.URL, isUsedForLogic: true
                ],
                [
                        name: [en: "EZB Gelbschaltung", de: "EZB Gelbschaltung"],
                        expl : [en: "", de: "Wird eine Gelbschaltung in der EZB vorgenommen?"],
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
                        name: [en: "Preisvorteil durch weitere Produktteilnahme", de: "Preisvorteil durch weitere Produktteilnahme"],
                        expl : [en: "", de: "Kann durch die Lizenzierung eines weiteren Produktes ein Preisvorteil gesichert werden?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Produktabhängigkeit", de: "Produktabhängigkeit"],
                        expl : [en: "", de: "Ist die Lizenz von einem anderen Produkt abhängig?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Bundesweit offen", de: "Bundesweit offen"],
                        expl : [en: "", de: "Hat die Lizenz eine überregionale Ausrichtung?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Rechnungsstellung durch Anbieter", de: "Rechnungsstellung durch Anbieter"],
                        expl: [en: "", de: "Erfolgt die Rechnungsstellung direkt über den Anbieter?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Mengenrabatt Stichtag", de: "Mengenrabatt Stichtag"],
                        expl: [en: "", de: "Wann ist der Stichtag für die Findung der erreichten Rabattstufe?"],
                        descr:allDescr, type: OT.Date, cat:'YN'
                ],
                [
                        name: [en: "Testzeitraum", de: "Testzeitraum"],
                        expl: [en: "", de: "Wie lange ermöglicht der Anbieter einen kostenfreien Testzugriff?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Unterjähriger Einstieg", de: "Unterjähriger Einstieg"],
                        expl: [en: "", de: "Ist ein unterjähriger Einstieg in die Lizenz möglich?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Neueinsteigerrabatt", de: "Neueinsteigerrabatt"],
                        expl : [en: "", de: "Existiert ein zusätzlicher Neueinsteigerpreis?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Simuser", de: "Simuser"],
                        expl : [en: "", de: "Sind die simuser relevant für die Preisbestimmung?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Simuser Zahl", de: "Simuser Zahl"],
                        expl : [en: "", de: "Wieviele gleichzeitige Nutzerzugriffe umfasst die Lizenz?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Rechnungszeitpunkt", de: "Rechnungszeitpunkt"],
                        expl : [en: "", de: "Zeitpunkt der Rechnung."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Zahlungsziel", de: "Zahlungsziel"],
                        expl : [en: "", de: "Zahlungsziel"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Preis gerundet", de: "Preis gerundet"],
                        expl : [en: "", de: "Gerundeter Preis."],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Teilzahlung", de: "Teilzahlung"],
                        expl : [en: "", de: "Ist bei der Lizenz eine Teilzahlung der Rechnung vorgesehen?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Statistik", de: "Statistik"],
                        expl : [en: "", de: ""],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Statistikzugang", de: "Statistikzugang"],
                        expl : [en: "", de: "Mit welchen Zugangsdaten können die Statistiken abgerufen werden?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [key: "StatisticsLink", en: "Statistics Link", de: "Statistik-Link"],
                        expl : [en: "", de: "Über welchen Link können die Statistiken abgerufen werden?"],
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
                        name: [en: "Private Einrichtungen", de: "Private Einrichtungen"],
                        expl: [en: "", de: "Dürfen auch private Einrichtungen an der Lizenz teilnehmen?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Mehrjahreslaufzeit", de: "Mehrjahreslaufzeit"],
                        expl: [en: "", de: "Mehrjahreslaufzeit."],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Rabatt", de: "Rabatt"],
                        expl: [en: "", de: "Höhe des Rabattes."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Rabattstaffel", de: "Rabattstaffel"],
                        expl: [en: "", de: "Wie sieht die Rabattstaffel für die Lizenz aus?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Rabatt Zählung", de: "Rabatt Zählung"],
                        expl: [en: "", de: "Wie wird die Rabatthöhe errechnet? Z.B. durch Zählung aller Teilnehmer bei dem Anbieter…"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Kündigungsfrist", de: "Kündigungsfrist"],
                        expl: [en: "", de: "Kündigungsfrist."],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Zusätzliche Software erforderlich?", de: "Zusätzliche Software erforderlich?"],
                        expl : [en: "", de: "Wird für die Lizenzierung eine zusätzliche Software benötigt?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Preissteigerung", de: "Preissteigerung"],
                        expl: [en: "", de: "Preissteigerung."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Preis abhängig von", de: "Preis abhängig von"],
                        expl : [en: "", de: "Ist der Preis von etwas abhängig?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Abbestellquote", de: "Abbestellquote"],
                        expl: [en: "", de: "Abbestellquote."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Bestellnummer im Erwerbungssystem", de: "Bestellnummer im Erwerbungssystem"],
                        expl: [en: "", de: "Bestellnummer im Erwerbungssystem."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Zugangskennungen für Nutzer (pro Zeitschrift)", de: "Zugangskennungen für Nutzer (pro Zeitschrift)"],
                        expl: [en: "", de: "Zugangskennungen für Nutzer (pro Zeitschrift)."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [key: "TaxExemption", en: "TaxExemption", de: "Steuerbefreiung"],
                        expl : [en: "", de: "Liegt eine Steuerbefreiung für die Lizenz vor?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Subscriptionsnummer vom Verlag", de: "Subskriptionsnummer des Verlags"],
                        expl: [en: "", de: "Subskriptionsnummer des Verlags."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Subskriptionsnummer des Lieferanten", de: "Subskriptionsnummer des Lieferanten"],
                        expl: [en: "", de: "Subskriptionsnummer des Lieferanten."],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "DBIS-Eintrag", de: "DBIS-Eintrag"],
                        expl: [en: "", de: "Existiert ein DBIS-Eintrag?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "DBIS-Link", de: "DBIS-Link"],
                        expl: [en: "", de: "Link zum DBIS-Eintrag."],
                        descr:allDescr, type: OT.URL
                ],
                [
                        name: [en: "Abbestellgrund", de: "Abbestellgrund"],
                        expl: [en: "", de: "Welchen Grund gab es für die Abbestellung?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Hosting-Gebühr", de: "Hosting-Gebühr"],
                        expl: [en: "", de: "Ist eine Hosting-Gebühr zu entrichten?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "Pick&Choose-Paket", de: "Pick&Choose-Paket"],
                        expl: [en: "", de: "Handelt es sich um ein Paket mit Einzeltitelauswahl (Pick & Choose)?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "PDA/EBS-Programm", de: "PDA/EBS-Programm"],
                        expl: [en: "", de: "Basiert die Lizenz auf einem PDA-, EBS- oder EBA-Modell?"],
                        descr:allDescr, type: OT.String
                ],
                                //[name: [en: "Produktsigel beantragt", de: "Produktsigel beantragt"],                    descr:allDescr, type: OT.String],
                [
                        name: [en: "Fachstatistik / Klassifikation", de: "Fachstatistik / Klassifikation"],
                        expl: [en: "", de: "Fachstatistik / Klassifikation"],
                        descr:allDescr, type: OT.Int
                ],
                [
                        name: [en: "Archivzugriff", de: "Archivzugriff"],
                        expl: [en: "", de: "Gibt es einen Archivzugriff?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ],
                [
                        name: [en: "Eingeschränkter Benutzerkreis", de: "Eingeschränkter Benutzerkreis"],
                        expl: [en: "", de: "Welche Einschränkung des Benutzerkreises gibt es?"],
                        descr:allDescr, type: OT.String
                ],
                [
                        name: [en: "SFX-Eintrag", de: "SFX-Eintrag"],
                        expl: [en: "", de: "Gibt es einen SFX-Eintrag?"],
                        descr:allDescr, type: OT.Rdv, cat:'YN'
                ]
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredProps)
    }

    def createSurveyProperties() {

        def requiredProps = [
                [name: [en: "Continue to license", de: "Weiter lizenzieren?"], type: OT.Rdv, cat:'YN'],
                [name: [en: "Interested", de: "Interessiert?"], type: OT.Rdv, cat:'YN']
        ]
        createSurveyPropertiesWithI10nTranslations(requiredProps)
    }

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
                    prop = PropertyDefinition.findByNameAndTenant(default_prop.name['en'], tenant)
                } else {
                    log.debug("Unable to locate tenant: ${default_prop.tenant} .. ignored")
                    return
                }
            } else {
                prop = PropertyDefinition.findWhere(name: default_prop.name['en'], tenant: null)
            }

            if (! prop) {
                if (tenant) {
                    log.debug("Unable to locate private property definition for ${default_prop.name['en']} for tenant: ${tenant} .. creating")
                    prop = new PropertyDefinition(name: default_prop.name['en'], tenant: tenant)
                } else {
                    log.debug("Unable to locate property definition for ${default_prop.name['en']} .. creating")
                    prop = new PropertyDefinition(name: default_prop.name['en'])
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
            prop.descr = default_prop.descr['en']
            //prop.softData = false
            prop.hardData = BOOTSTRAP
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
                    log.debug("Unable to locate owner: ${default_prop.owner} .. ignored")
                    return
                }
            } else {
                surveyProperty = SurveyProperty.findWhere(name: default_prop.name['en'], owner: null)
            }

            if (! surveyProperty) {
                if (owner) {
                    log.debug("Unable to locate private survey property definition for ${default_prop.name['en']} for owner: ${owner} .. creating")
                    surveyProperty = new SurveyProperty(name: default_prop.name['en'], owner: owner)
                } else {
                    log.debug("Unable to locate survey property definition for ${default_prop.name['en']} .. creating")
                    surveyProperty = new SurveyProperty(name: default_prop.name['en'])
                }
            }

            if (default_prop.cat != null) {
                surveyProperty.setRefdataCategory(default_prop.cat)
            }

            surveyProperty.type  = default_prop.type
            //prop.softData = false
            surveyProperty.hardData = BOOTSTRAP
            surveyProperty.save(failOnError: true)

            I10nTranslation.createOrUpdateI10n(surveyProperty, 'name', default_prop.name)

            if (default_prop.explain) {
                I10nTranslation.createOrUpdateI10n(surveyProperty, 'explain', default_prop.expl)
            }

            if (default_prop.introduction) {
                I10nTranslation.createOrUpdateI10n(surveyProperty, 'introduction', default_prop.expl)
            }
        }
    }

    def addDefaultPageMappings() {

        if (! SitePage.findAll()) {
            def home    = new SitePage(alias: "Home",    action: "index", controller: "home").save()
            def profile = new SitePage(alias: "Profile", action: "index", controller: "profile").save()
            //def pages   = new SitePage(alias: "Pages",   action: "managePages", controller: "spotlight").save()

            dataloadService.updateSiteMapping()
        }
    }

    def addDefaultJasperReports() {
        //Add default Jasper reports, if there are currently no reports in DB

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
                log.error("Jasper Report creation for " + reportName + ".jrxml failed with errors: \n")
                newReport.errors.each {
                    log.error(it + "\n")
                }
            }
        }
  }

    def ensurePermGrant(role, perm) {
        def existingPermGrant = PermGrant.findByRoleAndPerm(role,perm)
        if (! existingPermGrant) {
            log.debug("Create new perm grant for ${role}, ${perm}")
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

        RefdataCategory.loc('Organisational Role',  [en: 'Organisational Role', de: 'Organisational Role'], BOOTSTRAP)

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

        RefdataCategory.loc('filter.fake.values', [en: 'filter.fake.values', de: 'filter.fake.values'], BOOTSTRAP)
        RefdataValue.loc('filter.fake.values',   [key: 'subscription.status.no.status.set.but.null', en: 'No Status', de: 'Kein Status'], BOOTSTRAP)
        RefdataValue.loc('filter.fake.values',   [key: 'generic.null.value', en: 'Not set', de: 'Nicht gesetzt'], BOOTSTRAP)
        // refdata categories

        RefdataCategory.loc('YN',                   	                    [en: 'Yes/No', de: 'Ja/Nein'], BOOTSTRAP)
        RefdataCategory.loc('YNU',                   	                    [en: 'Yes/No/Unknown', de: 'Ja/Nein/Unbekannt'], BOOTSTRAP)
        RefdataCategory.loc('YNO',                  	                    [en: 'Yes/No/Others', de: 'Ja/Nein/Anderes'], BOOTSTRAP)
        RefdataCategory.loc('Permissions',                                  [en: 'Permissions', de: 'Berechtigungen'], BOOTSTRAP)
        RefdataCategory.loc('Existence',                                    [en: 'Existence', de: 'Vorliegen'], BOOTSTRAP)
        RefdataCategory.loc('Indemnification',                              [en: 'Indemnification Choice', de: 'Entschädigung Auswahl'], BOOTSTRAP)
        RefdataCategory.loc('Confidentiality',                              [en: 'Confidentiality Choice', de: 'Vertraulichkeit Auswahl'], BOOTSTRAP)
        RefdataCategory.loc('Termination Condition',                        [en: 'Termination Condition', de: 'Kündigung Voraussetzung'], BOOTSTRAP)
        RefdataCategory.loc('AddressType',          	                    [en: 'Address Type', de: 'Art der Adresse'], BOOTSTRAP)
        RefdataCategory.loc('Cluster Type',         	                    [en: 'Cluster Type', de: 'Cluster Type'], BOOTSTRAP)
        RefdataCategory.loc('CreatorType',         	                        [en: 'Creator Type', de: 'Creator Type'], BOOTSTRAP)
        RefdataCategory.loc('Combo Type',           	                    [en: 'Combo Type', de: 'Combo Type'], BOOTSTRAP)
        RefdataCategory.loc('ConcurrentAccess',     	                    [en: 'Concurrent Access', de: 'SimUser'], BOOTSTRAP)
        RefdataCategory.loc('ContactContentType',   	                    [en: 'Type of Contact', de: 'Kontakttyp'], BOOTSTRAP)
        RefdataCategory.loc('ContactType',          	                    [en: 'Contact Type', de: 'Art des Kontaktes'], BOOTSTRAP)
        RefdataCategory.loc('CoreStatus',           	                    [en: 'Core Status', de: 'Kerntitel-Status'], BOOTSTRAP)
        RefdataCategory.loc('Cost configuration',                        [en: 'Cost configuration', de: 'Kostenkonfiguration'], BOOTSTRAP)
        RefdataCategory.loc('Country',              	                    [en: 'Country', de: 'Land'], BOOTSTRAP)
        RefdataCategory.loc('FactType',             	                    [en: 'FactType', de: 'FactType'], BOOTSTRAP)
        RefdataCategory.loc('Federal State',        	                    [en: 'Federal State', de: 'Bundesland'], BOOTSTRAP)
        RefdataCategory.loc('Funder Type',          	                    [en: 'Funder Type', de: 'Trägerschaft'], BOOTSTRAP)
        RefdataCategory.loc('Gender',               	                    [en: 'Gender', de: 'Geschlecht'], BOOTSTRAP)
        RefdataCategory.loc('Invoicing',               	                    [en: 'Invoicing', de: 'Rechnungsstellung'], BOOTSTRAP)
        RefdataCategory.loc('Library Network',      	                    [en: 'Library Network', de: 'Verbundzugehörigkeit'], BOOTSTRAP)
        RefdataCategory.loc('Library Type',         	                    [en: 'Library Type', de: 'Bibliothekstyp'], BOOTSTRAP)
        RefdataCategory.loc('OrgSector',            	                    [en: 'OrgSector', de: 'Bereich'], BOOTSTRAP)
        RefdataCategory.loc('OrgStatus',                	                [en: 'Status', de: 'Status'], BOOTSTRAP)
        RefdataCategory.loc('OrgRoleType',              	                    [en: 'Organisation Type', de: 'Organisationstyp'], BOOTSTRAP)
        RefdataCategory.loc('Person Function',      	                    [en: 'Person Function', de: 'Funktion'], BOOTSTRAP)
        RefdataCategory.loc('Person Contact Type',  	                    [en: 'Person: Contact Type', de: 'Kontaktart'], BOOTSTRAP)
        RefdataCategory.loc('Person Position',      	                    [en: 'Person Position', de: 'Position'], BOOTSTRAP)
        RefdataCategory.loc('Person Responsibility',	                    [en: 'Person Responsibility', de: 'Verantwortlich'], BOOTSTRAP)
        RefdataCategory.loc('Subscription Form',          	                [en: 'Subscription Form', de: 'Lizenzform'], BOOTSTRAP)
        RefdataCategory.loc('Subscription Resource',          	            [en: 'Resource type', de: 'Ressourcentyp'], BOOTSTRAP)
        RefdataCategory.loc('Subscription Status',          	            [en: 'Subscription Status', de: 'Lizenzstatus'], BOOTSTRAP)
        //RefdataCategory.loc('system.customer.type',          	            [en: 'Customer Type', de: 'Kundentyp'], BOOTSTRAP)
        RefdataCategory.loc('Task Priority',                	            [en: 'Task Priority', de: 'Aufgabenpriorität'], BOOTSTRAP)
        RefdataCategory.loc('Task Status',          	                    [en: 'Task Status', de: 'Aufgabenstatus'], BOOTSTRAP)
        RefdataCategory.loc('Ticket.Category',          	                  [en: 'Ticket Category', de: 'Kategorie'], BOOTSTRAP)
        RefdataCategory.loc('Ticket.Status',          	                      [en: 'Ticket Status', de: 'Ticketstatus'], BOOTSTRAP)
        RefdataCategory.loc('License.RemoteAccess',          	                      [en: 'Remote Access', de: 'Remote-Zugriff'], BOOTSTRAP)
        RefdataCategory.loc('License.OA.ReceivingModalities',               [en: 'Receiving Modalities', de: 'Bezugsmodalitäten'], BOOTSTRAP)
        RefdataCategory.loc('License.OA.Repository',                        [en: 'Repository', de: 'Repositorium'], BOOTSTRAP)
        RefdataCategory.loc('License.OA.CorrespondingAuthorIdentification', [en: 'Corresponding Author Identification', de: 'Autorenindentifikation'], BOOTSTRAP)
        RefdataCategory.loc('License.OA.LicenseToPublish',                  [en: 'License to Publish', de: 'Publikationslizenz'], BOOTSTRAP)
        RefdataCategory.loc('License.Arc.PaymentNote',                      [en: 'Archive Payment Note', de: 'Zugriffsrechte Kosten'], BOOTSTRAP)
		RefdataCategory.loc('License.Arc.TitletransferRegulation',          [en: 'Titletransfer Regulation', de: 'Titeltransfer Regeln'], BOOTSTRAP)
		RefdataCategory.loc('License.Arc.ArchivalCopyCost',                 [en: 'Archival Copy Cost', de: 'Archivkopie Kosten'], BOOTSTRAP)
		RefdataCategory.loc('License.Arc.ArchivalCopyTime',                 [en: 'Archival Copy Time', de: 'Archivkopie Zeitpunkt'], BOOTSTRAP)
        RefdataCategory.loc('License.Arc.ArchivalCopyContent',              [en: 'Archival Copy Content', de: 'Archivkopie Form'], BOOTSTRAP)
        RefdataCategory.loc('License.Arc.ArchivalCopyTransmissionFormat',      [en: 'Archival Copy Transmission Format', de: 'Archivkopie Übermittlungsformat'], BOOTSTRAP)
        RefdataCategory.loc('License.Arc.HostingTime',                      [en: 'Hosting Time', de: 'Hostingrecht Zeitpunkt'], BOOTSTRAP)
        RefdataCategory.loc('License.Arc.Authorized',                       [en: 'Hosting Authorized', de: 'Hostingrecht Berechtigte'], BOOTSTRAP)
        RefdataCategory.loc('License.Arc.HostingRestriction',               [en: 'Hosting Restriction', de: 'Hostingrecht Einschränkung'], BOOTSTRAP)
        RefdataCategory.loc('License.Arc.HostingSolution',                  [en: 'Hosting Solution', de: 'Hostingrecht Lösung'], BOOTSTRAP)
        RefdataCategory.loc('License.Statistics.Standards',                  [en: 'Statistics Standards', de: 'Statistikstandard'], BOOTSTRAP)
        RefdataCategory.loc('License.Statistics.Delivery',                  [en: 'Statistics Delivery', de: 'Statistik Liefermethode'], BOOTSTRAP)
        RefdataCategory.loc('License.Statistics.Format',                  [en: 'Statistics Format', de: 'Statistik Auslieferungsformat'], BOOTSTRAP)
        RefdataCategory.loc('License.Statistics.Frequency',                  [en: 'Statistics Frequency', de: 'Statistik Auslieferungsfrequenz'], BOOTSTRAP)
        RefdataCategory.loc('License.Statistics.UserCreds',                  [en: 'Statistics User Credentials', de: 'Statistik Nutzeridentifikation'], BOOTSTRAP)
        RefdataCategory.loc('Package Status',                               [en: 'Package Status', de: 'Paketstatus'], BOOTSTRAP)
        RefdataCategory.loc('Number Type',                                  [en: 'Number Type', de: 'Zahlen-Typ'], BOOTSTRAP)
        RefdataCategory.loc('Semester',                                  [en: 'Semester', de: 'Semester'], BOOTSTRAP)
        RefdataCategory.loc('User.Settings.Dashboard.Tab',                  [en: 'Dashboard Tab', de: 'Dashbord Tab'], BOOTSTRAP)
        RefdataCategory.loc('User.Settings.Theme',                  [en: 'Theme', de: 'Theme'], BOOTSTRAP)
        RefdataCategory.loc('Survey Type',                          [en: 'Survey Type', de: 'Umfrage-Typ'], BOOTSTRAP)
        RefdataCategory.loc('Survey Status',                        [en: 'Survey Status', de: 'Umfrage-Status'], BOOTSTRAP)

        // refdata values

        RefdataValue.loc('YN',   [en: 'Yes', de: 'Ja'], BOOTSTRAP)
        RefdataValue.loc('YN',   [en: 'No', de: 'Nein'], BOOTSTRAP)

        RefdataValue.loc('YNU',   [en: 'Yes', de: 'Ja'], BOOTSTRAP)
        RefdataValue.loc('YNU',   [en: 'No', de: 'Nein'], BOOTSTRAP)
        RefdataValue.loc('YNU',   [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)

        RefdataValue.loc('YNO',  [en: 'Yes', de: 'Ja'], BOOTSTRAP)
        RefdataValue.loc('YNO',  [en: 'No', de: 'Nein'], BOOTSTRAP)
        RefdataValue.loc('YNO',  [en: 'Not applicable', de: 'Nicht zutreffend'], BOOTSTRAP)
        RefdataValue.loc('YNO',  [en: 'Planed', de: 'Geplant'], BOOTSTRAP)
        RefdataValue.loc('YNO',  [en: 'Unknown', de: 'Unklar'], BOOTSTRAP)
        RefdataValue.loc('YNO',  [en: 'Other', de: 'Andere'], BOOTSTRAP)

        RefdataValue.loc('Permissions',  [en: 'Permitted (explicit)', de: 'Ausdrücklich erlaubt'], BOOTSTRAP)
        RefdataValue.loc('Permissions',  [en: 'Permitted (interpreted)', de: 'Vermutlich erlaubt'], BOOTSTRAP)
        RefdataValue.loc('Permissions',  [en: 'Prohibited (explicit)', de: 'Ausdrücklich verboten'], BOOTSTRAP)
        RefdataValue.loc('Permissions',  [en: 'Prohibited (interpreted)', de: 'Vermutlich verboten'], BOOTSTRAP)
        RefdataValue.loc('Permissions',  [en: 'Silent', de: 'Stillschweigend'], BOOTSTRAP)
        RefdataValue.loc('Permissions',  [en: 'Not applicable', de: 'Nicht zutreffend'], BOOTSTRAP)
        RefdataValue.loc('Permissions',  [en: 'Unknown', de: 'Unklar'], BOOTSTRAP)

        RefdataValue.loc('Existence',   [en: 'Existent', de: 'Bestehend'], BOOTSTRAP)
        RefdataValue.loc('Existence',   [en: 'Nonexistend', de: 'Fehlend'], BOOTSTRAP)

        RefdataValue.loc('Indemnification',  [en: 'General', de: 'Generell'], BOOTSTRAP)
        RefdataValue.loc('Indemnification',  [en: 'Intellectual Property Only', de: 'Nur geistiges Eigentum'], BOOTSTRAP)
        RefdataValue.loc('Indemnification',  [en: 'Other', de: 'Andere'], BOOTSTRAP)
        RefdataValue.loc('Indemnification',  [en: 'Unknown', de: 'Unklar'], BOOTSTRAP)

        RefdataValue.loc('Confidentiality',  [en: 'All', de: 'Alles'], BOOTSTRAP)
        RefdataValue.loc('Confidentiality',  [en: 'All but user terms', de: 'Alles außer Nutzungsbedingungen'], BOOTSTRAP)
        RefdataValue.loc('Confidentiality',  [en: 'Financial only', de: 'Nur Finanzangelegenheiten'], BOOTSTRAP)
        RefdataValue.loc('Confidentiality',  [en: 'No', de: 'Nein'], BOOTSTRAP)
        RefdataValue.loc('Confidentiality',  [en: 'Unknown', de: 'Unklar'], BOOTSTRAP)

        RefdataValue.loc('Termination Condition',  [en: 'At will', de: 'Nach Belieben'], BOOTSTRAP)
        RefdataValue.loc('Termination Condition',  [en: 'Breach by Licensor/Licensee', de: 'Wegen Verstoß des Vertragspartners'], BOOTSTRAP)
        RefdataValue.loc('Termination Condition',  [en: 'Other', de: 'Andere Gründe'], BOOTSTRAP)
        RefdataValue.loc('Termination Condition',  [en: 'Unknown', de: 'Unklar'], BOOTSTRAP)

        RefdataValue.loc('AddressType', [en: 'Postal address', de: 'Postanschrift'], BOOTSTRAP)
        RefdataValue.loc('AddressType', [en: 'Billing address', de: 'Rechnungsanschrift'], BOOTSTRAP)
        RefdataValue.loc('AddressType', [en: 'Delivery address', de: 'Lieferanschrift'], BOOTSTRAP)
        RefdataValue.loc('AddressType', [en: 'Library address', de: 'Anschrift'], BOOTSTRAP)
        RefdataValue.loc('AddressType', [en: 'Legal patron address', de: 'Anschrift des rechtlichen Trägers'], BOOTSTRAP)

        RefdataValue.loc('ClusterType', [en: 'Undefined'], BOOTSTRAP)

        RefdataValue.loc('CreatorType', [en: 'Author', de: 'Autor'], BOOTSTRAP)
        RefdataValue.loc('CreatorType', [en: 'Editor', de: 'Herausgeber'], BOOTSTRAP)

        RefdataValue.loc('ConcurrentAccess',     [en: 'Specified', de: 'Festgelegt'], BOOTSTRAP)
        RefdataValue.loc('ConcurrentAccess',     [en: 'Not Specified', de: 'Nicht festgelegt'], BOOTSTRAP)
        RefdataValue.loc('ConcurrentAccess',     [en: 'No limit', de: 'Ohne Begrenzung'], BOOTSTRAP)
        RefdataValue.loc('ConcurrentAccess',     [en: 'Other', de: 'Andere'], BOOTSTRAP)

        RefdataValue.loc('ContactContentType',   [en: 'E-Mail', de: 'E-Mail'], BOOTSTRAP)
        RefdataValue.loc('ContactContentType',   [en: 'Phone', de: 'Telefon'], BOOTSTRAP)
        RefdataValue.loc('ContactContentType',   [en: 'Fax', de: 'Fax'], BOOTSTRAP)
        RefdataValue.loc('ContactContentType',   [en: 'Url', de: 'Url'], BOOTSTRAP)

        RefdataValue.loc('ContactType',  [en: 'Personal', de: 'Privat'], BOOTSTRAP)
        RefdataValue.loc('ContactType',  [en: 'Job-related', de: 'Geschäftlich'], BOOTSTRAP)

        RefdataValue.loc('CoreStatus',   [en: 'Yes', de: 'Ja'], BOOTSTRAP)
        RefdataValue.loc('CoreStatus',   [en: 'No', de: 'Nein'], BOOTSTRAP)
        RefdataValue.loc('CoreStatus',   [en: 'Print', de: 'Print'], BOOTSTRAP)
        RefdataValue.loc('CoreStatus',   [en: 'Electronic', de: 'Elektronisch'], BOOTSTRAP)
        RefdataValue.loc('CoreStatus',   [en: 'Print+Electronic', de: 'Print & Elektronisch'], BOOTSTRAP)

        RefdataValue.loc('Cost configuration',[en:'positive',de:'positiv'],BOOTSTRAP)
        RefdataValue.loc('Cost configuration',[en:'negative',de:'negativ'],BOOTSTRAP)
        RefdataValue.loc('Cost configuration',[en:'neutral',de:'neutral'],BOOTSTRAP)

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
        RefdataValue.loc('Country', [key: 'AD', en: 'Andorra', de: 'Andorra'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AE', en: 'United Arab Emirates', de: 'Vereinigte Arabische Emirate'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AF', en: 'Afghanistan', de: 'Afghanistan'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AG', en: 'Antigua and Barbuda', de: 'Antigua und Barbuda'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AI', en: 'Anguilla', de: 'Anguilla'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AL', en: 'Albania', de: 'Albanien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AM', en: 'Armenia', de: 'Armenien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AO', en: 'Angola', de: 'Angola'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AQ', en: 'Antarctica', de: 'Antarktis'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AR', en: 'Argentina', de: 'Argentinien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AS', en: 'American Samoa', de: 'Samoa, amerikanischer Teil'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AT', en: 'Austria', de: 'Österreich'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AU', en: 'Australia', de: 'Australien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AW', en: 'Aruba', de: 'Aruba'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AX', en: 'Åland Islands', de: 'Åland'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'AZ', en: 'Azerbaijan', de: 'Aserbaidschan'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BA', en: 'Bosnia and Herzegovina', de: 'Bosnien-Herzegowina'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BB', en: 'Barbados', de: 'Barbados'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BD', en: 'Bangladesh', de: 'Bangladesh'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BE', en: 'Belgium', de: 'Belgien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BF', en: 'Burkina Faso', de: 'Burkina Faso'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BG', en: 'Bulgaria', de: 'Bulgarien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BH', en: 'Bahrain', de: 'Bahrain'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BI', en: 'Burundi', de: 'Burundi'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BJ', en: 'Benin', de: 'Benin'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BL', en: 'Saint Barthélemy', de: 'St. Barthélemy'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BM', en: 'Bermuda', de: 'Bermuda'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BN', en: 'Brunei Darussalam', de: 'Brunei'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BO', en: 'Bolivia, Plurinational State of', de: 'Bolivien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BQ', en: 'Bonaire, Sint Eustatius and Saba', de: 'Karibische Niederlande'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BR', en: 'Brazil', de: 'Brasilien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BS', en: 'Bahamas', de: 'Bahamas'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BT', en: 'Bhutan', de: 'Bhutan'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BV', en: 'Bouvet Island', de: 'Bouvet-Insel'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BW', en: 'Botswana', de: 'Botswana'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BY', en: 'Belarus', de: 'Belarus'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'BZ', en: 'Belize', de: 'Belize'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CA', en: 'Canada', de: 'Kanada'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CC', en: 'Cocos (Keeling) Islands', de: 'Kokos-Insel (Keeling)'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CD', en: 'Congo, the Democratic Republic of the', de: 'Kongo, demokratische Republik'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CF', en: 'Central African Republic', de: 'Zentralafrika'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CG', en: 'Congo', de: 'Kongo (Republik)'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CH', en: 'Switzerland', de: 'Schweiz'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CI', en: 'Côte d\'Ivoire', de: 'Elfenbeinküste'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CK', en: 'Cook Islands', de: 'Cook-Inseln'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CL', en: 'Chile', de: 'Chile'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CM', en: 'Cameroon', de: 'Kamerun'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CN', en: 'China', de: 'China (Volksrepublik)'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CO', en: 'Colombia', de: 'Kolumbien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CR', en: 'Costa Rica', de: 'Costa Rica'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CU', en: 'Cuba', de: 'Kuba'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CV', en: 'Cape Verde', de: 'Kapverdische Inseln'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CW', en: 'Curaçao', de: 'Curaçao'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CX', en: 'Christmas Island', de: 'Weihnachtsinseln (indischer Ozean)'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CY', en: 'Cyprus', de: 'Zypern'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'CZ', en: 'Czech Republic', de: 'Tschechische Republik'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'DE', en: 'Germany', de: 'Deutschland'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'DJ', en: 'Djibouti', de: 'Djibouti'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'DK', en: 'Denmark', de: 'Dänemark'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'DM', en: 'Dominica', de: 'Dominica'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'DO', en: 'Dominican Republic', de: 'Dominikanische Republik'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'DZ', en: 'Algeria', de: 'Algerien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'EC', en: 'Ecuador', de: 'Ekuador'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'EE', en: 'Estonia', de: 'Estland'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'EG', en: 'Egypt', de: 'Ägypten'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'EH', en: 'Western Sahara', de: 'Westsahara'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'ER', en: 'Eritrea', de: 'Eritrea'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'ES', en: 'Spain', de: 'Spanien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'ET', en: 'Ethiopia', de: 'Äthiopien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'FI', en: 'Finland', de: 'Finnland'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'FJ', en: 'Fiji', de: 'Fidschi'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'FK', en: 'Falkland Islands (Malvinas)', de: 'Falkland-Inseln'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'FM', en: 'Micronesia, Federated States of', de: 'Mikronesien (Föderierte Staaten von)'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'FO', en: 'Faroe Islands', de: 'Färöer-Inseln'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'FR', en: 'France', de: 'Frankreich'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GA', en: 'Gabon', de: 'Gabun'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GB', en: 'United Kingdom', de: 'Grossbritannien und Nordirland'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GD', en: 'Grenada', de: 'Grenada'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GE', en: 'Georgia', de: 'Georgien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GF', en: 'French Guiana', de: 'Französisch-Guyana'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GG', en: 'Guernsey', de: 'Guernsey'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GH', en: 'Ghana', de: 'Ghana'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GI', en: 'Gibraltar', de: 'Gibraltar'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GL', en: 'Greenland', de: 'Grönland'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GM', en: 'Gambia', de: 'Gambia'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GN', en: 'Guinea', de: 'Guinea (Republik)'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GP', en: 'Guadeloupe', de: 'Guadeloupe'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GQ', en: 'Equatorial Guinea', de: 'Äquatorial-Guinea'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GR', en: 'Greece', de: 'Griechenland'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GS', en: 'South Georgia and the South Sandwich Islands', de: 'Südgeorgien und die südlichen Sandwichinseln'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GT', en: 'Guatemala', de: 'Guatemala'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GU', en: 'Guam', de: 'Guam'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GW', en: 'Guinea-Bissau', de: 'Guinea-Bissau'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'GY', en: 'Guyana', de: 'Guyana'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'HK', en: 'Hong Kong', de: 'Hongkong'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'HM', en: 'Heard Island and McDonald Islands', de: 'Heard- und McDonald-Inseln'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'HN', en: 'Honduras', de: 'Honduras'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'HR', en: 'Croatia', de: 'Kroatien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'HT', en: 'Haiti', de: 'Haiti'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'HU', en: 'Hungary', de: 'Ungarn'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'ID', en: 'Indonesia', de: 'Indonesien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'IE', en: 'Ireland', de: 'Irland'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'IL', en: 'Israel', de: 'Israel'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'IM', en: 'Isle of Man', de: 'Man, Insel'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'IN', en: 'India', de: 'Indien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'IO', en: 'British Indian Ocean Territory', de: 'Britisches Territorium im indischen Ozean'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'IQ', en: 'Iraq', de: 'Irak'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'IR', en: 'Iran, Islamic Republic of', de: 'Iran'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'IS', en: 'Iceland', de: 'Island'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'IT', en: 'Italy', de: 'Italien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'JE', en: 'Jersey', de: 'Jersey'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'JM', en: 'Jamaica', de: 'Jamaika'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'JO', en: 'Jordan', de: 'Jordanien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'JP', en: 'Japan', de: 'Japan'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'KE', en: 'Kenya', de: 'Kenia'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'KG', en: 'Kyrgyzstan', de: 'Kirgisistan'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'KH', en: 'Cambodia', de: 'Kambodscha'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'KI', en: 'Kiribati', de: 'Kiribati'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'KM', en: 'Comoros', de: 'Komoren'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'KN', en: 'Saint Kitts and Nevis', de: 'St. Christoph (St. Kitts) und Nevis'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'KP', en: 'Korea, Democratic People\'s Republic of', de: 'Korea, demokratische Volksrepublik (Nordkorea)'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'KR', en: 'Korea, Republic of', de: 'Korea, Republik (Südkorea)'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'KW', en: 'Kuwait', de: 'Kuwait'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'KY', en: 'Cayman Islands', de: 'Cayman'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'KZ', en: 'Kazakhstan', de: 'Kasachstan'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'LA', en: 'Lao People\'s Democratic Republic', de: 'Laos'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'LB', en: 'Lebanon', de: 'Libanon'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'LC', en: 'Saint Lucia', de: 'St. Lucia'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'LI', en: 'Liechtenstein', de: 'Liechtenstein'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'LK', en: 'Sri Lanka', de: 'Sri Lanka'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'LR', en: 'Liberia', de: 'Liberia'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'LS', en: 'Lesotho', de: 'Lesotho'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'LT', en: 'Lithuania', de: 'Litauen'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'LU', en: 'Luxembourg', de: 'Luxemburg'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'LV', en: 'Latvia', de: 'Lettland'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'LY', en: 'Libya', de: 'Libyen'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MA', en: 'Morocco', de: 'Marokko'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MC', en: 'Monaco', de: 'Monaco'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MD', en: 'Moldova, Republic of', de: 'Moldova'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'ME', en: 'Montenegro', de: 'Montenegro, Republik'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MF', en: 'Saint Martin (French part)', de: 'St. Martin'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MG', en: 'Madagascar', de: 'Madagaskar'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MH', en: 'Marshall Islands', de: 'Marshall-Inseln'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MK', en: 'Macedonia, the Former Yugoslav Republic of', de: 'Mazedonien, ehemalige jugoslawische Republik'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'ML', en: 'Mali', de: 'Mali'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MM', en: 'Myanmar', de: 'Myanmar (Union)'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MN', en: 'Mongolia', de: 'Mongolei'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MO', en: 'Macao', de: 'Macao'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MP', en: 'Northern Mariana Islands', de: 'Marianen-Inseln'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MQ', en: 'Martinique', de: 'Martinique'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MR', en: 'Mauritania', de: 'Mauretanien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MS', en: 'Montserrat', de: 'Montserrat'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MT', en: 'Malta', de: 'Malta'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MU', en: 'Mauritius', de: 'Mauritius, Insel'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MV', en: 'Maldives', de: 'Malediven'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MW', en: 'Malawi', de: 'Malawi'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MX', en: 'Mexico', de: 'Mexiko'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MY', en: 'Malaysia', de: 'Malaysia'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'MZ', en: 'Mozambique', de: 'Mosambik'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'NA', en: 'Namibia', de: 'Namibia'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'NC', en: 'New Caledonia', de: 'Neukaledonien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'NE', en: 'Niger', de: 'Niger'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'NF', en: 'Norfolk Island', de: 'Norfolk-Insel'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'NG', en: 'Nigeria', de: 'Nigeria'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'NI', en: 'Nicaragua', de: 'Nicaragua'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'NL', en: 'Netherlands', de: 'Niederlande'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'NO', en: 'Norway', de: 'Norwegen'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'NP', en: 'Nepal', de: 'Nepal'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'NR', en: 'Nauru', de: 'Nauru'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'NU', en: 'Niue', de: 'Niue'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'NZ', en: 'New Zealand', de: 'Neuseeland'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'OM', en: 'Oman', de: 'Oman'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PA', en: 'Panama', de: 'Panama'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PE', en: 'Peru', de: 'Peru'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PF', en: 'French Polynesia', de: 'Französisch-Polynesien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PG', en: 'Papua New Guinea', de: 'Papua-Neuguinea'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PH', en: 'Philippines', de: 'Philippinen'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PK', en: 'Pakistan', de: 'Pakistan'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PL', en: 'Poland', de: 'Polen'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PM', en: 'Saint Pierre and Miquelon', de: 'St. Pierre und Miquelon'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PN', en: 'Pitcairn', de: 'Pitcairn'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PR', en: 'Puerto Rico', de: 'Puerto Rico'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PS', en: 'Palestine, State of', de: 'Palästina'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PT', en: 'Portugal', de: 'Portugal'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PW', en: 'Palau', de: 'Palau'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'PY', en: 'Paraguay', de: 'Paraguay'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'QA', en: 'Qatar', de: 'Qatar'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'RE', en: 'Réunion', de: 'Réunion'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'RO', en: 'Romania', de: 'Rumänien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'RS', en: 'Serbia', de: 'Serbien, Republik'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'RU', en: 'Russian Federation', de: 'Russische Föderation'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'RW', en: 'Rwanda', de: 'Rwanda'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SA', en: 'Saudi Arabia', de: 'Saudi-Arabien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SB', en: 'Solomon Islands', de: 'Salomon-Inseln'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SC', en: 'Seychelles', de: 'Seychellen'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SD', en: 'Sudan', de: 'Sudan'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SE', en: 'Sweden', de: 'Schweden'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SG', en: 'Singapore', de: 'Singapur'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SH', en: 'Saint Helena, Ascension and Tristan da Cunha', de: 'St. Helena, Ascension und Tristan da Cunha'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SI', en: 'Slovenia', de: 'Slowenien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SJ', en: 'Svalbard and Jan Mayen', de: 'Svalbard und Insel Jan Mayen'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SK', en: 'Slovakia', de: 'Slowakische Republik'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SL', en: 'Sierra Leone', de: 'Sierra Leone'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SM', en: 'San Marino', de: 'San Marino'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SN', en: 'Senegal', de: 'Senegal'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SO', en: 'Somalia', de: 'Somalia'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SR', en: 'Suriname', de: 'Suriname'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SS', en: 'South Sudan', de: 'Südsudan'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'ST', en: 'Sao Tome and Principe', de: 'St. Thomas und Principe'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SV', en: 'El Salvador', de: 'El Salvador'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SX', en: 'Sint Maarten (Dutch part)', de: 'St. Maarten'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SY', en: 'Syrian Arab Republic', de: 'Syrien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'SZ', en: 'Swaziland', de: 'Swasiland'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TC', en: 'Turks and Caicos Islands', de: 'Turks und Caicos'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TD', en: 'Chad', de: 'Tschad'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TF', en: 'French Southern Territories', de: 'Französische Süd- und Antarktisgebiete'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TG', en: 'Togo', de: 'Togo'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TH', en: 'Thailand', de: 'Thailand'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TJ', en: 'Tajikistan', de: 'Tadschikistan'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TK', en: 'Tokelau', de: 'Tokelau'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TL', en: 'Timor-Leste', de: 'Timor-Leste'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TM', en: 'Turkmenistan', de: 'Turkmenistan'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TN', en: 'Tunisia', de: 'Tunesien'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TO', en: 'Tonga', de: 'Tonga'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TR', en: 'Turkey', de: 'Türkei'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TT', en: 'Trinidad and Tobago', de: 'Trinidad und Tobago'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TV', en: 'Tuvalu', de: 'Tuvalu'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TW', en: 'Taiwan, Province of China', de: 'Taiwan (Chinesisches Taipei)'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'TZ', en: 'Tanzania, United Republic of', de: 'Tansania'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'UA', en: 'Ukraine', de: 'Ukraine'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'UG', en: 'Uganda', de: 'Uganda'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'UM', en: 'United States Minor Outlying Islands', de: 'Amerikanische Überseeinseln, kleinere'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'US', en: 'United States', de: 'Vereinigte Staaten von Amerika'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'UY', en: 'Uruguay', de: 'Uruguay'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'UZ', en: 'Uzbekistan', de: 'Usbekistan'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'VA', en: 'Holy See (Vatican City State)', de: 'Vatikan'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'VC', en: 'Saint Vincent and the Grenadines', de: 'St. Vincent und Grenadinen'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'VE', en: 'Venezuela, Bolivarian Republic of', de: 'Venezuela'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'VG', en: 'Virgin Islands, British', de: 'Virginische Inseln, britischer Teil (Tortola)'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'VI', en: 'Virgin Islands, U.S.', de: 'Amerikanische Jungferninseln'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'VN', en: 'Viet Nam', de: 'Vietnam'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'VU', en: 'Vanuatu', de: 'Vanuatu'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'WF', en: 'Wallis and Futuna', de: 'Wallis und Futuna'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'WS', en: 'Samoa', de: 'Samoa, West'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'YE', en: 'Yemen', de: 'Jemen'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'YT', en: 'Mayotte', de: 'Mayotte'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'ZA', en: 'South Africa', de: 'Südafrika'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'ZM', en: 'Zambia', de: 'Sambia'], BOOTSTRAP)
        RefdataValue.loc('Country', [key: 'ZW', en: 'Zimbabwe', de: 'Zimbabwe'], BOOTSTRAP)

        RefdataValue.loc('FactType', [en: 'JR1R4'], BOOTSTRAP)
        RefdataValue.loc('FactType', [en: 'JR1GOAR4'], BOOTSTRAP)
        RefdataValue.loc('FactType', [en: 'DB1R4'], BOOTSTRAP)

        RefdataValue.loc('FactMetric', [en: 'ft_total'], BOOTSTRAP)
        RefdataValue.loc('FactMetric', [en: 'record_view'], BOOTSTRAP)
        RefdataValue.loc('FactMetric', [en: 'result_click'], BOOTSTRAP)
        RefdataValue.loc('FactMetric', [en: 'search_reg'], BOOTSTRAP)
        RefdataValue.loc('FactMetric', [en: 'search_fed'], BOOTSTRAP)

        RefdataValue.loc('Federal State',   [en: 'Baden-Wurttemberg', de: 'Baden-Württemberg'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Bavaria', de: 'Bayern'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Berlin', de: 'Berlin'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Brandenburg', de: 'Brandenburg'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Bremen', de: 'Bremen'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Hamburg', de: 'Hamburg'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Hesse', de: 'Hessen'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Lower Saxony', de: 'Niedersachsen'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Mecklenburg-West Pomerania', de: 'Mecklenburg-Vorpommern'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'North Rhine-Westphalia', de: 'Nordrhein-Westfalen'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Rhineland-Palatinate', de: 'Rheinland-Pfalz'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Saarland', de: 'Saarland'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Saxony', de: 'Sachsen'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Saxony-Anhalt', de: 'Sachsen-Anhalt'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Schleswig-Holstein', de: 'Schleswig-Holstein'], BOOTSTRAP)
        RefdataValue.loc('Federal State',   [en: 'Thuringia', de: 'Thüringen'], BOOTSTRAP)

        RefdataValue.loc('Funder Type',   [en: 'Federal Republic of Germany', de: 'Bundesrepublik Deutschland'], BOOTSTRAP)
        RefdataValue.loc('Funder Type',   [en: 'Federal State', de: 'Land'], BOOTSTRAP)
        RefdataValue.loc('Funder Type',   [en: 'County', de: 'Kreis'], BOOTSTRAP)
        RefdataValue.loc('Funder Type',   [en: 'Commune', de: 'Gemeinde'], BOOTSTRAP)
        RefdataValue.loc('Funder Type',   [en: 'Other Territorial Authority', de: 'Sonstige Gebietskörperschaft'], BOOTSTRAP)
        RefdataValue.loc('Funder Type',   [en: 'Other Public Sector Funder', de: 'Sonstige öffentliche Trägerschaft'], BOOTSTRAP)
        RefdataValue.loc('Funder Type',   [en: 'Corporate Body or Foundation under Public Law', de: 'Körperschaft oder Stiftung des öffentlichen Rechts'], BOOTSTRAP)
        RefdataValue.loc('Funder Type',   [en: 'Corporate Body or Foundation under Private Law', de: 'Körperschaft oder Stiftung des privaten Rechts'], BOOTSTRAP)
        RefdataValue.loc('Funder Type',   [en: 'Protestant Church', de: 'Evangelische Kirche'], BOOTSTRAP)
        RefdataValue.loc('Funder Type',   [en: 'Catholic Church', de: 'Katholische Kirche'], BOOTSTRAP)
        RefdataValue.loc('Funder Type',   [en: 'Other Religious Communities', de: 'Sonstige Religionsgemeinschaften'], BOOTSTRAP)
        RefdataValue.loc('Funder Type',   [en: 'Private Funding Body (Natural Person)', de: 'Private Trägerschaft (natürliche Personen)'], BOOTSTRAP)
        RefdataValue.loc('Funder Type',   [en: 'Foreign Funding Body', de: 'Ausländische Trägerschaft'], BOOTSTRAP)

        RefdataValue.loc('Gender',   [en: 'Female', de: 'Weiblich'], BOOTSTRAP)
        RefdataValue.loc('Gender',   [en: 'Male', de: 'Männlich'], BOOTSTRAP)
		RefdataValue.loc('Gender',   [en: 'Third Gender', de: 'Inter/Divers'], BOOTSTRAP)

        RefdataValue.loc('Invoicing',   [key: 'Provider', en: 'Provider', de: 'Anbieter'], BOOTSTRAP)
        RefdataValue.loc('Invoicing',   [key: 'Consortium', en: 'Consortium', de: 'Konsortium'], BOOTSTRAP)

        RefdataValue.loc('Library Network',   [en: 'BVB', de: 'BVB'], BOOTSTRAP)
        RefdataValue.loc('Library Network',   [en: 'GBV', de: 'GBV'], BOOTSTRAP)
        RefdataValue.loc('Library Network',   [en: 'hbz', de: 'hbz'], BOOTSTRAP)
        RefdataValue.loc('Library Network',   [en: 'HeBIS', de: 'HeBIS'], BOOTSTRAP)
        RefdataValue.loc('Library Network',   [en: 'KOBV', de: 'KOBV'], BOOTSTRAP)
        RefdataValue.loc('Library Network',   [en: 'SWB', de: 'SWB'], BOOTSTRAP)
        RefdataValue.loc('Library Network',   [en: 'No Network', de: 'Keine Zugehörigkeit'], BOOTSTRAP)

        RefdataValue.loc('Library Type',   [en: 'Fachhochschule', de: 'Fachhochschule'], BOOTSTRAP)
        RefdataValue.loc('Library Type',   [en: 'Forschungseinrichtung', de: 'Forschungseinrichtung'], BOOTSTRAP)
        RefdataValue.loc('Library Type',   [en: 'Institutsbibliothek', de: 'Institutsbibliothek'], BOOTSTRAP)
        RefdataValue.loc('Library Type',   [en: 'Kunst- und Musikhochschule', de: 'Kunst- und Musikhochschule'], BOOTSTRAP)
        RefdataValue.loc('Library Type',   [en: 'Öffentliche Bibliothek', de: 'Öffentliche Bibliothek'], BOOTSTRAP)
        RefdataValue.loc('Library Type',   [en: 'Universität', de: 'Universität'], BOOTSTRAP)
        RefdataValue.loc('Library Type',   [en: 'Staats-/ Landes-/ Regionalbibliothek', de: 'Staats-/ Landes-/ Regionalbibliothek'], BOOTSTRAP)
        RefdataValue.loc('Library Type',   [en: 'Wissenschafltiche Spezialbibliothek', de: 'Wissenschafltiche Spezialbibliothek'], BOOTSTRAP)
        RefdataValue.loc('Library Type',   [en: 'Sonstige', de: 'Sonstige'], BOOTSTRAP)
        RefdataValue.loc('Library Type',   [en: 'keine Angabe', de: 'keine Angabe'], BOOTSTRAP)

        RefdataValue.loc('Link Type', [en: 'follows',de: '... ist Nachfolger von|... ist Vorgänger von'], BOOTSTRAP)
        RefdataValue.loc('Link Type', [en: 'references',de: '... referenziert|... wird referenziert durch'], BOOTSTRAP)
        RefdataValue.loc('Link Type', [en: 'is condition for',de: '... ist Bedingung für|... ist bedingt durch'], BOOTSTRAP)

        RefdataValue.loc('OrgStatus',      [en: 'Current', de: 'Aktuell'], BOOTSTRAP)
        RefdataValue.loc('OrgStatus',      [en: 'Deleted', de: 'Gelöscht'], BOOTSTRAP)

        RefdataValue.loc('OrgSector',    [en: 'Higher Education', de: 'Akademisch'], BOOTSTRAP)
        RefdataValue.loc('OrgSector',    [key: 'Publisher', en: 'Commercial', de: 'Kommerziell'], BOOTSTRAP)

        RefdataValue.loc('OrgRoleType',      [en: 'Consortium', de: 'Konsortium'], BOOTSTRAP)
        RefdataValue.loc('OrgRoleType',      [en: 'Institution', de: 'Einrichtung'], BOOTSTRAP)
        RefdataValue.loc('OrgRoleType',      [en: 'Publisher', de: 'Verlag'], BOOTSTRAP)
        RefdataValue.loc('OrgRoleType',      [en: 'Provider', de: 'Anbieter'], BOOTSTRAP)
        RefdataValue.loc('OrgRoleType',      [en: 'Agency', de: 'Lieferant'], BOOTSTRAP)
        RefdataValue.loc('OrgRoleType',      [en: 'Other', de: 'Andere'], BOOTSTRAP)
        RefdataValue.loc('OrgRoleType',      [en: 'Licensor', de: 'Lizenzgeber'], BOOTSTRAP)
        RefdataValue.loc('OrgRoleType',      [en: 'Licensee', de: 'Lizenznehmer'], BOOTSTRAP)
        RefdataValue.loc('OrgRoleType',      [en: 'Broker', de: 'Vermittler'], BOOTSTRAP)
        RefdataValue.loc('OrgRoleType',      [en: 'Vendor', de: 'Verkäufer'], BOOTSTRAP)
        RefdataValue.loc('OrgRoleType',      [en: 'Content Provider', de:'Inhalt Anbieter'], BOOTSTRAP)
        RefdataValue.loc('OrgRoleType',      [en: 'Platform Provider', de: 'Plattformanbieter'], BOOTSTRAP)
        RefdataValue.loc('OrgRoleType',      [en: 'Issuing Body'], BOOTSTRAP)
        RefdataValue.loc('OrgRoleType',      [en: 'Imprint'], BOOTSTRAP)

        RefdataValue.loc('Package Status',      [en: 'Deleted', de: 'Gelöscht'], BOOTSTRAP)
        RefdataValue.loc('Package Status',      [en: 'Current', de: 'Aktuell'], BOOTSTRAP)
        RefdataValue.loc('Package Status',      [en: 'Retired', de: 'Abgelaufen'], BOOTSTRAP)

        RefdataValue.loc('Person Contact Type', [en: 'Personal contact', de: 'Personenkontakt'], BOOTSTRAP)
        RefdataValue.loc('Person Contact Type', [en: 'Functional contact', de: 'Funktionskontakt'], BOOTSTRAP)

        RefdataValue.loc('Person Function',     [en: 'General contact person', de: 'Hauptkontakt'], BOOTSTRAP)
        RefdataValue.loc('Person Function',     [en: 'GASCO-Contact', de: 'GASCO-Kontakt'], BOOTSTRAP)
        RefdataValue.loc('Person Function',     [en: 'Statistical Support', de: 'Statistischer Support'], BOOTSTRAP) // neu
        RefdataValue.loc('Person Function',     [en: 'Technichal Support', de: 'Technischer Support'], BOOTSTRAP) // Funktion
        // RefdataValue.loc('Person Function',     [en: 'Bestandsaufbau', de: 'Bestandsaufbau'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Direktion', de: 'Direktion'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Direktionsassistenz', de: 'Direktionsassistenz'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Erwerbungsabteilung', de: 'Erwerbungsabteilung'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Erwerbungsleitung', de: 'Erwerbungsleitung'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Medienbearbeitung', de: 'Medienbearbeitung'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Zeitschriftenabteilung', de: 'Zeitschriftenabteilung'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Fachreferat', de: 'Fachreferat'], BOOTSTRAP) //Position
        // RefdataValue.loc('Person Function',     [en: 'Bereichsbibliotheksleitung', de: 'Bereichsbibliotheksleitung'], BOOTSTRAP) //Position

        RefdataValue.loc('Person Function', [key: 'Functional Contact Postal Address', en: 'Postal address', de: 'Postanschrift'], BOOTSTRAP)
        RefdataValue.loc('Person Function', [key: 'Functional Contact Billing Adress', en: 'Billing contact', de: 'Rechnungskontakt'], BOOTSTRAP)
        RefdataValue.loc('Person Function', [key: 'Functional Contact Delivery Address', en: 'Delivery address', de: 'Lieferanschrift'], BOOTSTRAP)
        RefdataValue.loc('Person Function', [key: 'Functional Contact Library Address', en: 'Library address', de: 'Bibliotheksanschrift'], BOOTSTRAP)
        RefdataValue.loc('Person Function', [key: 'Functional Contact Legal Patron Address', en: 'Legal patron contact', de: 'Anschrift des rechtlichen Trägers'], BOOTSTRAP)

        RefdataValue.loc('Person Position',     [en: 'Account Manager', de: 'Account Manager'], BOOTSTRAP)
        RefdataValue.loc('Person Position',     [en: 'Head Access Services', de: 'Erwerbungsleiter'], BOOTSTRAP)
        RefdataValue.loc('Person Position',     [en: 'Library Director', de: 'Bibliotheksdirektor'], BOOTSTRAP)
        RefdataValue.loc('Person Position',     [en: 'Sales Director', de: 'Sales Director'], BOOTSTRAP)
        RefdataValue.loc('Person Position',     [en: 'Sales Support', de: 'Sales Support'], BOOTSTRAP)
        // RefdataValue.loc('Person Position',     [en: 'Technichal Support', de: 'Technischer Support'], BOOTSTRAP) // Funktion
        RefdataValue.loc('Person Position',     [en: 'Bestandsaufbau', de: 'Bestandsaufbau'], BOOTSTRAP) //Position
        RefdataValue.loc('Person Position',     [en: 'Direktion', de: 'Direktion'], BOOTSTRAP) //Position
        RefdataValue.loc('Person Position',     [en: 'Direktionsassistenz', de: 'Direktionsassistenz'], BOOTSTRAP) //Position
        RefdataValue.loc('Person Position',     [en: 'Erwerbungsabteilung', de: 'Erwerbungsabteilung'], BOOTSTRAP) //Position
        RefdataValue.loc('Person Position',     [en: 'Erwerbungsleitung', de: 'Erwerbungsleitung'], BOOTSTRAP) //Position
        RefdataValue.loc('Person Position',     [en: 'Medienbearbeitung', de: 'Medienbearbeitung'], BOOTSTRAP) //Position
        RefdataValue.loc('Person Position',     [en: 'Zeitschriftenabteilung', de: 'Zeitschriftenabteilung'], BOOTSTRAP) //Position
        RefdataValue.loc('Person Position',     [en: 'Fachreferat', de: 'Fachreferat'], BOOTSTRAP) //Position
        RefdataValue.loc('Person Position',     [en: 'Bereichsbibliotheksleitung', de: 'Bereichsbibliotheksleitung'], BOOTSTRAP) //Position

        RefdataValue.loc('Person Responsibility',    [en: 'Specific license editor', de: 'Vertragsbearbeiter'], BOOTSTRAP)
        RefdataValue.loc('Person Responsibility',    [en: 'Specific subscription editor', de: 'Lizenzkontakt'], BOOTSTRAP)
        RefdataValue.loc('Person Responsibility',    [en: 'Specific package editor', de: 'Paketbearbeiter'], BOOTSTRAP)
        RefdataValue.loc('Person Responsibility',    [en: 'Specific cluster editor', de: 'Gruppenkontakt'], BOOTSTRAP)
        RefdataValue.loc('Person Responsibility',    [en: 'Specific title editor', de: 'Titelbearbeiter'], BOOTSTRAP)

        RefdataValue.loc('Share Configuration', [en: 'only for creator',de:'nur für Uploader'], BOOTSTRAP)
        RefdataValue.loc('Share Configuration', [key: 'only for author organisation', en: 'only for my organisation',de:'nur für meine Organisation'], BOOTSTRAP)
        //deactivated as March 21st, 2019 - the feature has been postponed into quartal II at least
        //RefdataValue.loc('Share Configuration', [key: 'only for author and target organisation', en: 'only for my and target organisation',de:'nur für meine Organisation und die Bezugsorganisation'], BOOTSTRAP)
        //RefdataValue.loc('Share Configuration', [key: 'only for consortia members',en:'only for my consortia members',de:'nur für meine Konsorten'], BOOTSTRAP)
        //RefdataValue.loc('Share Configuration', [en: 'everyone',de:'alle'], BOOTSTRAP)

        RefdataValue.loc('Subscription Form',      [key: 'test', en: 'Test', de: 'Test'], BOOTSTRAP)
        RefdataValue.loc('Subscription Form',      [key: 'offer', en: 'Offer', de: 'Angebot'], BOOTSTRAP)
        RefdataValue.loc('Subscription Form',      [key: 'license', en: 'License', de: 'Lizenz'], BOOTSTRAP)
        RefdataValue.loc('Subscription Form',      [key: 'singlePurchase', en: 'Single Purchase', de: 'Einmalkauf'], BOOTSTRAP)
        RefdataValue.loc('Subscription Form',      [key: 'desideratum', en: 'Desideratum', de: 'Desiderat'], BOOTSTRAP)
        RefdataValue.loc('Subscription Form',      [key: 'purchaseUpdate', en: 'Purchase with update fee', de: 'Kauf mit Update Fee'], BOOTSTRAP)
        RefdataValue.loc('Subscription Form',      [key: 'purchaseOngoing', en: 'Ongoing Purchase', de: 'Kauf laufend'], BOOTSTRAP)
        RefdataValue.loc('Subscription Form',      [key: 'obligation', en: 'Obligation', de: 'Pflichtlizenz'], BOOTSTRAP)

        RefdataValue.loc('Subscription Resource', [key: 'avMedia', en: 'AV-Media', de: 'AV-Medien'], BOOTSTRAP)
        RefdataValue.loc('Subscription Resource', [key: 'database', en: 'Database', de: 'Datenbank'], BOOTSTRAP)
        RefdataValue.loc('Subscription Resource', [key: 'econferenceProceedings', en: 'E-Conference Proceedings', de: 'E-Conference Proceedings'], BOOTSTRAP)
        RefdataValue.loc('Subscription Resource', [key: 'ejournalSingle', en: 'E-Journal single title', de: 'E-Journal Einzeltitel'], BOOTSTRAP)
        RefdataValue.loc('Subscription Resource', [key: 'ebookSingle', en: 'E-Book single title', de: 'E-Book Einzeltitel'], BOOTSTRAP)
        RefdataValue.loc('Subscription Resource', [key: 'data', en: 'Data', de: 'Daten'], BOOTSTRAP)
        RefdataValue.loc('Subscription Resource', [key: 'software', en: 'Software', de: 'Software'], BOOTSTRAP)
        RefdataValue.loc('Subscription Resource', [key: 'ejournalPackage', en: 'E-Journal Package', de: 'E-Journal Paket'], BOOTSTRAP)
        RefdataValue.loc('Subscription Resource', [key: 'ebookPackage', en: 'E-Book Package', de: 'E-Book Paket'], BOOTSTRAP)
        RefdataValue.loc('Subscription Resource', [key: 'mixed', en: 'Mixed Package', de: 'Gemischtes Paket'], BOOTSTRAP)
        RefdataValue.loc('Subscription Resource', [key: 'ereference', en: 'E-Reference', de: 'E-Reference'], BOOTSTRAP)
        RefdataValue.loc('Subscription Resource', [key: 'other', en: 'Other', de: 'Sonstige'], BOOTSTRAP)

        RefdataValue.loc('Subscription Status',      [en: 'Current', de: 'Aktiv'], BOOTSTRAP)
        RefdataValue.loc('Subscription Status',      [en: 'Deleted', de: 'Gelöscht'], BOOTSTRAP)
        RefdataValue.loc('Subscription Status',      [en: 'Expired', de: 'Abgelaufen'], BOOTSTRAP)
        RefdataValue.loc('Subscription Status',      [en: 'Ordered', de: 'Bestellt'], BOOTSTRAP)
        RefdataValue.loc('Subscription Status',      [en: 'Terminated', de: 'Abbestellt'], BOOTSTRAP)
        RefdataValue.loc('Subscription Status',      [en: 'Under Negotiation', de: 'In Verhandlung'], BOOTSTRAP)
        RefdataValue.loc('Subscription Status',      [en: 'Under Consideration', de: 'Entscheidung steht aus'], BOOTSTRAP)
        RefdataValue.loc('Subscription Status',      [en: 'Under Consortial Examination', de: 'Wird konsortial geprüft'], BOOTSTRAP)
        RefdataValue.loc('Subscription Status',      [en: 'Under Institutional Examination', de: 'Wird institutionell geprüft'], BOOTSTRAP)
        RefdataValue.loc('Subscription Status',      [en: 'No longer usable', de: 'Nicht mehr nutzbar'], BOOTSTRAP)
        RefdataValue.loc('Subscription Status',      [en: 'Publication discontinued', de: 'Erscheinen eingestellt'], BOOTSTRAP)
        RefdataValue.loc('Subscription Status',      [en: 'Rejected', de: 'Abgelehnt'], BOOTSTRAP)

        RefdataValue.loc('Subscription Type',      [en: 'Alliance Licence', de: 'Allianzlizenz'], BOOTSTRAP)
		    RefdataValue.loc('Subscription Type',      [en: 'National Licence', de: 'Nationallizenz'], BOOTSTRAP)
		    RefdataValue.loc('Subscription Type',      [en: 'Local Licence', de: 'Lokale Lizenz'], BOOTSTRAP)
		    RefdataValue.loc('Subscription Type',      [en: 'Consortial Licence', de: 'Konsortiallizenz'], BOOTSTRAP)

        //RefdataValue.loc('system.customer.type',    [key:'scp.basic',           en: 'Institution basic', de: 'Singlenutzer'], BOOTSTRAP)
        //RefdataValue.loc('system.customer.type',    [key:'scp.collective',      en: 'Institution collective', de: 'Kollektivnutzer'], BOOTSTRAP)
        //RefdataValue.loc('system.customer.type',    [key:'scp.member',          en: 'Institution consortium member', de: 'Konsorte'], BOOTSTRAP)
        //RefdataValue.loc('system.customer.type',    [key:'scp.consortium',      en: 'Consortium basic', de: 'Konsortium ohne Umfragefunktion'], BOOTSTRAP)
        //RefdataValue.loc('system.customer.type',    [key:'scp.consortium.survey', en: 'Consortium survey', de: 'Konsortium mit Umfragefunktion'], BOOTSTRAP)

        RefdataValue.loc('Task Priority',   [en: 'Trivial', de: 'Trivial'], BOOTSTRAP)
        RefdataValue.loc('Task Priority',   [en: 'Low', de: 'Niedrig'], BOOTSTRAP)
        RefdataValue.loc('Task Priority',   [en: 'Normal', de: 'Mittel'], BOOTSTRAP)
        RefdataValue.loc('Task Priority',   [en: 'High', de: 'Hoch'], BOOTSTRAP)
        RefdataValue.loc('Task Priority',   [en: 'Extreme', de: 'Extrem'], BOOTSTRAP)

        RefdataValue.loc('Task Status',      [en: 'Open', de: 'Offen'], BOOTSTRAP)
        RefdataValue.loc('Task Status',      [en: 'Done', de: 'Erledigt'], BOOTSTRAP)
        RefdataValue.loc('Task Status',      [en: 'Deferred', de: 'Zurückgestellt'], BOOTSTRAP)

        RefdataValue.loc('Ticket.Category',    [en: 'Bug', de: 'Fehler'], BOOTSTRAP)
        RefdataValue.loc('Ticket.Category',    [en: 'Improvement', de: 'Verbesserungsvorschlag'], BOOTSTRAP)

        RefdataValue.loc('Ticket.Status',      [en: 'New', de: 'Neu'], BOOTSTRAP)
        RefdataValue.loc('Ticket.Status',      [en: 'Open', de: 'Angenommen'], BOOTSTRAP)
        RefdataValue.loc('Ticket.Status',      [en: 'In Progress', de: 'In Bearbeitung'], BOOTSTRAP)
        RefdataValue.loc('Ticket.Status',      [en: 'Done', de: 'Erledigt'], BOOTSTRAP)
        RefdataValue.loc('Ticket.Status',      [en: 'Deferred', de: 'Zurückgestellt'], BOOTSTRAP)

        RefdataValue.loc('Lincense.RemoteAccess',      [en: 'Yes', de: 'Ja'], BOOTSTRAP)
        RefdataValue.loc('Lincense.RemoteAccess',      [en: 'No', de: 'Nein'], BOOTSTRAP)
        RefdataValue.loc('Lincense.RemoteAccess',      [en: 'All but Walk-ins', de: 'Alle außer Walk-In-User'], BOOTSTRAP)

        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'By Author', de: 'Über Autor'], BOOTSTRAP)
        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'On Demand', de: 'Auf Nachfrage'], BOOTSTRAP)
        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'From Database', de: 'Aus Datenank'], BOOTSTRAP)
        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'Automatic Delivery', de: 'Automatische Lieferung'], BOOTSTRAP)
        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'Link by Publisher', de: 'Verlinkung durch Verlag'], BOOTSTRAP)
        
        RefdataValue.loc('License.OA.Repository',                [en: 'Own Choice', de: 'Nach Wahl'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Repository',                [en: 'Publishers', de: 'Verlagseigen'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Repository',                [en: 'Subject specific', de: 'Fachspezifisch'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Repository',                [en: 'Website of Author', de: 'Webseite'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Repository',                [en: 'Institutional', de: 'Institutionell'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Repository',                [en: 'Social Media', de: 'Social Media'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Repository',                [en: 'Pre-print Server', de: 'Pre-print-Server'], BOOTSTRAP)
        
        RefdataValue.loc('License.OA.CorrespondingAuthorIdentification',    [en: 'IP Range', de: 'Über IP-Bereich'], BOOTSTRAP)
        RefdataValue.loc('License.OA.CorrespondingAuthorIdentification',    [en: 'Email Domain', de: 'E-Mail-Domäne'], BOOTSTRAP)
        RefdataValue.loc('License.OA.CorrespondingAuthorIdentification',    [en: 'Research Institute', de: 'Über Institut'], BOOTSTRAP)
        RefdataValue.loc('License.OA.CorrespondingAuthorIdentification',    [en: 'ORCID', de: 'ORCID'], BOOTSTRAP)
        RefdataValue.loc('License.OA.CorrespondingAuthorIdentification',    [en: 'Other', de: 'Anderes'], BOOTSTRAP)

        RefdataValue.loc('License.OA.LicenseToPublish',          [en: 'CC-BY', de: 'CC-BY'], BOOTSTRAP)
        RefdataValue.loc('License.OA.LicenseToPublish',          [en: 'CC-BY-NC', de: 'CC-BY-NC'], BOOTSTRAP)
        RefdataValue.loc('License.OA.LicenseToPublish',          [en: 'CC-BY-NC-ND', de: 'CC-BY-NC-ND'], BOOTSTRAP)
        RefdataValue.loc('License.OA.LicenseToPublish',          [en: 'CC-BY-SA', de: 'CC-BY-SA'], BOOTSTRAP)
        RefdataValue.loc('License.OA.LicenseToPublish',          [en: 'CC-BY-ND', de: 'CC-BY-ND'], BOOTSTRAP)
        RefdataValue.loc('License.OA.LicenseToPublish',          [en: 'CC-BY-NC-SA', de: 'CC-BY-NC-SA'], BOOTSTRAP)
        
        RefdataValue.loc('License.Arc.PaymentNote',       [en: 'No Hosting fee', de: 'Dauerhaft kostenfrei (keine Hosting Fee)'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.PaymentNote',       [en: 'Hosting fee', de: 'Hosting Fee zu zahlen'], BOOTSTRAP)
		
        RefdataValue.loc('License.Arc.TitletransferRegulation',  [en: 'Existing Regulation', de: 'Regelung vorhanden'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.TitletransferRegulation',  [en: 'Transfer Code of Practice', de: 'Transfer Code of Practice'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.TitletransferRegulation',  [en: 'No Regulation', de: 'Keine Regelung'], BOOTSTRAP)
		
        RefdataValue.loc('License.Arc.ArchivalCopyCost',         [en: 'Free', de: 'Kostenlos'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.ArchivalCopyCost',         [en: 'With Charge', de: 'Gegen Gebühr'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.ArchivalCopyCost',         [en: 'Self-copied', de: 'Kopie selbst anzufertigen'], BOOTSTRAP)
		
        RefdataValue.loc('License.Arc.ArchivalCopyTime',         [en: 'Licence Start Date', de: 'Mit Vertragsbeginn'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.ArchivalCopyTime',         [en: 'On Request', de: 'Auf Anfrage'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.ArchivalCopyTime',         [en: 'Licence End Date', de: 'Mit Vertragsende'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.ArchivalCopyTime',         [en: 'Trigger Event', de: 'Mit Trigger Event'], BOOTSTRAP)
        
        RefdataValue.loc('License.Arc.ArchivalCopyContent',      [en: 'Data', de: 'Rohdaten'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.ArchivalCopyContent',      [en: 'With Metadata', de: 'Inkl. Metadaten'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.ArchivalCopyContent',      [en: 'With Software', de: 'Inkl. Software'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.ArchivalCopyContent',      [en: 'DRM-free', de: 'Ohne DRM'], BOOTSTRAP)

        RefdataValue.loc('License.Arc.ArchivalCopyTransmissionFormat',      [en: 'Remote', de: 'Remote'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.ArchivalCopyTransmissionFormat',      [en: 'CDROM', de: 'CDROM'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.ArchivalCopyTransmissionFormat',      [en: 'Tape', de: 'Band'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.ArchivalCopyTransmissionFormat',      [en: 'Unspecified tangible format', de: 'Unspezifizierter Datenträger'], BOOTSTRAP)

        RefdataValue.loc('License.Arc.HostingTime',      [en: 'Always', de: 'Immer'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'Exclusive', de: 'Ohne Anbieter'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'From Expiration On', de: 'Ab Vertragsende'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'Predefined time', de: 'Fester Zeitpunkt'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'Trigger Event', de: 'Mit Trigger-Event'], BOOTSTRAP)
        
        RefdataValue.loc('License.Arc.Authorized',      [en: 'Licensee', de: 'Lizenznehmer'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.Authorized',      [en: 'SSG-Library', de: 'SSG-Bibliothek'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.Authorized',      [en: 'Contractor', de: 'Vertragspartner'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.Authorized',      [en: 'Contractor With Publisher\'s Assent', de: 'Vertragspartner nach Genehmigung durch Anbieter'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.Authorized',      [en: 'Former SSG library', de: 'Ehemals SSG-Bibliothek'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.Authorized',      [en: 'FID library', de: 'FID-Bibliothek'], BOOTSTRAP)
        
        RefdataValue.loc('License.Arc.HostingRestriction',      [en: 'File Format', de: 'Dateiformat'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingRestriction',      [en: 'Year', de: 'Jahrgang'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingRestriction',      [en: 'Access Period', de: 'Zugriffsdauer'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingRestriction',      [en: 'Use', de: 'Nutzung'], BOOTSTRAP)
        
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'NatHosting PLN', de: 'NatHosting PLN'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'NatHosting Portico', de: 'NatHosting Portico'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'Publisher', de: 'Verlag'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'Own Host', de: 'Eigener Host'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'Third Party Systems', de: 'Drittsysteme'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'LOCKSS', de: 'LOCKSS'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'CLOCKSS', de: 'CLOCKSS'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'Portico', de: 'Portico'], BOOTSTRAP)

        RefdataValue.loc('License.Statistics.Standards',      [en: 'COUNTER version 4', de: 'COUNTER version 4'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.Standards',      [en: 'COUNTER version 5', de: 'COUNTER version 5'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.Standards',      [en: 'ICOLC 1998', de: 'ICOLC 1998'], BOOTSTRAP)

        RefdataValue.loc('License.Statistics.Delivery',      [en: 'Online', de: 'Online'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.Delivery',      [en: 'Email', de: 'Email'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.Delivery',      [en: 'Paper', de: 'Papier'], BOOTSTRAP)

        RefdataValue.loc('License.Statistics.Format',      [en: 'HTML', de: 'HTML'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.Format',      [en: 'Delimited', de: 'Begrenzt (z.B. durch Tab)'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.Format',      [en: 'Excel', de: 'Excel'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.Format',      [en: 'PDF', de: 'PDF'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.Format',      [en: 'CSV', de: 'CSV'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.Format',      [en: 'ASCII', de: 'ASCII'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.Format',      [en: 'Other', de: 'Sonstiges Format'], BOOTSTRAP)

        RefdataValue.loc('License.Statistics.Frequency',      [en: 'Monthly', de: 'Monatlich'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.Frequency',      [en: 'Quarterly', de: 'Quartalsweise'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.Frequency',      [en: 'Bi-Annual', de: 'Halbjährlich'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.Frequency',      [en: 'User-selectable', de: 'Individuell'], BOOTSTRAP)

        RefdataValue.loc('License.Statistics.UserCreds',      [en: 'None', de: 'Keins'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.UserCreds',      [en: 'Same as Admin', de: 'Das gleiche wie Admin'], BOOTSTRAP)
        RefdataValue.loc('License.Statistics.UserCreds',      [en: 'Other', de: 'Anderes'], BOOTSTRAP)

        RefdataValue.loc('User.Settings.Dashboard.Tab',     [en: 'Changes', de: 'Änderungen'], BOOTSTRAP)
        RefdataValue.loc('User.Settings.Dashboard.Tab',     [en: 'Announcements', de: 'Ankündigungen'], BOOTSTRAP)
        RefdataValue.loc('User.Settings.Dashboard.Tab',     [en: 'Tasks', de: 'Aufgaben'], BOOTSTRAP)
        RefdataValue.loc('User.Settings.Dashboard.Tab',     [en: 'Due Dates', de: 'Fällige Termine'], BOOTSTRAP)

        RefdataValue.loc('User.Settings.Theme',     [key:'semanticUI',    en: 'Default', de: 'Standard'], BOOTSTRAP)
        RefdataValue.loc('User.Settings.Theme',     [key:'accessibility', en: 'Accessibility', de: 'Barrierefrei'], BOOTSTRAP)

        RefdataValue.loc('Access Method',      [key: 'ip4', en: 'IPv4', de: 'IPv4'], BOOTSTRAP)
        RefdataValue.loc('Access Method',      [key: 'ip6', en: 'IPv6', de: 'IPv6'], BOOTSTRAP)
        RefdataValue.loc('Access Method',      [key: 'proxy', en: 'Proxy', de: 'Proxy'], BOOTSTRAP)
        RefdataValue.loc('Access Method',      [key: 'shibb', en: 'Shibboleth', de: 'Shibboleth'], BOOTSTRAP)
        RefdataValue.loc('Access Method',      [key: 'up', en: 'Username/Password', de: 'Benutzername/Passwort'], BOOTSTRAP)
        RefdataValue.loc('Access Method',      [key: 'oa', en: 'Open Athens', de: 'OpenAthens'], BOOTSTRAP)
        RefdataValue.loc('Access Method',      [key: 'ref', en: 'Referrer', de: 'Referrer'], BOOTSTRAP)

        RefdataValue.loc('Access Method IP',      [en: 'IPv4', de: 'IPv4'], BOOTSTRAP)
        RefdataValue.loc('Access Method IP',      [en: 'IPv6', de: 'IPv6'], BOOTSTRAP)

        RefdataValue.loc('Access Point Type',      [key: 'ip', en: 'IP', de: 'IP'], BOOTSTRAP)
        //RefdataValue.loc('Access Point Type',      [key: 'shibb', en: 'Shibboleth', de: 'Shibboleth'], BOOTSTRAP)

        RefdataValue.loc('IPv4 Address Format',      [key: 'cidr',   en: 'IPv4 (CIDR)', de: 'IPv4 (CIDR)'], BOOTSTRAP)
        RefdataValue.loc('IPv4 Address Format',      [key: 'ranges', en: 'IPv4 (Ranges)', de: 'IPv4 (Bereiche)'], BOOTSTRAP)
        RefdataValue.loc('IPv4 Address Format',      [key: 'input', en: 'IPv4 (Input)', de: 'IPv4 (Eingabe)'], BOOTSTRAP)
        RefdataValue.loc('IPv6 Address Format',      [key: 'cidr',   en: 'IPv6 (CIDR)', de: 'IPv6 (CIDR)'], BOOTSTRAP)
        RefdataValue.loc('IPv6 Address Format',      [key: 'ranges', en: 'IPv6 (Ranges)', de: 'IPv6 (Bereiche)'], BOOTSTRAP)
        RefdataValue.loc('IPv6 Address Format',      [key: 'input', en: 'IPv6 (Input)', de: 'IPv6 (Eingabe)'], BOOTSTRAP)

        RefdataValue.loc('Semester',      [key: 'semester.not.applicable', en: 'Not applicable', de: 'Nicht anwendbar'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w17/18', en: 'winter semester 2017/18', de: 'Wintersemester 2017/18'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's18', en: 'summer semester 2018', de: 'Sommersemester 2018'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w18/19', en: 'winter semester 2018/19', de: 'Wintersemester 2018/19'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's19', en: 'summer semester 2019', de: 'Sommersemester 2019'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w19/20', en: 'winter semester 2019/20', de: 'Wintersemester 2019/20'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's20', en: 'summer semester 2020', de: 'Sommersemester 2020'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w20/21', en: 'winter semester 2020/21', de: 'Wintersemester 2020/21'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's21', en: 'summer semester 2021', de: 'Sommersemester 2021'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w21/22', en: 'winter semester 2021/22', de: 'Wintersemester 2021/22'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's22', en: 'summer semester 2022', de: 'Sommersemester 2022'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w22/23', en: 'winter semester 2022/23', de: 'Wintersemester 2022/23'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's23', en: 'summer semester 2023', de: 'Sommersemester 2023'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w23/24', en: 'winter semester 2023/24', de: 'Wintersemester 2023/24'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's24', en: 'summer semester 2024', de: 'Sommersemester 2024'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w24/25', en: 'winter semester 2024/25', de: 'Wintersemester 2024/25'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's25', en: 'summer semester 2025', de: 'Sommersemester 2025'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w25/26', en: 'winter semester 2025/26', de: 'Wintersemester 2025/26'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's26', en: 'summer semester 2026', de: 'Sommersemester 2026'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w26/27', en: 'winter semester 2026/27', de: 'Wintersemester 2026/27'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's27', en: 'summer semester 2027', de: 'Sommersemester 2027'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w27/28', en: 'winter semester 2027/28', de: 'Wintersemester 2027/28'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's28', en: 'summer semester 2028', de: 'Sommersemester 2028'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w28/29', en: 'winter semester 2028/29', de: 'Wintersemester 2028/29'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's29', en: 'summer semester 2029', de: 'Sommersemester 2029'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w29/30', en: 'winter semester 2029/30', de: 'Wintersemester 2029/30'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's30', en: 'summer semester 2030', de: 'Sommersemester 2030'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 'w30/31', en: 'winter semester 2030/31', de: 'Wintersemester 2030/31'], BOOTSTRAP)
        RefdataValue.loc('Semester',      [key: 's31', en: 'summer semester 2031', de: 'Sommersemester 2031'], BOOTSTRAP)

        RefdataValue.loc('Survey Type',      [key: 'renewal', en: 'Renewal Survey', de: 'Verlängerungsumfrage'], BOOTSTRAP)
        RefdataValue.loc('Survey Type',      [key: 'interest', en: 'Interest Survey', de: 'Interessenumfrage'], BOOTSTRAP)

        RefdataValue.loc('Survey Status',      [en: 'Ready', de: 'Bereit'], BOOTSTRAP)
        RefdataValue.loc('Survey Status',      [en: 'In Processing', de: 'In Bearbeitung'], BOOTSTRAP)
        RefdataValue.loc('Survey Status',      [en: 'In Evaluation', de: 'In Auswertung'], BOOTSTRAP)
        RefdataValue.loc('Survey Status',      [en: 'Abgeschlossen', de: 'Completed'], BOOTSTRAP)
        RefdataValue.loc('Survey Status',      [en: 'Survey started', de: 'Umfrage gestartet'], BOOTSTRAP)
        RefdataValue.loc('Survey Status',      [en: 'Survey started', de: 'Umfrage beendet'], BOOTSTRAP)

        createRefdataWithI10nExplanation()
    }

    void createRefdataWithI10nExplanation() {

        I10nTranslation.createOrUpdateI10n(RefdataValue.loc('Number Type',[en: 'Students', de: 'Studenten'], BOOTSTRAP),'expl',[en:'',de:'Eingeschriebene Studierende an der angeschlossenen Hochschule'])
        I10nTranslation.createOrUpdateI10n(RefdataValue.loc('Number Type',[en: 'Scientific staff', de: 'wissenschaftliches Personal'], BOOTSTRAP),'expl',[en:'',de:'Personal, das an Instituten der angeschlossenen Hochschule in Projekten o.Ä. beschäftigt ist'])
        I10nTranslation.createOrUpdateI10n(RefdataValue.loc('Number Type',[en: 'User', de: 'Nutzer'], BOOTSTRAP),'expl',[en:'',de:'Studierende, Lehrkräfte sowie weiteres Personal der Hochschule zusammengerechnet'])
        I10nTranslation.createOrUpdateI10n(RefdataValue.loc('Number Type',[en: 'Population', de: 'Einwohner'], BOOTSTRAP),'expl',[en:'',de:'Population der Ortschaft, in der die Bibliothek beheimatet ist'])

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

        RefdataCategory.loc('Authority',
                [en: 'Authority', de: 'Autorität'], BOOTSTRAP)

        RefdataValue.loc('Authority', [en: 'Author', de: 'Autor'], BOOTSTRAP)
        RefdataValue.loc('Authority', [en: 'Institution', de: 'Institution'], BOOTSTRAP)
        RefdataValue.loc('Authority', [en: 'Author and Institution', de: 'Autor und Institution'], BOOTSTRAP)
        RefdataValue.loc('Authority', [en: 'Research Funding Institution', de: 'Forschungsförderer'], BOOTSTRAP)

        RefdataCategory.loc('CostItem.Type',
                [en: 'Type', de: 'Typ'], BOOTSTRAP)

        RefdataValue.loc('CostItem.Type', [en: 'Actual', de: 'Konkrete Instanz'], BOOTSTRAP)
        RefdataValue.loc('CostItem.Type', [en: 'Template', de: 'Vorlage'], BOOTSTRAP)

        RefdataCategory.loc('CostItemCategory',
                [en: 'CostItemCategory', de: 'CostItemCategory'], BOOTSTRAP)

        //RefdataValue.loc('CostItemCategory', [en: 'Price', de: 'Preis'], BOOTSTRAP)
        //RefdataValue.loc('CostItemCategory', [en: 'Bank Charge', de: 'Bank Charge'], BOOTSTRAP)
        //RefdataValue.loc('CostItemCategory', [en: 'Refund', de: 'Erstattung'], BOOTSTRAP)
        //RefdataValue.loc('CostItemCategory', [en: 'Other', de: 'Andere'], BOOTSTRAP)

        RefdataCategory.loc('CostItemElement',
                [en: 'CostItemElement', de: 'CostItemElement'], BOOTSTRAP)

        RefdataValue.loc('CostItemElement', [key: 'price: list price', en: 'price: list price', de: 'Preis: Listenpreis'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'price: provider price', en: 'price: provider price', de: 'Preis: Anbieterpreis'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'price: consortial price', en: 'price: consortial price', de: 'Preis: Konsortialpreis'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'price: final price', en: 'price: final price', de: 'Preis: Endpreis'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'price: other', en: 'price: other', de: 'Preis: Sonstige'], BOOTSTRAP)

        RefdataValue.loc('CostItemElement', [key: 'discount: consortial discount', en: 'discount: consortial discount', de: 'Rabatt: Konsortialrabatt'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'discount: alliance licence discount', en: 'discount: alliance licence discount', de: 'Rabatt für Allianzlizenz'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'discount: single payment discount', en: 'discount: single payment discount', de: 'Rabatt für eine Rechnung via Konsortium'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'discount: multiyear discount', en: 'discount: multiyear discount', de: 'Rabatt für Mehrjahresvertrag'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'discount: quantity discount', en: 'discount: quantity discount', de: 'Rabatt: Mengenrabatt'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'discount: early pay discount', en: 'discount: early pay discount', de: 'Rabatt: Frühzahlerrabatt'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'discount: other', en: 'discount: other', de: 'Rabatt: Sonstige'], BOOTSTRAP)

        RefdataValue.loc('CostItemElement', [key: 'refund: currency rate', en: 'refund: currency rate', de: 'Erstattung: Kursgutschrift'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'refund: OA', en: 'refund: OA', de: 'Erstattung: Open-Acces-Gutschrift'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'refund: retransfer', en: 'refund: retransfer', de: 'Erstattung: Rücküberweisung'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'refund: system downtime', en: 'refund: system downtime', de: 'Erstattung: Ersatz für Ausfallzeiten'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'refund: other', en: 'refund: other', de: 'Erstattung: Sonstige'], BOOTSTRAP)

        RefdataValue.loc('CostItemElement', [key: 'additionalclaim: currency rate', en: 'additionalclaim: currency rate', de: 'Nachforderung aus Kursdifferenz'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'additionalclaim: other', en: 'additionalclaim: other', de: 'Nachforderung: Sonstige'], BOOTSTRAP)

        RefdataValue.loc('CostItemElement', [key: 'fee: bank charge', en: 'fee: bank charge', de: 'Gebühr: Bankgebühr'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'fee: invoicing', en: 'fee: invoicing', de: 'Gebühr: Rechnungsstellungsgebühr'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'fee: administration', en: 'fee: administration', de: 'Gebühr: Verwaltungsgebühr'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'fee: technical access', en: 'fee: technical access', de: 'Gebühr: Plattformgebühr'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'fee: setup', en: 'fee: setup', de: 'Gebühr: SetUp-Gebühr'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'fee: other', en: 'fee: other', de: 'Gebühr: Sonstige'], BOOTSTRAP)

        RefdataValue.loc('CostItemElement', [key: 'special funds: central funding', en: 'special funds: central funding', de: 'Sondermittel: Zentralmittel'], BOOTSTRAP)

        RefdataValue.loc('CostItemElement', [key: 'tax: purchase tax 19', en: 'tax: purchase tax 19%', de: 'Steuer: Umsatzsteuer 19%'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'tax: purchase tax 7', en: 'tax: purchase tax 7%', de: 'Steuer: Umsatzsteuer 7%'], BOOTSTRAP)
        RefdataValue.loc('CostItemElement', [key: 'tax: source tax', en: 'tax:  source tax', de: 'Steuer: Quellensteuer'], BOOTSTRAP)

        RefdataCategory.loc('CostItemStatus',
                [en: 'CostItemStatus', de: 'CostItemStatus'], BOOTSTRAP)

        RefdataValue.loc('CostItemStatus', [en: 'Estimate', de: 'geschätzt'], BOOTSTRAP)
        RefdataValue.loc('CostItemStatus', [en: 'Commitment', de: 'zugesagt'], BOOTSTRAP)
        RefdataValue.loc('CostItemStatus', [en: 'Actual', de: 'feststehend'], BOOTSTRAP)
        RefdataValue.loc('CostItemStatus', [en: 'Other', de: 'Sonstige'], BOOTSTRAP)

        RefdataCategory.loc('Document Context Status', [en: 'Document Context Status'], BOOTSTRAP)

        RefdataValue.loc('Document Context Status', [en: 'Deleted', de: 'Gelöscht'], BOOTSTRAP)

        RefdataCategory.loc('Document Type',
                [en: 'Document Type', de: 'Dokumenttyp'], BOOTSTRAP)

        RefdataValue.loc('Document Type', [en: 'Announcement', de: 'Angekündigung'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [en: 'Subscription', de: 'Lizenz'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [en: 'License', de: 'Vertrag'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [en: 'General', de: 'Allgemein'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [en: 'Addendum', de: 'Zusatz'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [en: 'Note', de: 'Anmerkung'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [en: 'ONIX-PL License', de: 'ONIX-PL Lizenz'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [key: 'Usage Statistics', en: 'Usage Statistics', de: 'Nutzungsstatistik'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [key: 'Offer', en: 'Offer', de: 'Angebot'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [key: 'Renewal', en: 'Renewal', de: 'Renewal'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [key: 'Order', en: 'Order', de: 'Bestellung'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [key: 'Invoice', en: 'Invoice', de: 'Rechnung'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [key: 'Announcement', en: 'Announcement', de: 'Ankündigung'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [key: 'Note', en: 'Note', de: 'Anmerkung'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [key: 'Costs', en: 'Costs', de: 'Preise'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [key: 'Metadata', en: 'Metadata', de: 'Metadaten'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [key: 'KBART', en: 'KBART', de: 'KBART'], BOOTSTRAP)
        RefdataValue.loc('Document Type', [key: 'Title List', en: 'Title List', de: 'Titelliste'], BOOTSTRAP)


        RefdataCategory.loc('Entitlement Issue Status',
                [en: 'Entitlement Issue Status', de: 'Entitlement Issue Status'], BOOTSTRAP)

        RefdataValue.loc('Entitlement Issue Status', [en: 'Live', de: 'Live'], BOOTSTRAP)
        RefdataValue.loc('Entitlement Issue Status', [en: 'Current', de: 'Current'], BOOTSTRAP)
        RefdataValue.loc('Entitlement Issue Status', [en: 'Deleted', de: 'Deleted'], BOOTSTRAP)

        RefdataCategory.loc('IE Access Status',
                [en: 'IE Access Status', de: 'IE Access Status'], BOOTSTRAP)

        RefdataValue.loc('IE Access Status', [en: 'ERROR - No Subscription Start and/or End Date', de: 'ERROR - No Subscription Start and/or End Date'], BOOTSTRAP)
        RefdataValue.loc('IE Access Status', [en: 'Current', de: 'Aktuell'], BOOTSTRAP)
        RefdataValue.loc('IE Access Status', [en: 'Current(*)', de: 'Aktuell(*)'], BOOTSTRAP)
        RefdataValue.loc('IE Access Status', [en: 'Expected', de: 'Erwartet'], BOOTSTRAP)
        RefdataValue.loc('IE Access Status', [en: 'Expired', de: 'Abgelaufen'], BOOTSTRAP)

        RefdataCategory.loc('IEMedium',
                [en: 'IEMedium', de: 'IEMedium'], BOOTSTRAP)

        RefdataValue.loc('IEMedium', [en: 'Print', de: 'Print'], BOOTSTRAP)
        RefdataValue.loc('IEMedium', [en: 'Electronic', de: 'Elektronisch'], BOOTSTRAP)
        RefdataValue.loc('IEMedium', [en: 'Print and Electronic', de: 'Print und Elektronisch'], BOOTSTRAP)

        RefdataCategory.loc('LicenseCategory',
                [en: 'LicenseCategory', de: 'Lizenzkategorie'], BOOTSTRAP)

        RefdataValue.loc('LicenseCategory', [en: 'Content', de: 'Content'], BOOTSTRAP)
        RefdataValue.loc('LicenseCategory', [en: 'Software', de: 'Software'], BOOTSTRAP)
        RefdataValue.loc('LicenseCategory', [en: 'Other', de: 'Andere'], BOOTSTRAP)

        RefdataCategory.loc(RefdataCategory.LIC_TYPE,
                [en: 'License Type', de: 'Lizenztyp'], BOOTSTRAP)

        RefdataValue.loc(RefdataCategory.LIC_TYPE, [en: 'Actual', de: 'Konkrete Instanz'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.LIC_TYPE, [en: 'Template', de: 'Vorlage'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.LIC_TYPE, [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)

        RefdataCategory.loc('License.OA.Type',
                [en: 'Open Acces Type', de: 'Open-Acces Typ'], BOOTSTRAP)

        RefdataValue.loc('License.OA.Type', [en: 'No Open Access', de: 'Kein Open-Access'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Type', [en: 'Hybrid', de: 'Hybrid'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Type', [en: 'Green Open Access', de: 'Green Open-Access'], BOOTSTRAP)
        // RefdataValue.loc('License.OA.Type', [en: 'Red Open Access', de: 'Red Open-Access'], BOOTSTRAP) // deleted
        RefdataValue.loc('License.OA.Type', [en: 'Gold Open Access', de: 'Gold Open-Access'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Type', [en: 'Blue Open Access', de: 'Blue Open-Access'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Type', [en: 'Yellow Open Access', de: 'Yellow Open-Access'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Type', [en: 'White Open Access', de: 'White Open-Access'], BOOTSTRAP)

        RefdataCategory.loc('License.OA.eArcVersion',
                [en: 'License.OA.eArcVersion', de: 'License.OA.eArcVersion'], BOOTSTRAP)

        RefdataValue.loc('License.OA.eArcVersion', [en: 'Accepted Author'], BOOTSTRAP)
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Manuscript (AAM)'], BOOTSTRAP)
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Publisher-PDF', de: 'Verlags-PDF-Datei'], BOOTSTRAP)
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Postprint', de: 'Postprint'], BOOTSTRAP)
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Preprint', de: 'Preprint'], BOOTSTRAP)
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Preprint with ePrint URL', de:'Preprint mit ePrint-URL'], BOOTSTRAP)

        RefdataCategory.loc(RefdataCategory.LIC_STATUS,
                [en: 'License Status', de: 'Lizenzstatus'], BOOTSTRAP)

        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Current', de: 'Aktiv'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Deleted', de: 'Gelöscht'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'In Progress', de:'In Bearbeitung'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Retired', de: 'Abgelaufen'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)

        RefdataCategory.loc('PendingChangeStatus',
                [en: 'PendingChangeStatus', de: 'PendingChangeStatus'], BOOTSTRAP)

        RefdataValue.loc('PendingChangeStatus', [en: 'Pending', de: 'Ausstehend'], BOOTSTRAP)
        RefdataValue.loc('PendingChangeStatus', [en: 'Accepted', de: 'Angenommen'], BOOTSTRAP)
        RefdataValue.loc('PendingChangeStatus', [en: 'Rejected', de: 'Abgelehnt'], BOOTSTRAP)

        RefdataCategory.loc(RefdataCategory.PKG_LIST_STAT,
                [en: RefdataCategory.PKG_LIST_STAT, de: RefdataCategory.PKG_LIST_STAT], BOOTSTRAP)
        RefdataCategory.loc(RefdataCategory.PKG_BREAKABLE,
                [en: RefdataCategory.PKG_BREAKABLE, de: RefdataCategory.PKG_BREAKABLE], BOOTSTRAP)
        RefdataCategory.loc(RefdataCategory.PKG_CONSISTENT,
                [en: RefdataCategory.PKG_CONSISTENT, de: RefdataCategory.PKG_CONSISTENT], BOOTSTRAP)
        RefdataCategory.loc(RefdataCategory.PKG_FIXED,
                [en: RefdataCategory.PKG_FIXED, de: RefdataCategory.PKG_FIXED], BOOTSTRAP)

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

        RefdataCategory.loc('TaxType',
                [en: 'TaxType', de: 'TaxType'], BOOTSTRAP)

        RefdataValue.loc('TaxType', [en: 'taxable', de: 'steuerbar'], BOOTSTRAP)
        RefdataValue.loc('TaxType', [en: 'not taxable', de: 'nicht steuerbar'], BOOTSTRAP)
        RefdataValue.loc('TaxType', [en: 'taxable tax-exempt', de: 'steuerbar steuerbefreit'], BOOTSTRAP)
        RefdataValue.loc('TaxType', [en: 'not applicable', de: 'nicht anwendbar'], BOOTSTRAP)

        RefdataCategory.loc(RefdataCategory.TI_STATUS,
                [en: RefdataCategory.TI_STATUS, de: RefdataCategory.TI_STATUS], BOOTSTRAP)

        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Current', de: 'Aktuell'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Deleted', de: 'Gelöscht'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'In Progress', de:'In Bearbeitung'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)

        RefdataCategory.loc(RefdataCategory.TI_TYPE,
                [en: RefdataCategory.TI_TYPE, de: RefdataCategory.TI_TYPE], BOOTSTRAP)

        RefdataValue.loc(RefdataCategory.TI_TYPE, [en: 'Journal', de: 'Journal'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TI_TYPE, [en: 'EBook', de: 'EBook'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TI_TYPE, [en: 'Database', de:'Datenbank'], BOOTSTRAP)

        RefdataCategory.loc('TitleInstancePackagePlatform.DelayedOA',
                [en: 'TitleInstancePackagePlatform.DelayedOA', de: 'TitleInstancePackagePlatform.DelayedOA'], BOOTSTRAP)

        RefdataValue.loc('TitleInstancePackagePlatform.DelayedOA', [en: 'No', de: 'Nein'], BOOTSTRAP)
        RefdataValue.loc('TitleInstancePackagePlatform.DelayedOA', [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)
        RefdataValue.loc('TitleInstancePackagePlatform.DelayedOA', [en: 'Yes', de: 'Ja'], BOOTSTRAP)

        RefdataCategory.loc('TitleInstancePackagePlatform.HybridOA',
                [en: 'TitleInstancePackagePlatform.HybridOA', de: 'TitleInstancePackagePlatform.HybridOA'], BOOTSTRAP)

        RefdataValue.loc('TitleInstancePackagePlatform.HybridOA', [en: 'No', de: 'Nein'], BOOTSTRAP)
        RefdataValue.loc('TitleInstancePackagePlatform.HybridOA', [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)
        RefdataValue.loc('TitleInstancePackagePlatform.HybridOA', [en: 'Yes', de: 'Ja'], BOOTSTRAP)

        RefdataCategory.loc('Tipp.StatusReason',
                [en: 'Tipp.StatusReason', de: 'Tipp.StatusReason'], BOOTSTRAP)

        RefdataValue.loc('Tipp.StatusReason', [en: 'Transfer Out', de: 'Transfer Out'], BOOTSTRAP)
        RefdataValue.loc('Tipp.StatusReason', [en: 'Transfer In', de: 'Transfer In'], BOOTSTRAP)

        RefdataCategory.loc('TitleInstancePackagePlatform.PaymentType',
                [en: 'TitleInstancePackagePlatform.PaymentType', de: 'TitleInstancePackagePlatform.PaymentType'], BOOTSTRAP)

        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'Complimentary', de: 'Complimentary'], BOOTSTRAP)
        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'Limited Promotion', de: 'Limited Promotion'], BOOTSTRAP)
        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'Paid', de: 'Paid'], BOOTSTRAP)
        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'OA', de: 'OA'], BOOTSTRAP)
        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'Opt Out Promotion', de: 'Opt Out Promotion'], BOOTSTRAP)
        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'Uncharged', de: 'Uncharged'], BOOTSTRAP)
        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)

        RefdataCategory.loc(RefdataCategory.TIPP_STATUS,
                [en: RefdataCategory.TIPP_STATUS, de: RefdataCategory.TIPP_STATUS], BOOTSTRAP)

        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Current', de: 'Aktuell'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Expected', de: 'Expected'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Deleted', de: 'Gelöscht'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Transferred', de: 'Transferred'], BOOTSTRAP)
        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)

        RefdataCategory.loc('TIPP Access Status',
                [en: 'TIPP Access Status', de: 'TIPP Access Status'], BOOTSTRAP)

        RefdataValue.loc('TIPP Access Status', [en: 'Current(*)', de: 'Aktuell(*)'], BOOTSTRAP)
        RefdataValue.loc('TIPP Access Status', [en: 'Expected', de: 'Erwartet'], BOOTSTRAP)
        RefdataValue.loc('TIPP Access Status', [en: 'Expired', de: 'Abgelaufen'], BOOTSTRAP)
        RefdataValue.loc('TIPP Access Status', [en: 'Current', de: 'Aktuell'], BOOTSTRAP)

        // Controlled values from the <UsageType> element.

        RefdataCategory.loc('UsageStatus',
                [en: 'UsageStatus', de: 'UsageStatus'], BOOTSTRAP)

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
    // log.debug("New gokb record source: ${gokb_record_source}")

        //Reminders for Cron
        RefdataCategory.loc("Language", [en: "Language", de: "Sprache"], BOOTSTRAP)
        RefdataValue.loc("Language", [key: 'en', en: "English", de:"Englisch"], BOOTSTRAP)
        RefdataValue.loc("Language", [key: 'de', en: "German", de:"Deutsch"], BOOTSTRAP)

        RefdataCategory.loc("ReminderMethod", [en: "email"], BOOTSTRAP)

        RefdataCategory.loc('ReminderUnit',
                [en: 'ReminderUnit', de: 'ReminderUnit'], BOOTSTRAP)

        RefdataValue.loc('ReminderUnit', [en: 'Day', de: 'Tag'], BOOTSTRAP)
        RefdataValue.loc('ReminderUnit', [en: 'Week', de: 'Woche'], BOOTSTRAP)
        RefdataValue.loc('ReminderUnit', [en: 'Month', de: 'Monat'], BOOTSTRAP)

        RefdataCategory.loc("ReminderTrigger", [en: "Subscription Manual Renewal Date"], BOOTSTRAP)
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

    def setupCurrencies = {

        RefdataCategory.loc('Currency', [en: 'Currency', de: 'Währung'], BOOTSTRAP)
        
        RefdataValue.loc('Currency', [key: 'AED',  en:'AED - United Arab Emirates Dirham', de:'AED - United Arab Emirates Dirham'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'AFN',  en:'AFN - Afghanistan Afghani', de:'AFN - Afghanistan Afghani'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'ALL',  en:'ALL - Albania Lek', de:'ALL - Albania Lek'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'AMD',  en:'AMD - Armenia Dram', de:'AMD - Armenia Dram'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'ANG',  en:'ANG - Netherlands Antilles Guilder', de:'ANG - Netherlands Antilles Guilder'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'AOA',  en:'AOA - Angola Kwanza', de:'AOA - Angola Kwanza'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'ARS',  en:'ARS - Argentina Peso', de:'ARS - Argentina Peso'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'AUD',  en:'AUD - Australia Dollar', de:'AUD - Australia Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'AWG',  en:'AWG - Aruba Guilder', de:'AWG - Aruba Guilder'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'AZN',  en:'AZN - Azerbaijan New Manat', de:'AZN - Azerbaijan New Manat'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BAM',  en:'BAM - Bosnia and Herzegovina Convertible Marka', de:'BAM - Bosnia and Herzegovina Convertible Marka'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BBD',  en:'BBD - Barbados Dollar', de:'BBD - Barbados Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BDT',  en:'BDT - Bangladesh Taka', de:'BDT - Bangladesh Taka'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BGN',  en:'BGN - Bulgaria Lev', de:'BGN - Bulgaria Lev'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BHD',  en:'BHD - Bahrain Dinar', de:'BHD - Bahrain Dinar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BIF',  en:'BIF - Burundi Franc', de:'BIF - Burundi Franc'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BMD',  en:'BMD - Bermuda Dollar', de:'BMD - Bermuda Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BND',  en:'BND - Brunei Darussalam Dollar', de:'BND - Brunei Darussalam Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BOB',  en:'BOB - Bolivia Boliviano', de:'BOB - Bolivia Boliviano'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BRL',  en:'BRL - Brazil Real', de:'BRL - Brazil Real'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BSD',  en:'BSD - Bahamas Dollar', de:'BSD - Bahamas Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BTN',  en:'BTN - Bhutan Ngultrum', de:'BTN - Bhutan Ngultrum'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BWP',  en:'BWP - Botswana Pula', de:'BWP - Botswana Pula'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BYR',  en:'BYR - Belarus Ruble', de:'BYR - Belarus Ruble'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'BZD',  en:'BZD - Belize Dollar', de:'BZD - Belize Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'CAD',  en:'CAD - Canada Dollar', de:'CAD - Canada Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'CDF',  en:'CDF - Congo/Kinshasa Franc', de:'CDF - Congo/Kinshasa Franc'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'CHF',  en:'CHF - Switzerland Franc', de:'CHF - Switzerland Franc'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'CLP',  en:'CLP - Chile Peso', de:'CLP - Chile Peso'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'CNY',  en:'CNY - China Yuan Renminbi', de:'CNY - China Yuan Renminbi'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'COP',  en:'COP - Colombia Peso', de:'COP - Colombia Peso'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'CRC',  en:'CRC - Costa Rica Colon', de:'CRC - Costa Rica Colon'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'CUC',  en:'CUC - Cuba Convertible Peso', de:'CUC - Cuba Convertible Peso'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'CUP',  en:'CUP - Cuba Peso', de:'CUP - Cuba Peso'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'CVE',  en:'CVE - Cape Verde Escudo', de:'CVE - Cape Verde Escudo'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'CZK',  en:'CZK - Czech Republic Koruna', de:'CZK - Czech Republic Koruna'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'DJF',  en:'DJF - Djibouti Franc', de:'DJF - Djibouti Franc'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'DKK',  en:'DKK - Denmark Krone', de:'DKK - Denmark Krone'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'DOP',  en:'DOP - Dominican Republic Peso', de:'DOP - Dominican Republic Peso'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'DZD',  en:'DZD - Algeria Dinar', de:'DZD - Algeria Dinar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'EGP',  en:'EGP - Egypt Pound', de:'EGP - Egypt Pound'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'ERN',  en:'ERN - Eritrea Nakfa', de:'ERN - Eritrea Nakfa'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'ETB',  en:'ETB - Ethiopia Birr', de:'ETB - Ethiopia Birr'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'EUR',  en:'EUR - Euro Member Countries', de:'EUR - Euro Member Countries'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'FJD',  en:'FJD - Fiji Dollar', de:'FJD - Fiji Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'FKP',  en:'FKP - Falkland Islands (Malvinas) Pound', de:'FKP - Falkland Islands (Malvinas) Pound'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'GBP',  en:'GBP - United Kingdom Pound', de:'GBP - United Kingdom Pound'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'GEL',  en:'GEL - Georgia Lari', de:'GEL - Georgia Lari'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'GGP',  en:'GGP - Guernsey Pound', de:'GGP - Guernsey Pound'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'GHS',  en:'GHS - Ghana Cedi', de:'GHS - Ghana Cedi'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'GIP',  en:'GIP - Gibraltar Pound', de:'GIP - Gibraltar Pound'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'GMD',  en:'GMD - Gambia Dalasi', de:'GMD - Gambia Dalasi'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'GNF',  en:'GNF - Guinea Franc', de:'GNF - Guinea Franc'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'GTQ',  en:'GTQ - Guatemala Quetzal', de:'GTQ - Guatemala Quetzal'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'GYD',  en:'GYD - Guyana Dollar', de:'GYD - Guyana Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'HKD',  en:'HKD - Hong Kong Dollar', de:'HKD - Hong Kong Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'HNL',  en:'HNL - Honduras Lempira', de:'HNL - Honduras Lempira'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'HRK',  en:'HRK - Croatia Kuna', de:'HRK - Croatia Kuna'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'HTG',  en:'HTG - Haiti Gourde', de:'HTG - Haiti Gourde'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'HUF',  en:'HUF - Hungary Forint', de:'HUF - Hungary Forint'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'IDR',  en:'IDR - Indonesia Rupiah', de:'IDR - Indonesia Rupiah'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'ILS',  en:'ILS - Israel Shekel', de:'ILS - Israel Shekel'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'IMP',  en:'IMP - Isle of Man Pound', de:'IMP - Isle of Man Pound'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'INR',  en:'INR - India Rupee', de:'INR - India Rupee'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'IQD',  en:'IQD - Iraq Dinar', de:'IQD - Iraq Dinar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'IRR',  en:'IRR - Iran Rial', de:'IRR - Iran Rial'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'ISK',  en:'ISK - Iceland Krona', de:'ISK - Iceland Krona'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'JEP',  en:'JEP - Jersey Pound', de:'JEP - Jersey Pound'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'JMD',  en:'JMD - Jamaica Dollar', de:'JMD - Jamaica Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'JOD',  en:'JOD - Jordan Dinar', de:'JOD - Jordan Dinar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'JPY',  en:'JPY - Japan Yen', de:'JPY - Japan Yen'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'KES',  en:'KES - Kenya Shilling', de:'KES - Kenya Shilling'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'KGS',  en:'KGS - Kyrgyzstan Som', de:'KGS - Kyrgyzstan Som'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'KHR',  en:'KHR - Cambodia Riel', de:'KHR - Cambodia Riel'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'KMF',  en:'KMF - Comoros Franc', de:'KMF - Comoros Franc'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'KPW',  en:'KPW - Korea (North) Won', de:'KPW - Korea (North) Won'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'KRW',  en:'KRW - Korea (South) Won', de:'KRW - Korea (South) Won'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'KWD',  en:'KWD - Kuwait Dinar', de:'KWD - Kuwait Dinar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'KYD',  en:'KYD - Cayman Islands Dollar', de:'KYD - Cayman Islands Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'KZT',  en:'KZT - Kazakhstan Tenge', de:'KZT - Kazakhstan Tenge'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'LAK',  en:'LAK - Laos Kip', de:'LAK - Laos Kip'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'LBP',  en:'LBP - Lebanon Pound', de:'LBP - Lebanon Pound'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'LKR',  en:'LKR - Sri Lanka Rupee', de:'LKR - Sri Lanka Rupee'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'LRD',  en:'LRD - Liberia Dollar', de:'LRD - Liberia Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'LSL',  en:'LSL - Lesotho Loti', de:'LSL - Lesotho Loti'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'LYD',  en:'LYD - Libya Dinar', de:'LYD - Libya Dinar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MAD',  en:'MAD - Morocco Dirham', de:'MAD - Morocco Dirham'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MDL',  en:'MDL - Moldova Leu', de:'MDL - Moldova Leu'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MGA',  en:'MGA - Madagascar Ariary', de:'MGA - Madagascar Ariary'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MKD',  en:'MKD - Macedonia Denar', de:'MKD - Macedonia Denar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MMK',  en:'MMK - Myanmar (Burma) Kyat', de:'MMK - Myanmar (Burma) Kyat'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MNT',  en:'MNT - Mongolia Tughrik', de:'MNT - Mongolia Tughrik'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MOP',  en:'MOP - Macau Pataca', de:'MOP - Macau Pataca'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MRO',  en:'MRO - Mauritania Ouguiya', de:'MRO - Mauritania Ouguiya'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MUR',  en:'MUR - Mauritius Rupee', de:'MUR - Mauritius Rupee'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MVR',  en:'MVR - Maldives (Maldive Islands) Rufiyaa', de:'MVR - Maldives (Maldive Islands) Rufiyaa'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MWK',  en:'MWK - Malawi Kwacha', de:'MWK - Malawi Kwacha'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MXN',  en:'MXN - Mexico Peso', de:'MXN - Mexico Peso'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MYR',  en:'MYR - Malaysia Ringgit', de:'MYR - Malaysia Ringgit'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'MZN',  en:'MZN - Mozambique Metical', de:'MZN - Mozambique Metical'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'NAD',  en:'NAD - Namibia Dollar', de:'NAD - Namibia Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'NGN',  en:'NGN - Nigeria Naira', de:'NGN - Nigeria Naira'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'NIO',  en:'NIO - Nicaragua Cordoba', de:'NIO - Nicaragua Cordoba'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'NOK',  en:'NOK - Norway Krone', de:'NOK - Norway Krone'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'NPR',  en:'NPR - Nepal Rupee', de:'NPR - Nepal Rupee'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'NZD',  en:'NZD - New Zealand Dollar', de:'NZD - New Zealand Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'OMR',  en:'OMR - Oman Rial', de:'OMR - Oman Rial'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'PAB',  en:'PAB - Panama Balboa', de:'PAB - Panama Balboa'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'PEN',  en:'PEN - Peru Nuevo Sol', de:'PEN - Peru Nuevo Sol'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'PGK',  en:'PGK - Papua New Guinea Kina', de:'PGK - Papua New Guinea Kina'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'PHP',  en:'PHP - Philippines Peso', de:'PHP - Philippines Peso'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'PKR',  en:'PKR - Pakistan Rupee', de:'PKR - Pakistan Rupee'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'PLN',  en:'PLN - Poland Zloty', de:'PLN - Poland Zloty'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'PYG',  en:'PYG - Paraguay Guarani', de:'PYG - Paraguay Guarani'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'QAR',  en:'QAR - Qatar Riyal', de:'QAR - Qatar Riyal'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'RON',  en:'RON - Romania New Leu', de:'RON - Romania New Leu'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'RSD',  en:'RSD - Serbia Dinar', de:'RSD - Serbia Dinar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'RUB',  en:'RUB - Russia Ruble', de:'RUB - Russia Ruble'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'RWF',  en:'RWF - Rwanda Franc', de:'RWF - Rwanda Franc'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SAR',  en:'SAR - Saudi Arabia Riyal', de:'SAR - Saudi Arabia Riyal'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SBD',  en:'SBD - Solomon Islands Dollar', de:'SBD - Solomon Islands Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SCR',  en:'SCR - Seychelles Rupee', de:'SCR - Seychelles Rupee'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SDG',  en:'SDG - Sudan Pound', de:'SDG - Sudan Pound'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SEK',  en:'SEK - Sweden Krona', de:'SEK - Sweden Krona'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SGD',  en:'SGD - Singapore Dollar', de:'SGD - Singapore Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SHP',  en:'SHP - Saint Helena Pound', de:'SHP - Saint Helena Pound'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SLL',  en:'SLL - Sierra Leone Leone', de:'SLL - Sierra Leone Leone'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SOS',  en:'SOS - Somalia Shilling', de:'SOS - Somalia Shilling'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SPL',  en:'SPL - Seborga Luigino', de:'SPL - Seborga Luigino'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SRD',  en:'SRD - Suriname Dollar', de:'SRD - Suriname Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'STD',  en:'STD - São Tomé and Príncipe Dobra', de:'STD - São Tomé and Príncipe Dobra'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SVC',  en:'SVC - El Salvador Colon', de:'SVC - El Salvador Colon'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SYP',  en:'SYP - Syria Pound', de:'SYP - Syria Pound'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'SZL',  en:'SZL - Swaziland Lilangeni', de:'SZL - Swaziland Lilangeni'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'THB',  en:'THB - Thailand Baht', de:'THB - Thailand Baht'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'TJS',  en:'TJS - Tajikistan Somoni', de:'TJS - Tajikistan Somoni'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'TMT',  en:'TMT - Turkmenistan Manat', de:'TMT - Turkmenistan Manat'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'TND',  en:'TND - Tunisia Dinar', de:'TND - Tunisia Dinar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'TOP',  en:'TOP - Tonga Pa\'anga', de:'TOP - Tonga Pa\'anga'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'TRY',  en:'TRY - Turkey Lira', de:'TRY - Turkey Lira'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'TTD',  en:'TTD - Trinidad and Tobago Dollar', de:'TTD - Trinidad and Tobago Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'TVD',  en:'TVD - Tuvalu Dollar', de:'TVD - Tuvalu Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'TWD',  en:'TWD - Taiwan New Dollar', de:'TWD - Taiwan New Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'TZS',  en:'TZS - Tanzania Shilling', de:'TZS - Tanzania Shilling'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'UAH',  en:'UAH - Ukraine Hryvnia', de:'UAH - Ukraine Hryvnia'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'UGX',  en:'UGX - Uganda Shilling', de:'UGX - Uganda Shilling'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'USD',  en:'USD - United States Dollar', de:'USD - United States Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'UYU',  en:'UYU - Uruguay Peso', de:'UYU - Uruguay Peso'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'UZS',  en:'UZS - Uzbekistan Som', de:'UZS - Uzbekistan Som'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'VEF',  en:'VEF - Venezuela Bolivar', de:'VEF - Venezuela Bolivar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'VND',  en:'VND - Viet Nam Dong', de:'VND - Viet Nam Dong'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'VUV',  en:'VUV - Vanuatu Vatu', de:'VUV - Vanuatu Vatu'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'WST',  en:'WST - Samoa Tala', de:'WST - Samoa Tala'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'XAF',  en:'XAF - Communauté Financière Africaine (BEAC) CFA Franc BEAC', de:'XAF - Communauté Financière Africaine (BEAC) CFA Franc BEAC'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'XCD',  en:'XCD - East Caribbean Dollar', de:'XCD - East Caribbean Dollar'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'XDR',  en:'XDR - International Monetary Fund (IMF) Special Drawing Rights', de:'XDR - International Monetary Fund (IMF) Special Drawing Rights'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'XOF',  en:'XOF - Communauté Financière Africaine (BCEAO) Franc', de:'XOF - Communauté Financière Africaine (BCEAO) Franc'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'XPF',  en:'XPF - Comptoirs Français du Pacifique (CFP) Franc', de:'XPF - Comptoirs Français du Pacifique (CFP) Franc'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'YER',  en:'YER - Yemen Rial', de:'YER - Yemen Rial'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'ZAR',  en:'ZAR - South Africa Rand', de:'ZAR - South Africa Rand'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'ZMW',  en:'ZMW - Zambia Kwacha', de:'ZMW - Zambia Kwacha'], BOOTSTRAP)
        RefdataValue.loc('Currency', [key: 'ZWD',  en:'ZWD - Zimbabwe Dollar', de:'ZWD - Zimbabwe Dollar'], BOOTSTRAP)
    }

    def setIdentifierNamespace() {

        def namespaces = [
                            [ns: "GND", typ: "com.k_int.kbplus.Creator"],
                            [ns: "ISIL"],
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
            IdentifierNamespace.findByNsIlike(namespace) ?: new IdentifierNamespace(ns: namespace, nsType: typ).save(flush: true);

        }

    }

    /*def setESGOKB() {
         ElasticsearchSource.findByIdentifier("gokb") ?: new ElasticsearchSource(name: 'GOKB ES', identifier: 'gokb', cluster: 'elasticsearch', index: 'gokb', host: '127.0.0.1', gokb_es: true)
    }*/
}