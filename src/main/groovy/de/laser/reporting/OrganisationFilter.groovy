package de.laser.reporting

import de.laser.Org
import de.laser.ReportingService
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

        List<String> queryParts         = [ 'select org.id from Org org']
        List<String> whereParts         = [ 'where org.id in (:orgIdList)']
        Map<String, Object> queryParams = [ orgIdList: [] ]

        switch (params.get(Cfg.filterPrefix + 'org_filter')) {
            case 'all-org':
                queryParams.orgIdList = Org.executeQuery(
                        'select o.id from Org o where (o.status is null or o.status != :orgStatus)',
                        [orgStatus: RDStore.ORG_STATUS_DELETED]
                )
                break
            case 'all-inst':
                queryParams.orgIdList = Org.executeQuery(
                        'select o.id from Org o where (o.status is null or o.status != :orgStatus) and exists (select ot from o.orgType ot where ot = :orgType )',
                        [orgStatus: RDStore.ORG_STATUS_DELETED, orgType: RDStore.OT_INSTITUTION]
                )
                break
            case 'all-provider':
                queryParams.orgIdList = Org.executeQuery(
                        'select o.id from Org o where (o.status is null or o.status != :orgStatus) and exists (select ot from o.orgType ot where ot in (:orgTypes) )',
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

        String cmbKey = Cfg.filterPrefix + 'org_'
        int pCount = 0

        Set<String> keys = params.keySet().findAll{ it.toString().startsWith(cmbKey) }
        keys.each { key ->
            println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = ReportingService.getFormFieldType(Cfg.config.Organisation, p)

                if (pType == Cfg.FORM_TYPE_PROPERTY) {
                    whereParts.add( 'org.' + p + ' = :p' + (++pCount) )
                    if (Org.getDeclaredField(p).getType() == Date) {
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                    }
                }
                else if (pType == Cfg.FORM_TYPE_REFDATA) {
                    whereParts.add( 'org.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println query
//        println queryParams
//        println whereParts

        result.orgIdList = Subscription.executeQuery( query, queryParams )

//        println 'orgs >> ' + result.orgIdList.size()

        result
    }
}
