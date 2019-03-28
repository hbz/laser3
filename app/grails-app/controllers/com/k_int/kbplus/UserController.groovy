package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import grails.plugin.springsecurity.annotation.Secured
import com.k_int.kbplus.auth.*;
import grails.gorm.*

import java.security.MessageDigest

@Secured(['IS_AUTHENTICATED_FULLY'])
class UserController extends AbstractDebugController {

    def springSecurityService
    def genericOIDService
    def instAdmService
    def contextService
    def accessService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    def index() {
        redirect action: 'list', params: params
    }

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")')
    @Secured(closure = {
        ctx.springSecurityService.getCurrentUser()?.hasRole('ROLE_ADMIN') ||
                ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM")
    })
    def list() {

        def result = setResultGenerics()

        List baseQuery = ['select distinct u from User u']
        List whereQuery = []
        Map queryParams = [:]

        if (! result.editor.hasRole('ROLE_ADMIN') || params.org) {
            // only context org depending
            baseQuery.add('UserOrg uo')
            whereQuery.add('( uo.user = u and uo.org = :org )')
            //whereQuery.add('( uo.user = u and uo.org = :ctxOrg ) or not exists ( SELECT uoCheck from UserOrg uoCheck where uoCheck.user = u ) )')

            Org comboOrg = params.org ? Org.get(params.org) : contextService.getOrg()
            queryParams.put('org', comboOrg)
        }

        if (params.authority) {
            baseQuery.add('UserRole ur')
            whereQuery.add('ur.user = u and ur.role = :role')
            queryParams.put('role', Role.get(params.authority.toLong()))
        }

        if (params.name && params.name != '' ) {
            whereQuery.add('(lower(username) like :name or lower(display) like :name)')
            queryParams.put('name', "%${params.name.toLowerCase()}%")
        }

        params.max = params.max ?: result.editor?.getDefaultPageSizeTMP() // TODO

        result.users = User.executeQuery(
                baseQuery.join(', ') + (whereQuery ? ' where ' + whereQuery.join(' and ') : '') ,
                queryParams /*,
                params */
        )

        result.availableComboOrgs = Combo.executeQuery(
                'select c.fromOrg from Combo c where c.toOrg = :ctxOrg order by c.fromOrg.name', [ctxOrg: contextService.getOrg()]
        )
        result.availableComboOrgs.add(contextService.getOrg())
        result.total = result.users.size()

        result
    }

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")')
    @Secured(closure = {
        ctx.springSecurityService.getCurrentUser()?.hasRole('ROLE_ADMIN') ||
                ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM")
    })
    def edit() {
        def result = setResultGenerics()

        result.editable = result.editable || instAdmService.isUserEditableForInstAdm(result.user, result.editor)

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
            // check if api key and secret are existing
            def readRole  = UserRole.findAllWhere(user: result.user, role: Role.findByAuthority('ROLE_API_READER'))
            def writeRole = UserRole.findAllWhere(user: result.user, role: Role.findByAuthority('ROLE_API_WRITER'))
            if((readRole || writeRole)){
                if(! result.user.apikey){
                    result.user.apikey = User.generateRandomPassword()
                }
                if(! result.user.apisecret){
                    result.user.apisecret = User.generateRandomPassword()
                }
            }

            if (! result.editor.hasRole('ROLE_ADMIN')) {
                result.availableOrgs = contextService.getOrg()
                result.availableComboOrgs = Combo.executeQuery(
                        'select c.fromOrg from Combo c where c.toOrg = :ctxOrg order by c.fromOrg.name', [ctxOrg: contextService.getOrg()]
                )
                result.availableOrgRoles = Role.findAllByRoleType('user')
            }
            else {
                result.availableOrgs = Org.executeQuery('from Org o where o.sector.value = ? order by o.name', 'Higher Education')
                result.availableOrgRoles = Role.findAllByRoleType('user')
            }
        }
        result
    }

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")')
    @Secured(closure = {
        ctx.springSecurityService.getCurrentUser()?.hasRole('ROLE_ADMIN') ||
                ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM")
    })
    def show() {
        def result = setResultGenerics()
        result
    }

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")')
    @Secured(closure = {
        ctx.springSecurityService.getCurrentUser()?.hasRole('ROLE_ADMIN') ||
                ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM")
    })
    def newPassword() {
        def result = setResultGenerics()

        result.editable = result.editable || instAdmService.isUserEditableForInstAdm(result.user, result.editor)

        if (! result.editable) {
            flash.error = message(code: 'default.noPermissions', default: 'KEINE BERECHTIGUNG')
            redirect controller: 'user', action: 'edit', id: params.id
            return
        }
        if (result.user) {
            String newPassword = User.generateRandomPassword()
            result.user.password = newPassword
            if (result.user.save(flush: true)) {
                flash.message = message(code: 'user.newPassword.success', args: [newPassword])

                instAdmService.sendMail(result.user, 'Passwortänderung',
                        '/mailTemplates/text/newPassword', [user: result.user, newPass: newPassword])

                redirect controller: 'user', action: 'edit', id: params.id
                return
            }
        }

        flash.error = message(code: 'user.newPassword.fail')
        redirect controller: 'user', action: 'edit', id: params.id
    }

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")')
    @Secured(closure = {
        ctx.springSecurityService.getCurrentUser()?.hasRole('ROLE_ADMIN') ||
                ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM")
    })
    def addAffiliation(){
        def result = setResultGenerics()

        result.editable = result.editable || instAdmService.isUserEditableForInstAdm(result.user, result.editor)

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

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")')
    @Secured(closure = {
        ctx.springSecurityService.getCurrentUser()?.hasRole('ROLE_ADMIN') ||
                ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM")
    })
    def create() {
        def result = setResultGenerics()
        if (! result.editable) {
            flash.error = message(code: 'default.noPermissions', default: 'KEINE BERECHTIGUNG')
            redirect controller: 'user', action: 'list'
            return
        }

        if (! result.editor.hasRole('ROLE_ADMIN')) {
            result.availableOrgs = contextService.getOrg()
            result.availableComboOrgs = Combo.executeQuery(
                    'select c.fromOrg from Combo c where c.toOrg = :ctxOrg order by c.fromOrg.name', [ctxOrg: contextService.getOrg()]
            )
            result.availableComboOrgs.push(contextService.getOrg())

            result.availableOrgRoles = Role.findAllByRoleType('user')
        }
        else {
            result.availableOrgs = Org.executeQuery('from Org o where o.sector.value = ? order by o.name', 'Higher Education')
            result.availableOrgRoles = Role.findAllByRoleType('user')
        }

        switch (request.method) {
            case 'POST':
                def user = new User(params)

                if (! user.save(flush: true)) {
                    flash.error = message(code: 'default.not.created.message', args: [user])

                    render view: 'create', model: [
                            userInstance: user,
                            editable: result.editable,
                            availableOrgs: result.availableOrgs,
                            availableOrgRoles: result.availableOrgRoles
                    ]
                    return
                }

                def defaultRole = new UserRole(user: user, role: Role.findByAuthority('ROLE_USER'))
                defaultRole.save(flush: true)

                if (params.org && params.formalRole) {
                    Org org = Org.get(params.org)
                    Role formalRole = Role.get(params.formalRole)
                    if (org && formalRole) {
                        instAdmService.createAffiliation(user, org, formalRole, UserOrg.STATUS_APPROVED, flash)
                    }
                }
                if (params.comboOrg && params.comboFormalRole) {
                    Org org2 = Org.get(params.comboOrg)
                    Role formalRole2 = Role.get(params.comboFormalRole)
                    if (org2 && formalRole2) {
                        instAdmService.createAffiliation(user, org2, formalRole2, UserOrg.STATUS_APPROVED, flash)
                    }
                }

                flash.message = message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), user.id])
                redirect action: 'edit', id: user.id
                break
        }
        result
    }

    private LinkedHashMap setResultGenerics() {
        def result = [:]
        result.editor = User.get(springSecurityService.principal.id)

        if (params.get('id')) {
            result.user = User.get(params.id)
            result.editable = accessService.checkIsEditableForAdmin(result.user, result.editor, contextService.getOrg())
        }
        else {
            result.editable = result.editor.hasRole('ROLE_ADMIN') || result.editor.hasAffiliation('INST_ADM')
        }

        result
    }
}
