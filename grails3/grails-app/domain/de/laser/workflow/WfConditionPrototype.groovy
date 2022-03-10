package de.laser.workflow

/**
 * A prototype of a workflow condition. For the actual condition, see {@link WfCondition}
 */
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
                  file1 column: 'wfcp_file1'
            file1_title column: 'wfcp_file1_title'

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
        file1           (nullable: true)
        file1_title     (nullable: true)
    }

    /**
     * Checks whether this condition is used by any {@link WfTaskPrototype}
     * @return is there any {@link WfTaskPrototype} linked to this condition?
     */
    boolean inUse() {
        WfTaskPrototype.findByCondition( this ) != null
    }

    /**
     * Factory method to instantiate a new {@link WfCondition} based on this prototype
     * @return the new {@link WfCondition}
     * @throws Exception
     */
    WfCondition instantiate() throws Exception {

        WfCondition condition = new WfCondition(
                title:              this.title,
                description:        this.description,
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
                file1:                  this.file1,
                file1_title:            this.file1_title,
        )
        if (! condition.validate()) {
            log.debug( '[ ' + this.id + ' ].instantiate() : ' + condition.getErrors().toString() )
        }

        condition
    }

    /**
     * Gets the task prototype associated to this prototype. A warning is being emit when there are multiple matches to this condition prototype; in such a case, the first is being returned (ordered by id)
     * @return the associated {@link WfTaskPrototype}
     */
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
