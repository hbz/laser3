package com.k_int.properties

import com.k_int.kbplus.GenericOIDService
import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import de.laser.domain.I10nTranslation
import de.laser.helper.RefdataAnnotation
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient

@Log4j
class PropertyDefinitionGroup {

    String name
    String description
    Org    tenant
    String ownerType // PropertyDefinition.[LIC_PROP, SUB_PROP, ORG_PROP]

    @RefdataAnnotation(cat = 'YN')
    RefdataValue visible // default value: will be overwritten by existing bindings

    static hasMany = [
            items: PropertyDefinitionGroupItem,
            bindings: PropertyDefinitionGroupBinding
    ]
    static mappedBy = [
            items:    'propDefGroup',
            bindings: 'propDefGroup'
    ]

    static mapping = {
        id          column: 'pdg_id'
        version     column: 'pdg_version'
        name        column: 'pdg_name'
        description column: 'pdg_description', type: 'text'
        tenant      column: 'pdg_tenant_fk'
        ownerType   column: 'pdg_owner_type'
        visible     column: 'pdg_visible_rv_fk'

        items    cascade: 'all' // for deleting
        bindings cascade: 'all' // for deleting
    }

    static constraints = {
        name        (nullable: false, blank: false)
        description (nullable: true,  blank: true)
        tenant      (nullable: true, blank: false)
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

    static getAvailableGroups(Org tenant, String ownerType) {
        def result = []
        def global  = findAllWhere( tenant: null, ownerType: ownerType)
        def context = findAllByTenantAndOwnerType(tenant, ownerType)

        result.addAll(global)
        result.addAll(context)

        result
    }

    static refdataFind(params) {
        def result = []

        def genericOIDService = grails.util.Holders.applicationContext.getBean('genericOIDService') as GenericOIDService

        def currentObject = genericOIDService.resolveOID(params.oid)
        def propDefs = currentObject.getPropertyDefinitions()

        def matches = I10nTranslation.refdataFindHelper(
                'com.k_int.properties.PropertyDefinition',
                'name',
                params.q,
                LocaleContextHolder.getLocale()
        )?.collect{ it.id }

        propDefs.each { it ->
            if (it.id in matches) {
                if (params.desc && params.desc != "*") {
                    if (it.getDescr() == params.desc) {
                        result.add([id: "${it.id}", text: "${it.getI10n('name')}"])
                    }
                } else {
                    result.add([id: "${it.id}", text: "${it.getI10n('name')}"])
                }
            }
        }

        result
    }
}

