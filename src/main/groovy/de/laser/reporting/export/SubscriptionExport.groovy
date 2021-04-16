package de.laser.reporting.export

import de.laser.Identifier
import de.laser.Subscription
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.myInstitution.GenericHelper
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
                            'hasPerpetualAccess'    : FIELD_TYPE_PROPERTY,
                            'isPublicForApi'        : FIELD_TYPE_PROPERTY,
                            'identifier-assignment' : FIELD_TYPE_CUSTOM_IMPL, // <- no BaseConfig.getCustomRefdata(fieldName)
                            //'property-assignment'   : FIELD_TYPE_CUSTOM_IMPL, // <- no BaseConfig.getCustomRefdata(fieldName)
                    ]
            ]
    ]

    SubscriptionExport (Map<String, Object> fields) {
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
