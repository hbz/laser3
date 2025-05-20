package de.laser.ctrl

import de.laser.*
import de.laser.auth.User
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.utils.SwissKnife
import de.laser.interfaces.CalculatedType
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

import java.text.SimpleDateFormat

/**
 * This class is a service mirror for {@link LicenseController} to capsule the complex data manipulation
 * methods of the controller
 */
@Transactional
class LicenseControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AuditService auditService
    ContextService contextService
    DocstoreService docstoreService
    LinksGenerationService linksGenerationService
    LicenseService licenseService
    SubscriptionsQueryService subscriptionsQueryService
    TaskService taskService
    WorkflowService workflowService


    //--------------------------------------------- workflows -------------------------------------------------

    /**
     * Gets the workflows linked to the given license
     * @param controller the controller instance
     * @param params the request parameter map
     * @return OK if the retrieval was successful, ERROR otherwise
     */
    Map<String,Object> workflows(LicenseController controller, GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(controller, params, AccessService.CHECK_VIEW)

        workflowService.executeCmdAndUpdateResult(result, params)

        [result: result, status: (result ? STATUS_OK : STATUS_ERROR)]
    }

    //------------------------------------ general or ungroupable section ------------------------------------------

    /**
     * Displays the tasks for the given license
     * @param controller the controller instance
     * @param params the request parameter map
     * @return the tasks attached to the given license
     */
    Map<String,Object> tasks(LicenseController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params, AccessService.CHECK_VIEW)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            SwissKnife.setPaginationParams(result, params, result.user as User)
            result.cmbTaskInstanceList = taskService.getTasks((User) result.user, (License) result.license)['cmbTaskInstanceList']
            [result:result,status:STATUS_OK]
        }
    }

    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Sets generic parameters for page calls which are widespreadly needed
     * @param controller unused
     * @param params the request parameter map
     * @param checkOption the permission (edit or view) to check
     * @return a map containing generic result data
     */
    Map<String, Object> getResultGenericsAndCheckAccess(LicenseController controller, GrailsParameterMap params, String checkOption) {

        Map<String, Object> result = [:]

        result.user            = contextService.getUser()
        result.institution     = contextService.getOrg()
        result.contextCustomerType = contextService.getOrg().getCustomerType()
        result.license         = License.get(params.id)
        result.licenseInstance = result.license
        result.inContextOrg = (contextService.getOrg().id in result.license.getAllLicensee().id || (!result.license.instanceOf && contextService.getOrg().id == result.license.getLicensingConsortium()?.id))

        if(result.license.instanceOf)
            result.auditConfigs = auditService.getAllAuditConfigs(result.license.instanceOf)
        else result.auditConfigs = auditService.getAllAuditConfigs(result.license)

        LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(result.license)
        result.navPrevLicense = links.prevLink
        result.navNextLicense = links.nextLink

        result.showConsortiaFunctions = showConsortiaFunctions(result.license)

        int tc1 = taskService.getTasksByResponsibilityAndObject(result.user, result.license).size()
        int tc2 = taskService.getTasksByCreatorAndObject(result.user, result.license).size()
        result.tasksCount = (tc1 || tc2) ? "${tc1}/${tc2}" : ''

        result.notesCount       = docstoreService.getNotesCount(result.license, contextService.getOrg())
        result.docsCount        = docstoreService.getDocsCount(result.license, contextService.getOrg())
        result.checklistCount   = workflowService.getWorkflowCount(result.license, contextService.getOrg())

        GrailsParameterMap clone = params.clone() as GrailsParameterMap
        if(!clone.license){
            clone.license = result.license.id
        }

        Map notNeededOnlySetCloneParams = setSubscriptionFilterData(clone)
        if(result.license._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && result.license.getLicensingConsortium().id == contextService.getOrg().id) {
            Set<RefdataValue> subscriberRoleTypes = [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]
            Map<String,Object> queryParams = [lic:result.license, subscriberRoleTypes:subscriberRoleTypes, linkType:RDStore.LINKTYPE_LICENSE]
            String whereClause = ""
            if (clone.status) {
                whereClause += " and s.status.id = :status"
                queryParams.status = clone.long('status')
            }
            String query = "select count(*) from Links l join l.destinationSubscription s join s.orgRelations oo where l.sourceLicense = :lic and l.linkType = :linkType and oo.roleType in :subscriberRoleTypes ${whereClause} "
            result.subsCount = Subscription.executeQuery(query.split('order by')[0], queryParams)[0]
        }else {
            List tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(clone)
            result.subsCount   = Subscription.executeQuery( "select count(*) " + tmpQ[0].split('order by')[0] , tmpQ[1] )[0]
        }


        SwissKnife.setPaginationParams(result, params, (User) result.user)

        if (checkOption in [AccessService.CHECK_VIEW, AccessService.CHECK_VIEW_AND_EDIT]) {
            if (! result.license.isVisibleBy(result.user)) {
                log.debug( "--- NOT VISIBLE ---")
                //perform further check because it may be linked to a subscription and the linking is missing ...
                List<Links> subLinks = Links.executeQuery('select li from Links li where li.sourceLicense = :lic and li.destinationSubscription in (select oo.sub from OrgRole oo where oo.org = :ctx)', [ctx: contextService.getOrg(), lic: result.license])
                if(subLinks) {
                    //substitute missing link upon call
                    log.debug("--- SUBSTITUTING ---")
                    OrgRole substitute = new OrgRole(org: contextService.getOrg(), lic: result.license)
                    if(contextService.getOrg().isCustomerType_Consortium()) {
                        substitute.roleType = RDStore.OR_LICENSING_CONSORTIUM
                    }
                    else {
                        if(result.license._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION)
                            substitute.roleType = RDStore.OR_LICENSEE_CONS
                        else substitute.roleType = RDStore.OR_LICENSEE
                    }
                    substitute.save()
                }
                else return null
            }
        }
        result.editable = result.license.isEditableBy(result.user)

        if (checkOption in [AccessService.CHECK_EDIT, AccessService.CHECK_VIEW_AND_EDIT]) {
            if (! result.editable) {
                log.debug( "--- NOT EDITABLE ---")
                return null
            }
        }

        result.visibleProviders = licenseService.getVisibleProviders(result.license)
        result.visibleVendors = licenseService.getVisibleVendors(result.license)

        result
    }

    /**
     * Substitution call for {@link #showConsortiaFunctions(de.laser.Org, de.laser.License)}
     * @param license the license to check
     * @return result of {@link #showConsortiaFunctions(de.laser.Org, de.laser.License)}
     */
    boolean showConsortiaFunctions(License license) {
        showConsortiaFunctions(contextService.getOrg(), license)
    }

    /**
     * Checks if the given institution is the licensing consortium for the given license and thus if consortial functions
     * should be shown
     * @param contextOrg the institution whose access should be checked
     * @param license the license to check
     * @return true in the given institution is the licensing consortium and if the license is a consortial parent license, false otherwise
     */
    boolean showConsortiaFunctions(Org contextOrg, License license) {
        return license.getLicensingConsortium()?.id == contextOrg.id && license._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL
    }

    /**
     * this is very ugly and should be subject of refactor - - but unfortunately, the
     * {@link SubscriptionsQueryService#myInstitutionCurrentSubscriptionsBaseQuery(java.util.Map)}
     * requires the {@link GrailsParameterMap} as parameter.
     * @return validOn and defaultSet-parameters of the filter
     */
    Map<String,Object> setSubscriptionFilterData(GrailsParameterMap params) {
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
        }
        result
    }
}