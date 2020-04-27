package com.k_int.kbplus

import de.laser.domain.AbstractBaseDomain
import de.laser.helper.RDConstants
import de.laser.helper.RefdataAnnotation
import de.laser.interfaces.DeleteFlag
import de.laser.interfaces.CalculatedType
import javax.persistence.Transient
import java.time.Year

class CostItem
        extends AbstractBaseDomain
        implements DeleteFlag, CalculatedType  {

    static enum TAX_TYPES {
        TAXABLE_7          (RefdataValue.getByValueAndCategory('taxable', RDConstants.TAX_TYPE),7,true),
        TAXABLE_19         (RefdataValue.getByValueAndCategory('taxable', RDConstants.TAX_TYPE),19,true),
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

    Boolean isVisibleForSubscriber = false

    @RefdataAnnotation(cat = RDConstants.COST_ITEM_TYPE)
    RefdataValue type

    @RefdataAnnotation(cat = RDConstants.COST_ITEM_STATUS)
    RefdataValue costItemStatus

    @RefdataAnnotation(cat = RDConstants.COST_ITEM_CATEGORY)
    RefdataValue costItemCategory

    @RefdataAnnotation(cat = RDConstants.COST_ITEM_ELEMENT)
    RefdataValue costItemElement

    @RefdataAnnotation(cat = RDConstants.COST_CONFIGURATION)
    RefdataValue costItemElementConfiguration

    @RefdataAnnotation(cat = RDConstants.TAX_TYPE)
    RefdataValue taxCode          //to be deleted, will be replaced by TAX_TYPES

    @RefdataAnnotation(cat = RDConstants.CURRENCY)
    RefdataValue billingCurrency

    //Boolean includeInSubscription include in sub details page - is in fact always true

    Double costInBillingCurrency   //The actual amount - new cost ex tax
    Double costInLocalCurrency     //local amount entered
    Double currencyRate

    //legacy, to be replaced by ...
    Integer taxRate
    //... this construct:
    TAX_TYPES taxKey

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
    //User lastUpdatedBy
    Date dateCreated
    //User createdBy

    //@Transient
    //def budgetcodes //Binds getBudgetcodes

    @Transient
    def springSecurityService

    static final TAX_RATES = [ 0, 7, 19 ]

    static mapping = {
        id              column: 'ci_id'
        globalUID       column: 'ci_guid'
        type            column: 'ci_type_rv_fk'
        version         column: 'ci_version'
        sub             column: 'ci_sub_fk',        index: 'ci_sub_idx'
        owner           column: 'ci_owner',         index: 'ci_owner_idx'
        subPkg          column: 'ci_sub_pkg_fk'
        issueEntitlement    column: 'ci_e_fk'
        surveyOrg       column: 'ci_surorg_fk'
        order           column: 'ci_ord_fk'
        invoice         column: 'ci_inv_fk'
        costItemStatus  column: 'ci_status_rv_fk'
        billingCurrency column: 'ci_billing_currency_rv_fk'
        costDescription column: 'ci_cost_description', type:'text'
        costTitle       column: 'ci_cost_title'
        costInBillingCurrency           column: 'ci_cost_in_billing_currency'
        datePaid            column: 'ci_date_paid'
        costInLocalCurrency             column: 'ci_cost_in_local_currency'
        currencyRate    column: 'ci_currency_rate'
        finalCostRounding               column:'ci_final_cost_rounding'
        taxCode         column: 'ci_tax_code'
        taxRate                         column: 'ci_tax_rate'
        taxKey          column: 'ci_tax_enum'
        invoiceDate                     column: 'ci_invoice_date'
        financialYear                   column: 'ci_financial_year'
        isVisibleForSubscriber          column: 'ci_is_viewable'
        //includeInSubscription column: 'ci_include_in_subscr'
        costItemCategory    column: 'ci_cat_rv_fk'
        costItemElement     column: 'ci_element_rv_fk'
        costItemElementConfiguration column: 'ci_element_configuration_rv_fk'
        endDate         column: 'ci_end_date',    index:'ci_dates_idx'
        startDate       column: 'ci_start_date',  index:'ci_dates_idx'
        copyBase        column: 'ci_copy_base'
        reference       column: 'ci_reference'
        autoTimestamp true
    }

    static constraints = {
        globalUID(nullable: true, blank: false, unique: true, maxSize: 255)
        owner(nullable: false, blank: false)
        type(nullable: true, blank: false)
        sub(nullable: true, blank: false)
        subPkg(nullable: true, blank: false, validator: { val, obj ->
            if (obj.subPkg) {
                if (obj.subPkg.subscription.id != obj.sub.id) return ['subscriptionPackageMismatch']
            }
        })
        issueEntitlement(nullable: true, blank: false, validator: { val, obj ->
            if (obj.issueEntitlement) {
                if (!obj.subPkg || (obj.issueEntitlement.tipp.pkg.gokbId != obj.subPkg.pkg.gokbId)) return ['issueEntitlementNotInPackage']
            }
        })
        surveyOrg       (nullable: true, blank: false)
        order(nullable: true, blank: false)
        invoice(nullable: true, blank: false)
        billingCurrency(nullable: false, blank: false)
        costDescription(nullable: true, blank: false)
        costTitle(nullable: true, blank: false)
        costInBillingCurrency(nullable: true, blank: false)
        datePaid(nullable: true, blank: false)
        costInLocalCurrency(nullable: true, blank: false)
        currencyRate(nullable: true, blank: false)
        finalCostRounding       (nullable: false, blank: false)
        taxCode(nullable: true, blank: false)
        taxRate(nullable: true, blank: false)
        taxKey(nullable: true, blank: false)
        invoiceDate(nullable: true, blank: false)
        financialYear(nullable: true, blank: false)
        isVisibleForSubscriber  (nullable: false, blank: false)
        //includeInSubscription   (nullable: false, blank: false)
        costItemCategory(nullable: true, blank: false)
        costItemStatus(nullable: false, blank: false)
        costItemElement(nullable: true, blank: false)
        costItemElementConfiguration(nullable: true, blank: false)
        reference(nullable: true, blank: false)
        startDate(nullable: true, blank: false)
        endDate(nullable: true, blank: false)
        copyBase(nullable: true)
        //lastUpdatedBy(nullable: true)
        //createdBy(nullable: true)
    }

    @Override
    boolean isDeleted() {
        return false
    }

    def beforeInsert() {
        super.beforeInsert()
    }

    def beforeUpdate() {
        super.beforeUpdate()
    }

    @Override
    String getCalculatedType() {
        CalculatedType.CALCULATED_TYPE_UNKOWN
    }

    List<BudgetCode> getBudgetcodes() {
        BudgetCode.executeQuery("select bc from BudgetCode as bc, CostItemGroup as cig, CostItem as ci where cig.costItem = ci and cig.budgetCode = bc and ci = ?", [this])
    }

    def getCostInLocalCurrencyAfterTax() {
        Double result = ( costInLocalCurrency ?: 0.0 ) * ( taxKey ? ((taxKey.taxRate/100) + 1) : 1.0 )

        finalCostRounding ? result.round(0) : result.round(2)
    }

    def getCostInBillingCurrencyAfterTax() {
        Double result = ( costInBillingCurrency ?: 0.0 ) * ( taxKey ? ((taxKey.taxRate/100) + 1) : 1.0 )

        finalCostRounding ? result.round(0) : result.round(2)
    }

    Date getDerivedStartDate() {
        startDate ? startDate : sub?.startDate
    }

    Date getDerivedEndDate() {
        endDate ? endDate : sub?.endDate
    }

}
