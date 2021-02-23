package de.laser.reporting

import de.laser.Org
import de.laser.OrgSetting
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.auth.Role
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.ApplicationContext

class OrganisationFilter extends GenericFilter {

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
        Map<String, Object> result      = [ filterLabels : [:] ]

        List<String> queryParts         = [ 'select distinct (org.id) from Org org']
        List<String> whereParts         = [ 'where org.id in (:orgIdList)']
        Map<String, Object> queryParams = [ orgIdList : [] ]

        String filterSource = params.get(GenericConfig.FILTER_PREFIX + 'org' + GenericConfig.FILTER_SOURCE_POSTFIX)
        result.filterLabels.put('base', [source: getFilterSourceLabel(OrganisationConfig.CONFIG.base, filterSource)])

        switch (filterSource) {
            case 'all-org':
                queryParams.orgIdList = Org.executeQuery(
                        'select o.id from Org o where (o.status is null or o.status != :orgStatus)',
                        [orgStatus: RDStore.ORG_STATUS_DELETED]
                )
                break
            case 'all-inst':
                queryParams.orgIdList = Org.executeQuery(
                        'select o.id from Org o where (o.status is null or o.status != :orgStatus) and exists (select ot from o.orgType ot where ot = :orgType)',
                        [orgStatus: RDStore.ORG_STATUS_DELETED, orgType: RDStore.OT_INSTITUTION]
                )
                break
            case 'all-provider':
                queryParams.orgIdList = Org.executeQuery(
                        'select o.id from Org o where (o.status is null or o.status != :orgStatus) and exists (select ot from o.orgType ot where ot in (:orgTypes))',
                        [orgStatus: RDStore.ORG_STATUS_DELETED, orgTypes: [RDStore.OT_PROVIDER, RDStore.OR_AGENCY]]
                )
                break
            case 'my-inst':
                queryParams.orgIdList = Org.executeQuery(
                        'select o.id from Org as o, Combo as c where c.fromOrg = o and c.toOrg = :org and c.type = :comboType and (o.status is null or o.status != :orgStatus)',
                        [org: contextService.getOrg(), orgStatus: RDStore.ORG_STATUS_DELETED, comboType: RDStore.COMBO_TYPE_CONSORTIUM]
                )
                break
            case 'my-provider':
                queryParams.orgIdList = Org.executeQuery(
                        '''
select distinct(prov.org.id) from OrgRole prov 
    join prov.sub sub 
    join sub.orgRelations subOr 
where (prov.roleType in (:provRoleTypes)) and (sub = subOr.sub and subOr.org = :org and subOr.roleType in (:subRoleTypes))
    and (prov.org.status is null or prov.org.status != :orgStatus) 
''',
                        [
                            org: contextService.getOrg(), orgStatus: RDStore.ORG_STATUS_DELETED,
                            subRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA],
                            provRoleTypes: [RDStore.OR_PROVIDER, RDStore.OR_AGENCY]
                        ]
                )
                break
        }

        String cmbKey = GenericConfig.FILTER_PREFIX + 'org_'
        int pCount = 0

        Set<String> keys = params.keySet().findAll{ it.toString().startsWith(cmbKey) && ! it.toString().endsWith(GenericConfig.FILTER_SOURCE_POSTFIX) }
        keys.each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = getFieldType(OrganisationConfig.CONFIG.base, p)

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
                        queryParts.add('OrgSubjectGroup osg')
                        whereParts.add('osg.org = org and osg.subjectGroup.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                    }
                    else if (p == GenericConfig.CUSTOM_KEY_ORG_TYPE) {
                        whereParts.add('exists (select ot from org.orgType ot where ot = :p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, RefdataValue.get(params.long(key)))

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
                        queryParts.add('OrgSetting oss')

                        whereParts.add('oss.org = org and oss.key = :p' + (++pCount))
                        queryParams.put('p' + pCount, OrgSetting.KEYS.CUSTOMER_TYPE)

                        whereParts.add('oss.roleValue = :p' + (++pCount))
                        queryParams.put('p' + pCount, Role.get(params.get(key)))

                        Map<String, Object> customRdv = GenericConfig.getCustomRefdata(p)
                        filterLabelValue = customRdv.get('from').find{ it.id == params.long(key) }.value_de
                    }
                }

                if (filterLabelValue) {
                    result.filterLabels.get('base').put(p, [label: getFieldLabel(OrganisationConfig.CONFIG.base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println 'OrganisationFilter.filter() -->'
//        println query
//        println queryParams
//        println whereParts

        result.orgIdList = Subscription.executeQuery( query, queryParams )

//        println 'orgs >> ' + result.orgIdList.size()

        result
    }
}
