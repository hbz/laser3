package de.laser

import de.laser.addressbook.Person
import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.dates.DashboardDueDate
import de.laser.dates.DueDateObject
import de.laser.utils.AppUtils
import de.laser.utils.DateUtils
import de.laser.storage.BeanStore
import de.laser.config.ConfigMapper
import de.laser.storage.RDStore
import de.laser.survey.SurveyInfo
import de.laser.system.SystemEvent
import de.laser.utils.LocaleUtils
import de.laser.utils.SqlDateUtils
import grails.plugins.mail.MailService
import grails.web.mapping.LinkGenerator
import org.springframework.context.MessageSource

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

    DueObjectsService dueObjectsService
    MailService mailService
    LinkGenerator grailsLinkGenerator
    GenericOIDService genericOIDService
    MessageSource messageSource
    EscapeService escapeService

    Locale locale
    String from
    String replyTo
    boolean update_running = false

    /**
     * Initialises the service with configuration parameters
     */
    @javax.annotation.PostConstruct
    void init() {
        from = ConfigMapper.getNotificationsEmailFrom()
        replyTo = ConfigMapper.getNotificationsEmailReplyTo()
        messageSource = BeanStore.getMessageSource()
        locale = LocaleUtils.getCurrentLocale()
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
//                SystemEvent.createEvent('DBDD_SERVICE_START_1')

                // _removeInvalidDueDates()

                if (isUpdateDashboardTableInDatabase) {
                    flash = _updateDashboardTableInDatabase(flash)
                }
                if (isSendEmailsForDueDatesOfAllUsers) {
                    flash = _sendEmailsForDueDatesOfAllUsers(flash)
                }
                log.debug("Finished DashboardDueDatesService takeCareOfDueDates")
//                SystemEvent.createEvent('DBDD_SERVICE_COMPLETE_1')

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

    private int _removeInvalidDueDates() {
        log.debug '_removeInvalidDueDates' // missing fk constraint
        int countInvalid = 0

        DueDateObject.executeQuery('select ddo.id, ddo.oid from DueDateObject ddo order by ddo.id').each {

            if (!genericOIDService.existsOID(it[1])) {
                List<Long> ddd = DashboardDueDate.executeQuery('select ddd.id from DashboardDueDate ddd where ddd.dueDateObject.id = :id', [id: it[0]])
                log.debug 'remove outdated DueDateObject/DashboardDueDate: ' + it + ' <- ' + ddd

                ddd.each { did ->
                    DashboardDueDate.get(did).delete()
                    countInvalid++
                }
                DueDateObject.get(it[0]).delete()
            }
        }
        countInvalid
    }

    /**
     * Updates the {@link DashboardDueDate} table and inserts new reminders for which no reminder exists yet
     * @param flash the message container
     * @return the message container, filled with the processing output
     */
    private _updateDashboardTableInDatabase(def flash){
        SystemEvent sysEvent = SystemEvent.createEvent('DBDD_SERVICE_START_2')

        int countNew = 0, countUpdated = 0, countDeleted = 0, countInvalid = _removeInvalidDueDates()

        Date now = new Date();
        log.debug("Start DashboardDueDatesService updateDashboardTableInDatabase")

        List<DashboardDueDate> dashboarEntriesToInsert = []
        List<User> users = User.findAllByEnabledAndAccountExpiredAndAccountLocked(true, false, false)
        users.each { user ->
            if (user.formalOrg) {
                List dueObjects = dueObjectsService.getDueObjectsByUserSettings(user)
                dueObjects.each { obj ->
                    String attributeName = DashboardDueDate.getAttributeName(obj, user)
                    String oid = genericOIDService.getOID(obj)
                    DashboardDueDate das = DashboardDueDate.executeQuery(
                            """select das from DashboardDueDate as das join das.dueDateObject ddo 
                            where das.responsibleUser = :user and ddo.attribute_name = :attribute_name and ddo.oid = :oid
                            order by ddo.date""",
                            [user: user,
                             attribute_name: attributeName,
                             oid: oid
                            ])[0]
                    // TODO ERMS-5862
//                    das = DashboardDueDate.getByObjectAndAttributeNameAndResponsibleUser(obj, attributeName, user)

                    if (das) { //update
                        das.update(obj)
                        log.debug('DashboardDueDatesService UPDATE: ' + das + ' > ' + obj)
                        countUpdated++
                    }
                    else { // insert
                        das = new DashboardDueDate(obj, user)
                        das.save()
                        dashboarEntriesToInsert << das
                        log.debug('DashboardDueDatesService INSERT: ' + das + ' < ' + obj)
                        countNew++
                    }
                }
            }
        }

        DashboardDueDate.withTransaction { session ->
            try {
                // delete (not-inserted and non-updated entries, they are obsolet)
                countDeleted = DashboardDueDate.executeUpdate("DELETE from DashboardDueDate WHERE lastUpdated < :now and isHidden = false", [now: now])
                log.debug("DashboardDueDatesService DELETES: " + countDeleted)

                flash.message += messageSource.getMessage('menu.admin.updateDashboardTable.successful', null, locale)
            } catch (Throwable t) {
                String tMsg = t.message
                SystemEvent.createEvent('DBDD_SERVICE_ERROR_2', ['error': tMsg])

                session.setRollbackOnly()
                countDeleted = -1
                log.error("DashboardDueDatesService - updateDashboardTableInDatabase() :: Rollback for reason: ${tMsg}")
                flash.error += messageSource.getMessage('menu.admin.updateDashboardTable.error', null, locale)
            }
        }

        log.debug( '_updateDashboardTableInDatabase() -> new: ' + countNew + ', updated: ' + countUpdated + ', deleted: ' + countDeleted + ', INVALID: ' + countInvalid + ')')
        sysEvent.changeTo('DBDD_SERVICE_COMPLETE_2', [new: countNew, updated: countUpdated, deleted: countDeleted, INVALID: countInvalid])

        log.debug("Finished DashboardDueDatesService updateDashboardTableInDatabase")

        flash
    }

    /**
     * Collects the {@link User}s to remind per mail and processes the dashboard entries for each {@link Org} the user is affiliated with
     * @param flash the message collector container
     * @return the message collector container with the processing output
     */
    private _sendEmailsForDueDatesOfAllUsers(def flash) {
        SystemEvent sysEvent = SystemEvent.createEvent('DBDD_SERVICE_START_3')

        int userCount = 0, mailCount = 0
        try {
            List<User> users = User.findAllByEnabledAndAccountExpired(true, false)
            users.each { user ->
                    boolean userWantsEmailReminder = RDStore.YN_YES.equals(user.getSetting(UserSetting.KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO).rdValue)
                    if (userWantsEmailReminder) {
                        if (user.formalOrg) {
                            List<DashboardDueDate> dashboardEntries = getDashboardDueDates(user)
                            if (_sendEmail(user, user.formalOrg, dashboardEntries)) {
                                mailCount++
                            }
                            userCount++
                        }
                    }
            }
            flash.message += messageSource.getMessage('menu.admin.sendEmailsForDueDates.successful', null, locale)
        } catch (Exception e) {
            e.printStackTrace()
            flash.error += messageSource.getMessage('menu.admin.sendEmailsForDueDates.error', null, locale)
        }
        sysEvent.changeTo('DBDD_SERVICE_COMPLETE_3', [users: userCount, mails: mailCount])

        flash
    }

    /**
     * Sends the mail to the given {@link User}; the current affiliation is handed to the mail as well in order to let the user know the context s/he should act
     * @param user the {@link User} who should be reminded
     * @param org the context {@link Org} the object is settled in
     * @param dashboardEntries the {@link List} of {@link DashboardDueDate}s to process
     * @return isMailed
     */
    private boolean _sendEmail(User user, Org org, List<DashboardDueDate> dashboardEntries) {
        boolean isMailed = false

        String emailReceiver = user.getEmail()
        Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RDStore.LANGUAGE_DE).value.toString())
        RefdataValue userLang = user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RDStore.LANGUAGE_DE).value as RefdataValue
        String currentServer = AppUtils.getCurrentServer()
        String subjectSystemPraefix = (currentServer == AppUtils.PROD) ? "LAS:eR - " : (ConfigMapper.getLaserSystemId() + " - ")
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
            List<Map<String, Object>> dueDateRows = []
            dashboardEntries.each { DashboardDueDate dashDueDate ->
                Map<String, Object> dashDueDateRow = [:]
                def obj = genericOIDService.resolveOID(dashDueDate.dueDateObject.oid)
//                def obj = dashDueDate.dueDateObject.getObject() // TODO ERMS-5862

                if (obj) {
                    if(userLang == RDStore.LANGUAGE_DE)
                        dashDueDateRow.valueDate = escapeService.replaceUmlaute(dashDueDate.dueDateObject.attribute_value_de)
                    else dashDueDateRow.valueDate = escapeService.replaceUmlaute(dashDueDate.dueDateObject.attribute_value_en)
                    dashDueDateRow.valueDate += " ${DateUtils.getLocalizedSDF_noTime().format(dashDueDate.dueDateObject.date)}"
                    if(SqlDateUtils.isToday(dashDueDate.dueDateObject.date))
                        dashDueDateRow.importance = '!'
                    else if(SqlDateUtils.isBeforeToday(dashDueDate.dueDateObject.date))
                        dashDueDateRow.importance = '!!'
                    else dashDueDateRow.importance = ' '

                    if(obj instanceof Subscription) {
                        dashDueDateRow.classLabel = messageSource.getMessage('subscription', null, language)
                        dashDueDateRow.link = grailsLinkGenerator.link(controller: 'subscription', action: 'show', id: obj.id, absolute: true)
                        dashDueDateRow.objLabel = obj.name
                    }
                    else if(obj instanceof License) {
                        dashDueDateRow.classLabel = messageSource.getMessage('license.label', null, language)
                        dashDueDateRow.link = grailsLinkGenerator.link([controller: "license", action: "show", id: obj.id, absolute: true])
                        dashDueDateRow.objLabel = obj.reference
                    }
                    else if(obj instanceof Task) {
                        dashDueDateRow.classLabel = messageSource.getMessage('task.label', null, language)
                        dashDueDateRow.link = grailsLinkGenerator.link(obj.getDisplayArgs())
                        String objectName = obj.getObjectName()
                        if(objectName != ''){
                            dashDueDateRow.objLabel = obj.title + ' ('+objectName+')'
                        }else {
                            dashDueDateRow.objLabel = obj.title
                        }
                    }
                    else if (obj instanceof AbstractPropertyWithCalculatedLastUpdated) {
                        dashDueDateRow.classLabel = messageSource.getMessage('attribute', null, language)+': '
                        if (obj.owner instanceof Person) {
                            dashDueDateRow.classLabel += "${messageSource.getMessage('default.person.label', null, language)}: "
                            // dashDueDateRow.link
                            dashDueDateRow.objLabel = obj.owner?.first_name+' '+obj.owner?.last_name
                        }
                        else if (obj.owner instanceof Subscription) {
                            dashDueDateRow.classLabel += "${messageSource.getMessage('subscription', null, language)}: "
                            dashDueDateRow.link = grailsLinkGenerator.link([controller: "subscription", action: "show", id: obj.owner?.id, absolute: true])
                            dashDueDateRow.objLabel = obj.owner?.name
                        }
                        else if(obj.owner instanceof License) {
                            dashDueDateRow.classLabel += "${messageSource.getMessage('license.label', null, language)}: "
                            dashDueDateRow.link = grailsLinkGenerator.link([controller: "license", action: "show", id: obj.owner?.id, absolute: true])
                            dashDueDateRow.objLabel = obj.owner?.reference
                        }
                        else if(obj.owner instanceof Org) {
                            dashDueDateRow.classLabel += "${messageSource.getMessage('org.label', null, language)}: "
                            dashDueDateRow.link = grailsLinkGenerator.link([controller: "organisation", action: "show", id: obj.owner?.id, absolute: true])
                            dashDueDateRow.objLabel = obj.owner?.name
                        }
                        else {
                            dashDueDateRow.classLabel += obj.owner?.name
                        }
                    }
                    else if(obj instanceof SurveyInfo) {
                        dashDueDateRow.classLabel = messageSource.getMessage('survey', null, language)
                        if(obj.owner == org) {
                            dashDueDateRow.link = grailsLinkGenerator.link([controller: "survey", action: "show", id: obj.id, absolute: true])
                        }else {
                            dashDueDateRow.link = grailsLinkGenerator.link([controller: "myInstitution", action: "surveyInfos", id: obj.id, absolute: true, params: [surveyConfigID: obj.surveyConfigs[0].id]])
                        }
                        dashDueDateRow.objLabel = obj.name
                    }
                    else {
                        dashDueDateRow.classLabel = 'Not implemented yet!'
                    }
                }
                dueDateRows << dashDueDateRow
            }
            if (isRemindCCbyEmail && ccAddress) {
                try {
                    mailService.sendMail {
                        to emailReceiver
                        from from
                        cc ccAddress
                        replyTo replyTo
                        subject mailSubject
                        html(view: "/mailTemplates/html/dashboardDueDates", model: [user: user, org: org, dueDates: dueDateRows])
                    }
                    isMailed = true
                }
                catch (Exception e) {
                    log.error "Unable to perform email due to exception: ${e.message}"
                }
            } else {
                try {
                    mailService.sendMail {
                        to emailReceiver
                        from from
                        replyTo replyTo
                        subject mailSubject
                        html(view: "/mailTemplates/html/dashboardDueDates", model: [user: user, org: org, dueDates: dueDateRows])
                    }
                    isMailed = true
                }
                catch (Exception e) {
                    log.error "Unable to perform email due to exception: ${e.message}"
                }
            }
            log.debug("DashboardDueDatesService - finished sendEmail() to "+ user.displayName + " (" + user.email + ") " + org.name);
        }

        isMailed
    }

    /**
     * Gets all due dates of the given user in the given context org; matching hidden=false and done=false
     * @param user the {@link User} whose due dates are to be queried
     * @return a {@link List} of {@link DashboardDueDate} entries to be processed
     */
    List<DashboardDueDate> getDashboardDueDates(User user) {
        DashboardDueDate.executeQuery(
                """select das from DashboardDueDate as das join das.dueDateObject ddo 
                        where das.responsibleUser = :user and das.isHidden = :isHidden and ddo.isDone = :isDone
                        order by ddo.date""",
                [user: user, isHidden: false, isDone: false])
    }

    /**
     * Gets all due dates of the given user in the given context org; matching hidden=false and done=false with pagination
     * @param user the {@link User} whose due dates are to be queried
     * @param max the maximum count of entries to load
     * @param offset the number of entry to start from
     * @return a {@link List} of {@link DashboardDueDate} entries to be processed
     */
    List<DashboardDueDate> getDashboardDueDates(User user, max, offset){
        DashboardDueDate.executeQuery(
                """select das from DashboardDueDate as das join das.dueDateObject ddo 
                where das.responsibleUser = :user and das.isHidden = :isHidden and ddo.isDone = :isDone
                order by ddo.date""",
                [user: user, isHidden: false, isDone: false], [max: max, offset: offset])
    }

    /**
     * Counts the {@link DashboardDueDate}s the responsible user has set, matching hidden=false and done=false
     * @param user the user whose due dates should be counted
     * @return the count of the due dates matching the flags
     */
    int countDashboardDueDates(User user){
        return DashboardDueDate.executeQuery(
                """select count(*) from DashboardDueDate as das join das.dueDateObject ddo 
                where das.responsibleUser = :user and das.isHidden = :isHidden and ddo.isDone = :isDone""",
                [user: user, isHidden: false, isDone: false])[0]
    }

}

