package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.properties.PropertyDefinition
import de.laser.domain.StatsTripleCursor
import de.laser.helper.RDStore
import de.laser.helper.SqlDateUtils
import grails.util.Holders
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
    def executorService
    def grailsApplication
    String from
    String replyTo
    def update_running = false
    private static final String QRY_ALL_ORGS_OF_USER = "select distinct o from Org as o where exists ( select uo from UserOrg as uo where uo.org = o and uo.user = ? and ( uo.status=1 or uo.status=3)) order by o.name"

    @javax.annotation.PostConstruct
    void init() {
        from = grailsApplication.config.notifications.email.from
        replyTo = grailsApplication.config.notifications.email.replyTo
        log.info("Initialised DashboardDueDatesService...")
    }

    public void takeCareOfDueDates(boolean isUpdateDashboardTableInDatabase, boolean isSendEmailsForDueDatesOfAllUsers) {
//        synchronized(this) {
            if ( update_running == true ) {
                log.info("Exiting DashboardDueDatesService takeCareOfDueDates - one already running");
                return
            } else {
                try {
                    update_running = true;
                    log.info("Start DashboardDueDatesService takeCareOfDueDates");
                    if (isUpdateDashboardTableInDatabase) {
                        updateDashboardTableInDatabase()
                    }
                    if (isSendEmailsForDueDatesOfAllUsers) {
                        sendEmailsForDueDatesOfAllUsers()
                    }
                    log.info("Finished DashboardDueDatesService takeCareOfDueDates");
                } catch (Throwable t) {
                    log.error("DashboardDueDatesService - takeCareOfDueDates() :: Unable to perform email due to exception ${t.message}")
                    update_running = false
                } finally {
                    update_running = false
                }
            }
//        }
    }
    private void updateDashboardTableInDatabase(){
        List<DashboardDueDate> dashboarEntriesToInsert = []
        def users = User.findAllByEnabledAndAccountExpiredAndAccountLocked(true, false, false)
        users.each { user ->
            def orgs = Org.executeQuery(QRY_ALL_ORGS_OF_USER, user);
            int reminderPeriod = user.getSetting(UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14).value
            orgs.each {org ->
                def dueObjects = queryService.getDueObjects(org, user, reminderPeriod)
                dueObjects.each { obj ->
                    if (obj instanceof Subscription) {
                        if (obj.manualCancellationDate && SqlDateUtils.isDateBetweenTodayAndReminderPeriod(obj.manualCancellationDate, reminderPeriod)) {
                            dashboarEntriesToInsert.add(new DashboardDueDate(obj, true, user, org))
                        }
                        if (obj.endDate && SqlDateUtils.isDateBetweenTodayAndReminderPeriod(obj.endDate, reminderPeriod)) {
                            dashboarEntriesToInsert.add(new DashboardDueDate(obj, false, user, org))
                        }
                    } else {
                        dashboarEntriesToInsert.add(new DashboardDueDate(obj, user, org))
                    }
                }
            }
        }
        DashboardDueDate.withTransaction { status ->
            try {
                DashboardDueDate.executeUpdate("DELETE from DashboardDueDate ")
                dashboarEntriesToInsert.each { it.save(flush: true) }
            } catch (Throwable t) {
                log.error("DashboardDueDatesService - updateDashboardTableInDatabase() :: Rollback for reason: ${t.message}")
                status.setRollbackOnly()
            }
        }
    }

    private void sendEmailsForDueDatesOfAllUsers() {
        def users = User.findAllByEnabledAndAccountExpiredAndAccountLocked(true, false, false)
        users.each { user ->
            boolean userWantsEmailReminder = RDStore.YN_YES.equals(user.getSetting(UserSettings.KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO).rdValue)
            if (userWantsEmailReminder) {
                def orgs = Org.executeQuery(QRY_ALL_ORGS_OF_USER, user);
                orgs.each { org ->
                    def dashboardEntries = DashboardDueDate.findAllByResponsibleUserAndResponsibleOrg(user, org)
                    sendEmailWithDashboardToUser(user, org, dashboardEntries)
                }
            }
        }
    }

    private void sendEmailWithDashboardToUser(User user, Org org, List<DashboardDueDate> dashboardEntries) {
        def emailReceiver = user.getEmail()
        def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        def locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
        String subject = messageSource.getMessage('email.subject.dueDates', null, locale) + " (" + org.name + ")"
        sendEmail(emailReceiver, subject, dashboardEntries, null, null, user, org)
    }

    private void sendEmail(userAddress, subjectTrigger, dashboardEntries, overrideReplyTo, overrideFrom, user, org) {
        try {
            if (userAddress == null || userAddress.isEmpty()) {
                log.info("The following user does not have an email address and can not be informed about due dates: " + user.username);
            } else if (dashboardEntries == null || dashboardEntries.isEmpty()) {
                log.info("The user has no due dates, so no email will be sent (" + user.username + "/"+ org.name + ")");
            } else {
                mailService.sendMail {
                    to userAddress
                    from overrideFrom != null ? overrideFrom : from
                    replyTo overrideReplyTo != null ? overrideReplyTo : replyTo
                    subject subjectTrigger
                    body(view: "/user/_emailDueDatesView", model: [user: user, org: org, dueDates: dashboardEntries])
                }
                log.info("DashboardDueDatesService - sendEmail() finished to "+ user.getDisplayName() + " (" + user.email + ") " + org.name);
            }
        } catch (Exception e) {
            log.error("DashboardDueDatesService - sendEmail() :: Unable to perform email due to exception ${e.message}")
        }
    }
}

