package de.laser.base

import groovy.util.logging.Slf4j

/**
 *  class Test extends AbstractBase
 *
 *  static mapping     = { globalUID column:'test_guid' .. }
 *  static constraints = { globalUID(nullable:true, blank:false, unique:true, maxSize:255) .. }
 *
 */

@Slf4j
abstract class AbstractBase {

    String globalUID

    void setGlobalUID() {

        if (! globalUID) {
            UUID uid = UUID.randomUUID()
            String scn = this.getClass().getSimpleName().toLowerCase()

            globalUID = scn + ":" + uid.toString()
        }
    }

    protected void beforeInsertHandler() {

        log.debug("beforeInsertHandler()")

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

        log.debug("beforeUpdateHandler() " + changes.toMapString())
        return changes
    }

    protected void beforeDeleteHandler() {

        log.debug("beforeDeleteHandler()")
    }

    abstract def beforeInsert() /* { beforeInsertHandler() } */

    abstract def beforeUpdate() /* { beforeUpdateHandler() } */

    abstract def beforeDelete() /* { beforeDeleteHandler() } */
}
