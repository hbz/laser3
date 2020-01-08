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
    String attribute_value_de
    String attribute_value_en
    String attribute_name
    Date date
    /** Subscription, CustomProperty, PrivateProperty oder Task*/
    String oid
    User responsibleUser
    Org  responsibleOrg
    boolean isDone
    boolean isHidden
    Date lastUpdated

    Date dateCreated

    DashboardDueDate(messageSource, Subscription obj, boolean isManualCancellationDate, User responsibleUser, Org responsibleOrg, boolean isDone, boolean isHidden){
        this(
                isManualCancellationDate? messageSource.getMessage('dashboardDueDate.subscription.manualCancellationDate', null, Locale.GERMAN) :
                        messageSource.getMessage('dashboardDueDate.subscription.endDate', null, Locale.GERMAN),
                isManualCancellationDate? messageSource.getMessage('dashboardDueDate.subscription.manualCancellationDate', null, Locale.ENGLISH) :
                        messageSource.getMessage('dashboardDueDate.subscription.endDate', null, Locale.ENGLISH),
                isManualCancellationDate? 'manualCancellationDate' : 'endDate',
                isManualCancellationDate? obj.manualCancellationDate : obj.endDate,
                obj,
                responsibleUser,
                responsibleOrg,
                isDone,
                isHidden
        )
    }
    DashboardDueDate(messageSource, AbstractProperty obj, User responsibleUser, Org responsibleOrg, boolean isDone, boolean isHidden){
        this(
                obj.type.getI10n('name', Locale.GERMAN) ?: obj.type.getI10n('name', Locale.ENGLISH),
                obj.type.getI10n('name', Locale.ENGLISH) ?: obj.type.getI10n('name', Locale.GERMAN),
                'type.name',
                obj.dateValue, obj, responsibleUser, responsibleOrg, isDone, isHidden)
    }
    DashboardDueDate(messageSource, Task obj, User responsibleUser, Org responsibleOrg, boolean isDone, boolean isHidden){
        this(   messageSource.getMessage('dashboardDueDate.task.endDate', null, Locale.GERMAN),
                messageSource.getMessage('dashboardDueDate.task.endDate', null, Locale.ENGLISH),
                'endDate',
                obj.endDate, obj, responsibleUser, responsibleOrg, isDone, isHidden)
    }
    DashboardDueDate(messageSource, SurveyInfo obj, User responsibleUser, Org responsibleOrg, boolean isDone, boolean isHidden){
        this(   messageSource.getMessage('dashboardDueDate.surveyInfo.endDate', null, Locale.GERMAN),
                messageSource.getMessage('dashboardDueDate.surveyInfo.endDate', null, Locale.ENGLISH),
                'endDate',
                obj.endDate, obj, responsibleUser, responsibleOrg, isDone, isHidden)
    }
    private DashboardDueDate(attribute_value_de, attribute_value_en, attribute_name, date, object, responsibleUser, responsibleOrg, isDone, isHidden){
        this.attribute_value_de = attribute_value_de
        this.attribute_value_en = attribute_value_en
        this.attribute_name = attribute_name
        this.date = date
        this.oid = "${object.class.name}:${object.id}"
        this.responsibleUser = responsibleUser
        this.responsibleOrg = responsibleOrg
        this.isDone = isDone
        this.isHidden = isHidden
    }

    static mapping = {
        id                      column: 'das_id'
        version                 column: 'das_version'
        attribute_value_de      column: 'das_attribute_value_de'
        attribute_value_en      column: 'das_attribute_value_en'
        attribute_name          column: 'das_attribute_name'
        date                    column: 'das_date'
        lastUpdated             column: 'das_last_updated'
        oid                     column: 'das_oid'
        responsibleUser         column: 'das_responsible_user_fk', index: 'das_responsible_user_fk_idx'
        responsibleOrg          column: 'das_responsible_org_fk',  index: 'das_responsible_org_fk_idx'
        isDone                  column: 'das_is_done'
        isHidden                column: 'das_is_hidden'
        dateCreated             column: 'das_date_created'
        autoTimestamp true
    }

    static constraints = {
//        attribute_value_de      (nullable:false, blank:false)
//        attribute_value_en      (nullable:false, blank:false)
//        attribute_name          (nullable:false, blank:false)
        date                    (nullable:false, blank:false)
        oid                     (nullable:false, blank:false)//, unique: ['attribut_name', 'das_oid', 'das_responsibleOrg', 'das_responsibleUser'])
        responsibleUser         (nullable:true, blank:false)
        responsibleOrg          (nullable:true, blank:false)
        isDone                  (nullable:false, blank:false)
        isHidden                (nullable:false, blank:false)
        lastUpdated             (nullable:true, blank:false)
        dateCreated (nullable: true, blank: false)
    }

    private static String getPropertyValue(String messageKey) {
        def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
        String value = messageSource.getMessage(messageKey, null, locale)
        value
    }

}
