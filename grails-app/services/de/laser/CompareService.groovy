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
@Deprecated
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
    @Deprecated
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
     * Retrieves a set of issue entitlements for the given subscription (defined in configMap), loading from the given offset
     * the given maximum count of objects
     * @param params the parameter map with query parameters
     * @param configMap a map containing configuration params to restrict loading
     * @return a list of issue entitlements
     */
    @Deprecated
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
