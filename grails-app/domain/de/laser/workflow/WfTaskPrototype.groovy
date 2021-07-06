package de.laser.workflow

import de.laser.RefdataValue
import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants

class WfTaskPrototype {

    @RefdataAnnotation(cat = RDConstants.WORKFLOW_TASK_PRIORITY)
    RefdataValue priority

    @RefdataAnnotation(cat = RDConstants.WORKFLOW_TASK_TYPE)
    RefdataValue type

    String title
    String description

    Date dateCreated
    Date lastUpdated

    static mapping = {
                 id column: 'wftp_id'
            version column: 'wftp_version'
           priority column: 'wftp_priority_rv_fk'
               type column: 'wftp_type_rv_fk'
              title column: 'wftp_title'
        description column: 'wftp_description', type: 'text'

        dateCreated column: 'wftp_date_created'
        lastUpdated column: 'wftp_last_updated'
    }

    static constraints = {
        description (nullable: true, blank: false)
    }
}
