package de.laser.reporting.export

import de.laser.ContextService
import de.laser.Identifier
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

            'x-identifier'              : 'Identifikatoren',                    // dyn.value
            'x-provider'                : 'Anbieter',                           // XYCfg.CONFIG.base.query2.Verteilung
            'x-property'                : 'Merkmal',                            // QDP; dyn.value

            // virtual; without XY.CONFIG.base.x

            '@ae-subscription-member'   : 'Anzahl Teilnehmer',
            '@ae-license-subscription'  : 'Anzahl Lizenzen',
            '@ae-license-member'        : 'Anzahl Teilnehmerverträge',
            '@ae-org-accessPoint'       : 'Zugangskonfigurationen (ohne Links)',    // dyn.value
            '@ae-org-contact'           : 'Kontaktdaten',
            '@ae-org-readerNumber'      : 'Nutzerzahlen und Stichtage',             // dyn.value
    ]

    String token

    Map<String, Object> selectedExportFields = [:]

    abstract Map<String, Object> getSelectedFields()

    abstract String getFieldLabel(String fieldName)

    abstract List<String> getObject(Long id, Map<String, Object> fields)

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
        String cfg   = ExportHelper.getCachedConfigStrategy( token )
        String field = ExportHelper.getCachedFieldStrategy( token )

        Map<String, Object> base = getCurrentConfig( KEY ).base as Map

        if (! base.fields.keySet().contains(cfg)) {
            cfg = 'default'
        }
        base.fields.get(cfg).findAll {
            (it.value != FIELD_TYPE_CUSTOM_IMPL_QDP) || (it.key == field)
        }
    }
}
