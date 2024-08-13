package de.laser.wekb

import de.laser.RefdataValue
import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants

class ElectronicBilling implements Comparable<ElectronicBilling> {

    Vendor vendor
    Provider provider

    @RefdataInfo(cat = RDConstants.VENDOR_INVOICING_FORMAT)
    RefdataValue invoicingFormat

    static mapping = {
        id column: 'eb_id'
        version column: 'eb_version'
        vendor column: 'eb_vendor_fk'
        provider column: 'eb_provider_fk'
        invoicingFormat column: 'eb_invoicing_format_rv_fk'
    }

    static constraints = {
        vendor (nullable: true)
        provider (nullable: true)
    }

    @Override
    int compareTo(ElectronicBilling eb) {
        int result = invoicingFormat <=> eb.invoicingFormat
        if(!result && vendor && eb.vendor)
            result = vendor <=> eb.vendor
        if(!result && provider && eb.provider)
            result = provider <=> eb.provider
        result
    }
}
