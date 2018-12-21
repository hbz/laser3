import com.k_int.kbplus.*

import com.k_int.kbplus.auth.*
import com.k_int.properties.PropertyDefinition
import de.laser.domain.I10nTranslation
import grails.converters.JSON
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils

class BootStrap {

    def grailsApplication
    def dataloadService

    //  indicates this object is created via current bootstrap
    final static BOOTSTRAP = true

    final static OT = [
            Date:   Date.toString(),
            Int:    Integer.toString(),
            RdC:    RefdataCategory.toString(),
            Rdv:    RefdataValue.toString(),
            String: String.toString(),
    ]

    def init = { servletContext ->

        log.info("SystemId: ${grailsApplication.config.laserSystemId}")

        if (grailsApplication.config.laserSystemId != null) {
            def system_object = SystemObject.findBySysId(grailsApplication.config.laserSystemId) ?: new SystemObject(sysId: grailsApplication.config.laserSystemId).save(flush: true)
        }

        def evt_startup   = new EventLog(event: 'kbplus.startup', message: 'Normal startup', tstp: new Date(System.currentTimeMillis())).save(flush: true)
        def so_filetype   = DataloadFileType.findByName('Subscription Offered File') ?: new DataloadFileType(name: 'Subscription Offered File')
        def plat_filetype = DataloadFileType.findByName('Platforms File') ?: new DataloadFileType(name: 'Platforms File')

        // Reset harddata flag for given refdata

        RefdataValue.executeUpdate('UPDATE RefdataValue rdv SET rdv.hardData =:reset', [reset: false])
        RefdataCategory.executeUpdate('UPDATE RefdataCategory rdc SET rdc.hardData =:reset', [reset: false])

        // Here we go ..

        log.debug("setupRefdata ..")
        setupRefdata()

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

        def auto_approve_memberships = Setting.findByName('AutoApproveMemberships') ?: new Setting(name: 'AutoApproveMemberships', tp: Setting.CONTENT_TYPE_BOOLEAN, defvalue: 'true', value: 'true').save()

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

        //log.debug("createOrgProperties ..")
        //createOrgProperties()

        log.debug("createLicenseProperties ..")
        createLicenseProperties()

        log.debug("createSubscriptionProperties ..")
        createSubscriptionProperties()

        //log.debug("createPrivateProperties ..")
        //createPrivateProperties()

        log.debug("initializeDefaultSettings ..")
        initializeDefaultSettings()

        log.debug("setIdentifierNamespace ..")
        setIdentifierNamespace()

        log.debug("setESGOKB ..")
        setESGOKB()

        log.debug("setJSONFormatDate ..")
        JSON.registerObjectMarshaller(Date) {
            return it?.format("yyyy-MM-dd'T'HH:mm:ss'Z'")
        }

        log.debug("Init completed ..")
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

        OrgPermShare.assertPermShare(view_permission, or_lc_role)
        OrgPermShare.assertPermShare(edit_permission, or_lc_role)

        OrgPermShare.assertPermShare(view_permission, or_licensee_role)
        OrgPermShare.assertPermShare(edit_permission, or_licensee_role)

        OrgPermShare.assertPermShare(view_permission, or_licensee_cons_role)

        def or_sc_role          = RefdataValue.loc('Organisational Role', [en: 'Subscription Consortia', de:'Konsortium'], BOOTSTRAP)
        def or_subscr_role      = RefdataValue.loc('Organisational Role', [en: 'Subscriber', de: 'Teilnehmer'], BOOTSTRAP)
        def or_subscr_cons_role = RefdataValue.loc('Organisational Role', [key: 'Subscriber_Consortial', en: 'Consortial subscriber', de: 'Konsortialteilnehmer'], BOOTSTRAP)

        OrgPermShare.assertPermShare(view_permission, or_sc_role)
        OrgPermShare.assertPermShare(edit_permission, or_sc_role)

        OrgPermShare.assertPermShare(view_permission, or_subscr_role)
        OrgPermShare.assertPermShare(edit_permission, or_subscr_role)

        OrgPermShare.assertPermShare(view_permission, or_subscr_cons_role)

        def cl_owner_role       = RefdataValue.loc('Cluster Role',   [en: 'Cluster Owner'], BOOTSTRAP)
        def cl_member_role      = RefdataValue.loc('Cluster Role',   [en: 'Cluster Member'], BOOTSTRAP)

        def cons_combo          = RefdataValue.loc('Combo Type',     [en: 'Consortium', de: 'Konsortium'], BOOTSTRAP)

        OrgPermShare.assertPermShare(view_permission, cl_owner_role)
        OrgPermShare.assertPermShare(edit_permission, cl_owner_role)

        OrgPermShare.assertPermShare(view_permission, cl_member_role)
        OrgPermShare.assertPermShare(edit_permission, cl_member_role)

        OrgPermShare.assertPermShare(view_permission, cons_combo)

        // Global System Roles

        def yodaRole    = Role.findByAuthority('ROLE_YODA')        ?: new Role(authority: 'ROLE_YODA', roleType: 'transcendent').save(failOnError: true)
        def adminRole   = Role.findByAuthority('ROLE_ADMIN')       ?: new Role(authority: 'ROLE_ADMIN', roleType: 'global').save(failOnError: true)
        def dmRole      = Role.findByAuthority('ROLE_DATAMANAGER') ?: new Role(authority: 'ROLE_DATAMANAGER', roleType: 'global').save(failOnError: true)
        def userRole    = Role.findByAuthority('ROLE_USER')        ?: new Role(authority: 'ROLE_USER', roleType: 'global').save(failOnError: true)
        def apiRole     = Role.findByAuthority('ROLE_API')         ?: new Role(authority: 'ROLE_API', roleType: 'global').save(failOnError: true)

        def apiReaderRole      = Role.findByAuthority('ROLE_API_READER')      ?: new Role(authority: 'ROLE_API_READER', roleType: 'global').save(failOnError: true)
        def apiWriterRole      = Role.findByAuthority('ROLE_API_WRITER')      ?: new Role(authority: 'ROLE_API_WRITER', roleType: 'global').save(failOnError: true)
        def apiDataManagerRole = Role.findByAuthority('ROLE_API_DATAMANAGER') ?: new Role(authority: 'ROLE_API_DATAMANAGER', roleType: 'global').save(failOnError: true)

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
            pd.softData = false
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
        /*
        def allOrgDescr = [en: PropertyDefinition.ORG_PROP, de: PropertyDefinition.ORG_PROP]
        def requiredOrgProps = []
        createPropertyDefinitionsWithI10nTranslations(requiredOrgProps)
        */
    }

    def createLicenseProperties() {

        def allDescr = [en: PropertyDefinition.LIC_PROP, de: PropertyDefinition.LIC_PROP]

        def requiredProps = [
                [name: [en: "Agreement Date", de: "Abschlussdatum"],                            descr:allDescr, type: OT.Date],
                [name: [en: "Alumni Access"],                                                   descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Change to licensed material", de: "Änderung am Vertragsgegenstand"], descr:allDescr, type: OT.String],
                [name: [en: "Concurrent Access", de: "Concurrent Access"],                      descr:allDescr, type: OT.Rdv, cat:'ConcurrentAccess'],
                [name: [en: "Concurrent Users", de: "Concurrent Users"],                        descr:allDescr, type: OT.Int],
                [name: [en: "Enterprise Access"],                                               descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Include in VLE"],                                                  descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Invoicing", de: "Rechnungsstellung"],                              descr:allDescr, type: OT.Date],
                [name: [en: "Metadata delivery"],                                               descr:allDescr, type: OT.String],
                [name: [en: "Method of Authentication", de: "Authentifizierungsverfahren"],     descr:allDescr, type: OT.String],
                [name: [en: "Multi Site Access"],                                               descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "New Underwriter", de: "Aufnahme neuer Teilnehmer"],                descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Payment target", de: "Zahlungsziel"],                              descr:allDescr, type: OT.Date],
                [name: [en: "Partners Access"],                                                 descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Permitted Uses"],                                                  descr:allDescr, type: OT.String],
                [name: [en: "Post Cancellation Access Entitlement"],                            descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Regional Restriction", de: "Regionale Einschränkung"],             descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Service regulations", de: "Servicestandards"],                     descr:allDescr, type: OT.String],
                [name: [en: "Signed"],                                                          descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Usage Statistics", de: "Lieferung von Statistiken"],               descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Wifi Access", de: "WLAN-Zugriff"],                                 descr:allDescr, type: OT.Rdv, cat:'YNO'],

                // New Properties by FAK / Verde Review
                [name: [en: "General Terms note", de: "Allgemeine Bedingungen"],                    descr:allDescr, type: OT.String],
                [name: [en: "User restriction note", de: "Benutzungsbeschränkungen"],               descr:allDescr, type: OT.String],
                [name: [en: "Authorized user definition", de: "Definition für berechtigte Nutzer"], descr:allDescr, type: OT.String],
                [name: [en: "Local authorized user defintion", de: "Lokale Definition für berechtigte Nutzer"],      descr:allDescr, type: OT.String],
                [name: [en: "ILL print or fax", de: "Fernleihe per Papier oder Fax"],           descr:allDescr, type: OT.Rdv, cat:'Permissions'],
                [name: [en: "ILL secure electronic transmission", de: "Fernleihe über sichere elektonische Übermittlung"], descr:allDescr, type: OT.Rdv, cat:'Permissions'],
                [name: [en: "ILL electronic", de: "Fernleihe elektronisch"],                    descr:allDescr, type: OT.Rdv, cat:'Permissions'],
                [name: [en: "ILL record keeping required", de: "Fernleihdatensatz muss gespeichert werden"], descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Fair use clause indicator", de: "Hinweis auf Klausel über die 'faire Nutzung'"], descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "All rights reserved indicator", de: "Hinweis auf 'Alle Rechte vorbehalten'"], descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Data protection override", de: "Datenschutz aufgehoben"],          descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Citation requirement detail", de: "Details der Zitier-Regeln"],    descr:allDescr, type: OT.String],
                [name: [en: "Digitial copy", de: "Digitalkopie"],                               descr:allDescr, type: OT.Rdv, cat:'Permissions'],
                [name: [en: "Print copy", de: "Druckkopie"],                                    descr:allDescr, type: OT.Rdv, cat:'Permissions'],
                [name: [en: "Scholarly sharing", de: "Weitergabe im Rahmen der Lehre"],         descr:allDescr, type: OT.Rdv, cat:'Permissions'],
                [name: [en: "Distance Education", de: "Fernstudium"],                           descr:allDescr, type: OT.Rdv, cat:'Permissions'],
                [name: [en: "Course reserve print", de: "Seminarapparat gedruckt"],             descr:allDescr, type: OT.Rdv, cat:'Permissions'],
                [name: [en: "Course reserve electronic/cached", de: "Seminarapparat elektronisch"], descr:allDescr, type: OT.Rdv, cat:'Permissions'],
                [name: [en: "Electronic link", de: "Elektronischer Link"],                      descr:allDescr, type: OT.Rdv, cat:'Permissions'],
                [name: [en: "Course pack print", de: "Skripte gedruckt"],                       descr:allDescr, type: OT.Rdv, cat:'Permissions'],
                [name: [en: "Course pack electronic", de: "Skripte elektronisch"],              descr:allDescr, type: OT.Rdv, cat:'Permissions'],
                [name: [en: "Remote Access", de: "Remote-Zugriff"],                             descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Walk-in Access", de: "Vor-Ort-Nutzung"],                           descr:allDescr, type: OT.Rdv, cat:'Permissions'],
                [name: [en: "Completeness of content clause", de: "Klausel zur Vollständigkeit der Inhalte"], descr:allDescr, type: OT.Rdv, cat:'Existence'],
                [name: [en: "Concurrency with print version", de: "Gleichzeitigkeit mit Druckversion"], descr:allDescr, type: OT.Rdv, cat:'Existence'],
                [name: [en: "User information confidentiality", de: "Vertraulichkeit der Nutzerdaten"], descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Clickwrap modification", de: "Clickthrough"],                        descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Indemnification by licensor", de: "Entschädigung durch den Lizenzgeber"], descr:allDescr, type: OT.Rdv, cat:'Indemnification'],
                [name: [en: "Indemnification by licensor indicator", de: "Entschädigung durch den Lizenzgeber Anzeiger"], descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Confidentiality of agreement", de: "Vertraulichkeit der Vereinbarung"], descr:allDescr, type: OT.Rdv, cat:'Confidentiality'],
                [name: [en: "Governing law", de: "Anzuwendendes Recht"],                        descr:allDescr, type: OT.String],
                [name: [en: "Governing jurisdiction", de: "Gerichtsstand"],                     descr:allDescr, type: OT.String],
                [name: [en: "Applicable copyright law", de: "Maßgebliches Urheberrechtsgesetz"], descr:allDescr, type: OT.String],
                [name: [en: "Cure period for breach", de: "Zeitraum der Behebung bei Vertragsbruch"], descr:allDescr, type: OT.String],
                [name: [en: "Content warranty", de: "Gewährleistung über den Inhalt"],          descr:allDescr, type: OT.String],
                [name: [en: "Performance warranty", de: "Gewährleistung einer Systemleistung/Performanz"], descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Uptime guarantee", de: "Gewährleistung einer verfügbaren Betriebszeit"], descr:allDescr, type: OT.String],
                [name: [en: "Maintenance window", de: "Wartungsfenster"],                       descr:allDescr, type: OT.String],
                [name: [en: "Licensee termination right", de: "Kündigungsrecht des Lizenznehmers"], descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Licensee termination condition", de: "Kündigungsrecht des Lizenznehmers Voraussetzung"], descr:allDescr, type: OT.Rdv, cat:'Termination Condition'],
                [name: [en: "Licensee termination notice period", de: "Kündigungsfrist des Lizenznehmers"], descr:allDescr, type: OT.String],
                [name: [en: "Licensor termination right", de: "Kündigungsrecht des Lizenzgebers"], descr:allDescr, type: OT.Rdv, cat:'YNO'],
                [name: [en: "Licensor termination condition", de: "Kündigungsrecht des Lizenzgebers Voraussetzung"], descr:allDescr, type: OT.Rdv, cat:'Termination Condition'],
                [name: [en: "Licensor termination notice period", de: "Kündigungsfrist des Lizenzgebers"], descr:allDescr, type: OT.String],
                [name: [en: "Termination requirement note", de: "Kündigungsrecht besondere Anforderung"], descr:allDescr, type: OT.String]

        ]
        createPropertyDefinitionsWithI10nTranslations(requiredProps)

        //def allOADescr = [en: PropertyDefinition.LIC_OA_PROP, de: PropertyDefinition.LIC_OA_PROP]
        def allOADescr = [en: PropertyDefinition.LIC_PROP, de: PropertyDefinition.LIC_PROP]

        def requiredOAProps = [
                [name: [en: "Open Access", de: "Open Access"],                                                              descr: allOADescr, type: OT.Rdv, cat: 'YN'],
                [name: [en: "Type", de: "Variante"],                                                                        descr: allOADescr, type: OT.Rdv, cat: 'License.OA.Type'],
                [name: [en: "Electronically Archivable Version", de: "Archivierbare Version"],                              descr: allOADescr, type: OT.Rdv, cat: 'License.OA.eArcVersion'],
                [name: [en: "Embargo Period", de: "Embargo"],                                                               descr: allOADescr, type: OT.Int],
                [name: [en: "Receiving Modalities", de: "Bezugsmodalitäten"],                                               descr: allOADescr, type: OT.Rdv, cat: 'License.OA.ReceivingModalities', multiple:true],
                [name: [en: "Authority", de: "Autorität"],                                                                  descr: allOADescr, type: OT.Rdv, cat:'Authority'],
                [name: [en: "Repository", de: "Repositorium"],                                                              descr: allOADescr, type: OT.Rdv, cat: 'License.OA.Repository', multiple:true],
                [name: [en: "APC Discount", de: "Sonderkonditionen für Autoren"],                                           descr: allOADescr, type: OT.String],
                [name: [en: "Vouchers Free OA Articles", de: "Vouchers"],                                                   descr: allOADescr, type: OT.String],
                [name: [en: "Corresponding Author Identification", de: "Autorenidentifikation"],                            descr: allOADescr, type: OT.Rdv, cat: 'License.OA.CorrespondingAuthorIdentification', multiple:true],
                [name: [en: "Branding", de: "Branding"],                                                                    descr: allOADescr, type: OT.String],
                [name: [en: "Funder", de: "Funder"],                                                                        descr: allOADescr, type: OT.String],
                [name: [en: "License to Publish", de: "Publikationslizenz"],                                                descr: allOADescr, type: OT.Rdv, cat: 'License.OA.LicenseToPublish', multiple:true],
                [name: [en: "Offsetting", de: "Offsetting Berechnungsmodell"],                                              descr: allOADescr, type: OT.String],
                [name: [en: "Publishing Fee", de: "Publishing Fee"],                                                        descr: allOADescr, type: OT.String],
                [name: [en: "Reading Fee", de: "Reading Fee"],                                                              descr: allOADescr, type: OT.String],
                [name: [en: "OA First Date", de: "OA Startdatum"],                                                          descr: allOADescr, type: OT.Date],
                [name: [en: "OA Last Date", de: "OA Enddatum"],                                                             descr: allOADescr, type: OT.Date],
                [name: [en: "OA Note", de: "OA Bemerkung"],                                                                 descr: allOADescr, type: OT.String]
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredOAProps)

        //def allArcDescr = [en: PropertyDefinition.LIC_ARC_PROP, de: PropertyDefinition.LIC_ARC_PROP]
        def allArcDescr = [en: PropertyDefinition.LIC_PROP, de: PropertyDefinition.LIC_PROP]

        def requiredARCProps = [
                [name: [en: "Post Cancellation Online Access", de: "Zugriffsrechte: Dauerhaft"],                            descr: allArcDescr, type: OT.Rdv, cat: 'YNO'],
                [name: [en: "Continuing Access: Payment Note", de: "Zugriffsrechte: Kosten"],                               descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.PaymentNote'],
                [name: [en: "Continuing Access: Restrictions", de: "Zugriffsrechte: Einschränkungen"],                      descr: allArcDescr, type: OT.Rdv, cat: 'YNO'],
                [name: [en: "Continuing Access: Title Transfer", de: "Zugriffsrechte: Titeltransfer"],                      descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.TitleTransferRegulation'],
                [name: [en: "Archival Copy: Permission", de: "Archivkopie: Recht"],                                         descr: allArcDescr, type: OT.Rdv, cat: 'YNO'],
                [name: [en: "Archival Copy Content", de: "Archivkopie Form"],                                               descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.ArchivalCopyContent', multiple:true],
                [name: [en: "Archival Copy: Cost", de: "Archivkopie: Kosten"],                                              descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.ArchivalCopyCost'],
                [name: [en: "Archival Copy: Time", de: "Archivkopie: Zeitpunkt"],                                           descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.ArchivalCopyTime'],
                [name: [en: "Hosting: Permission", de: "Hostingrecht"],                                                     descr: allArcDescr, type: OT.Rdv, cat: 'YNO'],
                [name: [en: "Hosting: Obligation", de: "Hostingpflicht"],                                                   descr: allArcDescr, type: OT.Rdv, cat: 'YN'],
                [name: [en: "Hosting Time", de: "Hostingrecht Zeitpunkt"],                                                  descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.HostingTime', multiple:true],
                [name: [en: "Hosting: Additonal Agreement Necessary", de: "Hostingrecht: Zusatzvereinbarung notwendig"],    descr: allArcDescr, type: OT.Rdv, cat: 'YN'],
                [name: [en: "Hosting: Authorized", de: "Hostingrecht: Berechtigte"],                                        descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.Authorized', multiple:true],
                [name: [en: "Hosting: Restriction", de: "Hostingrecht: Einschränkung"],                                     descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.HostingRestriction', multiple:true],
                [name: [en: "Hosting: Solution", de: "Hostingrecht: Lösung"],                                               descr: allArcDescr, type: OT.Rdv, cat: 'License.Arc.HostingSolution', multiple:true],
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredARCProps)
    }

    def createSubscriptionProperties() {

        def allDescr = [en: PropertyDefinition.SUB_PROP, de: PropertyDefinition.SUB_PROP]

        def requiredProps = [
                [name: [en: "GASCO Entry", de: "GASCO-Eintrag"],                    descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "EZB Gelbschaltung", de: "EZB Gelbschaltung"],          descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Metadata Delivery", de: "Metadatenlieferung"],         descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Metadata Source", de: "Metadaten Quelle"],             descr:allDescr, type: OT.String],
                [name: [en: "Preisvorteil durch weitere Produktteilnahme", de: "Preisvorteil durch weitere Produktteilnahme"], descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Produktabhängigkeit", de: "Produktabhängigkeit"],      descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Rabattstaffel", de: "Rabattstaffel"],                  descr:allDescr, type: OT.String],
                [name: [en: "Bundesweit offen", de: "Bundesweit offen"],            descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Rechnungsstellung durch Anbieter", de: "Rechnungsstellung durch Anbieter"],    descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Mengenrabatt Stichtag", de: "Mengenrabatt Stichtag"],  descr:allDescr, type: OT.Date, cat:'YN'],
                [name: [en: "Testzeitraum", de: "Testzeitraum"],                    descr:allDescr, type: OT.String],
                [name: [en: "Unterjähriger Einstieg", de: "Unterjähriger Einstieg"],descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Neueinsteigerrabatt", de: "Neueinsteigerrabatt"],      descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Simuser", de: "Simuser"],                              descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Simuser Zahl", de: "Simuser Zahl"],                    descr:allDescr, type: OT.String],
                [name: [en: "Rechnungszeitpunkt", de: "Rechnungszeitpunkt"],        descr:allDescr, type: OT.String],
                [name: [en: "Zahlungsziel", de: "Zahlungsziel"],                    descr:allDescr, type: OT.String],
                [name: [en: "Preis gerundet", de: "Preis gerundet"],                descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Teilzahlung", de: "Teilzahlung"],                      descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Statistik", de: "Statistik"],                          descr:allDescr, type: OT.String],
                [name: [en: "Statistikzugang", de: "Statistikzugang"],              descr:allDescr, type: OT.String],
                [name: [en: "Simuser", de: "Simuser"],                              descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "KBART", de: "KBART"],                                  descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "reverse charge", de: "reverse charge"],                descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Private Einrichtungen", de: "Private Einrichtungen"],  descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Mehrjahreslaufzeit", de: "Mehrjahreslaufzeit"],        descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Rabatt", de: "Rabatt"],                                descr:allDescr, type: OT.String],
                [name: [en: "Rabatt Zählung", de: "Rabatt Zählung"],                descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Kündigungsfrist", de: "Kündigungsfrist"],              descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Zusätzliche Software erforderlich?", de: "Zusätzliche Software erforderlich?"],    descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Preissteigerung", de: "Preissteigerung"],              descr:allDescr, type: OT.String],
                [name: [en: "Preis abhängig von", de: "Preis abhängig von"],        descr:allDescr, type: OT.String],
                [name: [en: "Abbestellquote", de: "Abbestellquote"],                descr:allDescr, type: OT.String],
                [name: [en: "Bestellnummer im Erwerbungssystem", de: "Bestellnummer im Erwerbungssystem"],      descr:allDescr, type: OT.String],
                [name: [en: "Zugangskennungen für Nutzer (pro Zeitschrift)", de: "Zugangskennungen für Nutzer (pro Zeitschrift)"], descr:allDescr, type: OT.String],
                [name: [en: "Subscriptionsnummer vom Verlag", de: "Subscriptionsnummer vom Verlag"],            descr:allDescr, type: OT.String],
                [name: [en: "DBIS-Eintrag", de: "DBIS-Eintrag"],                    descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Abbestellgrund", de: "Abbestellgrund"],                descr:allDescr, type: OT.String],
                [name: [en: "Hosting-Gebühr", de: "Hosting-Gebühr"],                descr:allDescr, type: OT.String],
                [name: [en: "Pick&Choose-Paket", de: "Pick&Choose-Paket"],          descr:allDescr, type: OT.String],
                [name: [en: "PDA/EBS-Programm", de: "PDA/EBS-Programm"],            descr:allDescr, type: OT.String],
                [name: [en: "Produktsigel beantragt", de: "Produktsigel beantragt"],                    descr:allDescr, type: OT.String],
                [name: [en: "Fachstatistik / Klassifikation", de: "Fachstatistik / Klassifikation"],    descr:allDescr, type: OT.Int],
                [name: [en: "Archivzugriff", de: "Archivzugriff"],                                      descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "Eingeschränkter Benutzerkreis", de: "Eingeschränkter Benutzerkreis"],      descr:allDescr, type: OT.String],
                [name: [en: "SFX-Eintrag", de: "SFX-Eintrag"],                      descr:allDescr, type: OT.Rdv, cat:'YN'],
                [name: [en: "GASCO-Anzeigename", de: "GASCO-Anzeigename"],          descr:allDescr, type: OT.String],
                [name: [en: "GASCO-Verhandlername", de: "GASCO-Verhandlername"],    descr:allDescr, type: OT.String]
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredProps)
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

            prop.type  = default_prop.type
            prop.descr = default_prop.descr['en']
            prop.softData = false
            prop.save(failOnError: true)

            I10nTranslation.createOrUpdateI10n(prop, 'name', default_prop.name)
            I10nTranslation.createOrUpdateI10n(prop, 'descr', default_prop.descr)
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

        // refdata categories

        RefdataCategory.loc('YN',                   	                    [en: 'Yes/No', de: 'Ja/Nein'], BOOTSTRAP)
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
        RefdataCategory.loc('Country',              	                    [en: 'Country', de: 'Land'], BOOTSTRAP)
        RefdataCategory.loc('FactType',             	                    [en: 'FactType', de: 'FactType'], BOOTSTRAP)
        RefdataCategory.loc('Federal State',        	                    [en: 'Federal State', de: 'Bundesland'], BOOTSTRAP)
        RefdataCategory.loc('Funder Type',          	                    [en: 'Funder Type', de: 'Trägerschaft'], BOOTSTRAP)
        RefdataCategory.loc('Gender',               	                    [en: 'Gender', de: 'Geschlecht'], BOOTSTRAP)
        RefdataCategory.loc('OrgSector',            	                    [en: 'OrgSector', de: 'Bereich'], BOOTSTRAP)
        RefdataCategory.loc('Library Network',      	                    [en: 'Library Network', de: 'Verbundzugehörigkeit'], BOOTSTRAP)
        RefdataCategory.loc('Library Type',         	                    [en: 'Library Type', de: 'Bibliothekstyp'], BOOTSTRAP)
        RefdataCategory.loc('OrgType',              	                    [en: 'Organisation Type', de: 'Organisationstyp'], BOOTSTRAP)
        RefdataCategory.loc('OrgRoleType',              	                    [en: 'Organisation Type', de: 'Organisationstyp'], BOOTSTRAP)
        RefdataCategory.loc('Person Function',      	                    [en: 'Person Function', de: 'Funktion'], BOOTSTRAP)
        RefdataCategory.loc('Person Contact Type',  	                    [en: 'Person: Contact Type', de: 'Kontaktart'], BOOTSTRAP)
        RefdataCategory.loc('Person Position',      	                    [en: 'Person Position', de: 'Position'], BOOTSTRAP)
        RefdataCategory.loc('Person Responsibility',	                    [en: 'Person Responsibility', de: 'Verantwortlich'], BOOTSTRAP)
        RefdataCategory.loc('Subscription Form',          	                [en: 'Subscription Form', de: 'Lizenzform'], BOOTSTRAP)
        RefdataCategory.loc('Subscription Resource',          	            [en: 'Resource type', de: 'Ressourcentyp'], BOOTSTRAP)
        RefdataCategory.loc('Subscription Status',          	            [en: 'Subscription Status', de: 'Lizenzstatus'], BOOTSTRAP)
        RefdataCategory.loc('Task Priority',                	            [en: 'Task Priority', de: 'Aufgabenpriorität'], BOOTSTRAP)
        RefdataCategory.loc('Task Status',          	                    [en: 'Task Status', de: 'Aufgabenstatus'], BOOTSTRAP)
        RefdataCategory.loc('Ticket.Category',          	                  [en: 'Ticket Category', de: 'Kategorie'], BOOTSTRAP)
        RefdataCategory.loc('Ticket.Status',          	                      [en: 'Ticket Status', de: 'Ticketstatus'], BOOTSTRAP)
        RefdataCategory.loc('License.OA.ReceivingModalities',               [en: 'Receiving Modalities', de: 'Bezugsmodalitäten'], BOOTSTRAP)
        RefdataCategory.loc('License.OA.Repository',                        [en: 'Repository', de: 'Repositorium'], BOOTSTRAP)
        RefdataCategory.loc('License.OA.CorrespondingAuthorIdentification', [en: 'Corresponding Author Identification', de: 'Autorenindentifikation'], BOOTSTRAP)
        RefdataCategory.loc('License.OA.LicenseToPublish',                  [en: 'License to Publish', de: 'Publikationslizenz'], BOOTSTRAP)
        RefdataCategory.loc('License.Arc.PaymentNote',                      [en: 'Archive Payment Note', de: 'Zugriffsrechte Kosten'], BOOTSTRAP)
		RefdataCategory.loc('License.Arc.TitletransferRegulation',          [en: 'Titletransfer Regulation', de: 'Titeltransfer Regeln'], BOOTSTRAP)
		RefdataCategory.loc('License.Arc.ArchivalCopyCost',                 [en: 'Archival Copy Cost', de: 'Archivkopie Kosten'], BOOTSTRAP)
		RefdataCategory.loc('License.Arc.ArchivalCopyTime',                 [en: 'Archival Copy Time', de: 'Archivkopie Zeitpunkt'], BOOTSTRAP)
        RefdataCategory.loc('License.Arc.ArchivalCopyContent',              [en: 'Archival Copy Content', de: 'Archivkopie Form'], BOOTSTRAP)
        RefdataCategory.loc('License.Arc.HostingTime',                      [en: 'Hosting Time', de: 'Hostingrecht Zeitpunkt'], BOOTSTRAP)
        RefdataCategory.loc('License.Arc.Authorized',                       [en: 'Hosting Authorized', de: 'Hostingrecht Berechtigte'], BOOTSTRAP)
        RefdataCategory.loc('License.Arc.HostingRestriction',               [en: 'Hosting Restriction', de: 'Hostingrecht Einschränkung'], BOOTSTRAP)
        RefdataCategory.loc('License.Arc.HostingSolution',                  [en: 'Hosting Solution', de: 'Hostingrecht Lösung'], BOOTSTRAP)
        RefdataCategory.loc('Package Status',                               [en: 'Package Status', de: 'Paketstatus'], BOOTSTRAP)
        RefdataCategory.loc('Number Type',                                  [en: 'Number Type', de: 'Zahlen-Typ'], BOOTSTRAP)
        RefdataCategory.loc('User.Settings.Dashboard.Tab',                  [en: 'Dashboard Tab', de: 'Dashbord Tab'], BOOTSTRAP)
        // refdata values

        RefdataValue.loc('YN',   [en: 'Yes', de: 'Ja'], BOOTSTRAP)
        RefdataValue.loc('YN',   [en: 'No', de: 'Nein'], BOOTSTRAP)

        RefdataValue.loc('YNO',  [en: 'Yes', de: 'Ja'], BOOTSTRAP)
        RefdataValue.loc('YNO',  [en: 'No', de: 'Nein'], BOOTSTRAP)
        RefdataValue.loc('YNO',  [en: 'Not applicable', de: 'Nicht zutreffend'], BOOTSTRAP)
        RefdataValue.loc('YNO',  [en: 'Planed', de: 'Geplant'], BOOTSTRAP)
        RefdataValue.loc('YNO',  [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)
        RefdataValue.loc('YNO',  [en: 'Other', de: 'Andere'], BOOTSTRAP)

        RefdataValue.loc('Permissions',  [en: 'Permitted (explicit)', de: 'Ausdrücklich erlaubt'], BOOTSTRAP)
        RefdataValue.loc('Permissions',  [en: 'Permitted (interpreted)', de: 'Vermutlich erlaubt'], BOOTSTRAP)
        RefdataValue.loc('Permissions',  [en: 'Prohibited (explicit)', de: 'Ausdrücklich verboten'], BOOTSTRAP)
        RefdataValue.loc('Permissions',  [en: 'Prohibited (interpreted)', de: 'Vermutlich verboten'], BOOTSTRAP)
        RefdataValue.loc('Permissions',  [en: 'Silent', de: 'Stillschweigend'], BOOTSTRAP)
        RefdataValue.loc('Permissions',  [en: 'Not applicable', de: 'Nicht zutreffend'], BOOTSTRAP)
        RefdataValue.loc('Permissions',  [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)

        RefdataValue.loc('Existence',   [en: 'Existent', de: 'Bestehend'], BOOTSTRAP)
        RefdataValue.loc('Existence',   [en: 'Nonexistend', de: 'Fehlend'], BOOTSTRAP)

        RefdataValue.loc('Indemnification',  [en: 'General', de: 'Generell'], BOOTSTRAP)
        RefdataValue.loc('Indemnification',  [en: 'Intellectual Property Only', de: 'Nur geistiges Eigentum'], BOOTSTRAP)
        RefdataValue.loc('Indemnification',  [en: 'Other', de: 'Andere'], BOOTSTRAP)
        RefdataValue.loc('Indemnification',  [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)

        RefdataValue.loc('Confidentiality',  [en: 'All', de: 'Alles'], BOOTSTRAP)
        RefdataValue.loc('Confidentiality',  [en: 'All but user terms', de: 'Alles außer Nutzungsbedingungen'], BOOTSTRAP)
        RefdataValue.loc('Confidentiality',  [en: 'Financial only', de: 'Nur Finanzangelegenheiten'], BOOTSTRAP)
        RefdataValue.loc('Confidentiality',  [en: 'No', de: 'Nein'], BOOTSTRAP)
        RefdataValue.loc('Confidentiality',  [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)

        RefdataValue.loc('Termination Condition',  [en: 'At will', de: 'Nach Belieben'], BOOTSTRAP)
        RefdataValue.loc('Termination Condition',  [en: 'Breach by Licensor/Licensee', de: 'Wegen Verstoß des Vertragspartners'], BOOTSTRAP)
        RefdataValue.loc('Termination Condition',  [en: 'Other', de: 'Andere Gründe'], BOOTSTRAP)
        RefdataValue.loc('Termination Condition',  [en: 'Unknown', de: 'Unbekannt'], BOOTSTRAP)

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

        RefdataValue.loc('Country',   [en: 'Germany', de: 'Deutschland'], BOOTSTRAP)
        RefdataValue.loc('Country',   [en: 'Switzerland', de: 'Schweiz'], BOOTSTRAP)
        RefdataValue.loc('Country',   [en: 'Austria', de: 'Österreich'], BOOTSTRAP)
        RefdataValue.loc('Country',   [en: 'France', de: 'Frankreich'], BOOTSTRAP)
        RefdataValue.loc('Country',   [en: 'Great Britain', de: 'Großbritannien'], BOOTSTRAP)
        RefdataValue.loc('Country',   [en: 'United States of America', de: 'Vereinigte Staaten von Amerika'], BOOTSTRAP)
        RefdataValue.loc('Country',   [en: 'Belgium', de: 'Belgien'], BOOTSTRAP)
        RefdataValue.loc('Country',   [en: 'Italy', de: 'Italien'], BOOTSTRAP)
        RefdataValue.loc('Country',   [en: 'Netherlands', de: 'Niederlande'], BOOTSTRAP)
        RefdataValue.loc('Country',   [en: 'Italy', de: 'Italien'], BOOTSTRAP)

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

        RefdataValue.loc('OrgSector',    [en: 'Higher Education', de: 'Akademisch'], BOOTSTRAP)
        RefdataValue.loc('OrgSector',    [key: 'Publisher', en: 'Commercial', de: 'Kommerziell'], BOOTSTRAP)

        RefdataValue.loc('OrgType',      [en: 'Consortium', de: 'Konsortium'], BOOTSTRAP)
        RefdataValue.loc('OrgType',      [en: 'Institution', de: 'Einrichtung'], BOOTSTRAP)
        RefdataValue.loc('OrgType',      [en: 'Publisher', de: 'Verlag'], BOOTSTRAP)
        RefdataValue.loc('OrgType',      [en: 'Provider', de: 'Anbieter'], BOOTSTRAP)
        RefdataValue.loc('OrgType',      [en: 'Other', de: 'Andere'], BOOTSTRAP)

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
        RefdataValue.loc('Person Function',     [en: 'Bestandsaufbau', de: 'Bestandsaufbau'], BOOTSTRAP)
        RefdataValue.loc('Person Function',     [en: 'Direktion', de: 'Direktion'], BOOTSTRAP)
        RefdataValue.loc('Person Function',     [en: 'Direktionsassistenz', de: 'Direktionsassistenz'], BOOTSTRAP)
        RefdataValue.loc('Person Function',     [en: 'Erwerbungsabteilung', de: 'Erwerbungsabteilung'], BOOTSTRAP)
        RefdataValue.loc('Person Function',     [en: 'Erwerbungsleitung', de: 'Erwerbungsleitung'], BOOTSTRAP)
        RefdataValue.loc('Person Function',     [en: 'Medienbearbeitung', de: 'Medienbearbeitung'], BOOTSTRAP)
        RefdataValue.loc('Person Function',     [en: 'Zeitschriftenabteilung', de: 'Zeitschriftenabteilung'], BOOTSTRAP)
        RefdataValue.loc('Person Function',     [en: 'Fachreferat', de: 'Fachreferat'], BOOTSTRAP)
        RefdataValue.loc('Person Function',     [en: 'Bereichsbibliotheksleitung', de: 'Bereichsbibliotheksleitung'], BOOTSTRAP)

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
        RefdataValue.loc('Person Position',     [en: 'Technichal Support', de: 'Technischer Support'], BOOTSTRAP)

        RefdataValue.loc('Person Responsibility',    [en: 'Specific license editor', de: 'Vertragsbearbeiter'], BOOTSTRAP)
        RefdataValue.loc('Person Responsibility',    [en: 'Specific subscription editor', de: 'Lizenzkontakt'], BOOTSTRAP)
        RefdataValue.loc('Person Responsibility',    [en: 'Specific package editor', de: 'Paketbearbeiter'], BOOTSTRAP)
        RefdataValue.loc('Person Responsibility',    [en: 'Specific cluster editor', de: 'Gruppenkontakt'], BOOTSTRAP)
        RefdataValue.loc('Person Responsibility',    [en: 'Specific title editor', de: 'Titelbearbeiter'], BOOTSTRAP)

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

        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'By Author', de: 'Über Autor'], BOOTSTRAP)
        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'On Demand', de: 'Auf Nachfrage'], BOOTSTRAP)
        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'From Database', de: 'Aus Datenank'], BOOTSTRAP)
        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'Automatic Delivery', de: 'Automatische Lieferung'], BOOTSTRAP)
        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'Link by Publisher', de: 'Verlinkung durch Verlag'], BOOTSTRAP)
        
        RefdataValue.loc('License.OA.Repository',                [en: 'Own Choice', de: 'Nach Wahl'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Repository',                [en: 'Publishers', de: 'Verlagseigenes'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Repository',                [en: 'Subject specific', de: 'Fachspezifisches'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Repository',                [en: 'Website of Author', de: 'Website des Autors'], BOOTSTRAP)
        RefdataValue.loc('License.OA.Repository',                [en: 'Institutional', de: 'Institutionelles'], BOOTSTRAP)
        
        RefdataValue.loc('License.OA.CorrespondingAuthorIdentification',    [en: 'IP Range', de: 'Über IP-Bereich'], BOOTSTRAP)
        RefdataValue.loc('License.OA.CorrespondingAuthorIdentification',    [en: 'Email Domain', de: 'E-Mail-Domäne'], BOOTSTRAP)
        RefdataValue.loc('License.OA.CorrespondingAuthorIdentification',    [en: 'Research Institute', de: 'Über Institut'], BOOTSTRAP)
        RefdataValue.loc('License.OA.CorrespondingAuthorIdentification',    [en: 'ORCID', de: 'ORCID'], BOOTSTRAP)
        
        RefdataValue.loc('License.OA.LicenseToPublish',          [en: 'CC-BY', de: 'CC-BY'], BOOTSTRAP)
        RefdataValue.loc('License.OA.LicenseToPublish',          [en: 'CC-BY-NC', de: 'CC-BY-NC'], BOOTSTRAP)
        RefdataValue.loc('License.OA.LicenseToPublish',          [en: 'CC-BY-NC-ND', de: 'CC-BY-NC-ND'], BOOTSTRAP)
        
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
        
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'Always', de: 'Immer'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'Exclusive', de: 'Ohne Anbieter'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'From Expiration On', de: 'Ab Vertragsende'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'Predefined time', de: 'Fester Zeitpunkt'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'Trigger Event', de: 'Mit Trigger-Event'], BOOTSTRAP)
        
        RefdataValue.loc('License.Arc.Authorized',      [en: 'Licensee', de: 'Lizenznehmer'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.Authorized',      [en: 'SSG-Library', de: 'SSG-Bibliothek'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.Authorized',      [en: 'Contractor', de: 'Vertragspartner'], BOOTSTRAP)
        RefdataValue.loc('License.Arc.Authorized',      [en: 'Contractor With Publisher\'s Assent', de: 'Vertragspartner nach Genehmigung durch Anbieter'], BOOTSTRAP)
        
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

        RefdataValue.loc('Number Type',      [en: 'Students', de: 'Studenten'], BOOTSTRAP)
        RefdataValue.loc('Number Type',      [en: 'Scientific staff', de: 'wissenschaftliches Personal'], BOOTSTRAP)
        RefdataValue.loc('Number Type',      [en: 'User', de: 'Nutzer'], BOOTSTRAP)
        RefdataValue.loc('Number Type',      [en: 'Population', de: 'Einwohner'], BOOTSTRAP)

        RefdataValue.loc('User.Settings.Dashboard.Tab',     [en: 'Changes', de: 'Änderungen'], BOOTSTRAP)
        RefdataValue.loc('User.Settings.Dashboard.Tab',     [en: 'Announcements', de: 'Ankündigungen'], BOOTSTRAP)
        RefdataValue.loc('User.Settings.Dashboard.Tab',     [en: 'Tasks', de: 'Aufgaben'], BOOTSTRAP)
        RefdataValue.loc('User.Settings.Dashboard.Tab',     [en: 'Due Dates', de: 'Fällige Termine'], BOOTSTRAP)

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
        RefdataValue.loc('License.OA.Type', [en: 'Red Open Access', de: 'Red Open-Access'], BOOTSTRAP)

        RefdataCategory.loc('License.OA.eArcVersion',
                [en: 'License.OA.eArcVersion', de: 'License.OA.eArcVersion'], BOOTSTRAP)

        RefdataValue.loc('License.OA.eArcVersion', [en: 'Accepted Author'], BOOTSTRAP)
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Manuscript (AAM)'], BOOTSTRAP)
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Publisher-PDF', de: 'Verlags-PDF'], BOOTSTRAP)
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Postprint', de: 'Postprint'], BOOTSTRAP)
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Preprint', de: 'Preprint'], BOOTSTRAP)
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Preprint with ePrint URL'], BOOTSTRAP)

        RefdataCategory.loc(RefdataCategory.LIC_STATUS,
                [en: 'License Status', de: 'Lizenzstatus'], BOOTSTRAP)

        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Current', de: 'Aktuell'], BOOTSTRAP)
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

    def setESGOKB() {
         ElasticsearchSource.findByIdentifier("gokb") ?: new ElasticsearchSource(name: 'GOKB ES', identifier: 'gokb', cluster: 'elasticsearch', index: 'gokb', host: '127.0.0.1', gokb_es: true)
    }
}