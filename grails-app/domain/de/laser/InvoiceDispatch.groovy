package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants

class InvoiceDispatch implements Comparable<InvoiceDispatch> {

    Vendor vendor

    @RefdataInfo(cat = RDConstants.VENDOR_INVOICING_DISPATCH)
    RefdataValue invoiceDispatch

    static mapping = {
        id column: 'idi_id'
        version column: 'idi_version'
        vendor column: 'idi_vendor_fk'
        invoiceDispatch column: 'idi_invoice_dispatch_rv_fk'
    }

    @Override
    int compareTo(InvoiceDispatch idi) {
        int result = invoiceDispatch <=> idi.invoiceDispatch
        if(!result)
            result = vendor.sortname <=> idi.vendor.sortname
        if(!result)
            result = vendor.name <=> idi.vendor.name
        result
    }
}
