package de.laser.base

import de.laser.Org
import de.laser.RefdataValue
import de.laser.helper.DateUtil
import de.laser.interfaces.CalculatedLastUpdated
import de.laser.properties.PropertyDefinition
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import javax.persistence.Transient

abstract class AbstractPropertyWithCalculatedLastUpdated
        implements CalculatedLastUpdated, Serializable {

    //@Autowired
    def cascadingUpdateService // DO NOT OVERRIDE IN SUB CLASSES

    static Log static_logger = LogFactory.getLog(AbstractPropertyWithCalculatedLastUpdated)

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

    def static parseValue(String value, String type){
        def result
        static_logger.debug( value + " << " + type )

        switch (type){
            case Integer.toString():
            case Integer.class.name:
                result = Integer.parseInt(value)
                break
            case String.toString():
            case String.class.name:
                result = value
                break
            case BigDecimal.toString():
            case BigDecimal.class.name:
                result = new BigDecimal(value)
                break
            case org.codehaus.groovy.runtime.NullObject.toString():
            case org.codehaus.groovy.runtime.NullObject.class.name:
                result = null
                break
            case Date.toString():
            case Date.class.name:
                result = DateUtil.toDate_NoTime(value)
                break
            case URL.toString():
            case URL.class.name:
                result = new URL(value)
                break
            default:
                result = "AbstractProperty.parseValue failed"
        }
        return result
    }

    def setValue(String value, String type, String rdc) {

        if (type == Integer.toString() || type == Integer.class.name) {
            intValue = parseValue(value, type)
        }
        else if (type == BigDecimal.toString() || type == BigDecimal.class.name) {
            decValue = parseValue(value, type)
        }
        else if (type == String.toString() || type == String.class.name) {
            stringValue = parseValue(value, type)
        }
        else if (type == Date.toString() || type == Date.class.name) {
            dateValue = parseValue(value, type)
        }
        else if (type == RefdataValue.toString() || type == RefdataValue.class.name) {
            refValue = RefdataValue.getByValueAndCategory(value.toString(), rdc)
        }
        else if (type == URL.toString() || type == URL.class.name) {
            urlValue = parseValue(value, type)
        }
    }
}
