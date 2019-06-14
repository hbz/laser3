package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.domain.AbstractBaseDomain
import de.laser.helper.RefdataAnnotation
import de.laser.interfaces.DeleteFlag
import de.laser.interfaces.TemplateSupport
import java.time.Year

import javax.persistence.Transient

class CostItem
        extends AbstractBaseDomain
        implements DeleteFlag, TemplateSupport {

    static enum TAX_TYPES {
        TAXABLE_7          (RefdataValue.getByValueAndCategory('taxable','TaxType'),7),
        TAXABLE_19         (RefdataValue.getByValueAndCategory('taxable','TaxType'),19),
        TAX_EXEMPT         (RefdataValue.getByValueAndCategory('taxable tax-exempt','TaxType'),0),
        TAX_NOT_TAXABLE    (RefdataValue.getByValueAndCategory('not taxable','TaxType'),0),
        TAX_NOT_APPLICABLE (RefdataValue.getByValueAndCategory('not applicable','TaxType'),0)

        TAX_TYPES(taxType, taxRate) {
            this.taxType = taxType
            this.taxRate = taxRate
        }

        public RefdataValue taxType
        public int taxRate
    }

    Org owner
    Subscription sub
    SubscriptionPackage subPkg
    IssueEntitlement issueEntitlement
    SurveyOrg surveyOrg
    Order order
    Invoice invoice

    Boolean isVisibleForSubscriber

    @RefdataAnnotation(cat = 'CostItem.Type')
    RefdataValue type

    @RefdataAnnotation(cat = 'CostItemStatus')
    RefdataValue costItemStatus

    @RefdataAnnotation(cat = 'CostItemCategory')
    RefdataValue costItemCategory

    @RefdataAnnotation(cat = 'Currency')
    RefdataValue billingCurrency    // GDP,USD,etc

    @RefdataAnnotation(cat = 'CostItemElement')
    RefdataValue costItemElement

    @RefdataAnnotation(cat = 'Cost configuration')
    RefdataValue costItemElementConfiguration

    @RefdataAnnotation(cat = 'TaxType')
    RefdataValue taxCode          //to be deleted, will be replaced by TAX_TYPES

    Boolean includeInSubscription //include in sub details page

    BigDecimal costInBillingCurrency   //The actual amount - new cost ex tax
    BigDecimal costInLocalCurrency     //local amount entered
    BigDecimal currencyRate

    //legacy, to be replaced by ...
    Integer taxRate
    //... this construct:
    TAX_TYPES taxKey

    Boolean finalCostRounding

    @Transient
    BigDecimal costInLocalCurrencyAfterTax
    @Transient
    BigDecimal costInBillingCurrencyAfterTax

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
        subPkg          column: 'ci_subPkg_fk'
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
        includeInSubscription column: 'ci_include_in_subscr'
        costItemCategory    column: 'ci_cat_rv_fk'
        costItemElement     column: 'ci_element_rv_fk'
        costItemElementConfiguration column: 'ci_element_configuration_rv_fk'
        endDate         column: 'ci_end_date'
        startDate       column: 'ci_start_date'
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
        billingCurrency(nullable: true, blank: false)
        costDescription(nullable: true, blank: false)
        costTitle(nullable: true, blank: false)
        costInBillingCurrency(nullable: true, blank: false)
        datePaid(nullable: true, blank: false)
        costInLocalCurrency(nullable: true, blank: false)
        currencyRate(nullable: true, blank: false, scale: 9)
        finalCostRounding(nullable: true, blank: false)
        taxCode(nullable: true, blank: false)
        taxRate(nullable: true, blank: false)
        taxKey(nullable: true, blank: false)
        invoiceDate(nullable: true, blank: false)
        financialYear(nullable: true, blank: false)
        isVisibleForSubscriber(nullable: true, blank: false)
        includeInSubscription(nullable: true, blank: false)
        costItemCategory(nullable: true, blank: false)
        costItemStatus(nullable: true, blank: false)
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

    @Deprecated
    @Override
    boolean isTemplate() {
        false
    }

    @Deprecated
    @Override
    boolean hasTemplate() {
        false
    }

    @Override
    String getCalculatedType() {
        def result = TemplateSupport.CALCULATED_TYPE_UNKOWN

        if (isTemplate()) {
            result = TemplateSupport.CALCULATED_TYPE_TEMPLATE
        }

        result
    }

    def getBudgetcodes() {
        def result = BudgetCode.executeQuery("select bc from BudgetCode as bc, CostItemGroup as cig, CostItem as ci where cig.costItem = ci and cig.budgetCode = bc and ci = ?", [this])
        return result
    }

    def getCostInLocalCurrencyAfterTax() {
        Double result = ( costInLocalCurrency ?: 0.0 ) * ( taxKey ? ((taxKey.taxRate/100) + 1) : 1.0 )

        finalCostRounding ? result.round(0) : result.round(2)
    }

    def getCostInBillingCurrencyAfterTax() {
        Double result = ( costInBillingCurrency ?: 0.0 ) * ( taxKey ? ((taxKey.taxRate/100) + 1) : 1.0 )

        finalCostRounding ? result.round(0) : result.round(2)
    }

    @Transient
    static def orderedCurrency() {
        //def all_currencies = RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=?', 'Currency')
        // restrict only to new refdata values
        // created in bootstrap.groovy with e.g. [key:'EUR']
        // TODO: migrate old values to new ones
        def all_currencies = RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=? and LENGTH(rdv.value) = 3', 'Currency')
        def staticOrder    = grails.util.Holders.config?.financials?.currency?.split("[|]")

        if (staticOrder) {
            def currencyPriorityList = staticOrder.collect {RefdataCategory.lookupOrCreate('Currency', it)}
            if (currencyPriorityList) {
                all_currencies.removeAll(currencyPriorityList)
                all_currencies.addAll(0, currencyPriorityList)
            }
        }

        def result = all_currencies.collect { rdv ->
            [id: rdv.id, text: rdv.getI10n('value')]
        }

        result
    }

    Date getDerivedStartDate() {
        startDate ? startDate : sub?.startDate
    }

    Date getDerivedEndDate() {
        endDate ? endDate : sub?.endDate
    }

}
