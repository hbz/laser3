package com.k_int.kbplus

import com.k_int.kbplus.auth.User

class Task {

    License         license
    Org             org
    Package         pkg
    Subscription    subscription

    String          title
    String          description
    RefdataValue    status          // RefdataCategory 'YN'

    User            creator
    Date            endDate
    Date            createDate

    User            responsibleUser
    Org             responsibleOrg

    static constraints = {
        license         (nullable:true, blank:false)
        org             (nullable:true, blank:false)
        pkg             (nullable:true, blank:false)
        subscription    (nullable:true, blank:false)
        title           (nullable:false, blank:false)
        description     (nullable:true, blank:true)
        status          (nullable:false, blank:false)
        creator         (nullable:false, blank:false)
        endDate         (nullable:false, blank:false)
        createDate      (nullable:false, blank:false)
        responsibleUser (nullable:true,  blank:true)
        responsibleOrg  (nullable:true,  blank:true)
    }

    static mapping = {
        id              column:'tsk_id'
        version         column:'tsk_version'

        license         column:'tsk_lic_fk'
        org             column:'tsk_org_fk'
        pkg             column:'tsk_pkg_fk'
        subscription    column:'tsk_sub_fk'

        title           column:'tsk_title'
        description     column:'tsk_description'
        status          column:'tsk_status_rdv_fk'

        creator         column:'tsk_creator_fk'
        endDate         column:'tsk_end_date'
        createDate      column:'tsk_create_date'

        responsibleUser      column:'tsk_responsible_user_fk'
        responsibleOrg       column:'tsk_responsible_org_fk'
    }
}
