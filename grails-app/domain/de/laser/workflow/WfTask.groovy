package de.laser.workflow

import de.laser.RefdataValue
import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants

class WfTask extends WfTaskBase {

    static final String KEY = 'WFT'

    @RefdataAnnotation(cat = RDConstants.WORKFLOW_TASK_STATUS)
    RefdataValue status

    WfTaskPrototype prototype
    WfTask head
    WfTask next

    String comment

    static mapping = {
                 id column: 'wft_id'
            version column: 'wft_version'
           priority column: 'wft_priority_rv_fk'
             status column: 'wft_status_rv_fk'
               type column: 'wft_type_rv_fk'
          prototype column: 'wft_prototype_fk'
              title column: 'wft_title'
        description column: 'wft_description', type: 'text'
            comment column: 'wft_comment', type: 'text'
               head column: 'wft_head_fk'
               next column: 'wft_next_fk'

        dateCreated column: 'wft_date_created'
        lastUpdated column: 'wft_last_updated'
    }

    static constraints = {
        description (nullable: true, blank: false)
        head        (nullable: true)
        next        (nullable: true)
        comment     (nullable: true, blank: false)
    }

    List<WfTask> getWorkflow() {
        List<WfTask> wf = []

        WfTask t = this
        while (t) {
            wf.add( t ); t = t.next
        }
        wf
    }

    WfTask getPrevious() {
        List<WfTask> result = WfTask.executeQuery('select wft from WfTask wft where next = :current order by id', [current: this] )

        if (result.size() > 1) {
            log.warn( 'MULTIPLE MATCHES - getPrevious()')
        }

        if (result) {
            return result.first() as WfTask
        }
    }
}
