package de.laser


import de.laser.base.AbstractBase
import de.laser.storage.RDConstants
import de.laser.annotations.RefdataAnnotation
import groovy.util.logging.Slf4j

/**
 * An access method to a given {@link Platform}; the given access method may be restricted to a certain timespan. The access method itself is one value of the controlled list of access methods
 * @see RDConstants#ACCESS_METHOD
 * @see Platform
 */
@Slf4j
class PlatformAccessMethod extends AbstractBase {

    Date validFrom
    Date validTo
    Platform platf
    Date dateCreated
    Date lastUpdated

    @RefdataAnnotation(cat = RDConstants.ACCESS_METHOD)
    RefdataValue accessMethod

    static belongsTo = [
        platf:Platform,
    ]
    
    static mapping = {
        globalUID       column:'plat_guid'
        validFrom       column:'pam_valid_from'
        validTo         column:'pam_valid_to'
        accessMethod    column:'pam_access_method_rv_fk'
        platf           column:'pam_platf_fk'
    }
    
    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        validFrom(nullable: true)
        validTo(nullable: true)
  }
    
    static List<RefdataValue> getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }

    @Override
    def beforeInsert() {
        super.beforeInsertHandler()
    }
    @Override
    def beforeUpdate() {
        super.beforeUpdateHandler()
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }
}
