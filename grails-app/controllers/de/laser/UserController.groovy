package de.laser


import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.ctrl.UserControllerService
import de.laser.annotations.DebugAnnotation
import de.laser.helper.RDStore
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class UserController  {

    def genericOIDService
    def instAdmService
    def contextService
    def accessService
    def deletionService
    def userService
    UserControllerService userControllerService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: ['GET', 'POST']]

    @Secured(['ROLE_ADMIN'])
    def index() {
        redirect action: 'list', params: params
    }

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")')
    @Secured(closure = {
        ctx.contextService.getUser()?.hasRole('ROLE_ADMIN') || ctx.contextService.getUser()?.hasAffiliation("INST_ADM")
    })
    def delete() {
        Map<String, Object> result = userControllerService.getResultGenerics(params)

        if (result.user) {
            List<Org> affils = Org.executeQuery('select distinct uo.org from UserOrg uo where uo.user = :user', [user: result.user])

            if (affils.size() > 1) {
                flash.error = message(code: 'user.delete.error.multiAffils') as String
                redirect action: 'edit', params: [id: params.id]
                return
            }
            else if (affils.size() == 1 && (affils.get(0).id != contextService.getOrg().id)) {
                flash.error = message(code: 'user.delete.error.foreignOrg') as String
                redirect action: 'edit', params: [id: params.id]
                return
            }

            if (params.process && result.editable) {
                User userReplacement = (User) genericOIDService.resolveOID(params.userReplacement)

                result.delResult = deletionService.deleteUser(result.user, userReplacement, false)
            }
            else {
                result.delResult = deletionService.deleteUser(result.user, null, DeletionService.DRY_RUN)
            }

            List<Org> orgList = Org.executeQuery('select distinct uo.org from UserOrg uo where uo.user = :self', [self: result.user])
            result.substituteList = orgList ? User.executeQuery(
                    'select distinct u from User u join u.affiliations ua where ua.org in :orgList and u != :self and ua.formalRole = :instAdm order by u.username',
                    [orgList: orgList, self: result.user, instAdm: Role.findByAuthority('INST_ADM')]
            ) : []
        }
        else {
            redirect controller: 'user', action: 'list'
            return
        }

        render view: '/user/global/delete', model: result
    }

    @Secured(['ROLE_ADMIN'])
    def list() {

        Map<String, Object> result = userControllerService.getResultGenerics(params)
        Map filterParams = params

        params.max = params.max ?: result.editor?.getDefaultPageSize() // TODO

        result.users = userService.getUserSet(filterParams)
        result.titleMessage = message(code:'user.show_all.label') as String
        Set<Org> availableComboOrgs = Org.executeQuery('select c.fromOrg from Combo c where c.toOrg = :ctxOrg order by c.fromOrg.name asc', [ctxOrg:contextService.getOrg()])
        availableComboOrgs.add(contextService.getOrg())
        result.filterConfig = [filterableRoles:Role.findAllByRoleTypeInList(['user','global']), orgField: true, availableComboOrgs: availableComboOrgs]

        result.tmplConfig = [
                editable:result.editable,
                editor: result.editor,
                editLink: 'edit',
                deleteLink: 'delete',
                users: result.users,
                showAllAffiliations: true,
                modifyAccountEnability: SpringSecurityUtils.ifAllGranted('ROLE_YODA')
        ]
        result.total = result.users.size()

        render view: '/user/global/list', model: result
    }

    @Secured(['ROLE_ADMIN'])
    def edit() {
        Map<String, Object> result = userControllerService.getResultGenerics(params)

        if (! result.editable) {
            redirect action: 'list'
            return
        }
        else if (! result.user) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label'), params.id]) as String
            redirect action: 'list'
            return
        }
        else {
            result.availableOrgs = Org.executeQuery(
                    "select o from Org o left join o.status s where exists (select os.org from OrgSetting os where os.org = o and os.key = :customerType) and (s = null or s.value != 'Deleted') and o not in ( select c.fromOrg from Combo c where c.type = :type ) order by o.sortname",
                        [customerType: OrgSetting.KEYS.CUSTOMER_TYPE, type: RDStore.COMBO_TYPE_DEPARTMENT]
                )
            result.manipulateAffiliations = true
        }
        render view: '/user/global/edit', model: result
    }

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")')
    @Secured(closure = {
        ctx.contextService.getUser()?.hasRole('ROLE_ADMIN') || ctx.contextService.getUser()?.hasAffiliation("INST_ADM")
    })
    def show() {
        Map<String, Object> result = userControllerService.getResultGenerics(params)
        result
    }

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")', wtc = 2)
    @Secured(closure = {
        ctx.contextService.getUser()?.hasRole('ROLE_ADMIN') || ctx.contextService.getUser()?.hasAffiliation("INST_ADM")
    })
    def newPassword() {
        User.withTransaction {
            Map<String, Object> result = userControllerService.getResultGenerics(params)

            if (!result.editable) {
                flash.error = message(code: 'default.noPermissions') as String
                redirect url: request.getHeader('referer'), id: params.id
            }
            if (result.user) {
                String newPassword = User.generateRandomPassword()
                result.user.password = newPassword
                if (result.user.save()) {
                    flash.message = message(code: 'user.newPassword.success') as String

                    instAdmService.sendMail(result.user, 'Passwortänderung',
                            '/mailTemplates/text/newPassword', [user: result.user, newPass: newPassword])

                    redirect url: request.getHeader('referer'), id: params.id
                    return
                }
            }

            flash.error = message(code: 'user.newPassword.fail') as String
            redirect url: request.getHeader('referer'), id: params.id
        }
    }

    @Secured(['ROLE_ADMIN'])
    @Transactional
    def addAffiliation(){
        Map<String, Object> result = userControllerService.getResultGenerics(params)

        if (! result.editable) {
            flash.error = message(code: 'default.noPermissions') as String
            redirect controller: 'user', action: 'edit', id: params.id
            return
        }

        Org org = Org.get(params.org)
        Role formalRole = Role.get(params.formalRole)

        if (result.user && org && formalRole) {
            instAdmService.createAffiliation(result.user, org, formalRole, flash)
        }

        redirect controller: 'user', action: 'edit', id: params.id
    }

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

    @Secured(['ROLE_ADMIN'])
    @Transactional
    def processCreateUser() {
        def success = userService.addNewUser(params,flash)
        //despite IntelliJ's warnings, success may be an array other than the boolean true
        if(success instanceof User) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'user.label'), success.id]) as String
            redirect action: 'edit', id: success.id
        }
        else if(success instanceof List) {
            flash.error = success.join('<br>')
            redirect action: 'create'
        }
    }
}
