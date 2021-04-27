package de.laser.reporting.export

import de.laser.ContextService
import de.laser.Identifier
import de.laser.Org
import de.laser.Subscription
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.properties.SubscriptionProperty
import de.laser.reporting.myInstitution.base.BaseDetails
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib

import java.text.SimpleDateFormat

class SubscriptionExport extends AbstractExport {

    static String KEY = 'subscription'

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: Subscription
                    ],
                    fields : [
                            'globalUID'             : FIELD_TYPE_PROPERTY,
                            'name'                  : FIELD_TYPE_PROPERTY,
                            'startDate'             : FIELD_TYPE_PROPERTY,
                            'endDate'               : FIELD_TYPE_PROPERTY,
                            'status'                : FIELD_TYPE_REFDATA,
                            'kind'                  : FIELD_TYPE_REFDATA,
                            'form'                  : FIELD_TYPE_REFDATA,
                            'resource'              : FIELD_TYPE_REFDATA,
                            '___members'            : FIELD_TYPE_CUSTOM_IMPL,   // virtual
                            'provider-assignment'   : FIELD_TYPE_CUSTOM_IMPL,   // <- no BaseConfig.getCustomRefdata(fieldName)
                            'hasPerpetualAccess'    : FIELD_TYPE_PROPERTY,
                            'isPublicForApi'        : FIELD_TYPE_PROPERTY,
                            'identifier-assignment' : FIELD_TYPE_CUSTOM_IMPL,       // <- no BaseConfig.getCustomRefdata(fieldName)
                            'property-assignment'   : FIELD_TYPE_CUSTOM_IMPL_QDP,   // qdp
                    ]
            ]
    ]

    SubscriptionExport (String token, Map<String, Object> fields) {
        this.token = token
        selectedExportFields = getAllFields().findAll{ it.key in fields.keySet() }
    }

    @Override
    Map<String, Object> getAllFields() {
        String suffix = ExportHelper.getCachedQuerySuffix(token)

        CONFIG.base.fields.findAll {
            (it.value != FIELD_TYPE_CUSTOM_IMPL_QDP) || (it.key == suffix)
        }
    }

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    @Override
    String getFieldLabel(String fieldName) {
        ExportHelper.getFieldLabel( token, CONFIG.base, fieldName )
    }

    @Override
    List<String> getObject(Long id, Map<String, Object> fields) {

        ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        Subscription sub = Subscription.get(id)
        List<String> content = []

        fields.each{ f ->
            String key = f.key
            String type = f.value

            // --> generic properties
            if (type == FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( g.createLink( controller: 'subscription', action: 'show', absolute: true ) + '/' + sub.getProperty(key) as String )
                }
                else if (Subscription.getDeclaredField(key).getType() == Date) {
                    if (sub.getProperty(key)) {
                        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                        content.add( sdf.format( sub.getProperty(key) ) as String )
                    }
                    else {
                        content.add( '' )
                    }
                }
                else if (Subscription.getDeclaredField(key).getType() in [boolean, Boolean]) {
                    if (sub.getProperty(key) == true) {
                        content.add( RDStore.YN_YES.getI10n('value') )
                    }
                    else if (sub.getProperty(key) == false) {
                        content.add( RDStore.YN_NO.getI10n('value') )
                    }
                    else {
                        content.add( '' )
                    }
                }
                else {
                    content.add( sub.getProperty(key) as String)
                }
            }
            // --> generic refdata
            else if (type == FIELD_TYPE_REFDATA) {
                String value = sub.getProperty(key)?.getI10n('value')
                content.add( value ?: '')
            }
            // --> refdata join tables
            else if (type == FIELD_TYPE_REFDATA_JOINTABLE) {
                Set refdata = sub.getProperty(key) as Set
                content.add( refdata.collect{ it.getI10n('value') }.join( CSV_VALUE_SEPARATOR ))
            }
            // --> custom filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL) {

                if (key == 'identifier-assignment') {
                    List<Identifier> ids = Identifier.executeQuery(
                            "select i from Identifier i where i.value != null and i.value != '' and i.sub = :sub", [sub: sub]
                    )
                    content.add( ids.collect{ it.ns.ns + ':' + it.value }.join( CSV_VALUE_SEPARATOR ))
                }
                else if (key == 'provider-assignment') {
                    List<Org> plts = Org.executeQuery('select ro.org from OrgRole ro where ro.sub.id = :id and ro.roleType in (:roleTypes)',
                            [id: sub.id, roleTypes: [RDStore.OR_PROVIDER]]
                    )
                    content.add( plts.collect{ it.name }.join( CSV_VALUE_SEPARATOR ))
                }
                else if (key == '___members') {
                    int members = Subscription.executeQuery('select count(s) from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes',
                            [parent: sub, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                    )[0]
                    content.add( members as String )
                }
            }
            // --> custom query depending filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL_QDP) {

                if (key == 'property-assignment') {
                    Long pdId = BaseDetails.getDetailsCache(token).id as Long

                    List<SubscriptionProperty> properties = SubscriptionProperty.executeQuery(
                            "select sp from SubscriptionProperty sp join sp.type pd where sp.owner = :sub and pd.id = :pdId " +
                                    "and (sp.isPublic = true or sp.tenant = :ctxOrg) and pd.descr like '%Property' ",
                            [sub: sub, pdId: pdId, ctxOrg: contextService.getOrg()]
                    )
                    content.add(
                            properties.collect { sp ->
                                if (sp.getType().isRefdataValueType()) {
                                    sp.getRefValue()?.getI10n('value')
                                } else {
                                    sp.getValue()
                                }
                            }.findAll().join( CSV_VALUE_SEPARATOR ) // removing empty and null values
                    )
                }
            }
            else {
                content.add( '- not implemented -' )
            }
        }

        content
    }
}
