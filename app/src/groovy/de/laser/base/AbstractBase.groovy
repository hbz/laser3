package de.laser.base

/**
 *  class Test extends AbstractBase
 *
 *  static mapping     = { globalUID column:'test_guid' .. }
 *  static constraints = { globalUID(nullable:true, blank:false, unique:true, maxSize:255) .. }
 *
 */

abstract class AbstractBase {

    String globalUID

    def setGlobalUID() {
        if (! globalUID) {
            UUID uid = UUID.randomUUID()
            String scn = this.getClass().getSimpleName().toLowerCase()

            globalUID = scn + ":" + uid.toString()
        }
    }

    protected beforeInsertHandler() {
        if (! globalUID) {
            setGlobalUID()
        }
    }
    protected beforeUpdateHandler() {
        if (! globalUID) {
            setGlobalUID()
        }
    }

    abstract def beforeInsert() /* { beforeInsertHandler() } */

    abstract def beforeUpdate() /* { beforeUpdateHandler() } */
}
