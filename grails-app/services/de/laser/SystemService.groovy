package de.laser

import de.laser.helper.AppUtils
import de.laser.helper.ConfigUtils
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.core.session.SessionRegistryImpl

/**
 * This service checks the system health
 */
@Transactional
class SystemService {

    ContextService contextService
    SessionRegistryImpl sessionRegistry

    /**
     * Dumps the state of currently active services
     * @return a map with the services marked as active by configuration or with sources marked as active and their running state
     */
    Map<String, Object> serviceCheck() {
        Map<String, Object> checks = [:]

        if(SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA') || ConfigUtils.getShowSystemInfo()) {
            //GlobalData Sync
            if ((GlobalRecordSource.findAllByActive(false)?.size() == GlobalRecordSource.findAll()?.size()) || GlobalRecordSource.findAll()?.size() == 0) {
                checks.globalSync = "Global Record Source is not active"
            }

            if ((ApiSource.findAllByActive(false)?.size() == ApiSource.findAll()?.size()) || ApiSource.findAll()?.size() == 0) {
                checks.apiSource = "Api Source is not active"
            }

            if (! ConfigUtils.getNotificationsJobActive()) {
                checks.notificationsJobActive = "NotificationsJob is not active"
            }
            if (! ConfigUtils.getGlobalDataSyncJobActiv()) {
                checks.globalDataSyncJob = "global Data Sync Job is not active"
            }
            if (! ConfigUtils.getIsUpdateDashboardTableInDatabase()) {
                checks.UpdateDashboardTableInDatabase = "Update Dashboard Table In Database is not active"
            }
            if (! ConfigUtils.getIsSendEmailsForDueDatesOfAllUsers()) {
                checks.SendEmailsForDueDatesOfAllUsers = "Send Emails for DueDates Of All Users is not active"
            }
            if (! ConfigUtils.getReporting()) {
                checks.Reporting = "ElasticSearch Config for Reporting not found"
            }

            if (AppUtils.getConfig('grails.mail.disabled')) {
                checks.MailService = "Mail Service not active"
            }

        }

        return checks
    }

    /**
     * Returns the current count of active sessions by last access time
     * @return the number of active users
     */
    int getNumberOfActiveUsers() {
        getActiveUsers( (1000 * 60 * 10) ).size() // 10 minutes
    }

    /**
     * Helper method to determine the count of active users by the last access count
     * @param ms maximum number of milliseconds elapsed since last action
     * @return a list of users having done any action in the given span of time
     */
    List getActiveUsers(long ms) {
        List result = []

        sessionRegistry.getAllPrincipals().each { user ->
            List lastAccessTimes = []

            sessionRegistry.getAllSessions(user, false).each { userSession ->
                if (user.username == contextService.getUser()?.username) {
                    userSession.refreshLastRequest()
                }
                lastAccessTimes << userSession.getLastRequest().getTime()
            }
            if (lastAccessTimes.max() > System.currentTimeMillis() - ms) {
                result.add(user)
            }
        }
        result
    }
}
