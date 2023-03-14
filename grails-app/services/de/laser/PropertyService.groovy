package de.laser


import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.utils.CodeUtils
import de.laser.utils.DateUtils
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.properties.*
import de.laser.survey.SurveyResult
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.context.MessageSource

import java.text.SimpleDateFormat

/**
 * This service handles generic property workflows
 */
@Transactional
class PropertyService {

    AccessService accessService
    ContextService contextService
    GenericOIDService genericOIDService
    MessageSource messageSource

    /**
     * Chops the order clause from the given query
     * @param sql the input query
     * @return the reduced query string
     */
    private List<String> _splitQueryFromOrderBy(String sql) {
        String order_by
        int pos = sql.toLowerCase().indexOf("order by")
        if (pos >= 0) {
            order_by = sql.substring(pos-1)
            sql = sql.substring(0, pos-1)
        }
        [sql, order_by]
    }

    /**
     * Processes filter params and generates query clauses for the given base query; extending the existing query and clauses
     * @param params the filter parameter map
     * @param base_qry the base query, generated by a controller call
     * @param hqlVar the object whose properties should be accessed
     * @param base_qry_params the query parameters for the base query
     * @return the extended base query and parameter map
     */
    Map<String, Object> evalFilterQuery(Map params, String base_qry, String hqlVar, Map base_qry_params) {
        String order_by
        (base_qry, order_by) = _splitQueryFromOrderBy(base_qry)

        if (params.filterPropDef) {
            PropertyDefinition pd = (PropertyDefinition) genericOIDService.resolveOID(params.filterPropDef)
            base_qry += ' and ( exists ( select gProp from '+hqlVar+'.propertySet as gProp where gProp.type = :propDef and (gProp.tenant = :tenant or (gProp.tenant != :tenant and gProp.isPublic = true) or gProp.tenant is null) '
            base_qry_params.put('propDef', pd)
            base_qry_params.put('tenant', contextService.getOrg())
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
                            base_qry += " and gProp.stringValue = :prop ) "
                            base_qry_params.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
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

    /**
     * Sets for the given property the given value
     * Explicit assignment raises a grails warning
     * @param prop the {@link AbstractPropertyWithCalculatedLastUpdated} whose value should be accessed
     * @param filterPropValue the value to be assigned
     * @return true if the assignment was successful, false otherwise
     */
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
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
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
     * Adds new PrivateProperty for the given institution if not existing
     * @param params the request parameter map
     * @return a list containing the process status
     */
    List addPrivatePropertyDefinition(GrailsParameterMap params) {
        log.debug("trying to add private property definition for institution: " + params)
        Locale locale = LocaleUtils.getCurrentLocale()
        Org tenant = contextService.getOrg()

        if ( (params.pd_name && params.pd_descr && params.pd_type) &&
             (params.pd_name != "null" && params.pd_descr != "null" && params.pd_type != "null")
        ) {

            RefdataCategory rdc = null
            if (params.pd_type == RefdataValue.class.name) {
                if (params.refdatacategory) {
                    rdc = RefdataCategory.findById( Long.parseLong(params.refdatacategory) )
                }
                if (! rdc) {
                    return ['error', messageSource.getMessage('propertyDefinition.descr.missing2', null, locale)]
                }
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
            Object[] args = [messageSource.getMessage("propertyDefinition.${privatePropDef.descr}.create.label", null, locale), privatePropDef.getI10n('name')]
            if (privatePropDef.save()) {
                return ['message', messageSource.getMessage('default.created.message', args, locale), params.pd_descr]
            }
            else {
                return ['error', messageSource.getMessage('default.not.created.message', args, locale)]
            }
        }
        else return ['error', messageSource.getMessage('propertyDefinition.descr.missing',null,locale)]
    }

    /**
     * Retrieves the usage details of the property definitions for the context institution
     * @return a map containing usage counts and details for each property definition
     */
    List getUsageDetails() {
        List<Long> usedPdList  = []
        Map<String, Object> detailsMap = [:]
        List<Long> multiplePdList = []

        CodeUtils.getAllDomainArtefacts().each { dc ->

            if (dc.shortName.endsWith('Property')) {

                String query = "SELECT DISTINCT type FROM " + dc.name
                Set<PropertyDefinition> pds = PropertyDefinition.executeQuery(query)

                detailsMap.putAt( dc.shortName, pds.collect{ PropertyDefinition pd -> "${pd.id}:${pd.type}:${pd.descr}"}.sort() )

                pds.each{ PropertyDefinition pd ->
                    usedPdList << pd.id
                }

                String query2 = "select p.type.id from ${dc.name} p where p.type.tenant = null or p.type.tenant = :ctx group by p.type.id, p.owner having count(p) > 1"
                multiplePdList.addAll(PropertyDefinition.executeQuery( query2, [ctx: contextService.getOrg()] ))
            }
            else if(SurveyResult.class.name.contains(dc.name)) {
                Set<PropertyDefinition> pds = PropertyDefinition.executeQuery('select distinct type from SurveyResult')
                detailsMap.putAt( dc.shortName, pds.collect{ PropertyDefinition pd -> "${pd.id}:${pd.type}:${pd.descr}"}.sort() )
                pds.each { PropertyDefinition pd ->
                    usedPdList << pd.id
                }
                String query2 = "select p.type.id from SurveyResult p where p.type.tenant = null or p.type.tenant = :ctx group by p.type.id, p.owner having count(p) > 1"
                multiplePdList.addAll(PropertyDefinition.executeQuery( query2, [ctx: contextService.getOrg()] ))
            }
        }

        [usedPdList.unique().sort(), detailsMap.sort(), multiplePdList]
    }

    /**
     * Retrieves the usages for each controlled list type
     * @return a map containing the usage for each reference category
     */
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

    /**
     * Prepares the property management display; preparing data for links to object lists in the list of property definitions.
     * The link should lead to a filter enumerating objects with the given property definition
     * @param obj the object to which links should be generated
     * @param contextOrg the institution whose perspective is going to be taken
     * @param propDef the property definition for filter preset
     * @return
     */
    Map<String,Object> processObjects(obj,Org contextOrg,PropertyDefinition propDef) {
        Map<String,Object> objMap = [id:obj.id,propertySet:obj.propertySet,displayAction:"show"]
        if(obj instanceof Subscription) {
            Subscription s = (Subscription) obj
            objMap.name = s.dropdownNamingConvention(contextOrg)
            if(contextOrg.getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_PRO']) {
                objMap.manageChildren = "membersSubscriptionsManagement"
                objMap.manageChildrenParams = [id:s.id,propertiesFilterPropDef:genericOIDService.getOID(propDef), tab: 'properties']
            }
            else objMap.subscriber = s.getSubscriber()
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
            String name = ""
            if(p.title)
                name += p.title+' '
            if(p.last_name)
                name += p.last_name
            if(p.first_name)
                name += ', '+p.first_name
            objMap.name = name
            objMap.displayController = "person"
        }
        objMap
    }

    /**
     * Replaces the given property definition by another
     * @param pdFrom the property type to be replaced
     * @param pdTo the replacing property type
     * @param overwrite should existing values be overwritten by the replacement?
     * @param asAdmin is the action done by a global admin changing every occurrence of a public property?
     * @return the count of replacements performed
     */
    int replacePropertyDefinitions(PropertyDefinition pdFrom, PropertyDefinition pdTo, boolean overwrite, boolean asAdmin) {

        log.debug("replacing: ${pdFrom} with: ${pdTo}, overwrite: ${overwrite}")
        int count = 0
        Org contextOrg = contextService.getOrg()
        PropertyDefinition.executeUpdate("update PropertyDefinitionGroupItem set propDef = :pdTo where propDef in (select pdgi.propDef from PropertyDefinitionGroupItem pdgi join pdgi.propDefGroup pdg where pdgi.propDef = :pdFrom and pdg.tenant = :context)", [pdTo: pdTo, pdFrom: pdFrom, context: contextOrg])
        String implClass = pdFrom.getImplClass(), targetImpl = pdTo.getImplClass()
        Class<?> customPropDef = Class.forName(implClass), targetPropDef = Class.forName(targetImpl)
        Set<AbstractPropertyWithCalculatedLastUpdated> customProps = customPropDef.findAllByType(pdFrom)
        Set<AbstractPropertyWithCalculatedLastUpdated> existingTargetProps = targetPropDef.findAllByType(pdTo)
        customProps.each{ AbstractPropertyWithCalculatedLastUpdated cp ->
            //decision tree: is the property to be changed private?
            if(pdFrom.tenant) {
                //assign tenant of property definition to target property
                cp.tenant = pdFrom.tenant
                if(moveProperty(cp, pdTo, existingTargetProps, overwrite)) {
                    log.debug("exchange type at: ${implClass}(${cp.id}) from: ${pdFrom.id} to: ${pdTo.id}")
                    count++
                }
            }
            //no, it is a general property to be moved
            else {
                //is it the tenant moving his property or is an admin changing every property of this type?
                if(cp.tenant.id == contextOrg.id || asAdmin) {
                    if(moveProperty(cp, pdTo, existingTargetProps, overwrite)) {
                        log.debug("exchange type at: ${implClass}(${cp.id}) from: ${pdFrom.id} to: ${pdTo.id}")
                        count++
                    }
                }
            }
        }
        count
    }

    /**
     * Moves the properties of the given type to the given replacement
     * @param cp the property to be moved
     * @param pdTo the replacing property definition
     * @param existingTargetProps properties already existing in the given object of the target type
     * @param overwrite should existing values being overwritten?
     * @return true if the movement was successful, false otherwise
     */
    boolean moveProperty(AbstractPropertyWithCalculatedLastUpdated cp, PropertyDefinition pdTo, Set<AbstractPropertyWithCalculatedLastUpdated> existingTargetProps, boolean overwrite) {
        boolean changed = false
        //can the property exist several times?
        if (pdTo.multipleOccurrence) {
            //change foreign key
            cp.type = pdTo
            changed = true
        }
        //no multiple occurrence: should the old value be retained?
        else {
            AbstractPropertyWithCalculatedLastUpdated existing = existingTargetProps.find { AbstractPropertyWithCalculatedLastUpdated ep -> ep.owner == cp.owner }
            //does a property exist at target?
            if (existing && overwrite) {
                cp.copyInto(existing)
                if (pdTo.descr == PropertyDefinition.LIC_PROP) {
                    existing.paragraph = cp.paragraph
                }
                cp.delete()
                return existing.save()
            }
            //no property to overwrite: move property
            else if(!existing) {
                cp.type = pdTo
                changed = true
            }
        }
        if(changed) {
            return cp.save()
        }
        else return false
    }

    /**
     * Get properties of the given object not belonging to any group
     * @param obj the object whose properties should be retrieved
     * @param sorted properties already belonging to a group
     * @return a list of properties without group
     */
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

    /**
     * Calculates the property definition groups defined by the given institution for the given object
     * @param obj the object whose properties should be retrieved
     * @param contextOrg the institution whose property groups should be retrieved
     * @return a map containing the following property groups: sorted, global, local and orphaned (for properties of type (definition) without group)
     * @see PropertyDefinition
     * @see PropertyDefinitionGroup
     */
    Map<String, Object> getCalculatedPropDefGroups(Object obj, Org contextOrg) {

        obj = GrailsHibernateUtil.unwrapIfProxy(obj)

        boolean isLic = obj.class.name == License.class.name
        boolean isOrg = obj.class.name == Org.class.name
        boolean isPlt = obj.class.name == Platform.class.name
        boolean isSub = obj.class.name == Subscription.class.name

        if ( ! (isLic || isOrg || isPlt || isSub)) {
            log.warn('unsupported call of getCalculatedPropDefGroups(): ' + obj.class)
            return [:]
        }

        Map<String, Object> result = [
                'sorted':[],
                'global':[],
                'local':[],
                'orphanedProperties':[]
        ]

        // ALL type depending groups without checking tenants or bindings
        List<PropertyDefinitionGroup> groups = PropertyDefinitionGroup.findAllByOwnerType(obj.class.name, [sort:'name', order:'asc'])

        if (isOrg || isPlt) {
            groups.each{ PropertyDefinitionGroup it ->

                PropertyDefinitionGroupBinding binding
                if (isOrg) {
                    binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndOrg(it, (Org) obj)
                }
                else {
                    binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndOrg(it, contextOrg)
                }

                if (it.tenant == null || it.tenant?.id == contextOrg.id) {
                    if (binding) {
                        result.local << [it, binding] // TODO: remove
                        result.sorted << ['local', it, binding]
                    } else {
                        result.global << it // TODO: remove
                        result.sorted << ['global', it, null]
                    }
                }
            }
        }

        else if (isLic || isSub) {
            result.member = []

            groups.each{ PropertyDefinitionGroup it ->

                // cons_members
                if (obj.instanceOf) {
                    Org consortium
                    Long objId
                    String query

                    if (isLic) {
                        consortium = obj.getLicensingConsortium()
                        objId = (consortium.id == contextOrg.id) ? obj.instanceOf.id : obj.id
                        query = 'select b from PropertyDefinitionGroupBinding b where b.propDefGroup = :pdg and b.lic.id = :id and b.propDefGroup.tenant = :ctxOrg'
                    }
                    else {
                        consortium = obj.getConsortia()
                        objId = (consortium.id == contextOrg.id) ? obj.instanceOf.id : obj.id
                        query = 'select b from PropertyDefinitionGroupBinding b where b.propDefGroup = :pdg and b.sub.id = :id and b.propDefGroup.tenant = :ctxOrg'
                    }
                    List<PropertyDefinitionGroupBinding> bindings = PropertyDefinitionGroupBinding.executeQuery( query, [pdg:it, id: objId, ctxOrg:contextOrg] )

                    PropertyDefinitionGroupBinding binding = bindings ? (PropertyDefinitionGroupBinding) bindings.get(0) : null

                    // global groups
                    if (it.tenant == null) {
                        if (binding) {
                            result.member << [it, binding] // TODO: remove
                            result.sorted << ['member', it, binding]
                        } else {
                            result.global << it // TODO: remove
                            result.sorted << ['global', it, null]
                        }
                    }
                    // consortium @ member or single user; getting group by tenant (and instanceOf.binding)
                    if (it.tenant?.id == contextOrg.id) {
                        if (binding) {
                            if (consortium.id == contextOrg.id) {
                                result.member << [it, binding] // TODO: remove
                                result.sorted << ['member', it, binding]
                            }
                            else {
                                result.local << [it, binding]
                                result.sorted << ['local', it, binding]
                            }
                        } else {
                            result.global << it // TODO: remove
                            result.sorted << ['global', it, null]
                        }
                    }
                    // licensee consortial; getting group by consortia and instanceOf.binding
                    // subscriber consortial; getting group by consortia and instanceOf.binding
                    else if (it.tenant?.id == consortium.id) {
                        if (binding) {
                            result.member << [it, binding] // TODO: remove
                            result.sorted << ['member', it, binding]
                        }
                    }
                }
                // consortium or locals
                else {
                    PropertyDefinitionGroupBinding binding
                    if (isLic) {
                        binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndLic(it, (License) obj)
                    }
                    else {
                        binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndSub(it, (Subscription) obj)
                    }

                    if (it.tenant == null || it.tenant?.id == contextOrg.id) {
                        if (binding) {
                            result.local << [it, binding] // TODO: remove
                            result.sorted << ['local', it, binding]
                        } else {
                            result.global << it // TODO: remove
                            result.sorted << ['global', it, null]
                        }
                    }
                }
            }
        }

        // storing properties without groups
        result.orphanedProperties = getOrphanedProperties(obj, result.sorted)

        result
    }

     Map<String, Object> getAvailableProperties(PropertyDefinition propDef, Org contextOrg, GrailsParameterMap params) {
         Set filteredObjs = [], objectsWithoutProp = []
         Map<String,Object> parameterMap = [type:propDef,ctx:contextOrg], orgFilterParams = [:], result = [:]
         if(params.objStatus)
             parameterMap.status = RefdataValue.get(params.objStatus)
         String subFilterClause = '', licFilterClause = '', spOwnerFilterClause = '', lpOwnerFilterClause = '', orgFilterClause = ''

         if(accessService.checkPerm('ORG_CONSORTIUM')) {
             subFilterClause += 'and oo.sub.instanceOf = null'
             spOwnerFilterClause += 'and sp.owner.instanceOf = null'
             licFilterClause += 'and oo.lic.instanceOf = null'
             lpOwnerFilterClause += 'and lp.owner.instanceOf = null'
         }
         else if(accessService.checkPerm('ORG_MEMBER_BASIC')) {
             orgFilterClause += 'and ot in (:providerAgency)'
             orgFilterParams.providerAgency = [RDStore.OT_AGENCY, RDStore.OT_PROVIDER, RefdataValue.getByValueAndCategory('Broker', RDConstants.ORG_TYPE), RefdataValue.getByValueAndCategory('Content Provider',RDConstants.ORG_TYPE), RefdataValue.getByValueAndCategory('Vendor',RDConstants.ORG_TYPE)]
         }
         switch(propDef.descr) {
             case PropertyDefinition.SUB_PROP:
                 if(!params.objStatus)
                     parameterMap.status = RDStore.SUBSCRIPTION_CURRENT
                 objectsWithoutProp.addAll(Subscription.executeQuery('select oo.sub from OrgRole oo where oo.org = :ctx '+subFilterClause+' and oo.roleType in (:roleTypes) and not exists (select sp from SubscriptionProperty sp where sp.owner = oo.sub and sp.tenant = :ctx and sp.type = :type) and oo.sub.status = :status order by oo.sub.name asc, oo.sub.startDate asc, oo.sub.endDate asc',parameterMap+[roleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER]]))
                 filteredObjs.addAll(SubscriptionProperty.executeQuery('select sp.owner from SubscriptionProperty sp where sp.type = :type and sp.tenant = :ctx '+spOwnerFilterClause+' and sp.owner.status = :status order by sp.owner.name asc',parameterMap))
                 result.auditable = propDef.tenant == null //blocked until inheritance of private property is cleared
                 result.manageChildren = true
                 break
             case PropertyDefinition.LIC_PROP:
                 if(!params.objStatus)
                     parameterMap.status = RDStore.LICENSE_CURRENT
                 objectsWithoutProp.addAll(License.executeQuery('select oo.lic from OrgRole oo where oo.org = :ctx '+licFilterClause+' and oo.roleType in (:roleTypes) and not exists (select lp from LicenseProperty lp where lp.owner = oo.lic and lp.tenant = :ctx and lp.type = :type) and oo.lic.status = :status order by oo.lic.reference asc, oo.lic.startDate asc, oo.lic.endDate asc',parameterMap+[roleTypes:[RDStore.OR_LICENSING_CONSORTIUM,RDStore.OR_LICENSEE_CONS,RDStore.OR_LICENSEE]]))
                 filteredObjs.addAll(LicenseProperty.executeQuery('select lp.owner from LicenseProperty lp where lp.type = :type and lp.tenant = :ctx '+lpOwnerFilterClause+' and lp.owner.status = :status order by lp.owner.reference asc',parameterMap))
                 result.auditable = propDef.tenant == null //blocked until inheritance of private property is cleared
                 break
             case PropertyDefinition.PRS_PROP: objectsWithoutProp.addAll(Person.executeQuery('select p from Person p where (p.tenant = :ctx or p.tenant = null) and not exists (select pp from PersonProperty pp where pp.owner = p and pp.tenant = :ctx and pp.type = :type) order by p.last_name asc, p.first_name asc',parameterMap))
                 filteredObjs.addAll(PersonProperty.executeQuery('select pp.owner from PersonProperty pp where pp.type = :type and pp.tenant = :ctx order by pp.owner.last_name asc, pp.owner.first_name asc',parameterMap))
                 break
             case PropertyDefinition.ORG_PROP:
                 if(!params.objStatus)
                     parameterMap.status = RDStore.ORG_STATUS_CURRENT
                 String orgfilter = ''
                 String orgfilter2 = ''
                 Map<String,Object> orgFilterMap = [:]
                 if (params.myProviderAgency) {
                     List<Long> myProvidersIds = Org.executeQuery("select distinct(or_pa.org.id) from OrgRole or_pa " +
                             "join or_pa.sub sub join sub.orgRelations or_sub " +
                             "where ( sub = or_sub.sub and or_sub.org = :subOrg ) " +
                             "and ( or_sub.roleType.id in (:subRoleTypes) ) " +
                             "and ( or_pa.roleType.id in (:paRoleTypes) ) ",
                             [subOrg      : contextOrg,
                              subRoleTypes: [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIPTION_CONSORTIA.id],
                              paRoleTypes : [RDStore.OR_PROVIDER.id, RDStore.OR_AGENCY.id]
                             ])
                     orgfilter += 'and o.id in (:myProvidersIds)'
                     orgfilter2 += 'and op.owner.id in (:myProvidersIds)'
                     orgFilterMap.myProvidersIds = myProvidersIds
                 }

                 if (accessService.checkPerm('ORG_CONSORTIUM')) {

                     if (params.myInsts) {
                         List<Long> myInstsIds = Org.executeQuery("select o.id from Org as o, Combo as c where c.fromOrg = o and c.toOrg = :context and c.type.id = :comboType " +
                                 "and exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org.id = o.id " +
                                 "and oo.roleType.id in (:subscrRoles) and ooCons.org = :context and ooCons.roleType.id = :consType " +
                                 "and sub.status.id = :subStatus ) ",
                                 [context    : contextOrg,
                                  comboType  : RDStore.COMBO_TYPE_CONSORTIUM.id,
                                  subscrRoles: [RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id],
                                  consType   : RDStore.OR_SUBSCRIPTION_CONSORTIA.id,
                                  subStatus  : RDStore.SUBSCRIPTION_CURRENT.id
                                 ])
                         orgfilter += 'and o.id in (:myInstsIds)'
                         orgfilter2 += 'and op.owner.id in (:myInstsIds)'
                         orgFilterMap.myInstsIds = myInstsIds
                     }
                 }

                 if(orgfilter != ''){
                     objectsWithoutProp.addAll(Org.executeQuery('select o from Org o join o.orgType ot where o.status != :deleted and not exists (select op from OrgProperty op where op.owner = o and op.tenant = :ctx and op.type = :type) ' + orgFilterClause + ' and o.status = :status  ' + orgfilter + ' order by o.sortname asc, o.name asc', parameterMap + orgFilterParams + orgFilterMap + [deleted: RDStore.ORG_STATUS_DELETED]))
                     filteredObjs.addAll(OrgProperty.executeQuery('select op.owner from OrgProperty op where op.type = :type and op.tenant = :ctx and op.owner.status = :status ' + orgfilter2 + ' order by op.owner.sortname asc, op.owner.name asc', parameterMap + orgFilterMap ))
                 }else {
                     objectsWithoutProp.addAll(Org.executeQuery('select o from Org o join o.orgType ot where o.status != :deleted and not exists (select op from OrgProperty op where op.owner = o and op.tenant = :ctx and op.type = :type) ' + orgFilterClause + ' and o.status = :status order by o.sortname asc, o.name asc', parameterMap + orgFilterParams + [deleted: RDStore.ORG_STATUS_DELETED]))
                     filteredObjs.addAll(OrgProperty.executeQuery('select op.owner from OrgProperty op where op.type = :type and op.tenant = :ctx and op.owner.status = :status order by op.owner.sortname asc, op.owner.name asc', parameterMap))
                 }
                 result.sortname = true
                 break
             case PropertyDefinition.PLA_PROP:
                 if(!params.objStatus)
                     parameterMap.status = RDStore.PLATFORM_STATUS_CURRENT
                 objectsWithoutProp.addAll(Platform.executeQuery('select pl from Platform pl where pl.status != :deleted and not exists (select plp from PlatformProperty plp where plp.owner = plp and plp.tenant = :ctx and plp.type = :type) and pl.status = :status order by pl.name asc',parameterMap+[deleted:RDStore.PLATFORM_STATUS_DELETED]))
                 filteredObjs.addAll(PlatformProperty.executeQuery('select plp.owner from PlatformProperty plp where plp.type = :type and plp.tenant = :ctx and plp.owner.status = :status order by plp.owner.name asc',parameterMap))
                 break
         }
         result.withoutProp = objectsWithoutProp
         result.withProp = filteredObjs
         result
     }
}

