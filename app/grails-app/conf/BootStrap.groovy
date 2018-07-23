import com.k_int.kbplus.*

import com.k_int.kbplus.auth.*
import com.k_int.properties.PropertyDefinition
import de.laser.domain.I10nTranslation
import grails.plugin.springsecurity.SecurityFilterPosition // 2.0
import grails.plugin.springsecurity.SpringSecurityUtils // 2.0

class BootStrap {

    def grailsApplication
    def dataloadService
    // def docstoreService

    def init = { servletContext ->

        log.info("SystemId: ${grailsApplication.config.laserSystemId}")

        if (grailsApplication.config.laserSystemId != null) {
            def system_object = SystemObject.findBySysId(grailsApplication.config.laserSystemId) ?: new SystemObject(sysId: grailsApplication.config.laserSystemId).save(flush: true)
        }

        def evt_startup   = new EventLog(event: 'kbplus.startup', message: 'Normal startup', tstp: new Date(System.currentTimeMillis())).save(flush: true)
        def so_filetype   = DataloadFileType.findByName('Subscription Offered File') ?: new DataloadFileType(name: 'Subscription Offered File')
        def plat_filetype = DataloadFileType.findByName('Platforms File') ?: new DataloadFileType(name: 'Platforms File')

        log.debug("setupRefdata ..")
        setupRefdata()

        log.debug("setupRolesAndPermissions ..")
        setupRolesAndPermissions()

        // Transforms types and formats Refdata

        RefdataCategory.loc('Transform Format', [en: 'Transform Format', de: 'Transform Format'])
        RefdataCategory.loc('Transform Type',   [en: 'Transform Type', de: 'Transform Type'])

        // !!! HAS TO BE BEFORE the script adding the Transformers as it is used by those tables !!!

        RefdataValue.loc('Transform Format', [en: 'json'])
        RefdataValue.loc('Transform Format', [en: 'xml'])
        RefdataValue.loc('Transform Format', [en: 'url'])

        RefdataValue.loc('Transform Type', [en: 'subscription'])
        RefdataValue.loc('Transform Type', [en: 'license'])
        RefdataValue.loc('Transform Type', [en: 'title'])
        RefdataValue.loc('Transform Type', [en: 'package'])

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

        //log.debug("setupRefdataFromCode ..")
        //setupRefdataFromCode()

        log.debug("setupCurrencies ..")
        setupCurrencies()

        log.debug("setupContentItems ..")
        setupContentItems()

        // if ( grailsApplication.config.doDocstoreMigration == true ) {
        //   docstoreService.migrateToDb()
        // }

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

        log.debug("createPrivateProperties ..")
        createPrivateProperties()

        log.debug("initializeDefaultSettings ..")
        initializeDefaultSettings()

        log.debug("setIdentifierNamespace ..")
        setIdentifierNamespace()

        log.debug("setESGOKB ..")
        setESGOKB()

        log.debug("Init completed ..")
    }

    def destroy = {
    }

    def setupRolesAndPermissions = {

        // Permissions

        def edit_permission = Perm.findByCode('edit') ?: new Perm(code: 'edit').save(failOnError: true)
        def view_permission = Perm.findByCode('view') ?: new Perm(code: 'view').save(failOnError: true)

        // Roles

        def or_lc_role            = RefdataValue.loc('Organisational Role', [en: 'Licensing Consortium', de:'Konsortium'])
        def or_licensee_role      = RefdataValue.loc('Organisational Role', [en: 'Licensee', de: 'Lizenznehmer'])
        def or_licensee_cons_role = RefdataValue.loc('Organisational Role', [key: 'Licensee_Consortial', en: 'Consortial licensee', de: 'Konsortiallizenznehmer'])

        OrgPermShare.assertPermShare(view_permission, or_lc_role)
        OrgPermShare.assertPermShare(edit_permission, or_lc_role)

        OrgPermShare.assertPermShare(view_permission, or_licensee_role)
        OrgPermShare.assertPermShare(edit_permission, or_licensee_role)

        OrgPermShare.assertPermShare(view_permission, or_licensee_cons_role)

        def or_sc_role          = RefdataValue.loc('Organisational Role', [en: 'Subscription Consortia', de:'Konsortium'])
        def or_subscr_role      = RefdataValue.loc('Organisational Role', [en: 'Subscriber', de: 'Teilnehmer'])
        def or_subscr_cons_role = RefdataValue.loc('Organisational Role', [key: 'Subscriber_Consortial', en: 'Consortial subscriber', de: 'Konsortialteilnehmer'])

        OrgPermShare.assertPermShare(view_permission, or_sc_role)
        OrgPermShare.assertPermShare(edit_permission, or_sc_role)

        OrgPermShare.assertPermShare(view_permission, or_subscr_role)
        OrgPermShare.assertPermShare(edit_permission, or_subscr_role)

        OrgPermShare.assertPermShare(view_permission, or_subscr_cons_role)

        def cl_owner_role       = RefdataValue.loc('Cluster Role',   [en: 'Cluster Owner'])
        def cl_member_role      = RefdataValue.loc('Cluster Role',   [en: 'Cluster Member'])

        def cons_combo          = RefdataValue.loc('Combo Type',     [en: 'Consortium', de: 'Konsortium'])

        OrgPermShare.assertPermShare(view_permission, cl_owner_role)
        OrgPermShare.assertPermShare(edit_permission, cl_owner_role)

        OrgPermShare.assertPermShare(view_permission, cl_member_role)
        OrgPermShare.assertPermShare(edit_permission, cl_member_role)

        OrgPermShare.assertPermShare(view_permission, cons_combo)

        // Global System Roles

        def yodaRole    = Role.findByAuthority('ROLE_YODA')        ?: new Role(authority: 'ROLE_YODA', roleType: 'transcendent').save(failOnError: true)
        def adminRole   = Role.findByAuthority('ROLE_ADMIN')       ?: new Role(authority: 'ROLE_ADMIN', roleType: 'global').save(failOnError: true)
        def dmRole      = Role.findByAuthority('ROLE_DATAMANAGER') ?: new Role(authority: 'ROLE_DATAMANAGER', roleType: 'global').save(failOnError: true)
        def globalDataRole      = Role.findByAuthority('ROLE_GLOBAL_DATA') ?: new Role(authority: 'ROLE_GLOBAL_DATA', roleType: 'global').save(failOnError: true)
        def userRole    = Role.findByAuthority('ROLE_USER')        ?: new Role(authority: 'ROLE_USER', roleType: 'global').save(failOnError: true)
        def apiRole     = Role.findByAuthority('ROLE_API')         ?: new Role(authority: 'ROLE_API', roleType: 'global').save(failOnError: true)

        def apiReaderRole      = Role.findByAuthority('ROLE_API_READER')      ?: new Role(authority: 'ROLE_API_READER', roleType: 'global').save(failOnError: true)
        def apiWriterRole      = Role.findByAuthority('ROLE_API_WRITER')      ?: new Role(authority: 'ROLE_API_WRITER', roleType: 'global').save(failOnError: true)
        def apiDataManagerRole = Role.findByAuthority('ROLE_API_DATAMANAGER') ?: new Role(authority: 'ROLE_API_DATAMANAGER', roleType: 'global').save(failOnError: true)

        def packageEditorRole = Role.findByAuthority('ROLE_PACKAGE_EDITOR') ?: new Role(authority: 'ROLE_PACKAGE_EDITOR', roleType: 'global').save(failOnError: true)
        def orgEditorRole     = Role.findByAuthority('ROLE_ORG_EDITOR')     ?: new Role(authority: 'ROLE_ORG_EDITOR', roleType: 'global').save(failOnError: true)
        def orgComRole     = Role.findByAuthority('ROLE_ORG_COM_EDITOR')     ?: new Role(authority: 'ROLE_ORG_COM_EDITOR', roleType: 'global').save(failOnError: true)

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
                    descr:allDescr, type:String.toString(), val:"Jisc Collections Model Journals License 2015", note:"Default license used for comparison when viewing a single onix license."],
                [name: [en: "net.sf.jasperreports.export.csv.exclude.origin.keep.first.band.1"],
                    descr:allDescr, type:String.toString(), val:"columnHeader", note:"Only show 1 column header for csv"],
                [name: [en: "net.sf.jasperreports.export.xls.exclude.origin.keep.first.band.1"],
                    descr:allDescr, type:String.toString(), val:"columnHeader", note:"Only show 1 column header for xls"],
                [name: [en: "net.sf.jasperreports.export.xls.exclude.origin.band.1"],
                    descr:allDescr, type:String.toString(), val:"pageHeader", note:" Remove header/footer from csv/xls"],
                [name: [en: "net.sf.jasperreports.export.xls.exclude.origin.band.2"],
                    descr:allDescr, type:String.toString(), val:"pageFooter", note:" Remove header/footer from csv/xls"],
                [name: [en: "net.sf.jasperreports.export.csv.exclude.origin.band.1"],
                    descr:allDescr, type:String.toString(), val:"pageHeader", note: " Remove header/footer from csv/xls"],
                [name: [en: "net.sf.jasperreports.export.csv.exclude.origin.band.2"],
                    descr:allDescr, type:String.toString(), val:"pageFooter", note: " Remove header/footer from csv/xls"]
        ]

        requiredProps.each { prop ->
            def name = prop.name['en']
            def pd   = PropertyDefinition.findByName(name)

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
                [name: [en: "API Key", de: "API Key"],         descr:allDescr, type:String.toString()],
                [name: [en: "RequestorID", de: "RequestorID"], descr:allDescr, type:String.toString()],
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
                [name: [en: "Agreement Date", de: "Abschlussdatum"],                            descr:allDescr, type:Date.toString()],
                //[name: [en: "Authorized Users", de: "Autorisierte Nutzer"],                     descr:allDescr, type:String.toString()],
                [name: [en: "Alumni Access"],                                                   descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                //[name: [en: "Cancellation Allowance", de: "Außerordentliche Kündigung"],        descr:allDescr, type:String.toString()],
                [name: [en: "Change to licensed material", de: "Änderung am Vertragsgegenstand"], descr:allDescr, type:String.toString()],
                [name: [en: "Concurrent Access", de: "Concurrent Access"],                      descr:allDescr, type:RefdataValue.toString(), cat:'ConcurrentAccess'],
                [name: [en: "Concurrent Users", de: "Concurrent Users"],                        descr:allDescr, type:Integer.toString()],
                //[name: [en: "Correction Time", de: "Korrekturfrist bei Vertragsverletzungen"],  descr:allDescr, type:String.toString()],
                [name: [en: "Enterprise Access"],                                               descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                //[name: [en: "ILL - InterLibraryLoans", de: "Fernleihe"],                        descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                //[name: [en: "Include In Coursepacks", de: "Semesterapparat"],                   descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Include in VLE"],                                                  descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Invoicing", de: "Rechnungsstellung"],                              descr:allDescr, type:Date.toString()],
                [name: [en: "Metadata delivery"],                                               descr:allDescr, type:String.toString()],
                [name: [en: "Method of Authentication", de: "Authentifizierungsverfahren"],     descr:allDescr, type:String.toString()],
                [name: [en: "Multi Site Access"],                                               descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                //[name: [en: "Notice Period"],                                                   descr:allDescr, type:Date.toString()],
                [name: [en: "New Underwriter", de: "Aufnahme neuer Teilnehmer"],                descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Payment target", de: "Zahlungsziel"],                              descr:allDescr, type:Date.toString()],
                // [name: [en: "Place of jurisdiction", de: "Gerichtsstand"],                      descr:allDescr, type:String.toString()],
                [name: [en: "Partners Access"],                                                 descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Permitted Uses"],                                                  descr:allDescr, type:String.toString()],
                [name: [en: "Post Cancellation Access Entitlement"],                            descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                //[name: [en: "Remote Access", de: "Remote-Zugriff"],                             descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Regional Restriction", de: "Regionale Einschränkung"],             descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Service regulations", de: "Servicestandards"],                     descr:allDescr, type:String.toString()],
                [name: [en: "Signed"],                                                          descr:allDescr, type:RefdataValue.toString(), cat:'YN'],
                [name: [en: "Usage Statistics", de: "Lieferung von Statistiken"],               descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                //[name: [en: "Walk In Access", de: "Walk-In User"],                              descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Wifi Access", de: "WLAN-Zugriff"],                                 descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],

                // New Properties by FAK / Verde Review
                [name: [en: "General Terms note", de: "Allgemeine Bedingungen"],                    descr:allDescr, type:String.toString()],
                [name: [en: "User restriction note", de: "Benutzungsbeschränkungen"],               descr:allDescr, type:String.toString()],
                [name: [en: "Authorized user definition", de: "Definition für berechtigte Nutzer"], descr:allDescr, type:String.toString()],
                [name: [en: "Local authorized user defintion", de: "Lokale Definition für berechtigte Nutzer"],      descr:allDescr, type:String.toString()],
                [name: [en: "ILL print or fax", de: "Fernleihe per Papier oder Fax"],           descr:allDescr, type:RefdataValue.toString(), cat:'Permissions'],
                [name: [en: "ILL secure electronic transmission", de: "Fernleihe über sichere elektonische Übermittlung"], descr:allDescr, type:RefdataValue.toString(), cat:'Permissions'],
                [name: [en: "ILL electronic", de: "Fernleihe elektronisch"],                    descr:allDescr, type:RefdataValue.toString(), cat:'Permissions'],
                [name: [en: "ILL record keeping required", de: "Fernleihdatensatz muss gespeichert werden"], descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                //[name: [en: "ILL term note", de: "Fernleihbedingungen"],                        descr:allDescr, type:String.toString()],
                [name: [en: "Fair use clause indicator", de: "Hinweis auf Klausel über die 'faire Nutzung'"], descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "All rights reserved indicator", de: "Hinweis auf 'Alle Rechte vorbehalten'"], descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Data protection override", de: "Datenschutz aufgehoben"],          descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Citation requirement detail", de: "Details der Zitier-Regeln"],    descr:allDescr, type:String.toString()],
                [name: [en: "Digitial copy", de: "Digitalkopie"],                               descr:allDescr, type:RefdataValue.toString(), cat:'Permissions'],
                //[name: [en: "Digitial copy term note", de: "Digitalkopie Bedingungen"],         descr:allDescr, type:String.toString()],
                [name: [en: "Print copy", de: "Druckkopie"],                                    descr:allDescr, type:RefdataValue.toString(), cat:'Permissions'],
                //[name: [en: "Print copy term note", de: "Druckkopie Bedingungen"],              descr:allDescr, type:String.toString()],
                [name: [en: "Scholarly sharing", de: "Weitergabe im Rahmen der Lehre"],         descr:allDescr, type:RefdataValue.toString(), cat:'Permissions'],
                //[name: [en: "Scholarly sharing term note", de: "Weitergabe im Rahmen der Lehre Bedingungen"], descr:allDescr, type:String.toString()],
                [name: [en: "Distance Education", de: "Fernstudium"],                           descr:allDescr, type:RefdataValue.toString(), cat:'Permissions'],
                //[name: [en: "Distance Education term note", de: "Fernstudium Bedingungen"],     descr:allDescr, type:String.toString()],
                [name: [en: "Course reserve print", de: "Seminarapparat gedruckt"],             descr:allDescr, type:RefdataValue.toString(), cat:'Permissions'],
                [name: [en: "Course reserve electronic/cached", de: "Seminarapparat elektronisch"], descr:allDescr, type:RefdataValue.toString(), cat:'Permissions'],
                //[name: [en: "Course reserve term note", de: "Seminarapparat Bedingungen"],      descr:allDescr, type:String.toString()],
                [name: [en: "Electronic link", de: "Elektronischer Link"],                      descr:allDescr, type:RefdataValue.toString(), cat:'Permissions'],
                //[name: [en: "Electronic link term note", de: "Elektronischer Link Bedingungen"], descr:allDescr, type:String.toString()],
                [name: [en: "Course pack print", de: "Skripte gedruckt"],                       descr:allDescr, type:RefdataValue.toString(), cat:'Permissions'],
                [name: [en: "Course pack electronic", de: "Skripte elektronisch"],              descr:allDescr, type:RefdataValue.toString(), cat:'Permissions'],
                //[name: [en: "Course pack term note", de: "Skripte Bedingungen"],                descr:allDescr, type:String.toString()],
                [name: [en: "Remote Access", de: "Remote-Zugriff"],                             descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Walk-in Access", de: "Vor-Ort-Nutzung"],                           descr:allDescr, type:RefdataValue.toString(), cat:'Permissions'],
                //[name: [en: "Walk-in term note", de: "Vor-Ort-Nutzung Bedingungen"],            descr:allDescr, type:String.toString()],
                [name: [en: "Completeness of content clause", de: "Klausel zur Vollständigkeit der Inhalte"], descr:allDescr, type:RefdataValue.toString(), cat:'Existence'],
                [name: [en: "Concurrency with print version", de: "Gleichzeitigkeit mit Druckversion"], descr:allDescr, type:RefdataValue.toString(), cat:'Existence'],
                [name: [en: "User information confidentiality", de: "Vertraulichkeit der Nutzerdaten"], descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Clickwrap modification", de: "Clickthrough"],                        descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Indemnification by licensor", de: "Entschädigung durch den Lizenzgeber"], descr:allDescr, type:RefdataValue.toString(), cat:'Indemnification'],
                [name: [en: "Indemnification by licensor indicator", de: "Entschädigung durch den Lizenzgeber Anzeiger"], descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Confidentiality of agreement", de: "Vertraulichkeit der Vereinbarung"], descr:allDescr, type:RefdataValue.toString(), cat:'Confidentiality'],
                //[name: [en: "Confidentiality note", de: "Vertraulichkeit der Vereinbarung Anmerkung"], descr:allDescr, type:String.toString()],
                [name: [en: "Governing law", de: "Anzuwendendes Recht"],                        descr:allDescr, type:String.toString()],
                [name: [en: "Governing jurisdiction", de: "Gerichtsstand"],                     descr:allDescr, type:String.toString()],
                [name: [en: "Applicable copyright law", de: "Maßgebliches Urheberrechtsgesetz"], descr:allDescr, type:String.toString()],
                [name: [en: "Cure period for breach", de: "Zeitraum der Behebung bei Vertragsbruch"], descr:allDescr, type:String.toString()],
                [name: [en: "Content warranty", de: "Gewährleistung über den Inhalt"],          descr:allDescr, type:String.toString()],
                [name: [en: "Performance warranty", de: "Gewährleistung einer Systemleistung/Performanz"], descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Uptime guarantee", de: "Gewährleistung einer verfügbaren Betriebszeit"], descr:allDescr, type:String.toString()],
                [name: [en: "Maintenance window", de: "Wartungsfenster"],                       descr:allDescr, type:String.toString()],
                [name: [en: "Licensee termination right", de: "Kündigungsrecht des Lizenznehmers"], descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Licensee termination condition", de: "Kündigungsrecht des Lizenznehmers Voraussetzung"], descr:allDescr, type:RefdataValue.toString(), cat:'Termination Condition'],
                [name: [en: "Licensee termination notice period", de: "Kündigungsfrist des Lizenznehmers"], descr:allDescr, type:String.toString()],
                [name: [en: "Licensor termination right", de: "Kündigungsrecht des Lizenzgebers"], descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Licensor termination condition", de: "Kündigungsrecht des Lizenzgebers Voraussetzung"], descr:allDescr, type:RefdataValue.toString(), cat:'Termination Condition'],
                [name: [en: "Licensor termination notice period", de: "Kündigungsfrist des Lizenzgebers"], descr:allDescr, type:String.toString()],
                //[name: [en: "Termination right note", de: "Kündigungsrecht Hinweise"], descr:allDescr, type:String.toString()],
                [name: [en: "Termination requirement note", de: "Kündigungsrecht besondere Anforderung"], descr:allDescr, type:String.toString()]

        ]
        createPropertyDefinitionsWithI10nTranslations(requiredProps)

        def allOADescr = [en: PropertyDefinition.LIC_OA_PROP, de: PropertyDefinition.LIC_OA_PROP]

        def requiredOAProps = [
                [name: [en: "Open Access", de: "Open Access"],                                                              descr: allOADescr, type: RefdataValue.toString(), cat: 'YN'],
                [name: [en: "Type", de: "Variante"],                                                                        descr: allOADescr, type: RefdataValue.toString(), cat: 'License.OA.Type'],
                [name: [en: "Electronically Archivable Version", de: "Archivierbare Version"],                              descr: allOADescr, type: RefdataValue.toString(), cat: 'License.OA.eArcVersion'],
                [name: [en: "Embargo Period", de: "Embargo"],                                                               descr: allOADescr, type: Integer.toString()],
                [name: [en: "Receiving Modalities", de: "Bezugsmodalitäten"],                                               descr: allOADescr, type: RefdataValue.toString(), cat: 'License.OA.ReceivingModalities', multiple:true],
                [name: [en: "Authority", de: "Autorität"],                                                                  descr: allOADescr, type: RefdataValue.toString(), cat:'Authority'],
                [name: [en: "Repository", de: "Repositorium"],                                                              descr: allOADescr, type: RefdataValue.toString(), cat: 'License.OA.Repository', multiple:true],
                [name: [en: "APC Discount", de: "Sonderkonditionen für Autoren"],                                           descr: allOADescr, type: String.toString()],
                [name: [en: "Vouchers Free OA Articles", de: "Vouchers"],                                                   descr: allOADescr, type: String.toString()],
                [name: [en: "Corresponding Author Identification", de: "Autorenidentifikation"],                            descr: allOADescr, type: RefdataValue.toString(), cat: 'License.OA.CorrespondingAuthorIdentification', multiple:true],
                [name: [en: "Branding", de: "Branding"],                                                                    descr: allOADescr, type: String.toString()],
                [name: [en: "Funder", de: "Funder"],                                                                        descr: allOADescr, type: String.toString()],
                [name: [en: "License to Publish", de: "Publikationslizenz"],                                                descr: allOADescr, type: RefdataValue.toString(), cat: 'License.OA.LicenseToPublish', multiple:true],
                [name: [en: "Offsetting", de: "Offsetting Berechnungsmodell"],                                              descr: allOADescr, type: String.toString()],
                [name: [en: "Publishing Fee", de: "Publishing Fee"],                                                        descr: allOADescr, type: String.toString()],
                [name: [en: "Reading Fee", de: "Reading Fee"],                                                                  descr: allOADescr, type: String.toString()],
                [name: [en: "OA First Date", de: "OA Startdatum"],                                                          descr: allOADescr, type: Date.toString()],
                [name: [en: "OA Last Date", de: "OA Enddatum"],                                                             descr: allOADescr, type: Date.toString()],
                [name: [en: "OA Note", de: "OA Bemerkung"],                                                                 descr: allOADescr, type: String.toString()]
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredOAProps)

        def allArcDescr = [en: PropertyDefinition.LIC_ARC_PROP, de: PropertyDefinition.LIC_ARC_PROP]

        def requiredARCProps = [
                [name: [en: "Post Cancellation Online Access", de: "Zugriffsrechte: Dauerhaft"],                            descr: allArcDescr, type: RefdataValue.toString(), cat: 'YNO'],
                [name: [en: "Continuing Access: Payment Note", de: "Zugriffsrechte: Kosten"],                               descr: allArcDescr, type: RefdataValue.toString(), cat: 'License.Arc.PaymentNote'],
                [name: [en: "Continuing Access: Restrictions", de: "Zugriffsrechte: Einschränkungen"],                      descr: allArcDescr, type: RefdataValue.toString(), cat: 'YNO'],
                [name: [en: "Continuing Access: Title Transfer", de: "Zugriffsrechte: Titeltransfer"],                      descr: allArcDescr, type: RefdataValue.toString(), cat: 'License.Arc.TitleTransferRegulation'],
                [name: [en: "Archival Copy: Permission", de: "Archivkopie: Recht"],                                         descr: allArcDescr, type: RefdataValue.toString(), cat: 'YNO'],
                [name: [en: "Archival Copy Content", de: "Archivkopie Form"],                                               descr: allArcDescr, type: RefdataValue.toString(), cat: 'License.Arc.ArchivalCopyContent', multiple:true],
                [name: [en: "Archival Copy: Cost", de: "Archivkopie: Kosten"],                                              descr: allArcDescr, type: RefdataValue.toString(), cat: 'License.Arc.ArchivalCopyCost'],
                [name: [en: "Archival Copy: Time", de: "Archivkopie: Zeitpunkt"],                                           descr: allArcDescr, type: RefdataValue.toString(), cat: 'License.Arc.ArchivalCopyTime'],
                [name: [en: "Hosting: Permission", de: "Hostingrecht"],                                                     descr: allArcDescr, type: RefdataValue.toString(), cat: 'YNO'],
                [name: [en: "Hosting: Obligation", de: "Hostingpflicht"],                                                   descr: allArcDescr, type: RefdataValue.toString(), cat: 'YN'],
                [name: [en: "Hosting Time", de: "Hostingrecht Zeitpunkt"],                                                  descr: allArcDescr, type: RefdataValue.toString(), cat: 'License.Arc.HostingTime', multiple:true],
                [name: [en: "Hosting: Additonal Agreement Necessary", de: "Hostingrecht: Zusatzvereinbarung notwendig"],    descr: allArcDescr, type: RefdataValue.toString(), cat: 'YN'],
                [name: [en: "Hosting: Authorized", de: "Hostingrecht: Berechtigte"],                                        descr: allArcDescr, type: RefdataValue.toString(), cat: 'License.Arc.Authorized', multiple:true],
                [name: [en: "Hosting: Restriction", de: "Hostingrecht: Einschränkung"],                                     descr: allArcDescr, type: RefdataValue.toString(), cat: 'License.Arc.HostingRestriction', multiple:true],
                [name: [en: "Hosting: Solution", de: "Hostingrecht: Lösung"],                                               descr: allArcDescr, type: RefdataValue.toString(), cat: 'License.Arc.HostingSolution', multiple:true],
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredARCProps)
    }

    def createSubscriptionProperties() {

        def allDescr = [en: PropertyDefinition.SUB_PROP, de: PropertyDefinition.SUB_PROP]

        def requiredProps = [
                [name: [en: "GASCO Entry", de: "GASCO-Eintrag"], descr:allDescr, type:RefdataValue.toString(), cat:'YN']

        ]
        createPropertyDefinitionsWithI10nTranslations(requiredProps)
    }

    def createPrivateProperties() {

        def allOrgDescr = [en: PropertyDefinition.ORG_PROP, de: PropertyDefinition.ORG_PROP]

        // TODO - remove HOTFIX: hardcoded hbz properties
        def requiredOrgProps = [
                [name: [en: "Note", de: "Anmerkung"],
                            tenant: 'hbz', descr: allOrgDescr, type: String.toString()],
                [name: [en: "promotionsrecht", de: "Promotionsrecht"],
                            tenant: 'hbz', descr: allOrgDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "privatrechtlich", de: "Privatrechtlich"],
                            tenant: 'hbz', descr: allOrgDescr, type:RefdataValue.toString(), cat:'YN'],
                [name: [en: "ezb teilnehmer", de: "EZB-Teilnehmer"],
                            tenant: 'hbz', descr: allOrgDescr, type:RefdataValue.toString(), cat:'YN'],
                [name: [en: "nationallizenz teilnehmer", de: "Nationallizenz-Teilnehmer"],
                            tenant: 'hbz', descr: allOrgDescr, type:RefdataValue.toString(), cat:'YN'],
                [name: [en: "discovery system", de: "Discovery-System"],
                            tenant: 'hbz', descr: allOrgDescr, type:RefdataValue.toString(), cat:'YN'],
                [name: [en: "verwendete discovery systeme", de: "Verwendete Discovery-Systeme"],
                            tenant: 'hbz', descr: allOrgDescr, type:String.toString()]
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredOrgProps)

        def allPrsDescr = [en: PropertyDefinition.PRS_PROP, de: PropertyDefinition.PRS_PROP]

        def requiredPrsProps = [
                [name: [en: "Note", de: "Anmerkung"], descr: allPrsDescr, type: String.toString()]
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredPrsProps)
    }

    def createPropertyDefinitionsWithI10nTranslations(requiredProps) {

        requiredProps.each { default_prop ->
            /* TODO merge_conflict @ 0.4.5 into 0.5
            def key = default_prop.key ?: default_prop.name['en']
            def prop = PropertyDefinition.findByName(key)

            if (! prop) {
                log.debug("Unable to locate property definition for ${key} .. creating")
                prop = new PropertyDefinition(name: key)

                if (default_prop.cat != null) {
                    prop.setRefdataCategory(default_prop.cat)
                }
            }
            */
            def prop
            def tenant

            if (default_prop.tenant) {
                tenant = Org.findByShortname(default_prop.tenant)

                if (tenant) {
                    prop = PropertyDefinition.findByNameAndTenant(default_prop.name['en'], tenant)
                } else {
                    log.debug("Unable to locate tenant: ${default_prop.tenant} .. ignored")
                    return
                }
            } else {
                prop = PropertyDefinition.findByName(default_prop.name['en'])
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
            def home    = new SitePage(alias: "Home",    action: "index",       controller: "home").save()
            def profile = new SitePage(alias: "Profile", action: "index",       controller: "profile").save()
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

    // Subscription.metaClass.static.methodMissing = { String methodName, args ->
    //   if ( methodName.startsWith('setNsId') ) {
    //     log.debug("methodMissing ${methodName}, ${args}")
    //   }
    //   else {
    //     throw new groovy.lang.MissingMethodException(methodName)
    //   }
    // }
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
                [[en: 'Agency', de: 'Agentur'], sub]
        ]

        RefdataCategory.loc('Organisational Role',  [en: 'Organisational Role', de: 'Organisational Role'])

        entries.each{ rdv ->
            def i10n = rdv[0]
            def group = rdv[1]

            def val = RefdataValue.loc("Organisational Role", i10n)
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

        RefdataCategory.loc('YN',                   	                    [en: 'Yes/No', de: 'Ja/Nein'])
        RefdataCategory.loc('YNO',                  	                    [en: 'Yes/No/Others', de: 'Ja/Nein/Anderes'])
        RefdataCategory.loc('Permissions',                                  [en: 'Permissions', de: 'Berechtigungen'])
        RefdataCategory.loc('Existence',                                    [en: 'Existence', de: 'Vorliegen'])
        RefdataCategory.loc('Indemnification',                              [en: 'Indemnification Choice', de: 'Entschädigung Auswahl'])
        RefdataCategory.loc('Confidentiality',                              [en: 'Confidentiality Choice', de: 'Vertraulichkeit Auswahl'])
        RefdataCategory.loc('Termination Condition',                        [en: 'Termination Condition', de: 'Kündigung Voraussetzung'])
        RefdataCategory.loc('AddressType',          	                    [en: 'Address Type', de: 'Art der Adresse'])
        RefdataCategory.loc('Cluster Type',         	                    [en: 'Cluster Type', de: 'Cluster Type'])
        RefdataCategory.loc('CreatorType',         	                        [en: 'Creator Type', de: 'Creator Type'])
        RefdataCategory.loc('Combo Type',           	                    [en: 'Combo Type', de: 'Combo Type'])
        RefdataCategory.loc('ConcurrentAccess',     	                    [en: 'Concurrent Access', de: 'SimUser'])
        RefdataCategory.loc('ContactContentType',   	                    [en: 'Type of Contact', de: 'Kontakttyp'])
        RefdataCategory.loc('ContactType',          	                    [en: 'Contact Type', de: 'Art des Kontaktes'])
        RefdataCategory.loc('CoreStatus',           	                    [en: 'Core Status', de: 'Kerntitel-Status'])
        RefdataCategory.loc('Country',              	                    [en: 'Country', de: 'Land'])
        RefdataCategory.loc('FactType',             	                    [en: 'FactType', de: 'FactType'])
        RefdataCategory.loc('Federal State',        	                    [en: 'Federal State', de: 'Bundesland'])
        RefdataCategory.loc('Funder Type',          	                    [en: 'Funder Type', de: 'Trägerschaft'])
        RefdataCategory.loc('Gender',               	                    [en: 'Gender', de: 'Geschlecht'])
        RefdataCategory.loc('OrgSector',            	                    [en: 'OrgSector', de: 'Bereich'])
        RefdataCategory.loc('Library Network',      	                    [en: 'Library Network', de: 'Verbundszugehörigkeit'])
        RefdataCategory.loc('Library Type',         	                    [en: 'Library Type', de: 'Bibliothekstyp'])
        RefdataCategory.loc('OrgType',              	                    [en: 'Organisation Type', de: 'Organisationstyp'])
        RefdataCategory.loc('Person Function',      	                    [en: 'Person Function', de: 'Funktion'])
        RefdataCategory.loc('Person Contact Type',  	                    [en: 'Person: Contact Type', de: 'Kontaktart'])
        RefdataCategory.loc('Person Position',      	                    [en: 'Person Position', de: 'Position'])
        RefdataCategory.loc('Person Responsibility',	                    [en: 'Person Responsibility', de: 'Verantwortlich'])
        RefdataCategory.loc('Subscription Status',          	            [en: 'Subscription Status', de: 'Lizenzstatus'])
        RefdataCategory.loc('Task Priority',                	            [en: 'Task Priority', de: 'Aufgabenpriorität'])
        RefdataCategory.loc('Task Status',          	                    [en: 'Task Status', de: 'Aufgabenstatus'])
        RefdataCategory.loc('License.OA.ReceivingModalities',               [en: 'Receiving Modalities', de: 'Bezugsmodalitäten'])
        RefdataCategory.loc('License.OA.Repository',                        [en: 'Repository', de: 'Repositorium'])
        RefdataCategory.loc('License.OA.CorrespondingAuthorIdentification', [en: 'Corresponding Author Identification', de: 'Autorenindentifikation'])
        RefdataCategory.loc('License.OA.LicenseToPublish',                  [en: 'License to Publish', de: 'Publikationslizenz'])
        RefdataCategory.loc('License.Arc.PaymentNote',                      [en: 'Archive Payment Note', de: 'Zugriffsrechte Kosten'])
		RefdataCategory.loc('License.Arc.TitletransferRegulation',          [en: 'Titletransfer Regulation', de: 'Titeltransfer Regeln'])
		RefdataCategory.loc('License.Arc.ArchivalCopyCost',                 [en: 'Archival Copy Cost', de: 'Archivkopie Kosten'])
		RefdataCategory.loc('License.Arc.ArchivalCopyTime',                 [en: 'Archival Copy Time', de: 'Archivkopie Zeitpunkt'])
        RefdataCategory.loc('License.Arc.ArchivalCopyContent',              [en: 'Archival Copy Content', de: 'Archivkopie Form'])
        RefdataCategory.loc('License.Arc.HostingTime',                      [en: 'Hosting Time', de: 'Hostingrecht Zeitpunkt'])
        RefdataCategory.loc('License.Arc.Authorized',                       [en: 'Hosting Authorized', de: 'Hostingrecht Berechtigte'])
        RefdataCategory.loc('License.Arc.HostingRestriction',               [en: 'Hosting Restriction', de: 'Hostingrecht Einschränkung'])
        RefdataCategory.loc('License.Arc.HostingSolution',                  [en: 'Hosting Solution', de: 'Hostingrecht Lösung'])
        RefdataCategory.loc('Package Status',                               [en: 'Package Status', de: 'Paketstatus'])
        RefdataCategory.loc('Number Type',                               [en: 'Number Type', de: 'Zahlen-Typ'])
        // refdata values

        RefdataValue.loc('YN',   [en: 'Yes', de: 'Ja'])
        RefdataValue.loc('YN',   [en: 'No', de: 'Nein'])

        RefdataValue.loc('YNO',  [en: 'Yes', de: 'Ja'])
        RefdataValue.loc('YNO',  [en: 'No', de: 'Nein'])
        RefdataValue.loc('YNO',  [en: 'Not applicable', de: 'Nicht zutreffend'])
        RefdataValue.loc('YNO',  [en: 'Planed', de: 'Geplant'])
        RefdataValue.loc('YNO',  [en: 'Unknown', de: 'Unbekannt'])
        RefdataValue.loc('YNO',  [en: 'Other', de: 'Andere'])

        RefdataValue.loc('Permissions',  [en: 'Permitted (explicit)', de: 'Ausdrücklich erlaubt'])
        RefdataValue.loc('Permissions',  [en: 'Permitted (interpreted)', de: 'Vermutlich erlaubt'])
        RefdataValue.loc('Permissions',  [en: 'Prohibited (explicit)', de: 'Ausdrücklich verboten'])
        RefdataValue.loc('Permissions',  [en: 'Prohibited (interpreted)', de: 'Vermutlich verboten'])
        RefdataValue.loc('Permissions',  [en: 'Silent', de: 'Stillschweigend'])
        RefdataValue.loc('Permissions',  [en: 'Not applicable', de: 'Nicht zutreffend'])
        RefdataValue.loc('Permissions',  [en: 'Unknown', de: 'Unbekannt'])

        RefdataValue.loc('Existence',   [en: 'Existent', de: 'Bestehend'])
        RefdataValue.loc('Existence',   [en: 'Nonexistend', de: 'Fehlend'])

        RefdataValue.loc('Indemnification',  [en: 'General', de: 'Generell'])
        RefdataValue.loc('Indemnification',  [en: 'Intellectual Property Only', de: 'Nur geistiges Eigentum'])
        RefdataValue.loc('Indemnification',  [en: 'Other', de: 'Andere'])
        RefdataValue.loc('Indemnification',  [en: 'Unknown', de: 'Unbekannt'])

        RefdataValue.loc('Confidentiality',  [en: 'All', de: 'Alles'])
        RefdataValue.loc('Confidentiality',  [en: 'All but user terms', de: 'Alles außer Nutzungsbedingungen'])
        RefdataValue.loc('Confidentiality',  [en: 'Financial only', de: 'Nur Finanzangelegenheiten'])
        RefdataValue.loc('Confidentiality',  [en: 'No', de: 'Nein'])
        RefdataValue.loc('Confidentiality',  [en: 'Unknown', de: 'Unbekannt'])

        RefdataValue.loc('Termination Condition',  [en: 'At will', de: 'Nach Belieben'])
        RefdataValue.loc('Termination Condition',  [en: 'Breach by Licensor/Licensee', de: 'Wegen Verstoß des Vertragspartners'])
        RefdataValue.loc('Termination Condition',  [en: 'Other', de: 'Andere Gründe'])
        RefdataValue.loc('Termination Condition',  [en: 'Unknown', de: 'Unbekannt'])

        RefdataValue.loc('AddressType', [en: 'Postal address', de: 'Postanschrift'])
        RefdataValue.loc('AddressType', [en: 'Billing address', de: 'Rechnungsanschrift'])
        RefdataValue.loc('AddressType', [en: 'Delivery address', de: 'Lieferanschrift'])
        RefdataValue.loc('AddressType', [en: 'Library address', de: 'Bibliotheksanschrift'])
        RefdataValue.loc('AddressType', [en: 'Legal patron address', de: 'Anschrift des rechtlichen Trägers'])

        RefdataValue.loc('ClusterType', [en: 'Undefined'])

        RefdataValue.loc('CreatorType', [en: 'Author', de: 'Autor'])
        RefdataValue.loc('CreatorType', [en: 'Editor', de: 'Herausgeber'])

        RefdataValue.loc('ConcurrentAccess',     [en: 'Specified', de: 'Festgelegt'])
        RefdataValue.loc('ConcurrentAccess',     [en: 'Not Specified', de: 'Nicht festgelegt'])
        RefdataValue.loc('ConcurrentAccess',     [en: 'No limit', de: 'Ohne Begrenzung'])
        RefdataValue.loc('ConcurrentAccess',     [en: 'Other', de: 'Andere'])

        RefdataValue.loc('ContactContentType',   [en: 'E-Mail', de: 'E-Mail'])
        RefdataValue.loc('ContactContentType',   [en: 'Phone', de: 'Telefon'])
        RefdataValue.loc('ContactContentType',   [en: 'Fax', de: 'Fax'])
        RefdataValue.loc('ContactContentType',   [en: 'Url', de: 'Url'])

        RefdataValue.loc('ContactType',  [en: 'Personal', de: 'Privat'])
        RefdataValue.loc('ContactType',  [en: 'Job-related', de: 'Geschäftlich'])

        RefdataValue.loc('CoreStatus',   [en: 'Yes', de: 'Ja'])
        RefdataValue.loc('CoreStatus',   [en: 'No', de: 'Nein'])
        RefdataValue.loc('CoreStatus',   [en: 'Print', de: 'Print'])
        RefdataValue.loc('CoreStatus',   [en: 'Electronic', de: 'Elektronisch'])
        RefdataValue.loc('CoreStatus',   [en: 'Print+Electronic', de: 'Print & Elektronisch'])

        RefdataValue.loc('Country',   [en: 'Germany', de: 'Deutschland'])
        RefdataValue.loc('Country',   [en: 'Switzerland', de: 'Schweiz'])
        RefdataValue.loc('Country',   [en: 'Austria', de: 'Österreich'])
        RefdataValue.loc('Country',   [en: 'France', de: 'Frankreich'])
        RefdataValue.loc('Country',   [en: 'Great Britain', de: 'Großbritannien'])
        RefdataValue.loc('Country',   [en: 'United States of America', de: 'Vereinigte Staaten von Amerika'])
        RefdataValue.loc('Country',   [en: 'Belgium', de: 'Belgien'])
        RefdataValue.loc('Country',   [en: 'Italy', de: 'Italien'])
        RefdataValue.loc('Country',   [en: 'Netherlands', de: 'Niederlande'])
        RefdataValue.loc('Country',   [en: 'Italy', de: 'Italien'])


        RefdataValue.loc('FactType', [en: 'STATS:JR1'])
        RefdataValue.loc('FactType', [en: 'STATS:JR1GOA'])

        RefdataValue.loc('Federal State',   [en: 'Baden-Wurttemberg', de: 'Baden-Württemberg'])
        RefdataValue.loc('Federal State',   [en: 'Bavaria', de: 'Bayern'])
        RefdataValue.loc('Federal State',   [en: 'Berlin', de: 'Berlin'])
        RefdataValue.loc('Federal State',   [en: 'Brandenburg', de: 'Brandenburg'])
        RefdataValue.loc('Federal State',   [en: 'Bremen', de: 'Bremen'])
        RefdataValue.loc('Federal State',   [en: 'Hamburg', de: 'Hamburg'])
        RefdataValue.loc('Federal State',   [en: 'Hesse', de: 'Hessen'])
        RefdataValue.loc('Federal State',   [en: 'Lower Saxony', de: 'Niedersachsen'])
        RefdataValue.loc('Federal State',   [en: 'Mecklenburg-West Pomerania', de: 'Mecklenburg-Vorpommern'])
        RefdataValue.loc('Federal State',   [en: 'North Rhine-Westphalia', de: 'Nordrhein-Westfalen'])
        RefdataValue.loc('Federal State',   [en: 'Rhineland-Palatinate', de: 'Rheinland-Pfalz'])
        RefdataValue.loc('Federal State',   [en: 'Saarland', de: 'Saarland'])
        RefdataValue.loc('Federal State',   [en: 'Saxony', de: 'Sachsen'])
        RefdataValue.loc('Federal State',   [en: 'Saxony-Anhalt', de: 'Sachsen-Anhalt'])
        RefdataValue.loc('Federal State',   [en: 'Schleswig-Holstein', de: 'Schleswig-Holstein'])
        RefdataValue.loc('Federal State',   [en: 'Thuringia', de: 'Thüringen'])

        RefdataValue.loc('Funder Type',   [en: 'Federal Republic of Germany', de: 'Bundesrepublik Deutschland'])
        RefdataValue.loc('Funder Type',   [en: 'Federal State', de: 'Land'])
        RefdataValue.loc('Funder Type',   [en: 'County', de: 'Kreis'])
        RefdataValue.loc('Funder Type',   [en: 'Commune', de: 'Gemeinde'])
        RefdataValue.loc('Funder Type',   [en: 'Other Territorial Authority', de: 'Sonstige Gebietskörperschaft'])
        RefdataValue.loc('Funder Type',   [en: 'Other Public Sector Funder', de: 'Sonstige öffentliche Trägerschaft'])
        RefdataValue.loc('Funder Type',   [en: 'Corporate Body or Foundation under Public Law', de: 'Körperschaft oder Stiftung des öffentlichen Rechts'])
        RefdataValue.loc('Funder Type',   [en: 'Corporate Body or Foundation under Private Law', de: 'Körperschaft oder Stiftung des privaten Rechts'])
        RefdataValue.loc('Funder Type',   [en: 'Protestant Church', de: 'Evangelische Kirche'])
        RefdataValue.loc('Funder Type',   [en: 'Catholic Church', de: 'Katholische Kirche'])
        RefdataValue.loc('Funder Type',   [en: 'Other Religious Communities', de: 'Sonstige Religionsgemeinschaften'])
        RefdataValue.loc('Funder Type',   [en: 'Private Funding Body (Natural Person)', de: 'Private Trägerschaft (natürliche Personen)'])
        RefdataValue.loc('Funder Type',   [en: 'Foreign Funding Body', de: 'Ausländische Trägerschaft'])

        RefdataValue.loc('Gender',   [en: 'Female', de: 'Weiblich'])
        RefdataValue.loc('Gender',   [en: 'Male', de: 'Männlich'])
		RefdataValue.loc('Gender',   [en: 'Third Gender', de: 'Inter/Divers'])

        RefdataValue.loc('Library Network',   [en: 'BVB', de: 'BVB'])
        RefdataValue.loc('Library Network',   [en: 'GBV', de: 'GBV'])
        RefdataValue.loc('Library Network',   [en: 'hbz', de: 'hbz'])
        RefdataValue.loc('Library Network',   [en: 'HeBIS', de: 'HeBIS'])
        RefdataValue.loc('Library Network',   [en: 'KOBV', de: 'KOBV'])
        RefdataValue.loc('Library Network',   [en: 'SWB', de: 'SWB'])
        RefdataValue.loc('Library Network',   [en: 'No Network', de: 'Keine Zugehörigkeit'])

        RefdataValue.loc('Library Type',   [en: 'Fachhochschule', de: 'Fachhochschule'])
        RefdataValue.loc('Library Type',   [en: 'Forschungseinrichtung', de: 'Forschungseinrichtung'])
        RefdataValue.loc('Library Type',   [en: 'Institutsbibliothek', de: 'Institutsbibliothek'])
        RefdataValue.loc('Library Type',   [en: 'Kunst- und Musikhochschule', de: 'Kunst- und Musikhochschule'])
        RefdataValue.loc('Library Type',   [en: 'Öffentliche Bibliothek', de: 'Öffentliche Bibliothek'])
        RefdataValue.loc('Library Type',   [en: 'Universität', de: 'Universität'])
        RefdataValue.loc('Library Type',   [en: 'Staats-/ Landes-/ Regionalbibliothek', de: 'Staats-/ Landes-/ Regionalbibliothek'])
        RefdataValue.loc('Library Type',   [en: 'Wissenschafltiche Spezialbibliothek', de: 'Wissenschafltiche Spezialbibliothek'])
        RefdataValue.loc('Library Type',   [en: 'Sonstige', de: 'Sonstige'])
        RefdataValue.loc('Library Type',   [en: 'keine Angabe', de: 'keine Angabe'])

        RefdataValue.loc('OrgSector',    [en: 'Higher Education', de: 'Akademisch'])
        RefdataValue.loc('OrgSector',    [key: 'Publisher', en: 'Commercial', de: 'Kommerziell'])

        RefdataValue.loc('OrgType',      [en: 'Consortium', de: 'Konsortium'])
        RefdataValue.loc('OrgType',      [en: 'Institution', de: 'Einrichtung'])
        RefdataValue.loc('OrgType',      [en: 'Publisher', de: 'Verlag'])
        RefdataValue.loc('OrgType',      [en: 'Provider', de: 'Anbieter'])
        RefdataValue.loc('OrgType',      [en: 'Other', de: 'Andere'])

        RefdataValue.loc('Package Status',      [en: 'Deleted', de: 'Gelöscht'])
        RefdataValue.loc('Package Status',      [en: 'Current', de: 'Aktuell'])
        RefdataValue.loc('Package Status',      [en: 'Retired', de: 'Abgelaufen'])

        RefdataValue.loc('Person Contact Type', [en: 'Personal contact', de: 'Personenkontakt'])
        RefdataValue.loc('Person Contact Type', [en: 'Functional contact', de: 'Funktionskontakt'])

        RefdataValue.loc('Person Function',     [en: 'General contact person', de: 'Hauptkontakt'])
        RefdataValue.loc('Person Function',     [en: 'Bestandsaufbau', de: 'Bestandsaufbau'])
        RefdataValue.loc('Person Function',     [en: 'Direktion', de: 'Direktion'])
        RefdataValue.loc('Person Function',     [en: 'Direktionsassistenz', de: 'Direktionsassistenz'])
        RefdataValue.loc('Person Function',     [en: 'Erwerbungsabteilung', de: 'Erwerbungsabteilung'])
        RefdataValue.loc('Person Function',     [en: 'Erwerbungsleitung', de: 'Erwerbungsleitung'])
        RefdataValue.loc('Person Function',     [en: 'Medienbearbeitung', de: 'Medienbearbeitung'])
        RefdataValue.loc('Person Function',     [en: 'Zeitschriftenabteilung', de: 'Zeitschriftenabteilung'])
        RefdataValue.loc('Person Function',     [en: 'Fachreferat', de: 'Fachreferat'])
        RefdataValue.loc('Person Function',     [en: 'Bereichsbibliotheksleitung', de: 'Bereichsbibliotheksleitung'])

        RefdataValue.loc('Person Position',     [en: 'Account Manager', de: 'Account Manager'])
        RefdataValue.loc('Person Position',     [en: 'Head Access Services', de: 'Erwerbungsleiter'])
        RefdataValue.loc('Person Position',     [en: 'Library Director', de: 'Bibliotheksdirektor'])
        RefdataValue.loc('Person Position',     [en: 'Sales Director', de: 'Sales Director'])
        RefdataValue.loc('Person Position',     [en: 'Sales Support', de: 'Sales Support'])
        RefdataValue.loc('Person Position',     [en: 'Technichal Support', de: 'Technischer Support'])

        RefdataValue.loc('Person Responsibility',    [en: 'Specific license editor', de: 'Vertragsbearbeiter'])
        RefdataValue.loc('Person Responsibility',    [en: 'Specific subscription editor', de: 'Lizenzkontakt'])
        RefdataValue.loc('Person Responsibility',    [en: 'Specific package editor', de: 'Paketbearbeiter'])
        RefdataValue.loc('Person Responsibility',    [en: 'Specific cluster editor', de: 'Gruppenkontakt'])
        RefdataValue.loc('Person Responsibility',    [en: 'Specific title editor', de: 'Titelbearbeiter'])

        RefdataValue.loc('Subscription Status',      [en: 'Current', de: 'Aktuell'])
        RefdataValue.loc('Subscription Status',      [en: 'Deleted', de: 'Gelöscht'])
        RefdataValue.loc('Subscription Status',      [en: 'Expired', de: 'Abgelaufen'])
        RefdataValue.loc('Subscription Status',      [en: 'Terminated', de: 'Beendet'])
        RefdataValue.loc('Subscription Status',      [en: 'Under Negotiation',   de: 'In Verhandlung'])
        RefdataValue.loc('Subscription Status',      [en: 'Under Consideration', de: 'Entscheidung steht aus'])
        RefdataValue.loc('Subscription Status',      [en: 'Under Consortial Examination',   de: 'Wird konsortial geprüft'])
        RefdataValue.loc('Subscription Status',      [en: 'Under Institutional Examination',   de: 'Wird institutionell geprüft'])
        RefdataValue.loc('Subscription Status',      [en: 'Test Access',   de: 'Testzugriff'])

		RefdataValue.loc('Subscription Type',      [en: 'Alliance Licence', de: 'Allianzlizenz'])
		RefdataValue.loc('Subscription Type',      [en: 'National Licence', de: 'Nationallizenz'])
		RefdataValue.loc('Subscription Type',      [en: 'Local Licence', de: 'Lokale Lizenz'])
		RefdataValue.loc('Subscription Type',      [en: 'Consortial Licence', de: 'Konsortiallizenz'])

        RefdataValue.loc('Task Priority',   [en: 'Trivial', de: 'Trivial'])
        RefdataValue.loc('Task Priority',   [en: 'Low', de: 'Niedrig'])
        RefdataValue.loc('Task Priority',   [en: 'Normal', de: 'Mittel'])
        RefdataValue.loc('Task Priority',   [en: 'High', de: 'Hoch'])
        RefdataValue.loc('Task Priority',   [en: 'Extreme', de: 'Extrem'])

        RefdataValue.loc('Task Status',      [en: 'Open', de: 'Offen'])
        RefdataValue.loc('Task Status',      [en: 'Done', de: 'Erledigt'])
        RefdataValue.loc('Task Status',      [en: 'Deferred', de: 'Zurückgestellt'])
		
        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'By Author', de: 'Über Autor'])
        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'On Demand', de: 'Auf Nachfrage'])
        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'From Database', de: 'Aus Datenank'])
        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'Automatic Delivery', de: 'Automatische Lieferung'])
        RefdataValue.loc('License.OA.ReceivingModalities',       [en: 'Link by Publisher', de: 'Verlinkung durch Verlag'])
        
        RefdataValue.loc('License.OA.Repository',                [en: 'Own Choice', de: 'Nach Wahl'])
        RefdataValue.loc('License.OA.Repository',                [en: 'Publishers', de: 'Verlagseigenes'])
        RefdataValue.loc('License.OA.Repository',                [en: 'Subject specific', de: 'Fachspezifisches'])
        RefdataValue.loc('License.OA.Repository',                [en: 'Website of Author', de: 'Website des Autors'])
        RefdataValue.loc('License.OA.Repository',                [en: 'Institutional', de: 'Institutionelles'])
        
        RefdataValue.loc('License.OA.CorrespondingAuthorIdentification',    [en: 'IP Range', de: 'Über IP-Bereich'])
        RefdataValue.loc('License.OA.CorrespondingAuthorIdentification',    [en: 'Email Domain', de: 'E-Mail-Domäne'])
        RefdataValue.loc('License.OA.CorrespondingAuthorIdentification',    [en: 'Research Institute', de: 'Über Institut'])
        RefdataValue.loc('License.OA.CorrespondingAuthorIdentification',    [en: 'ORCID', de: 'ORCID'])
        
        RefdataValue.loc('License.OA.LicenseToPublish',          [en: 'CC-BY', de: 'CC-BY'])
        RefdataValue.loc('License.OA.LicenseToPublish',          [en: 'CC-BY-NC', de: 'CC-BY-NC'])
        RefdataValue.loc('License.OA.LicenseToPublish',          [en: 'CC-BY-NC-ND', de: 'CC-BY-NC-ND'])
        
        RefdataValue.loc('License.Arc.PaymentNote',       [en: 'No Hosting fee', de: 'Dauerhaft kostenfrei (keine Hosting Fee)'])
        RefdataValue.loc('License.Arc.PaymentNote',       [en: 'Hosting fee', de: 'Hosting Fee zu zahlen'])
		
        RefdataValue.loc('License.Arc.TitletransferRegulation',  [en: 'Existing Regulation', de: 'Regelung vorhanden'])
        RefdataValue.loc('License.Arc.TitletransferRegulation',  [en: 'Transfer Code of Practice', de: 'Transfer Code of Practice'])
        RefdataValue.loc('License.Arc.TitletransferRegulation',  [en: 'No Regulation', de: 'Keine Regelung'])
		
        RefdataValue.loc('License.Arc.ArchivalCopyCost',         [en: 'Free', de: 'Kostenlos'])
        RefdataValue.loc('License.Arc.ArchivalCopyCost',         [en: 'With Charge', de: 'Gegen Gebühr'])
        RefdataValue.loc('License.Arc.ArchivalCopyCost',         [en: 'Self-copied', de: 'Kopie selbst anzufertigen'])
		
        RefdataValue.loc('License.Arc.ArchivalCopyTime',         [en: 'Licence Start Date', de: 'Mit Vertragsbeginn'])
        RefdataValue.loc('License.Arc.ArchivalCopyTime',         [en: 'On Request', de: 'Auf Anfrage'])
        RefdataValue.loc('License.Arc.ArchivalCopyTime',         [en: 'Licence End Date', de: 'Mit Vertragsende'])
        RefdataValue.loc('License.Arc.ArchivalCopyTime',         [en: 'Trigger Event', de: 'Mit Trigger Event'])
        
        RefdataValue.loc('License.Arc.ArchivalCopyContent',      [en: 'Data', de: 'Rohdaten'])
        RefdataValue.loc('License.Arc.ArchivalCopyContent',      [en: 'With Metadata', de: 'Inkl. Metadaten'])
        RefdataValue.loc('License.Arc.ArchivalCopyContent',      [en: 'With Software', de: 'Inkl. Software'])
        RefdataValue.loc('License.Arc.ArchivalCopyContent',      [en: 'DRM-free', de: 'Ohne DRM'])
        
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'Always', de: 'Immer'])
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'Exclusive', de: 'Ohne Anbieter'])
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'From Expiration On', de: 'Ab Vertragsende'])
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'Predefined time', de: 'Fester Zeitpunkt'])
        RefdataValue.loc('License.Arc.HostingTime',      [en: 'Trigger Event', de: 'Mit Trigger-Event'])
        
        RefdataValue.loc('License.Arc.Authorized',      [en: 'Licensee', de: 'Lizenznehmer'])
        RefdataValue.loc('License.Arc.Authorized',      [en: 'SSG-Library', de: 'SSG-Bibliothek'])
        RefdataValue.loc('License.Arc.Authorized',      [en: 'Contractor', de: 'Vertragspartner'])
        RefdataValue.loc('License.Arc.Authorized',      [en: 'Contractor With Publisher\'s Assent', de: 'Vertragspartner nach Genehmigung durch Anbieter'])
        
        RefdataValue.loc('License.Arc.HostingRestriction',      [en: 'File Format', de: 'Dateiformat'])
        RefdataValue.loc('License.Arc.HostingRestriction',      [en: 'Year', de: 'Jahrgang'])
        RefdataValue.loc('License.Arc.HostingRestriction',      [en: 'Access Period', de: 'Zugriffsdauer'])
        RefdataValue.loc('License.Arc.HostingRestriction',      [en: 'Use', de: 'Nutzung'])
        
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'NatHosting PLN', de: 'NatHosting PLN'])
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'NatHosting Portico', de: 'NatHosting Portico'])
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'Publisher', de: 'Verlag'])
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'Own Host', de: 'Eigener Host'])
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'Third Party Systems', de: 'Drittsysteme'])
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'LOCKSS', de: 'LOCKSS'])
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'CLOCKSS', de: 'CLOCKSS'])
        RefdataValue.loc('License.Arc.HostingSolution',      [en: 'Portico', de: 'Portico'])

        RefdataValue.loc('Number Type',      [en: 'Students', de: 'Studenten'])
        RefdataValue.loc('Number Type',      [en: 'Scientific staff', de: 'wissenschaftliches Personal'])
        RefdataValue.loc('Number Type',      [en: 'User', de: 'Nutzer'])
        RefdataValue.loc('Number Type',      [en: 'Population', de: 'Einwohner'])

        RefdataValue.loc('Access Method',      [key: 'ip4', en: 'IPv4', de: 'IPv4'])
        RefdataValue.loc('Access Method',      [key: 'ip6', en: 'IPv6', de: 'IPv6'])
        RefdataValue.loc('Access Method',      [key: 'proxy', en: 'Proxy', de: 'Proxy'])
        RefdataValue.loc('Access Method',      [key: 'shibb', en: 'Shibboleth', de: 'Shibboleth'])
        RefdataValue.loc('Access Method',      [key: 'up', en: 'Username/Password', de: 'Benutzername/Passwort'])
        RefdataValue.loc('Access Method',      [key: 'oa', en: 'Open Athens', de: 'OpenAthens'])
        RefdataValue.loc('Access Method',      [key: 'ref', en: 'Referrer', de: 'Referrer'])

        RefdataValue.loc('Access Method IP',      [en: 'IPv4', de: 'IPv4'])
        RefdataValue.loc('Access Method IP',      [en: 'IPv6', de: 'IPv6'])

        RefdataValue.loc('Access Point Type',      [key: 'ip', en: 'IP', de: 'IP'])
        //RefdataValue.loc('Access Point Type',      [key: 'shibb', en: 'Shibboleth', de: 'Shibboleth'])

        RefdataValue.loc('IPv4 Address Format',      [key: 'cidr',   en: 'IPv4 (CIDR)', de: 'IPv4 (CIDR)'])
        RefdataValue.loc('IPv4 Address Format',      [key: 'ranges', en: 'IPv4 (Ranges)', de: 'IPv4 (Bereiche)'])
        RefdataValue.loc('IPv4 Address Format',      [key: 'input', en: 'IPv4 (Input)', de: 'IPv4 (Eingabe)'])
        RefdataValue.loc('IPv6 Address Format',      [key: 'cidr',   en: 'IPv6 (CIDR)', de: 'IPv6 (CIDR)'])
        RefdataValue.loc('IPv6 Address Format',      [key: 'ranges', en: 'IPv6 (Ranges)', de: 'IPv6 (Bereiche)'])
        RefdataValue.loc('IPv6 Address Format',      [key: 'input', en: 'IPv6 (Input)', de: 'IPv6 (Eingabe)'])

    }

    def setupOnixPlRefdata = {

        // copied from Config.groovy .. START

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
                RefdataCategory.lookupOrCreate(rdc, rdv)
            }
        }

        // copied from Config.groovy .. END

        // -------------------------------------------------------------------
        // ONIX-PL Additions
        // -------------------------------------------------------------------

        RefdataCategory.loc('Authority',
                [en: 'Authority', de: 'Autorität'])

        RefdataValue.loc('Authority', [en: 'Author', de: 'Autor'])
        RefdataValue.loc('Authority', [en: 'Institution', de: 'Institution'])
        RefdataValue.loc('Authority', [en: 'Author and Institution', de: 'Autor und Institution'])

        RefdataCategory.loc('CostItemCategory',
                [en: 'CostItemCategory', de: 'CostItemCategory'])

        //RefdataValue.loc('CostItemCategory', [en: 'Price', de: 'Preis'])
        //RefdataValue.loc('CostItemCategory', [en: 'Bank Charge', de: 'Bank Charge'])
        //RefdataValue.loc('CostItemCategory', [en: 'Refund', de: 'Erstattung'])
        //RefdataValue.loc('CostItemCategory', [en: 'Other', de: 'Andere'])

        RefdataCategory.loc('CostItemElement',
                [en: 'CostItemElement', de: 'CostItemElement'])

        //RefdataValue.loc('CostItemElement', [en: 'Admin Fee', de: 'Admin Fee'])
        //RefdataValue.loc('CostItemElement', [en: 'Content', de: 'Content'])
        //RefdataValue.loc('CostItemElement', [en: 'Platform', de: 'Platform'])
        //RefdataValue.loc('CostItemElement', [en: 'Other', de: 'Andere'])

        RefdataValue.loc('CostItemElement', [key: 'price: list price', en: 'price: list price', de: 'Preis: Listenpreis'])
        RefdataValue.loc('CostItemElement', [key: 'price: provider price', en: 'price: provider price', de: 'Preis: Anbieterpreis'])
        RefdataValue.loc('CostItemElement', [key: 'price: consortial price', en: 'price: consortial price', de: 'Preis: Konsortialpreis'])
        RefdataValue.loc('CostItemElement', [key: 'price: final price', en: 'price: final price', de: 'Preis: Endpreis'])
        RefdataValue.loc('CostItemElement', [key: 'price: other', en: 'price: other', de: 'Preis: Sonstige'])

        RefdataValue.loc('CostItemElement', [key: 'discount: consortial discount', en: 'discount: consortial discount', de: 'Rabatt: Konsortialrabatt'])
        RefdataValue.loc('CostItemElement', [key: 'discount: alliance licence discount', en: 'discount: alliance licence discount', de: 'Rabatt für Allianzlizenz'])
        RefdataValue.loc('CostItemElement', [key: 'discount: single payment discount', en: 'discount: single payment discount', de: 'Rabatt für eine Rechnung via Konsortium'])
        RefdataValue.loc('CostItemElement', [key: 'discount: multiyear discount', en: 'discount: multiyear discount', de: 'Rabatt für Mehrjahresvertrag'])
        RefdataValue.loc('CostItemElement', [key: 'discount: quantity discount', en: 'discount: quantity discount', de: 'Rabatt: Mengenrabatt'])
        RefdataValue.loc('CostItemElement', [key: 'discount: early pay discount', en: 'discount: early pay discount', de: 'Rabatt: Frühzahlerrabatt'])
        RefdataValue.loc('CostItemElement', [key: 'discount: other', en: 'discount: other', de: 'Rabatt: Sonstige'])

        RefdataValue.loc('CostItemElement', [key: 'refund: currency rate', en: 'refund: currency rate', de: 'Erstattung: Kursgutschrift'])
        RefdataValue.loc('CostItemElement', [key: 'refund: OA', en: 'refund: OA', de: 'Erstattung: Open-Acces-Gutschrift'])
        RefdataValue.loc('CostItemElement', [key: 'refund: retransfer', en: 'refund: retransfer', de: 'Erstattung: Rücküberweisung'])
        RefdataValue.loc('CostItemElement', [key: 'refund: system downtime', en: 'refund: system downtime', de: 'Erstattung: Ersatz für Ausfallzeiten'])
        RefdataValue.loc('CostItemElement', [key: 'refund: other', en: 'refund: other', de: 'Erstattung: Sonstige'])

        RefdataValue.loc('CostItemElement', [key: 'additionalclaim: currency rate', en: 'additionalclaim: currency rate', de: 'Nachforderung aus Kursdifferenz'])
        RefdataValue.loc('CostItemElement', [key: 'additionalclaim: other', en: 'additionalclaim: other', de: 'Nachforderung: Sonstige'])

        RefdataValue.loc('CostItemElement', [key: 'fee: bank charge', en: 'fee: bank charge', de: 'Gebühr: Bankgebühr'])
        RefdataValue.loc('CostItemElement', [key: 'fee: invoicing', en: 'fee: invoicing', de: 'Gebühr: Rechnungsstellungsgebühr'])
        RefdataValue.loc('CostItemElement', [key: 'fee: administration', en: 'fee: administration', de: 'Gebühr: Verwaltungsgebühr'])
        RefdataValue.loc('CostItemElement', [key: 'fee: technical access', en: 'fee: technical access', de: 'Gebühr: Plattformgebühr'])
        RefdataValue.loc('CostItemElement', [key: 'fee: setup', en: 'fee: setup', de: 'Gebühr: SetUp-Gebühr'])
        RefdataValue.loc('CostItemElement', [key: 'fee: other', en: 'fee: other', de: 'Gebühr: Sonstige'])

        RefdataValue.loc('CostItemElement', [key: 'tax: purchase tax 19', en: 'tax: purchase tax 19%', de: 'Steuer: Umsatzsteuer 19%'])
        RefdataValue.loc('CostItemElement', [key: 'tax: purchase tax 7', en: 'tax: purchase tax 7%', de: 'Steuer: Umsatzsteuer 7%'])
        RefdataValue.loc('CostItemElement', [key: 'tax: source tax', en: 'tax:  source tax', de: 'Steuer: Quellensteuer'])

        RefdataCategory.loc('CostItemStatus',
                [en: 'CostItemStatus', de: 'CostItemStatus'])

        RefdataValue.loc('CostItemStatus', [en: 'Estimate', de: 'geschätzt'])
        RefdataValue.loc('CostItemStatus', [en: 'Commitment', de: 'zugesagt'])
        RefdataValue.loc('CostItemStatus', [en: 'Actual', de: 'feststehend'])
        RefdataValue.loc('CostItemStatus', [en: 'Other', de: 'Sonstige'])

        // TODO locCategory
        RefdataValue.loc('Document Context Status',
                [en: 'Deleted', de: 'Gelöscht'])

        RefdataCategory.loc('Document Type',
                [en: 'Document Type', de: 'Dokumenttyp'])

        RefdataValue.loc('Document Type', [en: 'Announcement', de: 'Angekündigung'])
        RefdataValue.loc('Document Type', [en: 'Subscription', de: 'Lizenz'])
        RefdataValue.loc('Document Type', [en: 'License', de: 'Vertrag'])
        RefdataValue.loc('Document Type', [en: 'General', de: 'Allgemein'])
        RefdataValue.loc('Document Type', [en: 'Addendum', de: 'Zusatz'])
        RefdataValue.loc('Document Type', [en: 'Note', de: 'Anmerkung'])
        RefdataValue.loc('Document Type', [en: 'ONIX-PL License', de: 'ONIX-PL Lizenz'])

        RefdataCategory.loc('Entitlement Issue Status',
                [en: 'Entitlement Issue Status', de: 'Entitlement Issue Status'])

        RefdataValue.loc('Entitlement Issue Status', [en: 'Live', de: 'Live'])
        RefdataValue.loc('Entitlement Issue Status', [en: 'Deleted', de: 'Deleted'])

        RefdataCategory.loc('IE Access Status',
                [en: 'IE Access Status', de: 'IE Access Status'])

        RefdataValue.loc('IE Access Status', [en: 'ERROR - No Subscription Start and/or End Date', de: 'ERROR - No Subscription Start and/or End Date'])
        RefdataValue.loc('IE Access Status', [en: 'Current', de: 'Aktuell'])
        RefdataValue.loc('IE Access Status', [en: 'Current(*)', de: 'Aktuell(*)'])
        RefdataValue.loc('IE Access Status', [en: 'Expected', de: 'Erwartet'])
        RefdataValue.loc('IE Access Status', [en: 'Expired', de: 'Abgelaufen'])

        RefdataCategory.loc('IEMedium',
                [en: 'IEMedium', de: 'IEMedium'])

        RefdataValue.loc('IEMedium', [en: 'Print', de: 'Print'])
        RefdataValue.loc('IEMedium', [en: 'Electronic', de: 'Elektronisch'])
        RefdataValue.loc('IEMedium', [en: 'Print and Electronic', de: 'Print und Elektronisch'])

        RefdataCategory.loc('LicenseCategory',
                [en: 'LicenseCategory', de: 'Lizenzkategorie'])

        RefdataValue.loc('LicenseCategory', [en: 'Content', de: 'Content'])
        RefdataValue.loc('LicenseCategory', [en: 'Software', de: 'Software'])
        RefdataValue.loc('LicenseCategory', [en: 'Other', de: 'Andere'])

        RefdataCategory.loc(RefdataCategory.LIC_TYPE,
                [en: 'License Type', de: 'Lizenztyp'])

        RefdataValue.loc(RefdataCategory.LIC_TYPE, [en: 'Actual', de: 'Konkrete Instanz'])
        RefdataValue.loc(RefdataCategory.LIC_TYPE, [en: 'Template', de: 'Vorlage'])
        RefdataValue.loc(RefdataCategory.LIC_TYPE, [en: 'Unknown', de: 'Unbekannt'])

        RefdataCategory.loc('License.OA.Type',
                [en: 'Open Acces Type', de: 'Open-Acces Typ'])

        RefdataValue.loc('License.OA.Type', [en: 'No Open Access', de: 'Kein Open-Access'])
        RefdataValue.loc('License.OA.Type', [en: 'Hybrid', de: 'Hybrid'])
        RefdataValue.loc('License.OA.Type', [en: 'Green Open Access', de: 'Green Open-Access'])
        RefdataValue.loc('License.OA.Type', [en: 'Red Open Access', de: 'Red Open-Access'])

        RefdataCategory.loc('License.OA.eArcVersion',
                [en: 'License.OA.eArcVersion', de: 'License.OA.eArcVersion'])

        RefdataValue.loc('License.OA.eArcVersion', [en: 'Accepted Author'])
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Manuscript (AAM)'])
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Publisher-PDF', de: 'Verlags-PDF'])
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Postprint', de: 'Postprint'])
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Preprint', de: 'Preprint'])
        RefdataValue.loc('License.OA.eArcVersion', [en: 'Preprint with ePrint URL'])

        RefdataCategory.loc(RefdataCategory.LIC_STATUS,
                [en: 'License Status', de: 'Lizenzstatus'])

        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Current', de: 'Aktuell'])
        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Deleted', de: 'Gelöscht'])
        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'In Progress', de:'In Bearbeitung'])
        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Retired', de: 'Abgelaufen'])
        RefdataValue.loc(RefdataCategory.LIC_STATUS, [en: 'Unknown', de: 'Unbekannt'])

        RefdataCategory.loc('PendingChangeStatus',
                [en: 'PendingChangeStatus', de: 'PendingChangeStatus'])

        RefdataValue.loc('PendingChangeStatus', [en: 'Pending', de: 'Ausstehend'])
        RefdataValue.loc('PendingChangeStatus', [en: 'Accepted', de: 'Angenommen'])
        RefdataValue.loc('PendingChangeStatus', [en: 'Rejected', de: 'Abgelehnt'])

        RefdataCategory.loc(RefdataCategory.PKG_LIST_STAT,
                [en: RefdataCategory.PKG_LIST_STAT, de: RefdataCategory.PKG_LIST_STAT])
        RefdataCategory.loc(RefdataCategory.PKG_BREAKABLE,
                [en: RefdataCategory.PKG_BREAKABLE, de: RefdataCategory.PKG_BREAKABLE])
        RefdataCategory.loc(RefdataCategory.PKG_CONSISTENT,
                [en: RefdataCategory.PKG_CONSISTENT, de: RefdataCategory.PKG_CONSISTENT])
        RefdataCategory.loc(RefdataCategory.PKG_FIXED,
                [en: RefdataCategory.PKG_FIXED, de: RefdataCategory.PKG_FIXED])

        RefdataValue.loc(RefdataCategory.PKG_LIST_STAT,  [en: 'Checked', de: 'Überprüft'])
        RefdataValue.loc(RefdataCategory.PKG_LIST_STAT,  [en: 'In Progress', de: 'In Bearbeitung'])
        RefdataValue.loc(RefdataCategory.PKG_LIST_STAT,  [en: 'Unknown', de: 'Unbekannt'])
        RefdataValue.loc(RefdataCategory.PKG_BREAKABLE,  [en: 'No', de: 'Nein'])
        RefdataValue.loc(RefdataCategory.PKG_BREAKABLE,  [en: 'Yes', de: 'Ja'])
        RefdataValue.loc(RefdataCategory.PKG_BREAKABLE,  [en: 'Unknown', de: 'Unbekannt'])
        RefdataValue.loc(RefdataCategory.PKG_CONSISTENT, [en: 'No', de: 'Nein'])
        RefdataValue.loc(RefdataCategory.PKG_CONSISTENT, [en: 'Yes', de: 'Ja'])
        RefdataValue.loc(RefdataCategory.PKG_CONSISTENT, [en: 'Unknown', de: 'Unbekannt'])
        RefdataValue.loc(RefdataCategory.PKG_FIXED,      [en: 'No', de: 'Nein'])
        RefdataValue.loc(RefdataCategory.PKG_FIXED,      [en: 'Yes', de: 'Ja'])
        RefdataValue.loc(RefdataCategory.PKG_FIXED,      [en: 'Unknown', de: 'Unbekannt'])
        RefdataValue.loc(RefdataCategory.PKG_SCOPE,      [en: 'Aggregator', de: 'Aggregator'])
        RefdataValue.loc(RefdataCategory.PKG_SCOPE,      [en: 'Front File', de: 'Front File'])
        RefdataValue.loc(RefdataCategory.PKG_SCOPE,      [en: 'Back File', de: 'Back File'])
        RefdataValue.loc(RefdataCategory.PKG_SCOPE,      [en: 'Master File', de: 'Master File'])
        RefdataValue.loc(RefdataCategory.PKG_SCOPE,      [en: 'Scope Undefined', de: 'Scope Undefined'])
        RefdataValue.loc(RefdataCategory.PKG_SCOPE,      [en: 'Unknown', de: 'Unbekannt'])

        RefdataCategory.loc('TaxType',
                [en: 'TaxType', de: 'TaxType'])

        //RefdataValue.loc('TaxType', [en: 'On Invoice', de: 'Auf Rechnung'])
        //RefdataValue.loc('TaxType', [en: 'Self Declared', de: 'Überweisung'])
        //RefdataValue.loc('TaxType', [en: 'Other', de: 'Andere'])
        RefdataValue.loc('TaxType', [en: 'taxable', de: 'steuerbar'])
        RefdataValue.loc('TaxType', [en: 'not taxable', de: 'nicht steuerbar'])
        RefdataValue.loc('TaxType', [en: 'taxable tax-exempt', de: 'steuerbar steuerbefreit'])
        RefdataValue.loc('TaxType', [en: 'not applicable', de: 'nicht anwendbar'])

        RefdataCategory.loc(RefdataCategory.TI_STATUS,
                [en: RefdataCategory.TI_STATUS, de: RefdataCategory.TI_STATUS])

        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Current', de: 'Aktuell'])
        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Deleted', de: 'Gelöscht'])
        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'In Progress', de:'In Bearbeitung'])
        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Unknown', de: 'Unbekannt'])

        RefdataCategory.loc(RefdataCategory.TI_TYPE,
                [en: RefdataCategory.TI_TYPE, de: RefdataCategory.TI_TYPE])

        RefdataValue.loc(RefdataCategory.TI_TYPE, [en: 'Journal', de: 'Journal'])
        RefdataValue.loc(RefdataCategory.TI_TYPE, [en: 'EBook', de: 'EBook'])
        RefdataValue.loc(RefdataCategory.TI_TYPE, [en: 'Database', de:'Datenbank'])

        RefdataCategory.loc('TitleInstancePackagePlatform.DelayedOA',
                [en: 'TitleInstancePackagePlatform.DelayedOA', de: 'TitleInstancePackagePlatform.DelayedOA'])

        RefdataValue.loc('TitleInstancePackagePlatform.DelayedOA', [en: 'No', de: 'Nein'])
        RefdataValue.loc('TitleInstancePackagePlatform.DelayedOA', [en: 'Unknown', de: 'Unbekannt'])
        RefdataValue.loc('TitleInstancePackagePlatform.DelayedOA', [en: 'Yes', de: 'Ja'])

        RefdataCategory.loc('TitleInstancePackagePlatform.HybridOA',
                [en: 'TitleInstancePackagePlatform.HybridOA', de: 'TitleInstancePackagePlatform.HybridOA'])

        RefdataValue.loc('TitleInstancePackagePlatform.HybridOA', [en: 'No', de: 'Nein'])
        RefdataValue.loc('TitleInstancePackagePlatform.HybridOA', [en: 'Unknown', de: 'Unbekannt'])
        RefdataValue.loc('TitleInstancePackagePlatform.HybridOA', [en: 'Yes', de: 'Ja'])

        RefdataCategory.loc('Tipp.StatusReason',
                [en: 'Tipp.StatusReason', de: 'Tipp.StatusReason'])

        RefdataValue.loc('Tipp.StatusReason', [en: 'Transfer Out', de: 'Transfer Out'])
        RefdataValue.loc('Tipp.StatusReason', [en: 'Transfer In', de: 'Transfer In'])

        RefdataCategory.loc('TitleInstancePackagePlatform.PaymentType',
                [en: 'TitleInstancePackagePlatform.PaymentType', de: 'TitleInstancePackagePlatform.PaymentType'])

        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'Complimentary', de: 'Complimentary'])
        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'Limited Promotion', de: 'Limited Promotion'])
        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'Paid', de: 'Paid'])
        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'OA', de: 'OA'])
        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'Opt Out Promotion', de: 'Opt Out Promotion'])
        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'Uncharged', de: 'Uncharged'])
        RefdataValue.loc('TitleInstancePackagePlatform.PaymentType', [en: 'Unknown', de: 'Unbekannt'])

        RefdataCategory.loc(RefdataCategory.TIPP_STATUS,
                [en: RefdataCategory.TIPP_STATUS, de: RefdataCategory.TIPP_STATUS])

        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Current', de: 'Aktuell'])
        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Expected', de: 'Expected'])
        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Deleted', de: 'Gelöscht'])
        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Transferred', de: 'Transferred'])
        RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Unknown', de: 'Unbekannt'])

        RefdataCategory.loc('TIPP Access Status',
                [en: 'TIPP Access Status', de: 'TIPP Access Status'])

        RefdataValue.loc('TIPP Access Status', [en: 'Current(*)', de: 'Aktuell(*)'])
        RefdataValue.loc('TIPP Access Status', [en: 'Expected', de: 'Erwartet'])
        RefdataValue.loc('TIPP Access Status', [en: 'Expired', de: 'Abgelaufen'])
        RefdataValue.loc('TIPP Access Status', [en: 'Current', de: 'Aktuell'])

        // Controlled values from the <UsageType> element.

        RefdataCategory.loc('UsageStatus',
                [en: 'UsageStatus', de: 'UsageStatus'])

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
        RefdataCategory.lookupOrCreate("ReminderMethod","email")

        RefdataCategory.loc('ReminderUnit',
                [en: 'ReminderUnit', de: 'ReminderUnit'])

        RefdataValue.loc('ReminderUnit', [en: 'Day', de: 'Tag'])
        RefdataValue.loc('ReminderUnit', [en: 'Week', de: 'Woche'])
        RefdataValue.loc('ReminderUnit', [en: 'Month', de: 'Monat'])

        RefdataCategory.lookupOrCreate("ReminderTrigger","Subscription Manual Renewal Date")
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

        RefdataCategory.loc('Currency', [en: 'Currency', de: 'Währung'])
        
        RefdataValue.loc('Currency', [key: 'AED',  en:'AED - United Arab Emirates Dirham', de:'AED - United Arab Emirates Dirham']).save()
        RefdataValue.loc('Currency', [key: 'AFN',  en:'AFN - Afghanistan Afghani', de:'AFN - Afghanistan Afghani']).save()
        RefdataValue.loc('Currency', [key: 'ALL',  en:'ALL - Albania Lek', de:'ALL - Albania Lek']).save()
        RefdataValue.loc('Currency', [key: 'AMD',  en:'AMD - Armenia Dram', de:'AMD - Armenia Dram']).save()
        RefdataValue.loc('Currency', [key: 'ANG',  en:'ANG - Netherlands Antilles Guilder', de:'ANG - Netherlands Antilles Guilder']).save()
        RefdataValue.loc('Currency', [key: 'AOA',  en:'AOA - Angola Kwanza', de:'AOA - Angola Kwanza']).save()
        RefdataValue.loc('Currency', [key: 'ARS',  en:'ARS - Argentina Peso', de:'ARS - Argentina Peso']).save()
        RefdataValue.loc('Currency', [key: 'AUD',  en:'AUD - Australia Dollar', de:'AUD - Australia Dollar']).save()
        RefdataValue.loc('Currency', [key: 'AWG',  en:'AWG - Aruba Guilder', de:'AWG - Aruba Guilder']).save()
        RefdataValue.loc('Currency', [key: 'AZN',  en:'AZN - Azerbaijan New Manat', de:'AZN - Azerbaijan New Manat']).save()
        RefdataValue.loc('Currency', [key: 'BAM',  en:'BAM - Bosnia and Herzegovina Convertible Marka', de:'BAM - Bosnia and Herzegovina Convertible Marka']).save()
        RefdataValue.loc('Currency', [key: 'BBD',  en:'BBD - Barbados Dollar', de:'BBD - Barbados Dollar']).save()
        RefdataValue.loc('Currency', [key: 'BDT',  en:'BDT - Bangladesh Taka', de:'BDT - Bangladesh Taka']).save()
        RefdataValue.loc('Currency', [key: 'BGN',  en:'BGN - Bulgaria Lev', de:'BGN - Bulgaria Lev']).save()
        RefdataValue.loc('Currency', [key: 'BHD',  en:'BHD - Bahrain Dinar', de:'BHD - Bahrain Dinar']).save()
        RefdataValue.loc('Currency', [key: 'BIF',  en:'BIF - Burundi Franc', de:'BIF - Burundi Franc']).save()
        RefdataValue.loc('Currency', [key: 'BMD',  en:'BMD - Bermuda Dollar', de:'BMD - Bermuda Dollar']).save()
        RefdataValue.loc('Currency', [key: 'BND',  en:'BND - Brunei Darussalam Dollar', de:'BND - Brunei Darussalam Dollar']).save()
        RefdataValue.loc('Currency', [key: 'BOB',  en:'BOB - Bolivia Boliviano', de:'BOB - Bolivia Boliviano']).save()
        RefdataValue.loc('Currency', [key: 'BRL',  en:'BRL - Brazil Real', de:'BRL - Brazil Real']).save()
        RefdataValue.loc('Currency', [key: 'BSD',  en:'BSD - Bahamas Dollar', de:'BSD - Bahamas Dollar']).save()
        RefdataValue.loc('Currency', [key: 'BTN',  en:'BTN - Bhutan Ngultrum', de:'BTN - Bhutan Ngultrum']).save()
        RefdataValue.loc('Currency', [key: 'BWP',  en:'BWP - Botswana Pula', de:'BWP - Botswana Pula']).save()
        RefdataValue.loc('Currency', [key: 'BYR',  en:'BYR - Belarus Ruble', de:'BYR - Belarus Ruble']).save()
        RefdataValue.loc('Currency', [key: 'BZD',  en:'BZD - Belize Dollar', de:'BZD - Belize Dollar']).save()
        RefdataValue.loc('Currency', [key: 'CAD',  en:'CAD - Canada Dollar', de:'CAD - Canada Dollar']).save()
        RefdataValue.loc('Currency', [key: 'CDF',  en:'CDF - Congo/Kinshasa Franc', de:'CDF - Congo/Kinshasa Franc']).save()
        RefdataValue.loc('Currency', [key: 'CHF',  en:'CHF - Switzerland Franc', de:'CHF - Switzerland Franc']).save()
        RefdataValue.loc('Currency', [key: 'CLP',  en:'CLP - Chile Peso', de:'CLP - Chile Peso']).save()
        RefdataValue.loc('Currency', [key: 'CNY',  en:'CNY - China Yuan Renminbi', de:'CNY - China Yuan Renminbi']).save()
        RefdataValue.loc('Currency', [key: 'COP',  en:'COP - Colombia Peso', de:'COP - Colombia Peso']).save()
        RefdataValue.loc('Currency', [key: 'CRC',  en:'CRC - Costa Rica Colon', de:'CRC - Costa Rica Colon']).save()
        RefdataValue.loc('Currency', [key: 'CUC',  en:'CUC - Cuba Convertible Peso', de:'CUC - Cuba Convertible Peso']).save()
        RefdataValue.loc('Currency', [key: 'CUP',  en:'CUP - Cuba Peso', de:'CUP - Cuba Peso']).save()
        RefdataValue.loc('Currency', [key: 'CVE',  en:'CVE - Cape Verde Escudo', de:'CVE - Cape Verde Escudo']).save()
        RefdataValue.loc('Currency', [key: 'CZK',  en:'CZK - Czech Republic Koruna', de:'CZK - Czech Republic Koruna']).save()
        RefdataValue.loc('Currency', [key: 'DJF',  en:'DJF - Djibouti Franc', de:'DJF - Djibouti Franc']).save()
        RefdataValue.loc('Currency', [key: 'DKK',  en:'DKK - Denmark Krone', de:'DKK - Denmark Krone']).save()
        RefdataValue.loc('Currency', [key: 'DOP',  en:'DOP - Dominican Republic Peso', de:'DOP - Dominican Republic Peso']).save()
        RefdataValue.loc('Currency', [key: 'DZD',  en:'DZD - Algeria Dinar', de:'DZD - Algeria Dinar']).save()
        RefdataValue.loc('Currency', [key: 'EGP',  en:'EGP - Egypt Pound', de:'EGP - Egypt Pound']).save()
        RefdataValue.loc('Currency', [key: 'ERN',  en:'ERN - Eritrea Nakfa', de:'ERN - Eritrea Nakfa']).save()
        RefdataValue.loc('Currency', [key: 'ETB',  en:'ETB - Ethiopia Birr', de:'ETB - Ethiopia Birr']).save()
        RefdataValue.loc('Currency', [key: 'EUR',  en:'EUR - Euro Member Countries', de:'EUR - Euro Member Countries']).save()
        RefdataValue.loc('Currency', [key: 'FJD',  en:'FJD - Fiji Dollar', de:'FJD - Fiji Dollar']).save()
        RefdataValue.loc('Currency', [key: 'FKP',  en:'FKP - Falkland Islands (Malvinas) Pound', de:'FKP - Falkland Islands (Malvinas) Pound']).save()
        RefdataValue.loc('Currency', [key: 'GBP',  en:'GBP - United Kingdom Pound', de:'GBP - United Kingdom Pound']).save()
        RefdataValue.loc('Currency', [key: 'GEL',  en:'GEL - Georgia Lari', de:'GEL - Georgia Lari']).save()
        RefdataValue.loc('Currency', [key: 'GGP',  en:'GGP - Guernsey Pound', de:'GGP - Guernsey Pound']).save()
        RefdataValue.loc('Currency', [key: 'GHS',  en:'GHS - Ghana Cedi', de:'GHS - Ghana Cedi']).save()
        RefdataValue.loc('Currency', [key: 'GIP',  en:'GIP - Gibraltar Pound', de:'GIP - Gibraltar Pound']).save()
        RefdataValue.loc('Currency', [key: 'GMD',  en:'GMD - Gambia Dalasi', de:'GMD - Gambia Dalasi']).save()
        RefdataValue.loc('Currency', [key: 'GNF',  en:'GNF - Guinea Franc', de:'GNF - Guinea Franc']).save()
        RefdataValue.loc('Currency', [key: 'GTQ',  en:'GTQ - Guatemala Quetzal', de:'GTQ - Guatemala Quetzal']).save()
        RefdataValue.loc('Currency', [key: 'GYD',  en:'GYD - Guyana Dollar', de:'GYD - Guyana Dollar']).save()
        RefdataValue.loc('Currency', [key: 'HKD',  en:'HKD - Hong Kong Dollar', de:'HKD - Hong Kong Dollar']).save()
        RefdataValue.loc('Currency', [key: 'HNL',  en:'HNL - Honduras Lempira', de:'HNL - Honduras Lempira']).save()
        RefdataValue.loc('Currency', [key: 'HRK',  en:'HRK - Croatia Kuna', de:'HRK - Croatia Kuna']).save()
        RefdataValue.loc('Currency', [key: 'HTG',  en:'HTG - Haiti Gourde', de:'HTG - Haiti Gourde']).save()
        RefdataValue.loc('Currency', [key: 'HUF',  en:'HUF - Hungary Forint', de:'HUF - Hungary Forint']).save()
        RefdataValue.loc('Currency', [key: 'IDR',  en:'IDR - Indonesia Rupiah', de:'IDR - Indonesia Rupiah']).save()
        RefdataValue.loc('Currency', [key: 'ILS',  en:'ILS - Israel Shekel', de:'ILS - Israel Shekel']).save()
        RefdataValue.loc('Currency', [key: 'IMP',  en:'IMP - Isle of Man Pound', de:'IMP - Isle of Man Pound']).save()
        RefdataValue.loc('Currency', [key: 'INR',  en:'INR - India Rupee', de:'INR - India Rupee']).save()
        RefdataValue.loc('Currency', [key: 'IQD',  en:'IQD - Iraq Dinar', de:'IQD - Iraq Dinar']).save()
        RefdataValue.loc('Currency', [key: 'IRR',  en:'IRR - Iran Rial', de:'IRR - Iran Rial']).save()
        RefdataValue.loc('Currency', [key: 'ISK',  en:'ISK - Iceland Krona', de:'ISK - Iceland Krona']).save()
        RefdataValue.loc('Currency', [key: 'JEP',  en:'JEP - Jersey Pound', de:'JEP - Jersey Pound']).save()
        RefdataValue.loc('Currency', [key: 'JMD',  en:'JMD - Jamaica Dollar', de:'JMD - Jamaica Dollar']).save()
        RefdataValue.loc('Currency', [key: 'JOD',  en:'JOD - Jordan Dinar', de:'JOD - Jordan Dinar']).save()
        RefdataValue.loc('Currency', [key: 'JPY',  en:'JPY - Japan Yen', de:'JPY - Japan Yen']).save()
        RefdataValue.loc('Currency', [key: 'KES',  en:'KES - Kenya Shilling', de:'KES - Kenya Shilling']).save()
        RefdataValue.loc('Currency', [key: 'KGS',  en:'KGS - Kyrgyzstan Som', de:'KGS - Kyrgyzstan Som']).save()
        RefdataValue.loc('Currency', [key: 'KHR',  en:'KHR - Cambodia Riel', de:'KHR - Cambodia Riel']).save()
        RefdataValue.loc('Currency', [key: 'KMF',  en:'KMF - Comoros Franc', de:'KMF - Comoros Franc']).save()
        RefdataValue.loc('Currency', [key: 'KPW',  en:'KPW - Korea (North) Won', de:'KPW - Korea (North) Won']).save()
        RefdataValue.loc('Currency', [key: 'KRW',  en:'KRW - Korea (South) Won', de:'KRW - Korea (South) Won']).save()
        RefdataValue.loc('Currency', [key: 'KWD',  en:'KWD - Kuwait Dinar', de:'KWD - Kuwait Dinar']).save()
        RefdataValue.loc('Currency', [key: 'KYD',  en:'KYD - Cayman Islands Dollar', de:'KYD - Cayman Islands Dollar']).save()
        RefdataValue.loc('Currency', [key: 'KZT',  en:'KZT - Kazakhstan Tenge', de:'KZT - Kazakhstan Tenge']).save()
        RefdataValue.loc('Currency', [key: 'LAK',  en:'LAK - Laos Kip', de:'LAK - Laos Kip']).save()
        RefdataValue.loc('Currency', [key: 'LBP',  en:'LBP - Lebanon Pound', de:'LBP - Lebanon Pound']).save()
        RefdataValue.loc('Currency', [key: 'LKR',  en:'LKR - Sri Lanka Rupee', de:'LKR - Sri Lanka Rupee']).save()
        RefdataValue.loc('Currency', [key: 'LRD',  en:'LRD - Liberia Dollar', de:'LRD - Liberia Dollar']).save()
        RefdataValue.loc('Currency', [key: 'LSL',  en:'LSL - Lesotho Loti', de:'LSL - Lesotho Loti']).save()
        RefdataValue.loc('Currency', [key: 'LYD',  en:'LYD - Libya Dinar', de:'LYD - Libya Dinar']).save()
        RefdataValue.loc('Currency', [key: 'MAD',  en:'MAD - Morocco Dirham', de:'MAD - Morocco Dirham']).save()
        RefdataValue.loc('Currency', [key: 'MDL',  en:'MDL - Moldova Leu', de:'MDL - Moldova Leu']).save()
        RefdataValue.loc('Currency', [key: 'MGA',  en:'MGA - Madagascar Ariary', de:'MGA - Madagascar Ariary']).save()
        RefdataValue.loc('Currency', [key: 'MKD',  en:'MKD - Macedonia Denar', de:'MKD - Macedonia Denar']).save()
        RefdataValue.loc('Currency', [key: 'MMK',  en:'MMK - Myanmar (Burma) Kyat', de:'MMK - Myanmar (Burma) Kyat']).save()
        RefdataValue.loc('Currency', [key: 'MNT',  en:'MNT - Mongolia Tughrik', de:'MNT - Mongolia Tughrik']).save()
        RefdataValue.loc('Currency', [key: 'MOP',  en:'MOP - Macau Pataca', de:'MOP - Macau Pataca']).save()
        RefdataValue.loc('Currency', [key: 'MRO',  en:'MRO - Mauritania Ouguiya', de:'MRO - Mauritania Ouguiya']).save()
        RefdataValue.loc('Currency', [key: 'MUR',  en:'MUR - Mauritius Rupee', de:'MUR - Mauritius Rupee']).save()
        RefdataValue.loc('Currency', [key: 'MVR',  en:'MVR - Maldives (Maldive Islands) Rufiyaa', de:'MVR - Maldives (Maldive Islands) Rufiyaa']).save()
        RefdataValue.loc('Currency', [key: 'MWK',  en:'MWK - Malawi Kwacha', de:'MWK - Malawi Kwacha']).save()
        RefdataValue.loc('Currency', [key: 'MXN',  en:'MXN - Mexico Peso', de:'MXN - Mexico Peso']).save()
        RefdataValue.loc('Currency', [key: 'MYR',  en:'MYR - Malaysia Ringgit', de:'MYR - Malaysia Ringgit']).save()
        RefdataValue.loc('Currency', [key: 'MZN',  en:'MZN - Mozambique Metical', de:'MZN - Mozambique Metical']).save()
        RefdataValue.loc('Currency', [key: 'NAD',  en:'NAD - Namibia Dollar', de:'NAD - Namibia Dollar']).save()
        RefdataValue.loc('Currency', [key: 'NGN',  en:'NGN - Nigeria Naira', de:'NGN - Nigeria Naira']).save()
        RefdataValue.loc('Currency', [key: 'NIO',  en:'NIO - Nicaragua Cordoba', de:'NIO - Nicaragua Cordoba']).save()
        RefdataValue.loc('Currency', [key: 'NOK',  en:'NOK - Norway Krone', de:'NOK - Norway Krone']).save()
        RefdataValue.loc('Currency', [key: 'NPR',  en:'NPR - Nepal Rupee', de:'NPR - Nepal Rupee']).save()
        RefdataValue.loc('Currency', [key: 'NZD',  en:'NZD - New Zealand Dollar', de:'NZD - New Zealand Dollar']).save()
        RefdataValue.loc('Currency', [key: 'OMR',  en:'OMR - Oman Rial', de:'OMR - Oman Rial']).save()
        RefdataValue.loc('Currency', [key: 'PAB',  en:'PAB - Panama Balboa', de:'PAB - Panama Balboa']).save()
        RefdataValue.loc('Currency', [key: 'PEN',  en:'PEN - Peru Nuevo Sol', de:'PEN - Peru Nuevo Sol']).save()
        RefdataValue.loc('Currency', [key: 'PGK',  en:'PGK - Papua New Guinea Kina', de:'PGK - Papua New Guinea Kina']).save()
        RefdataValue.loc('Currency', [key: 'PHP',  en:'PHP - Philippines Peso', de:'PHP - Philippines Peso']).save()
        RefdataValue.loc('Currency', [key: 'PKR',  en:'PKR - Pakistan Rupee', de:'PKR - Pakistan Rupee']).save()
        RefdataValue.loc('Currency', [key: 'PLN',  en:'PLN - Poland Zloty', de:'PLN - Poland Zloty']).save()
        RefdataValue.loc('Currency', [key: 'PYG',  en:'PYG - Paraguay Guarani', de:'PYG - Paraguay Guarani']).save()
        RefdataValue.loc('Currency', [key: 'QAR',  en:'QAR - Qatar Riyal', de:'QAR - Qatar Riyal']).save()
        RefdataValue.loc('Currency', [key: 'RON',  en:'RON - Romania New Leu', de:'RON - Romania New Leu']).save()
        RefdataValue.loc('Currency', [key: 'RSD',  en:'RSD - Serbia Dinar', de:'RSD - Serbia Dinar']).save()
        RefdataValue.loc('Currency', [key: 'RUB',  en:'RUB - Russia Ruble', de:'RUB - Russia Ruble']).save()
        RefdataValue.loc('Currency', [key: 'RWF',  en:'RWF - Rwanda Franc', de:'RWF - Rwanda Franc']).save()
        RefdataValue.loc('Currency', [key: 'SAR',  en:'SAR - Saudi Arabia Riyal', de:'SAR - Saudi Arabia Riyal']).save()
        RefdataValue.loc('Currency', [key: 'SBD',  en:'SBD - Solomon Islands Dollar', de:'SBD - Solomon Islands Dollar']).save()
        RefdataValue.loc('Currency', [key: 'SCR',  en:'SCR - Seychelles Rupee', de:'SCR - Seychelles Rupee']).save()
        RefdataValue.loc('Currency', [key: 'SDG',  en:'SDG - Sudan Pound', de:'SDG - Sudan Pound']).save()
        RefdataValue.loc('Currency', [key: 'SEK',  en:'SEK - Sweden Krona', de:'SEK - Sweden Krona']).save()
        RefdataValue.loc('Currency', [key: 'SGD',  en:'SGD - Singapore Dollar', de:'SGD - Singapore Dollar']).save()
        RefdataValue.loc('Currency', [key: 'SHP',  en:'SHP - Saint Helena Pound', de:'SHP - Saint Helena Pound']).save()
        RefdataValue.loc('Currency', [key: 'SLL',  en:'SLL - Sierra Leone Leone', de:'SLL - Sierra Leone Leone']).save()
        RefdataValue.loc('Currency', [key: 'SOS',  en:'SOS - Somalia Shilling', de:'SOS - Somalia Shilling']).save()
        RefdataValue.loc('Currency', [key: 'SPL',  en:'SPL - Seborga Luigino', de:'SPL - Seborga Luigino']).save()
        RefdataValue.loc('Currency', [key: 'SRD',  en:'SRD - Suriname Dollar', de:'SRD - Suriname Dollar']).save()
        RefdataValue.loc('Currency', [key: 'STD',  en:'STD - São Tomé and Príncipe Dobra', de:'STD - São Tomé and Príncipe Dobra']).save()
        RefdataValue.loc('Currency', [key: 'SVC',  en:'SVC - El Salvador Colon', de:'SVC - El Salvador Colon']).save()
        RefdataValue.loc('Currency', [key: 'SYP',  en:'SYP - Syria Pound', de:'SYP - Syria Pound']).save()
        RefdataValue.loc('Currency', [key: 'SZL',  en:'SZL - Swaziland Lilangeni', de:'SZL - Swaziland Lilangeni']).save()
        RefdataValue.loc('Currency', [key: 'THB',  en:'THB - Thailand Baht', de:'THB - Thailand Baht']).save()
        RefdataValue.loc('Currency', [key: 'TJS',  en:'TJS - Tajikistan Somoni', de:'TJS - Tajikistan Somoni']).save()
        RefdataValue.loc('Currency', [key: 'TMT',  en:'TMT - Turkmenistan Manat', de:'TMT - Turkmenistan Manat']).save()
        RefdataValue.loc('Currency', [key: 'TND',  en:'TND - Tunisia Dinar', de:'TND - Tunisia Dinar']).save()
        RefdataValue.loc('Currency', [key: 'TOP',  en:'TOP - Tonga Pa\'anga', de:'TOP - Tonga Pa\'anga']).save()
        RefdataValue.loc('Currency', [key: 'TRY',  en:'TRY - Turkey Lira', de:'TRY - Turkey Lira']).save()
        RefdataValue.loc('Currency', [key: 'TTD',  en:'TTD - Trinidad and Tobago Dollar', de:'TTD - Trinidad and Tobago Dollar']).save()
        RefdataValue.loc('Currency', [key: 'TVD',  en:'TVD - Tuvalu Dollar', de:'TVD - Tuvalu Dollar']).save()
        RefdataValue.loc('Currency', [key: 'TWD',  en:'TWD - Taiwan New Dollar', de:'TWD - Taiwan New Dollar']).save()
        RefdataValue.loc('Currency', [key: 'TZS',  en:'TZS - Tanzania Shilling', de:'TZS - Tanzania Shilling']).save()
        RefdataValue.loc('Currency', [key: 'UAH',  en:'UAH - Ukraine Hryvnia', de:'UAH - Ukraine Hryvnia']).save()
        RefdataValue.loc('Currency', [key: 'UGX',  en:'UGX - Uganda Shilling', de:'UGX - Uganda Shilling']).save()
        RefdataValue.loc('Currency', [key: 'USD',  en:'USD - United States Dollar', de:'USD - United States Dollar']).save()
        RefdataValue.loc('Currency', [key: 'UYU',  en:'UYU - Uruguay Peso', de:'UYU - Uruguay Peso']).save()
        RefdataValue.loc('Currency', [key: 'UZS',  en:'UZS - Uzbekistan Som', de:'UZS - Uzbekistan Som']).save()
        RefdataValue.loc('Currency', [key: 'VEF',  en:'VEF - Venezuela Bolivar', de:'VEF - Venezuela Bolivar']).save()
        RefdataValue.loc('Currency', [key: 'VND',  en:'VND - Viet Nam Dong', de:'VND - Viet Nam Dong']).save()
        RefdataValue.loc('Currency', [key: 'VUV',  en:'VUV - Vanuatu Vatu', de:'VUV - Vanuatu Vatu']).save()
        RefdataValue.loc('Currency', [key: 'WST',  en:'WST - Samoa Tala', de:'WST - Samoa Tala']).save()
        RefdataValue.loc('Currency', [key: 'XAF',  en:'XAF - Communauté Financière Africaine (BEAC) CFA Franc BEAC', de:'XAF - Communauté Financière Africaine (BEAC) CFA Franc BEAC']).save()
        RefdataValue.loc('Currency', [key: 'XCD',  en:'XCD - East Caribbean Dollar', de:'XCD - East Caribbean Dollar']).save()
        RefdataValue.loc('Currency', [key: 'XDR',  en:'XDR - International Monetary Fund (IMF) Special Drawing Rights', de:'XDR - International Monetary Fund (IMF) Special Drawing Rights']).save()
        RefdataValue.loc('Currency', [key: 'XOF',  en:'XOF - Communauté Financière Africaine (BCEAO) Franc', de:'XOF - Communauté Financière Africaine (BCEAO) Franc']).save()
        RefdataValue.loc('Currency', [key: 'XPF',  en:'XPF - Comptoirs Français du Pacifique (CFP) Franc', de:'XPF - Comptoirs Français du Pacifique (CFP) Franc']).save()
        RefdataValue.loc('Currency', [key: 'YER',  en:'YER - Yemen Rial', de:'YER - Yemen Rial']).save()
        RefdataValue.loc('Currency', [key: 'ZAR',  en:'ZAR - South Africa Rand', de:'ZAR - South Africa Rand']).save()
        RefdataValue.loc('Currency', [key: 'ZMW',  en:'ZMW - Zambia Kwacha', de:'ZMW - Zambia Kwacha']).save()
        RefdataValue.loc('Currency', [key: 'ZWD',  en:'ZWD - Zimbabwe Dollar', de:'ZWD - Zimbabwe Dollar']).save()
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