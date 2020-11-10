package de.laser.system

import de.laser.SystemService
import grails.util.Holders

class SystemActivityProfiler {

    Integer userCount
    Date dateCreated

    static mapping = {
        id          column:'sap_id'
        version     false
        userCount   column:'sap_user_count'
        dateCreated column:'sap_date_created'
    }

    static constraints = { }

    static void update() {
        SystemService systemService = (SystemService) Holders.grailsApplication.mainContext.getBean('systemService')

        SystemActivityProfiler.withTransaction {
            int userCount = systemService.getNumberOfActiveUsers()
            if (userCount > 0) {
                new SystemActivityProfiler(userCount: systemService.getNumberOfActiveUsers()).save()
            }
        }
    }
}
