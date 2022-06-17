package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.exceptions.CreationException
import de.laser.storage.RDConstants

/**
 * This class represents a switch cabinet for a subscription package. Each package linked to a subscription contains a
 * set of configuration settings which controls behavior of title (or package) updates:
 * <ul>
 *     <li>a title change may be accepted or not</li>
 *     <li>a new title may be accepted or rejected (or become subject of a survey)</li>
 *     <li>a title deletion may be accepted or not</li>
 *     <li>every change may be passed to member objects; change settings may differ between consortial and member level</li>
 *     <li>an editor may decide if the organisation should be notified about a change; this change may or may not passed to members, too</li>
 * </ul>
 * Means: there are three possible settings for each configuration (Accept, Prompt (default), Reject) and each configuration may be inherited.
 * A pending change configuration is mandatory for every package and is thus belonging to the subscription-package link since every subscription may have different settings to the same package
 * @see SubscriptionPackage
 * @see SurveyInfo
 * @see AuditConfig
 */
class PendingChangeConfiguration {

    static final String NEW_TITLE = "pendingChange.message_TP01"
    static final String TITLE_UPDATED = "pendingChange.message_TP02"
    static final String TITLE_DELETED = "pendingChange.message_TP03"
    static final String TITLE_REMOVED = "pendingChange.message_TP04"
    static final String COVERAGE_UPDATED = "pendingChange.message_TC01"
    static final String NEW_COVERAGE = "pendingChange.message_TC02"
    static final String COVERAGE_DELETED = "pendingChange.message_TC03"
    static final String PRICE_UPDATED = "pendingChange.message_TR01"
    static final String NEW_PRICE = "pendingChange.message_TR02"
    static final String PRICE_DELETED = "pendingChange.message_TR03"
    static final String PACKAGE_PROP = "pendingChange.message_PK01"
    static final String PACKAGE_DELETED = "pendingChange.message_PK02"
    static final String PACKAGE_TIPP_COUNT_CHANGED = "pendingChange.message_PK03"
    static final String BILLING_SUM_UPDATED = "pendingChange.message_CI01"
    static final String LOCAL_SUM_UPDATED = "pendingChange.message_CI02"
    static final String NOTIFICATION_SUFFIX = "_N"
    static final Set<String> SETTING_KEYS = [NEW_TITLE, TITLE_UPDATED, TITLE_DELETED, NEW_COVERAGE, COVERAGE_UPDATED, COVERAGE_DELETED, PACKAGE_PROP, PACKAGE_DELETED]

    String settingKey
    @RefdataInfo(cat = RDConstants.PENDING_CHANGE_CONFIG_SETTING)
    RefdataValue settingValue
    boolean withNotification = false

    static belongsTo = [subscriptionPackage: SubscriptionPackage]

    static mapping = {
        id                      column: 'pcc_id'
        version                 column: 'pcc_version'
        subscriptionPackage     column: 'pcc_sp_fk', index: 'pcc_sp_idx'
        settingKey              column: 'pcc_setting_key_enum'
        settingValue            column: 'pcc_setting_value_rv_fk'
        withNotification        column: 'pcc_with_notification'
    }

    static constraints = {
        settingValue(nullable:true) //for package changes; they are with notification only
    }

    /**
     * Constructs a new configuration entry with the given map
     * @param configMap the map of parameters which should be retained
     * @return the pending change configuration responding to the given subscription package and message token (setting), if it does not exist, it will be created
     * @throws CreationException
     */
    static PendingChangeConfiguration construct(Map<String,Object> configMap) throws CreationException {
        withTransaction {
            if (configMap.subscriptionPackage instanceof SubscriptionPackage) {
                PendingChangeConfiguration pcc = PendingChangeConfiguration.findBySubscriptionPackageAndSettingKey((SubscriptionPackage) configMap.subscriptionPackage, configMap.settingKey)
                if (!pcc)
                    pcc = new PendingChangeConfiguration(subscriptionPackage: (SubscriptionPackage) configMap.subscriptionPackage, settingKey: configMap.settingKey)
                pcc.settingValue = configMap.settingValue
                pcc.withNotification = configMap.withNotification
                if (pcc.save()) {
                    pcc
                } else {
                    throw new CreationException("Error on saving pending change configuration: ${pcc.errors}")
                }
            } else {
                throw new CreationException("Invalid subscription package object given: ${configMap.subscriptionPackage}")
            }
        }
    }
}
