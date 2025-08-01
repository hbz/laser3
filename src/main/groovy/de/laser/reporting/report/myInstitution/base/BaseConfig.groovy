package de.laser.reporting.report.myInstitution.base

import de.laser.ContextService
import de.laser.License
import de.laser.Org
import de.laser.wekb.Package
import de.laser.wekb.PackageVendor
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.RefdataCategory
import de.laser.Subscription
import de.laser.SubscriptionsQueryService
import de.laser.auth.Role
import de.laser.ui.Icon
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
import de.laser.reporting.report.myInstitution.config.ProviderXCfg
import de.laser.reporting.report.myInstitution.config.SubscriptionConsCfg
import de.laser.reporting.report.myInstitution.config.SubscriptionInstCfg
import de.laser.reporting.report.myInstitution.config.VendorXCfg
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import groovy.util.logging.Slf4j
import org.springframework.context.MessageSource


import java.time.Year

/**
 * This class contains general methods for retrieving configuration parameters.
 * It is moreover the base class for the detailed object configuration, containing parameters for data sources and display.
 * The subclasses are not annotated in detail!
 */
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
    static String KEY_PROVIDER                  = 'provider'
    static String KEY_SUBSCRIPTION              = 'subscription'
    static String KEY_VENDOR                    = 'vendor'

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

    static String CI_GENERIC_CUSTOMER_TYPE      = 'customerType'
    static String CI_GENERIC_LEGAL_INFO         = 'legalInfo'
    static String CI_GENERIC_ANNUAL             = 'annual'
    static String CI_GENERIC_REFERENCE_YEAR     = 'referenceYear'
    static String CI_GENERIC_ENDDATE_LIMIT      = 'endDateLimit'
    static String CI_GENERIC_STARTDATE_LIMIT    = 'startDateLimit'
    static String CI_GENERIC_SUBJECT_GROUP      = 'subjectGroup'

    static String CI_GENERIC_INVOICING_FORMAT     = 'electronicBillings'
    static String CI_GENERIC_INVOICING_DISPATCH   = 'invoiceDispatchs'

    static String CI_GENERIC_IE_STATUS                  = 'issueEntitlement$status'     // IE
//    static String CI_GENERIC_PACKAGE_OR_PROVIDER        = 'package$orgRole$provider'    // IE, PKG
    static String CI_GENERIC_PACKAGE_PLATFORM           = 'package$platform'            // IE, PKG
    static String CI_GENERIC_PACKAGE_PACKAGESTATUS      = 'package$packageStatus'       // IE, PKG, PLT
    static String CI_GENERIC_PACKAGE_PROVIDER           = 'package$provider'            // PKG
    static String CI_GENERIC_PACKAGE_VENDOR             = 'package$vendor'              // PKG
    static String CI_GENERIC_PLATFORM_SERVICEPROVIDER   = 'platform$serviceProvider'    // PLT
    static String CI_GENERIC_PLATFORM_SOFTWAREPROVIDER  = 'platform$softwareProvider'   // PLT
    static String CI_GENERIC_PLATFORM_PROVIDER          = 'platform$provider'           // PLT
    static String CI_GENERIC_SUBSCRIPTION_STATUS        = 'subscription$status'         // PKG, PLT, IE

    static String CI_CTX_PROPERTY_KEY           = 'propertyKey'
    static String CI_CTX_PROPERTY_VALUE         = 'propertyValue'
    static String CI_CTX_IE_PACKAGE             = 'issueEntitlement$pkg'                // IE -- TODO
    static String CI_CTX_IE_SUBSCRIPTION        = 'issueEntitlement$subscription'       // IE

    static List<String> FILTER = [
            KEY_SUBSCRIPTION,
            KEY_LICENSE,
            KEY_PROVIDER,
            KEY_VENDOR,
            KEY_PLATFORM,
            KEY_PACKAGE,
            KEY_ORGANISATION,
            // KEY_ISSUEENTITLEMENT,
            // 'costItem'
    ]

    static List<String> CHARTS = [
            CHART_BAR,
            CHART_PIE
    ]

    static Map<String, Object> GENERIC_PROVIDER_QUERY_DEFAULT = [
        provider : [
            'provider-paperInvoice' :                       [ 'generic.provider.paperInvoice' ],
            'provider-managementOfCredits' :                [ 'generic.provider.managementOfCredits' ],
            'provider-processingOfCompensationPayments' :   [ 'generic.provider.processingOfCompensationPayments' ],
            'provider-individualInvoiceDesign' :            [ 'generic.provider.individualInvoiceDesign' ],
            'provider-status' :                             [ 'generic.provider.status' ],
            'provider-*' :                                  [ 'generic.all' ]
        ]
    ]

    static Map<String, Object> GENERIC_VENDOR_QUERY_DEFAULT = [
        vendor : [
            'vendor-paperInvoice' :                         [ 'generic.vendor.paperInvoice' ],
            'vendor-managementOfCredits' :                  [ 'generic.vendor.managementOfCredits' ],
            'vendor-processingOfCompensationPayments' :     [ 'generic.vendor.processingOfCompensationPayments' ],
            'vendor-individualInvoiceDesign' :              [ 'generic.vendor.individualInvoiceDesign' ],
            'vendor-status' :                               [ 'generic.vendor.status' ],
            'vendor-*' :                                    [ 'generic.all' ],
        ]
    ]

    /**
     * Determines the configuration class for the given object type key
     * @param key the object type key
     * @return the appropriate configuration {@link Class}
     */
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
        else if (key == KEY_PROVIDER) { ProviderXCfg }
        else if (key == KEY_SUBSCRIPTION) {
            if (BaseDetailsExport.ctxConsortium()) { SubscriptionConsCfg }
            else if (BaseDetailsExport.ctxInst()) { SubscriptionInstCfg }
        }
        else if (key == KEY_VENDOR) { VendorXCfg }
    }

    /**
     * Gets for the given object type the configuration details
     * @param key the object type to which the configuration table should be retrieved
     * @return the configuration details map
     */
    static Map<String, Map> getCurrentConfigDetailsTable(String key) {
        Class config = getCurrentConfigClass(key)

        if (config && config.getDeclaredFields().collect { it.getName() }.contains('CONFIG_DTC_ES')) {
            config.CONFIG_DTC_ES.subMap( config.CONFIG_DTC_ES.findResults { it.value.containsKey('dtc') ? it.key : null } )
        } else {
            [:]
        }
    }

    /**
     * Gets for the given object type the configuration about the data to be retrieved from an ElasticSearch index
     * @param key the object type for which the configuration should be retrieved
     * @return the ElasticSeach data configuration map
     */
    static Map<String, Map> getCurrentConfigElasticsearchData(String key) {
        Class config = getCurrentConfigClass(key)

        if (config && config.getDeclaredFields().collect { it.getName() }.contains('CONFIG_DTC_ES')) {
            config.CONFIG_DTC_ES.subMap( config.CONFIG_DTC_ES.findResults { it.value.containsKey('es') ? it.key : null } )
        } else {
            [:]
        }
    }

    /**
     * Gets the complete configuration for the given object
     * @param key the object type key
     * @return the entire config map for the given object
     */
    static Map<String, Object> getCurrentConfig(String key) {
        Class config = getCurrentConfigClass(key)

        if (config && config.getDeclaredFields().collect { it.getName() }.contains('CONFIG')) {
            config.CONFIG
        } else {
            [:]
        }
    }

    /**
     * Gets the current object configuration from the given filter key
     * @param filter the filter key for which the configuration should be retrieved
     * @return the matching object configuration map
     */
    static Map<String, Object> getCurrentConfigByFilter(String filter) {
        Map<String, Object> cfg = [:]
        // println '|--- BaseConfig.getCurrentConfigByFilterAndPrefix( ' + filter + ' )'

        if (filter in [ KEY_COSTITEM, KEY_ISSUEENTITLEMENT, KEY_LICENSE, KEY_ORGANISATION, KEY_PACKAGE, KEY_PLATFORM, KEY_PROVIDER, KEY_SUBSCRIPTION, KEY_VENDOR ]) {
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

    /**
     * Substitution call for {@link #getCustomImplRefdata(java.lang.String, java.lang.Class)}, without configuration class
     * @param key the object type key for which the reference data should be retrieved
     * @return the reference data map containing the labels for the chart
     */
    static Map<String, Object> getCustomImplRefdata(String key) {
        getCustomImplRefdata(key, null)
    }

    /**
     * Retrieves the reference data values in order to display the requested attribute on a chart
     * @param key the object type key for which the reference data should be retrieved
     * @param cfgClass the configuration class (= requested object type); determining the property definition type whose property definitions may be retrieved
     * @return the reference data map containing the labels for the chart
     */
    static Map<String, Object> getCustomImplRefdata(String key, Class cfgClass) {

        ContextService contextService = BeanStore.getContextService()
        MessageSource messageSource = BeanStore.getMessageSource()
        SubscriptionsQueryService subscriptionsQueryService = BeanStore.getSubscriptionsQueryService()

        // println 'BaseConfig.getCustomImplRefdata() -> ' + cfgClass + ' ' + key

        Locale locale = LocaleUtils.getCurrentLocale()
        String ck = 'reporting.customImpl.'

        if (key == CI_GENERIC_CUSTOMER_TYPE) {
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
                            ],  // icon green check circle
                            [   id: 2,
                                value_de: messageSource.getMessage(ck + 'legalInfo.2', null, localeDe),
                                value_en: messageSource.getMessage(ck + 'legalInfo.2', null, localeEn),
                            ],  // icon grey outline circle
                            [   id: 3,
                                value_de: messageSource.getMessage(ck + 'legalInfo.3', null, localeDe),
                                value_en: messageSource.getMessage(ck + 'legalInfo.3', null, localeEn),
                            ]   // icon red question mark
                    ]]
        }
        else if (key == CI_GENERIC_ANNUAL) {
            Long y = Year.now().value // frontend
            return [
                    label: messageSource.getMessage(ck + 'annual.label', null, locale),
                    from: (y+2..y-4).collect{[ id: it, value_de: it, value_en: it] } + [ id: 0 as Long, value_de: 'Alle ohne Ablauf', value_en: 'Open-Ended']
            ]
        }
        else if (key == CI_GENERIC_REFERENCE_YEAR) {
            Long y = Year.now().value // frontend
            return [
                    label: messageSource.getMessage(ck + 'referenceYear.label', null, locale),
                    from: (y+2..y-4).collect{[ id: it, value_de: it, value_en: it] }
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
        else if (key == CI_GENERIC_PACKAGE_PACKAGESTATUS) {
            return [
                    label: messageSource.getMessage('reporting.cfg.query.package.package-packageStatus', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.PACKAGE_STATUS)
            ]
        }
        else if (key == CI_GENERIC_PACKAGE_PROVIDER) {
            return [
                    label: messageSource.getMessage('reporting.cfg.provider', null, locale),
                    from: Provider.executeQuery('select distinct pro from Package pkg join pkg.provider pro').collect{[
                            id: it.id,
                            value_de: it.name,
                            value_en: it.name,
                    ]}.sort({ a, b -> a.value_de.toLowerCase() <=> b.value_de.toLowerCase() })
            ]
        }
        else if (key == CI_GENERIC_PACKAGE_VENDOR) {
            return [
                    label: messageSource.getMessage('vendor', null, locale),
                    from: PackageVendor.executeQuery('select distinct pv.vendor from PackageVendor pv').collect{[
                            id: it.id,
                            value_de: it.name,
                            value_en: it.name,
                    ]}.sort({ a, b -> a.value_de.toLowerCase() <=> b.value_de.toLowerCase() })
            ]
        }
        else if (key == CI_GENERIC_PLATFORM_PROVIDER) {
            return [
//                    label: messageSource.getMessage('reporting.cfg.provider', null, locale),
                    label: messageSource.getMessage('reporting.cfg.platformProvider', null, locale),
                    from: Provider.executeQuery('select distinct pro from Platform plt join plt.provider pro').collect{[
                            id: it.id,
                            value_de: it.name,
                            value_en: it.name,
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
        else if (key == CI_GENERIC_INVOICING_FORMAT) {
            return [
                    label: messageSource.getMessage('vendor.invoicing.formats.label', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.VENDOR_INVOICING_FORMAT)
            ]
        }
        else if (key == CI_GENERIC_INVOICING_DISPATCH) {
            return [
                    label: messageSource.getMessage('vendor.invoicing.dispatch.label', null, locale),
                    from: RefdataCategory.getAllRefdataValues(RDConstants.VENDOR_INVOICING_DISPATCH)
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

            List tmp = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([validOn: null])
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
            List query = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([validOn: null])
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

    /**
     * Gets the reference data labels from the we:kb ElasticSearch API
     * @param key the attribute key for which the labels should be retrieved
     * @return a map containing the label and the reference data values for chart display
     */
    static Map<String, Object> getElasticSearchRefdata(String key) {

        // println 'BaseConfig.getElasticSearchRefdata() ' + key
        MessageSource messageSource = BeanStore.getMessageSource()
        Locale locale = LocaleUtils.getCurrentLocale()

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

    /**
     * Gets the label associated to the given token
     * @param token the token to which the label should be retrieved
     * @return the matching label from the message resource bundle
     */
    static String getLabel(String token) {
        //println 'getConfigLabel(): ' + key
        MessageSource messageSource = BeanStore.getMessageSource()
        messageSource.getMessage(token, null, LocaleUtils.getCurrentLocale())
    }

    /**
     * Gets the config label for the given token
     * @param token the token being queried
     * @return the associated label
     */
    static String getConfigLabel(def token) {
        getLabel('reporting.cfg.' + token)
    }

    /**
     * Gets the filter label for the given key
     * @param key the key being queried
     * @return the associated label
     */
    static String getFilterLabel(String key) {
        getLabel('reporting.cfg.filter.' + key)
    }

    /**
     * Gets the source label for the given key and source
     * @param key the key being queried
     * @param source the source from which the request is coming
     * @return the associated label
     */
    static String getSourceLabel(String key, String source) {
        getLabel('reporting.cfg.source.' + key + '.' + source)
    }

    /**
     * Gets the label for the given key, query and value
     * @param key the field key to be displayed
     * @param qKey the query key specifying the key
     * @param qValues the values set among which a generic value may be displayed
     * @return the associated message key
     */
    static String getQueryLabel(String key, String qKey, List qValues) {
        if (qValues[0].startsWith('generic')) {
            getLabel('reporting.cfg.' + qValues[0])
        } else {
            getLabel('reporting.cfg.query.' + key + '.' + qKey)
        }
    }

    /**
     * Gets the distribution label matching to the given key and distribution
     * @param key the field key to which the distribution should be retrieved
     * @param dist the distribution value for which the label should be displayed
     * @return the associated message key
     */
    static String getDistributionLabel(String key, String dist) {
        getLabel('reporting.cfg.dist.' + key + '.' + dist)
    }

    static String getIcon(String objKey) {
        String icon = Icon.SYM.UNKOWN

             if (objKey == KEY_ISSUEENTITLEMENT){ icon = 'book icon' }
        else if (objKey == KEY_LICENSE)         { icon = Icon.LICENSE }
        else if (objKey == KEY_ORGANISATION)    { icon = Icon.ORG }
        else if (objKey == KEY_PACKAGE)         { icon = Icon.PACKAGE }
        else if (objKey == KEY_PLATFORM)        { icon = Icon.PLATFORM }
        else if (objKey == KEY_PROVIDER)        { icon = Icon.PROVIDER }
        else if (objKey == KEY_SUBSCRIPTION)    { icon = Icon.SUBSCRIPTION }
        else if (objKey == KEY_VENDOR)          { icon = Icon.VENDOR }

        icon
    }
}
