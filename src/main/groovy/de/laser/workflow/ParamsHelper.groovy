package de.laser.workflow

import de.laser.DocContext
import de.laser.RefdataValue
import de.laser.storage.BeanStore
import de.laser.utils.DateUtils
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * Helper class for workflow filter parameter processing
 */
class ParamsHelper {

    /**
     * Constructor call
     * @param cmpKey the component key
     * @param params the request parameter map
     */
    ParamsHelper(String cmpKey, GrailsParameterMap params) {
        this.cmpKey = cmpKey + '_'
        this.params = params
    }

    String cmpKey
    GrailsParameterMap params

    /**
     * Checks if the component-field-combination is contained in the parameter map
     * @param key the field key to retrieve
     * @return true if the component-field-combination is being contained, false otherwise
     */
    boolean containsKey(String key) {
        params.containsKey(cmpKey + key)
    }

    /**
     * Gets the string value for the given key from the request parameter map
     * @param key the field key to retrieve
     * @return the string value behind the key
     */
    String getString(String key) {
        params.get(cmpKey + key) ? params.get(cmpKey + key).toString().trim() : null
    }

    /**
     * Gets the long value for the given key from the request parameter map
     * @param key the field key to retrieve
     * @return the long value behind the key
     */
    Long getLong(String key) {
        params.long(cmpKey + key)
    }

    /**
     * Gets the integer value for the given key from the request parameter map
     * @param key the field key to retrieve
     * @return the integer value behind the key
     */
    Integer getInt(String key) {
        params.int(cmpKey + key)
    }

    /**
     * Gets the date value for the given key from the request parameter map
     * @param key the field key to retrieve
     * @return the date value behind the key
     */
    Date getDate(String key) {
        params.get(cmpKey + key) ? DateUtils.parseDateGeneric(params.get(cmpKey + key) as String) : null
    }

    /**
     * Gets the checkbox state for the given key from the request parameter map
     * @param key the key whose value to retrieve
     * @return true if the checkbox is checked, false otherwise
     */
    boolean getChecked(String key) {
        params.get(cmpKey + key) ? params.get(cmpKey + key) == 'on' : false
    }

    // --

    /**
     * Gets the document context (i.e. document linking to an object) for the given key
     * @param key the parameter key
     * @return the {@link DocContext} matching the given key
     */
    DocContext getDocContext(String key) {
        Long id = getLong(key)
        DocContext.findById(id)
    }

    /**
     * Gets the controlled list entry for the given key
     * @param key the parameter key
     * @return the {@link RefdataValue} matching the given key
     */
    RefdataValue getRefdataValue(String key) {
        Long id = getLong(key)
        RefdataValue.findById(id)
    }

    // --

    /**
     * Resolves a target object if defined
     * @return the target object, if the parameter is set and can be resolved, null otherwise
     */
    def getTarget() {
        params.get('target') ? BeanStore.getGenericOIDService().resolveOID(params.target) : null
    }
}