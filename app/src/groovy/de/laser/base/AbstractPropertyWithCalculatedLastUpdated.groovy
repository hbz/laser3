package de.laser.base

import de.laser.RefdataValue
import de.laser.helper.DateUtil
import de.laser.interfaces.CalculatedLastUpdated
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import javax.persistence.Transient

abstract class AbstractPropertyWithCalculatedLastUpdated
        implements CalculatedLastUpdated, Serializable {

    //@Autowired
    def cascadingUpdateService // DO NOT OVERRIDE IN SUB CLASSES

    static Log static_logger = LogFactory.getLog(AbstractPropertyWithCalculatedLastUpdated)

    /* grails-3-fix
    abstract PropertyDefinition type

    abstract String           stringValue
    abstract Integer          intValue
    abstract BigDecimal       decValue
    abstract RefdataValue     refValue
    abstract URL              urlValue
    abstract String           note = ""
    abstract Date             dateValue
    abstract Org              tenant

    abstract boolean isPublic = false

    abstract Date dateCreated
    abstract Date lastUpdated
    abstract Date lastUpdatedCascading
    */

    protected void beforeInsertHandler() {
        static_logger.debug("beforeInsertHandler()")
    }

    protected void afterInsertHandler() {
        static_logger.debug("afterInsertHandler()")

        cascadingUpdateService.update(this, dateCreated)
    }

    protected Map<String, Object> beforeUpdateHandler() {

        Map<String, Object> changes = [
                oldMap: [:],
                newMap: [:]
        ]
        this.getDirtyPropertyNames().each { prop ->
            changes.oldMap.put( prop, this.getPersistentValue(prop) )
            changes.newMap.put( prop, this.getProperty(prop) )
        }

        static_logger.debug("beforeUpdateHandler() " + changes.toMapString())
        return changes
    }

    protected void afterUpdateHandler() {
        static_logger.debug("afterUpdateHandler()")

        cascadingUpdateService.update(this, lastUpdated)
    }

    protected void beforeDeleteHandler() {
        static_logger.debug("beforeDeleteHandler()")
    }

    protected void afterDeleteHandler() {
        static_logger.debug("afterDeleteHandler()")

        cascadingUpdateService.update(this, new Date())
    }

    abstract def beforeInsert()     /* { beforeInsertHandler() } */

    abstract def afterInsert()      /* { afterInsertHandler() } */

    abstract def beforeUpdate()     /* { beforeUpdateHandler() } */

    abstract def afterUpdate()      /* { afterUpdateHandler() } */

    abstract def beforeDelete()     /* { beforeDeleteHandler() } */

    abstract def afterDelete()      /* { afterDeleteHandler() } */

    Date _getCalculatedLastUpdated() {
        (lastUpdatedCascading > lastUpdated) ? lastUpdatedCascading : lastUpdated
    }

    @Transient
    String getValueType(){
        if (stringValue) { return "stringValue" }
        if (intValue)    { return "intValue" }
        if (decValue)    { return "decValue" }
        if (refValue)    { return "refValue" }
        if (dateValue)   { return "dateValue" }
        if (urlValue)    { return "urlValue" }
    }

    String getValue() {
        return toString()
    }

    @Override
    String toString(){
        if (stringValue)      { return stringValue }
        if (intValue != null) { return intValue.toString() }
        if (decValue != null) { return decValue.toString() }
        if (refValue)         { return refValue.toString() }
        if (dateValue)        { return dateValue.getDateString() }
        if (urlValue)         { return urlValue.toString() }
    }

    def copyInto(AbstractPropertyWithCalculatedLastUpdated newProp){
        if (type != newProp.type) {
            throw new IllegalArgumentException("AbstractProperty.copyInto nicht möglich, weil die Typen nicht übereinstimmen.")
        }
        else {
            newProp.stringValue = stringValue
            newProp.intValue = intValue
            newProp.decValue = decValue
            newProp.refValue = refValue
            newProp.dateValue = dateValue
            newProp.urlValue = urlValue
            newProp.note = note
        }
        newProp
    }

    def static parseValue(value, type){
        def result
        static_logger.debug( value + " << " + type )

        switch (type){
            case Integer.toString():
                result = Integer.parseInt(value)
                break
            case String.toString():
                result = value
                break
            case BigDecimal.toString():
                result = new BigDecimal(value)
                break
            case org.codehaus.groovy.runtime.NullObject.toString():
                result = null
                break
            case Date.toString():
                result = DateUtil.toDate_NoTime(value)
                break
            case URL.toString():
                result = new URL(value)
                break
            default:
                result = "AbstractProperty.parseValue failed"
        }
        return result
    }

    def setValue(value, type, rdc) {

        if (type == Integer.toString()) {
            intValue = parseValue(value, type)
        }
        else if (type == BigDecimal.toString()) {
            decValue = parseValue(value, type)
        }
        else if (type == String.toString()) {
            stringValue = parseValue(value, type)
        }
        else if (type == Date.toString()) {
            dateValue = parseValue(value, type)
        }
        else if (type == RefdataValue.toString()) {
            refValue = RefdataValue.getByValueAndCategory(value.toString(), rdc)
        }
        else if (type == URL.toString()) {
            urlValue = parseValue(value, type)
        }
    }
}
