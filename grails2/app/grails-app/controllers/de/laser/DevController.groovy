package de.laser

import de.laser.controller.AbstractDebugController
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class DevController extends AbstractDebugController {

    def contextService
    def springSecurityService

    @Secured(['ROLE_ADMIN'])
    def frontend() {
        Map<String, Object> result = [test:123]
        result
    }
}
