package com.k_int.kbplus
import de.laser.domain.BaseDomainComponent

import javax.persistence.Transient
import groovy.util.logging.Log4j
import org.apache.commons.logging.LogFactory
import com.sun.org.apache.xalan.internal.xsltc.compiler.Sort
import groovy.util.logging.*

@Log4j
class OrgAccessPoint extends BaseDomainComponent {
    
    String name
    Org organisation
    RefdataValue accessMethod
    RefdataValue dataType       // RefdataCategory 'Access Mode'
    String data
    Date validFrom
    Date validTo
    
    static belongsTo = [
        org:Org
    ]
    
    static mapping = {
        globalUID       column:'oar_guid'
        name            column:'oar_name'
        org             column:'oar_organisation_fk'
        accessMethod    column:'oar_access_method_rv_fk'
        dataType        column:'oar_data_type_rv_fk'
        data            column:'oar_data'
        validFrom       column:'oar_valid_from'
        validTo         column:'oar_valid_to'
    }
    
    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        dataType(nullable:true, blank:true)
        data(nullable:true, blank:true)
        validFrom(nullable:true, blank:true)
        validTo(nullable:true, blank:true)
  }
    
    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }
    
    
}
