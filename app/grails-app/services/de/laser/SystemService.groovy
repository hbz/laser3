package de.laser


import com.k_int.kbplus.ApiSource
import com.k_int.kbplus.GlobalRecordSource
import de.laser.helper.ConfigUtils
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.transaction.Transactional

@Transactional
class SystemService {

    def grailsApplication

    def serviceCheck() {
        def checks = [:]

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

            if (grailsApplication.config.grails.mail.disabled ) {
                checks.MailService = "Mail Service not active"
            }

        }

        return checks
    }
}
