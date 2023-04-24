package de.laser


import de.laser.annotations.DebugInfo
import de.laser.storage.RDStore
import de.laser.cache.SessionCacheWrapper
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages pending change processing calls
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class PendingChangeController  {

    ContextService contextService
    GenericOIDService genericOIDService
    PendingChangeService pendingChangeService

    /**
     * Call to accept the given change and to trigger processing of the changes stored in the record
     */
    @DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_EDITOR'])
    @Secured(closure = {
        ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR')
    })
    def accept() {
        log.debug("Accept")
        PendingChange pc = PendingChange.get(params.long('id'))
        pendingChangeService.accept(pc, params.subId)
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Call to accept the given title change and to trigger processing of the changes stored in the record
     */
    @DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_EDITOR'])
    @Secured(closure = {
        ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR')
    })
    def acceptTitleChange() {
        log.debug("Accept")
        TitleChange tic = TitleChange.get(params.id)
        pendingChangeService.applyPendingChange(tic, SubscriptionPackage.executeQuery('select sp from SubscriptionPackage sp where sp.pkg = :pkg and sp.subscription.id = :subId', [pkg: tic.tipp.pkg, subId: params.long('subId')]), contextService.getOrg())
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Call to reject the given change
     */
    @DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_EDITOR'])
    @Secured(closure = {
        ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR')
    })
    def reject() {
        log.debug("Reject")
        PendingChange pc = PendingChange.get(params.long('id'))
        pendingChangeService.reject(pc, params.subId)
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Call to reject the given change
     */
    @DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_EDITOR'])
    @Secured(closure = {
        ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR')
    })
    def rejectTitleChange() {
        log.debug("Reject")
        TitleChange tic = TitleChange.get(params.id)
        IssueEntitlementChange.construct([titleChange: tic, subscription: Subscription.get(params.subId), status: RDStore.PENDING_CHANGE_REJECTED, owner: contextService.getOrg()])
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Call to bulk-process a set of changes. Loops through the changes and performs accepting or rejecting
     */
    @DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_EDITOR'])
    @Secured(closure = {
        ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR')
    })
    def processAll() {
        log.debug("processAll - ${params}")
        List<String> concernedPackage = params.list("acceptChangesForPackages")
        boolean acceptAll = params.acceptAll != null
        boolean rejectAll = params.rejectAll != null
        if(concernedPackage){
            /*
            SessionCacheWrapper scw = new SessionCacheWrapper()
            String ctx = 'dashboard/changes'
            Map<String, Object> changesCache = scw.get(ctx) as Map<String, Object>
            if(changesCache)
                scw.remove(ctx)
            */
            concernedPackage.each { String spID ->
                if(spID) {
                    SubscriptionPackage sp = SubscriptionPackage.get(Long.parseLong(spID))
                    String query = ''
                    Map<String, Object> queryParams = [:]
                    if(params.eventType in [PendingChangeConfiguration.NEW_TITLE, PendingChangeConfiguration.TITLE_DELETED]) {
                        query = 'select tic from TitleChange tic join tic.tipp tipp join tipp.pkg pkg where pkg = :package and tic.dateCreated >= :entryDate and tic.event = :eventType and not exists (select iec from IssueEntitlementChange iec where iec.titleChange = tic and iec.subscription = :sub and iec.status in (:pendingStatus))'
                        queryParams = [package: sp.pkg, entryDate: sp.dateCreated, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], sub: sp.subscription]
                    }
                    /*
                    else if(params.eventType == PendingChangeConfiguration.TITLE_UPDATED) {
                        query = 'select pc from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and not exists (select pca.id from PendingChange pca join pca.tipp tippA where tippA = pc.tipp and pca.oid = :subOid and pca.newValue = pc.newValue and pca.status in (:pendingStatus)) and pc.status = :packageHistory'
                        queryParams = [package: sp.pkg, entryDate: sp.dateCreated, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(sp.subscription), packageHistory: RDStore.PENDING_CHANGE_HISTORY]
                    }
                    */
                    else if(params.eventType == PendingChangeConfiguration.TITLE_REMOVED) {
                        query = 'select tic from TitleChange tic join tic.tipp tipp join tipp.pkg pkg where pkg = :package and tic.event = :eventType and exists(select ie from IssueEntitlement ie where ie.tipp = tipp and ie.subscription = :subscription and ie.status != :removed)'
                        queryParams = [package: sp.pkg, eventType: params.eventType, subscription: sp.subscription, removed: RDStore.TIPP_STATUS_REMOVED]
                    }
                    Set<TitleChange> titleChanges = TitleChange.executeQuery(query, queryParams)

                    titleChanges.each { TitleChange tic ->
                        log.info("processing change ${tic}")
                        if(acceptAll) {
                            //log.info("is rejectAll simultaneously set? ${params.rejectAll}")
                            pendingChangeService.applyPendingChange(tic, sp, contextService.getOrg())
                        }
                        else if(rejectAll) {
                            //log.info("is acceptAll simultaneously set? ${params.acceptAll}")
                            //PendingChange toReject = PendingChange.construct([target: target, oid: genericOIDService.getOID(sp.subscription), newValue: pc.newValue, oldValue: pc.oldValue, prop: pc.targetProperty, msgToken: pc.msgToken, status: RDStore.PENDING_CHANGE_PENDING, owner: contextService.getOrg()])
                            IssueEntitlementChange.construct([titleChange: tic, subscription: sp.subscription, status: RDStore.PENDING_CHANGE_REJECTED, owner: contextService.getOrg()])
                        }
                    }
                }
            }
        }
        redirect(url: request.getHeader('referer'))
    }
}