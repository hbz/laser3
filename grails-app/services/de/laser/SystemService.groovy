package de.laser

import de.laser.helper.ConfigMapper
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

        if(SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA') || ConfigMapper.getShowSystemInfo()) {
            //GlobalData Sync
            if ((GlobalRecordSource.findAllByActive(false)?.size() == GlobalRecordSource.findAll()?.size()) || GlobalRecordSource.findAll()?.size() == 0) {
                checks.globalSync = "NOT active"
            }

            if ((ApiSource.findAllByActive(false)?.size() == ApiSource.findAll()?.size()) || ApiSource.findAll()?.size() == 0) {
                checks.apiSource = "NOT active"
            }

            if (! ConfigMapper.getNotificationsJobActive()) {
                checks.notificationsJobActive = "NOT active"
            }
            if (! ConfigMapper.getGlobalDataSyncJobActive()) {
                checks.globalDataSyncJob = "NOT active"
            }
            if (! ConfigMapper.getIsUpdateDashboardTableInDatabase()) {
                checks.UpdateDashboardTableInDatabase = "NOT active"
            }
            if (! ConfigMapper.getIsSendEmailsForDueDatesOfAllUsers()) {
                checks.SendEmailsForDueDatesOfAllUsers = "NOT active"
            }
            if (! ConfigMapper.getReporting()) {
                checks.Reporting = "ElasticSearch Config for Reporting not found"
            }
            if (ConfigMapper.getConfig('grails.mail.disabled', Boolean)) {
                checks.MailService = "NOT active"
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
