package com.k_int.kbplus


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
        if (pc.msgParams) {
            pendingChangeService.performAccept(pc)
        }
        else if (!pc.payload) {
            pc.accept()
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def reject() {
        log.debug("Reject")
        //distinct between legacy reject and new reject!
        PendingChange pc = PendingChange.get(params.long('id'))
        if (pc.msgParams) {
            pendingChangeService.performReject(pc)
        }
        else if (!pc.payload) {
            pc.reject()
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def acceptAll() {
        log.debug("acceptAll - ${params}")
        def owner = genericOIDService.resolveOID(params.OID)

        RefdataValue pending_change_pending_status = RefdataValue.getByValueAndCategory("Pending", RDConstants.PENDING_CHANGE_STATUS)

        Collection<PendingChange> pendingChanges = owner?.pendingChanges.findAll {
            (it.status == pending_change_pending_status) || it.status == null
        }

        executorWrapperService.processClosure({
            pendingChanges.each { pc ->
                pendingChangeService.performAccept(pc)
            }
        }, owner)

        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def rejectAll() {
        log.debug("rejectAll ${params}")
        def owner = genericOIDService.resolveOID(params.OID)

        RefdataValue pending_change_pending_status = RefdataValue.getByValueAndCategory("Pending", RDConstants.PENDING_CHANGE_STATUS)

        Collection<PendingChange> pendingChanges = owner?.pendingChanges.findAll {
            (it.status == pending_change_pending_status) || it.status == null
        }

        executorWrapperService.processClosure({
            pendingChanges.each { pc ->
                pendingChangeService.performReject(pc)
            }
        }, owner)

        redirect(url: request.getHeader('referer'))
    }
}
