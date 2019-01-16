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
    def messageSource
    def locale
    String from
    String replyTo
    def update_running = false
    private static final String QRY_ALL_ORGS_OF_USER = "select distinct o from Org as o where exists ( select uo from UserOrg as uo where uo.org = o and uo.user = ? and ( uo.status=1 or uo.status=3)) order by o.name"

    @javax.annotation.PostConstruct
    void init() {
        from = grailsApplication.config.notifications.email.from
        replyTo = grailsApplication.config.notifications.email.replyTo
        messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
        log.info("Initialised DashboardDueDatesService...")
    }

    def takeCareOfDueDates(boolean isUpdateDashboardTableInDatabase, boolean isSendEmailsForDueDatesOfAllUsers, def flash) {
        if (flash == null) flash = new HashMap<>()
        if (flash.message == null) flash.put('message', '')
        if (flash.error == null)   flash.put('error', '')

        if ( update_running == true ) {
                log.info("Existing DashboardDueDatesService takeCareOfDueDates - one already running");
            } else {
                try {
                    update_running = true;
                    log.info("Start DashboardDueDatesService takeCareOfDueDates");
                    new EventLog(event:'DashboardDueDatesService takeCareOfDueDates', message:'Start', tstp:new Date(System.currentTimeMillis())).save(flush:true)
                    if (isUpdateDashboardTableInDatabase) {
                        flash= updateDashboardTableInDatabase(flash)
                    }
                    if (isSendEmailsForDueDatesOfAllUsers) {
                        flash = sendEmailsForDueDatesOfAllUsers(flash)
                    }
                    log.info("Finished DashboardDueDatesService takeCareOfDueDates");
                    new EventLog(event:'DashboardDueDatesService takeCareOfDueDates', message:'Finished', tstp:new Date(System.currentTimeMillis())).save(flush:true)
                } catch (Throwable t) {
                    log.error("DashboardDueDatesService takeCareOfDueDates :: Unable to perform email due to exception ${t.message}")
                    new EventLog(event:'DashboardDueDatesService takeCareOfDueDates', message:'Unable to perform email due to exception '+ t.message, tstp:new Date(System.currentTimeMillis())).save(flush:true)
                    flash.error += messageSource.getMessage('menu.admin.error', null, locale)
                    update_running = false
                } finally {
                    update_running = false
                }
            }
        flash
    }
    private String updateDashboardTableInDatabase(def flash){
        log.info("Start DashboardDueDatesService updateDashboardTableInDatabase");
        new EventLog(event:'DashboardDueDatesService.updateDashboardTableInDatabase', message:'Start', tstp:new Date(System.currentTimeMillis())).save(flush:true)
        List<DashboardDueDate> dashboarEntriesToInsert = []
        def users = User.findAllByEnabledAndAccountExpiredAndAccountLocked(true, false, false)
        users.each { user ->
            def orgs = Org.executeQuery(QRY_ALL_ORGS_OF_USER, user);
            int reminderPeriod = user.getSetting(UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14).value
            orgs.each {org ->
                def dueObjects = queryService.getDueObjectsCorrespondingUserSettings(org, user, reminderPeriod)
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
                log.debug("DashboardDueDatesService DELETE from DashboardDueDate");
                dashboarEntriesToInsert.each {
                    it.save(flush: true)
                    log.debug("DashboardDueDatesService INSERT: " + it);
                }
                log.debug("DashboardDueDatesService INSERT Anzahl: " + dashboarEntriesToInsert.size);
                new EventLog(event:'DashboardDueDatesService INSERT Anzahl: ' + dashboarEntriesToInsert.size, message:'SQL Insert', tstp:new Date(System.currentTimeMillis())).save(flush:true)
                flash.message += messageSource.getMessage('menu.admin.updateDashboardTable.successful', null, locale)
            } catch (Throwable t) {
                status.setRollbackOnly()
                log.error("DashboardDueDatesService - updateDashboardTableInDatabase() :: Rollback for reason: ${t.message}")
                new EventLog(event:'DashboardDueDatesService.updateDashboardTableInDatabase', message:'Rollback for reason: ' + t.message, tstp:new Date(System.currentTimeMillis())).save(flush:true)
                flash.error += messageSource.getMessage('menu.admin.updateDashboardTable.error', null, locale)
            }
        }
        log.info("Finished DashboardDueDatesService updateDashboardTableInDatabase");
        new EventLog(event:'DashboardDueDatesService updateDashboardTableInDatabase', message:'Finished', tstp:new Date(System.currentTimeMillis())).save(flush:true)
        flash
    }

    private sendEmailsForDueDatesOfAllUsers(def flash) {
        try {
            new EventLog(event: 'DashboardDueDatesService.sendEmailsForDueDatesOfAllUsers', message: 'Start', tstp: new Date(System.currentTimeMillis())).save(flush: true)
            def users = User.findAllByEnabledAndAccountExpiredAndAccountLocked(true, false, false)
            users.each { user ->
                boolean userWantsEmailReminder = RDStore.YN_YES.equals(user.getSetting(UserSettings.KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO).rdValue)
                if (userWantsEmailReminder) {
                    def orgs = Org.executeQuery(QRY_ALL_ORGS_OF_USER, user);
                    orgs.each { org ->
                        def dashboardEntries = DashboardDueDate.findAllByResponsibleUserAndResponsibleOrg(user, org)
                        sendEmail(user, org, dashboardEntries)
                    }
                }
            }
            new EventLog(event: 'DashboardDueDatesService.sendEmailsForDueDatesOfAllUsers', message: 'Finished', tstp: new Date(System.currentTimeMillis())).save(flush: true)
            flash.message += messageSource.getMessage('menu.admin.sendEmailsForDueDates.successful', null, locale)
        } catch (Exception e) {
            flash.error += messageSource.getMessage('menu.admin.sendEmailsForDueDates.error', null, locale)
        }
        flash
    }

    private void sendEmail(User user, Org org, List<DashboardDueDate> dashboardEntries) {
        def emailReceiver = user.getEmail()
        def currentServer = grailsApplication.config.getCurrentServer()
        def subjectSystemPraefix = (currentServer == ContextService.SERVER_PROD)? "LAS:eR - " : (grailsApplication.config.laserSystemId + " - ")
        String mailSubject = subjectSystemPraefix + messageSource.getMessage('email.subject.dueDates', null, locale) + " (" + org.name + ")"
        try {
            if (emailReceiver == null || emailReceiver.isEmpty()) {
                log.info("The following user does not have an email address and can not be informed about due dates: " + user.username);
            } else if (dashboardEntries == null || dashboardEntries.isEmpty()) {
                log.info("The user has no due dates, so no email will be sent (" + user.username + "/"+ org.name + ")");
            } else {

                mailService.sendMail {
                    to      emailReceiver
                    from    from
                    replyTo replyTo
                    subject mailSubject
                    body    (view: "/user/_emailDueDatesView", model: [user: user, org: org, dueDates: dashboardEntries])
                }
                log.debug("DashboardDueDatesService - finished sendEmail() to "+ user.displayName + " (" + user.email + ") " + org.name);
            }
        } catch (Exception e) {
            log.error("DashboardDueDatesService - sendEmail() :: Unable to perform email due to exception ${e.message}")
            new EventLog(event:'DashboardDueDatesService.sendEmail', message:'Unable to perform email due to exception ' + e.message, tstp:new Date(System.currentTimeMillis())).save(flush:true)
            throw e
        }
    }
}

