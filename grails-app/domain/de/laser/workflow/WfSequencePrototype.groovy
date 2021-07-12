package de.laser.workflow

class WfSequencePrototype extends WfSequenceBase {

    static final String KEY = 'WFSP'

    WfTaskPrototype child

    static mapping = {
                 id column: 'wfsp_id'
            version column: 'wfsp_version'
               type column: 'wfsp_type_rv_fk'
              child column: 'wfsp_child_fk'
              title column: 'wfsp_title'
        description column: 'wfsp_description', type: 'text'

        dateCreated column: 'wfsp_date_created'
        lastUpdated column: 'wfsp_last_updated'
    }

    static constraints = {
        child       (nullable: true)
        description (nullable: true, blank: false)
    }

    List<WfTaskPrototype> getStructure() {
        child ? child.getStructure() : []
    }
}
