package de.laser.reporting

import de.laser.Org
import de.laser.Subscription
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.ApplicationContext

class OrganisationFilter {

    def contextService
    def filterService
    def subscriptionsQueryService

    OrganisationFilter() {
        ApplicationContext mainContext  = Holders.grailsApplication.mainContext
        contextService                  = mainContext.getBean('contextService')
        filterService                   = mainContext.getBean('filterService')
        subscriptionsQueryService       = mainContext.getBean('subscriptionsQueryService')
    }

    Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> result      = [:]

        params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value // TODO - manipulation of params

        Map<String, Object> fsq = filterService.getOrgComboQuery(params, contextService.getOrg())

        Map<String, Object> queryParams = [ orgIdList: Org.executeQuery("select o.id " + fsq.query.minus("select o "), fsq.queryParams)]
        List<String> queryParts         = [ 'select org.id from Org org']
        List<String> whereParts         = [ 'where org.id in (:orgIdList)']

        String cmbKey = Cfg.filterPrefix + 'org_'
        int pCount = 0

        Set<String> keys = params.keySet().findAll{ it.toString().startsWith(cmbKey) }
        keys.each { key ->
            println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')

                if (p in Cfg.config.Organisation.properties) {
                    whereParts.add( 'org.' + p + ' = :p' + (++pCount) )
                    if (Org.getDeclaredField(p).getType() == Date) {
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                    }
                }
                else if (p in Cfg.config.Organisation.refdata) {
                    whereParts.add( 'org.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

        println query
        println queryParams
        println whereParts

        result.orgIdList = Subscription.executeQuery( query, queryParams )

        println 'orgs >> ' + result.orgIdList.size()

        result
    }
}
