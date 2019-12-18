package com.k_int.kbplus

import de.laser.domain.AbstractBaseDomain
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j

@Log4j
class AccessPointData extends AbstractBaseDomain {
    
    OrgAccessPoint orgAccessPoint
    String datatype
    String data

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
        orgAccessPoint:OrgAccessPoint
    ]
    
    static mapping = {
        globalUID       column:'apd_guid'
        orgAccessPoint  column:'apd_org_access_point_fk'
        datatype        column:'apd_datatype'
        data            column:'apd_data'

        lastUpdated     column: 'apd_last_updated'
        dateCreated     column: 'apd_date_created'
    }
    
    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255) 
        datatype(nullable:true, blank:true)
        data(nullable:true, blank:true)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }
    
    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }

    String getInputStr() {
        def jsonSluper = new JsonSlurper();
        return jsonSluper.parseText(data).getAt('inputStr');
    }

}
