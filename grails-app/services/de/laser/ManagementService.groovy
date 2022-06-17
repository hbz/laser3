package de.laser


import com.k_int.kbplus.GenericOIDService
import com.k_int.kbplus.PackageService
import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.ctrl.MyInstitutionControllerService
import de.laser.ctrl.SubscriptionControllerService
import de.laser.finance.CostItem
import de.laser.helper.AppUtils
import de.laser.helper.ConfigUtils
import de.laser.helper.DateUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import grails.core.GrailsClass
import grails.gorm.transactions.Transactional
import grails.web.mvc.FlashScope
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.transaction.TransactionStatus

import javax.servlet.http.HttpServletRequest
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService

/**
 * This service is for the consortial subscription's member management handling
 */
@Transactional
class ManagementService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AccessService accessService
    FormService formService
    AddressbookService addressbookService
    SubscriptionService subscriptionService
    ContextService contextService
    ExecutorService executorService
    AuditService auditService
    GenericOIDService genericOIDService
    MessageSource messageSource
    SubscriptionControllerService subscriptionControllerService
    MyInstitutionControllerService myInstitutionControllerService
    PackageService packageService

    /**
     * The overall menu of the calls - determines which data should be processed and which tab should be opened as next
     * @param controller the controller instance
     * @param parameterMap the request parameter map
     * @param input_file an uploaded document which should be passed to the members
     * @return the map containing the (updated) view parameters
     */
    Map subscriptionsManagement(def controller, GrailsParameterMap parameterMap, def input_file = null) {
        Map<String, Object> result = [:]

        switch (parameterMap.tab) {
            case "linkLicense":
                    if(parameterMap.processOption) {
                        processLinkLicense(controller, parameterMap)
                        parameterMap.remove('processOption')
                    }
                    result << linkLicense(controller, parameterMap)
                break
            case "linkPackages":
                    if(parameterMap.processOption) {
                        processLinkPackages(controller, parameterMap)
                    }
                    result << linkPackages(controller, parameterMap)
                break
            case "properties":
                    if(parameterMap.processOption) {
                        processProperties(controller, parameterMap)
                        parameterMap.remove('processOption')
                    }
                    result << properties(controller, parameterMap)
                break
            case "generalProperties":
                    if(parameterMap.processOption) {
                        processSubscriptionProperties(controller, parameterMap)
                        parameterMap.remove('processOption')
                    }
                    result << subscriptionProperties(controller, parameterMap)
                break
            case "providerAgency":
                result << subscriptionProperties(controller, parameterMap)
                break
            case "multiYear":
                if(parameterMap.processOption) {
                    processSubscriptionProperties(controller, parameterMap)
                    parameterMap.remove('processOption')
                }
                result << subscriptionProperties(controller, parameterMap)
                break
            case "notes":
                if(parameterMap.processOption) {
                    processNotes(controller, parameterMap)
                    parameterMap.remove('processOption')
                }
                result << subscriptionProperties(controller, parameterMap)
                break
            case "documents":
                if(parameterMap.processOption && input_file) {
                    processDocuments(controller, parameterMap, input_file)
                    parameterMap.remove('processOption')
                }

                result << subscriptionProperties(controller, parameterMap)
                break
            case "customerIdentifiers":
                result << customerIdentifierMembers(controller, parameterMap)
                break
        }

        //println(result)
        result.result

    }

    //--------------------------------------------- subscriptions management section for SubscriptionController-------------------------------------------------

    /**
     * Lists the customer numbers of the subscription members for the linked platforms. This is necessary for statistics data loading
     * as those are the key-value pairs which will authenticate the caller for the SUSHI call!
     * @param controller the controller instance
     * @param params the request parameter map
     * @return OK with the data if access to this view is granted, ERROR otherwise
     * @see CustomerIdentifier
     */
    Map<String, Object> customerIdentifierMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params)
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            result.platforms = Platform.executeQuery('select plat from Platform plat where plat.org in (select oo.org from OrgRole oo where oo.pkg in (select sp.pkg from SubscriptionPackage sp where sp.subscription = :parentSub)) and plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :parentSub))', [parentSub: result.subscription]) as Set<Platform>
            if(!params.tabPlat && result.platforms)
                result.tabPlat = result.platforms[0].id
            else result.tabPlat = params.long('tabPlat')
            result.members = Org.executeQuery("select org from OrgRole oo join oo.sub sub join oo.org org where sub.instanceOf = :parent and oo.roleType in (:subscrTypes) order by org.sortname asc, org.name asc",[parent: result.subscription, subscrTypes: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]) as Set<Org>
            result.keyPairs = []
            result.platforms.each { Platform platform ->
                if(platform.id == result.tabPlat) {
                    result.members.each { Org customer ->
                        //create dummies for that they may be xEdited - OBSERVE BEHAVIOR for eventual performance loss!
                        CustomerIdentifier keyPair = CustomerIdentifier.findByPlatformAndCustomer(platform, customer)
                        if(!keyPair) {
                            keyPair = new CustomerIdentifier(platform: platform,
                                    customer: customer,
                                    type: RefdataValue.getByValueAndCategory('Default', RDConstants.CUSTOMER_IDENTIFIER_TYPE),
                                    owner: contextService.getOrg(),
                                    isPublic: true)
                            if(!keyPair.save()) {
                                log.warn(keyPair.errors.getAllErrors().toListString())
                            }
                        }
                        result.keyPairs << keyPair
                    }
                }
            }
            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Unsets the given customer number
     * @param id the customer number ID to unser
     * @return true if the unsetting was successful, false otherwise
     */
    boolean deleteCustomerIdentifier(Long id) {
        CustomerIdentifier ci = CustomerIdentifier.get(id)
        ci.value = null
        ci.requestorKey = null
        ci.save()
    }

    //--------------------------------------------- general subscriptions management section -------------------------------------------------

    /**
     * Lists the current license links of the members
     * @param controller the controller instance
     * @param params the request parameter map
     * @return OK with the data if access to this view is granted, ERROR otherwise
     */
    Map<String,Object> linkLicense(def controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else{

            if(controller instanceof SubscriptionController) {
                result.parentLicenses = Links.executeQuery('select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType',[subscription:result.subscription,linkType: RDStore.LINKTYPE_LICENSE])
                result.validLicenses = []
                if(result.parentLicenses) {
                    result.validLicenses.addAll(License.findAllByInstanceOfInList(result.parentLicenses))
                }
                result.filteredSubscriptions = subscriptionControllerService.getFilteredSubscribers(params,result.subscription)
            }

            if(controller instanceof MyInstitutionController) {
                result.putAll(subscriptionService.getMySubscriptions(params,result.user,result.institution))

                result.filteredSubscriptions = result.subscriptions

                String base_qry
                Map qry_params

                if (accessService.checkPerm("ORG_INST")) {
                    base_qry = "from License as l where ( exists ( select o from l.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType = :roleType2 ) AND o.org = :lic_org ) ) )"
                    qry_params = [roleType1:RDStore.OR_LICENSEE, roleType2:RDStore.OR_LICENSEE_CONS, lic_org:result.institution]
                }
                else if (accessService.checkPerm("ORG_CONSORTIUM")) {
                    base_qry = "from License as l where exists ( select o from l.orgRelations as o where ( o.roleType = :roleTypeC AND o.org = :lic_org AND l.instanceOf is null AND NOT exists ( select o2 from l.orgRelations as o2 where o2.roleType = :roleTypeL ) ) )"
                    qry_params = [roleTypeC:RDStore.OR_LICENSING_CONSORTIUM, roleTypeL:RDStore.OR_LICENSEE_CONS, lic_org:result.institution]
                }
                else {
                    base_qry = "from License as l where exists ( select o from l.orgRelations as o where  o.roleType = :roleType AND o.org = :lic_org ) "
                    qry_params = [roleType:RDStore.OR_LICENSEE_CONS, lic_org:result.institution]
                }

                result.validLicenses = License.executeQuery( "select l " + base_qry, qry_params )

            }

            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Processes the given input and performs (un-)linking of the selected members to the given license(s)
     * @param controller the controller instance
     * @param params the request parameter map
     */
    void processLinkLicense(def controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params)
        if(result.editable && formService.validateToken(params)) {
            Locale locale = LocaleContextHolder.getLocale()
            FlashScope flash = getCurrentFlashScope()
            List selectedSubs = params.list("selectedSubs"), selectedLicenseIDs = params.list("selectedLicense")
            if(selectedSubs && selectedLicenseIDs[0]) {
                List<License> selectedLicenses = License.findAllByIdInList(selectedLicenseIDs.collect { String key -> Long.parseLong(key) })
                selectedLicenses.each { License newLicense ->
                    if (params.processOption == 'linkLicense' || params.processOption == 'unlinkLicense') {
                        Set<Subscription> subscriptions = Subscription.findAllByIdInList(selectedSubs)
                        List<GString> changeAccepted = []
                        subscriptions.each { Subscription subscription ->
                            if (subscription.isEditableBy(result.user)) {
                                if (newLicense && subscriptionService.setOrgLicRole(subscription, newLicense, params.processOption == 'unlinkLicense'))
                                    changeAccepted << "${subscription.name} (${messageSource.getMessage('subscription.linkInstance.label', null, locale)} ${subscription.getSubscriber().sortname})"
                            }
                        }
                        if (changeAccepted) {
                            flash.message = changeAccepted.join('<br>')
                        }
                    }
                }
            }
            else{
                if (selectedSubs.size() < 1) {
                    flash.error = messageSource.getMessage('subscriptionsManagement.noSelectedSubscriptions', null, locale)
                }
                if (!selectedLicenseIDs[0]) {
                    flash.error = messageSource.getMessage('subscriptionsManagement.noSelectedLicense', null, locale)
                }
            }
        }
    }

    /**
     * Lists the current package links of the members
     * @param controller the controller instance
     * @param params the request parameter map
     * @return OK with the data if access to this view is granted, ERROR otherwise
     */
    Map<String,Object> linkPackages(def controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            if(controller instanceof SubscriptionController) {
                Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
                Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
                threadArray.each {
                    if (it.name == 'PackageTransfer_'+result.subscription.id) {
                        result.isLinkingRunning = true
                    }
                }
                result.validPackages = result.subscription.packages
                result.filteredSubscriptions = subscriptionControllerService.getFilteredSubscribers(params,result.subscription)
                if(result.filteredSubscriptions)
                    result.childWithCostItems = CostItem.executeQuery('select ci.subPkg from CostItem ci where ci.subPkg.subscription in (:filteredSubChildren) and ci.costItemStatus != :deleted and ci.owner = :context',[context:result.institution, deleted:RDStore.COST_ITEM_DELETED, filteredSubChildren:result.filteredSubscriptions.collect { row -> row.sub }])
            }

            if(controller instanceof MyInstitutionController) {
                Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
                Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
                threadArray.each {
                    if (it.name == 'PackageTransfer_'+result.user.id) {
                        result.isLinkingRunning = true
                    }
                }
                result.validPackages = Package.findAllByGokbIdIsNotNullAndPackageStatusNotEqual(RDStore.PACKAGE_STATUS_DELETED)

                result.putAll(subscriptionService.getMySubscriptions(params,result.user,result.institution))

                result.filteredSubscriptions = result.subscriptions
                if(result.filteredSubscriptions)
                    result.childWithCostItems = CostItem.executeQuery('select ci.subPkg from CostItem ci where ci.subPkg.subscription in (:filteredSubscriptions) and ci.costItemStatus != :deleted and ci.owner = :context',[context:result.institution, deleted:RDStore.COST_ITEM_DELETED, filteredSubscriptions:result.filteredSubscriptions])
            }

            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Processes the given input and performs (un-)linking of the selected members to the given package(s).
     * If specified, titles will be generated or deleted as well
     * @param controller the controller instance
     * @param params the request parameter map
     */
    void processLinkPackages(def controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params)
        if (result.editable && formService.validateToken(params)) {
            FlashScope flash = getCurrentFlashScope()
            Locale locale = LocaleContextHolder.getLocale()
            List selectedSubs = params.list("selectedSubs")
            result.message = []
            result.error = []
            if (result.subscription && (params.processOption == 'allWithoutTitle' || params.processOption == 'allWithTitle')) {
                List<SubscriptionPackage> validSubChildPackages = SubscriptionPackage.executeQuery("select sp from SubscriptionPackage sp join sp.subscription sub where sub.instanceOf = :parent", [parent: result.subscription])
                validSubChildPackages.each { SubscriptionPackage sp ->
                    if (!CostItem.executeQuery('select ci from CostItem ci where ci.subPkg = :sp and ci.costItemStatus != :deleted and ci.owner = :context', [sp: sp, deleted: RDStore.COST_ITEM_DELETED, context: result.institution])) {
                        if (params.processOption == 'allWithTitle') {
                            if (packageService.unlinkFromSubscription(sp.pkg, sp.subscription, result.institution, true)) {
                                Object[] args = [sp.pkg.name, sp.subscription.getSubscriber().name]
                                result.message << messageSource.getMessage('subscriptionsManagement.unlinkInfo.withIE.successful', args, locale)
                            } else {
                                Object[] args = [sp.pkg.name, sp.subscription.getSubscriber().name]
                                result.error << messageSource.getMessage('subscriptionsManagement.unlinkInfo.withIE.fail', args, locale)
                            }
                        } else {
                            if (packageService.unlinkFromSubscription(sp.pkg, sp.subscription, result.institution, false)) {
                                Object[] args = [sp.pkg.name, sp.subscription.getSubscriber().name]
                                result.message << messageSource.getMessage('subscriptionsManagement.unlinkInfo.onlyPackage.successful', args, locale)
                            } else {
                                Object[] args = [sp.pkg.name, sp.subscription.getSubscriber().name]
                                result.error << messageSource.getMessage('subscriptionsManagement.unlinkInfo.onlyPackage.fail', args, locale)
                            }
                        }
                    } else {
                        Object[] args = [sp.pkg.name, sp.subscription.getSubscriber().name]
                        result.error << messageSource.getMessage('subscriptionsManagement.unlinkInfo.costsExisting', args, locale)
                    }
                }
            } else if (selectedSubs && params.selectedPackage && params.processOption) {
                Package pkg_to_link
                SubscriptionPackage subscriptionPackage
                String threadName
                if(controller instanceof SubscriptionController) {
                    subscriptionPackage = SubscriptionPackage.get(params.selectedPackage)
                    pkg_to_link = subscriptionPackage.pkg
                    threadName = "PackageTransfer_${result.subscription.id}"
                }

                if(controller instanceof MyInstitutionController) {
                    pkg_to_link = Package.get(params.selectedPackage)
                    threadName = "PackageTransfer_${result.user.id}"
                }

                if (pkg_to_link) {
                    List<Subscription> editableSubs = []
                    selectedSubs.each { id ->
                        Subscription subscription = Subscription.get(Long.parseLong(id))
                        if(subscription.isEditableBy(result.user)){
                            editableSubs << subscription
                        }
                    }
                    executorService.execute({
                        Thread.currentThread().setName(threadName)
                        List<Subscription> memberSubsToLink = []
                        editableSubs.each { Subscription subscription ->
                            if (params.processOption == 'linkwithIE' || params.processOption == 'linkwithoutIE') {
                                if (!(subscription.packages && (pkg_to_link.id in subscription.packages.pkg.id))) {
                                    if (params.processOption == 'linkwithIE') {
                                        if (result.subscription) {
                                            //subscriptionService.addToSubscriptionCurrentStock(subscription, result.subscription, pkg_to_link)
                                            memberSubsToLink << subscription
                                        } else {
                                            subscriptionService.addToSubscription(subscription, pkg_to_link, true)
                                        }
                                    } else {
                                        subscriptionService.addToSubscription(subscription, pkg_to_link, false)
                                    }
                                }
                            }
                            if (params.processOption == 'unlinkwithIE' || params.processOption == 'unlinkwithoutIE') {
                                if (subscription.packages && (pkg_to_link.id in subscription.packages.pkg.id)) {
                                    SubscriptionPackage subPkg = SubscriptionPackage.findBySubscriptionAndPkg(subscription, pkg_to_link)
                                    if (!CostItem.executeQuery('select ci from CostItem ci where ci.subPkg = :sp and ci.costItemStatus != :deleted and ci.owner = :context', [sp: subPkg, deleted: RDStore.COST_ITEM_DELETED, context: result.institution])) {
                                        packageService.unlinkFromSubscription(pkg_to_link, subscription, result.institution, params.processOption == 'unlinkwithIE')
                                    } else {
                                        Object[] args = [subPkg.pkg.name, subPkg.subscription.getSubscriber().name]
                                        result.error << messageSource.getMessage('subscriptionsManagement.unlinkInfo.costsExisting', args, locale)
                                    }
                                }
                            }
                        }
                        if(memberSubsToLink && result.subscription) {
                            subscriptionService.addToMemberSubscription(result.subscription, memberSubsToLink, pkg_to_link, params.processOption == 'linkwithIE')
                        }
                    })
                }
            } else {
                if (selectedSubs.size() < 1) {
                    flash.error = messageSource.getMessage('subscriptionsManagement.noSelectedSubscriptions', null, locale)
                }
                if (!params.selectedPackage) {
                    flash.error = messageSource.getMessage('subscriptionsManagement.noSelectedPackage', null, locale)
                }
            }
            if (result.error) {
                flash.error = result.error.join('<br>')
            }

            if (result.message) {
                flash.message = result.message.join('<br>')
            }
        }
    }

    /**
     * Loads the public and private properties defined for each subscription member
     * @param controller the controller instance
     * @param params the request parameter map
     * @return OK with the data if access to this view is granted, ERROR otherwise
     */
    Map<String,Object> properties(def controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {

            result.propertiesFilterPropDef = params.propertiesFilterPropDef ? genericOIDService.resolveOID(params.propertiesFilterPropDef.replace(" ", "")) : null

            params.remove('propertiesFilterPropDef')

            if(controller instanceof SubscriptionController) {
                Set<Subscription> validSubChildren = Subscription.executeQuery("select oo.sub from OrgRole oo where oo.sub.instanceOf = :parent and oo.roleType = :roleType order by oo.org.sortname asc", [parent: result.subscription, roleType: RDStore.OR_SUBSCRIBER_CONS])
                if (validSubChildren) {
                    Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :ctx and sp.instanceOf = null", [subscriptionSet: validSubChildren, ctx: result.institution])
                    propList.addAll(result.subscription.propertySet.type)
                    result.propList = propList
                    result.filteredSubscriptions = validSubChildren
                    List<Subscription> childSubs = result.subscription.getNonDeletedDerivedSubscriptions()
                    if (childSubs) {
                        String localizedName
                        switch (LocaleContextHolder.getLocale()) {
                            case Locale.GERMANY:
                            case Locale.GERMAN: localizedName = "name_de"
                                break
                            default: localizedName = "name_en"
                                break
                        }
                        String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
                        Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet: childSubs, context: result.institution])
                        result.memberProperties = memberProperties
                    }
                }
            }

            if(controller instanceof MyInstitutionController) {

                result.putAll(subscriptionService.getMySubscriptions(params,result.user,result.institution))


                result.filteredSubscriptions = result.subscriptions
            }

            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Processes the given input and performs property manipulation for the selected members
     * @param controller the controller instance
     * @param params the request parameter map
     */
    void processProperties(def controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params)
        if(result.editable && formService.validateToken(params)) {
            Locale locale = LocaleContextHolder.getLocale()
            FlashScope flash = getCurrentFlashScope()
            PropertyDefinition propertiesFilterPropDef = params.propertiesFilterPropDef ? genericOIDService.resolveOID(params.propertiesFilterPropDef.replace(" ", "")) : null
            List selectedSubs = []
            if(params.containsKey('selectedSubs'))
                selectedSubs.addAll(params.list('selectedSubs'))
            else if(params.processOption == 'deleteAllProperties')
                selectedSubs.addAll(Subscription.findAllByInstanceOf(result.subscription))
            if (selectedSubs.size() > 0 && params.processOption && propertiesFilterPropDef) {
                int newProperties = 0
                int changeProperties = 0
                int deletedProperties = 0
                Object[] args
                    if(params.processOption == 'changeCreateProperty') {
                        if(params.filterPropValue) {
                            Set<Subscription> subscriptions = Subscription.findAllByIdInList(selectedSubs)
                            subscriptions.each { Subscription subscription ->
                                if (subscription.isEditableBy(result.user)) {
                                    List<SubscriptionProperty> existingProps = []
                                    String propDefFlag
                                    if (propertiesFilterPropDef.tenant == result.institution) {
                                        //private Property
                                        existingProps.addAll(subscription.propertySet.findAll { SubscriptionProperty sp ->
                                            sp.owner.id == subscription.id && sp.type.id == propertiesFilterPropDef.id
                                        })
                                        propDefFlag = PropertyDefinition.PRIVATE_PROPERTY
                                    } else {
                                        //custom Property
                                        existingProps.addAll(subscription.propertySet.findAll { SubscriptionProperty sp ->
                                            sp.type.id == propertiesFilterPropDef.id && sp.owner.id == subscription.id && sp.tenant.id == result.institution.id
                                        })
                                        propDefFlag = PropertyDefinition.CUSTOM_PROPERTY
                                    }
                                    if (existingProps.size() == 0 || propertiesFilterPropDef.multipleOccurrence) {
                                        AbstractPropertyWithCalculatedLastUpdated newProp = PropertyDefinition.createGenericProperty(propDefFlag, subscription, propertiesFilterPropDef, result.institution)
                                        if (newProp.hasErrors()) {
                                            log.error(newProp.errors.toString())
                                        } else {
                                            log.debug("New property created: " + newProp.type.name)
                                            newProperties++
                                            subscriptionService.updateProperty(controller, newProp, params.filterPropValue)
                                        }
                                    }
                                    if (existingProps.size() == 1) {
                                        SubscriptionProperty privateProp = SubscriptionProperty.get(existingProps[0].id)
                                        changeProperties++
                                        subscriptionService.updateProperty(controller, privateProp, params.filterPropValue)
                                    }
                                }
                            }

                            args = [newProperties, changeProperties]
                            flash.message = messageSource.getMessage('subscriptionsManagement.successful.property', args, locale)
                        }else{
                                flash.error = messageSource.getMessage('subscriptionsManagement.noPropertyValue', null, locale)
                        }

                    }else if(params.processOption == 'deleteAllProperties'){
                        List<Subscription> validSubChilds = Subscription.findAllByInstanceOf(result.subscription)
                        validSubChilds.each { Subscription subChild ->
                            SubscriptionProperty existingProp
                            if (propertiesFilterPropDef.tenant == result.institution) {
                                //private Property
                                existingProp = subChild.propertySet.find { SubscriptionProperty sp ->
                                    sp.owner.id == subChild.id && sp.type.id == propertiesFilterPropDef.id
                                }
                                if (existingProp){
                                    try {
                                        subChild.propertySet.remove(existingProp)
                                        existingProp.delete()
                                        deletedProperties++
                                    }
                                    catch (Exception e) {
                                        log.error( e.toString() )
                                    }
                                }
                            }
                            else {
                                //custom Property
                                existingProp = subChild.propertySet.find { SubscriptionProperty sp ->
                                    sp.type.id == propertiesFilterPropDef.id && sp.owner.id == subChild.id && sp.tenant.id == result.institution.id
                                }
                                if (existingProp && !(existingProp.hasProperty('instanceOf') && existingProp.instanceOf && AuditConfig.getConfig(existingProp.instanceOf))){
                                    try {
                                        subChild.propertySet.remove(existingProp)
                                        existingProp.delete()
                                        deletedProperties++
                                    }
                                    catch (Exception e){
                                        log.error( e.toString() )
                                    }
                                }
                            }
                        }
                        args = [deletedProperties]
                        result.message = messageSource.getMessage('subscriptionsManagement.deletedProperties', args, LocaleContextHolder.getLocale())
                    }
            } else {
                if (selectedSubs.size() < 1) {
                    flash.error = messageSource.getMessage('subscriptionsManagement.noSelectedSubscriptions', null, locale)
                }
                else if (!propertiesFilterPropDef) {
                    flash.error = messageSource.getMessage('subscriptionsManagement.noPropertySelected',null, locale)
                }
                else if (!params.filterPropValue) {
                    flash.error = messageSource.getMessage('subscriptionsManagement.noPropertyValue', null, locale)
                }
            }
        }
    }

    /**
     * Loads for each member subscription the general attributes
     * @param controller the controller instance
     * @param params the request parameter map
     * @return OK with the data if access to this view is granted, ERROR otherwise
     */
    Map<String,Object> subscriptionProperties(def controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params)
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {

            if(controller instanceof SubscriptionController) {
                result.filteredSubscriptions = subscriptionControllerService.getFilteredSubscribers(params,result.subscription)
            }

            if(controller instanceof MyInstitutionController) {
                result.putAll(subscriptionService.getMySubscriptions(params,result.user,result.institution))

                result.filteredSubscriptions = result.subscriptions
            }

            if(params.tab == 'providerAgency') {
                result.modalPrsLinkRole = RefdataValue.getByValueAndCategory('Specific subscription editor', RDConstants.PERSON_RESPONSIBILITY)
                result.modalVisiblePersons = addressbookService.getPrivatePersonsByTenant(result.institution)
                if(result.subscription) {
                    result.visibleOrgRelations = OrgRole.executeQuery("select oo from OrgRole oo join oo.org org where oo.sub = :parent and oo.roleType in (:roleTypes) order by org.name asc", [parent: result.subscription, roleTypes: [RDStore.OR_PROVIDER, RDStore.OR_AGENCY]])
                }
            }
            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Processes the given input and performs attribute manipulation for the selected members
     * @param controller the controller instance
     * @param params the request parameter map
     */
    void processSubscriptionProperties(def controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params)
        if(result.editable && formService.validateToken(params)) {
            Locale locale = LocaleContextHolder.getLocale()
            FlashScope flash = getCurrentFlashScope()
            List selectedSubs = params.list("selectedSubs")
            if (selectedSubs) {
                Set change = [], noChange = []
                SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                Date startDate = params.valid_from ? sdf.parse(params.valid_from) : null
                Date endDate = params.valid_to ? sdf.parse(params.valid_to) : null
                Set<Subscription> subscriptions = Subscription.findAllByIdInList(selectedSubs)
                if(params.processOption == 'changeProperties') {
                    subscriptions.each { Subscription subscription ->
                        if (subscription.isEditableBy(result.user)) {
                            if (startDate && !auditService.getAuditConfig(subscription.instanceOf, 'startDate')) {
                                subscription.startDate = startDate
                                change << messageSource.getMessage('default.startDate.label', null, locale)
                            }
                            if (startDate && auditService.getAuditConfig(subscription.instanceOf, 'startDate')) {
                                noChange << messageSource.getMessage('default.startDate.label', null, locale)
                            }
                            if (endDate && !auditService.getAuditConfig(subscription.instanceOf, 'endDate')) {
                                subscription.endDate = endDate
                                change << messageSource.getMessage('default.endDate.label', null, locale)
                            }
                            if (endDate && auditService.getAuditConfig(subscription.instanceOf, 'endDate')) {
                                noChange << messageSource.getMessage('default.endDate.label', null, locale)
                            }
                            if (params.process_status && !auditService.getAuditConfig(subscription.instanceOf, 'status')) {
                                subscription.status = RefdataValue.get(params.process_status) ?: subscription.status
                                change << messageSource.getMessage('subscription.status.label', null, locale)
                            }
                            if (params.process_status && auditService.getAuditConfig(subscription.instanceOf, 'status')) {
                                noChange << messageSource.getMessage('subscription.status.label', null, locale)
                            }
                            if (params.process_kind && !auditService.getAuditConfig(subscription.instanceOf, 'kind')) {
                                subscription.kind = RefdataValue.get(params.process_kind) ?: subscription.kind
                                change << messageSource.getMessage('subscription.kind.label', null, locale)
                            }
                            if (params.process_kind && auditService.getAuditConfig(subscription.instanceOf, 'kind')) {
                                noChange << messageSource.getMessage('subscription.kind.label', null, locale)
                            }
                            if (params.process_form && !auditService.getAuditConfig(subscription.instanceOf, 'form')) {
                                subscription.form = RefdataValue.get(params.process_form) ?: subscription.form
                                change << messageSource.getMessage('subscription.form.label', null, locale)
                            }
                            if (params.process_form && auditService.getAuditConfig(subscription.instanceOf, 'form')) {
                                noChange << messageSource.getMessage('subscription.form.label', null, locale)
                            }
                            if (params.process_resource && !auditService.getAuditConfig(subscription.instanceOf, 'resource')) {
                                subscription.resource = RefdataValue.get(params.process_resource) ?: subscription.resource
                                change << messageSource.getMessage('subscription.resource.label', null, locale)
                            }
                            if (params.process_resource && auditService.getAuditConfig(subscription.instanceOf, 'resource')) {
                                noChange << messageSource.getMessage('subscription.resource.label', null, locale)
                            }
                            if (params.process_isPublicForApi && !auditService.getAuditConfig(subscription.instanceOf, 'isPublicForApi')) {
                                subscription.isPublicForApi = RefdataValue.get(params.process_isPublicForApi) == RDStore.YN_YES
                                change << messageSource.getMessage('subscription.isPublicForApi.label', null, locale)
                            }
                            if (params.process_isPublicForApi && auditService.getAuditConfig(subscription.instanceOf, 'isPublicForApi')) {
                                noChange << messageSource.getMessage('subscription.isPublicForApi.label', null, locale)
                            }
                            if (params.process_hasPerpetualAccess && !auditService.getAuditConfig(subscription.instanceOf, 'hasPerpetualAccess')) {
                                subscription.hasPerpetualAccess = RefdataValue.get(params.process_hasPerpetualAccess) == RDStore.YN_YES
                                //subscription.hasPerpetualAccess = RefdataValue.get(params.process_hasPerpetualAccess)
                                change << messageSource.getMessage('subscription.hasPerpetualAccess.label', null, locale)
                            }
                            if (params.process_hasPerpetuaLAccess && auditService.getAuditConfig(subscription.instanceOf, 'hasPerpetualAccess')) {
                                noChange << messageSource.getMessage('subscription.hasPerpetualAccess.label', null, locale)
                            }
                            if (params.process_hasPublishComponent && !auditService.getAuditConfig(subscription.instanceOf, 'hasPublishComponent')) {
                                subscription.hasPublishComponent = RefdataValue.get(params.process_hasPublishComponent) == RDStore.YN_YES
                                change << messageSource.getMessage('subscription.hasPublishComponent.label', null, locale)
                            }
                            if (params.process_hasPublishComponent && auditService.getAuditConfig(subscription.instanceOf, 'hasPublishComponent')) {
                                noChange << messageSource.getMessage('subscription.hasPublishComponent.label', null, locale)
                            }
                            if (params.process_isMultiYear && !auditService.getAuditConfig(subscription.instanceOf, 'isMultiYear')) {
                                subscription.isMultiYear = RefdataValue.get(params.process_isMultiYear) == RDStore.YN_YES
                                change << messageSource.getMessage('subscription.isMultiYear.label', null, locale)
                            }
                            if (params.process_isMultiYear && auditService.getAuditConfig(subscription.instanceOf, 'isMultiYear')) {
                                noChange << messageSource.getMessage('subscription.isMultiYear.label', null, locale)
                            }

                            if (params.process_isAutomaticRenewAnnually && !auditService.getAuditConfig(subscription.instanceOf, 'isAutomaticRenewAnnually')) {
                                subscription.isAutomaticRenewAnnually = RefdataValue.get(params.process_isAutomaticRenewAnnually) == RDStore.YN_YES
                                change << messageSource.getMessage('subscription.isAutomaticRenewAnnually.label', null, locale)
                            }
                            if (subscription.isDirty()) {
                                subscription.save()
                            }
                        }
                    }
                }

            } else {
                flash.error = messageSource.getMessage('subscriptionsManagement.noSelectedSubscriptions', null, locale)
            }
        }
    }

    /**
     * Processes the given input and adds notes to the selected members
     * @param controller the controller instance
     * @param params the request parameter map
     */
    void processNotes(def controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params)
        if(result.editable && formService.validateToken(params)) {
            Locale locale = LocaleContextHolder.getLocale()
            FlashScope flash = getCurrentFlashScope()
            List selectedSubs = params.list("selectedSubs")
            if (selectedSubs) {
                Set<Subscription> subscriptions = Subscription.findAllByIdInList(selectedSubs)
                if(params.licenseNoteTitle && params.licenseNote) {
                    if(params.processOption == 'newNote') {
                        subscriptions.each { Subscription subscription ->
                            if (subscription.isEditableBy(result.user)) {

                                Doc doc_content = new Doc(contentType: Doc.CONTENT_TYPE_STRING,
                                        title: params.licenseNoteTitle,
                                        content: params.licenseNote,
                                        type: RDStore.DOC_TYPE_NOTE,
                                        owner: contextService.getOrg(),
                                        user: result.user).save()


                                DocContext doc_context = new DocContext(
                                        subscription: subscription,
                                        owner: doc_content,
                                        doctype: RDStore.DOC_TYPE_NOTE)
                                doc_context.save()
                            }
                        }
                    }
                }else{
                    flash.error = messageSource.getMessage('subscriptionsManagement.note.noNoteParameter', null, locale)
                }

            } else {
                flash.error = messageSource.getMessage('subscriptionsManagement.noSelectedSubscriptions', null, locale)
            }
        }
    }

    /**
     * Processes the given input and adds the given document to the selected members
     * @param controller the controller instance
     * @param params the request parameter map
     */
    void processDocuments(def controller, GrailsParameterMap params, def input_file) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params)

        //Is be to need, because with upload_file the formService.validateToken(params) is not working really
        params.remove('upload_file')

        if(result.editable && formService.validateToken(params)) {
            Locale locale = LocaleContextHolder.getLocale()
            FlashScope flash = getCurrentFlashScope()
            List selectedSubs = params.list("selectedSubs")
            if (selectedSubs) {
                Set<Subscription> subscriptions = Subscription.findAllByIdInList(selectedSubs)
                    if(params.processOption == 'newDoc') {
                        subscriptions.each { Subscription subscription ->
                            if (subscription.isEditableBy(result.user)) {

                                def input_stream = input_file.inputStream
                                if (input_stream) {
                                    Doc doc_content = new Doc(
                                            contentType: Doc.CONTENT_TYPE_FILE,
                                            filename: params.original_filename,
                                            mimeType: params.mimeType,
                                            title: params.upload_title ?: params.original_filename,
                                            type: RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE),
                                            creator: result.user,
                                            owner: contextService.getOrg())

                                    doc_content.save()

                                    File new_File
                                    try {
                                        String fPath = ConfigUtils.getDocumentStorageLocation() ?: '/tmp/laser'
                                        String fName = doc_content.uuid

                                        File folder = new File("${fPath}")
                                        if (!folder.exists()) {
                                            folder.mkdirs()
                                        }
                                        new_File = new File("${fPath}/${fName}")

                                        input_file.transferTo(new_File)
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace()
                                    }

                                    DocContext doc_context = new DocContext(
                                            subscription: subscription,
                                            owner: doc_content,
                                            doctype: RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE)
                                    )

                                    doc_context.save()
                                }

                            }
                    }
                }

            } else {
                flash.error = messageSource.getMessage('subscriptionsManagement.noSelectedSubscriptions', null, locale)
            }
        }
    }


    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Gets the message container for the current call
     * @return the message container
     */
    FlashScope getCurrentFlashScope() {
        GrailsWebRequest grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        HttpServletRequest request = grailsWebRequest.getCurrentRequest()

        grailsWebRequest.attributes.getFlashScope(request)
    }

    /**
     * Sets generic parameters used in the methods and checks whether the given user may access the view
     * @param controller the controller instance
     * @param params the request parameter map
     * @return the result map with the base data if successful, an empty map otherwise
     */
    Map<String,Object> getResultGenericsAndCheckAccess(def controller, GrailsParameterMap params) {
        Map<String, Object> result = [:]

        if(controller instanceof SubscriptionController) {
            result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        }

        if(controller instanceof MyInstitutionController) {
            result = myInstitutionControllerService.getResultGenerics(controller, params)
            result.contextOrg = contextService.getOrg()
        }

        return  result

    }
}
