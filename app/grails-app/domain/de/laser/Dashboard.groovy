package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.Task
import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.auth.User
import groovy.util.logging.Log4j



@Log4j
class Dashboard {
    String attribut
    Date manualCancellationDate
    Date endDate
    /** Subscription, CustomProperty, PrivateProperty oder Task*/
    String oid
    User responsibleUser
    Org  responsibleOrg
//    Timestamp lastUpdated

    Dashboard(Subscription obj, boolean isManualCancellationDate, User responsibleUser, Org responsibleOrg){
        this(
//                isManualCancellationDate? 'myinst.dash.due_date.noticePeriod.label' : 'myinst.dash.due_date.enddate.label',
                isManualCancellationDate? 'Kündigungsdatum' : 'Enddatum',
                isManualCancellationDate? obj.manualCancellationDate : null,
                isManualCancellationDate? null : obj.endDate,
                obj,
                null,
                responsibleOrg)
    }
    Dashboard(AbstractProperty obj, User responsibleUser, Org responsibleOrg){
        this(obj.type?.name?: obj.class.simpleName, null, obj.dateValue, obj, null, responsibleOrg)
    }
    Dashboard(Task obj, User responsibleUser, Org responsibleOrg){
//        this('myinst.dash.due_date.task.label', null, obj.endDate, obj, responsibleUser, responsibleOrg)
        this('Fälligkeitsdatum', null, obj.endDate, obj, obj.responsibleUser, obj.responsibleOrg)
    }
    private Dashboard(attribut, manualCancellationDate, endDate, object, responsibleUser, responsibleOrg){
        this.attribut = attribut
        this.manualCancellationDate = manualCancellationDate
        this.endDate = endDate
        this.oid = "${object.class.name}:${object.id}"
        this.responsibleUser = responsibleUser
        this.responsibleOrg = responsibleOrg
    }
    static Dashboard[] create(){

    }

    static mapping = {
        id                      column: 'das_id'
        attribut                column: 'das_attribut'
        manualCancellationDate  column: 'das_manualcancellationdate'
        endDate                 column: 'das_enddate'//, index:  'das_enddate_idx'
        oid                     column: 'das_oid'
        responsibleUser         column: 'das_responsible_user_fk', index: 'das_responsible_user_fk_idx'
        responsibleOrg          column: 'das_responsible_org_fk', index:  'das_responsible_org_fk_idx'
//        lastUpdated              column: 'das_lastupdated', autoTimestamp           true
    }

    static constraints = {
        attribut                (nullable:false, blank:false)
        manualCancellationDate  (nullable:true, blank:true)
        endDate                 (nullable:true, blank:true)
        oid                     (nullable:false, blank:false)
        responsibleUser         (nullable:true, blank:true)
        responsibleOrg          (nullable:true, blank:true)
    }
}
