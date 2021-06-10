package de.laser

import com.k_int.kbplus.GenericOIDService
import de.laser.helper.RDStore
import de.laser.properties.PropertyDefinitionGroup
import de.laser.properties.PropertyDefinitionGroupBinding
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class CompareService {

    ContextService contextService
    GenericOIDService genericOIDService
    ComparisonService comparisonService
    AccessService accessService
    SubscriptionService subscriptionService
    FilterService filterService


    List compareElements(Object obj) {
        List result = []
        switch (obj.class.simpleName) {
            case License.class.simpleName:
                result = ['startDate', 'endDate', 'status', 'licenseCategory', 'openEnded', 'isPublicForApi']
                break
            case Subscription.class.simpleName:
                result = ['startDate', 'endDate', 'manualCancellationDate', 'status', 'kind', 'form', 'resource', 'isPublicForApi', 'hasPerpetualAccess']
                break
        }
        result
    }

    Map compareProperties(List objects) {
        LinkedHashMap result = [groupedProperties: [:], orphanedProperties: [:], privateProperties: [:]]
        Org contextOrg = contextService.getOrg()
        objects.each { object ->

            Map<String, Object> allPropDefGroups = object.getCalculatedPropDefGroups(contextOrg)
            allPropDefGroups.entrySet().each { propDefGroupWrapper ->
                /*
                  group group level
                  There are: global, local, member (consortium@subscriber) property *groups* and orphaned *properties* which is ONE group
                 */
                String wrapperKey = propDefGroupWrapper.getKey()
                if (wrapperKey.equals("orphanedProperties")) {
                    List allowedProperties = propDefGroupWrapper.getValue().findAll { prop -> (prop.tenant?.id == contextOrg.id || !prop.tenant) || prop.isPublic || (prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf)) }
                    Map orphanedProperties = result.orphanedProperties
                    orphanedProperties = comparisonService.buildComparisonTree(orphanedProperties, object, allowedProperties)
                    result.orphanedProperties = orphanedProperties
                } else {
                    LinkedHashMap groupedProperties = result.groupedProperties
                    /*
                      group level
                      Each group may have different property groups
                    */
                    propDefGroupWrapper.getValue().each { propDefGroup ->
                        PropertyDefinitionGroup groupKey
                        PropertyDefinitionGroupBinding groupBinding
                        switch (wrapperKey) {
                            case "global":
                                groupKey = (PropertyDefinitionGroup) propDefGroup
                                if (groupKey.isVisible)
                                    groupedProperties.put(groupKey, comparisonService.getGroupedPropertyTreesSortedAndAllowed(groupedProperties, groupKey, null, object, contextOrg))
                                break
                            case "local":
                                try {
                                    groupKey = (PropertyDefinitionGroup) propDefGroup.get(0)
                                    groupBinding = (PropertyDefinitionGroupBinding) propDefGroup.get(1)
                                    if (groupBinding.isVisible) {
                                        groupedProperties.put(groupKey, comparisonService.getGroupedPropertyTreesSortedAndAllowed(groupedProperties, groupKey, groupBinding, object, contextOrg))
                                    }
                                }
                                catch (ClassCastException e) {
                                    log.error("Erroneous values in calculated property definition group! Stack trace as follows:")
                                    e.printStackTrace()
                                }
                                break
                            case "member":
                                try {
                                    groupKey = (PropertyDefinitionGroup) propDefGroup.get(0)
                                    groupBinding = (PropertyDefinitionGroupBinding) propDefGroup.get(1)
                                    if (groupBinding.isVisible && groupBinding.isVisibleForConsortiaMembers) {
                                        groupedProperties.put(groupKey, comparisonService.getGroupedPropertyTreesSortedAndAllowed(groupedProperties, groupKey, groupBinding, object, contextOrg))
                                    }
                                }
                                catch (ClassCastException e) {
                                    log.error("Erroneous values in calculated property definition group! Stack trace as follows:")
                                    e.printStackTrace()
                                }
                                break
                        }
                    }
                    result.groupedProperties = groupedProperties
                }
            }
            TreeMap privateProperties = result.privateProperties
            privateProperties = comparisonService.buildComparisonTree(privateProperties, object, object.propertySet.findAll { it.type.tenant?.id == contextOrg.id })
            result.privateProperties = privateProperties
        }

        result.orphanedProperties = result.orphanedProperties.sort { genericOIDService.resolveOID(it.key).getI10n('name') }
        result.privateProperties = result.privateProperties.sort { genericOIDService.resolveOID(it.key).getI10n('name') }

        result.objects = objects
        result
    }


    List getMyLicenses(Map params) {

        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.institution = contextService.getOrg()

        String base_qry
        Map qry_params

        if (accessService.checkPerm("ORG_INST")) {
            base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType = :roleType2 ) AND o.org = :lic_org ) ) 
            )"""
            qry_params = [roleType1: RDStore.OR_LICENSEE, roleType2: RDStore.OR_LICENSEE_CONS, lic_org: result.institution]

        } else if (accessService.checkPerm("ORG_CONSORTIUM")) {
            base_qry = """from License as l where (
                    exists ( select o from l.orgRelations as o where ( 
                    ( o.roleType = :roleTypeC 
                        AND o.org = :lic_org 
                        AND l.instanceOf is null
                        AND NOT exists (
                        select o2 from l.orgRelations as o2 where o2.roleType = :roleTypeL
                    )
                )
            )))"""
            qry_params = [roleTypeC: RDStore.OR_LICENSING_CONSORTIUM, roleTypeL: RDStore.OR_LICENSEE_CONS, lic_org: result.institution]
        } else {
            base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( o.roleType = :roleType AND o.org = :lic_org ) ) 
            )"""
            qry_params = [roleType: RDStore.OR_LICENSEE_CONS, lic_org: result.institution]
        }

        if (params.status) {
            if (params.status instanceof List) {
                base_qry += " and l.status.id in (:status) "
                qry_params.put('status', params.status.collect { it instanceof Long ? it : Long.parseLong(it) })

            } else {
                base_qry += " and l.status.id = :status "
                qry_params.put('status', (params.status as Long))
            }
        }

        base_qry += " order by lower(trim(l.reference)) asc"

        List<License> totalLicenses = License.executeQuery("select l " + base_qry, qry_params)

        totalLicenses

    }

    List getMySubscriptions(Map params) {

        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.institution = contextService.getOrg()

        String base_qry
        Map qry_params = [:]

        if (accessService.checkPerm(result.institution, 'ORG_CONSORTIUM')) {
            base_qry = " from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                    " AND s.instanceOf is null "
            qry_params << ['roleType': RDStore.OR_SUBSCRIPTION_CONSORTIA, 'activeInst': result.institution]
        }
        else {
            base_qry = "from Subscription as s where (exists ( select o from s.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType in (:roleType2) ) AND o.org = :activeInst ) ) AND (( not exists ( select o from s.orgRelations as o where o.roleType in (:scRoleType) ) ) or ( ( exists ( select o from s.orgRelations as o where o.roleType in (:scRoleType) ) ) AND ( s.instanceOf is not null) ) ) )"

            qry_params << ['roleType1': RDStore.OR_SUBSCRIBER, 'roleType2': [RDStore.OR_SUBSCRIBER_CONS], 'activeInst': result.institution, 'scRoleType': [RDStore.OR_SUBSCRIPTION_CONSORTIA]]
        }

        if (params.status) {
            if (params.status instanceof List) {
                base_qry += " and s.status.id in (:status) "
                qry_params.put('status', params.status.collect { it instanceof Long ? it : Long.parseLong(it) })

            } else {
                base_qry += " and s.status.id = :status "
                qry_params.put('status', (params.status as Long))
            }
        }


        base_qry += " order by lower(trim(s.name)) asc"

        List<Subscription> totalSubscriptions = Subscription.executeQuery("select s " + base_qry, qry_params)

        totalSubscriptions

    }

    Map compareEntitlements(GrailsParameterMap grailsParameterMap, List objects) {
        LinkedHashMap result = [ies: [:]]
        GrailsParameterMap newMap = grailsParameterMap.clone()
        for (Iterator<Integer> iterator = newMap.iterator(); iterator.hasNext();) {
            if(iterator.next()) {
                iterator.remove()
            }
        }
        newMap.max = 5000
        newMap.offset = 0
        objects.each { object ->
            Map ies = result.ies
            Map query = filterService.getIssueEntitlementQuery(newMap, object)
            ies = comparisonService.buildComparisonTreeIEs(ies, object, IssueEntitlement.executeQuery("select ie " + query.query, query.queryParams+[max:5000,offset:0]))
            result.ies = ies
        }
        result
    }

}
