package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.i18n.LocaleContextHolder

//@CompileStatic
class UserService {

    GrailsApplication grailsApplication

    def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
    def mailService = Holders.grailsApplication.mainContext.getBean('mailService')

    def createAffiliation(User user, Org org, Role formalRole, def uoStatus, def flash){

        try {
            def check = UserOrg.findByOrgAndUserAndFormalRole(org, user, formalRole)

            if (check && check.status == UserOrg.STATUS_CANCELLED) {
                check.delete()
                check = null
            }

            if (check) {
                flash?.error = messageSource.getMessage('profile.processJoinRequest.error', null, LocaleContextHolder.getLocale())
            }
            else {
                log.debug("Create new user_org entry....");
                def uo = new UserOrg(
                        dateRequested:System.currentTimeMillis(),
                        status: uoStatus,
                        org: org,
                        user: user,
                        formalRole: formalRole)

                if (uoStatus in [UserOrg.STATUS_APPROVED, UserOrg.STATUS_AUTO_APPROVED]) {
                    uo.dateActioned = uo.dateRequested
                }
                if (uo.save(flush:true)) {
                    flash?.message = "OK"

                    if (uoStatus == UserOrg.STATUS_APPROVED) {
                        // only send if manually approved
                        sendMail(uo.user, 'Änderung der Organisationszugehörigkeit',
                                '/mailTemplates/text/newMembership', [userOrg: uo])
                    }
                }
                else {
                    flash?.error = "Problem requesting affiliation"
                }
            }
        }
        catch (Exception e) {
            flash?.error = "Problem requesting affiliation"
        }
    }

    def sendMail(User user, String subj, String view, Map model) {

        if (grailsApplication.config.getCurrentServer() == ContextService.SERVER_LOCAL) {
            println "--- UserService.sendMail() --- IGNORED SENDING MAIL because of SERVER_LOCAL ---"
            return
        }

        model.serverURL = grailsApplication.config.grails.serverURL

        try {

            mailService.sendMail {
                to      user.email
                from    grailsApplication.config.notifications.email.from
                replyTo grailsApplication.config.notifications.email.replyTo
                subject grailsApplication.config.laserSystemId + ' - ' + subj
                body    view: view, model: model
            }
        }
        catch (Exception e) {
            println "Unable to perform email due to exception ${e.message}"
        }
    }
}
