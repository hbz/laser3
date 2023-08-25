package de.laser.finance

import de.laser.IssueEntitlement
import de.laser.IssueEntitlementGroup
import de.laser.survey.SurveyOrg
import de.laser.Org
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.base.AbstractBase
import de.laser.storage.RDConstants
import de.laser.annotations.RefdataInfo
import de.laser.interfaces.CalculatedType
import de.laser.interfaces.DeleteFlag

import javax.persistence.Transient
import java.time.Year

/**
 * Represents a cost item which belongs to an {@link Org} and may be linked above that to a {@link Subscription} or to a {@link SurveyOrg}.
 * A cost item cannot be linked simultaneously to a {@link Subscription} and to a {@link SurveyOrg}.
 */
class CostItem extends AbstractBase
        implements DeleteFlag, CalculatedType  {

    /**
     * The tax types which are currently allowed in the system;
     * an association between the {@link RefdataValue} of category {@link RDConstants#TAX_TYPE} and the actual tax rate (7 or 19 %, their reduced variants for 2020 5 resp. 16 % or 0 %)
     * and a display flag
     */
    static enum TAX_TYPES {
        //are correct! Cost items flagged with these tax keys already contain the 7 resp. 19 per cent tax
        TAXABLE_5          (RefdataValue.getByValueAndCategory('taxable', RDConstants.TAX_TYPE),5,true),
        TAXABLE_7          (RefdataValue.getByValueAndCategory('taxable', RDConstants.TAX_TYPE),7,true),
        TAXABLE_16         (RefdataValue.getByValueAndCategory('taxable', RDConstants.TAX_TYPE),16,true),
        TAXABLE_19         (RefdataValue.getByValueAndCategory('taxable', RDConstants.TAX_TYPE),19,true),
        TAX_CONTAINED_7    (RefdataValue.getByValueAndCategory('tax contained 7', RDConstants.TAX_TYPE),0,false),
        TAX_CONTAINED_19   (RefdataValue.getByValueAndCategory('tax contained 19', RDConstants.TAX_TYPE),0,false),
        TAX_EXEMPT         (RefdataValue.getByValueAndCategory('taxable tax-exempt', RDConstants.TAX_TYPE),0,true),
        TAX_NOT_TAXABLE    (RefdataValue.getByValueAndCategory('not taxable', RDConstants.TAX_TYPE),0,true),
        TAX_NOT_APPLICABLE (RefdataValue.getByValueAndCategory('not applicable', RDConstants.TAX_TYPE),0,true),
        TAX_REVERSE_CHARGE (RefdataValue.getByValueAndCategory('reverse charge', RDConstants.TAX_TYPE),0,false)

        TAX_TYPES(RefdataValue taxType, int taxRate, display) {
            this.taxType = taxType
            this.taxRate = taxRate
            this.display = display
        }

        public RefdataValue taxType
        public int taxRate
        public boolean display
    }

    Org owner
    Subscription sub // NOT set if surveyOrg (exclusive)
    SubscriptionPackage subPkg // only set if sub
    IssueEntitlement issueEntitlement // only set if sub
    SurveyOrg surveyOrg // NOT set if sub (exclusive)
    Order order
    Invoice invoice
    IssueEntitlementGroup issueEntitlementGroup // only set if sub

    Boolean isVisibleForSubscriber = false

    @RefdataInfo(cat = RDConstants.COST_ITEM_TYPE)
    RefdataValue type

    @RefdataInfo(cat = RDConstants.COST_ITEM_STATUS, i18n = 'default.status.label')
    RefdataValue costItemStatus

    @RefdataInfo(cat = RDConstants.COST_ITEM_CATEGORY)
    RefdataValue costItemCategory

    @RefdataInfo(cat = RDConstants.COST_ITEM_ELEMENT, i18n = 'financials.costItemElement')
    RefdataValue costItemElement

    @RefdataInfo(cat = RDConstants.COST_CONFIGURATION, i18n = 'financials.costItemConfiguration')
    RefdataValue costItemElementConfiguration

    @RefdataInfo(cat = RDConstants.CURRENCY, i18n = 'default.currency.label')
    RefdataValue billingCurrency

    Double costInBillingCurrency   //The actual amount - new cost ex tax
    Double costInLocalCurrency     //local amount entered
    Double currencyRate

    //... this construct:
    TAX_TYPES taxKey

    Boolean billingSumRounding = false
    Boolean finalCostRounding = false

    @Transient
    Double costInLocalCurrencyAfterTax
    @Transient
    Double costInBillingCurrencyAfterTax

    Date invoiceDate
    Year financialYear

    String costTitle
    String costDescription
    String reference
    Date datePaid
    Date startDate
    Date endDate
    CostItem copyBase              //the base cost item from which this item has been copied

    //Edits...
    Date lastUpdated
    Date dateCreated

    static final TAX_RATES = [ 0, 5, 7, 16, 19 ]

    static transients = ['deleted', 'derivedStartDate', 'derivedEndDate'] // mark read-only accessor methods

    static mapping = {
        id              column: 'ci_id'
        globalUID       column: 'ci_guid'
        type            column: 'ci_type_rv_fk'
        version         column: 'ci_version'
        sub             column: 'ci_sub_fk',        index: 'ci_sub_idx'
        owner           column: 'ci_owner',         index: 'ci_owner_idx'
        subPkg          column: 'ci_sub_pkg_fk'
        issueEntitlement    column: 'ci_e_fk',      index: 'ci_e_idx' //the index is needed for deletion checks of issue entitlements where each foreign key is being checked
        surveyOrg       column: 'ci_surorg_fk'
        order           column: 'ci_ord_fk'
        invoice         column: 'ci_inv_fk'
        issueEntitlementGroup column: 'ci_ie_group_fk'
        costItemStatus  column: 'ci_status_rv_fk'
        billingCurrency column: 'ci_billing_currency_rv_fk'
        costDescription column: 'ci_cost_description', type:'text'
        costTitle       column: 'ci_cost_title'
        costInBillingCurrency           column: 'ci_cost_in_billing_currency'
        datePaid            column: 'ci_date_paid'
        costInLocalCurrency             column: 'ci_cost_in_local_currency'
        currencyRate    column: 'ci_currency_rate'
        finalCostRounding               column:'ci_final_cost_rounding'
        billingSumRounding              column:'ci_billing_sum_rounding'
        taxKey          column: 'ci_tax_enum'
        invoiceDate                     column: 'ci_invoice_date'
        financialYear                   column: 'ci_financial_year'
        isVisibleForSubscriber          column: 'ci_is_viewable'
        costItemCategory    column: 'ci_cat_rv_fk'
        costItemElement     column: 'ci_element_rv_fk'
        costItemElementConfiguration column: 'ci_element_configuration_rv_fk'
        endDate         column: 'ci_end_date',    index:'ci_dates_idx'
        startDate       column: 'ci_start_date',  index:'ci_dates_idx'
        copyBase        column: 'ci_copy_base'
        reference       column: 'ci_reference'
        dateCreated     column: 'ci_date_created'
        lastUpdated     column: 'ci_last_updated'
        autoTimestamp true
    }

    static constraints = {
        globalUID(nullable: true, blank: false, unique: true, maxSize: 255)
        type    (nullable: true)
        sub     (nullable: true)
        issueEntitlementGroup   (nullable: true)
        subPkg  (nullable: true, validator: { val, obj ->
            if (obj.subPkg) {
                if (obj.subPkg.subscription.id != obj.sub.id) return ['subscriptionPackageMismatch']
            }
        })
        issueEntitlement(nullable: true, validator: { val, obj ->
            if (obj.issueEntitlement) {
                if (!obj.subPkg || (obj.issueEntitlement.tipp.pkg.gokbId != obj.subPkg.pkg.gokbId)) return ['issueEntitlementNotInPackage']
            }
        })
        surveyOrg       (nullable: true)
        order           (nullable: true)
        invoice         (nullable: true)
        costDescription (nullable: true, blank: false)
        costTitle       (nullable: true, blank: false)
        costInBillingCurrency(nullable: true)
        datePaid        (nullable: true)
        costInLocalCurrency (nullable: true)
        currencyRate(nullable: true)
        taxKey      (nullable: true)
        invoiceDate (nullable: true)
        financialYear(nullable: true)
        costItemCategory    (nullable: true)
        costItemElement     (nullable: true)
        costItemElementConfiguration    (nullable: true)
        reference   (nullable: true, blank: false)
        startDate   (nullable: true)
        endDate     (nullable: true)
        copyBase    (nullable: true)
        //lastUpdatedBy(nullable: true)
        //createdBy(nullable: true)
    }

    @Override
    boolean isDeleted() {
        return false
    }

    @Override
    def beforeInsert() {
        super.beforeInsertHandler()
    }
    @Override
    def beforeUpdate() {
        super.beforeUpdateHandler()
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }

    /**
     * Determines the type of the cost item - is it a shared consortial item or local resp. hidden item?
     * @return a {@link CalculatedType} constant (TYPE_CONSORTIAL or TYPE_LOCAL)
     */
    @Override
    // currently only used for API
    String _getCalculatedType() {
        if (isVisibleForSubscriber) {
            return CalculatedType.TYPE_CONSORTIAL // boolean flag = true -> shared consortia costs
        }

        CalculatedType.TYPE_LOCAL // boolean flag = false -> local costs or hidden consortia costs
    }

    /**
     * Gets the list of {@link BudgetCode}s to which this cost item is belonging to
     * @return a {@link List} of {@link BudgetCode}s
     */
    List<BudgetCode> getBudgetcodes() {
        BudgetCode.executeQuery(
                "select bc from BudgetCode as bc, CostItemGroup as cig, CostItem as ci where cig.costItem = ci and cig.budgetCode = bc and ci = :costitem",
                [costitem: this]
        )
    }

    /**
     * Executes the tax calculation of the local sum and returns the final local cost
     * @return the local cost after taxation
     */
    //needs to be def because of GORM magic, looking for a database mapping ...
    def getCostInLocalCurrencyAfterTax() {
        Double result = ( costInLocalCurrency ?: 0.0 ) * ( taxKey ? ((taxKey.taxRate/100) + 1) : 1.0 )

        finalCostRounding ? result.round(0) : result.round(2)
    }

    /**
     * Executes the tax calculation of the billing sum and returns the final billing cost
     * @return the billing cost after taxation
     */
    //needs to be def because of GORM magic, looking for a database mapping ...
    def getCostInBillingCurrencyAfterTax() {
        Double result = ( costInBillingCurrency ?: 0.0 ) * ( taxKey ? ((taxKey.taxRate/100) + 1) : 1.0 )

        finalCostRounding ? result.round(0) : result.round(2)
    }

    /**
     * Determines the start date of the cost period covered by this cost item; as fallback, the {@link Subscription}'s start date is being taken if it is set
     * @return the start date of the cost item; if not specified, the owner {@link Subscription}'s start date; null if that is missing as well
     */
    Date getDerivedStartDate() {
        startDate ? startDate : sub?.startDate
    }

    /**
     * Determines the end date of the cost period covered by this cost item; as fallback, the {@link Subscription}'s end date is being taken if it is set
     * @return the end date of the cost item; if not specified, the owner {@link Subscription}'s end date; null if that is missing as well
     */
    Date getDerivedEndDate() {
        endDate ? endDate : sub?.endDate
    }

}
