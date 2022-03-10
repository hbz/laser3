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
import de.laser.reporting.export.LocalExportHelper
import de.laser.reporting.export.local.CostItemExport as CostItemExportLocal
import de.laser.reporting.export.local.IssueEntitlementExport as IssueEntitlementExportLocal
import de.laser.reporting.export.local.OrgExport as OrgExportLocal
import de.laser.reporting.export.local.SubscriptionExport as SubscriptionExportLocal
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.myInstitution.OrgExport as OrgExportGlobal
import de.laser.reporting.export.myInstitution.LicenseExport as LicenseExportGlobal
import de.laser.reporting.export.myInstitution.PackageExport
import de.laser.reporting.export.myInstitution.PlatformExport
import de.laser.reporting.export.myInstitution.SubscriptionExport as SubscriptionExportGlobal
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import grails.util.Holders
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

import java.time.Year

abstract class BaseDetailsExport {

    static String FIELD_TYPE_PROPERTY               = BaseConfig.FIELD_TYPE_PROPERTY
    static String FIELD_TYPE_REFDATA                = BaseConfig.FIELD_TYPE_REFDATA
    static String FIELD_TYPE_REFDATA_JOINTABLE      = BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE
    static String FIELD_TYPE_CUSTOM_IMPL            = BaseConfig.FIELD_TYPE_CUSTOM_IMPL
    static String FIELD_TYPE_CUSTOM_IMPL_QDP        = 'customImplementationQDP' // query depending
    static String FIELD_TYPE_COMBINATION            = 'combination' // TODO
    static String FIELD_TYPE_ELASTICSEARCH          = BaseConfig.FIELD_TYPE_ELASTICSEARCH

    static String CSV_VALUE_SEPARATOR   = ';'
    static String CSV_FIELD_SEPARATOR   = ','
    static String CSV_FIELD_QUOTATION   = '"'

    String token                    // cache token

    static List<String> CUSTOM_MULTIPLE_FIELDS = [
            'x-identifier', '@-org-accessPoint', '@-org-contact', '@-org-readerNumber', '@-entitlement-tippIdentifier'
    ]

    void init(String token, Map<String, Object> fields)  {
        this.token = token

        // keeping order ..
        getAllFields().keySet().each { k ->
            if (k in fields.keySet() ) {
                if (k.contains('+')) {
                    String[] parts = k.split('\\+')
                    parts.eachWithIndex { p,i -> if (i>0) { selectedExportFields.put((parts[0] ? parts[0] + '+' : '') + p, 'on') } }
                }
                else {
                    selectedExportFields.put(k, fields.get(k))
                }
            }
        }
        normalizeSelectedMultipleFields( this )
    }

    Map<String, Object> selectedExportFields = [:]

    abstract Map<String, Object> getSelectedFields()

    abstract String getFieldLabel(String fieldName)

    abstract List<Object> getDetailedObject(Object obj, Map<String, Object> fields)

    Map<String, Object> getCurrentConfig(String key) {

        if (key == CostItemExportLocal.KEY) {
            if (ctxConsortium()) {
                CostItemExportLocal.CONFIG_ORG_CONSORTIUM
            }
        }
        else if (key == IssueEntitlementExportLocal.KEY) {
            IssueEntitlementExportLocal.CONFIG_X
        }
        else if (key == LicenseExportGlobal.KEY) {
            if (ctxConsortium()) {
                LicenseExportGlobal.CONFIG_ORG_CONSORTIUM
            } else if (ctxInst()) {
                LicenseExportGlobal.CONFIG_ORG_INST
            }
        }
        else if (key in [OrgExportLocal.KEY, OrgExportGlobal.KEY]) {
            if (isGlobal(this)) {
                OrgExportGlobal.CONFIG_X
            } else if (isLocal(this)) {
                OrgExportLocal.CONFIG_X
            }
        }
        else if (key == PackageExport.KEY) {
            PackageExport.CONFIG_X
        }
        else if (key == PlatformExport.KEY) {
            PlatformExport.CONFIG_X
        }
        else if (key in [SubscriptionExportLocal.KEY, SubscriptionExportGlobal.KEY]) {
            if (isGlobal(this)) {
                if (ctxConsortium()) {
                    SubscriptionExportGlobal.CONFIG_ORG_CONSORTIUM
                } else if (ctxInst()) {
                    SubscriptionExportGlobal.CONFIG_ORG_INST
                }
            }
            else if (isLocal(this)) {
                if (ctxConsortium()) {
                    SubscriptionExportLocal.CONFIG_ORG_CONSORTIUM
                } else if (ctxInst()) {
                    SubscriptionExportLocal.CONFIG_ORG_INST
                }
            }
        }
    }

    Map<String, Object> getAllFields() {

        String cfg, field

        if (isGlobal(this)) {
            cfg   = GlobalExportHelper.getCachedConfigStrategy( token )
            field = GlobalExportHelper.getCachedFieldStrategy( token )
        }
        else if (isLocal(this)) {
            cfg   = LocalExportHelper.getCachedConfigStrategy( token )
            field = LocalExportHelper.getCachedFieldsStrategy( token )
        }

        Map<String, Object> base = getCurrentConfig( KEY ).base as Map

//        println '----------- BaseDetailsExport.getAllFields() ' + KEY
//        println '- base      ' + base
//        println '- cfg       ' + cfg
//        println '- field     ' + field
//        println '- keySet    ' + base.fields.keySet()

        if (! base.fields.keySet().contains(cfg)) {
            cfg = 'default'
        }
        base.fields.get(cfg).findAll {
            (it.value.type != FIELD_TYPE_CUSTOM_IMPL_QDP) || (it.key == field)
        }
    }

    // -----

    static boolean isLocal(Object clazz) {
        clazz.class.package.toString().endsWith('.local')
    }
    static boolean isGlobal(Object clazz) {
        clazz.class.package.toString().endsWith('.myInstitution')
    }
    static boolean ctxConsortium() {
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')
        contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM'
    }
    static boolean ctxInst() {
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')
        contextService.getOrg().getCustomerType() == 'ORG_INST'
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
        return fieldName in CUSTOM_MULTIPLE_FIELDS
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
            [ it.id, it.getI10n('name') ?: GenericHelper.flagUnmatched( it.ns )]
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

    static String getExportLabel(String token) {
        String msg = '[reporting.export.custom.' + token + ']'
        try {
            MessageSource messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
            Locale locale = LocaleContextHolder.getLocale()
            msg = messageSource.getMessage('reporting.export.custom.' + token, null, locale)
        }
        catch (Exception e) {
            println e.getMessage()
        }

        msg
    }
}
