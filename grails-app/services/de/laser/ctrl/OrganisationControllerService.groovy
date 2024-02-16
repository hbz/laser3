package de.laser.ctrl


import de.laser.*
import de.laser.auth.User
import de.laser.finance.CostItem
import de.laser.remote.ApiSource
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyResult
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.util.WebUtils
import org.springframework.context.MessageSource

import java.text.SimpleDateFormat
import java.time.Year

/**
 * This service is a mirror of the {@link OrganisationController}, containing those controller methods
 * which manipulate data
 */
@Transactional
class OrganisationControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    ContextService contextService
    DocstoreService docstoreService
    FilterService filterService
    FinanceService financeService
    FormService formService
    GokbService gokbService
    LinksGenerationService linksGenerationService
    MessageSource messageSource
    SubscriptionsQueryService subscriptionsQueryService
    TaskService taskService
    WorkflowService workflowService

    //---------------------------------------- linking section -------------------------------------------------

    /**
     * Links two organisations by combo
     * @param params the parameter map, containing the link parameters
     * @return true if the link saving was successful, false otherwise
     */
    boolean linkOrgs(GrailsParameterMap params) {
        log.debug(params.toMapString())
        Combo c
        if(params.linkType_new) {
            c = new Combo()
            int perspectiveIndex = Integer.parseInt(params["linkType_new"].split("§")[1])
            c.type = RDStore.COMBO_TYPE_FOLLOWS
            if(perspectiveIndex == 0) {
                c.fromOrg = Org.get(params.pair_new)
                c.toOrg = Org.get(params.context)
            }
            else if(perspectiveIndex == 1) {
                c.fromOrg = Org.get(params.context)
                c.toOrg = Org.get(params.pair_new)
            }
        }
        c.save()
    }

    /**
     * Disjoins the given link between two organisatons
     * @param params the parameter map containing the combo to unlink
     * @return true if the deletion was successful, false otherwise
     */
    boolean unlinkOrg(GrailsParameterMap params) {
        int del = Combo.executeUpdate('delete from Combo c where c.id = :id',[id: params.long("combo")])
        return del > 0
    }

    //--------------------------------------------- member section -------------------------------------------------

    /**
     * Creates a new institution as member for the current consortium with the submitted parameters
     * @param controller the controller instance
     * @param params the input map containing the new institution's parameters
     * @return OK and the new institution details if the creation was successful, ERROR otherwise
     */
    Map<String,Object> createMember(OrganisationController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller,params)
        Org orgInstance
        Locale locale = LocaleUtils.getCurrentLocale()
        if(formService.validateToken(params)) {
            try {
                // createdBy will set by Org.beforeInsert()
                orgInstance = new Org(name: params.institution, sector: RDStore.O_SECTOR_HIGHER_EDU, status: RDStore.O_STATUS_CURRENT)
                orgInstance.save()
                Combo newMember = new Combo(fromOrg:orgInstance,toOrg:result.institution,type: RDStore.COMBO_TYPE_CONSORTIUM)
                newMember.save()
                orgInstance.setDefaultCustomerType()
                orgInstance.addToOrgType(RDStore.OT_INSTITUTION) //RDStore adding causes a DuplicateKeyException - RefdataValue.getByValueAndCategory('Institution', RDConstants.ORG_TYPE)
                result.orgInstance = orgInstance
                Object[] args = [messageSource.getMessage('org.institution.label',null,locale), orgInstance.name]
                result.message = messageSource.getMessage('default.created.message', args, locale)
                [result:result,status:STATUS_OK]
            }
            catch (Exception e) {
                log.error("Problem creating institution")
                log.error(e.printStackTrace())
                Object[] args = [orgInstance ? orgInstance.errors : 'unbekannt']
                result.message = messageSource.getMessage("org.error.createInstitutionError", args, locale)
                [result:result,status:STATUS_ERROR]
            }
        }
        else [result:null,status:STATUS_ERROR]
    }

    /**
     * Switches the consortial membership state between a consortium and a given institution
     * @param controller the controller instance
     * @param params the parameter map containing the combo link data
     * @return OK if the switch was successful, ERROR otherwise
     */
    Map<String, Object> toggleCombo(OrganisationController controller, GrailsParameterMap params) {
        Locale locale = LocaleUtils.getCurrentLocale()
        Map<String, Object> result = getResultGenericsAndCheckAccess(controller, params)
        if (!result) {
            return [result:null, status:STATUS_ERROR]
        }
        if (!params.direction) {
            result.error = messageSource.getMessage('org.error.noToggleDirection',null,locale)
            return [result:result, status:STATUS_ERROR]
        }
        switch(params.direction) {
            case 'add':
                Map map = [toOrg: result.institution, fromOrg: Org.get(params.fromOrg), type: RDStore.COMBO_TYPE_CONSORTIUM]
                if (! Combo.findByToOrgAndFromOrgAndType(result.institution, Org.get(params.fromOrg), RDStore.COMBO_TYPE_CONSORTIUM)) {
                    Combo cmb = new Combo(map)
                    cmb.save()
                }
                break
            case 'remove':
                if(Subscription.executeQuery("from Subscription as s where exists ( select o from s.orgRelations as o where o.org in (:orgs) )", [orgs: [result.institution, Org.get(params.fromOrg)]])){
                    result.error = messageSource.getMessage('org.consortiaToggle.remove.notPossible.sub',null,locale)
                    return [result:result, status:STATUS_ERROR]
                }
                else if(License.executeQuery("from License as l where exists ( select o from l.orgRelations as o where o.org in (:orgs) )", [orgs: [result.institution, Org.get(params.fromOrg)]])){
                    result.error = messageSource.getMessage('org.consortiaToggle.remove.notPossible.lic',null,locale)
                    return [result:result, status:STATUS_ERROR]
                }
                else {
                    Combo cmb = Combo.findByFromOrgAndToOrgAndType(result.institution,
                            Org.get(params.fromOrg),
                            RDStore.COMBO_TYPE_CONSORTIUM)
                    cmb.delete()
                }
                break
        }
        [result:result, status:STATUS_OK]
    }

    //--------------------------------------------- info -------------------------------------------------

    Map<String,Object> info(OrganisationController controller, GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(controller, params)

        Closure listToMap = { List<List> list ->
            list.groupBy{ it[0] }.sort{ it -> RefdataValue.get(it.key).getI10n('value') }
        }

        Closure reduceMap = { Map map ->
            map.collectEntries{ k,v -> [(k):(v.collect{ it[1] })] }
        }

        Closure getTimelineMap = { struct ->
            Map<String, Map> years = [:]
            IntRange timeline = (Integer.parseInt(Year.now().toString()) - 7)..(Integer.parseInt(Year.now().toString()) + 3)

            timeline.each { year ->
                String y = year.toString()
                years[y] = [:]

                struct.each { e ->
                    String s          = e[0] ? e[0].toString() : null
                    Integer startYear = e[2] ? DateUtils.getYearAsInteger(e[2]) : null
                    Integer endYear   = e[3] ? DateUtils.getYearAsInteger(e[3]) : null
                    boolean current = false

                    if (! startYear && endYear && year <= endYear) {
                        current = true
                    }
                    else if (! endYear && startYear && year >= startYear) {
                        current = true
                    }
                    else if (startYear <= year && year <= endYear) {
                        current = true
                    }
                    else if (!startYear && !endYear) {
                        current = true
                    }

                    if (current) {
                        if (! years[y][s]) {
                            years[y][s] = []
                        }
                        years[y][s] << e[1]
                    }
                }
            }
            years
        }

        // subscriptions

        Map<String, Object> subQueryParams = [org: result.orgInstance, actionName: 'manageMembers', status: 'FETCH_ALL']
        def (base_qry, qry_params) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(subQueryParams)
//        println base_qry
//        println qry_params

        List<List> subStruct = Subscription.executeQuery('select s.status.id, s.id, s.startDate, s.endDate, s.isMultiYear, s.referenceYear ' + base_qry, qry_params)
        result.subscriptionMap = reduceMap(listToMap(subStruct))
        result.subscriptionTimelineMap = getTimelineMap(subStruct)

//        println '\nsubStruct: ' + subStruct
//        println '\nsubscriptionMap: ' + result.subscriptionMap
//        println '\nsubscriptionTimelineMap: ' + result.subscriptionTimelineMap

        // licenses

        Map licenseParams = [org: result.orgInstance, activeInst: contextService.getOrg(), roleTypeC: RDStore.OR_LICENSING_CONSORTIUM]
        String licenseQuery = ''' from License as l where (
                                        exists ( select o from l.orgRelations as o where ( o.roleType = :roleTypeC AND o.org = :activeInst ) )
                                        AND l.instanceOf is not null
                                        AND exists ( select orgR from OrgRole as orgR where orgR.lic = l and orgR.org = :org )
                                    ) order by l.sortableReference, l.reference, l.startDate, l.endDate, l.instanceOf asc '''

        List<List> licStruct = License.executeQuery('select l.status.id, l.id, l.startDate, l.endDate, l.openEnded ' + licenseQuery, licenseParams)
        result.licenseMap = reduceMap(listToMap(licStruct))
        result.licenseTimelineMap = getTimelineMap(licStruct)

//        println '\nlicStruct: ' + licStruct
//        println '\nlicenseMap: ' + result.licenseMap
//        println '\nlicenseTimelineMap: ' + result.licenseTimelineMap

        // provider

//        String providerQuery = '''select sub.status.id, sub.id, sub.startDate, sub.endDate, sub.referenceYear, sub.name, por.org.id from OrgRole por
//                                    join por.sub sub
//                                    where sub.id in (:subIdList)
//                                    and por.roleType in (:porTypes)
//                                    order by por.org.sortname, por.org.name, sub.name, sub.startDate, sub.endDate asc '''
//
        String providerQuery = '''select por.org.id, sub.id, sub.startDate, sub.endDate, sub.referenceYear, sub.name, sub.status.id from OrgRole por
                                    join por.sub sub
                                    where sub.id in (:subIdList)
                                    and por.roleType in (:porTypes)
                                    order by por.org.sortname, por.org.name, sub.name, sub.startDate, sub.endDate asc '''

        Map providerParams = [
                subIdList: subStruct.collect { it[1] },
                porTypes : [RDStore.OR_PROVIDER, RDStore.OR_AGENCY]
        ]

//        println providerQuery
//        println providerParams

        List<List> providerStruct = Org.executeQuery(providerQuery, providerParams) /*.unique()*/
//        Map providerMap = listToMap(providerStruct)
        Map providerMap = providerStruct.groupBy{ it[0] }.sort{ it -> Org.get(it.key).sortname ?: Org.get(it.key).name }

//        println '\nproviderStruct: ' + providerStruct
//        println '\nproviderMap: ' + providerMap

        result.providerMap = providerMap.collectEntries{ k,v -> [(k):(v.collect{ it[1] })] }
        result.providerTimelineMap = getTimelineMap(providerStruct)

//        println '\nproviderTimelineMap: ' + result.providerTimelineMap

//        result.providerMap.each{subStatus, list ->
//            list.each{struct ->
//                Subscription sub = Subscription.get(struct[1])
//                List<CostItem> subCostItems = CostItem.executeQuery(
//                        ''' select ci from CostItem as ci right join ci.sub sub join sub.orgRelations oo
//                        where ci.owner = :owner
//                        and sub = :sub
//                        and oo.roleType = :roleType
//                        and ci.surveyOrg = null
//                        and ci.costItemStatus != :deleted
//                        order by ci.costTitle asc ''',
//                        [
//                                owner               : result.institution,
//                                sub                 : sub,
//                                roleType            : RDStore.OR_SUBSCRIPTION_CONSORTIA,
//                                deleted             : RDStore.COST_ITEM_DELETED
//                        ]
//                )
//                struct << [
//                        costItems   : subCostItems,
//                        sums        : financeService.calculateResults(subCostItems.id)
//                ]
//            }
//        }
//        println '\nproviderMap: ' + result.providerMap

        // surveys

//        List<SurveyInfo> surveyStruct =  SurveyInfo.executeQuery(
//                '''select so.finishDate != null, si.id, si.status.id, so.org.id, so.finishDate, sc.subscription.id
//                        from SurveyOrg so
//                        join so.surveyConfig sc
//                        join sc.surveyInfo si
//                        where so.org = :org and si.owner = :owner
//                        order by si.name, si.startDate, si.endDate ''',
//                [org: result.orgInstance, owner: result.institution]
//        )
//
//        Map surveyMap = surveyStruct.groupBy{ it[0] } // listToMap(surveyStruct)
//        result.surveyMap = surveyMap.collectEntries{ k,v -> [(k):(v.collect{ [ it[1], it[4], it[5] ] })] }
////        println 'surveyMap: ' + result.surveyMap

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        GrailsParameterMap surveyParams = new GrailsParameterMap(WebUtils.retrieveGrailsWebRequest().getCurrentRequest())
        surveyParams.owner = result.institution

        result.surveyMap = [:]
        ['notFinish', 'finish', 'open', 'termination'].sort().each{
            surveyParams.tab = it
            Map<String, Object> fsq = filterService.getParticipantSurveyQuery_New(surveyParams, sdf, result.orgInstance as Org)
            List sr = SurveyResult.executeQuery(fsq.query, fsq.queryParams, params)
            if (sr /*|| it == 'open' */) {
                result.surveyMap[it] = sr
            }
        }

        List<List> surveyStruct = []
        result.surveyMap.each{it -> it.value.each{e -> surveyStruct << [it.key, e, e[0].startDate, e[0].endDate]}}
        result.surveyTimelineMap = getTimelineMap(surveyStruct)

        println "!"
//        println '\nsurveyMap: ' + result.surveyMap
//        println '\nsurveyStruct: ' + surveyStruct
//        println '\nsurveyTimelineMap: ' + result.surveyTimelineMap

        // costs

        String costItemQuery = '''select ci from CostItem ci
                                    left join ci.costItemElementConfiguration ciec
                                    left join ci.costItemElement cie
                                    join ci.owner orgC
                                    join ci.sub sub
                                    join sub.instanceOf subC
                                    join subC.orgRelations roleC
                                    join sub.orgRelations roleMC
                                    join sub.orgRelations oo
                                    where orgC = :org and orgC = roleC.org and roleMC.roleType = :consortialType and oo.roleType in (:subscrType)
                                    and oo.org in (:filterConsMembers) and sub.status = :filterSubStatus
                                    and ci.surveyOrg = null and ci.costItemStatus != :deleted
                                    order by oo.org.sortname asc, sub.name, ciec.value desc, cie.value_''' + LocaleUtils.getCurrentLang() + ' desc '

        List<CostItem> consCostItems = CostItem.executeQuery( costItemQuery, [
                org                 : result.institution,
                consortialType      : RDStore.OR_SUBSCRIPTION_CONSORTIA,
                subscrType          : [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN],
                filterConsMembers   : [result.orgInstance],
                filterSubStatus     : RDStore.SUBSCRIPTION_CURRENT,
                deleted             : RDStore.COST_ITEM_DELETED
        ]
        )
        result.costs = [
                costItems   : consCostItems,
                sums        : financeService.calculateResults(consCostItems.id)
        ]
//        println result.costs

        [result: result, status: (result ? STATUS_OK : STATUS_ERROR)]
    }

    //--------------------------------------------- workflows -------------------------------------------------

    /**
     * Gets the workflows linked to the given organisation
     * @param controller the controller instance
     * @param params the request parameter map
     * @return OK if the retrieval was successful, ERROR otherwise
     */
    Map<String,Object> workflows(OrganisationController controller, GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(controller, params)

        workflowService.executeCmdAndUpdateResult(result, params)

        [result: result, status: (result ? STATUS_OK : STATUS_ERROR)]
    }

    //--------------------------------------------- identifier section -------------------------------------------------

    /**
     * Deletes the given customer identifier
     * @param controller the controller instance
     * @param params the parameter map containing the identifier data
     * @return OK if the deletion was successful, ERROR otherwise
     */
    Map<String,Object> deleteCustomerIdentifier(OrganisationController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller,params)
        Locale locale = LocaleUtils.getCurrentLocale()
        CustomerIdentifier ci = CustomerIdentifier.get(params.long('deleteCI'))
        Org owner = ci.owner
        if (ci) {
            ci.delete()
            log.debug("CustomerIdentifier deleted: ${params}")
            [result:result,status:STATUS_OK]
        } else {
            if ( ! ci ) {
                Object[] args = [messageSource.getMessage('org.customerIdentifier',null,locale), params.deleteCI]
                result.error = messageSource.getMessage('default.not.found.message', args, locale)
            }
            log.error("CustomerIdentifier NOT deleted: ${params}; CustomerIdentifier not found or ContextOrg is not " +
                    "owner of this CustomerIdentifier and has no rights to delete it!")
            [result:result,status:STATUS_ERROR]
        }
    }

    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Sets parameters which are used in many controller pages such as current user, context institution and perspectives
     * @param controller the controller instance
     * @param params the request parameter map
     * @return a result map containing the current user, institution, flags whether the view is that of the context
     * institution and which settings are available for the given call
     */
    Map<String, Object> getResultGenericsAndCheckAccess(OrganisationController controller, GrailsParameterMap params) {

        User user = contextService.getUser()
        Org org = contextService.getOrg()
        Map<String, Object> result = [user:user,
                                      institution:org,
                                      contextOrg: org,
                                      inContextOrg:true,
                                      isMyOrg:false,
                                      institutionalView:false,
                                      isGrantedOrgRoleAdminOrOrgEditor: SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'),
                                      isGrantedOrgRoleAdmin: SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'),
                                      contextCustomerType:org.getCustomerType()]

        //if(result.contextCustomerType == 'ORG_CONSORTIUM_BASIC')

        result.availableConfigs = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.SHARE_CONFIGURATION)
        if (org.isCustomerType_Consortium()) {
            result.availableConfigs-RDStore.SHARE_CONF_CONSORTIUM
        }

        if (params.id) {
            result.orgInstance = Org.get(params.id)
            if(result.orgInstance.gokbId) {
                ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
                result.editUrl = apiSource.editUrl.endsWith('/') ? apiSource.editUrl : apiSource.editUrl+'/'
                Map queryResult = gokbService.executeQuery(apiSource.baseUrl + apiSource.fixToken + "/searchApi", [uuid: result.orgInstance.gokbId])
                if (queryResult.error && queryResult.error == 404) {
                    result.error = messageSource.getMessage('wekb.error.404', null, LocaleUtils.getCurrentLocale())
                }
                else if (queryResult) {
                    List records = queryResult.result
                    result.orgInstanceRecord = records ? records[0] : [:]
                }
            }
            result.editable = controller._checkIsEditable(user, result.orgInstance)
            result.inContextOrg = result.orgInstance.id == org.id
            //this is a flag to check whether the page has been called for a consortia or inner-organisation member
            Combo checkCombo = Combo.findByFromOrgAndToOrg(result.orgInstance,org)
            if (checkCombo && checkCombo.type == RDStore.COMBO_TYPE_CONSORTIUM) {
                result.institutionalView = true
                result.isMyOrg = true //we make the existence of a combo relation condition to "my"
            }
            else if(!checkCombo) {
                checkCombo = Combo.findByToOrgAndFromOrgAndType(result.orgInstance, org, RDStore.COMBO_TYPE_CONSORTIUM)
                if(checkCombo) {
                    result.consortialView = true
                    result.isMyOrg = true
                }
            }
            //restrictions hold if viewed org is not the context org
//            if (!result.inContextOrg && !contextService.getOrg().isCustomerType_Consortium() && !SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
//                //restrictions further concern only single users or consortium members, not consortia
//                if (!contextService.getOrg().isCustomerType_Consortium() && result.orgInstance.isCustomerType_Inst()) {
//                    return null
//                }
//            }
            if (!result.inContextOrg && !SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
                //restrictions further concern only single users or consortium members, not consortia
                if (!(contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) && result.orgInstance.isCustomerType_Inst()) {
                    return null
                }
            }
            //set isMyOrg-flag for relations context -> provider
            if(OrgSetting.get(result.orgInstance, OrgSetting.KEYS.CUSTOMER_TYPE) == OrgSetting.SETTING_NOT_FOUND) {
                int relationCheck = OrgRole.executeQuery('select count(oo) from OrgRole oo join oo.sub sub where oo.org = :context and sub in (select os.sub from OrgRole os where os.roleType in (:providerRoles)) and (sub.status = :current or (sub.status = :expired and sub.hasPerpetualAccess = true))', [context: result.institution, providerRoles: [RDStore.OR_PROVIDER, RDStore.OR_AGENCY], current: RDStore.SUBSCRIPTION_CURRENT, expired: RDStore.SUBSCRIPTION_EXPIRED])[0]
                result.isMyOrg = relationCheck > 0
            }
        }
        else {
            result.editable = controller._checkIsEditable(user, org)
            result.orgInstance = result.institution
            result.inContextOrg = true
        }

        int tc1 = taskService.getTasksByResponsiblesAndObject(result.user, result.contextOrg, result.orgInstance).size()
        int tc2 = taskService.getTasksByCreatorAndObject(result.user, result.orgInstance).size()
        result.tasksCount = (tc1 || tc2) ? "${tc1}/${tc2}" : ''

        result.notesCount       = docstoreService.getNotesCount(result.orgInstance, result.contextOrg)
        result.checklistCount   = workflowService.getWorkflowCount(result.orgInstance, result.contextOrg)

        result.links = linksGenerationService.getOrgLinks(result.orgInstance)
        Map<String, List> nav = (linksGenerationService.generateNavigation(result.orgInstance, true))
        result.navPrevOrg = nav.prevLink
        result.navNextOrg = nav.nextLink
        result.targetCustomerType = result.orgInstance.getCustomerType()
        result.allOrgTypeIds = result.orgInstance.getAllOrgTypeIds()
        result.isProviderOrAgency = (RDStore.OT_PROVIDER.id in result.allOrgTypeIds) || (RDStore.OT_AGENCY.id in result.allOrgTypeIds)
        result
    }
}