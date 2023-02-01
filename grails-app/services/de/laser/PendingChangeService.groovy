package de.laser

import de.laser.base.AbstractCoverage
import de.laser.base.AbstractLockableService
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.cache.SessionCacheWrapper
import de.laser.exceptions.ChangeAcceptException
import de.laser.finance.CostItem
import de.laser.properties.PropertyDefinition
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.CodeUtils
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.web.databinding.DataBindingUtils
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject
import org.springframework.context.MessageSource
import org.springframework.transaction.TransactionStatus

import java.sql.Array
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Year

/**
 * This service handles pending change processing and display
 */
@Transactional
class PendingChangeService extends AbstractLockableService {

    AuditService auditService
    ContextService contextService
    EscapeService escapeService
    GenericOIDService genericOIDService
    MessageSource messageSource

    /**
     * Gets the recent and pending changes on titles for the given institution. This method has been translated into SQL
     * queries because the GORM loading slows the query very much down
     * @param configMap a map containing the configuration parameters such as context institution, user's time setting
     * @return a map containing title changes and notification
     */
    Map<String, Object> getChanges(LinkedHashMap<String, Object> configMap) {
        Map<String, Object> result = [:]
        Locale locale = LocaleUtils.getCurrentLocale()
        Date time = new Date(System.currentTimeMillis() - Duration.ofDays(configMap.periodInDays).toMillis())
        Sql sql = GlobalService.obtainSqlConnection()
        List pending = [], notifications = []
        Map<String, Long> roleTypes = ["consortia": RDStore.OR_SUBSCRIPTION_CONSORTIA.id, "subscriber": RDStore.OR_SUBSCRIBER.id, "member": RDStore.OR_SUBSCRIBER_CONS.id]
        Map<Long, String> status = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS).collectEntries { RefdataValue rdv -> [(rdv.id): rdv.getI10n("value")] }
        Map<Long, String> acceptedStatus = [(RDStore.PENDING_CHANGE_ACCEPTED.id): RDStore.PENDING_CHANGE_ACCEPTED.getI10n("value"), (RDStore.PENDING_CHANGE_REJECTED.id): RDStore.PENDING_CHANGE_REJECTED.getI10n("value"), (RDStore.PENDING_CHANGE_PENDING.id): RDStore.PENDING_CHANGE_PENDING.getI10n("value")]
        Object[] retObj = RefdataValue.findAllByOwnerAndValueInList(RefdataCategory.findByDesc(RDConstants.SUBSCRIPTION_STATUS), ['Terminated', 'Expired', 'Rejected', 'Publication discontinued', 'No longer usable', 'Deferred']).collect { RefdataValue rdv -> rdv.id } as Object[]
        //IMPORTANT! In order to avoid session mismatches, NO domain operation may take at this place! DO NOT USE GORM methods here!
        sql.withTransaction {
            Array retired = sql.connection.createArrayOf('bigint', retObj)
            //package changes
            String subscribedPackagesQuery = "select pcc_sp_fk as subPkg, sp_pkg_fk as pkg, sp_sub_fk as sub, sp_date_created, pcc_setting_key_enum as key, case when pcc_setting_value_rv_fk = :prompt then true else false end as prompt, pcc_with_notification as with_notification from pending_change_configuration join subscription_package on pcc_sp_fk = sp_id join subscription on sp_sub_fk = sub_id join org_role on sp_sub_fk = or_sub_fk where not (sub_status_rv_fk = any(:retired)) and or_org_fk = :context and or_roletype_fk = any(:roleTypes)"
            if(configMap.consortialView)
                subscribedPackagesQuery += " and (pcc_setting_value_rv_fk = :prompt or pcc_with_notification = true) and sub_parent_sub_fk is null"
            List subscribedPackages = sql.rows(subscribedPackagesQuery, [retired: retired, context: configMap.contextOrg.id, roleTypes: sql.connection.createArrayOf('bigint', [RDStore.OR_SUBSCRIPTION_CONSORTIA.id,RDStore.OR_SUBSCRIBER_CONS.id,RDStore.OR_SUBSCRIBER.id] as Object[]), prompt: RDStore.PENDING_CHANGE_CONFIG_PROMPT.id])
            if(!configMap.consortialView) {
                subscribedPackages.addAll(sql.rows("select sp_id as subPkg, sp_pkg_fk as pkg, sp_sub_fk as sub, sp_date_created, auc_reference_field as key, true as with_notification, null as prompt from subscription_package join org_role on sp_sub_fk = or_sub_fk join subscription on or_sub_fk = sub_id join audit_config on auc_reference_id = sub_parent_sub_fk where not (sub_status_rv_fk = any(:retired)) and or_org_fk = :context and auc_reference_field = any(:settingKeys) and sp_sub_fk = sub_id", [retired: retired, context: configMap.contextOrg.id, settingKeys: sql.connection.createArrayOf('varchar', PendingChangeConfiguration.SETTING_KEYS.collect { String key -> key+PendingChangeConfiguration.NOTIFICATION_SUFFIX }  as Object[])]))
            }
            Map<String, Map<List, Set<String>>> packageConfigMap = [notify: [:], prompt: [:]]
            subscribedPackages.each { GroovyRowResult row ->
                //log.debug(row.toString())
                Long spId = row.get('subPkg')
                Long spPkg = row.get('pkg')
                Long spSub = row.get('sub')
                Date dateEntry = row.get('sp_date_created')
                if(row.get('prompt') != null && row.get('prompt') == true) {
                    Set<String> packageSettings = packageConfigMap.prompt.get([spId, spPkg, dateEntry, spSub])
                    if(!packageSettings)
                        packageSettings = []
                    packageSettings << row.get('key')
                    packageConfigMap.prompt.put([spId, spPkg, dateEntry, spSub], packageSettings)
                }
                if(row.get('with_notification') == true) {
                    Set<String> packageSettings = packageConfigMap.notify.get([spId, spPkg, dateEntry, spSub])
                    if(!packageSettings)
                        packageSettings = []
                    packageSettings << row.get('key')
                    packageConfigMap.notify.put([spId, spPkg, dateEntry, spSub], packageSettings)
                }
            }
            //log.debug(packageConfigMap.notify.keySet().collect { List pkgSetting -> pkgSetting[0] }.toListString())
            //log.debug(packageConfigMap.prompt.keySet().collect { List pkgSetting -> pkgSetting[0] }.toListString())
            /*
                I need to:
                - get the concerned subscription IDs
                - get the change counts
             */
            //log.debug("start")
            //December 8th: observe the queries, it may be that with data, they will err
            Set subNotifyPkgs = packageConfigMap.notify.keySet(), subPromptPkgs = packageConfigMap.prompt.keySet()
            String notificationsQuery1 = "select count(pc_id) as count, pc_pkg_fk as pkg, sp_id, pc_msg_token from pending_change join subscription_package on pc_pkg_fk = sp_pkg_fk where sp_id = any(:sp) and pc_ts >= :date and pc_msg_token is not null and pc_status_rdv_fk = :accepted group by pc_pkg_fk, sp_id, pc_msg_token"
            List pkgCount = sql.rows(notificationsQuery1, [sp: sql.connection.createArrayOf('bigint', subNotifyPkgs.collect{ List sp -> sp[0] } as Object[]), date: time.toTimestamp(), accepted: RDStore.PENDING_CHANGE_ACCEPTED.id])
            //log.debug(pkgCount.toListString())
            String notificationsQuery2 = "select count(pc_id) as count, tipp_pkg_fk as pkg, sp_id, pc_msg_token from pending_change join title_instance_package_platform on pc_tipp_fk = tipp_id join subscription_package on tipp_pkg_fk = sp_pkg_fk where sp_id = any(:sp) and (regexp_split_to_array(pc_oid, '${Subscription.class.name}:'))[2]::bigint = sp_sub_fk and pc_ts >= :date and pc_msg_token is not null and pc_status_rdv_fk = :accepted group by tipp_pkg_fk, sp_id, pc_msg_token"
            List titleCount = sql.rows(notificationsQuery2, [sp: sql.connection.createArrayOf('bigint', subNotifyPkgs.collect{ List sp -> sp[0] } as Object[]), date: time.toTimestamp(), accepted: RDStore.PENDING_CHANGE_ACCEPTED.id])
            //log.debug(titleCount.toListString())
            String notificationsQuery3 = "select count(pc_id) as count, tipp_pkg_fk as pkg, sp_id, pc_msg_token from pending_change join tippcoverage on pc_tc_fk = tc_id join title_instance_package_platform on tc_tipp_fk = tipp_id join subscription_package on tipp_pkg_fk = sp_pkg_fk where sp_id = any(:sp) and (regexp_split_to_array(pc_oid, '${Subscription.class.name}:'))[2]::bigint = sp_sub_fk and pc_ts >= :date and pc_msg_token is not null and pc_status_rdv_fk = :accepted group by tipp_pkg_fk, sp_id, pc_msg_token"
            List coverageCount = sql.rows(notificationsQuery3, [sp: sql.connection.createArrayOf('bigint', subNotifyPkgs.collect{ List sp -> sp[0] } as Object[]), date: time.toTimestamp(), accepted: RDStore.PENDING_CHANGE_ACCEPTED.id])
            //log.debug(coverageCount.toListString())
            String pendingQuery1 = "select count(pc.pc_id) as count, tipp_pkg_fk as pkg, sp_id, pc.pc_msg_token from pending_change as pc join title_instance_package_platform on pc.pc_tipp_fk = tipp_id join subscription_package on tipp_pkg_fk = sp_pkg_fk where sp_id = any(:sp) and pc.pc_oid is null and not exists(select done.pc_id from pending_change as done where pc.pc_tipp_fk = done.pc_tipp_fk and done.pc_status_rdv_fk = any(:accepted) and (regexp_split_to_array(done.pc_oid, '${Subscription.class.name}:'))[2]::bigint = sp_sub_fk) and pc.pc_msg_token = :newTitle and pc.pc_ts > sp_date_created group by tipp_pkg_fk, sp_id, pc_msg_token"
            List titlePendingCount = sql.rows(pendingQuery1, [sp: sql.connection.createArrayOf('bigint', subPromptPkgs.collect{ List sp -> sp[0] } as Object[]), accepted: sql.connection.createArrayOf('bigint', [RDStore.PENDING_CHANGE_ACCEPTED.id, RDStore.PENDING_CHANGE_REJECTED.id] as Object[]), newTitle: PendingChangeConfiguration.NEW_TITLE])
            //log.debug(titlePendingCount.toListString())
            String pendingQuery2 = "select count(pc.pc_id) as count, tipp_pkg_fk as pkg, sp_id, pc.pc_msg_token from pending_change as pc join title_instance_package_platform on pc.pc_tipp_fk = tipp_id join subscription_package on tipp_pkg_fk = sp_pkg_fk where sp_id = any(:sp) and pc.pc_oid is null and not exists(select done.pc_id from pending_change as done where pc.pc_tipp_fk = done.pc_tipp_fk and done.pc_status_rdv_fk = any(:accepted) and (regexp_split_to_array(done.pc_oid, '${Subscription.class.name}:'))[2]::bigint = sp_sub_fk) and pc.pc_msg_token = any(:msgTokens) and exists(select ie_id from issue_entitlement where ie_tipp_fk = pc_tipp_fk and ie_subscription_fk = sp_sub_fk and ie_status_rv_fk != :removed) and pc.pc_ts > sp_date_created group by tipp_pkg_fk, sp_id, pc_msg_token"
            titlePendingCount.addAll(sql.rows(pendingQuery2, [sp: sql.connection.createArrayOf('bigint', subPromptPkgs.collect{ List sp -> sp[0] } as Object[]), accepted: sql.connection.createArrayOf('bigint', [RDStore.PENDING_CHANGE_ACCEPTED.id, RDStore.PENDING_CHANGE_REJECTED.id] as Object[]), msgTokens: sql.connection.createArrayOf('varchar', [PendingChangeConfiguration.TITLE_DELETED, PendingChangeConfiguration.TITLE_UPDATED] as Object[]), removed: RDStore.TIPP_STATUS_REMOVED.id]))
            //log.debug(titlePendingCount.toListString())
            String pendingQuery3 = "select count(pc.pc_id) as count, tipp_pkg_fk as pkg, sp_id, pc.pc_msg_token from pending_change as pc join tippcoverage on pc.pc_tc_fk = tc_id join title_instance_package_platform on tc_tipp_fk = tipp_id join subscription_package on tipp_pkg_fk = sp_pkg_fk where sp_id = any(:sp) and pc.pc_oid is null and not exists(select done.pc_id from pending_change as done where pc.pc_tc_fk = done.pc_tc_fk and done.pc_status_rdv_fk = any(:accepted) and regexp_split_to_array(done.pc_oid, ':') @> '{${Subscription.class.name}}' and done.pc_oid = concat('${Subscription.class.name}:',sp_sub_fk)) and pc.pc_msg_token is not null and exists(select ie_id from issue_entitlement where ie_tipp_fk = tc_tipp_fk and ie_subscription_fk = sp_sub_fk and ie_status_rv_fk != :removed) and pc.pc_ts > sp_date_created group by tipp_pkg_fk, sp_id, pc.pc_msg_token"
            //log.debug("select count(pc.id) as count, tipp_pkg_fk as pkg, sp_id, pc.pc_msg_token from pending_change as pc join tippcoverage on pc.pc_tc_fk = tc_id join title_instance_package_platform on tc_tipp_fk = tipp_id join subscription_package on tipp_pkg_fk = sp_pkg_fk where sp_id = any('{${subPromptPkgs.collect{ List sp -> sp[0] }}}') and pc.pc_oid is null and not exists(select done.pc_id from pending_change as done where pc.pc_tc_fk = done.pc_tc_fk and (regexp_split_to_array(done.pc_oid, '${Subscription.class.name}:'))[2]::bigint = sp_sub_fk) and pc.pc_msg_token is not null and pc.pc_ts > sp_date_created group by tipp_pkg_fk, sp_id, pc.pc_msg_token")
            List coveragePendingCount = sql.rows(pendingQuery3, [sp: sql.connection.createArrayOf('bigint', subPromptPkgs.collect{ List sp -> sp[0] } as Object[]), accepted: sql.connection.createArrayOf('bigint', [RDStore.PENDING_CHANGE_ACCEPTED.id, RDStore.PENDING_CHANGE_REJECTED.id] as Object[]), removed: RDStore.TIPP_STATUS_REMOVED.id])
            String consortialFilter = ""
            if(configMap.consortialView)
                consortialFilter = "and sub_parent_sub_fk is null"
            //log.debug("select count(id) as count, tipp_pkg_fk as pkg, sp_id, pc_msg_token from pending_change join title_instance_package_platform on pc_tipp_fk = tipp_id join subscription_package on sp_pkg_fk = tipp_pkg_fk join org_role on or_sub_fk = sp_sub_fk join subscription on sp_sub_fk = sub_id where or_org_fk = :context and or_roletype_fk = any(:roleTypes) and pc_msg_token = :removed and exists(select ie_id from issue_entitlement where ie_tipp_fk = pc_tipp_fk and ie_subscription_fk = sp_sub_fk and ie_status_rv_fk != :ieRemoved) ${consortialFilter} group by pc_msg_token, tipp_pkg_fk, sp_id")
            //log.debug([context: configMap.contextOrg.id, roleTypes: sql.connection.createArrayOf('bigint', roleTypes.values() as Object[]), removed: PendingChangeConfiguration.TITLE_REMOVED, ieRemoved: RDStore.TIPP_STATUS_REMOVED.id].toMapString())
            String removedTitlesQuery = "select count(pc_id) as count, tipp_pkg_fk as pkg, sp_id, pc_msg_token from pending_change join title_instance_package_platform on pc_tipp_fk = tipp_id join subscription_package on sp_pkg_fk = tipp_pkg_fk join org_role on or_sub_fk = sp_sub_fk join subscription on sp_sub_fk = sub_id where or_org_fk = :context and or_roletype_fk = any(:roleTypes) and pc_msg_token = :removed and exists(select ie_id from issue_entitlement where ie_tipp_fk = pc_tipp_fk and ie_subscription_fk = sp_sub_fk and ie_status_rv_fk != :ieRemoved) ${consortialFilter} group by pc_msg_token, tipp_pkg_fk, sp_id"
            List removedTitlesCount = sql.rows(removedTitlesQuery, [context: configMap.contextOrg.id, roleTypes: sql.connection.createArrayOf('bigint', roleTypes.values() as Object[]), removed: PendingChangeConfiguration.TITLE_REMOVED, ieRemoved: RDStore.TIPP_STATUS_REMOVED.id])
            //log.debug(coveragePendingCount.toListString())

            /*
                aim:
                Map<String,Object> eventRow = [packageSubscription:[id: sp.subscription.id, name: sp.subscription.dropdownNamingConvention()],eventString:messageSource.getMessage(token,args,locale),msgToken:token]
                                notifications << eventRow
             */
            notifications.addAll(getEventRows(pkgCount, locale, status, roleTypes, sql, subNotifyPkgs))
            notifications.addAll(getEventRows(titleCount, locale, status, roleTypes, sql, subNotifyPkgs))
            notifications.addAll(getEventRows(coverageCount, locale, status, roleTypes, sql, subNotifyPkgs))
            pending.addAll(getEventRows(titlePendingCount, locale, status, roleTypes, sql, subPromptPkgs))
            pending.addAll(getEventRows(coveragePendingCount, locale, status, roleTypes, sql, subPromptPkgs))
            pending.addAll(getEventRows(removedTitlesCount, locale, status, roleTypes, sql, new HashSet()))
            sql.rows("select pc_id, pc_ci_fk, pc_msg_token, pc_old_value, pc_new_value, pc_sub_fk, pc_status_rdv_fk, pc_ts from pending_change right join cost_item on pc_ci_fk = ci_id where pc_owner = :contextOrg and pc_status_rdv_fk = any(:status) and (pc_msg_token = :newSubscription or (pc_ci_fk is not null and ci_status_rv_fk != :deleted))", [contextOrg: configMap.contextOrg.id, status: sql.connection.createArrayOf('bigint', acceptedStatus.keySet() as Object[]), newSubscription: 'pendingChange.message_SU_NEW_01', deleted: RDStore.COST_ITEM_DELETED.id]).each { GroovyRowResult pc ->
                Map<String,Object> eventRow = [event:pc.get("pc_msg_token")]
                boolean hasObject = true
                if(pc.get("pc_ci_fk")) {
                    Map<String, Long> subscrRoleTypes = roleTypes
                    subscrRoleTypes.remove('consortia')
                    List subRows = sql.rows("select ci_sub_fk, sub_name, sub_start_date, sub_end_date, sub_status_rv_fk, org_sortname, or_roletype_fk, sub_parent_sub_fk from cost_item join subscription on ci_sub_fk = sub_id join org_role on sub_id = or_sub_fk join org on or_org_fk = org_id where ci_id = :ciId and or_roletype_fk = any(:subscriberTypes)", [ciId: pc.get("pc_ci_fk"), subscriberTypes: sql.connection.createArrayOf('bigint', subscrRoleTypes.values() as Object[])])
                    if(subRows) {
                        GroovyRowResult entry = subRows[0]
                        eventRow.costItem = pc.get("pc_ci_fk")
                        eventRow.costItemSubscription = [id: entry.get("ci_sub_fk"), name: subscriptionName(entry, status, locale)]
                        Object[] args = [pc.get("pc_old_value"),pc.get("pc_new_value")]
                        eventRow.eventString = messageSource.getMessage(pc.get("pc_msg_token"),args,locale)
                        eventRow.changeId = pc.get("pc_id")
                    }
                    else hasObject = false
                }
                else {
                    List prevSub = sql.rows("select l_dest_sub_fk, sub_name, sub_start_date, sub_end_date, sub_status_rv_fk, org_sortname, or_roletype_fk, sub_parent_sub_fk from links join subscription on l_dest_sub_fk = sub_id join org_role on sub_id = or_sub_fk join org on or_org_fk = org_id where l_source_sub_fk = :newSub and l_link_type_rv_fk = :follows", [newSub: pc.get("pc_sub_fk"), follows: RDStore.LINKTYPE_FOLLOWS.id])
                    prevSub.each { GroovyRowResult previous ->
                        eventRow.eventString = messageSource.getMessage("${pc.get("pc_msg_token")}.eventString", null, locale)
                        eventRow.changeId = pc.get("pc_id")
                        eventRow.subscription = [source: Subscription.class.name + ':' + previous.get("l_dest_sub_fk"), target: Subscription.class.name + ':' + pc.get("pc_sub_fk"), id: pc.get("pc_sub_fk"), name: subscriptionName(previous, status, locale)]
                    }
                }
                if(hasObject) {
                    if(pc.get("pc_status_rdv_fk") == RDStore.PENDING_CHANGE_PENDING.id)
                        pending << eventRow
                    else if(pc.get("pc_status_rdv_fk") in [RDStore.PENDING_CHANGE_ACCEPTED.id, RDStore.PENDING_CHANGE_REJECTED.id]) {
                        if(pc.get("pc_ts") >= time)
                            notifications << eventRow
                    }
                }
            }
        }
        result.notifications = notifications.drop(configMap.acceptedOffset).take(configMap.max)
        result.notificationsCount = notifications.size()
        result.pending = pending.drop(configMap.pendingOffset).take(configMap.max)
        result.pendingCount = pending.size()
        result.acceptedOffset = configMap.acceptedOffset
        result.pendingOffset = configMap.pendingOffset
        result
    }

    /**
     * Helper method to collect the events in rows containing also the subscription data
     * @param entries the changes to display
     * @param locale the locale to use for the message constants
     * @param status the possible subscription status
     * @param roleTypes the possible subscriber role types
     * @param sql the SQL connection
     * @param subPkgConfigs the map of subscription package pending change configurations
     * @return a list of maps containing the subscription data, the event string and the change token
     */
    List<Map<String, Object>> getEventRows(List<GroovyRowResult> entries, Locale locale, Map status, Map roleTypes, Sql sql, Set subPkgConfigs) {
        List result = []
        entries.each { GroovyRowResult row ->
            List spData = subPkgConfigs.find { List spPkgCfg -> spPkgCfg[1] == row.get("pkg") && spPkgCfg[0] == row.get("sp_id") }
            List subRows
            //subPkgConfigs is set for configurable events
            if(subPkgConfigs)
                subRows = sql.rows("select sub_id, sub_name, sub_start_date, sub_end_date, sub_status_rv_fk, org_sortname, or_roletype_fk, sub_parent_sub_fk from subscription_package join subscription on sp_sub_fk = sub_id join org_role on sub_id = or_sub_fk join org on or_org_fk = org_id where sp_id = :spId and or_roletype_fk = any(:subscriberTypes)", [spId: spData[0], subscriberTypes: sql.connection.createArrayOf('bigint', roleTypes.values() as Object[])])
            else subRows = sql.rows("select sub_id, sub_name, sub_start_date, sub_end_date, sub_status_rv_fk, org_sortname, or_roletype_fk, sub_parent_sub_fk from subscription_package join subscription on sp_sub_fk = sub_id join org_role on sub_id = or_sub_fk join org on or_org_fk = org_id where sp_id = :spId and or_roletype_fk = any(:subscriberTypes)", [spId: row.get('sp_id'), subscriberTypes: sql.connection.createArrayOf('bigint', roleTypes.values() as Object[])])
            if(subRows) {
                GroovyRowResult entry = subRows[0]
                result << [packageSubscription: [id: entry.get('sub_id'), name: subscriptionName(subRows[0], status, locale)], eventString: messageSource.getMessage(row.get('pc_msg_token'), [row.get('count')] as Object[], locale), msgToken: row.get('pc_msg_token')]
            }
        }
        result
    }

    /**
     * Helper to display the subscription name according to the dropdown naming convention
     * @param entry the subscription data
     * @param status the possible subscription status
     * @param locale the locale to use for message constants
     * @return the subscription name conform to {@link Subscription#dropdownNamingConvention(de.laser.Org)}
     */
    String subscriptionName(GroovyRowResult entry, Map<Long, String> status, Locale locale) {
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        //log.debug(subRows.toListString())
        String startDate = "", endDate = "", additionalInfo = ""
        if(entry.get('sub_start_date'))
            startDate = sdf.format(entry.get('sub_start_date'))
        if(entry.get('sub_end_date'))
            endDate = sdf.format(entry.get('sub_end_date'))
        if(entry.get('sub_parent_sub_fk')) {
            additionalInfo = " - ${messageSource.getMessage('gasco.filter.consortialLicence', null, locale)}"
        }
        "${entry.get('sub_name')} - ${status.get(entry.get('sub_status_rv_fk'))} (${startDate} - ${endDate})${additionalInfo}"
    }

    /**
     * Kept for reasons of reference
     */
    @Deprecated
    Map<String,Object> getChanges_old(LinkedHashMap<String, Object> configMap) {
        SessionCacheWrapper scw = new SessionCacheWrapper()
        String ctx = 'dashboard/changes'
        Map<String, Object> changesCache = scw.get(ctx) as Map<String, Object>
        if(!changesCache) {
            Locale locale = LocaleUtils.getCurrentLocale()
            Date time = new Date(System.currentTimeMillis() - Duration.ofDays(configMap.periodInDays).toMillis())
            //package changes
            String subscribedPackagesQuery = 'select new map(sp as subPackage, pcc as config) from PendingChangeConfiguration pcc join pcc.subscriptionPackage sp join sp.subscription sub join sub.orgRelations oo where oo.org = :context and oo.roleType in (:roleTypes) and ((pcc.settingValue = :prompt or pcc.withNotification = true))'
            Map<String,Object> spQueryParams = [context:configMap.contextOrg,roleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER],prompt:RDStore.PENDING_CHANGE_CONFIG_PROMPT]
            if(configMap.consortialView) {
                subscribedPackagesQuery += ' and sub.instanceOf = null'
            }
            List tokensWithNotifications = SubscriptionPackage.executeQuery(subscribedPackagesQuery,spQueryParams)
            if(!configMap.consortialView){
                spQueryParams.remove("roleTypes")
                spQueryParams.remove("prompt")
                spQueryParams.subscrRole = RDStore.OR_SUBSCRIBER_CONS
                Set parentSettings = SubscriptionPackage.executeQuery("select new map(ac.referenceId as parentId, ac.referenceField as settingKey) from AuditConfig ac where ac.referenceClass = '"+Subscription.class.name+"' and ac.referenceId in (select sub.instanceOf.id from SubscriptionPackage sp join sp.subscription sub join sub.orgRelations oo where oo.org = :context and oo.roleType = :subscrRole) and ac.referenceField in (:settingKeys)",[context: configMap.contextOrg, subscrRole: RDStore.OR_SUBSCRIBER_CONS, settingKeys: PendingChangeConfiguration.SETTING_KEYS.collect { String key -> key+PendingChangeConfiguration.NOTIFICATION_SUFFIX }])
                spQueryParams.parentIDs = parentSettings.collect { row -> row.parentId }
                if(spQueryParams.parentIDs) {
                    Set subscriptionPackagesWithInheritedNotification = SubscriptionPackage.executeQuery("select sp, sp.subscription.instanceOf.id from SubscriptionPackage sp join sp.subscription sub join sub.orgRelations oo where oo.org = :context and oo.roleType = :subscrRole and sub.instanceOf.id in (:parentIDs)",spQueryParams)
                    parentSettings.each { Map row ->
                        SubscriptionPackage subPackage = subscriptionPackagesWithInheritedNotification.find { subRow -> subRow[1] == row.parentId } ? (SubscriptionPackage) subscriptionPackagesWithInheritedNotification.find { subRow -> subRow[1] == row.parentId }[0] : null
                        if(subPackage) {
                            PendingChangeConfiguration config = PendingChangeConfiguration.executeQuery('select pcc from PendingChangeConfiguration pcc join pcc.subscriptionPackage sp join sp.subscription s where s.id = :parent and pcc.settingKey = :key',[parent: row.parentId, key: row.settingKey.replace(PendingChangeConfiguration.NOTIFICATION_SUFFIX,'')])[0]
                            if(config)
                                tokensWithNotifications.add([subPackage: subPackage, config: config])
                        }
                    }
                }
            }
            Set<Long> subscribedPackages = []
            Map<SubscriptionPackage,Map<String, String>> packageSettingMap = [:]
            Set<String> withNotification = [], prompt = []
            tokensWithNotifications.each { row ->
                subscribedPackages << row.subPackage.pkg.id
                PendingChangeConfiguration pcc = (PendingChangeConfiguration) row.config
                Map<String, String> setting = packageSettingMap.get(row.subPackage)
                if(!setting)
                    setting = [:]
                if(pcc.settingValue == RDStore.PENDING_CHANGE_CONFIG_PROMPT) {
                    prompt << pcc.settingKey
                    setting[pcc.settingKey] = "prompt"
                }
                else if(pcc.settingValue == RDStore.PENDING_CHANGE_CONFIG_ACCEPT && pcc.withNotification) {
                    withNotification << pcc.settingKey
                    setting[pcc.settingKey] = "notify"
                }
                if(setting) {
                    packageSettingMap.put(row.subPackage, setting)
                }
            }
            List query1Clauses = [], query2Clauses = []
            String query1 = "select pc from PendingChange pc where pc.owner = :contextOrg and pc.status in (:status) and (pc.msgToken = :newSubscription or pc.costItem != null)",
                   query2 = 'select pc.msgToken,pkg.id,count(distinct pkg.id),\'pkg\',\'pkg.id\' from PendingChange pc join pc.pkg pkg where pkg.id in (:packages) and pc.oid is not null',
                   query3 = 'select pc.msgToken,pkg.id,count(distinct tipp.id),\'tipp.pkg\',\'tipp.id\'  from PendingChange pc join pc.tipp tipp join tipp.pkg pkg where pkg.id in (:packages) and pc.oid is not null',
                   query4 = 'select pc.msgToken,pkg.id,count(distinct tc.id),\'tippCoverage.tipp.pkg\',\'tippCoverage.id\'  from PendingChange pc join pc.tippCoverage tc join tc.tipp tipp join tipp.pkg pkg where pkg.id in (:packages) and pc.oid is not null'
            Map<String,Object> query1Params = [contextOrg:configMap.contextOrg, status:[RDStore.PENDING_CHANGE_PENDING,RDStore.PENDING_CHANGE_ACCEPTED], newSubscription: "pendingChange.message_SU_NEW_01"],
                               query2Params = [packages:subscribedPackages]
            if(configMap.periodInDays) {
                query1Clauses << "pc.ts >= :time"
                query1Params.time = time
                if(withNotification && prompt)
                {
                    query2Clauses << "(pc.ts >= :time and pc.msgToken in (:withNotification))"
                    query2Params.time = time
                    query2Params.withNotification = withNotification
                }
                else if (withNotification && !prompt){
                    query2Clauses << "(pc.ts >= :time and pc.msgToken in (:withNotification))"
                    query2Params.time = time
                    query2Params.withNotification = withNotification
                }

            }
            if(query1Clauses) {
                query1 += ' and ' + query1Clauses.join(" and ")
            }
            if(query2Clauses) {
                query2 += ' and ' + query2Clauses.join(" and ")
                query3 += ' and ' + query2Clauses.join(" and ")
                query4 += ' and ' + query2Clauses.join(" and ")
                //query5 += ' and ' + query2Clauses.join(" and ")
            }
            List<PendingChange> nonPackageChanges = PendingChange.executeQuery(query1,query1Params) //PendingChanges need to be refilled in maps
            List tokensNotifications = [], pending = [], notifications = []
            if (subscribedPackages) {
                tokensNotifications.addAll(PendingChange.executeQuery(query2 + ' group by pc.msgToken, pkg.id', query2Params))
                tokensNotifications.addAll(PendingChange.executeQuery(query3 + ' group by pc.msgToken, pkg.id', query2Params))
                tokensNotifications.addAll(PendingChange.executeQuery(query4 + ' group by pc.msgToken, pkg.id', query2Params))
                /*
                   I need to summarize here:
                   - the subscription package (I need both)
                   - for package changes: the old and new value (there, I can just add the pc row as is)
                   - for title and coverage changes: I just need to record that *something* happened and then, on the details page (method subscriptionControllerService.entitlementChanges()), to enumerate the actual changes
                */
                Long start = System.currentTimeMillis()
                packageSettingMap.each { SubscriptionPackage sp, Map<String, String> setting ->
                    //List<PendingChange> pendingChange1 = PendingChange.executeQuery('select pc.id from PendingChange pc where pc.'+row[3]+' = :package and pc.oid = :oid and pc.status != :accepted',[package:sp.pkg,oid:genericOIDService.getOID(sp.subscription),accepted:RDStore.PENDING_CHANGE_ACCEPTED])
                    setting.each { String token, String settingValue ->
                        if(settingValue == "notify") {
                            def row = tokensNotifications.find { row -> row[1] == sp.pkg.id && row[0] == token }
                            if(row) {
                                Object[] args = [row[2]]
                                Map<String,Object> eventRow = [packageSubscription:[id: sp.subscription.id, name: sp.subscription.dropdownNamingConvention()],eventString:messageSource.getMessage(token,args,locale),msgToken:token]
                                notifications << eventRow
                            }
                        }
                        else if(settingValue == "prompt") {
                            Map<String, String> entity = [entity: 'tipp.id', entityPackage: 'tipp.pkg']
                            if(entity) {
                                List newerCount
                                if(token == PendingChangeConfiguration.NEW_TITLE)
                                    newerCount = PendingChange.executeQuery('select count(distinct pc.id) from PendingChange pc where pc.'+entity.entityPackage+' = :package and pc.oid = null and pc.ts >= :entryDate and pc.msgToken = :token and not exists (select pca.id from PendingChange pca where pca.'+entity.entity+' = pc.'+entity.entity+' and pca.oid = :oid and pca.status in (:acceptedStatus))',[package: sp.pkg, entryDate: sp.dateCreated, token: token, oid: genericOIDService.getOID(sp.subscription), acceptedStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED]])
                                else
                                    newerCount = PendingChange.executeQuery('select count(distinct pc.id) from PendingChange pc where pc.'+entity.entityPackage+' = :package and pc.oid = null and pc.ts >= :entryDate and pc.msgToken = :token and not exists (select pca.id from PendingChange pca where pca.'+entity.entity+' = pc.'+entity.entity+' and pca.oid = :oid and pca.status in (:acceptedStatus) and pca.newValue = pc.newValue)',[package: sp.pkg, entryDate: sp.dateCreated, token: token, oid: genericOIDService.getOID(sp.subscription), acceptedStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED]])
                                log.debug("processing at ${System.currentTimeMillis()-start} msecs")
                                //List processedCount = PendingChange.executeQuery('',[package:sp.pkg, entity: entity])
                                if(newerCount && newerCount[0] > 0){
                                    Object[] args = [newerCount[0]]
                                    Map<String,Object> eventRow = [packageSubscription:[id: sp.subscription.id, name: sp.subscription.dropdownNamingConvention()],eventString:messageSource.getMessage(token,args,locale),msgToken:token]
                                    //eventRow.changeId = pendingChange1 ? pendingChange1[0] : null
                                    pending << eventRow
                                }
                            }
                        }
                    }
                }
            }
            nonPackageChanges.each { PendingChange pc ->
                Map<String,Object> eventRow = [event:pc.msgToken]
                if(pc.costItem) {
                    eventRow.costItem = pc.costItem
                    eventRow.costItemSubscription = [id: pc.costItem.sub.id, name: pc.costItem.sub.dropdownNamingConvention()]
                    Object[] args = [pc.oldValue,pc.newValue]
                    eventRow.eventString = messageSource.getMessage(pc.msgToken,args,locale)
                    eventRow.changeId = pc.id
                }
                else {
                    eventRow.eventString = messageSource.getMessage("${pc.msgToken}.eventString", null, locale)
                    eventRow.changeId = pc.id
                    eventRow.subscription = [sources: pc.subscription._getCalculatedPrevious(), target: genericOIDService.getOID(pc.subscription)]
                }
                if(pc.status == RDStore.PENDING_CHANGE_PENDING)
                    pending << eventRow
                else if(pc.status == RDStore.PENDING_CHANGE_ACCEPTED || pc.status == RDStore.PENDING_CHANGE_REJECTED) {
                    notifications << eventRow
                }
            }
            changesCache = [notifications: notifications, notificationsCount: notifications.size(),
                            pending: pending, pendingCount: pending.size()]
            scw.put(ctx, changesCache)
        }
        //[changes:result,changesCount:result.size(),subscribedPackages:subscribedPackages]
        [notifications: changesCache.notifications.drop(configMap.acceptedOffset).take(configMap.max), notificationsCount: changesCache.notificationsCount,
         pending: changesCache.pending.drop(configMap.pendingOffset).take(configMap.max), pendingCount: changesCache.pendingCount, acceptedOffset: configMap.acceptedOffset, pendingOffset: configMap.pendingOffset]
    }

    @Deprecated
    Map<String,Object> printRow(PendingChange change) {
        Locale locale = LocaleUtils.getCurrentLocale()
        String eventIcon, instanceIcon, eventString
        List<Object> eventData
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        if(change.subscription && change.msgToken == "pendingChange.message_SU_NEW_01") {
            eventIcon = '<span data-tooltip="' + messageSource.getMessage("${change.msgToken}", null, locale) + '"><i class="yellow circle icon"></i></span>'
            instanceIcon = '<span data-tooltip="' + messageSource.getMessage('subscription', null, locale) + '"><i class="clipboard icon"></i></span>'
            eventString = messageSource.getMessage('pendingChange.message_SU_NEW_01.eventString', null, locale)
        }
        else if(change.costItem) {
            eventIcon = '<span data-tooltip="'+messageSource.getMessage('default.change.label',null,locale)+'"><i class="yellow circle icon"></i></span>'
            instanceIcon = '<span data-tooltip="'+messageSource.getMessage('financials.costItem',null,locale)+'"><i class="money bill icon"></i></span>'
            eventData = [change.oldValue,change.newValue]
        }
        else {
            switch(change.msgToken) {
                case [ PendingChangeConfiguration.PACKAGE_PROP, PendingChangeConfiguration.PACKAGE_TIPP_COUNT_CHANGED ]:
                    eventIcon = '<span data-tooltip="'+messageSource.getMessage('default.change.label',null,locale)+'"><i class="yellow circle icon"></i></span>'
                    instanceIcon = '<span data-tooltip="'+messageSource.getMessage('package',null,locale)+'"><i class="gift icon"></i></span>'
                    eventData = [change.targetProperty,change.oldValue,change.newValue]
                    break
                case PendingChangeConfiguration.PACKAGE_DELETED:
                    eventIcon = '<span data-tooltip="'+messageSource.getMessage('subscription.packages.'+change.msgToken)+'"><i class="red minus icon"></i></span>'
                    instanceIcon = '<span data-tooltip="'+messageSource.getMessage('package',null,locale)+'"><i class="gift icon"></i></span>'
                    eventData = [change.pkg.name]
                    break
            }
        }
        if(eventString == null && eventData)
            eventString = messageSource.getMessage(change.msgToken,eventData.toArray(),locale)
        [instanceIcon:instanceIcon,eventIcon:eventIcon,eventString:eventString]
    }

    /**
     * Converts the given value according to the field type
     * @param change the change whose property should be output
     * @param key the string value
     * @return the value as {@link Date} or {@link String}
     */
    def output(PendingChange change,String key) {
        Locale locale = LocaleUtils.getCurrentLocale()
        def ret
        if(change.targetProperty in PendingChange.DATE_FIELDS) {
            Date date = DateUtils.parseDateGeneric(change[key])
            if(date)
                ret = DateUtils.getLocalizedSDF_noTime().format(date)
            else ret = null
        }
        else if(change.targetProperty in PendingChange.REFDATA_FIELDS) {
            ret = RefdataValue.get(change[key])
        }
        else ret = change[key]
        ret
    }

    /**
     * Retrieves the counts of changes for each of the packages in the given list
     * @param pkgList the list of packages (as {@link SubscriptionPackage} link objects) whose counts should be retrieved
     * @return a {@link Map} of counts, grouped by events and application status (pending or accepted)
     */
    Map<String, Integer> getCountsForPackages(Map<SubscriptionPackage, Set<String>> pkgList) {
        Integer newTitlesPending = 0, titlesUpdatedPending = 0, titlesDeletedPending = 0, titlesRemovedPending = 0
        Integer newTitlesAccepted = 0, titlesUpdatedAccepted = 0, titlesDeletedAccepted = 0
        List<RefdataValue> pendingStatus = [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED]
        pkgList.each { SubscriptionPackage sp, Set<String> settings ->
            //spontaneous convention: 1: title, a: accepted, b: pending
            List acceptedTitleCounts = PendingChange.executeQuery('select count(pc.id), pc.msgToken from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken in (:eventTypes) and pc.oid = :subOid and pc.status in (:pendingStatus) group by pc.msgToken', [package: sp.pkg, entryDate: sp.dateCreated, subOid: genericOIDService.getOID(sp.subscription), eventTypes: [PendingChangeConfiguration.NEW_TITLE, PendingChangeConfiguration.TITLE_UPDATED, PendingChangeConfiguration.TITLE_DELETED], pendingStatus: pendingStatus])
            //String query1b
            List pendingTitleCounts = [], pendingCoverageCounts = []
            if(PendingChangeConfiguration.NEW_TITLE in settings) {
                //if(params.eventType == PendingChangeConfiguration.NEW_TITLE)
                pendingTitleCounts.addAll(PendingChange.executeQuery('select count(pc.id), pc.msgToken from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.tipp.status != :removed and (not exists (select pca.id from PendingChange pca join pca.tipp tippA where tippA = pc.tipp and pca.oid = :subOid and pca.status in (:pendingStatus)) and not exists (select ie.id from IssueEntitlement ie where ie.tipp = pc.tipp and ie.status != :removed and ie.subscription = :subscription)) and pc.status = :packageHistory group by pc.msgToken, pc.tipp', [package: sp.pkg, entryDate: sp.dateCreated, eventType: PendingChangeConfiguration.NEW_TITLE, subOid: genericOIDService.getOID(sp.subscription), pendingStatus: pendingStatus, removed: RDStore.TIPP_STATUS_REMOVED, subscription: sp.subscription, packageHistory: RDStore.PENDING_CHANGE_HISTORY]))
            }
            if(PendingChangeConfiguration.TITLE_UPDATED in settings) {
                //else if(params.eventType == PendingChangeConfiguration.TITLE_UPDATED)
                pendingTitleCounts.addAll(PendingChange.executeQuery('select count(pc.id), pc.msgToken from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.tipp.status != :removed and not exists (select pca.id from PendingChange pca join pca.tipp tippA where tippA = pc.tipp and pca.oid = :subOid and (pca.newValue = pc.newValue or (pca.newValue is null and pc.newValue is null)) and pca.status in (:pendingStatus)) and pc.status = :packageHistory group by pc.msgToken', [package: sp.pkg, entryDate: sp.dateCreated, eventType: PendingChangeConfiguration.TITLE_UPDATED, subOid: genericOIDService.getOID(sp.subscription), pendingStatus: pendingStatus, removed: RDStore.TIPP_STATUS_REMOVED, packageHistory: RDStore.PENDING_CHANGE_HISTORY]))
            }
            if(PendingChangeConfiguration.TITLE_DELETED in settings) {
                //else if(params.eventType == PendingChangeConfiguration.TITLE_DELETED)
                pendingTitleCounts.addAll(PendingChange.executeQuery('select count(pc.id), pc.msgToken from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.tipp.status != :removed and not exists (select pca.id from PendingChange pca join pca.tipp tippA where tippA = pc.tipp and pca.oid = :subOid and pca.status in (:pendingStatus)) and exists(select ie.id from IssueEntitlement ie where ie.tipp = pc.tipp and ie.status not in (:deleted) and ie.subscription = :subscription) and pc.status = :packageHistory group by pc.msgToken', [package: sp.pkg, entryDate: sp.dateCreated, eventType: PendingChangeConfiguration.TITLE_DELETED, subOid: genericOIDService.getOID(sp.subscription), pendingStatus: pendingStatus, removed: RDStore.TIPP_STATUS_REMOVED, deleted: [RDStore.TIPP_STATUS_REMOVED, RDStore.TIPP_STATUS_DELETED], subscription: sp.subscription, packageHistory: RDStore.PENDING_CHANGE_HISTORY]))
            }
            if(PendingChangeConfiguration.TITLE_REMOVED in settings) {
                //else if(params.eventType == PendingChangeConfiguration.TITLE_REMOVED)
                pendingTitleCounts.addAll(PendingChange.executeQuery('select count(pc.id), pc.msgToken from PendingChange pc join pc.tipp tipp where tipp in (select ie.tipp from IssueEntitlement ie where ie.subscription = :sub and ie.status != :removed) and tipp.pkg = :pkg and pc.msgToken = :eventType group by pc.msgToken', [sub: sp.subscription, pkg: sp.pkg, removed: RDStore.TIPP_STATUS_REMOVED, eventType: PendingChangeConfiguration.TITLE_REMOVED]))
            }
            //pendingTitleCounts.addAll(sql.rows("select count(id) as count, pc_msg_token from pending_change join title_instance_package_platform on pc_tipp_fk = tipp_id join subscription_package on sp_pkg_fk = tipp_pkg_fk join org_role on or_sub_fk = sp_sub_fk join subscription on sp_sub_fk = sub_id where or_org_fk = :context and or_roletype_fk = any(:roleTypes) and pc_msg_token = :removed and exists(select ie_id from issue_entitlement where ie_tipp_fk = pc_tipp_fk and ie_subscription_fk = sp_sub_fk and ie_status_rv_fk != :ieRemoved) and sp_sub_fk = :subId group by pc_msg_token", [context: contextOrg.id, roleTypes: connection.createArrayOf('bigint', [RDStore.OR_SUBSCRIPTION_CONSORTIA.id, RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id] as Object[]), subId: sp.subscription.id, removed: PendingChangeConfiguration.TITLE_REMOVED, ieRemoved: RDStore.TIPP_STATUS_REMOVED.id]))
            acceptedTitleCounts.each { row ->
                switch(row[1]) {
                    case PendingChangeConfiguration.NEW_TITLE: newTitlesAccepted += row[0]
                        break
                    case PendingChangeConfiguration.TITLE_UPDATED: titlesUpdatedAccepted += row[0]
                        break
                    case PendingChangeConfiguration.TITLE_DELETED: titlesDeletedAccepted += row[0]
                        break
                }
            }
            pendingTitleCounts.each { row ->
                switch(row[1]) {
                    case PendingChangeConfiguration.NEW_TITLE: newTitlesPending += row[0]
                        break
                    case PendingChangeConfiguration.TITLE_UPDATED: titlesUpdatedPending += row[0]
                        break
                    case PendingChangeConfiguration.TITLE_DELETED: titlesDeletedPending += row[0]
                        break
                    case PendingChangeConfiguration.TITLE_REMOVED: titlesRemovedPending += row[0]
                        break
                }
            }
        }
        Integer pendingCount = newTitlesPending+titlesUpdatedPending+titlesDeletedPending+titlesRemovedPending
        Integer acceptedCount = newTitlesAccepted+titlesUpdatedAccepted+titlesDeletedAccepted
        [countPendingChanges: pendingCount,
         countAcceptedChanges: acceptedCount,
         newTitlesPending: newTitlesPending,
         titlesUpdatedPending: titlesUpdatedPending,
         titlesDeletedPending: titlesDeletedPending,
         titlesRemovedPending: titlesRemovedPending,
         newTitlesAccepted: newTitlesAccepted,
         titlesUpdatedAccepted: titlesUpdatedAccepted,
         titlesDeletedAccepted: titlesDeletedAccepted]
    }

    /**
     * Accepts the given change and applies the parameters
     * @param pc the change to accept
     * @param subId the subscription on which the change should be applied
     * @return true if the change could be applied successfully, false otherwise
     * @throws ChangeAcceptException
     */
    boolean accept(PendingChange pc, subId = null) throws ChangeAcceptException {
        log.debug("accept: ${pc.msgToken} for ${pc.pkg} or ${pc.tipp} or ${pc.tippCoverage}")
        def done = false
        def target
        if(pc.oid)
            target = genericOIDService.resolveOID(pc.oid)
        else if(pc.costItem)
            target = pc.costItem
        else if(subId)
            target = Subscription.get(subId)
        def parsedNewValue
        if(pc.targetProperty in PendingChange.DATE_FIELDS)
            parsedNewValue = DateUtils.parseDateGeneric(pc.newValue)
        else if(pc.targetProperty in PendingChange.REFDATA_FIELDS) {
            if(pc.newValue)
                parsedNewValue = RefdataValue.get(Long.parseLong(pc.newValue))
            else reject(pc) //i.e. do nothing, wrong value
        }else if(pc.targetProperty in PendingChange.PRICE_FIELDS) {
            parsedNewValue = escapeService.parseFinancialValue(pc.newValue)
        }
        else parsedNewValue = pc.newValue
        switch(pc.msgToken) {
        //pendingChange.message_TP01 (newTitle)
            case PendingChangeConfiguration.NEW_TITLE:
                if(target instanceof TitleInstancePackagePlatform) {
                    TitleInstancePackagePlatform tipp = (TitleInstancePackagePlatform) target
                    IssueEntitlement newTitle = IssueEntitlement.construct([subscription:pc.subscription,tipp:tipp,acceptStatus:RDStore.IE_ACCEPT_STATUS_FIXED,status:tipp.status])
                    if(newTitle) {
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when creating new entitlement - pending change not accepted: ${newTitle.errors}")
                }
                else if(target instanceof Subscription) {
                    IssueEntitlement newTitle = IssueEntitlement.construct([subscription:target,tipp:pc.tipp,acceptStatus:RDStore.IE_ACCEPT_STATUS_FIXED,status:pc.tipp.status])
                    if(newTitle) {
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when creating new entitlement - pending change not accepted: ${newTitle.errors}")
                }
                //else throw new ChangeAcceptException("no instance of TitleInstancePackagePlatform stored: ${pc.oid}! Pending change is void!")
                break
        //pendingChange.message_TP02 (titleUpdated)
            case PendingChangeConfiguration.TITLE_UPDATED:
                log.debug("update title")
                IssueEntitlement targetTitle
                if(target instanceof IssueEntitlement) {
                    targetTitle = (IssueEntitlement) target
                }
                else if(target instanceof Subscription) {
                    Set<IssueEntitlement> titleCandidates = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.subscription = :target and ie.tipp = :tipp order by ie_last_updated desc',[target:target,tipp:pc.tipp])
                    targetTitle = titleCandidates.find { IssueEntitlement ie -> ie.status != RDStore.TIPP_STATUS_REMOVED }
                    if(!targetTitle)
                        targetTitle = titleCandidates[0]
                }
                if(targetTitle) {
                    //special case of deleted title restore ...
                    if(targetTitle.status == RDStore.TIPP_STATUS_REMOVED && pc.oldValue == RDStore.TIPP_STATUS_DELETED.id.toString() && pc.newValue != RDStore.TIPP_STATUS_REMOVED.id.toString()) {
                        log.debug("restore deleted ${targetTitle}")
                        IssueEntitlement restoredTitle = IssueEntitlement.construct([subscription:target,tipp:pc.tipp,acceptStatus:RDStore.IE_ACCEPT_STATUS_FIXED,status:pc.tipp.status])
                        if(restoredTitle) {
                            done = true
                        }
                        else throw new ChangeAcceptException("problems when restoring entitlement - pending change not accepted: ${targetTitle.errors}")
                    }
                    else {
                        log.debug("set ${targetTitle} ${pc.targetProperty} to ${parsedNewValue}")
                        targetTitle[pc.targetProperty] = parsedNewValue
                        if(targetTitle.save()) {
                            done = true
                        }
                        else throw new ChangeAcceptException("problems when updating entitlement - pending change not accepted: ${targetTitle.errors}")
                    }
                }
                else {
                    log.warn("no instance of IssueEntitlement stored: ${pc.oid}! Pending change is void!")
                    done = "reject"
                }
                //else throw new ChangeAcceptException("no instance of IssueEntitlement stored: ${pc.oid}! Pending change is void!")
                break
        //pendingChange.message_TP03 (titleDeleted)
            case PendingChangeConfiguration.TITLE_DELETED:
                IssueEntitlement targetTitle
                if(target instanceof IssueEntitlement) {
                    targetTitle = (IssueEntitlement) target
                }
                else if(target instanceof Subscription) {
                    targetTitle = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.subscription = :target and ie.tipp = :tipp and ie.status != :ieStatus',[target:target,tipp:pc.tipp,ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
                }
                if(targetTitle) {
                    if(pc.tipp.status == RDStore.TIPP_STATUS_DELETED) {
                        log.debug("deleting ${targetTitle} from holding ...")
                        targetTitle.status = RDStore.TIPP_STATUS_DELETED
                        if(targetTitle.save()) {
                            done = true
                        }
                        else throw new ChangeAcceptException("problems when deleting entitlement - pending change not accepted: ${targetTitle.errors}")
                    }
                    else {
                        log.debug("false deletion - pending change is superseded")
                        done = 'reject'
                    }
                }
                //else throw new ChangeAcceptException("no instance of IssueEntitlement stored: ${pc.oid}! Pending change is void!")
                break
        //pendingChange.message_TP04 (titleRemoved)
            case PendingChangeConfiguration.TITLE_REMOVED:
                log.debug("removing instances of ${pc.tipp} from holding ...")
                if(subId instanceof String)
                    subId = Long.parseLong(subId)
                IssueEntitlement.executeUpdate("update IssueEntitlement ie set ie.status = :removed where ie.tipp = :title and ie.subscription in (select s from Subscription s where :subscription in (s.id, s.instanceOf.id))", [removed: RDStore.TIPP_STATUS_REMOVED, title: pc.tipp, subscription: subId])
                //else throw new ChangeAcceptException("no instance of IssueEntitlement stored: ${pc.oid}! Pending change is void!")
                break
        //pendingChange.message_CI01 (billingSum)
            case PendingChangeConfiguration.BILLING_SUM_UPDATED:
                if(target instanceof CostItem) {
                    CostItem costItem = (CostItem) target
                    costItem.costInBillingCurrency = Double.parseDouble(pc.newValue)
                    if(costItem.save())
                        done = true
                    else throw new ChangeAcceptException("problems when updating billing sum - pending change not accepted: ${costItem.errors}")
                }
                break
        //pendingChange.message_CI02 (localSum)
            case PendingChangeConfiguration.LOCAL_SUM_UPDATED:
                if(target instanceof CostItem) {
                    CostItem costItem = (CostItem) target
                    costItem.costInLocalCurrency = Double.parseDouble(pc.newValue)
                    if(costItem.save())
                        done = true
                    else throw new ChangeAcceptException("problems when updating local sum - pending change not accepted: ${costItem.errors}")
                }
                break
        }
        if(done == 'reject') {
            //post reject for void changes
            pc.status = RDStore.PENDING_CHANGE_REJECTED
            pc.actionDate = new Date()
            if(!pc.save()) {
                throw new ChangeAcceptException("problems when submitting new pending change status: ${pc.errors}")
            }
        }
        else if(done || pc.msgToken == 'pendingChange.message_SU_NEW_01') {
            pc.status = RDStore.PENDING_CHANGE_ACCEPTED
            pc.actionDate = new Date()
            if(!pc.save()) {
                throw new ChangeAcceptException("problems when submitting new pending change status: ${pc.errors}")
            }
        }
        done
    }

    /**
     * Rejects the given change and sets the flag to prevent accidents
     * @param pc the change to reject
     * @param subId the subscription which would have been affected
     * @return true if the rejection was successful, false otherwise
     */
    boolean reject(PendingChange pc, subId = null) {
        if(pc.status != RDStore.PENDING_CHANGE_HISTORY) {
            pc.status = RDStore.PENDING_CHANGE_REJECTED
            pc.actionDate = new Date()
            if(!pc.save()) {
                throw new ChangeAcceptException("problems when submitting new pending change status: ${pc.errors}")
            }
        }
        else if(subId) {
            Org contextOrg = contextService.getOrg()
            def target
            Package targetPkg
            if(pc.tipp) {
                targetPkg = pc.tipp.pkg
                target = pc.tipp
            }
            else if(pc.tippCoverage) {
                targetPkg = pc.tippCoverage.tipp.pkg
                target = pc.tippCoverage
            }
            if(target) {
                List<SubscriptionPackage> subPkg = SubscriptionPackage.executeQuery('select sp from SubscriptionPackage sp join sp.subscription s join s.orgRelations oo where sp.pkg = :pkg and sp.subscription.id = :subscription and oo.org = :ctx and s.instanceOf is null',[ctx:contextOrg,pkg:targetPkg,subscription:Long.parseLong(subId)])
                if(subPkg.size() == 1) {
                    PendingChange toReject = PendingChange.construct([target: target, oid: genericOIDService.getOID(subPkg[0].subscription), newValue: pc.newValue, oldValue: pc.oldValue, prop: pc.targetProperty, msgToken: pc.msgToken, status: RDStore.PENDING_CHANGE_REJECTED, owner: contextOrg])
                    if (!toReject)
                        log.error("Error when auto-rejecting pending change ${toReject} with token ${toReject.msgToken}!")
                }
                else log.error("unable to determine subscription package for pending change ${pc}")
            }
            else log.error("Unable to determine target object! Ignoring change ${pc}!")
        }
        true
    }

    /**
     * Auto-applies the given change to the given subscription
     * @param newChange the change to apply
     * @param subPkg the subscription package on which the change should be applied
     * @param contextOrg the subscriber
     */
    void applyPendingChange(PendingChange newChange,SubscriptionPackage subPkg,Org contextOrg) {
        log.debug("applyPendingChange")
        def target
        if(newChange.tipp)
            target = newChange.tipp
        else if(newChange.tippCoverage)
            target = newChange.tippCoverage
        /*else if(newChange.priceItem && newChange.priceItem.tipp)
            target = newChange.priceItem*/
        if(newChange.msgToken != PendingChangeConfiguration.TITLE_REMOVED) {
            if(target) {
                PendingChange toApply = PendingChange.construct([target: target, oid: genericOIDService.getOID(subPkg.subscription), newValue: newChange.newValue, oldValue: newChange.oldValue, prop: newChange.targetProperty, msgToken: newChange.msgToken, status: RDStore.PENDING_CHANGE_PENDING, owner: contextOrg])
                if(accept(toApply, subPkg.subscription.id)) {
                    if(auditService.getAuditConfig(subPkg.subscription,newChange.msgToken)) {
                        log.debug("got audit config, processing ...")
                        applyPendingChangeForHolding(newChange, subPkg, contextOrg)
                    }
                }
                else
                    log.error("Error when auto-accepting pending change ${toApply} with token ${toApply.msgToken}!")
            }
            else log.error("Unable to determine target object! Ignoring change ${newChange}!")
        }
        else {
            accept(newChange, subPkg.subscription.id)
            subPkg.subscription.getDerivedSubscriptions().each { Subscription child ->
                accept(newChange, child.id)
            }
        }
    }

    /**
     * Auto-applies the given change to derived subscriptions (i.e. inherits the change applied on a consortial subscription)
     * @param newChange the change to apply
     * @param subPkg the (consortial) subscription package on which the change should be applied
     * @param contextOrg the subscription consortium
     */
    void applyPendingChangeForHolding(PendingChange newChange,SubscriptionPackage subPkg,Org contextOrg) {
        log.debug("applyPendingChangeForHolding")
        def target
        Set<Subscription> childSubscriptions = []
        if(newChange.tipp) {
            target = newChange.tipp
            childSubscriptions.addAll(Subscription.findAllByInstanceOf(subPkg.subscription))
        }
        else if(newChange.tippCoverage) {
            target = newChange.tippCoverage
            childSubscriptions.addAll(Subscription.executeQuery('select s from Subscription s join s.issueEntitlements ie where s.instanceOf = :parent and ie.tipp = :tipp and ie.status != :ieStatus',[parent:subPkg.subscription,tipp:target.tipp,ieStatus:RDStore.TIPP_STATUS_REMOVED]))
        }
        /*else if(newChange.priceItem && newChange.priceItem.tipp) {
            target = newChange.priceItem
            childSubscriptions.addAll(Subscription.executeQuery('select s from Subscription s join s.issueEntitlements ie where s.instanceOf = :parent and ie.tipp = :tipp and ie.status != :removed',[parent:subPkg.subscription,tipp:target.tipp,removed:RDStore.TIPP_STATUS_REMOVED]))
        }*/
        if(target) {
            if(childSubscriptions) {
                childSubscriptions.each { Subscription child ->
                    String oid
                    if(target instanceof TIPPCoverage /*|| target instanceof PriceItem*/) {
                        IssueEntitlement childIE = child.issueEntitlements.find { childIE -> childIE.tipp == target.tipp }
                        //if(target instanceof TIPPCoverage) {
                            if(target.findEquivalent(childIE.coverages))
                                oid = genericOIDService.getOID(child)
                        //}
                        /*else if(target instanceof PriceItem) {
                            if(target.findEquivalent(childIE.priceItems))
                                oid = genericOIDService.getOID(child)
                        }*/
                    }
                    else oid = genericOIDService.getOID(child)
                    if(oid) {
                        //log.debug("applyPendingChangeForHolding: processing child ${child.id}")
                        PendingChange toApplyChild = PendingChange.construct([target: target, oid: oid, newValue: newChange.newValue, oldValue: newChange.oldValue, prop: newChange.targetProperty, msgToken: newChange.msgToken, status: RDStore.PENDING_CHANGE_PENDING, owner: contextOrg])
                        if (!accept(toApplyChild, child.id)) {
                            log.error("Error when auto-accepting pending change ${toApplyChild} with token ${toApplyChild.msgToken}!")
                        }
                    }
                }
            }
        }
        else log.error("Unable to determine target object! Ignoring change ${newChange}!")
    }

    /**
     * Marks a change as acknowledged, i.e. deletes it
     * @param changeAccepted the change being acknowledged
     */
    void acknowledgeChange(PendingChange changeAccepted) {
        changeAccepted.delete()
    }

}
