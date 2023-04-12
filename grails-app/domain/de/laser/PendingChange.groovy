package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.exceptions.CreationException
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.grails.web.json.JSONElement

import java.text.SimpleDateFormat

/**
 * A reflected change which may be accepted or rejected and thus controls local objects against consortial or global records
 * It tracks also history, i.e. title changes being performed on a subscription. Currently, also the property inheritance is directed by pending chnage entries as well;
 * that is why there are two parallel structures running in this domain what makes the domain very complex
 * @see AuditConfig
 * @see PendingChangeConfiguration
 */
@Slf4j
class PendingChange {

    public static final Set<String> PRICE_FIELDS = ['listPrice']
    public static final Set<String> DATE_FIELDS = ['accessStartDate', 'accessEndDate', 'startDate', 'endDate']
    public static final Set<String> REFDATA_FIELDS = ['status', 'breakable', 'file', 'consistent', 'packageStatus', 'scope', 'accessType', 'openAccess']

    public static final String PROP_PKG = 'pkg'
    public static final String PROP_SUBSCRIPTION = 'subscription'
    @Deprecated
    public static final String PROP_TIPP = 'tipp'
    @Deprecated
    public static final String PROP_TIPP_COVERAGE = 'tippCoverage'
    @Deprecated
    public static final String PROP_PRICE_ITEM = 'priceItem'
    public static final String PROP_COST_ITEM = 'costItem'

    public static final String MSG_LI01 = 'pendingChange.message_LI01'
    public static final String MSG_LI02 = 'pendingChange.message_LI02'
    public static final String MSG_SU01 = 'pendingChange.message_SU01'
    public static final String MSG_SU02 = 'pendingChange.message_SU02'

    Subscription subscription
    Package pkg
    @Deprecated
    TitleInstancePackagePlatform tipp
    @Deprecated
    TIPPCoverage tippCoverage
    @Deprecated
    PriceItem priceItem
    CostItem costItem
    Date ts
    Org owner

    String oid

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

    @RefdataInfo(cat = RDConstants.PENDING_CHANGE_STATUS)
    RefdataValue status

    static transients = ['payloadAsJSON', 'changeDocAsJSON', 'message', 'parsedParams']
    // mark read-only accessor methods

    static mapping = {
        id          column: 'pc_id'
        version     column: 'pc_version'
        subscription column: 'pc_sub_fk', index: 'pending_change_sub_idx'
        pkg column: 'pc_pkg_fk', index: 'pending_change_pkg_idx'
        tipp column: 'pc_tipp_fk', index: 'pending_change_tipp_idx'
        tippCoverage column: 'pc_tc_fk', index: 'pending_change_tc_idx'
        priceItem column: 'pc_pi_fk', index: 'pending_change_pi_idx'
        costItem column: 'pc_ci_fk', index: 'pending_change_costitem_idx'
        oid column: 'pc_oid', index: 'pending_change_oid_idx'
        targetProperty column: 'pc_target_property', type: 'text'
        oldValue column: 'pc_old_value', type: 'text'
        newValue column: 'pc_new_value', type: 'text'
        msgToken column: 'pc_msg_token', index: 'pending_change_msg_token_idx'
        msgParams column: 'pc_msg_doc', type: 'text'
        ts column: 'pc_ts', index: 'pending_change_ts_idx'
        owner column: 'pc_owner', index: 'pending_change_owner_idx'
        desc column: 'pc_desc', type: 'text'
        status column: 'pc_status_rdv_fk'
        actionDate column: 'pc_action_date'
        sort "ts": "asc"

        dateCreated column: 'pc_date_created'
        lastUpdated column: 'pc_last_updated'
    }

    static constraints = {
        subscription(nullable: true)
        msgToken(nullable: true, blank: false)
        msgParams(nullable: true, blank: false)
        pkg(nullable: true)
        tipp(nullable: true)
        tippCoverage(nullable: true)
        priceItem(nullable: true)
        costItem(nullable: true)
        ts(nullable: true)
        owner(nullable: true)
        oid(nullable: true, blank: false)
        targetProperty(nullable: true, blank: true)
        oldValue(nullable: true, blank: true)
        newValue(nullable: true, blank: true)
        desc(nullable: true, blank: false)
        status(nullable: true)
        actionDate(nullable: true)
        lastUpdated (nullable: true)
    }

    /**
     * Factory method which should replace the legacy method ChangeNotificationService.registerPendingChange()
     * @param configMap the configuration map containing the change which should be reflected
     * @return a new change record entry, with status pending or history
     * @throws CreationException
     */
    static PendingChange construct(Map<String, Object> configMap) throws CreationException {

        if ((configMap.target instanceof Subscription || configMap.target instanceof License || configMap.target instanceof CostItem || configMap.target instanceof Package ||
                configMap.target instanceof TitleInstancePackagePlatform)) {
            PendingChange pc
            String targetClass
            if (configMap.target instanceof Package)
                targetClass = PROP_PKG
            else if (configMap.target instanceof TitleInstancePackagePlatform)
                targetClass = PROP_TIPP
            else if (configMap.target instanceof Subscription)
                targetClass = PROP_SUBSCRIPTION
            else if (configMap.target instanceof CostItem)
                targetClass = PROP_COST_ITEM
            if (targetClass) {
                /*if (configMap.msgToken in SETTING_KEYS) {
                    pc = checkPendingChangeExistsForSync(configMap, targetClass)
                }*/
                if(configMap.msgToken == PendingChangeConfiguration.TITLE_REMOVED) {
                    Map<String, Object> changeParams = [target: configMap.target, msgToken: configMap.msgToken]
                    pc = new PendingChange()
                }
                else {
                    if (configMap.prop) {
                        Map<String, Object> changeParams = [target: configMap.target, prop: configMap.prop]
                        if (!configMap.oid) {
                            executeUpdate('update PendingChange pc set pc.status = :superseded where :target in (pc.subscription,pc.costItem) and pc.targetProperty = :prop', changeParams + [superseded: RDStore.PENDING_CHANGE_SUPERSEDED])
                            pc = new PendingChange()
                        } else {
                            pc = new PendingChange()
                        }
                    } else {
                        Map<String, Object> changeParams = [target: configMap.target, msgToken: configMap.msgToken, oid: configMap.oid]
                        pc = new PendingChange()
                        executeUpdate('update PendingChange pc set pc.status = :superseded where :target in (pc.subscription,pc.costItem) and pc.msgToken = :msgToken and pc.oid = :oid', changeParams + [superseded: RDStore.PENDING_CHANGE_SUPERSEDED])
                    }
                }
                switch (targetClass) {
                    case PROP_PKG: pc.pkg = (Package) configMap.target
                        break
                    case PROP_TIPP: pc.tipp = (TitleInstancePackagePlatform) configMap.target
                        break
                    case PROP_SUBSCRIPTION: pc.subscription = (Subscription) configMap.target
                        break
                    case PROP_COST_ITEM: pc.costItem = (CostItem) configMap.target
                        break
                }
                pc.msgToken = configMap.msgToken
                pc.targetProperty = configMap.prop
                if (pc.targetProperty in PendingChange.DATE_FIELDS) {
                    SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
                    pc.newValue = (configMap.newValue && configMap.newValue instanceof Date) ? sdf.format(configMap.newValue) : (configMap.newValue ?: null)
                    pc.oldValue = (configMap.oldValue && configMap.oldValue instanceof Date) ? sdf.format(configMap.oldValue) : (configMap.oldValue ?: null)
                }
                else if (pc.targetProperty in PendingChange.PRICE_FIELDS) {
                    pc.newValue = (configMap.newValue && configMap.newValue instanceof BigDecimal) ? configMap.newValue.toString() : (configMap.newValue ?: null)
                    pc.oldValue = (configMap.oldValue && configMap.oldValue instanceof BigDecimal) ? configMap.oldValue.toString() : (configMap.oldValue ?: null)
                }
                else if (pc.targetProperty in PendingChange.REFDATA_FIELDS) {
                    pc.newValue = (configMap.newValue && configMap.newValue instanceof Long) ? configMap.newValue.toString() : (configMap.newValue ?: null)
                    pc.oldValue = (configMap.oldValue && configMap.oldValue instanceof Long) ? configMap.oldValue.toString() : (configMap.oldValue ?: null)
                }
                else {
                    pc.newValue = configMap.newValue
                    pc.oldValue = configMap.oldValue
                }
                pc.oid = configMap.oid
                pc.status = configMap.status
                pc.ts = new Date()
                pc.owner = configMap.owner
                if (pc.hasErrors())
                    throw new CreationException("Error on hooking up pending change: ${pc.errors.getAllErrors().toListString()}")
                else pc.save()
            }
        } else throw new CreationException("Pending changes need a target! Check if configMap.target is correctly set!")
    }

    /**
     * Checks if a change record exists already with the given parameters
     * @param configMap the parameter map containing the chage data to be reflected
     * @param targetClass the domain class the change is attached to
     * @return the change record, if it exists or an empty one, if not
     */
    @Deprecated
    static PendingChange checkPendingChangeExistsForSync(Map<String, Object> configMap, String targetClass) {
        Map<String, Object> changeParams = [target  : configMap.target,
                                            msgToken: configMap.msgToken]
        PendingChange pc

        if(configMap.owner){
            changeParams << [owner: configMap.owner]
        }

        if (configMap.prop) {
            if (configMap.prop in PendingChange.DATE_FIELDS) {
                SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
                changeParams.newValue = (configMap.newValue && configMap.newValue instanceof Date) ? sdf.format(configMap.newValue) : (configMap.newValue ?: null)
                changeParams.oldValue = (configMap.oldValue && configMap.oldValue instanceof Date) ? sdf.format(configMap.oldValue) : (configMap.oldValue ?: null)
            }
            else if (configMap.prop in PendingChange.PRICE_FIELDS) {
                changeParams.newValue = (configMap.newValue && configMap.newValue instanceof BigDecimal) ? configMap.newValue.toString() : (configMap.newValue ?: null)
                changeParams.oldValue = (configMap.oldValue && configMap.oldValue instanceof BigDecimal) ? configMap.oldValue.toString() : (configMap.oldValue ?: null)
            }
            else if (configMap.prop in PendingChange.REFDATA_FIELDS) {
                changeParams.newValue = (configMap.newValue && configMap.newValue instanceof Long) ? configMap.newValue.toString() : (configMap.newValue ?: null)
                changeParams.oldValue = (configMap.oldValue && configMap.oldValue instanceof Long) ? configMap.oldValue.toString() : (configMap.oldValue ?: null)
            }
            else {
                changeParams.newValue = configMap.newValue
                changeParams.oldValue = configMap.oldValue
            }

            changeParams << [prop: configMap.prop]
            if (!configMap.oid) {
                List<PendingChange> pendingChangeCheck = executeQuery('select pc from PendingChange pc where pc.status in (:pending) and pc.' + targetClass + ' = :target and pc.targetProperty = :prop and pc.msgToken = :msgToken and pc.newValue = :newValue and pc.oldValue = :oldValue ' + (changeParams.owner ? 'and pc.owner = :owner' : '') + ' order by pc.ts desc', changeParams + [pending: [RDStore.PENDING_CHANGE_HISTORY]])
                if (pendingChangeCheck) {
                    pc = pendingChangeCheck[0]
                    if (pendingChangeCheck.size() > 0) {
                        log.error("checkPendingChangeExists: pendingChangeCheck.size() > 0 by " + changeParams)
                    }
                } else {
                    pc = new PendingChange()
                }
            } else {
                changeParams << [oid: configMap.oid]
                List<PendingChange> pendingChangeCheck = executeQuery('select pc from PendingChange pc where pc.status in (:pending) and pc.' + targetClass + ' = :target and pc.oid = :oid and pc.targetProperty = :prop and pc.msgToken = :msgToken and pc.newValue = :newValue and pc.oldValue = :oldValue ' + (changeParams.owner ? 'and pc.owner = :owner' : '') + ' order by pc.ts desc', changeParams + [pending: [RDStore.PENDING_CHANGE_PENDING]])
                if (pendingChangeCheck) {
                    pc = pendingChangeCheck[0]
                    if (pendingChangeCheck.size() > 0) {
                        log.error("checkPendingChangeExists: pendingChangeCheck.size() > 0 by " + changeParams)
                    }
                } else {
                    pc = new PendingChange()
                }
            }
        } else {
            if(configMap.containsKey('oid')) {
                changeParams << [oid: configMap.oid]
                if(configMap.containsKey('oldValue'))
                    changeParams.oldValue = configMap.oldValue
                if(configMap.containsKey('newValue'))
                    changeParams.newValue = configMap.newValue
                List<PendingChange> pendingChangeCheck = executeQuery('select pc from PendingChange pc where pc.status in (:pending) and pc.oid = :oid and pc.' + targetClass + ' = :target and pc.msgToken = :msgToken and pc.newValue = :newValue and pc.oldValue = :oldValue ' + (changeParams.owner ? 'and pc.owner = :owner' : '') + ' order by pc.ts desc', changeParams + [pending: [RDStore.PENDING_CHANGE_PENDING]])
                if (pendingChangeCheck) {
                    pc = pendingChangeCheck[0]
                    if (pendingChangeCheck.size() > 0) {
                        log.error("checkPendingChangeExists: pendingChangeCheck.size() > 0 by " + changeParams)
                    }
                } else {
                    pc = new PendingChange()
                }
            }
            else {
                List<PendingChange> pendingChangeCheck = executeQuery('select pc from PendingChange pc where pc.status in (:pending) and pc.' + targetClass + ' = :target and pc.msgToken = :msgToken order by pc.ts desc', changeParams + [pending: [RDStore.PENDING_CHANGE_HISTORY]])
                if (pendingChangeCheck) {
                    pc = pendingChangeCheck[0]
                    if (pendingChangeCheck.size() > 0) {
                        log.error("checkPendingChangeExists: pendingChangeCheck.size() > 0 by " + changeParams)
                    }
                } else {
                    pc = new PendingChange()
                }
            }
        }
        return pc
    }
}
