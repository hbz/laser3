package de.laser

import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.ctrl.MyInstitutionControllerService
import de.laser.ctrl.SubscriptionControllerService
import de.laser.finance.CostItem
import de.laser.config.ConfigDefaults
import de.laser.config.ConfigMapper
import de.laser.helper.Params
import de.laser.interfaces.CalculatedType
import de.laser.interfaces.ShareSupport
import de.laser.utils.AppUtils
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import grails.gorm.transactions.Transactional
import grails.web.mvc.FlashScope
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import org.springframework.context.MessageSource

import javax.servlet.http.HttpServletRequest
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.time.Year
import java.util.concurrent.ExecutorService

/**
 * This service is for the consortial subscription's member management handling
 */
@Transactional
class ManagementService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AuditService auditService
    AddressbookService addressbookService
    ContextService contextService
    ExecutorService executorService
    FormService formService
    GenericOIDService genericOIDService
    GlobalService globalService
    MessageSource messageSource
    MyInstitutionControllerService myInstitutionControllerService
    SubscriptionControllerService subscriptionControllerService
    PackageService packageService
    SubscriptionService subscriptionService

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
            case "permanentTitles":
                result << permanentTitles(controller, parameterMap)
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
            result.platforms = Platform.executeQuery('select plat from Platform plat where plat.org in (select oo.org from OrgRole oo where oo.pkg in (select sp.pkg from SubscriptionPackage sp where sp.subscription = :parentSub)) and plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :parentSub)', [parentSub: result.subscription]) as Set<Platform>
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
                                    type: RDStore.CUSTOMER_IDENTIFIER_TYPE_DEFAULT,
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

                if (contextService.getOrg().isCustomerType_Inst_Pro()) {
                    base_qry = "from License as l where ( exists ( select o from l.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType = :roleType2 ) AND o.org = :lic_org ) ) )"
                    qry_params = [roleType1:RDStore.OR_LICENSEE, roleType2:RDStore.OR_LICENSEE_CONS, lic_org:result.institution]
                }
                else if (contextService.getOrg().isCustomerType_Consortium()) {
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
            Locale locale = LocaleUtils.getCurrentLocale()
            FlashScope flash = getCurrentFlashScope()
            List selectedLicenseIDs = params.list("selectedLicense")
            Set<Subscription> subscriptions
            if(params.containsKey("membersListToggler")) {
                if(controller instanceof SubscriptionController) {
                    subscriptions = subscriptionControllerService.getFilteredSubscribers(params,result.subscription).sub
                }
                else if(controller instanceof MyInstitutionController) {
                    subscriptions = subscriptionService.getMySubscriptions(params,result.user,result.institution).allSubscriptions
                }
            }
            else subscriptions = Subscription.findAllByIdInList(params.list("selectedSubs"))
            if(subscriptions && selectedLicenseIDs[0]) {
                List<License> selectedLicenses = License.findAllByIdInList(selectedLicenseIDs.collect { String key -> Long.parseLong(key) })
                selectedLicenses.each { License newLicense ->
                    if (params.processOption == 'linkLicense' || params.processOption == 'unlinkLicense') {
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
                if (subscriptions.size() < 1) {
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
                result.isLinkingRunning = subscriptionService.checkThreadRunning('PackageTransfer_'+result.subscription.id)
                result.isUnlinkingRunning = subscriptionService.checkThreadRunning('PackageUnlink_'+result.subscription.id)
                result.validPackages = Package.executeQuery('select sp from SubscriptionPackage sp where sp.subscription = :subscription', [subscription: result.subscription])
                result.filteredSubscriptions = subscriptionControllerService.getFilteredSubscribers(params,result.subscription)
                if(result.filteredSubscriptions)
                    result.childWithCostItems = CostItem.executeQuery('select ci.subPkg from CostItem ci where ci.subPkg.subscription in (:filteredSubChildren) and ci.costItemStatus != :deleted and ci.owner = :context',[context:result.institution, deleted:RDStore.COST_ITEM_DELETED, filteredSubChildren:result.filteredSubscriptions.collect { row -> row.sub }])
            }

            if(controller instanceof MyInstitutionController) {
                result.isLinkingRunning = subscriptionService.checkThreadRunning('PackageTransfer_'+result.user.id)
                result.isUnlinkingRunning = subscriptionService.checkThreadRunning('PackageUnlink_'+result.user.id)
                result.validPackages = Package.findAllByGokbIdIsNotNullAndPackageStatusNotEqual(RDStore.PACKAGE_STATUS_DELETED)

                result.putAll(subscriptionService.getMySubscriptions(params,result.user,result.institution))

                result.filteredSubscriptions = result.subscriptions
                if(result.filteredSubscriptions)
                    result.childWithCostItems = CostItem.executeQuery('select ci.subPkg from CostItem ci where ci.subPkg.subscription in (:filteredSubscriptions) and ci.costItemStatus != :deleted and ci.owner = :context',[context:result.institution, deleted:RDStore.COST_ITEM_DELETED, filteredSubscriptions:result.filteredSubscriptions])
            }

            [result:result,status:STATUS_OK]
        }
    }

    Map<String,Object> permanentTitles(def controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            if(controller instanceof SubscriptionController) {
                result.filteredSubscriptions = subscriptionControllerService.getFilteredSubscribers(params,result.subscription)
              }

            if(controller instanceof MyInstitutionController) {
                result.putAll(subscriptionService.getMySubscriptions(params,result.user,result.institution))
                result.filteredSubscriptions = result.subscriptions
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
        String threadName, unlinkName
        if(controller instanceof SubscriptionController) {
            threadName = "PackageTransfer_${result.subscription.id}"
            unlinkName = "PackageUnlink_${result.subscription.id}"
        }
        else if(controller instanceof MyInstitutionController) {
            threadName = "PackageTransfer_${result.user.id}"
            unlinkName = "PackageUnlink_${result.user.id}"
        }
        if (result.editable && formService.validateToken(params) && !subscriptionService.checkThreadRunning(threadName) && !subscriptionService.checkThreadRunning(unlinkName)) {
            FlashScope flash = getCurrentFlashScope()
            Locale locale = LocaleUtils.getCurrentLocale()
            Set<Subscription> subscriptions, permittedSubs = []
            if(params.membersListToggler == 'on') {
                if(controller instanceof SubscriptionController) {
                    subscriptions = subscriptionControllerService.getFilteredSubscribers(params,result.subscription).sub
                }
                else if(controller instanceof MyInstitutionController) {
                    subscriptions = subscriptionService.getMySubscriptions(params,result.user,result.institution).allSubscriptions
                }
                else {
                    //fallback
                    subscriptions = []
                }
            }
            else subscriptions = Subscription.findAllByIdInList(params.list("selectedSubs"))
            List selectedPackageKeys = params.list("selectedPackages")
            Set<Package> pkgsToProcess = []
            result.message = []
            result.error = []
            if(selectedPackageKeys.contains('all') && result.subscription) {
                pkgsToProcess.addAll(Package.executeQuery('select sp.pkg from SubscriptionPackage sp where sp.subscription = :subscription', [subscription: result.subscription]))
            }
            else {
                selectedPackageKeys.each { String pkgKey ->
                    pkgsToProcess.add(Package.get(pkgKey))
                }
            }
            subscriptions.each { Subscription selectedSub ->
                if(selectedSub.isEditableBy(result.user))
                    permittedSubs << selectedSub
            }
            long userId = contextService.getUser().id
            executorService.execute({
                long start = System.currentTimeSeconds()
                Thread.currentThread().setName(threadName)
                pkgsToProcess.each { Package pkg ->
                    permittedSubs.each { Subscription selectedSub ->
                            SubscriptionPackage sp = SubscriptionPackage.findBySubscriptionAndPkg(selectedSub, pkg)
                            if(params.processOption =~ /^link/) {
                                if(!sp) {
                                    if(result.subscription) {
                                        subscriptionService.addToSubscriptionCurrentStock(selectedSub, result.subscription, pkg, params.processOption == 'linkwithIE')
                                    }
                                    else {
                                        subscriptionService.addToSubscription(selectedSub, pkg, params.processOption == 'linkwithIE')
                                    }
                                }
                            }
                            else if(params.processOption =~ /^unlink/) {
                                if(sp) {
                                    if (!CostItem.executeQuery('select ci from CostItem ci where ci.subPkg = :sp and ci.costItemStatus != :deleted and ci.owner = :context', [sp: sp, deleted: RDStore.COST_ITEM_DELETED, context: result.institution])) {
                                        packageService.unlinkFromSubscription(pkg, selectedSub, result.institution, params.processOption == 'unlinkwithIE')
                                    }
                                    /*
                                    else {
                                        Object[] args = [pkg.name, selectedSub.getSubscriber().name]
                                        result.error << messageSource.getMessage('subscriptionsManagement.unlinkInfo.costsExisting', args, locale)
                                    }
                                    */
                                }
                            }

                    }
                }
                if(System.currentTimeSeconds()-start >= GlobalService.LONG_PROCESS_LIMBO) {
                    globalService.notifyBackgroundProcessFinish(userId, threadName, messageSource.getMessage('subscription.details.linkPackage.thread.completed', [result.subscription.name] as Object[], LocaleUtils.getCurrentLocale()))
                }
            })

            /*
            dos:
            1. extend to multi package option
            else if (selectedSubs && params.selectedPackage && params.processOption) {
                List<Long> selectedKeys = []
                if(!params.list("selectedPackage").contains("all")) {
                    params.list("selectedPackage").each { String pkgId ->
                        selectedKeys << Long.parseLong(pkgId)
                    }
                }
                Set<Package> pkgs_to_link
                SubscriptionPackage subscriptionPackage
                String threadName
                if(controller instanceof SubscriptionController) {
                    subscriptionPackage = SubscriptionPackage.findAllByIdInList(selectedKeys)
                    pkgs_to_link = subscriptionPackage.pkg
                    threadName = "PackageTransfer_${result.subscription.id}"
                }

                if(controller instanceof MyInstitutionController) {
                    pkgs_to_link = Package.findAllByIdInList(selectedKeys)
                    threadName = "PackageTransfer_${result.user.id}"
                }

                if (pkgs_to_link) {
                    List<Subscription> editableSubs = []
                    selectedSubs.each { id ->
                        Subscription subscription = Subscription.get(Long.parseLong(id))
                        if(subscription.isEditableBy(result.user)){
                            editableSubs << subscription
                        }
                    }
                    executorService.execute({
                        Thread.currentThread().setName(threadName)
                        pkgs_to_link.each { Package pkg_to_link ->
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
                    */
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
                    String localizedName = LocaleUtils.getLocalizedAttributeName('name')
                    String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :ctx and sp.instanceOf = null order by sp.type.${localizedName}"
                    Set<PropertyDefinition> propList = new TreeSet<PropertyDefinition>()
                    propList.addAll(PropertyDefinition.executeQuery(query, [subscriptionSet: validSubChildren, ctx: result.institution]))
                    propList.addAll(result.subscription.propertySet.type)
                    result.propList = propList
                    result.filteredSubscriptions = validSubChildren
                    List<Subscription> childSubs = result.subscription.getNonDeletedDerivedSubscriptions()
                    if (childSubs) {
                        String childQuery = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
                        Set<PropertyDefinition> memberProperties = new TreeSet<PropertyDefinition>()
                        memberProperties.addAll(PropertyDefinition.executeQuery(childQuery, [subscriptionSet: childSubs, context: result.institution]))
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
            Locale locale = LocaleUtils.getCurrentLocale()
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
                                if (subscription.isEditableBy(result.user) || (subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && result.institution.isCustomerType_Inst_Pro())) {
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

                    }else if(params.processOption == 'deleteAllProperties' || params.processOption == 'deleteProperty'){
                        Set<Subscription> subscriptions = Subscription.findAllByIdInList(selectedSubs)
                        subscriptions.each { Subscription subChild ->
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
                                Set<SubscriptionProperty> existingProps = subChild.propertySet.findAll { SubscriptionProperty sp ->
                                    sp.type.id == propertiesFilterPropDef.id && sp.owner.id == subChild.id && sp.tenant.id == result.institution.id
                                }
                                existingProps.each { SubscriptionProperty ep ->
                                    if (ep && !(ep.hasProperty('instanceOf') && ep.instanceOf && AuditConfig.getConfig(ep.instanceOf))){
                                        try {
                                            subChild.propertySet.remove(ep)
                                            ep.delete()
                                            deletedProperties++
                                        }
                                        catch (Exception e){
                                            log.error( e.toString() )
                                        }
                                    }
                                }

                            }
                        }
                        args = [deletedProperties]
                        flash.message = messageSource.getMessage('subscriptionsManagement.deletedProperties', args, locale)
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
                result.modalPrsLinkRole = RDStore.PRS_RESP_SPEC_SUB_EDITOR
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
            Locale locale = LocaleUtils.getCurrentLocale()
            FlashScope flash = getCurrentFlashScope()
            Set<Subscription> subscriptions
            if(params.containsKey("membersListToggler")) {
                if(controller instanceof SubscriptionController) {
                    subscriptions = subscriptionControllerService.getFilteredSubscribers(params,result.subscription).sub
                }
                else if(controller instanceof MyInstitutionController) {
                    subscriptions = subscriptionService.getMySubscriptions(params,result.user,result.institution).allSubscriptions
                }
            }
            else subscriptions = Subscription.findAllByIdInList(params.list("selectedSubs"))
            if (subscriptions) {
                Set change = [], noChange = []
                SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
                Date startDate = params.valid_from ? sdf.parse(params.valid_from) : null
                Date endDate = params.valid_to ? sdf.parse(params.valid_to) : null
                Year referenceYear = params.reference_year ? Year.parse(params.reference_year) : null
                Map<String, String> auditable = ['audit_valid_from': 'startDate',
                                                 'audit_valid_to': 'endDate',
                                                 'audit_reference_year': 'referenceYear',
                                                 'audit_process_status': 'status',
                                                 'audit_process_kind': 'kind',
                                                 'audit_process_form': 'form',
                                                 'audit_process_resource': 'resource',
                                                 'audit_isPublicForApi': 'isPublicForApi',
                                                 'audit_hasPerpetualAccess': 'hasPerpetualAccess',
                                                 'audit_hasPublishComponent': 'hasPublishComponent',
                                                 'audit_holdingSelection': 'holdingSelection',
                                                 'audit_isMultiYear': 'isMultiYear']
                //implicates customerType check -> customerType != consortia cannot set those keys
                Map<String, String> selectedAuditable = auditable.findAll { String auditSetting, String field ->
                    params.containsKey(auditSetting) && params.get(auditSetting) != ""
                }
                if(params.processOption == 'changeProperties') {
                    if(result.contextOrg.isCustomerType_Consortium() && controller instanceof MyInstitutionController) {
                        Set<String> updateParts = [], auditTrigger = []
                        Map<String, Object> updateParams = [subscriptions: subscriptions]
                        if (startDate) {
                            auditTrigger << "startDate"
                            updateParts << "s.startDate = :startDate"
                            updateParams.startDate = startDate
                        }
                        if (endDate) {
                            auditTrigger << "endDate"
                            updateParts << "s.endDate = :endDate"
                            updateParams.endDate = endDate
                        }
                        if (referenceYear) {
                            auditTrigger << "referenceYear"
                            updateParts << "s.referenceYear = :referenceYear"
                            updateParams.referenceYear = referenceYear
                        }
                        if (params.process_status && RefdataValue.get(params.process_status)) {
                            auditTrigger << "status"
                            updateParts << "s.status = :status"
                            updateParams.status = RefdataValue.get(params.process_status)
                        }
                        if (params.process_kind && RefdataValue.get(params.process_kind)) {
                            auditTrigger << "kind"
                            updateParts << "s.kind = :kind"
                            updateParams.kind = RefdataValue.get(params.process_kind)
                        }
                        if (params.process_form && RefdataValue.get(params.process_form)) {
                            auditTrigger << "form"
                            updateParts << "s.form = :form"
                            updateParams.form = RefdataValue.get(params.process_form)
                        }
                        if (params.process_resource && RefdataValue.get(params.process_resource)) {
                            auditTrigger << "resource"
                            updateParts << "s.resource = :resource"
                            updateParams.resource = RefdataValue.get(params.process_resource)
                        }
                        if (params.process_isPublicForApi) {
                            auditTrigger << "isPublicForApi"
                            updateParts << "s.isPublicForApi = :isPublicForApi"
                            updateParams.isPublicForApi = RefdataValue.get(params.process_isPublicForApi) == RDStore.YN_YES
                        }
                        if (params.process_hasPerpetualAccess) {
                            auditTrigger << "hasPerpetualAccess"
                            updateParts << "s.hasPerpetualAccess = :hasPerpetualAccess"
                            updateParams.hasPerpetualAccess = RefdataValue.get(params.process_hasPerpetualAccess) == RDStore.YN_YES
                        }
                        if (params.process_hasPublishComponent) {
                            auditTrigger << "hasPublishComponent"
                            updateParts << "s.hasPublishComponent = :hasPublishComponent"
                            updateParams.hasPublishComponent = RefdataValue.get(params.process_hasPublishComponent) == RDStore.YN_YES
                        }
                        if (params.process_holdingSelection && RefdataValue.get(params.process_holdingSelection)) {
                            auditTrigger << "holdingSelection"
                            updateParts << "s.holdingSelection = :holdingSelection"
                            updateParams.holdingSelection = RefdataValue.get(params.process_holdingSelection)
                        }
                        if (params.process_isMultiYear) {
                            auditTrigger << "ísMultiYear"
                            updateParts << "s.isMultiYear = :isMultiYear"
                            updateParams.isMultiYear = RefdataValue.get(params.process_isMultiYear) == RDStore.YN_YES
                        }
                        String query = "update Subscription s set ${updateParts.join(', ')} where s in (:subscriptions)"
                        Subscription.executeUpdate(query, updateParams)
                        globalService.cleanUpGorm()
                        subscriptions.each { Subscription subscription ->
                            selectedAuditable.each { String auditSetting, String auditProp ->
                                if(RefdataValue.get(params.get(auditSetting)) == RDStore.YN_YES && !AuditConfig.getConfig(subscription, auditProp)) {
                                    AuditConfig.addConfig(subscription, auditProp)
                                }
                                else if(RefdataValue.get(params.get(auditSetting)) == RDStore.YN_NO && AuditConfig.getConfig(subscription, auditProp)) {
                                    AuditConfig.removeConfig(subscription, auditProp)
                                }
                            }
                            auditTrigger.each { String auditProp ->
                                if(AuditConfig.getConfig(subscription, auditProp)) {
                                    //weird way of an update from, but I doubt that hql supports this syntax
                                    String queryChild = "update Subscription s set s.${auditProp} = (select p.${auditProp} from Subscription p where p = :subscription) where s.instanceOf = :subscription"
                                    Subscription.executeUpdate(queryChild, [subscription: subscription])
                                }
                            }
                        }
                    }
                    else {
                        Map<String, Object> changeMap = [:]
                        if (startDate && !auditService.getAuditConfig(result.subscription, 'startDate')) {
                            changeMap.startDate = startDate
                            change << messageSource.getMessage('default.startDate.label', null, locale)
                        }
                        if (startDate && auditService.getAuditConfig(result.subscription, 'startDate')) {
                            noChange << messageSource.getMessage('default.startDate.label', null, locale)
                        }
                        if (endDate && !auditService.getAuditConfig(result.subscription, 'endDate')) {
                            changeMap.endDate = endDate
                            change << messageSource.getMessage('default.endDate.label', null, locale)
                        }
                        if (endDate && auditService.getAuditConfig(result.subscription, 'endDate')) {
                            noChange << messageSource.getMessage('default.endDate.label', null, locale)
                        }
                        if (referenceYear && !auditService.getAuditConfig(result.subscription, 'referenceYear')) {
                            changeMap.referenceYear = referenceYear
                            change << messageSource.getMessage('subscription.referenceYear.label', null, locale)
                        }
                        if (referenceYear && auditService.getAuditConfig(result.subscription, 'referenceYear')) {
                            noChange << messageSource.getMessage('subscription.referenceYear.label', null, locale)
                        }
                        if (params.process_status && !auditService.getAuditConfig(result.subscription, 'status')) {
                            changeMap.status = RefdataValue.get(params.process_status)
                            change << messageSource.getMessage('subscription.status.label', null, locale)
                        }
                        if (params.process_status && auditService.getAuditConfig(result.subscription, 'status')) {
                            noChange << messageSource.getMessage('subscription.status.label', null, locale)
                        }
                        if (params.process_kind && !auditService.getAuditConfig(result.subscription, 'kind')) {
                            changeMap.kind = RefdataValue.get(params.process_kind)
                            change << messageSource.getMessage('subscription.kind.label', null, locale)
                        }
                        if (params.process_kind && auditService.getAuditConfig(result.subscription, 'kind')) {
                            noChange << messageSource.getMessage('subscription.kind.label', null, locale)
                        }
                        if (params.process_form && !auditService.getAuditConfig(result.subscription, 'form')) {
                            changeMap.form = RefdataValue.get(params.process_form) ?: 'noChange'
                            change << messageSource.getMessage('subscription.form.label', null, locale)
                        }
                        if (params.process_form && auditService.getAuditConfig(result.subscription, 'form')) {
                            noChange << messageSource.getMessage('subscription.form.label', null, locale)
                        }
                        if (params.process_resource && !auditService.getAuditConfig(result.subscription, 'resource')) {
                            changeMap.resource = RefdataValue.get(params.process_resource) ?: 'noChange'
                            change << messageSource.getMessage('subscription.resource.label', null, locale)
                        }
                        if (params.process_resource && auditService.getAuditConfig(result.subscription, 'resource')) {
                            noChange << messageSource.getMessage('subscription.resource.label', null, locale)
                        }
                        if (params.process_isPublicForApi && !auditService.getAuditConfig(result.subscription, 'isPublicForApi')) {
                            changeMap.isPublicForApi = RefdataValue.get(params.process_isPublicForApi) == RDStore.YN_YES
                            change << messageSource.getMessage('subscription.isPublicForApi.label', null, locale)
                        }
                        if (params.process_isPublicForApi && auditService.getAuditConfig(result.subscription, 'isPublicForApi')) {
                            noChange << messageSource.getMessage('subscription.isPublicForApi.label', null, locale)
                        }
                        if (params.process_hasPerpetualAccess && !auditService.getAuditConfig(result.subscription, 'hasPerpetualAccess')) {
                            changeMap.hasPerpetualAccess = RefdataValue.get(params.process_hasPerpetualAccess) == RDStore.YN_YES
                            //subscription.hasPerpetualAccess = RefdataValue.get(params.process_hasPerpetualAccess)
                            change << messageSource.getMessage('subscription.hasPerpetualAccess.label', null, locale)
                        }
                        if (params.process_hasPerpetuaLAccess && auditService.getAuditConfig(result.subscription, 'hasPerpetualAccess')) {
                            noChange << messageSource.getMessage('subscription.hasPerpetualAccess.label', null, locale)
                        }
                        if (params.process_hasPublishComponent && !auditService.getAuditConfig(result.subscription, 'hasPublishComponent')) {
                            changeMap.hasPublishComponent = RefdataValue.get(params.process_hasPublishComponent) == RDStore.YN_YES
                            change << messageSource.getMessage('subscription.hasPublishComponent.label', null, locale)
                        }
                        if (params.process_hasPublishComponent && auditService.getAuditConfig(result.subscription, 'hasPublishComponent')) {
                            noChange << messageSource.getMessage('subscription.hasPublishComponent.label', null, locale)
                        }
                        if (params.process_holdingSelection && !auditService.getAuditConfig(result.subscription, 'holdingSelection')) {
                            changeMap.holdingSelection = RefdataValue.get(params.process_holdingSelection) ?: 'noChange'
                            change << messageSource.getMessage('subscription.holdingSelection.label', null, locale)
                        }
                        if (params.process_holdingSelection && auditService.getAuditConfig(result.subscription, 'holdingSelection')) {
                            noChange << messageSource.getMessage('subscription.holdingSelection.label', null, locale)
                        }
                        if (params.process_isMultiYear && !auditService.getAuditConfig(result.subscription, 'isMultiYear')) {
                            changeMap.isMultiYear = RefdataValue.get(params.process_isMultiYear) == RDStore.YN_YES
                            change << messageSource.getMessage('subscription.isMultiYear.label', null, locale)
                        }
                        if (params.process_isMultiYear && auditService.getAuditConfig(result.subscription, 'isMultiYear')) {
                            noChange << messageSource.getMessage('subscription.isMultiYear.label', null, locale)
                        }
                        subscriptions.each { Subscription subscription ->
                            if (subscription.isEditableBy(result.user)) {
                                changeMap.each { String key, value ->
                                    if(changeMap.get(key) != 'noChange')
                                        subscription[key] = changeMap.get(key)
                                }
                                if (params.process_isAutomaticRenewAnnually && !auditService.getAuditConfig(subscription.instanceOf, 'isAutomaticRenewAnnually') && subscription.isAllowToAutomaticRenewAnnually()) {
                                    subscription.isAutomaticRenewAnnually = RefdataValue.get(params.process_isAutomaticRenewAnnually) == RDStore.YN_YES
                                    change << messageSource.getMessage('subscription.isAutomaticRenewAnnually.label', null, locale)
                                }
                                if (subscription.isDirty()) {
                                    subscription.save()
                                }
                            }
                        }
                        if(change) {
                            Object[] affectedFields = [change.join(', ')]
                            flash.message = messageSource.getMessage('subscriptionsManagement.changeAccepted', affectedFields, locale)
                        }
                        if(noChange) {
                            Object[] affectedFields = [noChange.join(', ')]
                            flash.error = messageSource.getMessage('subscriptionsManagement.blockingAudit', affectedFields, locale)
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
            Locale locale = LocaleUtils.getCurrentLocale()
            FlashScope flash = getCurrentFlashScope()
            Set<Subscription> subscriptions
            if(params.containsKey("membersListToggler")) {
                if(controller instanceof SubscriptionController) {
                    subscriptions = subscriptionControllerService.getFilteredSubscribers(params,result.subscription).sub
                }
                else if(controller instanceof MyInstitutionController) {
                    subscriptions = subscriptionService.getMySubscriptions(params,result.user,result.institution).allSubscriptions
                }
            }
            else subscriptions = Subscription.findAllByIdInList(params.list("selectedSubs"))
            if (subscriptions) {
                if(params.noteTitle || params.noteContent) {
                    if(params.processOption == 'newNote') {
                        subscriptions.each { Subscription subscription ->
                            if (subscription.isEditableBy(result.user) || (subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && result.institution.isCustomerType_Inst_Pro())) {

                                Doc doc_content = new Doc(contentType: Doc.CONTENT_TYPE_STRING,
                                        title: params.noteTitle,
                                        content: params.noteContent,
                                        type: RDStore.DOC_TYPE_NOTE,
                                        owner: contextService.getOrg(),
                                        user: result.user).save()

                                DocContext doc_context = new DocContext(
                                        subscription: subscription,
                                        owner: doc_content,
                                        isShared: params.setSharing == 'on'
                                )

                                doc_context.save()
                                if (doc_context.isShared) {
                                    ((ShareSupport) subscription).updateShare(doc_context)
                                }
                            }
                        }
                    }
                }
                else{
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
        def input_stream = input_file.inputStream
        File sourceFile
        if(result.editable && formService.validateToken(params)) {
            Locale locale = LocaleUtils.getCurrentLocale()
            FlashScope flash = getCurrentFlashScope()
            Set<Subscription> subscriptions
            if(Boolean.valueOf(params.allMembers)) {
                if(controller instanceof SubscriptionController) {
                    subscriptions = subscriptionControllerService.getFilteredSubscribers(params,result.subscription).sub
                }
                else if(controller instanceof MyInstitutionController) {
                    subscriptions = subscriptionService.getMySubscriptions(params,result.user,result.institution).allSubscriptions
                }
            }
            else {
                if(params.selectedSubscriptionIds) {
                    List<Long> ids = Params.getLongList_forCommaSeparatedString(params, 'selectedSubscriptionIds')
                    subscriptions = Subscription.findAllByIdInList(ids)
                }
                else subscriptions = []
            }
            if (subscriptions) {
                    if(params.processOption == 'newDoc') {
                        subscriptions.eachWithIndex { Subscription subscription, int status ->
                            if (subscription.isEditableBy(result.user) || (subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && result.institution.isCustomerType_Inst_Pro())) {
                                if (input_stream) {
                                    Doc doc_content = new Doc(
                                            contentType: Doc.CONTENT_TYPE_FILE,
                                            filename: params.original_filename,
                                            mimeType: params.mimeType,
                                            title: params.upload_title ?: params.original_filename,
                                            type: RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE),
                                            creator: result.user,
                                            owner: contextService.getOrg(),
                                            server: AppUtils.getCurrentServer()
                                    )

                                    doc_content.save()

                                    try {
                                        String fPath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK
                                        String fName = doc_content.uuid

                                        File folder = new File("${fPath}")
                                        if (!folder.exists()) {
                                            folder.mkdirs()
                                        }

                                        if(status == 0){
                                            sourceFile = new File("${fPath}/${fName}")
                                            input_file.transferTo(sourceFile)
                                        }else {
                                            Path source = sourceFile.toPath()
                                            Path target = new File("${fPath}/${fName}").toPath()
                                            Files.copy(source, target)
                                        }

                                    }
                                    catch (Exception e) {
                                        e.printStackTrace()
                                    }

                                    DocContext doc_context = new DocContext(
                                            subscription: subscription,
                                            owner: doc_content,
                                            isShared: params.setSharing == 'on'
                                    )

                                    doc_context.save()
                                    if(doc_context.isShared) {
                                        ((ShareSupport) subscription).updateShare(doc_context)
                                    }
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
