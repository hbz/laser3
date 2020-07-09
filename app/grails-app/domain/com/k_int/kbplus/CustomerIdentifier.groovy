package com.k_int.kbplus

import de.laser.helper.RefdataAnnotation

class CustomerIdentifier {

    @RefdataAnnotation(cat = 'CustomerIdentifierType')
    RefdataValue type
    String value
    String note

    Org customer        // target org
    Platform platform   // target platform

    Org owner           // owner
    boolean isPublic = false    // true = visible only for owner

    static mapping = {
                id column:'cid_id'
           version column:'cid_version'
              type column:'cid_type_rv_fk'
             value column:'cid_value'
             note  column:'cid_note', type:'text'
          customer column:'cid_customer_fk'
          platform column:'cid_platform_fk'
             owner column:'cid_owner_fk'
          isPublic column:'cid_is_public'
    }

    static constraints = {
        type     (blank:false)
        value    (blank:true)
        note     (nullable:true, blank:true)
        customer (blank:false)
        platform (blank:false)
        owner    (blank:false)
        isPublic (blank:false)
    }

    Org getProvider() {
        return platform?.org
    }
}
