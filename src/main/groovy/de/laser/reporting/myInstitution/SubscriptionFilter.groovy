package de.laser.reporting.myInstitution

import de.laser.Org
import de.laser.OrgSetting
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.auth.Role
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.myInstitution.base.BaseConfig
import de.laser.reporting.myInstitution.base.BaseFilter
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.ApplicationContext

class SubscriptionFilter extends BaseFilter {

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
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (sub.id) from Subscription sub']
        List<String> whereParts         = [ 'where sub.id in (:subscriptionIdList)']
        Map<String, Object> queryParams = [ subscriptionIdList: [] ]

        String filterSource = params.get(BaseConfig.FILTER_PREFIX + 'subscription' + BaseConfig.FILTER_SOURCE_POSTFIX)
        filterResult.labels.put('base', [source: getFilterSourceLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).base, filterSource)])

        switch (filterSource) {
            case 'all-sub':
                queryParams.subscriptionIdList = Subscription.executeQuery( 'select s.id from Subscription s' )
                break
            case 'consortia-sub':
                List tmp = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(    // roleType:Subscription Consortia
                        [validOn: null, orgRole: 'Subscription Consortia'], contextService.getOrg() )
                queryParams.subscriptionIdList = Subscription.executeQuery( 'select s.id ' + tmp[0], tmp[1])
                break
            case 'my-sub':
                List tmp = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(    // roleType:Subscriber
                        [validOn: null, orgRole: 'Subscriber'], contextService.getOrg() )
                queryParams.subscriptionIdList = Subscription.executeQuery( 'select s.id ' + tmp[0], tmp[1])
                break
        }

        //println queryParams

        String cmbKey = BaseConfig.FILTER_PREFIX + 'subscription_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each{ key ->
            if (params.get(key)) {
                //println key + " >> " + params.get(key)

                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).base, p)

                def filterLabelValue

                // --> generic properties
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {
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
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'sub.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                    println ' ------------ not implemented ------------ '
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {

                    if (p == BaseConfig.CUSTOM_KEY_ANNUAL) {

                        whereParts.add( '(YEAR(sub.startDate) <= :p' + (++pCount) + ' or sub.startDate is null)' )
                        queryParams.put( 'p' + pCount, params.get(key) as Integer )

                        whereParts.add( '(YEAR(sub.endDate) >= :p' + (++pCount) + ' or sub.endDate is null)' )
                        queryParams.put( 'p' + pCount, params.get(key) as Integer )

                        filterLabelValue = params.get(key)
                    }
                }

                if (filterLabelValue) {
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println 'SubscriptionFilter.filter() -->'
//        println query
//        println queryParams
//        println whereParts

        filterResult.data.put('subscriptionIdList', queryParams.subscriptionIdList ? Subscription.executeQuery( query, queryParams ) : [])

        BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).keySet().each{pk ->
            if (pk != 'base') {
                handleInternalOrgFilter(params, pk, filterResult)
            }
        }

//        println 'subscriptions >> ' + result.subscriptionIdList.size()
//        println 'member >> ' + result.memberIdList.size()
//        println 'provider >> ' + result.providerIdList.size()

        filterResult
    }

    private void handleInternalOrgFilter(GrailsParameterMap params, String partKey, Map<String, Object> filterResult) {

        String filterSource = params.get(BaseConfig.FILTER_PREFIX + partKey + BaseConfig.FILTER_SOURCE_POSTFIX)
        filterResult.labels.put(partKey, [source: getFilterSourceLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).get(partKey), filterSource)])

        //println 'internalOrgFilter() ' + params + ' >>>>>>>>>>>>>>>< ' + partKey
        if (! filterResult.data.get('subscriptionIdList')) {
            filterResult.data.put( partKey + 'IdList', [] )
            return
        }

        String queryBase = 'select distinct (org.id) from Org org join org.links orgLink'
        List<String> whereParts = [ 'orgLink.roleType in (:roleTypes)', 'orgLink.sub.id in (:subscriptionIdList)' ]
        Map<String, Object> queryParams = [ 'subscriptionIdList': filterResult.data.subscriptionIdList ]

        if (partKey == 'member') {
            queryParams.put( 'roleTypes', [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN] ) // TODO <- RDStore.OR_SUBSCRIBER
            // check ONLY members
            queryParams.subscriptionIdList = Subscription.executeQuery(
                    'select distinct (sub.id) from Subscription sub where sub.instanceOf.id in (:subscriptionIdList)',
                    [ subscriptionIdList: queryParams.subscriptionIdList ]
            )
        }
        if (partKey == 'consortium') {
            queryParams.put( 'roleTypes', [RDStore.OR_SUBSCRIPTION_CONSORTIA] )
        }
        if (partKey == 'provider') {
            queryParams.put( 'roleTypes', [RDStore.OR_PROVIDER] )
        }
        if (partKey == 'agency') {
            queryParams.put( 'roleTypes', [RDStore.OR_AGENCY] )
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + partKey + '_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)
            List<String> validPartKeys = ['member', 'consortium', 'provider', 'agency']

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType

                if (partKey in validPartKeys) {
                    pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).get( partKey ), p)
                }

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {

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
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'org.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {

                    if (p == BaseConfig.CUSTOM_KEY_ORG_TYPE) {
                        whereParts.add('exists (select ot from org.orgType ot where ot = :p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, RefdataValue.get(params.long(key)))

                        filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                    }
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {

                    if (p == BaseConfig.CUSTOM_KEY_SUBJECT_GROUP) {
                        queryBase = queryBase + ' join org.subjectGroup osg join osg.subjectGroup rdvsg'
                        whereParts.add('rdvsg.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                    }
                    else if (p == BaseConfig.CUSTOM_KEY_LEGAL_INFO) {
                        long li = params.long(key)
                        whereParts.add( getLegalInfoQueryWhereParts(li) )

                        Map<String, Object> customRdv = BaseConfig.getCustomRefdata(p)
                        filterLabelValue = customRdv.get('from').find{ it.id == li }.value_de
                    }
                    else if (p == BaseConfig.CUSTOM_KEY_CUSTOMER_TYPE) {
                        queryBase = queryBase + ' , OrgSetting oss'

                        whereParts.add('oss.org = org and oss.key = :p' + (++pCount))
                        queryParams.put('p' + pCount, OrgSetting.KEYS.CUSTOMER_TYPE)

                        whereParts.add('oss.roleValue = :p' + (++pCount))
                        queryParams.put('p' + pCount, Role.get(params.get(key)))

                        Map<String, Object> customRdv = BaseConfig.getCustomRefdata(p)
                        filterLabelValue = customRdv.get('from').find{ it.id == params.long(key) }.value_de
                    }
                }

                if (filterLabelValue) {
                    if (partKey in validPartKeys) {
                        filterResult.labels.get(partKey).put( p, [
                                label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).get( partKey ), p),
                                value: filterLabelValue
                        ] )
                    }
                }
            }
        }

        String query = queryBase + ' where ' + whereParts.join(' and ')

//        println 'SubscriptionFilter.internalOrgFilter() -->'
//        println query
//        println queryParams
        filterResult.data.put( partKey + 'IdList', queryParams.subscriptionIdList ? Org.executeQuery(query, queryParams) : [] )
    }

}
