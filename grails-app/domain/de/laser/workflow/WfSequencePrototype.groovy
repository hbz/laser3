package de.laser.workflow

import de.laser.Org
import de.laser.RefdataValue
import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants

class WfSequencePrototype {

    @RefdataAnnotation(cat = RDConstants.WORKFLOW_SEQUENCE_TYPE)
    RefdataValue type

    Org owner

    String title
    String description

    Date dateCreated
    Date lastUpdated

    static mapping = {
                 id column: 'wfsp_id'
            version column: 'wfsp_version'
               type column: 'wfsp_type_rv_fk'
              owner column: 'wfsp_owner_fk'
              title column: 'wfsp_title'
        description column: 'wfsp_description', type: 'text'

        dateCreated column: 'wfsp_date_created'
        lastUpdated column: 'wfsp_last_updated'
    }

    static constraints = {
        description (nullable: true, blank: false)
    }
}
