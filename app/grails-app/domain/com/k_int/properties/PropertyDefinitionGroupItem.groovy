package com.k_int.properties

import groovy.util.logging.Log4j

@Log4j
class PropertyDefinitionGroupItem {

    static belongsTo = [
            propDef:        PropertyDefinition,
            propDefGroup:   PropertyDefinitionGroup
    ]

    static mapping = {
        id              column: 'pde_id'
        version         column: 'pde_version'
        propDef         column: 'pde_property_definition_fk'
        propDefGroup    column: 'pde_property_definition_group_fk'
    }

    static constraints = {
        propDef         (nullable: false, blank: false, unique: ['propDefGroup'])
        propDefGroup    (nullable: false, blank: false)
    }
}

