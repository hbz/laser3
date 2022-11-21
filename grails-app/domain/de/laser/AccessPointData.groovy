package de.laser


import de.laser.base.AbstractBase
import de.laser.oap.OrgAccessPoint
import de.uni_freiburg.ub.IpRange
import groovy.json.JsonSlurper

/**
 * Container class for the access configuration.
 */
class AccessPointData extends AbstractBase {
    
    OrgAccessPoint orgAccessPoint
    String datatype
    String data

    Date dateCreated
    Date lastUpdated

    static transients = ['inputStr'] // mark read-only accessor methods

    static belongsTo = [
        orgAccessPoint:OrgAccessPoint
    ]
    
    static mapping = {
        id              column:'apd_id'
        version         column:'apd_version'

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
        lastUpdated (nullable: true)
    }

    /**
     * Gets the input string from the JSON access point data
     * @return the inputStr field of the data object
     */
    String getInputStr() {
        JsonSlurper jsonSluper = new JsonSlurper()
        return jsonSluper.parseText(data).getAt('inputStr')
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

    /**
     * Gets the IP range in the given format iff the access point data is IPv4 or IPv6
     * @param format one of the following formats: cidr, range, input
     * @return the IP range in the specified format; null, if datatype is not matching, empty string of format is invalid
     */
    String getIPString(String format) {
        if(datatype in ['ipv4', 'ipv6']) {
            JsonSlurper jsonSluper = new JsonSlurper()
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
