package de.laser

import com.k_int.kbplus.GenericOIDService
import de.laser.auth.User
import de.laser.helper.ConfigUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.helper.ServerUtils
import de.laser.system.SystemEvent
import grails.core.GrailsApplication
import grails.plugins.mail.MailService
import grails.util.Holders
import org.springframework.context.i18n.LocaleContextHolder

/**
 * This service takes care of the due dates: sets up and sends reminders about task end dates, due dates and notice periods linked to subscriptions, surveys or date properties (for organisations, subscriptions, platforms, person contacts or licenses)
 * @see de.laser.properties.SubscriptionProperty#dateValue
 * @see de.laser.properties.OrgProperty#dateValue
 * @see de.laser.properties.LicenseProperty#dateValue
 * @see de.laser.properties.PersonProperty#dateValue
 * @see de.laser.properties.PlatformProperty#dateValue
 * @see Task#endDate
 * @see Subscription#endDate
 * @see Subscription#manualCancellationDate
 * @see SurveyInfo#endDate
 */
//@Transactional
class DashboardDueDatesService {

    QueryService queryService
    MailService mailService
    GrailsApplication grailsApplication
    GenericOIDService genericOIDService
    def messageSource
    EscapeService escapeService
    Locale locale
    String from
    String replyTo
    boolean update_running = false
    private static final String QRY_ALL_ORGS_OF_USER = "select distinct o from Org as o where exists ( select uo from UserOrg as uo where uo.org = o and uo.user = :user) order by o.name"

    @javax.annotation.PostConstruct
    void init() {
        from = ConfigUtils.getNotificationsEmailFrom()
        replyTo = ConfigUtils.getNotificationsEmailReplyTo()
        messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        locale = LocaleContextHolder.getLocale()
        log.debug("Initialised DashboardDueDatesService...")
    }

    /**
     * Triggered by cronjob; may also be triggered by Yoda
     * Starts updating and setting up reminders if their reminder date is reached
     * @param isUpdateDashboardTableInDatabase should the dashboard table being updated?
     * @param isSendEmailsForDueDatesOfAllUsers should reminder mails be sent to the users?
     * @param flash a message container ({@link grails.web.mvc.FlashScope} if request context is given, an empty map otherwise)
     * @return true if succeeded, false otherwise
     */
    boolean takeCareOfDueDates(boolean isUpdateDashboardTableInDatabase, boolean isSendEmailsForDueDatesOfAllUsers, def flash) {
        //workaround to generate messages when FlashScope is missing due to missing request context
        if (flash == null) flash = [:]
        flash.message = ''
        flash.error = ''

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

    /**
     * Updates the {@link DashboardDueDate} table and inserts new reminders for which no reminder exists yet
     * @param flash the message container
     * @return the message container, filled with the processing output
     */
    private updateDashboardTableInDatabase(def flash){
        SystemEvent.createEvent('DBDD_SERVICE_START_2')
        SystemEvent.createEvent('DBDD_SERVICE_START_COLLECT_DASHBOARD_DATA')
        Date now = new Date();
        log.debug("Start DashboardDueDatesService updateDashboardTableInDatabase")

        List<DashboardDueDate> dashboarEntriesToInsert = []
        List<User> users = User.findAllByEnabledAndAccountExpiredAndAccountLocked(true, false, false)
//        List<User> users = [User.get(96)]
        users.each { user ->
            List<Org> orgs = Org.executeQuery(QRY_ALL_ORGS_OF_USER, [user: user])
            orgs.each {org ->
                List dueObjects = queryService.getDueObjectsCorrespondingUserSettings(org, user)
                dueObjects.each { obj ->
                    String attributeName = DashboardDueDate.getAttributeName(obj, user)
                    String oid = genericOIDService.getOID(obj)
                    DashboardDueDate das = DashboardDueDate.executeQuery(
                            """select das from DashboardDueDate as das join das.dueDateObject ddo 
                            where das.responsibleUser = :user and das.responsibleOrg = :org and ddo.attribute_name = :attribute_name and ddo.oid = :oid
                            order by ddo.date""",
                            [user: user,
                             org: org,
                             attribute_name: attributeName,
                             oid: oid
                            ])[0]

                    if (das){//update
                        das.update(messageSource, obj)
                        log.debug("DashboardDueDatesService UPDATE: " + das);
                    } else {//insert
                        das = new DashboardDueDate(messageSource, obj, user, org, false, false)
                        das.save()
                        log.debug("DashboardDueDatesService UPDATE: " + das);
                    }

                }
            }
        }
        SystemEvent.createEvent('DBDD_SERVICE_END_COLLECT_DASHBOARD_DATA', ['count': dashboarEntriesToInsert.size])
        DashboardDueDate.withTransaction { session ->
            SystemEvent.createEvent('DBDD_SERVICE_START_TRANSACTION', ['count': dashboarEntriesToInsert.size])

            try {
                // delete (not-inserted and non-updated entries, they are obsolet)
                int anzDeletes = DashboardDueDate.executeUpdate("DELETE from DashboardDueDate WHERE lastUpdated < :now and isHidden = false", [now: now])
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

    /**
     * Collects the {@link User}s to remind per mail and processes the dashboard entries for each {@link Org} the user is affiliated with
     * @param flash the message collector container
     * @return the message collector container with the processing output
     */
    private sendEmailsForDueDatesOfAllUsers(def flash) {
        try {
            SystemEvent.createEvent('DBDD_SERVICE_START_3')

            List<User> users = User.findAllByEnabledAndAccountExpiredAndAccountLocked(true, false, false)
            users.each { user ->
                boolean userWantsEmailReminder = RDStore.YN_YES.equals(user.getSetting(UserSetting.KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO).rdValue)
                if (userWantsEmailReminder) {
                    List<Org> orgs = Org.executeQuery(QRY_ALL_ORGS_OF_USER, [user: user])
                    orgs.each { org ->
                        List<DashboardDueDate> dashboardEntries = getDashboardDueDates(user, org, false, false)
                        sendEmail(user, org, dashboardEntries)
                    }
                }
            }
            SystemEvent.createEvent('DBDD_SERVICE_COMPLETE_3')

            flash.message += messageSource.getMessage('menu.admin.sendEmailsForDueDates.successful', null, locale)
        } catch (Exception e) {
            println(e)
            flash.error += messageSource.getMessage('menu.admin.sendEmailsForDueDates.error', null, locale)
        }
        flash
    }

    /**
     * Sends the mail to the given {@link User}; the current affiliation is handed to the mail as well in order to let the user know the context s/he should act
     * @param user the {@link User} who should be reminded
     * @param org the context {@link Org} the object is settled in
     * @param dashboardEntries the {@link List} of {@link DashboardDueDate}s to process
     */
    private void sendEmail(User user, Org org, List<DashboardDueDate> dashboardEntries) {
        String emailReceiver = user.getEmail()
        Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE)).value.toString())
        String currentServer = ServerUtils.getCurrentServer()
        String subjectSystemPraefix = (currentServer == ServerUtils.SERVER_PROD) ? "LAS:eR - " : (ConfigUtils.getLaserSystemId() + " - ")
        String mailSubject = escapeService.replaceUmlaute(subjectSystemPraefix + messageSource.getMessage('email.subject.dueDates', null, language) + " (" + org.name + ")")
        if (emailReceiver == null || emailReceiver.isEmpty()) {
            log.debug("The following user does not have an email address and can not be informed about due dates: " + user.username);
        } else if (dashboardEntries == null || dashboardEntries.isEmpty()) {
            log.debug("The user has no due dates, so no email will be sent (" + user.username + "/"+ org.name + ")");
        } else {
            boolean isRemindCCbyEmail = user.getSetting(UserSetting.KEYS.IS_REMIND_CC_BY_EMAIL, RDStore.YN_NO)?.rdValue == RDStore.YN_YES
            String ccAddress = null
            if (isRemindCCbyEmail){
                ccAddress = user.getSetting(UserSetting.KEYS.REMIND_CC_EMAILADDRESS, null)?.getValue()
            }
            if (isRemindCCbyEmail && ccAddress) {
                mailService.sendMail {
                    to      emailReceiver
                    from    from
                    cc      ccAddress
                    replyTo replyTo
                    subject mailSubject
                    html    (view: "/mailTemplates/html/dashboardDueDates", model: [user: user, org: org, dueDates: dashboardEntries])
                }
            } else {
                mailService.sendMail {
                    to      emailReceiver
                    from    from
                    replyTo replyTo
                    subject mailSubject
                    html    (view: "/mailTemplates/html/dashboardDueDates", model: [user: user, org: org, dueDates: dashboardEntries])
                }
            }
            log.debug("DashboardDueDatesService - finished sendEmail() to "+ user.displayName + " (" + user.email + ") " + org.name);
        }
    }

    /**
     * Gets all due dates of the given user in the given context org; query can be limited to hidden (shown) / (un-)done tasks
     * @param user the {@link User} whose due dates are to be queried
     * @param org the {@link Org} whose context is requested
     * @param isHidden are the tasks hidden?
     * @param isDone are the tasts done?
     * @return a {@link List} of {@link DashboardDueDate} entries to be processed
     */
    List<DashboardDueDate> getDashboardDueDates(User user, Org org, isHidden, isDone) {
        List liste = DashboardDueDate.executeQuery(
                """select das from DashboardDueDate as das 
                        join das.dueDateObject ddo 
                        where das.responsibleUser = :user and das.responsibleOrg = :org and das.isHidden = :isHidden and ddo.isDone = :isDone
                        order by ddo.date""",
                [user: user, org: org, isHidden: isHidden, isDone: isDone])
        liste
    }

    /**
     * Gets all due dates of the given user in the given context org; query can be limited to hidden (shown) / (un-)done tasks and paginated
     * @param user the {@link User} whose due dates are to be queried
     * @param org the {@link Org} whose context is requested
     * @param isHidden are the tasks hidden?
     * @param isDone are the tasts done?
     * @param max the maximum count of entries to load
     * @param offset the number of entry to start from
     * @return a {@link List} of {@link DashboardDueDate} entries to be processed
     */
    List<DashboardDueDate> getDashboardDueDates(User user, Org org, isHidden, isDone, max, offset){
        List liste = DashboardDueDate.executeQuery(
                """select das from DashboardDueDate as das join das.dueDateObject ddo 
                where das.responsibleUser = :user and das.responsibleOrg = :org and das.isHidden = :isHidden and ddo.isDone = :isDone
                order by ddo.date""",
                [user: user, org: org, isHidden: isHidden, isDone: isDone], [max: max, offset: offset])

        liste
    }

}

