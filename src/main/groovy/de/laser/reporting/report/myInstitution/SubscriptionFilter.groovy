package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.Org
import de.laser.OrgSetting
import de.laser.wekb.Provider
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.SubscriptionsQueryService
import de.laser.wekb.Vendor
import de.laser.auth.Role
import de.laser.storage.BeanStore
import de.laser.utils.DateUtils
import de.laser.storage.RDStore
import de.laser.properties.PropertyDefinition
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j

import java.time.Year

@Slf4j
class SubscriptionFilter extends BaseFilter {

    static Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (sub.id) from Subscription sub']
        List<String> whereParts         = [ 'where sub.id in (:subscriptionIdList)']
        Map<String, Object> queryParams = [ subscriptionIdList: [] ]

        ContextService contextService = BeanStore.getContextService()
        SubscriptionsQueryService subscriptionsQueryService = BeanStore.getSubscriptionsQueryService()

        String filterSource = getCurrentFilterSource(params, BaseConfig.KEY_SUBSCRIPTION)
        filterResult.labels.put('base', [source: BaseConfig.getSourceLabel(BaseConfig.KEY_SUBSCRIPTION, filterSource)])

        switch (filterSource) {
            case 'all-sub':
                queryParams.subscriptionIdList = Subscription.executeQuery( 'select s.id from Subscription s' )
                break
            case 'consortia-sub':
                List tmp = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(
                        [validOn: null, orgRole: 'Subscription Consortia'])
                queryParams.subscriptionIdList = Subscription.executeQuery( 'select s.id ' + tmp[0], tmp[1])
                break
            case 'inst-sub':
                List tmp = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(
                        [validOn: null, orgRole: 'Subscriber'])
                queryParams.subscriptionIdList = Subscription.executeQuery( 'select s.id ' + tmp[0], tmp[1])
                break
            case 'inst-sub-consortia':
                List tmp = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(
                        [validOn: null, orgRole: RDStore.OR_SUBSCRIBER.value, subTypes: RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id])
                queryParams.subscriptionIdList = Subscription.executeQuery( 'select s.id ' + tmp[0], tmp[1])
                break
            case 'inst-sub-local':
                List tmp = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(
                        [validOn: null, orgRole: RDStore.OR_SUBSCRIBER.value, subTypes: RDStore.SUBSCRIPTION_TYPE_LOCAL.id])
                queryParams.subscriptionIdList = Subscription.executeQuery( 'select s.id ' + tmp[0], tmp[1])
                break
        }

        //println queryParams

        String cmbKey = BaseConfig.FILTER_PREFIX + BaseConfig.KEY_SUBSCRIPTION + '_'
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
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES)     { whereParts.add( 'sub.' + p + ' is true' ) }
                        else if (rdv == RDStore.YN_NO) { whereParts.add( 'sub.' + p + ' is false' ) }

                        filterLabelValue = rdv.getI10n('value')
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

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                    log.info ' --- ' + pType +' not implemented --- '
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {

                    if (p == BaseConfig.CI_GENERIC_ANNUAL) {
                        List tmpList = []

                        params.list(key).each { pk ->
                            if (pk == 0) {
                                tmpList.add('( sub.startDate != null and sub.endDate is null )')
                            }
                            else {
                                tmpList.add('( (YEAR(sub.startDate) <= :p' + (++pCount) + ' or sub.startDate is null) and (YEAR(sub.endDate) >= :p' + pCount + ' or sub.endDate is null) )')
                                queryParams.put('p' + pCount, pk as Integer) // integer - hql
                            }
                        }
                        whereParts.add( '(' + tmpList.join(' or ') + ')' )

                        Map<String, Object> customRdv = BaseConfig.getCustomImplRefdata(p)
                        List labels = customRdv.get('from').findAll { it -> it.id in params.list(key).collect{ it2 -> Long.parseLong(it2) } }
                        filterLabelValue = labels.collect { it.get('value_de') } // TODO
                    }
                    else if (p == BaseConfig.CI_GENERIC_REFERENCE_YEAR) {
                        whereParts.add( '(sub.referenceYear = :p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, Year.of(params.int(key))) // TODO !!!

                        filterLabelValue = params.get(key)
                    }
                    else if (p == BaseConfig.CI_GENERIC_STARTDATE_LIMIT) {
                        whereParts.add( '(YEAR(sub.startDate) >= :p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, params.int(key))

                        filterLabelValue = params.get(key)
                    }
                    else if (p == BaseConfig.CI_GENERIC_ENDDATE_LIMIT) {
                        whereParts.add( '(YEAR(sub.endDate) <= :p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, params.int(key))

                        filterLabelValue = params.get(key)
                    }
                    else if (p == BaseConfig.CI_CTX_PROPERTY_KEY) {
                        Long pValue = params.long('filter:subscription_propertyValue')

                        String pq = getPropertyFilterSubQuery(
                                'SubscriptionProperty', 'sub',
                                params.long(key),
                                pValue,
                                queryParams
                        )
                        whereParts.add( '(exists (' + pq + '))' )
                        filterLabelValue = PropertyDefinition.get(params.long(key)).getI10n('name') + ( pValue ? ': ' + RefdataValue.get( pValue ).getI10n('value') : '')
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

        // --- subset ---

        handleExpandoSubsetFilter(this, BaseConfig.KEY_SUBSCRIPTION, filterResult, params)

//        println 'subscriptions >> ' + result.subscriptionIdList.size()
//        println 'member >> ' + result.memberIdList.size()
//        println 'provider >> ' + result.providerIdList.size()
//        println 'vendor >> ' + result.vendorIdList.size()

        filterResult
    }

    static void _handleSubsetSubscriptionFilter(String partKey, Map<String, Object> filterResult, GrailsParameterMap params) {

        String filterSource = getCurrentFilterSource(params, partKey)
        filterResult.labels.put(partKey, [source: BaseConfig.getSourceLabel(BaseConfig.KEY_SUBSCRIPTION, filterSource)])

        if (! filterResult.data.get('subscriptionIdList')) {
            filterResult.data.put( partKey + 'IdList', [] )
        }

        String queryBase                = 'select distinct (mbr.id) from Subscription mbr join mbr.instanceOf sub'
        List<String> whereParts         = [ 'sub.id in (:subscriptionIdList)']
        Map<String, Object> queryParams = [ subscriptionIdList: filterResult.data.subscriptionIdList ]

        String cmbKey = BaseConfig.FILTER_PREFIX + 'memberSubscription_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each{ key ->
            if (params.get(key)) {
                //println key + " >> " + params.get(key)

                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).memberSubscription, p)

                def filterLabelValue

                // --> generic properties
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {
                    if (Subscription.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'mbr.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Subscription.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES)     { whereParts.add( 'mbr.' + p + ' is true' ) }
                        else if (rdv == RDStore.YN_NO) { whereParts.add( 'mbr.' + p + ' is false' ) }

                        filterLabelValue = rdv.getI10n('value')
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                        filterLabelValue = params.get(key)
                    }
                }
                // --> generic refdata
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'mbr.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                    log.info ' --- ' + pType +' not implemented --- '
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {

                    if (p == BaseConfig.CI_GENERIC_ANNUAL) {
                        List tmpList = []

                        params.list(key).each { pk ->
                            if (pk == 0) {
                                tmpList.add('( sub.startDate != null and sub.endDate is null )')
                            }
                            else {
                                tmpList.add('( (YEAR(mbr.startDate) <= :p' + (++pCount) + ' or mbr.startDate is null) and (YEAR(mbr.endDate) >= :p' + pCount + ' or mbr.endDate is null) )')
                                queryParams.put('p' + pCount, pk as Integer)
                            }
                        }
                        whereParts.add( '(' + tmpList.join(' or ') + ')' )

                        Map<String, Object> customRdv = BaseConfig.getCustomImplRefdata(p)
                        List labels = customRdv.get('from').findAll { it -> it.id in params.list(key).collect{ it2 -> Long.parseLong(it2) } }
                        filterLabelValue = labels.collect { it.get('value_de') } // TODO
                    }
                    else if (p == BaseConfig.CI_GENERIC_REFERENCE_YEAR) {
                        whereParts.add( '(sub.referenceYear = :p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, Year.of(params.int(key))) // TODO !!!

                        filterLabelValue = params.get(key)
                    }
                    else if (p == BaseConfig.CI_GENERIC_STARTDATE_LIMIT) {
                        whereParts.add( '(YEAR(mbr.startDate) >= :p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, params.int(key))

                        filterLabelValue = params.get(key)
                    }
                    else if (p == BaseConfig.CI_GENERIC_ENDDATE_LIMIT) {
                        whereParts.add( '(YEAR(mbr.endDate) <= :p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, params.int(key))

                        filterLabelValue = params.get(key)
                    }
                    else if (p == BaseConfig.CI_CTX_PROPERTY_KEY) {
                        Long pValue = params.long('filter:memberSubscription_propertyValue')

                        String pq = getPropertyFilterSubQuery(
                                'SubscriptionProperty', 'mbr',
                                params.long(key),
                                pValue,
                                queryParams
                        )
                        whereParts.add( '(exists (' + pq + '))' )
                        filterLabelValue = PropertyDefinition.get(params.long(key)).getI10n('name') + ( pValue ? ': ' + RefdataValue.get( pValue ).getI10n('value') : '')
                    }
                }

                if (filterLabelValue) {
                    filterResult.labels.get('memberSubscription').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).memberSubscription, p), value: filterLabelValue])
                }
            }
        }

        String query = queryBase + ' where ' + whereParts.join(' and ')

//        println 'SubscriptionFilter.handleSubsetSubFilter() -->'
//        println query
//        println queryParams

        filterResult.data.put( partKey + 'IdList', queryParams.subscriptionIdList ? Subscription.executeQuery(query, queryParams) : [] )
    }

    static void _handleSubsetOrgFilter(String partKey, Map<String, Object> filterResult, GrailsParameterMap params) {

        String filterSource = getCurrentFilterSource(params, partKey)
        if (!filterSource) { return /* e.g. inst-sub-local */ }

        filterResult.labels.put(partKey, [source: BaseConfig.getSourceLabel(BaseConfig.KEY_SUBSCRIPTION, filterSource)])

        if (! filterResult.data.get('subscriptionIdList')) {
            filterResult.data.put( partKey + 'IdList', [] )
        }

        String queryBase = 'select distinct (org.id) from Org org join org.links orgLink'
        List<String> whereParts = [ 'orgLink.roleType in (:roleTypes)', 'orgLink.sub.id in (:subscriptionIdList)' ]
        Map<String, Object> queryParams = [ 'subscriptionIdList': filterResult.data.subscriptionIdList ]

        if (partKey == 'member') {
            queryParams.put( 'roleTypes', [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN] ) // TODO <- RDStore.OR_SUBSCRIBER
            // check ONLY members
            queryParams.subscriptionIdList = filterResult.data.get('memberSubscriptionIdList') // if memberSubscription filter is set
        }
        if (partKey == 'consortium') {
            queryParams.put( 'roleTypes', [RDStore.OR_SUBSCRIPTION_CONSORTIUM] )
        }
//        if (partKey == 'provider') {
//            queryParams.put( 'roleTypes', [RDStore.OR_PROVIDER] )
//        }

        String cmbKey = BaseConfig.FILTER_PREFIX + partKey + '_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            List<String> validPartKeys = ['member', 'consortium' /*, 'provider' */]

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
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES)     { whereParts.add( 'org.' + p + ' is true' ) }
                        else if (rdv == RDStore.YN_NO) { whereParts.add( 'org.' + p + ' is false' ) }

                        filterLabelValue = rdv.getI10n('value')
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

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {

                    if (p == BaseConfig.CI_GENERIC_SUBJECT_GROUP) {
                        queryBase = queryBase + ' join org.subjectGroup osg join osg.subjectGroup rdvsg'
                        whereParts.add('rdvsg.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
                    else if (p == BaseConfig.CI_GENERIC_LEGAL_INFO) {
                        long li = params.long(key)
                        whereParts.add( getLegalInfoQueryWhereParts(li) )

                        Map<String, Object> customRdv = BaseConfig.getCustomImplRefdata(p)
                        filterLabelValue = customRdv.get('from').find{ it.id == li }.value_de
                    }
                    else if (p == BaseConfig.CI_GENERIC_CUSTOMER_TYPE) {
                        queryBase = queryBase + ' , OrgSetting oss'

                        whereParts.add('oss.org = org and oss.key = :p' + (++pCount))
                        queryParams.put('p' + pCount, OrgSetting.KEYS.CUSTOMER_TYPE)

                        whereParts.add('oss.roleValue = :p' + (++pCount))
                        queryParams.put('p' + pCount, Role.get(params.long(key)))

                        Map<String, Object> customRdv = BaseConfig.getCustomImplRefdata(p)
                        filterLabelValue = customRdv.get('from').find{ it.id == params.long(key) }.value_de
                    }
                    else if (p == BaseConfig.CI_CTX_PROPERTY_KEY) {
                        Long pValue = params.long('filter:member_propertyValue')

                        String pq = getPropertyFilterSubQuery(
                                'OrgProperty', 'org',
                                params.long(key),
                                pValue,
                                queryParams
                        )
                        whereParts.add( '(exists (' + pq + '))' )
                        filterLabelValue = PropertyDefinition.get(params.long(key)).getI10n('name') + ( pValue ? ': ' + RefdataValue.get( pValue ).getI10n('value') : '')
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

//        println 'SubscriptionFilter.handleSubsetOrgFilter() -->'
//        println query
//        println queryParams
        filterResult.data.put( partKey + 'IdList', queryParams.subscriptionIdList ? Org.executeQuery(query, queryParams) : [] )
    }

    static void _handleSubsetVendorFilter(String partKey, Map<String, Object> filterResult, GrailsParameterMap params) {

        String filterSource = getCurrentFilterSource(params, partKey)
        if (!filterSource) { return /* e.g. inst-sub-local */ }

        filterResult.labels.put(partKey, [source: BaseConfig.getSourceLabel(BaseConfig.KEY_SUBSCRIPTION, filterSource)])

        if (! filterResult.data.get('subscriptionIdList')) {
            filterResult.data.put( partKey + 'IdList', [] )
        }

        String queryBase = 'select distinct (ven.id) from VendorRole vr join vr.vendor ven'
        List<String> whereParts = [ 'vr.subscription.id in (:subscriptionIdList)' ]
        Map<String, Object> queryParams = [ 'subscriptionIdList': filterResult.data.subscriptionIdList ]

        String cmbKey = BaseConfig.FILTER_PREFIX + partKey + '_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            List<String> validPartKeys = ['vendor']

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType

                if (partKey in validPartKeys) {
                    pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).get( partKey ), p)
                }

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {

                    if (Vendor.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'ven.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Vendor.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES)     { whereParts.add( 'ven.' + p + ' is true' ) }
                        else if (rdv == RDStore.YN_NO) { whereParts.add( 'ven.' + p + ' is false' ) }

                        filterLabelValue = rdv.getI10n('value')
                    }
                    else {
                        whereParts.add( 'ven.' + p + ' = :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, params.get(key) )

                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'ven.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {
                    log.info ' --- ' + pType +' not implemented --- '
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

//        println 'SubscriptionFilter.handleSubsetVendorFilter() -->'
//        println query
//        println queryParams
        filterResult.data.put( partKey + 'IdList', queryParams.subscriptionIdList ? Vendor.executeQuery(query, queryParams) : [] )
    }

    static void _handleSubsetProviderFilter(String partKey, Map<String, Object> filterResult, GrailsParameterMap params) {

        String filterSource = getCurrentFilterSource(params, partKey)
        if (!filterSource) { return /* e.g. inst-sub-local */ }

        filterResult.labels.put(partKey, [source: BaseConfig.getSourceLabel(BaseConfig.KEY_SUBSCRIPTION, filterSource)])

        if (! filterResult.data.get('subscriptionIdList')) {
            filterResult.data.put( partKey + 'IdList', [] )
        }

        String queryBase = 'select distinct (pro.id) from ProviderRole pr join pr.provider pro'
        List<String> whereParts = [ 'pr.subscription.id in (:subscriptionIdList)' ]
        Map<String, Object> queryParams = [ 'subscriptionIdList': filterResult.data.subscriptionIdList ]

        String cmbKey = BaseConfig.FILTER_PREFIX + partKey + '_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            List<String> validPartKeys = ['provider']

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType

                if (partKey in validPartKeys) {
                    pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).get( partKey ), p)
                }

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {

                    if (Provider.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'pro.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Provider.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES)     { whereParts.add( 'pro.' + p + ' is true' ) }
                        else if (rdv == RDStore.YN_NO) { whereParts.add( 'pro.' + p + ' is false' ) }

                        filterLabelValue = rdv.getI10n('value')
                    }
                    else {
                        whereParts.add( 'pro.' + p + ' = :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, params.get(key) )

                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'pro.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {
                    log.info ' --- ' + pType +' not implemented --- '
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

//        println 'SubscriptionFilter.handleSubsetProviderFilter() -->'
//        println query
//        println queryParams
        filterResult.data.put( partKey + 'IdList', queryParams.subscriptionIdList ? Provider.executeQuery(query, queryParams) : [] )
    }

}
