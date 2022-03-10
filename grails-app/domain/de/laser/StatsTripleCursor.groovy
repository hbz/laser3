package de.laser


import de.laser.helper.RDConstants
import de.laser.annotations.RefdataAnnotation

/**
 * This domain is part of the statistics component implemented for the Nationaler Statistikserver data.
 * It serves to mark availability for titles; from when to when are reports available and how many of them
 * Reports referred here are objects of the {@link Fact} class!
 */
class StatsTripleCursor {

    String titleId
    IdentifierNamespace identifierType
    String supplierId
    String customerId
    String jerror
    Date availFrom
    Date availTo
    Integer numFacts

    @RefdataAnnotation(cat = RDConstants.FACT_TYPE)
    RefdataValue factType

    static mapping = {
        titleId     column:'stats_title_id',    index:'stats_cursor_idx'
        supplierId  column:'stats_supplier_id', index:'stats_cursor_idx'
        customerId  column:'stats_customer_id', index:'stats_cursor_idx'
    }

    static constraints = {
        titleId(blank:false,maxSize:32)
        supplierId(blank:false,maxSize:32)
        customerId(blank:false,maxSize:32)
        availTo(nullable:true)
        numFacts(maxSize:11)
        jerror(nullable:true, blank:true)
    }
}