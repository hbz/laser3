package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants

/**
 * Represents a connection between two {@link Org}s. The types of combo connections see {@link RDConstants#COMBO_TYPE}
 */
class Combo {

    @RefdataInfo(cat = RDConstants.COMBO_STATUS)
    RefdataValue status

    @RefdataInfo(cat = RDConstants.COMBO_TYPE)
    RefdataValue type

    Org fromOrg
    Org toOrg

    Date dateCreated
    Date lastUpdated

    static mapping = {
                cache true
                id column:'combo_id'
           version column:'combo_version'
            status column:'combo_status_rv_fk'
              type column:'combo_type_rv_fk'
           fromOrg column:'combo_from_org_fk',  index: 'combo_from_to_org_idx'
             toOrg column:'combo_to_org_fk',    index: 'combo_from_to_org_idx'

        dateCreated column: 'combo_date_created'
        lastUpdated column: 'combo_last_updated'
    }

    static constraints = {
        status      (nullable:true)
        lastUpdated (nullable: true)
    }
}
