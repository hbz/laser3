package de.laser.reporting.export.myInstitution

import de.laser.ContextService
import de.laser.Identifier
import de.laser.License
import de.laser.Subscription
import de.laser.helper.BeanStore
import de.laser.helper.RDStore
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseDetails
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib


class LicenseExport extends BaseDetailsExport {

    static String KEY = BaseConfig.KEY_LICENSE

    static Map<String, Object> CONFIG_ORG_CONSORTIUM = [

            base : [
                    meta : [
                            class: License
                    ],
                    fields : [
                            default: [
                                    'globalUID'         : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'reference'         : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'startDate'         : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'endDate'           : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'status'            : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'licenseCategory'   : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    '@-license-subscriptionCount'       : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-license-memberCount'             : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-license-memberSubscriptionCount' : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-identifier'      : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-property'        : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL_QDP ]
                            ]
                    ]
            ]
    ]

    static Map<String, Object> CONFIG_ORG_INST = [

            base : [
                    meta : [
                            class: License
                    ],
                    fields : [
                            default: [
                                    'globalUID'         : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'reference'         : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'startDate'         : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'endDate'           : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'status'            : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'licenseCategory'   : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'x-identifier'      : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-property'        : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL_QDP ]
                            ]
                    ]
            ]
    ]

    LicenseExport (String token, Map<String, Object> fields) {
        init(token, fields)
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

        ApplicationTagLib g = BeanStore.getApplicationTagLib()
        ContextService contextService = BeanStore.getContextService()

        License lic = obj as License
        List content = []

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)?.type

            // --> generic properties
            if (type == BaseDetailsExport.FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( g.createLink( controller: 'license', action: 'show', absolute: true ) + '/' + lic.getProperty(key) + '@' + lic.getProperty(key) )
                }
                else {
                    content.add( getPropertyContent(lic, key, License.getDeclaredField(key).getType()) )
                }
            }
            // --> generic refdata
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA) {
                content.add( getRefdataContent(lic, key) )
            }
            // --> refdata join tables
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA_JOINTABLE) {
                content.add( getJointableRefdataContent(lic, key) )
            }
            // --> custom filter implementation
            else if (type == BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL) {

                if (key == 'x-identifier') {
                    List<Identifier> ids = []

                    if (f.value) {
                        ids = Identifier.executeQuery( "select i from Identifier i where i.value != null and i.value != '' and i.lic = :lic and i.ns.id in (:idnsList)",
                                [lic: lic, idnsList: f.value] )
                    }
                    content.add( ids.collect{ (it.ns.getI10n('name') ?: GenericHelper.flagUnmatched( it.ns.ns )) + ':' + it.value }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ))
                }
                else if (key == '@-license-subscriptionCount') { // TODO: query
//                    int count = License.executeQuery(
//                            'select count(distinct li.destinationSubscription) from Links li where li.sourceLicense = :lic and li.linkType = :linkType',
//                            [lic: lic, linkType: RDStore.LINKTYPE_LICENSE]
//                    )[0]
//                    content.add( count )

                    String counts = Subscription.executeQuery(
                            'select status, count(status) from Links li join li.destinationSubscription sub join sub.status status where li.sourceLicense = :lic and li.linkType = :linkType group by status',
                            [lic: lic, linkType: RDStore.LINKTYPE_LICENSE]
                    ).collect { it[1] + ' ' + it[0].getI10n('value').toLowerCase() }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) ?: '0'

                    content.add( counts )
                }
                else if (key == '@-license-memberCount') {
                    int count = License.executeQuery('select count(l) from License l where l.instanceOf = :parent', [parent: lic])[0]
                    content.add( count )
                }
                else if (key == '@-license-memberSubscriptionCount') {
//                    int count = License.executeQuery('select count( distinct sub ) from Links li join li.destinationSubscription sub where li.sourceLicense in (' +
//                            'select l from License l where l.instanceOf = :parent' +
//                            ') and li.linkType = :linkType',
//                                [parent: lic, linkType: RDStore.LINKTYPE_LICENSE]
//                        )[0]
//                    content.add( count )

                    String counts = License.executeQuery('select status, count(status) from Links li join li.destinationSubscription sub join sub.status status where li.sourceLicense in (' +
                            'select l from License l where l.instanceOf = :parent' +
                            ') and li.linkType = :linkType group by status',
                            [parent: lic, linkType: RDStore.LINKTYPE_LICENSE]
                    ).collect { it[1] + ' ' + it[0].getI10n('value').toLowerCase() }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) ?: '0'

                    content.add( counts )
                }
                else {
                    content.add( '- ' + key + ' not implemented -' )
                }
            }
            // --> custom query depending filter implementation
            else if (type == BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL_QDP) {

                if (key == 'x-property') {
                    Long pdId = GlobalExportHelper.getDetailsCache(token).id as Long

                    List<String> properties = BaseDetails.resolvePropertiesGeneric(lic, pdId, contextService.getOrg())
                    content.add( properties.findAll().join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) ) // removing empty and null values
                }
                else {
                    content.add( '- ' + key + ' not implemented -' )
                }
            }
            else {
                content.add( '- ' + key + ' not implemented -' )
            }
        }

        content
    }
}
