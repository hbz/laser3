package de.laser

import de.laser.base.AbstractLockableService
import de.laser.exceptions.ChangeAcceptException
import de.laser.finance.CostItem
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.context.MessageSource

import java.sql.Array
import java.text.SimpleDateFormat
import java.time.Duration

/**
 * This service handles pending change processing and display
 */
@Transactional
class PendingChangeService extends AbstractLockableService {

    AuditService auditService
    ContextService contextService
    EscapeService escapeService
    GenericOIDService genericOIDService
    GlobalService globalService
    MessageSource messageSource

    /**
     * Much of this code has been commented out because the functionality got deprecated and disused.
     * Some of the functionality remained nevertheless intact, so that this method delivers changes concerning subscriptions and cost items.
     * They may be pending; those require actions on behalf of a user with editing rights or accepted, then, they appear on the notifications tab
     * for the next X days since the action date. X is the time span configured in the user profile for which notifications appear on the dashboard
     * @see PendingChangeConfiguration
     * @see CostItem
     * @see Subscription
     * @return a {@link Map} containing pending and accepted changes
     */
    Map<String, Object> getSubscriptionChanges(Map configMap) {
        Map<String, Object> result = [:]
        Locale locale = LocaleUtils.getCurrentLocale()
        Date time = new Date(System.currentTimeMillis() - Duration.ofDays(configMap.periodInDays).toMillis())
        /*
        get all subscription packages about whose movements the subscriber wishes to be notified about or needs to act
        */
        /*
        Set<SubscriptionPackage> subscriptionPackages = [], inheritedSubPackages = []
        if(configMap.consortialView == true) {
            subscriptionPackages.addAll(SubscriptionPackage.executeQuery('select sp from PendingChangeConfiguration pcc join pcc.subscriptionPackage sp join sp.subscription s join s.orgRelations oo ' +
                    'where oo.org = :context and oo.roleType = :roleType and s.instanceOf is null '+
                    'and (pcc.settingValue = :prompt or (pcc.settingValue = :accept and pcc.withNotification = true) or (pcc.settingValue = null and pcc.withNotification = true)) ' +
                    'order by s.name, s.startDate desc',
                    [context: configMap.contextOrg, roleType: RDStore.OR_SUBSCRIPTION_CONSORTIA, prompt: RDStore.PENDING_CHANGE_CONFIG_PROMPT, accept: RDStore.PENDING_CHANGE_CONFIG_ACCEPT]))
        }
        else {
            subscriptionPackages.addAll(SubscriptionPackage.executeQuery('select sp from PendingChangeConfiguration pcc join pcc.subscriptionPackage sp join sp.subscription s join s.orgRelations oo ' +
                    'where oo.org = :context and oo.roleType = :roleType '+
                    'and (pcc.settingValue = :prompt or (pcc.settingValue = :accept and pcc.withNotification = true) or (pcc.settingValue = null and pcc.withNotification = true)) ' +
                    'order by s.name, s.startDate desc',
                    [context: configMap.contextOrg, roleType: RDStore.OR_SUBSCRIBER, prompt: RDStore.PENDING_CHANGE_CONFIG_PROMPT, accept: RDStore.PENDING_CHANGE_CONFIG_ACCEPT]))
            inheritedSubPackages.addAll(SubscriptionPackage.executeQuery('select sp from SubscriptionPackage sp join sp.subscription s join s.orgRelations oo ' +
                    'where oo.org = :context and oo.roleType = :roleType '+
                    'and exists(select ac from AuditConfig ac where ac.referenceId = s.instanceOf.id and ac.referenceClass = :subscriptionClass and ac.referenceField in (:settingKeys)) ' +
                    'order by s.name, s.startDate desc',
                    [context: configMap.contextOrg, roleType: RDStore.OR_SUBSCRIBER_CONS,
                     subscriptionClass: Subscription.class.name, settingKeys: PendingChangeConfiguration.NOTIFICATION_KEYS]))
        }
        Map<SubscriptionPackage, Set<String>> packagesWithPending = [:], packagesWithNotification = [:]
        packagesWithPending.putAll(subscriptionPackages.collectEntries{ SubscriptionPackage sp ->
            Set<String> values = sp.pendingChangeConfig.findAll { PendingChangeConfiguration pcc -> pcc.settingValue == RDStore.PENDING_CHANGE_CONFIG_PROMPT }.collect { PendingChangeConfiguration pcc -> pcc.settingKey }
            [sp, values]
        })
        packagesWithNotification.putAll(subscriptionPackages.collectEntries{ SubscriptionPackage sp ->
            Set<String> values = sp.pendingChangeConfig.findAll { PendingChangeConfiguration pcc -> pcc.withNotification }.collect { PendingChangeConfiguration pcc -> pcc.settingKey }
            [sp, values]
        })
        */
        //inheritedSubPackages is always empty for consortia
        /*
        inheritedSubPackages.each { SubscriptionPackage sp ->
            Set<String> values = PendingChangeConfiguration.executeQuery("select ac.referenceField from AuditConfig ac where ac.referenceId = :id and ac.referenceClass = :subscriptionClass and ac.referenceField in (:settingKeys)",
                    [id: sp.subscription.instanceOf.id,subscriptionClass: Subscription.class.name, settingKeys: PendingChangeConfiguration.NOTIFICATION_KEYS]).collect { String val -> val.replace(PendingChangeConfiguration.NOTIFICATION_SUFFIX, '') }
            //should never overwrite something as only the consortium has the right to define values
            packagesWithNotification.put(sp, values)
        }
        */
        Set<Map> pending = [], notifications = []
        /*Map<Subscription, Set<Map>> pending = [:], notifications = [:]
        Set<RefdataValue> processed = [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED, RDStore.PENDING_CHANGE_SUPERSEDED]
        packagesWithPending.each { SubscriptionPackage sp, Set<String> keys ->
            //Set<Map> changes = pending.containsKey(sp.subscription) ? pending.get(sp.subscription) : []
            //changes.addAll(
            TitleChange.executeQuery('select new map(count(tic.id) as count, tic.event as event) from TitleChange tic join tic.tipp tipp join tipp.pkg pkg where tic.event in (:keys) and pkg = :pkg and tic.dateCreated >= :startDate ' +
                    'and not exists(select iec from IssueEntitlementChange iec where iec.titleChange = tic and iec.subscription = :sub and iec.status in (:processed)) '+
                    'group by tic.event ' +
                    'order by tic.event',
                    [keys: keys, pkg: sp.pkg, startDate: sp.dateCreated, sub: sp.subscription, processed: processed]).each { change ->
                pending << change+[subscription: sp.subscription]
            }
            //)
            //if(changes)
                //pending.put(sp.subscription, changes)
        }
        */
        Sql sql = globalService.obtainSqlConnection()
        if(configMap.consortialView == true) {
            //keep as alternative
            /*
            TitleChange.executeQuery('select s, count(tic.id), tic.event from TitleChange tic join tic.tipp tipp join tipp.pkg pkg, IssueEntitlement ie join ie.subscription s join s.orgRelations oo where tic.event = :titleRemoved '+
                    'and oo.org = :context and oo.roleType = :roleType and s.instanceOf = null ' +
                    'and ie.tipp = tipp and ie.status != :removed '+
                    'group by pkg, tic.event, s',
                    [context: configMap.contextOrg, roleType: RDStore.OR_SUBSCRIPTION_CONSORTIA, titleRemoved: PendingChangeConfiguration.TITLE_REMOVED, removed: RDStore.TIPP_STATUS_REMOVED])
            */
            sql.rows("select or_sub_fk, count(tic_id) as count, tic_event from title_change "+
                    "join title_instance_package_platform on tic_tipp_fk = tipp_id "+
                    "join issue_entitlement on ie_tipp_fk = tic_tipp_fk "+
                    "join org_role on or_sub_fk = ie_subscription_fk "+
                    "join subscription on ie_subscription_fk = sub_id "+
                    "where tic_event = :titleRemoved and or_org_fk = :context and or_roletype_fk = :roleType and sub_parent_sub_fk is null and ie_status_rv_fk != :removed "+
                    "group by tipp_pkg_fk, tic_event, or_sub_fk, sub_name, sub_start_date "+
                    "order by sub_name, sub_start_date desc",
                    [titleRemoved: PendingChangeConfiguration.TITLE_REMOVED, context: configMap.contextOrg.id, roleType: RDStore.OR_SUBSCRIPTION_CONSORTIA.id, removed: RDStore.TIPP_STATUS_REMOVED.id]).each { GroovyRowResult row ->
                Subscription subscription = Subscription.get(row['or_sub_fk'])
                //Set<Map> changes = pending.containsKey(subscription) ? pending.get(subscription) : []
                //changes << [count: row['count'], event: row['tic_event']]
                //pending.put(subscription, changes)
                pending << [count: row['count'], event: row['tic_event'], subscription: subscription]
            }
            /*
            sql.rows("select or_sub_fk, count(tic_id) as count, tic_event from title_change "+
                    "join title_instance_package_platform on tic_tipp_fk = tipp_id "+
                    "join issue_entitlement on ie_tipp_fk = tic_tipp_fk "+
                    "join org_role on or_sub_fk = ie_subscription_fk "+
                    "join subscription on ie_subscription_fk = sub_id "+
                    "where tic_event = :titleDeleted and or_org_fk = :context and or_roletype_fk = :roleType and sub_parent_sub_fk is null and ie_status_rv_fk != :deleted "+
                    "group by tipp_pkg_fk, tic_event, or_sub_fk, sub_name, sub_start_date "+
                    "order by sub_name, sub_start_date desc",
                    [titleDeleted: PendingChangeConfiguration.TITLE_DELETED, context: configMap.contextOrg.id, roleType: RDStore.OR_SUBSCRIPTION_CONSORTIA.id, deleted: RDStore.TIPP_STATUS_DELETED.id]).each { GroovyRowResult row ->
                Subscription subscription = Subscription.get(row['or_sub_fk'])
                //Set<Map> changes = pending.containsKey(subscription) ? pending.get(subscription) : []
                //changes << [count: row['count'], event: row['tic_event']]
                //pending.put(subscription, changes)
                pending << [count: row['count'], event: row['tic_event'], subscription: subscription]
            }
            */
        }
        else {
            //keep as alternative
            /*
            TitleChange.executeQuery('select s, count(tic.id), tic.event from TitleChange tic join tic.tipp tipp join tipp.pkg pkg, IssueEntitlement ie join ie.subscription s join s.orgRelations oo where tic.event = :titleRemoved '+
                    'and oo.org = :context and oo.roleType = :roleType ' +
                    'and ie.tipp = tipp and ie.status != :removed '+
                    'group by pkg, tic.event, s',
                    [context: configMap.contextOrg, roleType: RDStore.OR_SUBSCRIBER, titleRemoved: PendingChangeConfiguration.TITLE_REMOVED, removed: RDStore.TIPP_STATUS_REMOVED])
             */
            sql.rows("select or_sub_fk, count(tic_id) as count, tic_event from title_change "+
                    "join title_instance_package_platform on tic_tipp_fk = tipp_id "+
                    "join issue_entitlement on ie_tipp_fk = tic_tipp_fk "+
                    "join org_role on or_sub_fk = ie_subscription_fk "+
                    "join subscription on ie_subscription_fk = sub_id "+
                    "where tic_event = :titleRemoved and or_org_fk = :context and or_roletype_fk = :roleType and ie_status_rv_fk != :removed "+
                    "group by tipp_pkg_fk, tic_event, or_sub_fk, sub_name, sub_start_date "+
                    "order by sub_name, sub_start_date desc",
                    [titleRemoved: PendingChangeConfiguration.TITLE_REMOVED, context: configMap.contextOrg.id, roleType: RDStore.OR_SUBSCRIBER.id, removed: RDStore.TIPP_STATUS_REMOVED.id]).each { GroovyRowResult row ->
                Subscription subscription = Subscription.get(row['or_sub_fk'])
                //Set<Map> changes = pending.containsKey(subscription) ? pending.get(subscription) : []
                //changes << [count: row['count'], event: row['tic_event']]
                //pending.put(subscription, changes)
                pending << [count: row['count'], event: row['tic_event'], subscription: subscription]
            }
            /*
            sql.rows("select or_sub_fk, count(tic_id) as count, tic_event from title_change "+
                    "join title_instance_package_platform on tic_tipp_fk = tipp_id "+
                    "join issue_entitlement on ie_tipp_fk = tic_tipp_fk "+
                    "join org_role on or_sub_fk = ie_subscription_fk "+
                    "join subscription on ie_subscription_fk = sub_id "+
                    "where tic_event = :titleDeleted and or_org_fk = :context and or_roletype_fk = :roleType and ie_status_rv_fk != :deleted "+
                    "group by tipp_pkg_fk, tic_event, or_sub_fk, sub_name, sub_start_date "+
                    "order by sub_name, sub_start_date desc",
                    [titleDeleted: PendingChangeConfiguration.TITLE_DELETED, context: configMap.contextOrg.id, roleType: RDStore.OR_SUBSCRIBER.id, deleted: RDStore.TIPP_STATUS_DELETED.id]).each { GroovyRowResult row ->
                Subscription subscription = Subscription.get(row['or_sub_fk'])
                //Set<Map> changes = pending.containsKey(subscription) ? pending.get(subscription) : []
                //changes << [count: row['count'], event: row['tic_event']]
                //pending.put(subscription, changes)
                pending << [count: row['count'], event: row['tic_event'], subscription: subscription]
            }
            */
        }
        /*
        packagesWithNotification.each { SubscriptionPackage sp, Set<String> keys ->
            //Set<Map> changes = notifications.containsKey(sp.subscription) ? notifications.get(sp.subscription) : []
            //changes.addAll(
            Set<String> withAction = [], notificationOnly = []
            keys.each { String key ->
                switch(key) {
                    case PendingChangeConfiguration.NEW_TITLE: withAction << key
                        break
                    case PendingChangeConfiguration.TITLE_STATUS_CHANGED: notificationOnly << key
                        break
                }
            }
            if(withAction) {
                IssueEntitlementChange.executeQuery('select new map(count(tic.id) as count, tic.event as event) from IssueEntitlementChange iec join iec.titleChange tic join tic.tipp tipp join tipp.pkg pkg where tic.event in (:keys) and pkg = :pkg ' +
                        'and iec.status = :accepted and iec.subscription = :sub and iec.actionDate >= :startDate ' +
                        'group by tic.event '+
                        'order by tic.event',
                        [keys: withAction, pkg: sp.pkg, sub: sp.subscription, accepted: RDStore.PENDING_CHANGE_ACCEPTED, startDate: time]).each { change ->
                    notifications << change+[subscription: sp.subscription]
                }
            }
            if(notificationOnly) {
                TitleChange.executeQuery('select new map(count(tic.id) as count, tic.event as event) from TitleChange tic join tic.tipp tipp join tipp.pkg pkg where tic.event in (:keys) and pkg = :pkg and tic.dateCreated >= :startDate ' +
                        'group by tic.event '+
                        'order by tic.event',
                        [keys: notificationOnly, pkg: sp.pkg, startDate: time]).each { change ->
                    notifications << change+[subscription: sp.subscription]
                }
            }
            PendingChange.executeQuery('select new map(count(pc.id) as count, pc.msgToken as event) from PendingChange pc where pc.msgToken in (:keys) and pc.pkg = :pkg and pc.dateCreated >= :startDate group by pc.msgToken order by pc.msgToken',
                [keys: keys, pkg: sp.pkg, startDate: time]).each { change ->
                notifications << change+[subscription: sp.subscription]
            }
            //)
            //if(changes)
                //notifications.put(sp.subscription, changes)
        }
        */
        //non title changes
        PendingChange.executeQuery("select pc from PendingChange pc full join pc.costItem ci where pc.owner = :contextOrg and pc.status in (:status) and (pc.msgToken in (:newSubscription) or (pc.costItem != null and ci.costItemStatus != :deleted))",
                [contextOrg: configMap.contextOrg, status: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED, RDStore.PENDING_CHANGE_PENDING], newSubscription: [PendingChangeConfiguration.NEW_SUBSCRIPTION, PendingChangeConfiguration.SUBSCRIPTION_RENEWED], deleted: RDStore.COST_ITEM_DELETED]).each { PendingChange pc ->
            if(pc.costItem) {
                /*Set<Map> changes
                if(pc.status == RDStore.PENDING_CHANGE_ACCEPTED)
                    changes = notifications.containsKey(pc.costItem.sub) ? notifications.get(pc.costItem.sub) : []
                else if(pc.status == RDStore.PENDING_CHANGE_PENDING)
                    changes = pending.containsKey(pc.costItem.sub) ? pending.get(pc.costItem.sub) : []
                if(changes != null) { */
                    Object[] args = [pc.oldValue, pc.newValue]
                    Map<String, Object> change = [event: pc.msgToken, costItem: pc.costItem, changeId: pc.id, args: args, subscription: pc.costItem.sub]
                    if(pc.status == RDStore.PENDING_CHANGE_ACCEPTED) {
                        if(pc.actionDate >= time || pc.ts >= time) {
                            //notifications.put(pc.costItem.sub, changes)
                            notifications << change
                        }
                    }
                    else if(pc.status == RDStore.PENDING_CHANGE_PENDING) {
                        //pending.put(pc.costItem.sub, changes)
                        pending << change
                    }
                //}
            }
            else {
                //Set<Map> changes
                pc.subscription._getCalculatedPrevious().each { Subscription previous ->
                    /*if(pc.status == RDStore.PENDING_CHANGE_ACCEPTED)
                        changes = notifications.containsKey(previous) ? notifications.get(previous) : []
                    else if(pc.status == RDStore.PENDING_CHANGE_PENDING)
                        changes = pending.containsKey(previous) ? pending.get(previous) : []
                    if(changes != null) { */
                        Map change = [event: pc.msgToken, source: genericOIDService.getOID(previous), changeId: pc.id, target: genericOIDService.getOID(pc.subscription), subscription: previous]
                        if(pc.status == RDStore.PENDING_CHANGE_ACCEPTED) {
                            if(pc.actionDate >= time || pc.ts >= time) {
                                //notifications.put(previous, changes)
                                notifications << change
                            }
                        }
                        else if(pc.status == RDStore.PENDING_CHANGE_PENDING) {
                            //pending.put(previous, changes)
                            pending << change
                        }
                    //}
                }
            }
        }
        /*
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
        */
        //log.debug(pending.toMapString())
        //log.debug(notifications.toMapString())
        result.notifications = notifications.drop(configMap.acceptedOffset).take(configMap.max)
        result.notificationsCount = notifications.size()
        result.pending = pending.drop(configMap.pendingOffset).take(configMap.max)
        result.pendingCount = pending.size()
        result.acceptedOffset = configMap.acceptedOffset
        result.pendingOffset = configMap.pendingOffset
        result
    }

    /**
     * Gets the recent and pending changes on titles for the given institution. This method has been translated into SQL
     * queries because the GORM loading slows the query very much down
     * @param configMap a map containing the configuration parameters such as context institution, user's time setting
     * @return a map containing title changes and notification
     * @deprecated disused and replaced by {@link #getSubscriptionChanges(java.util.Map)}
     */
    @Deprecated
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
            sql.rows("select pc_id, pc_ci_fk, pc_msg_token, pc_old_value, pc_new_value, pc_sub_fk, pc_status_rdv_fk, pc_ts from pending_change right join cost_item on pc_ci_fk = ci_id where pc_owner = :contextOrg and pc_status_rdv_fk = any(:status) and (pc_msg_token = :newSubscription or (pc_ci_fk is not null and ci_status_rv_fk != :deleted))", [contextOrg: configMap.contextOrg.id, status: sql.connection.createArrayOf('bigint', acceptedStatus.keySet() as Object[]), newSubscription: PendingChangeConfiguration.NEW_SUBSCRIPTION, deleted: RDStore.COST_ITEM_DELETED.id]).each { GroovyRowResult pc ->
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
     * @deprectaed not needed any more; functionality is in the new principal method {@link #getSubscriptionChanges(java.util.Map)}
     */
    @Deprecated
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
     * @deprecated not needed any more; functionality is in the new principal method {@link #getSubscriptionChanges(java.util.Map)}
     */
    @Deprecated
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
     * Converts the given value according to the field type
     * @param change the change whose property should be output
     * @param key the string value
     * @return the value as {@link Date} or {@link String}
     * @deprecated not needed any more; functionality is in the new principal method {@link #getSubscriptionChanges(java.util.Map)}
     */
    @Deprecated
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
     * @deprecated unused since we:kb changes are monitored differently and handled by {@link WekbStatsService} / {@link MarkerService}
     */
    @Deprecated
    Map<String, Integer> getCountsForPackages(Map<SubscriptionPackage, Map<String, RefdataValue>> pkgList) {
        Integer newTitlesPending = 0, titlesDeletedPending = 0, titlesRemovedAccepted = 0
        Integer newTitlesAccepted = 0, titlesStatusChangedAccepted = 0
        List<RefdataValue> processed = [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED]
        pkgList.each { SubscriptionPackage sp, Map<String, RefdataValue> settings ->
            //spontaneous convention: 1: title, a: accepted, b: pending
            List acceptedTitleCounts = IssueEntitlement.executeQuery("select count(ie.id), 'pendingChange.message_TP04' from IssueEntitlement ie join ie.tipp tipp where tipp.pkg = :pkg and ie.subscription = :sub and ie.status = :removed", [sub: sp.subscription, pkg: sp.pkg, removed: RDStore.TIPP_STATUS_REMOVED])
            /*List acceptedTitleCounts = IssueEntitlementChange.executeQuery('select count(iec.id), tic.event from IssueEntitlementChange iec join iec.titleChange tic join tic.tipp tipp where tipp.pkg = :package and tic.event = :eventType and iec.status in (:processed) and iec.subscription = :sub group by tic.event', [package: sp.pkg, sub: sp.subscription, eventType: PendingChangeConfiguration.NEW_TITLE, processed: processed])
            //acceptedTitleCounts.addAll(TitleChange.executeQuery('select count(tic.id), tic.event from TitleChange tic join tic.tipp tipp where tipp.pkg = :package and tipp in (select ie.tipp from IssueEntitlement ie where ie.subscription = :sub and ie.status != :removed) and tic.event = :eventType and tic.dateCreated >= :entryDate group by tic.event', [sub: sp.subscription, package: sp.pkg, removed: RDStore.TIPP_STATUS_REMOVED, eventType: PendingChangeConfiguration.TITLE_STATUS_CHANGED, entryDate: sp.dateCreated]))
            List pendingTitleCounts = []
            if(PendingChangeConfiguration.NEW_TITLE in settings.keySet() && settings.get(PendingChangeConfiguration.NEW_TITLE) != RDStore.PENDING_CHANGE_CONFIG_REJECT) {
                //if(params.eventType == PendingChangeConfiguration.NEW_TITLE)
                pendingTitleCounts.addAll(TitleChange.executeQuery('select count(tic.id), tic.event from TitleChange tic join tic.tipp tipp join tipp.pkg pkg where pkg = :package and tic.dateCreated >= :entryDate and tic.event = :eventType and tipp.status != :removed and not exists (select iec.id from IssueEntitlementChange iec where iec.titleChange = tic and iec.subscription = :subscription and iec.status in (:processed)) group by tic.event, tipp', [package: sp.pkg, entryDate: sp.dateCreated, removed: RDStore.TIPP_STATUS_REMOVED, eventType: PendingChangeConfiguration.NEW_TITLE, subscription: sp.subscription, processed: processed]))
            }
            if(PendingChangeConfiguration.TITLE_REMOVED in settings.keySet()) {
                //else if(params.eventType == PendingChangeConfiguration.TITLE_REMOVED)
                pendingTitleCounts.addAll(PendingChange.executeQuery('select count(tic.id), tic.event from TitleChange tic join tic.tipp tipp where tipp in (select ie.tipp from IssueEntitlement ie where ie.subscription = :sub and ie.status != :removed) and tipp.pkg = :pkg and tic.event = :eventType group by tic.event', [sub: sp.subscription, pkg: sp.pkg, removed: RDStore.TIPP_STATUS_REMOVED, eventType: PendingChangeConfiguration.TITLE_REMOVED]))
            }
            if(PendingChangeConfiguration.TITLE_DELETED in settings.keySet()) {
                //else if(params.eventType == PendingChangeConfiguration.TITLE_DELETED)
                pendingTitleCounts.addAll(PendingChange.executeQuery('select count(tic.id), tic.event from TitleChange tic join tic.tipp tipp where tipp in (select ie.tipp from IssueEntitlement ie where ie.subscription = :sub and ie.status != :deleted) and tipp.pkg = :pkg and tic.event = :eventType group by tic.event', [sub: sp.subscription, pkg: sp.pkg, deleted: RDStore.TIPP_STATUS_DELETED, eventType: PendingChangeConfiguration.TITLE_DELETED]))
            }
            */
            /*
            List acceptedTitleCounts = PendingChange.executeQuery('select count(pc.id), pc.msgToken from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken in (:eventTypes) and pc.oid = :subOid and pc.status in (:pendingStatus) group by pc.msgToken', [package: sp.pkg, entryDate: sp.dateCreated, subOid: genericOIDService.getOID(sp.subscription), eventTypes: [PendingChangeConfiguration.NEW_TITLE, PendingChangeConfiguration.TITLE_UPDATED, PendingChangeConfiguration.TITLE_DELETED], pendingStatus: pendingStatus])
            //String query1b
            List pendingTitleCounts = []
            if(PendingChangeConfiguration.NEW_TITLE in settings.keySet() && (settings.get(PendingChangeConfiguration.NEW_TITLE) == null || settings.get(PendingChangeConfiguration.NEW_TITLE) == RDStore.PENDING_CHANGE_CONFIG_PROMPT)) {
                //if(params.eventType == PendingChangeConfiguration.NEW_TITLE)
                pendingTitleCounts.addAll(PendingChange.executeQuery('select count(pc.id), pc.msgToken from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.tipp.status != :removed and (not exists (select pca.id from PendingChange pca join pca.tipp tippA where tippA = pc.tipp and pca.oid = :subOid and pca.status in (:pendingStatus)) and not exists (select ie.id from IssueEntitlement ie where ie.tipp = pc.tipp and ie.status != :removed and ie.subscription = :subscription)) and pc.status = :packageHistory group by pc.msgToken, pc.tipp', [package: sp.pkg, entryDate: sp.dateCreated, eventType: PendingChangeConfiguration.NEW_TITLE, subOid: genericOIDService.getOID(sp.subscription), pendingStatus: pendingStatus, removed: RDStore.TIPP_STATUS_REMOVED, subscription: sp.subscription, packageHistory: RDStore.PENDING_CHANGE_HISTORY]))
            }
            if(PendingChangeConfiguration.TITLE_UPDATED in settings.keySet() && (settings.get(PendingChangeConfiguration.TITLE_UPDATED) == null || settings.get(PendingChangeConfiguration.TITLE_UPDATED) == RDStore.PENDING_CHANGE_CONFIG_PROMPT)) {
                //else if(params.eventType == PendingChangeConfiguration.TITLE_UPDATED)
                pendingTitleCounts.addAll(PendingChange.executeQuery('select count(pc.id), pc.msgToken from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.tipp.status != :removed and not exists (select pca.id from PendingChange pca join pca.tipp tippA where tippA = pc.tipp and pca.oid = :subOid and (pca.newValue = pc.newValue or (pca.newValue is null and pc.newValue is null)) and pca.status in (:pendingStatus)) and pc.status = :packageHistory group by pc.msgToken', [package: sp.pkg, entryDate: sp.dateCreated, eventType: PendingChangeConfiguration.TITLE_UPDATED, subOid: genericOIDService.getOID(sp.subscription), pendingStatus: pendingStatus, removed: RDStore.TIPP_STATUS_REMOVED, packageHistory: RDStore.PENDING_CHANGE_HISTORY]))
            }
            if(PendingChangeConfiguration.TITLE_DELETED in settings.keySet() && (settings.get(PendingChangeConfiguration.TITLE_DELETED) == null || settings.get(PendingChangeConfiguration.TITLE_DELETED) == RDStore.PENDING_CHANGE_CONFIG_PROMPT)) {
                //else if(params.eventType == PendingChangeConfiguration.TITLE_DELETED)
                pendingTitleCounts.addAll(PendingChange.executeQuery('select count(pc.id), pc.msgToken from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.tipp.status != :removed and not exists (select pca.id from PendingChange pca join pca.tipp tippA where tippA = pc.tipp and pca.oid = :subOid and pca.status in (:pendingStatus)) and exists(select ie.id from IssueEntitlement ie where ie.tipp = pc.tipp and ie.status not in (:deleted) and ie.subscription = :subscription) and pc.status = :packageHistory group by pc.msgToken', [package: sp.pkg, entryDate: sp.dateCreated, eventType: PendingChangeConfiguration.TITLE_DELETED, subOid: genericOIDService.getOID(sp.subscription), pendingStatus: pendingStatus, removed: RDStore.TIPP_STATUS_REMOVED, deleted: [RDStore.TIPP_STATUS_REMOVED, RDStore.TIPP_STATUS_DELETED], subscription: sp.subscription, packageHistory: RDStore.PENDING_CHANGE_HISTORY]))
            }
            if(PendingChangeConfiguration.TITLE_REMOVED in settings.keySet()) {
                //else if(params.eventType == PendingChangeConfiguration.TITLE_REMOVED)
                pendingTitleCounts.addAll(PendingChange.executeQuery('select count(pc.id), pc.msgToken from PendingChange pc join pc.tipp tipp where tipp in (select ie.tipp from IssueEntitlement ie where ie.subscription = :sub and ie.status != :removed) and tipp.pkg = :pkg and pc.msgToken = :eventType group by pc.msgToken', [sub: sp.subscription, pkg: sp.pkg, removed: RDStore.TIPP_STATUS_REMOVED, eventType: PendingChangeConfiguration.TITLE_REMOVED]))
            }
            //pendingTitleCounts.addAll(sql.rows("select count(id) as count, pc_msg_token from pending_change join title_instance_package_platform on pc_tipp_fk = tipp_id join subscription_package on sp_pkg_fk = tipp_pkg_fk join org_role on or_sub_fk = sp_sub_fk join subscription on sp_sub_fk = sub_id where or_org_fk = :context and or_roletype_fk = any(:roleTypes) and pc_msg_token = :removed and exists(select ie_id from issue_entitlement where ie_tipp_fk = pc_tipp_fk and ie_subscription_fk = sp_sub_fk and ie_status_rv_fk != :ieRemoved) and sp_sub_fk = :subId group by pc_msg_token", [context: contextOrg.id, roleTypes: connection.createArrayOf('bigint', [RDStore.OR_SUBSCRIPTION_CONSORTIA.id, RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id] as Object[]), subId: sp.subscription.id, removed: PendingChangeConfiguration.TITLE_REMOVED, ieRemoved: RDStore.TIPP_STATUS_REMOVED.id]))
             */
            acceptedTitleCounts.each { row ->
                switch(row[1]) {
                    case PendingChangeConfiguration.NEW_TITLE: newTitlesAccepted += row[0]
                        break
                    case PendingChangeConfiguration.TITLE_STATUS_CHANGED: titlesStatusChangedAccepted += row[0]
                        break
                    case PendingChangeConfiguration.TITLE_REMOVED: titlesRemovedAccepted += row[0]
                        break
                }
            }
            /*
            pendingTitleCounts.each { row ->
                switch(row[1]) {
                    case PendingChangeConfiguration.NEW_TITLE: newTitlesPending += row[0]
                        break
                    case PendingChangeConfiguration.TITLE_DELETED: titlesDeletedPending += row[0]
                        break
                }
            }
            */
        }
        Integer pendingCount = newTitlesPending+titlesDeletedPending
        Integer acceptedCount = newTitlesAccepted+titlesStatusChangedAccepted+titlesRemovedAccepted
        [countPendingChanges: pendingCount,
         countAcceptedChanges: acceptedCount,
         newTitlesPending: newTitlesPending,
         titlesDeletedPending: titlesDeletedPending,
         titlesRemovedAccepted: titlesRemovedAccepted,
         newTitlesAccepted: newTitlesAccepted,
         titlesStatusChangedAccepted: titlesStatusChangedAccepted]
    }

    /**
     * Accepts the given change and applies the parameters
     * @param pc the change to accept
     * @param subId the subscription on which the change should be applied
     * @return true if the change could be applied successfully, false otherwise
     * @throws ChangeAcceptException
     */
    boolean accept(PendingChange pc) throws ChangeAcceptException {
        log.debug("accept: ${pc.msgToken} for ${pc.pkg}")
        boolean done = false
        def target
        if(pc.oid)
            target = genericOIDService.resolveOID(pc.oid)
        else if(pc.costItem)
            target = pc.costItem
        /*else if(subId)
            target = Subscription.get(subId)*/
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
        /*if(done == 'reject') {
            //post reject for void changes
            pc.status = RDStore.PENDING_CHANGE_REJECTED
            pc.actionDate = new Date()
            if(!pc.save()) {
                throw new ChangeAcceptException("problems when submitting new pending change status: ${pc.errors}")
            }
        }
        else*/
        if(done || pc.msgToken in [PendingChangeConfiguration.NEW_SUBSCRIPTION, PendingChangeConfiguration.SUBSCRIPTION_RENEWED]) {
            pc.status = RDStore.PENDING_CHANGE_ACCEPTED
            pc.actionDate = new Date()
            if(!pc.save()) {
                throw new ChangeAcceptException("problems when submitting new pending change status: ${pc.errors}")
            }
        }
        done
    }

    @Deprecated
    boolean acceptTitleChange(IssueEntitlementChange iec) throws ChangeAcceptException {
        boolean done = false
        switch(iec.titleChange.event) {
            //pendingChange.message_TP01 (newTitle)
            case PendingChangeConfiguration.NEW_TITLE:
                TitleInstancePackagePlatform target = iec.titleChange.tipp
                IssueEntitlement newTitle = IssueEntitlement.construct([subscription:iec.subscription,tipp:target,status:target.status])
                if(newTitle) {
                    done = true
                }
                else throw new ChangeAcceptException("problems when creating new entitlement - pending change not accepted: ${newTitle.errors}")
                break
            /*
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
                        IssueEntitlement restoredTitle = IssueEntitlement.construct([subscription:target,tipp:pc.tipp,status:pc.tipp.status])
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
            */
            //pendingChange.message_TP03 (titleDeleted)
            case PendingChangeConfiguration.TITLE_DELETED:
                IssueEntitlement.executeUpdate("update IssueEntitlement ie set ie.status = :deleted where ie.tipp = :title and ie.subscription in (select s from Subscription s where :subscription in (s.id, s.instanceOf.id))", [deleted: RDStore.TIPP_STATUS_DELETED, title: iec.titleChange.tipp, subscription: iec.subscription])
                break
                //pendingChange.message_TP04 (titleRemoved)
            case PendingChangeConfiguration.TITLE_REMOVED:
                //log.debug("removing instances of ${pc.tipp} from holding ...")
                IssueEntitlement.executeUpdate("update IssueEntitlement ie set ie.status = :removed where ie.tipp = :title and ie.subscription in (select s from Subscription s where :subscription in (s.id, s.instanceOf.id))", [removed: RDStore.TIPP_STATUS_REMOVED, title: iec.titleChange.tipp, subscription: iec.subscription])
                //else throw new ChangeAcceptException("no instance of IssueEntitlement stored: ${pc.oid}! Pending change is void!")
                break
        }
        if(done) {
            iec.status = RDStore.PENDING_CHANGE_ACCEPTED
            iec.actionDate = new Date()
            iec.save()
        }
        done
    }

    /**
     * Rejects the given change and sets the flag to prevent accidents
     * @param pc the change to reject
     * @return true if the rejection was successful, false otherwise
     */
    boolean reject(PendingChange pc) {
        pc.status = RDStore.PENDING_CHANGE_REJECTED
        pc.actionDate = new Date()
        if(!pc.save()) {
            throw new ChangeAcceptException("problems when submitting new pending change status: ${pc.errors}")
        }
        true
    }

    /**
     * Rejects the given issue entitlement change and sets the flag to prevent accidents
     * @param iec the change to reject
     * @return true if the rejection was successful, false otherwise
     * @deprecated functionality removed without replacement
     */
    @Deprecated
    boolean reject(IssueEntitlementChange iec) {
        iec.status = RDStore.PENDING_CHANGE_REJECTED
        iec.actionDate = new Date()
        if(!iec.save()) {
            throw new ChangeAcceptException("problems when submitting new pending change status: ${iec.errors}")
        }
        true
    }

    /**
     * Auto-applies the given change to the given subscription
     * @param newChange the change to apply
     * @param subPkg the subscription package on which the change should be applied
     * @param contextOrg the subscriber
     * @deprectaed functionality removed without replacement
     */
    @Deprecated
    void applyPendingChange(TitleChange newChange,SubscriptionPackage subPkg,Org contextOrg) {
        log.debug("applyPendingChange")
        if(!(newChange.event in [PendingChangeConfiguration.TITLE_REMOVED, PendingChangeConfiguration.TITLE_DELETED])) {
            IssueEntitlementChange toApply = IssueEntitlementChange.construct([titleChange: newChange, subscription: subPkg.subscription, status: RDStore.PENDING_CHANGE_PENDING, owner: contextOrg])
            if (acceptTitleChange(toApply)) {
                if (auditService.getAuditConfig(subPkg.subscription, newChange.event)) {
                    log.debug("got audit config, processing ...")
                    applyPendingChangeForHolding(newChange, subPkg, contextOrg)
                }
            }
            else
                log.error("Error when auto-accepting title change ${toApply} with token ${newChange.event}!")
        }
        else {
            acceptTitleChange(newChange)
            subPkg.subscription.getDerivedSubscriptions().each { Subscription child ->
                acceptTitleChange(newChange)
            }
        }
    }

    /**
     * Auto-applies the given change to derived subscriptions (i.e. inherits the change applied on a consortial subscription)
     * @param newChange the change to apply
     * @param subPkg the (consortial) subscription package on which the change should be applied
     * @param contextOrg the subscription consortium
     * @deprectaed functionality removed without replacement
     */
    @Deprecated
    void applyPendingChangeForHolding(TitleChange newChange,SubscriptionPackage subPkg,Org contextOrg) {
        log.debug("applyPendingChangeForHolding")
        Set<Subscription> childSubscriptions = Subscription.findAllByInstanceOf(subPkg.subscription)
        childSubscriptions.each { Subscription child ->
            //log.debug("applyPendingChangeForHolding: processing child ${child.id}")
            IssueEntitlementChange toApplyChild = IssueEntitlementChange.construct([titleChange: newChange, subscription: child, status: RDStore.PENDING_CHANGE_PENDING, owner: contextOrg])
            if (!acceptTitleChange(toApplyChild)) {
                log.error("Error when auto-accepting title change ${toApplyChild} with token ${newChange.event}!")
            }
        }
    }

    /**
     * Marks a change as acknowledged, i.e. deletes it
     * @param changeAccepted the change being acknowledged
     */
    void acknowledgeChange(PendingChange changeAccepted) {
        changeAccepted.delete()
    }

}
