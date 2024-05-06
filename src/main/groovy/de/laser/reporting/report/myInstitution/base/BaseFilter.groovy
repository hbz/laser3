package de.laser.reporting.report.myInstitution.base

import de.laser.ContextService
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.storage.BeanStore
import de.laser.properties.PropertyDefinition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * The class containing the generic filter data and methods shared by the detailed object filters
 */
class BaseFilter {

    /**
     * Gets the current filter source from the request parameter map
     * @param params the request parameter map
     * @param source the object being source of the report
     * @return the filter source
     */
    static String getCurrentFilterSource(GrailsParameterMap params, String source) {
        params.get(BaseConfig.FILTER_PREFIX + source + BaseConfig.FILTER_SOURCE_POSTFIX)
    }

    /**
     * Gets the current filter keys from the request parameter map
     * @param params the request parameter map
     * @param cmbKey the filter key prefix
     * @return the set of filter keys
     */
    static Set<String> getCurrentFilterKeys(GrailsParameterMap params, String cmbKey) {
        params.keySet().findAll{ it.toString().startsWith(cmbKey) && ! it.toString().endsWith(BaseConfig.FILTER_SOURCE_POSTFIX) }
    }

    /**
     * Gets the sources of the given report configuration
     * @param config the config map whose source should be retrieved
     * @return the source list
     */
    static List<String> getRestrictedConfigSources(Map<String, Object> config) {

        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            config.source
        }
        else {
            config.source.findAll{ ! it.endsWith('-deleted') } as List<String>
        }
    }

    /**
     * Gets the operator matching to the given modifier
     * @param modifier the modifier as string
     * @return the operator sign for the label
     */
    static String getDateModifier(String modifier) {

        if (modifier == 'less') {
            return '<'
        }
        else if (modifier == 'greater') {
            return '>'
        }
        else if (modifier == 'less-equal') {
            return '<='
        }
        else if (modifier == 'greater-equal') {
            return '>='
        }
        else {
            return '='
        }
    }

    /**
     * Gets the legal information query part matching the given key
     * @param key the key switch determining the legal information restriction
     * @return the query part matching the key
     */
    static String getLegalInfoQueryWhereParts(Long key) {

        if (key == 0){
            return 'org.createdBy is null and org.legallyObligedBy is null'
        }
        else if (key == 1){
            return 'org.createdBy is not null and org.legallyObligedBy is not null'
        }
        else if (key == 2){
            return 'org.createdBy is not null and org.legallyObligedBy is null'
        }
        else if (key == 3){
            return 'org.createdBy is null and org.legallyObligedBy is not null'
        }
    }

    /**
     * Gets the subquery for the property filter
     * @param hqlDc the domain class, i.e. the type of property being queried
     * @param hqlVar the object in which the property has been defined
     * @param pdId the property definition ID
     * @param pValue the property value
     * @param queryParams the query parameter map
     * @return the query string
     */
    static String getPropertyFilterSubQuery(String hqlDc, String hqlVar, Long pdId, Long pValue, Map<String, Object> queryParams) {

        ContextService contextService = BeanStore.getContextService()

        String pvQuery = ''
        if (pValue) {
            PropertyDefinition pd = PropertyDefinition.get(pdId)
            pvQuery = ' and prop.' + pd.getImplClassValueProperty() + ' = :pfsq3'
            queryParams.put('pfsq3', AbstractPropertyWithCalculatedLastUpdated.parseValue(pValue as String, pd.type))
        }

        String query =  'select prop from ' + hqlDc + ' prop join prop.owner owner join prop.type pd' +
                        ' where owner = ' + hqlVar +
                        ' and pd.id = :pfsq1 ' +
                        ' and (prop.tenant = :pfsq2 or prop.isPublic = true)' + pvQuery

        queryParams.put('pfsq1', pdId)
        queryParams.put('pfsq2', contextService.getOrg())

        query
    }

    /**
     * Returns the cached filter ID list
     * @param prefix the cache prefix
     * @param params the cached parameter map
     * @return the ID list or an empty list, if not existing
     */
    static List<Long> getCachedFilterIdList(prefix, params) {

        List<Long> idList = params?.filterCache?.data?.get(prefix + 'IdList')?.collect { it as Long }
        return idList ?: []
    }

    /**
     * Returns cached ElasticSearch query results
     * @param prefix the cache prefix
     * @param params the cached parameter map
     * @return the record map or an empty map, if not existing
     */
    static Map<String, Object> getCachedFilterESRecords(prefix, params) {

        Map<String, Object> esRecords = params?.filterCache?.data?.get(prefix + 'ESRecords')
        return esRecords ?: [:]
    }

    static void handleExpandoSubsetFilter(Object filter, String configKey, Map<String, Object> filterResult, GrailsParameterMap params) {

        BaseConfig.getCurrentConfig( configKey ).each { c ->
            String partKey = c.getKey()
            if (partKey != 'base') {
                String subsetClass = c.getValue().meta.class.simpleName
                String subsetFilter = '_handleSubset' + subsetClass + 'Filter'

                if (filter.metaClass.respondsTo(filter, subsetFilter).findAll{ it.parameterTypes.length == 3 }) {
                    println 'Reporting: called ' + configKey + '.' + subsetFilter + '(GPM) @ ' + partKey
                    filter."${subsetFilter}"(partKey, filterResult, params)
                }
                else if (filter.metaClass.respondsTo(filter, subsetFilter)) {
                    println 'Reporting: called ' + configKey + '.' + subsetFilter + ' @ ' + partKey
                    filter."${subsetFilter}"(partKey, filterResult)
                }
                else {
                    println 'Reporting: Subset Filter NOT FOUND ' + configKey + '.' + subsetFilter + ' @ ' + partKey
                }
            }
        }
    }
}
