package de.laser.api.v0.entities

import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgAccessPoint
import de.laser.api.v0.*
import de.laser.helper.Constants
import de.laser.helper.RDStore
import grails.converters.JSON
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

import java.sql.Timestamp

class ApiOrgAccessPoint {

    /**
     * @return ApiBox(obj: CostItem | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
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
     * @return boolean
     */
    static boolean calculateAccess(OrgAccessPoint orgAccessPoint, Org context) {

        boolean hasAccess = false

        if (orgAccessPoint.org?.id == context.id) {
            hasAccess = true
        }

        hasAccess
    }
    /**
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
     * @return JSON | FORBIDDEN
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


