package de.laser.system

import de.laser.YodaService
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
        YodaService yodaService = (YodaService) Holders.grailsApplication.mainContext.getBean('yodaService')

        withTransaction {
            int userCount = yodaService.getNumberOfActiveUsers()
            if (userCount > 0) {
                new SystemActivityProfiler(userCount: yodaService.getNumberOfActiveUsers()).save()
            }
        }
    }
}
