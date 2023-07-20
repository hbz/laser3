package de.laser.workflow

import de.laser.*
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.context.MessageSource

class WfChecklist {

    static final String KEY = 'WF_CHECKLIST'

//    @RefdataInfo(cat = RDConstants.WF_WORKFLOW_STATUS)
//    RefdataValue status

    String title                // instantiate
    String description          // instantiate
    String comment

    Subscription subscription
    License license
    Org org

    Org owner                   // instantiate
    Boolean template = false

    Date dateCreated
    Date lastUpdated

    static mapping = {
                   id column: 'wfcl_id'
              version column: 'wfcl_version'
//               status column: 'wfcl_status_rv_fk'
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
    
    Set<WfCheckpoint> getSequence() {
        WfCheckpoint.executeQuery('select cp from WfCheckpoint cp where cp.checklist = :cl order by cp.position', [cl: this]) as Set<WfCheckpoint>
    }

    int getNextPosition() {
        int position = WfCheckpoint.executeQuery('select max(cp.position) from WfCheckpoint cp where cp.checklist = :cl', [cl: this])[0]
        Math.max(position, 0) + 1
    }

    Map<String, Object> getInfo() {

        MessageSource ms = BeanStore.getMessageSource()
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> info = [
                target: null,
                targetName: '',
                targetTitle: '',
                targetIcon: '',
                targetController: '',
                lastUpdated: lastUpdated,
                status: RDStore.WF_WORKFLOW_STATUS_DONE
        ]

        if (org) {
            info.target = GrailsHibernateUtil.unwrapIfProxy(org)
            info.targetName = org.name
            info.targetTitle = ms.getMessage('org.institution.label', null, locale) + '/' + ms.getMessage('default.provider.label', null, locale)
            info.targetIcon = 'university'
            info.targetController = 'org'
        }
        else if (license) {
            info.target = GrailsHibernateUtil.unwrapIfProxy(license)
            info.targetName = license.reference
            info.targetTitle = ms.getMessage('license.label', null, locale)
            info.targetIcon = 'balance scale'
            info.targetController = 'lic'
        }
        else if (subscription) {
            info.target = GrailsHibernateUtil.unwrapIfProxy(subscription)
            info.targetName = subscription.name
            info.targetTitle = ms.getMessage('subscription.label', null, locale)
            info.targetIcon = 'clipboard'
            info.targetController = 'subscription'
        }

        boolean sequenceIsDone = true
        getSequence().each {cpoint ->
            // TODO
            if (cpoint.lastUpdated > info.lastUpdated) { info.lastUpdated = cpoint.lastUpdated }
            sequenceIsDone = sequenceIsDone && cpoint.done
        }

        info.status = sequenceIsDone ? RDStore.WF_WORKFLOW_STATUS_DONE : RDStore.WF_WORKFLOW_STATUS_OPEN

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
