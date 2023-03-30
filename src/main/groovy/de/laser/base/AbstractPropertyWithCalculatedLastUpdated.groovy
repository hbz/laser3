package de.laser.base

import de.laser.RefdataValue
import de.laser.storage.BeanStore
import de.laser.utils.DateUtils
import de.laser.interfaces.CalculatedLastUpdated
import groovy.util.logging.Slf4j

/**
 * This is an abstract property which contains configurations and settings for the object it is belonging to.
 * Further belonging is specified by the subclasses.
 * The value may be:
 * <ul>
 *  <li>String</li>
 *  <li>Integer</li>
 *  <li>{@link BigDecimal}</li>
 *  <li>{@link RefdataValue}</li>
 *  <li>{@link URL}</li>
 *  <li>{@link Date}</li>
 * </ul>
 * The property belongs to a tenant {@link de.laser.Org} who may edit the value and configure its visibility setting. If it is not visible, no one except the tenant organisation may see the property.
 * Subscription and license properties may be inherited to member subscriptions / licenses; if they are public, the member may view but not edit the property.
 * This is also the case if a consortium defines a property directly in a member subscription or license and sets it to public; the member can see but not edit.
 * This is inversely the case if a member organisation (it needs to have at least ORG_INST_PRO role) defines a property in the member subscription or license and sets it to public - then everyone sees but no one can edit the property.
 * This is also the case if someone defines an {@link de.laser.properties.OrgProperty} or {@link de.laser.properties.PlatformProperty} - only the tenant can edit the property and the visibility depends on the isPublic flag.
 * Excepted hereof are OrgProperties/PlatformProperties which are defined before the introduction of tenant; those properties are editable by everyone as tenant is null at those properties.
 * {@link de.laser.properties.PersonProperty} is always private.
 * Moreover, a property may contain a note.
 * {@link de.laser.properties.LicenseProperty} may moreover contain a paragraph of the license describing or referring to this property.
 */
@Slf4j
abstract class AbstractPropertyWithCalculatedLastUpdated
        implements CalculatedLastUpdated, Serializable {

    /*
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
        log.debug("beforeInsertHandler()")
    }

    protected void afterInsertHandler() {
        log.debug("afterInsertHandler()")

        BeanStore.getCascadingUpdateService().update(this, dateCreated)
    }

    /**
     * Assembles the changes done on the property to pass them onto inheriting properties
     * @return the {@link Map} of changes
     */
    protected Map<String, Object> beforeUpdateHandler() {

        Map<String, Object> changes = [
                oldMap: [:],
                newMap: [:]
        ]
        this.getDirtyPropertyNames().each { prop ->
            changes.oldMap.put( prop, this.getPersistentValue(prop) )
            changes.newMap.put( prop, this.getProperty(prop) )
        }

        log.debug("beforeUpdateHandler() " + changes.toMapString())
        return changes
    }

    protected void afterUpdateHandler() {
        log.debug("afterUpdateHandler()")

        BeanStore.getCascadingUpdateService().update(this, lastUpdated)
    }

    protected void beforeDeleteHandler() {
        log.debug("beforeDeleteHandler()")
    }

    protected void afterDeleteHandler() {
        log.debug("afterDeleteHandler()")

        BeanStore.getCascadingUpdateService().update(this, new Date())
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

    /**
     * Calls {@link #toString()}
     */
    String getValue() {
        return toString()
    }

    /**
     * Does the same as {@link #toString()} but delivers reference values with their translated values
     * @return the stringified value of the property, depending on its value type
     */
    String getValueInI10n() {
        if (stringValue)      { return stringValue }
        if (intValue != null) { return intValue.toString() }
        if (decValue != null) { return decValue.toString() }
        if (refValue)         { return refValue.getI10n('value') }
        if (dateValue)        { return DateUtils.getLocalizedSDF_noTime().format(dateValue) }
        if (urlValue)         { return urlValue.toString() }
    }

    /**
     * Retrieves the stringified value of the property
     * @return the stringified value of the property, depending on its value type
     */
    @Override
    String toString(){
        if (stringValue)      { return stringValue }
        if (intValue != null) { return intValue.toString() }
        if (decValue != null) { return decValue.toString() }
        if (refValue)         { return refValue.toString() }
        if (dateValue)        { return DateUtils.getLocalizedSDF_noTime().format(dateValue) }
        if (urlValue)         { return urlValue.toString() }
    }

    /**
     * This method copies this property's value and note into the given new one
     * @param newProp the new property to set its value
     * @return the new property with the values and note set
     */
    def copyInto(AbstractPropertyWithCalculatedLastUpdated newProp){
        if (type.descr != newProp.type.descr) {
            throw new IllegalArgumentException("AbstractProperty.copyInto not possible because types do not match: ${type.descr} vs. ${newProp.type.descr}")
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

    /**
     * Parses the given value accoring to the given type and returns the converted value. Reference data values cannot be parsed here.
     * @param value the value to parse
     * @param type the type of value according to the value should be parsed
     * @return the parsed value
     */
    def static parseValue(String value, String type){
        def result
        log.debug( value + " << " + type )

        switch (type){
            case [ Integer.toString(), Integer.class.name ]:
                result = Integer.parseInt(value)
                break
            case [ String.toString(), String.class.name ]:
                result = value
                break
            case [ BigDecimal.toString(), BigDecimal.class.name ]:
                result = new BigDecimal(value)
                break
            case [ org.codehaus.groovy.runtime.NullObject.toString(), org.codehaus.groovy.runtime.NullObject.class.name ]:
                result = null
                break
            case [ Date.toString(), Date.class.name ]:
                result = (Date) DateUtils.getLocalizedSDF_noTime().parseObject(value)
                break
            case [ RefdataValue.toString(), RefdataValue.class.name ]:
                result = RefdataValue.get(value)
                break
            case [ URL.toString(), URL.class.name ]:
                result = new URL(value)
                break
            default:
                result = "AbstractProperty.parseValue failed"
        }
        return result
    }

    /**
     * Parses and stores the value according to the given type and eventually {@link de.laser.RefdataCategory}.
     * @param value the value to store
     * @param type the value type of the property definition
     * @param rdc if the value is a reference vale ({@link RefdataValue}), this is the {@link de.laser.RefdataCategory} to which the value belongs to
     */
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
