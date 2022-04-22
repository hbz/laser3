package de.laser


import de.laser.auth.User
import de.laser.storage.BeanStorage
import de.laser.storage.RDConstants
import de.laser.annotations.RefdataInfo

/**
 * Represents a single task which can be attached to an object an is, unlike {@link de.laser.workflow.WfTask}, not part of a more complex workflow
 * Tasks may but need not to be linked to an object; if they are, the following objects can contain them:
 * <ul>
 *     <li>{@link License}</li>
 *     <li>{@link Org}</li>
 *     <li><s>{@link Package}</s> (is still included in the domain model but is disused)</li>
 *     <li>{@link Subscription}</li>
 *     <li>{@link SurveyConfig}</li>
 * </ul>
 * Tasks have a status and can be assigned either to a single {@link User} or to the institution {@link Org} as a whole
 */
class Task {

    License         license
    Org             org
    Package         pkg
    Subscription    subscription
    SurveyConfig    surveyConfig

    String          title
    String          description

    @RefdataInfo(cat = RDConstants.TASK_STATUS)
    RefdataValue    status

    User            creator
    Date            endDate
    Date            systemCreateDate
    Date            createDate

    User            responsibleUser
    Org             responsibleOrg

    Date dateCreated
    Date lastUpdated

    static constraints = {
        license         (nullable:true)
        org             (nullable:true)
        pkg             (nullable:true)
        subscription    (nullable:true)
        surveyConfig    (nullable:true)
        title           (blank:false)
        description     (nullable:true, blank:true)
        responsibleUser (nullable:true)
        responsibleOrg  (nullable:true)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }

    static transients = ['objects'] // mark read-only accessor methods

    static mapping = {
        id              column:'tsk_id'
        version         column:'tsk_version'

        license         column:'tsk_lic_fk'
        org             column:'tsk_org_fk'
        pkg             column:'tsk_pkg_fk'
        subscription    column:'tsk_sub_fk'
        surveyConfig      column:'tsk_sur_config_fk'

        title           column:'tsk_title'
        description     column:'tsk_description', type: 'text'
        status          column:'tsk_status_rdv_fk'

        creator         column:'tsk_creator_fk'
        endDate         column:'tsk_end_date'
        systemCreateDate column:'tsk_system_create_date'
        createDate      column:'tsk_create_date'

        responsibleUser      column:'tsk_responsible_user_fk'
        responsibleOrg       column:'tsk_responsible_org_fk'

        dateCreated     column: 'tsk_date_created'
        lastUpdated     column: 'tsk_last_updated'

    }

    def afterDelete() {
        BeanStorage.getDeletionService().deleteDocumentFromIndex(this.getClass().getSimpleName().toLowerCase()+":"+this.id, this.class.simpleName)
    }

    /**
     * Retrieves the associated objects to this task as link parameters
     * @return a {@link List} of link argument maps, depending on the object type to which this task is associated
     */
    List getObjects() {
        List result = []

        if (license)
            result << [controller: 'license', object: license]
        if (org)
            result << [controller: 'organisation', object: org]
        if (pkg)
            result << [controller: 'package', object: pkg]
        if (subscription)
            result << [controller: 'subscription', object: subscription]
        if (surveyConfig)
            result << [controller: 'survey', object: surveyConfig]

        result
    }

    /**
     * This getter is used by the mail template and retrieves link arguments for the object display to which this task is associated
     * @return a {@link Map} of link arguments for the object with which this task is associated
     */
    def getDisplayArgs() {
        Map<String, Object> displayArgs = [action: 'show', absolute: true]
        if (license) {
            displayArgs.controller = 'license'
            displayArgs.id = license.id
        }
        else if (org) {
            displayArgs.controller = 'organisation'
            displayArgs.id = org.id
        }
        else if (pkg) {
            displayArgs.controller = 'package'
            displayArgs.id = pkg.id
        }
        else if (subscription) {
            displayArgs.controller = 'subscription'
            displayArgs.id = subscription.id
        }
        else if (surveyConfig) {
            displayArgs.controller = 'survey'
            displayArgs.id = surveyConfig.surveyInfo.id
            displayArgs.surveyConfigID = surveyConfig.id
        }
        else {
            displayArgs.controller = 'myInstitution'
            displayArgs.action = 'tasks'
        }
        displayArgs
    }
}
