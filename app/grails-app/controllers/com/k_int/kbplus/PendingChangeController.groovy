package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDConstants
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class PendingChangeController extends AbstractDebugController {

    def genericOIDService
    def pendingChangeService
    def executorWrapperService
    def contextService
    def springSecurityService

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def accept() {
        log.debug("Accept")
        //distinct between legacy accept and new accept!
        PendingChange pc = PendingChange.get(params.long('id'))
        if(pc.msgParams)
            pendingChangeService.performAccept(pc, User.get(springSecurityService.principal.id))
        else if(pc.targetProperty)
            pc.accept()
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def reject() {
        log.debug("Reject")
        //distinct between legacy reject and new reject!
        PendingChange pc = PendingChange.get(Long.parseLong(params.id))
        if(pc.msgParams)
            pendingChangeService.performReject(pc, User.get(springSecurityService.principal.id))
        else if(pc.targetProperty)
            pc.reject()
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def acceptAll() {
        log.debug("acceptAll - ${params}")
        def owner = genericOIDService.resolveOID(params.OID)

        def changes_to_accept = []
        def pending_change_pending_status = RefdataValue.getByValueAndCategory("Pending", RDConstants.PENDING_CHANGE_STATUS)
        List<PendingChange> pendingChanges = owner?.pendingChanges.findAll {
            (it.status == pending_change_pending_status) || it.status == null
        }
        User user = User.get(springSecurityService.principal.id)
        executorWrapperService.processClosure({
            pendingChanges.each { pc ->
                pendingChangeService.performAccept(pc, user)
            }
        }, owner)

        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def rejectAll() {
        log.debug("rejectAll ${params}")
        def owner = genericOIDService.resolveOID(params.OID)

        def changes_to_reject = []
        def pending_change_pending_status = RefdataValue.getByValueAndCategory("Pending", RDConstants.PENDING_CHANGE_STATUS)
        def pendingChanges = owner?.pendingChanges.findAll {
            (it.status == pending_change_pending_status) || it.status == null
        }
        pendingChanges = pendingChanges.collect { it.id }

        //def user = [user: request.user]
        User user = User.get(springSecurityService.principal.id)
        executorWrapperService.processClosure({
            pendingChanges.each { pc ->
                pendingChangeService.performReject(PendingChange.get(pc), user)
            }
        }, owner)

        redirect(url: request.getHeader('referer'))
    }
}
