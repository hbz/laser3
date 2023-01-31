package de.laser.ctrl


import de.laser.*
import de.laser.auth.User
import de.laser.remote.ApiSource
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import de.laser.workflow.WfWorkflow
import de.laser.workflow.light.WfChecklist
import de.laser.workflow.light.WfCheckpoint
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

/**
 * This service is a mirror of the {@link OrganisationController}, containing those controller methods
 * which manipulate data
 */
@Transactional
class OrganisationControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AccessService accessService
    ContextService contextService
    DocstoreService docstoreService
    FormService formService
    GenericOIDService genericOIDService
    GokbService gokbService
    LinksGenerationService linksGenerationService
    MessageSource messageSource
    TaskService taskService
    WorkflowLightService workflowLightService
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
                c.fromOrg = Org.get(params.context)
                c.toOrg = Org.get(params.pair_new)
            }
            else if(perspectiveIndex == 1) {
                c.fromOrg = Org.get(params.pair_new)
                c.toOrg = Org.get(params.context)
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

    //--------------------------------------------- workflows -------------------------------------------------

    Map<String,Object> workflows(OrganisationController controller, GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(controller, params)

        if (params.cmd) {
            String[] cmd = params.cmd.split(':')

            if (cmd[1] in [WfChecklist.KEY, WfCheckpoint.KEY] ) { // light
                result.putAll( workflowLightService.cmd(params) )
            }
            else {
                if (cmd[0] in ['edit']) {
                    result.putAll( workflowService.cmd(params) ) // @ workflows
                }
                else {
                    result.putAll( workflowService.usage(params) ) // @ workflows
                }
            }
        }
        if (params.info) {
            result.info = params.info // @ currentWorkflows @ dashboard
        }

        result.checklists = workflowLightService.sortByLastUpdated( WfChecklist.findAllByOrgAndOwner(result.orgInstance as Org, result.contextOrg as Org) )
        result.checklistCount = result.checklists.size()

        result.workflows = workflowService.sortByLastUpdated( WfWorkflow.findAllByOrgAndOwner(result.orgInstance as Org, result.contextOrg as Org) )
        result.workflowCount = result.workflows.size()

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
        CustomerIdentifier ci = (CustomerIdentifier) genericOIDService.resolveOID(params.deleteCI)
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
                                      institutionalView:false,
                                      isGrantedOrgRoleAdminOrOrgEditor: SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'),
                                      isGrantedOrgRoleAdmin: SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'),
                                      contextCustomerType:org.getCustomerType()]

        //if(result.contextCustomerType == 'ORG_CONSORTIUM')

        result.availableConfigs = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.SHARE_CONFIGURATION)
        if(result.contextCustomerType == "ORG_CONSORTIUM"){
            result.availableConfigs-RDStore.SHARE_CONF_CONSORTIUM
        }

        if (params.id) {
            if(params.id instanceof Long || params.id.isLong())
                result.orgInstance = Org.get(params.id)
            else if(params.id ==~ /[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}/)
                result.orgInstance = Org.findByGokbId(params.id)
            else result.orgInstance = Org.findByGlobalUID(params.id)
            if(result.orgInstance.gokbId) {
                ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
                result.editUrl = apiSource.editUrl.endsWith('/') ? apiSource.editUrl : apiSource.editUrl+'/'
                Map queryResult = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + "/find?uuid=${result.orgInstance.gokbId}")
                if (queryResult.error && queryResult.error == 404) {
                    result.error = messageSource.getMessage('wekb.error.404', null, LocaleUtils.getCurrentLocale())
                }
                else if (queryResult.warning) {
                    List records = queryResult.warning.records
                    result.orgInstanceRecord = records ? records[0] : [:]
                }
            }
            result.editable = controller.checkIsEditable(user, result.orgInstance)
            result.inContextOrg = result.orgInstance.id == org.id
            //this is a flag to check whether the page has been called for a consortia or inner-organisation member
            Combo checkCombo = Combo.findByFromOrgAndToOrg(result.orgInstance,org)
            if (checkCombo && checkCombo.type == RDStore.COMBO_TYPE_CONSORTIUM) {
                result.institutionalView = true
            }
            else if(!checkCombo) {
                checkCombo = Combo.findByToOrgAndFromOrgAndType(result.orgInstance, org, RDStore.COMBO_TYPE_CONSORTIUM)
                if(checkCombo)
                    result.consortialView = true
            }
            //restrictions hold if viewed org is not the context org
            if (!result.inContextOrg && !accessService.checkPerm("ORG_CONSORTIUM") && !SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
                //restrictions further concern only single users or consortium members, not consortia
                if (!accessService.checkPerm("ORG_CONSORTIUM") && result.orgInstance.getCustomerType() in ["ORG_BASIC_MEMBER","ORG_INST"]) {
                    return null
                }
            }
        }
        else {
            result.editable = controller.checkIsEditable(user, org)
            result.orgInstance = result.institution
            result.inContextOrg = true
        }

        int tc1 = taskService.getTasksByResponsiblesAndObject(result.user, result.contextOrg, result.orgInstance).size()
        int tc2 = taskService.getTasksByCreatorAndObject(result.user, result.orgInstance).size()
        result.tasksCount = (tc1 || tc2) ? "${tc1}/${tc2}" : ''

        result.notesCount       = docstoreService.getNotes(result.orgInstance, result.contextOrg).size()
        result.workflowCount    = workflowService.getWorkflowCount(result.orgInstance, result.contextOrg)
        result.checklistCount   = workflowLightService.getWorkflowCount(result.orgInstance, result.contextOrg)

        result.links = linksGenerationService.getOrgLinks(result.orgInstance)
        result.targetCustomerType = result.orgInstance.getCustomerType()
        result.allOrgTypeIds = result.orgInstance.getAllOrgTypeIds()
        result.isProviderOrAgency = (RDStore.OT_PROVIDER.id in result.allOrgTypeIds) || (RDStore.OT_AGENCY.id in result.allOrgTypeIds)
        result
    }
}