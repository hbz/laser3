package com.k_int.kbplus

import de.laser.helper.RefdataAnnotation

class Combo {

    @RefdataAnnotation(cat = 'Combo Status')
    RefdataValue status

    @RefdataAnnotation(cat = 'Combo Type')
    RefdataValue type

    Org fromOrg
    Org toOrg

    static mapping = {
                id column:'combo_id'
           version column:'combo_version'
            status column:'combo_status_rv_fk'
              type column:'combo_type_rv_fk'
           fromOrg column:'combo_from_org_fk'
             toOrg column:'combo_to_org_fk'
    }

    static constraints = {
        status  (nullable:true, blank:false)
        type    (nullable:false, blank:false)
        fromOrg (nullable:false, blank:false)
        toOrg   (nullable:false, blank:false)
    }
}
