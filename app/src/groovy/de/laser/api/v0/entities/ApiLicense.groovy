package de.laser.api.v0.entities

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import de.laser.api.v0.ApiBox
import de.laser.api.v0.ApiCollectionReader
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiStubReader
import de.laser.api.v0.ApiToolkit
import de.laser.helper.Constants
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class ApiLicense {

    /**
     * @return ApiBox(obj: License | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND )
     */
    static ApiBox findLicenseBy(String query, String value) {
        ApiBox result = ApiBox.get()

        switch(query) {
            case 'id':
                result.obj = License.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result.obj = License.findAllWhere(globalUID: value)
                break
            case 'ns:identifier':
                result.obj = Identifier.lookupObjectsByIdentifierString(new License(), value)
                break
            default:
                result.status = Constants.HTTP_BAD_REQUEST
                return result
                break
        }
        result.validatePrecondition_1()

        //if (result.obj instanceof License) {
        //    result.validateDeletedStatus_2('status', RDStore.LICENSE_DELETED)
        //}
        result
    }


    /**
     * @return boolean
     */
    static boolean calculateAccess(License lic, Org context) {

        boolean hasAccess = false

        if (! lic.isPublicForApi) {
            hasAccess = false
        }
        else if (OrgRole.findByLicAndRoleTypeAndOrg(lic, RDStore.OR_LICENSING_CONSORTIUM, context)) {
            hasAccess = true
        }
        else if (OrgRole.findByLicAndRoleTypeAndOrg(lic, RDStore.OR_LICENSEE, context)) {
            hasAccess = true
        }
        else if (OrgRole.findByLicAndRoleTypeAndOrg(lic, RDStore.OR_LICENSEE_CONS, context)) {
            hasAccess = true
        }

        hasAccess
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static requestLicense(License lic, Org context){
        Map<String, Object> result = [:]

        boolean hasAccess = calculateAccess(lic, context)
        if (hasAccess) {
            result = getLicenseMap(lic, ApiReader.IGNORE_NONE, context)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return JSON
     */
    static JSON getLicenseList(Org owner, Org context){
        Collection<Object> result = []

        List<License> available = License.executeQuery(
                'SELECT DISTINCT(lic) FROM License lic JOIN lic.orgLinks oo WHERE oo.org = :owner AND oo.roleType in (:roles )' ,
                [
                        owner: owner,
                        roles: [RDStore.OR_LICENSING_CONSORTIUM, RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]
                ]
        )

        println "${available.size()} available licenses found .."

        available.each { lic ->
            result.add(ApiStubReader.requestLicenseStub(lic, context))
        }

        ApiToolkit.cleanUpDebugInfo(result)

        return (result ? new JSON(result) : null)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> getLicenseMap(License lic, def ignoreRelation, Org context){
        Map<String, Object> result = [:]

        lic = GrailsHibernateUtil.unwrapIfProxy(lic)

        result.globalUID        = lic.globalUID
        // removed - result.contact          = lic.contact
        result.dateCreated      = ApiToolkit.formatInternalDate(lic.dateCreated)
        result.endDate          = ApiToolkit.formatInternalDate(lic.endDate)
        result.lastUpdated      = ApiToolkit.formatInternalDate(lic.getCalculatedLastUpdated())
        //result.licenseType      = lic.licenseType
        result.reference        = lic.reference
        result.startDate        = ApiToolkit.formatInternalDate(lic.startDate)
        result.normReference    = lic.sortableReference

        // erms-888
        result.calculatedType   = lic.getCalculatedType()

        // RefdataValues

        result.licenseCategory  = lic.licenseCategory?.value
        result.status           = lic.status?.value
        result.type             = lic.type?.value

        // References

        result.identifiers      = ApiCollectionReader.getIdentifierCollection(lic.ids) // com.k_int.kbplus.Identifier
        result.instanceOf       = ApiStubReader.requestLicenseStub(lic.instanceOf, context) // com.k_int.kbplus.License
        result.properties       = ApiCollectionReader.getPropertyCollection(lic, context, ApiReader.IGNORE_NONE)  // com.k_int.kbplus.(LicenseCustomProperty, LicensePrivateProperty)
        result.documents        = ApiCollectionReader.getDocumentCollection(lic.documents) // com.k_int.kbplus.DocContext
        //result.onixplLicense    = ApiReader.requestOnixplLicense(lic.onixplLicense, lic, context) // com.k_int.kbplus.OnixplLicense

        if (ignoreRelation != ApiReader.IGNORE_ALL) {
            if (ignoreRelation != ApiReader.IGNORE_SUBSCRIPTION) {
                result.subscriptions = ApiStubReader.getStubCollection(lic.subscriptions, ApiReader.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
            }
            if (ignoreRelation != ApiReader.IGNORE_LICENSE) {
                def allOrgRoles = []

                def licenseeConsortial = OrgRole.findByOrgAndLicAndRoleType(context, lic, RDStore.OR_LICENSEE_CONS)
                // restrict, if context is OR_LICENSEE_CONS for current license
                if (licenseeConsortial) {
                    allOrgRoles.add(licenseeConsortial)
                    allOrgRoles.addAll(
                            OrgRole.executeQuery(
                                    "select oo from OrgRole oo where oo.lic = :lic and oo.roleType not in (:roles)",
                                    [lic: lic, roles: [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]]
                            )
                    )
                }
                else {
                    allOrgRoles.addAll(lic.orgLinks)

                    // add derived licenses org roles
                    if (lic.derivedLicenses) {
                        allOrgRoles.addAll(
                                OrgRole.executeQuery(
                                        "select oo from OrgRole oo where oo.lic in (:derived) and oo.roleType in (:roles)",
                                        [derived: lic.derivedLicenses, roles: [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]]
                                )
                        )
                    }

                }
                allOrgRoles = allOrgRoles.unique()

                result.organisations = ApiCollectionReader.getOrgLinkCollection(allOrgRoles, ApiReader.IGNORE_LICENSE, context) // com.k_int.kbplus.OrgRole
            }
        }

        // Ignored

        //result.packages         = exportHelperService.getStubCollection(lic.pkgs, exportHelperService.PACKAGE_STUB) // com.k_int.kbplus.Package
        /*result.persons          = exportHelperService.resolvePrsLinks(
                lic.prsLinks, exportHelperService.NO_CONSTRAINT, exportHelperService.NO_CONSTRAINT, context
        ) // com.k_int.kbplus.PersonRole
        */
        ApiToolkit.cleanUp(result, true, true)
    }
}
