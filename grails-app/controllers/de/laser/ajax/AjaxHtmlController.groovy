package de.laser.ajax

import com.k_int.kbplus.GenericOIDService
import de.laser.AccessService
import de.laser.AddressbookService
import de.laser.ContextService
import de.laser.FinanceService
import de.laser.GokbService
import de.laser.License
import de.laser.LinksGenerationService
import de.laser.Org
import de.laser.OrgRole
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.Address
import de.laser.Doc
import de.laser.Person
import de.laser.PersonRole
import de.laser.SubscriptionService
import de.laser.Task
import de.laser.TaskService
import de.laser.TitleInstancePackagePlatform
import de.laser.annotations.DebugAnnotation
import de.laser.auth.User
import de.laser.ctrl.FinanceControllerService
import de.laser.ctrl.LicenseControllerService
import de.laser.exporting.AbstractExport
import de.laser.exporting.GenericExportManager
import de.laser.finance.CostItem
import de.laser.helper.DateUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.helper.SessionCacheWrapper
import de.laser.reporting.myInstitution.CostItemConfig
import de.laser.reporting.myInstitution.base.BaseConfig
import de.laser.reporting.myInstitution.base.BaseQuery
import de.laser.reporting.myInstitution.LicenseConfig
import de.laser.reporting.myInstitution.OrganisationConfig
import de.laser.reporting.myInstitution.SubscriptionConfig
import de.laser.reporting.subscription.SubscriptionReporting
import grails.plugin.springsecurity.annotation.Secured
import grails.web.servlet.mvc.GrailsParameterMap

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat

@Secured(['IS_AUTHENTICATED_FULLY'])
class AjaxHtmlController {

    /**
     * only template rendering here ..
     * no object manipulation
     *
     */

    AddressbookService addressbookService
    ContextService contextService
    FinanceService financeService
    FinanceControllerService financeControllerService
    GenericOIDService genericOIDService
    TaskService taskService
    LinksGenerationService linksGenerationService
    AccessService accessService
    GokbService gokbService
    SubscriptionService subscriptionService
    LicenseControllerService licenseControllerService

    @Secured(['ROLE_USER'])
    def test() {
        String result = '<p data-status="ok">OK'
        if (params.id) {
            result += ', ID:' + params.id
        }
        result += '</p>'
        render result
    }

    @Deprecated
    @Secured(['ROLE_USER'])
    def loadGeneralFilter() {
        Map<String,Object> result = [entry:params.entry,queried:params.queried]
        render view: '/reporting/_displayConfigurations', model: result
    }

    //-------------------------------------------------- subscription/show ---------------------------------------------

    @Secured(['ROLE_USER'])
    def getLinks() {
        Map<String,Object> result = [user:contextService.getUser(),contextOrg:contextService.getOrg(),subscriptionLicenseLink:params.subscriptionLicenseLink]
        def entry = genericOIDService.resolveOID(params.entry)
        result.entry = entry
        result.editable = entry.isEditableBy(result.user)
        if(entry instanceof Subscription) {
            result.subscription = (Subscription) entry
            result.atConsortialParent = result.contextOrg.id == result.subscription.getConsortia()?.id ? "true" : "false"
        }
        else if(entry instanceof License) {
            result.license = (License) entry
            result.atConsortialParent = result.contextOrg == result.license.getLicensingConsortium() ? "true" : "false"
        }
        List<RefdataValue> linkTypes = RefdataCategory.getAllRefdataValues(RDConstants.LINK_TYPE)
        if(result.subscriptionLicenseLink) {
            linkTypes.removeIf({ RefdataValue rdv -> (rdv != RDStore.LINKTYPE_LICENSE) })
        }
        else linkTypes.remove(RDStore.LINKTYPE_LICENSE)
        result.links = linksGenerationService.getSourcesAndDestinations(entry, result.user, linkTypes)
        render template: '/templates/links/linksListing', model: result
    }

    @Secured(['ROLE_USER'])
    def getPackageData() {
        Map<String,Object> result = [subscription:Subscription.get(params.subscription), curatoryGroups: []], packageMetadata
        Org contextOrg = contextService.getOrg()
        result.contextCustomerType = contextOrg.getCustomerType()
        result.roleLinks = result.subscription.orgRelations.findAll { OrgRole oo -> !(oo.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA]) }
        result.roleObject = result.subscription
        result.roleRespValue = 'Specific subscription editor'
        result.editmode = result.subscription.isEditableBy(contextService.getUser())
        result.accessConfigEditable = accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') && result.subscription.getSubscriber().id == contextOrg.id)
        render template: '/subscription/packages', model: result
    }

    @Secured(['ROLE_USER'])
    def getProperties() {
        Org contextOrg = contextService.getOrg()
        User user = contextService.getUser()
        if(params.subscription) {
            Subscription subscription = Subscription.get(params.subscription)
            render template: "/subscription/properties", model: [subscription: subscription,
                                                                 showConsortiaFunctions: subscriptionService.showConsortiaFunctions(contextOrg, subscription),
                                                                 contextOrg: contextOrg,
                                                                 editable: subscription.isEditableBy(user)]
        }
        else if(params.license) {
            License license = License.get(params.license)
            render template: "/license/properties", model: [license: license,
                                                            showConsortiaFunctions: licenseControllerService.showConsortiaFunctions(license),
                                                            contextOrg: contextOrg,
                                                            institution: contextOrg,
                                                            editable: license.isEditableBy(user)]
        }
    }

    @Secured(['ROLE_USER'])
    def editNote() {
        Map<String, Object> result = [:]
        result.params = params
        result.noteInstance = Doc.get(params.id)

        render template: "/templates/notes/modal_edit", model: result
    }

    @Secured(['ROLE_USER'])
    def readNote() {
        Map<String, Object> result = [:]
        result.params = params
        result.noteInstance = Doc.get(params.id)

        render template: "/templates/notes/modal_read", model: result
    }

    @Secured(['ROLE_USER'])
    def createTask() {
        long backendStart = System.currentTimeMillis()
        Org contextOrg = contextService.getOrg()
        Map<String, Object> result = taskService.getPreconditions(contextOrg)

        result.backendStart = backendStart

        render template: "/templates/tasks/modal_create", model: result
    }

    @Secured(['ROLE_USER'])
    def editTask() {
        Org contextOrg = contextService.getOrg()
        Map<String, Object> result = taskService.getPreconditionsWithoutTargets(contextOrg)

        result.params = params
        result.taskInstance = Task.get(params.id)

        if (result.taskInstance){
            render template: "/templates/tasks/modal_edit", model: result
//        } else {
//            flash.error = "Diese Aufgabe existiert nicht (mehr)."
//            redirect(url: request.getHeader('referer'))
        }
    }

    @Secured(['ROLE_USER'])
    def createAddress() {
        Map<String, Object> model = [:]
        model.orgId = params.orgId
        model.prsId = params.prsId
        model.redirect = params.redirect
        model.typeId = params.typeId ? Long.valueOf(params.typeId) : null
        model.hideType = params.hideType

        if (model.orgId && model.typeId) {
            String messageCode = 'addressFormModalLibraryAddress'
            if (model.typeId == RDStore.ADRESS_TYPE_LEGAL_PATRON.id)  {
                messageCode = 'addressFormModalLegalPatronAddress'
            }
            else if (model.typeId == RDStore.ADRESS_TYPE_BILLING.id)  {
                messageCode = 'addressFormModalBillingAddress'
            }
            else if (model.typeId == RDStore.ADRESS_TYPE_POSTAL.id)   {
                messageCode = 'addressFormModalPostalAddress'
            }
            else if (model.typeId == RDStore.ADRESS_TYPE_DELIVERY.id) {
                messageCode = 'addressFormModalDeliveryAddress'
            }
            else if (model.typeId == RDStore.ADRESS_TYPE_LIBRARY.id)  {
                messageCode = 'addressFormModalLibraryAddress'
            }

            model.modalText = message(code: 'default.create.label', args: [message(code: messageCode)])
        } else {
            model.modalText = message(code: 'default.new.label', args: [message(code: 'person.address.label')])
        }
        model.modalMsgSave = message(code: 'default.button.create.label')
        model.url = [controller: 'address', action: 'create']

        render template: "/templates/cpa/addressFormModal", model: model
    }

    @Secured(['ROLE_USER'])
    def editAddress() {
        Map<String, Object> model = [:]
        model.addressInstance = Address.get(params.id)

        if (model.addressInstance){
            model.modalId = 'addressFormModal'
            String messageCode = 'person.address.label'
            model.typeId = model.addressInstance.type.id
            if(model.addressInstance.prs) {
                model.modalText = message(code: 'default.edit.label', args: [message(code: messageCode)]) + ' (' + model.addressInstance.prs.toString() + ')'
            }
            else if(model.addressInstance.org) {
                model.modalText = message(code: 'default.edit.label', args: [message(code: messageCode)]) + ' (' + model.addressInstance.org.toString() + ')'
            }
            else{
                model.modalText = message(code: 'default.edit.label', args: [message(code: messageCode)])
            }
            model.modalMsgSave = message(code: 'default.button.save_changes')
            model.url = [controller: 'address', action: 'edit']

            render template: "/templates/cpa/addressFormModal", model: model
        }
    }

    @Secured(['ROLE_USER'])
    def createPerson() {
        Map<String, Object> result = [:]
        result.contextOrg = contextService.getOrg()
        result.tenant = result.contextOrg
        result.modalId = 'personModal'
        result.presetFunctionType = RDStore.PRS_FUNC_GENERAL_CONTACT_PRS
        result.showContacts = params.showContacts == "true" ? true : ''
        result.addContacts = params.showContacts == "true" ? true : ''
        result.org = params.org ? Org.get(Long.parseLong(params.org)) : null
        result.functions = [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, RDStore.PRS_FUNC_CONTACT_PRS, RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN]
        if(result.contextOrg.getCustomerType() == 'ORG_CONSORTIUM'){
            result.functions << RDStore.PRS_FUNC_GASCO_CONTACT
        }
        result.positions = PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION) - [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS]

        switch(params.contactFor) {
            case 'contactPersonForInstitution':
                result.isPublic = false
                if (result.org) {
                    result.modalText = message(code: "person.create_new.contactPersonForInstitution.label") + ' (' + result.org.toString() + ')'
                } else {
                    result.modalText = message(code: "person.create_new.contactPersonForInstitution.label")
                    result.orgList = Org.executeQuery("from Org o where exists (select roletype from o.orgType as roletype where roletype.id = :orgType ) and o.sector.id = :orgSector order by LOWER(o.sortname)", [orgSector: RDStore.O_SECTOR_HIGHER_EDU.id, orgType: RDStore.OT_INSTITUTION.id])
                }
                break
            case 'contactPersonForProviderAgency':
                result.isPublic    = false
                result.functions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FUNC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FUNC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FUNC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS, RDStore.PRS_FUNC_FUNC_DELIVERY_ADDRESS]
                result.positions = [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_DIREKTION, RDStore.PRS_POS_DIREKTION_ASS, RDStore.PRS_POS_RB, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS, RDStore.PRS_POS_TS]
                if (result.org) {
                    result.modalText = message(code: "person.create_new.contactPersonForProviderAgency.label") + ' (' + result.org.toString() + ')'
                } else {
                    result.modalText = message(code: "person.create_new.contactPersonForProviderAgency.label")
                    result.orgList = Org.executeQuery("from Org o where exists (select roletype from o.orgType as roletype where roletype.id in (:orgType) ) and o.sector.id = :orgSector order by LOWER(o.sortname)", [orgSector: RDStore.O_SECTOR_PUBLISHER.id, orgType: [RDStore.OT_PROVIDER.id, RDStore.OT_AGENCY.id]])
                }
                break
            case 'contactPersonForProviderAgencyPublic':
                result.contactPersonForProviderAgencyPublic = true
                result.isPublic    = true
                result.presetFunctionType = RDStore.PRS_FUNC_TECHNICAL_SUPPORT
                //result.functions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FUNC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FUNC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FUNC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS, RDStore.PRS_FUNC_FUNC_DELIVERY_ADDRESS]
                //result.positions = [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_DIREKTION, RDStore.PRS_POS_DIREKTION_ASS, RDStore.PRS_POS_RB, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS, RDStore.PRS_POS_TS]
                if(result.org){
                    result.modalText = message(code: "person.create_new.contactPersonForProviderAgency.label") + ' (' + result.org.toString() + ')'
                    result.tenant = result.org
                }else {
                    result.modalText = message(code: "person.create_new.contactPersonForProviderAgency.label")
                    result.orgList = result.orgList = Org.executeQuery("from Org o where exists (select roletype from o.orgType as roletype where roletype.id in (:orgType) ) and o.sector.id = :orgSector order by LOWER(o.sortname)", [orgSector: RDStore.O_SECTOR_PUBLISHER.id, orgType: [RDStore.OT_PROVIDER.id, RDStore.OT_AGENCY.id]])
                }

                break
            case 'contactPersonForPublic':
                result.isPublic    = true
                result.modalText = message(code: "person.create_new.contactPersonForPublic.label")
                break
        }
        result.url = [controller: 'person', action: 'create']


        render template: "/templates/cpa/personFormModal", model: result
    }

    @Secured(['ROLE_USER'])
    def editPerson() {
        Map<String, Object> result = [:]
        Org contextOrg = contextService.getOrg()
        result.personInstance = Person.get(params.id)

        if (result.personInstance){
            result.org = result.personInstance.getBelongsToOrg()
            result.functions = [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, RDStore.PRS_FUNC_CONTACT_PRS, RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN]
            if(contextOrg.getCustomerType() == 'ORG_CONSORTIUM'){
                result.functions << RDStore.PRS_FUNC_GASCO_CONTACT
            }
            result.positions = PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION) - [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS]

            if (result.org || (params.org && params.org instanceof String)) {
                result.org = params.org ? Org.get(Long.parseLong(params.org)) : result.org
                List allOrgTypeIds =result.org.getAllOrgTypeIds()
                if(RDStore.OT_PROVIDER.id in allOrgTypeIds || RDStore.OT_AGENCY.id in allOrgTypeIds){
                    result.functions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FUNC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FUNC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FUNC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS, RDStore.PRS_FUNC_FUNC_DELIVERY_ADDRESS]
                    result.positions = [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_DIREKTION, RDStore.PRS_POS_DIREKTION_ASS, RDStore.PRS_POS_RB, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS, RDStore.PRS_POS_TS]
                    result.modalText = message(code: 'default.edit.label', args: [message(code: "person.contactPersonForProviderAgency.label")]) + ' (' + result.org.toString() + ')'
                    result.contactPersonForProviderAgencyPublic = result.personInstance.isPublic
                }else{
                    result.modalText = message(code: 'default.edit.label', args: [message(code: "person.contactPersonForInstitution.label")]) + ' (' + result.org.toString() + ')'
                }
            }else {
                result.modalText = message(code: 'default.edit.label', args: [message(code: 'person.label')])
            }

            result.modalId = 'personModal'
            result.modalMsgSave = message(code: 'default.button.save_changes')
            result.showContacts = params.showContacts == "true" ? true : ''
            result.addContacts = params.showContacts == "true" ? true : ''
            result.showAddresses = params.showAddresses == "true" ? true : ''
            result.addAddresses = params.showAddresses == "true" ? true : ''
            result.isPublic = result.personInstance.isPublic
            result.editable = addressbookService.isPersonEditable(result.personInstance, contextService.getUser())
            result.tmplShowDeleteButton = result.editable
            result.url = [controller: 'person', action: 'edit', id: result.personInstance.id]
            result.contextOrg = contextService.getOrg()

            render template: "/templates/cpa/personFormModal", model: result
        }
    }

    @Secured(['ROLE_USER'])
    def contactFields() {
        render template: "/templates/cpa/contactFields"
    }

    @Secured(['ROLE_USER'])
    def addressFields() {
        render template: "/templates/cpa/addressFields", model: [multipleAddresses: params.multipleAddresses]
    }

    @Secured(['ROLE_USER'])
    def getLicensePropertiesForSubscription() {
        License loadFor = License.get(params.loadFor)
        if (loadFor) {
            Map<String, Object> derivedPropDefGroups = loadFor.getCalculatedPropDefGroups(contextService.org)
            render view: '/subscription/_licProp', model: [license: loadFor, derivedPropDefGroups: derivedPropDefGroups, linkId: params.linkId]
        }
    }

    // ----- reporting -----

    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_USER")
    })
    def chartDetails() {
        Map<String, Object> result = [
            query:  params.query,
            id:     params.id ? params.id as Long : ''
        ]

        if (params.context == BaseConfig.KEY && params.query) {
//            SessionCacheWrapper sessionCache = contextService.getSessionCache()
//            Map<String, Object> cached = sessionCache.get("MyInstitutionController/reporting/" + params.token)
//
//            println cached
//            GrailsParameterMap clone = params.clone() as GrailsParameterMap// TODO: simplify
//            if (cached) {
//                clone.putAll(cached)
//            }

            String prefix = params.query.split('-')[0]
            List idList = params.list('idList[]').collect { it as Long }

            if (prefix in ['license']) {
                result.labels = BaseQuery.getQueryLabels(LicenseConfig.CONFIG, params)
                result.list   = License.executeQuery('select l from License l where l.id in (:idList) order by l.sortableReference, l.reference', [idList: idList])
                result.tmpl   = '/myInstitution/reporting/details/license'
            }
            else if (prefix in ['licensor']) {
                result.labels = BaseQuery.getQueryLabels(LicenseConfig.CONFIG, params)
                result.list   = Org.executeQuery('select o from Org o where o.id in (:idList) order by o.sortname, o.name', [idList: idList])
                result.tmpl   = '/myInstitution/reporting/details/organisation'
            }
            else if (prefix in ['org']) {
                result.labels = BaseQuery.getQueryLabels(OrganisationConfig.CONFIG, params)
                result.list   = Org.executeQuery('select o from Org o where o.id in (:idList) order by o.sortname, o.name', [idList: idList])
                result.tmpl   = '/myInstitution/reporting/details/organisation'
            }
            else if (prefix in ['subscription']) {
                result.labels = BaseQuery.getQueryLabels(SubscriptionConfig.CONFIG, params)
                result.list   = Subscription.executeQuery('select s from Subscription s where s.id in (:idList) order by s.name', [idList: idList])
                result.tmpl   = '/myInstitution/reporting/details/subscription'
            }
            else if (prefix in ['member', 'provider']) {
                result.labels = BaseQuery.getQueryLabels(SubscriptionConfig.CONFIG, params)
                result.list   = Org.executeQuery('select o from Org o where o.id in (:idList) order by o.sortname, o.name', [idList: idList])
                result.tmpl   = '/myInstitution/reporting/details/organisation'
            }
            else if (prefix in ['costItem']) {
                result.labels = BaseQuery.getQueryLabels(CostItemConfig.CONFIG, params)
                result.list   = CostItem.executeQuery('select ci from CostItem ci where ci.id in (:idList) order by ci.costTitle', [idList: idList])
                result.tmpl   = '/myInstitution/reporting/details/costItem'
            }
        }
        else if (params.context == SubscriptionConfig.KEY && params.query) {
            if (params.query == 'timeline-cost') {
                result.labels = SubscriptionReporting.getTimelineQueryLabels(params)

                GrailsParameterMap clone = params.clone() as GrailsParameterMap
                clone.setProperty('id', params.id)
                Map<String, Object> finance = financeService.getCostItemsForSubscription(clone, financeControllerService.getResultGenerics(clone))

                result.billingSums = finance.cons.sums.billingSums ?: []
                result.localSums   = finance.cons.sums.localSums ?: []
                result.tmpl        = '/subscription/reporting/details/timeline/cost'
            }
            else if (params.query in ['timeline-entitlement', 'timeline-member']) {
                result.labels = SubscriptionReporting.getTimelineQueryLabels(params)

                List idList      = params.list('idList[]').collect { it as Long }
                List plusIdList  = params.list('plusIdList[]').collect { it as Long }
                List minusIdList = params.list('minusIdList[]').collect { it as Long }

                if (params.query == 'timeline-entitlement') {
                    String hql = 'select tipp from TitleInstancePackagePlatform tipp where tipp.id in (:idList) order by tipp.sortName, tipp.name'

                    result.list      = idList      ? TitleInstancePackagePlatform.executeQuery( hql, [idList: idList] ) : []
                    result.plusList  = plusIdList  ? TitleInstancePackagePlatform.executeQuery( hql, [idList: plusIdList] ) : []
                    result.minusList = minusIdList ? TitleInstancePackagePlatform.executeQuery( hql, [idList: minusIdList] ) : []
                    result.tmpl      = '/subscription/reporting/details/timeline/entitlement'
                }
                else {
                    String hql = 'select o from Org o where o.id in (:idList) order by o.sortname, o.name'

                    result.list      = idList      ? Org.executeQuery( hql, [idList: idList] ) : []
                    result.plusList  = plusIdList  ? Org.executeQuery( hql, [idList: plusIdList] ) : []
                    result.minusList = minusIdList ? Org.executeQuery( hql, [idList: minusIdList] ) : []
                    result.tmpl      = '/subscription/reporting/details/timeline/organisation'
                }
            }
            else {
                List idList = params.list('idList[]').collect { it as Long }

                result.labels = BaseQuery.getQueryLabels(SubscriptionReporting.CONFIG, params)
                result.list   = TitleInstancePackagePlatform.executeQuery('select tipp from TitleInstancePackagePlatform tipp where tipp.id in (:idList) order by tipp.sortName, tipp.name', [idList: idList])
                result.tmpl   = '/subscription/reporting/details/entitlement'
            }
        }

        render template: result.tmpl, model: result
    }

    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_USER")
    })
    def chartDetailsExport() {

        Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('cde:') }
        Map<String, Object> selectedFields = [:]
        selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('cde:', ''), it.value ) }

        AbstractExport export = GenericExportManager.getCurrentExport( params.query, selectedFields )

        // TODO
        // TODO
        List<Long> idList = params.get('idList_cs') ? params.get('idList_cs').split(',').collect{ it as Long } : []
        //List<Long> idList = params.get('idList[]') ? params.get('idList[]').collect{ it.id as Long } : []
        // TODO
        // TODO
        List<String> rows = GenericExportManager.doExport( export, idList )

        SimpleDateFormat sdf = DateUtils.getSDF_forFilename()
        String filename
        if (params.filename) {
            filename = sdf.format(new Date()) + '_' + params.filename + '.csv'
        }
        else {
            filename = sdf.format(new Date()) + '_reporting.csv'
        }

        response.setHeader('Content-disposition', 'attachment; filename="' + filename + '"')
        response.contentType = 'text/csv'

        ServletOutputStream out = response.outputStream
        out.withWriter { w ->
            rows.each { r ->
                w.write( r + '\n')
            }
        }
        out.close()
    }
}