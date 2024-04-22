package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants

class ElectronicDeliveryDelayNotification implements Comparable<ElectronicDeliveryDelayNotification> {

    Vendor vendor

    @RefdataInfo(cat = RDConstants.VENDOR_ELECTRONIC_DELIVERY_DELAY)
    RefdataValue delayNotification

    static mapping = {
        id column: 'eddn_id'
        version column: 'eddn_version'
        vendor column: 'eddn_vendor_fk'
        delayNotification column: 'eddn_delay_notification_rv_fk'
    }

    @Override
    int compareTo(ElectronicDeliveryDelayNotification eddn) {
        int result = delayNotification <=> eddn.delayNotification
        if(!result)
            result = vendor.sortname <=> eddn.vendor.sortname
        if(!result)
            result = vendor.name <=> eddn.vendor.name
        result
    }
}
