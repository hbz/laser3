package de.laser.reporting.export

import de.laser.Identifier
import de.laser.License
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib

import java.text.SimpleDateFormat

class LicenseExport extends AbstractExport {

    static String KEY = 'license'

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: License
                    ],
                    fields : [
                            'globalUID'         : FIELD_TYPE_PROPERTY,
                            'reference'         : FIELD_TYPE_PROPERTY,
                            'startDate'         : FIELD_TYPE_PROPERTY,
                            'endDate'           : FIELD_TYPE_PROPERTY,
                            'status'            : FIELD_TYPE_REFDATA,
                            'licenseCategory'   : FIELD_TYPE_REFDATA,
                            'type'              : FIELD_TYPE_REFDATA,
                            'identifier-assignment' : FIELD_TYPE_CUSTOM_IMPL, // <- no BaseConfig.getCustomRefdata(fieldName)
                            //'property-assignment'   : FIELD_TYPE_CUSTOM_IMPL, // <- no BaseConfig.getCustomRefdata(fieldName)
                    ]
            ]
    ]

    LicenseExport (Map<String, Object> fields) {
        selectedExport = getAllFields().findAll{ it.key in fields.keySet() }
    }

    @Override
    Map<String, Object> getAllFields() {
        CONFIG.base.fields
    }

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExport
    }

    @Override
    String getFieldLabel(String fieldName) {
        ExportHelper.getFieldLabel( CONFIG.base, fieldName )
    }

    @Override
    List<String> getObject(Long id, Map<String, Object> fields) {

        ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)

        License lic = License.get(id)
        List<String> content = []

        fields.each{ f ->
            String key = f.key
            String type = f.value

            // --> generic properties
            if (type == FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( g.createLink( controller: 'license', action: 'show', absolute: true ) + '/' + lic.getProperty(key) as String )
                }
                else if (License.getDeclaredField(key).getType() == Date) {
                    if (lic.getProperty(key)) {
                        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                        content.add( sdf.format( lic.getProperty(key) ) as String )
                    }
                    else {
                        content.add( '' )
                    }
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
                    content.add( lic.getProperty(key) as String )
                }
            }
            // --> generic refdata
            else if (type == FIELD_TYPE_REFDATA) {
                String value = lic.getProperty(key)?.getI10n('value')
                content.add( value ?: '')
            }
            // --> refdata join tables
            else if (type == FIELD_TYPE_REFDATA_JOINTABLE) {
                Set refdata = lic.getProperty(key) as Set
                content.add( refdata.collect{ it.getI10n('value') }.join( CSV_VALUE_SEPARATOR ))
            }
            // --> custom filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL) {

                if (key == 'identifier-assignment') {
                    List<Identifier> ids = Identifier.executeQuery(
                            "select i from Identifier i where i.value != null and i.value != '' and i.lic = :lic", [lic: lic]
                    )
                    content.add( ids.collect{ it.ns.ns + ':' + it.value }.join( CSV_VALUE_SEPARATOR ))
                }
                else if (key == 'property-assignment') {
                }
                else {
                    content.add('* ' + FIELD_TYPE_CUSTOM_IMPL)
                }
            }
            else {
                content.add( '- not implemented -' )
            }
        }

        content
    }
}
