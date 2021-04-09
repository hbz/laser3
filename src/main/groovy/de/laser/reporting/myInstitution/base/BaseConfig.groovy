package de.laser.reporting.myInstitution.base

import de.laser.RefdataCategory
import de.laser.auth.Role
import de.laser.helper.RDConstants
import grails.util.Holders
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

class BaseConfig {

    static String KEY                           = 'myInstitution'

    static String FILTER_PREFIX                 = 'filter:'
    static String FILTER_SOURCE_POSTFIX         = '_source'

    static String CHART_BAR                     = 'bar'
    static String CHART_PIE                     = 'pie'
    static String CHART_RADAR                   = 'radar'

    static String FIELD_TYPE_PROPERTY           = 'property'
    static String FIELD_TYPE_REFDATA            = 'refdata'
    static String FIELD_TYPE_REFDATA_JOINTABLE  = 'refdataJoinTable'
    static String FIELD_TYPE_CUSTOM_IMPL        = 'customImplementation'

    static String CUSTOM_KEY_SUBJECT_GROUP      = 'subjectGroup'
    static String CUSTOM_KEY_ORG_TYPE           = 'orgType'
    static String CUSTOM_KEY_CUSTOMER_TYPE      = 'customerType'
    static String CUSTOM_KEY_LEGAL_INFO         = 'legalInfo'

    static Map<String, String> FILTER = [

            organisation : 'Organisationen',
            subscription : 'Lizenzen',
            license      : 'Verträge',
            // costItem     : 'Kosten (* experimentelle Funktion)'
    ]

    static Map<String, String> CHARTS = [

            bar   : 'Balkendiagramm',
            pie   : 'Tortendiagramm',
            //radar : 'Netzdiagramm'
    ]

    static Map<String, Object> getCustomRefdata(String key) {

        MessageSource messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = LocaleContextHolder.getLocale()

        if (key == CUSTOM_KEY_SUBJECT_GROUP) {
            return [
                    label: messageSource.getMessage('org.subjectGroup.label', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.SUBJECT_GROUP)
            ]
        }
        else if (key == CUSTOM_KEY_ORG_TYPE) {
            return [
                    label: messageSource.getMessage('org.orgType.label', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.ORG_TYPE)
            ]
        }
        else if (key == CUSTOM_KEY_CUSTOMER_TYPE) {
            List<Role> roles = Role.findAllByRoleType('org')
            return [
                    label: messageSource.getMessage('org.setting.CUSTOMER_TYPE', null, locale),
                    from: roles.collect{[ id: it.id, value_de: it.getI10n('authority') ] }
            ]
        }
        else if (key == CUSTOM_KEY_LEGAL_INFO) {
            return [
                    label: 'Erstellt bzw. organisiert durch ..', // TODO
                    from: [
                        [id: 0, value_de: 'Keine Einträge'],
                        [id: 1, value_de: 'Erstellt von / Organisiert durch (beides)'], // ui icon green check circle
                        [id: 2, value_de: 'Erstellt von (exklusive)'],                  // ui icon grey outline circle
                        [id: 3, value_de: 'Organisiert durch (exklusive)']              // ui icon red question mark
            ]]
        }
    }
}
