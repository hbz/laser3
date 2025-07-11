package de.laser.base

import de.laser.utils.RandomUtils
import groovy.util.logging.Slf4j

/**
 *  This base class contains principal methods common to all classes which implement laserID storage for an
 *  eventual data exchange.
 *
 *  implementation guide for implementing classes:
 *  class Test extends AbstractBase
 *
 *  static mapping     = { laserID column:'test_guid' .. }
 *  static constraints = { laserID(nullable:true, blank:false, unique:true, maxSize:255) .. }
 *
 */
@Slf4j
abstract class AbstractBase {

    String laserID

    /**
     * Sets the Laser-ID of the object. The Laser-ID is in the structure className:UUID
     */
    void setLaserID() {

        if (! laserID) {
            String scn = this.getClass().getSimpleName().toLowerCase()

            laserID = scn + ":" + RandomUtils.getUUID()
        }
    }

    /**
     * Before the object is being persisted, the Laser-ID is being created if not called manually before
     */
    protected void beforeInsertHandler() {

        //log.debug("beforeInsertHandler()")

        if (! laserID) {
            setLaserID()
        }
    }

    /**
     * Before each update, set the Laser-ID if it does not exist and output the changes to be persisted
     * @return a {@link Map} reflecting the changes done on the object
     */
    protected Map<String, Object> beforeUpdateHandler() {

        if (! laserID) {
            setLaserID()
        }
        Map<String, Object> changes = [
                oldMap: [:],
                newMap: [:]
        ]
        this.getDirtyPropertyNames().each { prop ->
            changes.oldMap.put( prop, this.getPersistentValue(prop) )
            changes.newMap.put( prop, this.getProperty(prop) )
        }

        log.debug("beforeUpdateHandler() " + this.getDirtyPropertyNames())
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
