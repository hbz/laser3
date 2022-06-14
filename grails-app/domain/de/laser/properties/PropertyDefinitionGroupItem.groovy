package de.laser.properties

import groovy.util.logging.Slf4j

/**
 * A group entry, showing the membership of the {@link PropertyDefinition} to a {@link PropertyDefinitionGroup}
 */
@Slf4j
class PropertyDefinitionGroupItem {

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            propDef:        PropertyDefinition,
            propDefGroup:   PropertyDefinitionGroup
    ]

    static mapping = {
        id              column: 'pde_id'
        version         column: 'pde_version'
        propDef         column: 'pde_property_definition_fk'
        propDefGroup    column: 'pde_property_definition_group_fk'
        lastUpdated     column: 'pde_last_updated'
        dateCreated     column: 'pde_date_created'
    }

    static constraints = {
        propDef     (unique: ['propDefGroup'])
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }
}

