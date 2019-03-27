package de.laser

import com.k_int.kbplus.Combo
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
class InstAdmService {

    GrailsApplication grailsApplication
    def accessService

    def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
    def mailService = Holders.grailsApplication.mainContext.getBean('mailService')

    // checking org and combo related orgs
    boolean hasInstAdmPivileges(User user, Org org) {
        boolean result = accessService.checkMinUserOrgRole(user, org, 'INST_ADM')

        List<Org> topOrgs = Org.executeQuery(
                'select c.toOrg from Combo c where c.fromOrg = :org', [org: org]
        )
        topOrgs.each{ top ->
            if (accessService.checkMinUserOrgRole(user, top, 'INST_ADM')) {
                result = true
            }
        }
        result
    }

    // checking user.userOrg against editor.userOrg
    boolean isUserEditableForInstAdm(User user, User editor) {
        boolean result = false
        List<Org> userOrgs = user.getAuthorizedAffiliations().collect{ it.org }

        userOrgs.each { org ->
            result = result || hasInstAdmPivileges(editor, org)
        }
        result
    }

    def createAffiliation(User user, Org org, Role formalRole, def uoStatus, def flash) {

        try {
            def check = UserOrg.findByOrgAndUserAndFormalRole(org, user, formalRole)

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

                if (uoStatus == UserOrg.STATUS_APPROVED) {
                    uo.dateActioned = uo.dateRequested
                }
                if (uo.save(flush:true)) {
                    flash?.message = "Die neue Organisations-Zugehörigkeit wurde angelegt."

                    if (uoStatus == UserOrg.STATUS_APPROVED) {
                        // TODO: only send if manually approved
                        //sendMail(uo.user, 'Änderung der Organisationszugehörigkeit',
                        //        '/mailTemplates/text/newMembership', [userOrg: uo])
                    }
                }
                else {
                    flash?.error = "Die neue Organisations-Zugehörigkeit konnte nicht angelegt werden."
                }
            }
        }
        catch (Exception e) {
            flash?.error = "Problem requesting affiliation"
        }
    }

    def sendMail(User user, String subj, String view, Map model) {

        if (grailsApplication.config.getCurrentServer() == ContextService.SERVER_LOCAL) {
            println "--- instAdmService.sendMail() --- IGNORED SENDING MAIL because of SERVER_LOCAL ---"
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
