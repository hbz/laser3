package com.k_int.kbplus


import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class PropertyDefinitionController extends AbstractDebugController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    def springSecurityService
    def accessService
    def contextService

    @Secured(['ROLE_USER'])
    def list() {
        redirect controller: 'home', action: 'index'
        return // ----- deprecated
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
        redirect controller: 'home', action: 'index'
        return // ----- deprecated
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
        redirect controller: 'home', action: 'index'
        return // ----- deprecated
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        redirect controller: 'home', action: 'index'
        return // ----- deprecated
    }
}
