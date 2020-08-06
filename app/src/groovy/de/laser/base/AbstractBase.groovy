package de.laser.base

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 *  class Test extends AbstractBase
 *
 *  static mapping     = { globalUID column:'test_guid' .. }
 *  static constraints = { globalUID(nullable:true, blank:false, unique:true, maxSize:255) .. }
 *
 */

abstract class AbstractBase {

    String globalUID

    static Log static_logger = LogFactory.getLog(AbstractBase)

    void setGlobalUID() {

        if (! globalUID) {
            UUID uid = UUID.randomUUID()
            String scn = this.getClass().getSimpleName().toLowerCase()

            globalUID = scn + ":" + uid.toString()
        }
    }

    protected void beforeInsertHandler() {

        static_logger.debug("beforeInsertHandler()")

        if (! globalUID) {
            setGlobalUID()
        }
    }

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

        static_logger.debug("beforeUpdateHandler() " + changes.toMapString())
        return changes
    }

    protected void beforeDeleteHandler() {

        static_logger.debug("beforeDeleteHandler()")
    }

    abstract def beforeInsert() /* { beforeInsertHandler() } */

    abstract def beforeUpdate() /* { beforeUpdateHandler() } */

    abstract def beforeDelete() /* { beforeDeleteHandler() } */
}
