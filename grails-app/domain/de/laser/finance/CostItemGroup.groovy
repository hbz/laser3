package de.laser.finance

import de.laser.Org
import grails.web.servlet.mvc.GrailsParameterMap

import javax.persistence.Transient

/**
 * This M to N domain is being used to represent the different budget codes
 * {@link CostItem} has many {@link BudgetCode}s and budget codes have many cost items
 * It is thus a grouping unit for {@link CostItem}s
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

    /**
     * Retrieves a list of {@link BudgetCode}s for dropdown selection
     * @param params the query params passed for lookup
     * @return a {@link List} of {@link Map}s[id: text] containing role ids and names
     */
    @Transient
    static def refdataFind(GrailsParameterMap params) {
        //used in AjaxJsonController.lookup() by generic access method
        List<Map<String, Object>> result = []
        Org owner = Org.findByShortcode(params.shortcode)

        if (owner) {
            String searchTerm = (params.q ? params.q.toLowerCase() : '' ) + "%"
            List<BudgetCode> qryResults = BudgetCode.findAllByOwnerAndValueIlike(owner, searchTerm)

            qryResults.each { bc ->
                result.add([id:bc.id, text:bc.value])
            }
        }
        result
    }
}
