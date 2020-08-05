package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.helper.RDConstants
import de.laser.helper.RefdataAnnotation

import javax.persistence.Transient

class Task {

    @Transient
    def deletionService

    License         license
    Org             org
    Package         pkg
    Subscription    subscription
    SurveyConfig    surveyConfig

    String          title
    String          description

    @RefdataAnnotation(cat = RDConstants.TASK_STATUS)
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
        license         (nullable:true, blank:false)
        org             (nullable:true, blank:false)
        pkg             (nullable:true, blank:false)
        subscription    (nullable:true, blank:false)
        surveyConfig      (nullable:true, blank:false)
        title           (blank:false)
        description     (nullable:true, blank:true)
        status          (blank:false)
        creator         (blank:false)
        endDate         (blank:false)
        systemCreateDate (blank:false)
        createDate      (blank:false)
        responsibleUser (nullable:true,  blank:true)
        responsibleOrg  (nullable:true,  blank:true)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }

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
        deletionService.deleteDocumentFromIndex(this.getClass().getSimpleName().toLowerCase()+":"+this.id)
    }

    def getObjects() {
        def result = []

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
}
