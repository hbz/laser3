package com.k_int.kbplus
import de.laser.domain.BaseDomainComponent
import groovy.json.JsonSlurper
import ubfr.IpRange
import ubfr.IpRangeCollection

import javax.persistence.Transient
import groovy.util.logging.Log4j
import org.apache.commons.logging.LogFactory
import com.sun.org.apache.xalan.internal.xsltc.compiler.Sort
import groovy.util.logging.*

@Log4j
class OrgAccessPoint extends BaseDomainComponent {
    
    String name
    Org org
    RefdataValue accessMethod
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
        org:Org
    ]

    static hasMany = [
            accessPointData : AccessPointData,
            oapp: OrgAccessPointLink
    ]
    
    static mapping = {
        globalUID       column:'oar_guid'
        name            column:'oar_name'
        org             column:'oar_org_fk'
        accessMethod    column:'oar_access_method_rv_fk'
    }
    
    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        name(unique: ['org'])
  }
    
    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }

    def String[] getIpRangeStrings(String datatype, String format) {

        def jsonSluper = new JsonSlurper()
        def ipRanges = new IpRangeCollection()

        for (data in accessPointData) {
            if (data.datatype == datatype) {
                def o = jsonSluper.parseText(data.data)
                IpRange ipRange = IpRange.parseIpRange(o.getAt('inputStr'))
                ipRanges.add(ipRange)
            }
        }

        switch (format) {
            case 'cidr':
                return ipRanges.compact().toCidrStrings()
                break
            case 'ranges':
                return ipRanges.compact().toRangeStrings()
                break
            case 'input':
                return ipRanges.toInputStrings()
                break
            default:
                return []
        }
    }

    def hasActiveLink() {
        def active = false
        def oapps = this.oapp
        oapps.each {
            if (it.active){
                active = true
            }
        }
        active
    }
}
