package de.laser.system

import de.laser.SystemService
import de.laser.helper.BeanStore
import grails.util.Holders

/**
 * This class keeps track of the active users for a given time point.
 * It reflects overall activity of users and permits to record when the system is most used
 */
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

    /**
     * This is a cronjob-triggered method to record the next sample of users
     */
    static void update() {
        SystemService systemService = BeanStore.getSystemService()

        SystemActivityProfiler.withTransaction {
            int userCount = systemService.getNumberOfActiveUsers()
            if (userCount > 0) {
                new SystemActivityProfiler(userCount: systemService.getNumberOfActiveUsers()).save()
            }
        }
    }
}
