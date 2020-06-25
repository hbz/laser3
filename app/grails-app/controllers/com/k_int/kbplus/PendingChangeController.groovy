package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import de.laser.domain.IssueEntitlementCoverage
import de.laser.domain.TIPPCoverage
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

@Secured(['IS_AUTHENTICATED_FULLY'])
class PendingChangeController extends AbstractDebugController {

    def genericOIDService
    def pendingChangeService
    def executorWrapperService
    ExecutorService executorService
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
    def processAll() {
        log.debug("processAll - ${params}")
        List<String> concernedPackage = params.list("acceptChangesForPackages")
        boolean acceptAll = params.acceptAll != null
        boolean rejectAll = params.rejectAll != null
        //executorService.submit({
            concernedPackage.each { String spID ->
                SubscriptionPackage sp = SubscriptionPackage.get(spID)
                Set<PendingChange> pendingChanges = PendingChange.executeQuery("select pc from PendingChange pc where pc.subscription = :sub and pc.owner = :context and pc.status = :pending",[context:contextService.org,sub:sp.subscription,pending:RDStore.PENDING_CHANGE_PENDING])
                pendingChanges.each { PendingChange pc ->
                    log.info("processing change ${pc}")
                    def changedObject = genericOIDService.resolveOID(pc.oid)
                    Package targetPkg
                    if(changedObject instanceof TitleInstancePackagePlatform) {
                        targetPkg = changedObject.pkg
                    }
                    else if(changedObject instanceof IssueEntitlement || changedObject instanceof TIPPCoverage) {
                        targetPkg = changedObject.tipp.pkg
                    }
                    else if(changedObject instanceof IssueEntitlementCoverage) {
                        targetPkg = changedObject.issueEntitlement.tipp.pkg
                    }
                    else if(changedObject instanceof Package) {
                        targetPkg = changedObject
                    }
                    if(targetPkg?.id == sp.pkg.id) {
                        if(acceptAll) {
                            //log.info("is rejectAll simultaneously set? ${params.rejectAll}")
                            pc.accept()
                        }
                        else if(rejectAll) {
                            //log.info("is acceptAll simultaneously set? ${params.acceptAll}")
                            pc.reject()
                        }
                    }
                }
            }
        //} as Callable)
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def acceptAll() {
        log.debug("acceptAll - ${params}")
        if(params.OID) {
            def owner = genericOIDService.resolveOID(params.OID)
            Collection<PendingChange> pendingChanges = owner?.pendingChanges.findAll {
                (it.status == RDStore.PENDING_CHANGE_PENDING) || it.status == null
            }
            executorWrapperService.processClosure({
                pendingChanges.each { pc ->
                    pendingChangeService.performAccept(pc)
                }
            }, owner)
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def rejectAll() {
        log.debug("rejectAll ${params}")
        if(params.OID) {
            def owner = genericOIDService.resolveOID(params.OID)
            Collection<PendingChange> pendingChanges = owner?.pendingChanges.findAll {
                (it.status == RDStore.PENDING_CHANGE_PENDING) || it.status == null
            }
            executorWrapperService.processClosure({
                pendingChanges.each { pc ->
                    pendingChangeService.performReject(pc)
                }
            }, owner)
        }
        redirect(url: request.getHeader('referer'))
    }
}
