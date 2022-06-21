package de.laser

import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.utils.AppUtils
import de.laser.utils.ConfigMapper
import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional
import grails.plugins.mail.MailService
import groovy.util.logging.Slf4j
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

/**
 * This service manages calls specific to institutional administrator (INST_ADM) matters
 */
@Transactional
@Slf4j
class InstAdmService {

    AccessService accessService
    ContextService contextService
    MailService mailService
    MessageSource messageSource

    /**
     * Checks if the given institution has an administrator
     * @param org the institution to check
     * @return true if there is at least one user affiliated as INST_ADM, false otherwise
     */
    boolean hasInstAdmin(Org org) {
        //selecting IDs is much more performant than whole objects
        List<Long> admins = User.executeQuery("select u.id from User u join u.affiliations uo join uo.formalRole role where " +
                "uo.org = :org and role.authority = :role and u.enabled = true",
                [org: org,
                 role: 'INST_ADM'])
        admins.size() > 0
    }

    /**
     * Checks if the given user is an admin; checking the context institution and combo related institutions
     * @param user the user whose privileges should be checked
     * @param org the institution to check
     * @param types a list of combo types
     * @return true if the user has an INST_ADM grant to either the consortium or one of the members, false otherwise
     */
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

    /**
     * All user.userOrg must be accessible from editor as INST_ADMIN
     * Checks if the given user is edtable for the given editor
     * @param user the user who should be accessed
     * @param editor the editor accessing the user
     * @return true if access is granted, false otherwise
     */
    boolean isUserEditableForInstAdm(User user, User editor) {
        List<Org> userOrgs = user.getAuthorizedOrgs()

        if (! userOrgs.isEmpty()) {
            boolean result = true

            userOrgs.each { org ->
                if (result) {
                    result = hasInstAdmPivileges(editor, org, [RDStore.COMBO_TYPE_CONSORTIUM])
                }
                else {
                    result = false
                }
            }
            return result
        }
        else {
            return accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_ADM")
        }
    }

    @Deprecated
    boolean isUserLastInstAdminForAnyOrgInList(User user, List<Org> orgList){
        boolean match = false

        orgList.each{ org ->
            if (! match) {
                match = isUserLastInstAdminForOrg(user, org)
            }
        }
        return match
    }

    /**
     * Checks if the given user is the last remaining institutional admin of the given institution
     * @param user the user to check
     * @param org the institution to check
     * @return true if the given user is the last admin of the given institution
     */
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

    /**
     * Links the given user to the given institution with the given role
     * @param user the user to link
     * @param org the institution to which the user should be linked
     * @param formalRole the role to attribute
     * @param flash the message container
     */
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
                UserOrg uo = new UserOrg(
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

    /**
     * Sends a mail to the given user
     * @param user the user to whom the mail should be sent
     * @param subj the subject of the mail
     * @param view the template of the mail body
     * @param model the parameters for the mail template
     */
    void sendMail(User user, String subj, String view, Map model) {

        if (AppUtils.getCurrentServer() == AppUtils.LOCAL) {
            log.info "--- instAdmService.sendMail() --- IGNORED SENDING MAIL because of SERVER_LOCAL ---"
            return
        }

        model.serverURL = ConfigMapper.getGrailsServerURL()

        try {

            mailService.sendMail {
                to      user.email
                from    ConfigMapper.getNotificationsEmailFrom()
                replyTo ConfigMapper.getNotificationsEmailReplyTo()
                subject ConfigMapper.getLaserSystemId() + ' - ' + subj
                body    view: view, model: model
            }
        }
        catch (Exception e) {
            log.error "Unable to perform email due to exception ${e.message}"
        }
    }
}
