package de.laser.reporting

import de.laser.Org
import de.laser.Subscription
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.ApplicationContext

class SubscriptionFilter {

    def contextService
    def filterService
    def subscriptionsQueryService

    SubscriptionFilter() {
        ApplicationContext mainContext  = Holders.grailsApplication.mainContext
        contextService                  = mainContext.getBean('contextService')
        filterService                   = mainContext.getBean('filterService')
        subscriptionsQueryService       = mainContext.getBean('subscriptionsQueryService')
    }

    Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> result      = [:]

        List baseQuery                  = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([validOn: null], contextService.getOrg())
        Map<String, Object> queryParams = [ subIdList: Subscription.executeQuery( 'select s.id ' + baseQuery[0], baseQuery[1]) ]
        List<String> queryParts         = [ 'select sub.id from Subscription sub']
        List<String> whereParts         = [ 'where sub.id in (:subIdList)']

        String cmbKey = Cfg.filterPrefix + 'subscription_'
        int pCount = 0

        Set<String> keys = params.keySet().findAll{ it.toString().startsWith(cmbKey) }
        keys.each{ key ->
            if (params.get(key)) {
                println key + " >> " + params.get(key)

                String p = key.replaceFirst(cmbKey,'')

                if (p in Cfg.config.Subscription.properties) {
                    whereParts.add( 'sub.' + p + ' = :p' + (++pCount) )
                    if (Subscription.getDeclaredField(p).getType() == Date) {
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                    }
                }
                else if (p in Cfg.config.Subscription.refdata) {
                    whereParts.add( 'sub.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

        println query
        // println queryParams
        // println whereParts

        result.subIdList = Subscription.executeQuery( query, queryParams )

        result.memberIdList   = internalOrgFilter(params, 'member', result.subIdList)
        result.providerIdList = internalOrgFilter(params, 'provider', result.subIdList)

        println 'subscriptions >> ' + result.subIdList.size()
        println 'member >> ' + result.memberIdList.size()
        println 'provider >> ' + result.providerIdList.size()

        result
    }

    private List<Long> internalOrgFilter(GrailsParameterMap params, String partKey, List<Long> subIdList) {

        String queryBase = 'select distinct (org.id) from Org org join org.links orgLink'
        List<String> whereParts = [ 'orgLink.roleType in (:roleTypes)', 'orgLink.sub.id in (:subIdList)' ]
        Map<String, Object> queryParams = [ 'subIdList': subIdList ]

        if (partKey == 'member') {
            queryParams.put( 'roleTypes', [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN] ) // TODO <- RDStore.OR_SUBSCRIBER
            // check ONLY members
            queryParams.subIdList = Subscription.executeQuery(
                    'select distinct (sub.id) from Subscription sub where sub.instanceOf.id in (:subIdList)',
                    [ subIdList: queryParams.subIdList ]
            )
        }
        if (partKey == 'provider') {
            queryParams.put( 'roleTypes', [RDStore.OR_PROVIDER] )
        }

        String cmbKey = Cfg.filterPrefix + partKey + '_'
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

        String query = queryBase + ' where ' + whereParts.join(' and ')

        println query

        Org.executeQuery(query, queryParams)
    }
}
