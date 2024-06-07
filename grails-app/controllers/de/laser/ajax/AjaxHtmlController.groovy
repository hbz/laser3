package de.laser.ajax

import de.laser.AlternativeName
import de.laser.CacheService
import de.laser.ControlledListService
import de.laser.CustomerTypeService
import de.laser.DiscoverySystemFrontend
import de.laser.DiscoverySystemIndex
import de.laser.DocContext
import de.laser.GenericOIDService
import de.laser.PendingChangeService
import de.laser.AddressbookService
import de.laser.WekbNewsService
import de.laser.WorkflowService
import de.laser.cache.EhcacheWrapper
import de.laser.config.ConfigDefaults
import de.laser.config.ConfigMapper
import de.laser.ctrl.SubscriptionControllerService
import de.laser.remote.ApiSource
import de.laser.ContextService
import de.laser.GokbService
import de.laser.IssueEntitlement
import de.laser.License
import de.laser.LinksGenerationService
import de.laser.Org
import de.laser.OrgRole
import de.laser.Provider
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
import de.laser.storage.BeanStore
import de.laser.storage.PropertyStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyResult
import de.laser.Task
import de.laser.TaskService
import de.laser.TitleInstancePackagePlatform
import de.laser.UserSetting
import de.laser.Vendor
import de.laser.annotations.DebugInfo
import de.laser.auth.User
import de.laser.ctrl.LicenseControllerService
import de.laser.ctrl.MyInstitutionControllerService
import de.laser.custom.CustomWkhtmltoxService
import de.laser.utils.DateUtils
import de.laser.utils.SwissKnife
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
import de.laser.workflow.WfChecklist
import de.laser.workflow.WfCheckpoint
import de.laser.workflow.WorkflowHelper
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.ss.usermodel.Workbook
import org.mozilla.universalchardet.UniversalDetector

import javax.servlet.ServletOutputStream
import java.nio.charset.Charset

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
    ControlledListService controlledListService
    CustomWkhtmltoxService wkhtmltoxService // custom
    GenericOIDService genericOIDService
    GokbService gokbService
    LicenseControllerService licenseControllerService
    LinksGenerationService linksGenerationService
    MyInstitutionControllerService myInstitutionControllerService
    PendingChangeService pendingChangeService
    ReportingGlobalService reportingGlobalService
    ReportingLocalService reportingLocalService
    SubscriptionService subscriptionService
    SubscriptionControllerService subscriptionControllerService
    TaskService taskService
    WekbNewsService wekbNewsService
    WorkflowService workflowService

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
     * Adds a new object stub to an xEditable enumeration.
     * Currently supported are {@link AlternativeName}s (params.object == "altname") or {@link de.laser.finance.PriceItem}s (params.object == "priceItem")
     * @return a template fragment for the new xEditable item
     */
    @Secured(['ROLE_USER'])
    def addObject() {
        def resultObj, owner = genericOIDService.resolveOID(params.owner)
        switch(params.object) {
            case "altname": Map<String, Object> config = [name: 'Unknown']
                if(owner instanceof License)
                    config.license = owner
                else if(owner instanceof Org)
                    config.org = owner
                else if(owner instanceof Provider)
                    config.provider = owner
                else if(owner instanceof Subscription)
                    config.subscription = owner
                else if(owner instanceof Vendor)
                    config.vendor = owner
                resultObj = AlternativeName.construct(config)
                if(resultObj) {
                    render template: '/templates/ajax/newXEditable', model: [wrapper: params.object, showConsortiaFunctions: contextService.getOrg().isCustomerType_Consortium(), ownObj: resultObj, objOID: genericOIDService.getOID(resultObj), field: "name", overwriteEditable: true]
                }
                break
            case "frontend":
                resultObj = new DiscoverySystemFrontend([org: owner, frontend: RDStore.GENERIC_NULL_VALUE]).save()
                if(resultObj) {
                    render template: '/templates/ajax/newXEditable', model: [wrapper: params.object, ownObj: resultObj, objOID: genericOIDService.getOID(resultObj), field: "frontend", config: RDConstants.DISCOVERY_SYSTEM_FRONTEND, overwriteEditable: true]
                }
                break
            case "index":
                resultObj = new DiscoverySystemIndex([org: owner, index: RDStore.GENERIC_NULL_VALUE]).save()
                if(resultObj) {
                    render template: '/templates/ajax/newXEditable', model: [wrapper: params.object, ownObj: resultObj, objOID: genericOIDService.getOID(resultObj), field: "index", config: RDConstants.DISCOVERY_SYSTEM_INDEX, overwriteEditable: true]
                }
                break
            case "coverage": //TODO
                break
            case "priceItem":
                Map<String, Object> ctrlResult = subscriptionControllerService.addEmptyPriceItem(params)
                if(ctrlResult.status == SubscriptionControllerService.STATUS_OK) {
                    render template: '/templates/tipps/priceItem', model: [priceItem: ctrlResult.result.newItem, editable: true] //editable check is implicitly done by call; the AJAX loading can be triggered iff editable == true
                }
                break
        }
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
        Map<String, Object> pendingChangeConfigMap = [
                contextOrg: result.institution,
                consortialView: (result.institution as Org).isCustomerType_Consortium(),
                periodInDays:periodInDays,
                max:result.max,
                acceptedOffset:result.acceptedOffset,
                pendingOffset: result.pendingOffset
        ]
        Map<String, Object> changes = pendingChangeService.getSubscriptionChanges(pendingChangeConfigMap)
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

        if (contextService.getOrg().isCustomerType_Consortium_Pro()){
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

    /**
     * Call to render the flyout to display recent changes in the we:kb knowledge base
     * @return the template fragment for the changes
     */
    @Secured(['ROLE_USER'])
    def wekbNewsFlyout() {
        log.debug('ajaxHtmlController.wekbNewsFlyout ' + params)

        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(null, params)
        result.wekbNews = wekbNewsService.getCurrentNews()
        result.tmplView = 'details'

        render template: '/myInstitution/wekbNews', model: result
    }

    //-------------------------------------------------- subscription/show ---------------------------------------------

    /**
     * Gets the subscription and license links for the given subscription or license
     * @return the fragment listing the links going out from the given object
     */
    @Secured(['ROLE_USER'])
    def getLinks() {
        Map<String, Object> result = [user:contextService.getUser(), contextOrg:contextService.getOrg(), subscriptionLicenseLink:params.subscriptionLicenseLink]
        def entry = genericOIDService.resolveOID(params.entry)
        result.entry = entry
        result.editable = entry.isEditableBy(result.user)
        if(entry instanceof Subscription) {
            result.subscription = (Subscription) entry
            result.atConsortialParent = result.contextOrg.id == result.subscription.getConsortia()?.id && !result.subscription.instanceOf ? "true" : "false"
        }
        else if(entry instanceof License) {
            result.license = (License) entry
            result.atConsortialParent = result.contextOrg == result.license.getLicensingConsortium() && !result.license.instanceOf ? "true" : "false"
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
        Map<String,Object> result = [subscription:Subscription.get(params.subscription), curatoryGroups: []], packageMetadata = [:]
        Org contextOrg = contextService.getOrg()
        result.contextCustomerType = contextOrg.getCustomerType()
        result.institution = contextOrg
        result.showConsortiaFunctions = contextOrg.isCustomerType_Consortium()
        result.roleLinks = result.subscription.orgRelations.findAll { OrgRole oo -> !(oo.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA]) }
        result.roleObject = result.subscription
        result.roleRespValue = 'Specific subscription editor'
        result.editmode = result.subscription.isEditableBy(contextService.getUser())
        result.accessConfigEditable = contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_INST_BASIC) || (contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_BASIC) && result.subscription.getSubscriberRespConsortia().id == contextOrg.id)
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        result.subscription.packages.pkg.gokbId.each { String uuid ->
            Map queryResult = gokbService.executeQuery(apiSource.baseUrl + apiSource.fixToken + "/searchApi", [uuid: uuid])
            if (queryResult) {
                List records = queryResult.result
                packageMetadata.put(uuid, records[0])
            }
        }
        result.packageMetadata = packageMetadata
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

            Map queryResult = gokbService.executeQuery(apiSource.baseUrl + apiSource.fixToken + "/searchApi", [uuid: subscriptionPackage.pkg.gokbId])
            if (queryResult.error && queryResult.error == 404) {
                flash.error = message(code: 'wekb.error.404') as String
            } else if (queryResult) {
                List records = queryResult.result
                packageInfos.packageInstanceRecord = records ? records[0] : [:]
            }
            result.packages << packageInfos
        }

        render template: '/survey/generalPackageData', model: result
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
     * Generates a list of selectable metrics or access types for the given report types in the statistics filter
     * @return a {@link List} of available metric types
     */
    @Secured(['ROLE_USER'])
    def loadFilterList() {
        Map<String, Object> result = subscriptionControllerService.loadFilterList(params)
        result.multiple = params.multiple ? Boolean.valueOf(params.multiple) : true
        render template: "/templates/filter/statsFilter", model: result
    }

    /**
     * Retrieves a list of {@link Provider}s for table view
     * @return the result of {@link de.laser.ControlledListService#getProviders(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupProviders() {
        Map<String, Object> model = [:], result = controlledListService.getProviders(params)
        model.providerList = result.results
        model.tmplShowCheckbox = true
        model.tmplConfigShow = ['sortname', 'name', 'altname', 'isWekbCurated']
        model.fixedHeader = 'la-ignore-fixed'
        render template: "/templates/filter/providerFilterTable", model: model
    }

    /**
     * Retrieves a list of {@link Vendor}s for table view
     * @return the result of {@link de.laser.ControlledListService#getVendors(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupVendors() {
        Map<String, Object> model = [:], result = controlledListService.getVendors(params)
        model.vendorList = result.results
        model.tmplShowCheckbox = true
        model.tmplConfigShow = ['sortname', 'name', 'isWekbCurated', 'linkVendors']
        model.fixedHeader = 'la-ignore-fixed'
        render template: "/templates/filter/vendorFilterTable", model: model
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
        Org contextOrg = contextService.getOrg()
        Map<String, Object> result = taskService.getPreconditions(contextOrg)
        result.contextOrg = contextOrg

        render template: "/templates/tasks/modal_create", model: result
    }

    /**
     * Opens the task editing modal
     */
    @Secured(['ROLE_USER'])
    def editTask() {
        Map<String, Object> result = [:]
        result.params = params
        result.taskInstance = Task.get(params.id)
        result.contextOrg = contextService.getOrg()

        if (result.taskInstance){
            render template: "/templates/tasks/modal_edit", model: result
        }
    }

    /**
     * Opens the task reading modal
     */
    @Secured(['ROLE_USER'])
    def readTask() {
        Map<String, Object> result = [:]
        result.params = params
        result.taskInstance = Task.get(params.id)
        result.contextOrg = contextService.getOrg()

        if (result.taskInstance) {
            render template: "/templates/tasks/modal_read", model: result
        }
    }

    /**
     * Opens the address creation modal and sets the underlying parameters
     */
    @Secured(['ROLE_USER'])
    def createAddress() {
        Map<String, Object> model = [:]
        model.prsId = params.prsId
        model.redirect = params.redirect
        model.typeId = params.typeId ? Long.valueOf(params.typeId) : null
        model.hideType = params.hideType
        model.contextOrg = contextService.getOrg()

        switch(params.addressFor) {
            case 'addressForInstitution':
                if(params.orgId)
                    model.orgId = params.orgId
                else
                    model.orgList = Org.executeQuery("from Org o where exists (select roletype from o.orgType as roletype where roletype.id = :orgType ) and o.sector.id = :orgSector order by LOWER(o.sortname) nulls last", [orgSector: RDStore.O_SECTOR_HIGHER_EDU.id, orgType: RDStore.OT_INSTITUTION.id])
                model.tenant = model.contextOrg.id
                break
            case 'addressForProvider':
                if(params.providerId)
                    model.providerId = params.providerId
                else
                    model.providerList = Provider.executeQuery("from Provider p order by LOWER(p.sortname), LOWER(p.name)")
                model.tenant = model.contextOrg.id
                break
            case 'addressForVendor':
                if(params.vendorId)
                    model.vendorId = params.vendorId
                else
                    model.vendorList = Vendor.executeQuery("from Vendor v order by LOWER(v.sortname), LOWER(v.name)")
                model.tenant = model.contextOrg.id
                break
            default: model.orgId = params.orgId ?: model.contextOrg.id
                break
        }

        if (model.orgId && model.typeId) {
            String messageCode = 'addressFormModalLibraryAddress'
            if (model.typeId == RDStore.ADDRESS_TYPE_LEGAL_PATRON.id)  {
                messageCode = 'addressFormModalLegalPatronAddress'
            }
            else if (model.typeId == RDStore.ADDRESS_TYPE_BILLING.id)  {
                messageCode = 'addressFormModalBillingAddress'
            }
            else if (model.typeId == RDStore.ADDRESS_TYPE_POSTAL.id)   {
                messageCode = 'addressFormModalPostalAddress'
            }
            else if (model.typeId == RDStore.ADDRESS_TYPE_DELIVERY.id) {
                messageCode = 'addressFormModalDeliveryAddress'
            }
            else if (model.typeId == RDStore.ADDRESS_TYPE_LIBRARY.id)  {
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
            /*if(model.addressInstance.prs) {
                model.modalText = message(code: 'default.edit.label', args: [message(code: messageCode)]) + ' (' + model.addressInstance.prs.toString() + ')'
            }*/
            if(model.addressInstance.org) {
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
        result.org = params.org ? Org.get(params.long('org')) : null
        result.provider = params.provider ? Provider.get(params.long('provider')) : null
        result.vendor = params.vendor ? Vendor.get(params.long('vendor')) : null
        result.functions = [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, RDStore.PRS_FUNC_CONTACT_PRS, RDStore.PRS_FUNC_FC_BILLING_ADDRESS, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_OA_CONTACT]
        if (result.contextOrg.isCustomerType_Consortium() || result.contextOrg.isCustomerType_Support()) {
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
            case 'contactPersonForProvider':
            case 'contactPersonForProviderPublic':
                result.isPublic    = params.contactFor == 'contactPersonForProviderPublic'
                Set<RefdataValue> excludes = [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FC_DELIVERY_ADDRESS]
                if(params.existsWekbRecord)
                    excludes.addAll([RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_SERVICE_SUPPORT, RDStore.PRS_FUNC_METADATA])
                result.functions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - excludes
                result.positions = [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_DIREKTION, RDStore.PRS_POS_DIREKTION_ASS, RDStore.PRS_POS_RB, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS, RDStore.PRS_POS_TS]
                if (result.provider) {
                    result.modalText = message(code: "person.create_new.contactPersonForProvider.label") + ' (' + result.provider.sortname + ')'
                }
                else {
                    result.modalText = message(code: "person.create_new.contactPersonForProvider.label")
                    result.provList = Provider.executeQuery("from Provider p order by LOWER(p.sortname), LOWER(p.name)")
                }
                break
            case 'contactPersonForVendor':
            case 'contactPersonForVendorPublic':
                result.isPublic    = params.contactFor == 'contactPersonForVendorPublic'
                Set<RefdataValue> excludes = [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FC_DELIVERY_ADDRESS]
                if(params.existsWekbRecord)
                    excludes.addAll([RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_SERVICE_SUPPORT, RDStore.PRS_FUNC_METADATA])
                result.functions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - excludes
                result.positions = [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_DIREKTION, RDStore.PRS_POS_DIREKTION_ASS, RDStore.PRS_POS_RB, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS, RDStore.PRS_POS_TS]
                if (result.vendor) {
                    result.modalText = message(code: "person.create_new.contactPersonForVendor.label") + ' (' + result.vendor.sortname + ')'
                }
                else {
                    result.modalText = message(code: "person.create_new.contactPersonForVendor.label")
                    result.venList = Vendor.executeQuery("from Vendor v order by LOWER(v.sortname), LOWER(v.name)")
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
            result.vendor = PersonRole.executeQuery("select distinct(pr.vendor) from PersonRole as pr where pr.prs = :person ", [person: result.personInstance])[0]
            result.provider = PersonRole.executeQuery("select distinct(pr.provider) from PersonRole as pr where pr.prs = :person ", [person: result.personInstance])[0]
            result.functions = [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, RDStore.PRS_FUNC_CONTACT_PRS, RDStore.PRS_FUNC_FC_BILLING_ADDRESS, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_OA_CONTACT]
            if (contextOrg.isCustomerType_Consortium() || contextOrg.isCustomerType_Support()) {
                result.functions << RDStore.PRS_FUNC_GASCO_CONTACT
            }
            result.positions = PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION) - [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS]

            if (result.org || (params.org && params.org instanceof String)) {
                result.org = params.org ? Org.get(params.long('org')) : result.org
                List allOrgTypeIds =result.org.getAllOrgTypeIds()
                result.modalText = message(code: 'default.edit.label', args: [message(code: "person.contactPersonForInstitution.label")]) + ' (' + result.org.toString() + ')'
            }
            else if(result.provider != null || params.containsKey('provider')) {
                Provider prv = Provider.get(params.long('provider'))
                if(prv)
                    result.provider = prv
                result.functions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FC_DELIVERY_ADDRESS]
                result.positions = [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_DIREKTION, RDStore.PRS_POS_DIREKTION_ASS, RDStore.PRS_POS_RB, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS, RDStore.PRS_POS_TS]
                result.modalText = message(code: 'default.edit.label', args: [message(code: "person.contactPersonForProvider.label")]) + ' (' + result.provider.toString() + ')'
                result.contactPersonForProviderPublic = result.personInstance.isPublic
            }
            else if(result.vendor != null || params.containsKey('vendor')) {
                Vendor ven = Provider.get(params.long('vendor'))
                if(ven)
                    result.provider = ven
                result.functions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FC_DELIVERY_ADDRESS]
                result.positions = [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_DIREKTION, RDStore.PRS_POS_DIREKTION_ASS, RDStore.PRS_POS_RB, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS, RDStore.PRS_POS_TS]
                result.modalText = message(code: 'default.edit.label', args: [message(code: "person.contactPersonForVendor.label")]) + ' (' + result.provider.toString() + ')'
                result.contactPersonForVendorPublic = result.personInstance.isPublic
            }
            else {
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

    /**
     * Opens the modal for selection title with kbart upload
     */
    @Secured(['ROLE_USER'])
    def kbartSelectionUpload() {
        log.debug('ajaxHtmlController.kbartSelectionUpload ' + params)
        Map<String,Object> result = [subscription:Subscription.get(params.id)]
        Org contextOrg = contextService.getOrg()
        result.institution = contextOrg
        result.tab = params.tab

        if(params.surveyConfigID){
            result.surveyConfig = SurveyConfig.findById(params.surveyConfigID)
        }

        render template: '/subscription/KBARTSelectionUploadFormModal', model: result
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
        SurveyConfig surveyConfig = params.surveyConfigID ? SurveyConfig.get(params.surveyConfigID) : surveyInfo.surveyConfigs[0]
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
                noParticipation = (SurveyResult.findByParticipantAndSurveyConfigAndType(contextOrg, surveyConfig, PropertyStore.SURVEY_PROPERTY_PARTICIPATION).refValue == RDStore.YN_NO)
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
    @DebugInfo(isInstUser_denySupport_or_ROLEADMIN = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)
    })
    def reporting() {
        Map<String, Object> result = [
            tab: params.tab
        ]

        String cachePref = ReportingCache.CTX_GLOBAL + '/' + BeanStore.getContextService().getUser().id // user bound
        EhcacheWrapper ttl3600 = cacheService.getTTL3600Cache(cachePref)

        List<String> reportingKeys = ttl3600.getKeys().findAll { it.startsWith(cachePref + '_') } as List<String>
        List<String> reportingTokens = reportingKeys.collect { it.replace(cachePref + '_', '')}

        if (params.context == BaseConfig.KEY_MYINST) {

            if (params.cmd == 'deleteHistory') {
                reportingTokens.each {it -> ttl3600.remove( it ) }
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
        result.bookmarks     = ReportingFilter.findAllByOwner( contextService.getUser(), [sort: 'lastUpdated', order: 'desc'] )
        result.filterHistory = reportingTokens.sort { a,b -> ttl3600.get(b).meta.timestamp <=> ttl3600.get(a).meta.timestamp }.take(5)

        render template: '/myInstitution/reporting/historyAndBookmarks', model: result
    }

    /**
     * Retrieves the details for the given charts
     */
    @DebugInfo(isInstUser_denySupport_or_ROLEADMIN = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)
    })
    def chartDetails() {
        // TODO - SESSION TIMEOUTS

        Map<String, Object> result = [
            token:  params.token,
            query:  params.query
        ]
        result.id = params.id ? (params.id != 'null' ? params.long('id') : '') : ''

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
    @DebugInfo(isInstUser_denySupport_or_ROLEADMIN = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)
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
                    view    = '/myInstitution/reporting/export/pdf/pdfTmpl_generic_details'
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
                    view    = '/subscription/reporting/export/pdf/pdfTmpl_generic_details'
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
    @DebugInfo(isInstUser_denySupport_or_ROLEADMIN = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)
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
                    view: '/myInstitution/reporting/export/pdf/pdfTmpl_generic_query',
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

//                render view: '/myInstitution/reporting/export/pdf/pdf_generic_query', model: model
        }
    }

    /**
     * Call to render the flyout containing the steps of a given workflow
     * @return the template containing the data for the flyout
     */
    @Secured(['ROLE_USER'])
    def workflowFlyout() {
        Map<String, Object> result = [
                tmplCmd:    'usage',
                tmplFormUrl: createLink(controller: 'myInstitution', action: 'currentWorkflows')
        ]

        if (params.cmd) {
            String[] cmd = params.cmd.split(':')

            if (cmd[1] in [WfChecklist.KEY, WfCheckpoint.KEY] ) {
                result.putAll( workflowService.executeCmd(params) )
            }
        }
//        if (params.info) {
//            result.info = params.info // @ currentWorkflows @ dashboard
//        }

        if (params.key) {
            String[] key = (params.key as String).split(':')
            result.prefix = key[0]

            if (result.prefix == WfChecklist.KEY) {
                result.clist      = WfChecklist.get( key[1] )
            }
//            else if (result.prefix == WfCheckpoint.KEY) {
//                result.checkpoint     = WfCheckpoint.get( key[1] )
//                result.tmplModalTitle = g.message(code:'task.label') + ': ' +  result.checkpoint.title
//            }
        }
        result.referer = request.getHeader('referer')

        render template: '/templates/workflow/flyout', model: result
    }

    /**
     * Opens the modal for a given object, containing workflow details for the given object. Along that, the form
     * processing parameters are set according to the object type being treated
     * @return the template fragment for the workflow modal
     */
    @Secured(['ROLE_USER'])
    def workflowModal() {
        Map<String, Object> result = [
                tmplCmd:    'usage',
                tmplFormUrl: createLink(controller: 'myInstitution', action: 'currentWorkflows')
        ]

        if (params.key) {
            String[] key = (params.key as String).split(':')
            //println key

            result.prefix = key[3]

            // myInstitution::action:WF_X:id
            // subscription:id:action:WF_X:id
            if (key[0] in [License.class.name, Org.class.name, Provider.class.name, Subscription.class.name, Vendor.class.name]) {

                if (key[0] == License.class.name) {
                    result.targetObject = License.get( key[1] )
                    result.tmplFormUrl  = createLink(controller: 'lic', action: key[2], id: key[1])
                }
                else if (key[0] == Org.class.name){
                    result.targetObject = Org.get( key[1] )
                    result.tmplFormUrl  = createLink(controller: 'org', action: key[2], id: key[1])
                }
                else if (key[0] == Provider.class.name){
                    result.targetObject = Provider.get( key[1] )
                    result.tmplFormUrl  = createLink(controller: 'provider', action: key[2], id: key[1])
                }
                else if (key[0] == Subscription.class.name) {
                    result.targetObject = Subscription.get( key[1] )
                    result.tmplFormUrl  = createLink(controller: 'subscription', action: key[2], id: key[1])
                }
                else if (key[0] == Vendor.class.name) {
                    result.targetObject = Vendor.get( key[1] )
                    result.tmplFormUrl  = createLink(controller: 'vendor', action: key[2], id: key[1])
                }

            }
            else if (key[0] == 'myInstitution') {
                result.tmplFormUrl  = createLink(controller: 'myInstitution', action: key[2])
                if (key[2] == 'dashboard') {
                    result.tmplFormUrl = result.tmplFormUrl + '?view=Workflows'
                }
            }

            if (result.prefix == WfChecklist.KEY) {
                result.checklist      = WfChecklist.get( key[4] )
                result.tmplModalTitle = g.message(code:'workflow.label') as String
            }
            else if (result.prefix == WfCheckpoint.KEY) {
                result.checkpoint     = WfCheckpoint.get( key[4] )

                if (result.checkpoint.done) {
                    result.tmplModalTitle = '<i class="icon ' + WorkflowHelper.getCssIconAndColorByStatus( RDStore.WF_TASK_STATUS_DONE ) + '"></i>&nbsp;' + result.checkpoint.title
                }
                else {
                    result.tmplModalTitle = '<i class="icon ' + WorkflowHelper.getCssIconAndColorByStatus( RDStore.WF_TASK_STATUS_OPEN ) + '"></i>&nbsp;' + result.checkpoint.title
                }
            }
        }
        if (params.info) {
            result.info = params.info
        }

        render template: '/templates/workflow/modal', model: result
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

        render template: "/templates/titles/title_modal", model: result

    }

    /**
     * Retrieves detailed title information to a given entitlement and opens a modal showing those details
     */
    @Secured(['ROLE_USER'])
    Map<String,Object> showAllTitleInfosAccordion() {
        Map<String, Object> result = [:]

        result.apisources = ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

        result.tipp = params.tippID ? TitleInstancePackagePlatform.get(params.tippID) : null
        result.ie = params.ieID ? IssueEntitlement.get(params.ieID) : null
        result.showPackage = params.showPackage
        result.showPlattform = params.showPlattform
        result.showCompact = params.showCompact
        result.showEmptyFields = params.showEmptyFields

        render template: "/templates/titles/title_long_accordion", model: result

    }

    /**
     * Opens a modal containing a preview of the given document if rights are granted and the file being found.
     * The preview is being generated according to the MIME type of the requested document; the document key is
     * expected in structure docUUID:docContextID
     * @return the template containing a preview of the document (either document viewer or fulltext extract)
     */
    @Secured(['ROLE_USER'])
    def documentPreview() {
        Map<String, Object> result = [:]

        try {
            if (params.key) {
                String[] keys = params.key.split(':')

                Doc doc = Doc.findByUuid(keys[0])
                DocContext docCtx = DocContext.findByIdAndOwner(Long.parseLong(keys[1]), doc)

                if (doc && docCtx) {
                    result.doc = doc
                    result.docCtx = docCtx

                    Closure checkPermission = {
                        // logic based on /views/templates/documents/card

                        boolean check = false
                        long ctxOrgId = contextService.getOrg().id

                        if ( doc.owner.id == ctxOrgId ) {
                            check = true
                        }
                        else if ( docCtx.shareConf ) {
                            if ( docCtx.shareConf == RDStore.SHARE_CONF_UPLOADER_ORG ) {
                                check = (doc.owner.id == ctxOrgId)
                            }
                            if ( docCtx.shareConf == RDStore.SHARE_CONF_UPLOADER_AND_TARGET ) {
                                check = (doc.owner.id == ctxOrgId) || (docCtx.targetOrg.id == ctxOrgId)
                            }
                            if ( docCtx.shareConf == RDStore.SHARE_CONF_ALL ) {
                                // context based restrictions must be applied
                                check = true
                            }
                        }
                        else if ( docCtx.sharedFrom ) {
                            if (docCtx.license) {
                                docCtx.license.orgRelations.each {
                                    if (it.org.id == ctxOrgId && it.roleType in [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]) {
                                        check = true
                                    }
                                }
                            }
                            else if (docCtx.subscription) {
                                docCtx.subscription.orgRelations.each {
                                    if (it.org.id == ctxOrgId && it.roleType in [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER]) {
                                        check = true
                                    }
                                }
                            }
                        }
                        // survey workaround
                        else if ( docCtx.surveyConfig ) {
                            Map orgIdMap = docCtx.surveyConfig.getSurveyOrgsIDs()
                            if (contextService.getOrg().id in orgIdMap.orgsWithSubIDs || contextService.getOrg().id in orgIdMap.orgsWithoutSubIDs) {
                                check = true
                            }
                        }
                        return check
                    }

                    if (checkPermission()) {
                        Map<String, String> mimeTypes = Doc.getPreviewMimeTypes()
                        if (mimeTypes.containsKey(doc.mimeType)) {
                            String fPath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK
                            File f = new File(fPath + '/' +  doc.uuid)

                            if (f.exists()) {

                                if (mimeTypes.get(doc.mimeType) == 'raw'){
                                    result.docBase64 = f.getBytes().encodeBase64()
                                    result.docDataType = doc.mimeType
                                }
                                else if (mimeTypes.get(doc.mimeType) == 'encode') {
                                    String fCharset = UniversalDetector.detectCharset(f) ?: Charset.defaultCharset()
                                    result.docBase64 = f.getText(fCharset).encodeAsRaw().getBytes().encodeBase64()
                                    result.docDataType = 'text/plain;charset=' + fCharset
                                }
                                else {
                                    result.error = 'Unbekannter Fehler'
                                }
                                // encodeAsHTML().replaceAll(/\r\n|\r|\n/,'<br />')
                            }
                            else {
                                result.error = message(code: 'template.documents.preview.fileNotFound') as String
                            }
                        }
                        else {
                            result.error = message(code: 'template.documents.preview.unsupportedMimeType') as String
                        }
                    }
                    else {
                        result.info = message(code: 'template.documents.preview.forbidden') as String
                    }
                }
            }
        }
        catch (Exception e) {
            log.error e.getMessage()
        }

        render template: '/templates/documents/preview', model: result
    }
}