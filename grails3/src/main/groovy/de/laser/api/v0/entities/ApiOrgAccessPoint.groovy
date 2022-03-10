package de.laser.api.v0.entities

import de.laser.Org
import de.laser.oap.OrgAccessPoint
import de.laser.api.v0.*
import de.laser.helper.Constants
import grails.converters.JSON
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * An API representation of a {@link OrgAccessPoint}
 */
class ApiOrgAccessPoint {

    /**
     * Locates the given {@link OrgAccessPoint} and returns the object (or null if not found) and the request status for further processing
     * @param the field to look for the identifier, one of {id, globalUID}
     * @param the identifier value
     * @return ApiBox(obj: OrgAccessPoint | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
     * @see ApiBox#validatePrecondition_1()
     */
    static ApiBox findAccessPointBy(String query, String value) {
        ApiBox result = ApiBox.get()

        switch(query) {
            case 'id':
                result.obj = OrgAccessPoint.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result.obj = OrgAccessPoint.findAllWhere(globalUID: value)
                break
            default:
                result.status = Constants.HTTP_BAD_REQUEST
                return result
                break
        }
        result.validatePrecondition_1()


        result
    }

    /**
     * Checks if the requesting institution can access to the given access point
     * @param orgAccessPoint the {@link OrgAccessPoint} to which access is being requested
     * @param context the institution ({@link Org}) requesting access
     * @return true if the access is granted, false otherwise
     */
    static boolean calculateAccess(OrgAccessPoint orgAccessPoint, Org context) {

        boolean hasAccess = false

        if (orgAccessPoint.org?.id == context.id) {
            hasAccess = true
        }

        hasAccess
    }

    /**
     * Checks if the given institution can access the given access point. The access point
     * is returned in case of success
     * @param orgAccessPoint the {@link OrgAccessPoint} whose details should be retrieved
     * @param context the institution ({@link Org}) requesting the access point
     * @return JSON | FORBIDDEN
     */
    static requestOrgAccessPoint(OrgAccessPoint orgAccessPoint, Org context){
        Map<String, Object> result = [:]

        boolean hasAccess = calculateAccess(orgAccessPoint, context)
        if (hasAccess) {
            result = getOrgAccessPointMap(orgAccessPoint, context)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * Checks if the requesting institution can access the access point list of the requested institution.
     * The list of access points is returned in case of success
     * @param owner the institution whose cost items should be retrieved
     * @param context the institution who requests the list
     * @return JSON | FORBIDDEN
     * @see Org
     */
    static requestOrgAccessPointList(Org owner, Org context){
        Collection<Object> result = []

        boolean hasAccess = (owner.id == context.id)
        if (hasAccess) {
                result = OrgAccessPoint.findAllByOrg(owner).globalUID
                result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? (result ? new JSON(result) : null) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * Assembles the given access point attributes into a {@link Map}. The schema of the map can be seen in
     * schemas.gsp
     * @param orgAccessPoint the {@link OrgAccessPoint} which should be output
     * @param context the institution ({@link Org}) requesting
     * @return Map<String, Object>
     */
    static Map<String, Object> getOrgAccessPointMap(OrgAccessPoint orgAccessPoint, Org context){
        Map<String, Object> result = [:]

        orgAccessPoint = GrailsHibernateUtil.unwrapIfProxy(orgAccessPoint)

        result.globalUID = orgAccessPoint.globalUID

        result."${orgAccessPoint.accessMethod.value}" = [:]

        if(orgAccessPoint.accessMethod.value == 'ezproxy'){
          result."${orgAccessPoint.accessMethod.value}".name   = orgAccessPoint.name
          result."${orgAccessPoint.accessMethod.value}".proxyurl   = orgAccessPoint.url

           result."${orgAccessPoint.accessMethod.value}".ipv4ranges   = []

            orgAccessPoint.getIpRangeStrings('ipv4', 'cidr').each {
               result."${orgAccessPoint.accessMethod.value}".ipv4ranges   << it
            }

           result."${orgAccessPoint.accessMethod.value}".ipv6ranges   = []

            orgAccessPoint.getIpRangeStrings('ipv6', 'cidr').each {
               result."${orgAccessPoint.accessMethod.value}".ipv6ranges   << it
            }

        }

        if(orgAccessPoint.accessMethod.value == 'ip'){
           result."${orgAccessPoint.accessMethod.value}".name   = orgAccessPoint.name

           result."${orgAccessPoint.accessMethod.value}".ipv4ranges   = []
            orgAccessPoint.getIpRangeStrings('ipv4', 'cidr').each {
               result."${orgAccessPoint.accessMethod.value}".ipv4ranges   << it
            }

           result."${orgAccessPoint.accessMethod.value}".ipv6ranges   = []
            orgAccessPoint.getIpRangeStrings('ipv6', 'cidr').each {
               result."${orgAccessPoint.accessMethod.value}".ipv6ranges   << it
            }
        }

        if(orgAccessPoint.accessMethod.value == 'openathens'){
           result."${orgAccessPoint.accessMethod.value}".name   = orgAccessPoint.name
           result."${orgAccessPoint.accessMethod.value}".entityid   = orgAccessPoint.hasProperty('entityId') ? orgAccessPoint.entityId : ''
           result."${orgAccessPoint.accessMethod.value}".url   = orgAccessPoint.hasProperty('url') ? orgAccessPoint.url : ''

        }

        if(orgAccessPoint.accessMethod.value == 'proxy'){
           result."${orgAccessPoint.accessMethod.value}".name   = orgAccessPoint.name

           result."${orgAccessPoint.accessMethod.value}".ipv4ranges   = []
            orgAccessPoint.getIpRangeStrings('ipv4', 'cidr').each {
               result."${orgAccessPoint.accessMethod.value}".ipv4ranges   << it
            }

           result."${orgAccessPoint.accessMethod.value}".ipv6ranges   = []
            orgAccessPoint.getIpRangeStrings('ipv6', 'cidr').each {
               result."${orgAccessPoint.accessMethod.value}".ipv6ranges   << it
            }

        }

        if(orgAccessPoint.accessMethod.value == 'shibboleth'){
           result."${orgAccessPoint.accessMethod.value}".name   = orgAccessPoint.name
           result."${orgAccessPoint.accessMethod.value}".entityid   = orgAccessPoint.hasProperty('entityId') ? orgAccessPoint.entityId : ''
           result."${orgAccessPoint.accessMethod.value}".url   = orgAccessPoint.hasProperty('url') ? orgAccessPoint.url : ''
        }

        ApiToolkit.cleanUp(result, true, true)
    }
}


