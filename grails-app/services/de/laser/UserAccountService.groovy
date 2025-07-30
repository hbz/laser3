package de.laser

import de.laser.auth.User
import de.laser.config.ConfigMapper
import de.laser.system.SystemEvent
import de.laser.utils.DateUtils
import grails.gorm.transactions.Transactional
import grails.plugin.asyncmail.AsynchronousMailService

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * This service handles generic user-related matters
 */
@Transactional
class UserAccountService {

    public static final int INACTIVITY_WARNING_AFTER_MONTHS = 6

    public static final int FLAG_EXPIRED_AFTER_ANOTHER_MONTHS = 1
    public static final int FLAG_LOCKED_AFTER_INVALID_ATTEMPTS = 5
    public static final int FLAG_UNLOCKED_AFTER_MINUTES = 20

    AsynchronousMailService asynchronousMailService
    ContextService contextService

    void warnInactiveUserAccounts() {
        List expiringAccounts = []
        LocalDate now = LocalDate.now()

        // not expired and not warned - but inactivity since INACTIVITY_WARNING_AFTER_MONTHS
        User.executeQuery("select u from User u where u.accountExpired = false and u.inactivityWarning is null and u.username != 'anonymous' order by u.username").each{ User usr ->
            LocalDate lastLogin = usr.lastLogin ? DateUtils.dateToLocalDate(usr.lastLogin) : DateUtils.dateToLocalDate(usr.dateCreated)
            // TODO: ignore INST_ADM ?
            if (lastLogin.isBefore(now.minusMonths(INACTIVITY_WARNING_AFTER_MONTHS))) {
                usr.inactivityWarning = new Date()
                usr.save()
                expiringAccounts.add([usr.id, usr.username, usr.lastLogin ? DateUtils.getLocalizedSDF_noZ().format(usr.lastLogin) : null])

                try {
                    asynchronousMailService.sendMail {
                        to      usr.getEmail()
                        from    ConfigMapper.getNotificationsEmailFrom()
                        replyTo ConfigMapper.getNotificationsEmailReplyTo()
                        subject ConfigMapper.getLaserSystemId() + ' - Deaktivierung Ihres Accounts'
                        body    (view: '/mailTemplates/text/inactivityWarning', model: [user:usr, serverURL:ConfigMapper.getGrailsServerURL()])
                    }
                }
                catch (Exception e) {
                    log.error 'warnInactiveUserAccounts > sendMail exception: ' + e.message
                }
            }
        }

        if (expiringAccounts) {
            log.info '--> warnInactiveUserAccounts after ' + INACTIVITY_WARNING_AFTER_MONTHS + ' months: ' + expiringAccounts.size()
            SystemEvent.createEvent('USER_INFO', [warned: expiringAccounts])
        }
    }

    void expireUserAccounts() {
        List expiredAccounts = []
        LocalDate now = LocalDate.now()

        // not expired, but warned since FLAG_EXPIRED_AFTER_ANOTHER_MONTHS
        User.executeQuery("select u from User u where u.accountExpired = false and u.inactivityWarning != null and u.username != 'anonymous' order by u.username").each{ User usr ->
            LocalDate lastWarning = DateUtils.dateToLocalDate(usr.inactivityWarning)
            // TODO: ignore INST_ADM ?
            if (lastWarning.isBefore(now.minusMonths(FLAG_EXPIRED_AFTER_ANOTHER_MONTHS))) {
                usr.accountExpired = true
                usr.save()
                expiredAccounts.add([usr.id, usr.username, usr.lastLogin ? DateUtils.getLocalizedSDF_noZ().format(usr.lastLogin) : null])

                try {
                    asynchronousMailService.sendMail {
                        to      usr.getEmail()
                        from    ConfigMapper.getNotificationsEmailFrom()
                        replyTo ConfigMapper.getNotificationsEmailReplyTo()
                        subject ConfigMapper.getLaserSystemId() + ' - Deaktivierung Ihres Accounts'
                        body    (view: '/mailTemplates/text/accountExpired', model: [user:usr, serverURL:ConfigMapper.getGrailsServerURL()])
                    }
                }
                catch (Exception e) {
                    log.error 'expireUserAccounts > sendMail exception: ' + e.message
                }
            }
        }

        if (expiredAccounts) {
            log.info '--> expireUserAccounts after additional ' + FLAG_EXPIRED_AFTER_ANOTHER_MONTHS + ' months: ' + expiredAccounts.size()
            SystemEvent.createEvent('UA_FLAG_EXPIRED', [expired: expiredAccounts])
        }
    }

    void unlockLockedUserAccounts() {
        List unlockedAccounts = []
        LocalDateTime now = LocalDateTime.now()

        User.executeQuery("select u from User u where u.accountLocked = true and u.username != 'anonymous' order by u.username").each{ User usr ->
            LocalDateTime lastUpdated = DateUtils.dateToLocalDateTime(usr.lastUpdated)
            if (lastUpdated.isBefore(now.minusMinutes(FLAG_UNLOCKED_AFTER_MINUTES))) {
                usr.invalidLoginAttempts = 0
                usr.accountLocked = false
                usr.save()

                unlockedAccounts.add([usr.id, usr.username, DateUtils.getLocalizedSDF_noZ().format(usr.lastUpdated)])
            }
        }

        if (unlockedAccounts) {
            log.info '--> unlockLockedUserAccounts after ' + FLAG_UNLOCKED_AFTER_MINUTES + ' minutes: ' + unlockedAccounts.size()
            SystemEvent.createEvent('UA_FLAG_UNLOCKED', [unlocked: unlockedAccounts])
        }
    }
}
