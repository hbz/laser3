package de.laser


import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import de.laser.helper.ConfigUtils
import de.laser.helper.RDStore
import de.laser.helper.ServerUtils
import grails.util.Holders
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.i18n.LocaleContextHolder

class InstAdmService {

    GrailsApplication grailsApplication
    def accessService

    def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
    def mailService = Holders.grailsApplication.mainContext.getBean('mailService')

    // checking org and combo related orgs
    boolean hasInstAdmPivileges(User user, Org org, List<RefdataValue> types) {
        boolean result = accessService.checkMinUserOrgRole(user, org, 'INST_ADM')

        List<Org> topOrgs = Org.executeQuery(
                'select c.toOrg from Combo c where c.fromOrg = :org and c.type in (:types)', [
                    org: org, types: types
            ]
        )
        topOrgs.each{ top ->
            if (accessService.checkMinUserOrgRole(user, top, 'INST_ADM')) {
                result = true
            }
        }
        result
    }

    // all user.userOrg must be accessible from editor as INST_ADMIN
    boolean isUserEditableForInstAdm(User user, User editor) {
        boolean result = false
        List<Org> userOrgs = user.getAuthorizedAffiliations().collect{ it.org }

        if (! userOrgs.isEmpty()) {
            result = true

            userOrgs.each { org ->
                result = result && hasInstAdmPivileges(editor, org, [RDStore.COMBO_TYPE_DEPARTMENT, RDStore.COMBO_TYPE_CONSORTIUM])
            }
        }
        else result = accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM","INST_ADM")
        result
    }

    boolean isUserLastInstAdminForOrg(User user, Org org){

        List<UserOrg> userOrgs = UserOrg.findAllByOrgAndFormalRoleAndStatus(
                org,
                Role.findByAuthority("INST_ADM"),
                UserOrg.STATUS_APPROVED
        )

        if (userOrgs.size() == 1 && userOrgs[0].user == user) {
            return  true
        }
        else {
            return false
        }
    }

	@Deprecated
	// moved here from AccessService
	boolean isUserEditableForInstAdm(User user, User editor, Org org) {

		boolean roleAdmin = editor.hasRole('ROLE_ADMIN')
		boolean instAdmin = editor.hasAffiliation('INST_ADM') // check @ contextService.getOrg()
		boolean orgMatch  = accessService.checkUserIsMember(user, contextService.getOrg())

		roleAdmin || (instAdmin && orgMatch)
	}

    void createAffiliation(User user, Org org, Role formalRole, def uoStatus, def flash) {

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

    void sendMail(User user, String subj, String view, Map model) {

        if (ServerUtils.getCurrentServer() == ServerUtils.SERVER_LOCAL) {
            println "--- instAdmService.sendMail() --- IGNORED SENDING MAIL because of SERVER_LOCAL ---"
            return
        }

        model.serverURL = grailsApplication.config.grails.serverURL

        try {

            mailService.sendMail {
                to      user.email
                from    grailsApplication.config.notifications.email.from
                replyTo grailsApplication.config.notifications.email.replyTo
                subject ConfigUtils.getLaserSystemId() + ' - ' + subj
                body    view: view, model: model
            }
        }
        catch (Exception e) {
            println "Unable to perform email due to exception ${e.message}"
        }
    }
}
