package de.laser.workflow

import de.laser.storage.RDStore

/**
 * A workflow condition, mapping a trigger for a workflow task ({@link WfTask})
 */
class WfCondition extends WfConditionBase {

    static final String KEY = 'WF_CONDITION'

    static mapping = {
                     id column: 'wfc_id'
                version column: 'wfc_version'
                  type  column: 'wfc_type'
                  title column: 'wfc_title'
            description column: 'wfc_description', type: 'text'

              checkbox1 column: 'wfc_checkbox1'
              checkbox2 column: 'wfc_checkbox2'
              checkbox3 column: 'wfc_checkbox3'
              checkbox4 column: 'wfc_checkbox4'
        checkbox1_title column: 'wfc_checkbox1_title'
        checkbox2_title column: 'wfc_checkbox2_title'
        checkbox3_title column: 'wfc_checkbox3_title'
        checkbox4_title column: 'wfc_checkbox4_title'
    checkbox1_isTrigger column: 'wfc_checkbox1_is_trigger'
    checkbox2_isTrigger column: 'wfc_checkbox2_is_trigger'
    checkbox3_isTrigger column: 'wfc_checkbox3_is_trigger'
    checkbox4_isTrigger column: 'wfc_checkbox4_is_trigger'
                  date1 column: 'wfc_date1'
                  date2 column: 'wfc_date2'
                  date3 column: 'wfc_date3'
                  date4 column: 'wfc_date4'
            date1_title column: 'wfc_date1_title'
            date2_title column: 'wfc_date2_title'
            date3_title column: 'wfc_date3_title'
            date4_title column: 'wfc_date4_title'
                  file1 column: 'wfc_file1'
                  file2 column: 'wfc_file2'
            file1_title column: 'wfc_file1_title'
            file2_title column: 'wfc_file2_title'

            dateCreated column: 'wfc_date_created'
            lastUpdated column: 'wfc_last_updated'
    }

    static constraints = {
        title           (blank: false)
        description     (nullable: true)
        checkbox1_title (nullable: true)
        checkbox2_title (nullable: true)
        checkbox3_title (nullable: true)
        checkbox4_title (nullable: true)
        date1           (nullable: true)
        date2           (nullable: true)
        date3           (nullable: true)
        date4           (nullable: true)
        date1_title     (nullable: true)
        date2_title     (nullable: true)
        date3_title     (nullable: true)
        date4_title     (nullable: true)
        file1           (nullable: true)
        file2           (nullable: true)
        file1_title     (nullable: true)
        file2_title     (nullable: true)
    }

    /**
     * Removes this condition
     * @throws Exception
     */
    void remove() throws Exception {
        this.delete()
    }

    def afterUpdate() {
        if ((checkbox1_isTrigger && checkbox1) || (checkbox2_isTrigger && checkbox2)) {
            WfTask task = getTask()
            if (task.status == RDStore.WF_TASK_STATUS_OPEN) {
                task.status = RDStore.WF_TASK_STATUS_DONE
                task.save()
            }
        }
    }

    /**
     * Gets the task for this condition
     * @return the associated {@link WfTask}
     */
    WfTask getTask() {
        WfTask.findByCondition( this )
    }
}
