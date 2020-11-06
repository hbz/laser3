package de.laser.ctrl

import de.laser.ContextService
import de.laser.auth.User
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class PlatformControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    ContextService contextService
    SpringSecurityService springSecurityService

    //--------------------------------------------- helper section -------------------------------------------------

    Map<String, Object> getResultGenerics(GrailsParameterMap params) {

        Map<String, Object> result = [:]

        result.user = User.get(springSecurityService.principal.id)
        result.institution = contextService.org
        result.contextOrg = result.institution //temp fix

        result
    }

}