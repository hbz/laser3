package de.laser.reporting.report.myInstitution

import de.laser.Platform
import de.laser.Package
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.web.servlet.mvc.GrailsParameterMap

class PackageQuery extends BaseQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static Map<String, Object> query(GrailsParameterMap params) {

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        String prefix = params.query.split('-')[0]
        String suffix = params.query.split('-')[1] // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            println '--- TODO ---'
        }
        else if ( suffix in ['breakable']) {

            _processSimpleRefdataQuery(params.query, 'breakable', idList, result)
        }
        else if ( suffix in ['consistent']) {

            _processSimpleRefdataQuery(params.query, 'consistent', idList, result)
        }
        else if ( suffix in ['contentType']) {

            _processSimpleRefdataQuery(params.query, 'contentType', idList, result)
        }
        else if ( suffix in ['file']) {

            _processSimpleRefdataQuery(params.query, 'file', idList, result)
        }
        else if ( suffix in ['packageStatus']) {

            _processSimpleRefdataQuery(params.query, 'packageStatus', idList, result)
        }
        else if ( suffix in ['scope']) {

            _processSimpleRefdataQuery(params.query, 'scope', idList, result)
        }
        else if ( suffix in ['x']) {

            if (params.query in ['package-x-platform']) {

                result.data = Platform.executeQuery(
                        'select p.id, p.name, count(*) from Package pkg join pkg.nominalPlatform p where pkg.id in (:idList) group by p.id order by p.name',
                        [idList: idList]
                )

                result.data.eachWithIndex { d, i ->
                    List<Long> pkgIdList = Package.executeQuery(
                            'select pkg.id from Package pkg join pkg.nominalPlatform p where pkg.id in (:idList) and p.id = :d order by pkg.name',
                            [idList: idList, d: d[0]]
                    )
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: pkgIdList
                    ])
                }

                handleGenericNonMatchingData(
                        params.query,
                        'select distinct pkg.id from Package pkg where pkg.id in (:idList) and pkg.nominalPlatform is null',
                        idList,
                        result
                )
            }
        }

        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                PROPERTY_QUERY[0] + 'from Package pkg join pkg.' + refdata + ' p where pkg.id in (:idList)' + PROPERTY_QUERY[1],
                'select pkg.id from Package pkg join pkg.' + refdata + ' p where pkg.id in (:idList) and p.id = :d order by pkg.name',
                'select distinct pkg.id from Package pkg where pkg.id in (:idList) and pkg.'+ refdata + ' is null',
                idList,
                result
        )
    }
}
