package com.k_int.properties

import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.Subscription
import de.laser.helper.RefdataAnnotation
import groovy.util.logging.Log4j

@Log4j
class PropertyDefinitionGroupBinding {

    @RefdataAnnotation(cat = 'YN')
    RefdataValue visible // default value: will overwrite existing groups

    @RefdataAnnotation(cat = 'YN')
    RefdataValue visibleForConsortiaMembers  // Subscriber_Consortial, Licensee_Consortial

    static belongsTo = [
            lic:    License,
            org:    Org,
            sub:    Subscription,
            propDefGroup:   PropertyDefinitionGroup
    ]

    static mapping = {
        id              column: 'pgb_id'
        version         column: 'pgb_version'
        lic             column: 'pgb_lic_fk'
        org             column: 'pgb_org_fk'
        sub             column: 'pgb_sub_fk'
        propDefGroup    column: 'pgb_property_definition_group_fk'
        visible         column: 'pbg_visible_rv_fk'
        visibleForConsortiaMembers column: 'pbg_is_viewable_rv_fk'
    }

    static constraints = {
        lic                         (nullable: true, unique: ['propDefGroup'])
        org                         (nullable: true, unique: ['propDefGroup'])
        sub                         (nullable: true, unique: ['propDefGroup'])
        propDefGroup                (nullable: false, blank: false)
        visible                     (nullable: true)
        visibleForConsortiaMembers  (nullable: true)
    }
}

