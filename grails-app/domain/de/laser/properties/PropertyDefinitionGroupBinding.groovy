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
        id              column: 'pdgb_id'
        version         column: 'pdgb_version'
        lic             column: 'pdgb_lic_fk'
        org             column: 'pdgb_org_fk'
        sub             column: 'pdgb_sub_fk'
        propDefGroup    column: 'pdgb_property_definition_group_fk'
        isVisible       column: 'pdgb_is_visible'
        isVisibleForConsortiaMembers column: 'pdgb_is_visible_for_cons_member'
        lastUpdated     column: 'pdgb_last_updated'
        dateCreated     column: 'pdgb_date_created'
    }

    static constraints = {
        lic                         (nullable: true, unique: ['propDefGroup'])
        org                         (nullable: true, unique: ['propDefGroup'])
        sub                         (nullable: true, unique: ['propDefGroup'])
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }
}

