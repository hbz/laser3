package de.laser

import de.laser.helper.Params
import de.laser.storage.RDStore
import de.laser.properties.PropertyDefinitionGroup
import de.laser.properties.PropertyDefinitionGroupBinding
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * A service managing comparison calls for the entity copy functionality.
 * Not to confound with the {@link ComparisonService}
 */
@Transactional
class CompareService {

    ComparisonService comparisonService
    ContextService contextService
    GenericOIDService genericOIDService
    FilterService filterService

    /**
     * Returns the list of base elements depending on the given object class
     * @param obj the object whose base attributes should be retrieved
     * @return a list of base attributes either for {@link Subscription} or {@link License}
     */
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

    /**
     * Compares the properties of the given list of objects and groups them by public properties with or without group and private properties
     * @param objects a list of objects with properties to compare
     * @return a map containing for each grouped, orphaned and private properties the sets of properties for each object
     */
    Map compareProperties(List objects) {
        LinkedHashMap result = [groupedProperties: [:], orphanedProperties: [:], privateProperties: [:]]

        objects.each { object ->

            Map<String, Object> allPropDefGroups = object.getCalculatedPropDefGroups(contextService.getOrg())
            allPropDefGroups.entrySet().each { propDefGroupWrapper ->
                /*
                  group group level
                  There are: global, local, member (consortium@subscriber) property *groups* and orphaned *properties* which is ONE group
                 */
                String wrapperKey = propDefGroupWrapper.getKey()
                if (wrapperKey.equals("orphanedProperties")) {
                    List allowedProperties = propDefGroupWrapper.getValue().findAll { prop -> (prop.tenant?.id == contextService.getOrg().id || !prop.tenant) || prop.isPublic || (prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf)) }
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
                                    groupedProperties.put(groupKey, comparisonService.getGroupedPropertyTreesSortedAndAllowed(groupedProperties, groupKey, null, object, contextService.getOrg()))
                                break
                            case "local":
                                try {
                                    groupKey = (PropertyDefinitionGroup) propDefGroup.get(0)
                                    groupBinding = (PropertyDefinitionGroupBinding) propDefGroup.get(1)
                                    if (groupBinding.isVisible) {
                                        groupedProperties.put(groupKey, comparisonService.getGroupedPropertyTreesSortedAndAllowed(groupedProperties, groupKey, groupBinding, object, contextService.getOrg()))
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
                                        groupedProperties.put(groupKey, comparisonService.getGroupedPropertyTreesSortedAndAllowed(groupedProperties, groupKey, groupBinding, object, contextService.getOrg()))
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
            privateProperties = comparisonService.buildComparisonTree(privateProperties, object, object.propertySet.findAll { it.type.tenant?.id == contextService.getOrg().id })
            result.privateProperties = privateProperties
        }

        result.orphanedProperties = result.orphanedProperties.sort { genericOIDService.resolveOID(it.key).getI10n('name') }
        result.privateProperties = result.privateProperties.sort { genericOIDService.resolveOID(it.key).getI10n('name') }

        result.objects = objects
        result
    }

    /**
     * Retrieves a list of licenses eligible as copy targets. The list may be filtered
     * @param params a parameter map containing filter settings
     * @return a (filtered) list of licenses
     */
    List getMyLicenses(Map params, boolean onlyIds = false) {

        String base_qry
        Map qry_params

        if (contextService.getOrg().isCustomerType_Inst_Pro()) {
            base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType = :roleType2 ) AND o.org = :lic_org ) ) 
            )"""
            qry_params = [roleType1: RDStore.OR_LICENSEE, roleType2: RDStore.OR_LICENSEE_CONS, lic_org: contextService.getOrg()]

        } else if (contextService.getOrg().isCustomerType_Consortium()) {
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
            qry_params = [roleTypeC: RDStore.OR_LICENSING_CONSORTIUM, roleTypeL: RDStore.OR_LICENSEE_CONS, lic_org: contextService.getOrg()]
        } else {
            base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( o.roleType = :roleType AND o.org = :lic_org ) ) 
            )"""
            qry_params = [roleType: RDStore.OR_LICENSEE_CONS, lic_org: contextService.getOrg()]
        }

        if (params.status) {
            base_qry += " and l.status.id in (:status) "
            qry_params.put('status', Params.getLongList(params, 'status'))
        }
        base_qry += " order by lower(trim(l.reference)) asc"

        License.executeQuery( (onlyIds ? "select l.id " : "select l ") + base_qry, qry_params)
    }

    /**
     * Retrieves a list of subscriptions eligible as copy targets. The list may be filtered
     * @param params a parameter map containing filter settings
     * @return a (filtered) list of subscriptions
     */
    List getMySubscriptions(Map params, boolean onlyIds = false) {

        String base_qry
        Map qry_params = [:]

        if (contextService.getOrg().isCustomerType_Consortium()) {
            base_qry = " from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                    " AND s.instanceOf is null "
            qry_params << ['roleType': RDStore.OR_SUBSCRIPTION_CONSORTIUM, 'activeInst': contextService.getOrg()]
        }
        else {
            base_qry = "from Subscription as s where (exists ( select o from s.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType in (:roleType2) ) AND o.org = :activeInst ) ) AND (( not exists ( select o from s.orgRelations as o where o.roleType in (:scRoleType) ) ) or ( ( exists ( select o from s.orgRelations as o where o.roleType in (:scRoleType) ) ) AND ( s.instanceOf is not null) ) ) )"

            qry_params << ['roleType1': RDStore.OR_SUBSCRIBER, 'roleType2': [RDStore.OR_SUBSCRIBER_CONS], 'activeInst': contextService.getOrg(), 'scRoleType': [RDStore.OR_SUBSCRIPTION_CONSORTIUM]]
        }

        if (params.status) {
            base_qry += " and s.status.id in (:status) "
            qry_params.put('status', Params.getLongList(params, 'status'))
        }
        base_qry += " order by lower(trim(s.name)) asc"

        Subscription.executeQuery( (onlyIds ? "select s.id " : "select s ") + base_qry, qry_params)
    }

    /**
     * Retrieves a set of issue entitlements for the given subscription (defined in configMap), loading from the given offset
     * the given maximum count of objects
     * @param params the parameter map with query parameters
     * @param configMap a map containing configuration params to restrict loading
     * @return a list of issue entitlements
     */
    Map compareEntitlements(GrailsParameterMap params, Map<String, Object> configMap) {
        List objects = configMap.objects
        LinkedHashMap result = [ies: [:]]
        GrailsParameterMap paramsClone = params.clone()
        for (Iterator<Integer> iterator = paramsClone.iterator(); iterator.hasNext();) {
            if(iterator.next()) {
                iterator.remove()
            }
        }
        objects.each { object ->
            Map ies = result.ies
            Map query = filterService.getIssueEntitlementQuery(paramsClone, object)
            String queryString = 'select ie ' + query.query
            Map queryParams = query.queryParams+[max:configMap.max, offset:configMap.offset]
            List objEntitlements = IssueEntitlement.executeQuery(queryString, queryParams)
            ies = comparisonService.buildComparisonTreeIEs(ies, object, objEntitlements)
            result.ies = ies
        }
        result
    }

}
