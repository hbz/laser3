package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.UserSettings
import com.k_int.kbplus.auth.User
import static de.laser.helper.RDStore.*
import de.laser.helper.SqlDateUtils
import static com.k_int.kbplus.UserSettings.DEFAULT_REMINDER_PERIOD
import grails.util.Holders

class DashboardDueDatesService {

    def queryService
    def mailService
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
        log.debug("Initialised DashboardDueDatesService...")
    }

    boolean takeCareOfDueDates(boolean isUpdateDashboardTableInDatabase, boolean isSendEmailsForDueDatesOfAllUsers, def flash) {
        if (flash == null) flash = new HashMap<>()
        if (flash.message == null) flash.put('message', '')
        if (flash.error == null)   flash.put('error', '')

        if ( update_running ) {
                log.info("Existing DashboardDueDatesService takeCareOfDueDates - one already running");
                return false
        } else {
            try {
                update_running = true;
                log.debug("Start DashboardDueDatesService takeCareOfDueDates")
                SystemEvent.createEvent('DBDD_SERVICE_START_1')

                if (isUpdateDashboardTableInDatabase) {
                    flash = updateDashboardTableInDatabase(flash)
                }
                if (isSendEmailsForDueDatesOfAllUsers) {
                    flash = sendEmailsForDueDatesOfAllUsers(flash)
                }
                log.debug("Finished DashboardDueDatesService takeCareOfDueDates")
                SystemEvent.createEvent('DBDD_SERVICE_COMPLETE_1')

            } catch (Throwable t) {
                String tMsg = t.message
                log.error("DashboardDueDatesService takeCareOfDueDates :: Unable to perform takeCareOfDueDates due to exception ${tMsg}")
                SystemEvent.createEvent('DBDD_SERVICE_ERROR_1', ['error': tMsg])

                flash.error += messageSource.getMessage('menu.admin.error', null, locale)
                update_running = false
            } finally {
                update_running = false
            }
            return true
        }
    }
    private updateDashboardTableInDatabase(def flash){
        SystemEvent.createEvent('DBDD_SERVICE_START_2')
        SystemEvent.createEvent('DBDD_SERVICE_START_COLLECT_DASHBOARD_DATA')
        Date now = new Date();
        log.debug("Start DashboardDueDatesService updateDashboardTableInDatabase")

        List<DashboardDueDate> dashboarEntriesToInsert = []
        def users = User.findAllByEnabledAndAccountExpiredAndAccountLocked(true, false, false)
//        def users = [User.get(6)]
        users.each { user ->
            def orgs = Org.executeQuery(QRY_ALL_ORGS_OF_USER, user);
            orgs.each {org ->
                def dueObjects = queryService.getDueObjectsCorrespondingUserSettings(org, user)
                dueObjects.each { obj ->
                    if (obj instanceof Subscription) {
                        int reminderPeriodForManualCancellationDate = user.getSetting(UserSettings.KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD, DEFAULT_REMINDER_PERIOD).value ?: 1
                        if (obj.manualCancellationDate && SqlDateUtils.isDateBetweenTodayAndReminderPeriod(obj.manualCancellationDate, reminderPeriodForManualCancellationDate)) {
                            dashboarEntriesToInsert.add(new DashboardDueDate(messageSource, obj, true, user, org, false, false))
                        }
                        int reminderPeriodForSubsEnddate = user.getSetting(UserSettings.KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE, DEFAULT_REMINDER_PERIOD).value ?: 1
                        if (obj.endDate && SqlDateUtils.isDateBetweenTodayAndReminderPeriod(obj.endDate, reminderPeriodForSubsEnddate)) {
                            dashboarEntriesToInsert.add(new DashboardDueDate(messageSource, obj, false, user, org, false, false))
                        }
                    } else {
                        dashboarEntriesToInsert.add(new DashboardDueDate(messageSource, obj, user, org, false, false))
                    }
                }
            }
        }
        SystemEvent.createEvent('DBDD_SERVICE_END_COLLECT_DASHBOARD_DATA', ['count': dashboarEntriesToInsert.size])
        DashboardDueDate.withTransaction { session ->
            SystemEvent.createEvent('DBDD_SERVICE_START_TRANSACTION', ['count': dashboarEntriesToInsert.size])

            try {

                dashboarEntriesToInsert.each { DashboardDueDate newDueDate ->
                    //update
                    int anzUpdates = DashboardDueDate.executeUpdate("""UPDATE DashboardDueDate 
                        SET version = ((select version from DashboardDueDate WHERE attribute_name = :attribute_name 
                        AND oid = :oid 
                        AND responsibleOrg = :org 
                        AND responsibleUser = :user ) + 1), 
                        date = :date, 
                        lastUpdated = :now, 
                        attribute_value_de = :attribute_value_de, 
                        attribute_value_en = :attribute_value_en
                        WHERE attribute_name = :attribute_name 
                        AND oid = :oid 
                        AND responsibleOrg = :org 
                        AND responsibleUser = :user""",
                            [
                                    date: newDueDate.date,
                                    now: now,
                                    attribute_value_de: newDueDate.attribute_value_de,
                                    attribute_value_en: newDueDate.attribute_value_en,
                                    attribute_name: newDueDate.attribute_name,
                                    oid: newDueDate.oid,
                                    org: newDueDate.responsibleOrg,
                                    user: newDueDate.responsibleUser
                            ])

                    if (anzUpdates == 1) {
                        log.debug("DashboardDueDatesService UPDATE: " + newDueDate);
                    //insert if not exist
                    } else if (anzUpdates < 1){
                        newDueDate.save(flush: true)
                        log.debug("DashboardDueDatesService INSERT: " + newDueDate);
                    } else if (anzUpdates > 1){
                        log.error("DashboardDueDate Error: Update "+anzUpdates+" records! It should be 0 or 1 record! "+ newDueDate.toString())
                    }
                }
                // delete (not-inserted and non-updated entries, they are obsolet)
                int anzDeletes = DashboardDueDate.executeUpdate("DELETE from DashboardDueDate WHERE lastUpdated < :now and isDone = false and isHidden = false", [now: now])
                log.debug("DashboardDueDatesService DELETES: " + anzDeletes);

                log.debug("DashboardDueDatesService INSERT Anzahl: " + dashboarEntriesToInsert.size)
                SystemEvent.createEvent('DBDD_SERVICE_END_TRANSACTION', ['count': dashboarEntriesToInsert.size])
                SystemEvent.createEvent('DBDD_SERVICE_PROCESSING_2', ['count': dashboarEntriesToInsert.size])

                flash.message += messageSource.getMessage('menu.admin.updateDashboardTable.successful', null, locale)
            } catch (Throwable t) {
                String tMsg = t.message
                SystemEvent.createEvent('DBDD_SERVICE_ERROR_2', ['error': tMsg])

                session.setRollbackOnly()
                log.error("DashboardDueDatesService - updateDashboardTableInDatabase() :: Rollback for reason: ${tMsg}")

                flash.error += messageSource.getMessage('menu.admin.updateDashboardTable.error', null, locale)
            }
        }
        log.debug("Finished DashboardDueDatesService updateDashboardTableInDatabase")
        SystemEvent.createEvent('DBDD_SERVICE_COMPLETE_2')

        flash
    }

    private sendEmailsForDueDatesOfAllUsers(def flash) {
        try {
            SystemEvent.createEvent('DBDD_SERVICE_START_3')

            def users = User.findAllByEnabledAndAccountExpiredAndAccountLocked(true, false, false)
            users.each { user ->
                boolean userWantsEmailReminder = YN_YES.equals(user.getSetting(UserSettings.KEYS.IS_REMIND_BY_EMAIL, YN_NO).rdValue)
                if (userWantsEmailReminder) {
                    def orgs = Org.executeQuery(QRY_ALL_ORGS_OF_USER, user);
                    orgs.each { org ->
                        def dashboardEntries = DashboardDueDate.findAllByResponsibleUserAndResponsibleOrgAndIsDoneAndIsHidden(user, org, false, false, [sort: "date", order: "asc"])
                        sendEmail(user, org, dashboardEntries)
                    }
                }
            }
            SystemEvent.createEvent('DBDD_SERVICE_COMPLETE_3')

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
                log.debug("The following user does not have an email address and can not be informed about due dates: " + user.username);
            } else if (dashboardEntries == null || dashboardEntries.isEmpty()) {
                log.debug("The user has no due dates, so no email will be sent (" + user.username + "/"+ org.name + ")");
            } else {
                boolean isRemindCCbyEmail = user.getSetting(UserSettings.KEYS.IS_REMIND_CC_BY_EMAIL, YN_NO)?.rdValue == YN_YES
                String ccAddress = null
                if (isRemindCCbyEmail){
                    ccAddress = user.getSetting(UserSettings.KEYS.REMIND_CC_EMAILADDRESS, null)?.getValue()
                }
                if (isRemindCCbyEmail && ccAddress) {
                    mailService.sendMail {
                        to      emailReceiver
                        from    from
                        cc      ccAddress
                        replyTo replyTo
                        subject mailSubject
                        body    (view: "/mailTemplates/html/dashboardDueDates", model: [user: user, org: org, dueDates: dashboardEntries])
                    }
                } else {
                    mailService.sendMail {
                        to      emailReceiver
                        from    from
                        replyTo replyTo
                        subject mailSubject
                        body    (view: "/mailTemplates/html/dashboardDueDates", model: [user: user, org: org, dueDates: dashboardEntries])
                    }
                }

                log.debug("DashboardDueDatesService - finished sendEmail() to "+ user.displayName + " (" + user.email + ") " + org.name);
            }
        } catch (Exception e) {
            String eMsg = e.message

            log.error("DashboardDueDatesService - sendEmail() :: Unable to perform email due to exception ${eMsg}")
            SystemEvent.createEvent('DBDD_SERVICE_ERROR_3', ['error': eMsg])
        }
    }
}

