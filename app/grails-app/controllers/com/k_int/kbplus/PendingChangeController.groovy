package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class PendingChangeController extends AbstractDebugController {

    def genericOIDService
    def pendingChangeService
    def executorWrapperService
    def contextService
    def springSecurityService

    @Secured(['ROLE_USER'])
    def accept() {
        log.debug("Accept");
        pendingChangeService.performAccept(params.id, User.get(springSecurityService.principal.id))
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_USER'])
    def reject() {
        log.debug("Reject")
        pendingChangeService.performReject(params.id, User.get(springSecurityService.principal.id))
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_USER'])
    def acceptAll() {
        log.debug("acceptAll - ${params}")
        def owner = genericOIDService.resolveOID(params.OID)

        def changes_to_accept = []
        def pending_change_pending_status = RefdataValue.getByValueAndCategory("Pending", "PendingChangeStatus")
        def pendingChanges = owner?.pendingChanges.findAll {
            (it.status == pending_change_pending_status) || it.status == null
        }
        def user = User.get(springSecurityService.principal.id)
        pendingChanges = pendingChanges.collect { it.id }
        executorWrapperService.processClosure({
            pendingChanges.each { pc ->
                pendingChangeService.performAccept(pc, request.user)
            }
        }, owner)

        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_USER'])
    def rejectAll() {
        log.debug("rejectAll ${params}")
        def owner = genericOIDService.resolveOID(params.OID)

        def changes_to_reject = []
        def pending_change_pending_status = RefdataValue.getByValueAndCategory("Pending", "PendingChangeStatus")
        def pendingChanges = owner?.pendingChanges.findAll {
            (it.status == pending_change_pending_status) || it.status == null
        }
        pendingChanges = pendingChanges.collect { it.id }

        //def user = [user: request.user]
        def user = User.get(springSecurityService.principal.id)
        executorWrapperService.processClosure({
            pendingChanges.each { pc ->
                pendingChangeService.performReject(pc, user)
            }
        }, owner)

        redirect(url: request.getHeader('referer'))
    }
}
