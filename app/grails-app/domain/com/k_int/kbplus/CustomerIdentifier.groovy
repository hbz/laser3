package com.k_int.kbplus

import de.laser.helper.RefdataAnnotation

class CustomerIdentifier {

    @RefdataAnnotation(cat = 'CustomerIdentifierType')
    RefdataValue type

    String value

    Org owner
    Platform platform

    static mapping = {
                id column:'cid_id'
           version column:'cid_version'
              type column:'cid_type_rv_fk'
             owner column:'cid_owner_fk'
          platform column:'cid_platform_fk'
             value column:'cid_value'
    }

    static constraints = {
        type     (nullable:false, blank:false)
        owner    (nullable:false, blank:false)
        platform (nullable:false, blank:false)
        value    (nullable:false, blank:true)
    }

    Org getProvider() {

        return platform?.org
    }
}
