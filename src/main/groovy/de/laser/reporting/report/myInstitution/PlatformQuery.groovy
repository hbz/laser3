package de.laser.reporting.report.myInstitution

import de.laser.*
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.reporting.report.ElasticSearchHelper
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import de.laser.utils.LocaleUtils
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

class PlatformQuery extends BaseQuery {

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = BeanStore.getContextService()
        MessageSource messageSource = BeanStore.getMessageSource()
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Map> esdConfig  = BaseConfig.getCurrentConfigElasticsearchData( BaseConfig.KEY_PLATFORM )

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        def (String prefix, String suffix) = params.query.split('-') // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)
        List<Long> orphanedIdList = BaseFilter.getCachedFilterIdList(prefix + 'Orphaned', params)

        //println 'PlatformQuery.query() -> ' + params.query + ' : ' + suffix

//        Closure sharedQuery_platform_org = {
//            // println 'sharedQuery_platform_org()'
//            handleGenericAllQuery(
//                    params.query,
//                    'select org.name, org.name, count(*) from Platform plt join plt.org org where plt.id in (:idList) group by org.name order by org.name',
//                    'select plt.id from Platform plt where plt.id in (:idList) and plt.org.name = :d order by plt.name',
//                    idList,
//                    result
//            )
//            handleGenericNonMatchingData(params.query, 'select plt.id from Platform plt where plt.id in (:idList) and plt.org is null order by plt.name', idList, result)
//        }
        Closure sharedQuery_platform_primaryUrl = {
            // println 'sharedQuery_platform_primaryUrl()'
            handleGenericAllQuery(
                    params.query,
                    'select plt.primaryUrl, plt.primaryUrl, count(*) from Platform plt where plt.id in (:idList) and plt.primaryUrl != null group by plt.primaryUrl order by plt.primaryUrl',
                    'select plt.id from Platform plt where plt.id in (:idList) and plt.primaryUrl = :d order by plt.name',
                    idList,
                    result
            )
            handleGenericNonMatchingData(params.query, 'select plt.id from Platform plt where plt.id in (:idList) and plt.primaryUrl is null order by plt.name', idList, result)
        }

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            handleGenericAllSignOrphanedQuery(
                    params.query,
                    'select plt.id, plt.name, 1, plt.id from Platform plt where plt.id in (:idList) order by plt.name',
                    'select plt.id from Platform plt where plt.id in (:idList) and plt.name = :d order by plt.id',
                    idList,
                    orphanedIdList,
                    result
            )
        }
        else if (suffix in ['ipAuthentication']) {

            _processESRefdataQuery(params.query, RDConstants.IP_AUTHENTICATION, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if (suffix in ['passwordAuthentication', 'otherProxies', 'shibbolethAuthentication']) {

            _processESRefdataQuery(params.query, RDConstants.Y_N, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if (suffix in ['statisticsFormat']) {

            _processESRefdataQuery(params.query, RDConstants.PLATFORM_STATISTICS_FORMAT, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if (suffix in ['statisticsUpdate']) {

            _processESRefdataQuery(params.query, RDConstants.PLATFORM_STATISTICS_FREQUENCY, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if (suffix in ['counterCertified', 'counterR4Supported', 'counterR4SushiApiSupported', 'counterR5Supported', 'counterR5SushiApiSupported']) {

            _processESRefdataQuery(params.query, RDConstants.Y_N, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if ( suffix in [
                'accessAudio', 'accessDatabase', 'accessEPub', 'accessPdf', 'accessPlatform', 'accessVideo', 'accessibilityStatementAvailable',
                'playerForAudio', 'playerForVideo', 'serviceProvider', 'softwareProvider', 'status', 'viewerForEpub', 'viewerForPdf'
        ]) {

            _processSimpleRefdataQuery(params.query, suffix, idList, result)
        }
//        else if ( suffix in ['org']) {
//            sharedQuery_platform_org()
//        }
        else if ( suffix in ['primaryUrl']) {
            sharedQuery_platform_primaryUrl()
        }
        else if ( suffix in ['x']) {

            if (params.query in ['platform-x-property']) {

                handleGenericPropertyXQuery(
                        params.query,
                        'select pd.id, pd.name, count(*) from Platform plt join plt.propertySet prop join prop.type pd where plt.id in (:idList)',
                        'select plt.id from Platform plt join plt.propertySet prop join prop.type pd where plt.id in (:idList)',
                        idList,
                        contextService.getOrg(),
                        result
                )
            }
            else if (params.query in ['platform-x-propertyWekb']) {

                List<String> esProperties = BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).base.distribution.getAt('default').getAt(params.query).esProperties ?: []
                List<List> queryList = []
                Set<Long> positiveIdSet = []

                esProperties.each { String esProp ->
                    Map<String, Map> prop = esdConfig.getAt(esProp)
                    Map<String, Object> tmp = [data: [], dataDetails: []]
                    _processESRefdataQuery(esProp, prop.rdc as String, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, tmp)

                    queryList.add([esProp, prop, tmp])
                }
                //println groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(queryList))

                queryList.eachWithIndex { List entry, int idx ->
                    Set<Long> combinedIdSet = []
                    entry[2].dataDetails.each { dd ->
                        if (dd.id != null && dd.id != 0) {
                            combinedIdSet.addAll(dd.idList)
                        }
                    }
                    if (combinedIdSet) {
                        String label = messageSource.getMessage(entry[1].label, null, locale)

                        result.data.add([idx + 1, label, combinedIdSet.size()])
                        result.dataDetails.add([
                                query : params.query,
                                id    : idx + 1, // 0 = NO_COUNTERPART_ID
                                label : label,
                                idList: combinedIdSet.toList(),
                                value1: combinedIdSet.size(),
                                esProperty: entry[0] // ?? TODO
                        ] )
                        positiveIdSet.addAll(combinedIdSet)
                    }
                }

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_DATA_LABEL, (idList - orphanedIdList - positiveIdSet.toList()), result)
                _handleGenericNoCounterpartData_TMP(params.query, orphanedIdList, result)
            }
//            else if (params.query in ['platform-x-org']) {
//                sharedQuery_platform_org()
//            }
            else if (params.query in ['platform-x-provider']) {

                result.data = idList ? Provider.executeQuery(
                        'select pro.id, pro.name, count(*) from Platform plt join plt.provider pro where pro.id in (:providerIdList) and plt.id in (:idList) group by pro.id order by pro.name',
                        [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList]
                ) : []

                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Platform.executeQuery(
                                    'select plt.id from Platform plt join plt.provider pro where plt.id in (:idList) and pro.id = :d order by plt.name',
                                    [idList: idList, d: d[0]]
                            )
                    ])
                }

                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                List<Long> noDataList = nonMatchingIdList ? Platform.executeQuery('select plt.id from Platform plt where plt.id in (:idList)', [idList: nonMatchingIdList]) : []

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_PROVIDER_LABEL, noDataList, result)
            }
            else if (params.query in ['platform-x-primaryUrl']) {
                sharedQuery_platform_primaryUrl()
            }
        }
        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                REFDATA_QUERY[0] + 'from Platform plt join plt.' + refdata + ' ref where plt.id in (:idList)' + REFDATA_QUERY[1],
                'select plt.id from Platform plt join plt.' + refdata + ' ref where plt.id in (:idList) and ref.id = :d order by plt.name',
                'select distinct plt.id from Platform plt where plt.id in (:idList) and plt.'+ refdata + ' is null',
                idList,
                result
        )
    }

    static void _processESRefdataQuery(String query, String rdCategory, Map<String, Object> esRecords, List<Long> orphanedIdList, Map<String, Object> result) {

        Map<String, Object> struct = [:]
        String suffix = query.split('-')[1]

        esRecords.each { it ->
            String key = it.value.get( suffix )
            if (! struct.containsKey(key)) {
                struct.put(key, [])
            }
            struct.get(key).add( Long.parseLong(it.key) )
        }
        struct.eachWithIndex { it, idx ->
            List d = [BaseQuery.FAKE_DATA_ID_1, getChartLabel(BaseQuery.NO_DATA_LABEL), it.value.size()]
            if (it.key) {
                RefdataValue rdv = RefdataValue.getByValueAndCategory(it.key, rdCategory)
                if (rdv) {
                    d = [rdv.id, rdv.getI10n('value'), it.value.size()]
                } else {
                    d = [idx * -1, GenericHelper.flagUnmatched( it.key ), it.value.size()]
                }
            }
            result.data.add( d )
            result.dataDetails.add([
                    query : query,
                    id    : d[0],
                    label : d[1],
                    idList: it.value
            ])
        }
        ElasticSearchHelper.sortResultDataList( result.data )

        _handleGenericNoCounterpartData_TMP(query, orphanedIdList, result)
    }

    static _handleGenericNoCounterpartData_TMP(String query, List<Long> orphanedIdList, Map<String, Object> result) {
        if (orphanedIdList) {
            List d = [BaseQuery.NO_COUNTERPART_ID, getChartLabel(BaseQuery.NO_COUNTERPART_LABEL), orphanedIdList.size()]
            result.data.add( d )

            result.dataDetails.add([
                    query : query,
                    id    : d[0],
                    label : d[1],
                    idList: orphanedIdList
            ])
        }
    }
}
