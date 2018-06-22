package com.k_int.kbplus
import de.laser.domain.BaseDomainComponent
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
    Org organisation
    RefdataValue accessMethod

    static belongsTo = [
        org:Org
    ]

    static hasMany = [
            accessPointData : AccessPointData
    ]
    
    static mapping = {
        globalUID       column:'oar_guid'
        name            column:'oar_name'
        org             column:'oar_organisation_fk'
        accessMethod    column:'oar_access_method_rv_fk'
    }
    
    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        name(unique: ['organisation'])
  }
    
    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }

    String[] getIpv4Cidr() {

        IpRangeCollection ipRanges = new IpRangeCollection();
        for (data in accessPointData) {
            IpRange ipRange = IpRange.parseIpRange(data.data);
            ipRanges.add(ipRange)
        }
        ipRanges = ipRanges.compact();

        return ipRanges.toCidr()
    }

    String[] getIpv6Cidr() {
        return [""];
    }


}
