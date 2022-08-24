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
              checkbox2 column: 'wfcp_checkbox2'
              checkbox3 column: 'wfcp_checkbox3'
              checkbox4 column: 'wfcp_checkbox4'
        checkbox1_title column: 'wfcp_checkbox1_title'
        checkbox2_title column: 'wfcp_checkbox2_title'
        checkbox3_title column: 'wfcp_checkbox3_title'
        checkbox4_title column: 'wfcp_checkbox4_title'
    checkbox1_isTrigger column: 'wfcp_checkbox1_is_trigger'
    checkbox2_isTrigger column: 'wfcp_checkbox2_is_trigger'
    checkbox3_isTrigger column: 'wfcp_checkbox3_is_trigger'
    checkbox4_isTrigger column: 'wfcp_checkbox4_is_trigger'
                  date1 column: 'wfcp_date1'
                  date2 column: 'wfcp_date2'
                  date3 column: 'wfcp_date3'
                  date4 column: 'wfcp_date4'
            date1_title column: 'wfcp_date1_title'
            date2_title column: 'wfcp_date2_title'
            date3_title column: 'wfcp_date3_title'
            date4_title column: 'wfcp_date4_title'
                  file1 column: 'wfcp_file1'
                  file2 column: 'wfcp_file2'
            file1_title column: 'wfcp_file1_title'
            file2_title column: 'wfcp_file2_title'
            dateCreated column: 'wfcp_date_created'
            lastUpdated column: 'wfcp_last_updated'
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
                checkbox2:              this.checkbox2,
                checkbox3:              this.checkbox3,
                checkbox4:              this.checkbox4,
                checkbox1_title:        this.checkbox1_title,
                checkbox2_title:        this.checkbox2_title,
                checkbox3_title:        this.checkbox3_title,
                checkbox4_title:        this.checkbox4_title,
                checkbox1_isTrigger:    this.checkbox1_isTrigger,
                checkbox2_isTrigger:    this.checkbox2_isTrigger,
                checkbox3_isTrigger:    this.checkbox3_isTrigger,
                checkbox4_isTrigger:    this.checkbox4_isTrigger,
                date1:                  this.date1,
                date2:                  this.date2,
                date3:                  this.date3,
                date4:                  this.date4,
                date1_title:            this.date1_title,
                date2_title:            this.date2_title,
                date3_title:            this.date3_title,
                date4_title:            this.date4_title,
                file1:                  this.file1,
                file2:                  this.file2,
                file1_title:            this.file1_title,
                file2_title:            this.file2_title,
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
        List<WfTaskPrototype> result = WfTaskPrototype.findAllByCondition(this, [sort: 'id'])

        if (result.size() > 1) {
            log.debug('Multiple matches for WfConditionPrototype.getTask() ' + this.id + ' -> ' + result.collect{ it.id })
        }
        return result ? result.first() : null
    }
}
