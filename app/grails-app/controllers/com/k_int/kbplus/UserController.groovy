package com.k_int.kbplus

import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.kbplus.auth.UserRole
import de.laser.DeletionService
import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.validation.FieldError

@Secured(['IS_AUTHENTICATED_FULLY'])
class UserController extends AbstractDebugController {

    def springSecurityService
    def genericOIDService
    def instAdmService
    def contextService
    def accessService
    def deletionService
    def userService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    def index() {
        redirect action: 'list', params: params
    }

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")')
    @Secured(closure = {
        ctx.springSecurityService.getCurrentUser()?.hasRole('ROLE_ADMIN') ||
                ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM")
    })
    def _delete() {
        def result = userService.setResultGenerics(params)

        if (result.user) {
            List<Org> affils = Org.executeQuery('select distinct uo.org from UserOrg uo where uo.user = :user and uo.status = :status',
                    [user: result.user, status: UserOrg.STATUS_APPROVED])

            if (affils.size() > 1) {
                flash.error = 'Dieser Nutzer ist mehreren Organisationen zugeordnet und kann daher nicht gelöscht werden.'
                redirect action: 'edit', params: [id: params.id]
                return
            }
            else if (affils.size() == 1 && (affils.get(0).id != contextService.getOrg().id)) {
                flash.error = 'Dieser Nutzer ist nicht ihrer Organisationen zugeordnet und kann daher nicht gelöscht werden.'
                redirect action: 'edit', params: [id: params.id]
                return
            }

            if (params.process && result.editable) {
                User userReplacement = genericOIDService.resolveOID(params.userReplacement)

                result.delResult = deletionService.deleteUser(result.user, userReplacement, false)
            }
            else {
                result.delResult = deletionService.deleteUser(result.user, null, DeletionService.DRY_RUN)
            }

            result.substituteList = User.executeQuery(
                    'select distinct u from User u join u.affiliations ua where ua.status = :uaStatus and ua.org = :ctxOrg',
                    [uaStatus: UserOrg.STATUS_APPROVED, ctxOrg: contextService.getOrg()]
            )
        }

        render view: 'delete', model: result
    }

    @Secured(['ROLE_ADMIN'])
    def list() {

        def result = userService.setResultGenerics(params)

        /*
        as of ERMS-1557, this method can be called only with admin rights. All other contexts are deployed to their respective controllers.
        if (! result.editor.hasRole('ROLE_ADMIN') || params.org) {
            // only context org depending
            baseQuery.add('UserOrg uo')
            whereQuery.add('( uo.user = u and uo.org = :org )')
            //whereQuery.add('( uo.user = u and uo.org = :ctxOrg ) or not exists ( SELECT uoCheck from UserOrg uoCheck where uoCheck.user = u ) )')

            Org comboOrg = params.org ? Org.get(params.org) : contextService.getOrg()
            queryParams.put('org', comboOrg)
        }
        */

        Map filterParams = params

        params.max = params.max ?: result.editor?.getDefaultPageSizeTMP() // TODO

        result.users = userService.getUserSet(filterParams)
        result.titleMessage = message(code:'user.show_all.label')
        result.breadcrumb = 'breadcrumb'
        Set<Org> availableComboOrgs = Org.executeQuery('select c.fromOrg from Combo c where c.toOrg = :ctxOrg order by c.fromOrg.name asc', [ctxOrg:contextService.org])
        availableComboOrgs.add(contextService.org)
        result.filterConfig = [filterableRoles:Role.findAllByRoleTypeInList(['user','global']), orgField: true, availableComboOrgs: availableComboOrgs]
        result.tableConfig = [editable:result.editable, editor: result.editor, editLink: 'edit', users: result.users, showAllAffiliations: true, modifyAccountEnability: SpringSecurityUtils.ifAllGranted('ROLE_YODA')]
        result.total = result.users.size()

        render view: '/templates/user/_list', model: result
    }

    @Secured(['ROLE_ADMIN'])
    def edit() {
        def result = userService.setResultGenerics(params)

        if (! result.editable) {
            redirect action: 'list'
            return
        }
        else if (! result.user) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'Org'), params.id])
            redirect action: 'list'
            return
        }
        else {
            /*if (! result.editor.hasRole('ROLE_ADMIN')) {
                result.availableOrgs = contextService.getOrg()

                result.availableComboConsOrgs = Combo.executeQuery(
                        'select c.fromOrg from Combo c where (c.fromOrg.status is null or c.fromOrg.status.value != \'Deleted\') ' +
                                'and c.toOrg = :ctxOrg and c.type = :type order by c.fromOrg.name', [
                        ctxOrg: contextService.getOrg(), type: RDStore.COMBO_TYPE_CONSORTIUM
                ]
                )
                result.availableComboDeptOrgs = Combo.executeQuery(
                        'select c.fromOrg from Combo c where (c.fromOrg.status is null or c.fromOrg.status.value != \'Deleted\') ' +
                                'and c.toOrg = :ctxOrg and c.type = :type order by c.fromOrg.name', [
                        ctxOrg: contextService.getOrg(), type: RDStore.COMBO_TYPE_DEPARTMENT
                ]
                )
                result.availableOrgRoles = Role.findAllByRoleType('user')
            }
            else {*/
                result.availableOrgs = Org.executeQuery("select o from Org o left join o.status s where exists (select os.org from OrgSettings os where os.org = o and os.key = :customerType) and (s = null or s.value != 'Deleted') and o not in ( select c.fromOrg from Combo c where c.type = :type ) order by o.sortname",
                        [customerType: OrgSettings.KEYS.CUSTOMER_TYPE, type: RDStore.COMBO_TYPE_DEPARTMENT]
                )
                result.availableOrgRoles = Role.findAllByRoleType('user')
            result.manipulateAffiliations = true
            //}
        }
        render view: '/templates/user/_edit', model: result
    }

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")')
    @Secured(closure = {
        ctx.springSecurityService.getCurrentUser()?.hasRole('ROLE_ADMIN') ||
                ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM")
    })
    def show() {
        def result = userService.setResultGenerics(params)
        result
    }

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")')
    @Secured(closure = {
        ctx.springSecurityService.getCurrentUser()?.hasRole('ROLE_ADMIN') ||
                ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM")
    })
    def newPassword() {
        def result = userService.setResultGenerics(params)

        if (! result.editable) {
            flash.error = message(code: 'default.noPermissions', default: 'KEINE BERECHTIGUNG')
            redirect url: request.getHeader('referer'), id: params.id
        }
        if (result.user) {
            String newPassword = User.generateRandomPassword()
            result.user.password = newPassword
            if (result.user.save(flush: true)) {
                flash.message = message(code: 'user.newPassword.success')

                instAdmService.sendMail(result.user, 'Passwortänderung',
                        '/mailTemplates/text/newPassword', [user: result.user, newPass: newPassword])

                redirect url: request.getHeader('referer'), id: params.id
                return
            }
        }

        flash.error = message(code: 'user.newPassword.fail')
        redirect url: request.getHeader('referer'), id: params.id
    }

    @Secured(['ROLE_ADMIN'])
    def addAffiliation(){
        def result = userService.setResultGenerics(params)

        if (! result.editable) {
            flash.error = message(code: 'default.noPermissions', default: 'KEINE BERECHTIGUNG')
            redirect controller: 'user', action: 'edit', id: params.id
            return
        }

        Org org = Org.get(params.org)
        Role formalRole = Role.get(params.formalRole)

        if (result.user && org && formalRole) {
            instAdmService.createAffiliation(result.user, org, formalRole, UserOrg.STATUS_APPROVED, flash)
        }

        redirect controller: 'user', action: 'edit', id: params.id
    }

    @Secured(['ROLE_ADMIN'])
    def create() {
        def result = userService.setResultGenerics(params)
        if (! result.editable) {
            flash.error = message(code: 'default.noPermissions')
            redirect controller: 'user', action: 'list'
            return
        }

        result.breadcrumb = 'breadcrumb'
        result.availableOrgs = Org.executeQuery('from Org o where o.sector = ? order by o.name', [RDStore.O_SECTOR_HIGHER_EDU])
        result.availableOrgRoles = Role.findAllByRoleType('user')

        render view: '/templates/user/_create', model: result
    }

    @Secured(['ROLE_ADMIN'])
    def processUserCreate() {
        def success = userService.addNewUser(params,flash)
        //despite IntelliJ's warnings, success may be an array other than the boolean true
        if(success instanceof User) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), success.id])
            redirect action: 'edit', id: success.id
        }
        else if(success instanceof List) {
            flash.error = success.join('<br>')
            redirect action: 'create'
        }
    }

}
