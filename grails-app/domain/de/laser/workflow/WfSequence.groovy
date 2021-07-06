package de.laser.workflow

import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants

class WfSequence extends WfSequencePrototype {

    @RefdataAnnotation(cat = RDConstants.WORKFLOW_SEQUENCE_STATUS)
    RefdataValue status

    WfSequencePrototype prototype
    Subscription subscription

    String comment

    static mapping = {
                 id column: 'wfs_id'
            version column: 'wfs_version'
             status column: 'wfs_status_rv_fk'
               type column: 'wfs_type_rv_fk'
              owner column: 'wfs_owner_fk'
          prototype column: 'wfs_prototype_fk'
       subscription column: 'wfs_subscription_fk'
              title column: 'wfs_title'
        description column: 'wfs_description', type: 'text'
            comment column: 'wfs_comment', type: 'text'

        dateCreated column: 'wfs_date_created'
        lastUpdated column: 'wfs_last_updated'
    }

    static constraints = {
        description (nullable: true, blank: false)
        comment     (nullable: true, blank: false)
    }
}
