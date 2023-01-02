package de.laser


import de.laser.base.AbstractLockableService
import de.laser.storage.RDStore
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
@Deprecated
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
    @Deprecated
    def fireEvent(Map<String, Object> changeDocument) {
        log.debug("fireEvent(${changeDocument})")

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

}
