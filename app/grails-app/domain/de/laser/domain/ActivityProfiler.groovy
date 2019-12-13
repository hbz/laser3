package de.laser.domain

import de.laser.YodaService
import grails.util.Holders

class ActivityProfiler {

    Integer userCount
    Date dateCreated

    static mapping = {
        id          column:'ap_id'
        version     column:'ap_version'
        userCount   column:'ap_user_count'
        dateCreated column:'ap_date_created'
    }

    static constraints = {
        userCount   (nullable:false, blank:false)
    }

    static void update() {
        YodaService yodaService = (YodaService) Holders.grailsApplication.mainContext.getBean('yodaService')

        int userCount = yodaService.getNumberOfActiveUsers()
        if (userCount > 0) {
            new ActivityProfiler(userCount: yodaService.getNumberOfActiveUsers()).save()
        }
    }
}
