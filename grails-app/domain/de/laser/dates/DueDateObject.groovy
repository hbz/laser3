package de.laser.dates

import de.laser.License
import de.laser.Org
import de.laser.Subscription
import de.laser.Task
import de.laser.storage.BeanStore
import de.laser.survey.SurveyInfo
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * This is the linking table from a {@link DashboardDueDate} to the connected object; stored are the internationalised attribute value and names for the reminders to be sent out or posted
 * @see DashboardDueDate
 * @see Subscription
 * @see de.laser.base.AbstractPropertyWithCalculatedLastUpdated
 * @see Task
 * @see de.laser.survey.SurveyInfo
 */
@Slf4j
class DueDateObject {

    String attribute_name
    String attribute_value_de
    String attribute_value_en
    Date date
    /**
     * {@link de.laser.Subscription}, {@link de.laser.base.AbstractPropertyWithCalculatedLastUpdated}, {@link de.laser.Task} or {@link de.laser.survey.SurveyInfo}
     */
    @Deprecated
    String oid
    boolean isDone = false

    License         license
    Org             org
    Provider        provider
    Subscription    subscription
    SurveyInfo      surveyInfo
    Task            task
    Vendor          vendor
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
    DueDateObject(String attribute_value_de, String attribute_value_en, String attribute_name, Date date, def object, Date now){
        object = GrailsHibernateUtil.unwrapIfProxy(object)

        this.attribute_value_de = attribute_value_de
        this.attribute_value_en = attribute_value_en
        this.attribute_name = attribute_name
        this.date = date
        this.oid = "${object.class.name}:${object.id}"
        // this.isDone = false // TODO
        this.dateCreated = now
        this.lastUpdated = now

        // TODO: ERMS-5862
//        String propName = object.getClass().simpleName.uncapitalize()
//        if (this.hasProperty(propName) && this.hasProperty(propName).getType().getCanonicalName() == object.getClass().getCanonicalName()) {
//            this.setProperty(propName, object)
//        }
//        else {
//            if (object instanceof AbstractPropertyWithCalculatedLastUpdated) {
//                this.propertyOID = "${object.class.name}:${object.id}"
//            }
//            else {
//                log.warn 'DueDateObject.create( ' + object + ' ) > new matching strategy failed'
//            }
//        }
    }

//    static belongsTo = [
//            license: License,
//            org: Org,
//            provider: Provider,
//            subscription: Subscription,
//            surveyInfo: SurveyInfo,
//            task: Task,
//            vendor: Vendor
//    ]

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
        org                     column: 'ddo_org_fk'
        provider                column: 'ddo_provider_fk'
        subscription            column: 'ddo_subscription_fk'
        surveyInfo              column: 'ddo_survey_info_fk'
        task                    column: 'ddo_task_fk'
        vendor                  column: 'ddo_vendor_fk'
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
        org                     (nullable:true)
        provider                (nullable:true)
        subscription            (nullable:true)
        surveyInfo              (nullable:true)
        task                    (nullable:true)
        vendor                  (nullable:true)
        propertyOID             (nullable:true)
    }

    String getOID() {
        BeanStore.getGenericOIDService().getOID(this)
    }

    def getObject() { // TODO ERMS-5862

             if (this.license)      { this.license }
        else if (this.org)          { this.org }
        else if (this.provider)     { this.provider }
        else if (this.subscription) { this.subscription }
        else if (this.surveyInfo)   { this.surveyInfo }
        else if (this.task)         { this.task }
        else if (this.vendor)       { this.vendor }
        else if (this.propertyOID)  { BeanStore.getGenericOIDService().resolveOID(propertyOID) }
        else if (this.oid)          { BeanStore.getGenericOIDService().resolveOID(oid) }            // FALLBACK
        else {
            log.warn 'DueDateObject.getObject( ' + this.id + ' ) FAILED'
        }
    }

    static DueDateObject getByObjectAndAttributeName(def object, String attribute_name) {
        return DueDateObject.findWhere(oid: "${object.class.name}:${object.id}", attribute_name: attribute_name)

        // TODO: ERMS-5862
//        DueDateObject ddo
//
//        String propName = object.getClass().simpleName.uncapitalize()
//        if (this.hasProperty(propName) && this.hasProperty(propName).getType().getCanonicalName() == object.getClass().getCanonicalName()) {
//            ddo = DueDateObject.findWhere("${propName}": object, attribute_name: attribute_name)
//        }
//        else {
//            if (object instanceof AbstractPropertyWithCalculatedLastUpdated) {
//                ddo = DueDateObject.findWhere(propertyOID: "${object.class.name}:${object.id}", attribute_name: attribute_name)
//            }
//            else {
//                ddo = DueDateObject.findWhere(oid: "${object.class.name}:${object.id}", attribute_name: attribute_name)   // FALLBACK
//            }
//        }
//        return ddo
    }
}
