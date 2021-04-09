package de.laser.reporting.myInstitution.base


import grails.web.servlet.mvc.GrailsParameterMap

class BaseFilter {

    static String getFilterSourceLabel(Map<String, Object> objConfig, String key) {
        objConfig.source.get(key)
    }

    static Set<String> getCurrentFilterKeys(GrailsParameterMap params, String cmbKey) {

        params.keySet().findAll{ it.toString().startsWith(cmbKey) && ! it.toString().endsWith(BaseConfig.FILTER_SOURCE_POSTFIX) }
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
}
