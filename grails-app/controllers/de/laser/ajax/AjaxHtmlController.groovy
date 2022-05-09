package de.laser.ajax

import de.laser.GenericOIDService
import de.laser.PendingChangeService
import de.laser.AccessService
import de.laser.AddressbookService
import de.laser.ApiSource
import de.laser.CacheService
import de.laser.ContextService
import de.laser.GokbService
import de.laser.IssueEntitlement
import de.laser.License
import de.laser.LinksGenerationService
import de.laser.Org
import de.laser.OrgRole
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.ReportingFilter
import de.laser.ReportingGlobalService
import de.laser.ReportingLocalService
import de.laser.Subscription
import de.laser.Address
import de.laser.Doc
import de.laser.Person
import de.laser.PersonRole
import de.laser.SubscriptionPackage
import de.laser.SubscriptionService
import de.laser.SurveyConfig
import de.laser.SurveyConfigProperties
import de.laser.SurveyInfo
import de.laser.SurveyOrg
import de.laser.SurveyResult
import de.laser.Task
import de.laser.TaskService
import de.laser.TitleInstancePackagePlatform
import de.laser.UserSetting
import de.laser.annotations.DebugInfo
import de.laser.auth.User
import de.laser.ctrl.LicenseControllerService
import de.laser.ctrl.MyInstitutionControllerService
import de.laser.custom.CustomWkhtmltoxService
import de.laser.helper.DateUtils
import de.laser.helper.EhcacheWrapper
import de.laser.helper.SessionCacheWrapper
import de.laser.helper.SwissKnife
import de.laser.reporting.report.ReportingCache
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.export.base.BaseExportHelper
import de.laser.reporting.export.base.BaseQueryExport
import de.laser.reporting.export.LocalExportHelper
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.DetailsExportManager
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.reporting.export.QueryExportManager
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.workflow.WfCondition
import de.laser.workflow.WfConditionPrototype
import de.laser.workflow.WfWorkflow
import de.laser.workflow.WfWorkflowPrototype
import de.laser.workflow.WfTask
import de.laser.workflow.WfTaskPrototype
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.ss.usermodel.Workbook

import javax.servlet.ServletOutputStream

/**
 * This controller manages HTML fragment rendering calls; object manipulation is done in the AjaxController!
 * For JSON rendering, see AjaxJsonController.
 * IMPORTANT: Only template rendering here, no object manipulation!
 * @see AjaxController
 * @see AjaxJsonController
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class AjaxHtmlController {

    AddressbookService addressbookService
    CacheService cacheService
    ContextService contextService
    MyInstitutionControllerService myInstitutionControllerService
    PendingChangeService pendingChangeService
    GenericOIDService genericOIDService
    TaskService taskService
    LinksGenerationService linksGenerationService
    AccessService accessService
    ReportingGlobalService reportingGlobalService
    ReportingLocalService reportingLocalService
    SubscriptionService subscriptionService
    LicenseControllerService licenseControllerService
    CustomWkhtmltoxService wkhtmltoxService // custom
    GokbService gokbService

    /**
     * Test render call
     * @return a paragraph with sample text
     */
    @Secured(['ROLE_USER'])
    def test() {
        String result = '<p data-status="ok">OK'
        if (params.id) {
            result += ', ID:' + params.id
        }
        result += '</p>'
        render result
    }

    /**
     * Loads the display configuration fragment for the given entry point and the queried parameters
     * @return the display configurations fragment
     */
    @Deprecated
    @Secured(['ROLE_USER'])
    def loadGeneralFilter() {
        Map<String,Object> result = [entry:params.entry,queried:params.queried]
        render view: '/reporting/_displayConfigurations', model: result
    }

    //-------------------------------------------------- myInstitution/dashboard ---------------------------------------

    /**
     * Loads the pending changes for the dashboard. Is still subject of refactoring as the assembly of the relevant data still takes very long
     * @return the accepted and pending changes tab fragment for the dashboard
     */
    @Secured(['ROLE_USER'])
    def getChanges() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(null, params)
        SwissKnife.setPaginationParams(result, params, (User) result.user)
        result.acceptedOffset = params.acceptedOffset ? params.int("acceptedOffset") : result.offset
        result.pendingOffset = params.pendingOffset ? params.int("pendingOffset") : result.offset
        def periodInDays = result.user.getSettingsValue(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)
        Map<String, Object> pendingChangeConfigMap = [contextOrg:result.institution,consortialView:accessService.checkPerm(result.institution,"ORG_CONSORTIUM"),periodInDays:periodInDays,max:result.max,acceptedOffset:result.acceptedOffset, pendingOffset: result.pendingOffset]
        Map<String, Object> changes = pendingChangeService.getChanges(pendingChangeConfigMap)
        changes.max = result.max
        changes.editable = result.editable
        render template: '/myInstitution/changesWrapper', model: changes
    }

    /**
     * Loads the survey tab for the dashboard, containing current surveys
     * @return the survey tab fragment for the dashboard
     */
    @Secured(['ROLE_USER'])
    def getSurveys() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(null, params)
        SwissKnife.setPaginationParams(result, params, (User) result.user)
        List activeSurveyConfigs = SurveyConfig.executeQuery("from SurveyConfig surConfig where exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org and surOrg.finishDate is null AND surConfig.surveyInfo.status = :status) " +
                " order by surConfig.surveyInfo.endDate",
                [org: result.institution,
                 status: RDStore.SURVEY_SURVEY_STARTED])

        if(accessService.checkPerm('ORG_CONSORTIUM')){
            activeSurveyConfigs = SurveyConfig.executeQuery("from SurveyConfig surConfig where surConfig.surveyInfo.status = :status  and surConfig.surveyInfo.owner = :org " +
                    " order by surConfig.surveyInfo.endDate",
                    [org: result.institution,
                     status: RDStore.SURVEY_SURVEY_STARTED])
        }

        result.surveys = activeSurveyConfigs.groupBy {it?.id}
        result.countSurvey = result.surveys.size()
        result.surveys = result.surveys.drop((int) result.offset).take((int) result.max)

        result.surveysOffset = result.offset

        render template: '/myInstitution/surveys', model: result
    }

    //-------------------------------------------------- subscription/show ---------------------------------------------

    /**
     * Gets the subscription and license links for the given subscription or license
     * @return the fragment listing the links going out from the given object
     */
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

    /**
     * Gets the data of the packages linked to the given subscription
     * @return the package details fragment
     */
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

    /**
     * Gets the data of the linked packages to the subscription which is target of the given survey
     * @return the package details fragment for the survey view
     */
    @Secured(['ROLE_USER'])
    def getGeneralPackageData() {
        Map<String,Object> result = [subscription:Subscription.get(params.subscription)]

        result.packages = []
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        result.subscription.packages.each { SubscriptionPackage subscriptionPackage ->
            Map packageInfos = [:]

            packageInfos.packageInstance = subscriptionPackage.pkg

            Map queryResult = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + "/find?uuid=${subscriptionPackage.pkg.gokbId}")
            if (queryResult.error && queryResult.error == 404) {
                flash.error = message(code: 'wekb.error.404')
            } else if (queryResult.warning) {
                List records = queryResult.warning.records
                packageInfos.packageInstanceRecord = records ? records[0] : [:]
            }
            result.packages << packageInfos
        }

        render template: '/survey/packages', model: result
    }

    /**
     * Gets the data of the linked packages to the subscription which is target of the given survey
     * @return the package details fragment for the survey view
     */
    @Secured(['ROLE_USER'])
    def getIeInfos() {
        Map<String,Object> result = [subscription:Subscription.get(params.subscription)]

        render template: '/survey/ieInfos', model: result
    }

    /**
     * Gets the properties to the given subscription or license
     * @return the properties view for the respective details view
     */
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

    /**
     * Opens the edit modal for the given note
     */
    @Secured(['ROLE_USER'])
    def editNote() {
        Map<String, Object> result = [:]
        result.params = params
        result.noteInstance = Doc.get(params.id)

        render template: "/templates/notes/modal_edit", model: result
    }

    /**
     * Opens the view modal for the given note
     */
    @Secured(['ROLE_USER'])
    def readNote() {
        Map<String, Object> result = [:]
        result.params = params
        result.noteInstance = Doc.get(params.id)

        render template: "/templates/notes/modal_read", model: result
    }

    /**
     * Opens the task creation modal
     */
    @Secured(['ROLE_USER'])
    def createTask() {
        long backendStart = System.currentTimeMillis()
        Org contextOrg = contextService.getOrg()
        Map<String, Object> result = taskService.getPreconditions(contextOrg)

        result.backendStart = backendStart

        render template: "/templates/tasks/modal_create", model: result
    }

    /**
     * Opens the task editing modal
     */
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

    /**
     * Opens the address creation modal and sets the underlying parameters
     */
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

    /**
     * Opens the edit modal for an existing address
     */
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

    /**
     * Opens the contact entity creation modal and sets the underlying parameters
     */
    @Secured(['ROLE_USER'])
    def createPerson() {
        Map<String, Object> result = [:]
        result.contextOrg = contextService.getOrg()
        result.tenant = result.contextOrg
        result.modalId = 'personModal'
        result.presetFunctionType = RDStore.PRS_FUNC_GENERAL_CONTACT_PRS
        result.showContacts = params.showContacts == "true" ? true : ''
        result.addContacts = params.showContacts == "true" ? true : ''
        result.showAddresses = params.showAddresses == "true" ? true : ''
        result.addAddresses = params.showAddresses == "true" ? true : ''
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
                result.functions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FUNC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FUNC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FUNC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FUNC_DELIVERY_ADDRESS]
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
                result.presetFunctionType = RefdataValue.get(params.supportType)
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

    /**
     * Opens the edit modal for an existing contact entity
     */
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
                    result.functions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FUNC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FUNC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FUNC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FUNC_DELIVERY_ADDRESS]
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

    /**
     * Retrieves the contact fields for an entity modal
     */
    @Secured(['ROLE_USER'])
    def contactFields() {
        render template: "/templates/cpa/contactFields"
    }

    /**
     * Retrieves the address fields for an entity modal
     */
    @Secured(['ROLE_USER'])
    def addressFields() {
        render template: "/templates/cpa/addressFields", model: [multipleAddresses: params.multipleAddresses]
    }

    /**
     * Loads for the subscription-license link table the properties table for a license linked to the triggering subscription
     */
    @Secured(['ROLE_USER'])
    def getLicensePropertiesForSubscription() {
        License loadFor = License.get(params.loadFor)
        if (loadFor) {
            Map<String, Object> derivedPropDefGroups = loadFor.getCalculatedPropDefGroups(contextService.getOrg())
            render view: '/subscription/_licProp', model: [license: loadFor, derivedPropDefGroups: derivedPropDefGroups, linkId: params.linkId]
        }
    }

    // ----- surveyInfos -----

    /**
     * Checks if the preconditions for a survey submission are given
     * @return the message depending on the survey's completion status
     */
    @Secured(['ROLE_USER'])
    def getSurveyFinishMessage() {
        Org contextOrg = contextService.getOrg()
        SurveyInfo surveyInfo = SurveyInfo.get(params.id)
        SurveyConfig surveyConfig = SurveyConfig.get(params.surveyConfigID)
        SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(contextOrg, surveyConfig)
        List<SurveyResult> surveyResults = SurveyResult.findAllByParticipantAndSurveyConfig(contextOrg, surveyConfig)
        boolean allResultHaveValue = true
        List<String> notProcessedMandatoryProperties = []
        surveyResults.each { SurveyResult surre ->
            SurveyConfigProperties surveyConfigProperties = SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surre.type)
            if (surveyConfigProperties.mandatoryProperty && !surre.isResultProcessed() && !surveyOrg.existsMultiYearTerm()) {
                allResultHaveValue = false
                notProcessedMandatoryProperties << surre.type.getI10n('name')
            }
        }
        boolean noParticipation = false
        if(surveyInfo.isMandatory) {
            if(surveyConfig && surveyConfig.subSurveyUseForTransfer){
                noParticipation = (SurveyResult.findByParticipantAndSurveyConfigAndType(contextOrg, surveyConfig, RDStore.SURVEY_PROPERTY_PARTICIPATION).refValue == RDStore.YN_NO)
            }
        }
            if(notProcessedMandatoryProperties.size() > 0){
                render message(code: "confirm.dialog.concludeBinding.survey.notProcessedMandatoryProperties", args: [notProcessedMandatoryProperties.join(', ')])
            }
            else if(noParticipation || allResultHaveValue)
                render message(code: "confirm.dialog.concludeBinding.survey")
            else if(!noParticipation && !allResultHaveValue)
                render message(code: "confirm.dialog.concludeBinding.surveyIncomplete")
    }

    // ----- reporting -----

    /**
     * Retrieves the filter history and bookmarks for the given reporting view.
     * If a command is being submitted, the cache is being updated. The updated view is being rendered afterwards
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def reporting() {
        Map<String, Object> result = [
            tab: params.tab
        ]

        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        Closure getReportingKeys = {
            sessionCache.list().keySet().findAll{ it.startsWith("MyInstitutionController/reporting/") }
        }

        if (params.context == BaseConfig.KEY_MYINST) {

            if (params.cmd == 'deleteHistory') {
                getReportingKeys().each {it ->
                    sessionCache.remove( it )
                }
            }
            else if (params.token) {
                if (params.cmd == 'addBookmark') {
                    ReportingCache rc = new ReportingCache(ReportingCache.CTX_GLOBAL, params.token)
                    ReportingFilter rf = ReportingFilter.construct(
                            rc,
                            contextService.getUser(),
                            BaseConfig.getFilterLabel(rc.readMeta().filter.toString()) + ' - ' + DateUtils.getLocalizedSDF_noTime().format(System.currentTimeMillis()),
                            rc.readFilterCache().result.replaceAll('<strong>', '').replaceAll('</strong>', '') as String
                    )
                    result.lastAddedBookmarkId = rf.id
                }
                else if (params.cmd == 'deleteBookmark') {
                    ReportingFilter rf = ReportingFilter.findByTokenAndOwner(params.token, contextService.getUser())
                    if (rf) {
                        rf.delete()
                    }
                }
            }
        }
        result.bookmarks = ReportingFilter.findAllByOwner( contextService.getUser(), [sort: 'lastUpdated', order: 'desc'] )

        result.filterHistory = getReportingKeys().sort { a,b -> sessionCache.get(b).meta.timestamp <=> sessionCache.get(a).meta.timestamp }.take(5)
        getReportingKeys().findAll{ it -> ! result.filterHistory.contains( it ) }.each { it -> sessionCache.remove(it) }

        render template: '/myInstitution/reporting/historyAndBookmarks', model: result
    }

    /**
     * Retrieves the details for the given charts
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def chartDetails() {
        // TODO - SESSION TIMEOUTS

        Map<String, Object> result = [
            token:  params.token,
            query:  params.query
        ]
        result.id = params.id ? params.id != 'null' ? params.id as Long : '' : ''

        if (params.context == BaseConfig.KEY_MYINST) {
            reportingGlobalService.doChartDetails( result, params ) // manipulates result
        }
        else if (params.context == BaseConfig.KEY_SUBSCRIPTION) {
            if (params.idx) {
                // TODO !!!!
                params.idx = params.idx.replaceFirst(params.id + ':', '') // TODO !!!!
                // TODO !!!!
            }
            reportingLocalService.doChartDetails( result, params ) // manipulates result
        }

        render template: result.tmpl, model: result
    }

    /**
     * Assembles the chart details and outputs the result in the given format.
     * Currently supported formats are:
     * <ul>
     *     <li>CSV</li>
     *     <li>Excel</li>
     *     <li>PDF</li>
     * </ul>
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def chartDetailsExport() {

        Map<String, Object> selectedFieldsRaw = params.findAll { it -> it.toString().startsWith('cde:') }
        Map<String, Object> selectedFields = [:]
        selectedFieldsRaw.each { it -> selectedFields.put(it.key.replaceFirst('cde:', ''), it.value) }

        String filename = params.filename ?: BaseExportHelper.getFileName()
        ReportingCache rCache
        BaseDetailsExport export
        Map<String, Object> detailsCache

        if (params.context == BaseConfig.KEY_MYINST) {
            rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, params.token )

            if (rCache.exists()) {
                detailsCache = GlobalExportHelper.getDetailsCache(params.token)
                export = DetailsExportManager.createGlobalExport(params.token, selectedFields)
            }
            else {
                redirect(url: request.getHeader('referer')) // TODO
                return
            }
        }
        else if (params.context == BaseConfig.KEY_SUBSCRIPTION) {
            rCache = new ReportingCache( ReportingCache.CTX_SUBSCRIPTION, params.token )

            if (rCache.exists()) {
                detailsCache = LocalExportHelper.getDetailsCache(params.token)
                export = DetailsExportManager.createLocalExport(params.token, selectedFields)
            }
            else {
                redirect(url: request.getHeader('referer')) // TODO
                return
            }
        }

        if (export && detailsCache) {

            if (params.fileformat == 'csv') {

                response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.csv"')
                response.contentType = 'text/csv'

                List<String> rows = DetailsExportManager.exportAsList(
                        export,
                        detailsCache.idList as List<Long>,
                        'csv',
                        [hideEmptyResults: params.containsKey('hideEmptyResults-csv')]
                )

                ServletOutputStream out = response.outputStream
                out.withWriter { w ->
                    rows.each { r ->
                        w.write(r + '\n')
                    }
                }
                out.close()
            }
            else if (params.fileformat == 'xlsx') {

                response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.xlsx"')
                response.contentType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'

                Workbook wb = DetailsExportManager.exportAsWorkbook(
                        export,
                        detailsCache.idList as List<Long>,
                        'xlsx',
                        [   hideEmptyResults: params.containsKey('hideEmptyResults-xlsx'),
                            insertNewLines: params.containsKey('insertNewLines-xlsx'),
                            useHyperlinks: params.containsKey('useHyperlinks-xlsx') ]
                )

                ServletOutputStream out = response.outputStream
                wb.write(out)
                out.close()
            }
            else if (params.fileformat == 'pdf') {

                Map<String, Boolean> options = [
                        hideEmptyResults: params.containsKey('hideEmptyResults-pdf'),
                        useHyperlinks: params.containsKey('useHyperlinks-pdf'),
                        useLineNumbers: params.containsKey('useLineNumbers-pdf'),
                        useSmallFont: params.containsKey('useSmallFont-pdf'),
                        pageFormat: params.get('pageFormat-pdf') != 'auto'
                ]

                List<List<String>> content = DetailsExportManager.exportAsList(
                        export,
                        detailsCache.idList as List<Long>,
                        'pdf',
                        options
                )

                String view = ''
                Map<String, Object> model = [:]
                Map<String, Object> struct = [:]

                List<String> pf = BaseExportHelper.PDF_OPTIONS.get(params.get('pageFormat-pdf'))
                if ( pf[0] != 'auto' ) {
                    struct.pageSize = pf[0]
                    struct.orientation = pf[1]
                }
                else {
                    struct = BaseExportHelper.calculatePdfPageStruct(content, 'chartDetailsExport')
                }

                if (params.context == BaseConfig.KEY_MYINST) {
                    view    = '/myInstitution/reporting/export/pdf/generic_details'
                    model   = [
                            filterLabels: GlobalExportHelper.getCachedFilterLabels(params.token),
                            filterResult: GlobalExportHelper.getCachedFilterResult(params.token),
                            queryLabels : GlobalExportHelper.getCachedQueryLabels(params.token),
                            title       : filename,
                            header      : content.remove(0),
                            content     : content,
                            // struct      : [struct.width, struct.height, struct.pageSize + ' ' + struct.orientation],
                            options     : options
                    ]
                }
                else if (params.context == BaseConfig.KEY_SUBSCRIPTION) {
                    view    = '/subscription/reporting/export/pdf/generic_details'
                    model   = [
                            //filterLabels: LocalExportHelper.getCachedFilterLabels(params.token),
                            filterResult: LocalExportHelper.getCachedFilterResult(params.token),
                            queryLabels : LocalExportHelper.getCachedQueryLabels(params.token),
                            title       : filename,
                            header      : content.remove(0),
                            content     : content,
                            // struct      : [struct.width, struct.height, struct.pageSize + ' ' + struct.orientation],
                            options     : options
                    ]
                }

                byte[] pdf = wkhtmltoxService.makePdf(
                        view: view,
                        model: model,
                        // header: '',
                        // footer: '',
                        pageSize: struct.pageSize,
                        orientation: struct.orientation,
                        marginLeft: 10,
                        marginTop: 15,
                        marginBottom: 15,
                        marginRight: 10
                )

                response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.pdf"')
                response.setContentType('application/pdf')
                response.outputStream.withStream { it << pdf }
            }
        }
    }

    /**
     * Assembles the chart query and outputs the result in the given format.
     * Currently supported formats are:
     * <ul>
     *     <li>CSV</li>
     *     <li>Excel</li>
     *     <li>PDF</li>
     * </ul>
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def chartQueryExport() {

        ReportingCache rCache
        BaseQueryExport export
        List<String> queryLabels = []
        String filename = params.filename ?: BaseExportHelper.getFileName()

        if (params.context == BaseConfig.KEY_MYINST) {
            rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, params.token )

            if (rCache.exists()) {
                export      = QueryExportManager.createExport( params.token, BaseConfig.KEY_MYINST )
                queryLabels = GlobalExportHelper.getIncompleteQueryLabels( params.token )
                //detailsCache = GlobalExportHelper.getDetailsCache(params.token)
                //export = DetailsExportManager.createGlobalExport(params.token, selectedFields)
            }
            else {
                redirect(url: request.getHeader('referer')) // TODO
                return
            }
        }
        else if (params.context == BaseConfig.KEY_SUBSCRIPTION) {
            rCache = new ReportingCache( ReportingCache.CTX_SUBSCRIPTION, params.token )

            if (rCache.exists()) {
                export      = QueryExportManager.createExport( params.token, BaseConfig.KEY_SUBSCRIPTION )
                queryLabels = LocalExportHelper.getCachedQueryLabels( params.token )
                //detailsCache = LocalExportHelper.getDetailsCache(params.token)
                //export = DetailsExportManager.createLocalExport(params.token, selectedFields)
            }
            else {
                redirect(url: request.getHeader('referer')) // TODO
                return
            }
        }

        if (params.fileformat == 'csv') {

            response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.csv"')
            response.contentType = 'text/csv'

            List<String> rows = QueryExportManager.exportAsList( export, 'csv' )

            ServletOutputStream out = response.outputStream
            out.withWriter { w ->
                rows.each { r ->
                    w.write( r + '\n')
                }
            }
            out.close()
        }
        else if (params.fileformat == 'xlsx') {

            response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.xlsx"')
            response.contentType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'

            Workbook wb = QueryExportManager.exportAsWorkbook( export, 'xlsx' )

            ServletOutputStream out = response.outputStream
            wb.write(out)
            out.close()
        }
        else if (params.fileformat == 'pdf') {
            // TODO
            // TODO
            // TODO
            // TODO
            List<List<String>> content = QueryExportManager.exportAsList(export, 'pdf')

            Map<String, Object> struct = [:]

//            if (params.contentType == 'table') {
//                struct = ExportHelper.calculatePdfPageStruct(content, 'chartQueryExport')
//            }
//            if (params.contentType == 'image') {
                // struct = ExportHelper.calculatePdfPageStruct(content, 'chartQueryExport-image') // TODO

//                struct = [
//                        width       : Float.parseFloat( params.imageSize.split(':')[0] ),
//                        height      : Float.parseFloat( params.imageSize.split(':')[1] ),
//                        pageSize    : 'A4',
//                        orientation : 'Portrait'
//                ] as Map<String, Object>
//
//                struct.whr = struct.width / struct.height
//                if (struct.height < 400 && struct.whr >= 2) {
//                    struct.orientation = 'Landscape'
//                }

                //Map<String, Object> queryCache = BaseQuery.getQueryCache( params.token )
                //queryCache.put( 'tmpBase64Data', params.imageData )
//            }

            Map<String, Object> model = [
                    token:        params.token,
                    filterLabels: GlobalExportHelper.getCachedFilterLabels(params.token),
                    filterResult: GlobalExportHelper.getCachedFilterResult(params.token),
                    queryLabels : queryLabels,
                    //imageData   : params.imageData,
                    //tmpBase64Data : BaseQuery.getQueryCache( params.token ).get( 'tmpBase64Data' ),
                    contentType : params.contentType,
                    title       : filename,
                    header      : content.remove(0),
                    content     : content,
                    struct      : [struct.width, struct.height, struct.pageSize + ' ' + struct.orientation]
            ]

            byte[] pdf = wkhtmltoxService.makePdf(
                    view: '/myInstitution/reporting/export/pdf/generic_query',
                    model: model,
                    // header: '',
                    // footer: '',
                    pageSize: struct.pageSize,
                    orientation: struct.orientation,
                    marginLeft: 10,
                    marginTop: 15,
                    marginBottom: 15,
                    marginRight: 10
            )

            response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.pdf"')
            response.setContentType('application/pdf')
            response.outputStream.withStream { it << pdf }

//                render view: '/myInstitution/reporting/export/pdf/generic_query', model: model
        }
    }

    /**
     * Opens the modal for creation of one of the workflow components:
     * <ul>
     *     <li>workflow</li>
     *     <li>workflow task</li>
     *     <li>workflow condition</li>
     * </ul>
     * Upon opening, the underlying parameters are being set
     */
    @Secured(['ROLE_USER'])
    def createWfXModal() {
        Map<String, Object> result = [
                tmplCmd: 'create',
                tmplModalTitle: g.message(code: 'default.create.label', args: [ g.message(code: 'workflow.object.' + params.key) ]),
                tmplFormUrl: createLink(controller: 'admin', action: 'manageWorkflows'),
                prefix: params.key
        ]

        if (params.tab) { result.tmplTab = params.tab }

        if (params.key in [WfWorkflow.KEY]) {
            result.tmpl = '/templates/workflow/forms/wfWorkflow'
        }
        else if (params.key in [WfTask.KEY]) {
            result.tmpl = '/templates/workflow/forms/wfTask'
        }
        else if (params.key in [WfCondition.KEY]) {
            result.tmpl = '/templates/workflow/forms/wfCondition'
        }
        else if (params.key in [WfWorkflowPrototype.KEY]) {
            result.tmpl = '/templates/workflow/forms/wfWorkflow'

            // not: * used as tp.next * used as tp.child
            result.dd_taskList = WfTaskPrototype.executeQuery(
                    'select wftp from WfTaskPrototype wftp where ' +
                            'wftp not in (select tp.next from WfTaskPrototype tp) ' +
                            'and wftp not in (select tp.child from WfTaskPrototype tp) ' +
                            'order by id'
            )
        }
        else if (params.key in [WfTaskPrototype.KEY]) {
            result.tmpl = '/templates/workflow/forms/wfTask'

            // not: * used as tp.child * used as wp.task
            result.dd_nextList = WfTaskPrototype.executeQuery(
                    'select wftp from WfTaskPrototype wftp where ' +
                            'wftp not in (select tp.child from WfTaskPrototype tp) ' +
                            'and wftp not in (select wp.task from WfWorkflowPrototype wp) ' +
                            'order by id'
            )
            // not: * used as tp.next * used as wp.task
            result.dd_childList = WfTaskPrototype.executeQuery(
                    'select wftp from WfTaskPrototype wftp where ' +
                            'wftp not in (select tp.next from WfTaskPrototype tp) ' +
                            'and wftp not in (select wp.task from WfWorkflowPrototype wp) ' +
                            'order by id'
            )

            result.dd_conditionList = WfConditionPrototype.executeQuery('select wfcp from WfConditionPrototype wfcp order by id')
        }
        else if (params.key in [WfConditionPrototype.KEY]) {
            result.tmpl = '/templates/workflow/forms/wfCondition'
        }
        render template: '/templates/workflow/forms/modalWrapper', model: result
    }

    /**
     * Opens a modal to display workflow details
     */
    @Secured(['ROLE_USER'])
    def useWfXModal() {
        Map<String, Object> result = [
                tmplCmd:    'usage',
                tmplFormUrl: createLink(controller: 'myInstitution', action: 'currentWorkflows')
        ]

        if (params.key) {
            String[] key = (params.key as String).split(':')

            result.prefix = key[2]

            if (key[0] == 'subscription') {
                result.subscription = Subscription.get( key[1] )
                result.tmplFormUrl  = createLink(controller: 'subscription', action: 'workflows', id: key[1])
            }
            else if (key[0] == 'myInstitution') {
                result.workflow = WfWorkflow.get (key[1] ) // TODO
            }
            else if (key[0] == 'dashboard') {
                result.workflow = WfWorkflow.get (key[1] )
                result.tmplFormUrl  = createLink(controller: 'myInstitution', action: 'dashboard')
            }

            if (result.prefix == WfWorkflow.KEY) {
                result.workflow       = WfWorkflow.get( key[3] )
                result.tmplModalTitle = '<i class="icon tasks"></i> ' + result.workflow.title

            }
            else if (result.prefix == WfTask.KEY) {
                result.task           = WfTask.get( key[3] )
                result.tmplModalTitle = '<i class="icon check circle outline"></i> ' + result.task.title
            }
        }

        if (params.info) {
            result.info = params.info
        }

        render template: '/templates/workflow/forms/modalWrapper', model: result
    }

    /**
     * Opens an editing modal for the given workflow
     */
    @Secured(['ROLE_USER'])
    def editWfXModal() {
        Map<String, Object> result = [
                tmplCmd : 'edit',
                tmplFormUrl: createLink(controller: 'admin', action: 'manageWorkflows')
        ]

        if (params.tab) { result.tmplTab = params.tab }
        if (params.info) { result.tmplInfo = params.info }

        if (params.key) {
            String[] key = (params.key as String).split(':')

            // WF_X:id
            String prefix = key[0]
            Long wfObjId = key[1] as Long

            // subscription:id:WF_X:id
            if (prefix == 'subscription') {
                prefix = key[2]
                wfObjId = key[3] as Long
                result.tmplFormUrl = createLink(controller: 'subscription', action: 'workflows', id: key[1])
            }

            result.prefix = prefix
            result.tmplModalTitle = g.message(code: 'default.edit.label', args: [ g.message(code: 'workflow.object.' + result.prefix) ]) + ': '

            if (result.prefix == WfWorkflowPrototype.KEY) {
                result.workflow       = WfWorkflowPrototype.get( wfObjId )
                result.tmpl           = '/templates/workflow/forms/wfWorkflow'

                if (result.workflow) {
                    // not: * used as tp.next * used as tp.child
                    result.dd_taskList = WfTaskPrototype.executeQuery(
                            'select wftp from WfTaskPrototype wftp where ' +
                            'wftp not in (select tp.next from WfTaskPrototype tp) ' +
                            'and wftp not in (select tp.child from WfTaskPrototype tp) ' +
                            'order by id'
                    )
                }
            }
            else if (result.prefix == WfWorkflow.KEY) {
                result.workflow       = WfWorkflow.get( wfObjId )
                result.tmpl           = '/templates/workflow/forms/wfWorkflow'
                result.tmplModalTitle = result.tmplModalTitle + result.workflow.title

//                if (result.workflow) {
//                    result.dd_taskList          = result.workflow.task ? [ result.workflow.task ] : []
//                    result.dd_prototypeList     = result.workflow.prototype ? [ result.workflow.prototype ] : []
//                    result.dd_subscriptionList  = result.workflow.subscription ? [ result.workflow.subscription ] : []
//                }
            }
            else if (result.prefix == WfTaskPrototype.KEY) {
                result.task           = WfTaskPrototype.get( wfObjId )
                result.tmpl           = '/templates/workflow/forms/wfTask'
                result.tmplModalTitle = result.tmplModalTitle + result.task.title

                if (result.task) {
//                    String sql = 'select wftp from WfTaskPrototype wftp where id != :id order by id'
                    Map<String, Object> sqlParams = [id: wfObjId]

                    // not: * self * used as tp.child * used as wp.task
                    result.dd_nextList = WfTaskPrototype.executeQuery(
                            'select wftp from WfTaskPrototype wftp where id != :id ' +
                            'and wftp not in (select tp.child from WfTaskPrototype tp) ' +
                            'and wftp not in (select wp.task from WfWorkflowPrototype wp) ' +
                            'order by id', sqlParams
                    )
                    // not: * self * used as tp.next * used as wp.task
                    result.dd_childList = WfTaskPrototype.executeQuery(
                            'select wftp from WfTaskPrototype wftp where id != :id ' +
                            'and wftp not in (select tp.next from WfTaskPrototype tp) ' +
                            'and wftp not in (select wp.task from WfWorkflowPrototype wp) ' +
                            'order by id', sqlParams
                    )
//                    result.dd_previousList  = WfTaskPrototype.executeQuery(sql, sqlParams)
//                    result.dd_parentList    = WfTaskPrototype.executeQuery(sql, sqlParams)

                    result.dd_conditionList = WfConditionPrototype.executeQuery('select wfcp from WfConditionPrototype wfcp order by id')
                }
            }
            else if (result.prefix == WfTask.KEY) {
                result.task           = WfTask.get( wfObjId )
                result.tmpl           = '/templates/workflow/forms/wfTask'
                result.tmplModalTitle = result.tmplModalTitle + result.task.title

                if (result.task) {

                    result.dd_nextList      = result.task.next ? [ result.task.next ] : []
                    result.dd_childList     = result.task.child ? [ result.task.child ] : []
                    result.dd_conditionList = result.task.condition ? [ result.task.condition ] : []
//                    result.dd_prototypeList = result.task.prototype ? [ result.task.prototype ] : []

//                    String sql = 'select wft from WfTask wft where id != :id order by id'
//                    Map<String, Object> sqlParams = [id: key[1] as Long]

//                    result.dd_previousList  = WfTask.executeQuery(sql, sqlParams)
//                    result.dd_parentList    = WfTask.executeQuery(sql, sqlParams)
                }
            }
            else if (result.prefix == WfConditionPrototype.KEY) {
                result.condition      = WfConditionPrototype.get( wfObjId )
                result.tmpl           = '/templates/workflow/forms/wfCondition'
                result.tmplModalTitle = result.tmplModalTitle + result.condition.title

//                if (result.condition) {
//                    result.dd_taskList = WfTaskPrototype.executeQuery( 'select wftp from WfTaskPrototype wftp' )
//                }
            }
            else if (result.prefix == WfCondition.KEY) {
                result.condition      = WfCondition.get( wfObjId )
                result.tmpl           = '/templates/workflow/forms/wfCondition'
                result.tmplModalTitle = result.tmplModalTitle + result.condition.title

//                if (result.condition) {
//                    result.dd_taskList = WfTask.executeQuery( 'select wft from WfTask wft' )
//                    result.dd_prototypeList = result.condition.prototype ? [ result.condition.prototype ] : []
//                }
            }

            EhcacheWrapper cache = cacheService.getTTL1800Cache('admin/manageWorkflows')
            result.wfpIdTable = cache.get( 'wfpIdTable') ?: [:]
            result.tpIdTable  = cache.get( 'tpIdTable')  ?: [:]
            result.cpIdTable  = cache.get( 'cpIdTable')  ?: [:]

            render template: '/templates/workflow/forms/modalWrapper', model: result
        }
    }

    /**
     * Retrieves detailed title information to a given entitlement and opens a modal showing those details
     */
    @Secured(['ROLE_USER'])
    Map<String,Object> showAllTitleInfos() {
        Map<String, Object> result = [:]

        result.apisources = ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

        result.tipp = params.tippID ? TitleInstancePackagePlatform.get(params.tippID) : null
        result.ie = params.ieID ? IssueEntitlement.get(params.ieID) : null
        result.showPackage = params.showPackage
        result.showPlattform = params.showPlattform
        result.showCompact = params.showCompact
        result.showEmptyFields = params.showEmptyFields

        render template: "/templates/title_modal", model: result

    }
}