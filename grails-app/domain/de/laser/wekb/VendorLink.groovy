package de.laser.wekb

import de.laser.RefdataValue
import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants

/**
 * Represents a connection between two {@link Vendor}s. The types of combo connections see {@link RDConstants#PROVIDER_LINK_TYPE}
 */
class VendorLink {

    @RefdataInfo(cat = RDConstants.PROVIDER_LINK_TYPE)
    RefdataValue type

    Vendor from
    Vendor to

    Date dateCreated
    Date lastUpdated

    static mapping = {
                cache true
                id column:'vl_id'
           version column:'vl_version'
              type column:'vl_type_rv_fk'
           from column:'vl_from_ven_fk'
             to column:'vl_to_ven_fk'

        dateCreated column: 'vl_date_created'
        lastUpdated column: 'vl_last_updated'
    }

    static constraints = {
        lastUpdated (nullable: true)
    }
}
