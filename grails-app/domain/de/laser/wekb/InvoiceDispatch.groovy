package de.laser.wekb

import de.laser.RefdataValue
import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants

class InvoiceDispatch implements Comparable<InvoiceDispatch> {

    Vendor vendor
    Provider provider

    @RefdataInfo(cat = RDConstants.VENDOR_INVOICING_DISPATCH)
    RefdataValue invoiceDispatch

    static mapping = {
        id column: 'idi_id'
        version column: 'idi_version'
        vendor column: 'idi_vendor_fk'
        provider column: 'idi_provider_fk'
        invoiceDispatch column: 'idi_invoice_dispatch_rv_fk'
    }

    static constraints = {
        vendor (nullable: true)
        provider (nullable: true)
    }

    @Override
    int compareTo(InvoiceDispatch idi) {
        int result = invoiceDispatch <=> idi.invoiceDispatch
        if(!result && vendor && idi.vendor)
            result = vendor <=> idi.vendor
        if(!result && provider && idi.provider)
            result = provider <=> idi.provider
        result
    }
}
