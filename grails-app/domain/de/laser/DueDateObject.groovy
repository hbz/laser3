package de.laser

/**
 * This is the linking table from a {@link DashboardDueDate} to the connected object; stored are the internationalised attribute value and names for the reminders to be sent out or posted
 * @see DashboardDueDate
 * @see Subscription
 * @see de.laser.base.AbstractPropertyWithCalculatedLastUpdated
 * @see Task
 * @see SurveyInfo
 */
class DueDateObject {

    String attribute_name
    String attribute_value_de
    String attribute_value_en
    Date date
    /**
     * {@link Subscription}, {@link de.laser.base.AbstractPropertyWithCalculatedLastUpdated}, {@link Task} or {@link SurveyInfo}
     */
    String oid
    boolean isDone = false
    Date lastUpdated
    Date dateCreated

    /**
     * Shorthand constructor call which supplies current time stamp for dateCreated and lastUpdated
     * @param attribute_value_de the value according German locale
     * @param attribute_value_en the value according English locale
     * @param attribute_name the attribute name
     * @param date the due date to be kept track
     * @param object the object about which reminder should be kept
     * @param isDone is the task done?
     */
    DueDateObject(attribute_value_de, attribute_value_en, attribute_name, date, object, isDone){
        this(attribute_value_de, attribute_value_en, attribute_name, date, object, isDone, new Date(), new Date())
    }

    /**
     * Constructor call for a new due date object connection
     * @param attribute_value_de the value according German locale
     * @param attribute_value_en the value according English locale
     * @param attribute_name the attribute name
     * @param date the due date to be kept track
     * @param object the object about which reminder should be kept
     * @param isDone is the task done?
     * @param dateCreated time stamp to retain the connection's creation date
     * @param lastUpdated time stamp to retain the connection's last modification date
     */
    DueDateObject(attribute_value_de, attribute_value_en, attribute_name, date, object, isDone, dateCreated, lastUpdated){
        this.attribute_value_de = attribute_value_de
        this.attribute_value_en = attribute_value_en
        this.attribute_name = attribute_name
        this.date = date
        this.oid = "${object.class.name}:${object.id}"
        this.isDone = isDone
        this.dateCreated = dateCreated
        this.lastUpdated = lastUpdated
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
        dateCreated             column: 'ddo_date_created'
        lastUpdated             column: 'ddo_last_updated'
        autoTimestamp true
    }

    static constraints = {
//        attribute_value_de      (nullable:false, blank:false)
//        attribute_value_en      (nullable:false, blank:false)
//        attribute_name          (nullable:false, blank:false)
        oid                     (blank:false)//, unique: ['attribut_name', 'ddo_oid'])
        dateCreated             (nullable:true)
        lastUpdated             (nullable:true)
    }

}
