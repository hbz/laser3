package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.base.AbstractBase
import de.laser.storage.RDConstants

/**
 * An access method to a given {@link Platform}; the given access method may be restricted to a certain timespan. The access method itself is one value of the controlled list of access methods
 * @see RDConstants#ACCESS_METHOD
 * @see Platform
 */
class PlatformAccessMethod extends AbstractBase {

    Date validFrom
    Date validTo
    Platform platf
    Date dateCreated
    Date lastUpdated

    @RefdataInfo(cat = RDConstants.ACCESS_METHOD)
    RefdataValue accessMethod

    static belongsTo = [
        platf:Platform,
    ]
    
    static mapping = {
        id              column:'pam_id'
        version         column:'pam_version'
        globalUID       column:'pam_guid'
        validFrom       column:'pam_valid_from'
        validTo         column:'pam_valid_to'
        accessMethod    column:'pam_access_method_rv_fk'
        platf           column:'pam_platf_fk'
        dateCreated     column:'pam_date_created'
        lastUpdated     column:'pam_last_updated'
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
