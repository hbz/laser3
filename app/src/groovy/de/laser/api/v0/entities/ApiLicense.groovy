package de.laser.api.v0.entities

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import de.laser.helper.Constants
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiReaderHelper
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiLicense {

    /**
     * @return License | BAD_REQUEST | PRECONDITION_FAILED
     */
    static findLicenseBy(String query, String value) {
        def result

        switch(query) {
            case 'id':
                result = License.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result = License.findAllWhere(globalUID: value)
                break
            case 'impId':
                result = License.findAllWhere(impId: value)
                break
            case 'ns:identifier':
                result = Identifier.lookupObjectsByIdentifierString(new License(), value)
                break
            default:
                return Constants.HTTP_BAD_REQUEST
                break
        }
        if (result) {
            result = result.size() == 1 ? result.get(0) : Constants.HTTP_PRECONDITION_FAILED
        }
        result
    }


    /**
     * @return boolean
     */
    static boolean calculateAccess(License lic, Org context, boolean hasAccess) {

        if (! hasAccess) {
            if (OrgRole.findByLicAndRoleTypeAndOrg(lic, RDStore.OR_LICENSING_CONSORTIUM, context)) {
                hasAccess = true
            }
            else if (OrgRole.findByLicAndRoleTypeAndOrg(lic, RDStore.OR_LICENSEE, context)) {
                hasAccess = true
            }
            else if (OrgRole.findByLicAndRoleTypeAndOrg(lic, RDStore.OR_LICENSEE_CONS, context)) {
                hasAccess = true
            }
        }

        hasAccess
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static getLicense(License lic, Org context, boolean hasAccess){
        Collection<Object> result = []
        hasAccess = calculateAccess(lic, context, hasAccess)

        if (hasAccess) {
            result = ApiReader.retrieveLicenseMap(lic, ApiReaderHelper.IGNORE_NONE, context)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return JSON
     */
    static JSON getLicenseList(Org owner, Org context, boolean hasAccess){
        Collection<Object> result = []

        List<License> available = License.executeQuery(
                'SELECT lic FROM License lic JOIN lic.orgLinks oo WHERE oo.org = :owner AND oo.roleType in (:roles ) AND lic.status != :del' ,
                [
                        owner: owner,
                        roles: [RDStore.OR_LICENSING_CONSORTIUM, RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE],
                        del:   RDStore.LICENSE_DELETED
                ]
        )

        available.each { lic ->
            //if (calculateAccess(lic, context, hasAccess)) {
                //println lic.id + ' ' + lic.reference
                result.add(ApiReaderHelper.requestLicenseStub(lic, context, true))
                //result.add([globalUID: lic.globalUID])
            //}
        }

        return (result ? new JSON(result) : null)
    }
}
