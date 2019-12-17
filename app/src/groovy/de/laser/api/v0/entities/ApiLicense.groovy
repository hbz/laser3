package de.laser.api.v0.entities

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
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

        ApiToolkit.checkPreconditionFailed(result)
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
        Map<String, Object> result = [:]
        hasAccess = calculateAccess(lic, context, hasAccess)

        if (hasAccess) {
            result = retrieveLicenseMap(lic, ApiReader.IGNORE_NONE, context)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return JSON
     */
    static JSON getLicenseList(Org owner, Org context, boolean hasAccess){
        Collection<Object> result = []

        List<License> available = License.executeQuery(
                'SELECT lic FROM License lic JOIN lic.orgLinks oo WHERE oo.org = :owner AND oo.roleType in (:roles )' ,
                [
                        owner: owner,
                        roles: [RDStore.OR_LICENSING_CONSORTIUM, RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]
                ]
        )

        available.each { lic ->
            //if (calculateAccess(lic, context, hasAccess)) {
                //println lic.id + ' ' + lic.reference
                result.add(ApiStubReader.requestLicenseStub(lic, context, true))
                //result.add([globalUID: lic.globalUID])
            //}
        }

        return (result ? new JSON(result) : null)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> retrieveLicenseMap(License lic, def ignoreRelation, Org context){
        Map<String, Object> result = [:]

        lic = GrailsHibernateUtil.unwrapIfProxy(lic)

        result.globalUID        = lic.globalUID
        // removed - result.contact          = lic.contact
        result.dateCreated      = lic.dateCreated
        result.endDate          = lic.endDate
        result.impId            = lic.impId
        // result.lastmod          = lic.lastmod // legacy ?
        result.lastUpdated      = lic.lastUpdated
        // result.licenseUrl       = lic.licenseUrl
        // removed - result.licensorRef      = lic.licensorRef
        // removed - result.licenseeRef      = lic.licenseeRef
        result.licenseType      = lic.licenseType
        //result.noticePeriod     = lic.noticePeriod
        result.reference        = lic.reference
        result.startDate        = lic.startDate
        result.normReference= lic.sortableReference

        // erms-888
        result.calculatedType   = lic.getCalculatedType()

        // RefdataValues

        result.isPublic         = lic.isPublic ? 'Yes' : 'No'
        // result.licenseCategory  = lic.licenseCategory?.value // legacy
        result.status           = lic.status?.value
        // result.type             = lic.type?.value

        // References

        result.identifiers      = ApiCollectionReader.retrieveIdentifierCollection(lic.ids) // com.k_int.kbplus.Identifier
        result.instanceOf       = ApiStubReader.requestLicenseStub(lic.instanceOf, context) // com.k_int.kbplus.License
        result.properties       = ApiCollectionReader.retrievePropertyCollection(lic, context, ApiReader.IGNORE_NONE)  // com.k_int.kbplus.(LicenseCustomProperty, LicensePrivateProperty)
        result.documents        = ApiCollectionReader.retrieveDocumentCollection(lic.documents) // com.k_int.kbplus.DocContext
        result.onixplLicense    = ApiReader.requestOnixplLicense(lic.onixplLicense, lic, context) // com.k_int.kbplus.OnixplLicense

        if (ignoreRelation != ApiReader.IGNORE_ALL) {
            if (ignoreRelation != ApiReader.IGNORE_SUBSCRIPTION) {
                result.subscriptions = ApiStubReader.retrieveStubCollection(lic.subscriptions, ApiReader.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
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

                result.organisations = ApiCollectionReader.retrieveOrgLinkCollection(allOrgRoles, ApiReader.IGNORE_LICENSE, context) // com.k_int.kbplus.OrgRole
            }
        }

        // Ignored

        //result.packages         = exportHelperService.resolveStubs(lic.pkgs, exportHelperService.PACKAGE_STUB) // com.k_int.kbplus.Package
        /*result.persons          = exportHelperService.resolvePrsLinks(
                lic.prsLinks, exportHelperService.NO_CONSTRAINT, exportHelperService.NO_CONSTRAINT, context
        ) // com.k_int.kbplus.PersonRole
        */
        return ApiToolkit.cleanUp(result, true, true)
    }
}
