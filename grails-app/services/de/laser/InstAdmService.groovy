package de.laser

import de.laser.auth.Role
import de.laser.auth.User
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import org.springframework.context.MessageSource

/**
 * This service manages calls specific to institutional administrator (INST_ADM) matters
 */
@Transactional
@Slf4j
class InstAdmService {

    AccessService accessService
    ContextService contextService
    MessageSource messageSource
    UserService userService

    /**
     * Checks if the given institution has an administrator
     * @param org the institution to check
     * @return true if there is at least one user affiliated as INST_ADM, false otherwise
     */
    boolean hasInstAdmin(Org org) {
        List<Long> admins = User.executeQuery(
                'select u.id from User u where u.formalOrg = :fo and u.formalRole = :fr and u.enabled = true',
                [fo: org, fr: Role.findByAuthority('INST_ADM')])
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
        boolean result = userService.checkAffiliationAndCtxOrg(user, org, 'INST_ADM')

        List<Org> topOrgs = Org.executeQuery(
                'select c.toOrg from Combo c where c.fromOrg = :org and c.type in (:types)', [
                    org: org, types: types
            ]
        )
        topOrgs.each{ top ->
            if (userService.checkAffiliationAndCtxOrg(user, top, 'INST_ADM')) {
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
        if (user.formalOrg) {
            return hasInstAdmPivileges(editor, user.formalOrg, [RDStore.COMBO_TYPE_CONSORTIUM])
        }
        else {
            return accessService.ctxPermAffiliation(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC, 'INST_ADM')
        }

//        List<Org> userOrgs = user.getAffiliationOrgs()
//
//        if (! userOrgs.isEmpty()) {
//            boolean result = true
//
//            userOrgs.each { org ->
//                if (result) {
//                    result = hasInstAdmPivileges(editor, org, [RDStore.COMBO_TYPE_CONSORTIUM])
//                }
//                else {
//                    result = false
//                }
//            }
//            return result
//        }
//        else {
//            return accessService.ctxPermAffiliation(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC, 'INST_ADM')
//        }
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
        List<User> users = User.findAllByFormalOrgAndFormalRole(org, Role.findByAuthority('INST_ADM'))

        println users        // todo check
        return (users.size() == 1 && users[0] == user)
    }

//    /**
//     * Links the given user to the given institution with the given role
//     * @param user the user to link
//     * @param org the institution to which the user should be linked
//     * @param formalRole the role to attribute
//     * @param flash the message container
//     */
//    @Deprecated
//    void createAffiliation(User user, Org org, Role formalRole, def flash) {
//
//        try {
//            Locale loc = LocaleUtils.getCurrentLocale()
//            UserOrgRole check = UserOrgRole.findByOrgAndUserAndFormalRole(org, user, formalRole)
//
//            if (formalRole.roleType == 'user') {
//                check = UserOrgRole.findByOrgAndUserAndFormalRoleInList(org, user, Role.findAllByRoleType('user'))
//            }
//
//            if (check) {
//                if (user == contextService.getUser()) {
//                    flash?.error = messageSource.getMessage('user.affiliation.request.error2', null, loc)
//                } else {
//                    flash?.error = messageSource.getMessage('user.affiliation.request.error1', null, loc)
//                }
//            }
//            else {
//                log.debug("Create new user_org entry....");
//                UserOrgRole uo = new UserOrgRole( org: org, user: user, formalRole: formalRole )
//
//                if (uo.save()) {
//                    flash?.message = messageSource.getMessage('user.affiliation.request.success', null, loc)
//                }
//                else {
//                    flash?.error = messageSource.getMessage('user.affiliation.request.failed', null, loc)
//                }
//            }
//        }
//        catch (Exception e) {
//            flash?.error = messageSource.getMessage('user.affiliation.request.failed', null, loc)
//        }
//    }
}
