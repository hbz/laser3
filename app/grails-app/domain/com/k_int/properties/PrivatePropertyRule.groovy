package com.k_int.properties

import com.k_int.kbplus.Org
import groovy.util.logging.*

@Log4j
class PrivatePropertyRule {

    PropertyDefinition  propertyDefinition
    String              propertyOwnerType
    Org                 propertyTenant
    
    static mapping = {
        id                  column:'pr_id'
        version             column:'pr_version'
        propertyDefinition  column:'pr_property_definition_fk'
        propertyOwnerType   column:'pr_property_owner_type'
        propertyTenant      column:'pr_property_tenant_fk'
    }
    
    static constraints = {
        propertyDefinition (nullable:true, blank:false)
        propertyOwnerType  (nullable:true, blank:false)
        propertyTenant     (nullable:true, blank:false)
    }

    static belongsTo = [
        propertyDefinition: PropertyDefinition,
        propertyTenant: Org
    ]

    static getAvailablePropertyDescriptions() {
        return [
                "com.k_int.kbplus.Org"      : PropertyDefinition.ORG_PROP,
                "com.k_int.kbplus.Person"   : PropertyDefinition.PRS_PROP
        ]
    }

    static getRules(String propertyOwnerType, Org propertyTenant){
        PrivatePropertyRule.findAllWhere(
            propertyOwnerType: propertyOwnerType,
            propertyTenant: propertyTenant
            )
    }
}

