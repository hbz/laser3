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

    protected def beforeInsertHandler() {
        if (! globalUID) {
            setGlobalUID()
        }
    }
    protected def beforeUpdateHandler() {
        if (! globalUID) {
            setGlobalUID()
        }
    }
    protected def beforeDeleteHandler() {
    }

    abstract def beforeInsert() /* { beforeInsertHandler() } */

    abstract def beforeUpdate() /* { beforeUpdateHandler() } */

    abstract def beforeDelete() /* { beforeDeleteHandler() } */
}
