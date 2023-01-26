package de.laser.workflow.light

import de.laser.*

class WfChecklist {

    static final String KEY = 'WF_CHECKLIST'

    String title
    String description
    String comment

    Subscription subscription
    License license
    Org org

    Org owner
    Boolean template

    Date dateCreated
    Date lastUpdated

    static mapping = {
                   id column: 'wfcl_id'
              version column: 'wfcl_version'
                title column: 'wfcl_title'
          description column: 'wfcl_description', type: 'text'
              comment column: 'wfcl_comment', type: 'text'
         subscription column: 'wfcl_subscription_fk'
              license column: 'wfcl_license_fk'
                  org column: 'wfcl_org_fk'
                owner column: 'wfcl_owner_fk'
             template column: 'wfcl_is_template'
          dateCreated column: 'wfcl_date_created'
          lastUpdated column: 'wfcl_last_updated'
    }

    static constraints = {
        title            (blank: false)
        description      (nullable: true)
        comment          (nullable: true)
        subscription     (nullable: true)
        license          (nullable: true)
        org              (nullable: true)
    }

    WfCheckpoint instantiate(Object target) throws Exception {

    }

    void remove() throws Exception {
        WfCheckpoint.executeUpdate('delete from WfCheckpoint cp where cp.checklist = :cl', [cl: this])
        this.delete()
    }

    Set<WfCheckpoint> getSequence() {
        WfCheckpoint.executeQuery('select cp from WfCheckpoint cp where cp.checklist = :cl order by cp.position', [cl: this]) as Set<WfCheckpoint>
    }

    Map<String, Object> getInfo() {

        Map<String, Object> info = [
                lastUpdated: lastUpdated
        ]

        getSequence().each {cpoint ->
            // TODO
            if (cpoint.lastUpdated > info.lastUpdated) { info.lastUpdated = cpoint.lastUpdated }
        }

        info
    }

    static Set<WfChecklist> getAllChecklistsByOwnerAndObj(Org owner, def obj) {
        String query = 'select cl from WfChecklist cl where cl.owner = :owner and cl.template = false'

        if (obj instanceof Org) {
            executeQuery( query + ' and cl.org = :obj', [owner: owner, obj: obj]) as Set<WfChecklist>
        }
        else if (obj instanceof License) {
            executeQuery( query + ' and cl.license = :obj', [owner: owner, obj: obj]) as Set<WfChecklist>
        }
        else if (obj instanceof Subscription) {
            executeQuery( query + ' and cl.subscription = :obj', [owner: owner, obj: obj]) as Set<WfChecklist>
        }
        else {
            []
        }
    }

    static Set<WfChecklist> getAllTemplatesByOwnerAndObjType(Org owner, def obj) {
        String query = 'select cl from WfChecklist cl where cl.owner = :owner and cl.template = true'

        if (obj instanceof Org) {
            executeQuery( query + ' and cl.org != null', [owner: owner]) as Set<WfChecklist>
        }
        else if (obj instanceof License) {
            executeQuery( query + ' and cl.license != null', [owner: owner]) as Set<WfChecklist>
        }
        else if (obj instanceof Subscription) {
            executeQuery( query + ' and cl.subscription != null', [owner: owner]) as Set<WfChecklist>
        }
        else {
            []
        }
    }
}
