package com.k_int.kbplus

import de.laser.base.AbstractBase
import de.uni_freiburg.ub.IpRange
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j

@Log4j
class AccessPointData extends AbstractBase {
    
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
    
    static List<RefdataValue> getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }

    String getInputStr() {
        def jsonSluper = new JsonSlurper();
        return jsonSluper.parseText(data).getAt('inputStr');
    }

    @Override
    def beforeInsert() {
        super.beforeInsertHandler()
    }
    @Override
    def beforeUpdate() {
        super.beforeUpdateHandler()
    }

    String getIPString(String format) {
        if(datatype in ['ipv4', 'ipv6']) {
            def jsonSluper = new JsonSlurper();
            def o = jsonSluper.parseText(data)
            IpRange ipRange = IpRange.parseIpRange(o.getAt('inputStr'))

            switch (format) {
                case 'cidr':
                    return ipRange.toCidr().join(', ')
                    break
                case 'range':
                    return ipRange.toRangeString()
                    break
                case 'input':
                    return ipRange.toInputString()
                    break
                default:
                    return ''
            }
        }
    }
}
