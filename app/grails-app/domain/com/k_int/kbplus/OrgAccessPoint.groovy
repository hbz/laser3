package com.k_int.kbplus
import de.laser.domain.AbstractBaseDomain
import de.laser.helper.RDStore
import de.laser.helper.RefdataAnnotation
import groovy.json.JsonSlurper
import de.uni_freiburg.ub.IpRange
import de.uni_freiburg.ub.IpRangeCollection
import groovy.util.logging.*

@Log4j
class OrgAccessPoint extends AbstractBaseDomain {
    
    String name
    Org org
    Date dateCreated
    Date lastUpdated

    def orgTypeService

    @RefdataAnnotation(cat = '?')
    RefdataValue accessMethod

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
        List currentSubIds = orgTypeService.getCurrentSubscriptions(org).collect{ it.id }

        String qry = "select distinct p from SubscriptionPackage subPkg join subPkg.subscription s join subPkg.pkg pkg, " +
            "TitleInstancePackagePlatform tipp join tipp.platform p " +
            "where tipp.pkg = pkg and s.id in (:currentSubIds) " +
            " and not exists (select 1 from OrgAccessPointLink oapl where oapl.platform = p and oapl.active = true)"
        qry += " and ((pkg.packageStatus is null) or (pkg.packageStatus != :pkgDeleted))"
        qry += " and ((p.status is null) or (p.status != :platformDeleted))"
        qry += " and ((tipp.status is null) or (tipp.status != :tippDeleted))"
        qry += " order by p.normname asc"

        def qryParams = [
            currentSubIds: currentSubIds,
            pkgDeleted: RDStore.PACKAGE_DELETED,
            platformDeleted: RDStore.PLATFORM_DELETED,
            tippDeleted: RDStore.TIPP_DELETED
        ]

        return Subscription.executeQuery(qry, qryParams)
    }

    def getNotLinkedSubscriptions()
    {
        def notAllowedSubscriptionStatusList  = ['Deleted', 'Expired', 'Terminated', 'No longer usable', 'Rejected', 'ExpiredPerennial']
        def statusList = []
        notAllowedSubscriptionStatusList.each { statusList += RDStore.getRefdataValue(it, 'Subscription Status')}
        // Get not active subscriptions for the access point org
        def hql = "select sub from Subscription sub join sub.orgRelations as orgrel where orgrel.org.id = ${org.id} and sub.status not in (:status) and not exists (select 1 from OrgAccessPointLink oapl where oapl.subscription = sub and oapl.active = true) order by sub.name asc"
        return Subscription.executeQuery(hql, [status: statusList])
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
