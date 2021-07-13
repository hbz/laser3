package de.laser.workflow

import de.laser.RefdataValue
import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants

class WfTask extends WfTaskBase {

    static final String KEY = 'WFT'

    @RefdataAnnotation(cat = RDConstants.WORKFLOW_TASK_STATUS)
    RefdataValue status

    WfTaskPrototype prototype
    WfTask child
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
              child column: 'wft_child_fk'
               next column: 'wft_next_fk'

        dateCreated column: 'wft_date_created'
        lastUpdated column: 'wft_last_updated'
    }

    static constraints = {
        description (nullable: true, blank: false)
        child       (nullable: true)
        next        (nullable: true)
        comment     (nullable: true, blank: false)
    }

    List<WfTask> getStructure() {
        List<WfTask> struct = []

        WfTask t = this
        while (t) {
            struct.add( t ); t = t.next
        }
        struct
    }

    WfTaskPrototype getParent() {
        List<WfTask> result = WfTask.executeQuery('select t from WfTask t where child = :current order by id', [current: this] )

        if (result.size() > 1) {
            log.warn( 'MULTIPLE MATCHES - getParent()')
        }
        if (result) {
            return result.first() as WfTask
        }
    }

    WfTask getPrevious() {
        List<WfTask> result = WfTask.executeQuery('select t from WfTask t where next = :current order by id', [current: this] )

        if (result.size() > 1) {
            log.warn( 'MULTIPLE MATCHES - getPrevious()')
        }
        if (result) {
            return result.first() as WfTask
        }
    }

    boolean inStructure() {
        child != null
    }
}
