package de.laser

import com.k_int.kbplus.CostItem
import com.k_int.kbplus.RefdataValue
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional

@Transactional
class CostItemUpdateService {

    @Secured(['ROLE_YODA'])
    void updateTaxRates() {
        CostItem.executeUpdate('update CostItem ci set ci.taxKey = :key where ci.taxRate = 7 and ci.taxKey = null',[key:CostItem.TAX_TYPES.TAXABLE_7])
        CostItem.executeUpdate('update CostItem ci set ci.taxKey = :key where ci.taxRate = 19 and ci.taxKey = null',[key:CostItem.TAX_TYPES.TAXABLE_19])
        CostItem.executeUpdate('update CostItem ci set ci.taxKey = :key where ci.taxCode = :value and ci.taxKey = null',[key:CostItem.TAX_TYPES.TAX_EXEMPT,value: RefdataValue.getByValueAndCategory('taxable tax-exempt','TaxType')])
        CostItem.executeUpdate('update CostItem ci set ci.taxKey = :key where ci.taxCode = :value and ci.taxKey = null',[key:CostItem.TAX_TYPES.TAX_NOT_TAXABLE,value: RefdataValue.getByValueAndCategory('not taxable','TaxType')])
        CostItem.executeUpdate('update CostItem ci set ci.taxKey = :key where ci.taxCode = :value and ci.taxKey = null',[key:CostItem.TAX_TYPES.TAX_NOT_APPLICABLE,value: RefdataValue.getByValueAndCategory('not applicable','TaxType')])
    }
}
