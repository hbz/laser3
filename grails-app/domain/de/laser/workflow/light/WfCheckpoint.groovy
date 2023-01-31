package de.laser.workflow.light

class WfCheckpoint {

    static final String KEY = 'WF_CHECKPOINT'

    String title
    String description
    String comment

    Date date
    Boolean done

    WfChecklist checklist
    int position

    Date dateCreated
    Date lastUpdated

    static mapping = {
                 id column: 'wfcp_id'
            version column: 'wfcp_version'
              title column: 'wfcp_title'
        description column: 'wfcp_description', type: 'text'
            comment column: 'wfcp_comment', type: 'text'
               date column: 'wfcp_date'
               done column: 'wfcp_is_done'
          checklist column: 'wfcp_checklist_fk'
           position column: 'wfcp_position'
        dateCreated column: 'wfcp_date_created'
        lastUpdated column: 'wfcp_last_updated'
    }

    static constraints = {
        title       (blank: false)
        description (nullable: true)
        comment     (nullable: true)
        date        (nullable: true)
    }

//    def afterInsert() {
//        super.afterUpdateHandler()
//    }
//    def afterUpdate() {
//        super.afterUpdateHandler()
//    }
//    def afterDelete() {
//        super.afterUpdateHandler()
//    }
}
