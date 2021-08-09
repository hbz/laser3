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
            pendingChangeService.reject(pc, params.subId)
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
                    SubscriptionPackage sp = SubscriptionPackage.get(Long.parseLong(spID))
                    Set<PendingChange> pendingChanges = []
                    pendingChanges.addAll(PendingChange.executeQuery("select pc from PendingChange pc join pc.tipp tipp where tipp.pkg = :pkg and pc.ts >= :creationDate and pc.status = :history",[pkg: sp.pkg, creationDate: sp.dateCreated, history: RDStore.PENDING_CHANGE_HISTORY]))
                    pendingChanges.addAll(PendingChange.executeQuery("select pc from PendingChange pc join pc.tippCoverage tc join tc.tipp tipp where tipp.pkg = :pkg and pc.ts >= :creationDate and pc.status = :history",[pkg: sp.pkg, creationDate: sp.dateCreated, history: RDStore.PENDING_CHANGE_HISTORY]))

                    pendingChanges.each { PendingChange pc ->
                        log.info("processing change ${pc}")
                        if(acceptAll) {
                            //log.info("is rejectAll simultaneously set? ${params.rejectAll}")
                            pendingChangeService.applyPendingChange(pc, sp, contextService.getOrg())
                        }
                        else if(rejectAll) {
                            //log.info("is acceptAll simultaneously set? ${params.acceptAll}")
                            //PendingChange toReject = PendingChange.construct([target: target, oid: genericOIDService.getOID(sp.subscription), newValue: pc.newValue, oldValue: pc.oldValue, prop: pc.targetProperty, msgToken: pc.msgToken, status: RDStore.PENDING_CHANGE_PENDING, owner: contextService.getOrg()])
                            pendingChangeService.reject(pc, sp.subscription.id)
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