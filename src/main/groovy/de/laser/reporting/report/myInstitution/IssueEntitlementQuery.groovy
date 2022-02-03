package de.laser.reporting.report.myInstitution


import de.laser.ContextService
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

@Deprecated
class IssueEntitlementQuery extends BaseQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')
        MessageSource messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = LocaleContextHolder.getLocale()

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        def (String prefix, String suffix) = params.query.split('-') // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            handleGenericAllQuery(
                    params.query,
                    'select ie.name, ie.name, count(ie.name) from IssueEntitlement ie where ie.id in (:idList) group by ie.name order by ie.name',
                    'select ie.id from IssueEntitlement ie where ie.id in (:idList) and ie.name = :d order by ie.id',
                    idList,
                    result
            )
        }
        else if (suffix in ['status']) {
            _processSimpleRefdataQuery(params.query, suffix, idList, result)
        }
        else if ( suffix in ['x']) {

            if (params.query in ['issueEntitlement-x-pkg']) {
                // TODO
            }
            else if (params.query in ['issueEntitlement-x-provider']) {
                // TODO
            }
            else if (params.query in ['issueEntitlement-x-platform']) {
                // TODO
            }
            else if (params.query in ['issueEntitlement-x-subscription']) {
                // TODO
            }
        }

        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                PROPERTY_QUERY[0] + 'from IssueEntitlement ie join ie.' + refdata + ' p where ie.id in (:idList)' + PROPERTY_QUERY[1],
                'select ie.id from IssueEntitlement ie join ie.' + refdata + ' p where ie.id in (:idList) and p.id = :d order by ie.name',
                'select distinct ie.id from IssueEntitlement ie where ie.id in (:idList) and ie.'+ refdata + ' is null',
                idList,
                result
        )
    }
}
