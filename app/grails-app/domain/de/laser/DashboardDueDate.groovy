package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.SurveyInfo
import com.k_int.kbplus.Task
import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.auth.User
import grails.util.Holders
import groovy.util.logging.Log4j

import java.sql.Timestamp


@Log4j
class DashboardDueDate {
    String attribut
    Date date
    /** Subscription, CustomProperty, PrivateProperty oder Task*/
    String oid
    User responsibleUser
    Org  responsibleOrg
    boolean isDone
    boolean isHidden
    Timestamp lastUpdated

    DashboardDueDate(Subscription obj, boolean isManualCancellationDate, User responsibleUser, Org responsibleOrg, boolean isDone, boolean isHidden){
        this(
                isManualCancellationDate? 'Kündigungsdatum' : 'Enddatum',
                isManualCancellationDate? obj.manualCancellationDate : obj.endDate,
                obj,
                responsibleUser,
                responsibleOrg,
                isDone,
                isHidden
        )
    }
    DashboardDueDate(AbstractProperty obj, User responsibleUser, Org responsibleOrg, boolean isDone, boolean isHidden){
        this(obj.type?.name?: obj.class.simpleName, obj.dateValue, obj, responsibleUser, responsibleOrg, isDone, isHidden)
    }
    DashboardDueDate(Task obj, User responsibleUser, Org responsibleOrg, boolean isDone, boolean isHidden){
        this('Fälligkeitsdatum', obj.endDate, obj, responsibleUser, responsibleOrg, isDone, isHidden)
    }
    DashboardDueDate(SurveyInfo obj, User responsibleUser, Org responsibleOrg, boolean isDone, boolean isHidden){
        this('Enddatum', obj.endDate, obj, responsibleUser, responsibleOrg, isDone, isHidden)
    }
    private DashboardDueDate(attribut, date, object, responsibleUser, responsibleOrg, isDone, isHidden){
        this.attribut = attribut
        this.date = date
        this.oid = "${object.class.name}:${object.id}"
        this.responsibleUser = responsibleUser
        this.responsibleOrg = responsibleOrg
        this.isDone = isDone
        this.isHidden = isHidden
    }

    static mapping = {
        id                      column: 'das_id'
        attribut                column: 'das_attribut'
        date                    column: 'das_date'
        oid                     column: 'das_oid'
        responsibleUser         column: 'das_responsible_user_fk', index: 'das_responsible_user_fk_idx'
        responsibleOrg          column: 'das_responsible_org_fk',  index: 'das_responsible_org_fk_idx'
        isDone                  column: 'das_is_done'
        isHidden                  column: 'das_is_hidden'
        autoTimestamp true
    }

    static constraints = {
        attribut                (nullable:false, blank:false)
        date                    (nullable:false, blank:false)
        oid                     (nullable:false, blank:false)//,unique: ['date', 'oid', 'responsibleOrg'])
        responsibleUser         (nullable:true, blank:false)
        responsibleOrg          (nullable:true, blank:false)
        isDone                  (nullable:false, blank:false)
        isHidden                  (nullable:false, blank:false)
        lastUpdated             (nullable:true, blank:false)
    }

    private static String getPropertyValue(String messageKey) {
        def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        def locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
        String value = messageSource.getMessage(messageKey, null, locale)
        value
    }

}
