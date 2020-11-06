package de.laser.ctrl

import de.laser.ContextService
import de.laser.InstAdmService
import de.laser.auth.User
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class UserControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    ContextService contextService
    InstAdmService instAdmService

    Map<String, Object> getResultGenerics(GrailsParameterMap params) {

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