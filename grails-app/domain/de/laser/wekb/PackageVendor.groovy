package de.laser.wekb

class PackageVendor implements Comparable<PackageVendor> {

    Package pkg
    Vendor vendor

    Date dateCreated
    Date lastUpdated

    static mapping = {
        id column: 'pv_id'
        version column: 'pv_version'

        lastUpdated column: 'pv_last_updated'
        dateCreated column: 'pv_date_created'

        pkg column: 'pv_pkg_fk'
        vendor column: 'pv_vendor_fk'
    }

    @Override
    int compareTo(PackageVendor pv) {
        int result = this.vendor <=> pv.vendor
        if(result == 0)
            result = this.pkg.name <=> pv.pkg.name
        if(result == 0)
            result = this.id <=> pv.id
        result
    }
}
