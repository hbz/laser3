package com.k_int.kbplus
import de.laser.domain.AbstractBaseDomain
import groovy.json.JsonSlurper
import de.uni_freiburg.ub.IpRange
import de.uni_freiburg.ub.IpRangeCollection
import groovy.util.logging.*

@Log4j
class OrgAccessPoint extends AbstractBaseDomain {
    
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

    def getNotLinkedPlatforms()
    {
        // Platforms not in OrgAccessPointLink
        def hql = 'select p from Platform p where not exists (select 1 from OrgAccessPointLink oapl where oapl.platform = p and oapl.active = true)'
        return Platform.executeQuery(hql)
    }

    def getNotLinkedSubscriptions()
    {
        // Get not active subscriptions for the access point org
        def hql = "select sub from Subscription sub join sub.orgRelations as orgrel where orgrel.org.id = ${org.id} and not exists (select 1 from OrgAccessPointLink oapl where oapl.subscription = sub and oapl.active = true)"
        return License.executeQuery(hql)
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
