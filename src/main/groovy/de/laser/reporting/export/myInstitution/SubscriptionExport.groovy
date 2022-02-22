package de.laser.reporting.export.myInstitution

import de.laser.ContextService
import de.laser.Identifier
import de.laser.Org
import de.laser.Subscription
import de.laser.helper.RDStore
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseDetails
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib

class SubscriptionExport extends BaseDetailsExport {

    static String KEY = BaseConfig.KEY_SUBSCRIPTION

    static Map<String, Object> CONFIG_ORG_CONSORTIUM = [

            base : [
                    meta : [
                            class: Subscription
                    ],
                    fields : [
                            default: [
                                    'globalUID'             : [ type: FIELD_TYPE_PROPERTY ],
                                    'name'                  : [ type: FIELD_TYPE_PROPERTY ],
                                    'startDate'             : [ type: FIELD_TYPE_PROPERTY ],
                                    'endDate'               : [ type: FIELD_TYPE_PROPERTY ],
                                    'status'                : [ type: FIELD_TYPE_REFDATA ],
                                    'kind'                  : [ type: FIELD_TYPE_REFDATA ],
                                    'form'                  : [ type: FIELD_TYPE_REFDATA ],
                                    'resource'              : [ type: FIELD_TYPE_REFDATA ],
                                    '@-subscription-memberCount' : [ type: FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-provider+sortname+name'   : [ type: FIELD_TYPE_COMBINATION ],
                                    'hasPerpetualAccess'    : [ type: FIELD_TYPE_PROPERTY ],
                                    'hasPublishComponent'   : [ type: FIELD_TYPE_PROPERTY ],
                                    'isPublicForApi'        : [ type: FIELD_TYPE_PROPERTY ],
                                    'x-identifier'          : [ type: FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-property'            : [ type: FIELD_TYPE_CUSTOM_IMPL_QDP ],
                                    'x-memberSubscriptionProperty' : [ type: FIELD_TYPE_CUSTOM_IMPL_QDP ]
                            ],
                            memberSubscription: [
                                    'globalUID'             : [ type: FIELD_TYPE_PROPERTY ],
                                    'name'                  : [ type: FIELD_TYPE_PROPERTY ],
                                    'startDate'             : [ type: FIELD_TYPE_PROPERTY ],
                                    'endDate'               : [ type: FIELD_TYPE_PROPERTY ],
                                    'status'                : [ type: FIELD_TYPE_REFDATA ],
                                    'kind'                  : [ type: FIELD_TYPE_REFDATA ],
                                    'form'                  : [ type: FIELD_TYPE_REFDATA ],
                                    'resource'              : [ type: FIELD_TYPE_REFDATA ],
                                    'x-provider+sortname+name' : [ type: FIELD_TYPE_COMBINATION ],
                                    'hasPerpetualAccess'    : [ type: FIELD_TYPE_PROPERTY ],
                                    'hasPublishComponent'   : [ type: FIELD_TYPE_PROPERTY ],
                                    'isPublicForApi'        : [ type: FIELD_TYPE_PROPERTY ],
                                    'x-identifier'          : [ type: FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-property'            : [ type: FIELD_TYPE_CUSTOM_IMPL_QDP ],
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
                                    'globalUID'             : [ type: FIELD_TYPE_PROPERTY ],
                                    'name'                  : [ type: FIELD_TYPE_PROPERTY ],
                                    'startDate'             : [ type: FIELD_TYPE_PROPERTY ],
                                    'endDate'               : [ type: FIELD_TYPE_PROPERTY ],
                                    'status'                : [ type: FIELD_TYPE_REFDATA ],
                                    'kind'                  : [ type: FIELD_TYPE_REFDATA ],
                                    'form'                  : [ type: FIELD_TYPE_REFDATA ],
                                    'resource'              : [ type: FIELD_TYPE_REFDATA ],
                                    'x-provider+sortname+name' : [ type: FIELD_TYPE_COMBINATION ],
                                    'hasPerpetualAccess'    : [ type: FIELD_TYPE_PROPERTY ],
                                    'hasPublishComponent'   : [ type: FIELD_TYPE_PROPERTY ],
                                    'isPublicForApi'        : [ type: FIELD_TYPE_PROPERTY ],
                                    'x-identifier'          : [ type: FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                                    'x-property'            : [ type: FIELD_TYPE_CUSTOM_IMPL_QDP ],
                            ]
                    ]
            ]
    ]

    SubscriptionExport (String token, Map<String, Object> fields) {
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

        ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        Subscription sub = obj as Subscription
        List content = []

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)?.type

            // --> generic properties
            if (type == FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( g.createLink( controller: 'subscription', action: 'show', absolute: true ) + '/' + sub.getProperty(key) + '@' + sub.getProperty(key) )
                }
                else {
                    content.add( getPropertyContent(sub, key, Subscription.getDeclaredField(key).getType()) )
                }
            }
            // --> generic refdata
            else if (type == FIELD_TYPE_REFDATA) {
                content.add( getRefdataContent(sub, key) )
            }
            // --> refdata join tables
            else if (type == FIELD_TYPE_REFDATA_JOINTABLE) {
                content.add( getJointableRefdataContent(sub, key) )
            }
            // --> custom filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL) {

                if (key == 'x-identifier') {
                    List<Identifier> ids = []

                    if (f.value) {
                        ids = Identifier.executeQuery( "select i from Identifier i where i.value != null and i.value != '' and i.sub = :sub and i.ns.id in (:idnsList)",
                                [sub: sub, idnsList: f.value] )
                    }
                    content.add( ids.collect{ (it.ns.getI10n('name') ?: GenericHelper.flagUnmatched( it.ns.ns )) + ':' + it.value }.join( CSV_VALUE_SEPARATOR ))
                }
//                else if (key == 'x-provider') {
//                    List<Org> plts = Org.executeQuery('select ro.org from OrgRole ro where ro.sub.id = :id and ro.roleType in (:roleTypes)',
//                            [id: sub.id, roleTypes: [RDStore.OR_PROVIDER]]
//                    )
//                    content.add( plts.collect{ it.name }.join( CSV_VALUE_SEPARATOR ))
//                }
                else if (key == '@-subscription-memberCount') {
                    int members = Subscription.executeQuery('select count(s) from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes',
                            [parent: sub, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                    )[0]
                    content.add( members )
                }
                else {
                    content.add( '- ' + key + ' not implemented -' )
                }
            }
            // --> custom query depending filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL_QDP) {

                if (key == 'x-property') {
                    Long pdId = GlobalExportHelper.getDetailsCache(token).id as Long

                    List<String> properties = BaseDetails.resolvePropertiesGeneric(sub, pdId, contextService.getOrg())
                    content.add( properties.findAll().join( CSV_VALUE_SEPARATOR ) ) // removing empty and null values
                }
                else if (key == 'x-memberSubscriptionProperty') {
                    Long pdId = GlobalExportHelper.getDetailsCache(token).id as Long

                    List<String> properties = BaseDetails.resolvePropertiesGeneric(sub, pdId, contextService.getOrg())
                    content.add( properties.findAll().join( CSV_VALUE_SEPARATOR ) ) // removing empty and null values
                }
                else {
                    content.add( '- ' + key + ' not implemented -' )
                }
            }
            // --> combined properties : TODO
            else if (key in ['x-provider+sortname', 'x-provider+name']) {
                List<Org> plts = Org.executeQuery('select ro.org from OrgRole ro where ro.sub.id = :id and ro.roleType in (:roleTypes)',
                        [id: sub.id, roleTypes: [RDStore.OR_PROVIDER]]
                )
                String prop = key.split('\\+')[1]
                content.add( plts.collect{ it.getProperty(prop) ?: '' }.join( CSV_VALUE_SEPARATOR ))
            }
            else {
                content.add( '- ' + key + ' not implemented -' )
            }
        }

        content
    }
}
