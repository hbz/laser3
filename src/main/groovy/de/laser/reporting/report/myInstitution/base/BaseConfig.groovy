package de.laser.reporting.report.myInstitution.base

import de.laser.ContextService
import de.laser.License
import de.laser.Org
import de.laser.RefdataCategory
import de.laser.Subscription
import de.laser.auth.Role
import de.laser.helper.RDConstants
import de.laser.properties.PropertyDefinition
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.myInstitution.config.CostItemXCfg
import de.laser.reporting.report.myInstitution.config.LicenseConsCfg
import de.laser.reporting.report.myInstitution.config.LicenseInstCfg
import de.laser.reporting.report.myInstitution.config.OrganisationConsCfg
import de.laser.reporting.report.myInstitution.config.OrganisationInstCfg
import de.laser.reporting.report.myInstitution.config.PackageXCfg
import de.laser.reporting.report.myInstitution.config.PlatformXCfg
import de.laser.reporting.report.myInstitution.config.SubscriptionConsCfg
import de.laser.reporting.report.myInstitution.config.SubscriptionInstCfg
import grails.util.Holders
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

import java.time.Year

class BaseConfig {

    static String KEY_MYINST                    = 'myInstitution'

    static String KEY_COSTITEM                  = 'costItem'
    static String KEY_PACKAGE                   = 'package'
    static String KEY_PLATFORM                  = 'platform'
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
    static String FIELD_TYPE_ELASTICSEARCH      = 'elasticSearch'

    static String CUSTOM_IMPL_KEY_SUBJECT_GROUP     = 'subjectGroup'
    static String CUSTOM_IMPL_KEY_ORG_TYPE          = 'orgType'
    static String CUSTOM_IMPL_KEY_CUSTOMER_TYPE     = 'customerType'
    static String CUSTOM_IMPL_KEY_LEGAL_INFO        = 'legalInfo'
    static String CUSTOM_IMPL_KEY_ANNUAL            = 'annual'
    static String CUSTOM_IMPL_KEY_STARTDATE_LIMIT   = 'startDateLimit'
    static String CUSTOM_IMPL_KEY_ENDDATE_LIMIT     = 'endDateLimit'
    static String CUSTOM_IMPL_KEY_PROPERTY_KEY      = 'propertyKey'
    static String CUSTOM_IMPL_KEY_PROPERTY_VALUE    = 'propertyValue'

    static String ELASTICSEARCH_KEY_PKG_BREAKABLE   = 'breakable'
    static String ELASTICSEARCH_KEY_PKG_CONSISTENT  = 'consistent'
    static String ELASTICSEARCH_KEY_PKG_OPENACCESS  = 'openAccess'
    static String ELASTICSEARCH_KEY_PKG_PAYMENTTYPE = 'paymentType'
    static String ELASTICSEARCH_KEY_PKG_SCOPE       = 'scope'

    static String CUSTOM_IMPL_KEY_PLT_ORG               = 'org'
    static String CUSTOM_IMPL_KEY_PLT_SERVICEPROVIDER   = 'serviceProvider'
    static String CUSTOM_IMPL_KEY_PLT_SOFTWAREPROVIDER  = 'softwareProvider'

    static String ELASTICSEARCH_KEY_PLT_IP_AUTHENTICATION           = 'ipAuthentication'
    static String ELASTICSEARCH_KEY_PLT_SHIBBOLETH_AUTHENTICATION   = 'shibbolethAuthentication'
    static String ELASTICSEARCH_KEY_PLT_PASSWORD_AUTHENTICATION     = 'passwordAuthentication'
    static String ELASTICSEARCH_KEY_PLT_PROXY_SUPPORTED             = 'proxySupported'

    static List<String> FILTER = [
            KEY_ORGANISATION, KEY_SUBSCRIPTION, KEY_LICENSE, KEY_PACKAGE, KEY_PLATFORM // 'costItem'
    ]

    static List<String> CHARTS = [
            CHART_BAR, CHART_PIE
    ]

    static Map<String, Object> getCurrentConfigByPrefix(String prefix) {

        Map<String, Object> cfg = [:]

        if (prefix in [ KEY_COSTITEM ]) {
            cfg = getCurrentConfig( BaseConfig.KEY_COSTITEM )
        }
        else if (prefix in [ KEY_LICENSE, 'licensor' ]) {
            cfg = getCurrentConfig( BaseConfig.KEY_LICENSE )
        }
        else if (prefix in ['org']) {
            cfg = getCurrentConfig( BaseConfig.KEY_ORGANISATION )
        }
        else if (prefix in [ KEY_PACKAGE ]) {
            cfg = getCurrentConfig( BaseConfig.KEY_PACKAGE )
        }
        else if (prefix in [ KEY_PLATFORM]) {
            cfg = getCurrentConfig( BaseConfig.KEY_PLATFORM )
        }
        else if (prefix in [ KEY_SUBSCRIPTION, 'memberSubscription', 'member', 'consortium', 'provider', 'agency' ]) {
            cfg = getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION )
        }
        cfg
    }

    static Map<String, Object> getCurrentConfig(String key) {

        if (key == KEY_COSTITEM) {
            CostItemXCfg.CONFIG
        }
        else if (key == KEY_LICENSE) {
            if (BaseDetailsExport.ctxConsortium()) {
                LicenseConsCfg.CONFIG
            }
            else if (BaseDetailsExport.ctxInst()) {
                LicenseInstCfg.CONFIG
            }
        }
        else if (key == KEY_ORGANISATION) {
            if (BaseDetailsExport.ctxConsortium()) {
                OrganisationConsCfg.CONFIG
            }
            else if (BaseDetailsExport.ctxInst()) {
                OrganisationInstCfg.CONFIG
            }
        }
        else if (key == KEY_PACKAGE) {
            return PackageXCfg.CONFIG
        }
        else if (key == KEY_PLATFORM) {
            return PlatformXCfg.CONFIG
        }
        else if (key == KEY_SUBSCRIPTION) {
            if (BaseDetailsExport.ctxConsortium()) {
                SubscriptionConsCfg.CONFIG
            }
            else if (BaseDetailsExport.ctxInst()) {
                SubscriptionInstCfg.CONFIG
            }
        }
    }

    static List<String> getCurrentEsData(String key) {
        if (key == KEY_PACKAGE) {
            return PackageXCfg.ES_DATA
        }
        else if (key == KEY_PLATFORM) {
            return PlatformXCfg.ES_DATA
        }
    }

    static Map<String, Object> getCustomImplRefdata(String key) {
        getCustomImplRefdata(key, null)
    }

    static Map<String, Object> getCustomImplRefdata(String key, Class clazz) {

        ApplicationContext mainContext = Holders.grailsApplication.mainContext
        ContextService contextService  = mainContext.getBean('contextService')
        MessageSource messageSource    = mainContext.getBean('messageSource')
        
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
                    label: 'Merkmalswert (nur Referenzwerte)',
                    from: []
            ]
        }
        else if (key == CUSTOM_IMPL_KEY_PLT_ORG) { // TODO
            return [
                    label: messageSource.getMessage('platform.provider', null, locale),
                    from: Org.findAll().collect{[
                        id: it.id,
                        value_de: it.name,
                        value_en: it.name,
                ]}
            ]
        }
        else if (key == CUSTOM_IMPL_KEY_PLT_SERVICEPROVIDER) {
            return [
                    label: messageSource.getMessage('platform.serviceProvider', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.Y_N)
            ]
        }
        else if (key == CUSTOM_IMPL_KEY_PLT_SOFTWAREPROVIDER) {
            return [
                    label: messageSource.getMessage('platform.softwareProvider', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.Y_N)
            ]
        }
        else {
            getElasticSearchRefdata(key) // TODO
        }
    }

    static Map<String, Object> getElasticSearchRefdata(String key) {

        ApplicationContext mainContext = Holders.grailsApplication.mainContext
        MessageSource messageSource = mainContext.getBean('messageSource')

        Locale locale = LocaleContextHolder.getLocale()

        if (key == ELASTICSEARCH_KEY_PKG_BREAKABLE) {
            return [
                    label: messageSource.getMessage('package.breakable', null, locale) + ' (we:kb)',
                    from: RefdataCategory.getAllRefdataValues( RDConstants.PACKAGE_BREAKABLE )
            ]
        }
        else if (key == ELASTICSEARCH_KEY_PKG_CONSISTENT) {
            return [
                    label: messageSource.getMessage('package.consistent', null, locale) + ' (we:kb)',
                    from: RefdataCategory.getAllRefdataValues( RDConstants.PACKAGE_CONSISTENT )
            ]
        }
        else if (key == ELASTICSEARCH_KEY_PKG_OPENACCESS) {
            return [
                    label: messageSource.getMessage('package.openAccess.label', null, locale) + ' (we:kb)',
                    from: RefdataCategory.getAllRefdataValues( RDConstants.LICENSE_OA_TYPE )
            ]
        }
        else if (key == ELASTICSEARCH_KEY_PKG_PAYMENTTYPE) {
            return [
                    label: messageSource.getMessage('package.paymentType.label', null, locale) + ' (we:kb)',
                    from: RefdataCategory.getAllRefdataValues( RDConstants.PAYMENT_TYPE )
            ]
        }
        else if (key == ELASTICSEARCH_KEY_PKG_SCOPE) {
            return [

                    label: messageSource.getMessage('package.scope.label', null, locale) + ' (we:kb)',
                    from: RefdataCategory.getAllRefdataValues( RDConstants.PACKAGE_SCOPE )
            ]
        }
        else if (key == ELASTICSEARCH_KEY_PLT_IP_AUTHENTICATION) {
            return [
                    label: messageSource.getMessage('platform.auth.ip.supported', null, locale) + ' (we:kb)',
                    from: RefdataCategory.getAllRefdataValues(RDConstants.IP_AUTHENTICATION)
            ]
        }
        else if (key == ELASTICSEARCH_KEY_PLT_SHIBBOLETH_AUTHENTICATION) {
            return [
                    label: messageSource.getMessage('platform.auth.shibboleth.supported', null, locale) + ' (we:kb)',
                    from: RefdataCategory.getAllRefdataValues(RDConstants.Y_N)
            ]
        }
        else if (key == ELASTICSEARCH_KEY_PLT_PASSWORD_AUTHENTICATION) {
            return [
                    label: messageSource.getMessage('platform.auth.userPass.supported', null, locale) + ' (we:kb)',
                    from: RefdataCategory.getAllRefdataValues(RDConstants.Y_N)
            ]
        }
        else if (key == ELASTICSEARCH_KEY_PLT_PROXY_SUPPORTED) {
            return [
                    label: messageSource.getMessage('platform.auth.proxy.supported', null, locale) + ' (we:kb)',
                    from: RefdataCategory.getAllRefdataValues(RDConstants.Y_N)
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
