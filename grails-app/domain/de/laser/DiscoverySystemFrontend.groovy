package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants

class DiscoverySystemFrontend implements Comparable {

    @RefdataInfo(cat = RDConstants.DISCOVERY_SYSTEM_FRONTEND)
    RefdataValue frontend

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            org: Org,
            frontend: RefdataValue
    ]

    static mapping = {
        id                  column: 'dsf_id'
        version             column: 'dsf_version'
        org                 column: 'dsf_org_fk', index: 'dsf_org_idx'
        frontend            column: 'dsf_frontend_rv_fk', index: 'dsf_frontend_idx'
        dateCreated         column: 'dsf_date_created'
        lastUpdated         column: 'dsf_last_updated'
    }

    @Override
    int compareTo(Object o) {
        DiscoverySystemFrontend b = (DiscoverySystemFrontend) o
        return frontend.getI10n('value') <=> b.frontend.getI10n('value')
    }
}
