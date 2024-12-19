package de.laser.ctrl

import de.laser.GenericOIDService
import de.laser.ContextService
import de.laser.UserService
import de.laser.auth.User
import de.laser.utils.SwissKnife
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * This service is a mirror of the {@link de.laser.UserController}, containing its data processing methods
 */
@Transactional
class UserControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    ContextService contextService
    GenericOIDService genericOIDService
    UserService userService

    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Sets common parameters which are used in many controller calls
     * @param params the request parameter map
     * @return a map containing the context user and his permissions
     */
    Map<String, Object> getResultGenerics(GrailsParameterMap params) {
        Map<String, Object> result = [ orgInstance: contextService.getOrg() ]

        SwissKnife.setPaginationParams(result, params, contextService.getUser())

        if (params.get('id')) {
            result.user = User.get(params.id)
            result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') || userService.isUserEditableForInstAdm(result.user as User)
        }
        else {
            result.editable = contextService.isInstAdm_or_ROLEADMIN()
        }
        result
    }

    /**
     * Sets common parameters for a user oid call
     * @param params the request parameter map
     * @return a map containing the context user and his permissions
     */
    Map<String, Object> getResultGenericsERMS3067(GrailsParameterMap params) {
        Map<String, Object> result = [ orgInstance: contextService.getOrg() ]

        if (params.get('uoid')) {
            result.user = genericOIDService.resolveOID(params.uoid)
            result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') || userService.isUserEditableForInstAdm(result.user as User)
        }
        else {
            result.editable = contextService.isInstAdm_or_ROLEADMIN()
        }
        result
    }
}