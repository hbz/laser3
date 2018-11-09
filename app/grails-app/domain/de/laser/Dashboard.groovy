package de.laser

import com.k_int.kbplus.GenericOIDService
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.Task
import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.auth.User
import groovy.util.logging.Log4j


@Log4j
class Dashboard {
    Date manualCancellationDate
    Date endDate
    /** Subscription, CustomProperty, PrivateProperty oder Task*/
    String oid
    User responsibleUser
    Org  responsibleOrg

    Dashboard(Subscription obj, User responsibleUser, Org responsibleOrg){
        this(obj.manualCancellationDate, obj.endDate, obj, null, responsibleOrg)
    }
    Dashboard(AbstractProperty obj, User responsibleUser, Org responsibleOrg){
        this(null, obj.dateValue, obj, null, responsibleOrg)
    }
    Dashboard(Task obj, User responsibleUser, Org responsibleOrg){
        this(null, obj.endDate, obj, responsibleUser, responsibleOrg)
    }
    private Dashboard(manualCancellationDate, endDate, object, responsibleUser, responsibleOrg){
        this.manualCancellationDate = manualCancellationDate
        this.endDate = endDate
        this.oid = "${object.class.name}:${object.id}"
        this.responsibleUser = responsibleUser
        this.responsibleOrg = responsibleOrg
    }

    static mapping = {
        id                      column: 'das_id'
        manualCancellationDate  column: 'das_manualcancellationdate'
        endDate                 column: 'das_enddate'
//        endDate                 index:  'das_enddate_idx'
        oid                     column: 'das_oid'
        responsibleUser         column: 'das_responsible_user_fk'
        responsibleOrg          column: 'das_responsible_org_fk'
//        responsibleOrg          index:  'das_responsible_org_fk_idx'
//        responsibleUser         index:  'das_responsible_user_fk_idx'
    }

    static constraints = {
        endDate         (nullable:false, blank:false)
        oid             (nullable:false, blank:false)
    }

}
