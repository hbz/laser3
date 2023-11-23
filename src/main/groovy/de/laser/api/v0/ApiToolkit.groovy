package de.laser.api.v0

import de.laser.Org
import de.laser.OrgSetting
import de.laser.storage.Constants
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import static java.time.temporal.TemporalAdjusters.firstDayOfYear
import static java.time.temporal.TemporalAdjusters.lastDayOfYear

/**
 * This class is a toolbox for checkings and validations during the API usage
 */
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
    static final DATE_TIME_PATTERN_SQL  = 'yyyy-MM-dd"T"HH24:MI:SS'

    /**
     * Gets all defined API levels
     * @return a {@link List} of API level constants
     */
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

    /**
     * Gets all API levels which have reading permissions
     * @return a {@link List} of API levels with reading rights granted
     */
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

    /**
     * Gets all API levels which have writing permissions
     * @return a {@link List} of API levelsm with writing rights granted
     */
    static List getWritingApiLevels() {
        [
            API_LEVEL_WRITE
        ]
    }

    /**
     * Gets the start of the current year ring
     * @return the first of January of the current year
     */
    static String getStartOfYearRing() {
        LocalDateTime ldt = LocalDateTime.now()
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
        ldt.with(firstDayOfYear())
        dateTimeFormatter.format(ldt)
    }

    /**
     * Gets the end of the current year ring
     * @return the 31st of December of the current year
     */
    static String getEndOfYearRing() {
        LocalDateTime ldt = LocalDateTime.now()
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
        ldt.with(lastDayOfYear())
        dateTimeFormatter.format(ldt)
    }

    /**
     * Sets the given API level; if the not existent, API credentials will be created as well
     * @param org the institution ({@link Org}) to which the API level should be set up
     * @param apiLevel the API level to define
     */
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

    /**
     * Revokes the API level and the API credentials from the given institution
     * @param org the institution ({@link Org}) from which the API rights should be revoked
     */
    static void removeApiLevel(Org org) {

        OrgSetting.delete(org, OrgSetting.KEYS.API_LEVEL)
        OrgSetting.delete(org, OrgSetting.KEYS.API_KEY)
        OrgSetting.delete(org, OrgSetting.KEYS.API_PASSWORD)
    }

    /**
     * Checks if the given institution has the given API level granted
     * @param org the institution ({@link Org}) to be checked
     * @param apiLevel the API level to be verified
     * @return true if the level has been granted to the org, false otherwise
     */
    static boolean hasApiLevel(Org org, String apiLevel) {
        def orgSetting = OrgSetting.get(org, OrgSetting.KEYS.API_LEVEL)

        if (orgSetting != OrgSetting.SETTING_NOT_FOUND) {
            return apiLevel == orgSetting.getValue()
        }
        return false
    }

    /**
     * Gets all institutions ({@link Org}) to which the given special access grant has been given
     * @param specGrant the grant which has been granted to the institutions
     * @return a {@link List} of {@link Org}s (institutions) to which the given access right has been granted
     * @see OrgSetting.KEYS#EZB_SERVER_ACCESS
     * @see OrgSetting.KEYS#OAMONITOR_SERVER_ACCESS
     * @see OrgSetting.KEYS#NATSTAT_SERVER_ACCESS
     */
    static List<Org> getOrgsWithSpecialAPIAccess(String specGrant) {
        Org.executeQuery('select o.globalUID from OrgSetting os join os.org o where os.key = :customerType and os.strValue = :specGrant', [customerType: OrgSetting.KEYS.API_LEVEL, specGrant: specGrant])
    }

    /**
     * Checks if the debugMode flag is set among the request parameters
     * @return the flag value if it is set, null otherwise
     */
    static boolean isDebugMode() {
        RequestAttributes reqAttr = RequestContextHolder.currentRequestAttributes()
        reqAttr.getAttribute('debugMode', RequestAttributes.SCOPE_REQUEST)
    }

    /**
     * Removes the debug information from the response if the debugMode flag is missing
     * @param list the response list containing the objects
     * @return the cleaned response list
     */
    static Collection<Object> cleanUpDebugInfo(Collection<Object> list) {
        if (! isDebugMode()) {
            list.removeAll(Constants.HTTP_FORBIDDEN)
        }

        list
    }

    /**
     * Cleans up the given response {@link Map} from null values or empty lists if specified
     * @param map the response map to be cleaned
     * @param removeNullValues should null values be removed?
     * @param removeEmptyLists should empty lists being removed?
     * @return the cleaned map
     */
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

    /**
     * Cleans up the given response {@link List} from null values or empty lists if specified
     * @param list the response list to be cleaned
     * @param removeNullValues should null values be removed?
     * @param removeEmptyLists should empty lists being removed?
     * @return the cleaned list
     */
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

    /**
     * Outputs the given date with the internal format ({@link #DATE_TIME_PATTERN})
     * @param date the date to format
     * @return the date string in the format specified in {@link #DATE_TIME_PATTERN}
     */
    static String formatInternalDate(Date date) {
        if (! date) {
            return null
        }

        SimpleDateFormat sdf = new SimpleDateFormat(ApiToolkit.DATE_TIME_PATTERN) // DateUtil.getSDF_NoZ()
        sdf.format(date)
    }

    /**
     * Parses the timespan specified in the given query
     * @param query the fields whose values are specified, comma-separated
     * @param value the values, comma-separated
     * @return a {@link Map} containing the parsed key:value pairs
     */
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
