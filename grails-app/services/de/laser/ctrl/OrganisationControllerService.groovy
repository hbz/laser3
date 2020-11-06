package de.laser.ctrl

import de.laser.AccessService
import de.laser.Combo
import de.laser.ContextService
import de.laser.Org
import de.laser.OrganisationController
import de.laser.auth.User
import de.laser.helper.RDStore
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class OrganisationControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AccessService accessService
    ContextService contextService
    SpringSecurityService springSecurityService

    //--------------------------------------------- helper section -------------------------------------------------

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
}