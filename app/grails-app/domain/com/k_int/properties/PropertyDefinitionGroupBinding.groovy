package com.k_int.properties

import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.Subscription
import de.laser.helper.RefdataAnnotation
import groovy.util.logging.Log4j

@Log4j
class PropertyDefinitionGroupBinding {

    boolean isVisible = false // default value: will overwrite existing groups
    boolean isVisibleForConsortiaMembers = false // Subscriber_Consortial, Licensee_Consortial

    Date dateCreated
    Date lastUpdated

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
        isVisible       column: 'pbg_is_visible'
        isVisibleForConsortiaMembers column: 'pbg_is_visible_for_cons_member'
        lastUpdated     column: 'pbg_last_updated'
        dateCreated     column: 'pbg_date_created'
    }

    static constraints = {
        lic                         (nullable: true, unique: ['propDefGroup'])
        org                         (nullable: true, unique: ['propDefGroup'])
        sub                         (nullable: true, unique: ['propDefGroup'])
        propDefGroup                (nullable: false, blank: false)
        isVisible                   (nullable: false, blank: false)
        isVisibleForConsortiaMembers(nullable: false, blank: false)
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }
}

