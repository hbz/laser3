package de.laser.api.v0


import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgSettings
import de.laser.helper.Constants
import de.laser.helper.DateUtil
import groovy.util.logging.Log4j
import org.apache.commons.lang.RandomStringUtils

import java.text.SimpleDateFormat

@Log4j
class ApiToolkit {

    static final API_LEVEL_READ         = 'API_LEVEL_READ'
    static final API_LEVEL_WRITE        = 'API_LEVEL_WRITE'
    static final API_LEVEL_DATAMANAGER  = 'API_LEVEL_DATAMANAGER'
    static final API_LEVEL_INVOICETOOL  = 'API_LEVEL_INVOICETOOL'

    static final NO_ACCESS_DUE_NO_APPROVAL  = 'NO_APPROVAL'
    static final NO_ACCESS_DUE_NOT_PUBLIC   = 'NOT_PUBLIC'

    static List getAllApiLevels() {
        [API_LEVEL_READ, API_LEVEL_WRITE, API_LEVEL_DATAMANAGER, API_LEVEL_INVOICETOOL]
    }

    static void setApiLevel(Org org, String apiLevel) {

        if (! getAllApiLevels().contains(apiLevel)) {
            return
        }

        def oss = OrgSettings.get(org, OrgSettings.KEYS.API_LEVEL)
        if (oss != OrgSettings.SETTING_NOT_FOUND) {
            oss.strValue = apiLevel
            oss.save(flush:true)
        }
        else {
            OrgSettings.add(org, OrgSettings.KEYS.API_LEVEL, apiLevel)
            OrgSettings.add(org, OrgSettings.KEYS.API_KEY, RandomStringUtils.randomAlphanumeric(24))
            OrgSettings.add(org, OrgSettings.KEYS.API_PASSWORD, RandomStringUtils.randomAlphanumeric(24))
        }
    }

    static void removeApiLevel(Org org) {

        OrgSettings.delete(org, OrgSettings.KEYS.API_LEVEL)
        OrgSettings.delete(org, OrgSettings.KEYS.API_KEY)
        OrgSettings.delete(org, OrgSettings.KEYS.API_PASSWORD)
    }

    static boolean isDataManager(Org org) {
        def apiLevel = OrgSettings.get(org, OrgSettings.KEYS.API_LEVEL)

        if (apiLevel != OrgSettings.SETTING_NOT_FOUND) {
            return ApiToolkit.API_LEVEL_DATAMANAGER == apiLevel.getValue()
        }
        return false
    }

    static boolean isInvoiceTool(Org org) {
        def apiLevel = OrgSettings.get(org, OrgSettings.KEYS.API_LEVEL)

        if (apiLevel != OrgSettings.SETTING_NOT_FOUND) {
            return ApiToolkit.API_LEVEL_INVOICETOOL == apiLevel.getValue()
        }
        return false
    }

    static Map<String, Object> cleanUp(Map map, boolean removeNullValues, boolean removeEmptyLists) {
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

    static Collection<Object> cleanUp(Collection<Object> list, boolean removeNullValues, boolean removeEmptyLists) {
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

    static Object checkPreconditionFailed(result) {

        if (result) {
            result = result.size() == 1 ? result.get(0) : Constants.HTTP_PRECONDITION_FAILED
        }

        result
    }

    static String formatInternalDate(Date date) {
        if (! date) {
            return null
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss") // DateUtil.getSDF_NoZ()
        sdf.format(date)
    }

    static Object parseTimeLimitedQuery(String query, String value) {
        String[] queries = query.split(",")
        String[] values = value.split(",")

        Map<String, Object> identifier = [:]
        Map<String, Object> timestamp = [:]

        if (queries.size() == 2 && values.size() == 2) {
            identifier.key = queries[0].trim()
            identifier.value = values[0].trim()

            timestamp.key = queries[1].trim()
            timestamp.value = values[1].trim()

        }
        else if (queries.size() == 1 && values.size() == 1) {
            identifier.key = queries[0].trim()
            identifier.value = values[0].trim()

        }
        else {
            return Constants.HTTP_BAD_REQUEST
        }

        [identifier, timestamp]
    }
}
