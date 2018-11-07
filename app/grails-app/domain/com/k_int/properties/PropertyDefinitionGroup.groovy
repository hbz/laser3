package com.k_int.properties

import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class PropertyDefinitionGroup {

    String name
    String description
    Org    tenant
    String ownerType // PropertyDefinition.[LIC_PROP, SUB_PROP, ORG_PROP]

    RefdataValue visible // RefdataCategory 'YN'

    static hasMany = [
            items: PropertyDefinitionGroupItem
    ]
    static mappedBy = [
            items: 'propDefGroup'
    ]

    static mapping = {
        id          column: 'pdg_id'
        version     column: 'pdg_version'
        name        column: 'pdg_name'
        description column: 'pdg_description', type: 'text'
        tenant      column: 'pdg_tenant_fk'
        ownerType   column: 'pdg_owner_type'
        visible     column: 'pdg_visible_rv_fk'

        items cascade: 'all' // for deleting
    }

    static constraints = {
        name        (nullable: false, blank: false)
        description (nullable: true,  blank: true)
        tenant      (nullable: false, blank: false)
        ownerType   (nullable: false, blank: false)
        visible     (nullable: true)
    }

    def getPropertyDefinitions() {

        PropertyDefinition.executeQuery(
            "SELECT pd from PropertyDefinition pd, PropertyDefinitionGroupItem pdgi WHERE pdgi.propDef = pd AND pdgi.propDefGroup = ?",
            [this]
        )
    }

    def getCurrentProperties(def currentObject) {

        def result = []
        def givenIds = getPropertyDefinitions().collect{ it -> it.id }

        currentObject?.customProperties?.each{ cp ->
            if (cp.type.id in givenIds) {
                result << GrailsHibernateUtil.unwrapIfProxy(cp)
            }
        }
        result
    }
}

