package de.laser.ctrl

import de.laser.*
import de.laser.auth.User
import de.laser.exceptions.FinancialDataException
import de.laser.finance.BudgetCode
import de.laser.finance.CostItemElementConfiguration
import de.laser.utils.LocaleUtils
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

/**
 * This class is a service mirror for {@link FinanceController} to capsule the complex data manipulation
 * methods of the controller
 */
@Transactional
class FinanceControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AccessService accessService
    ContextService contextService
    FinanceService financeService
    LinksGenerationService linksGenerationService
    MessageSource messageSource

    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Sets parameters which are valid for many finance view calls and defines the perspective which is being taken
     * when displaying data. Many different configurations may be taken. A decision tree determining the perspective is
     * listed as follows.
     * We may see this view (the views) from the perspective of:
     * <ol>
     *  <li>consortia: parent subscription (show own and consortial tabs) (level 1)</li>
     *  <li>consortia: child subscription (show consortial tab) (level 2)</li>
     *  <li>consortia: child subscription preview (show consortial tab) (level 2)</li>
     *  <li>single user: own subscription (show own tab) (level 1)</li>
     *  <li>single user: child subscription (show own and subscriber tab) (level 2)</li>
     *  <li>basic member: child subscription (show subscriber tab) (level 2)</li>
     * </ol>
     * @param params the request parameter map
     * @return the response map with global parameters set
     * @throws FinancialDataException
     */
    Map<String,Object> getResultGenerics(GrailsParameterMap params) throws FinancialDataException {
        Map<String,Object> result = [user: contextService.getUser(),
                                     offsets:[consOffset:0, subscrOffset:0, ownOffset:0],
                                     sortConfig:[
                                             consSort:'oo.org.sortname', consOrder:'asc',
                                             subscrSort:'sub.name', subscrOrder:'asc',
                                             ownSort:'ci.costTitle', ownOrder:'asc'
                                     ],
                                     showVisibilitySettings: false,
                                     yn: RefdataCategory.getAllRefdataValues(RDConstants.Y_N),
                                     institution:contextService.getOrg()]
        List<Map<String,Object>> currenciesList = financeService.orderedCurrency()
        currenciesList.remove(currenciesList.find{Map<String,Object> entry -> entry.id == 0})
        result.currenciesList = currenciesList

        result.showBulkCostItems = params.showBulkCostItems ? params.showBulkCostItems : null

        if(result.showBulkCostItems == 'true'){
            params.max = '1000000'
        }else if(result.showBulkCostItems == 'false'){
            params.remove('max')
        }

        if(params.ownSort) {
            result.sortConfig.ownSort = params.sort
            result.sortConfig.ownOrder = params.order
        }
        if(params.consSort) {
            result.sortConfig.consSort = (params.sort.contains("oo.org") || params.sort.contains("ci.")) ? params.sort : 'ci.'+params.sort
            result.sortConfig.consOrder = params.order
        }
        if(params.subscrSort) {
            result.sortConfig.subscrSort = params.sort
            result.sortConfig.subscrOrder = params.order
        }
        if (params.forExport) {
            result.max = 1000000
        }
        else {
            result.max = params.max ? Integer.parseInt(params.max) : result.user.getPageSizeOrDefault()
        }
        if (!(result.user instanceof User))
            throw new FinancialDataException("Context user not loaded successfully!")
        if (!(result.institution instanceof Org))
            throw new FinancialDataException("Context org not loaded successfully!")
        if (params.sub || params.id) {
            String subId
            if(params.sub)
                subId = params.sub
            else if(params.id)
                subId = params.id
            result.subscription = Subscription.get(subId)
            if (!(result.subscription instanceof Subscription))
                throw new FinancialDataException("Invalid or no subscription found!")

            Map navigation = linksGenerationService.generateNavigation(result.subscription)
            result.navNextSubscription = navigation.nextLink
            result.navPrevSubscription = navigation.prevLink
        }
        Locale locale = LocaleUtils.getCurrentLocale()

        //see the decision tree above
        List<String> dataToDisplay = []
        boolean editable = false
        //Determine own org belonging, then, in which relationship I am to the given subscription instance
        switch(result.institution.getCustomerType()) {
        //cases one to three
            case 'ORG_CONSORTIUM':
                if (result.subscription) {
                    //cases two and three: child subscription
                    if (result.subscription.instanceOf) {
                        //case three: child subscription preview
                        if (params.orgBasicMemberView) {
                            dataToDisplay << 'subscr'
                            result.showView = 'subscr'
                        }
                        //case two: child subscription, consortial view
                        else {
                            dataToDisplay << 'consAtSubscr'
                            result.showView = 'cons'
                            result.showVisibilitySettings = true
                            result.showConsortiaFunctions = true
                            result.sortConfig.consSort = 'ci.costTitle'
                            result.subMemberLabel = messageSource.getMessage('consortium.subscriber',null, locale)
                            result.subMembers = Subscription.executeQuery('select s, oo.org.sortname as sortname from Subscription s join s.orgRelations oo where s = :parent and oo.roleType in :subscrRoles order by sortname asc',[parent:result.subscription,subscrRoles:[RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]).collect { row -> row[0]}
                            result.showVisibilitySettings = true
                            editable = true
                        }
                    }
                    //case one: parent subscription
                    else {
                        dataToDisplay.addAll(['own','cons'])
                        result.showView = 'cons'
                        result.showConsortiaFunctions = true
                        result.subMemberLabel = messageSource.getMessage('consortium.subscriber',null, locale)
                        result.subMembers = Subscription.executeQuery('select s, oo.org.sortname as sortname from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscrRoles order by sortname asc',[parent:result.subscription,subscrRoles:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]).collect { row -> row[0]}
                        result.showVisibilitySettings = true
                        editable = true
                    }
                }
                //case one for all subscriptions
                else {
                    dataToDisplay.addAll(['own','cons'])
                    result.showView = 'cons'
                    result.showConsortiaFunctions = true
                    result.showVisibilitySettings = true
                    result.subMemberLabel = messageSource.getMessage('consortium.subscriber',null, locale)
                    Set<Org> consMembers = Subscription.executeQuery(
                            'select oo.org, oo.org.sortname as sortname from Subscription s ' +
                                    'join s.instanceOf subC ' +
                                    'join subC.orgRelations roleC ' +
                                    'join s.orgRelations roleMC ' +
                                    'join s.orgRelations oo ' +
                                    'where roleC.org = :contextOrg and roleMC.roleType = :consortialType and oo.roleType in :subscrRoles order by sortname asc',[contextOrg:result.institution,consortialType:RDStore.OR_SUBSCRIPTION_CONSORTIA,subscrRoles:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]).collect { row -> row[0]}
                    result.consMembers = consMembers
                    result.showVisibilitySettings = true
                    editable = true
                }
                break
        //cases four and five
            case 'ORG_INST':
                if (result.subscription) {
                    //case four: child subscription
                    if(result.subscription.instanceOf) {
                        dataToDisplay.addAll(['own','subscr'])
                        result.showView = 'subscr'
                        editable = true
                    }
                    //case five: local subscription
                    else {
                        dataToDisplay << 'own'
                        result.showView = 'own'
                        editable = true
                    }
                }
                //case five for all subscriptions
                else {
                    dataToDisplay.addAll(['own','subscr'])
                    result.showView = 'subscr'
                    editable = true
                }
                break
        //cases six: basic member
            case 'ORG_BASIC_MEMBER':
                dataToDisplay << 'subscr'
                result.showView = 'subscr'
                break
        }
        if (editable)
            result.editable = accessService.checkPermAffiliationX("ORG_INST, ORG_CONSORTIUM","INST_EDITOR","ROLE_ADMIN")
        result.dataToDisplay = dataToDisplay
        //override default view to show if checked by pagination or from elsewhere
        if (params.showView){
            result.showView = params.showView
            if (params.offset && !params.forExport) {
                if(params.showView == 'consAtSubscr')
                    result.offsets["consOffset"] = Integer.parseInt(params.offset)
                else
                    result.offsets["${params.showView}Offset"] = Integer.parseInt(params.offset)
            }
        }
        result.putAll(getAdditionalGenericEditResults(result))
        result
    }

    /**
     * This method completed getResultGenerics(); additional variables are being set here which are generic for all editing fields
     * @param configMap the map containing the call parameters which are important to determine the perspective taken
     * @return a {@link Map} containing further general parameters
     */
    Map<String,Object> getAdditionalGenericEditResults(Map configMap) {
        Locale locale = LocaleUtils.getCurrentLocale()
        Map<String,Object> result = getEditVars(configMap.institution)

        log.debug(configMap.dataToDisplay)

        if (configMap.dataToDisplay.stream().anyMatch(['cons','consAtSubscr'].&contains)) {
            result.licenseeLabel = messageSource.getMessage( 'consortium.member',null,locale)
            result.licenseeTargetLabel = messageSource.getMessage('financials.newCosts.consortia.licenseeTargetLabel',null,locale)
        }

        if (configMap.dataToDisplay.contains("cons") && configMap.subMembers) {
            result.validSubChilds = [[id: 'forParent', label: messageSource.getMessage('financials.newCosts.forParentSubscription', null, locale)], [id: 'forAllSubscribers', label: result.licenseeTargetLabel]]
            result.validSubChilds.addAll(configMap.subMembers)
        }
        result
    }

    /**
     * This method replaced the view (!!) _vars.gsp.
     * @return a {@link Map} containing generic parameters for manipulating cost items
     */
    Map<String,Object> getEditVars(Org org) {
        String lang = LocaleUtils.getCurrentLang()
        [
            costItemStatus:     RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_STATUS) - RDStore.COST_ITEM_DELETED,
            costItemSigns:      RefdataCategory.getAllRefdataValues(RDConstants.COST_CONFIGURATION),
            costItemElements:   CostItemElementConfiguration.executeQuery('select ciec from CostItemElementConfiguration ciec join ciec.costItemElement cie where ciec.forOrganisation = :org order by cie.value_'+lang+' asc',[org:org]),
            taxType:            RefdataCategory.getAllRefdataValues(RDConstants.TAX_TYPE),
            budgetCodes:        BudgetCode.findAllByOwner(org, [sort: 'value']),
            currency:           financeService.orderedCurrency()
        ]
    }
}