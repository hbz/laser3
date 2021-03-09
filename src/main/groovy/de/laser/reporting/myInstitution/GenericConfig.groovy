package de.laser.reporting.myInstitution

import de.laser.RefdataCategory
import de.laser.auth.Role
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
    static String FIELD_TYPE_CUSTOM_IMPL        = 'customImplementation'

    static String CUSTOM_KEY_SUBJECT_GROUP      = 'subjectGroup'
    static String CUSTOM_KEY_ORG_TYPE           = 'orgType'
    static String CUSTOM_KEY_CUSTOMER_TYPE      = 'customerType'
    static String CUSTOM_KEY_LEGAL_INFO         = 'legalInfo'

    static Map<String, String> FILTER = [

            organisation : 'Organisationen',
            subscription : 'Lizenzen',
            license      : 'Verträge'
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
                    query   : [],
                    query2  : []
            ]
    ]

    static Map<String, Object> getCustomRefdata(String key) {

        if (key == CUSTOM_KEY_SUBJECT_GROUP) {
            return [
                    label: 'Fächergruppe',
                    from: RefdataCategory.getAllRefdataValues(RDConstants.SUBJECT_GROUP)
            ]
        }
        else if (key == CUSTOM_KEY_ORG_TYPE) {
            return [
                    label: 'Organisationstyp',
                    from: RefdataCategory.getAllRefdataValues(RDConstants.ORG_TYPE)
            ]
        }
        else if (key == CUSTOM_KEY_CUSTOMER_TYPE) {
            List<Role> roles = Role.findAllByRoleType('org')
            return [
                    label: 'Kundentyp',
                    from: roles.collect{[ id: it.id, value_de: it.getI10n('authority') ] }
            ]
        }
        else if (key == CUSTOM_KEY_LEGAL_INFO) {
            return [
                    label: 'Erstellt bzw. organisiert durch ..',
                    from: [
                        [id: 0, value_de: 'Keine Einträge'],
                        [id: 1, value_de: 'Erstellt von / Organisiert durch (beides)'], // ui icon green check circle
                        [id: 2, value_de: 'Erstellt von (exklusive)'],                  // ui icon grey outline circle
                        [id: 3, value_de: 'Organisiert durch (exklusive)']              // ui icon red question mark
            ]]
        }
    }
}
