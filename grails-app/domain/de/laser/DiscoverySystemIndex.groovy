package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants

class DiscoverySystemIndex implements Comparable {

    @RefdataInfo(cat = RDConstants.DISCOVERY_SYSTEM_INDEX)
    RefdataValue index

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            org: Org,
            index: RefdataValue
    ]

    static mapping = {
        id               column: 'dsi_id'
        version          column: 'dsi_version'
        org              column: 'dsi_org_fk', index: 'dsi_org_idx'
        index            column: 'dsi_index_rv_fk', index: 'dsi_index_idx'
        dateCreated      column: 'dsi_date_created'
        lastUpdated      column: 'dsi_last_updated'
    }

    @Override
    int compareTo(Object o) {
        DiscoverySystemIndex b = (DiscoverySystemIndex) o
        return index.getI10n('value') <=> b.index.getI10n('value')
    }
}
