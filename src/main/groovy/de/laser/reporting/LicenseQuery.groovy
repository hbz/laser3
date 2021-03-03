package de.laser.reporting

import de.laser.ContextService
import de.laser.Org
import de.laser.properties.PropertyDefinition
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

class LicenseQuery extends GenericQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        Map<String, Object> result = [
                chart    : params.chart,
                query    : params.query,
                data     : [],
                dataDetails : []
        ]

        String prefix = params.query.split('-')[0]
        List idList = params.list(prefix + 'IdList').collect { it as Long }

        if (! idList) {
        }
        else if ( params.query in ['license-licenseCategory']) {

            processSimpleRefdataQuery(params.query,'licenseCategory', idList, result)
        }
        else if ( params.query in ['license-type']) {

            processSimpleRefdataQuery(params.query,'type', idList, result)
        }
        else if ( params.query in ['license-status']) {

            processSimpleRefdataQuery(params.query,'status', idList, result)
        }
        else if ( params.query in ['license-property-assignment']) {

            handleGenericPropertyAssignmentQuery(
                    params.query,
                    'select pd.id, pd.name, count(*) from License lic join lic.propertySet prop join prop.type pd where lic.id in (:idList)',
                    'select lic.id from License lic join lic.propertySet prop join prop.type pd where lic.id in (:idList)',
                    idList,
                    contextService.getOrg(),
                    result
            )
        }
        else if ( params.query in ['license-identifier-assignment']) {

            handleGenericIdentifierAssignmentQuery(
                    params.query,
                    'select ns.id, ns.ns, count(*) from License lic join lic.ids ident join ident.ns ns where lic.id in (:idList)',
                    'select lic.id from License lic join lic.ids ident join ident.ns ns where lic.id in (:idList) ',
                    idList,
                    result
            )
        }

        result
    }

    static void processSimpleRefdataQuery(String query, String refdata, List idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                PROPERTY_QUERY[0] + 'from License l join l.' + refdata + ' p where l.id in (:idList)' + PROPERTY_QUERY[1],
                'select l.id from License l join l.' + refdata + ' p where l.id in (:idList) and p.id = :d order by l.reference',
                'select distinct l.id from License l where l.id in (:idList) and l.'+ refdata + ' is null',
                idList,
                result
        )
    }
}
