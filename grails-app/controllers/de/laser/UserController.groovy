package de.laser

import de.laser.annotations.Check404
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.ctrl.UserControllerService
import de.laser.annotations.DebugInfo
import de.laser.utils.PasswordUtils
import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller handles calls related to global user management
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class UserController {

    ContextService contextService
    DeletionService deletionService
    GenericOIDService genericOIDService
    UserControllerService userControllerService
    UserService userService
    MailSendService mailSendService

    //-----

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: ['GET', 'POST']]

    public static final Map<String, String> CHECK404_ALTERNATIVES = [ 'list' : 'menu.institutions.users' ]

    //-----

    /**
     * Redirects to the list view
     */
    @Secured(['ROLE_ADMIN'])
    def index() {
        redirect action: 'list', params: params
    }

    /**
     * Call to delete the given user, listing eventual substitutes for personal belongings.
     * If confirmed, the deletion will be executed and objects reassigned to the given substitute
     */
    @DebugInfo(isInstAdm_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN()
    })
    @Check404()
    def delete() {
        Map<String, Object> result = userControllerService.getResultGenerics(params)

            if (params.process && result.editable) {
                User userReplacement = (User) genericOIDService.resolveOID(params.userReplacement)

                result.delResult = deletionService.deleteUser(result.user as User, userReplacement, false)
            }
            else {
                result.delResult = deletionService.deleteUser(result.user as User, null, DeletionService.DRY_RUN)
            }

            result.substituteList = result.user.formalOrg ? User.executeQuery(
                    'select u from User u where u.formalOrg = :org and u != :self and u.formalRole = :instAdm order by u.username',
                    [org: result.user.formalOrg, self: result.user, instAdm: Role.findByAuthority('INST_ADM')]
            ) : []

        render view: '/user/global/delete', model: result
    }

    /**
     * Call to list all user accounts in the system
     */
    @Secured(['ROLE_ADMIN'])
    def list() {
        Map<String, Object> result = userControllerService.getResultGenerics(params)
        Map filterParams = params

        result.users = userService.getUserSet(filterParams)
        result.total = result.users.size()

        result.titleMessage = message(code:'user.show_all.label') as String
        Set<Org> availableComboOrgs = Org.executeQuery(
                'select c.fromOrg from Combo c where c.toOrg = :ctxOrg and c.type = :type order by c.fromOrg.name asc',
                [ctxOrg: contextService.getOrg(), type: RDStore.COMBO_TYPE_CONSORTIUM]
        )
        availableComboOrgs.add(contextService.getOrg())
        result.filterConfig = [filterableRoles:Role.findAllByRoleTypeInList(['user']), orgField: true, availableComboOrgs: availableComboOrgs]

        result.tmplConfig = [
                editable:result.editable,
                editor: result.editor,
                editLink: 'edit',
                deleteLink: 'delete',
                users: result.users,
                showAllAffiliations: true,
                modifyAccountEnability: SpringSecurityUtils.ifAllGranted('ROLE_YODA')
        ]

        render view: '/user/global/list', model: result
    }

    /**
     * Call to edit the given user profile. This is a global institution context-independent call, so
     * affiliations of the user may be edited freely
     */
    @Secured(['ROLE_ADMIN'])
    @Check404()
    def edit() {
        Map<String, Object> result = userControllerService.getResultGenerics(params)

        if (! result.editable) {
            redirect action: 'list'
            return
        }
        else {
            result.availableOrgs = Org.executeQuery(
                    "select o from Org o left join o.status s where exists (select os.org from OrgSetting os where os.org = o and os.key = :customerType) and (s = null or s.value != 'Deleted') order by o.sortname",
                        [customerType: OrgSetting.KEYS.CUSTOMER_TYPE]
                )
            result.manipulateAffiliations = true
        }
        render view: '/user/global/edit', model: result
    }

    /**
     * Shows the affiliations and global roles given user
     * @return a list of the user's affiliations and roles
     */
    @DebugInfo(isInstAdm_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN()
    })
    @Check404()
    def show() {
        Map<String, Object> result = userControllerService.getResultGenerics(params)
        result
    }

    /**
     * Creates a new random password to the given user and sends that via mail to the address registered to the account
     * @return a redirect to the referer
     */
    @DebugInfo(isInstAdm_or_ROLEADMIN = true, wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN()
    })
    def newPassword() {
        User.withTransaction {
            Map<String, Object> result = userControllerService.getResultGenerics(params)

            if (!result.editable) {
                flash.error = message(code: 'default.noPermissions') as String
                redirect url: request.getHeader('referer'), id: params.id
                return
            }
            if (result.user) {
                String newPassword = PasswordUtils.getRandomUserPassword()
                result.user.password = newPassword
                if (result.user.save()) {
                    flash.message = message(code: 'user.newPassword.success') as String

                    mailSendService.sendMailToUser(result.user, message(code: 'email.subject.forgottenPassword'),
                            '/mailTemplates/text/newPassword', [user: result.user, newPass: newPassword])

                    redirect url: request.getHeader('referer'), id: params.id
                    return
                }
            }

            flash.error = message(code: 'user.newPassword.fail') as String
            redirect url: request.getHeader('referer'), id: params.id
            return
        }
    }

    /**
     * get username and sends that via mail to the address registered to the account
     * @return a redirect to the referer
     */
    @DebugInfo(isInstAdm_or_ROLEADMIN = true, wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN()
    })
    def sendUsername() {
        User.withTransaction {
            Map<String, Object> result = userControllerService.getResultGenerics(params)

            if (!result.editable) {
                flash.error = message(code: 'default.noPermissions') as String
                redirect url: request.getHeader('referer'), id: params.id
                return
            }
            if (result.user) {
                User user = params.forgotten_username_mail ? User.findByEmail(params.forgotten_username_mail) : result.user
                if (user) {
                    flash.message = message(code: 'menu.user.forgottenUsername.success') as String
                    mailSendService.sendMailToUser(user, message(code: 'email.subject.forgottenUsername'), '/mailTemplates/text/forgotUserName', [user: user])

                    redirect url: request.getHeader('referer'), id: params.id
                    return
                }
            }

            flash.error = message(code: 'menu.user.forgottenUsername.fail') as String
            redirect url: request.getHeader('referer'), id: params.id
            return
        }
    }

    /**
     * Assigns the given user to the given institution
     * @return a redirect to the user profile editing page
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def setAffiliation(){
        Map<String, Object> result = userControllerService.getResultGenerics(params)

        if (! result.editable) {
            flash.error = message(code: 'default.noPermissions') as String
            redirect controller: 'user', action: 'edit', id: params.id
            return
        }

        userService.setAffiliation(result.user as User, params.org, params.formalRole, flash)

        redirect controller: 'user', action: 'edit', id: params.id
    }

    /**
     * Call to create a new user system-widely. This procedure can only be
     * called by global admins and is used for example to create the first access to the app to
     * an institution, mainly the institution admin, before user management is being handed over to
     * the institution itself
     * @return the user creation form
     */
    @Secured(['ROLE_ADMIN'])
    def create() {
        Map<String, Object> result = userControllerService.getResultGenerics(params)
        if (! result.editable) {
            flash.error = message(code: 'default.noPermissions') as String
            redirect controller: 'user', action: 'list'
            return
        }

        result.availableOrgs = Org.executeQuery('from Org o where o.sector = :sector order by o.name', [sector: RDStore.O_SECTOR_HIGHER_EDU])

        render view: '/user/global/create', model: result
    }

    /**
     * Takes the given form parameters and creates a new user account based on the data submitted
     * @return the profile editing view in case of success, the creation view otherwise
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def processCreateUser() {
        def success = userService.addNewUser(params,flash)
        //despite IntelliJ's warnings, success may be an array other than the boolean true
        if(success instanceof User) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'user.label'), success.id]) as String
            redirect action: 'edit', id: success.id
            return
        }
        else if(success instanceof List) {
            flash.error = success.join('<br>')
            redirect action: 'create'
            return
        }
    }
}
