package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.abstract_domain.AbstractPropertyWithCalculatedLastUpdated
import com.k_int.properties.PropertyDefinition
import de.laser.helper.RDStore

class PropertyService {

    def grailsApplication
    def genericOIDService
    def contextService

    private List<String> splitQueryFromOrderBy(String sql) {
        String order_by = null
        int pos = sql.toLowerCase().indexOf("order by")
        if (pos >= 0) {
            order_by = sql.substring(pos-1)
            sql = sql.substring(0, pos-1)
        }
        [sql, order_by]
    }

    Map<String, Object> evalFilterQuery(Map params, String base_qry, String hqlVar, Map base_qry_params) {
        def order_by
        (base_qry, order_by) = splitQueryFromOrderBy(base_qry)



        if (params.filterPropDef) {
            PropertyDefinition pd = genericOIDService.resolveOID(params.filterPropDef)
            base_qry += " and ( exists ( select gProp from ${hqlVar}.propertySet as gProp where gProp.type = :propDef and gProp.tenant = :tenant "
            base_qry_params.put('propDef', pd)
            base_qry_params.put('tenant', contextService.org)
            if(params.filterProp) {
                switch (pd.type) {
                    case RefdataValue.toString():
                        List<String> selFilterProps = params.filterProp.split(',')
                        List filterProp = []
                        selFilterProps.each { String sel ->
                            filterProp << genericOIDService.resolveOID(sel)
                        }
                        base_qry += " and "
                        if (filterProp.contains(RDStore.GENERIC_NULL_VALUE) && filterProp.size() == 1) {
                            base_qry += " gProp.refValue = null "
                            filterProp.remove(RDStore.GENERIC_NULL_VALUE)
                        }
                        else if(filterProp.contains(RDStore.GENERIC_NULL_VALUE) && filterProp.size() > 1) {
                            base_qry += " ( gProp.refValue = null or gProp.refValue in (:prop) ) "
                            filterProp.remove(RDStore.GENERIC_NULL_VALUE)
                            base_qry_params.put('prop', filterProp)
                        }
                        else {
                            base_qry += " gProp.refValue in (:prop) "
                            base_qry_params.put('prop', filterProp)
                        }
                        base_qry += " ) "
                        break
                    case Integer.toString():
                        if (!params.filterProp || params.filterProp.length() < 1) {
                            base_qry += " and gProp.intValue = null ) "
                        } else {
                            base_qry += " and gProp.intValue = :prop ) "
                            base_qry_params.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                        }
                        break
                    case String.toString():
                        if (!params.filterProp || params.filterProp.length() < 1) {
                            base_qry += " and gProp.stringValue = null ) "
                        } else {
                            base_qry += " and lower(gProp.stringValue) like lower(:prop) ) "
                            base_qry_params.put('prop', "%${AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type)}%")
                        }
                        break
                    case BigDecimal.toString():
                        if (!params.filterProp || params.filterProp.length() < 1) {
                            base_qry += " and gProp.decValue = null ) "
                        } else {
                            base_qry += " and gProp.decValue = :prop ) "
                            base_qry_params.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                        }
                        break
                    case Date.toString():
                        if (!params.filterProp || params.filterProp.length() < 1) {
                            base_qry += " and gProp.dateValue = null ) "
                        } else {
                            base_qry += " and gProp.dateValue = :prop ) "
                            base_qry_params.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                        }
                        break
                    case URL.toString():
                        if (!params.filterProp || params.filterProp.length() < 1) {
                            base_qry += " and gProp.urlValue = null ) "
                        } else {
                            base_qry += " and genfunc_filter_matcher(gProp.urlValue, :prop) = true ) "
                            base_qry_params.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                        }
                        break
                }
                base_qry += " ) "
            }
            else {
                base_qry += " ) ) "
            }
        }
        if (order_by) {
            base_qry += order_by
        }
        [query: base_qry, queryParams: base_qry_params]
    }

    def getUsageDetails() {
        def usedPdList  = []
        def detailsMap = [:]

        grailsApplication.getArtefacts("Domain").toList().each { dc ->

            if (dc.shortName.endsWith('CustomProperty') || dc.shortName.endsWith('PrivateProperty')) {

                //log.debug( dc.shortName )
                def query = "SELECT DISTINCT type FROM ${dc.name}"
                //log.debug(query)

                def pds = PropertyDefinition.executeQuery(query)
                //log.debug(pds)
                detailsMap << ["${dc.shortName}": pds.collect{ it -> "${it.id}:${it.type}:${it.descr}"}.sort()]

                // ids of used property definitions
                pds.each{ it ->
                    usedPdList << it.id
                }
            }
        }

        [usedPdList.unique().sort(), detailsMap.sort()]
    }

    Map<String, Object> getRefdataCategoryUsage() {

        Map<String, Object> result = [:]

        List usage = PropertyDefinition.executeQuery(
                "select pd.descr, pd.type, pd.refdataCategory, count(pd.refdataCategory) from PropertyDefinition pd " +
                        "where pd.refdataCategory is not null group by pd.descr, pd.type, pd.refdataCategory " +
                        "order by pd.descr, count(pd.refdataCategory) desc, pd.refdataCategory"
        )

        usage.each { u ->
            if (! result.containsKey(u[0])) {
                result.put(u[0], [])
            }
            result[u[0]].add([u[2], u[3]])
        }

        result
    }

    def replacePropertyDefinitions(PropertyDefinition pdFrom, PropertyDefinition pdTo) {

        log.debug("replacing: ${pdFrom} with: ${pdTo}")
        def count = 0

        PropertyDefinition.executeUpdate(
                "update PropertyDefinitionGroupItem set propDef = :pdTo where propDef = :pdFrom",
                [pdTo: pdTo, pdFrom: pdFrom]
        )

        def implClass = pdFrom.getImplClass('custom')
        def customProps = Class.forName(implClass)?.findAllWhere(
                type: pdFrom
        )
        customProps.each{ cp ->
            log.debug("exchange type at: ${implClass}(${cp.id}) from: ${pdFrom.id} to: ${pdTo.id}")
            cp.type = pdTo
            cp.save(flush:true)
            count++
        }
        count
    }

    List<AbstractPropertyWithCalculatedLastUpdated> getOrphanedProperties(Object obj, List<List> sorted) {

        List<AbstractPropertyWithCalculatedLastUpdated> result = []
        List orphanedIds = obj.propertySet.findAll{ it.type.tenant == null }.collect{ it.id }

        sorted.each{ List entry -> orphanedIds.removeAll(entry[1].getCurrentProperties(obj).id)}

        switch (obj.class.simpleName) {

            case License.class.simpleName:
                result = LicenseProperty.findAllByIdInList(orphanedIds)
                break
            case Subscription.class.simpleName:
                result = SubscriptionProperty.findAllByIdInList(orphanedIds)
                break
            case Org.class.simpleName:
                result = OrgProperty.findAllByIdInList(orphanedIds)
                break
            case Platform.class.simpleName:
                result = PlatformProperty.findAllByIdInList(orphanedIds)
                break
        }

        log.debug('object             : ' + obj.class.simpleName + ' - ' + obj)
        log.debug('orphanedIds        : ' + orphanedIds)
        log.debug('orphaned Properties: ' + result)

        result
    }
}

