package de.laser.api.v0

import de.laser.Org
import de.laser.OrgSetting
import de.laser.helper.Constants
import groovy.util.logging.Slf4j
import org.apache.commons.lang.RandomStringUtils
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

import java.text.SimpleDateFormat

@Slf4j
class ApiToolkit {

    static final API_LEVEL_READ         = 'API_LEVEL_READ'
    static final API_LEVEL_WRITE        = 'API_LEVEL_WRITE'
    static final API_LEVEL_DATAMANAGER  = 'API_LEVEL_DATAMANAGER'
    static final API_LEVEL_EZB          = 'API_LEVEL_EZB'
    static final API_LEVEL_OAMONITOR    = 'API_LEVEL_OAMONITOR'
    static final API_LEVEL_NATSTAT      = 'API_LEVEL_NATSTAT'
    static final API_LEVEL_INVOICETOOL  = 'API_LEVEL_INVOICETOOL'

    static final NO_ACCESS_DUE_NO_APPROVAL  = 'NO_APPROVAL'
    static final NO_ACCESS_DUE_NOT_PUBLIC   = 'NOT_PUBLIC'

    static final DATE_TIME_PATTERN      = "yyyy-MM-dd'T'HH:mm:ss"

    static List getAllApiLevels() {
        [
            API_LEVEL_READ,
            API_LEVEL_WRITE,
            API_LEVEL_DATAMANAGER,
            API_LEVEL_EZB,
            API_LEVEL_OAMONITOR,
            API_LEVEL_NATSTAT,
            API_LEVEL_INVOICETOOL
        ]
    }
    static List getReadingApiLevels() {
        [
            API_LEVEL_READ,
            API_LEVEL_DATAMANAGER,
            API_LEVEL_EZB,
            API_LEVEL_OAMONITOR,
            API_LEVEL_NATSTAT,
            API_LEVEL_INVOICETOOL
        ]
    }
    static List getWritingApiLevels() {
        [
            API_LEVEL_WRITE
        ]
    }

    static void setApiLevel(Org org, String apiLevel) {

        if (! getAllApiLevels().contains(apiLevel)) {
            return
        }

        def oss = OrgSetting.get(org, OrgSetting.KEYS.API_LEVEL)
        if (oss != OrgSetting.SETTING_NOT_FOUND) {
            oss.strValue = apiLevel
            oss.save()
        }
        else {
            OrgSetting.add(org, OrgSetting.KEYS.API_LEVEL, apiLevel)
            OrgSetting.add(org, OrgSetting.KEYS.API_KEY, RandomStringUtils.randomAlphanumeric(24))
            OrgSetting.add(org, OrgSetting.KEYS.API_PASSWORD, RandomStringUtils.randomAlphanumeric(24))
        }
    }

    static void removeApiLevel(Org org) {

        OrgSetting.delete(org, OrgSetting.KEYS.API_LEVEL)
        OrgSetting.delete(org, OrgSetting.KEYS.API_KEY)
        OrgSetting.delete(org, OrgSetting.KEYS.API_PASSWORD)
    }

    static boolean hasApiLevel(Org org, String apiLevel) {
        def orgSetting = OrgSetting.get(org, OrgSetting.KEYS.API_LEVEL)

        if (orgSetting != OrgSetting.SETTING_NOT_FOUND) {
            return apiLevel == orgSetting.getValue()
        }
        return false
    }

    static boolean isDebugMode() {
        RequestAttributes reqAttr = RequestContextHolder.currentRequestAttributes()
        reqAttr.getAttribute('debugMode', RequestAttributes.SCOPE_REQUEST)
    }

    static Collection<Object> cleanUpDebugInfo(Collection<Object> list) {
        if (! isDebugMode()) {
            list.removeAll(Constants.HTTP_FORBIDDEN)
        }

        list
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

    static String formatInternalDate(Date date) {
        if (! date) {
            return null
        }

        SimpleDateFormat sdf = new SimpleDateFormat(ApiToolkit.DATE_TIME_PATTERN) // DateUtil.getSDF_NoZ()
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
