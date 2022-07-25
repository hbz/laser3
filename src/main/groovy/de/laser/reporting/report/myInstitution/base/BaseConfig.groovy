package de.laser.reporting.report.myInstitution.base

import de.laser.ContextService
import de.laser.License
import de.laser.Org
import de.laser.Package
import de.laser.Platform
import de.laser.RefdataCategory
import de.laser.Subscription
import de.laser.SubscriptionsQueryService
import de.laser.auth.Role
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.properties.PropertyDefinition
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.myInstitution.config.CostItemXCfg
import de.laser.reporting.report.myInstitution.config.IssueEntitlementXCfg
import de.laser.reporting.report.myInstitution.config.LicenseConsCfg
import de.laser.reporting.report.myInstitution.config.LicenseInstCfg
import de.laser.reporting.report.myInstitution.config.OrganisationConsCfg
import de.laser.reporting.report.myInstitution.config.OrganisationInstCfg
import de.laser.reporting.report.myInstitution.config.PackageXCfg
import de.laser.reporting.report.myInstitution.config.PlatformXCfg
import de.laser.reporting.report.myInstitution.config.SubscriptionConsCfg
import de.laser.reporting.report.myInstitution.config.SubscriptionInstCfg
import groovy.util.logging.Slf4j
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder


import java.time.Year

@Slf4j
class BaseConfig {

    static String KEY_MYINST                    = 'myInstitution'
    static String KEY_LOCAL_SUBSCRIPTION        = 'localSubscription'

    static String KEY_COSTITEM                  = 'costItem'
    static String KEY_ISSUEENTITLEMENT          = 'issueEntitlement'
    static String KEY_LICENSE                   = 'license'
    static String KEY_ORGANISATION              = 'organisation'
    static String KEY_PACKAGE                   = 'package'
    static String KEY_PLATFORM                  = 'platform'
    static String KEY_SUBSCRIPTION              = 'subscription'

    static String FILTER_PREFIX                 = 'filter:'
    static String FILTER_SOURCE_POSTFIX         = '_source'

    static String FIELD_IS_MULTIPLE             = 'isMultiple'
    static String FIELD_IS_VIRTUAL              = 'isVirtual'

    static String FIELD_TYPE_PROPERTY           = 'property'
    static String FIELD_TYPE_REFDATA            = 'refdata'
    static String FIELD_TYPE_REFDATA_JOINTABLE  = 'refdataJoinTable'
    static String FIELD_TYPE_CUSTOM_IMPL        = 'customImplementation'
    static String FIELD_TYPE_ELASTICSEARCH      = 'elasticSearch'

    static String CHART_BAR                     = 'bar'
    static String CHART_PIE                     = 'pie'

    static String RDJT_GENERIC_ORG_TYPE         = 'orgType'

    static String CI_GENERIC_CUSTOMER_TYPE      = 'customerType'
    static String CI_GENERIC_LEGAL_INFO         = 'legalInfo'
    static String CI_GENERIC_ANNUAL             = 'annual'
    static String CI_GENERIC_ENDDATE_LIMIT      = 'endDateLimit'
    static String CI_GENERIC_STARTDATE_LIMIT    = 'startDateLimit'
    static String CI_GENERIC_SUBJECT_GROUP      = 'subjectGroup'

    static String CI_GENERIC_IE_STATUS                  = 'issueEntitlement$status'     // IE
    static String CI_GENERIC_PACKAGE_OR_PROVIDER        = 'package$orgRole$provider'    // IE, PKG
    static String CI_GENERIC_PACKAGE_PLATFORM           = 'package$platform'            // IE, PKG
    static String CI_GENERIC_PACKAGE_STATUS             = 'package$packageStatus'       // IE, PKG, PLT
    static String CI_GENERIC_PLATFORM_SERVICEPROVIDER   = 'platform$serviceProvider'    // PLT
    static String CI_GENERIC_PLATFORM_SOFTWAREPROVIDER  = 'platform$softwareProvider'   // PLT
    static String CI_GENERIC_PLATFORM_ORG               = 'platform$org'                // PLT
    static String CI_GENERIC_SUBSCRIPTION_STATUS        = 'subscription$status'         // PKG, PLT, IE

    static String CI_CTX_PROPERTY_KEY           = 'propertyKey'
    static String CI_CTX_PROPERTY_VALUE         = 'propertyValue'
    static String CI_CTX_IE_PACKAGE             = 'issueEntitlement$pkg'                // IE -- TODO
    static String CI_CTX_IE_SUBSCRIPTION        = 'issueEntitlement$subscription'       // IE

    static List<String> FILTER = [
            KEY_ORGANISATION, KEY_SUBSCRIPTION, KEY_LICENSE, KEY_PACKAGE, KEY_PLATFORM //, KEY_ISSUEENTITLEMENT // 'costItem'
    ]

    static List<String> CHARTS = [
            CHART_BAR, CHART_PIE
    ]

    static Class getCurrentConfigClass(String key) {

        if (key == KEY_COSTITEM) { CostItemXCfg }
        else if (key == KEY_ISSUEENTITLEMENT) { IssueEntitlementXCfg }
        else if (key == KEY_LICENSE) {
            if (BaseDetailsExport.ctxConsortium()) { LicenseConsCfg }
            else if (BaseDetailsExport.ctxInst()) { LicenseInstCfg }
        }
        else if (key == KEY_ORGANISATION) {
            if (BaseDetailsExport.ctxConsortium()) { OrganisationConsCfg }
            else if (BaseDetailsExport.ctxInst()) { OrganisationInstCfg }
        }
        else if (key == KEY_PACKAGE) { PackageXCfg }
        else if (key == KEY_PLATFORM) { PlatformXCfg }
        else if (key == KEY_SUBSCRIPTION) {
            if (BaseDetailsExport.ctxConsortium()) { SubscriptionConsCfg }
            else if (BaseDetailsExport.ctxInst()) { SubscriptionInstCfg }
        }
    }

    static Map<String, Map> getCurrentConfigDetailsTable(String key) {
        Class config = getCurrentConfigClass(key)

        if (config && config.getDeclaredFields().collect { it.getName() }.contains('CMB_ES_DT_CONFIG')) {
            config.CMB_ES_DT_CONFIG.subMap( config.CMB_ES_DT_CONFIG.findResults { it.value.containsKey('dtc') ? it.key : null } )
        } else {
            [:]
        }
    }
    static Map<String, Map> getCurrentConfigElasticsearchData(String key) {
        Class config = getCurrentConfigClass(key)

        if (config && config.getDeclaredFields().collect { it.getName() }.contains('CMB_ES_DT_CONFIG')) {
            config.CMB_ES_DT_CONFIG.subMap( config.CMB_ES_DT_CONFIG.findResults { it.value.containsKey('es') ? it.key : null } )
        } else {
            [:]
        }
    }

    static Map<String, Object> getCurrentConfig(String key) {
        Class config = getCurrentConfigClass(key)

        if (config && config.getDeclaredFields().collect { it.getName() }.contains('CONFIG')) {
            config.CONFIG
        } else {
            [:]
        }
    }

    static Map<String, Object> getCurrentConfigByFilter(String filter) {
        Map<String, Object> cfg = [:]
        // println '|--- BaseConfig.getCurrentConfigByFilterAndPrefix( ' + filter + ' )'

        if (filter in [ KEY_COSTITEM, KEY_ISSUEENTITLEMENT, KEY_LICENSE, KEY_ORGANISATION, KEY_PACKAGE, KEY_PLATFORM, KEY_SUBSCRIPTION ]) {
            cfg = getCurrentConfig( filter )
        }
        else if (filter in ['org']) {
            cfg = getCurrentConfig( BaseConfig.KEY_ORGANISATION )
        }
        else {
            log.warn 'getCurrentConfigByFilter() failed'
        }
        cfg
    }

//    static Map<String, Object> getCurrentConfigByPrefix(String prefix) {
//        Map<String, Object> cfg = [:]
//
//        if (prefix in [ KEY_COSTITEM ]) {
//            cfg = getCurrentConfig( BaseConfig.KEY_COSTITEM )
//        }
//        else if (prefix in [ KEY_ISSUEENTITLEMENT ]) {
//            cfg = getCurrentConfig( BaseConfig.KEY_ISSUEENTITLEMENT )
//        }
//        else if (prefix in [ KEY_LICENSE, 'licensor' ]) {
//            cfg = getCurrentConfig( BaseConfig.KEY_LICENSE )
//        }
//        else if (prefix in ['org']) {
//            cfg = getCurrentConfig( BaseConfig.KEY_ORGANISATION )
//        }
//        else if (prefix in [ KEY_PACKAGE ]) {
//            cfg = getCurrentConfig( BaseConfig.KEY_PACKAGE )
//        }
//        else if (prefix in [ KEY_PLATFORM]) {
//            cfg = getCurrentConfig( BaseConfig.KEY_PLATFORM )
//        }
//        else if (prefix in [ KEY_SUBSCRIPTION, 'memberSubscription', 'member', 'consortium', 'provider', 'agency' ]) {
//            cfg = getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION )
//        }
//
//        cfg
//    }

    static Map<String, Object> getCustomImplRefdata(String key) {
        getCustomImplRefdata(key, null)
    }

    static Map<String, Object> getCustomImplRefdata(String key, Class cfgClass) {

        ContextService contextService = BeanStore.getContextService()
        MessageSource messageSource = BeanStore.getMessageSource()
        SubscriptionsQueryService subscriptionsQueryService = BeanStore.getSubscriptionsQueryService()

        // println 'BaseConfig.getCustomImplRefdata() -> ' + cfgClass + ' ' + key

        Locale locale = LocaleContextHolder.getLocale()
        String ck = 'reporting.customImpl.'

        if (key == RDJT_GENERIC_ORG_TYPE) {
            return [
                    label: messageSource.getMessage('org.orgType.label', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.ORG_TYPE)
            ]
        }
        else if (key == CI_GENERIC_CUSTOMER_TYPE) {
            List<Role> roles = Role.findAllByRoleType('org')
            return [
                    label: messageSource.getMessage('org.setting.CUSTOMER_TYPE', null, locale),
                    from: roles.collect{[id: it.id,
                                         value_de: it.getI10n('authority', 'de'),
                                         value_en: it.getI10n('authority', 'en')
                    ] }
            ]
        }
        else if (key == CI_GENERIC_LEGAL_INFO) {
            Locale localeDe = new Locale.Builder().setLanguage("de").build()
            Locale localeEn = new Locale.Builder().setLanguage("en").build()

            return [
                    label: messageSource.getMessage(ck + 'legalInfo.label', null, locale),
                    from: [
                            [   id: 0,
                                value_de: messageSource.getMessage(ck + 'legalInfo.0', null, localeDe),
                                value_en: messageSource.getMessage(ck + 'legalInfo.0', null, localeEn),
                            ],
                            [   id: 1,
                                value_de: messageSource.getMessage(ck + 'legalInfo.1', null, localeDe),
                                value_en: messageSource.getMessage(ck + 'legalInfo.1', null, localeEn),
                            ],  // ui icon green check circle
                            [   id: 2,
                                value_de: messageSource.getMessage(ck + 'legalInfo.2', null, localeDe),
                                value_en: messageSource.getMessage(ck + 'legalInfo.2', null, localeEn),
                            ],  // ui icon grey outline circle
                            [   id: 3,
                                value_de: messageSource.getMessage(ck + 'legalInfo.3', null, localeDe),
                                value_en: messageSource.getMessage(ck + 'legalInfo.3', null, localeEn),
                            ]   // ui icon red question mark
                    ]]
        }
        else if (key == CI_GENERIC_ANNUAL) {
            Long y = Year.now().value // frontend
            return [
                    label: messageSource.getMessage(ck + 'annual.label', null, locale),
                    from: (y+2..y-4).collect{[ id: it, value_de: it, value_en: it] } + [ id: 0 as Long, value_de: 'Alle ohne Ablauf', value_en: 'Open-Ended']
            ]
        }
        else if (key == CI_GENERIC_ENDDATE_LIMIT) {
            Long y = Year.now().value // frontend
            return [
                    label: messageSource.getMessage(ck + 'endDateLimit.label', null, locale),
                    from: (y+2..y-4).collect{[ id: it, value_de: it, value_en: it] }
            ]
        }
        else if (key == CI_GENERIC_STARTDATE_LIMIT) {
            Long y = Year.now().value // frontend
            return [
                    label: messageSource.getMessage(ck + 'startDateLimit.label', null, locale),
                    from: (y..y-6).collect{[ id: it, value_de: it, value_en: it] }
            ]
        }
        else if (key == CI_GENERIC_SUBJECT_GROUP) {
            return [
                    label: messageSource.getMessage('org.subjectGroup.label', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.SUBJECT_GROUP)
            ]
        }
        else if (key == CI_GENERIC_IE_STATUS) {
            return [
                    label: messageSource.getMessage('default.status.label', null, locale),
                    from: [RDStore.TIPP_STATUS_CURRENT, RDStore.TIPP_STATUS_EXPECTED, RDStore.TIPP_STATUS_DELETED].collect{
                        [
                                id: it.id,
                                value_de: it.getI10n('value', 'de'),
                                value_en: it.getI10n('value', 'en'),
                        ]}
            ]
        }
        else if (key == CI_GENERIC_PACKAGE_OR_PROVIDER) {
            return [
                    label: messageSource.getMessage('default.provider.label', null, locale),
                    from: Org.executeQuery('select distinct(org) from Org org join org.orgType ot where ot in (:otList)',
                            [ otList: [RDStore.OT_PROVIDER] ]).collect{[
                            id: it.id,
                            value_de: it.shortname ? (it.shortname + ' - ' + it.name) : it.name,
                            value_en: it.shortname ? (it.shortname + ' - ' + it.name) : it.name,
                    ]}.sort({ a, b -> a.value_de.toLowerCase() <=> b.value_de.toLowerCase() })
            ]
        }
        else if (key == CI_GENERIC_PACKAGE_PLATFORM) {
            return [
                    label: messageSource.getMessage('platform.label', null, locale),
                    //from: Platform.executeQuery('select distinct(plt) from Package pkg join pkg.nominalPlatform plt order by plt.name').collect{[ // ?
                    from: Platform.executeQuery('select plt from Platform plt order by plt.name').collect{[
                            id: it.id,
                            value_de: it.name,
                            value_en: it.name,
                    ]}
            ]
        }
        else if (key == CI_GENERIC_PACKAGE_STATUS) {
            return [
                    label: messageSource.getMessage('reporting.cfg.query.package.package-packageStatus', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.PACKAGE_STATUS)
            ]
        }
        else if (key == CI_GENERIC_PLATFORM_ORG) {
            return [
                    label: messageSource.getMessage('platform.provider', null, locale),
                    from: Org.executeQuery('select distinct(org) from Platform plt join plt.org org').collect{[
                            id: it.id,
                            value_de: it.shortname ? (it.shortname + ' - ' + it.name) : it.name,
                            value_en: it.shortname ? (it.shortname + ' - ' + it.name) : it.name,
                    ]}.sort({ a, b -> a.value_de.toLowerCase() <=> b.value_de.toLowerCase() })
            ]
        }
        else if (key == CI_GENERIC_PLATFORM_SERVICEPROVIDER) {
            return [
                    label: messageSource.getMessage('platform.serviceProvider', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.Y_N)
            ]
        }
        else if (key == CI_GENERIC_PLATFORM_SOFTWAREPROVIDER) {
            return [
                    label: messageSource.getMessage('platform.softwareProvider', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.Y_N)
            ]
        }
        else if (key == CI_GENERIC_SUBSCRIPTION_STATUS) {
            return [
                    label: messageSource.getMessage('subscription.status.label', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)
            ]
        }
        else if (key == CI_CTX_PROPERTY_KEY) {
            String descr = ''

            if (cfgClass == License) { descr = PropertyDefinition.LIC_PROP }
            else if (cfgClass == Org) { descr = PropertyDefinition.ORG_PROP }
            else if (cfgClass == Subscription) { descr = PropertyDefinition.SUB_PROP }

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
        else if (key == CI_CTX_PROPERTY_VALUE) {
            return [
                    label: 'Merkmalswert (nur Referenzwerte)',
                    from: []
            ]
        }
        else if (key == CI_CTX_IE_PACKAGE) {

            List tmp = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([validOn: null], contextService.getOrg())
            List<Long> subIdList = Subscription.executeQuery( 'select s.id ' + tmp[0], tmp[1])

            List<Long> pkgList = Package.executeQuery(
                    'select distinct subPkg.pkg from SubscriptionPackage subPkg where subPkg.subscription.id in (:subIdList) and subPkg.pkg.packageStatus != :pkgStatus',
                    [subIdList: subIdList, pkgStatus: RDStore.PACKAGE_STATUS_DELETED]
            )

            return [
                    label: messageSource.getMessage('package.label', null, locale),
                    from: pkgList.collect{[
                            id: it.id,
                            value_de: it.getLabel(),
                            value_en: it.getLabel(),
                    ]}.sort({ a, b -> a.value_de.toLowerCase() <=> b.value_de.toLowerCase() })
            ]
        }
        else if (key == CI_CTX_IE_SUBSCRIPTION) {
            List query = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([validOn: null], contextService.getOrg())
            return [
                    label: messageSource.getMessage('subscription.label', null, locale),
                    from: Subscription.executeQuery( 'select s ' + query[0], query[1]).collect{
                        [
                                id: it.id,
                                value_de: it.getLabel(),
                                value_en: it.getLabel(),
                        ]}
            ]
        }
    }

    static Map<String, Object> getElasticSearchRefdata(String key) {

        // println 'BaseConfig.getElasticSearchRefdata() ' + key
        MessageSource messageSource = BeanStore.getMessageSource()
        Locale locale = LocaleContextHolder.getLocale()

        Map pkgMap = getCurrentConfigElasticsearchData(KEY_PACKAGE).get( KEY_PACKAGE + '-' + key ) as Map<String, Object>
        Map pltMap = getCurrentConfigElasticsearchData(KEY_PLATFORM).get( KEY_PLATFORM + '-' + key )  as Map<String, Object>

        if (pkgMap) {
            return [
                    label: messageSource.getMessage( pkgMap.label as String, null, locale) + ' (we:kb)',
                    from: RefdataCategory.getAllRefdataValues( pkgMap.rdc as String)
            ]
        }
        else if (pltMap) {
            return [
                    label: messageSource.getMessage( pltMap.label as String, null, locale) + ' (we:kb)',
                    from: RefdataCategory.getAllRefdataValues( pltMap.rdc as String)
            ]
        }
    }

    static String getLabel(String token) {
        //println 'getConfigLabel(): ' + key
        MessageSource messageSource = BeanStore.getMessageSource()
        messageSource.getMessage(token, null, LocaleContextHolder.getLocale())
    }

    static String getConfigLabel(def token) {
        getLabel('reporting.cfg.' + token)
    }

    static String getFilterLabel(String key) {
        getLabel('reporting.cfg.filter.' + key)
    }

    static String getSourceLabel(String key, String source) {
        getLabel('reporting.cfg.source.' + key + '.' + source)
    }

    static String getQueryLabel(String key, String qKey, List qValues) {
        if (qValues[0].startsWith('generic')) {
            getLabel('reporting.cfg.' + qValues[0])
        } else {
            getLabel('reporting.cfg.query.' + key + '.' + qKey)
        }
    }

    static String getDistributionLabel(String key, String dist) {
        getLabel('reporting.cfg.dist.' + key + '.' + dist)
    }
}
