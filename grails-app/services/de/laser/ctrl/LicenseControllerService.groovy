package de.laser.ctrl

import de.laser.*
import de.laser.auth.User
import de.laser.helper.SwissKnife
import de.laser.interfaces.CalculatedType
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

@Transactional
class LicenseControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AuditService auditService
    ContextService contextService
    MessageSource messageSource
    TaskService taskService
    LinksGenerationService linksGenerationService

    //------------------------------------ general or ungroupable section ------------------------------------------

    Map<String,Object> tasks(LicenseController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller, params, AccessService.CHECK_VIEW)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            int offset = params.offset ? Integer.parseInt(params.offset) : 0
            result.putAll(taskService.getTasks(offset, (User) result.user, (Org) result.institution, (License) result.license))
            [result:result,status:STATUS_OK]
        }
    }

    //--------------------------------------------- helper section -------------------------------------------------

    Map<String, Object> getResultGenericsAndCheckAccess(LicenseController controller, GrailsParameterMap params, String checkOption) {

        Map<String, Object> result = [:]

        result.user            = contextService.getUser()
        result.institution     = contextService.getOrg()
        result.contextOrg      = result.institution
        result.contextCustomerType = result.institution.getCustomerType()
        result.license         = License.get(params.id)
        result.licenseInstance = result.license

        if(result.license.instanceOf)
            result.auditConfigs = auditService.getAllAuditConfigs(result.license.instanceOf)
        else result.auditConfigs = auditService.getAllAuditConfigs(result.license)

        LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(result.license)
        result.navPrevLicense = links.prevLink
        result.navNextLicense = links.nextLink

        result.showConsortiaFunctions = showConsortiaFunctions(result.license)

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        if (checkOption in [AccessService.CHECK_VIEW, AccessService.CHECK_VIEW_AND_EDIT]) {
            if (! result.license.isVisibleBy(result.user)) {
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
        showConsortiaFunctions(contextService.getOrg(), license)
    }

    boolean showConsortiaFunctions(Org contextOrg, License license) {
        return license.getLicensingConsortium()?.id == contextOrg.id && license._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL
    }
}