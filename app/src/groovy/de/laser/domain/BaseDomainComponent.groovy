package de.laser.domain

/**
 *  class Test extends BaseDomainComponent
 *
 *  static mapping     = { globalUID column:'test_guid' .. }
 *  static constraints = { globalUID(nullable:true, blank:false, unique:true, maxSize:256) .. }
 *
 *  def beforeInsert() { ..; super.beforeInsert() }
 *  def beforeUpdate() { ..; super.beforeUpdate() }
 *
 */
abstract class BaseDomainComponent {

    String globalUID

    def setGlobalUID() {
        if (! globalUID) {
            def uid = UUID.randomUUID()
            def scn = this.getClass().getSimpleName().toLowerCase()

            globalUID = scn + ":" + uid
        }
    }

    def beforeInsert() {
        if (! globalUID) {
            setGlobalUID()
        }
    }

    def beforeUpdate() {
        if (! globalUID) {
            setGlobalUID()
        }
    }
}
