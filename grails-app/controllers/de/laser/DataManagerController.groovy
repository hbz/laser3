package de.laser

import de.laser.storage.RDConstants
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class DataManagerController  {

    ContextService contextService

    @Secured(['ROLE_ADMIN'])
    def index() {
        Map<String, Object> result = [:]
        result
    }
}
