package de.laser.oap

import de.laser.Org
import de.laser.Subscription
import de.laser.AccessPointData
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.base.AbstractBase
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.annotations.RefdataAnnotation
import de.uni_freiburg.ub.IpRange
import de.uni_freiburg.ub.IpRangeCollection
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

/**
 * An access configuration for an organisation. It defined conditions of access the organisation has met to use certain kinds of resources; those may be platforms or subscription packages.
 * They are specified in the {@link OrgAccessPointLink} domain.
 * The condition itself i.e. what conditions (e.g. IPv4/IPv6 ranges, EZProxy/Proxy/Shibboleth/OpenAthens configurations etc.) are needed to be met are specified in one or more {@link AccessPointData} objects.
 */
@Slf4j
class OrgAccessPoint extends AbstractBase {

    String name
    Org org
    Date dateCreated
    Date lastUpdated

    def orgTypeService

    @RefdataAnnotation(cat = RDConstants.ACCESS_POINT_TYPE)
    RefdataValue accessMethod

    static belongsTo = [
        Org
    ]

    static hasMany = [
            accessPointData : AccessPointData,
            oapp: OrgAccessPointLink
    ]

    static transients = ['notLinkedPlatforms', 'notLinkedSubscriptions', 'accessPointIpRanges'] // mark read-only accessor methods
    
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

    String[] getIpRangeStrings(String datatype, String format) {

        JsonSlurper jsonSluper = new JsonSlurper()
        IpRangeCollection ipRanges = new IpRangeCollection()

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
                return ipRanges.toRangeStrings()
                //TODO: NOCHMAL PRÜFEN MOE. ERMS-3975: 500er bei Zugriff auf Zugangskonfiguration
                //return ipRanges.compact().toRangeStrings()
                break
            case 'input':
                return ipRanges.toInputStrings().sort{it}
                break
            default:
                return []
        }
    }

    def getNotLinkedPlatforms()
    {
        List currentSubIds = orgTypeService.getCurrentSubscriptionIds(org)
        // TODO check if this is enough
        if (!currentSubIds){
            return
        }
        String qry = "select distinct p from SubscriptionPackage subPkg join subPkg.subscription s join subPkg.pkg pkg, " +
            "TitleInstancePackagePlatform tipp join tipp.platform p " +
            "where tipp.pkg = pkg and s.id in (:currentSubIds) " +
            " and not exists (select 1 from OrgAccessPointLink oapl where oapl.platform = p and oapl.active = true and oapl.oap = :orgAccessPoint) "

        qry += " and ((pkg.packageStatus is null) or (pkg.packageStatus != :pkgDeleted))"
        qry += " and ((p.status is null) or (p.status != :platformDeleted))"
        qry += " and ((tipp.status is null) or (tipp.status != :tippDeleted))"
        qry += " order by p.normname asc"

        def qryParams = [
            currentSubIds: currentSubIds,
            pkgDeleted: RDStore.PACKAGE_STATUS_DELETED,
            platformDeleted: RDStore.PLATFORM_STATUS_DELETED,
            tippDeleted: RDStore.TIPP_STATUS_DELETED,
            orgAccessPoint: this
        ]

        def result = Subscription.executeQuery(qry, qryParams)

        return result
    }

    def getNotLinkedSubscriptions()
    {
        def notAllowedSubscriptionStatusList  = ['Deleted', 'Expired', 'Terminated', 'No longer usable', 'Rejected']
        def statusList = []
        notAllowedSubscriptionStatusList.each { statusList += RDStore.getRefdataValue(it, RDConstants.SUBSCRIPTION_STATUS)}
        // Get not active subscriptions for the access point org
        def hql = "select sub from Subscription sub join sub.orgRelations as orgrel where orgrel.org.id = ${org.id} and sub.status not in (:status) and not exists (select 1 from OrgAccessPointLink oapl where oapl.subscription = sub and oapl.active = true) order by sub.name asc"
        return Subscription.executeQuery(hql, [status: statusList])
    }

    boolean hasActiveLink() {
        boolean active = false
        def oapps = this.oapp
        oapps.each {
            if (it.active){
                active = true
            }
        }
        active
    }

    Map<String, Object> getAccessPointIpRanges() {
        Map<String, Object> accessPointIpRanges = [:]

        accessPointIpRanges.ipv4Ranges = []
        accessPointIpRanges.ipv6Ranges = []


        accessPointData.each { accPD ->

            Map apd = [:]

            if (accPD.datatype == 'ipv4') {
                apd.id = accPD.id
                apd.name = name
                apd.ipRange = accPD.getIPString('range')
                apd.ipCidr = accPD.getIPString('cidr')
                apd.ipInput = accPD.getIPString('input')
                accessPointIpRanges.ipv4Ranges << apd
            }
            if (accPD.datatype == 'ipv6') {
                apd.id = accPD.id
                apd.name = name
                apd.ipRange = accPD.getIPString('range')
                apd.ipCidr = accPD.getIPString('cidr')
                apd.ipInput = accPD.getIPString('input')
                accessPointIpRanges.ipv6Ranges << apd
            }
        }

        accessPointIpRanges.ipv4Ranges = accessPointIpRanges.ipv4Ranges.sort{it.ipInput}
        accessPointIpRanges.ipv6Ranges = accessPointIpRanges.ipv6Ranges.sort{it.ipInput}

        accessPointIpRanges

    }
}
