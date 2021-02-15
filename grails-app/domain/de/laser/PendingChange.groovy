package de.laser

import de.laser.base.AbstractCoverage
import de.laser.finance.CostItem
import de.laser.exceptions.ChangeAcceptException
import de.laser.exceptions.CreationException
import de.laser.finance.PriceItem
import de.laser.helper.DateUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.annotations.RefdataAnnotation
import grails.converters.JSON
import net.sf.json.JSONObject
import org.grails.web.json.JSONElement

import java.text.SimpleDateFormat

class PendingChange {

    def genericOIDService
    def messageSource

    final static Set<String> DATE_FIELDS = ['accessStartDate','accessEndDate','startDate','endDate']
    final static Set<String> REFDATA_FIELDS = ['status','packageListStatus','breakable','fixed','consistent','packageStatus','packageScope']

    final static PROP_LICENSE       = 'license'
    final static PROP_PKG           = 'pkg'
    final static PROP_SUBSCRIPTION  = 'subscription'
    final static PROP_TIPP          = 'tipp'
    final static PROP_TIPP_COVERAGE = 'tippCoverage'
    final static PROP_PRICE_ITEM    = 'priceItem'
    final static PROP_COST_ITEM = 'costItem'

    final static MSG_LI01 = 'pendingChange.message_LI01'
    final static MSG_LI02 = 'pendingChange.message_LI02'
    final static MSG_SU01 = 'pendingChange.message_SU01'
    final static MSG_SU02 = 'pendingChange.message_SU02'

    Subscription subscription
    License license
    Package pkg
    TitleInstancePackagePlatform tipp
    TIPPCoverage tippCoverage
    PriceItem priceItem
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

    static transients = ['payloadAsJSON', 'changeDocAsJSON', 'message', 'parsedParams'] // mark read-only accessor methods

    static mapping = {
        subscription column:'pc_sub_fk',        index:'pending_change_sub_idx'
            license column:'pc_lic_fk',         index:'pending_change_lic_idx'
                pkg column:'pc_pkg_fk',         index:'pending_change_pkg_idx'
               tipp column:'pc_tipp_fk',        index:'pending_change_tipp_idx'
       tippCoverage column:'pc_tc_fk',          index:'pending_change_tc_idx'
          priceItem column:'pc_pi_fk',          index:'pending_change_pi_idx'
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
              owner column:'pc_owner',         index:'pending_change_owner_idx'
               desc column:'pc_desc', type:'text'
             status column:'pc_status_rdv_fk'
         actionDate column:'pc_action_date'
               sort "ts":"asc"

        dateCreated column: 'pc_date_created'
        lastUpdated column: 'pc_last_updated'
    }

    static constraints = {
        subscription    (nullable:true)
        license         (nullable:true)
        payload(nullable:true, blank:false)
        msgToken(nullable:true, blank:false)
        msgParams(nullable:true, blank:false)
        pkg             (nullable:true)
        tipp            (nullable:true)
        tippCoverage    (nullable:true)
        priceItem       (nullable:true)
        costItem        (nullable:true)
        ts              (nullable:true)
        owner           (nullable:true)
        oid(nullable:true, blank:false)
        payloadChangeType       (nullable:true, blank:true)
        payloadChangeTargetOid  (nullable:true, blank:false)
        payloadChangeDocOid     (nullable:true, blank:false)
        targetProperty(nullable:true, blank:true)
        oldValue(nullable:true, blank:true)
        newValue(nullable:true, blank:true)
        desc(nullable:true, blank:false)
        status          (nullable:true)
        actionDate (nullable:true)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }

    /**
     * Factory method which should replace the legacy method ChangeNotificationService.registerPendingChange().
     * @param configMap
     * @return
     * @throws CreationException
     */
    static PendingChange construct(Map<String,Object> configMap) throws CreationException {
        if((configMap.target instanceof Subscription || configMap.target instanceof License || configMap.target instanceof CostItem || configMap.target instanceof Package ||
            configMap.target instanceof TitleInstancePackagePlatform || configMap.target instanceof PriceItem || configMap.target instanceof TIPPCoverage)) {
            PendingChange pc
            String targetClass
            if(configMap.target instanceof Package)
                targetClass = PROP_PKG
            else if(configMap.target instanceof TitleInstancePackagePlatform)
                targetClass = PROP_TIPP
            else if(configMap.target instanceof TIPPCoverage)
                targetClass = PROP_TIPP_COVERAGE
            else if(configMap.target instanceof PriceItem)
                targetClass = PROP_PRICE_ITEM
            else if(configMap.target instanceof Subscription)
                targetClass = PROP_SUBSCRIPTION
            else if(configMap.target instanceof License)
                targetClass = PROP_LICENSE
            else if(configMap.target instanceof CostItem)
                targetClass = PROP_COST_ITEM
            if(targetClass) {
                if(configMap.prop) {
                    Map<String, Object> changeParams = [target: configMap.target, prop: configMap.prop]
                    if(!configMap.oid) {
                        List<PendingChange> pendingChangeCheck = executeQuery('select pc from PendingChange pc where pc.status in (:processed) and pc.' + targetClass + ' = :target and pc.targetProperty = :prop', changeParams + [processed: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_PENDING, RDStore.PENDING_CHANGE_HISTORY]])
                        if (pendingChangeCheck)
                            return pendingChangeCheck[0]
                        else pc = new PendingChange()
                        executeUpdate('update PendingChange pc set pc.status = :superseded where :target in (pc.subscription,pc.license,pc.costItem) and pc.targetProperty = :prop',changeParams+[superseded:RDStore.PENDING_CHANGE_SUPERSEDED])
                    }
                    else {
                        changeParams.oid = configMap.oid
                        List<PendingChange> pendingChangeCheck = executeQuery('select pc from PendingChange pc where pc.status in (:processed) and pc.' + targetClass + ' = :target and pc.oid = :oid and pc.targetProperty = :prop', changeParams + [processed: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_PENDING]])
                        if (pendingChangeCheck)
                            return pendingChangeCheck[0]
                        else pc = new PendingChange()
                    }
                }
                else {
                    Map<String,Object> changeParams = [target:configMap.target,msgToken:configMap.msgToken,oid:configMap.oid]
                    List<PendingChange> pendingChangeCheck = executeQuery('select pc from PendingChange pc where pc.status in (:processed) and pc.oid = :oid and pc.'+targetClass+' = :target and pc.msgToken = :msgToken',changeParams+[processed:[RDStore.PENDING_CHANGE_ACCEPTED,RDStore.PENDING_CHANGE_PENDING,RDStore.PENDING_CHANGE_HISTORY]])
                    if(pendingChangeCheck)
                        return pendingChangeCheck[0]
                    else pc = new PendingChange()
                    executeUpdate('update PendingChange pc set pc.status = :superseded where :target in (pc.subscription,pc.license,pc.costItem) and pc.msgToken = :msgToken and pc.oid = :oid',changeParams+[superseded:RDStore.PENDING_CHANGE_SUPERSEDED])
                }
                switch (targetClass) {
                    case PROP_PKG: pc.pkg = (Package) configMap.target
                        break
                    case PROP_TIPP: pc.tipp = (TitleInstancePackagePlatform) configMap.target
                        break
                    case PROP_TIPP_COVERAGE: pc.tippCoverage = (TIPPCoverage) configMap.target
                        break
                    case PROP_PRICE_ITEM: pc.priceItem = (PriceItem) configMap.target
                        break
                    case PROP_SUBSCRIPTION: pc.subscription = (Subscription) configMap.target
                        break
                    case PROP_LICENSE: pc.license = (License) configMap.target
                        break
                    case PROP_COST_ITEM: pc.costItem = (CostItem) configMap.target
                        break
                }
                pc.msgToken = configMap.msgToken
                pc.targetProperty = configMap.prop
                if(pc.targetProperty in PendingChange.DATE_FIELDS) {
                    SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                    pc.newValue = configMap.newValue && configMap.newValue instanceof Date ? sdf.format(configMap.newValue) : null
                    pc.oldValue = configMap.oldValue && configMap.oldValue instanceof Date ? sdf.format(configMap.oldValue) : null
                }
                else {
                    pc.newValue = configMap.newValue
                    pc.oldValue = configMap.oldValue
                }
                pc.oid = configMap.oid
                pc.status = configMap.status
                pc.ts = new Date()
                pc.owner = configMap.owner
                if(pc.save())
                    pc
                else throw new CreationException("Error on hooking up pending change: ${pc.errors}")
            }
        }
        else throw new CreationException("Pending changes need a target! Check if configMap.target is correctly set!")
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
