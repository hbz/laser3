package de.laser

import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.helper.ConfigUtils
import de.laser.helper.RDStore
import de.laser.helper.ServerUtils
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.springframework.context.i18n.LocaleContextHolder

@Transactional
class InstAdmService {

    GrailsApplication grailsApplication
    def accessService
    def contextService

    def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
    def mailService = Holders.grailsApplication.mainContext.getBean('mailService')

    boolean hasInstAdmin(Org org) {
        //selecting IDs is much more performant than whole objects
        List<Long> admins = User.executeQuery("select u.id from User u join u.affiliations uo join uo.formalRole role where " +
                "uo.org = :org and role.authority = :role and u.enabled = true",
                [org: org,
                 role: 'INST_ADM'])
        admins.size() > 0
    }

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

        List<UserOrg> userOrgs = UserOrg.findAllByOrgAndFormalRole(
                org,
                Role.findByAuthority("INST_ADM")
        )

        return (userOrgs.size() == 1 && userOrgs[0].user == user)
    }

	@Deprecated
	// moved here from AccessService
	boolean isUserEditableForInstAdm(User user, User editor, Org org) {

		boolean roleAdmin = editor.hasRole('ROLE_ADMIN')
		boolean instAdmin = editor.hasAffiliation('INST_ADM') // check @ contextService.getOrg()
		boolean orgMatch  = accessService.checkUserIsMember(user, contextService.getOrg())

		roleAdmin || (instAdmin && orgMatch)
	}

    void createAffiliation(User user, Org org, Role formalRole, def flash) {

        try {
            Locale loc = LocaleContextHolder.getLocale()
            UserOrg check = UserOrg.findByOrgAndUserAndFormalRole(org, user, formalRole)

            if (formalRole.roleType == 'user') {
                check = UserOrg.findByOrgAndUserAndFormalRoleInList(org, user, Role.findAllByRoleType('user'))
            }

            if (check) {
                if (user == contextService.getUser()) {
                    flash?.error = messageSource.getMessage('user.affiliation.request.error2', null, loc)
                } else {
                    flash?.error = messageSource.getMessage('user.affiliation.request.error1', null, loc)
                }
            }
            else {
                log.debug("Create new user_org entry....");
                def uo = new UserOrg(
                        org: org,
                        user: user,
                        formalRole: formalRole)

                if (uo.save()) {
                    flash?.message = messageSource.getMessage('user.affiliation.request.success', null, loc)
                }
                else {
                    flash?.error = messageSource.getMessage('user.affiliation.request.failed', null, loc)
                }
            }
        }
        catch (Exception e) {
            flash?.error = messageSource.getMessage('user.affiliation.request.failed', null, loc)
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
                from    ConfigUtils.getNotificationsEmailFrom()
                replyTo ConfigUtils.getNotificationsEmailReplyTo()
                subject ConfigUtils.getLaserSystemId() + ' - ' + subj
                body    view: view, model: model
            }
        }
        catch (Exception e) {
            println "Unable to perform email due to exception ${e.message}"
        }
    }
}
