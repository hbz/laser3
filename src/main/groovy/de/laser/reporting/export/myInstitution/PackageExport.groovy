package de.laser.reporting.export.myInstitution

import de.laser.ContextService
import de.laser.Identifier
import de.laser.Package
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseDetails
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib

class PackageExport extends BaseDetailsExport {

    static String KEY = BaseConfig.KEY_PACKAGE

    static Map<String, Object> CONFIG_X = [

            base : [
                    meta : [
                            class: de.laser.Package
                    ],
                    fields : [
                            default: [
                                    'globalUID'             : FIELD_TYPE_PROPERTY,
                                    'name'                  : FIELD_TYPE_PROPERTY,
                            ]
                    ]
            ]
    ]

    PackageExport(String token, Map<String, Object> fields) {
        this.token = token

        // keeping order ..
        getAllFields().keySet().each { k ->
            if (k in fields.keySet() ) {
                selectedExportFields.put(k, fields.get(k))
            }
        }
        normalizeSelectedMultipleFields( this )
    }

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    @Override
    String getFieldLabel(String fieldName) {
        GlobalExportHelper.getFieldLabel( this, fieldName )
    }

    @Override
    List<Object> getDetailedObject(Object obj, Map<String, Object> fields) {

        ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        Package pkg = obj as Package
        List content = []

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)

            // --> generic properties
            if (type == FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( g.createLink( controller: 'package', action: 'show', absolute: true ) + '/' + pkg.getProperty(key) as String )
                }
                else {
                    content.add( getPropertyContent(pkg, key, Package.getDeclaredField(key).getType()))
                }
            }
            // --> generic refdata
            else if (type == FIELD_TYPE_REFDATA) {
                content.add( getRefdataContent(pkg, key) )
            }
            // --> refdata join tables
            else if (type == FIELD_TYPE_REFDATA_JOINTABLE) {
                content.add( getJointableRefdataContent(pkg, key) )
            }
            // --> custom filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL) {

                if (key == 'x-identifier') {
                    List<Identifier> ids = []

                    if (f.value) {
                        ids = Identifier.executeQuery( "select i from Identifier i where i.value != null and i.value != '' and i.pkg = :pkg and i.ns.id in (:idnsList)",
                                [pkg: pkg, idnsList: f.value] )
                    }
                    content.add( ids.collect{ (it.ns.getI10n('name') ?: it.ns.ns + ' *') + ':' + it.value }.join( CSV_VALUE_SEPARATOR ))
                }
                else {
                    content.add( '- not implemented -' )
                }
            }
            // --> custom query depending filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL_QDP) {

                if (key == 'x-property') {
                    Long pdId = GlobalExportHelper.getDetailsCache(token).id as Long

                    List<String> properties = BaseDetails.resolvePropertiesGeneric(pkg, pdId, contextService.getOrg())
                    content.add( properties.findAll().join( CSV_VALUE_SEPARATOR ) ) // removing empty and null values
                }
                else {
                    content.add( '- not implemented -' )
                }
            }
            else {
                content.add( '- not implemented -' )
            }
        }

        content
    }
}
