package de.laser.api.v0

import com.k_int.kbplus.*
import de.laser.CacheService
import de.laser.helper.Constants
import de.laser.helper.RDStore
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class ApiToolkit {

    /**
     * @param Map map
     * @param removeEmptyValues
     * @param removeEmptyLists
     * @return
     */
    static Map<String, Object> cleanUp(Map map, removeNullValues, removeEmptyLists) {
        if (! map) {
            return null
        }
        Collection<String> values = map.values()

        if (removeNullValues){
            while (values.remove(null));
            while (values.remove(""));
        }
        if (removeEmptyLists){
            while (values.remove([]));
        }
        map
    }

    /**
     * @param Collection list
     * @param removeEmptyValues
     * @param removeEmptyLists
     * @return
     */
    static Collection cleanUp(Collection list, removeNullValues, removeEmptyLists) {
        if (! list) {
            return null
        }

        if (removeNullValues){
            while (list.remove(null));
            while (list.remove(""));
        }
        if (removeEmptyLists){
            while (list.remove([]));
        }
        list
    }

    static boolean isDataManager(Org org) {
        def apiLevel = OrgSettings.get(org, OrgSettings.KEYS.API_LEVEL)

        if (apiLevel != OrgSettings.SETTING_NOT_FOUND) {
            return ApiManager.API_LEVEL_DATAMANAGER == apiLevel.getValue()
        }
        return false
    }
}
