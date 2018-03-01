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

        // Permissions
        def edit_permission = Perm.findByCode('edit') ?: new Perm(code: 'edit').save(failOnError: true)
        def view_permission = Perm.findByCode('view') ?: new Perm(code: 'view').save(failOnError: true)

        log.debug("setupRefdata ..")
        setupRefdata()

        // TODO locCategory
        def cl_owner_role       = RefdataValue.loc('Cluster Role',   [en: 'Cluster Owner'])
        def cl_member_role      = RefdataValue.loc('Cluster Role',   [en: 'Cluster Member'])

        def cons_combo          = RefdataValue.loc('Combo Type',     [en: 'Consortium', de: 'Konsortium'])

        def or_licensee_role    = RefdataValue.loc('Organisational Role', [en: 'Licensee', de: 'Lizenznehmer'])
        def or_subscriber_role  = RefdataValue.loc('Organisational Role', [en: 'Subscriber', de: 'Teilnehmer'])
        def or_sc_role          = RefdataValue.loc('Organisational Role', [en: 'Subscription Consortia'])

        // assertPermShare
        OrgPermShare.assertPermShare(view_permission, or_licensee_role)
        OrgPermShare.assertPermShare(edit_permission, or_licensee_role)
        OrgPermShare.assertPermShare(view_permission, or_subscriber_role)
        OrgPermShare.assertPermShare(edit_permission, or_subscriber_role)
        OrgPermShare.assertPermShare(view_permission, or_sc_role)
        OrgPermShare.assertPermShare(edit_permission, or_sc_role)
        // TODO
        OrgPermShare.assertPermShare(view_permission, cl_owner_role)
        OrgPermShare.assertPermShare(edit_permission, cl_owner_role)
        // TODO
        OrgPermShare.assertPermShare(view_permission, cl_member_role)
        OrgPermShare.assertPermShare(edit_permission, cl_member_role)

        OrgPermShare.assertPermShare(view_permission, cons_combo)

        // Global System Roles

        def yodaRole    = Role.findByAuthority('ROLE_YODA')        ?: new Role(authority: 'ROLE_YODA', roleType: 'global').save(failOnError: true)
        def adminRole   = Role.findByAuthority('ROLE_ADMIN')       ?: new Role(authority: 'ROLE_ADMIN', roleType: 'global').save(failOnError: true)
        def dmRole      = Role.findByAuthority('ROLE_DATAMANAGER') ?: new Role(authority: 'ROLE_DATAMANAGER', roleType: 'global').save(failOnError: true)
        def userRole    = Role.findByAuthority('ROLE_USER')        ?: new Role(authority: 'ROLE_USER', roleType: 'global').save(failOnError: true)
        def apiRole     = Role.findByAuthority('ROLE_API')         ?: new Role(authority: 'ROLE_API', roleType: 'global').save(failOnError: true)

        def apiReaderRole      = Role.findByAuthority('ROLE_API_READER')      ?: new Role(authority: 'ROLE_API_READER', roleType: 'global').save(failOnError: true)
        def apiWriterRole      = Role.findByAuthority('ROLE_API_WRITER')      ?: new Role(authority: 'ROLE_API_WRITER', roleType: 'global').save(failOnError: true)
        def apiDataManagerRole = Role.findByAuthority('ROLE_API_DATAMANAGER') ?: new Role(authority: 'ROLE_API_DATAMANAGER', roleType: 'global').save(failOnError: true)

        def packageEditorRole = Role.findByAuthority('ROLE_PACKAGE_EDITOR') ?: new Role(authority: 'ROLE_PACKAGE_EDITOR', roleType: 'global').save(failOnError: true)
        def orgEditorRole     = Role.findByAuthority('ROLE_ORG_EDITOR')     ?: new Role(authority: 'ROLE_ORG_EDITOR', roleType: 'global').save(failOnError: true)

        // Institutional Roles
        def institutionalAdmin = Role.findByAuthority('INST_ADM')
        if (! institutionalAdmin) {
            institutionalAdmin = new Role(authority: 'INST_ADM', roleType: 'user').save(failOnError: true)
        }
        ensurePermGrant(institutionalAdmin, edit_permission)
        ensurePermGrant(institutionalAdmin, view_permission)

        def institutionalUser = Role.findByAuthority('INST_USER')
        if (! institutionalUser) {
            institutionalUser = new Role(authority: 'INST_USER', roleType: 'user').save(failOnError: true)
        }
        ensurePermGrant(institutionalUser, view_permission)

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

        log.debug("createOrganisationProperties ..")
        createOrganisationConfig()

        log.debug("createSubscriptionProperties ..")
        createSubscriptionProperties()

        log.debug("createLicenseProperties ..")
        createLicenseProperties()

        log.debug("createPrivateProperties ..")
        createPrivateProperties()

        log.debug("initializeDefaultSettings ..")
        initializeDefaultSettings()

        log.debug("Init completed ..")
    }

    def destroy = {
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

    def createOrganisationConfig() {
        def allDescr = [en: PropertyDefinition.ORG_CONF, de: PropertyDefinition.ORG_CONF]
        def requiredProps = [
                [name: [en: "API Key", de: "API Key"],                            descr:allDescr, type:String.toString()],
                [name: [en: "statslogin", de: "statslogin"],                      descr:allDescr, type:String.toString()],
        ]
        createPropertyDefinitionsWithI10nTranslations(requiredProps)
    }

    def createSubscriptionProperties() {
        
        def allDescr = [en: PropertyDefinition.SUB_PROP, de: PropertyDefinition.SUB_PROP]
        
        def requiredProps = [
                [name: [en: "GASCO Entry", de: "GASCO-Eintrag"], descr:allDescr, type:RefdataValue.toString(), cat:'YN']

        ]
        createPropertyDefinitionsWithI10nTranslations(requiredProps)
    }
    
    def createLicenseProperties() {

        def allDescr = [en: PropertyDefinition.LIC_PROP, de: PropertyDefinition.LIC_PROP]

        def requiredProps = [
                [name: [en: "Agreement Date", de: "Abschlussdatum"],                            descr:allDescr, type:Date.toString()],
                [name: [en: "Authorized Users", de: "Autorisierte Nutzer"],                     descr:allDescr, type:String.toString()],
                [name: [en: "Alumni Access"],                                                   descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Cancellation Allowance", de: "Außerordentliche Kündigung"],        descr:allDescr, type:String.toString()],
                [name: [en: "Change to licensed material", de: "Änderung am Vertragsgegenstand"], descr:allDescr, type:String.toString()],
                [name: [en: "Concurrent Access", de: "Concurrent Access"],                      descr:allDescr, type:RefdataValue.toString(), cat:'ConcurrentAccess'],
                [name: [en: "Concurrent Users", de: "Concurrent Users"],                        descr:allDescr, type:Integer.toString()],
                [name: [en: "Correction Time", de: "Korrekturfrist bei Vertragsverletzungen"],  descr:allDescr, type:String.toString()],
                [name: [en: "Enterprise Access"],                                               descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "ILL - InterLibraryLoans", de: "Fernleihe"],                        descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Include In Coursepacks", de: "Semesterapparat"],                   descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Include in VLE"],                                                  descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Invoicing", de: "Rechnungsstellung"],                              descr:allDescr, type:Date.toString()],
                [name: [en: "Metadata delivery"],                                               descr:allDescr, type:String.toString()],
                [name: [en: "Method of Authentication", de: "Authentifizierungsverfahren"],     descr:allDescr, type:String.toString()],
                [name: [en: "Multi Site Access"],                                               descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Notice Period"],                                                   descr:allDescr, type:Date.toString()],
                [name: [en: "New Underwriter", de: "Aufnahme neuer Teilnehmer"],                descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Payment target", de: "Zahlungsziel"],                              descr:allDescr, type:Date.toString()],
                [name: [en: "Place of jurisdiction", de: "Gerichtsstand"],                      descr:allDescr, type:String.toString()],
                [name: [en: "Partners Access"],                                                 descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Permitted Uses"],                                                  descr:allDescr, type:String.toString()],
                [name: [en: "Post Cancellation Access Entitlement"],                            descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Remote Access", de: "Remote-Zugriff"],                             descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Regional Restriction", de: "Regionale Einschränkung"],             descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Service regulations", de: "Servicestandards"],                     descr:allDescr, type:String.toString()],
                [name: [en: "Signed"],                                                          descr:allDescr, type:RefdataValue.toString(), cat:'YN'],
                [name: [en: "Usage Statistics", de: "Lieferung von Statistiken"],               descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Walk In Access", de: "Walk-In User"],                              descr:allDescr, type:RefdataValue.toString(), cat:'YNO'],
                [name: [en: "Wifi Access", de: "WLAN-Zugriff"],                                 descr:allDescr, type:RefdataValue.toString(), cat:'YNO']
               
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

    def createPrivateProperties() {

        def allOrgDescr = [en: PropertyDefinition.ORG_PROP, de: PropertyDefinition.ORG_PROP]

        def requiredOrgProps = [
                [name: [en: "Note", de: "Anmerkung"], descr: allOrgDescr, type: String.toString()]
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
            def prop = PropertyDefinition.findByName(default_prop.name['en'])

            if (! prop) {
                log.debug("Unable to locate property definition for ${default_prop.name['en']} .. creating")
                prop = new PropertyDefinition(name: default_prop.name['en'])

                if (default_prop.cat != null) {
                    prop.setRefdataCategory(default_prop.cat)
                }
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
            def pages   = new SitePage(alias: "Pages",   action: "managePages", controller: "spotlight").save()

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
                [[en: 'Licensing Consortium'], lic],
                [[en: 'Negotiator'], lic],
                [[en: 'Subscriber'], sub],
                [[en: 'Provider', de: 'Anbieter'], sub],
                [[en: 'Subscription Agent'], sub],
                [[en: 'Subscription Consortia'], sub],
                [[en: 'Content Provider', de: 'Anbieter'], pkg],
                [[en: 'Package Consortia'], pkg],
                [[en: 'Publisher', de: 'Verlag'], null]
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
        RefdataCategory.loc('AddressType',          	                    [en: 'Address Type', de: 'Art der Adresse'])
        RefdataCategory.loc('Cluster Type',         	                    [en: 'Cluster Type', de: 'Cluster Type'])
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
        RefdataCategory.loc('Person Contact Type',  	                    [en: 'Person: Contact Type', de: 'Person: Typ des Kontakts'])
        RefdataCategory.loc('Person Position',      	                    [en: 'Person Position', de: 'Person Position'])
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
        // refdata values

        RefdataValue.loc('YN',   [en: 'Yes', de: 'Ja'])
        RefdataValue.loc('YN',   [en: 'No', de: 'Nein'])

        RefdataValue.loc('YNO',  [en: 'Yes', de: 'Ja'])
        RefdataValue.loc('YNO',  [en: 'No', de: 'Nein'])
        RefdataValue.loc('YNO',  [en: 'Not applicable', de: 'Nicht zutreffend'])
        RefdataValue.loc('YNO',  [en: 'Unknown', de: 'Unbekannt'])
        RefdataValue.loc('YNO',  [en: 'Other', de: 'Andere'])

        RefdataValue.loc('AddressType', [en: 'Postal address', de: 'Postanschrift'])
        RefdataValue.loc('AddressType', [en: 'Billing address', de: 'Rechnungsanschrift'])
        RefdataValue.loc('AddressType', [en: 'Delivery address', de: 'Lieferanschrift'])

        RefdataValue.loc('ClusterType', [en: 'Undefined'])

        RefdataValue.loc('ConcurrentAccess',     [en: 'Specified', de: 'Festgelegt'])
        RefdataValue.loc('ConcurrentAccess',     [en: 'Not Specified', de: 'Nicht festgelegt'])
        RefdataValue.loc('ConcurrentAccess',     [en: 'No limit', de: 'Ohne Begrenzung'])
        RefdataValue.loc('ConcurrentAccess',     [en: 'Other', de: 'Andere'])

        RefdataValue.loc('ContactContentType',   [en: 'E-Mail', de: 'E-Mail'])
        RefdataValue.loc('ContactContentType',   [en: 'Phone', de: 'Telefon'])
        RefdataValue.loc('ContactContentType',   [en: 'Fax', de: 'Fax'])

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
        RefdataValue.loc('Library Type',   [en: 'Staats-/ Landes- / Regionalbibliothek', de: 'Staats-/ Landes- / Regionalbibliothek'])
        RefdataValue.loc('Library Type',   [en: 'Wissenschafltiche Spezialbibliothek', de: 'Wissenschafltiche Spezialbibliothek'])
        RefdataValue.loc('Library Type',   [en: 'Sonstige', de: 'Sonstige'])

        RefdataValue.loc('OrgSector',    [en: 'Higher Education', de: 'Akademisch'])
        RefdataValue.loc('OrgSector',    [en: 'Publisher', de: 'Verlag'])

        RefdataValue.loc('OrgType',      [en: 'Consortium', de: 'Konsortium'])
        RefdataValue.loc('OrgType',      [en: 'Institution', de: 'Einrichtung'])
        RefdataValue.loc('OrgType',      [en: 'Other', de: 'Andere'])

        RefdataValue.loc('Package Status',                   [en: 'Deleted', de: 'Gelöscht'])
        RefdataValue.loc('Package Status',                   [en: 'Current', de: 'Aktuell'])
        RefdataValue.loc('Package Status',                   [en: 'Retired', de: 'Abgelaufen'])

        RefdataValue.loc('Person Contact Type',      [en: 'Personal contact', de: 'Personenkontakt'])
        RefdataValue.loc('Person Contact Type',      [en: 'Functional contact', de: 'Funktionskontakt'])

        RefdataValue.loc('Person Function',     [en: 'General contact person', de: 'Allgemeine Kontaktperson'])

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

        RefdataValue.loc('CostItemCategory', [en: 'Price', de: 'Preis'])
        RefdataValue.loc('CostItemCategory', [en: 'Bank Charge', de: 'Bank Charge'])
        RefdataValue.loc('CostItemCategory', [en: 'Refund', de: 'Erstattung'])
        RefdataValue.loc('CostItemCategory', [en: 'Other', de: 'Andere'])

        RefdataCategory.loc('CostItemElement',
                [en: 'CostItemElement', de: 'CostItemElement'])

        RefdataValue.loc('CostItemElement', [en: 'Admin Fee', de: 'Admin Fee'])
        RefdataValue.loc('CostItemElement', [en: 'Content', de: 'Content'])
        RefdataValue.loc('CostItemElement', [en: 'Platform', de: 'Platform'])
        RefdataValue.loc('CostItemElement', [en: 'Other', de: 'Andere'])

        RefdataCategory.loc('CostItemStatus',
                [en: 'CostItemStatus', de: 'CostItemStatus'])

        RefdataValue.loc('CostItemStatus', [en: 'Estimate', de: 'Schätzung'])
        RefdataValue.loc('CostItemStatus', [en: 'Commitment', de: 'Commitment'])
        RefdataValue.loc('CostItemStatus', [en: 'Actual', de: 'Actual'])
        RefdataValue.loc('CostItemStatus', [en: 'Other', de: 'Andere'])

        // TODO locCategory
        RefdataValue.loc('Document Context Status',
                [en: 'Deleted', de: 'Gelöscht'])

        RefdataCategory.loc('Document Type',
                [en: 'Document Type', de: 'Dokumenttyp'])

        RefdataValue.loc('Document Type', [en: 'Announcement', de: 'Angekündigung'])
        RefdataValue.loc('Document Type', [en: 'License', de: 'Lizenz'])
        RefdataValue.loc('Document Type', [en: 'Note', de: 'Anmerkung'])
        RefdataValue.loc('Document Type', [en: 'ONIX-PL License', de: 'ONIX-PL Lizenz'])

        RefdataCategory.loc('Entitlement Issue Status',
                [en: 'Entitlement Issue Status', de: 'Entitlement Issue Status'])

        RefdataValue.loc('Entitlement Issue Status', [en: 'Live', de: 'Live'])
        RefdataValue.loc('Entitlement Issue Status', [en: 'Deleted', de: 'Deleted'])

        RefdataCategory.loc('IE Access Status',
                [en: 'IE Access Status', de: 'IE Access Status'])

        RefdataValue.loc('IE Access Status', [en: 'ERROR - No Subscription Start and/or End Date', de: 'ERROR - No Subscription Start and/or End Date'])
        RefdataValue.loc('IE Access Status', [en: 'Current', de: 'Current'])
        RefdataValue.loc('IE Access Status', [en: 'Current(*)', de: 'Current(*)'])
        RefdataValue.loc('IE Access Status', [en: 'Expected', de: 'Expected'])
        RefdataValue.loc('IE Access Status', [en: 'Expired', de: 'Expired'])

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

        RefdataValue.loc(RefdataCategory.LIC_TYPE, [en: 'Actual', de: 'Lokal'])
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

        RefdataCategory.loc('TaxType',
                [en: 'TaxType', de: 'TaxType'])

        RefdataValue.loc('TaxType', [en: 'On Invoice', de: 'Auf Rechnung'])
        RefdataValue.loc('TaxType', [en: 'Self Declared', de: 'Überweisung'])
        RefdataValue.loc('TaxType', [en: 'Other', de: 'Andere'])

        RefdataCategory.loc(RefdataCategory.TI_STATUS,
                [en: RefdataCategory.TI_STATUS, de: RefdataCategory.TI_STATUS])

        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Current', de: 'Aktuell'])
        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Deleted', de: 'Gelöscht'])
        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'In Progress', de:'In Bearbeitung'])
        RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Unknown', de: 'Unknown'])

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

        RefdataCategory.lookupOrCreate('Currency','AED - United Arab Emirates Dirham').save()
        RefdataCategory.lookupOrCreate('Currency','AFN - Afghanistan Afghani').save()
        RefdataCategory.lookupOrCreate('Currency','ALL - Albania Lek').save()
        RefdataCategory.lookupOrCreate('Currency','AMD - Armenia Dram').save()
        RefdataCategory.lookupOrCreate('Currency','ANG - Netherlands Antilles Guilder').save()
        RefdataCategory.lookupOrCreate('Currency','AOA - Angola Kwanza').save()
        RefdataCategory.lookupOrCreate('Currency','ARS - Argentina Peso').save()
        RefdataCategory.lookupOrCreate('Currency','AUD - Australia Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','AWG - Aruba Guilder').save()
        RefdataCategory.lookupOrCreate('Currency','AZN - Azerbaijan New Manat').save()
        RefdataCategory.lookupOrCreate('Currency','BAM - Bosnia and Herzegovina Convertible Marka').save()
        RefdataCategory.lookupOrCreate('Currency','BBD - Barbados Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','BDT - Bangladesh Taka').save()
        RefdataCategory.lookupOrCreate('Currency','BGN - Bulgaria Lev').save()
        RefdataCategory.lookupOrCreate('Currency','BHD - Bahrain Dinar').save()
        RefdataCategory.lookupOrCreate('Currency','BIF - Burundi Franc').save()
        RefdataCategory.lookupOrCreate('Currency','BMD - Bermuda Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','BND - Brunei Darussalam Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','BOB - Bolivia Boliviano').save()
        RefdataCategory.lookupOrCreate('Currency','BRL - Brazil Real').save()
        RefdataCategory.lookupOrCreate('Currency','BSD - Bahamas Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','BTN - Bhutan Ngultrum').save()
        RefdataCategory.lookupOrCreate('Currency','BWP - Botswana Pula').save()
        RefdataCategory.lookupOrCreate('Currency','BYR - Belarus Ruble').save()
        RefdataCategory.lookupOrCreate('Currency','BZD - Belize Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','CAD - Canada Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','CDF - Congo/Kinshasa Franc').save()
        RefdataCategory.lookupOrCreate('Currency','CHF - Switzerland Franc').save()
        RefdataCategory.lookupOrCreate('Currency','CLP - Chile Peso').save()
        RefdataCategory.lookupOrCreate('Currency','CNY - China Yuan Renminbi').save()
        RefdataCategory.lookupOrCreate('Currency','COP - Colombia Peso').save()
        RefdataCategory.lookupOrCreate('Currency','CRC - Costa Rica Colon').save()
        RefdataCategory.lookupOrCreate('Currency','CUC - Cuba Convertible Peso').save()
        RefdataCategory.lookupOrCreate('Currency','CUP - Cuba Peso').save()
        RefdataCategory.lookupOrCreate('Currency','CVE - Cape Verde Escudo').save()
        RefdataCategory.lookupOrCreate('Currency','CZK - Czech Republic Koruna').save()
        RefdataCategory.lookupOrCreate('Currency','DJF - Djibouti Franc').save()
        RefdataCategory.lookupOrCreate('Currency','DKK - Denmark Krone').save()
        RefdataCategory.lookupOrCreate('Currency','DOP - Dominican Republic Peso').save()
        RefdataCategory.lookupOrCreate('Currency','DZD - Algeria Dinar').save()
        RefdataCategory.lookupOrCreate('Currency','EGP - Egypt Pound').save()
        RefdataCategory.lookupOrCreate('Currency','ERN - Eritrea Nakfa').save()
        RefdataCategory.lookupOrCreate('Currency','ETB - Ethiopia Birr').save()
        RefdataCategory.lookupOrCreate('Currency','EUR - Euro Member Countries').save()
        RefdataCategory.lookupOrCreate('Currency','FJD - Fiji Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','FKP - Falkland Islands (Malvinas) Pound').save()
        RefdataCategory.lookupOrCreate('Currency','GBP - United Kingdom Pound').save()
        RefdataCategory.lookupOrCreate('Currency','GEL - Georgia Lari').save()
        RefdataCategory.lookupOrCreate('Currency','GGP - Guernsey Pound').save()
        RefdataCategory.lookupOrCreate('Currency','GHS - Ghana Cedi').save()
        RefdataCategory.lookupOrCreate('Currency','GIP - Gibraltar Pound').save()
        RefdataCategory.lookupOrCreate('Currency','GMD - Gambia Dalasi').save()
        RefdataCategory.lookupOrCreate('Currency','GNF - Guinea Franc').save()
        RefdataCategory.lookupOrCreate('Currency','GTQ - Guatemala Quetzal').save()
        RefdataCategory.lookupOrCreate('Currency','GYD - Guyana Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','HKD - Hong Kong Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','HNL - Honduras Lempira').save()
        RefdataCategory.lookupOrCreate('Currency','HRK - Croatia Kuna').save()
        RefdataCategory.lookupOrCreate('Currency','HTG - Haiti Gourde').save()
        RefdataCategory.lookupOrCreate('Currency','HUF - Hungary Forint').save()
        RefdataCategory.lookupOrCreate('Currency','IDR - Indonesia Rupiah').save()
        RefdataCategory.lookupOrCreate('Currency','ILS - Israel Shekel').save()
        RefdataCategory.lookupOrCreate('Currency','IMP - Isle of Man Pound').save()
        RefdataCategory.lookupOrCreate('Currency','INR - India Rupee').save()
        RefdataCategory.lookupOrCreate('Currency','IQD - Iraq Dinar').save()
        RefdataCategory.lookupOrCreate('Currency','IRR - Iran Rial').save()
        RefdataCategory.lookupOrCreate('Currency','ISK - Iceland Krona').save()
        RefdataCategory.lookupOrCreate('Currency','JEP - Jersey Pound').save()
        RefdataCategory.lookupOrCreate('Currency','JMD - Jamaica Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','JOD - Jordan Dinar').save()
        RefdataCategory.lookupOrCreate('Currency','JPY - Japan Yen').save()
        RefdataCategory.lookupOrCreate('Currency','KES - Kenya Shilling').save()
        RefdataCategory.lookupOrCreate('Currency','KGS - Kyrgyzstan Som').save()
        RefdataCategory.lookupOrCreate('Currency','KHR - Cambodia Riel').save()
        RefdataCategory.lookupOrCreate('Currency','KMF - Comoros Franc').save()
        RefdataCategory.lookupOrCreate('Currency','KPW - Korea (North) Won').save()
        RefdataCategory.lookupOrCreate('Currency','KRW - Korea (South) Won').save()
        RefdataCategory.lookupOrCreate('Currency','KWD - Kuwait Dinar').save()
        RefdataCategory.lookupOrCreate('Currency','KYD - Cayman Islands Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','KZT - Kazakhstan Tenge').save()
        RefdataCategory.lookupOrCreate('Currency','LAK - Laos Kip').save()
        RefdataCategory.lookupOrCreate('Currency','LBP - Lebanon Pound').save()
        RefdataCategory.lookupOrCreate('Currency','LKR - Sri Lanka Rupee').save()
        RefdataCategory.lookupOrCreate('Currency','LRD - Liberia Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','LSL - Lesotho Loti').save()
        RefdataCategory.lookupOrCreate('Currency','LYD - Libya Dinar').save()
        RefdataCategory.lookupOrCreate('Currency','MAD - Morocco Dirham').save()
        RefdataCategory.lookupOrCreate('Currency','MDL - Moldova Leu').save()
        RefdataCategory.lookupOrCreate('Currency','MGA - Madagascar Ariary').save()
        RefdataCategory.lookupOrCreate('Currency','MKD - Macedonia Denar').save()
        RefdataCategory.lookupOrCreate('Currency','MMK - Myanmar (Burma) Kyat').save()
        RefdataCategory.lookupOrCreate('Currency','MNT - Mongolia Tughrik').save()
        RefdataCategory.lookupOrCreate('Currency','MOP - Macau Pataca').save()
        RefdataCategory.lookupOrCreate('Currency','MRO - Mauritania Ouguiya').save()
        RefdataCategory.lookupOrCreate('Currency','MUR - Mauritius Rupee').save()
        RefdataCategory.lookupOrCreate('Currency','MVR - Maldives (Maldive Islands) Rufiyaa').save()
        RefdataCategory.lookupOrCreate('Currency','MWK - Malawi Kwacha').save()
        RefdataCategory.lookupOrCreate('Currency','MXN - Mexico Peso').save()
        RefdataCategory.lookupOrCreate('Currency','MYR - Malaysia Ringgit').save()
        RefdataCategory.lookupOrCreate('Currency','MZN - Mozambique Metical').save()
        RefdataCategory.lookupOrCreate('Currency','NAD - Namibia Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','NGN - Nigeria Naira').save()
        RefdataCategory.lookupOrCreate('Currency','NIO - Nicaragua Cordoba').save()
        RefdataCategory.lookupOrCreate('Currency','NOK - Norway Krone').save()
        RefdataCategory.lookupOrCreate('Currency','NPR - Nepal Rupee').save()
        RefdataCategory.lookupOrCreate('Currency','NZD - New Zealand Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','OMR - Oman Rial').save()
        RefdataCategory.lookupOrCreate('Currency','PAB - Panama Balboa').save()
        RefdataCategory.lookupOrCreate('Currency','PEN - Peru Nuevo Sol').save()
        RefdataCategory.lookupOrCreate('Currency','PGK - Papua New Guinea Kina').save()
        RefdataCategory.lookupOrCreate('Currency','PHP - Philippines Peso').save()
        RefdataCategory.lookupOrCreate('Currency','PKR - Pakistan Rupee').save()
        RefdataCategory.lookupOrCreate('Currency','PLN - Poland Zloty').save()
        RefdataCategory.lookupOrCreate('Currency','PYG - Paraguay Guarani').save()
        RefdataCategory.lookupOrCreate('Currency','QAR - Qatar Riyal').save()
        RefdataCategory.lookupOrCreate('Currency','RON - Romania New Leu').save()
        RefdataCategory.lookupOrCreate('Currency','RSD - Serbia Dinar').save()
        RefdataCategory.lookupOrCreate('Currency','RUB - Russia Ruble').save()
        RefdataCategory.lookupOrCreate('Currency','RWF - Rwanda Franc').save()
        RefdataCategory.lookupOrCreate('Currency','SAR - Saudi Arabia Riyal').save()
        RefdataCategory.lookupOrCreate('Currency','SBD - Solomon Islands Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','SCR - Seychelles Rupee').save()
        RefdataCategory.lookupOrCreate('Currency','SDG - Sudan Pound').save()
        RefdataCategory.lookupOrCreate('Currency','SEK - Sweden Krona').save()
        RefdataCategory.lookupOrCreate('Currency','SGD - Singapore Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','SHP - Saint Helena Pound').save()
        RefdataCategory.lookupOrCreate('Currency','SLL - Sierra Leone Leone').save()
        RefdataCategory.lookupOrCreate('Currency','SOS - Somalia Shilling').save()
        RefdataCategory.lookupOrCreate('Currency','SPL* - Seborga Luigino').save()
        RefdataCategory.lookupOrCreate('Currency','SRD - Suriname Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','STD - São Tomé and Príncipe Dobra').save()
        RefdataCategory.lookupOrCreate('Currency','SVC - El Salvador Colon').save()
        RefdataCategory.lookupOrCreate('Currency','SYP - Syria Pound').save()
        RefdataCategory.lookupOrCreate('Currency','SZL - Swaziland Lilangeni').save()
        RefdataCategory.lookupOrCreate('Currency','THB - Thailand Baht').save()
        RefdataCategory.lookupOrCreate('Currency','TJS - Tajikistan Somoni').save()
        RefdataCategory.lookupOrCreate('Currency','TMT - Turkmenistan Manat').save()
        RefdataCategory.lookupOrCreate('Currency','TND - Tunisia Dinar').save()
        RefdataCategory.lookupOrCreate('Currency','TOP - Tonga Pa\'anga').save()
        RefdataCategory.lookupOrCreate('Currency','TRY - Turkey Lira').save()
        RefdataCategory.lookupOrCreate('Currency','TTD - Trinidad and Tobago Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','TVD - Tuvalu Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','TWD - Taiwan New Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','TZS - Tanzania Shilling').save()
        RefdataCategory.lookupOrCreate('Currency','UAH - Ukraine Hryvnia').save()
        RefdataCategory.lookupOrCreate('Currency','UGX - Uganda Shilling').save()
        RefdataCategory.lookupOrCreate('Currency','USD - United States Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','UYU - Uruguay Peso').save()
        RefdataCategory.lookupOrCreate('Currency','UZS - Uzbekistan Som').save()
        RefdataCategory.lookupOrCreate('Currency','VEF - Venezuela Bolivar').save()
        RefdataCategory.lookupOrCreate('Currency','VND - Viet Nam Dong').save()
        RefdataCategory.lookupOrCreate('Currency','VUV - Vanuatu Vatu').save()
        RefdataCategory.lookupOrCreate('Currency','WST - Samoa Tala').save()
        RefdataCategory.lookupOrCreate('Currency','XAF - Communauté Financière Africaine (BEAC) CFA Franc BEAC').save()
        RefdataCategory.lookupOrCreate('Currency','XCD - East Caribbean Dollar').save()
        RefdataCategory.lookupOrCreate('Currency','XDR - International Monetary Fund (IMF) Special Drawing Rights').save()
        RefdataCategory.lookupOrCreate('Currency','XOF - Communauté Financière Africaine (BCEAO) Franc').save()
        RefdataCategory.lookupOrCreate('Currency','XPF - Comptoirs Français du Pacifique (CFP) Franc').save()
        RefdataCategory.lookupOrCreate('Currency','YER - Yemen Rial').save()
        RefdataCategory.lookupOrCreate('Currency','ZAR - South Africa Rand').save()
        RefdataCategory.lookupOrCreate('Currency','ZMW - Zambia Kwacha').save()
        RefdataCategory.lookupOrCreate('Currency','ZWD - Zimbabwe Dollar').save()

    }

}
