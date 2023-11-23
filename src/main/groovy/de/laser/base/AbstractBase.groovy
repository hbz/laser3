package de.laser.base

import groovy.util.logging.Slf4j

/**
 *  This base class contains principal methods common to all classes which implement globalUID storage for an
 *  eventual data exchange.
 *
 *  implementation guide for implementing classes:
 *  class Test extends AbstractBase
 *
 *  static mapping     = { globalUID column:'test_guid' .. }
 *  static constraints = { globalUID(nullable:true, blank:false, unique:true, maxSize:255) .. }
 *
 */
@Slf4j
abstract class AbstractBase {

    String globalUID

    /**
     * Sets the global UID of the object. The global UID is in the structure className:UUID
     */
    void setGlobalUID() {

        if (! globalUID) {
            UUID uid = UUID.randomUUID()
            String scn = this.getClass().getSimpleName().toLowerCase()

            globalUID = scn + ":" + uid.toString()
        }
    }

    /**
     * Before the object is being persisted, the global UID is being created if not called manually before
     */
    protected void beforeInsertHandler() {

        log.debug("beforeInsertHandler()")

        if (! globalUID) {
            setGlobalUID()
        }
    }

    /**
     * Before each update, set the global UID if it does not exist and output the changes to be persisted
     * @return a {@link Map} reflecting the changes done on the object
     */
    protected Map<String, Object> beforeUpdateHandler() {

        if (! globalUID) {
            setGlobalUID()
        }
        Map<String, Object> changes = [
                oldMap: [:],
                newMap: [:]
        ]
        this.getDirtyPropertyNames().each { prop ->
            changes.oldMap.put( prop, this.getPersistentValue(prop) )
            changes.newMap.put( prop, this.getProperty(prop) )
        }

        log.debug("beforeUpdateHandler() " + changes.toMapString())
        return changes
    }

    /**
     * Empty delete handler
     */
    protected void beforeDeleteHandler() {

        //log.debug("beforeDeleteHandler()")
    }

    abstract def beforeInsert() /* { beforeInsertHandler() } */

    abstract def beforeUpdate() /* { beforeUpdateHandler() } */

    abstract def beforeDelete() /* { beforeDeleteHandler() } */
}
