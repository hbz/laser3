package de.laser

import com.k_int.kbplus.GenericOIDService
import com.k_int.kbplus.Subscription
import de.laser.helper.RDStore
import de.laser.properties.PropertyDefinitionGroup
import de.laser.properties.PropertyDefinitionGroupBinding
import grails.transaction.Transactional

@Transactional
class CompareService {

    ContextService contextService
    GenericOIDService genericOIDService
    ComparisonService comparisonService
    AccessService accessService
    SubscriptionService subscriptionService


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

            Map<String, Object> allPropDefGroups = object._getCalculatedPropDefGroups(contextOrg)
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

        RefdataValue licensee_role = RDStore.OR_LICENSEE
        RefdataValue licensee_cons_role = RDStore.OR_LICENSEE_CONS
        RefdataValue lic_cons_role = RDStore.OR_LICENSING_CONSORTIUM

        String base_qry
        Map qry_params

        if (accessService.checkPerm("ORG_INST")) {
            base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType = :roleType2 ) AND o.org = :lic_org ) ) 
            )"""
            qry_params = [roleType1: licensee_role, roleType2: licensee_cons_role, lic_org: result.institution]

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
            qry_params = [roleTypeC: lic_cons_role, roleTypeL: licensee_cons_role, lic_org: result.institution]
        } else {
            base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( o.roleType = :roleType AND o.org = :lic_org ) ) 
            )"""
            qry_params = [roleType: licensee_cons_role, lic_org: result.institution]
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

        RefdataValue role_sub = RDStore.OR_SUBSCRIBER
        RefdataValue role_subCons = RDStore.OR_SUBSCRIBER_CONS
        RefdataValue role_sub_consortia = RDStore.OR_SUBSCRIPTION_CONSORTIA
        RefdataValue role_subColl = RDStore.OR_SUBSCRIBER_COLLECTIVE
        RefdataValue role_sub_collective = RDStore.OR_SUBSCRIPTION_COLLECTIVE


        String base_qry
        Map qry_params = [:]

        if (accessService.checkPerm(result.institution, 'ORG_CONSORTIUM')) {
            base_qry = " from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                    " AND s.instanceOf is null "
            qry_params << ['roleType': role_sub_consortia, 'activeInst': result.institution]
        } else {

            base_qry = "from Subscription as s where (exists ( select o from s.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType in (:roleType2) ) AND o.org = :activeInst ) ) AND (( not exists ( select o from s.orgRelations as o where o.roleType in (:scRoleType) ) ) or ( ( exists ( select o from s.orgRelations as o where o.roleType in (:scRoleType) ) ) AND ( s.instanceOf is not null) ) ) )"

            qry_params << ['roleType1': role_sub, 'roleType2': [role_subCons, role_subColl], 'activeInst': result.institution, 'scRoleType': [role_sub_consortia, role_sub_collective]]
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

    Map compareEntitlements(List objects) {
        LinkedHashMap result = [ies: [:]]
        objects.each { object ->
            Map ies = result.ies
            ies = comparisonService.buildComparisonTreeIEs(ies, object, subscriptionService.getIssueEntitlementsWithFilter(object, [max: 5000, offset: 0]))
            result.ies = ies
        }
        result
    }

}
