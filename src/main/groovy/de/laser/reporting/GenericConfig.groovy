package de.laser.reporting

import de.laser.RefdataCategory
import de.laser.helper.RDConstants

class GenericConfig {

    static String KEY                           = 'Generic'

    static String FILTER_PREFIX                 = 'filter:'
    static String FILTER_SOURCE_POSTFIX         = '_source'

    static String CHART_BAR                     = 'bar'
    static String CHART_PIE                     = 'pie'

    static String FIELD_TYPE_PROPERTY           = 'property'
    static String FIELD_TYPE_REFDATA            = 'refdata'
    static String FIELD_TYPE_REFDATA_RELTABLE   = 'refdataRelationTable'

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
                    source  : [],
                    filter  : [],
                    query   : []
            ]
    ]

    static Map<String, Object> getRefdataRelTableInfo(String key) {

        if (key == 'subjectGroup') {
            return [ label: 'Fächergruppe', from: RefdataCategory.getAllRefdataValues(RDConstants.SUBJECT_GROUP) ]
        }
    }
}
