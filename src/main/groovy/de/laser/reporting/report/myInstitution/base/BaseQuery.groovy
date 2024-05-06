package de.laser.reporting.report.myInstitution.base

import de.laser.IdentifierNamespace
import de.laser.Org
import de.laser.RefdataValue
import de.laser.auth.Role
import de.laser.utils.LocaleUtils
import de.laser.storage.BeanStore
import de.laser.utils.DateUtils
import de.laser.properties.PropertyDefinition
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.local.SubscriptionReport
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

import java.sql.Timestamp
import java.time.Year

/**
 * This class contains general methods for data querying.
 * Those methods are being shared by classes specifying queries for each object individually
 */
class BaseQuery {

    static String NO_DATA_LABEL              = 'noData.label'
    static String NO_MATCH_LABEL             = 'noMatch.label'
    static String NO_COUNTERPART_LABEL       = 'noCounterpart.label'
    static String NO_IDENTIFIER_LABEL        = 'noIdentifier.label'
    static String NO_LICENSE_LABEL           = 'noLicense.label'
    static String NO_PLATFORM_LABEL          = 'noPlatform.label'
    static String NO_PLATFORM_PROVIDER_LABEL = 'noPlatformProvider.label'
    static String NO_PROVIDER_LABEL          = 'noProvider.label'
    static String NO_VENDOR_LABEL            = 'noVendor.label'
    static String NO_STARTDATE_LABEL         = 'noStartDate.label'
    static String NO_ENDDATE_LABEL           = 'noEndDate.label'

    static List<String> PROPERTY_QUERY       = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static def NO_DATA_ID           = null
    static int NO_COUNTERPART_ID    = 0 // dyn.neg.values for unmapped es refdata
    static int FAKE_DATA_ID_1       = -1
    static int FAKE_DATA_ID_2       = -2
    static int FAKE_DATA_ID_3       = -3

    static String SQM_MASK      = "\\\\\'"

    /**
     * Delivers an empty result map for initial chart display
     * @param query the query string assembling the chart
     * @param chart the chart data
     * @return a map containing initial values
     */
    static Map<String, Object> getEmptyResult(String query, String chart) {
        return [
                chart       : chart,
                query       : query,
                labels      : [:],
                data        : [],
                dataDetails : []
        ]
    }

    /**
     * Gets the labels for the given config and requested in the given map
     * @param config the configuration from where the labels should be read off
     * @param params the request parameter map
     * @return the labels illustrating the report
     */
    static List<String> getQueryLabels(Map<String, Object> config, GrailsParameterMap params) {

        List<String> labels = getQueryLabels(config, params.query)

        if (labels) {
            labels.add( params.label ) // not set in step 2
        }
        labels
    }

    /**
     * Gets the labels for the query parameters
     * @param config the configuration from where the labels should be read off
     * @param query the data being queried
     * @return the labels illustrating the report
     */
    static List<String> getQueryLabels(Map<String, Object> config, String query) {
        List<String> meta = []

        config.each {it ->
            String cfgKey = it.value.get('meta').cfgKey

            it.value.get('query')?.default?.each { it2 ->
                if (it2.value.containsKey(query)) {
                    if (cfgKey == BaseConfig.KEY_LOCAL_SUBSCRIPTION) {
                        meta = [SubscriptionReport.getMessage(it2.key), SubscriptionReport.getQueryLabel(query, it2.value.get(query))]
                    } else {
                        meta = [BaseConfig.getConfigLabel(it2.key), BaseConfig.getQueryLabel(cfgKey, query, it2.value.get(query))]
                    }
                }
            }
            it.value.get('distribution')?.each { it2 ->
                if (it2.value.containsKey(query)) {
                    meta = [BaseConfig.getConfigLabel('distribution'), BaseConfig.getDistributionLabel(cfgKey, query) ]
                }
            }
            it.value.get('timeline')?.each { it2 ->
                if (it2.value.containsKey(query)) {
                    meta = [SubscriptionReport.getMessage(it2.key), SubscriptionReport.getMessage('timeline.' + query) ]
                }
            }
        }
        meta
    }

    /**
     * Gets the details of the object being addressed by key and ID from the given list
     * @param id the database ID of the requested object
     * @param key the key whose value should be read off from the given object
     * @param ddList the list of candidate objects
     * @return the value being defined in the given object under the given key
     */
    static Object getDataDetailsByIdAndKey(Long id, String key, List<Map<String, Object>> ddList) {
        def result

        ddList.each{ it ->
            if (it.id == id) {
                result = it.get(key)
                return
            }
        }
        result
    }

    /**
     * Performs the given generic object query with the given settings, filling the given result map
     * @param query the parameters being requested
     * @param dataHql the query input, translated into HQL
     * @param dataDetailsHql the query for the details of the queried data
     * @param nonMatchingHql the query for non matching objects
     * @param idList the list of database IDs
     * @param result the result map being filled
     */
    static void handleGenericQuery(String query, String dataHql, String dataDetailsHql, String nonMatchingHql, List<Long> idList, Map<String, Object> result) {

        result.data = idList ? Org.executeQuery( dataHql, [idList: idList] ) : []

        result.data.each { d ->
            d[1] = d[0].toString()

            result.dataDetails.add( [
                    query:  query,
                    id:     d[0],
                    label:  d[1],
                    idList: Org.executeQuery( dataDetailsHql, [idList: idList, d: d[0]] )
            ])
        }
        handleGenericNonMatchingData( query, nonMatchingHql, idList, result )
    }

    /**
     * Performs the given generic object query with the given settings, filling the given result map and sorting between matched and orphaned objects
     * @param query the parameters being requested
     * @param dataHql the query input, translated into HQL
     * @param dataDetailsHql the query for the details of the queried data
     * @param idList the list of database IDs
     * @param orphanedIdList the list of database object IDs which are orphaned
     * @param result the result map being filled
     */
    static void handleGenericAllSignOrphanedQuery(String query, String dataHql, String dataDetailsHql, List<Long> idList, List<Long> orphanedIdList, Map<String, Object> result) {

        result.data = idList ? Org.executeQuery( dataHql, [idList: idList] ) : []

        result.data.each { d ->
            d[3] = orphanedIdList.contains(d[0]) ? 1 : 0
            d[2] = d[3] ? 0 : 1
            d[0] = Math.abs(d[1].hashCode())

            result.dataDetails.add([
                    query : query,
                    id    : d[0],
                    label : d[1],
                    idList: Org.executeQuery( dataDetailsHql, [idList: idList, d: d[1]] ),
                    value1: d[2], // matched
                    value2: d[3]  // orphaned
            ])
        }
    }

    /**
     * Performs the given generic object query with the given settings, filling the given result map
     * @param query the parameters being requested
     * @param dataHql the query input, translated into HQL
     * @param dataDetailsHql the data details query
     * @param idList the list of database IDs
     * @param result the result map being filled
     */
    static void handleGenericAllQuery(String query, String dataHql, String dataDetailsHql, List<Long> idList, Map<String, Object> result) {

        result.data = idList ? Org.executeQuery( dataHql, [idList: idList] ) : []

        result.data.each { d ->
            d[0] = Math.abs(d[0].hashCode())

            result.dataDetails.add([
                    query : query,
                    id    : d[0],
                    label : d[1],
                    idList: Org.executeQuery( dataDetailsHql, [idList: idList, d: d[1]] )
            ])
        }
    }

    /**
     * Performs the given reference data query with the given settings
     * @param query the parameters being requested
     * @param dataHql the query input, translated into HQL
     * @param dataDetailsHql the data details query
     * @param nonMatchingHql the query for non matching objects
     * @param idList the list of database IDs
     * @param result the result map being filled
     */
    static void handleGenericRefdataQuery(String query, String dataHql, String dataDetailsHql, String nonMatchingHql, List<Long> idList, Map<String, Object> result) {

        result.data = idList ? Org.executeQuery( dataHql, [idList: idList] ) : []

        result.data.each { d ->
            d[1] = RefdataValue.get(d[0]).getI10n('value')

            result.dataDetails.add( [
                    query:  query,
                    id:     d[0],
                    label:  d[1],
                    idList: Org.executeQuery( dataDetailsHql, [idList: idList, d: d[0]] )
            ])
        }
        handleGenericNonMatchingData( query, nonMatchingHql, idList, result )
    }

    /**
     * Performs the given role query with the given settings
     * @param query the parameters being requested
     * @param dataHql the query input, translated into HQL
     * @param dataDetailsHql the data details query
     * @param nonMatchingHql the query for non matching objects
     * @param idList the list of database IDs
     * @param result the result map being filled
     */
    static void handleGenericRoleQuery(String query, String dataHql, String dataDetailsHql, String nonMatchingHql, List<Long> idList, Map<String, Object> result) {

        result.data = idList ? Org.executeQuery( dataHql, [idList: idList] ) : []

        result.data.each { d ->
            d[1] = Role.get(d[0]).getI10n('authority')

            result.dataDetails.add( [
                    query:  query,
                    id:     d[0],
                    label:  d[1],
                    idList: Org.executeQuery( dataDetailsHql, [idList: idList, d: d[0]] )
            ])
        }
        handleGenericNonMatchingData( query, nonMatchingHql, idList, result )
    }

    /**
     * Generic handler for no matching data queries
     * @param query the parameters being requested
     * @param hql the query input, translated into HQL
     * @param idList the list of database IDs
     * @param result the result map being filled
     */
    static void handleGenericNonMatchingData(String query, String hql, List<Long> idList, Map<String, Object> result) {

        List<Long> noDataList = idList ? Org.executeQuery( hql, [idList: idList] ) : []

        handleGenericNonMatchingData1Value_TMP(query, NO_DATA_LABEL, noDataList, result)
    }

    /**
     * Output for a query delivering no matching data
     * @param query the data being queried
     * @param label the chart label key
     * @param noDataList the list of IDs where no data is matching
     * @param result the result map being filled
     */
    static void handleGenericNonMatchingData1Value_TMP(String query, String label, List<Long> noDataList, Map<String, Object> result) {

        if (noDataList) {
            result.data.add([NO_DATA_ID, getChartLabel(label), noDataList.size()])

            result.dataDetails.add([
                    query : query,
                    id    : NO_DATA_ID,
                    label : getChartLabel(label),
                    idList: noDataList,
            ])
        }
    }

    /**
     * Output for a query delivering no matching data
     * @param query the data being queried
     * @param label the chart label key
     * @param noDataIdList the list of IDs where no data is matching
     * @param result the result map being filled
     */
    static void handleGenericNonMatchingData2Values_TMP(String query, String label, List<Long> noDataIdList, Map<String, Object> result) {

        if (noDataIdList) {
            result.data.add([NO_DATA_ID, getChartLabel(label), noDataIdList.size()])

            result.dataDetails.add([
                    query : query,
                    id    : NO_DATA_ID,
                    label : getChartLabel(label),
                    idList: noDataIdList,
                    value1: 0,
                    value2: noDataIdList.size()
            ])
        }
    }

    /**
     * Performs the given role query with the given settings
     * @param query the parameters being requested
     * @param dataHql the query input, translated into HQL
     * @param dataDetailsHql the details of the queried data
     * @param idList the list of database object IDs
     * @param result the result map being filled
     */
    static void handleGenericBooleanQuery(String query, String dataHql, String dataDetailsHql, List<Long> idList, Map<String, Object> result) {

        result.data = idList ? Org.executeQuery( dataHql, [idList: idList] ) : []

        result.data.each { d ->
            d[0] = (d[0] == true ? 1 : 0)
            d[1] = (d[1] == true ? 'Ja' : 'Nein')

            result.dataDetails.add([
                    query : query,
                    id    : d[0],
                    label : d[1],
                    idList: Org.executeQuery( dataDetailsHql, [idList: idList, d: (d[0] == 1)] )
            ])
        }
    }

    /**
     * Performs the given date query with the given settings
     * @param query the parameters being requested
     * @param dataHql the query input, translated into HQL
     * @param dataDetailsHql the details of the queried data
     * @param idList the list of database object IDs
     * @param result the result map being filled
     */
    static void handleGenericDateQuery(String query, String dataHql, String dataDetailsHql, String nonMatchingHql, List<Long> idList, Map<String, Object> result) {

        result.data = idList ? Org.executeQuery( dataHql, [idList: idList] ) : []

        result.data.each { d ->
            Timestamp ts = d[0]
            Long d0Id = ts.toInstant().getEpochSecond()
            d[1] = DateUtils.getLocalizedSDF_noTime().format(d[1])

            result.dataDetails.add( [
                    query:  query,
                    id:     d0Id,
                    label:  d[1],
                    idList: Org.executeQuery( dataDetailsHql, [idList: idList, d: d[0]] )
            ])

            d[0] = d0Id
        }
        handleGenericNonMatchingData( query, nonMatchingHql, idList, result )
    }

    /**
     * Performs the given identifier query with the given settings
     * @param query the parameters being requested
     * @param dataHqlPart the part of query input, translated into HQL
     * @param dataDetailsHqlPart the details of the queried data
     * @param idList the list of database object IDs
     * @param result the result map being filled
     */
    static void handleGenericIdentifierXQuery(String query, String dataHqlPart, String dataDetailsHqlPart, String nonMatchingHql, List<Long> idList, Map<String, Object> result) {

        result.data = idList ? Org.executeQuery(
                dataHqlPart + " and ident.value is not null and trim(ident.value) != '' group by ns.id order by ns.ns",
                [idList: idList]
        ) : []

        result.data.each { d ->
            List<Long> objIdList = Org.executeQuery(
                    dataDetailsHqlPart + " and ns.id = :d and ident.value is not null and trim(ident.value) != ''",
                    [idList: idList, d: d[0]]
            )
            d[1] = IdentifierNamespace.get(d[0]).getI10n('name') ?: GenericHelper.flagUnmatched(d[1])

            result.dataDetails.add([
                    query : query,
                    id    : d[0],
                    label : d[1],
                    idList: objIdList,
                    value1: objIdList.size(),
                    value2: objIdList.unique().size()
            ])
        }

        List<Long> nonMatchingIdList = idList.minus( result.dataDetails.collect { it.idList }.flatten() )
        List<Long> noDataList = nonMatchingIdList ? Org.executeQuery( nonMatchingHql, [idList: nonMatchingIdList] ) : []

        handleGenericNonMatchingData2Values_TMP(query, NO_IDENTIFIER_LABEL, noDataList, result)
    }

    /**
     * Performs the given property query with the given settings
     * @param query the parameters being requested
     * @param dataHqlPart the query input, translated into HQL
     * @param dataDetailsHqlPart the details of the queried data
     * @param idList the list of database object IDs
     * @param ctxOrg the context institution who is tenant of the (private) properties
     * @param result the result map being filled
     */
    static void handleGenericPropertyXQuery(String query, String dataHqlPart, String dataDetailsHqlPart, List<Long> idList, Org ctxOrg, Map<String, Object> result) {

        String lang = LocaleUtils.getCurrentLang()

        result.data = idList ? Org.executeQuery(
                dataHqlPart + " and (prop.tenant = :ctxOrg or prop.isPublic = true) and pd.descr like '%Property' group by pd.id order by pd.name_" + lang,
                [idList: idList, ctxOrg: ctxOrg]
        ) : []

        result.data.each { d ->
            d[1] = PropertyDefinition.get(d[0]).getI10n('name')

            List<Long> obj2IdList =  Org.executeQuery(
                    dataDetailsHqlPart + ' and (prop.isPublic = true) and pd.id = :d order by pd.name_' + lang,
                    [idList: idList, d: d[0]]
            )
            List<Long> obj3IdList =  Org.executeQuery(
                    dataDetailsHqlPart + ' and (prop.tenant = :ctxOrg and prop.isPublic != true) and pd.id = :d order by pd.name_' + lang,
                    [idList: idList, d: d[0], ctxOrg: ctxOrg]
            )
            int obj2IdListSize = obj2IdList.size()
            int obj3IdListSize = obj3IdList.size()
            result.dataDetails.add([
                    query : query,
                    id    : d[0],
                    label : d[1],
                    idList: obj3IdList + obj2IdList,
                    value1: obj3IdList.unique().size() + obj2IdList.unique().size(),
                    value2: obj2IdListSize,
                    value3: obj3IdListSize
            ])
        }
    }

    /**
     * Performs the given generic query with the given settings, matching for a given year ring
     * @param query the parameters being requested
     * @param domainClass the type of object being queried
     * @param idList the list of database object IDs
     * @param result the result map being filled
     */
    static void handleGenericAnnualXQuery(String query, String domainClass, List<Long> idList, Map<String, Object> result) {

        List dd = Org.executeQuery( 'select min(YEAR(dc.startDate)), max(YEAR(dc.endDate)) from ' + domainClass + ' dc where dc.id in (:idList)', [idList: idList] )
        dd[0][1] = dd[0][1] ? Math.min( dd[0][1] as int, Year.now().value + 5 ) : Year.now().value
        List years = ( (dd[0][0] ?: Year.now().value)..(dd[0][1]) ).toList()

        years.sort().each { y ->
            String hql = 'select dc.id from ' + domainClass + ' dc where dc.id in (:idList) and ' +
                    '( (YEAR(dc.startDate) <= ' + y + ') and (YEAR(dc.endDate) >= ' + y + ' or dc.endDate is null) ) and ' +
                    'not (dc.startDate is null and dc.endDate is null)'

            List<Long> annualList = Org.executeQuery( hql, [idList: idList] )

            if (annualList) {
                result.data.add( [y, y, annualList.size()] )
                result.dataDetails.add( [
                        query: query,
                        id: y,
                        label: y,
                        idList: annualList
                ] )
            }
        }

        List<Long> sp1DataList = Org.executeQuery( 'select dc.id from ' + domainClass + ' dc where dc.id in (:idList) and dc.startDate != null and dc.endDate is null', [idList: idList] )
        if (sp1DataList) {
            result.data.add([FAKE_DATA_ID_1, getChartLabel(NO_ENDDATE_LABEL), sp1DataList.size()])

            result.dataDetails.add([
                    query : query,
                    id    : FAKE_DATA_ID_1,
                    label : getChartLabel(NO_ENDDATE_LABEL),
                    idList: sp1DataList
            ])
        }

        List<Long> sp2DataList = Org.executeQuery( 'select dc.id from ' + domainClass + ' dc where dc.id in (:idList) and dc.startDate is null and dc.endDate != null', [idList: idList] )
        if (sp2DataList) {
            result.data.add([FAKE_DATA_ID_2, getChartLabel(NO_STARTDATE_LABEL), sp2DataList.size()])

            result.dataDetails.add([
                    query : query,
                    id    : FAKE_DATA_ID_2,
                    label : getChartLabel(NO_STARTDATE_LABEL),
                    idList: sp2DataList
            ])
        }

        List<Long> noDataList = Org.executeQuery( 'select dc.id from ' + domainClass + ' dc where dc.id in (:idList) and dc.startDate is null and dc.endDate is null', [idList: idList] )

        handleGenericNonMatchingData1Value_TMP(query, NO_DATA_LABEL, noDataList, result)
    }

    /**
     * Retrieves for the given token the associated chart label
     * @param token the token for which the label is being requested
     * @return the matching message token
     */
    static String getChartLabel(String token) {
        //println 'getChartLabel(): ' + token
        MessageSource messageSource = BeanStore.getMessageSource()
        messageSource.getMessage('reporting.chart.result.' + token, null, LocaleUtils.getCurrentLocale())
    }
}
