package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants

class ElectronicBilling implements Comparable<ElectronicBilling> {

    Vendor vendor

    @RefdataInfo(cat = RDConstants.VENDOR_INVOICING_FORMAT)
    RefdataValue invoicingFormat

    static mapping = {
        id column: 'eb_id'
        version column: 'eb_version'
        vendor column: 'eb_vendor_fk'
        invoicingFormat column: 'eb_invoicing_format_rv_fk'
    }

    @Override
    int compareTo(ElectronicBilling eb) {
        int result = invoicingFormat <=> eb.invoicingFormat
        if(!result)
            result = vendor.sortname <=> eb.vendor.sortname
        if(!result)
            result = vendor.name <=> eb.vendor.name
        result
    }
}
