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
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
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
import de.laser.utils.LocaleUtils
import groovy.util.logging.Slf4j
import org.springframework.context.MessageSource

import java.time.Year

/**
 * Abstract class containing export configurations common for every report such as cache,
 * separators for export and init methods. See the implementing classes for more details
 * on each report
 */
@Slf4j
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

    /**
     * common nested fields
     */
    static List<String> CUSTOM_MULTIPLE_FIELDS = [
            'x-identifier', '@-org-accessPoint', '@-org-contact', '@-org-readerNumber', '@-entitlement-tippIdentifier'
    ]

    /**
     * Initialises a new export with the given set of selected fields
     * @param token the cache token under which the report will be stored
     * @param fields the fields to be included in export
     */
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

    /**
     * Gets the fields requested for the implementing report
     * @return a {@link Map} containing the selected field keys and paths to fetch
     */
    abstract Map<String, Object> getSelectedFields()

    /**
     * Builds the field label appearing in the report export.
     * The methods implementing are taking care of the report context which determines how the
     * label has to appear in the given circumstances
     * @param fieldName the field key to which the label should be generated
     * @return the field label appearing in the export
     */
    abstract String getFieldLabel(String fieldName)

    /**
     * Gets the details of the implementing object fields and assembles them in a list of human-readable entries
     * @param obj the object whose properties should be read off
     * @param fields the fields to be retrieved from the object
     * @return a {@link List} containing the details to be exported from the object
     */
    abstract List<Object> getDetailedObject(Object obj, Map<String, Object> fields)

    /**
     * Determines the configuration for the given report and context customer type
     * @param key the key for which the configuration should be loaded
     * @return the {@link Map} with the configuration settings
     */
    Map<String, Object> getCurrentConfig(String key) {

        if (key == CostItemExportLocal.KEY) {
            if (ctxConsortium()) {
                CostItemExportLocal.CONFIG_ORG_CONSORTIUM_BASIC
            }
            else if (ctxInst()) {
                CostItemExportLocal.CONFIG_ORG_INST
            }
        }
        else if (key == IssueEntitlementExportLocal.KEY) {
            IssueEntitlementExportLocal.CONFIG_X
        }
        else if (key == LicenseExportGlobal.KEY) {
            if (ctxConsortium()) {
                LicenseExportGlobal.CONFIG_ORG_CONSORTIUM_BASIC
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
                    SubscriptionExportGlobal.CONFIG_ORG_CONSORTIUM_BASIC
                } else if (ctxInst()) {
                    SubscriptionExportGlobal.CONFIG_ORG_INST
                }
            }
            else if (isLocal(this)) {
                if (ctxConsortium()) {
                    SubscriptionExportLocal.CONFIG_ORG_CONSORTIUM_BASIC
                } else if (ctxInst()) {
                    SubscriptionExportLocal.CONFIG_ORG_INST
                }
            }
        }
    }

    /**
     * Gets the fields for the cached configuration
     * @return a {@link Map} of fields with their respective types
     */
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

    /**
     * Checks if this report is a local one, checking the class package name as reference
     * @param obj the report class to check
     * @return true if it is located in the local report package, false otherwise
     */
    static boolean isLocal(Object obj) {
        obj.class.package.toString().endsWith('.local')
    }

    /**
     * Checks if this report is a global (= institution-wide) one, checking the class package name as reference
     * @param obj the report class to check
     * @return true if it is located in the global report package, false otherwise
     */
    static boolean isGlobal(Object obj) {
        obj.class.package.toString().endsWith('.myInstitution')
    }

    /**
     * Checks if the context institution is a consortium
     * @return true if the context institution is a consortium customer, false otherwise
     */
    static boolean ctxConsortium() {
        ContextService contextService = BeanStore.getContextService()
        contextService.getOrg().isCustomerType_Consortium()
    }

    /**
     * Checks if the context institution is a PRO customer
     * @return true if the context institution is a pro institution customer, false otherwise
     */
    static boolean ctxInst() {
        ContextService contextService = BeanStore.getContextService()
        contextService.getOrg().isCustomerType_Inst_Pro()
    }

    // -----

    /**
     * Gets the content of the given property in the given object. Boolean types
     * are being mapped to the {@link RefdataValue} of category {@link RDConstants#Y_N}
     * @param obj the object whose value should be read
     * @param field the field to be read
     * @param type the type of object, used to determine whether boolean type or not
     * @return the field content
     */
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

    /**
     * Gets the localised text content of the given reference data field
     * @param obj the object whose value should be read
     * @param field the field to be read
     * @return the localised field content
     */
    static String getRefdataContent(Object obj, String field) {

        String rdv = obj.getProperty(field)?.getI10n('value') ?: ''
        rdv
    }

    /**
     * Gets the localised text contents of the given reference data field list
     * and concatenates all values of the list with the given value separator
     * @param obj the object whose value should be read
     * @param field the list to be read
     * @return the concatenated string containing the list values
     */
    static String getJointableRefdataContent(Object obj, String field) {

        Set refdata = obj.getProperty(field) as Set
        refdata.collect{ it.getI10n('value') }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR )
    }

    // -----

    /**
     * Checks if the given field is among those which can have multiple values
     * @param fieldName the field name to check
     * @return true if the field name is among the multiple fields, false otherwise
     */
    static boolean isFieldMultiple(String fieldName) {
        return fieldName in CUSTOM_MULTIPLE_FIELDS
    }

    /**
     * Normalises the multiple fields in the given export into lists
     * @param export the report to be exported
     */
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

    /**
     * Build the list for a dropdown with the values of the given key.
     * If identifier namespaces should be selected, those namespaces are retrieved based on the given configuration
     * which are for the identifiers for the object type defined in the configuration
     * @param key the field key to which the dropdown should be generated
     * @param cfg the report configuration, used for identifier namespace determination
     * @return a {@link List} of values for the dropdown
     */
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

    /**
     * Builds a list of identifier namespaces of the given object type. The type
     * of object is defined in the report configuration
     * @param cfg the report configuration containing the object type to which suitable identifier namespaces should be proposed in the dropdown
     * @return a {@link List} of matching identifier namespaces
     */
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

    /**
     * Builds a list of access point methods for dropdown selection
     * @return values from the controlled list {@link RDConstants#ACCESS_POINT_TYPE}
     */
    static List getAccessPointMethodsforDropdown() {

        List<RefdataValue> aptList = RefdataCategory.getAllRefdataValues( RDConstants.ACCESS_POINT_TYPE )

        aptList.collect{ it ->
            [ it.id, it.getI10n('value') ]
        }
    }

    /**
     * Builds a list of contact types for dropdown selection
     * @return values from the controlled list {@link RDConstants#REPORTING_CONTACT_TYPE}
     */
    static List getContactOptionsforDropdown() {

        List<RefdataValue> aptList = RefdataCategory.getAllRefdataValues( RDConstants.REPORTING_CONTACT_TYPE )

        aptList.collect{ it ->
            [ it.id, it.getI10n('value') ]
        }
    }

    /**
     * Builds a list of university terms and years for reader number filter dropdown
     * @return a {@link List} of term semesters and due date years
     */
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

    /**
     * Gets the label for the given token
     * @param token the token to which the output label should be generated
     * @return the localised message string
     */
    static String getExportLabel(String token) {
        String msg = '[reporting.export.custom.' + token + ']'
        try {
            MessageSource messageSource = BeanStore.getMessageSource()
            msg = messageSource.getMessage('reporting.export.custom.' + token, null, LocaleUtils.getCurrentLocale())
        }
        catch (Exception e) {
            log.error e.getMessage()
        }

        msg
    }
}
