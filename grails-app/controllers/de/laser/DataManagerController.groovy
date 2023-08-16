package de.laser

import de.laser.storage.RDConstants
import grails.plugin.springsecurity.annotation.Secured

@Deprecated
@Secured(['IS_AUTHENTICATED_FULLY'])
class DataManagerController  {

    ContextService contextService

    @Deprecated
    @Secured(['ROLE_ADMIN'])
    def index() {
        Map<String, Object> result = [:]
        result
    }
}
