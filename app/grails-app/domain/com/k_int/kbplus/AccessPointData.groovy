package com.k_int.kbplus

import de.laser.domain.BaseDomainComponent
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j
import ubfr.IpRange
import ubfr.IpRangeCollection

@Log4j
class AccessPointData extends BaseDomainComponent {
    
    OrgAccessPoint orgAccessPoint
    String datatype
    String data

    static belongsTo = [
        orgAccessPoint:OrgAccessPoint
    ]
    
    static mapping = {
        globalUID       column:'apd_guid'
        orgAccessPoint  column:'apd_org_access_point_fk'
        datatype        column:'apd_datatype'
        data            column:'apd_data'
    }
    
    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255) 
        datatype(nullable:true, blank:true)
        data(nullable:true, blank:true)
  }
    
    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }

    String getInputStr() {
        def jsonSluper = new JsonSlurper();
        return jsonSluper.parseText(data).getAt('inputStr');
    }

}
