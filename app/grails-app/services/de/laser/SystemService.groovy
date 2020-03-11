package de.laser


import com.k_int.kbplus.ApiSource
import com.k_int.kbplus.GlobalRecordSource
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.transaction.Transactional

@Transactional
class SystemService {

    def grailsApplication

    def serviceCheck() {
        def checks = [:]

        if(SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA') || grailsApplication.config.showSystemInfo) {
            //GlobalData Sync
            if ((GlobalRecordSource.findAllByActive(false)?.size() == GlobalRecordSource.findAll()?.size()) || GlobalRecordSource.findAll()?.size() == 0) {
                checks.globalSync = "Global Record Source is not active"
            }

            if ((ApiSource.findAllByActive(false)?.size() == ApiSource.findAll()?.size()) || ApiSource.findAll()?.size() == 0) {
                checks.apiSource = "Api Source is not active"
            }

            if (!grailsApplication.config.notificationsJobActive) {
                checks.notificationsJobActive = "NotificationsJob is not active"
            }
            if (!grailsApplication.config.globalDataSyncJobActiv) {
                checks.globalDataSyncJob = "global Data Sync Job is not active"
            }
            if (!grailsApplication.config.feature_finance) {
                checks.featureFinance = "Feature Finance is not active"
            }

            if (!grailsApplication.config.featureSurvey) {
                checks.featureSurvey = "Feature Survey is not active"
            }

            if (!grailsApplication.config.isUpdateDashboardTableInDatabase) {
                checks.UpdateDashboardTableInDatabase = "Update Dashboard Table In Database is not active"
            }
            if (!grailsApplication.config.isSendEmailsForDueDatesOfAllUsers) {
                checks.SendEmailsForDueDatesOfAllUsers = "Send Emails for DueDates Of All Users is not active"
            }

            if (grailsApplication.config.grails.mail.disabled ) {
                checks.MailService = "Mail Service not active"
            }

        }

        return checks
    }
}
