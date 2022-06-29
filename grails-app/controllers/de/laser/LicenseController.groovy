package de.laser


import de.laser.auth.User
import de.laser.ctrl.LicenseControllerService
import de.laser.custom.CustomWkhtmltoxService
import de.laser.utils.LocaleUtils
import de.laser.storage.RDConstants
import de.laser.properties.LicenseProperty
import de.laser.auth.Role
import de.laser.auth.UserOrg
import de.laser.properties.PropertyDefinition
 
import de.laser.utils.DateUtils
import de.laser.annotations.DebugInfo
import de.laser.helper.Profiler
import de.laser.storage.RDStore
import de.laser.interfaces.CalculatedType
import de.laser.properties.PropertyDefinitionGroup
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent
import grails.web.servlet.mvc.GrailsParameterMap
import org.codehaus.groovy.runtime.InvokerHelper

import java.text.SimpleDateFormat

/**
 * This controller is responsible for the license related calls
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class LicenseController {

    AccessService accessService
    AddressbookService addressbookService
    ContextService contextService
    CopyElementsService copyElementsService
    CustomWkhtmltoxService wkhtmltoxService
    DeletionService deletionService
    DocstoreService docstoreService
    EscapeService escapeService
    ExecutorWrapperService executorWrapperService
    FilterService filterService
    FormService formService
    GenericOIDService genericOIDService
    InstitutionsService institutionsService
    LicenseControllerService licenseControllerService
    LinksGenerationService linksGenerationService
    LicenseService licenseService
    OrgTypeService orgTypeService
    PendingChangeService pendingChangeService
    PropertyService propertyService
    SubscriptionsQueryService subscriptionsQueryService
    SubscriptionService subscriptionService
    TaskService taskService

    //----------------------------------------- general or ungroupable section ----------------------------------------

    /**
     * Shows the given license
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def show() {

        Profiler prf = new Profiler()
        prf.setBenchmark('this-n-that')

        log.debug("license: ${params}");
        Map<String,Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        //used for showing/hiding the License Actions menus
        List<Role> admin_role = Role.findAllByAuthority("INST_ADM")
        result.canCopyOrgs = UserOrg.executeQuery("select uo.org from UserOrg uo where uo.user=(:user) and uo.formalRole=(:role) ", [user: result.user, role: admin_role])

        // ---- pendingChanges : start

        prf.setBenchmark('pending changes')

        if (executorWrapperService.hasRunningProcess(result.license)) {
            log.debug("PendingChange processing in progress")
            result.processingpc = true
        }
        else {
            List<PendingChange> pendingChanges = PendingChange.executeQuery("" +
                    "select pc from PendingChange as pc where license = :lic and ( pc.status is null or pc.status = :status ) order by pc.ts desc",
                    [lic: result.license, status: RDStore.PENDING_CHANGE_PENDING]
            )

            log.debug("pc result is ${result.pendingChanges}");

            if (result.license.isSlaved && ! pendingChanges.isEmpty()) {
                log.debug("Slaved lincence, auto-accept pending changes")
                List changesDesc = []
                pendingChanges.each { change ->
                    if (!pendingChangeService.performAccept(change)) {
                        log.debug("Auto-accepting pending change has failed.")
                    } else {
                        changesDesc.add(change.desc)
                    }
                }
                flash.message = changesDesc
            } else {
                result.pendingChanges = pendingChanges
            }
        }

        // ---- pendingChanges : end

        prf.setBenchmark('tasks')

            // tasks
            result.tasks = taskService.getTasksByResponsiblesAndObject(result.user, result.institution, result.license)
            Map<String, Object> preCon = taskService.getPreconditionsWithoutTargets(result.institution)
            result << preCon

            String i10value = LocaleUtils.getLocalizedAttributeName('value')
            // restrict visible for templates/links/orgLinksAsList
            result.visibleOrgRelations = OrgRole.executeQuery(
                    "select oo from OrgRole oo where oo.lic = :license and oo.org != :context and oo.roleType not in (:roleTypes) order by oo.roleType." + i10value + " asc, oo.org.sortname asc, oo.org.name asc",
                    [license:result.license,context:result.institution,roleTypes:[RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS]]
            )

        prf.setBenchmark('properties')

            // -- private properties

            result.authorizedOrgs = result.user.getAffiliationOrgs()

            // create mandatory LicensePrivateProperties if not existing

            List<PropertyDefinition> mandatories = PropertyDefinition.getAllByDescrAndMandatoryAndTenant(PropertyDefinition.LIC_PROP, true, result.institution)

            mandatories.each { pd ->
                //TODO [ticket=2436]
                if (!LicenseProperty.findWhere(owner: result.license, type: pd, tenant: result.institution, isPublic: false)) {
                    def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, result.license, pd, result.institution)

                    if (newProp.hasErrors()) {
                        log.error(newProp.errors.toString())
                    } else {
                        log.debug("New license private property created via mandatory: ${newProp.type.name}")
                    }
                }
            }

            if(result.license._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL) {
                prf.setBenchmark('non-inherited member properties')
                Set<License> childLics = result.license.getDerivedLicenses()
                if(childLics) {
                    String localizedName = LocaleUtils.getLocalizedAttributeName('name')
                    String query = "select lp.type from LicenseProperty lp where lp.owner in (:licenseSet) and lp.instanceOf = null and lp.tenant = :context order by lp.type.${localizedName} asc"
                    Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery( query, [licenseSet:childLics,context:result.institution] )
                    result.memberProperties = memberProperties
                }
            }

            prf.setBenchmark('links')

            if(params.export)
                result.links = linksGenerationService.getSourcesAndDestinations(result.license, result.user, RefdataCategory.getAllRefdataValues(RDConstants.LINK_TYPE)-RDStore.LINKTYPE_LICENSE)
            else result.links = linksGenerationService.getSourcesAndDestinations(result.license,result.user)

            // -- private properties

            result.modalPrsLinkRole = RDStore.PRS_RESP_SPEC_LIC_EDITOR
            result.modalVisiblePersons = addressbookService.getPrivatePersonsByTenant(result.institution)

            result.visiblePrsLinks = []

            result.license.prsLinks.each { pl ->
                if (!result.visiblePrsLinks.contains(pl.prs)) {
                    if (pl.prs.isPublic) {
                        result.visiblePrsLinks << pl
                    } else {
                        // nasty lazy loading fix
                        result.user.getAffiliationOrgs().each { ao ->
                            if (ao.getId() == pl.prs.tenant.getId()) {
                                result.visiblePrsLinks << pl
                            }
                        }
                    }
                }
            }

        prf.setBenchmark('licensor filter')


        //a new query builder service for selection lists has been introduced
        //result.availableSubs = controlledListService.getSubscriptions(params+[status:SUBSCRIPTION_CURRENT]).results
        //result.availableSubs = []

        result.availableLicensorList = orgTypeService.getOrgsForTypeLicensor().minus(result.visibleOrgRelations.collect { OrgRole oo -> oo.org })
                /*OrgRole.executeQuery(
                        "select o from OrgRole oo join oo.org o where oo.lic.id = :lic and oo.roleType.value = 'Licensor'",
                        [lic: result.license.id]
                )*/
        result.existingLicensorIdList = []

        List bm = prf.stopBenchmark()
        result.benchMark = bm
        if(params.export) {
            result.availablePropDefGroups = PropertyDefinitionGroup.getAvailableGroups(result.institution, License.class.name)
            result.allPropDefGroups = result.license.getCalculatedPropDefGroups(result.institution)
            result.prop_desc = PropertyDefinition.LIC_PROP
            result.memberLicenses = License.findAllByInstanceOf(result.license)
            result.linkedSubscriptions = Subscription.executeQuery('select sub from Links li join li.destinationSubscription sub join sub.orgRelations oo where li.sourceLicense = :lic and li.linkType = :linkType and sub.status = :current and oo.org = :context', [lic: result.license, linkType: RDStore.LINKTYPE_LICENSE, current: RDStore.SUBSCRIPTION_CURRENT, context: result.institution])
            result.entry = result.license
            result.tasks = taskService.getTasksForExport((User) result.user, (Org) result.institution, (License) result.license)
            result.documents = docstoreService.getDocumentsForExport((Org) result.institution, (License) result.license)
            result.notes = docstoreService.getNotesForExport((Org) result.institution, (License) result.license)

            Map<String, Object> pageStruct = [
                    width       : 85,
                    height      : 35,
                    pageSize    : 'A4',
                    orientation : 'Portrait'
            ]
            result.struct = [pageStruct.width, pageStruct.height, pageStruct.pageSize + ' ' + pageStruct.orientation]
            byte[] pdf = wkhtmltoxService.makePdf(
                    view: '/license/licensePdf',
                    model: result,
                    pageSize: pageStruct.pageSize,
                    orientation: pageStruct.orientation,
                    marginLeft: 10,
                    marginRight: 10,
                    marginTop: 15,
                    marginBottom: 15
            )
            response.setHeader('Content-disposition', 'attachment; filename="'+ escapeService.escapeString(result.license.dropdownNamingConvention()) +'.pdf"')
            response.setContentType('application/pdf')
            response.outputStream.withStream { it << pdf }
        }
        else result
  }

    /**
     * Gets the tasks connected to this license
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def tasks() {
        Map<String,Object> ctrlResult = licenseControllerService.tasks(this,params)
        if(ctrlResult.error == LicenseControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
            }
        }
        else {
            flash.message = ctrlResult.result.message
        }
        ctrlResult.result
    }

    /**
     * Call to delete the given license; a parameter specifies whether the deletion should be executed or not
     * @return the view showing the attached object to the given license
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        Map<String,Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, AccessService.CHECK_EDIT)

        if (params.process && result.editable) {
            result.delResult = deletionService.deleteLicense(result.license, false)
        }
        else {
            result.delResult = deletionService.deleteLicense(result.license, DeletionService.DRY_RUN)
        }

        result
    }

    /**
     * Creates a new member license to the given consortial license if it not exists
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDTIOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processAddMembers() {
        log.debug( params.toMapString() )

        Map<String,Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, AccessService.CHECK_VIEW_AND_EDIT)
        if (!result) {
            response.sendError(401); return
        }
        result.institution = contextService.getOrg()

        License licenseCopy
            if (accessService.checkPerm(" ORG_CONSORTIUM")) {

                if (params.cmd == 'generate') {
                    licenseCopy = institutionsService.copyLicense(
                            result.license, [
                                lic_name: "${result.license.reference} (Teilnehmervertrag)",
                                isSlaved: "true",
                                copyStartEnd: true
                            ],
                            InstitutionsService.CUSTOM_PROPERTIES_ONLY_INHERITED)
                }
            }
        if(licenseCopy) {
            redirect action: 'show', params: [id: licenseCopy.id]
            return
        }
        else {
            redirect action: 'show', params: [id: result.license?.id]
            return
        }
    }

    /**
     * Processes a linking between one or more subscriptions. Depending on the call level,
     * the action redirects to the appropriate table
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
  def linkToSubscription(){
        log.debug("linkToSubscription :: ${params}")
        Map<String,Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, AccessService.CHECK_VIEW_AND_EDIT)
        result.tableConfig = ['showLinking','onlyMemberSubs']
        Set<Subscription> allSubscriptions = []
        String action
        if(result.license.instanceOf) {
            result.putAll(subscriptionService.getMySubscriptionsForConsortia(params, result.user, result.institution, result.tableConfig))
            allSubscriptions.addAll(result.entries.collect { row -> (Subscription) row[0] })
            result.allSubscriptions = allSubscriptions
            action = 'linkMemberLicensesToSubs'
        }
        else {
            result.putAll(subscriptionService.getMySubscriptions(params, result.user, result.institution))
            allSubscriptions.addAll(result.allSubscriptions)
            action = 'linkLicenseToSubs'
        }
        if(formService.validateToken(params)) {
            License newLicense = (License) result.license
            boolean unlink = params.unlink == 'true'
            if(params.subscription == "all") {
                allSubscriptions.each { s->
                    subscriptionService.setOrgLicRole(s,newLicense,unlink)
                }
            }
            else {
                try {
                    subscriptionService.setOrgLicRole(Subscription.get(Long.parseLong(params.subscription)),newLicense,unlink)
                }
                catch (NumberFormatException e) {
                    log.error("Invalid identifier supplied!")
                }
            }
            params.remove("unlink")
            //result.linkedSubscriptions = Links.findAllBySourceAndLinkType(genericOIDService.getOID(result.license),RDStore.LINKTYPE_LICENSE).collect { Links l -> genericOIDService.resolveOID(l.destination) }
        }

        redirect action: action, params: params
  }

    /**
     * Opens possible subscriptions to link to the given license; the parent level is being considered
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    Map<String,Object> linkLicenseToSubs() {
        Map<String, Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, AccessService.CHECK_VIEW_AND_EDIT)
        result.putAll(subscriptionService.getMySubscriptions(params,result.user,result.institution))
        result.tableConfig = ['showLinking']
        result.linkedSubscriptions = Links.executeQuery('select l.destinationSubscription from Links l where l.sourceLicense = :license and l.linkType = :linkType',[license:result.license,linkType:RDStore.LINKTYPE_LICENSE])
        result
    }

    /**
     * Shows all subscriptions linked to the current license
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def linkedSubs() {
        Map<String,Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result.subscriptions = []
        result.putAll(_setSubscriptionFilterData())
        result.subscriptionsForFilter = []
        if(params.status != 'FETCH_ALL') {
            result.subscriptionsForFilter.addAll(Subscription.executeQuery("select l.destinationSubscription from Links l join l.destinationSubscription s where s.status.id = :status and l.sourceLicense = :lic and l.linkType = :linkType" , [status:params.status as Long, lic:result.license, linkType:RDStore.LINKTYPE_LICENSE] ))
        }
        else if(params.status == 'FETCH_ALL') {
            result.subscriptionsForFilter.addAll(Subscription.executeQuery("select l.destinationSubscription from Links l where l.sourceLicense = :lic and l.linkType = :linkType" , [lic:result.license, linkType:RDStore.LINKTYPE_LICENSE] ))
        }
        if(result.license._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && result.license.getLicensingConsortium().id == result.institution.id) {
            Set<RefdataValue> subscriberRoleTypes = [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]
            Map<String,Object> queryParams = [lic:result.license, subscriberRoleTypes:subscriberRoleTypes, linkType:RDStore.LINKTYPE_LICENSE]
            String whereClause = ""
            if(params.status != 'FETCH_ALL') {
                whereClause += " and s.status.id = :status"
                queryParams.status = params.status as Long
            }
            if(result.validOn) {
                whereClause += " and ( ( s.startDate is null or s.startDate >= :validOn ) and ( s.endDate is null or s.endDate <= :validOn ) )"
                queryParams.validOn = result.validOn
            }
            result.consAtMember = true
            result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())
            String query = "select l.destinationSubscription from Links l join l.destinationSubscription s join s.orgRelations oo where l.sourceLicense = :lic and l.linkType = :linkType and oo.roleType in :subscriberRoleTypes ${whereClause} order by oo.org.sortname asc, oo.org.name asc, s.name asc, s.startDate asc, s.endDate asc"
            result.validSubChilds = Subscription.executeQuery( query, queryParams )
            ArrayList<Long> filteredOrgIds = _getOrgIdsForFilter()

            result.validSubChilds.each { sub ->
                List<Org> subscr = sub.getAllSubscribers()
                def filteredSubscr = []
                subscr.each { Org subOrg ->
                    if (filteredOrgIds.contains(subOrg.id)) {
                        filteredSubscr << subOrg
                    }
                }
                if (filteredSubscr) {
                    if(params.list("subscription").contains(sub.id) || !params.list("subscription")) {
                        if (params.subRunTimeMultiYear || params.subRunTime) {

                            if (params.subRunTimeMultiYear && !params.subRunTime) {
                                if(sub.isMultiYear) {
                                    result.subscriptions << [sub: sub, orgs: filteredSubscr]
                                }
                            }else if (!params.subRunTimeMultiYear && params.subRunTime){
                                if(!sub.isMultiYear) {
                                    result.subscriptions << [sub: sub, orgs: filteredSubscr]
                                }
                            }
                            else {
                                result.subscriptions << [sub: sub, orgs: filteredSubscr]
                            }
                        }
                        else {
                            result.subscriptions << [sub: sub, orgs: filteredSubscr]
                        }
                    }
                }
            }
        }
        else {
            params.license = params.id
            List tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.getOrg())
            Set<Subscription> subscriptions = Subscription.executeQuery( "select s " + tmpQ[0], tmpQ[1] )
            if(params.subscription) {
                result.subscriptions = []
                List subIds = params.list("subscription")
                subIds.each { subId ->
                    result.subscriptions << subscriptions.find { Subscription s -> s.id == Long.parseLong(subId) }
                }
            }
            else result.subscriptions = subscriptions

            result.consAtMember = false
        }

        result
    }

    /**
     * Lists the member licenses to the given consortial license
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def members() {
        log.debug("license id:${params.id}");

        Map<String,Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result.putAll(_setSubscriptionFilterData())
        Set<License> validMemberLicenses = License.findAllByInstanceOf(result.license)
        Set<Map<String,Object>> filteredMemberLicenses = []
        validMemberLicenses.each { License memberLicense ->
            //memberLicense.getAllLicensee().sort{ Org a, Org b -> a.sortname <=> b.sortname }.each { Org org ->
            //if(org.id in filteredOrgIds) {
            String dateFilter = ""
            Map<String,Object> subQueryParams = [lic:memberLicense, linkType:RDStore.LINKTYPE_LICENSE]
            if(params.validOn) {
                dateFilter += " and ((s.startDate = null or s.startDate <= :validOn) and (s.endDate = null or s.endDate >= :validOn))"
                subQueryParams.validOn = result.dateRestriction
            }
            Set<Subscription> subscriptions = Subscription.executeQuery("select l.destinationSubscription from Links l join l.destinationSubscription s where l.sourceLicense = :lic and l.linkType = :linkType"+dateFilter,subQueryParams)
            if(params.status != 'FETCH_ALL') {
                subscriptions.removeAll { Subscription s -> s.status.id != params.status as Long }
            }
            if (params.subRunTimeMultiYear || params.subRunTime) {
                if (params.subRunTimeMultiYear && !params.subRunTime) {
                    subscriptions = subscriptions.findAll{ Subscription s -> s.isMultiYear}
                }else if (!params.subRunTimeMultiYear && params.subRunTime){
                    subscriptions = subscriptions.findAll{ Subscription s -> !s.isMultiYear}
                }
            }
            if(params.subscription) {
                List<String> subFilter = params.list("subscription")
                subscriptions.removeAll { Subscription s -> !subFilter.contains(s.id.toString()) }
            }
            filteredMemberLicenses << [license:memberLicense,subs:subscriptions.size()]
            //}
            //}
        }
        String subQuery = "select l.destinationSubscription from Links l join l.destinationSubscription s where l.sourceLicense in (:licenses) and l.linkType = :linkType"
        if(params.status == "FETCH_ALL" && validMemberLicenses)
            result.subscriptionsForFilter = Subscription.executeQuery(subQuery,[linkType:RDStore.LINKTYPE_LICENSE,licenses:validMemberLicenses])
        else if(validMemberLicenses) {
            result.subscriptionsForFilter = Subscription.executeQuery(subQuery+" and s.status = :status",[linkType:RDStore.LINKTYPE_LICENSE, licenses:validMemberLicenses, status:RefdataValue.get(params.status as Long)])
        }
        result.validMemberLicenses = filteredMemberLicenses
        result
    }

    /**
     * Opens possible subscriptions to link to the given license; the member level is being considered
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def linkMemberLicensesToSubs() {
        Map<String,Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, AccessService.CHECK_VIEW_AND_EDIT)
        result.tableConfig = ['onlyMemberSubs']
        result.linkedSubscriptions = Links.executeQuery('select li.destinationSubscription from Links li where li.sourceLicense = :license and li.linkType = :linkType',[license:result.license,linkType:RDStore.LINKTYPE_LICENSE])
        result.putAll(subscriptionService.getMySubscriptionsForConsortia(params,result.user,result.institution,result.tableConfig))
        result
    }

    /**
     * this is very ugly and should be subject of refactor - - but unfortunately, the
     * {@link SubscriptionsQueryService#myInstitutionCurrentSubscriptionsBaseQuery(java.lang.Object, de.laser.Org)}
     * requires the {@link GrailsParameterMap} as parameter.
     * @return validOn and defaultSet-parameters of the filter
     */
    private Map<String,Object> _setSubscriptionFilterData() {
        Map<String, Object> result = [:]
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Date dateRestriction = null
        if (params.validOn == null || params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            dateRestriction = sdf.parse(params.validOn)
        }
        result.dateRestriction = dateRestriction
        if (! params.status) {
            if (!params.filterSet) {
                params.status = RDStore.SUBSCRIPTION_CURRENT.id
                result.defaultSet = true
            }
            else {
                params.status = 'FETCH_ALL'
            }
        }
        result
    }

    /**
     * Gets the linked consortia member institution IDs for filter views
     * @return a {@link List} of institution IDs
     */
    private ArrayList<Long> _getOrgIdsForFilter() {
        Map<String,Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, accessService.CHECK_VIEW)
        GrailsParameterMap tmpParams = (GrailsParameterMap) params.clone()
        tmpParams.remove("max")
        tmpParams.remove("offset")
        if (accessService.checkPerm("ORG_CONSORTIUM"))
            tmpParams.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
        Map<String,Object> fsq = filterService.getOrgComboQuery(tmpParams, result.institution)

        if (tmpParams.filterPropDef) {
            fsq = propertyService.evalFilterQuery(tmpParams, fsq.query, 'o', fsq.queryParams)
        }
        fsq.query = fsq.query.replaceFirst("select o from ", "select o.id from ")
        Org.executeQuery(fsq.query, fsq.queryParams, tmpParams)
    }

    @Deprecated
    @DebugInfo(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def pendingChanges() {
        log.debug("license id:${params.id}");

        Map<String,Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        def validMemberLicenses = License.where {
            instanceOf == result.license
        }

        result.pendingChanges = [:]

        validMemberLicenses.each{ member ->

            if (executorWrapperService.hasRunningProcess(member)) {
                log.debug("PendingChange processing in progress")
                result.processingpc = true
            }
            else {
                List<PendingChange> pendingChanges = PendingChange.executeQuery(
                        "select pc from PendingChange as pc where license.id = :licId and ( pc.status is null or pc.status = :status ) order by pc.ts desc",
                        [licId: member.id, status: RDStore.PENDING_CHANGE_PENDING]
                )

                result.pendingChanges << ["${member.id}": pendingChanges]
            }
        }
        result
    }

    @Deprecated
    @DebugInfo(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def history() {
        log.debug("license::history : ${params}");

        Map<String,Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        // postgresql migration
        String subQuery = 'select cast(lp.id as string) from LicenseProperty as lp where lp.owner = :owner'
        List subQueryResult = LicenseProperty.executeQuery(subQuery, [owner: result.license])

        String base_query = "select e from AuditLogEvent as e where ( (className=:licClass and persistedObjectId = cast(:licId as string))"
        Map<String, Object> query_params = [licClass:result.license.class.name, licId:"${result.license.id}"]

        // postgresql migration
        if (subQueryResult) {
            base_query += ' or (className = :prop and persistedObjectId in (:subQueryResult)) ) order by e.dateCreated desc'
            query_params.'prop' = LicenseProperty.class.name
            query_params.'subQueryResult' = subQueryResult
        }
        else {
            base_query += ') order by e.dateCreated desc'
        }

        result.historyLines = AuditLogEvent.executeQuery(
                base_query, query_params, [max:result.max, offset:result.offset]
        )

    String propertyNameHql = "select pd.name from LicenseProperty as licP, PropertyDefinition as pd where licP.id = :lpid and licP.type = pd"
    
    result.historyLines?.each{
      if(it.className == query_params.prop ){
        def propertyName = LicenseProperty.executeQuery(propertyNameHql, [lpid: it.persistedObjectId.toLong()])[0]
        it.propertyName = propertyName
      }
    }

    result.historyLinesTotal = AuditLogEvent.executeQuery(base_query, query_params).size()
    result
  }

    /**
     * Should enumerate the changes done on the given license, function currently undetermined
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def changes() {
        log.debug("license::changes : ${params}")

        Map<String,Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        String baseQuery = "select pc from PendingChange as pc where pc.license = :lic and pc.status.value in (:stats)"
        Map<String, Object> baseParams = [lic: result.license, stats: ['Accepted', 'Rejected']]

        result.todoHistoryLines = PendingChange.executeQuery(
                baseQuery + " order by pc.ts desc",
                baseParams,
                [max: result.max, offset: result.offset]
        )

        result.todoHistoryLinesTotal = PendingChange.executeQuery(
                baseQuery,
                baseParams
        )[0] ?: 0

        result
    }

    /**
     * Opens the notes view for the given license
     * @return a {@link List} of notes ({@link Doc})
     * @see Doc
     * @see DocContext
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def notes() {
        Map<String,Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result
    }

    /**
     * Opens the documents view for the given license
     * @return a {@link List} of {@link Doc}s (except notes) linked to the given license
     * @see Doc
     * @see DocContext
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def documents() {
        Map<String,Object> result = licenseControllerService.getResultGenericsAndCheckAccess(this, params, AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result
    }

    /**
     * Call to delete the given document
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def deleteDocuments() {
        log.debug("deleteDocuments ${params}")

        docstoreService.unifiedDeleteDocuments(params)

        redirect controller: 'license', action:params.redirectAction, id:params.instanceId /*, fragment:'docstab' */
    }

    /**
     * Entry point for copying a license
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def copyLicense() {
        Map<String,Object> result = [:]
        result.user = contextService.getUser()
        result.contextOrg = contextService.getOrg()
        flash.error = ""
        flash.message = ""
        if (params.sourceObjectId == "null") params.remove("sourceObjectId")
        result.sourceObjectId = params.sourceObjectId
        result.sourceObject = genericOIDService.resolveOID(params.sourceObjectId)

        if (params.targetObjectId == "null") params.remove("targetObjectId")
        if (params.targetObjectId) {
            result.targetObjectId = params.targetObjectId
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }


        result.showConsortiaFunctions = licenseControllerService.showConsortiaFunctions(result.sourceObject)
        result.consortialView = result.showConsortiaFunctions

        result.editable = result.sourceObject?.isEditableBy(result.user)

        if (!result.editable) {
            response.sendError(401); return
        }

        result.isConsortialObjects = (result.sourceObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL)
        result.copyObject = true

        if (params.name && !result.targetObject) {
            String lic_name = params.name ?: "Kopie von ${result.sourceObject.reference}"

            License.withTransaction {
                Object targetObject = new License(
                        reference: lic_name,
                        status: RDStore.LICENSE_NO_STATUS,
                        openEnded: result.sourceObject.openEnded)

                //Copy InstanceOf
                if (params.targetObject?.copylinktoLicense) {
                    targetObject.instanceOf = result.sourceObject.instanceOf ?: null
                }


                if (!targetObject.save()) {
                    log.error("Problem saving license ${targetObject.errors}");
                } else {
                    result.targetObject = targetObject
                    params.targetObjectId = genericOIDService.getOID(targetObject)

                    //Copy References
                    result.sourceObject.orgRelations.each { OrgRole or ->
                        if ((or.org.id == result.contextOrg.id) || (or.roleType.id in [RDStore.OR_LICENSEE.id, RDStore.OR_LICENSEE_CONS.id])) {
                            OrgRole newOrgRole = new OrgRole()
                            InvokerHelper.setProperties(newOrgRole, or.properties)
                            newOrgRole.lic = result.targetObject
                            newOrgRole.save()
                        }
                    }

                }
            }
        }

        switch (params.workFlowPart) {
            case CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS:
                result << copyElementsService.copyObjectElements_DatesOwnerRelations(params)
                if(result.targetObject) {
                    params.workFlowPart = CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
                }
                result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                break
            case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                result << copyElementsService.copyObjectElements_DocsAnnouncementsTasks(params)
                params.workFlowPart = CopyElementsService.WORKFLOW_PROPERTIES
                result << copyElementsService.loadDataFor_Properties(params)
                break
            case CopyElementsService.WORKFLOW_END:
                result << copyElementsService.copyObjectElements_Properties(params)
                if (result.targetObject){
                    redirect controller: 'license', action: 'show', params: [id: result.targetObject.id]
                    return
                }
                break
            default:
                result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                break
        }

        result.workFlowPart = params.workFlowPart ?: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS

        result

    }

    /**
     * Controller menu for copying components of the given license into another license
     */
    @DebugInfo(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def copyElementsIntoLicense() {
        def result             = [:]
        result.user            = contextService.getUser()
        result.institution     = contextService.getOrg()
        result.contextOrg      = result.institution

        flash.error = ""
        flash.message = ""
        if (params.sourceObjectId == "null") params.remove("sourceObjectId")
        result.sourceObjectId = params.sourceObjectId ?: params.id
        result.sourceObject = genericOIDService.resolveOID(params.sourceObjectId)

        if (params.targetObjectId == "null") params.remove("targetObjectId")
        if (params.targetObjectId) {
            result.targetObjectId = params.targetObjectId
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        result.editable = result.sourceObject.isEditableBy(result.user)

        if (!result.editable) {
            response.sendError(401); return
        }

        result.isConsortialObjects = (result.sourceObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL && result.targetObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL) ?: false

        result.allObjects_readRights = licenseService.getMyLicenses_readRights([status: RDStore.LICENSE_CURRENT.id])
        result.allObjects_writeRights = licenseService.getMyLicenses_writeRights([status: RDStore.LICENSE_CURRENT.id])

        List<String> licTypSubscriberVisible = [CalculatedType.TYPE_CONSORTIAL,
                                                CalculatedType.TYPE_ADMINISTRATIVE]
        result.isSubscriberVisible =
                result.sourceObject &&
                        result.targetObject &&
                        licTypSubscriberVisible.contains(result.sourceObject._getCalculatedType()) &&
                        licTypSubscriberVisible.contains(result.targetObject._getCalculatedType())

        switch (params.workFlowPart) {
            case CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS:
                result << copyElementsService.copyObjectElements_DatesOwnerRelations(params)
                result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                break
            case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                result << copyElementsService.copyObjectElements_DocsAnnouncementsTasks(params)
                result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                break
            case CopyElementsService.WORKFLOW_SUBSCRIBER:
                result << copyElementsService.copyObjectElements_Subscriber(params)
                result << copyElementsService.loadDataFor_Subscriber(params)
                break
            case CopyElementsService.WORKFLOW_PROPERTIES:
                result << copyElementsService.copyObjectElements_Properties(params)
                result << copyElementsService.loadDataFor_Properties(params)
                break
            case CopyElementsService.WORKFLOW_END:
                result << copyElementsService.copyObjectElements_Properties(params)
                if (params.targetObjectId){
                    flash.error = ""
                    flash.message = ""
                    redirect controller: 'license', action: 'show', params: [id: genericOIDService.resolveOID(params.targetObjectId).id]
                    return
                }
                break
            default:
                result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                break
        }

        if (params.targetObjectId) {
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }
        result.workFlowPart = params.workFlowPart ?: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS
        result.workFlowPartNext = params.workFlowPartNext ?: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS

        result
    }

}
