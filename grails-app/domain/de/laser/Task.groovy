package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.auth.User
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.survey.SurveyConfig
import de.laser.ui.Icon
import de.laser.wekb.Provider
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import org.grails.plugins.web.taglib.ApplicationTagLib

/**
 * Represents a single task which can be attached to an object an is, unlike {@link de.laser.workflow.WfCheckpoint}, not part of a more complex workflow
 * Tasks may but need not to be linked to an object; if they are, the following objects can contain them:
 * <ul>
 *     <li>{@link License}</li>
 *     <li>{@link Org}</li>
 *     <li>{@link de.laser.wekb.Provider}</li>
 *     <li>{@link de.laser.wekb.Vendor}</li>
 *     <li><s>{@link de.laser.wekb.Package}</s> (is still included in the domain model but is disused)</li>
 *     <li>{@link Subscription}</li>
 *     <li>{@link SurveyConfig}</li>
 * </ul>
 * Tasks have a status and can be assigned either to a single {@link User} or to the institution {@link Org} as a whole
 */
class Task {

    License         license
    Org             org
    Provider        provider
    Vendor          vendor
    Subscription    subscription
    SurveyConfig    surveyConfig
    TitleInstancePackagePlatform tipp

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
        provider        (nullable:true)
        vendor          (nullable:true)
        subscription    (nullable:true)
        surveyConfig    (nullable:true)
        tipp            (nullable:true)
        title           (blank:false)
        description     (nullable:true, blank:true)
        responsibleUser (nullable:true)
        responsibleOrg  (nullable:true)
        lastUpdated     (nullable: true)
    }

    static transients = ['objects'] // mark read-only accessor methods

    static mapping = {
        id              column:'tsk_id'
        version         column:'tsk_version'

        license         column:'tsk_lic_fk',        index: 'tsk_lic_idx'
        org             column:'tsk_org_fk',        index: 'tsk_org_idx'
        provider        column:'tsk_prov_fk',       index: 'tsk_prov_idx'
        vendor          column:'tsk_ven_fk',        index: 'tsk_ven_idx'
        subscription    column:'tsk_sub_fk',        index: 'tsk_sub_idx'
        surveyConfig    column:'tsk_sur_config_fk', index: 'tsk_sur_config_idx'
        tipp            column:'tsk_tipp_fk',       index: 'tsk_tipp_idx'

        title           column:'tsk_title'
        description     column:'tsk_description', type: 'text'
        status          column:'tsk_status_rdv_fk'

        creator         column:'tsk_creator_fk',    index: 'tsk_creator_idx'
        endDate         column:'tsk_end_date'
        systemCreateDate column:'tsk_system_create_date'
        createDate      column:'tsk_create_date'

        responsibleUser      column:'tsk_responsible_user_fk', index: 'tsk_responsible_user_idx'
        responsibleOrg       column:'tsk_responsible_org_fk',  index: 'tsk_responsible_org_idx'

        dateCreated     column: 'tsk_date_created'
        lastUpdated     column: 'tsk_last_updated'

    }

    def afterDelete() {
        BeanStore.getDeletionService().deleteDocumentFromIndex(this.getClass().getSimpleName().toLowerCase()+":"+this.id, this.class.simpleName)
    }

    /**
     * Retrieves associated object info
     * @return a {@link Map} depending on the object to which this task is associated
     */
    Map getObjectInfo() {
        Map result = null

        if (license)      { result = [controller: 'license',      icon: Icon.LICENSE,      object: license,      name: license.reference] }
        if (org)          { result = [controller: 'organisation', icon: Icon.ORG,          object: org,          name: org.name] }
        if (provider)     { result = [controller: 'provider',     icon: Icon.PROVIDER,     object: provider,     name: provider.name] }
        if (subscription) { result = [controller: 'subscription', icon: Icon.SUBSCRIPTION, object: subscription, name: subscription.name] }
        if (surveyConfig) { result = [controller: 'survey',       icon: Icon.SURVEY,       object: surveyConfig, name: surveyConfig.surveyInfo.name] }
        if (tipp)         { result = [controller: 'tipp',         icon: Icon.TIPP,         object: tipp,         name: tipp.name] }
        if (vendor)       { result = [controller: 'vendor',       icon: Icon.VENDOR,       object: vendor,       name: vendor.name] }

        result
    }

    /**
     * This getter is used by the mail template and retrieves link arguments for the object display to which this task is associated
     * @return a {@link Map} of link arguments for the object with which this task is associated
     */
    Map getDisplayArgs() {
        Map<String, Object> displayArgs = [action: 'show', absolute: true]

        if (license) {
            displayArgs.controller = 'license'
            displayArgs.id = license.id
        }
        else if (org) {
            displayArgs.controller = 'organisation'
            displayArgs.id = org.id
        }
        else if (provider) {
            displayArgs.controller = 'provider'
            displayArgs.id = provider.id
        }
        else if (vendor) {
            displayArgs.controller = 'vendor'
            displayArgs.id = vendor.id
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
        else if (tipp) {
            displayArgs.controller = 'tipp'
            displayArgs.id = tipp.id
        }
        else {
            displayArgs.controller = 'myInstitution'
            displayArgs.action = 'tasks'
        }
        displayArgs
    }

    /**
     * Gets the name of the object to which this task is related
     * @return one of:
     * <ul>
     *     <li>{@link License#reference}</li>
     *     <li>{@link Org#name}</li>
     *     <li>{@link Provider#name}</li>
     *     <li>{@link Vendor#name}</li>
     *     <li>{@link de.laser.wekb.Package#name}</li>
     *     <li>{@link Subscription#name}</li>
     *     <li>{@link de.laser.survey.SurveyInfo#name}</li>
     * </ul>
     */
    String getObjectName() { // TODO: search + spotlight
        String name = ''
        if (license)            { name = license.reference }
        else if (org)           { name = org.name }
        else if (provider)      { name = provider.name }
        else if (subscription)  { name = subscription.name }
        else if (surveyConfig)  { name = surveyConfig.surveyInfo.name }
        else if (tipp)          { name = tipp.name }
        else if (vendor)        { name = vendor.name }
        name
    }

    static Map<String, String> getObjectMap() {
        ApplicationTagLib g = BeanStore.getApplicationTagLib()
        [
            license :       g.message(code: 'license.label'),
            org :           g.message(code: 'org.label'),
            provider :      g.message(code: 'provider.label'),
            subscription :  g.message(code: 'subscription.label'),
            surveyConfig :  g.message(code: 'survey.label'),
            tipp :          g.message(code: 'title.label'),
            vendor :        g.message(code: 'vendor.label')
        ].sort{ it.value } as Map<String, String>
    }
}
