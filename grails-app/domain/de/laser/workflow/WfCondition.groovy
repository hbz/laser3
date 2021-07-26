package de.laser.workflow

import de.laser.RefdataValue
import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants

class WfCondition extends WfConditionBase {

    static final String KEY = 'WF_CONDITION'

    @RefdataAnnotation(cat = RDConstants.WF_CONDITION_STATUS)
    RefdataValue status

    // static belongsTo = [ task: WfTask ]

    static mapping = {
                     id column: 'wfc_id'
                version column: 'wfc_version'
                status  column: 'wfc_status_rv_fk'
                  type  column: 'wfc_type'
                  title column: 'wfc_title'
            description column: 'wfc_description', type: 'text'

              checkbox1 column: 'wfc_checkbox1'
        checkbox1_title column: 'wfc_checkbox1_title'
    checkbox1_isTrigger column: 'wfc_checkbox1_is_trigger'
              checkbox2 column: 'wfc_checkbox2'
        checkbox2_title column: 'wfc_checkbox2_title'
    checkbox2_isTrigger column: 'wfc_checkbox2_is_trigger'
                  date1 column: 'wfc_date1'
            date1_title column: 'wfc_date1_title'
                  date2 column: 'wfc_date2'
            date2_title column: 'wfc_date2_title'

            dateCreated column: 'wfc_date_created'
            lastUpdated column: 'wfc_last_updated'
    }

    static constraints = {
        description     (nullable: true, blank: false)
        checkbox1_title (nullable: true, blank: false)
        checkbox2_title (nullable: true, blank: false)
        date1           (nullable: true)
        date1_title     (nullable: true, blank: false)
        date2           (nullable: true)
        date2_title     (nullable: true, blank: false)
    }

}
