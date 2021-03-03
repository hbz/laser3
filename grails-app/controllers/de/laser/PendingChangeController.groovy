package de.laser


 
import de.laser.annotations.DebugAnnotation
import de.laser.exceptions.ChangeAcceptException
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class PendingChangeController  {

    def genericOIDService
    def pendingChangeService
    def executorWrapperService
    def contextService

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def accept() {
        log.debug("Accept")
        //distinct between legacy accept and new accept!
        PendingChange pc = PendingChange.get(params.long('id'))
        if (pc.msgParams) {
            pendingChangeService.performAccept(pc)
        }
        else if (!pc.payload) {
            if(pc.status == RDStore.PENDING_CHANGE_HISTORY) {
                Org contextOrg = contextService.getOrg()
                Package targetPkg
                if(pc.tipp) {
                    targetPkg = pc.tipp.pkg
                }
                else if(pc.tippCoverage) {
                    targetPkg = pc.tippCoverage.tipp.pkg
                }
                else if(pc.priceItem) {
                    targetPkg = pc.priceItem.tipp.pkg
                }
                if(targetPkg) {
                    List<SubscriptionPackage> subPkg = SubscriptionPackage.executeQuery('select sp from SubscriptionPackage sp join sp.subscription s join s.orgRelations oo where sp.pkg = :pkg and sp.subscription.id = :subscription and oo.org = :ctx and s.instanceOf is null',[ctx:contextOrg,pkg:targetPkg,subscription:Long.parseLong(params.subId)])
                    if(subPkg.size() == 1)
                        pendingChangeService.applyPendingChange(pc,(SubscriptionPackage) subPkg[0], contextOrg)
                    else log.error("unable to determine subscription package for pending change ${pc}")
                }
            }
            else {
                pendingChangeService.accept(pc)
            }
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def reject() {
        log.debug("Reject")
        //distinct between legacy reject and new reject!
        PendingChange pc = PendingChange.get(params.long('id'))
        if (pc.msgParams) {
            pendingChangeService.performReject(pc)
        }
        else if (!pc.payload) {
            if(pc.status != RDStore.PENDING_CHANGE_HISTORY) {
                pendingChangeService.reject(pc)
            }
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processAll() {
        log.debug("processAll - ${params}")
        List<String> concernedPackage = params.list("acceptChangesForPackages")
        boolean acceptAll = params.acceptAll != null
        boolean rejectAll = params.rejectAll != null
        if(concernedPackage){
            concernedPackage.each { String spID ->
                if(spID) {
                    SubscriptionPackage sp = SubscriptionPackage.get(spID)
                    Set<PendingChange> pendingChanges = PendingChange.executeQuery("select pc from PendingChange pc where pc.subscription = :sub and pc.owner = :context and pc.status = :pending",[context:contextService.getOrg(),sub:sp.subscription,pending:RDStore.PENDING_CHANGE_PENDING])
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
                                pendingChangeService.accept(pc)
                            }
                            else if(rejectAll) {
                                //log.info("is acceptAll simultaneously set? ${params.acceptAll}")
                                pendingChangeService.reject(pc)
                            }
                        }
                    }
                }
            }
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
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
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
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