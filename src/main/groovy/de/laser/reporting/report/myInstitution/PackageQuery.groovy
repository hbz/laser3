package de.laser.reporting.report.myInstitution

import de.laser.IdentifierNamespace
import de.laser.wekb.Language
import de.laser.wekb.Platform
import de.laser.wekb.Package
import de.laser.wekb.Provider
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.wekb.Vendor
import de.laser.storage.RDConstants
import de.laser.reporting.report.ElasticSearchHelper
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.web.servlet.mvc.GrailsParameterMap

class PackageQuery extends BaseQuery {

    static Map<String, Object> query(GrailsParameterMap params) {

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        def (String prefix, String suffix) = params.query.split('-') // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)
        List<Long> orphanedIdList = BaseFilter.getCachedFilterIdList(prefix + 'Orphaned', params)

        //println 'PackageQuery.query() -> ' + params.query + ' : ' + suffix

        Closure sharedQuery_package_platform = {
            // println 'sharedQuery_package_platform()'
            result.data = idList ? Platform.executeQuery(
                    'select plt.id, plt.name, count(*) from Package pkg join pkg.nominalPlatform plt where plt.id in (:platformIdList) and pkg.id in (:idList) group by plt.id order by plt.name',
                    [platformIdList: BaseFilter.getCachedFilterIdList('platform', params), idList: idList]
            ) : []

            result.data.eachWithIndex { d, i ->
                List<Long> pkgIdList = Package.executeQuery(
                        'select pkg.id from Package pkg join pkg.nominalPlatform plt where pkg.id in (:idList) and plt.id = :d order by pkg.name',
                        [idList: idList, d: d[0]]
                )
                result.dataDetails.add([
                        query : params.query,
                        id    : d[0],
                        label : d[1],
                        idList: pkgIdList
                ])
            }

            List<Long> noDataList = idList ? Package.executeQuery(
                    'select distinct pkg.id from Package pkg where pkg.id in (:idList) and pkg.nominalPlatform is null', [idList: idList]
            ) : []
            handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_PLATFORM_LABEL, noDataList, result)
        }

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            handleGenericAllSignOrphanedQuery(
                    params.query,
                    'select pkg.id, pkg.name, 1, false from Package pkg where pkg.id in (:idList) order by pkg.name',
                    'select pkg.id from Package pkg where pkg.id in (:idList) and pkg.name = :d order by pkg.id',
                    idList,
                    orphanedIdList,
                    result
            )
        }
        else if ( suffix in ['breakable']) {

            _processESRefdataQuery(params.query, RDConstants.PACKAGE_BREAKABLE, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if ( suffix in ['consistent']) {

            _processESRefdataQuery(params.query, RDConstants.PACKAGE_CONSISTENT, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if ( suffix in ['contentType', 'file', 'packageStatus']) {

            _processSimpleRefdataQuery(params.query, suffix, idList, result)
        }
        else if ( suffix in ['openAccess']) {

            _processESRefdataQuery(params.query, RDConstants.LICENSE_OA_TYPE, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if ( suffix in ['paymentType']) {

            _processESRefdataQuery(params.query, RDConstants.PAYMENT_TYPE, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if ( suffix in ['scope']) {

            _processESRefdataQuery(params.query, RDConstants.PACKAGE_SCOPE, BaseFilter.getCachedFilterESRecords(prefix, params), orphanedIdList, result)
        }
        else if ( suffix in ['nominalPlatform']) {

            sharedQuery_package_platform()
        }
        else if ( suffix in ['x']) {

            if (params.query in ['package-x-id']) {

                Map<String, Object> esRecords = BaseFilter.getCachedFilterESRecords(prefix, params)
                Map<String, Object> struct = [:]
                Map<String, Object> helper = [:]
                List<Long> noDataList = []

                esRecords.each { it ->
                    List idfsList = it.value.get('identifiers')
                    idfsList.each { id ->
                        if (! struct.containsKey(id.namespace)) {
                            struct.put(id.namespace, [])
                            helper.put(id.namespace, id)
                        }
                        struct.get(id.namespace).add( Long.parseLong(it.key) )
                    }
                    if (!idfsList) {
                        noDataList.add(Long.parseLong(it.key))
                    }
                }
                struct.eachWithIndex { it, idx ->
                    Map<String, Object> id = helper.get(it.key)
                    IdentifierNamespace ns = IdentifierNamespace.findByNsAndNsType(id.namespace, 'de.laser.wekb.Package')
                    String label = ns ? (ns.getI10n('name') ?: ns.ns) : GenericHelper.flagUnmatched( id.namespaceName ?: id.namespace )
                    List d = [ ns ? ns.id : (idx * -1), label, it.value.size()]

                    result.data.add( d )
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: it.value
                    ])
                }
                ElasticSearchHelper.sortResultDataList( result.data )

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_DATA_LABEL, noDataList, result)
                _handleGenericNoCounterpartData_TMP(params.query, orphanedIdList, result)
            }
            else if (params.query in ['package-x-language']) {

                result.data = idList ? Language.executeQuery(
                        'select lang.id, lang.language.id, count(*) from Package pkg join pkg.languages lang where pkg.id in (:idList) group by lang.id, lang.language.id order by lang.id',
                        [idList: idList]
                ) : []

                result.data.each { d ->
                    d[1] = RefdataValue.get(d[1]).getI10n('value')

                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.languages lang where pkg.id in (:idList) and lang.id = :d order by pkg.name',
                                    [d: d[0], idList: idList]
                            )
                    ])
                }

                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select pkg.id from Package pkg where pkg.id in (:idList)', [idList: nonMatchingIdList]) : []

                handleGenericNonMatchingData2Values_TMP(params.query, BaseQuery.NO_DATA_LABEL, noDataList, result)
            }
            else if (params.query in ['package-x-provider']) {

                result.data = idList ? Provider.executeQuery(
                        'select p.id, p.name, count(*) from Package pkg join pkg.provider p where p.id in (:providerIdList) and pkg.id in (:idList) group by p.id order by p.name',
                        [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList]
                ) : []

                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.provider p where pkg.id in (:idList) and p.id = :d order by pkg.name',
                                    [idList: idList, d: d[0]]
                            )
                    ])
                }

                List<Long> noDataList = Package.executeQuery(
                        'select pkg.id from Package pkg where pkg.id in (:idList) and pkg.provider is null order by pkg.name', [idList: idList]
                )

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_PROVIDER_LABEL, noDataList, result)
            }
            else if (params.query in ['package-x-platform']) {

                sharedQuery_package_platform()
            }
            else if (params.query in ['package-x-platformProvider']) {

                // TODO
                result.data = idList ? Provider.executeQuery(
                        'select pro.id, pro.name, count(*) from Package pkg join pkg.nominalPlatform plt join plt.provider pro ' +
                        'where plt.id in (:platformIdList) and pro.id in (:providerIdList) and pkg.id in (:idList) group by pro.id order by pro.name',
                        [platformIdList: BaseFilter.getCachedFilterIdList('platform', params), providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList]
                ) : []
//                result.data = idList ? Provider.executeQuery(
//                        'select pro.id, pro.name, count(*) from Package pkg join pkg.nominalPlatform plt join plt.provider pro ' +
//                        'where plt.id in (:platformIdList) and pkg.id in (:idList) group by pro.id order by pro.name',
//                        [platformIdList: BaseFilter.getCachedFilterIdList('platform', params), idList: idList]
//                ) : []

                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.nominalPlatform plt join plt.provider pro where pkg.id in (:idList) and pro.id = :d order by pkg.name',
                                    [idList: idList, d: d[0]]
                            )
                    ])
                }

                List<Long> noDataList = idList ? Package.executeQuery(
                        'select distinct pkg.id from Package pkg where pkg.id in (:idList) and (pkg.nominalPlatform is null or pkg.nominalPlatform.provider is null)', [idList: idList]
                ) : []
                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_PLATFORM_PROVIDER_LABEL, noDataList, result)
            }
            else if (params.query in ['package-x-curatoryGroup']) {

                Map<String, Object> esRecords = BaseFilter.getCachedFilterESRecords(prefix, params)
                Map<String, Object> struct = [:]
                Map<String, Object> helper = [:]
                List<Long> noDataList = []

                esRecords.each { it ->
                    List cgList = it.value.get('curatoryGroups')
                    cgList.each { cg ->
                        if (! struct.containsKey(cg.curatoryGroup)) {
                            struct.put(cg.curatoryGroup, [])
                            helper.put(cg.curatoryGroup, cg)
                        }
                        struct.get(cg.curatoryGroup).add( Long.parseLong(it.key) )
                    }
                    if (!cgList) {
                        noDataList.add(Long.parseLong(it.key))
                    }
                }
                struct.each {
                    Map<String, Object> cg = helper.get(it.key)
                    String cgType = cg.type ? (RefdataValue.getByValueAndCategory(cg.type as String, RDConstants.CURATORY_GROUP_TYPE)?.getI10n('value') ?: cg.type) : null
                    List d = [Long.parseLong(cg.curatoryGroup.split(':')[1]), cg.name + ( cgType ? ' (' + cgType + ')' : '' ), it.value.size()]
                    result.data.add( d )
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: it.value
                    ])
                }
                ElasticSearchHelper.sortResultDataList( result.data )

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_DATA_LABEL, noDataList, result)
                _handleGenericNoCounterpartData_TMP(params.query, orphanedIdList, result)
            }
            else if (params.query in ['package-x-ddc']) {

                Map<String, Object> esRecords = BaseFilter.getCachedFilterESRecords(prefix, params)
                Map<String, Object> struct = [:]
                Map<String, Object> helper = [:]
                List<Long> noDataList = []

                esRecords.each { it ->
                    List ddcList = it.value.get('ddcs')
                    ddcList.each { ddc ->
                        if (! struct.containsKey(ddc.value)) {
                            struct.put(ddc.value, [])
                            helper.put(ddc.value, ddc)
                        }
                        struct.get(ddc.value).add( Long.parseLong(it.key) )
                    }
                    if (!ddcList) {
                        noDataList.add(Long.parseLong(it.key))
                    }
                }
                struct.eachWithIndex { it, idx ->
                    Map<String, Object> ddc = helper.get(it.key)
                    List d = [idx * -1,  GenericHelper.flagUnmatched( ddc.value_de ), it.value.size()]
                    RefdataValue rdv = RefdataValue.getByValueAndCategory(ddc.value as String, RDConstants.DDC)
                    if (rdv) {
                        d = [rdv.id, rdv.getI10n('value'), it.value.size()]
                    }
                    result.data.add( d )
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: it.value
                    ])
                }
                ElasticSearchHelper.sortResultDataList( result.data )

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_DATA_LABEL, noDataList, result)
                _handleGenericNoCounterpartData_TMP(params.query, orphanedIdList, result)
            }
            else if (params.query in ['package-x-archivingAgency']) {

                Map<String, Object> esRecords = BaseFilter.getCachedFilterESRecords(prefix, params)
                Map<String, Object> struct = [:]
                Map<String, Object> helper = [:]
                List<Long> noDataList = []

                esRecords.each { it ->
                    List cgList = it.value.get('packageArchivingAgencies')
                    cgList.each { aa ->
                        if (! struct.containsKey(aa.archivingAgency)) {
                            struct.put(aa.archivingAgency, [])
                            helper.put(aa.archivingAgency, aa)
                        }
                        struct.get(aa.archivingAgency).add( Long.parseLong(it.key) )
                    }
                    if (!cgList) {
                        noDataList.add(Long.parseLong(it.key))
                    }
                }
                struct.eachWithIndex {it, idx ->
                    Map<String, Object> aa = helper.get(it.key)
                    List d = [idx + 1, aa.archivingAgency, it.value.size()]
                    result.data.add( d )
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: it.value
                    ])
                }
                ElasticSearchHelper.sortResultDataList( result.data )

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_DATA_LABEL, noDataList, result)
                _handleGenericNoCounterpartData_TMP(params.query, orphanedIdList, result)
            }
            else if (params.query in ['package-x-nationalRange']) {

                Map<String, Object> esRecords = BaseFilter.getCachedFilterESRecords(prefix, params)
                Map<String, Object> struct = [:]
                Map<String, Object> helper = [:]
                List<Long> noDataList = []

                esRecords.each { it ->
                    List nrList = it.value.get('nationalRanges')
                    nrList.each { nr ->
                        if (! struct.containsKey(nr.value)) {
                            struct.put(nr.value, [])
                            helper.put(nr.value, nr)
                        }
                        struct.get(nr.value).add( Long.parseLong(it.key) )
                    }
                    if (!nrList) {
                        noDataList.add(Long.parseLong(it.key))
                    }
                }
                struct.eachWithIndex { it, idx ->
                    Map<String, Object> nr = helper.get(it.key)
                    List d = [idx * -1, GenericHelper.flagUnmatched( nr.value ), it.value.size()]
                    RefdataValue rdv = RefdataValue.getByValueAndCategory(nr.value as String, RDConstants.COUNTRY)
                    if (rdv) {
                        d = [rdv.id, rdv.getI10n('value'), it.value.size()]
                    }
                    result.data.add( d )
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: it.value
                    ])
                }
                ElasticSearchHelper.sortResultDataList( result.data )

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_DATA_LABEL, noDataList, result)
                _handleGenericNoCounterpartData_TMP(params.query, orphanedIdList, result)
            }
            else if (params.query in ['package-x-regionalRange']) {

                Map<String, Object> esRecords = BaseFilter.getCachedFilterESRecords(prefix, params)
                Map<String, Object> struct = [:]
                Map<String, Object> helper = [:]
                List<Long> noDataList = []

                esRecords.each { it ->
                    List rrList = it.value.get( 'regionalRanges' )
                    rrList.each { nr ->
                        if (! struct.containsKey(nr.value)) {
                            struct.put(nr.value, [])
                            helper.put(nr.value, nr)
                        }
                        struct.get(nr.value).add( Long.parseLong(it.key) )
                    }
                    if (!rrList) {
                        noDataList.add(Long.parseLong(it.key))
                    }
                }
                struct.eachWithIndex { it, idx ->
                    Map<String, Object> nr = helper.get(it.key)
                    List d = [idx * -1, GenericHelper.flagUnmatched( nr.value ), it.value.size()]
                    RefdataValue rdv = RefdataValue.getByValueAndCategory(nr.value as String, RDConstants.REGIONS_DE)
                    if (rdv) {
                        d = [rdv.id, rdv.getI10n('value'), it.value.size()]
                    }
                    result.data.add( d )
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: it.value
                    ])
                }
                ElasticSearchHelper.sortResultDataList( result.data )

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_DATA_LABEL, noDataList, result)
                _handleGenericNoCounterpartData_TMP(params.query, orphanedIdList, result)
            }
            else if (params.query in ['package-x-vendor']) {

                result.data = idList ? Vendor.executeQuery(
                        'select ven.id, ven.name, count(*) from PackageVendor pv join pv.vendor ven where ven.id in (:vendorIdList) and pv.pkg.id in (:idList) group by ven.id order by ven.name',
                        [vendorIdList: BaseFilter.getCachedFilterIdList('vendor', params), idList: idList]
                ) : []

                result.data.eachWithIndex { d, i ->
                    List<Long> pkgIdList = Package.executeQuery(
                            'select pkg.id from PackageVendor pv join pv.pkg pkg where pkg.id in (:idList) and pv.vendor.id = :d order by pkg.name',
                            [idList: idList, d: d[0]]
                    )
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: pkgIdList
                    ])
                }

                List<Long> noDataList = idList ? Package.executeQuery(
                        'select distinct pkg.id from Package pkg where pkg.id in (:idList) and not exists (select pv from PackageVendor pv where pv.pkg.id = pkg.id)', [idList: idList]
                ) : []
                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_VENDOR_LABEL, noDataList, result)
            }
        }
        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                REFDATA_QUERY[0] + 'from Package pkg join pkg.' + refdata + ' ref where pkg.id in (:idList)' + REFDATA_QUERY[1],
                'select pkg.id from Package pkg join pkg.' + refdata + ' ref where pkg.id in (:idList) and ref.id = :d order by pkg.name',
                'select distinct pkg.id from Package pkg where pkg.id in (:idList) and pkg.'+ refdata + ' is null',
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
