package de.laser.workflow

class WfTaskPrototype extends WfTaskBase {

    static final String KEY = 'WFTP'

    WfTaskPrototype child
    WfTaskPrototype next

    static mapping = {
                 id column: 'wftp_id'
            version column: 'wftp_version'
           priority column: 'wftp_priority_rv_fk'
               type column: 'wftp_type_rv_fk'
              child column: 'wftp_child_fk'
               next column: 'wftp_next_fk'
              title column: 'wftp_title'
        description column: 'wftp_description', type: 'text'

        dateCreated column: 'wftp_date_created'
        lastUpdated column: 'wftp_last_updated'
    }

    static constraints = {
        description (nullable: true, blank: false)
        child       (nullable: true)
        next        (nullable: true)
    }

    List<WfTaskPrototype> getStructure() {
        List<WfTaskPrototype> struct = []

        WfTaskPrototype t = this
        while (t) {
            struct.add( t ); t = t.next
        }
        struct
    }

    WfTaskPrototype getParent() {
        List<WfTaskPrototype> result = WfTaskPrototype.executeQuery('select wftp from WfTaskPrototype wftp where child = :current order by id', [current: this] )

        if (result.size() > 1) {
            log.warn( 'MULTIPLE MATCHES - getParent()')
        }
        if (result) {
            return result.first() as WfTaskPrototype
        }
    }

    WfTaskPrototype getPrevious() {
        List<WfTaskPrototype> result = WfTaskPrototype.executeQuery('select wftp from WfTaskPrototype wftp where next = :current order by id', [current: this] )

        if (result.size() > 1) {
            log.warn( 'MULTIPLE MATCHES - getPrevious()')
        }
        if (result) {
            return result.first() as WfTaskPrototype
        }
    }
}
