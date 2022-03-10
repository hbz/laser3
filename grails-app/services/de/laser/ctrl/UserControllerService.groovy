package de.laser.ctrl

import com.k_int.kbplus.GenericOIDService
import de.laser.ContextService
import de.laser.InstAdmService
import de.laser.auth.User
import grails.gorm.transactions.Transactional
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
    InstAdmService instAdmService

    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Sets common parameters which are used in many controller calls
     * @param params the request parameter map
     * @return a map containing the context user and his permissions
     */
    Map<String, Object> getResultGenerics(GrailsParameterMap params) {

        Map<String, Object> result = [orgInstance: contextService.getOrg()]
        result.editor = contextService.getUser()

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

    /**
     * Sets common parameters for a user oid call
     * @param params the request parameter map
     * @return a map containing the context user and his permissions
     */
    Map<String, Object> getResultGenericsERMS3067(GrailsParameterMap params) {

        Map<String, Object> result = [orgInstance: contextService.getOrg()]
        result.editor = contextService.getUser()

        if (params.get('uoid')) {
            result.user = genericOIDService.resolveOID(params.uoid)
            result.editable = result.editor.hasRole('ROLE_ADMIN') || instAdmService.isUserEditableForInstAdm(result.user, result.editor)
            //result.editable = instAdmService.isUserEditableForInstAdm(result.user, result.editor, contextService.getOrg())
        }
        else {
            result.editable = result.editor.hasRole('ROLE_ADMIN') || result.editor.hasAffiliation('INST_ADM')
        }
        result
    }
}