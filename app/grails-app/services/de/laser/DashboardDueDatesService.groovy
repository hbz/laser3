package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.properties.PropertyDefinition
import de.laser.domain.StatsTripleCursor
import de.laser.helper.SqlDateUtils
import groovyx.gpars.GParsPool
import groovyx.net.http.RESTClient
import groovyx.net.http.URIBuilder
import org.hibernate.Transaction
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import java.text.SimpleDateFormat

import static groovyx.net.http.ContentType.ANY

class DashboardDueDatesService {

    def queryService
    def mailService
    def grailsApplication
    String from
    String replyTo
    def update_running = false

    @javax.annotation.PostConstruct
    void init() {
        from = grailsApplication.config.notifications.email.from
        replyTo = grailsApplication.config.notifications.email.replyTo
        log.debug("Initialised DashboardDueDatesService...")
    }

    def takeCareOfDueDates() {
        synchronized(this) {
            if ( update_running == true ) {
                log.debug("Exiting DashboardDueDatesService takeCareOfDueDates - one already running");
                return
            }
            else {
                update_running = true;
                log.debug("Start DashboardDueDatesService takeCareOfDueDates");
                updateDashboardTableInDatabase(false)
                log.debug("Finished DashboardDueDatesService takeCareOfDueDates");
            }
        }
    }

    //TODO beim Rollback Eintrag in die Logdatei
    @Transactional(propagation= Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    def updateDashboardTableInDatabase(boolean isSendEmail){
        try{

        DashboardDueDate.executeUpdate("DELETE from DashboardDueDate ")

        def users = User.getAll()
        users.each { user ->
            def userOrgs = UserOrg.findAllByUser(user)
            int reminderPeriod = user.getSetting(UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14).value
            userOrgs.each {userOrg ->
                def org = userOrg.getOrg()
                HashSet dueObjects = queryService.getDueObjects(org, user, reminderPeriod)
                List<DashboardDueDate> dashbordEntries = []
                dueObjects.each { obj ->
                    if (obj instanceof Subscription) {
                        if (obj.manualCancellationDate && SqlDateUtils.isDateBetweenTodayAndReminderPeriod(obj.manualCancellationDate, reminderPeriod)) {
                            DashboardDueDate dashEntry = new DashboardDueDate(obj, true, user, org)
                            dashEntry.save(flush: true)
                            dashbordEntries.add(dashEntry)
                        }
                        if (obj.endDate && SqlDateUtils.isDateBetweenTodayAndReminderPeriod(obj.endDate, reminderPeriod)) {
                            DashboardDueDate dashEntry = new DashboardDueDate(obj, false, user, org)
                            dashEntry.save(flush: true)
                            dashbordEntries.add(dashEntry)
                        }
                    } else {
                        DashboardDueDate dashEntry = new DashboardDueDate(obj, user, org)
                        dashEntry.save(flush: true)
                        dashbordEntries.add(dashEntry)
                    }
                }
                if (isSendEmail && user.getSetting(UserSettings.KEYS.IS_REMIND_BY_EMAIL, RefdataValue.getByValueAndCategory('Yes','YN'))) {
                    sendEmailWithDashboardToUser(user, dashbordEntries)
                    log.debug("Start DashboardDueDatesService sendEmail to "+ user.getDisplayName() + user.email);
                }
            }
        }
        } catch (Throwable t) {
            log.debug("Bei DashboardDueDatesService.updateDashboardTableInDatabase ist ein Fehler aufgetreten: " + t.getMessage());
        }
    }

    def sendEmailWithDashboardToUser(User user, List<DashboardDueDate> dashboardEntries) {
        def emailReceiver = user.getEmail()
        def subject = "LAS:eR - Erinnerung an Ihre fälligen Termine"
        sendEmail(emailReceiver, subject, dashboardEntries, null, null)
    }

    def sendEmail(userAddress, subjectTrigger, dashboardEntries, overrideReplyTo, overrideFrom) {
        try {
            mailService.sendMail {
                to userAddress
                from overrideFrom != null ? overrideFrom : from
                replyTo overrideReplyTo != null ? overrideReplyTo : replyTo
                subject subjectTrigger
                body(view: "/user/_emailDueDatesView", model: [dashboardEntries: dashboardEntries])
            }
        } catch (Exception e) {
            log.error("DashboardDueDatesService - mailReminder() :: Unable to perform email due to exception ${e.message}")
        }
    }
}

