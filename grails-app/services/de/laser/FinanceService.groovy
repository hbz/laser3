package de.laser

import de.laser.cache.EhcacheWrapper
import de.laser.ctrl.FinanceControllerService
import de.laser.exceptions.CreationException
import de.laser.exceptions.FinancialDataException
import de.laser.finance.*
import de.laser.helper.*
import de.laser.interfaces.CalculatedType
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.validation.ObjectError
import org.springframework.web.multipart.MultipartFile

import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Year
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * This service replaced very complicatedly written methods in the FinanceController class and delivers financial data
 * for finance and survey cost views
 */
@Transactional
class FinanceService {

    AccessService accessService
    ContextService contextService
    DeletionService deletionService
    EscapeService escapeService
    FinanceControllerService financeControllerService
    GenericOIDService genericOIDService
    MessageSource messageSource

    String genericExcludes = ' and ci.surveyOrg = null and ci.costItemStatus != :deleted '

    //model attempt; the finance controller is subject of general refactoring
    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    /**
     * Creates or updates the given cost item(s) with the given parameter. Multiple cost items may be
     * created at once when more than one subscription has been picked upon which the cost should be applied;
     * this is the case whenever the consortium distributes costs among subscription members.
     * New cost items will be created if no cost item identifier has been submitted or if there is no cost item
     * matching to the given identifier
     * @param params the cost item data to persist
     * @return result status map: OK upon success, ERROR on failure
     */
    Map<String,Object> createOrUpdateCostItem(GrailsParameterMap params) {
        Locale locale = LocaleUtils.getCurrentLocale()
        Map<String,Object> result = financeControllerService.getResultGenerics(params)
        CostItem newCostItem
        try {
            Set<Subscription> subsToDo = []

            if(params.selectedSubs){
                if (params.selectedSubs) {
                    subsToDo.clear()
                    if (params.selectedSubs instanceof String)
                        subsToDo << Subscription.get(Long.parseLong(params.selectedSubs))
                    else if (params.selectedSubs instanceof String[]) {
                        params.selectedSubs.each { selectedSubs ->
                            subsToDo <<  Subscription.get(Long.parseLong(selectedSubs))
                        }
                    }
                }
            }else {

                if (params.newSubscription?.contains("${Subscription.class.name}:")) {
                    subsToDo << (Subscription) genericOIDService.resolveOID(params.newSubscription)
                }
                switch (params.newLicenseeTarget) {
                    case "${Subscription.class.name}:forParent":
                        // keep current
                        break
                    case "${Subscription.class.name}:forAllSubscribers":
                        // iterate over members
                        Subscription parentSub = (Subscription) genericOIDService.resolveOID(params.newSubscription)
                        subsToDo = parentSub.getDerivedSubscriptions()
                        break
                    default:
                        if (params.newLicenseeTarget) {
                            subsToDo.clear()
                            if (params.newLicenseeTarget instanceof String)
                                subsToDo << (Subscription) genericOIDService.resolveOID(params.newLicenseeTarget)
                            else if (params.newLicenseeTarget instanceof String[]) {
                                params.newLicenseeTarget.each { newLicenseeTarget ->
                                    subsToDo << (Subscription) genericOIDService.resolveOID(newLicenseeTarget)
                                }
                            }
                        }
                        break
                }
            }
            SubscriptionPackage pkg
            if (params.newPackage?.contains("${SubscriptionPackage.class.name}:")) {
                try {
                    if (params.newPackage.split(":")[1] != 'null') {
                        pkg = (SubscriptionPackage) genericOIDService.resolveOID(params.newPackage)
                    }
                } catch (Exception e) {
                    log.error("Non-valid sub-package sent ${params.newPackage}",e)
                }
            }
            IssueEntitlement ie = params.newIE ? (IssueEntitlement) genericOIDService.resolveOID(params.newIE) : null
            IssueEntitlementGroup issueEntitlementGroup = params.newTitleGroup ? (IssueEntitlementGroup) genericOIDService.resolveOID(params.newTitleGroup) : null
            Map<String, Object> configMap = setupConfigMap(params, result.institution)
            Boolean billingSumRounding = params.newBillingSumRounding == 'on'
            Boolean finalCostRounding = params.newFinalCostRounding == 'on'
            if (! subsToDo) {
                subsToDo << null // Fallback for editing cost items via myInstitution/finance // TODO: ugly
            }
            subsToDo.each { Subscription sub ->
                List<CostItem> copiedCostItems = []
                if(params.costItemId && params.mode != 'copy') {
                    newCostItem = CostItem.get(Long.parseLong(params.costItemId))
                    //get copied cost items
                    copiedCostItems = CostItem.findAllByCopyBaseAndCostItemStatusNotEqualAndOwnerNotEqualAndSubIsNotNull(newCostItem, RDStore.COST_ITEM_DELETED, result.institution)
                    if(params.newOrderNumber == null || params.newOrderNumber.length() < 1) {
                        CostItem costItemWithOrder = CostItem.findByOrderAndIdNotEqualAndCostItemStatusNotEqual(newCostItem.order,newCostItem.id,RDStore.COST_ITEM_DELETED)
                        if(!costItemWithOrder)
                            newCostItem.order.delete()
                    }
                    if(params.newInvoiceNumber == null || params.newInvoiceNumber.length() < 1) {
                        CostItem costItemWithInvoice = CostItem.findByInvoiceAndIdNotEqualAndCostItemStatusNotEqual(newCostItem.invoice,newCostItem.id,RDStore.COST_ITEM_DELETED)
                        if(!costItemWithInvoice)
                            newCostItem.invoice.delete()
                    }
                }
                else {
                    newCostItem = new CostItem()
                    if(params.mode == 'copy' && sub?._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && sub?.getSubscriber() == result.institution)
                        newCostItem.copyBase = CostItem.get(Long.parseLong(params.costItemId))
                }
                newCostItem.owner = (Org) result.institution
                newCostItem.sub = sub
                newCostItem.subPkg = SubscriptionPackage.findBySubscriptionAndPkg(sub,pkg?.pkg) ?: null
                newCostItem.issueEntitlement = IssueEntitlement.findBySubscriptionAndTipp(sub,ie?.tipp) ?: null
                newCostItem.issueEntitlementGroup = issueEntitlementGroup ?: null
                newCostItem.order = configMap.order
                newCostItem.invoice = configMap.invoice
                if(configMap.isVisibleForSubscriber != null)
                    newCostItem.isVisibleForSubscriber = sub?._getCalculatedType() == CalculatedType.TYPE_ADMINISTRATIVE ? false : configMap.isVisibleForSubscriber
                newCostItem.costItemElement = configMap.costItemElement
                newCostItem.costItemStatus = configMap.costItemStatus
                newCostItem.billingCurrency = configMap.billingCurrency
                newCostItem.costDescription = configMap.costDescription
                newCostItem.costTitle = configMap.costTitle
                newCostItem.costInBillingCurrency = configMap.costBillingCurrency
                newCostItem.costInLocalCurrency = configMap.costLocalCurrency
                newCostItem.billingSumRounding = billingSumRounding
                newCostItem.finalCostRounding = finalCostRounding
                newCostItem.costInBillingCurrencyAfterTax = configMap.costBillingCurrencyAfterTax
                newCostItem.costInLocalCurrencyAfterTax = configMap.costLocalCurrencyAfterTax
                newCostItem.currencyRate = configMap.currencyRate
                newCostItem.taxKey = configMap.taxKey
                newCostItem.costItemElementConfiguration = configMap.elementSign
                newCostItem.datePaid = configMap.datePaid
                newCostItem.startDate = configMap.startDate
                newCostItem.endDate = configMap.endDate
                newCostItem.invoiceDate = configMap.invoiceDate
                newCostItem.financialYear = configMap.financialYear
                newCostItem.reference = configMap.reference
                if (newCostItem.save()) {
                    List<BudgetCode> newBcObjs = []
                    params.list('newBudgetCodes').each { newbc ->
                        BudgetCode bc = (BudgetCode) genericOIDService.resolveOID(newbc)
                        if (bc) {
                            newBcObjs << bc
                            if (! CostItemGroup.findByCostItemAndBudgetCode( newCostItem, bc )) {
                                new CostItemGroup(costItem: newCostItem, budgetCode: bc).save()
                            }
                        }
                    }
                    List<BudgetCode> toDelete = newCostItem.budgetcodes.minus(newBcObjs)
                    toDelete.each{ BudgetCode bc ->
                        CostItemGroup cig = CostItemGroup.findByCostItemAndBudgetCode( newCostItem, bc )
                        if (cig) {
                            log.debug('deleting ' + cig)
                            cig.delete()
                        }
                    }
                    //notify cost items copied from this cost item
                    copiedCostItems.each { CostItem cci ->
                        if(cci.sub._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION) {
                            List diffs = []
                            if(newCostItem.costInBillingCurrencyAfterTax != cci.costInBillingCurrency) {
                                diffs.add([prop:'billingCurrency', msgToken: PendingChangeConfiguration.BILLING_SUM_UPDATED, oldValue: cci.costInBillingCurrency, newValue:newCostItem.costInBillingCurrencyAfterTax])
                            }
                            if(newCostItem.costInLocalCurrencyAfterTax != cci.costInLocalCurrency) {
                                diffs.add([prop:'localCurrency',msgToken:PendingChangeConfiguration.LOCAL_SUM_UPDATED,oldValue: cci.costInLocalCurrency,newValue:newCostItem.costInLocalCurrencyAfterTax])
                            }
                            diffs.each { diff ->
                                try {
                                    PendingChange.construct([target:cci,owner:cci.owner,prop:diff.prop,oldValue:diff.oldValue,newValue:diff.newValue,msgToken:diff.msgToken,status:RDStore.PENDING_CHANGE_PENDING])
                                }
                                catch (CreationException e) {
                                    log.error( e.toString() )
                                }
                            }
                        }
                    }
                }
                else {
                    result.error = newCostItem.errors.allErrors.collect { ObjectError oe ->
                        log.error("Field: ${oe.properties.field}, user input: ${oe.properties.rejectedValue}, Reason! ${oe.properties.code}")
                        Object[] args = [oe.properties.field]
                        messageSource.getMessage('finance.addNew.error', args, locale)
                    }
                    [result:result,status:STATUS_ERROR]
                }
            } // subsToDo.each
        }
        catch (Exception e) {
            e.printStackTrace()
            result.message = messageSource.getMessage('default.save.error.general.message',null,locale)
            [result:result,status:STATUS_ERROR]
        }
        [result:result,status:STATUS_OK]
    }

    /**
     * Takes the params submitted by the bulk processing form and applies the changes
     * @param params the parameter map containing the changes to apply and the items on which the changes should take effect
     * @return result status map; OK if the editing was successful, ERROR otherwise
     */
    Map<String,Object> processCostItemsBulk(GrailsParameterMap params) {
        Map<String,Object> result = financeControllerService.getResultGenerics(params)
        result.putAll(financeControllerService.getEditVars(result.institution))
        List<Long> selectedCostItems = []
        Boolean billingSumRounding = params.newBillingSumRounding == 'on'
        Boolean finalCostRounding = params.newFinalCostRounding == 'on'
        if(params.containsKey('costItemListToggler')) {
            if(result.subscription)
                selectedCostItems = getCostItemsForSubscription(params, result).get(params.view).ids
            else selectedCostItems = getCostItems(params, result).get(params.view).ids
        }
        else {
            params.list("selectedCostItems").each { id ->
                selectedCostItems << Long.parseLong(id)
            }
        }
        if(selectedCostItems) {
            if(Boolean.valueOf(params.delete)) {
                CostItem.findAllByIdInList(selectedCostItems).each { CostItem ci ->
                    deletionService.deleteCostItem(ci)
                }
            }
            else if(params.percentOnOldPrice) {
                Double percentage = 1 + params.double('percentOnOldPrice') / 100
                List<String> memberFailures = []
                //Set<Long> lastYearEquivalents = CostItem.executeQuery('select ci.id from CostItem ci where ci.sub in (select l.destinationSubscription from Links l where l.linkType = :follows and l.sourceSubscription in (select cil.sub from CostItem cil where cil.id in (:selectedCostItems)))', [selectedCostItems: selectedCostItems, follows: RDStore.LINKTYPE_FOLLOWS])
                //CostItem.executeUpdate('update CostItem ci set ci.costInBillingCurrency = floor(abs(cil.costInBillingCurrency * :percentage) * 100)/100.0, ci.costInLocalCurrency = floor(abs(cil.costInLocalCurrency * :percentage) * 100)/100.0 where ci = (select cil )',[ids: selectedCostItems, percentage:percentage])
                CostItem.findAllByIdInList(selectedCostItems).each { CostItem ci ->
                    if(ci.sub) {
                        CostItem lastYearEquivalent = CostItem.executeQuery('select ci from CostItem ci where ci.sub = (select l.destinationSubscription from Links l where l.linkType = :follows and l.sourceSubscription = :currentYearSub) and ci.costItemElement = :element and ci.costItemStatus != :deleted', [follows: RDStore.LINKTYPE_FOLLOWS, currentYearSub: ci.sub, element: ci.costItemElement, deleted: RDStore.COST_ITEM_DELETED])[0]
                        if(lastYearEquivalent) {
                            ci.billingSumRounding = billingSumRounding != ci.billingSumRounding ? billingSumRounding : ci.billingSumRounding
                            ci.finalCostRounding = finalCostRounding != ci.finalCostRounding ? finalCostRounding : ci.finalCostRounding
                            ci.costInBillingCurrency = Math.floor(Math.abs(lastYearEquivalent.costInBillingCurrency * percentage))
                            ci.costInLocalCurrency = Math.floor(Math.abs(lastYearEquivalent.costInLocalCurrency * percentage))
                            if (ci.billingSumRounding) {
                                ci.costInBillingCurrency = Math.round(ci.costInBillingCurrency)
                                ci.costInLocalCurrency = Math.round(ci.costInLocalCurrency)
                            }
                            ci.save()
                        }
                        else {
                            memberFailures << ci.sub.getSubscriber().sortname
                        }
                    }
                }
                if(memberFailures)
                    result.failures = memberFailures
            }
            else {
                Map<String, Object> configMap = setupConfigMap(params, result.institution)
                List<CostItem> costItems = CostItem.findAllByIdInList(selectedCostItems)
                costItems.each { CostItem costItem ->
                    if(costItem && costItem.costItemStatus != RDStore.COST_ITEM_DELETED){
                        costItem.order = configMap.order ?: costItem.order
                        costItem.invoice = configMap.invoice ?: costItem.invoice
                        costItem.costItemElement = configMap.costItemElement ?: costItem.costItemElement
                        costItem.costItemElementConfiguration = configMap.elementSign ?: costItem.costItemElementConfiguration
                        costItem.costItemStatus = (params.newCostItemStatus && params.newCostItemStatus != RDStore.GENERIC_NULL_VALUE.id.toString()) ? RefdataValue.get(params.newCostItemStatus) : costItem.costItemStatus
                        if(configMap.taxKey)
                            costItem.taxKey = configMap.taxKey
                        int taxRate = 0 //fallback
                        if(costItem.taxKey)
                            taxRate = costItem.taxKey.taxRate
                        costItem.billingSumRounding = billingSumRounding != costItem.billingSumRounding ? billingSumRounding : costItem.billingSumRounding
                        costItem.finalCostRounding = finalCostRounding != costItem.finalCostRounding ? finalCostRounding : costItem.finalCostRounding
                        if(configMap.currencyRate) {
                            costItem.currencyRate = configMap.currencyRate
                            if(!configMap.containsKey('costLocalCurrency')) {
                                costItem.costInLocalCurrency = configMap.currencyRate * costItem.costInBillingCurrency
                                costItem.costInLocalCurrencyAfterTax = costItem.costInLocalCurrency * (1.0 + (0.01 * taxRate))
                            }
                        }
                        if(configMap.costBillingCurrency) {
                            costItem.costInBillingCurrency = configMap.costBillingCurrency
                            costItem.costInBillingCurrencyAfterTax = configMap.costBillingCurrency * (1.0 + (0.01 * taxRate))
                            if(!configMap.containsKey('costLocalCurrency')) {
                                costItem.costInLocalCurrency = costItem.currencyRate * configMap.costBillingCurrency
                                costItem.costInLocalCurrencyAfterTax = costItem.costInLocalCurrency * (1.0 + (0.01 * taxRate))
                            }
                        }
                        costItem.billingCurrency = configMap.billingCurrency ?: costItem.billingCurrency
                        if(configMap.costLocalCurrency) {
                            costItem.costInLocalCurrency = configMap.costLocalCurrency
                            costItem.costInLocalCurrencyAfterTax = costItem.costInLocalCurrency * (1.0 + (0.01 * taxRate))
                            if(!configMap.containsKey('costBillingCurrency')) {
                                costItem.costInBillingCurrency = configMap.costLocalCurrency / costItem.currencyRate
                                costItem.costInBillingCurrencyAfterTax = configMap.costBillingCurrency * (1.0 + (0.01 * taxRate))
                            }
                        }
                        if (costItem.billingSumRounding) {
                            costItem.costInBillingCurrency = Math.round(costItem.costInBillingCurrency)
                            costItem.costInLocalCurrency = Math.round(costItem.costInLocalCurrency)
                        }
                        if (costItem.finalCostRounding) {
                            costItem.costInBillingCurrencyAfterTax = Math.round(costItem.costInBillingCurrencyAfterTax)
                            costItem.costInLocalCurrencyAfterTax = Math.round(costItem.costInLocalCurrencyAfterTax)
                        }
                        if(configMap.isVisibleForSubscriber != null) {
                            costItem.isVisibleForSubscriber = result.subscription?._getCalculatedType() == CalculatedType.TYPE_ADMINISTRATIVE ? false : configMap.isVisibleForSubscriber
                        }
                        costItem.costDescription = configMap.costDescription ?: costItem.costDescription
                        costItem.costTitle = configMap.costTitle ?: costItem.costTitle
                        costItem.datePaid = configMap.datePaid ?: costItem.datePaid
                        costItem.startDate = configMap.startDate ?: costItem.startDate
                        costItem.endDate = configMap.endDate ?: costItem.endDate
                        costItem.invoiceDate = configMap.invoiceDate ?: costItem.invoiceDate
                        costItem.financialYear = configMap.financialYear ?: costItem.financialYear
                        costItem.reference = configMap.reference ?: costItem.reference
                        if(costItem.save()) {
                            List<BudgetCode> newBcObjs = []
                            params.list('newBudgetCodes').each { newbc ->
                                BudgetCode bc = (BudgetCode) genericOIDService.resolveOID(newbc)
                                if (bc) {
                                    newBcObjs << bc
                                    if (! CostItemGroup.findByCostItemAndBudgetCode( costItem, bc )) {
                                        new CostItemGroup(costItem: costItem, budgetCode: bc).save()
                                    }
                                }
                            }
                            List<BudgetCode> toDelete = costItem.budgetcodes.minus(newBcObjs)
                            toDelete.each{ BudgetCode bc ->
                                CostItemGroup cig = CostItemGroup.findByCostItemAndBudgetCode( costItem, bc )
                                if (cig) {
                                    log.debug('deleting ' + cig)
                                    cig.delete()
                                }
                            }
                            List<CostItem> copiedCostItems = CostItem.findAllByCopyBaseAndCostItemStatusNotEqualAndOwnerNotEqual(costItem, RDStore.COST_ITEM_DELETED, result.institution)
                            //notify cost items copied from this cost item
                            copiedCostItems.each { CostItem cci ->
                                List diffs = []
                                if(costItem.costInBillingCurrencyAfterTax != cci.costInBillingCurrency) {
                                    diffs.add([prop:'billingCurrency', msgToken: PendingChangeConfiguration.BILLING_SUM_UPDATED, oldValue: cci.costInBillingCurrency, newValue:costItem.costInBillingCurrencyAfterTax])
                                }
                                if(costItem.costInLocalCurrencyAfterTax != cci.costInLocalCurrency) {
                                    diffs.add([prop:'localCurrency',msgToken:PendingChangeConfiguration.LOCAL_SUM_UPDATED,oldValue: cci.costInLocalCurrency,newValue:costItem.costInLocalCurrencyAfterTax])
                                }
                                diffs.each { diff ->
                                    try {
                                        PendingChange.construct([target:cci,owner:cci.owner,prop:diff.prop,oldValue:diff.oldValue,newValue:diff.newValue,msgToken:diff.msgToken,status:RDStore.PENDING_CHANGE_PENDING])
                                    }
                                    catch (CreationException e) {
                                        log.error( e.toString() )
                                    }
                                }
                            }
                        }
                        else {
                            log.error("error on updating cost item: ${costItem.errors.getAllErrors().toListString()}")
                        }
                    }
                }
            }
        }
        [result:result,status:STATUS_OK]
    }

    /**
     * Parses the given tax input and returns the matching tax enum key
     * @param newTaxRateString the tax input from any cost input modal
     * @return the tax key if a match was found, null otherwise
     */
    CostItem.TAX_TYPES setTaxKey(String newTaxRateString) {
        CostItem.TAX_TYPES tax_key = null //on invoice, self declared, etc
        if(newTaxRateString && !newTaxRateString.contains("null")) {
            String[] newTaxRate = newTaxRateString.split("§")
            RefdataValue taxType = (RefdataValue) genericOIDService.resolveOID(newTaxRate[0])
            int taxRate = Integer.parseInt(newTaxRate[1])
            switch(taxType.id) {
                case RDStore.TAX_TYPE_TAXABLE.id:
                    switch(taxRate) {
                        case 5: tax_key = CostItem.TAX_TYPES.TAXABLE_5
                            break
                        case 7: tax_key = CostItem.TAX_TYPES.TAXABLE_7
                            break
                        case 16: tax_key = CostItem.TAX_TYPES.TAXABLE_16
                            break
                        case 19: tax_key = CostItem.TAX_TYPES.TAXABLE_19
                            break
                    }
                    break
                case RDStore.TAX_TYPE_TAXABLE_EXEMPT.id:
                    tax_key = CostItem.TAX_TYPES.TAX_EXEMPT
                    break
                case RDStore.TAX_TYPE_NOT_TAXABLE.id:
                    tax_key = CostItem.TAX_TYPES.TAX_NOT_TAXABLE
                    break
                case RDStore.TAX_TYPE_NOT_APPLICABLE.id:
                    tax_key = CostItem.TAX_TYPES.TAX_NOT_APPLICABLE
                    break
                case RDStore.TAX_TYPE_REVERSE_CHARGE.id:
                    tax_key = CostItem.TAX_TYPES.TAX_REVERSE_CHARGE
                    break
                case RDStore.TAX_TYPE_TAX_CONTAINED_7.id:
                    tax_key = CostItem.TAX_TYPES.TAX_CONTAINED_7
                    break
                case RDStore.TAX_TYPE_TAX_CONTAINED_19.id:
                    tax_key = CostItem.TAX_TYPES.TAX_CONTAINED_19
                    break
            }
        }
        tax_key
    }

    /**
     * Configures the fields for the cost item input modal
     * @param params the parameters with which the modal has been called and which serve as configuration base
     * @param contextOrg the institution ({@link Org}) whose perspective is going to be considered
     * @return a {@link Map} containing the display parameters for the cost item editing modal
     */
    Map<String, Object> setupConfigMap(GrailsParameterMap params, Org contextOrg) {
        //structure according to the cost item input modal
        //block header
        //row 1
        String costTitle = params.newCostTitle ?: null
        Boolean isVisibleForSubscriber = null //built-in datatype cannot be set to null while I need null value to check if value should be changed or not!
        //one form sends 'null' as string, the other sends indeed no value ...
        if(params.newIsVisibleForSubscriber != 'null' && params.newIsVisibleForSubscriber != null)
            isVisibleForSubscriber = params.long('newIsVisibleForSubscriber') == RDStore.YN_YES.id
        RefdataValue costItemElement = params.newCostItemElement ? (RefdataValue.get(params.long('newCostItemElement'))): null    //admin fee, platform, etc
        RefdataValue elementSign
        try {
            elementSign = RefdataValue.get(Long.parseLong(params.ciec))
        }
        catch (Exception ignored) {
            elementSign = null
        }
        //row 2
        String reference = params.newReference ? params.newReference.trim() : null
        RefdataValue costItemStatus = params.newCostItemStatus ? (RefdataValue.get(params.long('newCostItemStatus'))) : null    //estimate, commitment, etc
        //block sum
        Locale userInputLocale = LocaleUtils.getCurrentLocale()
        if(contextService.getUser().getSettingsValue(UserSetting.KEYS.LANGUAGE) == RDStore.LANGUAGE_DE) {
            userInputLocale = LocaleUtils.getLocaleDE()
        }
        else if(contextService.getUser().getSettingsValue(UserSetting.KEYS.LANGUAGE) == RDStore.LANGUAGE_EN) {
            userInputLocale = LocaleUtils.getLocaleEN()
        }
        NumberFormat format = NumberFormat.getInstance( userInputLocale )
        //row 1
        Double costBillingCurrency = params.newCostInBillingCurrency ? format.parse(params.newCostInBillingCurrency).doubleValue() : 0.0 //0.00
        RefdataValue billingCurrency = RefdataValue.get(params.long('newCostCurrency')) //billingCurrency should be not null
        //value is transient
        Double costBillingCurrencyAfterTax = params.newCostInBillingCurrencyAfterTax ? format.parse(params.newCostInBillingCurrencyAfterTax).doubleValue() : costBillingCurrency
        //row 2
        Double currencyRate = params.newCostCurrencyRate ? format.parse(params.newCostCurrencyRate) : 1.0 //1.00
        CostItem.TAX_TYPES taxKey = setTaxKey(params.newTaxRate)
        //row 3
        Double costLocalCurrency = params.newCostInLocalCurrency ? format.parse(params.newCostInLocalCurrency).doubleValue() : 0.0 //0.00
        //value is transient
        Double costLocalCurrencyAfterTax = params.newCostInLocalCurrencyAfterTax ? format.parse(params.newCostInLocalCurrencyAfterTax).doubleValue() : costLocalCurrency
        //block footer
        //row 1
        Date datePaid = DateUtils.parseDateGeneric(params.newDatePaid)
        Year financialYear = params.newFinancialYear ? Year.parse(params.newFinancialYear) : null
        Date invoiceDate = DateUtils.parseDateGeneric(params.newInvoiceDate)
        Invoice invoice = resolveInvoice(params.newInvoiceNumber, contextOrg)
        //row 2
        Date startDate = DateUtils.parseDateGeneric(params.newStartDate)
        Date endDate = DateUtils.parseDateGeneric(params.newEndDate)
        String costDescription = params.newDescription ? params.newDescription.trim() : null
        Order order = resolveOrder(params.newOrderNumber, contextOrg)
        [costTitle: costTitle,
         isVisibleForSubscriber: isVisibleForSubscriber,
         costItemElement: costItemElement,
         elementSign: elementSign,
         reference: reference,
         costItemStatus: costItemStatus,
         costBillingCurrency: costBillingCurrency,
         billingCurrency: billingCurrency,
         costBillingCurrencyAfterTax: costBillingCurrencyAfterTax,
         currencyRate: currencyRate,
         taxKey: taxKey,
         costLocalCurrency: costLocalCurrency,
         costLocalCurrencyAfterTax: costLocalCurrencyAfterTax,
         datePaid: datePaid,
         financialYear: financialYear,
         invoiceDate: invoiceDate,
         invoice: invoice,
         startDate: startDate,
         endDate: endDate,
         costDescription: costDescription,
         order: order]
    }

    /**
     * Gets the given order for the given institution; if it does not exist, it will be created
     * @param newOrderNumber the order number being requested
     * @param contextOrg the institution whose order number should be retrieved
     * @return the new or retrieved order number
     */
    Order resolveOrder(String newOrderNumber, Org contextOrg) {
        Order order = null
        if (newOrderNumber) {
            order = Order.findByOrderNumberAndOwner(newOrderNumber, contextOrg)
            if(!order) {
                order = new Order(orderNumber: newOrderNumber, owner: contextOrg)
                order.save()
            }
        }
        order
    }

    /**
     * Gets the given invoice for the given institution; if it does not exist, it will be created
     * @param newInvoiceNumber the invoice number being requested
     * @param contextOrg the institution whose invoice number should be retrieved
     * @return the new or retrieved invoice number
     */
    Invoice resolveInvoice(String newInvoiceNumber, Org contextOrg) {
        Invoice invoice = null
        if (newInvoiceNumber) {
            invoice = Invoice.findByInvoiceNumberAndOwner(newInvoiceNumber, contextOrg)
            if(!invoice) {
                invoice = new Invoice(invoiceNumber: newInvoiceNumber, owner: contextOrg)
                invoice.save()
            }
        }
        invoice
    }

    //---------------------------------------------- display section ---------------------------------------------------

    /**
     * Retrieves the cost item data for the given subscription type and returns a map grouping the cost items per view
     * @param params the request parameter map containing filter data for the costs
     * @param configMap a map containing result generics and general view configuration
     * @return a LinkedHashMap with the cost items for each tab to display
     */
    Map getCostItemsForSubscription(GrailsParameterMap params,Map configMap) throws FinancialDataException {
        Map<String,RefdataValue> genericExcludeParams = [deleted: RDStore.COST_ITEM_DELETED]
        if(configMap.subscription) {
            Profiler prf = new Profiler()
            prf.setBenchmark("init")
            Subscription sub = (Subscription) configMap.subscription
            params.filterKey = sub.id
            Org org = (Org) configMap.institution
            prf.setBenchmark("load filter")
            Map<String,Object> filterQuery = processFilterParams(params)
            Map<String,Object> result = [filterPresets:filterQuery.filterData]
            SortedSet<String> costTitles = new TreeSet<String>()
            costTitles.addAll(CostItem.executeQuery('select ci.costTitle from CostItem ci where (ci.owner = :ctx or ci.isVisibleForSubscriber = true) and ci.costTitle != null and (ci.sub = :sub or ci.sub.instanceOf = :sub) order by ci.costTitle asc', [ctx: org, sub: sub]))
            SortedSet<BudgetCode> budgetCodes = new TreeSet<BudgetCode>()
            budgetCodes.addAll(BudgetCode.findAllByOwner(org, [sort: 'value']))
            result.filterSet = filterQuery.subFilter || filterQuery.ciFilter
            configMap.dataToDisplay.each { String dataToDisplay ->
                switch(dataToDisplay) {
                    case "own":
                        prf.setBenchmark("before own query")
                        String subFilter = filterQuery.subFilter
                        subFilter = subFilter.replace(" and oo.org in (:filterConsMembers) ","")
                        Map<String,Object> ownFilter = [:]
                        ownFilter.putAll(filterQuery.filterData)
                        ownFilter.remove('filterConsMembers')
                        Set<Long> ownCostItems = CostItem.executeQuery(
                                'select ci from CostItem ci where ci.owner = :owner and ci.sub = :sub '+
                                        genericExcludes + subFilter + filterQuery.ciFilter,
                                [owner:org,sub:sub]+genericExcludeParams+ownFilter,
                                [sort: configMap.sortConfig.ownSort, order: configMap.sortConfig.ownOrder])
                        prf.setBenchmark("assembling map")
                        result.own = [count:ownCostItems.size()]
                        if(ownCostItems){
                            result.own.costItems = ownCostItems.drop(configMap.offsets.ownOffset).take(configMap.max)
                            result.own.sums = calculateResults(ownCostItems.id)
                            result.own.ids = ownCostItems.id
                        }
                        break
                    case "cons":
                        prf.setBenchmark("before cons query")
                        List consCostItems = CostItem.executeQuery('select ci from CostItem ci right join ci.sub sub join sub.orgRelations oo left join ci.costItemElement cie left join ci.costItemElementConfiguration ciec ' +
                                'where ci.owner = :owner and sub.instanceOf = :sub '+filterQuery.subFilter+' and oo.roleType in (:roleTypes) ' + genericExcludes + filterQuery.ciFilter +
                                'order by '+configMap.sortConfig.consSort+' '+configMap.sortConfig.consOrder+', ciec.value desc nulls first'+', cie.value_'+ LocaleUtils.getCurrentLang() +' asc nulls first',
                                [owner: org, sub: sub, roleTypes:[RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]+genericExcludeParams+filterQuery.filterData)
                        prf.setBenchmark("assembling map")
                        result.cons = [count:consCostItems.size()]
                        if(consCostItems) {
                            result.cons.costItems = consCostItems.drop(configMap.offsets.consOffset).take(configMap.max)
                            result.cons.sums = calculateResults(consCostItems.id)
                            result.cons.ids = consCostItems.id
                        }
                        break
                    case "consAtSubscr":
                        prf.setBenchmark("before cons at subscr")
                        Set<CostItem> consCostItems = CostItem.executeQuery('select ci from CostItem as ci right join ci.sub sub join sub.orgRelations oo where ci.owner = :owner and sub = :sub and oo.roleType = :roleType'+
                            filterQuery.subFilter + genericExcludes + filterQuery.ciFilter,
                            [owner:org,sub:sub,roleType: RDStore.OR_SUBSCRIPTION_CONSORTIA]+genericExcludeParams+filterQuery.filterData,
                                [sort: configMap.sortConfig.consSort, order: configMap.sortConfig.consOrder])
                        prf.setBenchmark("assembling map")
                        result.cons = [count:consCostItems.size()]
                        if(consCostItems) {
                            result.cons.costItems = consCostItems.drop(configMap.offsets.consOffset).take(configMap.max)
                            result.cons.sums = calculateResults(consCostItems.id)
                            result.cons.ids = consCostItems.id
                        }
                        break
                    case "subscr":
                        prf.setBenchmark("before subscr")
                        Set<CostItem> subscrCostItems = CostItem.executeQuery('select ci from CostItem as ci left join ci.costItemElementConfiguration ciec left join ci.costItemElement cie join ci.sub sub where ci.owner in :owner and sub = :sub and ci.isVisibleForSubscriber = true'+
                                 genericExcludes + filterQuery.subFilter + filterQuery.ciFilter + ' order by ' + configMap.sortConfig.subscrSort + ' ' + configMap.sortConfig.subscrOrder + ', ciec.value desc nulls first, cie.value_'+LocaleUtils.getCurrentLang(),
                                 [owner:[sub.getConsortia()],sub:sub]+genericExcludeParams+filterQuery.filterData)
                        prf.setBenchmark("assembling map")
                        result.subscr = [count:subscrCostItems.size()]
                        if(subscrCostItems) {
                            result.subscr.costItems = subscrCostItems.drop(configMap.offsets.subscrOffset).take(configMap.max)
                            result.subscr.sums = calculateResults(subscrCostItems.id)
                            result.subscr.ids = subscrCostItems.id
                        }
                        break
                }
            }
            result.ciTitles = costTitles
            result.budgetCodes = budgetCodes
            result.benchMark = prf.stopBenchmark()
            result
        }
        else if(!configMap.subscription) {
            throw new FinancialDataException("Method called without valid subscription data!")
        }
    }

    /**
     * Retrieves the cost item data according to the given parameter and configuration maps and returns a map grouping the cost items per view
     * @param params the request parameter map containing filter data for the costs
     * @param configMap a map containing result generics and general view configuration
     * @return a {@link Map} with the cost items for each tab to display
     * @see CostItem
     */
    Map<String,Object> getCostItems(GrailsParameterMap params, Map configMap) throws FinancialDataException {
        Map<String,RefdataValue> genericExcludeParams = [deleted: RDStore.COST_ITEM_DELETED]
        Profiler prf = new Profiler()
        prf.setBenchmark("load filter params")
        params.filterKey = "global"
        Map<String,Object> filterQuery = processFilterParams(params)
        Map<String,Object> result = [filterPresets:filterQuery.filterData]
        result.filterSet = filterQuery.subFilter || filterQuery.ciFilter
        Org org = (Org) configMap.institution
        SortedSet<String> ciTitles = new TreeSet<String>()
        ciTitles.addAll(CostItem.executeQuery('select ci.costTitle from CostItem ci where (ci.owner = :ctx or (exists(select oo from OrgRole oo where oo.sub = ci.sub and oo.org = :ctx and oo.roleType = :subscrType and ci.isVisibleForSubscriber = true))) and ci.costTitle != null order by ci.costTitle asc', [ctx: org, subscrType: RDStore.OR_SUBSCRIBER_CONS]))
        result.ciTitles = ciTitles
        SortedSet<BudgetCode> budgetCodes = new TreeSet<BudgetCode>()
        budgetCodes.addAll(BudgetCode.findAllByOwner(org, [sort: 'value']))
        result.budgetCodes = budgetCodes
        prf.setBenchmark("load cost data for tabs")
        configMap.dataToDisplay.each { String dataToDisplay ->
            switch(dataToDisplay) {
                //get own costs
                case "own":
                    //exclude double listing of cost items belonging to member subscriptions
                    String instanceFilter = ""
                    if (org.isCustomerType_Consortium()) {
                        instanceFilter = " and sub.instanceOf = null "
                    }
                    String subJoin = filterQuery.subFilter || instanceFilter ? "join ci.sub sub " : ""
                    String subFilter = filterQuery.subFilter+instanceFilter
                    subFilter = subFilter.replace(" and oo.org in (:filterConsMembers) ","")
                    Map<String,Object> ownFilter = [:]
                    ownFilter.putAll(filterQuery.filterData)
                    ownFilter.remove('filterConsMembers')
                    String queryStringBase = "select ci from CostItem ci ${subJoin} left join ci.costItemElement cie left join ci.costItemElementConfiguration ciec " +
                        "where ci.owner = :org ${genericExcludes+subFilter+filterQuery.ciFilter} "+
                        "order by "+configMap.sortConfig.ownSort+" "+configMap.sortConfig.ownOrder+', ciec.value, cie.value_'+LocaleUtils.getCurrentLang()
                    prf.setBenchmark("execute own query")
                    Set<CostItem> ownSubscriptionCostItems = CostItem.executeQuery(queryStringBase,[org:org]+genericExcludeParams+ownFilter)
                    if(!filterQuery.subFilter) {
                        ownFilter.remove('filterSubStatus')
                        String queryWithoutSub = "select ci from CostItem ci left join ci.costItemElement cie left join ci.costItemElementConfiguration ciec " +
                                "where ci.owner = :org and ci.sub = null ${genericExcludes+filterQuery.ciFilter} "+
                                "order by "+configMap.sortConfig.ownSort+" "+configMap.sortConfig.ownOrder + ', ciec.value desc, cie.value_' + LocaleUtils.getCurrentLang()
                        prf.setBenchmark("execute second own query")
                        ownSubscriptionCostItems.addAll(CostItem.executeQuery(queryWithoutSub,[org:org]+genericExcludeParams+ownFilter))
                    }
                    result.own = [count:ownSubscriptionCostItems.size()]
                    prf.setBenchmark("map assembly")
                    if(ownSubscriptionCostItems) {
                        result.own.costItems = ownSubscriptionCostItems.drop(configMap.offsets.ownOffset).take(configMap.max)
                        result.own.ids = ownSubscriptionCostItems.id
                        result.own.sums = calculateResults(ownSubscriptionCostItems.id)
                    }
                        break
                //get consortial costs
                case "cons":
                    prf.setBenchmark("execute cons query")
                    Set<CostItem> consortialCostRows = CostItem.executeQuery('select ci from CostItem ci ' +
                        'left join ci.costItemElementConfiguration ciec ' +
                        'left join ci.costItemElement cie ' +
                        'join ci.owner orgC ' +
                        'join ci.sub sub ' +
                        'join sub.instanceOf subC ' +
                        'join subC.orgRelations roleC ' +
                        'join sub.orgRelations roleMC ' +
                        'join sub.orgRelations oo ' +
                        'where orgC = :org and orgC = roleC.org and roleMC.roleType = :consortialType and oo.roleType in (:subscrType)'+
                        genericExcludes+filterQuery.subFilter+filterQuery.ciFilter+
                        'order by '+configMap.sortConfig.consSort+' '+configMap.sortConfig.consOrder+', sub.name, ciec.value desc, cie.value_'+ LocaleUtils.getCurrentLang() +' desc',
                        [org:org,consortialType:RDStore.OR_SUBSCRIPTION_CONSORTIA,subscrType:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]+genericExcludeParams+filterQuery.filterData)
                    result.cons = [count:consortialCostRows.size()]
                    if(consortialCostRows) {
                        Set<CostItem> consortialCostItems = consortialCostRows
                        prf.setBenchmark("map assembly")
                        result.cons.ids = consortialCostRows.id
                        //result.cons.costItems = CostItem.executeQuery('select ci from CostItem ci right join ci.sub sub join sub.orgRelations oo left join ci.costItemElementConfiguration ciec where ci.id in (:ids) order by '+configMap.sortConfig.consSort+' '+configMap.sortConfig.consOrder+', ciec.value desc',[ids:consortialCostRows],[max:configMap.max, offset:configMap.offsets.consOffset]).toSet()
                        result.cons.costItems = consortialCostItems.drop(configMap.offsets.consOffset).take(configMap.max)
                        //very ugly ... any ways to achieve this more elegantly are greatly appreciated!!
                        /*if(configMap.sortConfig.consSort == 'oo.org.sortname') {
                            result.cons.costItems = result.cons.costItems.sort{ ciA, ciB ->
                                ciA.sub?.orgRelations?.find{ oo -> oo.roleType in [RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]}?.org?.sortname?.toLowerCase() <=> ciB.sub?.orgRelations?.find{ oo -> oo.roleType in [RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]}?.org?.sortname?.toLowerCase() ?:
                                        ciA.sub?.orgRelations?.find { oo -> oo.roleType in [RDStore.OR_AGENCY,RDStore.OR_PROVIDER]}?.org?.name?.toLowerCase() <=> ciB.sub?.orgRelations?.find{ oo -> oo.roleType in [RDStore.OR_AGENCY,RDStore.OR_PROVIDER]}?.org?.name?.toLowerCase() ?:
                                        ciA.sub?.name?.toLowerCase() <=> ciB.sub?.name?.toLowerCase() }
                        }*/
                        result.cons.sums = calculateResults(consortialCostItems.id)
                    }
                    break
                //get membership costs
                case "subscr":
                    prf.setBenchmark("execute subscr query")
                    Set<CostItem> consortialMemberSubscriptionCostItems = CostItem.executeQuery('select ci from CostItem ci '+
                        'join ci.sub sub ' +
                        'left join ci.subPkg subPkg ' +
                        'join sub.instanceOf subC ' +
                        'join subC.orgRelations roleC ' +
                        'join sub.orgRelations oo ' +
                        'join ci.owner orgC ' +
                        'left join ci.costItemElement cie ' +
                        'left join ci.costItemElementConfiguration ciec ' +
                        'where orgC = roleC.org and roleC.roleType = :consType and oo.org = :org and oo.roleType = :subscrType and ci.isVisibleForSubscriber = true'+
                        genericExcludes + filterQuery.subFilter + filterQuery.ciFilter +
                        ' order by '+configMap.sortConfig.subscrSort+' '+configMap.sortConfig.subscrOrder+', sub.name, ciec.value desc, cie.value_'+ LocaleUtils.getCurrentLang() +' asc nulls first',
                        [org:org,consType:RDStore.OR_SUBSCRIPTION_CONSORTIA,subscrType:RDStore.OR_SUBSCRIBER_CONS]+genericExcludeParams+filterQuery.filterData)
                    result.subscr = [count:consortialMemberSubscriptionCostItems.size()]
                    if(consortialMemberSubscriptionCostItems) {
                        result.subscr.sums = calculateResults(consortialMemberSubscriptionCostItems.id)
                        result.subscr.ids = consortialMemberSubscriptionCostItems.id
                        result.subscr.costItems = consortialMemberSubscriptionCostItems.drop(configMap.offsets.subscrOffset).take(configMap.max)
                    }
                    break
                default: log.info("display call ${dataToDisplay} not handled here ... skipping ...")
                    break
            }
        }
        result.benchMark = prf.stopBenchmark()
        result
    }

    /**
     * Processes the given parameters to build a query part which will be included into the base query strings
     * @param params a GrailsParameterMap containing parameters to be processed
     * @return an array with the filter string on position 0 and the filter parameter map on position 1
     */
    Map<String,Object> processFilterParams(GrailsParameterMap params) {
        Map<String,Object> result
        String subFilterQuery = "", costItemFilterQuery = ""
        Map<String,Object> queryParams = [:]
        EhcacheWrapper cache = contextService.getUserCache("/finance/${params.filterKey}/filter/")
        if((cache && cache.get('cachedFilter')) && params.reset == null && params.submit == null && !params.subDetailsPage) {
            Map<String,Object> cachedFilter = (Map<String, Object>) cache.get('cachedFilter')
            result = [subFilter:cachedFilter.subFilter,ciFilter:cachedFilter.ciFilter,filterData:cachedFilter.filterData]
        }
        else {
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            //subscription filter settings
            //subscription members A (from /subFinance)
            if(params.filterSubMembers) {
                subFilterQuery += " and sub in (:filterSubMembers) "
                List<Subscription> filterSubMembers = []
                String[] subMembers = params.list("filterSubMembers")
                subMembers.each { String subMember ->
                    filterSubMembers.add(Subscription.get(Long.parseLong(subMember)))
                }
                queryParams.filterSubMembers = filterSubMembers
            }
            //subscription members B (from /finance)
            if(params.filterConsMembers) {
                subFilterQuery += " and oo.org in (:filterConsMembers) "
                List<Org> filterConsMembers = []
                String[] consMembers = params.list("filterConsMembers")
                consMembers.each { String consMember ->
                    filterConsMembers.add(Org.get(Long.parseLong(consMember)))
                }
                queryParams.filterConsMembers = filterConsMembers
            }
            //providers
            if(params.filterSubProviders) {
                subFilterQuery += " and sub in (select oo.sub from OrgRole as oo where oo.org in (:filterSubProviders)) "
                List<Org> filterSubProviders = []
                String[] subProviders
                if(params.filterSubProviders.contains(","))
                    subProviders = params.filterSubProviders.split(',')
                else subProviders = [params.filterSubProviders]
                subProviders.each { String subProvider ->
                    filterSubProviders.add(genericOIDService.resolveOID(subProvider))
                }
                queryParams.filterSubProviders = filterSubProviders
            }
            //subscription status
            //we have to distinct between not existent and present but zero length
            if(params.filterSubStatus) {
                subFilterQuery += " and sub.status = :filterSubStatus "
                queryParams.filterSubStatus = RefdataValue.get(Long.parseLong(params.filterSubStatus))
            }
            //!params.filterSubStatus is insufficient because it checks also the presence of a value - but the absence of a value is a valid setting (= all status except deleted; that is captured by the genericExcludes field)
            else if(!params.subscription && !params.sub && !params.id && !params.containsKey('filterSubStatus')) {
                subFilterQuery += " and sub.status = :filterSubStatus "
                queryParams.filterSubStatus = RDStore.SUBSCRIPTION_CURRENT
            }
            //cost item filter settings
            //cost item title
            if(params.filterCITitle) {
                costItemFilterQuery += " and (genfunc_filter_matcher(ci.costTitle, :filterCITitle) = true) "
                queryParams.filterCITitle = params.filterCITitle
                //queryParams.ciTitleLowerCase = params.filterCITitle.toLowerCase()
            }
            //cost item subscription
            if(params.filterCISub) {
                costItemFilterQuery += " and sub in (:filterCISub) "
                List<Subscription> filterSubs = []
                String[] subscriptions = params.list('filterCISub')
                subscriptions.each { String sub ->
                    filterSubs.add((Subscription) genericOIDService.resolveOID(sub))
                }
                queryParams.filterCISub = filterSubs
            }
            //subscription package
            if(params.filterCISPkg) {
                costItemFilterQuery += " and ci.subPkg in (:filterCISPkg) "
                List<SubscriptionPackage> filterSubPackages = []
                String[] subscriptionPackages = params.list('filterCISPkg')
                subscriptionPackages.each { String subPkg ->
                    filterSubPackages.add((SubscriptionPackage) genericOIDService.resolveOID(subPkg))
                }
                queryParams.filterCISPkg = filterSubPackages
            }
            //budget code
            if(params.filterCIBudgetCode) {
                costItemFilterQuery += " and ci in (select cig.costItem from CostItemGroup cig where cig.budgetCode in (:filterCIBudgetCode)) "
                List<BudgetCode> filterBudgetCodes = []
                String[] budgetCodes = params.list('filterCIBudgetCode')
                budgetCodes.each { String bc ->
                    filterBudgetCodes.add(BudgetCode.get(Long.parseLong(bc)))
                }
                queryParams.filterCIBudgetCode = filterBudgetCodes
            }
            //reference/code
            if(params.filterCIReference) {
                costItemFilterQuery += " and ci.reference in (:filterCIReference) "
                List<String> filterReferences = params.list('filterCIReference')
                queryParams.filterCIReference = filterReferences
            }
            //invoice number
            if(params.filterCIInvoiceNumber) {
                costItemFilterQuery += " and ci.invoice.invoiceNumber in (:filterCIInvoiceNumber) "
                List<String> filterInvoiceNumbers = []
                String[] invoiceNumbers = params.list('filterCIInvoiceNumber')
                invoiceNumbers.each { String invNum ->
                    filterInvoiceNumbers.add(invNum)
                }
                queryParams.filterCIInvoiceNumber = filterInvoiceNumbers
            }
            //order number
            if(params.filterCIOrderNumber) {
                costItemFilterQuery += " and ci.order.orderNumber in (:filterCIOrderNumber) "
                List<String> filterOrderNumbers = []
                String[] orderNumbers = params.list('filterCIOrderNumber')
                orderNumbers.each { String orderNum ->
                    filterOrderNumbers.add(orderNum)
                }
                queryParams.filterCIOrderNumber = filterOrderNumbers
            }
            //cost item element
            if(params.filterCIElement) {
                costItemFilterQuery += " and ci.costItemElement in (:filterCIElement) "
                List<RefdataValue> filterElements = []
                String[] costItemElements = params.list('filterCIElement')
                costItemElements.each { String cie ->
                    filterElements.add(genericOIDService.resolveOID(cie))
                }
                queryParams.filterCIElement = filterElements
            }
            //cost item status
            if(params.filterCIStatus) {
                costItemFilterQuery += " and ci.costItemStatus in (:filterCIStatus) "
                List<RefdataValue> filterStatus = []
                String[] costItemStatus = params.list("filterCIStatus")
                costItemStatus.each { String cis ->
                    filterStatus.add(genericOIDService.resolveOID(cis))
                }
                queryParams.filterCIStatus = filterStatus
            }
            //tax type
            if(params.filterCITaxType) {
                if(params.filterCITaxType == 'null') {
                    costItemFilterQuery += " and ci.taxKey = null"
                }
                else {
                    costItemFilterQuery += " and ci.taxKey = :filterCITaxType "
                    queryParams.filterCITaxType = CostItem.TAX_TYPES.valueOf(params.filterCITaxType)
                }
            }
            //currency
            if(params.filterCICurrency) {
                costItemFilterQuery += " and ci.billingCurrency = :filterCICurrency "
                queryParams.filterCICurrency = RefdataValue.get(params.filterCICurrency)
            }
            //financial year
            if(params.filterCIFinancialYear) {
                costItemFilterQuery += " and ci.financialYear = :filterCIFinancialYear "
                Year financialYear = Year.parse(params.filterCIFinancialYear)
                queryParams.filterCIFinancialYear = financialYear
            }
            //invoice from
            if(params.filterCIInvoiceFrom) {
                costItemFilterQuery += " and (ci.invoiceDate >= :filterCIInvoiceFrom AND ci.invoiceDate is not null) "
                Date invoiceFrom = sdf.parse(params.filterCIInvoiceFrom)
                queryParams.filterCIInvoiceFrom = invoiceFrom
            }
            //invoice to
            if(params.filterCIInvoiceTo) {
                costItemFilterQuery += " and (ci.invoiceDate <= :filterCIInvoiceTo AND ci.invoiceDate is not null) "
                Date invoiceTo = sdf.parse(params.filterCIInvoiceTo)
                queryParams.filterCIInvoiceTo = invoiceTo
            }
            //date from
            if(params.filterCIDateFrom) {
                costItemFilterQuery += " and ci.startDate = :filterCIDateFrom "
                Date dateFrom = sdf.parse(params.filterCIDateFrom)
                queryParams.filterCIDateFrom = dateFrom
            }
            //date to
            if(params.filterCIDateTo) {
                costItemFilterQuery += " and ci.endDate = :filterCIDateTo "
                Date dateTo = sdf.parse(params.filterCIDateTo)
                queryParams.filterCIDateTo = dateTo
            }
            //valid on
            if(params.filterCIValidOn) {
                costItemFilterQuery += " and (ci.startDate <= :filterCIValidOn OR ci.startDate is null) and (ci.endDate >= :filterCIValidOn OR ci.endDate is null) "
                Date validOn = sdf.parse(params.filterCIValidOn)
                queryParams.filterCIValidOn = validOn
            }
            if(params.filterCIUnpaid) {
                costItemFilterQuery += " and ci.datePaid is null "
            }
            else {
                //paid from
                if(params.filterCIPaidFrom) {
                    costItemFilterQuery += " and (ci.datePaid >= :filterCIPaidFrom AND ci.datePaid is not null) "
                    Date invoiceFrom = sdf.parse(params.filterCIPaidFrom)
                    queryParams.filterCIPaidFrom = invoiceFrom
                }
                //paid to
                if(params.filterCIPaidTo) {
                    costItemFilterQuery += " and (ci.datePaid <= :filterCIPaidTo AND ci.datePaid is not null) "
                    Date invoiceTo = sdf.parse(params.filterCIPaidTo)
                    queryParams.filterCIPaidTo = invoiceTo
                }
            }
            result = [subFilter:subFilterQuery,ciFilter:costItemFilterQuery,filterData:queryParams]
            if(params.reset || params.submit)
                cache.put('cachedFilter',result)
        }
        result
    }

    /**
     * Replaced the client-side calculation of the sums.
     * Calculates to a given key the sums (local and for each currency) and assigns the resulting map to a given key
     * @param costItems a list of cost items to count
     * @return a map with the following structure:
     * {
     *     localSum (means the actual value and not the amount which is going to be paid)
     *     localSumAfterTax (for Germany, VATs of 7 and 19 per cent apply) (consider ERMS-957 about that!)
     *     billingSums: {
     *        currency
     *        billingSum
     *        billingSumAfterTax (see above; consider ERMS-957, too!)
     *     }
     * }
     *
     */
    Map<String,Object> calculateResults(Collection<Long> costItems) {
        //List<Map> billingSumsPositive = CostItem.executeQuery("select NEW map(ci.billingCurrency.value as currency,sum(ci.costInBillingCurrency) as billingSum,sum(ci.costInBillingCurrency * ((ci.taxKey.taxRate/100.0) + 1)) as billingSumAfterTax) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'positive' group by ci.billingCurrency.value",[costItems:costItems])
        List billingSumsPositive = CostItem.executeQuery("select NEW map(ci.billingCurrency.value as currency,sum(ci.costInBillingCurrency) as billingSum,sum(ci.costInBillingCurrency * (((case when ci.taxKey = :tax5 then 5 when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax16 then 16 when ci.taxKey = :tax19 then 19 else 0 end)/100.0) + 1)) as billingSumAfterTax,sum(ci.costInLocalCurrency) as localSum,sum(ci.costInLocalCurrency * (((case when ci.taxKey = :tax5 then 5 when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax16 then 16 when ci.taxKey = :tax19 then 19 else 0 end)/100.0) + 1)) as localSumAfterTax,ci.billingCurrency.order as ciOrder) from CostItem ci where ci.id in (:costItems) and ci.costItemElementConfiguration.value = 'positive' group by ci.billingCurrency.value, ci.billingCurrency.order order by ciOrder",[costItems:costItems,tax5:CostItem.TAX_TYPES.TAXABLE_5,tax7:CostItem.TAX_TYPES.TAXABLE_7,tax16:CostItem.TAX_TYPES.TAXABLE_16,tax19:CostItem.TAX_TYPES.TAXABLE_19])
        List billingSumsNegative = CostItem.executeQuery("select NEW map(ci.billingCurrency.value as currency,sum(ci.costInBillingCurrency) as billingSum,sum(ci.costInBillingCurrency * (((case when ci.taxKey = :tax5 then 5 when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax16 then 16 when ci.taxKey = :tax19 then 19 else 0 end)/100.0) + 1)) as billingSumAfterTax,sum(ci.costInLocalCurrency) as localSum,sum(ci.costInLocalCurrency * (((case when ci.taxKey = :tax5 then 5 when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax16 then 16 when ci.taxKey = :tax19 then 19 else 0 end)/100.0) + 1)) as localSumAfterTax,ci.billingCurrency.order as ciOrder) from CostItem ci where ci.id in (:costItems) and ci.costItemElementConfiguration.value = 'negative' group by ci.billingCurrency.value, ci.billingCurrency.order order by ciOrder",[costItems:costItems,tax5:CostItem.TAX_TYPES.TAXABLE_5,tax7:CostItem.TAX_TYPES.TAXABLE_7,tax16:CostItem.TAX_TYPES.TAXABLE_16,tax19:CostItem.TAX_TYPES.TAXABLE_19])
        Map<BigDecimal,BigDecimal> localSumsPositive = CostItem.executeQuery("select NEW map(sum(ci.costInLocalCurrency) as localSum,sum(ci.costInLocalCurrency * (((case when ci.taxKey = :tax5 then 5 when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax16 then 16 when ci.taxKey = :tax19 then 19 else 0 end) / 100.0) + 1)) as localSumAfterTax) from CostItem ci where ci.id in (:costItems) and ci.costItemElementConfiguration.value = 'positive'",[costItems:costItems,tax5:CostItem.TAX_TYPES.TAXABLE_5,tax7:CostItem.TAX_TYPES.TAXABLE_7,tax16:CostItem.TAX_TYPES.TAXABLE_16,tax19:CostItem.TAX_TYPES.TAXABLE_19]).get(0)
        Map<BigDecimal,BigDecimal> localSumsNegative = CostItem.executeQuery("select NEW map(sum(ci.costInLocalCurrency) as localSum,sum(ci.costInLocalCurrency * (((case when ci.taxKey = :tax5 then 5 when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax16 then 16 when ci.taxKey = :tax19 then 19 else 0 end) / 100.0) + 1)) as localSumAfterTax) from CostItem ci where ci.id in (:costItems) and ci.costItemElementConfiguration.value = 'negative'",[costItems:costItems,tax5:CostItem.TAX_TYPES.TAXABLE_5,tax7:CostItem.TAX_TYPES.TAXABLE_7,tax16:CostItem.TAX_TYPES.TAXABLE_16,tax19:CostItem.TAX_TYPES.TAXABLE_19]).get(0)
        List billingSums = []
        Set<String> positiveCurrencies = []
        Map<String,BigDecimal> localSums = [:]
        BigDecimal billingSum = 0.0
        BigDecimal billingSumAfterTax = 0.0
        BigDecimal localSum = 0.0
        BigDecimal localSumAfterTax = 0.0
        if(billingSumsPositive.size() > 0) {
            billingSumsPositive.each { posEntry ->
                if (billingSumsNegative.size() > 0) {
                    int index = getCurrencyIndexInList(billingSumsNegative,posEntry.currency)
                    if(index > -1) {
                        Map negEntry = billingSumsNegative[index]
                        billingSum = posEntry.billingSum - negEntry.billingSum
                        billingSumAfterTax = posEntry.billingSumAfterTax - negEntry.billingSumAfterTax
                        localSum = posEntry.localSum - negEntry.localSum
                        localSumAfterTax = posEntry.localSumAfterTax - negEntry.localSumAfterTax
                    }
                    else {
                        billingSum = posEntry.billingSum
                        billingSumAfterTax = posEntry.billingSumAfterTax
                        localSum = posEntry.localSum ?: 0.0
                        localSumAfterTax = posEntry.localSumAfterTax ?: 0.0
                    }
                }
                else {
                    billingSum = posEntry.billingSum
                    billingSumAfterTax = posEntry.billingSumAfterTax
                    localSum = posEntry.localSum ?: 0.0
                    localSumAfterTax = posEntry.localSumAfterTax ?: 0.0
                }
                positiveCurrencies << posEntry.currency
                billingSums.add([currency: posEntry.currency, billingSum: billingSum, billingSumAfterTax: billingSumAfterTax, localSum: localSum, localSumAfterTax: localSumAfterTax])
            }
        }
        if(billingSumsNegative.size() > 0) {
            billingSumsNegative.each { negEntry ->
                if(!positiveCurrencies.contains(negEntry.currency))
                    billingSums.add([currency: negEntry.currency, billingSum: negEntry.billingSum * (-1), billingSumAfterTax: negEntry.billingSumAfterTax * (-1), localSum: negEntry.localSum * (-1), localSumAfterTax: negEntry.localSumAfterTax * (-1)])
            }
        }
        if(localSumsPositive.localSum && localSumsPositive.localSumAfterTax) {
            if(localSumsNegative.localSum && localSumsNegative.localSumAfterTax) {
                localSums.localSum = localSumsPositive.localSum-localSumsNegative.localSum
                localSums.localSumAfterTax = localSumsPositive.localSumAfterTax-localSumsNegative.localSumAfterTax
            }
            else {
                localSums.localSum = localSumsPositive.localSum
                localSums.localSumAfterTax = localSumsPositive.localSumAfterTax
            }
        }
        else if(localSumsNegative.localSum && localSumsNegative.localSumAfterTax) {
            localSums.localSum = localSumsNegative.localSum * (-1)
            localSums.localSumAfterTax = localSumsNegative.localSumAfterTax * (-1)
        }
        else {
            localSums.localSum = 0.0
            localSums.localSumAfterTax = 0.0
        }
        return [billingSums:billingSums,localSums:localSums]
    }

    /**
     * Finds the given currency in the given list of entries, returns -1 if the currency is not found in the list.
     * @param entryList the list of currency entries
     * @param currency the currency to be retrieved
     * @return the position index
     */
    int getCurrencyIndexInList(List entryList,String currency) {
        int ret = -1
        entryList.eachWithIndex { negEntry, int i ->
            if(currency.equals(negEntry.currency)) {
                ret = i
            }
        }
        return ret
    }

    /**
     * Processes the given TSV file with financial data and puts together a {@link Map} with the information read off the file
     * @param tsvFile the input file
     * @return a {@link Map} with the data read off
     */
    Map<String,Map> financeImport(MultipartFile tsvFile) {
        Org contextOrg = contextService.getOrg()
        Map<String,Map> result = [:]
        Map<CostItem,Map> candidates = [:]
        Map<Integer,String> budgetCodes = [:]
        List<String> rows = tsvFile.getInputStream().text.split('\n')
        Map<String,Integer> colMap = [:]
        rows[0].split('\t').eachWithIndex { String headerCol, int c ->
            if(headerCol.startsWith("\uFEFF"))
                headerCol = headerCol.substring(1)
            switch(headerCol.toLowerCase().trim()) {
                case ["bezeichnung", "title"]: colMap.title = c
                    break
                case "element": colMap.element = c
                    break
                case ["kostenvorzeichen", "cost item sign"]: colMap.costItemSign = c
                    break
                case "budgetcode": colMap.budgetCode = c
                    break
                case ["referenz/codes", "reference/codes"]: colMap.reference = c
                    break
                case "status": colMap.status = c
                    break
                case ["rechnungssumme", "invoice total"]: colMap.invoiceTotal = c
                    break
                case ["währung", "waehrung", "currency"]: colMap.currency = c
                    break
                case ["umrechnungsfaktor", "exchange rate"]: colMap.currencyRate = c
                    break
                case ["steuerbar", "tax type"]: colMap.taxType = c
                    break
                case ["steuersatz", "tax rate"]: colMap.taxRate = c
                    break
                case ["wert", "endpreis", "value"]: colMap.value = c
                    break
                case ["lizenz", "subscription"]: colMap.sub = c
                    break
                case ["paket", "package"]: colMap.subPkg = c
                    break
                case ["einzeltitel", "single title"]: colMap.ie = c
                    break
                case ["gezahlt am", "date paid"]: colMap.dateFrom = c
                    break
                case ["haushaltsjahr", "financial year"]: colMap.financialYear = c
                    break
                case ["datum von", "date from"]: colMap.dateFrom = c
                    break
                case ["datum bis", "date to"]: colMap.dateTo = c
                    break
                case ["rechnungsdatum", "invoice date"]: colMap.invoiceDate = c
                    break
                case ["anmerkung", "description"]: colMap.description = c
                    break
                case ["rechnungsnummer", "invoice number"]: colMap.invoiceNumber = c
                    break
                case ["auftragsnummer", "order number"]: colMap.orderNumber = c
                    break
                /*case "einrichtung":
                case "organisation": colMap.institution = c
                    break*/
                default: log.info("unhandled parameter type ${headerCol}, ignoring ...")
                    break
            }
        }
        rows.remove(0)
        Map<String,IdentifierNamespace> namespaces = [
                'wibid':IdentifierNamespace.findByNs('wibid'),
                'isil':IdentifierNamespace.findByNs('ISIL'),
                'doi':IdentifierNamespace.findByNsAndNsType('doi', TitleInstancePackagePlatform.class.name),
                'zdb':IdentifierNamespace.findByNsAndNsType('zdb', TitleInstancePackagePlatform.class.name),
                'issn':IdentifierNamespace.findByNsAndNsType('issn', TitleInstancePackagePlatform.class.name),
                'eissn':IdentifierNamespace.findByNsAndNsType('eissn', TitleInstancePackagePlatform.class.name),
                'isbn':IdentifierNamespace.findByNsAndNsType('isbn', TitleInstancePackagePlatform.class.name),
                'eisbn':IdentifierNamespace.findByNsAndNsType('eisbn', TitleInstancePackagePlatform.class.name),
                'title_id':IdentifierNamespace.findByNsAndNsType('title_id', TitleInstancePackagePlatform.class.name)
        ]
        rows.eachWithIndex { row, Integer r ->
            //log.debug("now processing entry ${r}")
            Map mappingErrorBag = [:]
            List<String> cols = row.split('\t')
            //check if we have some mandatory properties ...
            //owner(nullable: false, blank: false) -> to institution, defaults to context org
            CostItem costItem = new CostItem(owner: contextOrg)
            //sub(nullable: true, blank: false) -> to subscription
            Subscription subscription
            if(colMap.sub != null) {
                String subIdentifier = cols[colMap.sub]
                if(subIdentifier) {
                    //fetch possible identifier namespaces
                    List<Subscription> subMatches
                    if(contextService.getOrg().isCustomerType_Consortium())
                        subMatches = Subscription.executeQuery("select oo.sub from OrgRole oo where (cast(oo.sub.id as string) = :idCandidate or oo.sub.globalUID = :idCandidate) and oo.org = :org and oo.roleType in :roleType",[idCandidate:subIdentifier,org:costItem.owner,roleType:[RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER]])
                    else if(contextService.getOrg().isCustomerType_Inst_Pro())
                        subMatches = Subscription.executeQuery("select oo.sub from OrgRole oo where (cast(oo.sub.id as string) = :idCandidate or oo.sub.globalUID = :idCandidate) and oo.org = :org and oo.roleType in :roleType",[idCandidate:subIdentifier,org:costItem.owner,roleType:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER]])
                    if(!subMatches)
                        mappingErrorBag.noValidSubscription = subIdentifier
                    else if(subMatches.size() > 1)
                        mappingErrorBag.multipleSubError = subMatches.collect { sub -> sub.dropdownNamingConvention(contextOrg) }
                    else if(subMatches.size() == 1) {
                        subscription = subMatches[0]
                        costItem.sub = subscription
                    }
                }
            }
            /*
            subPkg(nullable: true, blank: false, validator: { val, obj ->
                if (obj.subPkg) {
                    if (obj.subPkg.subscription.id != obj.sub.id) return ['subscriptionPackageMismatch']
                }
            }) -> to package
            */
            SubscriptionPackage subPkg
            if(colMap.subPkg != null) {
                String subPkgIdentifier = cols[colMap.subPkg]
                if(subPkgIdentifier) {
                    if(subscription == null)
                        mappingErrorBag.packageWithoutSubscription = true
                    else {
                        //List<Package> pkgMatches = Package.executeQuery("select distinct idOcc.pkg from IdentifierOccurrence idOcc join idOcc.identifier id where cast(idOcc.pkg.id as string) = :idCandidate or idOcc.pkg.globalUID = :idCandidate or (id.value = :idCandidate and id.ns = :isil)",[idCandidate:subPkgIdentifier,isil:namespaces.isil])
                        List<Package> pkgMatches = []
                        if(subPkgIdentifier.isLong())
                            pkgMatches.add(Package.get(subPkgIdentifier))
                        if(!pkgMatches) {
                            pkgMatches.addAll(Package.findAllByGlobalUID(subPkgIdentifier))
                            if(!pkgMatches) {
                                pkgMatches = Package.executeQuery("select distinct ident.pkg from Identifier ident where (ident.ns = :isil and ident.value = :idCandidate)")
                                if(!pkgMatches)
                                    mappingErrorBag.noValidPackage = subPkgIdentifier
                            }
                        }
                        if(pkgMatches.size() > 1)
                            mappingErrorBag.multipleSubPkgError = pkgMatches.collect { pkg -> pkg.name }
                        else if(pkgMatches.size() == 1) {
                            subPkg = SubscriptionPackage.findBySubscriptionAndPkg(subscription,pkgMatches[0])
                            if(subPkg)
                                costItem.subPkg = subPkg
                            else if(!subPkg)
                                mappingErrorBag.packageNotInSubscription = subPkgIdentifier
                        }
                    }
                }
            }
            /*
            issueEntitlement(nullable: true, blank: false, validator: { val, obj ->
                if (obj.issueEntitlement) {
                    if (!obj.subPkg || (obj.issueEntitlement.tipp.pkg.gokbId != obj.subPkg.pkg.gokbId)) return ['issueEntitlementNotInPackage']
                }
            }) -> to issue entitlement
            */
            IssueEntitlement ie
            if(colMap.ie != null && cols[colMap.ie] != null) {
                String ieIdentifier = cols[colMap.ie].trim()
                if(ieIdentifier) {
                    if(subscription == null || subPkg == null)
                        mappingErrorBag.entitlementWithoutPackageOrSubscription = true
                    else {
                        List<IssueEntitlement> ieMatches = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.subscription = :subscription and ie.tipp in (select tipp from Identifier id join id.tipp tipp where ((id.value = :value and id.ns in (:namespaces)) or tipp.hostPlatformURL = :value) and tipp.status != :removed) and ie.status != :removed',[subscription:subscription,value:ieIdentifier, namespaces: namespaces.values(),removed:RDStore.TIPP_STATUS_REMOVED])
                        if(!ieMatches)
                            mappingErrorBag.noValidEntitlement = ieIdentifier
                        else if(ieMatches.size() > 1)
                            mappingErrorBag.multipleEntitlementError = ieMatches.collect { entMatch -> "${entMatch.subscription.dropdownNamingConvention(contextOrg)} - ${entMatch.name}" }
                        else if(ieMatches.size() == 1) {
                            ie = ieMatches[0]
                            costItem.issueEntitlement = ie
                        }
                    }
                }
            }
            //order(nullable: true, blank: false) -> to order number
            if(colMap.orderNumber != null) {
                String orderNumber = cols[colMap.orderNumber]
                if(orderNumber) {
                    List<Order> orderMatches = Order.findAllByOrderNumberAndOwner(orderNumber,contextOrg)
                    if(orderMatches.size() > 1)
                        mappingErrorBag.multipleOrderError = orderNumber
                    else if(orderMatches.size() == 1)
                        costItem.order = orderMatches[0]
                    else if(!orderMatches) {
                        Order order = new Order(orderNumber: orderNumber, owner: contextOrg)
                        if(order.save()) {
                            costItem.order = order
                        }
                        else log.error(order.errors)
                    }
                }
            }
            //invoice(nullable: true, blank: false) -> to invoice number
            if(colMap.invoiceNumber != null) {
                String invoiceNumber = cols[colMap.invoiceNumber]
                if(invoiceNumber) {
                    List<Invoice> invoiceMatches = Invoice.findAllByInvoiceNumberAndOwner(invoiceNumber,contextOrg)
                    if(invoiceMatches.size() > 1)
                        mappingErrorBag.multipleInvoiceError = invoiceNumber
                    else if(invoiceMatches.size() == 1)
                        costItem.invoice = invoiceMatches[0]
                    else if(!invoiceMatches) {
                        Invoice invoice = new Invoice(invoiceNumber: invoiceNumber, owner: contextOrg)
                        if(invoice.save()) {
                            costItem.invoice = invoice
                        }
                        else log.error(invoice.errors)
                    }
                }
            }
            //billingCurrency(nullable: true, blank: false) -> to currency
            if(colMap.currency != null) {
                String currencyKey = cols[colMap.currency]
                if(!currencyKey)
                    mappingErrorBag.noCurrencyError = true
                else {
                    RefdataValue currency = RefdataValue.getByValueAndCategory(currencyKey,"Currency")
                    if(!currency)
                        mappingErrorBag.invalidCurrencyError = true
                    else {
                        costItem.billingCurrency = currency
                    }
                }
            }
            //costDescription(nullable: true, blank: false) -> to description
            if(colMap.description != null) {
                costItem.costDescription = cols[colMap.description]
            }
            //costTitle(nullable: true, blank: false) -> to title
            if(colMap.title != null) {
                costItem.costTitle = cols[colMap.title]
            }
            //costInBillingCurrency(nullable: true, blank: false) -> to invoice total
            if(colMap.invoiceTotal != null && cols[colMap.invoiceTotal] != null) {
                try {
                    costItem.costInBillingCurrency = escapeService.parseFinancialValue(cols[colMap.invoiceTotal])
                }
                catch (NumberFormatException e) {
                    mappingErrorBag.invoiceTotalInvalid = true
                }
                catch (NullPointerException | ParseException e) {
                    mappingErrorBag.invoiceTotalMissing = true
                }
            }
            //datePaid(nullable: true, blank: false) -> to date paid
            if(colMap.datePaid != null) {
                Date datePaid = DateUtils.parseDateGeneric(cols[colMap.datePaid])
                if(datePaid)
                    costItem.datePaid = datePaid
            }
            //costInLocalCurrency(nullable: true, blank: false) -> to value
            if(colMap.value != null && cols[colMap.value] != null) {
                try {
                    costItem.costInLocalCurrency = escapeService.parseFinancialValue(cols[colMap.value])
                }
                catch (NumberFormatException e) {
                    mappingErrorBag.valueInvalid = true
                }
                catch (NullPointerException | ParseException e) {
                    mappingErrorBag.valueMissing = true
                }
            }
            //currencyRate(nullable: true, blank: false) -> to exchange rate
            if(colMap.currencyRate != null && cols[colMap.currencyRate] != null) {
                try {
                    costItem.currencyRate = escapeService.parseFinancialValue(cols[colMap.currencyRate])
                }
                catch (NumberFormatException e) {
                    mappingErrorBag.exchangeRateInvalid = true
                }
                catch (NullPointerException | ParseException e) {
                    mappingErrorBag.exchangeRateMissing = true
                }
            }
            //substitute missing values in case of
            if(costItem.costInBillingCurrency) {
                if(!costItem.currencyRate && costItem.costInLocalCurrency) {
                    costItem.currencyRate = costItem.costInLocalCurrency / costItem.costInBillingCurrency
                    mappingErrorBag.keySet().removeAll(['exchangeRateMissing','exchangeRateInvalid'])
                    mappingErrorBag.exchangeRateCalculated = true
                }
                else if(!costItem.costInLocalCurrency && costItem.currencyRate) {
                    costItem.costInLocalCurrency = costItem.costInBillingCurrency * costItem.currencyRate
                    mappingErrorBag.keySet().removeAll(['valueMissing','valueInvalid'])
                    mappingErrorBag.valueCalculated = true
                }
            }
            if(costItem.costInLocalCurrency) {
                if(!costItem.currencyRate && costItem.costInBillingCurrency) {
                    costItem.currencyRate = costItem.costInLocalCurrency / costItem.costInBillingCurrency
                    mappingErrorBag.keySet().removeAll(['exchangeRateMissing','exchangeRateInvalid'])
                    mappingErrorBag.exchangeRateCalculated = true
                }
                else if(!costItem.costInBillingCurrency && costItem.currencyRate) {
                    costItem.costInBillingCurrency = costItem.costInLocalCurrency * costItem.currencyRate
                    mappingErrorBag.keySet().removeAll(['invoiceTotalMissing','invoiceTotalInvalid'])
                    mappingErrorBag.invoiceTotalCalculated = true
                }
            }
            if(costItem.currencyRate) {
                if(!costItem.costInLocalCurrency && costItem.costInBillingCurrency) {
                    costItem.costInLocalCurrency = costItem.costInBillingCurrency * costItem.currencyRate
                    mappingErrorBag.keySet().removeAll(['valueMissing','valueInvalid'])
                    mappingErrorBag.valueCalculated = true
                }
                else if(!costItem.costInBillingCurrency && costItem.costInLocalCurrency) {
                    costItem.costInBillingCurrency = costItem.costInLocalCurrency * costItem.currencyRate
                    mappingErrorBag.keySet().removeAll(['invoiceTotalMissing','invoiceTotalInvalid'])
                    mappingErrorBag.invoiceTotalCalculated = true
                }
            }
            /*
            taxCode(nullable: true, blank: false) ---v
            taxRate(nullable: true, blank: false) ---v
            taxKey(nullable: true, blank: false) -> to combination of tax type and tax rate
             */
            if(colMap.taxType != null && cols[colMap.taxType] != null) {
                String taxTypeKey = cols[colMap.taxType].toLowerCase()
                int taxRate = 0
                if(cols[colMap.taxRate]) {
                    try {
                        taxRate = Integer.parseInt(cols[colMap.taxRate])
                    }
                    catch (Exception e) {
                        log.info("non-numeric tax rate parsed")
                        mappingErrorBag.invalidTaxType = true
                    }
                }
                if(!taxTypeKey)
                    mappingErrorBag.invalidTaxType = true
                else {
                    CostItem.TAX_TYPES taxKey
                    switch(taxRate) {
                        case 5: taxKey = CostItem.TAX_TYPES.TAXABLE_5
                            break
                        case 7: taxKey = CostItem.TAX_TYPES.TAXABLE_7
                            break
                        case 16: taxKey = CostItem.TAX_TYPES.TAXABLE_16
                            break
                        case 19: taxKey = CostItem.TAX_TYPES.TAXABLE_19
                            break
                        default: RefdataValue taxType = RefdataValue.getByValueAndCategory(taxTypeKey, RDConstants.TAX_TYPE)
                            if(!taxType)
                                taxType = RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.TAX_TYPE, taxTypeKey)
                            //reverse charge must not be displayed here according to Micha, December 3rd, '20!
                            switch(taxType) {
                                case RDStore.TAX_TYPE_NOT_TAXABLE: taxKey = CostItem.TAX_TYPES.TAX_NOT_TAXABLE
                                    break
                                case RDStore.TAX_TYPE_NOT_APPLICABLE: taxKey = CostItem.TAX_TYPES.TAX_NOT_APPLICABLE
                                    break
                                case RDStore.TAX_TYPE_TAXABLE_EXEMPT: taxKey = CostItem.TAX_TYPES.TAX_EXEMPT
                                    break
                                case RDStore.TAX_TYPE_TAX_CONTAINED_19: taxKey = CostItem.TAX_TYPES.TAX_CONTAINED_19
                                    break
                                case RDStore.TAX_TYPE_TAX_CONTAINED_7: taxKey = CostItem.TAX_TYPES.TAX_CONTAINED_7
                                    break
                                default: mappingErrorBag.invalidTaxType = true
                                    break
                            }
                            break
                    }
                    if(taxKey)
                        costItem.taxKey = taxKey
                    else
                        mappingErrorBag.invalidTaxType = true
                }
            }
            //invoiceDate(nullable: true, blank: false) -> to invoice date
            if(colMap.invoiceDate != null) {
                Date invoiceDate = DateUtils.parseDateGeneric(cols[colMap.invoiceDate])
                if(invoiceDate)
                    costItem.invoiceDate = invoiceDate
            }
            //financialYear(nullable: true, blank: false) -> to financial year
            if(colMap.financialYear != null) {
                if(cols[colMap.financialYear])  {
                    try {
                        Year financialYear = Year.parse(cols[colMap.financialYear])
                        costItem.financialYear = financialYear
                    }
                    catch(Exception e) {
                        mappingErrorBag.invalidYearFormat = true
                    }
                }
            }
            //costItemStatus(nullable: true, blank: false) -> to status
            if(colMap.status != null) {
                String statusKey = cols[colMap.status]
                RefdataValue status
                if(statusKey) {
                    status = RefdataValue.getByValueAndCategory(statusKey, RDConstants.COST_ITEM_STATUS)
                    if(!status)
                        status = RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.COST_ITEM_STATUS, statusKey)
                    if(!status) {
                        mappingErrorBag.noValidStatus = statusKey
                        status = RDStore.GENERIC_NULL_VALUE
                    }
                }
                else status = RDStore.GENERIC_NULL_VALUE
                costItem.costItemStatus = status
            }
            else costItem.costItemStatus = RDStore.GENERIC_NULL_VALUE
            //costItemElement(nullable: true, blank: false) -> to element
            if(colMap.element != null) {
                String elementKey = cols[colMap.element]
                if(elementKey) {
                    RefdataValue element = RefdataValue.getByValueAndCategory(elementKey, RDConstants.COST_ITEM_ELEMENT)
                    if(!element)
                        element = RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.COST_ITEM_ELEMENT,elementKey)
                    if(!element)
                        mappingErrorBag.noValidElement = elementKey
                    costItem.costItemElement = element
                    if(cols[colMap.costItemSign] == null || cols[colMap.costItemSign] == "") {
                        costItem.costItemElementConfiguration = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(element, contextOrg).elementSign
                    }
                }
            }
            //costItemElementConfiguration(nullable: true, blank: false) -> to cost item sign
            if(colMap.costItemSign != null && cols[colMap.costItemSign] != null) {
                String elementSign = cols[colMap.costItemSign]
                if(elementSign) {
                    RefdataValue ciec = RefdataValue.getByValueAndCategory(elementSign, RDConstants.COST_CONFIGURATION)
                    if(!ciec)
                        ciec = RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.COST_CONFIGURATION, elementSign)
                    if(!ciec)
                        mappingErrorBag.noValidSign = elementSign
                    costItem.costItemElementConfiguration = ciec
                }
            }
            //reference(nullable: true, blank: false) -> to reference/codes
            if(colMap.reference != null)
                costItem.reference = cols[colMap.reference]
            //budgetCode -> to budget code
            if(colMap.budgetCode != null) {
                if(cols[colMap.budgetCode]?.trim())
                    budgetCodes[r] = cols[colMap.budgetCode]?.trim()
            }
            //startDate(nullable: true, blank: false) -> to date from
            if(colMap.dateFrom != null) {
                Date startDate = DateUtils.parseDateGeneric(cols[colMap.dateFrom])
                if(startDate)
                    costItem.startDate = startDate
            }
            //endDate(nullable: true, blank: false) -> to date to
            if(colMap.dateTo != null) {
                Date endDate = DateUtils.parseDateGeneric(cols[colMap.dateTo])
                if(endDate)
                    costItem.endDate = endDate
            }
            //isVisibleForSubscriber(nullable: true, blank: false) -> in second configuration step, see ticket #1204
            //costItem.save() MUST NOT be executed here, ONLY AFTER postprocessing!
            candidates.put(costItem,mappingErrorBag)
        }
        result.candidates = candidates
        result.budgetCodes = budgetCodes
        result
    }

    /**
     * Takes the submitted form params which contain the cost item data to import and persists the new records
     * @param params the cost item data checked by the user and submitted by post processing form
     * @return result map OK on success or ERROR on fail
     */
    Map<String,Object> importCostItems(GrailsParameterMap params) {
        Map<String,Object> result = [errors:[]]
        Org contextOrg = contextService.getOrg()
        SimpleDateFormat sdf = DateUtils.getSDF_yyyyMMddTHHmmssZ()
        def candidates = JSON.parse(params.candidates)
        def bcJSON = JSON.parse(params.budgetCodes)
        Map<Integer, String> budgetCodes = [:]
        bcJSON.each { k,v ->
            if(v)
                budgetCodes[Integer.parseInt(k)] = v
        }
        candidates.eachWithIndex { ci, Integer c ->
            if(params["take${c}"]) {
                //a single cast did not work because of financialYear type mismatch
                CostItem costItem = new CostItem(owner: contextOrg)
                costItem.sub = Subscription.get(ci.sub.id) ?: null
                costItem.subPkg = SubscriptionPackage.get(ci.subPkg?.id) ?: null
                costItem.issueEntitlement = IssueEntitlement.get(ci.issueEntitlement?.id) ?: null
                costItem.order = Order.get(ci.order?.id) ?: null
                costItem.invoice = Invoice.get(ci.invoice?.id) ?: null
                costItem.billingCurrency = RefdataValue.get(ci.billingCurrency?.id) ?: null
                costItem.costItemElement = RefdataValue.get(ci.costItemElement?.id) ?: null
                costItem.costItemElementConfiguration = RefdataValue.get(ci.costItemElementConfiguration?.id) ?: null
                costItem.taxKey = ci.taxKey && CostItem.TAX_TYPES.valueOf(ci.taxKey.name) ?: null
                costItem.costInBillingCurrency = ci.costInBillingCurrency ?: 0.0
                costItem.costInLocalCurrency = ci.costInLocalCurrency ?: 0.0
                costItem.currencyRate = ci.currencyRate ?: 0.0
                costItem.invoiceDate = ci.invoiceDate ? sdf.parse(ci.invoiceDate) : null
                costItem.financialYear = ci.financialYear ? Year.parse(ci.financialYear.value.toString()) : null
                costItem.costTitle = ci.costTitle ?: null
                costItem.costDescription = ci.costDescription ?: null
                costItem.costItemStatus = ci.costItemStatus ? RefdataValue.get(ci.costItemStatus.id) : null
                costItem.reference = ci.reference ?: null
                costItem.datePaid = ci.datePaid ? sdf.parse(ci.datePaid) : null
                costItem.startDate = ci.startDate ? sdf.parse(ci.startDate) : null
                costItem.endDate = ci.endDate ? sdf.parse(ci.endDate) : null
                costItem.isVisibleForSubscriber = params["visibleForSubscriber${c}"] == 'true' ?: false
                if(!costItem.save()) {
                    result.errors << costItem.errors
                }
                else {
                    if(budgetCodes) {
                        String[] budgetCodeKeys
                        Pattern p = Pattern.compile('.*[,;].*')
                        if(budgetCodes.containsKey(c)) {
                            String code = budgetCodes.get(c)
                            Matcher m = p.matcher(code)
                            if(m.find())
                                budgetCodeKeys = code.split('[,;]')
                            else
                                budgetCodeKeys = [code]
                            budgetCodeKeys.each { String k ->
                                String bck = k.trim()
                                BudgetCode bc = BudgetCode.findByOwnerAndValue(contextOrg,bck)
                                if(!bc) {
                                    bc = new BudgetCode(owner: contextOrg, value: bck)
                                }
                                if(!bc.save()) {
                                    result.errors << bc.errors
                                }
                                else {
                                    CostItemGroup cig = new CostItemGroup(costItem: costItem, budgetCode: bc)
                                    if(!cig.save()) {
                                        result.errors << cig.errors
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(result.errors)
            [result:result,status:STATUS_ERROR]
        else [result:result,status:STATUS_OK]
    }

    /**
     * Orders the currencies available in the database
     * @return the ordered list of currencies
     */
    List<Map<String,Object>> orderedCurrency() {
        Set<RefdataValue> allCurrencies = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.CURRENCY)

        List<Map<String,Object>> result = [[id:0,text:messageSource.getMessage('financials.currency.none',null, LocaleUtils.getCurrentLocale())]]
        result.addAll(allCurrencies.collect { rdv ->
            [id: rdv.id, text: rdv.getI10n('value')]
        })

        result
    }

    //------------------------------------------- cost element section -------------------------------------------

    /**
     * Creates a new {@link CostItemElementConfiguration} with the given parameter map
     * @param params the parameter map with the attributes of the new configuration
     */
    void processConfigurationCreation(GrailsParameterMap params) {
        CostItemElementConfiguration ciec = new CostItemElementConfiguration()
        ciec.costItemElement = RefdataValue.get(params.cie)
        ciec.elementSign = RefdataValue.get(params.sign)
        ciec.useForCostPerUse = params.useForCostPerUse == 'on'
        ciec.forOrganisation = (Org) contextService.getOrg()
        if(!ciec.validate()) {
            ciec.errors.allErrors.collect {
                log.error("Error occurred: ${it.properties.field} has erroneous value ${it.properties.rejectedValue}, error code: ${it.properties.code}")
            }
        }
        else ciec.save()
    }

    /**
     * Deletes the given {@link CostItemElementConfiguration}
     * @param ciec the cost item element configuration to delete
     */
    void deleteCostConfiguration(CostItemElementConfiguration ciec) {
        ciec.delete()
    }

    //---------------------------------------------- poison cupboard ---------------------------------------------

    /**
     * Currently unused; should backwards-correct cost items to calculate the costs in local currency
     * @param dryRun do the execution or not?
     * @return a list of concerned cost items with their corrected value
     */
    Map correctCostsInLocalCurrency(boolean dryRun) {
        Map<CostItem,Double> result = [:]
        List res = CostItem.executeQuery('select ci, (ci.costInBillingCurrency * ci.currencyRate) as costInLocalCurrency from CostItem ci where ci.costInLocalCurrency = 0 and ci.costInBillingCurrency != 0')
        res.each { row ->
            result.put((CostItem) row[0],(Double) row[1])
        }
        if(!dryRun) {
            //CostItem.executeUpdate('update CostItem ci set ci.costInLocalCurrency = ci.costInBillingCurrency * ci.currencyRate where ci.costInBillingCurrency = 0 and ci.costInLocalCurrency != 0')
        }
        result
    }

}

