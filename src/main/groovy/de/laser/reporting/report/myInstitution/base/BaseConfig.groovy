package de.laser.reporting.report.myInstitution.base

import de.laser.ContextService
import de.laser.License
import de.laser.Org
import de.laser.RefdataCategory
import de.laser.Subscription
import de.laser.auth.Role
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.helper.RDConstants
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import de.laser.reporting.report.myInstitution.config.CostItemXCfg
import de.laser.reporting.report.myInstitution.config.LicenseConsCfg
import de.laser.reporting.report.myInstitution.config.LicenseInstCfg
import de.laser.reporting.report.myInstitution.config.OrganisationConsCfg
import de.laser.reporting.report.myInstitution.config.OrganisationInstCfg
import de.laser.reporting.report.myInstitution.config.SubscriptionConsCfg
import de.laser.reporting.report.myInstitution.config.SubscriptionInstCfg
import grails.util.Holders
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

import java.time.Year

class BaseConfig {

    static String KEY_MYINST                    = 'myInstitution'

    static String KEY_COSTITEM                  = 'costItem'
    static String KEY_LICENSE                   = 'license'
    static String KEY_ORGANISATION              = 'organisation'
    static String KEY_SUBSCRIPTION              = 'subscription'

    static String FILTER_PREFIX                 = 'filter:'
    static String FILTER_SOURCE_POSTFIX         = '_source'

    static String CHART_BAR                     = 'bar'
    static String CHART_PIE                     = 'pie'

    static String FIELD_TYPE_PROPERTY           = 'property'
    static String FIELD_TYPE_REFDATA            = 'refdata'
    static String FIELD_TYPE_REFDATA_JOINTABLE  = 'refdataJoinTable'
    static String FIELD_TYPE_CUSTOM_IMPL        = 'customImplementation'

    static String CUSTOM_IMPL_KEY_SUBJECT_GROUP     = 'subjectGroup'
    static String CUSTOM_IMPL_KEY_ORG_TYPE          = 'orgType'
    static String CUSTOM_IMPL_KEY_CUSTOMER_TYPE     = 'customerType'
    static String CUSTOM_IMPL_KEY_LEGAL_INFO        = 'legalInfo'
    static String CUSTOM_IMPL_KEY_ANNUAL            = 'annual'
    static String CUSTOM_IMPL_KEY_STARTDATE_LIMIT   = 'startDateLimit'
    static String CUSTOM_IMPL_KEY_ENDDATE_LIMIT     = 'endDateLimit'
    static String CUSTOM_IMPL_KEY_PROPERTY_KEY      = 'propertyKey'
    static String CUSTOM_IMPL_KEY_PROPERTY_VALUE    = 'propertyValue'

    static List<String> FILTER = [
            'organisation', 'subscription', 'license' // 'costItem'
    ]

    static List<String> CHARTS = [
            'bar', 'pie'
    ]

    static Map<String, Object> getCurrentConfigByPrefix(String prefix) {

        Map<String, Object> cfg = [:]

        if (prefix in ['license', 'licensor']) {
            cfg = getCurrentConfig( BaseConfig.KEY_LICENSE )
        }
        else if (prefix in ['org']) {
            cfg = getCurrentConfig( BaseConfig.KEY_ORGANISATION )
        }
        else if (prefix in ['subscription', 'memberSubscription', 'member', 'consortium', 'provider', 'agency']) {
            cfg = getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION )
        }
        else if (prefix in ['costItem']) {
            cfg = getCurrentConfig( BaseConfig.KEY_COSTITEM )
        }

        cfg
    }

    static Map<String, Object> getCurrentConfig(String key) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        if (key == KEY_COSTITEM) {

            CostItemXCfg.CONFIG
        }
        else if (key == KEY_LICENSE) {

            if (contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM') {
                LicenseConsCfg.CONFIG
            }
            else if (contextService.getOrg().getCustomerType() == 'ORG_INST') {
                LicenseInstCfg.CONFIG
            }
        }
        else if (key == KEY_ORGANISATION) {

            if (contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM') {
                OrganisationConsCfg.CONFIG
            }
            else if (contextService.getOrg().getCustomerType() == 'ORG_INST') {
                OrganisationInstCfg.CONFIG
            }
        }
        else if (key == KEY_SUBSCRIPTION) {

            if (contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM') {
                SubscriptionConsCfg.CONFIG
            }
            else if (contextService.getOrg().getCustomerType() == 'ORG_INST') {
                SubscriptionInstCfg.CONFIG
            }
        }
    }

    static Map<String, Object> getCustomImplRefdata(String key) {
        getCustomImplRefdata(key, null)
    }

    static Map<String, Object> getCustomImplRefdata(String key, Class clazz) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')
        MessageSource messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = LocaleContextHolder.getLocale()
        String ck = 'reporting.cfg.base.custom'

        if (key == CUSTOM_IMPL_KEY_SUBJECT_GROUP) {
            return [
                    label: messageSource.getMessage('org.subjectGroup.label', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.SUBJECT_GROUP)
            ]
        }
        else if (key == CUSTOM_IMPL_KEY_ORG_TYPE) {
            return [
                    label: messageSource.getMessage('org.orgType.label', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.ORG_TYPE)
            ]
        }
        else if (key == CUSTOM_IMPL_KEY_CUSTOMER_TYPE) {
            List<Role> roles = Role.findAllByRoleType('org')
            return [
                    label: messageSource.getMessage('org.setting.CUSTOMER_TYPE', null, locale),
                    from: roles.collect{[id: it.id,
                                         value_de: it.getI10n('authority', 'de'),
                                         value_en: it.getI10n('authority', 'en')
                    ] }
            ]
        }
        else if (key == CUSTOM_IMPL_KEY_LEGAL_INFO) {
            Locale localeDe = new Locale.Builder().setLanguage("de").build()
            Locale localeEn = new Locale.Builder().setLanguage("en").build()

            return [
                    label: messageSource.getMessage(ck + '.legalInfo.label', null, locale),
                    from: [
                        [   id: 0,
                            value_de: messageSource.getMessage(ck + '.legalInfo.0', null, localeDe),
                            value_en: messageSource.getMessage(ck + '.legalInfo.0', null, localeEn),
                        ],
                        [   id: 1,
                            value_de: messageSource.getMessage(ck + '.legalInfo.1', null, localeDe),
                            value_en: messageSource.getMessage(ck + '.legalInfo.1', null, localeEn),
                        ],  // ui icon green check circle
                        [   id: 2,
                            value_de: messageSource.getMessage(ck + '.legalInfo.2', null, localeDe),
                            value_en: messageSource.getMessage(ck + '.legalInfo.2', null, localeEn),
                        ],  // ui icon grey outline circle
                        [   id: 3,
                            value_de: messageSource.getMessage(ck + '.legalInfo.3', null, localeDe),
                            value_en: messageSource.getMessage(ck + '.legalInfo.3', null, localeEn),
                        ]   // ui icon red question mark
            ]]
        }
        else if (key == CUSTOM_IMPL_KEY_ANNUAL) {
            int y = Year.now().value
            return [
                    label: messageSource.getMessage(ck + '.annual.label', null, locale),
                    from: (y+2..y-4).collect{[ id: it, value_de: it, value_en: it] } + [ id: 0, value_de: 'Alle ohne Ablauf', value_en: 'Open-Ended']
            ]
        }
        else if (key == CUSTOM_IMPL_KEY_STARTDATE_LIMIT) {
            int y = Year.now().value
            return [
                    label: messageSource.getMessage(ck + '.startDateLimit.label', null, locale),
                    from: (y..y-6).collect{[ id: it, value_de: it, value_en: it] }
            ]
        }
        else if (key == CUSTOM_IMPL_KEY_ENDDATE_LIMIT) {
            int y = Year.now().value
            return [
                    label: messageSource.getMessage(ck + '.endDateLimit.label', null, locale),
                    from: (y+2..y-4).collect{[ id: it, value_de: it, value_en: it] }
            ]
        }
        else if (key == CUSTOM_IMPL_KEY_PROPERTY_KEY) {
            String descr = ''

            if (clazz == License) { descr = PropertyDefinition.LIC_PROP }
            else if (clazz == Org) { descr = PropertyDefinition.ORG_PROP }
            else if (clazz == Subscription) { descr = PropertyDefinition.SUB_PROP }

            List<PropertyDefinition> propList = descr ? PropertyDefinition.executeQuery(
                    'select pd from PropertyDefinition pd where pd.descr = :descr and (pd.tenant is null or pd.tenant = :ctx) order by pd.name_de',
                    [descr: descr, ctx: contextService.getOrg()]
            ) : []

            return [
                    label: 'Merkmal',
                    from: propList.collect{[
                            id: it.id,
                            value_de: it.name_de,
                            value_en: it.name_en,
                    ]}
            ]
        }
        else if (key == CUSTOM_IMPL_KEY_PROPERTY_VALUE) {
            return [
                    label: 'Merkmalswert',
                    from: []
            ]
        }
    }

    static String getMessage(String token) {
        MessageSource messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = LocaleContextHolder.getLocale()

        // println ' ---> ' + 'reporting.cfg.' + token
        messageSource.getMessage('reporting.cfg.' + token, null, locale)
    }
}
