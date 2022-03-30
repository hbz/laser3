package de.laser.properties

import com.k_int.kbplus.GenericOIDService
import de.laser.Org
import de.laser.CacheService
import de.laser.I10nTranslation
import de.laser.helper.BeanStore
import de.laser.helper.EhcacheWrapper
import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.i18n.LocaleContextHolder

/**
 * Properties may be grouped in groups defined by their type ({@link PropertyDefinition}); a group contains may contain n {@link PropertyDefinitionGroupItem}s.
 * The visibility may be configured in the group directly or overridden in {@link PropertyDefinitionGroupBinding}s which makes the visiblity configurable for each owner object
 * ({@link de.laser.License}, {@link de.laser.Subscription}, {@link de.laser.Org}); each configuration is reflected by a binding.
 * The property definition group is created and maintained by the {@link #tenant} organisation; an object which may be accessed by different contexts (orgs) shows the groupings owned by the respective tenant. If a general property
 * is used in groups by multiple tenants, it shows up in the context organisation's group.
 * @see de.laser.base.AbstractPropertyWithCalculatedLastUpdated
 */
@Slf4j
class PropertyDefinitionGroup {

    String name
    String description
    Org    tenant
    String ownerType // PropertyDefinition.[LIC_PROP, SUB_PROP, ORG_PROP]

    boolean isVisible = false // default value: will be overwritten by existing bindings

    Date dateCreated
    Date lastUpdated

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
        description column: 'pdg_description',  type: 'text'
        tenant      column: 'pdg_tenant_fk',    index: 'pdg_tenant_idx'
        ownerType   column: 'pdg_owner_type'
        isVisible   column: 'pdg_is_visible'
        lastUpdated     column: 'pdg_last_updated'
        dateCreated     column: 'pdg_date_created'

        items       cascade: 'all', batchSize: 10
        bindings    cascade: 'all', batchSize: 10
    }

    static constraints = {
        name        (blank: false)
        description (nullable: true,  blank: true)
        tenant      (nullable: true)
        ownerType   (blank: false)
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }

    /**
     * Retrieves the {@link PropertyDefinition} (property types) in this group
     * @return a {@link List} of {@link PropertyDefinition}s
     */
    List<PropertyDefinition> getPropertyDefinitions() {

        PropertyDefinition.executeQuery(
            "SELECT pd from PropertyDefinition pd, PropertyDefinitionGroupItem pdgi WHERE pdgi.propDef = pd AND pdgi.propDefGroup = :pdg",
            [pdg: this]
        )
    }

    /**
     * Retrieves the currently contained properties ({@link de.laser.base.AbstractPropertyWithCalculatedLastUpdated}) of {@link PropertyDefinition} types contained in this group
     * @param currentObject the object whose properties should be queried
     * @return a {@link List} of properties ({@link de.laser.base.AbstractPropertyWithCalculatedLastUpdated}) contained by the given object in this group of {@link PropertyDefinition}s
     */
    List getCurrentProperties(def currentObject) {
        List result = []
        def givenIds = getPropertyDefinitions().collect{ it.id }

        currentObject?.propertySet?.each{ cp ->
            if (cp.type.id in givenIds) {
                result << GrailsHibernateUtil.unwrapIfProxy(cp)
            }
        }
        result
    }

    /**
     * Retrieves the currently contained properties ({@link de.laser.base.AbstractPropertyWithCalculatedLastUpdated}) of {@link PropertyDefinition} types contained in this group, owned by a given {@link Org}
     * @param currentObject the object whose properties should be queried
     * @param tenant the {@link Org} which owns the property (!)
     * @return a {@link List} of properties ({@link de.laser.base.AbstractPropertyWithCalculatedLastUpdated}) contained by the given object in this group of {@link PropertyDefinition}s
     */
    List getCurrentPropertiesOfTenant(def currentObject, Org tenant) {
        List result = []
        def givenIds = getPropertyDefinitions().collect{ it.id } //continue here: wrong number delivered

        currentObject?.propertySet?.each{ cp ->
            if (cp.type.id in givenIds && cp.tenant.id == tenant.id) {
                result << GrailsHibernateUtil.unwrapIfProxy(cp)
            }
        }
        result
    }

    /**
     * Retrieves a list of available property definition groups of a given property owner type and a given {@link Org} plus global property definition groups (no tenant)
     * @param tenant the {@link Org} whose property definition groups should be queried
     * @param ownerType the owner type of the properties in this groups
     * @return a {@link List} of property definition groups, owned by the given {@link Org}
     * @see de.laser.base.AbstractPropertyWithCalculatedLastUpdated
     */
    static List<PropertyDefinitionGroup> getAvailableGroups(Org tenant, String ownerType) {
        List<PropertyDefinitionGroup> result = []
        List<PropertyDefinitionGroup> global  = findAllWhere( tenant: null, ownerType: ownerType)
        List<PropertyDefinitionGroup> context = findAllByTenantAndOwnerType(tenant, ownerType)

        result.addAll(global)
        result.addAll(context)

        result
    }

    /**
     * Searches for {@link PropertyDefinition}s and caches the result. Searched is among the {@link PropertyDefinition}s with owner type of a given object
     * @param params a parameter map containing a search query (q) and a context object (currentObject)
     * @return a {@link List} of {@link Map}s for the property definition selection dropdown
     */
    static def refdataFind(GrailsParameterMap params) {
        List<Map<String, Object>> result = []

        GenericOIDService genericOIDService = BeanStore.getGenericOIDService()
        def currentObject = genericOIDService.resolveOID(params.oid)

        CacheService cacheService = BeanStore.getCacheService()
        EhcacheWrapper cache

        cache = cacheService.getTTL300Cache("PropertyDefinitionGroup/refdataFind/${currentObject.id}")

        if (! cache.get('propDefs')) {
            List<PropertyDefinition> propDefs = currentObject.getPropertyDefinitions()

            List cacheContent = []
            propDefs.each { it ->
                cacheContent.add([id:"${it.id}", en:"${it.name_en}", de:"${it.name_de}"])
            }
            cache.put('propDefs', cacheContent)
        }

        cache.get('propDefs').each { it ->
            switch (I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())) {
                case 'en':
                    if (params.q == '*' || it.en?.toLowerCase()?.contains(params.q?.toLowerCase())) {
                        result.add([id:"${it.id}", text:"${it.en}"])
                    }
                    break
                case 'de':
                    if (params.q == '*' || it.de?.toLowerCase()?.contains(params.q?.toLowerCase())) {
                        result.add([id:"${it.id}", text:"${it.de}"])
                    }
                    break
            }
        }

        result
    }
}

