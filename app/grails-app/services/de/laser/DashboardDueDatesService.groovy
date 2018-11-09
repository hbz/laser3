package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.properties.PropertyDefinition
import de.laser.domain.StatsTripleCursor
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

    @javax.annotation.PostConstruct
    void init() {
        from = grailsApplication.config.notifications.email.from
        replyTo = grailsApplication.config.notifications.email.replyTo
        log.debug("Initialised DashboardDueDatesService...")
    }

    def takeCareOfDueDates() {
        updateDashboardTableInDatabase(true)
    }

    @Transactional(propagation= Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    def updateDashboardTableInDatabase(boolean isSendEmail){
        Dashboard.executeUpdate("DELETE from Dashboard")

        def users = User.getAll()
        users.each { user ->
            def orgs = UserOrg.findAllByUser(user)
            int reminderPeriod = user.getSetting(UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14).value
            orgs.each {org ->
                def dueObjects = queryService.getDueObjects(org, user, reminderPeriod)
                List<Dashboard> dashbordEntries = []
                dueObjects.each { obj ->
                    Dashboard dashEntry = new Dashboard(obj, user, org)
                    dashEntry.save()
                    dashbordEntries.add(dashEntry)
                }
                if (isSendEmail) {
                    sendEmailWithDashboardToUser(user, dashbordEntries)
                }
            }
        }
    }

    def sendEmailWithDashboardToUser(User user, List<Dashboard> dashboardEntries) {
        def emailReceiver = user.getEmail()
        def subject = "LAS:eR - Erinnerung an Ihre fälligen Termine"
        def content = dashboardEntries
        sendEmail(emailReceiver, subject, content, null, null)
    }

    def getEmailBodyForUser(){

//        [view: "/admin/_emailReminderView", model: [pendingRequests: content.pendingRequests]]
    }

    def sendEmail(userAddress, subjectTrigger, content, overrideReplyTo, overrideFrom) {
        try {
            mailService.sendMail {
                to userAddress
                from overrideFrom != null ? overrideFrom : from
                replyTo overrideReplyTo != null ? overrideReplyTo : replyTo
                subject subjectTrigger
                body(view: "/user/_emailDueDatesView", model: [dueDates: content])
            }
        } catch (Exception e) {
            log.error("DashboardDueDatesService - mailReminder() :: Unable to perform email due to exception ${e.message}")
        }
    }
}

