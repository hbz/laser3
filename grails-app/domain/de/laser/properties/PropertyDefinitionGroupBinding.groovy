package de.laser.properties

import de.laser.License
import de.laser.Org
import de.laser.Subscription

/**
 * This is a configuration class reflecting the visibility setting of a {@link PropertyDefinitionGroup}.
 */
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
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }
}

