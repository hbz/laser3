package de.laser.exporting

import de.laser.Subscription
import de.laser.helper.DateUtils
import de.laser.helper.RDStore

class SubscriptionExport extends GenericExportConfig {

    static String KEY = 'subscription'

    static Map<String, Object> FIELDS = [

            'endDate'               : GenericExportConfig.FIELD_TYPE_PROPERTY,
            'form'                  : GenericExportConfig.FIELD_TYPE_REFDATA,
            'hasPerpetualAccess'    : GenericExportConfig.FIELD_TYPE_PROPERTY,
            'isPublicForApi'        : GenericExportConfig.FIELD_TYPE_PROPERTY,
            'kind'                  : GenericExportConfig.FIELD_TYPE_REFDATA,
            'resource'              : GenericExportConfig.FIELD_TYPE_REFDATA,
            'startDate'             : GenericExportConfig.FIELD_TYPE_PROPERTY,
            'status'                : GenericExportConfig.FIELD_TYPE_REFDATA
    ]

    Map<String, Object> getCurrentConfig() {

        Map<String, Object> fields = [
                'name' : GenericExportConfig.FIELD_TYPE_PROPERTY
        ]
        return fields + FIELDS
    }

    List<String> exportSubscription(Long id, Map<String, Object> fields) {

        Subscription sub = Subscription.get(id)
        List<String> content = [sub.id as String]

        fields.each{ f ->
            String key = f.key
            String type = f.value

            // --> generic properties
            if (type == GenericExportConfig.FIELD_TYPE_PROPERTY) {

                if (Subscription.getDeclaredField(key).getType() == Date) {
                    content.add( DateUtils.parseDateGeneric( sub.getProperty(key) as String ) as String )
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
                    content.add( sub.getProperty(key) )
                }
            }
            // --> generic refdata
            else if (type == GenericExportConfig.FIELD_TYPE_REFDATA) {
                String value = sub.getProperty(key)?.getI10n('value')
                content.add( value ?: '')
            }
            // --> refdata join tables
            else if (type == GenericExportConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                Set refdata = sub.getProperty(key)
                content.add( refdata.collect{ it.getI10n('value') }.join('; '))
            }
            // --> custom filter implementation
            else if (type == GenericExportConfig.FIELD_TYPE_CUSTOM_IMPL) {

                content.add( '* ' + GenericExportConfig.FIELD_TYPE_CUSTOM_IMPL )
            }
            else {
                content.add( '- not implemented -' )
            }
        }

        content
    }
}
