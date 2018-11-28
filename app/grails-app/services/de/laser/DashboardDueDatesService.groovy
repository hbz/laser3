package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.properties.PropertyDefinition
import de.laser.domain.StatsTripleCursor
import de.laser.helper.RDStore
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
    private static final String QRY_ALL_ORGS_OF_USER = "select distinct o from Org as o where exists ( select uo from UserOrg as uo where uo.org = o and uo.user = ? and ( uo.status=1 or uo.status=3)) order by o.name"

    @javax.annotation.PostConstruct
    void init() {
        from = grailsApplication.config.notifications.email.from
        replyTo = grailsApplication.config.notifications.email.replyTo
        log.info("Initialised DashboardDueDatesService...")
    }

    public void takeCareOfDueDates() {
        synchronized(this) {
            if ( update_running == true ) {
                log.info("Exiting DashboardDueDatesService takeCareOfDueDates - one already running");
                return
            }
            else {
                update_running = true;
                log.info("Start DashboardDueDatesService takeCareOfDueDates");
                updateDashboardTableInDatabase(true)
                log.info("Finished DashboardDueDatesService takeCareOfDueDates");
            }
        }
    }
    //TODO Mails werden nach localhost Port 30 geschickt, siehe Einstellung unter grails-app/conf/Config.groovy
    //TODO Rollback überprüfen
//    @Transactional(propagation= Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public void updateDashboardTableInDatabase(boolean isSendEmail){
        try{
            DashboardDueDate.executeUpdate("DELETE from DashboardDueDate ")
            def users = User.findAllByEnabledAndAccountExpiredAndAccountLocked(true, false, false)
            users.each { user ->
                def orgs = Org.executeQuery(QRY_ALL_ORGS_OF_USER, user);
                int reminderPeriod = user.getSetting(UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14).value
                orgs.each {org ->
                    def dueObjects = queryService.getDueObjects(org, user, reminderPeriod)
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
                    boolean userWantsEmailReminder = RDStore.YN_YES.equals(user.getSetting(UserSettings.KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO).rdValue)
                    if (isSendEmail && userWantsEmailReminder){
                        sendEmailWithDashboardToUser(user, org, dashbordEntries)
                    }
                }
            }
        } catch (Throwable t) {
            log.error("Bei DashboardDueDatesService.updateDashboardTableInDatabase ist ein Fehler aufgetreten: " + t.getMessage());
//            log.error("Bei DashboardDueDatesService.updateDashboardTableInDatabase Transaction Rollback");
            throw t
        }
    }

    public void sendEmailsForDueDatesOfAllUsers() {
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
        def subject = "LAS:eR - Fälligen Termine ("+org.name+")"
        sendEmail(emailReceiver, subject, dashboardEntries, null, null, user, org)
    }

    private void sendEmail(emailReceiver, subject, dashboardEntries, overrideReplyTo, overrideFrom, user, org) {
        try {
            if (emailReceiver == null || emailReceiver.isEmpty()) {
                log.info("Folgender Benutzer hat keine Emailadresse hinterlegt und kann nicht über fällige Termine informiert werden: " + user.username);
            } else {
                mailService.sendMail {
                    to emailReceiver
                    from overrideFrom != null ? overrideFrom : from
                    replyTo overrideReplyTo != null ? overrideReplyTo : replyTo
                    subject subject
                    body(view: "/user/_emailDueDatesView", model: [user: user, org: org, dueDates: dashboardEntries])
                }
                log.info("SendEmail finished to "+ user.getDisplayName() + " - " + user.email);
            }
        } catch (Exception e) {
            log.error("DashboardDueDatesService - sendEmail() :: Unable to perform email due to exception ${e.message}")
        }
    }
}

