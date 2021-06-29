package de.laser.reporting.export

import de.laser.*

class IssueEntitlementExport extends AbstractExport {

    static String KEY = 'issueEntitlement'

    static Map<String, Object> CONFIG_X = [

            base : [
                    meta : [
                            class: IssueEntitlement
                    ],
                    fields : [
                            default: [
                                    'globalUID'         : FIELD_TYPE_PROPERTY
                            ]
                    ]
            ]
    ]

    IssueEntitlementExport(String token, Map<String, Object> fields) {
        this.token = token

        // keeping order ..
        getAllFields().keySet().each { k ->
            if (k in fields.keySet() ) {
                selectedExportFields.put(k, fields.get(k))
            }
        }
        ExportLocalHelper.normalizeSelectedMultipleFields( this )
    }

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    @Override
    String getFieldLabel(String fieldName) {
        ExportLocalHelper.getFieldLabel( this, fieldName )
    }

    @Override
    List<String> getObject(Long id, Map<String, Object> fields) {

        ['todo']
    }
}
