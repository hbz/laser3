package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.SurveyInfo
import com.k_int.kbplus.Task
import com.k_int.kbplus.UserSettings
import com.k_int.kbplus.abstract_domain.AbstractPropertyWithCalculatedLastUpdated
import com.k_int.kbplus.auth.User
import de.laser.helper.SqlDateUtils
import groovy.util.logging.Log4j

import static com.k_int.kbplus.UserSettings.DEFAULT_REMINDER_PERIOD

@Log4j
class DashboardDueDate {
    User responsibleUser
    Org  responsibleOrg
    boolean isHidden = false
    DueDateObject dueDateObject
    Date dateCreated
    Date lastUpdated

    DashboardDueDate(messageSource, obj, User responsibleUser, Org responsibleOrg, boolean isDone, boolean isHidden){
        this(   getAttributeValue(messageSource, obj, responsibleUser, Locale.GERMAN),
                getAttributeValue(messageSource, obj, responsibleUser, Locale.ENGLISH),
                getAttributeName(obj, responsibleUser),
                getDate(obj, responsibleUser),
                obj,
                responsibleUser,
                responsibleOrg,
                isDone,
                isHidden
        )
    }

    void update(messageSource, obj){
        Date now = new Date()
        this.version = this.version +1
        this.lastUpdated = now
        this.dueDateObject.version = this.dueDateObject.version +1
        this.dueDateObject.lastUpdated = now
        this.dueDateObject.attribute_value_de = getAttributeValue(messageSource, obj, responsibleUser,
                Locale.GERMAN)
        this.dueDateObject.attribute_value_en = getAttributeValue(messageSource, obj, responsibleUser,
                Locale.ENGLISH)
        this.dueDateObject.date = getDate(obj, responsibleUser)
        this.dueDateObject.save()
        this.save()
    }

    static Date getDate(obj, user){
        if (obj instanceof AbstractPropertyWithCalculatedLastUpdated)            return obj.dateValue
        if (obj instanceof Task)                        return obj.endDate
        if (obj instanceof SurveyInfo)                  return obj.endDate
        if (obj instanceof Subscription){
            if (isManualCancellationDate(obj, user))    return obj.manualCancellationDate
            else                                        return obj.endDate
        }
    }

    static String getAttributeValue(messageSource, obj, User user, Locale locale){
        if (obj instanceof AbstractPropertyWithCalculatedLastUpdated)            return obj.type.getI10n('name', locale)
        if (obj instanceof Task)                        return messageSource.getMessage('dashboardDueDate.task.endDate', null, locale)
        if (obj instanceof SurveyInfo)                  return messageSource.getMessage('dashboardDueDate.surveyInfo.endDate', null, locale)
        if (obj instanceof Subscription){
            if (isManualCancellationDate(obj, user))    return messageSource.getMessage('dashboardDueDate.subscription.manualCancellationDate', null, locale)
            else                                        return messageSource.getMessage('dashboardDueDate.subscription.endDate', null, locale)
        }
    }
    static String getAttributeName(obj, user){
        if (obj instanceof AbstractPropertyWithCalculatedLastUpdated)            return 'type.name'
        if (obj instanceof Task)                        return 'endDate'
        if (obj instanceof SurveyInfo)                  return 'endDate'
        if (obj instanceof Subscription){
            if (isManualCancellationDate(obj, user))    return 'manualCancellationDate'
            else                                        return 'endDate'
        }
    }
    static isManualCancellationDate(obj, user){
        int reminderPeriodForManualCancellationDate = user.getSetting(UserSettings.KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD, DEFAULT_REMINDER_PERIOD).value ?: 1
        return (obj.manualCancellationDate && SqlDateUtils.isDateBetweenTodayAndReminderPeriod(obj.manualCancellationDate, reminderPeriodForManualCancellationDate))
    }
    private DashboardDueDate(attribute_value_de, attribute_value_en, attribute_name, date, object, responsibleUser, responsibleOrg, isDone, isHidden){
        Date now = new Date()
        this.responsibleUser = responsibleUser
        this.responsibleOrg = responsibleOrg
        this.isHidden = isHidden
        this.dateCreated = now
        this.lastUpdated = now

        DueDateObject ddo = DueDateObject.findWhere(oid: "${object.class.name}:${object.id}", attribute_name: attribute_name)
        if ( ! ddo ) {
            ddo = new DueDateObject(attribute_value_de, attribute_value_en, attribute_name, date, object, isDone, now, now)
            ddo.save()
        }
        this.dueDateObject = ddo
        this.save()
    }

    static mapping = {
        id                      column: 'das_id'
        version                 column: 'das_version'
        responsibleUser         column: 'das_responsible_user_fk', index: 'das_responsible_user_fk_idx'
        responsibleOrg          column: 'das_responsible_org_fk',  index: 'das_responsible_org_fk_idx'
        isHidden                column: 'das_is_hidden'
        dueDateObject           (column: 'das_ddobj_fk',  lazy: false)
        dateCreated             column: 'das_date_created'
        lastUpdated             column: 'das_last_updated'
        autoTimestamp true
    }

    static constraints = {
        responsibleUser         (nullable:true, blank:false)
        responsibleOrg          (nullable:true, blank:false)
        isHidden                (nullable:false, blank:false)
        dueDateObject           (nullable:true, blank:false)
        dateCreated             (nullable: true, blank: false)
        lastUpdated             (nullable:true, blank:false)
    }
}
