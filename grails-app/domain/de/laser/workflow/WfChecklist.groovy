package de.laser.workflow

import de.laser.*
import de.laser.ui.Icon
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.context.MessageSource

/**
 * A workflow check list keeping track of the points needed to deal with regarding a given subscription or license
 * @see License
 * @see Subscription
 * @see WfCheckpoint
 */
class WfChecklist {

    static final String KEY = 'WF_CHECKLIST'


    String title                // instantiate
    String description          // instantiate
    String comment

    Subscription subscription
    License license
    Org org
    Provider provider
    Vendor vendor

    Org owner                   // instantiate
    Boolean template = false

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
             provider column: 'wfcl_prov_fk'
               vendor column: 'wfcl_ven_fk'
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
        provider         (nullable: true)
        vendor           (nullable: true)
    }

    /**
     * The actual checklist: gets all checkpoints of this list
     * @return a {@link Set} of {@link WfCheckpoint}s defined for this list
     */
    Set<WfCheckpoint> getSequence() {
        WfCheckpoint.executeQuery('select cp from WfCheckpoint cp where cp.checklist = :cl order by cp.position', [cl: this]) as Set<WfCheckpoint>
    }

    /**
     * Gets the next free position for this checklist
     * @return the next position
     */
    int getNextPosition() {
        int position = WfCheckpoint.executeQuery('select max(cp.position) from WfCheckpoint cp where cp.checklist = :cl', [cl: this])[0]
        Math.max(position, 0) + 1
    }

    /**
     * Retrieves the relevant information for this workflow and assembles its frontend output
     * @return a {@link Map} containing the template information for this checklist
     */
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

        if (license) {
            info.target = GrailsHibernateUtil.unwrapIfProxy(license)
            info.targetName = license.reference
            info.targetTitle = ms.getMessage('license.label', null, locale)
            info.targetIcon = Icon.LICENSE
            info.targetController = 'lic'
        }
        else if (org) {
            info.target = GrailsHibernateUtil.unwrapIfProxy(org)
            info.targetName = org.name
            info.targetTitle = ms.getMessage('org.institution.label', null, locale) + '/' + ms.getMessage('provider.label', null, locale)
            info.targetIcon = Icon.ORG
            info.targetController = 'org'
        }
        else if (provider) {
            info.target = GrailsHibernateUtil.unwrapIfProxy(provider)
            info.targetName = provider.name
            info.targetTitle = ms.getMessage('provider.label', null, locale)
            info.targetIcon = Icon.PROVIDER
            info.targetController = 'provider'
        }
        else if (subscription) {
            info.target = GrailsHibernateUtil.unwrapIfProxy(subscription)
            info.targetName = subscription.name
            info.targetTitle = ms.getMessage('subscription.label', null, locale)
            info.targetIcon = Icon.SUBSCRIPTION
            info.targetController = 'subscription'
        }
        else if (vendor) {
            info.target = GrailsHibernateUtil.unwrapIfProxy(vendor)
            info.targetName = vendor.name
            info.targetTitle = ms.getMessage('vendor.label', null, locale)
            info.targetIcon = Icon.VENDOR
            info.targetController = 'vendor'
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

    /**
     * Currently unused?
     * Gets all checklists belonging to the given institution and for the given object
     * @param owner the institution ({@link Org}) whose checklists are being requested
     * @param obj the object (one of {@link Org}, {@link License} or {@link Subscription}) for which the checklists are being requested
     * @return a {@link Set} of checklists belonging to the institution and related to the given object
     */
    static Set<WfChecklist> getAllChecklistsByOwnerAndObjAndStatus(Org owner, def obj, RefdataValue status = null) {
        Set<WfChecklist> workflows = []
        String sql

             if (obj instanceof License)        { sql = 'cl.license = :obj' }
        else if (obj instanceof Org)            { sql = 'cl.org = :obj' }
        else if (obj instanceof Provider)       { sql = 'cl.provider = :obj' }
        else if (obj instanceof Subscription)   { sql = 'cl.subscription = :obj' }
        else if (obj instanceof Vendor)         { sql = 'cl.vendor = :obj' }

        if (sql) {
            workflows = executeQuery('select cl from WfChecklist cl where cl.owner = :owner and ' + sql, [owner: owner, obj: obj]) as Set<WfChecklist>

            if (status) {
                workflows = workflows.findAll { w -> w.getInfo().status == status }
            }
        }
        workflows
    }

    /**
     * Gets all checklist templates belonging to the given institution and for the given object
     * @param owner the institution ({@link Org}) whose checklist templates are being requested
     * @param obj the object (one of {@link Org}, {@link License} or {@link Subscription}) for which the checklist templates are being requested
     * @return a {@link Set} of checklist templates belonging to the institution and related to the given object
     */
    static Set<WfChecklist> getAllTemplatesByOwnerAndObjType(Org owner, def obj) {
        String query = 'select cl from WfChecklist cl where cl.owner = :owner and cl.template = true'

        if (obj instanceof License) {
            executeQuery( query + ' and cl.license != null', [owner: owner]) as Set<WfChecklist>
        }
        else if (obj instanceof Org) {
            executeQuery( query + ' and cl.org != null', [owner: owner]) as Set<WfChecklist>
        }
        else if (obj instanceof Provider) {
            executeQuery( query + ' and cl.provider != null', [owner: owner]) as Set<WfChecklist>
        }
        else if (obj instanceof Subscription) {
            executeQuery( query + ' and cl.subscription != null', [owner: owner]) as Set<WfChecklist>
        }
        else if (obj instanceof Vendor) {
            executeQuery( query + ' and cl.vendor != null', [owner: owner]) as Set<WfChecklist>
        }
        else {
            []
        }
    }
}
