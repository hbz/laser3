package com.k_int.properties

import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import groovy.util.logging.*
import javax.persistence.Transient
import javax.validation.UnexpectedTypeException
import org.apache.commons.logging.LogFactory

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
    
    static getRule(PropertyDefinition propertyDefinition, String propertyOwnerType, Org propertyTenant){
        PrivatePropertyRule.findAllWhere(
            propertyDefinition: propertyDefinition,
            propertyOwnerType: propertyOwnerType,
            propertyTenant: propertyTenant
            )
    }
    static getRule(String propertyOwnerType, Org propertyTenant){
        PrivatePropertyRule.findAllWhere(
            propertyOwnerType: propertyOwnerType,
            propertyTenant: propertyTenant
            )
    }
    
    def getMatchingProperty(privateProperties){
        
        def result = []
        privateProperties.each{ pp ->
            
            if(pp?.type.id == propertyDefinition.id){
                if(pp?.tenant.id == propertyTenant.id){
                    if(pp?.owner.getClass().toString() == propertyOwnerType){
                        result << pp
                    }
                }
            }
            
        }
        result
    } 
}

