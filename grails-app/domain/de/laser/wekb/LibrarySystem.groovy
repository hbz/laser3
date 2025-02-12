package de.laser.wekb

import de.laser.RefdataValue
import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants

class LibrarySystem implements Comparable<LibrarySystem> {

    Vendor vendor

    @RefdataInfo(cat = RDConstants.SUPPORTED_LIBRARY_SYSTEM)
    RefdataValue librarySystem

    static mapping = {
        id column: 'ls_id'
        version column: 'ls_version'
        vendor column: 'ls_vendor_fk'
        librarySystem column: 'ls_library_system_rv_fk'
    }

    @Override
    int compareTo(LibrarySystem ls) {
        int result = librarySystem <=> ls.librarySystem
        if(!result && vendor && ls.vendor)
            result = vendor <=> ls.vendor
        result
    }
}
