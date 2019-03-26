package de.laser.domain

import com.k_int.kbplus.RefdataValue
import de.laser.helper.RefdataAnnotation

class StatsTripleCursor {

    String titleId
    String supplierId
    String customerId
    String jerror
    Date availFrom
    Date availTo
    Integer numFacts

    @RefdataAnnotation(cat = 'FactType')
    RefdataValue factType

    static mapping = {
        titleId column:'stats_title_id', index:'stats_cursor_idx'
        supplierId column:'stats_supplier_id', index:'stats_cursor_idx'
        customerId column:'stats_customer_id', index:'stats_cursor_idx'
    }

    static constraints = {
        titleId(nullable:false, blank:false,maxSize:32)
        supplierId(nullable:false, blank:false,maxSize:32)
        customerId(nullable:false, blank:false,maxSize:32)
        availFrom(nullable:false, blank:false)
        availTo(nullable:true, blank: false)
        numFacts(nullable:false, blank:false,maxSize:11)
        jerror(nullable:true, blank:true)
    }
}