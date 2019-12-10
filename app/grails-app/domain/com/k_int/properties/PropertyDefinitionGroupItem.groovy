package com.k_int.properties

import groovy.util.logging.Log4j

@Log4j
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
        propDef         (nullable: false, blank: false, unique: ['propDefGroup'])
        propDefGroup    (nullable: false, blank: false)
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }
}

