package de.laser.reporting.export.local

import de.laser.ContextService
import de.laser.Identifier
import de.laser.LinksGenerationService
import de.laser.Org
import de.laser.Provider
import de.laser.Subscription
import de.laser.Vendor
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.reporting.export.LocalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseDetails
import org.grails.plugins.web.taglib.ApplicationTagLib

/**
 * Contains configurations for the local subscription report.
 * Configurations are defined for pro customer institutions and consortia
 */
class SubscriptionExport extends BaseDetailsExport {

    static String KEY = 'subscription'

    static Map<String, Object> CONFIG_ORG_CONSORTIUM_BASIC = [

            base : [
                    meta : [
                            class: Subscription
                    ],
                    fields : [
                            default: [
                                    'globalUID'             : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'name'                  : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'startDate'             : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'endDate'               : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'status'                : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'kind'                  : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'form'                  : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'resource'              : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    '@-subscription-member+sortname+name' : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
                                    '@-subscription-prevNext'             : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-provider+sortname+name'            : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
                                    'x-vendor+sortname+name'              : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
                                    'hasPerpetualAccess'    : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'hasPublishComponent'   : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'isPublicForApi'        : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'x-identifier'          : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-property'            : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL_QDP ],
                            ]
                    ]
            ]
    ]

    static Map<String, Object> CONFIG_ORG_INST = [

            base : [
                    meta : [
                            class: Subscription
                    ],
                    fields : [
                            default: [
                                    'globalUID'             : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'name'                  : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'startDate'             : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'endDate'               : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'status'                : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'kind'                  : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'form'                  : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'resource'              : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    '@-subscription-prevNext'  : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-provider+sortname+name' : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
                                    'x-vendor+sortname+name'   : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
                                    'hasPerpetualAccess'    : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'hasPublishComponent'   : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'isPublicForApi'        : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'x-identifier'          : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-property'            : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL_QDP ]
                            ]
                    ]
            ]
    ]

    /**
     * Constructor call for a new subscription report
     * @param token the token under which the queried data is going to be stored
     * @param fields the {@link Map} with the fields selected for the export
     */
    SubscriptionExport(String token, Map<String, Object> fields) {
        init(token, fields)

        this.token = token

//        // keeping order ..
//        getAllFields().keySet().each { k ->
//            if (k in fields.keySet() ) {
//                selectedExportFields.put(k, fields.get(k))
//            }
//        }
//        normalizeSelectedMultipleFields( this )
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
        LocalExportHelper.getFieldLabel( this, fieldName )
    }

    /**
     * Collects the details of the given subscription and outputs the field values human-readably
     * @param obj the subscription to export
     * @param fields the selected fields which should appear in the report
     * @return a {@link List} with the subscription's human-readable field values
     */
    @Override
    List<Object> getDetailedObject(Object obj, Map<String, Object> fields) {

        ApplicationTagLib g = BeanStore.getApplicationTagLib()
        ContextService contextService = BeanStore.getContextService()
        LinksGenerationService linksGenerationService = BeanStore.getLinksGenerationService()

        Subscription sub = obj as Subscription
        List content = []

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)?.type

            // --> generic properties
            if (type == BaseDetailsExport.FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( g.createLink( controller: 'subscription', action: 'show', absolute: true ) + '/' + sub.getProperty(key) + '@' + sub.getProperty(key) )
                }
                else {
                    content.add( getPropertyContent(sub, key, Subscription.getDeclaredField(key).getType()) )
                }
            }
            // --> generic refdata
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA) {
                content.add( getRefdataContent(sub, key) )
            }
            // --> refdata join tables
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA_JOINTABLE) {
                content.add( getJointableRefdataContent(sub, key) )
            }
            // --> custom filter implementation
            else if (type == BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL) {

                if (key == 'x-identifier') {
                    List<Identifier> ids = []

                    if (f.value) {
                        ids = Identifier.executeQuery( "select i from Identifier i where i.value != null and i.value != '' and i.sub = :sub and i.ns.id in (:idnsList)",
                                [sub: sub, idnsList: f.value] )
                    }
                    content.add( ids.collect{ (it.ns.getI10n('name') ?: GenericHelper.flagUnmatched( it.ns.ns )) + ':' + it.value }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ))
                }
//                else if (key == 'x-provider') {
//                    List<Org> plts = Org.executeQuery('select ro.org from OrgRole ro where ro.sub.id = :id and ro.roleType in (:roleTypes)',
//                            [id: sub.id, roleTypes: [RDStore.OR_PROVIDER]]
//                    )
//                    content.add( plts.collect{ (it.sortname ? it.sortname + ' : ' : '') + it.name }.join( CSV_VALUE_SEPARATOR ))
//                }
//                else if (key == '@-subscription-member') {
//                    List<Org> members = Subscription.executeQuery(
//                            'select distinct oo.org from Subscription sub join sub.orgRelations oo where sub = :sub and oo.roleType in :subscriberRoleTypes',
//                            [sub: sub, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
//                    )
//                    content.add( members.collect{ (it.sortname ? it.sortname + ' : ' : '') + it.name }.join( CSV_VALUE_SEPARATOR ) )
//                }
                else if (key == '@-subscription-prevNext') {

                    Map<String, List> navMap = linksGenerationService.generateNavigation(sub, false)
                    content.add(
                            (navMap.prevLink ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value'))
                            + ' / ' +
                            (navMap.nextLink ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value'))
                    )
                }
            }
            // --> custom query depending filter implementation
            else if (type == BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL_QDP) {

                if (key == 'x-property') {
                    Long pdId = LocalExportHelper.getDetailsCache(token).id as Long

                    List<String> properties = BaseDetails.resolvePropertiesGeneric(sub, pdId, contextService.getOrg())
                    content.add( properties.findAll().join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) ) // removing empty and null values
                }
            }
            // --> combined properties : TODO
            else if (key in ['@-subscription-member+sortname', '@-subscription-member+name']) {
                List<Org> members = Subscription.executeQuery(
                        'select distinct oo.org from Subscription sub join sub.orgRelations oo where sub = :sub and oo.roleType in :subscriberRoleTypes',
                        [sub: sub, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                )
                String prop = key.split('\\+')[1]
                content.add( members.collect{ it.getProperty(prop) ?: '' }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) )
            }
            // --> combined properties : TODO
            else if (key in ['x-provider+sortname', 'x-provider+name']) {
                // todo: SubscriptionPackage -> Package -> Provider ?
                // todo: SubscriptionPackage -> Package -> Platform -> Provider ?
                List<Provider> providers = Provider.executeQuery('select pr.provider from ProviderRole pr where pr.subscription.id = :id order by pr.provider.sortname, pr.provider.name', [id: sub.id])
                String prop = key.split('\\+')[1]
                content.add( providers.collect{ it.getProperty(prop) ?: '' }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ))
            }
            // --> combined properties : TODO
            else if (key in ['x-vendor+sortname', 'x-vendor+name']) {
                // todo: SubscriptionPackage -> Package -> PackageVendor -> Vendor ?
                List<Vendor> vendors = Vendor.executeQuery('select vr.vendor from VendorRole vr where vr.subscription.id = :id order by vr.vendor.sortname, vr.vendor.name', [id: sub.id])
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
