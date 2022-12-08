package de.laser.workflow

import de.laser.DocContext
import de.laser.License
import de.laser.Org
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.UserSetting
import de.laser.annotations.RefdataInfo
import de.laser.auth.User
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import org.springframework.context.MessageSource

/**
 * Represents a workflow. It is based on a {@link WfWorkflowPrototype} and may contain several {@link WfTask}s. A workflow is linked to a {@link Subscription} to which tasks should be done in an ordered way and owned by an
 * {@link Org} whose members should solve the tasks in this workflow. Moreover, a workflow may be finalised or under testing, see the controlled list for {@link RDConstants#WF_WORKFLOW_STATE} and put in different stages, according
 * to {@link RDConstants#WF_WORKFLOW_STATUS}:
 * <ul>
 *     <li>open</li>
 *     <li>canceled</li>
 *     <li>done</li>
 * </ul>
 */
class WfWorkflow extends WfWorkflowBase {

    static final String KEY = 'WF_WORKFLOW'

    @RefdataInfo(cat = RDConstants.WF_WORKFLOW_STATUS)
    RefdataValue status

    WfTask task
    Org    owner
    User   user
    Date   userLastUpdated

    Subscription subscription
    License license
    Org org

    String comment

    WfWorkflowPrototype prototype
    String              prototypeTitle
    String              prototypeVariant
    Date                prototypeLastUpdated

    static mapping = {
                           id column: 'wfw_id'
                      version column: 'wfw_version'
                       status column: 'wfw_status_rv_fk'
                    prototype column: 'wfw_prototype_fk'
               prototypeTitle column: 'wfw_prototype_title'
             prototypeVariant column: 'wfw_prototype_variant'
         prototypeLastUpdated column: 'wfw_prototype_last_updated'
                         task column: 'wfw_task_fk'
                        owner column: 'wfw_owner_fk'
                         user column: 'wfw_user_fk'
              userLastUpdated column: 'wfw_user_last_updated'

       subscription column: 'wfw_subscription_fk'
            license column: 'wfw_license_fk'
                org column: 'wfw_org_fk'

              title column: 'wfw_title'
        description column: 'wfw_description', type: 'text'
            comment column: 'wfw_comment', type: 'text'

        dateCreated column: 'wfw_date_created'
        lastUpdated column: 'wfw_last_updated'
    }

    static constraints = {
        title            (blank: false)
        prototypeVariant (blank: false)
        prototypeTitle   (blank: false)

        user             (nullable: true)
        userLastUpdated  (nullable: true)

        subscription     (nullable: true)
        license          (nullable: true)
        org              (nullable: true)

        task             (nullable: true)
        description      (nullable: true)
        comment          (nullable: true)
    }

    /**
     * Retrieves the tasks in this workflow, ordered by the intellectually set order. Each {@link WfTask} is linked by the {@link WfTask#next} property
     * @return the {@link List} of {@link WfTask}s in this workflow
     */
    List<WfTask> getSequence() {
        List<WfTask> sequence = []

        if (task) {
            WfTask t = task

            while (t) {
                sequence.add( t ); t = t.next
            }
        }
        sequence
    }

    /**
     * Assembles a summary of information of this workflow. This summary contains:
     * <ul>
     *     <li>how many tasks are open?</li>
     *     <li>how many tasks are canceled?</li>
     *     <li>how many tasks are done?</li>
     *     <li>how many tasks are of normal priority?</li>
     *     <li>how many tasks are optional?</li>
     *     <li>how many tasks are important?</li>
     *     <li>how many tasks are blockers of normal priority?</li>
     *     <li>how many tasks are blockers of importance?</li>
     *     <li>when was the last update done on this workflow?</li>
     * </ul>
     * @return a {@link Map} containing the relevant information
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
            tasksOpen: 0,
            tasksCanceled: 0,
            tasksDone: 0,
            tasksNormal: 0,
            tasksOptional: 0,
            tasksImportant: 0,
            tasksNormalBlocking: 0,
            tasksImportantBlocking: 0,
            lastUpdated: lastUpdated
        ]

        if (org) {
            info.target = org
            info.targetName = org.name
            info.targetTitle = ms.getMessage('org.institution.label', null, locale) + '/' + ms.getMessage('default.provider.label', null, locale)
            info.targetIcon = 'university'
            info.targetController = 'org'
        }
        else if (license) {
            info.target = license
            info.targetName = license.reference
            info.targetTitle = ms.getMessage('license.label', null, locale)
            info.targetIcon = 'balance scale'
            info.targetController = 'lic'
        }
        else if (subscription) {
            info.target = subscription
            info.targetName = subscription.name
            info.targetTitle = ms.getMessage('subscription.label', null, locale)
            info.targetIcon = 'clipboard'
            info.targetController = 'subscription'
        }

        List<WfTask> sequence = []

        getSequence().each{ task ->
            sequence.add(task)
        }

        sequence.each{task ->
            if (task.status == RDStore.WF_TASK_STATUS_OPEN)     { info.tasksOpen++ }
            if (task.status == RDStore.WF_TASK_STATUS_CANCELED) { info.tasksCanceled++ }
            if (task.status == RDStore.WF_TASK_STATUS_DONE)     { info.tasksDone++ }

            if (task.priority == RDStore.WF_TASK_PRIORITY_NORMAL)       {
                info.tasksNormal++
                if (task.status != RDStore.WF_TASK_STATUS_DONE) {
                    info.tasksNormalBlocking++
                }
            }
            if (task.priority == RDStore.WF_TASK_PRIORITY_OPTIONAL)     { info.tasksOptional++ }
            if (task.priority == RDStore.WF_TASK_PRIORITY_IMPORTANT)    {
                info.tasksImportant++
                if (task.status != RDStore.WF_TASK_STATUS_DONE) {
                    info.tasksImportantBlocking++
                }
            }

            // TODO
            if (task.lastUpdated > info.lastUpdated) { info.lastUpdated = task.lastUpdated }
            if (task.condition && task.condition.lastUpdated > info.lastUpdated) { info.lastUpdated = task.condition.lastUpdated }
        }

        info
    }

    boolean isFlaggedAsNew(User currentUser) {
        long timeWindow = currentUser.getSettingsValue(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14) * 86400 * 1000
        return userLastUpdated ? (userLastUpdated.getTime() + timeWindow) > System.currentTimeMillis() : false
    }

    Set<DocContext> getCurrentDocContexts() {
        Set<DocContext> dcList = []

        getSequence().each {task ->
            WfCondition c = task.getCondition()
            if (c.file1) dcList.add(c.file1)
            if (c.file2) dcList.add(c.file2)
            if (c.file3) dcList.add(c.file3)
            if (c.file4) dcList.add(c.file4)
        }
        dcList
    }

    static List<WfWorkflow> getWorkflowsByObject(def obj) {
        List<WfWorkflow> workflows = []

        if (obj instanceof Org) {
            workflows = findAllByOrg(obj)
        }
        else if (obj instanceof License) {
            workflows = findAllByLicense(obj)
        }
        else if (obj instanceof Subscription) {
            workflows = findAllBySubscription(obj)
        }
        workflows
    }

    /**
     * Removes this workflow and all tasks linked to it
     * @throws Exception
     */
    void remove() throws Exception {
        if (this.task) {
            this.task.remove() //this calls the succession chain as well
        }
        this.delete()
    }
}
