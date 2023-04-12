package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.exceptions.CreationException
import de.laser.storage.RDConstants

class IssueEntitlementChange {

    TitleChange titleChange
    Subscription subscription
    @RefdataInfo(cat = RDConstants.PENDING_CHANGE_STATUS)
    RefdataValue changeStatus
    Date actionDate
    Org owner
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id                      column: 'iec_id'
        version                 column: 'iec_version'
        titleChange             column: 'iec_tic_fk', index: 'iec_tic_idx, iec_title_sub_status_idx'
        subscription            column: 'iec_sub_fk', index: 'iec_sub_idx, iec_title_sub_status_idx'
        changeStatus            column: 'iec_status_rv_fk', index: 'iec_status_idx, iec_title_sub_status_idx'
        owner                   column: 'iec_owner_fk', index: 'iec_owner_idx'
        actionDate              column: 'iec_action_date'
        dateCreated             column: 'iec_date_created'
        lastUpdated             column: 'iec_last_updated'
    }

    static constraints = {
    }

    static IssueEntitlementChange construct(Map configMap) throws CreationException {
        IssueEntitlementChange iec = new IssueEntitlementChange()
        if(iec.save())
            iec
        else if(iec.errors)
            throw new CreationException(iec.errors)
        else {
            log.error("unknown error")
            null
        }
    }
}
