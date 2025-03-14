package de.laser.api.v0.entities

import de.laser.Identifier
import de.laser.License
import de.laser.Links
import de.laser.Org
import de.laser.OrgRole
import de.laser.Subscription
import de.laser.api.v0.*
import de.laser.storage.Constants
import de.laser.storage.RDStore
import de.laser.traces.DeletedObject
import grails.converters.JSON
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * An API representation of a {@link License}
 */
class ApiLicense {

    /**
     * Locates the given {@link License} and returns the object (or null if not found) and the request status for further processing
     * @param the field to look for the identifier, one of {id, globalUID, namespace:id}
     * @param the identifier value with namespace, if needed
     * @return ApiBox(obj: License | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND )
     */
    static ApiBox findLicenseBy(String query, String value) {
        ApiBox result = ApiBox.get()

        switch(query) {
            case 'id':
                result.obj = License.findAllWhere(id: Long.parseLong(value))
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldDatabaseIDAndOldObjectType(Long.parseLong(value), License.class.name)
                    }
                }
                break
            case 'globalUID':
                result.obj = License.findAllWhere(globalUID: value)
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldGlobalUID(value)
                    }
                }
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
     * Checks if the requesting institution can access to the given license
     * @param lic the {@link License} to which access is being requested
     * @param context the institution ({@link Org}) requesting access
     * @return true if the access is granted, false otherwise
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
     * Checks if the given institution can access the given license. The license
     * is returned in case of success
     * @param lic the {@link License} whose details should be retrieved
     * @param context the institution ({@link Org}) requesting the license
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
     * Checks if the requesting institution can access the license list of the requested institution.
     * The list of license is returned in case of success
     * @param owner the institution whose licenses should be retrieved
     * @param context the institution who requests the list
     * @return JSON
     * @see Org
     */
    static JSON getLicenseList(Org owner, Org context){
        Collection<Object> result = []
        List<DeletedObject> deleted = []

        List<License> available = License.executeQuery(
                'SELECT DISTINCT(lic) FROM License lic JOIN lic.orgRelations oo WHERE oo.org = :owner AND oo.roleType in (:roles )' ,
                [
                        owner: owner,
                        roles: [RDStore.OR_LICENSING_CONSORTIUM, RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]
                ]
        )

        DeletedObject.withTransaction {
            deleted.addAll(DeletedObject.executeQuery(
                    'SELECT DISTINCT(del) FROM DeletedObject del JOIN del.combos delc WHERE delc.accessibleOrg = :owner AND del.oldObjectType = :objType' ,
                    [
                            owner: owner.globalUID,
                            objType: License.class.name
                    ]
            ))
        }

        println "${available.size()}+${deleted.size()} available licenses found .."

        available.each { lic ->
            result.add(ApiStubReader.requestLicenseStub(lic, context))
        }
        deleted.each { DeletedObject deletedObject ->
            result.add(ApiStubReader.requestDeletedObjectStub(deletedObject, context))
        }

        ApiToolkit.cleanUpDebugInfo(result)

        return (result ? new JSON(result) : null)
    }

    /**
     * Assembles the given license attributes into a {@link Map}. The schema of the map can be seen in
     * schemas.gsp
     * @param lic the {@link License} which should be output
     * @param ignoreRelation should outgoing links be included in the output or not?
     * @param context the institution doing the request
     * @return Map<String, Object>
     */
    static Map<String, Object> getLicenseMap(License lic, def ignoreRelation, Org context){
        Map<String, Object> result = [:]

        lic = GrailsHibernateUtil.unwrapIfProxy(lic)

        result.globalUID        = lic.globalUID
        result.isPublicForApi   = lic.isPublicForApi ? "Yes" : "No" //implemented for eventual later internal purposes
        result.dateCreated      = ApiToolkit.formatInternalDate(lic.dateCreated)
        result.altnames         = ApiCollectionReader.getAlternativeNameCollection(lic.altnames)
        result.endDate          = ApiToolkit.formatInternalDate(lic.endDate)
        result.openEnded        = lic.openEnded?.value
        result.lastUpdated      = ApiToolkit.formatInternalDate(lic._getCalculatedLastUpdated())
        result.reference        = lic.reference
        result.startDate        = ApiToolkit.formatInternalDate(lic.startDate)

        // erms-888
        result.calculatedType   = lic._getCalculatedType()

        // RefdataValues

        result.licenseCategory  = lic.licenseCategory?.value
        result.status           = lic.status?.value

        // References

        result.identifiers      = ApiCollectionReader.getIdentifierCollection(lic.ids) // de.laser.Identifier
        result.instanceOf       = ApiStubReader.requestLicenseStub(lic.instanceOf, context) // de.laser.License
        result.properties       = ApiCollectionReader.getPropertyCollection(lic, context, ApiReader.IGNORE_NONE)  // de.laser.(LicenseCustomProperty, LicensePrivateProperty)
        result.documents        = ApiCollectionReader.getDocumentCollection(lic.documents) // de.laser.DocContext
        //result.onixplLicense    = ApiReader.requestOnixplLicense(lic.onixplLicense, lic, context) // de.laser.OnixplLicense

        result.linkedLicenses = []
        result.predecessors = []
        result.successors   = []

        List<Links> otherLinks = Links.executeQuery("select li from Links li where (li.sourceLicense = :license or li.destinationLicense = :license) and li.linkType not in (:excludes)", [license: lic, excludes: [RDStore.LINKTYPE_LICENSE]])
        otherLinks.each { Links li ->
            if(li.linkType == RDStore.LINKTYPE_FOLLOWS) {
                if(lic == li.sourceLicense)
                    result.predecessors.add(ApiStubReader.requestLicenseStub(li.destinationLicense, context)) // de.laser.License
                else if(lic == li.destinationLicense)
                    result.successors.add(ApiStubReader.requestLicenseStub(li.sourceLicense, context)) // de.laser.License
            }
            else result.linkedLicenses.add([linktype: li.linkType.value, license: ApiStubReader.requestLicenseStub((License) li.getOther(lic), context)])
        }

        if (ignoreRelation != ApiReader.IGNORE_ALL) {
            if (ignoreRelation != ApiReader.IGNORE_SUBSCRIPTION) {
                result.subscriptions = ApiStubReader.getStubCollection(lic.getSubscriptions(), ApiReader.SUBSCRIPTION_STUB, context)
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
                    allOrgRoles.addAll(lic.orgRelations)

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

                result.organisations = ApiCollectionReader.getOrgLinkCollection(allOrgRoles, ApiReader.IGNORE_LICENSE, context) // de.laser.OrgRole
                result.providers	 = ApiCollectionReader.getProviderCollection(lic.getProviders())
                result.vendors		 = ApiCollectionReader.getVendorCollection(lic.getVendors())
            }
        }

        // Ignored

        //result.packages         = exportHelperService.getStubCollection(lic.pkgs, exportHelperService.PACKAGE_STUB) // de.laser.wekb.Package
        /*result.persons          = exportHelperService.resolvePrsLinks(
                lic.prsLinks, exportHelperService.NO_CONSTRAINT, exportHelperService.NO_CONSTRAINT, context
        ) // de.laser.addressbook.PersonRole
        */
        ApiToolkit.cleanUp(result, true, true)
    }
}
