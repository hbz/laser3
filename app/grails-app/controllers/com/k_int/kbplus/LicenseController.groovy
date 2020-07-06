package com.k_int.kbplus

import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.kbplus.traits.PendingChangeControllerTrait
import com.k_int.properties.PropertyDefinition
import de.laser.AccessService
import de.laser.DeletionService
import de.laser.LinksGenerationService
import de.laser.PropertyService
import de.laser.SubscriptionsQueryService
import de.laser.controller.AbstractDebugController
import de.laser.helper.DateUtil
import de.laser.helper.DebugAnnotation
import de.laser.helper.DebugUtil
import de.laser.helper.RDStore
import de.laser.interfaces.CalculatedType
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

import static de.laser.helper.RDStore.*

@Secured(['IS_AUTHENTICATED_FULLY'])
class LicenseController
        extends AbstractDebugController
        implements PendingChangeControllerTrait {

    def springSecurityService
    def taskService
    def docstoreService
    def genericOIDService
    def exportService
    def escapeService
    PropertyService propertyService
    def institutionsService
    def pendingChangeService
    def executorWrapperService
    def accessService
    def contextService
    def addressbookService
    def filterService
    def orgTypeService
    def deletionService
    def subscriptionService
    SubscriptionsQueryService subscriptionsQueryService
    LinksGenerationService linksGenerationService

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def show() {

        DebugUtil du = new DebugUtil()
        du.setBenchmark('this-n-that')

        log.debug("license: ${params}");
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        //used for showing/hiding the License Actions menus
        def admin_role = Role.findAllByAuthority("INST_ADM")
        result.canCopyOrgs = UserOrg.executeQuery("select uo.org from UserOrg uo where uo.user=(:user) and uo.formalRole=(:role) and uo.status in (:status)", [user: result.user, role: admin_role, status: [1, 3]])

        //def license_reference_str = result.license.reference ?: 'NO_LIC_REF_FOR_ID_' + params.id

        //String filename = "license_${escapeService.escapeString(license_reference_str)}"
        //result.onixplLicense = result.license.onixplLicense

        // ---- pendingChanges : start

        du.setBenchmark('pending changes')

        if (executorWrapperService.hasRunningProcess(result.license)) {
            log.debug("PendingChange processing in progress")
            result.processingpc = true
        } else {

            List<PendingChange> pendingChanges = PendingChange.executeQuery("select pc from PendingChange as pc where license=? and ( pc.status is null or pc.status = ? ) order by pc.ts desc", [result.license, PENDING_CHANGE_PENDING])

            log.debug("pc result is ${result.pendingChanges}");
            // refactoring: replace link table with instanceOf
            // if (result.license.incomingLinks.find { it?.isSlaved?.value == "Yes" } && pendingChanges) {

            if (result.license.isSlaved && ! pendingChanges.isEmpty()) {
                log.debug("Slaved lincence, auto-accept pending changes")
                def changesDesc = []
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

        //result.availableSubs = getAvailableSubscriptions(result.license, result.user)

        du.setBenchmark('tasks')

        // TODO: experimental asynchronous task
        //def task_tasks = task {

            // tasks
            result.tasks = taskService.getTasksByResponsiblesAndObject(result.user, result.institution, result.license)
            def preCon = taskService.getPreconditionsWithoutTargets(result.institution)
            result << preCon

            String i10value = LocaleContextHolder.getLocale().getLanguage() == Locale.GERMAN.getLanguage() ? 'value_de' : 'value_en'
            // restrict visible for templates/links/orgLinksAsList
            result.visibleOrgLinks = OrgRole.executeQuery("select oo from OrgRole oo where oo.lic = :license and oo.org != :context and oo.roleType not in (:roleTypes) order by oo.roleType.${i10value} asc, oo.org.sortname asc, oo.org.name asc",[license:result.license,context:result.institution,roleTypes:[OR_LICENSEE, OR_LICENSEE_CONS, OR_LICENSING_CONSORTIUM]])

            /*result.license.orgLinks?.each { or ->
                if (!(or.org.id == result.institution.id) && !(or.roleType in [RDStore.OR_LICENSEE, RDStore.OR_LICENSING_CONSORTIUM])) {
                    result.visibleOrgLinks << or
                }
            }*/
            //result.visibleOrgLinks.sort { it.org.sortname }
        //}

        du.setBenchmark('properties')

        // TODO: experimental asynchronous task
        //def task_properties = task {

            // -- private properties

            result.authorizedOrgs = result.user.authorizedOrgs

            // create mandatory LicensePrivateProperties if not existing

            List<PropertyDefinition> mandatories = PropertyDefinition.getAllByDescrAndMandatoryAndTenant(PropertyDefinition.LIC_PROP, true, result.institution)

            mandatories.each { pd ->
                //TODO [ticket=2436]
                if (!LicenseProperty.findWhere(owner: result.license, type: pd, tenant: result.institution, isPublic: false)) {
                    def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, result.license, pd, result.institution)

                    if (newProp.hasErrors()) {
                        log.error(newProp.errors)
                    } else {
                        log.debug("New license private property created via mandatory: ${newProp.type.name}")
                    }
                }
            }

            if(result.license.getCalculatedType() == CalculatedType.TYPE_CONSORTIAL) {
                du.setBenchmark('non-inherited member properties')
                Set<License> childLics = result.license.getDerivedLicenses()
                if(childLics) {
                    String localizedName
                    switch(LocaleContextHolder.getLocale()) {
                        case Locale.GERMANY:
                        case Locale.GERMAN: localizedName = "name_de"
                            break
                        default: localizedName = "name_en"
                            break
                    }
                    Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery("select lp.type from LicenseProperty lp where lp.owner in (:licenseSet) and lp.instanceOf = null order by lp.type.${localizedName} asc",[licenseSet:childLics])
                    result.memberProperties = memberProperties
                }
            }

            du.setBenchmark('links')

            result.links = linksGenerationService.getSourcesAndDestinations(result.license,result.user)

            // -- private properties

            result.modalPrsLinkRole = PRS_RESP_SPEC_LIC_EDITOR
            result.modalVisiblePersons = addressbookService.getPrivatePersonsByTenant(result.institution)

            result.visiblePrsLinks = []

            result.license.prsLinks.each { pl ->
                if (!result.visiblePrsLinks.contains(pl.prs)) {
                    if (pl.prs.isPublic) {
                        result.visiblePrsLinks << pl
                    } else {
                        // nasty lazy loading fix
                        result.user.authorizedOrgs.each { ao ->
                            if (ao.getId() == pl.prs.tenant.getId()) {
                                result.visiblePrsLinks << pl
                            }
                        }
                    }
                }
            }
        //}

        du.setBenchmark('licensor filter')

        // TODO: experimental asynchronous task
        //def task_licensorFilter = task {

        //a new query builder service for selection lists has been introduced
        //result.availableSubs = controlledListService.getSubscriptions(params+[status:SUBSCRIPTION_CURRENT]).results
        //result.availableSubs = []

        result.availableLicensorList = orgTypeService.getOrgsForTypeLicensor().minus(result.visibleOrgLinks.collect { OrgRole oo -> oo.org })
                /*OrgRole.executeQuery(
                        "select o from OrgRole oo join oo.org o where oo.lic.id = :lic and oo.roleType.value = 'Licensor'",
                        [lic: result.license.id]
                )*/
        result.existingLicensorIdList = []
        // performance problems: orgTypeService.getCurrentLicensors(contextService.getOrg()).collect { it -> it.id }
       // }

        List bm = du.stopBenchmark()
        result.benchMark = bm

        // TODO: experimental asynchronous task
        //waitAll(task_tasks, task_properties)
        result
        /*withFormat {
        html result

      /*json   def map = exportService.addLicensesToMap([:], [result.license])

        def json = map as JSON
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}.json\"")
        response.contentType = "application/json"
        render json.toString()
      }
      xml {
        def doc = exportService.buildDocXML("Licenses")

        exportService.addLicensesIntoXML(doc, doc.getDocumentElement(), [result.license])

        response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xml\"")
        response.contentTypexml"
        exportService.streamOutXML(doc, response.outputStream)
      }
      /*
      csv {
          response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
          response.contentType = "text/csv"
          ServletOutputStream out = response.outputStream
          //exportService.StreamOutLicenseCSV(out,null,[result.license])
          out.close()

      }
    }
    */
  }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_EDIT)

        if (params.process && result.editable) {
            result.delResult = deletionService.deleteLicense(result.license, false)
        }
        else {
            result.delResult = deletionService.deleteLicense(result.license, DeletionService.DRY_RUN)
        }

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDTIOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def processAddMembers() {
        log.debug(params)

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        if (!result) {
            response.sendError(401); return
        }
        result.institution = contextService.getOrg()

        // TODO: not longer used? -> remove and refactor params
        //RefdataValue role_lic      = OR_LICENSEE_CONS
        //RefdataValue role_lic_cons = OR_LICENSING_CONSORTIUM
        //if(accessService.checkPerm("ORG_INST_COLLECTIVE"))
        //    role_lic = OR_LICENSEE_COLL

        License licenseCopy
            if (accessService.checkPerm("ORG_INST_COLLECTIVE, ORG_CONSORTIUM")) {

                if (params.cmd == 'generate') {
                    licenseCopy = institutionsService.copyLicense(
                            result.license, [
                                lic_name: "${result.license.reference} (Teilnehmervertrag)",
                                isSlaved: "true",
                                copyStartEnd: true
                            ],
                            InstitutionsService.CUSTOM_PROPERTIES_ONLY_INHERITED)
                }

                /*--
                    not longer used? -> remove and refactor params


                License licenseCopy
                List<Org> members = []

                Map<String, Object> copyParams = [
                        lic_name: "${result.license.reference}",
                        isSlaved: params.isSlaved,
                        asOrgType: orgType,
                        copyStartEnd: true
                ]

                params.list('selectedOrgs').each { it ->
                    Org fo = Org.findById(Long.valueOf(it))
                    members << Combo.executeQuery("select c.fromOrg from Combo as c where c.toOrg = ? and c.fromOrg = ?", [result.institution, fo])
                }

                members.each { cm ->
                    String postfix = (members.size() > 1) ? 'Teilnehmervertrag' : (cm.get(0).shortname ?: cm.get(0).name)

                    if (result.license) {
                        copyParams['lic_name'] = copyParams['lic_name'] + " (${postfix})"

                        if (params.generateSlavedLics == 'explicit') {
                            licenseCopy = institutionsService.copyLicense(
                                    result.license, copyParams, InstitutionsService.CUSTOM_PROPERTIES_ONLY_INHERITED)
                            // licenseCopy.sortableReference = subLicense.sortableReference
                        }
                        else if (params.generateSlavedLics == 'shared' && ! licenseCopy) {
                            licenseCopy = institutionsService.copyLicense(
                                    result.license, copyParams, InstitutionsService.CUSTOM_PROPERTIES_ONLY_INHERITED)
                        }
                        else if (params.generateSlavedLics == 'reference' && ! licenseCopy) {
                            licenseCopy = genericOIDService.resolveOID(params.generateSlavedLicsReference)
                        }

                        if (licenseCopy) {
                            new OrgRole(org: cm, lic: licenseCopy, roleType: role_lic).save()
                        }
                    }
                }
                --*/

            }
        if(licenseCopy)
            redirect action: 'show', params: [id: licenseCopy.id]
        else redirect action: 'show', params: [id: result.license?.id]
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
  def linkToSubscription(){
        log.debug("linkToSubscription :: ${params}")
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
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
        License newLicense = result.license
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
        //result.linkedSubscriptions = Links.findAllBySourceAndLinkType(GenericOIDService.getOID(result.license),RDStore.LINKTYPE_LICENSE).collect { Links l -> genericOIDService.resolveOID(l.destination) }
        redirect action: action, params: params
  }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    Map<String,Object> linkLicenseToSubs() {
        Map<String, Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        result.putAll(subscriptionService.getMySubscriptions(params,result.user,result.institution))
        result.tableConfig = ['showLinking']
        result.linkedSubscriptions = Links.findAllBySourceAndLinkType(GenericOIDService.getOID(result.license),RDStore.LINKTYPE_LICENSE).collect { Links l -> genericOIDService.resolveOID(l.destination) }
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def linkedSubs() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result.subscriptions = []
        result.putAll(setSubscriptionFilterData())
        if(params.status != "FETCH_ALL") {
            result.subscriptionsForFilter = Subscription.executeQuery("select s from Subscription s where s.status.id = :status and concat('${Subscription.class.name}:',s.id) in (select l.destination from Links l where l.source = :lic and l.linkType = :linkType)",[status:params.status as Long,lic:GenericOIDService.getOID(result.license),linkType:RDStore.LINKTYPE_LICENSE])
        }
        if(result.license.getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && result.license.getLicensingConsortium().id == result.institution.id) {
            Set<RefdataValue> subscriberRoleTypes = [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER_COLLECTIVE]
            Map<String,Object> queryParams = [lic:GenericOIDService.getOID(result.license),status:result.status,subscriberRoleTypes:subscriberRoleTypes,linkType:RDStore.LINKTYPE_LICENSE]
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
            result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.org)
            result.validSubChilds = Subscription.executeQuery("select s from Subscription s join s.orgRelations oo where concat('${Subscription.class.name}:',s.id) in (select l.destination from Links l where l.source = :lic and l.linkType = :linkType) and oo.roleType in :subscriberRoleTypes ${whereClause} order by oo.org.sortname asc, oo.org.name asc, s.name asc, s.startDate asc, s.endDate asc",queryParams)
            ArrayList<Long> filteredOrgIds = getOrgIdsForFilter()

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
            def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
            Set<Subscription> subscriptions = Subscription.executeQuery("select s ${tmpQ[0]}",tmpQ[1])
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def members() {
        log.debug("license id:${params.id}");

        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result.putAll(setSubscriptionFilterData())
        Set<License> validMemberLicenses = License.findAllByInstanceOf(result.license)
        Set<Map<String,Object>> filteredMemberLicenses = []
        validMemberLicenses.each { License memberLicense ->
            //memberLicense.getAllLicensee().sort{ Org a, Org b -> a.sortname <=> b.sortname }.each { Org org ->
            //if(org.id in filteredOrgIds) {
            String dateFilter = ""
            Map<String,Object> subQueryParams = [lic:GenericOIDService.getOID(memberLicense),linkType:RDStore.LINKTYPE_LICENSE]
            if(params.validOn) {
                dateFilter += " and ((s.startDate = null or s.startDate <= :validOn) and (s.endDate = null or s.endDate >= :validOn))"
                subQueryParams.validOn = result.dateRestriction
            }
            Set<Subscription> subscriptions = Subscription.executeQuery("select s from Subscription s where concat('${Subscription.class.name}:',s.id) in (select l.destination from Links l where l.source = :lic and l.linkType = :linkType)${dateFilter}",subQueryParams)
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
        String subQuery = "select s from Subscription s where concat('${Subscription.class.name}:',s.id) in (select l.destination from Links l where l.source in (:licenses) and l.linkType = :linkType)"
        if(params.status == "FETCH_ALL")
            result.subscriptionsForFilter = Subscription.executeQuery(subQuery,[linkType:RDStore.LINKTYPE_LICENSE,licenses:validMemberLicenses.collect { License lic -> GenericOIDService.getOID(lic)}])
        else result.subscriptionsForFilter = Subscription.executeQuery(subQuery+" and s.status = :status",[linkType:RDStore.LINKTYPE_LICENSE,licenses:validMemberLicenses.collect{License lic -> GenericOIDService.getOID(lic)},status:RefdataValue.get(params.status as Long)])
        result.validMemberLicenses = filteredMemberLicenses
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def linkMemberLicensesToSubs() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        result.tableConfig = ['onlyMemberSubs']
        result.linkedSubscriptions = Links.findAllBySourceAndLinkType(GenericOIDService.getOID(result.license),RDStore.LINKTYPE_LICENSE).collect { Links l -> genericOIDService.resolveOID(l.destination) }
        result.putAll(subscriptionService.getMySubscriptionsForConsortia(params,result.user,result.institution,result.tableConfig))
        result
    }

    /**
     * this is very ugly and should be subject of refactor - - but unfortunately, the
     * {@link SubscriptionsQueryService#myInstitutionCurrentSubscriptionsBaseQuery(java.lang.Object, com.k_int.kbplus.Org)}
     * requires the {@link org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap} as parameter.
     * @return validOn and defaultSet-parameters of the filter
     */
    private Map<String,Object> setSubscriptionFilterData() {
        Map<String, Object> result = [:]
        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
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
                params.status = SUBSCRIPTION_CURRENT.id
                result.defaultSet = true
            }
            else {
                params.status = 'FETCH_ALL'
            }
        }
        result
    }

    private ArrayList<Long> getOrgIdsForFilter() {
        def result = setResultGenericsAndCheckAccess(accessService.CHECK_VIEW)
        ArrayList<Long> resultOrgIds
        def tmpParams = params.clone()
        tmpParams.remove("max")
        tmpParams.remove("offset")
        if (accessService.checkPerm("ORG_CONSORTIUM"))
            tmpParams.comboType = COMBO_TYPE_CONSORTIUM.value
        else if (accessService.checkPerm("ORG_INST_COLLECTIVE"))
            tmpParams.comboType = COMBO_TYPE_DEPARTMENT.value
        def fsq = filterService.getOrgComboQuery(tmpParams, result.institution)

        if (tmpParams.filterPropDef) {
            fsq = propertyService.evalFilterQuery(tmpParams, fsq.query, 'o', fsq.queryParams)
        }
        fsq.query = fsq.query.replaceFirst("select o from ", "select o.id from ")
        Org.executeQuery(fsq.query, fsq.queryParams, tmpParams)
    }

    /*
    @Deprecated
    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def deleteMember() {
        log.debug(params)

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        if (!result) {
            response.sendError(401); return
        }

        // adopted from SubscriptionDetailsController.deleteMember()

        def delLicense      = genericOIDService.resolveOID(params.target)
        def delInstitutions = delLicense?.getAllLicensee()

        if (delLicense?.hasPerm("edit", result.user)) {
            def derived_lics = License.findByInstanceOf(delLicense)

            if (! derived_lics) {
                if (delLicense.getLicensingConsortium() && ! ( delInstitutions.contains(delLicense.getLicensingConsortium() ) ) ) {
                    OrgRole.executeUpdate("delete from OrgRole where lic = :l and org IN (:orgs)", [l: delLicense, orgs: delInstitutions])
                }

                delLicense.status = RefdataValue.getByValueAndCategory('Deleted', RDConstants.LICENSE_STATUS)
                delLicense.save(flush: true)
            } else {
                flash.error = message(code: 'myinst.actionCurrentLicense.error')
            }
        } else {
            log.warn("${result.user} attempted to delete license ${delLicense} without perms")
            flash.message = message(code: 'license.delete.norights')
        }

        redirect action: 'members', params: [id: params.id], model: result
    }
    */

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def pendingChanges() {
        log.debug("license id:${params.id}");

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
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
                def pending_change_pending_status = RDStore.PENDING_CHANGE_PENDING
                List<PendingChange> pendingChanges = PendingChange.executeQuery("select pc from PendingChange as pc where license.id=? and ( pc.status is null or pc.status = ? ) order by pc.ts desc", [member.id, pending_change_pending_status])

                result.pendingChanges << ["${member.id}": pendingChanges]
            }
        }


        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def history() {
        log.debug("license::history : ${params}");

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        // postgresql migration
        String subQuery = 'select cast(lp.id as string) from LicenseCustomProperty as lp where lp.owner = :owner'
        def subQueryResult = LicenseProperty.executeQuery(subQuery, [owner: result.license])

        //def qry_params = [licClass:result.license.class.name, prop:LicenseCustomProperty.class.name,owner:result.license, licId:"${result.license.id}"]
        //result.historyLines = AuditLogEvent.executeQuery("select e from AuditLogEvent as e where (( className=:licClass and persistedObjectId=:licId ) or (className = :prop and persistedObjectId in (select lp.id from LicenseCustomProperty as lp where lp.owner=:owner))) order by e.dateCreated desc", qry_params, [max:result.max, offset:result.offset]);

        String base_query = "select e from AuditLogEvent as e where ( (className=:licClass and persistedObjectId = cast(:licId as string))"
        def query_params = [licClass:result.license.class.name, licId:"${result.license.id}"]

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

    def propertyNameHql = "select pd.name from LicenseCustomProperty as licP, PropertyDefinition as pd where licP.id= ? and licP.type = pd"
    
    result.historyLines?.each{
      if(it.className == query_params.prop ){
        def propertyName = LicenseProperty.executeQuery(propertyNameHql,[it.persistedObjectId.toLong()])[0]
        it.propertyName = propertyName
      }
    }

    result.historyLinesTotal = AuditLogEvent.executeQuery(base_query, query_params).size()
    result

  }


    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def changes() {
        log.debug("license::changes : ${params}")

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        def baseQuery = "select pc from PendingChange as pc where pc.license = :lic and pc.status.value in (:stats)"
        def baseParams = [lic: result.license, stats: ['Accepted', 'Rejected']]

        result.todoHistoryLines = PendingChange.executeQuery(
                baseQuery + " order by pc.ts desc",
                baseParams,
                [max: result.max, offset: result.offset]
        )

        result.todoHistoryLinesTotal = PendingChange.executeQuery(
                baseQuery,
                baseParams
        )[0]

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def notes() {
        log.debug("license id:${params.id}");

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def tasks() {
        log.debug("license id:${params.id}")

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (params.deleteId) {
            Task dTask = Task.get(params.deleteId)
            if (dTask && dTask.creator.id == result.user.id) {
                try {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: 'task.label'), dTask.title])
                    dTask.delete(flush: true)
                }
                catch (Exception e) {
                    flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'task.label'), params.deleteId])
                }
            }
        }

        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        result.taskInstanceList = taskService.getTasksByResponsiblesAndObject(result.user, contextService.getOrg(), result.license)
        result.taskInstanceCount = result.taskInstanceList?.size()
        result.taskInstanceList = taskService.chopOffForPageSize(result.taskInstanceList, result.user, offset)

        result.myTaskInstanceList = taskService.getTasksByCreatorAndObject(result.user,  result.license)
        result.myTaskInstanceCount = result.myTaskInstanceList?.size()
        result.myTaskInstanceList = taskService.chopOffForPageSize(result.myTaskInstanceList, result.user, offset)

        log.debug(result.taskInstanceList)
        log.debug(result.myTaskInstanceList)

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def properties() {
        log.debug("license id: ${params.id}");
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        result.authorizedOrgs = result.user?.authorizedOrgs

        // create mandatory LicensePrivateProperties if not existing

        List<PropertyDefinition> mandatories = []
        result.user?.authorizedOrgs?.each{ org ->
            List<PropertyDefinition> ppd = PropertyDefinition.getAllByDescrAndMandatoryAndTenant(PropertyDefinition.LIC_PROP, true, org)
            if (ppd) {
                mandatories << ppd
            }
        }
        mandatories.flatten().each{ PropertyDefinition pd ->
            if (! LicenseProperty.findWhere(owner: result.licenseInstance, type: pd, tenant: result.institution, isPublic: false)) {
                def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, result.licenseInstance, pd, result.institution)

                if (newProp.hasErrors()) {
                    log.error(newProp.errors)
                } else {
                    log.debug("New license private property created via mandatory: " + newProp.type.name)
                }
            }
        }
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def documents() {
        log.debug("license id:${params.id}");

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def deleteDocuments() {
        log.debug("deleteDocuments ${params}");

        params.id = params.instanceId // TODO refactoring frontend instanceId -> id
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_EDIT)
        if (!result) {
            response.sendError(401); return
        }

        //def user = User.get(springSecurityService.principal.id)
        //def l = License.get(params.instanceId);
        //userAccessCheck(l,user,'edit')

        docstoreService.unifiedDeleteDocuments(params)

        redirect controller: 'license', action:params.redirectAction, id:params.instanceId /*, fragment:'docstab' */
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
  def acceptChange() {
    processAcceptChange(params, License.get(params.id), genericOIDService)
    redirect controller: 'license', action:'show',id:params.id
  }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
  def rejectChange() {
    processRejectChange(params, License.get(params.id))
    redirect controller: 'license', action:'show',id:params.id
  }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def permissionInfo() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
  def create() {
    Map<String, Object> result = [:]
    result.user = User.get(springSecurityService.principal.id)
    result
  }

    /*
    @Deprecated
    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
  def unlinkLicense() {
      log.debug("unlinkLicense :: ${params}")
      License license = License.get(params.license_id);
      OnixplLicense opl = OnixplLicense.get(params.opl_id);
      if(! (opl && license)){
        log.error("Something has gone mysteriously wrong. Could not get License or OnixLicense. params:${params} license:${license} onix: ${opl}")
        flash.message = message(code:'license.unlink.error.unknown');
        redirect(action: 'show', id: license.id);
      }

      String oplTitle = opl?.title;
      DocContext dc = DocContext.findByOwner(opl.doc);
      Doc doc = opl.doc;
      license.removeFromDocuments(dc);
      opl.removeFromLicenses(license);
      // If there are no more links to this ONIX-PL License then delete the license and
      // associated data
      if (opl.licenses.isEmpty()) {
          opl.usageTerm.each{
            it.usageTermLicenseText.each{
              it.delete()
            }
          }
          opl.delete();
          dc.delete();
          doc.delete();
      }
      if (license.hasErrors()) {
          license.errors.each {
              log.error("License error: " + it);
          }
          flash.message = message(code:'license.unlink.error.known', args:[oplTitle]);
      } else {
          flash.message = message(code:'license.unlink.success', args:[oplTitle]);
      }
      redirect(action: 'show', id: license.id);
  }
     */

    def copyLicense()
    {
        log.debug("license: ${params}");
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        result.visibleOrgLinks = []
        result.license.orgLinks?.each { or ->
            if (!(or.org?.id == contextService.getOrg().id) && !(or.roleType.value in ["Licensee", "Licensee_Consortial"])) {
                result.visibleOrgLinks << or
            }
        }
        result.visibleOrgLinks.sort{ it.org.sortname }

        Org contextOrg = contextService.getOrg()
        result.tasks = taskService.getTasksByResponsiblesAndObject(result.user, contextOrg, result.license)
        def preCon = taskService.getPreconditionsWithoutTargets(contextOrg)
        result << preCon


        result.contextOrg = contextService.getOrg()

        result

    }

    def processcopyLicense() {

        params.id = params.baseLicense
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        License baseLicense = License.get(params.baseLicense)

        if (baseLicense) {

            def lic_name = params.lic_name ?: "Kopie von ${baseLicense.reference}"

            License licenseInstance = new License(
                    reference: lic_name,
                    type: baseLicense.type,
                    startDate: params.license.copyDates ? baseLicense?.startDate : null,
                    endDate: params.license.copyDates ? baseLicense?.endDate : null,
                    instanceOf: params.license.copyLinks ? baseLicense?.instanceOf : null,

            )


            if (!licenseInstance.save()) {
                log.error("Problem saving license ${licenseInstance.errors}")
                return licenseInstance
            }
            else {
                   log.debug("Save ok")

                    baseLicense.documents?.each { DocContext dctx ->

                        //Copy Docs
                        if (params.license.copyDocs) {
                            if (((dctx.owner?.contentType == 1) || (dctx.owner?.contentType == 3)) && (dctx.status?.value != 'Deleted')) {
                                Doc clonedContents = new Doc(
                                        blobContent: dctx.owner.blobContent,
                                        status: dctx.owner.status,
                                        type: dctx.owner.type,
                                        content: dctx.owner.content,
                                        uuid: dctx.owner.uuid,
                                        contentType: dctx.owner.contentType,
                                        title: dctx.owner.title,
                                        creator: dctx.owner.creator,
                                        filename: dctx.owner.filename,
                                        mimeType: dctx.owner.mimeType,
                                        user: dctx.owner.user,
                                        migrated: dctx.owner.migrated,
                                        owner: dctx.owner.owner
                                ).save()

                                DocContext ndc = new DocContext(
                                        owner: clonedContents,
                                        license: licenseInstance,
                                        domain: dctx.domain,
                                        status: dctx.status,
                                        doctype: dctx.doctype
                                ).save()
                            }
                        }
                        //Copy Announcements
                        if (params.license.copyAnnouncements) {
                            if ((dctx.owner.contentType == Doc.CONTENT_TYPE_STRING) && !(dctx.domain) && (dctx.status?.value != 'Deleted')) {
                                Doc clonedContents = new Doc(
                                        blobContent: dctx.owner.blobContent,
                                        status: dctx.owner.status,
                                        type: dctx.owner.type,
                                        content: dctx.owner.content,
                                        uuid: dctx.owner.uuid,
                                        contentType: dctx.owner.contentType,
                                        title: dctx.owner.title,
                                        creator: dctx.owner.creator,
                                        filename: dctx.owner.filename,
                                        mimeType: dctx.owner.mimeType,
                                        user: dctx.owner.user,
                                        migrated: dctx.owner.migrated
                                ).save()

                                DocContext ndc = new DocContext(
                                        owner: clonedContents,
                                        license: licenseInstance,
                                        domain: dctx.domain,
                                        status: dctx.status,
                                        doctype: dctx.doctype
                                ).save()
                            }
                        }
                    }
                    //Copy Tasks
                    if (params.license.copyTasks) {

                        Task.findAllByLicense(baseLicense).each { task ->

                            Task newTask = new Task()
                            InvokerHelper.setProperties(newTask, task.properties)
                            newTask.systemCreateDate = new Date()
                            newTask.license = licenseInstance
                            newTask.save()
                        }

                    }
                    //Copy References
                        baseLicense.orgLinks.each { OrgRole or ->
                            if ((or.org.id == result.institution.id) || (or.roleType.value in ["Licensee", "Licensee_Consortial"]) || (params.license.copyLinks)) {
                            OrgRole newOrgRole = new OrgRole()
                            InvokerHelper.setProperties(newOrgRole, or.properties)
                            newOrgRole.lic = licenseInstance
                            newOrgRole.save()

                            }

                    }

                    if(params.license.copyCustomProperties) {
                        //customProperties
                        baseLicense.customProperties.findAll{ LicenseProperty prop -> prop.tenant.id == result.institution.id && prop.isPublic }.each{ LicenseProperty prop ->
                            LicenseProperty copiedProp = new LicenseProperty(type: prop.type, owner: licenseInstance, tenant: prop.tenant, isPublic: prop.isPublic)
                            copiedProp = prop.copyInto(copiedProp)
                            copiedProp.instanceOf = null
                            copiedProp.save()
                        }
                    }
                    if(params.license.copyPrivateProperties){
                        //privatProperties

                        baseLicense.customProperties.findAll{ LicenseProperty prop -> prop.type.tenant.id == result.institution.id && prop.tenant.id == result.institution.id && !prop.isPublic }.each { LicenseProperty prop ->
                            LicenseProperty copiedProp = new LicenseProperty(type: prop.type, owner: licenseInstance, tenant: prop.tenant, isPublic: prop.isPublic)
                            copiedProp = prop.copyInto(copiedProp)
                            copiedProp.save()
                        }
                    }
                redirect controller: 'license', action: 'show', params: [id: licenseInstance.id]
                }

            }
    }

    private Map<String,Object> setResultGenericsAndCheckAccess(checkOption) {
        def result             = [:]
        result.user            = User.get(springSecurityService.principal.id)
        result.institution     = contextService.org
        result.license         = License.get(params.id)
        result.licenseInstance = License.get(params.id)
        LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(GenericOIDService.getOID(result.license))
        result.navPrevLicense = links.prevLink
        result.navNextLicense = links.nextLink
        result.showConsortiaFunctions = showConsortiaFunctions(result.license)

        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP()
        result.offset = params.offset ?: 0

        if (checkOption in [AccessService.CHECK_VIEW, AccessService.CHECK_VIEW_AND_EDIT]) {
            if (! result.licenseInstance.isVisibleBy(result.user)) {
                log.debug( "--- NOT VISIBLE ---")
                return null
            }
        }
        result.editable = result.license.isEditableBy(result.user)

        if (checkOption in [AccessService.CHECK_EDIT, AccessService.CHECK_VIEW_AND_EDIT]) {
            if (! result.editable) {
                log.debug( "--- NOT EDITABLE ---")
                return null
            }
        }

        result
    }

    boolean showConsortiaFunctions(License license) {

        return (license.getLicensingConsortium()?.id == contextService.getOrg().id)
    }

}
