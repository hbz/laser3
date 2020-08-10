package com.k_int.kbplus

import javax.persistence.Transient

/**
 * This M to N domain is being used to represent the different budget codes
 * Cost item has many budget codes and budget codes have many cost items
 */
class CostItemGroup {

    BudgetCode budgetCode
    CostItem   costItem

    static mapping = {
                id column:'cig_id'
           version column:'cig_version'
        budgetCode column:'cig_budget_code_fk'
          costItem column:'cig_cost_item_fk'
    }

    static constraints = {
      budgetCode  nullable: true
      costItem    nullable: true
    }

    @Transient
    static def refdataFind(params) {
        def result     = []
        def qryResults = []
        def searchTerm = (params.q ? params.q.toLowerCase() : '' ) + "%"
        Org orgOwner   = Org.findByShortcode(params.shortcode)

        if (! searchTerm) {
            qryResults = BudgetCode.findAllByOwner(orgOwner)
        } else {
            qryResults = BudgetCode.findAllByOwnerAndValueIlike(orgOwner, searchTerm)
        }

        qryResults.each { bc ->
            result.add([id:bc.id, text:bc.value])
        }
        result
    }
}
