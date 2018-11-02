package com.k_int.properties

import com.k_int.kbplus.Org
import groovy.util.logging.Log4j

@Log4j
class PropertyDefinitionGroup {

    String name
    Org tenant
    String ownerType // PropertyDefinition.[LIC_PROP, SUB_PROP, ORG_PROP]

    static mapping = {
        id          column: 'pdg_id'
        version     column: 'pdg_version'
        name        column: 'pdg_name'
        tenant      column: 'pdg_tenant_fk'
        ownerType   column: 'pdg_owner_type'
    }

    static constraints = {
        name        (nullable: false, blank: false)
        tenant      (nullable: false, blank: false)
        ownerType   (nullable: false, blank: false)
    }

    def getPropertyDefinitions() {

        PropertyDefinition.executeQuery(
            "SELECT pd from PropertyDefinition pd, PropertyDefinitionGroupItem pdgi WHERE pdgi.propDef = pd AND pdgi.propDefGroup = ?",
            [this]
        )
    }

    def getProperties(def objectWithProperties) {

        def result = []
        def givenIds = getPropertyDefinitions().collect{ it.id }

        objectWithProperties.customProperties?.each{ cp ->
            //println cp.type.id

            //println givenIds

            if (cp.type.id in givenIds) {
                result << cp
            }
        }

        result
    }
}

