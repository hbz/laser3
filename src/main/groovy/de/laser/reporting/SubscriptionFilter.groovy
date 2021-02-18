package de.laser.reporting

import de.laser.Org
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.ApplicationContext

class SubscriptionFilter extends GenericFilter {

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
        Map<String, Object> result      = [ filterLabels : [:] ]

        List<String> queryParts         = [ 'select sub.id from Subscription sub']
        List<String> whereParts         = [ 'where sub.id in (:subIdList)']
        Map<String, Object> queryParams = [ subIdList : [] ]

        String filterSource = params.get(GenericConfig.FILTER_PREFIX + 'subscription' + GenericConfig.FILTER_SOURCE_POSTFIX)
        result.filterLabels.put('base', [source: getFilterSourceLabel(SubscriptionConfig.CONFIG.base, filterSource)])

        switch (filterSource) {
            case 'all-sub':
                queryParams.subIdList = Subscription.executeQuery( 'select s.id from Subscription s' )
                break
            case 'my-sub':
                List tmp = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery( [validOn: null], contextService.getOrg() )
                println tmp
                queryParams.subIdList = Subscription.executeQuery( 'select s.id ' + tmp[0], tmp[1])
                break
        }

        String cmbKey = GenericConfig.FILTER_PREFIX + 'subscription_'
        int pCount = 0

        Set<String> keys = params.keySet().findAll{ it.toString().startsWith(cmbKey) && ! it.toString().endsWith(GenericConfig.FILTER_SOURCE_POSTFIX) }
        keys.each{ key ->
            if (params.get(key)) {
                println key + " >> " + params.get(key)

                String p = key.replaceFirst(cmbKey,'')
                String pType = getFilterFieldType(SubscriptionConfig.CONFIG.base, p)

                result.filterLabels.get('base').put(p, getFilterFieldLabel(SubscriptionConfig.CONFIG.base, p))

                // --> generic properties
                if (pType == GenericConfig.FIELD_TYPE_PROPERTY) {
                    if (Subscription.getDeclaredField(p).getType() == Date) {

                        String modifier = params.get(key + '_modifier')
                        if (modifier == 'lower') {
                            whereParts.add( 'sub.' + p + ' < :p' + (++pCount) )
                        }
                        else if (modifier == 'greater') {
                            whereParts.add( 'sub.' + p + ' > :p' + (++pCount) )
                        }
                        else {
                            whereParts.add( 'sub.' + p + ' = :p' + (++pCount) )
                        }
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )
                    }
                    else if (Subscription.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        if (RefdataValue.get(params.get(key)) == RDStore.YN_YES) {
                            whereParts.add( 'sub.' + p + ' is true' )
                        }
                        else if (RefdataValue.get(params.get(key)) == RDStore.YN_NO) {
                            whereParts.add( 'sub.' + p + ' is false' )
                        }
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                    }
                }
                // --> generic refdata
                else if (pType == GenericConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'sub.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )
                }
                // --> refdata relation tables
                else if (pType == GenericConfig.FIELD_TYPE_REFDATA_RELTABLE) {
                    println ' ------------ not implemented ------------ '
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println 'SubscriptionFilter.filter() -->'
//        println query
//        println queryParams
//        println whereParts

        result.subIdList = Subscription.executeQuery( query, queryParams )

        handleInternalOrgFilter(params, 'member', result)
        handleInternalOrgFilter(params, 'provider', result)

//        println 'subscriptions >> ' + result.subIdList.size()
//        println 'member >> ' + result.memberIdList.size()
//        println 'provider >> ' + result.providerIdList.size()

        result
    }

    private void handleInternalOrgFilter(GrailsParameterMap params, String partKey, Map<String, Object> result) {

        String filterSource = params.get(GenericConfig.FILTER_PREFIX + partKey + GenericConfig.FILTER_SOURCE_POSTFIX)
        result.filterLabels.put(partKey, [source: getFilterSourceLabel(SubscriptionConfig.CONFIG.get(partKey), filterSource)])

        //println 'internalOrgFilter() ' + params + ' >>>>>>>>>>>>>>>< ' + partKey
        if (! result.subIdList) {
            result.put( partKey + 'IdList', [] )
            return
        }

        String queryBase = 'select distinct (org.id) from Org org join org.links orgLink'
        List<String> whereParts = [ 'orgLink.roleType in (:roleTypes)', 'orgLink.sub.id in (:subIdList)' ]
        Map<String, Object> queryParams = [ 'subIdList': result.subIdList ]

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

        String cmbKey = GenericConfig.FILTER_PREFIX + partKey + '_'
        int pCount = 0

        Set<String> keys = params.keySet().findAll{ it.toString().startsWith(cmbKey) && ! it.toString().endsWith(GenericConfig.FILTER_SOURCE_POSTFIX) }
        keys.each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType
                if (partKey == 'member') {
                    pType = getFilterFieldType(SubscriptionConfig.CONFIG.member, p)

                    result.filterLabels.get(partKey).put(p, getFilterFieldLabel(SubscriptionConfig.CONFIG.member, p))
                }
                else if (partKey == 'provider') {
                    pType = getFilterFieldType(SubscriptionConfig.CONFIG.provider, p)

                    result.filterLabels.get(partKey).put(p, getFilterFieldLabel(SubscriptionConfig.CONFIG.provider, p))
                }

                // --> properties generic
                if (pType == GenericConfig.FIELD_TYPE_PROPERTY) {

                    if (Org.getDeclaredField(p).getType() == Date) {
                        String modifier = params.get(key + '_modifier')

                        if (modifier == 'lower') {
                            whereParts.add( 'org.' + p + ' < :p' + (++pCount) )
                        }
                        else if (modifier == 'greater') {
                            whereParts.add( 'org.' + p + ' > :p' + (++pCount) )
                        }
                        else {
                            whereParts.add( 'org.' + p + ' = :p' + (++pCount) )
                        }
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )
                    }
                    else if (Org.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        if (RefdataValue.get(params.get(key)) == RDStore.YN_YES) {
                            whereParts.add( 'org.' + p + ' is true' )
                        }
                        else if (RefdataValue.get(params.get(key)) == RDStore.YN_NO) {
                            whereParts.add( 'org.' + p + ' is false' )
                        }
                    }
                    else {
                        whereParts.add( 'org.' + p + ' = :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, params.get(key) )
                    }
                }
                // --> refdata generic
                else if (pType == GenericConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'org.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )
                }
                // --> refdata relation tables
                else if (pType == GenericConfig.FIELD_TYPE_REFDATA_RELTABLE) {
                    if (p == 'subjectGroup') {
                        queryBase = queryBase + ' join org.subjectGroup osg join osg.subjectGroup rdvsg'
                        whereParts.add('rdvsg.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))
                    }
                }
            }
        }

        String query = queryBase + ' where ' + whereParts.join(' and ')

//        println 'SubscriptionFilter.internalOrgFilter() -->'
//        println query

        result.put( partKey + 'IdList', Org.executeQuery(query, queryParams) )
    }
}
