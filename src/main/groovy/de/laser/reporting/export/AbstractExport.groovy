package de.laser.reporting.export

import de.laser.ContextService
import grails.util.Holders

abstract class AbstractExport {

    static String FIELD_TYPE_PROPERTY           = 'property'
    static String FIELD_TYPE_REFDATA            = 'refdata'
    static String FIELD_TYPE_REFDATA_JOINTABLE  = 'refdataJoinTable'
    static String FIELD_TYPE_CUSTOM_IMPL        = 'customImplementation'

    static String FIELD_TYPE_CUSTOM_IMPL_QDP    = 'customImplementationQDP' // query depending

    static String CSV_VALUE_SEPARATOR   = ';'
    static String CSV_FIELD_SEPARATOR   = ','
    static String CSV_FIELD_QUOTATION   = '"'

    static Map<String, String> CUSTOM_LABEL = [

            'globalUID'                 : 'Link (Global UID)',
            'x-identifier'              : 'Identifikatoren',            // XYCfg.CONFIG.base.query2.Verteilung
            'x-provider'                : 'Anbieter',                   // XYCfg.CONFIG.base.query2.Verteilung
            'x-property'                : 'impl @ ExportHelper.getFieldLabel()',
            '___subscription_members'   : 'Anzahl Teilnehmer',          // virtual
            '___license_subscriptions'  : 'Anzahl Lizenzen',            // virtual
            '___license_members'        : 'Anzahl Teilnehmerverträge',  // virtual
            '___org_contact'            : 'Kontaktdaten',               // virtual
            '___org_readerNumber'       : 'Nutzerzahlen (Semester)',    // virtual
    ]

    String token

    Map<String, Object> selectedExportFields = [:]

    Map<String, Object> getCurrentConfig(String key) {
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        if (key == LicenseExport.KEY) {

            if (contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM') {
                LicenseExport.CONFIG_ORG_CONSORTIUM
            }
            else if (contextService.getOrg().getCustomerType() == 'ORG_INST') {
                LicenseExport.CONFIG_ORG_INST
            }
        }
        else if (key == OrgExport.KEY) {

            OrgExport.CONFIG_X
        }
        else if (key == SubscriptionExport.KEY) {

            if (contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM') {
                SubscriptionExport.CONFIG_ORG_CONSORTIUM
            }
            else if (contextService.getOrg().getCustomerType() == 'ORG_INST') {
                SubscriptionExport.CONFIG_ORG_INST
            }
        }
    }

    Map<String, Object> getAllFields() {
        String fkey   = ExportHelper.getCachedQueryFieldKey( token )
        String suffix = ExportHelper.getCachedQuerySuffix( token )

        Map<String, Object> base = getCurrentConfig( KEY ).base as Map

        if (! base.fields.keySet().contains(fkey)) {
            fkey = 'default'
        }
        base.fields.get(fkey).findAll {
            (it.value != FIELD_TYPE_CUSTOM_IMPL_QDP) || (it.key == suffix)
        }
    }

    abstract Map<String, Object> getSelectedFields()

    abstract String getFieldLabel(String fieldName)

    abstract List<String> getObject(Long id, Map<String, Object> fields)
}
