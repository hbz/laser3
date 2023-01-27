package de.laser.workflow

import de.laser.DocContext
import de.laser.RefdataValue
import de.laser.storage.BeanStore
import de.laser.utils.DateUtils
import grails.web.servlet.mvc.GrailsParameterMap

class ParamsHelper {

    ParamsHelper(String cmpKey, GrailsParameterMap params) {
        this.cmpKey = cmpKey + '_'
        this.params = params
    }

    String cmpKey
    GrailsParameterMap params

    boolean containsKey(String key) {
        params.containsKey(cmpKey + key)
    }

    String getString(String key) {
        params.get(cmpKey + key) ? params.get(cmpKey + key).toString().trim() : null
    }
    Long getLong(String key) {
        params.long(cmpKey + key)
    }
    Integer getInt(String key) {
        params.int(cmpKey + key)
    }
    Date getDate(String key) {
        params.get(cmpKey + key) ? DateUtils.parseDateGeneric(params.get(cmpKey + key) as String) : null
    }
    boolean getChecked(String key) {
        params.get(cmpKey + key) ? params.get(cmpKey + key) == 'on' : false
    }

    // --

    DocContext getDocContext(String key) {
        Long id = getLong(key)
        DocContext.findById(id)
    }

    RefdataValue getRefdataValue(String key) {
        Long id = getLong(key)
        RefdataValue.findById(id)
    }

    // --

    def getTarget() {
        params.get('target') ? BeanStore.getGenericOIDService().resolveOID(params.target) : null
    }
}