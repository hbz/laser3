package de.laser.ajax

import de.laser.IssueEntitlement
import de.laser.License
import de.laser.auth.Role
import de.laser.helper.SessionCacheWrapper
import de.laser.properties.LicenseProperty
import de.laser.Org
import de.laser.properties.OrgProperty
import de.laser.properties.PersonProperty
import de.laser.Platform
import de.laser.properties.PlatformProperty
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.properties.SubscriptionProperty
import de.laser.auth.User
import de.laser.I10nTranslation
import de.laser.Contact
import de.laser.Person
import de.laser.PersonRole
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.base.AbstractI10n
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.helper.AppUtils
import de.laser.annotations.DebugAnnotation
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.properties.PropertyDefinition
import de.laser.reporting.myInstitution.GenericConfig
import de.laser.reporting.myInstitution.GenericQuery
import de.laser.reporting.myInstitution.LicenseConfig
import de.laser.reporting.myInstitution.LicenseQuery
import de.laser.reporting.myInstitution.OrganisationConfig
import de.laser.reporting.myInstitution.OrganisationQuery
import de.laser.reporting.myInstitution.SubscriptionConfig
import de.laser.reporting.myInstitution.SubscriptionQuery
import de.laser.reporting.subscription.SubscriptionReporting
import de.laser.traits.I10nTrait
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.core.GrailsClass
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.i18n.LocaleContextHolder
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Secured(['IS_AUTHENTICATED_FULLY'])
class AjaxJsonController {

    /**
     * only json rendering here ..
     * no object manipulation
     *
     */
    def accessService
    def compareService
    def contextService
    def controlledListService
    def dataConsistencyService
    def genericOIDService
    def licenseService
    def linksGenerationService
    def subscriptionService

    @Secured(['ROLE_USER'])
    def test() {
        Map<String, Object> result = [status: 'ok']
        result.id = params.id
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def adjustSubscriptionList(){
        List<Subscription> data
        List result = []
        boolean showSubscriber = params.showSubscriber == 'true'
        boolean showConnectedObjs = params.showConnectedObjs == 'true'
        Map queryParams = [:]

        queryParams.status = []
        if (params.get('status')){
            queryParams.status = params.list('status').collect{ Long.parseLong(it) }
        }
        queryParams.showSubscriber = showSubscriber
        queryParams.showConnectedObjs = showConnectedObjs

        data = subscriptionService.getMySubscriptions_writeRights(queryParams)
        if (data) {
            if (params.valueAsOID){
                data.each { Subscription s ->
                    result.add([value: genericOIDService.getOID(s), text: s.dropdownNamingConvention()])
                }
            } else {
                data.each { Subscription s ->
                    result.add([value: s.id, text: s.dropdownNamingConvention()])
                }
            }
        }
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def adjustLicenseList(){
        List<Subscription> data
        List result = []
        boolean showSubscriber = params.showSubscriber == 'true'
        boolean showConnectedObjs = params.showConnectedObjs == 'true'
        Map queryParams = [:]

        queryParams.status = []
        if (params.get('status')){
            queryParams.status = params.list('status').collect{ Long.parseLong(it) }
        }

        queryParams.showSubscriber = showSubscriber
        queryParams.showConnectedObjs = showConnectedObjs

        data =  licenseService.getMyLicenses_writeRights(queryParams)
        if (data) {
            if (params.valueAsOID){
                data.each { License l ->
                    result.add([value: genericOIDService.getOID(l), text: l.dropdownNamingConvention()])
                }
            } else {
                data.each { License l ->
                    result.add([value: l.id, text: l.dropdownNamingConvention()])
                }
            }
        }
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def adjustCompareSubscriptionList(){
        List<Subscription> data
        List result = []
        boolean showSubscriber = params.showSubscriber == 'true'
        boolean showConnectedObjs = params.showConnectedObjs == 'true'
        Map queryParams = [:]
        if (params.get('status')){
            queryParams.status = params.list('status').collect{ Long.parseLong(it) }
        }

        queryParams.showSubscriber = showSubscriber
        queryParams.showConnectedObjs = showConnectedObjs

        data = compareService.getMySubscriptions(queryParams)
        if (accessService.checkPerm("ORG_CONSORTIUM")) {
            if (showSubscriber) {
                List parents = data.clone()
                Set<RefdataValue> subscriberRoleTypes = [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER_COLLECTIVE]
                data.addAll(Subscription.executeQuery('select s from Subscription s join s.orgRelations oo where s.instanceOf in (:parents) and oo.roleType in :subscriberRoleTypes order by oo.org.sortname asc, oo.org.name asc', [parents: parents, subscriberRoleTypes: subscriberRoleTypes]))
            }
        }

        if (showConnectedObjs){
            data.addAll(linksGenerationService.getAllLinkedSubscriptions(data, contextService.getUser()))
        }

        if (data) {
            data.each { Subscription s ->
                result.add([value: s.id, text: s.dropdownNamingConvention()])
            }
            result.sort{it.text}
        }
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def adjustCompareLicenseList(){
        List<License> data
        List result = []
        boolean showSubscriber = params.showSubscriber == 'true'
        boolean showConnectedLics = params.showConnectedLics == 'true'
        Map queryParams = [:]
        if (params.get('status')){
            queryParams.status = params.list('status').collect{ Long.parseLong(it) }
        }

        queryParams.showSubscriber = showSubscriber
        queryParams.showConnectedLics = showConnectedLics

        data = compareService.getMyLicenses(queryParams)
        if (accessService.checkPerm("ORG_CONSORTIUM")) {
            if (showSubscriber) {
                List parents = data.clone()
                Set<RefdataValue> subscriberRoleTypes = [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]
                data.addAll(License.executeQuery('select l from License l join l.orgRelations oo where l.instanceOf in (:parents) and oo.roleType in :subscriberRoleTypes order by oo.org.sortname asc, oo.org.name asc', [parents: parents, subscriberRoleTypes: subscriberRoleTypes]))
            }
        }

        if (data) {
            data.each { License l ->
                result.add([value: l.id, text: l.dropdownNamingConvention()])
            }
            result.sort{it.text}
        }
        render result as JSON
    }


    @Secured(['ROLE_USER'])
    def consistencyCheck() {
        List result = dataConsistencyService.ajaxQuery(params.key, params.key2, params.value)
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def checkCascade() {
        Map<String, Object> result = [sub:true, subPkg:true, ie:true]
        if (!params.subscription && ((params.package && params.issueEntitlement) || params.issueEntitlement)) {
            result.sub = false
            result.subPkg = false
            result.ie = false
        }
        else if (params.subscription) {
            Subscription sub = (Subscription) genericOIDService.resolveOID(params.subscription)
            if (!sub) {
                result.sub = false
                result.subPkg = false
                result.ie = false
            }
            else if (params.issueEntitlement) {
                if (!params.package || params.package.contains('null')) {
                    result.subPkg = false
                    result.ie = false
                }
                else if (params.package && !params.package.contains('null')) {
                    SubscriptionPackage subPkg = (SubscriptionPackage) genericOIDService.resolveOID(params.package)
                    if(!subPkg || subPkg.subscription != sub) {
                        result.subPkg = false
                        result.ie = false
                    }
                    else {
                        IssueEntitlement ie = (IssueEntitlement) genericOIDService.resolveOID(params.issueEntitlement)
                        if(!ie || ie.subscription != subPkg.subscription || ie.tipp.pkg != subPkg.pkg) {
                            result.ie = false
                        }
                    }
                }
            }
        }
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def getBooleans() {
        List result = [
                [value: 1, text: RDStore.YN_YES.getI10n('value')],
                [value: 0, text: RDStore.YN_NO.getI10n('value')]
        ]
        render result as JSON
    }

    /*@Secured(['ROLE_USER'])
    def getLinkedLicenses() {
        render controlledListService.getLinkedObjects([destination:params.subscription, sourceType: License.class.name, linkTypes:[RDStore.LINKTYPE_LICENSE], status:params.status]) as JSON
    }

    @Secured(['ROLE_USER'])
    def getLinkedSubscriptions() {
        render controlledListService.getLinkedObjects([source:params.license, destinationType: Subscription.class.name, linkTypes:[RDStore.LINKTYPE_LICENSE], status:params.status]) as JSON
    }*/

    @Secured(['ROLE_USER'])
    def getPropValues() {
        List<Map<String, Object>> result = []

        if (params.oid != "undefined") {
            PropertyDefinition propDef = (PropertyDefinition) genericOIDService.resolveOID(params.oid)
            if (propDef) {
                List<AbstractPropertyWithCalculatedLastUpdated> values

                if (propDef.tenant) {
                    switch (propDef.descr) {
                        case PropertyDefinition.SUB_PROP: values = SubscriptionProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
                            break
                        case PropertyDefinition.ORG_PROP: values = OrgProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
                            break
                        case PropertyDefinition.PLA_PROP: values = PlatformProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
                            break
                        case PropertyDefinition.PRS_PROP: values = PersonProperty.findAllByType(propDef)
                            break
                        case PropertyDefinition.LIC_PROP: values = LicenseProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
                            break
                    }
                }
                else {
                    switch (propDef.descr) {
                        case PropertyDefinition.SUB_PROP: values = SubscriptionProperty.executeQuery('select sp from SubscriptionProperty sp left join sp.owner.orgRelations oo where sp.type = :propDef and (sp.tenant = :tenant or ((sp.tenant != :tenant and sp.isPublic = true) or sp.instanceOf != null) and :tenant in oo.org)',[propDef:propDef, tenant:contextService.getOrg()])
                            break
                        case PropertyDefinition.ORG_PROP: values = OrgProperty.executeQuery('select op from OrgProperty op where op.type = :propDef and ((op.tenant = :tenant and op.isPublic = true) or op.tenant = null)',[propDef:propDef,tenant:contextService.getOrg()])
                            break
                    /*case PropertyDefinition.PLA_PROP: values = PlatformProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.org,false)
                        break
                    case PropertyDefinition.PRS_PROP: values = PersonProperty.findAllByType(propDef)
                        break*/
                        case PropertyDefinition.LIC_PROP: values = LicenseProperty.executeQuery('select lp from LicenseProperty lp left join lp.owner.orgRelations oo where lp.type = :propDef and (lp.tenant = :tenant or ((lp.tenant != :tenant and lp.isPublic = true) or lp.instanceOf != null) and :tenant in oo.org)',[propDef:propDef, tenant:contextService.getOrg()])
                            break
                    }
                }
                if (values) {
                    //very ugly, needs a more elegant solution
                    if (propDef.isIntegerType()) {
                        values.intValue.findAll().unique().each { v ->
                            result.add([value:v,text:v])
                        }
                        result = result.sort { x, y -> x.text.compareTo(y.text) }
                    }
                    else if (propDef.isDateType()) {
                        values.dateValue.findAll().unique().sort().reverse().each { v ->
                            String vt = g.formatDate(formatName:"default.date.format.notime", date:v)
                            result.add([value: vt, text: vt])
                        }
                    }
                    else if (propDef.isRefdataValueType()) {
                        values.each { AbstractPropertyWithCalculatedLastUpdated v ->
                            if (v.getValue() != null)
                                result.add([value:v.getValue(),text:v.refValue.getI10n("value")])
                        }
                        result = result.sort { x, y -> x.text.compareToIgnoreCase(y.text) }
                    }
                    else {
                        values.value?.findAll()?.unique()?.each { v ->
                            result.add([value:v,text:v])
                        }
                        result = result.sort { x, y -> x.text.compareToIgnoreCase(y.text) }
                    }
                }
            }
        }
        //excepted structure: [[value:,text:],[value:,text:]]

        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def getProvidersWithPrivateContacts() {
        Map<String, Object> result = [:]
        String fuzzyString = params.sSearch ? ('%' + params.sSearch.trim().toLowerCase() + '%') : '%'

        Map<String, Object> query_params = [
                name: fuzzyString,
                status: RDStore.O_STATUS_DELETED
        ]
        String countQry = "select count(o) from Org as o where exists (select roletype from o.orgType as roletype where roletype.value = 'Provider' ) and lower(o.name) like :name and (o.status is null or o.status != :status)"
        String rowQry = "select o from Org as o where exists (select roletype from o.orgType as roletype where roletype.value = 'Provider' ) and lower(o.name) like :name and (o.status is null or o.status != :status) order by o.name asc"

        def cq = Org.executeQuery(countQry,query_params)

        List<Org> rq = Org.executeQuery(rowQry,
                query_params,
                [max:params.iDisplayLength?:1000,offset:params.iDisplayStart?:0])

        result.aaData = []
        result.sEcho = params.sEcho
        result.iTotalRecords = cq[0]
        result.iTotalDisplayRecords = cq[0]

        Org currOrg = (Org) genericOIDService.resolveOID(params.oid)
        List<Person> contacts = Person.findAllByContactTypeAndTenant(RDStore.PERSON_CONTACT_TYPE_PERSONAL, currOrg)

        LinkedHashMap personRoles = [:]
        PersonRole.findAll().each { PersonRole prs ->
            personRoles.put(prs.org, prs.prs)
        }
        rq.each { Org it ->
            int ctr = 0
            LinkedHashMap row = [:]
            String name = it["name"]
            if (personRoles.get(it) && contacts.indexOf(personRoles.get(it)) > -1)
                name += '<span data-tooltip="Persönlicher Kontakt vorhanden"><i class="address book icon"></i></span>'
            row["${ctr++}"] = name
            row["DT_RowId"] = "${it.class.name}:${it.id}"
            result.aaData.add(row)
        }

        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def getRegions() {
        List<RefdataValue> result = []
        if (params.country) {
            List<Long> countryIds = params.country.split(',')
            countryIds.each {
                switch (RefdataValue.get(it).value) {
                    case 'DE':
                        result.addAll( RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_DE]) )
                        break;
                    case 'AT':
                        result.addAll( RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_AT]) )
                        break;
                    case 'CH':
                        result.addAll( RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_CH]) )
                        break;
                }
            }
        }
        result = result.flatten()

        render result as JSON // TODO -> check response; remove unnecessary information! only id and value_<x>?
    }

    @Secured(['ROLE_USER'])
    def lookup() {
        // fallback for static refdataFind calls
        params.shortcode  = contextService.getOrg().shortcode

        Map<String, Object> result = [values: []]
        params.max = params.max ?: 40

        GrailsClass domain_class = AppUtils.getDomainClass(params.baseClass)

        if (domain_class) {
            result.values = domain_class.getClazz().refdataFind(params)
            result.values.sort{ x,y -> x.text.compareToIgnoreCase y.text }
        }
        else {
            log.error("Unable to locate domain class ${params.baseClass}")
        }

        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupBudgetCodes() {
        render controlledListService.getBudgetCodes(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupCombined() {
        render controlledListService.getElements(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupInvoiceNumbers() {
        render controlledListService.getInvoiceNumbers(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupIssueEntitlements() {
        params.checkView = true
        if(params.sub != "undefined") {
            render controlledListService.getIssueEntitlements(params) as JSON
        } else {
            Map entry = ["results": []]
            render entry as JSON
        }
    }

    @Secured(['ROLE_USER'])
    def lookupLicenses() {
        render controlledListService.getLicenses(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupOrderNumbers() {
        render controlledListService.getOrderNumbers(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupProvidersAgencies() {
        render controlledListService.getProvidersAgencies(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupProviderAndPlatforms() {
        List result = []

        List<Org> provider = Org.executeQuery('SELECT o FROM Org o JOIN o.orgType ot WHERE ot = :ot', [ot: RDStore.OT_PROVIDER])
        provider.each{ prov ->
            Map<String, Object> pp = [name: prov.name, value: prov.class.name + ":" + prov.id, platforms:[]]

            Platform.findAllByOrg(prov).each { plt ->
                pp.platforms.add([name: plt.name, value: plt.class.name + ":" + plt.id])
            }
            result.add(pp)
        }
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupReferences() {
        render controlledListService.getReferences(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupRoles() {
        List result = []
        List<Role> roles = params.type ? Role.findAllByRoleType(params.type.toLowerCase()) :  Role.findAll()

        roles.each { r ->
            result.add([text: message(code:'cv.roles.' + r.authority), key: "${r.getI10n('authority')}", value: "${r.class.name}:${r.id}"])
        }

        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupSubscriptions() {
        render controlledListService.getSubscriptions(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupSubscriptionPackages() {
        if (params.ctx != "undefined") {
            render controlledListService.getSubscriptionPackages(params) as JSON
        }
        else {
            render [:] as JSON
        }
    }

    @Secured(['ROLE_USER'])
    def lookupSubscriptionsLicenses() {
        Map<String, Object> result = [results:[]]
        result.results.addAll(controlledListService.getSubscriptions(params).results)
        result.results.addAll(controlledListService.getLicenses(params).results)

        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupCurrentAndIndendedSubscriptions() {
        params.status = [RDStore.SUBSCRIPTION_INTENDED, RDStore.SUBSCRIPTION_CURRENT]

        render controlledListService.getSubscriptions(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupTitleGroups() {
        params.checkView = true
        if(params.sub != "undefined") {
            render controlledListService.getTitleGroups(params) as JSON
        } else {
            Map empty = [results: []]
            render empty as JSON
        }
    }

    @Secured(['ROLE_USER'])
    def refdataSearchByCategory() {
        List result = []

        RefdataCategory rdc
        if(params.oid)
            rdc = (RefdataCategory) genericOIDService.resolveOID(params.oid)
        else if(params.cat)
            rdc = RefdataCategory.getByDesc(params.cat)
        if (rdc) {
            String locale = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())
            String query = "select rdv from RefdataValue as rdv where rdv.owner.id='${rdc.id}' order by rdv.order, rdv.value_" + locale

            List<RefdataValue> rq = RefdataValue.executeQuery(query, [], [max: params.iDisplayLength ?: 1000, offset: params.iDisplayStart ?: 0])

            rq.each { RefdataValue it ->
                if (it instanceof I10nTrait || it instanceof AbstractI10n) {
                    result.add([value: "${genericOIDService.getOID(it)}", text: "${it.getI10n('value')}"])
                }
                else {
                    String value = it.value
                    if (value) {
                        String no_ws = value.replaceAll(' ', '')
                        String locale_text = message(code: "refdata.${no_ws}", default: "${value}")
                        result.add([value: "${it.class.name}:${it.id}", text: "${locale_text}"])
                    }
                }
            }
        }
        if (result) {
            RefdataValue notSet = RDStore.GENERIC_NULL_VALUE
            result.add([value: "${genericOIDService.getOID(notSet)}", text: "${notSet.getI10n('value')}"])
        }

        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def searchPropertyAlternativesByOID() {
        List<Map<String, Object>> result = []
        PropertyDefinition pd = (PropertyDefinition) genericOIDService.resolveOID(params.oid)

        List<PropertyDefinition> queryResult = PropertyDefinition.findAllWhere(
                descr: pd.descr,
                refdataCategory: pd.refdataCategory,
                type: pd.type,
                multipleOccurrence: pd.multipleOccurrence,
                tenant: pd.tenant
        )//.minus(pd)

        queryResult.each { it ->
            PropertyDefinition rowobj = GrailsHibernateUtil.unwrapIfProxy(it)
            if (pd.isUsedForLogic) {
                if (it.isUsedForLogic) {
                    result.add([value: "${rowobj.class.name}:${rowobj.id}", text: "${it.getI10n('name')}"])
                }
            }
            else {
                if (! it.isUsedForLogic) {
                    result.add([value: "${rowobj.class.name}:${rowobj.id}", text: "${it.getI10n('name')}"])
                }
            }
        }
        if (result.size() > 1) {
            result.sort{ x,y -> x.text.compareToIgnoreCase y.text }
        }

        render result as JSON
    }

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.contextService.getUser()?.hasRole('ROLE_ADMIN') || ctx.contextService.getUser()?.hasAffiliation("INST_ADM") })
    def checkExistingUser() {
        Map<String, Object> result = [result: false]

        if (params.input) {
            result.result = null != User.findByUsernameIlike(params.input)
        }
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def getEmailAddresses() {
        List result = []

        if (params.orgIdList) {
            List<Long> orgIds = params.orgIdList.split(',').collect{ Long.parseLong(it) }
            List<Org> orgList = orgIds ? Org.findAllByIdInList(orgIds) : []

            String query = "select distinct p from Person as p inner join p.roleLinks pr where pr.org in (:orgs) "
            Map<String, Object> queryParams = [orgs: orgList]

            boolean showPrivateContactEmails = Boolean.valueOf(params.isPrivate)
            boolean showPublicContactEmails = Boolean.valueOf(params.isPublic)

            if (showPublicContactEmails && showPrivateContactEmails){
                query += "and ( (p.isPublic = false and p.tenant = :ctx) or (p.isPublic = true) ) "
                queryParams << [ctx: contextService.getOrg()]
            } else {
                if (showPublicContactEmails){
                    query += "and p.isPublic = true "
                } else if (showPrivateContactEmails){
                    query += "and (p.isPublic = false and p.tenant = :ctx) "
                    queryParams << [ctx: contextService.getOrg()]
                } else {
                    return [] as JSON
                }
            }

            if (params.selectedRoleTypIds) {
                List<Long> selectedRoleTypIds = params.selectedRoleTypIds.split(',').collect { Long.parseLong(it) }
                List<RefdataValue> selectedRoleTypes = selectedRoleTypIds ? RefdataValue.findAllByIdInList(selectedRoleTypIds) : []

                if (selectedRoleTypes) {
                    query += "and pr.functionType in (:selectedRoleTypes) "
                    queryParams << [selectedRoleTypes: selectedRoleTypes]
                }
            }

            List<Person> persons = Person.executeQuery(query, queryParams)
            if (persons) {
                result = Contact.executeQuery("select c.content from Contact c where c.prs in (:persons) and c.contentType = :contentType",
                        [persons: persons, contentType: RDStore.CCT_EMAIL])
            }
        }

        render result as JSON
    }

    // ----- reporting -----

    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_USER")
    })
    def chart() {
        Map<String, Object> result = [:]

        if (params.context == GenericConfig.KEY && params.query) {
            SessionCacheWrapper sessionCache = contextService.getSessionCache()
            Map<String, Object> cached = sessionCache.get("MyInstitutionController/reporting/" + params.token)

            GrailsParameterMap clone = params.clone() as GrailsParameterMap// TODO: simplify
            if (cached) {
                clone.putAll(cached)
            }

            Closure getTooltipLabel = { GrailsParameterMap pm ->
                if (pm.filter == 'license') {
                    GenericQuery.getQueryLabels(LicenseConfig.CONFIG, pm).get(1)
                }
                else if (pm.filter == 'organisation') {
                    GenericQuery.getQueryLabels(OrganisationConfig.CONFIG, pm).get(1)
                }
                else if (pm.filter == 'subscription') {
                    GenericQuery.getQueryLabels(SubscriptionConfig.CONFIG, pm).get(1)
                }
            }

            String prefix = clone.query.split('-')[0]

            if (prefix in ['license']) {
                result = LicenseQuery.query(clone)
                result.tooltipLabel = getTooltipLabel(clone)

                if (clone.query.endsWith('assignment')) {
                    result.chartLabels = LicenseConfig.CONFIG.base.query2.getAt('Verteilung').getAt(clone.query).getAt('chartLabels')
                    render template: '/myInstitution/reporting/chart/complex', model: result
                }
                else {
                    render template: '/myInstitution/reporting/chart/generic', model: result
                }
                return
            }
            else if (prefix in ['org', 'member', 'provider', 'licensor']) {
                result = OrganisationQuery.query(clone)
                result.tooltipLabel = getTooltipLabel(clone)

                if (clone.query.endsWith('assignment')) {
                    result.chartLabels = OrganisationConfig.CONFIG.base.query2.getAt('Verteilung').getAt(clone.query).getAt('chartLabels')
                    render template: '/myInstitution/reporting/chart/complex', model: result
                }
                else {
                    render template: '/myInstitution/reporting/chart/generic', model: result
                }
                return
            }
            else if (prefix in ['subscription']) {
                result = SubscriptionQuery.query(clone)
                result.tooltipLabel = getTooltipLabel(clone)

                if (clone.query.endsWith('assignment') && clone.query != 'subscription-provider-assignment') {
                    result.chartLabels = SubscriptionConfig.CONFIG.base.query2.getAt('Verteilung').getAt(clone.query).getAt('chartLabels')
                    render template: '/myInstitution/reporting/chart/complex', model: result
                }
                else {
                    render template: '/myInstitution/reporting/chart/generic', model: result
                }
                return
            }
        }
        else if (params.context == SubscriptionConfig.KEY && params.query) {
            GrailsParameterMap clone = params.clone() as GrailsParameterMap // TODO: simplify

            result = SubscriptionReporting.query(clone)
            result.chartLabels = SubscriptionReporting.QUERY.getAt('Entwicklung').getAt(clone.query).getAt('chartLabels')

            if (clone.query in ['cost-timeline']) {
                render template: '/subscription/reporting/chart/' + clone.query, model: result
            }
            else {
                render template: '/subscription/reporting/chart/generic-timeline', model: result
            }
            return
        }

        render result as JSON
    }
}