package de.laser

import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.storage.BeanStore
import de.laser.survey.SurveyInfo
import de.laser.utils.SqlDateUtils
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.context.MessageSource

/**
 * Represents a dashboard reminder for a user's dashboard. They are initialised every day per cronjob; the object's parameters to remind about are stored in {@link DueDateObject}
 * @see DueDateObject
 * @see DashboardDueDatesService
 * @see de.laser.jobs.DashboardDueDatesJob
 */
@Slf4j
class DashboardDueDate {

    User responsibleUser
    DueDateObject dueDateObject
    boolean isHidden = false
    Date dateCreated
    Date lastUpdated

    /**
     * Refreshes the due date object
     * @param obj the object (of type {@link Subscription}, {@link AbstractPropertyWithCalculatedLastUpdated}, {@link Task} or {@link SurveyInfo} for which the reminder is set up
     */
    void update(def obj){
        withTransaction {
            Date now = new Date()
            this.version = this.version + 1
            this.lastUpdated = now
            this.dueDateObject.version = this.dueDateObject.version + 1
            this.dueDateObject.lastUpdated = now
            this.dueDateObject.attribute_value_de = DashboardDueDate.getAttributeValue(obj, responsibleUser, Locale.GERMAN)
            this.dueDateObject.attribute_value_en = DashboardDueDate.getAttributeValue(obj, responsibleUser, Locale.ENGLISH)

            Date date = DashboardDueDate.getDate(obj, responsibleUser)

            if(date != this.dueDateObject.date){
                this.dueDateObject.isDone = false
                this.isHidden = false
            }

            this.dueDateObject.date = date
            this.dueDateObject.save()
            this.save()
        }
    }

    /**
     * Gets the underlying date (= the date due) of the reminder
     * @param obj the object whose due date should be retrieved
     * @param user the {@link User} for which the date should be required; needed to compare whether the reminder time is already reached
     * @return the due date of the object which may be {@link de.laser.properties.SubscriptionProperty#dateValue} (counts as well for {@link de.laser.properties.LicenseProperty#dateValue}, {@link de.laser.properties.OrgProperty#dateValue},
     * {@link de.laser.properties.PlatformProperty#dateValue}, {@link de.laser.properties.PersonProperty#dateValue}), {@link Task#endDate}, {@link SurveyInfo#endDate}, {@link Subscription#manualCancellationDate} or {@link Subscription#endDate}
     */
    static Date getDate(def obj, User user){
        if (obj instanceof AbstractPropertyWithCalculatedLastUpdated)            return obj.dateValue
        if (obj instanceof Task)                        return obj.endDate
        if (obj instanceof SurveyInfo)                  return obj.endDate
        if (obj instanceof Subscription){
            if (isManualCancellationDate(obj, user))    return obj.manualCancellationDate
            else                                        return obj.endDate
        }
    }

    /**
     * Gets the localised value name of the property to be reminded about
     * @param obj the object for which the reminder is set up
     * @param user the {@link User} for which the date should be required; needed to compare whether the reminder time is already reached
     * @param locale the {@link Locale} to load the string in
     * @return the internationalised name of the property being reminded
     */
    static String getAttributeValue(def obj, User user, Locale locale){
        MessageSource messageSource = BeanStore.getMessageSource()

        if (obj instanceof AbstractPropertyWithCalculatedLastUpdated)            return obj.type.getI10n('name', locale)
        if (obj instanceof Task)                        return messageSource.getMessage('dashboardDueDate.task.endDate', null, locale)
        if (obj instanceof SurveyInfo)                  return messageSource.getMessage('dashboardDueDate.surveyInfo.endDate', null, locale)
        if (obj instanceof Subscription){
            if (isManualCancellationDate(obj, user))    return messageSource.getMessage('dashboardDueDate.subscription.manualCancellationDate', null, locale)
            else                                        return messageSource.getMessage('dashboardDueDate.subscription.endDate', null, locale)
        }
    }

    /**
     * Gets the raw name of the property to be reminded about
     * @param obj the object for which the reminder is set up
     * @param user the {@link User} for which the date should be required; needed to compare whether the reminder time is already reached
     * @return the property name
     */
    static String getAttributeName(def obj, User user){
        if (obj instanceof AbstractPropertyWithCalculatedLastUpdated)            return 'type.name'
        if (obj instanceof Task)                        return 'endDate'
        if (obj instanceof SurveyInfo)                  return 'endDate'
        if (obj instanceof Subscription){
            if (isManualCancellationDate(obj, user))    return 'manualCancellationDate'
            else                                        return 'endDate'
        }
    }

    /**
     * Checks if the user should be reminded about a {@link Subscription}'s manual cancellation date and if so, if the manual cancellation date is set and if it is in the reminder period of the {@link User}
     * @param obj the {@link Subscription} whose manual cancellation date should be verified
     * @param user the {@link User} whose setting and reminder period should be checked
     * @return does the subscription have a manual cancellation date and is this date between today and the reminder period?
     */
    static isManualCancellationDate(def obj, User user){
        int reminderPeriodForManualCancellationDate = user.getSetting(UserSetting.KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD, UserSetting.DEFAULT_REMINDER_PERIOD).value ?: 1
        return (obj.manualCancellationDate && SqlDateUtils.isDateBetweenTodayAndReminderPeriod(obj.manualCancellationDate, reminderPeriodForManualCancellationDate))
    }

    static DashboardDueDate getByObjectAndAttributeNameAndResponsibleUser(def object, String attributeName, User user) {

        // TODO: ERMS-5862

        DashboardDueDate ddd
        String query

        object = GrailsHibernateUtil.unwrapIfProxy(object)
        
             if (object instanceof License)     { query = 'license' }
        else if (object instanceof Org)         { query = 'org' }
        else if (object instanceof Provider)    { query = 'provider' }
        else if (object instanceof Subscription){ query = 'subscription' }
        else if (object instanceof SurveyInfo)  { query = 'surveyInfo' }
        else if (object instanceof Task)        { query = 'task' }
        else if (object instanceof Vendor)      { query = 'vendor' }
        else if (object instanceof AbstractPropertyWithCalculatedLastUpdated) {
                 query  = 'propertyOID'
                 object = "${object.class.name}:${object.id}"   // TODO
        }
        else {
                 query  = 'oid'
                 object = "${object.class.name}:${object.id}"   // FALLBACK
        }

        if (query && attributeName && user) {
            ddd = DashboardDueDate.executeQuery(
                    'select das from DashboardDueDate das join das.dueDateObject ddo ' +
                    'where das.responsibleUser = :user and ddo.' + query + ' = :obj and ddo.attribute_name = :attributeName ' +
                    'order by ddo.date',
                    [
                        user: user,
                        obj: object,
                        attributeName: attributeName,
                    ]
            )[0]
        }
        else {
            log.warn 'DashboardDueDate.getByObjectAndAttributeNameAndResponsibleUser( ' + object + ', ' + attributeName + ', ' + user + ' ) FAILED'
        }

        return ddd
    }

    /**
     * Sets up a new due date reminder with the given parameters
     * @param obj the object (of type {@link Subscription}, {@link AbstractPropertyWithCalculatedLastUpdated}, {@link Task} or {@link SurveyInfo} for which the reminder should be set up
     * @param responsibleUser the {@link User} who should be reminded
     */
    DashboardDueDate(def object, User responsibleUser){
        String attribute_value_de   = getAttributeValue(object, responsibleUser, Locale.GERMAN)
        String attribute_value_en   = getAttributeValue(object, responsibleUser, Locale.ENGLISH)
        String attribute_name       = getAttributeName(object, responsibleUser)
        Date date                   = getDate(object, responsibleUser)

        withTransaction {
            Date now = new Date()
            this.responsibleUser = responsibleUser
            // this.isHidden = false // TODO
            this.dateCreated = now
            this.lastUpdated = now

            DueDateObject ddo = DueDateObject.getByObjectAndAttributeName(object, attribute_name) // TODO: ERMS-5862

            if (!ddo) {
                ddo = new DueDateObject(attribute_value_de, attribute_value_en, attribute_name, date, object, now)
                ddo.save()
            }

            if(date != ddo.date){
                ddo.date = date
                ddo.isDone = false
                ddo.lastUpdated = now
                ddo.save()
            }

            this.dueDateObject = ddo
            this.save()
        }
    }

    String getOID() {
        BeanStore.getGenericOIDService().getOID(this)
    }

    static mapping = {
        id                      column: 'das_id'
        version                 column: 'das_version'
        responsibleUser         column: 'das_responsible_user_fk', index: 'das_responsible_user_idx'
        isHidden                column: 'das_is_hidden'
        dueDateObject           column: 'das_ddobj_fk', index: 'das_ddobj_idx'
        dateCreated             column: 'das_date_created'
        lastUpdated             column: 'das_last_updated'
    }

    static constraints = {
        responsibleUser         (nullable:true)
        dueDateObject           (nullable:true)
        dateCreated             (nullable:true)
        lastUpdated             (nullable:true)
    }
}
