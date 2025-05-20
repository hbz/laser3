package de.laser.wekb


import de.laser.RefdataValue
import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants

/**
 * Represents a connection between two {@link Provider}s. The types of combo connections see {@link RDConstants#PROVIDER_LINK_TYPE}
 */
class ProviderLink {

    @RefdataInfo(cat = RDConstants.PROVIDER_LINK_TYPE)
    RefdataValue type

    Provider from
    Provider to

    Date dateCreated
    Date lastUpdated

    static mapping = {
                cache true
                id column:'pl_id'
           version column:'pl_version'
              type column:'pl_type_rv_fk'
           from column:'pl_from_prov_fk'
             to column:'pl_to_prov_fk'

        dateCreated column: 'pl_date_created'
        lastUpdated column: 'pl_last_updated'
    }

    static constraints = {
        lastUpdated (nullable: true)
    }
}
