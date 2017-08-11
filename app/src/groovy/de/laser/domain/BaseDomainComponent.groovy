package de.laser.domain

/**
 *  class Test extends BaseDomainComponent
 *
 *  static mapping     = { globalUID column:'test_guid' .. }
 *  static constraints = { globalUID(nullable:true, blank:false, unique:true, maxSize:255) .. }
 *
 *  def beforeInsert() { ..; super.beforeInsert() }
 *  def beforeUpdate() { ..; super.beforeUpdate() }
 *
 *  
 *  maxSize:255 ; to avoid the following problem:
 *  Specified key was too long; max key length is 767 bytes
 *
 *  => MySQL has a prefix limitation of 767 bytes in InnoDB
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
