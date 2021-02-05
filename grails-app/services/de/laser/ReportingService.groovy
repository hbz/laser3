package de.laser

import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class ReportingService {

    def contextService
    def filterService
    def subscriptionsQueryService

    static String filterPrefix = 'filter:'

    static Map<String, Object> config = [
            'Object' : [
                    properties: [ ],
                    refdata: [ ]
            ],

            // ---

            'Organisation' : [
                    meta : [
                            class: Org
                    ],
                    refdata: [
                            'country', 'region', 'libraryType', 'libraryNetwork', 'funderType', 'funderHskType'
                    ]
            ],

            'Subscription' : [
                    meta : [
                            class: Subscription
                    ],
                    properties: [
                            'startDate', 'endDate', 'manualRenewalDate', 'manualCancellationDate'
                    ],
                    refdata: [
                            'form', 'kind', 'status', 'type'
                    ]
            ]
    ]

    Map<String, Object> organisationFilter(GrailsParameterMap params) {
        Map<String, Object> result      = [:]

        params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value // TODO - manipulation of params

        Map<String, Object> fsq = filterService.getOrgComboQuery(params, contextService.getOrg())

        Map<String, Object> queryParams = [ orgIdList: Org.executeQuery("select o.id " + fsq.query.minus("select o "), fsq.queryParams)]
        List<String> queryParts         = [ 'select org.id from Org org']
        List<String> whereParts         = [ 'where org.id in (:orgIdList)']

        String cmbKey = ReportingService.filterPrefix + 'org_'
        int pCount = 0

        Set<String> keys = params.keySet().findAll{ it.toString().startsWith(cmbKey) }
        keys.each { key ->
            println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')

                if (p in ReportingService.config.Organisation.properties) {
                    whereParts.add( 'org.' + p + ' = :p' + (++pCount) )
                    if (Org.getDeclaredField(p).getType() == Date) {
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                    }
                }
                else if (p in ReportingService.config.Organisation.refdata) {
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

    Map<String, Object> subscriptionFilter(GrailsParameterMap params) {
        Map<String, Object> result      = [:]

        List baseQuery                  = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([validOn: null], contextService.getOrg())
        Map<String, Object> queryParams = [ subIdList: Subscription.executeQuery( 'select s.id ' + baseQuery[0], baseQuery[1]) ]
        List<String> queryParts         = [ 'select sub.id from Subscription sub']
        List<String> whereParts         = [ 'where sub.id in (:subIdList)']

        String cmbKey = ReportingService.filterPrefix + 'subscription_'
        int pCount = 0

        Set<String> keys = params.keySet().findAll{ it.toString().startsWith(cmbKey) }
        keys.each{ key ->
            if (params.get(key)) {
                println key + " >> " + params.get(key)

                String p = key.replaceFirst(cmbKey,'')

                if (p in ReportingService.config.Subscription.properties) {
                    whereParts.add( 'sub.' + p + ' = :p' + (++pCount) )
                    if (Subscription.getDeclaredField(p).getType() == Date) {
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                    }
                }
                else if (p in ReportingService.config.Subscription.refdata) {
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

        String cmbKey = ReportingService.filterPrefix + partKey + '_'
        int pCount = 0

        Set<String> keys = params.keySet().findAll{ it.toString().startsWith(cmbKey) }
        keys.each { key ->
            println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')

                if (p in ReportingService.config.Organisation.properties) {
                    whereParts.add( 'org.' + p + ' = :p' + (++pCount) )
                    if (Org.getDeclaredField(p).getType() == Date) {
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                    }
                }
                else if (p in ReportingService.config.Organisation.refdata) {
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
