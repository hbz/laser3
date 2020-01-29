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
    boolean isPublic    // true = visible only for owner

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
        type     (nullable:false, blank:false)
        value    (nullable:false, blank:true)
        note     (nullable:true, blank:true)
        customer (nullable:false, blank:false)
        platform (nullable:false, blank:false)
        owner    (nullable:false, blank:false)
        isPublic (nullable:false, blank:false)
    }

    Org getProvider() {
        return platform?.org
    }
}
