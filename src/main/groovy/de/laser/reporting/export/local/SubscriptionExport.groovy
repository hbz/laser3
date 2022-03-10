package de.laser.reporting.export.local

import de.laser.ContextService
import de.laser.Identifier
import de.laser.LinksGenerationService
import de.laser.Org
import de.laser.Subscription
import de.laser.helper.RDStore
import de.laser.reporting.export.LocalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseDetails
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib


class SubscriptionExport extends BaseDetailsExport {

    static String KEY = 'subscription'

    static Map<String, Object> CONFIG_ORG_CONSORTIUM = [

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
                                    'hasPerpetualAccess'    : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'hasPublishComponent'   : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'isPublicForApi'        : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'x-identifier'          : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-property'            : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL_QDP ]
                            ]
                    ]
            ]
    ]

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

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    @Override
    String getFieldLabel(String fieldName) {
        LocalExportHelper.getFieldLabel( this, fieldName )
    }

    @Override
    List<Object> getDetailedObject(Object obj, Map<String, Object> fields) {

        ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')
        LinksGenerationService linksGenerationService = (LinksGenerationService) Holders.grailsApplication.mainContext.getBean('linksGenerationService')

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
//                    content.add( plts.collect{ (it.shortname ? it.shortname + ' : ' : '') + it.name }.join( CSV_VALUE_SEPARATOR ))
//                }
//                else if (key == '@-subscription-member') {
//                    List<Org> members = Subscription.executeQuery(
//                            'select distinct oo.org from Subscription sub join sub.orgRelations oo where sub = :sub and oo.roleType in :subscriberRoleTypes',
//                            [sub: sub, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
//                    )
//                    content.add( members.collect{ (it.shortname ? it.shortname + ' : ' : '') + it.name }.join( CSV_VALUE_SEPARATOR ) )
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
                List<Org> plts = Org.executeQuery('select ro.org from OrgRole ro where ro.sub.id = :id and ro.roleType in (:roleTypes)',
                        [id: sub.id, roleTypes: [RDStore.OR_PROVIDER]]
                )
                String prop = key.split('\\+')[1]
                content.add( plts.collect{ it.getProperty(prop) ?: '' }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ))
            }
            else {
                content.add( '- ' + key + ' not implemented -' )
            }
        }

        content
    }
}
