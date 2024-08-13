package de.laser.wekb

class InvoicingVendor implements Comparable<InvoicingVendor> {

    Provider provider
    Vendor vendor

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            provider: Provider,
            vendor: Vendor
    ]

    static mapping = {
        id column: 'iv_id'
        version column: 'iv_version'
        provider column: 'iv_provider_fk'
        vendor column: 'iv_vendor_fk'
        dateCreated column: 'iv_date_created'
        lastUpdated column: 'iv_last_updated'
    }

    @Override
    int compareTo(InvoicingVendor iv) {
        int result = this.vendor <=> iv.vendor
        if(result == 0)
            result = this.provider <=> iv.provider
        if(result == 0)
            result = this.id <=> iv.id
        result
    }
}
