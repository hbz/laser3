package de.laser.reporting.export.base

import de.laser.ContextService
import de.laser.IdentifierNamespace
import de.laser.IssueEntitlement
import de.laser.License
import de.laser.Org
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.TitleInstancePackagePlatform
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.reporting.export.local.LocalExportHelper
import de.laser.reporting.export.local.CostItemExport as CostItemExportLocal
import de.laser.reporting.export.local.IssueEntitlementExport as IssueEntitlementExportLocal
import de.laser.reporting.export.local.OrgExport as OrgExportLocal
import de.laser.reporting.export.local.SubscriptionExport as SubscriptionExportLocal
import de.laser.reporting.export.myInstitution.GlobalExportHelper
import de.laser.reporting.export.myInstitution.OrgExport as OrgExportGlobal
import de.laser.reporting.export.myInstitution.LicenseExport as LicenseExportGlobal
import de.laser.reporting.export.myInstitution.SubscriptionExport as SubscriptionExportGlobal

import grails.util.Holders
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

import java.time.Year

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

            'x-identifier',                     // dyn.value
            'x-provider',                       // XYCfg.CONFIG.base.query2.Verteilung
            'x-property',                       // QDP; dyn.value

            // virtual; without XY.CONFIG.base.x

            '@-subscription-member',
            '@-subscription-memberCount',
            '@-subscription-prevNext',

            'x-memberSubscriptionProperty',     // QDP; dyn.value

            '@-license-subscriptionCount',
            '@-license-memberCount',
            '@-license-memberSubscriptionCount',

            '@-org-accessPoint',      // dyn.value
            '@-org-contact',          // dyn.value
            '@-org-readerNumber',     // dyn.value

            '@-entitlement-priceItem',
            '@-entitlement-tippName',
            '@-entitlement-tippDeweyDecimalClassification',
            '@-entitlement-tippEditionStatement',
            '@-entitlement-tippFirstAuthor',
            '@-entitlement-tippFirstEditor',
            '@-entitlement-tippHostPlatformURL',
            '@-entitlement-tippIdentifier',            // dyn.value
            '@-entitlement-tippLanguage',
            '@-entitlement-tippOpenAccessX',
            '@-entitlement-tippPackage',
            '@-entitlement-tippPlatform',
            '@-entitlement-tippProvider',
            '@-entitlement-tippPublisherName',
            '@-entitlement-tippSeriesName',
            '@-entitlement-tippSubjectReference',
            '@-entitlement-tippTitleType',

            '@-cost-entitlement',
            '@-cost-entitlementGroup',
            '@-cost-invoice',
            '@-cost-member',
            '@-cost-order',
            '@-cost-package',
            '@-cost-subscription',
            '@-cost-taxKey'
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
            cfg   = GlobalExportHelper.getCachedConfigStrategy( token )
            field = GlobalExportHelper.getCachedFieldStrategy( token )
        }
        else if (pkg.endsWith('.local')) {
            cfg   = LocalExportHelper.getCachedConfigStrategy( token )
            field = LocalExportHelper.getCachedFieldStrategy( token )
        }

        Map<String, Object> base = getCurrentConfig( KEY ).base as Map

        if (! base.fields.keySet().contains(cfg)) {
            cfg = 'default'
        }
        base.fields.get(cfg).findAll {
            (it.value != FIELD_TYPE_CUSTOM_IMPL_QDP) || (it.key == field)
        }
    }

    // -----

    static def getPropertyContent(Object obj, String field, Class type) {

        def content = obj.getProperty(field)

        if (type in [boolean, Boolean]) {
            if (obj.getProperty(field) == true) {
                content = RDStore.YN_YES.getI10n('value')
            }
            else if (obj.getProperty(field) == false) {
                content = RDStore.YN_NO.getI10n('value')
            }
            else {
                content = ''
            }
        }
        content
    }

    static String getRefdataContent(Object obj, String field) {

        String rdv = obj.getProperty(field)?.getI10n('value') ?: ''
        rdv
    }

    static String getJointableRefdataContent(Object obj, String field) {

        Set refdata = obj.getProperty(field) as Set
        refdata.collect{ it.getI10n('value') }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR )
    }

    // -----

    static boolean isFieldMultiple(String fieldName) {

        if (fieldName in [ 'x-identifier', '@-org-accessPoint', '@-org-contact', '@-org-readerNumber', '@-entitlement-tippIdentifier']) {
            return true
        }
        return false
    }

    static void normalizeSelectedMultipleFields(BaseDetailsExport export) {

        export.selectedExportFields.each {it ->
            if ( isFieldMultiple( it.key ) ) {
                if ( it.key == '@-org-readerNumber' ) {
                    export.selectedExportFields[it.key] = it.value instanceof String ? [ it.value ] : it.value.collect { it }
                }
                else {
                    export.selectedExportFields[it.key] = it.value instanceof String ? [Long.parseLong(it.value)] : it.value.collect { Long.parseLong(it) }
                }
            }
        }
    }

    static List getMultipleFieldListForDropdown(String key, Map<String, Object> cfg) {

        if (key == 'x-identifier') {
            getIdentifierNamespacesForDropdown( cfg )
        }
        else if (key == '@-org-accessPoint') {
            getAccessPointMethodsforDropdown()
        }
        else if (key == '@-org-contact') {
            getContactOptionsforDropdown()
        }
        else if (key == '@-org-readerNumber') {
            getReaderNumberSemesterAndDueDatesForDropdown()
        }
        else if (key == '@-entitlement-tippIdentifier') {
            getIdentifierNamespacesForDropdown( cfg )
        }
    }

    static List getIdentifierNamespacesForDropdown(Map<String, Object> cfg) {
        List<IdentifierNamespace> idnsList = []

        if (cfg.base.meta.class == Org) {
            idnsList = IdentifierNamespace.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type', [type: Org.class.name] )
        }
        else if (cfg.base.meta.class == License) {
            idnsList = IdentifierNamespace.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type', [type: License.class.name] )
        }
        else if (cfg.base.meta.class == Subscription) {
            idnsList = IdentifierNamespace.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type', [type: Subscription.class.name] )
        }
        else if (cfg.base.meta.class == IssueEntitlement) {
            idnsList = IdentifierNamespace.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type', [type: TitleInstancePackagePlatform.class.name] )
        }

        idnsList.collect{ it ->
            [ it.id, it.getI10n('name') ?: it.ns + ' *' ]
        }.sort { a,b -> a[1] <=> b[1] }
    }

    static List getAccessPointMethodsforDropdown() {
        List<RefdataValue> aptList = RefdataCategory.getAllRefdataValues( RDConstants.ACCESS_POINT_TYPE )

        aptList.collect{ it ->
            [ it.id, it.getI10n('value') ]
        }
    }
    static List getContactOptionsforDropdown() {
        List<RefdataValue> aptList = RefdataCategory.getAllRefdataValues( RDConstants.REPORTING_CONTACT_TYPE )

        aptList.collect{ it ->
            [ it.id, it.getI10n('value') ]
        }
    }

    static List getReaderNumberSemesterAndDueDatesForDropdown() {
        List<RefdataValue> semList = RefdataCategory.getAllRefdataValuesWithOrder( RDConstants.SEMESTER )

        List result = semList.collect{ it ->
            [ 'sem-' + it.id, it.getI10n('value') ]
        }

        int y = Year.now().value
        result.addAll( (y+2..y-4).collect{[ 'dd-' + it, 'Stichtage für ' + it ]} )

        result
    }

    // -----

    static String getMessage(String token) {
        MessageSource messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = LocaleContextHolder.getLocale()

        // println ' ---> ' + 'reporting.export.base.custom.' + token
        messageSource.getMessage('reporting.export.base.custom.' + token, null, locale)
    }
}
