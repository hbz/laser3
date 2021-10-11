package de.laser.reporting.export.base

import de.laser.ContextService
import de.laser.reporting.export.local.ExportLocalHelper
import de.laser.reporting.export.local.CostItemExport as CostItemExportLocal
import de.laser.reporting.export.local.IssueEntitlementExport as IssueEntitlementExportLocal
import de.laser.reporting.export.local.OrgExport as OrgExportLocal
import de.laser.reporting.export.local.SubscriptionExport as SubscriptionExportLocal
import de.laser.reporting.export.myInstitution.ExportGlobalHelper
import de.laser.reporting.export.myInstitution.OrgExport as OrgExportGlobal
import de.laser.reporting.export.myInstitution.LicenseExport as LicenseExportGlobal
import de.laser.reporting.export.myInstitution.SubscriptionExport as SubscriptionExportGlobal

import grails.util.Holders
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

abstract class BaseDetailsExport {

    static String FIELD_TYPE_PROPERTY           = 'property'
    static String FIELD_TYPE_REFDATA            = 'refdata'
    static String FIELD_TYPE_REFDATA_JOINTABLE  = 'refdataJoinTable'
    static String FIELD_TYPE_CUSTOM_IMPL        = 'customImplementation'

    static String FIELD_TYPE_CUSTOM_IMPL_QDP    = 'customImplementationQDP' // query depending

    static String CSV_VALUE_SEPARATOR   = ';'
    static String CSV_FIELD_SEPARATOR   = ','
    static String CSV_FIELD_QUOTATION   = '"'

    String token                    // cache token

    static List<String> CUSTOM_FIELD_KEY = [

            'globalUID',

            'x-identifier',                    // dyn.value
            'x-provider',                      // XYCfg.CONFIG.base.query2.Verteilung
            'x-property',                      // QDP; dyn.value

            // virtual; without XY.CONFIG.base.x

            '@ae-subscription-member',
            '@ae-subscription-memberCount',
            '@ae-subscription-prevNext',

            '@ae-license-subscriptionCount',
            '@ae-license-memberCount',

            '@ae-org-accessPoint',      // dyn.value
            '@ae-org-contact',          // dyn.value
            '@ae-org-readerNumber',     // dyn.value

            '@ae-entitlement-priceItem',
            '@ae-entitlement-tippName',
            '@ae-entitlement-tippDeweyDecimalClassification',
            '@ae-entitlement-tippEditionStatement',
            '@ae-entitlement-tippFirstAuthor',
            '@ae-entitlement-tippFirstEditor',
            '@ae-entitlement-tippHostPlatformURL',
            '@ae-entitlement-tippIdentifier',            // dyn.value
            '@ae-entitlement-tippLanguage',
            '@ae-entitlement-tippOpenAccessX',
            '@ae-entitlement-tippPackage',
            '@ae-entitlement-tippPlatform',
            '@ae-entitlement-tippProvider',
            '@ae-entitlement-tippPublisherName',
            '@ae-entitlement-tippSeriesName',
            '@ae-entitlement-tippSubjectReference',
            '@ae-entitlement-tippTitleType',

            '@ae-cost-entitlement',
            '@ae-cost-entitlementGroup',
            '@ae-cost-invoice',
            '@ae-cost-member',
            '@ae-cost-order',
            '@ae-cost-package',
            '@ae-cost-subscription',
            '@ae-cost-taxKey'
    ]

    Map<String, Object> selectedExportFields = [:]

    abstract Map<String, Object> getSelectedFields()

    abstract String getFieldLabel(String fieldName)

    abstract List<Object> getDetailedObject(Object obj, Map<String, Object> fields)

    Map<String, Object> getCurrentConfig(String key) {
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        if (key == LicenseExportGlobal.KEY) {

            if (contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM') {
                LicenseExportGlobal.CONFIG_ORG_CONSORTIUM
            }
            else if (contextService.getOrg().getCustomerType() == 'ORG_INST') {
                LicenseExportGlobal.CONFIG_ORG_INST
            }
        }
        else if (key in [OrgExportLocal.KEY, OrgExportGlobal.KEY]) {

            String pkg = this.class.package.toString()

            if (pkg.endsWith('.myInstitution')) {
                OrgExportGlobal.CONFIG_X
            }
            else if (pkg.endsWith('.local')) {
                OrgExportLocal.CONFIG_X
            }
        }
        else if (key in [SubscriptionExportLocal.KEY, SubscriptionExportGlobal.KEY]) {

            String pkg = this.class.package.toString()

            if (pkg.endsWith('.myInstitution')) {
                if (contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM') {
                    SubscriptionExportGlobal.CONFIG_ORG_CONSORTIUM
                }
                else if (contextService.getOrg().getCustomerType() == 'ORG_INST') {
                    SubscriptionExportGlobal.CONFIG_ORG_INST
                }
            }
            else if (pkg.endsWith('.local')) {
                if (contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM') {
                    SubscriptionExportLocal.CONFIG_ORG_CONSORTIUM
                }
                else if (contextService.getOrg().getCustomerType() == 'ORG_INST') {
                    SubscriptionExportLocal.CONFIG_ORG_INST
                }
            }
        }
        else if (key == IssueEntitlementExportLocal.KEY) {

            IssueEntitlementExportLocal.CONFIG_X
        }
        else if (key == CostItemExportLocal.KEY) {

            if (contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM') {
                CostItemExportLocal.CONFIG_ORG_CONSORTIUM
            }
        }
    }

    Map<String, Object> getAllFields() {

        String cfg, field
        String pkg = this.class.package.toString()

        if (pkg.endsWith('.myInstitution')) {
            cfg   = ExportGlobalHelper.getCachedConfigStrategy( token )
            field = ExportGlobalHelper.getCachedFieldStrategy( token )
        }
        else if (pkg.endsWith('.local')) {
            cfg   = ExportLocalHelper.getCachedConfigStrategy( token )
            field = ExportLocalHelper.getCachedFieldStrategy( token )
        }

        Map<String, Object> base = getCurrentConfig( KEY ).base as Map

        if (! base.fields.keySet().contains(cfg)) {
            cfg = 'default'
        }
        base.fields.get(cfg).findAll {
            (it.value != FIELD_TYPE_CUSTOM_IMPL_QDP) || (it.key == field)
        }
    }

    static String getMessage(String token) {
        MessageSource messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = LocaleContextHolder.getLocale()

        // println ' ---> ' + 'reporting.export.base.custom.' + token
        messageSource.getMessage('reporting.export.base.custom.' + token, null, locale)
    }
}
