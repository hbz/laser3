package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.domain.IssueEntitlementCoverage
import de.laser.domain.PendingChangeConfiguration
import de.laser.domain.TIPPCoverage
import de.laser.exceptions.ChangeAcceptException
import de.laser.exceptions.CreationException
import de.laser.helper.DateUtil
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.helper.RefdataAnnotation
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONElement
import org.springframework.context.MessageSource

import javax.persistence.Transient
import java.text.SimpleDateFormat

class PendingChange {

    @Transient
    def genericOIDService
    @Transient
    def pendingChangeService
    @Transient
    def auditService

    @Transient
    final static Set<String> DATE_FIELDS = ['accessStartDate','accessEndDate','startDate','endDate']
    @Transient
    final static Set<String> REFDATA_FIELDS = ['status']

    final static PROP_LICENSE       = 'license'
    final static PROP_PKG           = 'pkg'
    final static PROP_SUBSCRIPTION  = 'subscription'

    final static MSG_LI01 = 'pendingChange.message_LI01'
    final static MSG_LI02 = 'pendingChange.message_LI02'
    final static MSG_SU01 = 'pendingChange.message_SU01'
    final static MSG_SU02 = 'pendingChange.message_SU02'

    @Transient
    MessageSource messageSource

    Subscription subscription
    License license
    SystemObject systemObject
    @Deprecated
    Package pkg
    CostItem costItem
    Date ts
    Org owner

    String oid

    String payloadChangeType        // payload = {changeType:"string", [..]}
    String payloadChangeTargetOid   // payload = {changeTarget:"class:id", [..]}
    String payloadChangeDocOid      // payload = {[..], changeDoc:{OID:"class:id"}}

    String payload
    String msgToken
    String msgParams

    String targetProperty
    String oldValue
    String newValue

    Date actionDate

    Date dateCreated
    Date lastUpdated

    @Deprecated
    String desc

    @RefdataAnnotation(cat = RDConstants.PENDING_CHANGE_STATUS)
    RefdataValue status

    static mapping = {
        systemObject column:'pc_sys_obj'
        subscription column:'pc_sub_fk',        index:'pending_change_sub_idx'
            license column:'pc_lic_fk',         index:'pending_change_lic_idx'
                pkg column:'pc_pkg_fk',         index:'pending_change_pkg_idx'
           costItem column:'pc_ci_fk',          index:'pending_change_costitem_idx'
                oid column:'pc_oid',            index:'pending_change_oid_idx'
            payloadChangeType column:'pc_change_type'
       payloadChangeTargetOid column:'pc_change_target_oid', index:'pending_change_pl_ct_oid_idx'
       payloadChangeDocOid    column:'pc_change_doc_oid', index:'pending_change_pl_cd_oid_idx'
        targetProperty column: 'pc_target_property', type: 'text'
        oldValue column: 'pc_old_value', type: 'text'
        newValue column: 'pc_new_value', type: 'text'
            payload column:'pc_payload', type:'text'
           msgToken column:'pc_msg_token'
          msgParams column:'pc_msg_doc', type:'text'
                 ts column:'pc_ts'
              owner column:'pc_owner'
               desc column:'pc_desc', type:'text'
             status column:'pc_status_rdv_fk'
         actionDate column:'pc_action_date'
               sort "ts":"asc"

        dateCreated column: 'pc_date_created'
        lastUpdated column: 'pc_last_updated'
    }

    static constraints = {
        systemObject(nullable:true, blank:false)
        subscription(nullable:true, blank:false)
        license(nullable:true, blank:false)
        payload(nullable:true, blank:false)
        msgToken(nullable:true, blank:false)
        msgParams(nullable:true, blank:false)
        pkg(nullable:true, blank:false)
        costItem(nullable:true, blank: false)
        ts(nullable:true, blank:false)
        owner(nullable:true, blank:false)
        oid(nullable:true, blank:false)
        payloadChangeType       (nullable:true, blank:true)
        payloadChangeTargetOid  (nullable:true, blank:false)
        payloadChangeDocOid     (nullable:true, blank:false)
        targetProperty(nullable:true, blank:true)
        oldValue(nullable:true, blank:true)
        newValue(nullable:true, blank:true)
        desc(nullable:true, blank:false)
        status(nullable:true, blank:false)
        actionDate(nullable:true, blank:false)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }

    /**
     * Factory method which should replace the legacy method ${@link ChangeNotificationService}.registerPendingChange().
     * @param configMap
     * @return
     * @throws CreationException
     */
    static PendingChange construct(Map<String,Object> configMap) throws CreationException {
        if((configMap.target instanceof Subscription || configMap.target instanceof License || configMap.target instanceof CostItem)) {
            PendingChange pc = new PendingChange()
            if(configMap.prop) {
                executeUpdate('update PendingChange pc set status = :superseded where :target in (subscription,license,costItem) and targetProperty = :prop',[superseded:RDStore.PENDING_CHANGE_SUPERSEDED,target:configMap.target,prop:configMap.prop])
                pc.targetProperty = configMap.prop
            }
            else {
                executeUpdate('update PendingChange pc set status = :superseded where :target in (subscription,license,costItem) and oid = :oid and msgToken = :msgToken',[superseded:RDStore.PENDING_CHANGE_SUPERSEDED,target:configMap.target,oid:configMap.oid,msgToken:configMap.msgToken])
            }
            if(configMap.target instanceof Subscription)
                pc.subscription = (Subscription) configMap.target
            else if(configMap.target instanceof License)
                pc.license = (License) configMap.target
            else if(configMap.target instanceof CostItem)
                pc.costItem = (CostItem) configMap.target
            pc.msgToken = configMap.msgToken
            pc.targetProperty = configMap.prop
            if(pc.targetProperty in PendingChange.DATE_FIELDS) {
                SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
                pc.newValue = configMap.newValue ? sdf.format(configMap.newValue) : null
                pc.oldValue = configMap.oldValue ? sdf.format(configMap.oldValue) : null
            }
            else {
                pc.newValue = configMap.newValue
                pc.oldValue = configMap.oldValue //must be imperatively the IssueEntitlement's current value if it is a titleUpdated event!
            }
            pc.oid = configMap.oid
            pc.status = configMap.status
            pc.ts = new Date()
            pc.owner = configMap.owner
            if(pc.save())
                pc
            else throw new CreationException("Error on hooking up pending change: ${pc.errors}")
        }
        else throw new CreationException("Pending changes need a target! Check if configMap.target is correctly set!")
    }

    boolean accept() throws ChangeAcceptException {
        boolean done = false, broadcastChange = false
        def target = genericOIDService.resolveOID(oid)
        def parsedNewValue
        if(targetProperty in DATE_FIELDS)
            parsedNewValue = DateUtil.parseDateGeneric(newValue)
        else if(targetProperty in REFDATA_FIELDS)
            parsedNewValue = RefdataValue.get(Long.parseLong(newValue))
        else parsedNewValue = newValue
        switch(msgToken) {
            //pendingChange.message_TP01 (newTitle)
            case PendingChangeConfiguration.NEW_TITLE:
                if(target instanceof TitleInstancePackagePlatform) {
                    TitleInstancePackagePlatform tipp = (TitleInstancePackagePlatform) target
                    IssueEntitlement newTitle = IssueEntitlement.construct([subscription:subscription,tipp:tipp])
                    if(newTitle) {
                        if(auditService.getAuditConfig(subscription,msgToken)) {
                            subscription.derivedSubscriptions.each { Subscription childSub ->
                                IssueEntitlement newChildTitle = IssueEntitlement.construct([subscription:childSub,tipp:tipp])
                                if(!newChildTitle)
                                    throw new ChangeAcceptException("problems when broadcasting new entitlement creation - pending change not accepted: ${newChildTitle.errors}")
                            }
                        }
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when creating new entitlement - pending change not accepted: ${newTitle.errors}")
                }
                else throw new ChangeAcceptException("no instance of TitleInstancePackagePlatform stored: ${oid}! Pending change is void!")
                break
            //pendingChange.message_TP02 (titleUpdated)
            case PendingChangeConfiguration.TITLE_UPDATED:
                if(target instanceof IssueEntitlement) {
                    IssueEntitlement targetTitle = (IssueEntitlement) target
                    targetTitle[targetProperty] = parsedNewValue
                    if(targetTitle.save()) {
                        if(auditService.getAuditConfig(subscription,msgToken)) {
                            subscription.derivedSubscriptions.each { Subscription childSub ->
                                IssueEntitlement childTitle = IssueEntitlement.findBySubscriptionAndTipp(childSub,targetTitle.tipp)
                                childTitle[targetProperty] = parsedNewValue
                                if(!childTitle.save())
                                    throw new ChangeAcceptException("problems when broadcasting entitlement update - pending change not accepted: ${childTitle.errors}")
                            }
                        }
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when updating entitlement - pending change not accepted: ${targetTitle.errors}")
                }
                else throw new ChangeAcceptException("no instance of IssueEntitlement stored: ${oid}! Pending change is void!")
                break
            //pendingChange.message_TP03 (titleDeleted)
            case PendingChangeConfiguration.TITLE_DELETED:
                if(target instanceof IssueEntitlement) {
                    IssueEntitlement targetTitle = (IssueEntitlement) target
                    targetTitle.status = RDStore.TIPP_STATUS_DELETED
                    if(targetTitle.save()) {
                        if(auditService.getAuditConfig(subscription,msgToken)) {
                            subscription.derivedSubscriptions.each { Subscription childSub ->
                                IssueEntitlement childTitle = IssueEntitlement.findBySubscriptionAndTipp(childSub,targetTitle.tipp)
                                childTitle.status = RDStore.TIPP_STATUS_DELETED
                                if(!childTitle.save())
                                    throw new ChangeAcceptException("problems when broadcasting entitlement deletion - pending change not accepted: ${childTitle.errors}")
                            }
                        }
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when deleting entitlement - pending change not accepted: ${targetTitle.errors}")
                }
                else throw new ChangeAcceptException("no instance of IssueEntitlement stored: ${oid}! Pending change is void!")
                break
            //pendingChange.message_TC01 (coverageUpdated)
            case PendingChangeConfiguration.COVERAGE_UPDATED:
                if(target instanceof IssueEntitlementCoverage) {
                    IssueEntitlementCoverage targetCov = (IssueEntitlementCoverage) target
                    targetCov[targetProperty] = parsedNewValue
                    if(targetCov.save()) {
                        if(auditService.getAuditConfig(subscription,msgToken)) {
                            subscription.derivedSubscriptions.each { Subscription childSub ->
                                IssueEntitlementCoverage childCov = IssueEntitlementCoverage.executeQuery('select ic from IssueEntitlementCoverage ic where ic.issueEntitlement.tipp = :tipp and ic.issueEntitlement.subscription = :child',[child:childSub,tipp:targetCov.issueEntitlement.tipp])[0]
                                childCov[targetProperty] = parsedNewValue
                                if(!childCov.save())
                                    throw new ChangeAcceptException("problems when broadcasting entitlement coverage update - pending change not accepted: ${childCov.errors}")
                            }
                        }
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when updating coverage statement - pending change not accepted: ${targetCov.errors}")
                }
                else throw new ChangeAcceptException("no instance of IssueEntitlementCoverage stored: ${oid}! Pending change is void!")
                break
            //pendingChange.message_TC02 (newCoverage)
            case PendingChangeConfiguration.NEW_COVERAGE:
                if(target instanceof TIPPCoverage) {
                    TIPPCoverage tippCoverage = (TIPPCoverage) target
                    IssueEntitlement owner = IssueEntitlement.findBySubscriptionAndTipp(subscription,tippCoverage.tipp)
                    IssueEntitlementCoverage ieCov = new IssueEntitlementCoverage([issueEntitlement:owner])
                    if(ieCov.save()) {
                        if(auditService.getAuditConfig(subscription, msgToken)) {
                            subscription.derivedSubscriptions.each { Subscription childSub ->
                                IssueEntitlement childOwner = IssueEntitlement.findBySubscriptionAndTipp(childSub,tippCoverage.tipp)
                                IssueEntitlementCoverage childCov = new IssueEntitlementCoverage([issueEntitlement:childOwner])
                                if(!childCov.save())
                                    throw new ChangeAcceptException("problems when broadcasting new entitlement creation - pending change not accepted: ${childOwner.errors}")
                            }
                        }
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when creating new entitlement - pending change not accepted: ${ieCov.errors}")
                }
                else throw new ChangeAcceptException("no instance of TIPPCoverage stored: ${oid}! Pending change is void!")
                break
            //pendingChange.message_TC03 (coverageDeleted)
            case PendingChangeConfiguration.COVERAGE_DELETED:
                if(target instanceof IssueEntitlementCoverage) {
                    IssueEntitlementCoverage targetCov = (IssueEntitlementCoverage) target
                    if(targetCov.delete()) {
                        if(auditService.getAuditConfig(subscription,msgToken)) {
                            subscription.derivedSubscriptions.each { Subscription childSub ->
                                IssueEntitlementCoverage childCov = IssueEntitlementCoverage.executeQuery('select ic from IssueEntitlementCoverage ic where ic.issueEntitlement.tipp = :tipp and ic.issueEntitlement.subscription = :child',[child:childSub,tipp:targetCov.issueEntitlement.tipp])[0]
                                if(!childCov.delete())
                                    throw new ChangeAcceptException("problems when broadcasting entitlement coverage update - pending change not accepted: ${childCov.errors}")
                            }
                        }
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when deleting coverage statement - pending change not accepted: ${targetCov.errors}")
                }
                else throw new ChangeAcceptException("no instance of IssueEntitlementCoverage stored: ${oid}! Pending change is void!")
                break
        }
        if(done) {
            status = RDStore.PENDING_CHANGE_ACCEPTED
            if(!save()) {
                throw new ChangeAcceptException("problems when submitting new pending change status: ${errors}")
            }
        }
        done
    }

    boolean reject() {
        status = RDStore.PENDING_CHANGE_REJECTED
        if(!save()) {
            throw new ChangeAcceptException("problems when submitting new pending change status: ${errors}")
        }
        true
    }

    def workaroundForDatamigrate() {
        // workaround until refactoring is done
        if (payload) {
            JSONElement pl = getPayloadAsJSON()
            if (pl.changeType) {
                payloadChangeType = pl.changeType.toString()
            }
            if (pl.changeTarget) {
                payloadChangeTargetOid = pl.changeTarget.toString()
            }
            if (pl.changeDoc?.OID) {
                payloadChangeDocOid = pl.changeDoc.OID.toString()
            }
        }
    }

    def resolveOID() {
        genericOIDService.resolveOID(oid)
    }

    JSONElement getPayloadAsJSON() {
        payload ? JSON.parse(payload) : JSON.parse('{}')
    }

    JSONElement getChangeDocAsJSON() {
        def payload = getPayloadAsJSON()

        payload.changeDoc ?: JSON.parse('{}')
    }

    def getMessage() {

    }

    def getParsedParams() {

        Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
        JSONElement parsedParams = JSON.parse(msgParams)

        // def value type

        def type = parsedParams[0]
        parsedParams.removeAt(0)

        // find attr translation

        def prefix = ''

        if (msgToken in ['pendingChange.message_LI01']) {
            prefix = 'license.'
        }
        if (msgToken in ['pendingChange.message_SU01']) {
            prefix = 'subscription.'
        }

        if (prefix) {
            def parsed
            try {
                parsed = messageSource.getMessage(prefix + parsedParams[0], null, locale)
            }
            catch (Exception e1) {
                try {
                    parsed = messageSource.getMessage(prefix + parsedParams[0] + '.label', null, locale)
                }
                catch (Exception e2) {
                    parsed = prefix + parsedParams[0]
                }
            }
            parsedParams[0] = parsed
        }

        // resolve oid id for custom properties

        if (msgToken in ['pendingChange.message_LI02', 'pendingChange.message_SU02']) {

            def pd = genericOIDService.resolveOID(parsedParams[0])
            if (pd) {
                parsedParams[0] = pd.getI10n('name')
            }
        }

        // parse values

        if (type == 'rdv') {
            def rdv1 = genericOIDService.resolveOID(parsedParams[1])
            def rdv2 = genericOIDService.resolveOID(parsedParams[2])

            parsedParams[1] = rdv1.getI10n('value')
            parsedParams[2] = rdv2.getI10n('value')
        }
        else if (type == 'date') {
            //java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(messageSource.getMessage('default.date.format', null, locale))
            //TODO JSON @ Wed Jan 03 00:00:00 CET 2018

            //def date1 = parsedParams[1] ? sdf.parse(parsedParams[1]) : null
            //def date2 = parsedParams[2] ? sdf.parse(parsedParams[2]) : null

            //parsedParams[1] = date1
            //parsedParams[2] = date2
        }

        parsedParams
    }
}
