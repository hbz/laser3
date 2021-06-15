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
import de.laser.ReportingService
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
import de.laser.reporting.export.AbstractExport
import de.laser.reporting.export.GenericExportManager
import de.laser.finance.CostItem
import de.laser.helper.DateUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.helper.SessionCacheWrapper
import de.laser.reporting.myInstitution.base.BaseConfig
import de.laser.reporting.myInstitution.base.BaseDetails
import de.laser.reporting.myInstitution.base.BaseQuery
import de.laser.reporting.subscription.SubscriptionReporting
import grails.plugin.springsecurity.annotation.Secured
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.i18n.LocaleContextHolder

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
    ReportingService reportingService
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

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def chartDetails() {
        Map<String, Object> result = [
            token:  params.token,
            query:  params.query,
            id:     params.id ? params.id as Long : ''
        ]

        reportingService.doChartDetails( result, params ) // manipulates result

        render template: result.tmpl, model: result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def chartDetailsExport() {

        Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('cde:') }
        Map<String, Object> selectedFields = [:]
        selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('cde:', ''), it.value ) }

        AbstractExport export = GenericExportManager.createExport( params.token, selectedFields )

        Map<String, Object> detailsCache = BaseDetails.getDetailsCache( params.token )
        List<Long> idList = detailsCache.idList

        List<String> rows = GenericExportManager.export( export, idList )

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

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def deleteTask() {

        if (params.deleteId) {
            Task.withTransaction {
                Task dTask = Task.get(params.deleteId)
                if (dTask && dTask.creator.id == contextService.getUser().id) {
                    try {
                        flash.message = message(code: 'default.deleted.message', args: [message(code: 'task.label'), dTask.title])
                        dTask.delete()
                    }
                    catch (Exception e) {
                        log.error(e)
                        flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'task.label'), dTask.title])
                    }
                } else {
                    if (!dTask) {
                        flash.error = message(code: 'default.not.found.message', args: [message(code: 'task.label'), params.deleteId])
                    } else {
                        flash.error = message(code: 'default.noPermissions')
                    }
                }
            }
        }
        if(params.returnToShow) {
            redirect action: 'show', id: params.id, controller: params.returnToShow
            return
        }
        else {
            redirect(url: request.getHeader('referer'))
            return
        }
    }
}