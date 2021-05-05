package de.laser.reporting.export

import de.laser.ContextService
import de.laser.Identifier
import de.laser.License
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.myInstitution.base.BaseDetails
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib

import java.text.SimpleDateFormat

class LicenseExport extends AbstractExport {

    static String KEY = 'license'

    static Map<String, Object> CONFIG_ORG_CONSORTIUM = [

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
                            '___license_subscriptions'  : FIELD_TYPE_CUSTOM_IMPL,   // AbstractExport.CUSTOM_LABEL - virtual
                            '___license_members'        : FIELD_TYPE_CUSTOM_IMPL,   // AbstractExport.CUSTOM_LABEL - virtual
                            'identifier-assignment' : FIELD_TYPE_CUSTOM_IMPL,       // AbstractExport.CUSTOM_LABEL
                            'property-assignment'   : FIELD_TYPE_CUSTOM_IMPL_QDP,   // AbstractExport.CUSTOM_LABEL - qdp
                    ]
            ]
    ]

    static Map<String, Object> CONFIG_ORG_INST = [

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
                            'identifier-assignment' : FIELD_TYPE_CUSTOM_IMPL,       // AbstractExport.CUSTOM_LABEL
                            'property-assignment'   : FIELD_TYPE_CUSTOM_IMPL_QDP,   // AbstractExport.CUSTOM_LABEL - qdp
                    ]
            ]
    ]

    LicenseExport (String token, Map<String, Object> fields) {
        this.token = token
        selectedExportFields = getAllFields().findAll{ it.key in fields.keySet() }
    }

    @Override
    Map<String, Object> getAllFields() {
        String suffix = ExportHelper.getCachedQuerySuffix(token)

        getCurrentConfig( KEY ).base.fields.findAll {
            (it.value != FIELD_TYPE_CUSTOM_IMPL_QDP) || (it.key == suffix)
        }
    }

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    @Override
    String getFieldLabel(String fieldName) {
        ExportHelper.getFieldLabel( token, getCurrentConfig( KEY ).base as Map<String, Object>, fieldName )
    }

    @Override
    List<String> getObject(Long id, Map<String, Object> fields) {

        ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

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
                else if (key == '___license_subscriptions') { // TODO: query
                    Long count = License.executeQuery(
                            'select count(distinct li.destinationSubscription) from Links li where li.sourceLicense = :lic and li.linkType = :linkType',
                            [lic: lic, linkType: RDStore.LINKTYPE_LICENSE]
                    )[0]
                    content.add( count as String )
                }
                else if (key == '___license_members') {
                    Long count = License.executeQuery('select count(l) from License l where l.instanceOf = :parent', [parent: lic])[0]
                    content.add( count as String )
                }
            }
            // --> custom query depending filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL_QDP) {

                if (key == 'property-assignment') {
                    Long pdId = BaseDetails.getDetailsCache(token).id as Long

                    List<String> properties = BaseDetails.resolvePropertiesGeneric(lic, pdId, contextService.getOrg())
                    content.add( properties.findAll().join( CSV_VALUE_SEPARATOR ) ) // removing empty and null values
                }
            }
            else {
                content.add( '- not implemented -' )
            }
        }

        content
    }
}
