package de.laser.reporting

import de.laser.Org
import de.laser.OrgSetting
import de.laser.OrgSubjectGroup
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.auth.Role
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

        List<String> queryParts         = [ 'select distinct (sub.id) from Subscription sub']
        List<String> whereParts         = [ 'where sub.id in (:subscriptionIdList)']
        Map<String, Object> queryParams = [ subscriptionIdList : [] ]

        String filterSource = params.get(GenericConfig.FILTER_PREFIX + 'subscription' + GenericConfig.FILTER_SOURCE_POSTFIX)
        result.filterLabels.put('base', [source: getFilterSourceLabel(SubscriptionConfig.CONFIG.base, filterSource)])

        switch (filterSource) {
            case 'all-sub':
                queryParams.subscriptionIdList = Subscription.executeQuery( 'select s.id from Subscription s' )
                break
            case 'my-sub':
                List tmp = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery( [validOn: null], contextService.getOrg() )
                //println tmp
                queryParams.subscriptionIdList = Subscription.executeQuery( 'select s.id ' + tmp[0], tmp[1])
                break
        }

        String cmbKey = GenericConfig.FILTER_PREFIX + 'subscription_'
        int pCount = 0

        Set<String> keys = params.keySet().findAll{ it.toString().startsWith(cmbKey) && ! it.toString().endsWith(GenericConfig.FILTER_SOURCE_POSTFIX) }
        keys.each{ key ->
            if (params.get(key)) {
                //println key + " >> " + params.get(key)

                String p = key.replaceFirst(cmbKey,'')
                String pType = getFilterFieldType(SubscriptionConfig.CONFIG.base, p)

                def filterLabelValue

                // --> generic properties
                if (pType == GenericConfig.FIELD_TYPE_PROPERTY) {
                    if (Subscription.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'sub.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Subscription.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        if (RefdataValue.get(params.get(key)) == RDStore.YN_YES) {
                            whereParts.add( 'sub.' + p + ' is true' )
                        }
                        else if (RefdataValue.get(params.get(key)) == RDStore.YN_NO) {
                            whereParts.add( 'sub.' + p + ' is false' )
                        }
                        filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                        filterLabelValue = params.get(key)
                    }
                }
                // --> generic refdata
                else if (pType == GenericConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'sub.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                }
                // --> refdata relation tables
                else if (pType == GenericConfig.FIELD_TYPE_REFDATA_RELTABLE) {
                    println ' ------------ not implemented ------------ '
                }
                // --> custom filter implementation
                else if (pType == GenericConfig.FIELD_TYPE_CUSTOM_IMPL) {
                    println ' ------------ not implemented ------------ '
                }

                if (filterLabelValue) {
                    result.filterLabels.get('base').put(p, [label: getFilterFieldLabel(SubscriptionConfig.CONFIG.base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

        println 'SubscriptionFilter.filter() -->'
        println query
        println queryParams
        println whereParts

        result.subscriptionIdList = Subscription.executeQuery( query, queryParams )

        handleInternalOrgFilter(params, 'member', result)
        handleInternalOrgFilter(params, 'provider', result)

//        println 'subscriptions >> ' + result.subscriptionIdList.size()
//        println 'member >> ' + result.memberIdList.size()
//        println 'provider >> ' + result.providerIdList.size()

        result
    }

    private void handleInternalOrgFilter(GrailsParameterMap params, String partKey, Map<String, Object> result) {

        String filterSource = params.get(GenericConfig.FILTER_PREFIX + partKey + GenericConfig.FILTER_SOURCE_POSTFIX)
        result.filterLabels.put(partKey, [source: getFilterSourceLabel(SubscriptionConfig.CONFIG.get(partKey), filterSource)])

        //println 'internalOrgFilter() ' + params + ' >>>>>>>>>>>>>>>< ' + partKey
        if (! result.subscriptionIdList) {
            result.put( partKey + 'IdList', [] )
            return
        }

        String queryBase = 'select distinct (org.id) from Org org join org.links orgLink'
        List<String> whereParts = [ 'orgLink.roleType in (:roleTypes)', 'orgLink.sub.id in (:subscriptionIdList)' ]
        Map<String, Object> queryParams = [ 'subscriptionIdList': result.subscriptionIdList ]

        if (partKey == 'member') {
            queryParams.put( 'roleTypes', [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN] ) // TODO <- RDStore.OR_SUBSCRIBER
            // check ONLY members
            queryParams.subscriptionIdList = Subscription.executeQuery(
                    'select distinct (sub.id) from Subscription sub where sub.instanceOf.id in (:subscriptionIdList)',
                    [ subscriptionIdList: queryParams.subscriptionIdList ]
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
                }
                else if (partKey == 'provider') {
                    pType = getFilterFieldType(SubscriptionConfig.CONFIG.provider, p)
                }

                def filterLabelValue

                // --> properties generic
                if (pType == GenericConfig.FIELD_TYPE_PROPERTY) {

                    if (Org.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'org.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Org.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        if (RefdataValue.get(params.get(key)) == RDStore.YN_YES) {
                            whereParts.add( 'org.' + p + ' is true' )
                        }
                        else if (RefdataValue.get(params.get(key)) == RDStore.YN_NO) {
                            whereParts.add( 'org.' + p + ' is false' )
                        }
                        filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                    }
                    else {
                        whereParts.add( 'org.' + p + ' = :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, params.get(key) )

                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == GenericConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'org.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                }
                // --> refdata relation tables
                else if (pType == GenericConfig.FIELD_TYPE_REFDATA_RELTABLE) {

                    if (p == GenericConfig.CUSTOM_KEY_SUBJECT_GROUP) {
                        queryBase = queryBase + ' join org.subjectGroup osg join osg.subjectGroup rdvsg'
                        whereParts.add('rdvsg.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                    }
                }
                // --> custom filter implementation
                else if (pType == GenericConfig.FIELD_TYPE_CUSTOM_IMPL) {

                    if (p == GenericConfig.CUSTOM_KEY_LEGAL_INFO) {
                        long li = params.long(key)
                        whereParts.add( getLegalInfoQueryWhereParts(li) )

                        Map<String, Object> customRdv = GenericConfig.getCustomRefdata(p)
                        filterLabelValue = customRdv.get('from').find{ it.id == li }.value_de
                    }
                    else if (p == GenericConfig.CUSTOM_KEY_CUSTOMER_TYPE) {
                        queryBase = queryBase + ' , OrgSetting oss'

                        whereParts.add('oss.org = org and oss.key = :p' + (++pCount))
                        queryParams.put('p' + pCount, OrgSetting.KEYS.CUSTOMER_TYPE)

                        whereParts.add('oss.roleValue = :p' + (++pCount))
                        queryParams.put('p' + pCount, Role.get(params.get(key)))

                        Map<String, Object> customRdv = GenericConfig.getCustomRefdata(p)
                        filterLabelValue = customRdv.get('from').find{ it.id == params.long(key) }.value_de
                    }
                }

                if (filterLabelValue) {
                    if (partKey == 'member') {
                        result.filterLabels.get(partKey).put(p, [label: getFilterFieldLabel(SubscriptionConfig.CONFIG.member, p), value: filterLabelValue])
                    }
                    else if (partKey == 'provider') {
                        result.filterLabels.get(partKey).put(p, [label: getFilterFieldLabel(SubscriptionConfig.CONFIG.provider, p), value: filterLabelValue])
                    }
                }
            }
        }

        String query = queryBase + ' where ' + whereParts.join(' and ')

        println 'SubscriptionFilter.internalOrgFilter() -->'
        println query

        result.put( partKey + 'IdList', Org.executeQuery(query, queryParams) )
    }
}
