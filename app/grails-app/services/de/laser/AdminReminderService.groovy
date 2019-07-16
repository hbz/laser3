package de.laser


import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import de.laser.interfaces.AbstractLockableService

import javax.annotation.PostConstruct

class AdminReminderService extends AbstractLockableService {

    def mailService
    def grailsApplication
    String from
    String replyTo

    @PostConstruct
    void init() {
        from = grailsApplication.config.notifications.email.from
        replyTo = grailsApplication.config.notifications.email.replyTo
        log.debug("Initialised adminReminder Service...")
    }

    def checkPendingMembershipReqs() {
        def result = [:]

        result.pendingRequests = UserOrg.findAllByStatus(0, [sort: 'dateRequested'])
        result
    }

    def errorInLog() {

//        def result = [:]
//        new File("logs/laser-0.3.4.log").eachLine{ line->
//                def m = line =~ /(\d+-\d+-\d+) (\d+:\d+:\d+,\d+) \[\w+-\d+\] ERROR/
//                if(m.size()>0) {
//                    def x = line
//                    x.replaceAll(/(\d+-\d+-\d+) (\d+:\d+:\d+,\d+) \[\w+-\d+\] ERROR/, "")
//                    log.debug(x)
//                }
//        }
    }

    boolean adminReminder() {
        if (! running) {
            running = true

            def adminuser = []
            def users = User.getAll()

            users.each {
                it.roles.each { role ->
                    if (role.role.authority == "ROLE_YODA") {
                        adminuser.add(it)
                    }
                }
            }

            def content = checkPendingMembershipReqs()

            adminuser.each { admin ->
                if (content.pendingRequests.size() > 0)
                    mailReminder(admin.email, "${grailsApplication.config.laserSystemId} Admin Reminder", content, null, null)
            }
            running = false

            return true
        }
        else {
            return false
        }
    }

    def mailReminder(userAddress, subjectTrigger, content, overrideReplyTo, overrideFrom) {

        try {
            def currentServer = grailsApplication.config?.getCurrentServer()
            def subjectSystemPraefix = (currentServer == ContextService.SERVER_PROD)? "LAS:eR - " : (grailsApplication.config?.laserSystemId + " - ")

            mailService.sendMail {
                to userAddress
                from overrideFrom != null ? overrideFrom : from
                replyTo overrideReplyTo != null ? overrideReplyTo : replyTo
                subject subjectSystemPraefix + subjectTrigger
                body(view: "/mailTemplates/html/pendingMemberships", model: [pendingRequests: content.pendingRequests])
            }
        } catch (Exception e) {
            log.error("Admin Reminder Service - mailReminder() :: Unable to perform email due to exception ${e.message}")
        }
    }
}
