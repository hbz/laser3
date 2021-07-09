package de.laser.workflow

class WfSequencePrototype extends WfSequenceBase {

    static final String KEY = 'WFSP'

    WfTaskPrototype head

    static mapping = {
                 id column: 'wfsp_id'
            version column: 'wfsp_version'
               type column: 'wfsp_type_rv_fk'
               head column: 'wfsp_head_fk'
              title column: 'wfsp_title'
        description column: 'wfsp_description', type: 'text'

        dateCreated column: 'wfsp_date_created'
        lastUpdated column: 'wfsp_last_updated'
    }

    static constraints = {
        head        (nullable: true)
        description (nullable: true, blank: false)
    }

    List<WfTaskPrototype> getWorkflow() {
        head ? head.getWorkflow() : []
    }
}
