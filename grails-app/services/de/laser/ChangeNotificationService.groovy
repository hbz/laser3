package de.laser

import de.laser.auth.User
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.base.AbstractLockableService
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONElement

import java.sql.Timestamp
import java.util.concurrent.ExecutorService

/**
 * This service is actually deprecated as whole and the only method which is not marked as deprecated is subject
 * of refactoring, too. This service handles the so-called inheritance of properties between consortial and their
 * derived objects. If a change is being done, a pending change object will be created which retains what has been
 * changed on what and that change is being applied to every object derived from the object on which the change
 * has been triggered
 */
@Transactional
class ChangeNotificationService extends AbstractLockableService {

    ExecutorService executorService
    GenericOIDService genericOIDService
    GlobalService globalService
    ContextService contextService
    PendingChangeService pendingChangeService

    // N,B, This is critical for this service as it's called from domain object OnChange handlers
    static transactional = false

    static final Map<String, String> CHANGE_NOTIFICATIONS = [

        'TitleInstance.propertyChange' :
                'Title change - The <strong>${evt.prop}</strong> field was changed from "<strong>${evt.oldLabel?:evt.old}</strong>" to "<strong>${evt.newLabel?:evt.new}</strong>".',

        'TitleInstance.identifierAdded' :
                'An identifier was added to title ${OID?.title}.',

        'TitleInstance.identifierRemoved' :
                'An identifier was removed from title ${OID?.title}.',

        'TitleInstancePackagePlatform.updated' :
                'TIPP change for title ${OID?.title?.title} - The <strong>${evt.prop}</strong> field was changed from "<strong>${evt.oldLabel?:evt.old}</strong>" to "<strong>${evt.newLabel?:evt.new}</strong>".',

        'TitleInstancePackagePlatform.added' :
                'TIPP Added for title ${OID?.title?.title} ${evt.linkedTitle} on platform ${evt.linkedPlatform} .',

        'TitleInstancePackagePlatform.deleted' :
                'TIPP Deleted for title ${OID?.title?.title} ${evt.linkedTitle} on platform ${evt.linkedPlatform} .' ,

        'Package.created' :
                'New package added with id ${OID.id} - "${OID.name}".'
    ]


    @Deprecated
  void broadcastEvent(String contextObjectOID, changeDetailDocument) {
    // log.debug("broadcastEvent(${contextObjectOID},${changeDetailDocument})");

    def jsonChangeDocument = changeDetailDocument as JSON
      ChangeNotificationQueueItem new_queue_item = new ChangeNotificationQueueItem(oid:contextObjectOID,
                                                         changeDocument:jsonChangeDocument.toString(),
                                                         ts:new Date())
    if ( ! new_queue_item.save() ) {
      log.error(new_queue_item.errors.toString());
    }
  }


  @Deprecated
  boolean aggregateAndNotifyChanges() {
      if(!running) {
          running = true
          executorService.execute({
              internalAggregateAndNotifyChanges();
          })
          return true
      }
      else {
          log.warn("Not running, still one process active!")
          return false
      }
  }

    @Deprecated
    def internalAggregateAndNotifyChanges() {
        boolean running = true

        while (running) {
            try {
                List<ChangeNotificationQueueItem> queueItems = ChangeNotificationQueueItem.executeQuery(
                    "select distinct c.oid from ChangeNotificationQueueItem as c order by c.oid", [max: 1]
                )
                innerInternalAggregateAndNotifyChanges(queueItems)
            }
            catch (Exception e) {
                running = false
                log.error("Problem", e)
            }
            finally {
                running = false
                this.running = false
            }
        }
    }

    @Deprecated
    def innerInternalAggregateAndNotifyChanges(List<ChangeNotificationQueueItem> queueItems) throws Exception {

        queueItems.each { poidc ->

            int contr = 0
            def contextObject = genericOIDService.resolveOID(poidc);
            log.debug("Got contextObject ${contextObject} for poidc ${poidc}")

            if ( contextObject == null ) {
              log.warn("Pending changes for a now deleted item.. nuke them!");
              ChangeNotificationQueueItem.executeUpdate("delete ChangeNotificationQueueItem c where c.oid = :oid", [oid: poidc])
            }

            List<ChangeNotificationQueueItem> pendingChanges = ChangeNotificationQueueItem.executeQuery(
                    "select c from ChangeNotificationQueueItem as c where c.oid = :oid order by c.ts asc", [oid: poidc]
            )
            StringWriter sw = new StringWriter();

            if ( contextObject ) {
              if ( contextObject.metaClass.respondsTo(contextObject, 'getURL') ) {
                  // pendingChange.message_1001
                sw.write("<p>Änderungen an <a href=\"${contextObject.getURL()}\">${contextObject.toString()}</a> ${new Date().toString()}</p><p><ul>");
              }
              else  {
                  // pendingChange.message_1002
                sw.write("<p>Änderungen an ${contextObject.toString()} ${new Date().toString()}</p><p><ul>");
              }
            }
            List pc_delete_list = []

            log.debug("TODO: Processing ${pendingChanges.size()} notifications for object ${poidc}")

        pendingChanges.each { pc ->
          // log.debug("Process pending change ${pc}");    
            JSONElement parsed_event_info = JSON.parse(pc.changeDocument)
          log.debug("Event Info: ${parsed_event_info}")

            String change_template = CHANGE_NOTIFICATIONS.get(parsed_event_info.event)
          if ( change_template != null ) {
            Map event_props = [o:contextObject, evt:parsed_event_info]
            if ( parsed_event_info.OID != null && parsed_event_info.OID.length() > 0 ) {
              event_props.OID = genericOIDService.resolveOID(parsed_event_info.OID);
            }
            if( event_props.OID ) {
              def engine = new groovy.text.GStringTemplateEngine()
              def tmpl = engine.createTemplate(change_template).make(event_props)
              sw.write("<li>");
              sw.write(tmpl.toString());
              sw.write("</li>");
            }else{
              // pendingChange.message_1003
              sw.write("<li>Komponente ${parsed_event_info.OID} wurde gelöscht!</li>")
            }
          }
          else {
            // pendingChange.message_1004
            sw.write("<li>Template für das Ereignis \"ChangeNotification.${parsed_event_info.event}\" kann nicht gefunden werden. Infos zum Ereignis:\n\n${pc.changeDocument}</li>");
          }
          contr++;

          if(contr > 0 && contr % 100 == 0){
            log.debug("Processed ${contr} notifications for object ${poidc}")
          }
          pc_delete_list.add(pc.id)

        } // pendingChanges.each{}

        sw.write("</ul></p>");

            if (pc_delete_list) {
                log.debug('Deleting ChangeNotificationQueueItems: ' + pc_delete_list)
                ChangeNotificationQueueItem.executeUpdate('DELETE FROM ChangeNotificationQueueItem WHERE id in (:idList)', [idList: pc_delete_list])
            }

        globalService.cleanUpGorm()
      } // queueItems.each{}
  }

    /**
    *  An object has changed. Because we don't want to do heavy work of calculating dependent objects in the thread doing the DB
    *  commit, responsibility for handling the change is delegated to this method. However, the source object is the seat of
    *  knowledge for what dependencies there are (For example, a title change should propagate to all packages using that title).
    *  Therefore, we get a new handle to the object.
    */
    def fireEvent(Map<String, Object> changeDocument) {
        log.debug("fireEvent(${changeDocument})")

        //store changeDoc in cache
        //EhcacheWrapper cache = cacheService.getTTL1800Cache("/pendingChanges/")
        //cache.put(changeDocument.OID,changeDocument)

        // TODO [ticket=1807] should not be done in extra thread but collected and saved afterwards
        executorService.execute({
            Thread.currentThread().setName("PendingChangeSubmission")
            try {
                log.debug("inside executor task submission .. ${changeDocument.OID}")
                def contextObject = genericOIDService.resolveOID(changeDocument.OID)

                log.debug("Context object: ${contextObject}")
                contextObject?.notifyDependencies(changeDocument)
            }
            catch (Exception e) {
                log.error("Problem with event transmission for ${changeDocument.OID}" ,e)
            }
        })
    }

    @Deprecated
    def registerPendingChange(prop, target, desc, objowner, changeMap) {

        def msgToken = null
        def msgParams = null
        def legacyDesc = desc

        registerPendingChange(prop, target, objowner, changeMap, msgToken, msgParams, legacyDesc)
    }

    //def registerPendingChange(prop, target, desc, objowner, changeMap) << legacy
    @Deprecated
    /**
     * This method registers pending changes and is going to be kept because of backwards compatibility.
     * @deprecated Is going to be replaced by PendingChangeFactory ({@link PendingChange#construct()})
     */
    PendingChange registerPendingChange(String prop, def target, def objowner, def changeMap, String msgToken, def msgParams, String legacyDesc) {
        log.debug("Register pending change ${prop} ${target.class.name}:${target.id}")

        String desc = legacyDesc?.toString() // freeze string before altering referenced values

        def deltaTz = 0

        changeMap.changeDoc.each { k, v ->
            if (k in ['old', 'new']) {
                if (v instanceof Date || v instanceof Timestamp) {
                    v.setTime(v.getTime() + deltaTz)
                }
            }
        }
        def existsPendingChange = null
        //IF PENDING Change for PKG exists
        if (prop == PendingChange.PROP_PKG) {
            def payload = changeMap as JSON
            String changeDocNew = payload.toString()

            existsPendingChange = PendingChange.findWhere(
                    desc: desc,
                    oid: "${target.class.name}:${target.id}",
                    owner: objowner,
                    msgToken: msgToken,
                    msgParams: null,
                    pkg: target,
                    payload: changeDocNew,
            )
        }
        if (!existsPendingChange) {
            PendingChange new_pending_change = new PendingChange(
                    desc: desc,
                    oid: "${target.class.name}:${target.id}",
                    owner: objowner,
                    msgToken: msgToken,
                    ts: new Date()
            )

            new_pending_change[prop] = target;

            def payload = changeMap as JSON
            new_pending_change.payload = payload.toString()

            def jsonMsgParams = msgParams as JSON
            new_pending_change.msgParams = msgParams ? jsonMsgParams.toString() : null

            new_pending_change.workaroundForDatamigrate() // ERMS-2184

            if (new_pending_change.save(failOnError: true)) {
                return new_pending_change
            } else {
                log.error("Problem saving pending change: ${new_pending_change.errors}")
            }
            return null
        }
    }

    @Deprecated
    def determinePendingChangeBehavior(Map<String,Object> args, String msgToken, SubscriptionPackage subscriptionPackage) {
        println("determinePendingChangeBehavior")
        /*
            decision tree:
            is there a configuration map directly for the subscription?
                case one: if so: process as defined there
            - if not: is there a configuration map for the parent subscription?
                case two: if so: process as defined there
            - if neither: check if consortial subscription
                if so: case three - auto reject (because it is matter of survey)
                if not: case four - treat as prompt
         */
        Org contextOrg
        //consider collective later!
        if(subscriptionPackage) {
            if(subscriptionPackage.subscription.instanceOf)
                contextOrg = subscriptionPackage.subscription.getConsortia()
            else contextOrg = subscriptionPackage.subscription.getSubscriber()
            RefdataValue settingValue
            PendingChangeConfiguration directConf = subscriptionPackage.pendingChangeConfig.find { PendingChangeConfiguration pcc -> pcc.settingKey == msgToken}
            if(msgToken in [PendingChangeConfiguration.PACKAGE_PROP,PendingChangeConfiguration.PACKAGE_DELETED]) {
                if(directConf) {
                    if(directConf.withNotification)
                        PendingChange.construct([target:args.target,oid:args.oid,newValue:args.newValue,oldValue:args.oldValue,prop:args.prop,msgToken:msgToken,status:RDStore.PENDING_CHANGE_ACCEPTED,owner:contextOrg])
                }
                else {
                    SubscriptionPackage parentSP = SubscriptionPackage.findBySubscriptionAndPkg(subscriptionPackage.subscription.instanceOf, subscriptionPackage.pkg)
                    if(parentSP) {
                        PendingChangeConfiguration parentConf = parentSP.pendingChangeConfig.find { PendingChangeConfiguration pcc -> pcc.settingKey == msgToken }
                        if(parentConf && parentConf.withNotification)
                            PendingChange.construct([target:args.target,oid:args.oid,newValue:args.newValue,oldValue:args.oldValue,prop:args.prop,msgToken:msgToken,status:RDStore.PENDING_CHANGE_ACCEPTED,owner:contextOrg])
                    }
                }
            }
            else {
                if(directConf) {
                    //case one
                    settingValue = directConf.settingValue
                }
                else if(AuditConfig.getConfig(subscriptionPackage.subscription.instanceOf,msgToken)) {
                    //case two
                    List<RefdataValue> parentSPConfig = PendingChangeConfiguration.executeQuery('select pcc.settingValue from PendingChangeConfiguration pcc join pcc.subscriptionPackage sp where sp.subscription.instanceOf = :parent and sp.pkg = :pkg and pcc.settingKey = :msgToken',[parent:subscriptionPackage.subscription.instanceOf,pkg:subscriptionPackage.pkg,msgToken:msgToken])
                    if(parentSPConfig)
                        settingValue = parentSPConfig.get(0)
                }
                if((settingValue == null && !subscriptionPackage.subscription.instanceOf) || settingValue == RDStore.PENDING_CHANGE_CONFIG_PROMPT) {
                    //case four, then fallback or explicitly set as such
                    PendingChange.construct([target:args.target,oid:args.oid,newValue:args.newValue,oldValue:args.oldValue,prop:args.prop,msgToken:msgToken,status:RDStore.PENDING_CHANGE_PENDING,owner:contextOrg])
                }
                if(settingValue == RDStore.PENDING_CHANGE_CONFIG_ACCEPT) {
                    //set up announcement and do accept! Pending because if some error occurs, the notification should still take place
                    PendingChange pc = PendingChange.construct([target:args.target,oid:args.oid,newValue:args.newValue,oldValue:args.oldValue,prop:args.prop,msgToken:msgToken,status:RDStore.PENDING_CHANGE_PENDING,owner:contextOrg])
                    pendingChangeService.accept(pc)
                }
                if(settingValue == RDStore.PENDING_CHANGE_CONFIG_REJECT) {
                    PendingChange.construct([target:args.target,oid:args.oid,newValue:args.newValue,oldValue:args.oldValue,prop:args.prop,msgToken:msgToken,status:RDStore.PENDING_CHANGE_REJECTED,owner:contextOrg])
                }
                /*
                    else we have case three - a child subscription with no inherited settings ->
                    according to Micha as of March 16th, 2020, this means de facto that the holding manipulation should take place in a survey and
                    with that, the behavior can only be auto reject because the members need their current holding data as measurement for survey evaluation
                */
            }
        }
        else {
            if(msgToken == PendingChangeConfiguration.TITLE_DELETED) {
                IssueEntitlement ie = (IssueEntitlement) genericOIDService.resolveOID(args.oid)
                if(ie.subscription.instanceOf)
                    contextOrg = ie.subscription.getConsortia()
                else contextOrg = ie.subscription.getSubscriber()
                //set up announcement and do accept! Pending because if some error occurs, the notification should still take place
                PendingChange pc = PendingChange.construct([target:args.target,oid:args.oid,newValue:args.newValue,oldValue:args.oldValue,prop:args.prop,msgToken:msgToken,status:RDStore.PENDING_CHANGE_PENDING,owner:contextOrg])
                pendingChangeService.accept(pc)
            }
        }
    }

}
