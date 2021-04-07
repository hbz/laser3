package de.laser.exporting

import de.laser.License
import de.laser.helper.DateUtils
import de.laser.helper.RDStore

class LicenseExport extends GenericExportConfig {

    static String KEY = 'license'

    static Map<String, Object> FIELDS = [

            'endDate'               : GenericExportConfig.FIELD_TYPE_PROPERTY,
            'licenseCategory'       : GenericExportConfig.FIELD_TYPE_REFDATA,
            'startDate'             : GenericExportConfig.FIELD_TYPE_PROPERTY,
            'status'                : GenericExportConfig.FIELD_TYPE_REFDATA,
            'type'                  : GenericExportConfig.FIELD_TYPE_REFDATA
    ]

    Map<String, Object> getCurrentConfig() {

        Map<String, Object> fields = [
                'reference' : GenericExportConfig.FIELD_TYPE_PROPERTY
        ]
        return fields + FIELDS
    }

    List<String> exportLicense(Long id, Map<String, Object> fields) {

        License lic = License.get(id)
        List<String> content = [lic.id as String]

        fields.each{ f ->
            String key = f.key
            String type = f.value

            // --> generic properties
            if (type == GenericExportConfig.FIELD_TYPE_PROPERTY) {

                if (License.getDeclaredField(key).getType() == Date) {
                    content.add( DateUtils.parseDateGeneric( lic.getProperty(key) as String ) as String )
                }
                else if (License.getDeclaredField(key).getType() in [boolean, Boolean]) {
                    if (lic.getProperty(key) == true) {
                        content.add( RDStore.YN_YES.getI10n('value') )
                    }
                    else if (lic.getProperty(key) == false) {
                        content.add( RDStore.YN_NO.getI10n('value') )
                    }
                    else {
                        content.add( '' )
                    }
                }
                else {
                    content.add( lic.getProperty(key) )
                }
            }
            // --> generic refdata
            else if (type == GenericExportConfig.FIELD_TYPE_REFDATA) {
                String value = lic.getProperty(key)?.getI10n('value')
                content.add( value ?: '')
            }
            // --> refdata join tables
            else if (type == GenericExportConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                Set refdata = lic.getProperty(key)
                content.add( refdata.collect{ it.getI10n('value') }.join('; '))
            }
            // --> custom filter implementation
            else if (type == GenericExportConfig.FIELD_TYPE_CUSTOM_IMPL) {

                content.add( '* ' + GenericExportConfig.FIELD_TYPE_CUSTOM_IMPL )
            }
            else {
                content.add( '- not implemented -' )
            }
        }

        content
    }
}
