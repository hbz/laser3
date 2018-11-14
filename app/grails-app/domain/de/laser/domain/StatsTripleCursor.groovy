package de.laser.domain

import com.k_int.kbplus.RefdataValue

class StatsTripleCursor {

    String titleId
    String supplierId
    String customerId
    String haveUpTo
    String jerror
    RefdataValue factType
    Integer numFacts


    static mapping = {
        titleId column:'stats_title_id', index:'stats_cursor_idx'
        supplierId column:'stats_supplier_id', index:'stats_cursor_idx'
        customerId column:'stats_customer_id', index:'stats_cursor_idx'
    }

    static constraints = {
        titleId(nullable:false, blank:false,maxSize:32)
        supplierId(nullable:false, blank:false,maxSize:32)
        customerId(nullable:false, blank:false,maxSize:32)
        haveUpTo(nullable:false, blank:false,maxSize:32)
        numFacts(nullable:false, blank:false,maxSize:11)
        jerror(nullable:true, blank:true)
    }
}
