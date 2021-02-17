package de.laser.reporting

import de.laser.RefdataCategory
import de.laser.helper.RDConstants
import grails.web.servlet.mvc.GrailsParameterMap

class GenericConfig {

    static String KEY                           = 'Generic'

    static String FILTER_PREFIX                 = 'filter:'

    static String FORM_TYPE_PROPERTY            = 'property'
    static String FORM_TYPE_REFDATA             = 'refdata'
    static String FORM_TYPE_REFDATA_RELTABLE    = 'refdataRelationTable'

    static Map<String, String> FILTER = [

            organisation : 'Organisationen',
            subscription : 'Lizenzen'
    ]

    static Map<String, String> CHARTS = [

            bar : 'Balkendiagramm',
            pie : 'Tortendiagramm'
    ]

    static Map<String, Object> CONFIG = [

            object : [
                    meta    : [],
                    form    : [],
                    filter  : [],
                    query   : []
            ]
    ]

    static String getFormFieldType(Map<String, Object> objConfig, String fieldName) {

        String fieldType = '' // [ property, refdata ]

        objConfig.form.each {
            if (it.keySet().contains(fieldName)) {
                fieldType = it.get(fieldName)
            }
        }
        fieldType
    }

    static String getQueryLabel(Map<String, Object> config, GrailsParameterMap params) {

        String label = ''
        //println params

        config.each {it ->
            if (it.value.containsKey('query')) {
                it.value.get('query').each { it2 ->
                    println it2
                    if (it2.value.containsKey(params.query)) {
                        label = it2.key + ' > ' + it2.value.get(params.query) + ' : ' + params.label
                    }
                }
            }
        }
        label
    }

    static Map<String, Object> getRefdataRelTableInfo(String key) {

        if (key == 'subjectGroup') {
            return [ label: 'Fächergruppe', from: RefdataCategory.getAllRefdataValues(RDConstants.SUBJECT_GROUP) ]
        }
    }
}
