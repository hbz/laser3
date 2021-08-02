package de.laser.workflow

import de.laser.helper.RDStore

class WfConditionPrototype extends WfConditionBase {

    static final String KEY = 'WF_CONDITION_PROTOTYPE'

    static mapping = {
                     id column: 'wfcp_id'
                version column: 'wfcp_version'
                  type  column: 'wfcp_type'
                  title column: 'wfcp_title'
            description column: 'wfcp_description', type: 'text'

              checkbox1 column: 'wfcp_checkbox1'
        checkbox1_title column: 'wfcp_checkbox1_title'
    checkbox1_isTrigger column: 'wfcp_checkbox1_is_trigger'
              checkbox2 column: 'wfcp_checkbox2'
        checkbox2_title column: 'wfcp_checkbox2_title'
    checkbox2_isTrigger column: 'wfcp_checkbox2_is_trigger'
                  date1 column: 'wfcp_date1'
            date1_title column: 'wfcp_date1_title'
                  date2 column: 'wfcp_date2'
            date2_title column: 'wfcp_date2_title'

            dateCreated column: 'wfcp_date_created'
            lastUpdated column: 'wfcp_last_updated'
    }

    static constraints = {
        title           (blank: false)
        description     (nullable: true)
        checkbox1_title (nullable: true)
        checkbox2_title (nullable: true)
        date1           (nullable: true)
        date1_title     (nullable: true)
        date2           (nullable: true)
        date2_title     (nullable: true)
    }

    boolean inUse() {
        WfTaskPrototype.findByCondition( this ) != null
    }

    WfCondition instantiate() throws Exception {

        WfCondition condition = new WfCondition(
                title:              this.title,
                description:        this.description,
                prototype:          this,
                // status:             RDStore.WF_CONDITION_STATUS_OPEN,
                type:               this.type,
                checkbox1:              this.checkbox1,
                checkbox1_title:        this.checkbox1_title,
                checkbox1_isTrigger:    this.checkbox1_isTrigger,
                checkbox2:              this.checkbox2,
                checkbox2_title:        this.checkbox2_title,
                checkbox2_isTrigger:    this.checkbox2_isTrigger,
                date1:                  this.date1,
                date1_title:            this.date1_title,
                date2:                  this.date2,
                date2_title:            this.date2_title,
        )
        if (! condition.validate()) {
            log.debug( '[ ' + this.id + ' ].instantiate() : ' + condition.getErrors().toString() )
        }

        condition
    }

    WfTaskPrototype getTask() {
        List<WfTaskPrototype> result = WfTaskPrototype.executeQuery('select tp from WfTaskPrototype tp where condition = :current order by id', [current: this] )

        if (result.size() > 1) {
            log.warn( 'MULTIPLE MATCHES - getWorkflow()')
        }
        if (result) {
            return result.first() as WfTaskPrototype
        }
    }
}
