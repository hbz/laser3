package de.laser.oap

import de.laser.AccessPointData
import de.laser.Org
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.annotations.RefdataInfo
import de.laser.base.AbstractBase
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.uni_freiburg.ub.IpRange
import de.uni_freiburg.ub.IpRangeCollection
import groovy.json.JsonSlurper

/**
 * An access configuration for an organisation. It defined conditions of access the organisation has met to use certain kinds of resources; those may be platforms or subscription packages.
 * They are specified in the {@link OrgAccessPointLink} domain.
 * The condition itself i.e. what conditions (e.g. IPv4/IPv6 ranges, EZProxy/Proxy/Shibboleth/OpenAthens configurations, email domains etc.) are needed to be met are specified in one or more {@link AccessPointData} objects.
 */
class OrgAccessPoint extends AbstractBase {

    String name
    Org org
    String note
    Date dateCreated
    Date lastUpdated

    @RefdataInfo(cat = RDConstants.ACCESS_POINT_TYPE)
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
        id              column:'oar_id'
        version         column:'oar_version'
        name            column:'oar_name'
        org             column:'oar_org_fk'
        note            column:'oar_note', type: 'text'
        globalUID       column:'oar_guid'
        accessMethod    column:'oar_access_method_rv_fk'
        dateCreated     column:'oar_date_created'
        lastUpdated     column:'oar_last_updated'
    }
    
    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        note(nullable: true)
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

    /**
     * Retrieves the IP range strings of the given type in the specified format.
     * @param datatype the type of address (IPv4 or IPv6) to output
     * @param format the format in which the output should be listed. Valid formats are: cidr, ranges, input
     * @return an array of strings containing the ranges
     */
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

    /**
     * Gets all platforms for this access point which are not linked to any subscription
     * @return a {@link List} of {@link de.laser.Platform}s not linked anywhere
     */
    def getNotLinkedPlatforms()
    {
        List currentSubIds = BeanStore.getOrgTypeService().getCurrentSubscriptionIds(org)
        // TODO check if this is enough
        if (!currentSubIds){
            return
        }
        String qry = "select distinct p from SubscriptionPackage subPkg join subPkg.subscription s join subPkg.pkg pkg join pkg.nominalPlatform p " +
            "where s.id in (:currentSubIds) " +
            " and not exists (select 1 from OrgAccessPointLink oapl where oapl.platform = p and oapl.active = true and oapl.oap = :orgAccessPoint) "

        qry += " and ((pkg.packageStatus is null) or (pkg.packageStatus != :pkgDeleted))"
        qry += " and ((p.status is null) or (p.status != :platformDeleted))"
        qry += " order by p.normname asc"

        Map<String, Object> qryParams = [
            currentSubIds: currentSubIds,
            pkgDeleted: RDStore.PACKAGE_STATUS_DELETED,
            platformDeleted: RDStore.PLATFORM_STATUS_DELETED,
            orgAccessPoint: this
        ]

        Subscription.executeQuery(qry, qryParams)
    }

    /**
     * Gets all {@link Subscription}s which are not linked to the given access point
     * @return a {@link List} of {@link Subscription}s without any access point link
     */
    def getNotLinkedSubscriptions()
    {
        List<String> notAllowedSubscriptionStatusList  = ['Deleted', 'Expired', 'Terminated', 'No longer usable', 'Rejected']
        List statusList = []
        notAllowedSubscriptionStatusList.each { statusList += RDStore.getRefdataValue(it, RDConstants.SUBSCRIPTION_STATUS)}
        // Get not active subscriptions for the access point org
        String hql = "select sub from Subscription sub join sub.orgRelations as orgrel where orgrel.org.id = ${org.id} and sub.status not in (:status) and not exists (select 1 from OrgAccessPointLink oapl where oapl.subscription = sub and oapl.active = true) order by sub.name asc"
        Subscription.executeQuery(hql, [status: statusList])
    }

    /**
     * Used in _apLinkContent.gsp
     * Checks if the access point has an active link
     * @return true if there is at least one active access point
     */
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

    /**
     * Collects all the IP ranges for the given access point and returns sets of the IPv4 and IPv6 ranges
     * @return a {@link Map} of structure {ipv4Ranges: [], ipv6Ranges: []} containing the defined ranges of the access point
     */
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

    Map<String, Object> getAccessPointMailDomains() {
        Map<String, Object> accessPoints = [:]

        accessPoints.accessPointMailDomains = []

        accessPointData.each { accPD ->
            Map apd = [:]
            if (accPD.datatype == 'mailDomain') {
                apd.id = accPD.id
                apd.name = name
                apd.mailDomain = accPD.data
                accessPoints.accessPointMailDomains << apd
            }
        }

        accessPoints.accessPointMailDomains = accessPoints.accessPointMailDomains.sort{it.mailDomain}

        accessPoints
    }
}
