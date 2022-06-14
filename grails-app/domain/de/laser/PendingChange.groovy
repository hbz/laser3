package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.exceptions.CreationException
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.helper.DateUtils
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
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

    final static Set<String> PRICE_FIELDS = ['listPrice']
    final static Set<String> DATE_FIELDS = ['accessStartDate', 'accessEndDate', 'startDate', 'endDate']
    final static Set<String> REFDATA_FIELDS = ['status', 'breakable', 'file', 'consistent', 'packageStatus', 'scope']

    final static PROP_LICENSE = 'license'
    final static PROP_PKG = 'pkg'
    final static PROP_SUBSCRIPTION = 'subscription'
    final static PROP_TIPP = 'tipp'
    final static PROP_TIPP_COVERAGE = 'tippCoverage'
    final static PROP_PRICE_ITEM = 'priceItem'
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

    @RefdataInfo(cat = RDConstants.PENDING_CHANGE_STATUS)
    RefdataValue status

    static transients = ['payloadAsJSON', 'changeDocAsJSON', 'message', 'parsedParams']
    // mark read-only accessor methods

    static mapping = {
        subscription column: 'pc_sub_fk', index: 'pending_change_sub_idx'
        license column: 'pc_lic_fk', index: 'pending_change_lic_idx'
        pkg column: 'pc_pkg_fk', index: 'pending_change_pkg_idx'
        tipp column: 'pc_tipp_fk', index: 'pending_change_tipp_idx'
        tippCoverage column: 'pc_tc_fk', index: 'pending_change_tc_idx'
        priceItem column: 'pc_pi_fk', index: 'pending_change_pi_idx'
        costItem column: 'pc_ci_fk', index: 'pending_change_costitem_idx'
        oid column: 'pc_oid', index: 'pending_change_oid_idx'
        payloadChangeType column: 'pc_change_type'
        payloadChangeTargetOid column: 'pc_change_target_oid', index: 'pending_change_pl_ct_oid_idx'
        payloadChangeDocOid column: 'pc_change_doc_oid', index: 'pending_change_pl_cd_oid_idx'
        targetProperty column: 'pc_target_property', type: 'text'
        oldValue column: 'pc_old_value', type: 'text'
        newValue column: 'pc_new_value', type: 'text'
        payload column: 'pc_payload', type: 'text'
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
        license(nullable: true)
        payload(nullable: true, blank: false)
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
        payloadChangeType(nullable: true, blank: true)
        payloadChangeTargetOid(nullable: true, blank: false)
        payloadChangeDocOid(nullable: true, blank: false)
        targetProperty(nullable: true, blank: true)
        oldValue(nullable: true, blank: true)
        newValue(nullable: true, blank: true)
        desc(nullable: true, blank: false)
        status(nullable: true)
        actionDate(nullable: true)

        // Nullable is true, because values are already in the database
        lastUpdated(nullable: true)
        dateCreated(nullable: true)
    }

    /**
     * Factory method which should replace the legacy method ChangeNotificationService.registerPendingChange()
     * @param configMap the configuration map containing the change which should be reflected
     * @return a new change record entry, with status pending or history
     * @throws CreationException
     */
    static PendingChange construct(Map<String, Object> configMap) throws CreationException {

        Set<String> SETTING_KEYS = [PendingChangeConfiguration.NEW_TITLE,
                                    PendingChangeConfiguration.TITLE_UPDATED,
                                    PendingChangeConfiguration.NEW_COVERAGE,
                                    PendingChangeConfiguration.COVERAGE_UPDATED,
                                    PendingChangeConfiguration.PACKAGE_PROP]
        SETTING_KEYS << PendingChangeConfiguration.PACKAGE_TIPP_COUNT_CHANGED

        if ((configMap.target instanceof Subscription || configMap.target instanceof License || configMap.target instanceof CostItem || configMap.target instanceof Package ||
                configMap.target instanceof TitleInstancePackagePlatform || configMap.target instanceof PriceItem || configMap.target instanceof TIPPCoverage)) {
            PendingChange pc
            String targetClass
            if (configMap.target instanceof Package)
                targetClass = PROP_PKG
            else if (configMap.target instanceof TitleInstancePackagePlatform)
                targetClass = PROP_TIPP
            else if (configMap.target instanceof TIPPCoverage)
                targetClass = PROP_TIPP_COVERAGE
            else if (configMap.target instanceof PriceItem)
                targetClass = PROP_PRICE_ITEM
            else if (configMap.target instanceof Subscription)
                targetClass = PROP_SUBSCRIPTION
            else if (configMap.target instanceof License)
                targetClass = PROP_LICENSE
            else if (configMap.target instanceof CostItem)
                targetClass = PROP_COST_ITEM
            if (targetClass) {
                if (configMap.msgToken in SETTING_KEYS) {
                    pc = checkPendingChangeExistsForSync(configMap, targetClass)
                } else {
                    if (configMap.prop) {
                        Map<String, Object> changeParams = [target: configMap.target, prop: configMap.prop]
                        if (!configMap.oid) {
                            List<PendingChange> pendingChangeCheck = executeQuery('select pc from PendingChange pc where pc.status in (:processed) and pc.' + targetClass + ' = :target and pc.targetProperty = :prop', changeParams + [processed: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_PENDING, RDStore.PENDING_CHANGE_HISTORY]])
                            if (pendingChangeCheck)
                                return pendingChangeCheck[0]
                            else pc = new PendingChange()
                            executeUpdate('update PendingChange pc set pc.status = :superseded where :target in (pc.subscription,pc.license,pc.costItem) and pc.targetProperty = :prop', changeParams + [superseded: RDStore.PENDING_CHANGE_SUPERSEDED])
                        } else {
                            changeParams.oid = configMap.oid
                            List<PendingChange> pendingChangeCheck = executeQuery('select pc from PendingChange pc where pc.status in (:processed) and pc.' + targetClass + ' = :target and pc.oid = :oid and pc.targetProperty = :prop', changeParams + [processed: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_PENDING]])
                            if (pendingChangeCheck)
                                return pendingChangeCheck[0]
                            else pc = new PendingChange()
                        }
                    } else {
                        Map<String, Object> changeParams = [target: configMap.target, msgToken: configMap.msgToken, oid: configMap.oid]
                        List<PendingChange> pendingChangeCheck = executeQuery('select pc from PendingChange pc where pc.status in (:processed) and pc.oid = :oid and pc.' + targetClass + ' = :target and pc.msgToken = :msgToken', changeParams + [processed: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_PENDING, RDStore.PENDING_CHANGE_HISTORY]])
                        if (pendingChangeCheck)
                            return pendingChangeCheck[0]
                        else pc = new PendingChange()
                        executeUpdate('update PendingChange pc set pc.status = :superseded where :target in (pc.subscription,pc.license,pc.costItem) and pc.msgToken = :msgToken and pc.oid = :oid', changeParams + [superseded: RDStore.PENDING_CHANGE_SUPERSEDED])
                    }
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
    static PendingChange checkPendingChangeExistsForSync(Map<String, Object> configMap, String targetClass) {
        Map<String, Object> changeParams = [target  : configMap.target,
                                            msgToken: configMap.msgToken,
                                            newValue: configMap.newValue,
                                            oldValue: configMap.oldValue]
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
                List<PendingChange> pendingChangeCheck = executeQuery('select pc from PendingChange pc where pc.status in (:pending) and pc.' + targetClass + ' = :target and pc.targetProperty = :prop and pc.msgToken = :msgToken and pc.newValue = :newValue and pc.oldValue = :oldValue ' + (changeParams.owner ? 'and pc.owner = :owner' : '') + ' order by pc.ts desc', changeParams + [pending: [RDStore.PENDING_CHANGE_PENDING]])
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
            changeParams << [oid: configMap.oid]
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

        return pc
    }

    /**
     * This is a workaround to fetch data from a pending change; initially stored in JSON and thus unable to perform queries on
     */
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

    /**
     * Resolves the stored OID of this change entry
     * @return the resolved object
     */
    def resolveOID() {
        BeanStore.getGenericOIDService().resolveOID(oid)
    }

    /**
     * Returns the change data stored as JSON. Works only for those changes which are in JSON only
     * @return a JSON entry reflecting the change data, an empty object if no data exists
     */
    JSONElement getPayloadAsJSON() {
        payload ? JSON.parse(payload) : JSON.parse('{}')
    }

    /**
     * Backwards compatible getter method for the change stored as JSON
     * @return a JSON entry reflecting the change data, an empty object if no data exists
     * @see #getPayloadAsJSON()
     */
    JSONElement getChangeDocAsJSON() {
        def payload = getPayloadAsJSON()

        payload.changeDoc ?: JSON.parse('{}')
    }

    @Deprecated
    def getMessage() {

    }

    /**
     * Gets the parameters of this change and returns them as an array
     * @return an array containing the change data:
     * <ol>
     *     <li>the object's name</li>
     *     <li>the old value</li>
     *     <li>the new value</li>
     * </ol>
     * Subtract one from each list number to get the according array index
     */
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
                parsed = BeanStore.getMessageSource().getMessage(prefix + parsedParams[0], null, locale)
            }
            catch (Exception e1) {
                try {
                    parsed = BeanStore.getMessageSource().getMessage(prefix + parsedParams[0] + '.label', null, locale)
                }
                catch (Exception e2) {
                    parsed = prefix + parsedParams[0]
                }
            }
            parsedParams[0] = parsed
        }

        // resolve oid id for custom properties

        if (msgToken in ['pendingChange.message_LI02', 'pendingChange.message_SU02']) {

            def pd = resolveOID(parsedParams[0])
            if (pd) {
                parsedParams[0] = pd.getI10n('name')
            }
        }

        // parse values

        if (type == 'rdv') {
            def rdv1 = resolveOID(parsedParams[1])
            def rdv2 = resolveOID(parsedParams[2])

            parsedParams[1] = rdv1.getI10n('value')
            parsedParams[2] = rdv2.getI10n('value')
        } else if (type == 'date') {
            //java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(BeanStore.getMessageSource().getMessage('default.date.format', null, locale))
            //TODO JSON @ Wed Jan 03 00:00:00 CET 2018

            //def date1 = parsedParams[1] ? sdf.parse(parsedParams[1]) : null
            //def date2 = parsedParams[2] ? sdf.parse(parsedParams[2]) : null

            //parsedParams[1] = date1
            //parsedParams[2] = date2
        }

        parsedParams
    }
}
