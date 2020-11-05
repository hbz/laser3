package de.laser


import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.helper.AppUtils
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import de.laser.interfaces.CalculatedType
import de.laser.properties.LicenseProperty
import de.laser.properties.OrgProperty
import de.laser.properties.PlatformProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

import java.text.SimpleDateFormat

@Transactional
class PropertyService {

    def genericOIDService
    def contextService

    private List<String> splitQueryFromOrderBy(String sql) {
        String order_by
        int pos = sql.toLowerCase().indexOf("order by")
        if (pos >= 0) {
            order_by = sql.substring(pos-1)
            sql = sql.substring(0, pos-1)
        }
        [sql, order_by]
    }

    Map<String, Object> evalFilterQuery(Map params, String base_qry, String hqlVar, Map base_qry_params) {
        String order_by
        (base_qry, order_by) = splitQueryFromOrderBy(base_qry)

        if (params.filterPropDef) {
            PropertyDefinition pd = genericOIDService.resolveOID(params.filterPropDef)
            base_qry += ' and ( exists ( select gProp from '+hqlVar+'.propertySet as gProp where gProp.type = :propDef and (gProp.tenant = :tenant or (gProp.tenant != :tenant and gProp.isPublic = true) or gProp.tenant is null) '
            base_qry_params.put('propDef', pd)
            base_qry_params.put('tenant', contextService.org)
            if(params.filterProp) {
                if (pd.isRefdataValueType()) {
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
                }
                else if (pd.isIntegerType()) {
                        if (!params.filterProp || params.filterProp.length() < 1) {
                            base_qry += " and gProp.intValue = null ) "
                        } else {
                            base_qry += " and gProp.intValue = :prop ) "
                            base_qry_params.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                        }
                }
                else if (pd.isStringType()) {
                        if (!params.filterProp || params.filterProp.length() < 1) {
                            base_qry += " and gProp.stringValue = null ) "
                        } else {
                            base_qry += " and lower(gProp.stringValue) like lower(:prop) ) "
                            base_qry_params.put('prop', "%${AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type)}%")
                        }
                }
                else if (pd.isBigDecimalType()) {
                        if (!params.filterProp || params.filterProp.length() < 1) {
                            base_qry += " and gProp.decValue = null ) "
                        } else {
                            base_qry += " and gProp.decValue = :prop ) "
                            base_qry_params.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                        }
                }
                else if (pd.isDateType()) {
                        if (!params.filterProp || params.filterProp.length() < 1) {
                            base_qry += " and gProp.dateValue = null ) "
                        } else {
                            base_qry += " and gProp.dateValue = :prop ) "
                            base_qry_params.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                        }
                }
                else if (pd.isURLType()) {
                        if (!params.filterProp || params.filterProp.length() < 1) {
                            base_qry += " and gProp.urlValue = null ) "
                        } else {
                            base_qry += " and genfunc_filter_matcher(gProp.urlValue, :prop) = true ) "
                            base_qry_params.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                        }
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

    //explicit assignal raises a grails warning
    boolean setPropValue(prop, String filterPropValue) {
        prop = (AbstractPropertyWithCalculatedLastUpdated) prop

        if (prop.type.isIntegerType()) {
            prop.intValue = Integer.parseInt(filterPropValue)
        }
        else if (prop.type.isStringType()) {
            prop.stringValue = filterPropValue
        }
        else if (prop.type.isBigDecimalType()) {
            prop.decValue = new BigDecimal(filterPropValue)
        }
        else if (prop.type.isDateType()) {
            SimpleDateFormat sdf = DateUtil.SDF_NoTime
            prop.dateValue = sdf.parse(filterPropValue)
        }
        else if (prop.type.isURLType()) {
            prop.urlValue = filterPropValue.startsWith('http') ? new URL(filterPropValue) : new URL('http://'+filterPropValue)
        }
        else if (prop.type.isRefdataValueType()) {
            prop.refValue = RefdataValue.get(filterPropValue)
        }

        prop.save()
    }

    /**
     * Adding new PrivateProperty for this institution if not existing
     *
     * @param params
     * @return
     */
    List addPrivatePropertyDefinition(GrailsParameterMap params) {
        log.debug("trying to add private property definition for institution: " + params)

        Org tenant = contextService.getOrg()

        RefdataCategory rdc = null

        if (params.refdatacategory) {
            rdc = RefdataCategory.findById( Long.parseLong(params.refdatacategory) )
        }

        Map<String, Object> map = [
                token       : UUID.randomUUID(),
                category    : params.pd_descr,
                type        : params.pd_type,
                rdc         : rdc?.getDesc(),
                multiple    : (params.pd_multiple_occurrence ? true : false),
                mandatory   : (params.pd_mandatory ? true : false),
                i10n        : [
                        name_de: params.pd_name?.trim(),
                        name_en: params.pd_name?.trim(),
                        expl_de: params.pd_expl?.trim(),
                        expl_en: params.pd_expl?.trim()
                ],
                tenant      : tenant.globalUID]

        PropertyDefinition privatePropDef = PropertyDefinition.construct(map)
        if (privatePropDef.save()) {
            return ['message', message(code: 'default.created.message', args:[privatePropDef.descr, privatePropDef.getI10n('name')])]
        }
        else {
            return ['error', message(code: 'default.not.created.message', args:[privatePropDef.descr, privatePropDef.getI10n('name')])]
        }
    }

    List getUsageDetails() {
        List<Long> usedPdList  = []
        Map<String, Object> detailsMap = [:]
        List<Long> multiplePdList = []

        AppUtils.getAllDomainClasses().each { dc ->

            if (dc.shortName.endsWith('Property') && !SurveyProperty.class.name.contains(dc.name)) {

                //log.debug( dc.shortName )
                String query = "SELECT DISTINCT type FROM " + dc.name
                //log.debug(query)

                Set<PropertyDefinition> pds = PropertyDefinition.executeQuery(query)
                //log.debug(pds)
                detailsMap << ["${dc.shortName}": pds.collect{ PropertyDefinition pd -> "${pd.id}:${pd.type}:${pd.descr}"}.sort()]

                // ids of used property definitions
                pds.each{ PropertyDefinition pd ->
                    usedPdList << pd.id
                }

                String query2 = "select p.type.id from ${dc.name} p where p.type.tenant = null or p.type.tenant = :ctx group by p.type.id, p.owner having count(p) > 1"
                multiplePdList.addAll(PropertyDefinition.executeQuery( query2, [ctx: contextService.org] ))
            }
            else if(SurveyResult.class.name.contains(dc.name)) {
                Set<PropertyDefinition> pds = PropertyDefinition.executeQuery('select distinct type from SurveyResult')
                detailsMap << ["${dc.shortName}": pds.collect{ PropertyDefinition pd -> "${pd.id}:${pd.type}:${pd.descr}"}.sort()]
                pds.each { PropertyDefinition pd ->
                    usedPdList << pd.id
                }
                String query2 = "select p.type.id from SurveyResult p where p.type.tenant = null or p.type.tenant = :ctx group by p.type.id, p.owner having count(p) > 1"
                multiplePdList.addAll(PropertyDefinition.executeQuery( query2, [ctx: contextService.org] ))
            }
        }

        [usedPdList.unique().sort(), detailsMap.sort(), multiplePdList]
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

    Map<String,Object> processObjects(obj,Org contextOrg,PropertyDefinition propDef) {
        Map<String,Object> objMap = [id:obj.id,propertySet:obj.propertySet,displayAction:"show"]
        if(obj instanceof Subscription) {
            Subscription s = (Subscription) obj
            objMap.name = s.dropdownNamingConvention(contextOrg)
            switch(s._getCalculatedType()) {
                case CalculatedType.TYPE_PARTICIPATION: objMap.subscriber = s.getSubscriber()
                    break
                case CalculatedType.TYPE_CONSORTIAL:
                case CalculatedType.TYPE_ADMINISTRATIVE:
                    objMap.manageChildren = "propertiesMembers"
                    objMap.manageChildrenParams = [id:s.id,filterPropDef:genericOIDService.getOID(propDef)]
                    break
            }
            objMap.displayController = "subscription"
        }
        else if(obj instanceof License) {
            License l = (License) obj
            objMap.name = l.dropdownNamingConvention()
            objMap.displayController = "license"
        }
        else if(obj instanceof Org) {
            Org o = (Org) obj
            objMap.name = o.name
            objMap.sortname = o.sortname
            objMap.displayController = "org"
        }
        else if(obj instanceof Platform) {
            Platform p = (Platform) obj
            objMap.name = p.name
            objMap.displayController = "platform"
        }
        else if(obj instanceof Person) {
            Person p = (Person) obj
            objMap.name = "${p.title} ${p.last_name}, ${p.first_name}"
            objMap.displayController = "person"
        }
        objMap
    }

    def replacePropertyDefinitions(PropertyDefinition pdFrom, PropertyDefinition pdTo) {

        log.debug("replacing: ${pdFrom} with: ${pdTo}")
        def count = 0

        PropertyDefinition.executeUpdate("update PropertyDefinitionGroupItem set propDef = :pdTo where propDef = :pdFrom", [pdTo: pdTo, pdFrom: pdFrom])

        def implClass = pdFrom.getImplClass()
        def customPropDef = Class.forName(implClass)
        Set customProps = customPropDef.findAllWhere(type: pdFrom)
        customProps.each{ cp ->
            log.debug("exchange type at: ${implClass}(${cp.id}) from: ${pdFrom.id} to: ${pdTo.id}")
            cp.type = pdTo
            cp.save()
            count++
        }
        count
    }

    List<AbstractPropertyWithCalculatedLastUpdated> getOrphanedProperties(Object obj, List<List> sorted) {

        List<AbstractPropertyWithCalculatedLastUpdated> result = []
        List orphanedIds = obj.propertySet.findAll{ it.type.tenant == null }.collect{ it.id }

        sorted.each{ List entry -> orphanedIds.removeAll(entry[1].getCurrentProperties(obj).id)}

        if (! orphanedIds.isEmpty()) {
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
        }

        //log.debug('object             : ' + obj.class.simpleName + ' - ' + obj)
        //log.debug('orphanedIds        : ' + orphanedIds)
        //log.debug('orphaned Properties: ' + result)

        result
    }
}

