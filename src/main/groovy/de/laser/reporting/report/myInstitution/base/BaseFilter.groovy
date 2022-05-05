package de.laser.reporting.report.myInstitution.base

import de.laser.ContextService
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.storage.BeanStore
import de.laser.properties.PropertyDefinition
import grails.web.servlet.mvc.GrailsParameterMap

class BaseFilter {

    static String getCurrentFilterSource(GrailsParameterMap params, String source) {
        params.get(BaseConfig.FILTER_PREFIX + source + BaseConfig.FILTER_SOURCE_POSTFIX)
    }

    static Set<String> getCurrentFilterKeys(GrailsParameterMap params, String cmbKey) {
        params.keySet().findAll{ it.toString().startsWith(cmbKey) && ! it.toString().endsWith(BaseConfig.FILTER_SOURCE_POSTFIX) }
    }

    static List<String> getRestrictedConfigSources(Map<String, Object> config) {
        ContextService contextService = BeanStore.getContextService()

        if (contextService.getUser().hasRole(['ROLE_ADMIN', 'ROLE_YODA'])) {
            config.source
        }
        else {
            config.source.findAll{ ! it.endsWith('-deleted') } as List<String>
        }
    }

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

    static List<Long> getCachedFilterIdList(prefix, params) {

        List<Long> idList = params?.filterCache?.data?.get(prefix + 'IdList')?.collect { it as Long }
        return idList ?: []
    }

    static Map<String, Object> getCachedFilterESRecords(prefix, params) {

        Map<String, Object> esRecords = params?.filterCache?.data?.get(prefix + 'ESRecords')
        return esRecords ?: [:]
    }
}
