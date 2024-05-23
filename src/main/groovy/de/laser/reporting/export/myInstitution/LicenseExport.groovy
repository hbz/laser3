package de.laser.reporting.export.myInstitution

import de.laser.ContextService
import de.laser.Identifier
import de.laser.License
import de.laser.Provider
import de.laser.Subscription
import de.laser.Vendor
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseDetails
import org.grails.plugins.web.taglib.ApplicationTagLib

/**
 * Contains configurations for the institution-wide license report.
 * Configurations are being kept separately for institutions and consortia
 */
class LicenseExport extends BaseDetailsExport {

    static String KEY = BaseConfig.KEY_LICENSE

    static Map<String, Object> CONFIG_ORG_CONSORTIUM_BASIC = [

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
                                    'x-provider+sortname+name' : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
                                    'x-vendor+sortname+name'   : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
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
                                    'x-provider+sortname+name' : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
                                    'x-vendor+sortname+name'   : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
                                    'x-identifier'      : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-property'        : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL_QDP ]
                            ]
                    ]
            ]
    ]

    /**
     * Constructor call for a new license report
     * @param token the token under which the queried data is going to be stored
     * @param fields the {@link Map} with the fields selected for the export
     */
    LicenseExport (String token, Map<String, Object> fields) {
        init(token, fields)
    }

    /**
     * Gets the fields selected for the current report export
     * @return the class field map containing the selected report fields
     */
    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    /**
     * Builds the label for the selected field key
     * @param fieldName the field key to which the export label should be built
     * @return the label which will appear in the report export
     */
    @Override
    String getFieldLabel(String fieldName) {
        GlobalExportHelper.getFieldLabel( this, fieldName )
    }

    /**
     * Collects the details of the given license and outputs the field values human-readably
     * @param obj the license to export
     * @param fields the selected fields which should appear in the report
     * @return a {@link List} with the license's human-readable field values
     */
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
            // --> combined properties : TODO
            else if (key in ['x-provider+sortname', 'x-provider+name']) {
                List<Provider> providers = Provider.executeQuery('select pr.provider from ProviderRole pr where pr.license.id = :id', [id: lic.id])
                String prop = key.split('\\+')[1]
                content.add( providers.collect{ it.getProperty(prop) ?: '' }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ))
            }
            // --> combined properties : TODO
            else if (key in ['x-vendor+sortname', 'x-vendor+name']) {
                List<Vendor> vendors = Vendor.executeQuery('select vr.vendor from VendorRole vr where vr.license.id = :id', [id: lic.id])
                String prop = key.split('\\+')[1]
                content.add( vendors.collect{ it.getProperty(prop) ?: '' }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ))
            }
            else {
                content.add( '- ' + key + ' not implemented -' )
            }
        }

        content
    }
}
