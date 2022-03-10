package de.laser


 
import de.laser.annotations.DebugAnnotation
import de.laser.exceptions.ChangeAcceptException
import de.laser.helper.RDStore
import de.laser.helper.SessionCacheWrapper
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages pending change processing calls
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class PendingChangeController  {

    def genericOIDService
    def pendingChangeService
    def executorWrapperService
    def contextService

    /**
     * Call to accept the given change and to trigger processing of the changes stored in the record
     */
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
            SessionCacheWrapper scw = new SessionCacheWrapper()
            String ctx = 'dashboard/changes'
            Map<String, Object> changesCache = scw.get(ctx) as Map<String, Object>
            if(changesCache)
                scw.remove(ctx)
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

    /**
     * Call to reject the given change
     */
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

    /**
     * Call to bulk-process a set of changes. Loops through the changes and performs accepting or rejecting
     */
    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processAll() {
        log.debug("processAll - ${params}")
        List<String> concernedPackage = params.list("acceptChangesForPackages")
        boolean acceptAll = params.acceptAll != null
        boolean rejectAll = params.rejectAll != null
        if(concernedPackage){
            SessionCacheWrapper scw = new SessionCacheWrapper()
            String ctx = 'dashboard/changes'
            Map<String, Object> changesCache = scw.get(ctx) as Map<String, Object>
            if(changesCache)
                scw.remove(ctx)
            concernedPackage.each { String spID ->
                if(spID) {
                    SubscriptionPackage sp = SubscriptionPackage.get(Long.parseLong(spID))
                    Set<PendingChange> pendingChanges = []
                    pendingChanges.addAll(PendingChange.executeQuery("select pc from PendingChange pc join pc.tipp tipp where tipp.pkg = :pkg and pc.ts >= :creationDate and pc.status = :history and not exists (select pca.id from PendingChange pca where pca.tipp = pc.tipp and (pc.newValue = pca.newValue or (pc.newValue = null and pca.newValue = null)) and pca.oid = concat('"+Subscription.class.name+"',':',:subId))",[pkg: sp.pkg, creationDate: sp.dateCreated, history: RDStore.PENDING_CHANGE_HISTORY, subId: sp.subscription.id]))
                    pendingChanges.addAll(PendingChange.executeQuery("select pc from PendingChange pc join pc.tippCoverage tc join tc.tipp tipp where tipp.pkg = :pkg and pc.ts >= :creationDate and pc.status = :history and not exists (select pca.id from PendingChange pca where pca.tippCoverage = pc.tippCoverage and (pc.newValue = pca.newValue or (pc.newValue = null and pca.newValue = null)) and pca.oid = concat('"+Subscription.class.name+"',':',:subId))",[pkg: sp.pkg, creationDate: sp.dateCreated, history: RDStore.PENDING_CHANGE_HISTORY, subId: sp.subscription.id]))

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

    /**
     * Call to bulk-accept a set of changes. The changes are going to be looped and accepting triggered in each of them
     */
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

    /**
     * Call to bulk-reject a set of changes. The changes are going to be looped and rejecting triggered in each of them
     */
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