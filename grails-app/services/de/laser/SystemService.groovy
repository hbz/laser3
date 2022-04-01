package de.laser

import de.laser.helper.AppUtils
import de.laser.helper.ConfigUtils
import de.laser.system.SystemMessage
import de.laser.system.SystemSetting
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils

/**
 * This service checks the system health
 */
@Transactional
class SystemService {

    ContextService contextService

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

    Map getStatus() {
        Map result = [ status: 'error' ]

        try {
            result = [
                    status:      'ok',
                    maintenance: SystemSetting.findByName('MaintenanceMode').value == 'true',
                    messages:    SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION) ? true : false
            ]
        } catch(Exception e) {
            log.error( e.getMessage() )
        }

        result
    }
}
