package de.laser

import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.survey.SurveyInfo
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * This is the linking table from a {@link DashboardDueDate} to the connected object; stored are the internationalised attribute value and names for the reminders to be sent out or posted
 * @see DashboardDueDate
 * @see Subscription
 * @see de.laser.base.AbstractPropertyWithCalculatedLastUpdated
 * @see Task
 * @see de.laser.survey.SurveyInfo
 */
class DueDateObject {

    String attribute_name
    String attribute_value_de
    String attribute_value_en
    Date date
    /**
     * {@link Subscription}, {@link de.laser.base.AbstractPropertyWithCalculatedLastUpdated}, {@link Task} or {@link de.laser.survey.SurveyInfo}
     */
    String oid
    boolean isDone = false

    License         license
    Subscription    subscription
    SurveyInfo      surveyInfo
    Task            task
    String          propertyOID // TODO

    Date lastUpdated
    Date dateCreated

    /**
     * Constructor call for a new due date object connection
     * @param attribute_value_de the value according German locale
     * @param attribute_value_en the value according English locale
     * @param attribute_name the attribute name
     * @param date the due date to be kept track
     * @param object the object about which reminder should be kept
     * @param isDone is the task done?
     * @param now time stamp to retain the connection's creation date and last modification date
     */
    DueDateObject(String attribute_value_de, String attribute_value_en, String attribute_name, Date date, def object, boolean isDone, Date now){
        object = GrailsHibernateUtil.unwrapIfProxy(object)

        this.attribute_value_de = attribute_value_de
        this.attribute_value_en = attribute_value_en
        this.attribute_name = attribute_name
        this.date = date
        this.oid = "${object.class.name}:${object.id}"
        this.isDone = isDone
        this.dateCreated = now
        this.lastUpdated = now

        // TODO: ERMS-5862
//        if(object instanceof License) {
//            this.license = object
//        }
//        else if(object instanceof Subscription) {
//            this.subscription = object
//        }
//        else if(object instanceof SurveyInfo) {
//            this.surveyInfo = object
//        }
//        else if(object instanceof Task) {
//            this.task = object
//        }
//        else if (object instanceof AbstractPropertyWithCalculatedLastUpdated) {
//            this.oid = "${object.class.name}:${object.id}"
//        }
//        else {
//            log.warn 'DueDateObject.create( ' + object + ' ) FAILED'
//        }
    }

    static mapping = {
        id                      column: 'ddo_id'
        version                 column: 'ddo_version'
        attribute_name          column: 'ddo_attribute_name'
        attribute_value_de      column: 'ddo_attribute_value_de'
        attribute_value_en      column: 'ddo_attribute_value_en'
        date                    column: 'ddo_date'
        oid                     column: 'ddo_oid'
        isDone                  column: 'ddo_is_done'
        license                 column: 'ddo_license_fk'
        subscription            column: 'ddo_subscription_fk'
        surveyInfo              column: 'ddo_survey_info_fk'
        task                    column: 'ddo_task_fk'
        propertyOID             column: 'ddo_property_oid'
        dateCreated             column: 'ddo_date_created'
        lastUpdated             column: 'ddo_last_updated'
    }

    static constraints = {
//        attribute_value_de      (nullable:false, blank:false)
//        attribute_value_en      (nullable:false, blank:false)
//        attribute_name          (nullable:false, blank:false)
        oid                     (blank:false)//, unique: ['attribut_name', 'ddo_oid'])
        dateCreated             (nullable:true)
        lastUpdated             (nullable:true)

        license                 (nullable:true)
        subscription            (nullable:true)
        surveyInfo              (nullable:true)
        task                    (nullable:true)
        propertyOID             (nullable:true)
    }

    static DueDateObject getDueDateObject(def object, String attribute_name) {
        return DueDateObject.findWhere(oid: "${object.class.name}:${object.id}", attribute_name: attribute_name)

        // TODO: ERMS-5862
//        object = GrailsHibernateUtil.unwrapIfProxy(object)
//
//        if(object instanceof License) {
//            DueDateObject.findWhere(license: object, attribute_name: attribute_name)
//        }
//        else if(object instanceof Subscription) {
//            DueDateObject.findWhere(subscription: object, attribute_name: attribute_name)
//        }
//        else if(object instanceof SurveyInfo) {
//            DueDateObject.findWhere(surveyInfo: object, attribute_name: attribute_name)
//        }
//        else if(object instanceof Task) {
//            DueDateObject.findWhere(task: object, attribute_name: attribute_name)
//        }
//        else if (object instanceof AbstractPropertyWithCalculatedLastUpdated) {
//            DueDateObject.findWhere(oid: "${object.class.name}:${object.id}", attribute_name: attribute_name)
//        }
//        else {
//            log.warn 'DueDateObject.getDueDateObject( ' + object + ', ' + attribute_name + ' ) FAILED'
//        }
    }
}
