package de.laser

import de.laser.auth.User
import de.laser.helper.RDStore
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

@Transactional
class ResultGenericsService {

    AccessService accessService
    ContextService contextService
    InstAdmService instAdmService
    LinksGenerationService linksGenerationService
    MessageSource messageSource
    SubscriptionService subscriptionService

    Map<String, Object> getResultGenericsAndCheckAccess(LicenseController controller, GrailsParameterMap params, String checkOption) {

        Map<String, Object> result = [:]

        result.user            = User.get(springSecurityService.principal.id)
        result.institution     = contextService.org
        result.contextOrg      = result.institution
        result.license         = License.get(params.id)
        result.licenseInstance = result.license

        LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(result.license)
        result.navPrevLicense = links.prevLink
        result.navNextLicense = links.nextLink

        result.showConsortiaFunctions = controller.showConsortiaFunctions(result.license)

        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeAsInteger()
        result.offset = params.offset ?: 0

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

    Map<String, Object> getResultGenerics(MyInstitutionController controller, GrailsParameterMap params) {

        Map<String, Object> result = [:]

        switch(params.action){
            case 'currentSurveys':
            case 'surveyInfos':
            case 'surveyInfoFinish':
            case 'surveyInfosIssueEntitlements':
            case 'surveyResultFinish':
                result.user = User.get(springSecurityService.principal.id)
                break
            default:
                result.user = contextService.getUser()
        }
        result.institution = contextService.getOrg()
        result.editable = controller.checkIsEditable(result.user, result.institution)

        result
    }

    Map<String, Object> getResultGenericsAndCheckAccess(OrganisationController controller, GrailsParameterMap params) {

        User user = User.get(springSecurityService.principal.id)
        Org org = contextService.org
        Map<String, Object> result = [user:user, institution:org, inContextOrg:true, institutionalView:false]

        if (params.id) {
            result.orgInstance = Org.get(params.id)
            result.editable = controller.checkIsEditable(user, result.orgInstance)
            result.inContextOrg = result.orgInstance?.id == org.id
            //this is a flag to check whether the page has been called for a consortia or inner-organisation member
            Combo checkCombo = Combo.findByFromOrgAndToOrg(result.orgInstance,org)
            if (checkCombo && checkCombo.type == RDStore.COMBO_TYPE_CONSORTIUM) {
                result.institutionalView = true
            }
            //restrictions hold if viewed org is not the context org
            if (!result.inContextOrg && !accessService.checkPerm("ORG_CONSORTIUM") && !SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_ORG_EDITOR")) {
                //restrictions further concern only single users, not consortia
                if (accessService.checkPerm("ORG_INST") && result.orgInstance.getCustomerType() == "ORG_INST") {
                    return null
                }
            }
        }
        else {
            result.editable = controller.checkIsEditable(user, org)
        }

        result
    }

    Map<String, Object> getResultGenerics(PlatformController controller, GrailsParameterMap params) {

        Map<String, Object> result = [:]

        result.user = User.get(springSecurityService.principal.id)
        result.institution = contextService.org
        result.contextOrg = result.institution //temp fix

        result
    }

    Map<String, Object> getResultGenericsAndCheckAccess(SubscriptionController controller, GrailsParameterMap params, String checkOption) {

        Map<String, Object> result = [:]

        result.user = contextService.user
        result.subscription = Subscription.get(params.id)

        if (!params.id && params.subscription) {
            result.subscription = Subscription.get(params.subscription)
        }
        result.contextOrg = contextService.org
        result.institution = result.subscription ? result.subscription.subscriber : result.contextOrg //TODO temp, remove the duplicate

        if (result.subscription) {
            result.licenses = Links.findAllByDestinationSubscriptionAndLinkType(result.subscription, RDStore.LINKTYPE_LICENSE).collect { Links li -> li.sourceLicense }

            LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(result.subscription)
            result.navPrevSubscription = links.prevLink
            result.navNextSubscription = links.nextLink

            result.showConsortiaFunctions = subscriptionService.showConsortiaFunctions(result.contextOrg, result.subscription)

            if (checkOption in [AccessService.CHECK_VIEW, AccessService.CHECK_VIEW_AND_EDIT]) {
                if (!result.subscription.isVisibleBy(result.user)) {
                    log.debug("--- NOT VISIBLE ---")
                    return null
                }
            }
            result.editable = result.subscription.isEditableBy(result.user)

            if (params.orgBasicMemberView){
                result.editable = false
            }

            if (checkOption in [AccessService.CHECK_EDIT, AccessService.CHECK_VIEW_AND_EDIT]) {
                if (!result.editable) {
                    log.debug("--- NOT EDITABLE ---")
                    return null
                }
            }
        }
        else {
            if (checkOption in [AccessService.CHECK_EDIT, AccessService.CHECK_VIEW_AND_EDIT]) {
                result.editable = accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM","INST_EDITOR")
            }
        }
        result.consortialView = result.showConsortiaFunctions ?: result.contextOrg.getCustomerType() == "ORG_CONSORTIUM"

        Map args = [:]
        if (result.consortialView) {
            Locale locale = LocaleContextHolder.getLocale()

            args.superOrgType       = [messageSource.getMessage(code:'consortium.superOrgType', null, locale)]
            args.memberTypeSingle   = [messageSource.getMessage(code:'consortium.subscriber', null, locale)]
            args.memberType         = [messageSource.getMessage(code:'consortium.subscriber', null, locale)]
            args.memberTypeGenitive = [messageSource.getMessage(code:'consortium.subscriber', null, locale)]
        }
        result.args = args

        result
    }

    Map<String, Object> getResultGenericsAndCheckAccess(SurveyController controller, GrailsParameterMap params) {

        Map<String, Object> result = [:]

        result.institution = contextService.getOrg()
        result.contextOrg = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.surveyInfo = SurveyInfo.get(params.id)
        result.surveyConfig = params.surveyConfigID ? SurveyConfig.get(params.surveyConfigID as Long ? params.surveyConfigID: Long.parseLong(params.surveyConfigID)) : result.surveyInfo.surveyConfigs[0]
        result.surveyWithManyConfigs = (result.surveyInfo.surveyConfigs?.size() > 1)

        result.editable = result.surveyInfo.isEditable() ?: false

        if (result.surveyConfig) {
            result.transferWorkflow = result.surveyConfig.transferWorkflow ? JSON.parse(result.surveyConfig.transferWorkflow) : null
        }

        result.subscription = result.surveyConfig.subscription ?: null

        result
    }

    Map<String, Object> getResultGenerics(UserController controller, GrailsParameterMap params) {

        Map<String, Object> result = [orgInstance: contextService.org]
        result.editor = contextService.user

        if (params.get('id')) {
            result.user = User.get(params.id)
            result.editable = result.editor.hasRole('ROLE_ADMIN') || instAdmService.isUserEditableForInstAdm(result.user, result.editor)

            //result.editable = instAdmService.isUserEditableForInstAdm(result.user, result.editor, contextService.getOrg())
        }
        else {
            result.editable = result.editor.hasRole('ROLE_ADMIN') || result.editor.hasAffiliation('INST_ADM')
        }

        result
    }
}