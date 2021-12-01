package de.laser.reporting.report.myInstitution

import de.laser.*
import de.laser.helper.RDConstants
import de.laser.reporting.report.ElasticSearchHelper
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

class PlatformQuery extends BaseQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        String prefix = params.query.split('-')[0]
        String suffix = params.query.split('-')[1] // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)
        List<Long> orphanedIdList = BaseFilter.getCachedFilterIdList(prefix + 'Orphaned', params)

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            handleGenericAllQuery(
                    params.query,
                    'select plt.name, plt.name, count(plt.name) from Platform plt where plt.id in (:idList) group by plt.name order by plt.name',
                    'select plt.id from Platform plt where plt.id in (:idList) and plt.name = :d order by plt.id',
                    idList,
                    result
            )
        }
        else if (suffix in ['ipAuthentication']) {

            _processESRefdataQuery(params.query, RDConstants.IP_AUTHENTICATION, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if ( suffix in ['org']) {

            // TODO
            handleGenericAllQuery(
                    params.query,
                    'select org.name, org.name, count(*) from Platform plt join plt.org org where plt.id in (:idList) group by org.name order by org.name',
                    'select plt.id from Platform plt where plt.id in (:idList) and plt.org.name = :d order by plt.name',
                    idList,
                    result
            )
            handleGenericNonMatchingData( params.query, 'select plt.id from Platform plt where plt.id in (:idList) and plt.org is null order by plt.name', idList, result )
        }
        else if (suffix in ['passwordAuthentication', 'proxySupported', 'shibbolethAuthentication']) {

            _processESRefdataQuery(params.query, RDConstants.Y_N, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if (suffix in ['statisticsFormat']) {

            _processESRefdataQuery(params.query, RDConstants.PLATFORM_STATISTICS_FORMAT, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if (suffix in ['statisticsUpdate']) {

            _processESRefdataQuery(params.query, RDConstants.PLATFORM_STATISTICS_FREQUENCY, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if (suffix in ['counterCertified', 'counterR3Supported', 'counterR4Supported', 'counterR4SushiApiSupported', 'counterR5Supported', 'counterR5SushiApiSupported']) {

            _processESRefdataQuery(params.query, RDConstants.Y_N, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if ( suffix in ['serviceProvider', 'softwareProvider', 'status']) {

            _processSimpleRefdataQuery(params.query, suffix, idList, result)
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
        }
        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                PROPERTY_QUERY[0] + 'from Platform plt join plt.' + refdata + ' p where plt.id in (:idList)' + PROPERTY_QUERY[1],
                'select plt.id from Platform plt join plt.' + refdata + ' p where plt.id in (:idList) and p.id = :d order by plt.name',
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
            List d = [BaseQuery.NO_DATA_ID, getMessage(BaseQuery.NO_DATA_LABEL), it.value.size()]
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
            List d = [BaseQuery.NO_COUNTERPART_ID, getMessage(BaseQuery.NO_COUNTERPART_LABEL), orphanedIdList.size()]
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
