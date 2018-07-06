package com.k_int.kbplus

import de.laser.domain.BaseDomainComponent
import groovy.util.logging.Log4j

@Log4j
class PlatformAccessMethod extends BaseDomainComponent {

    Date validFrom
    Date validTo
    RefdataValue accessMethod       // RefdataCategory 'Access Method'
    Platform platf
    Date dateCreated
    Date lastUpdated
    
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
    
    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }
    
    
}
