package de.laser.api.v0.entities

import de.laser.Identifier
import de.laser.Org
import de.laser.Package
import de.laser.api.v0.*
import de.laser.helper.Constants
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * An API representation of a {@link Package}
 */
@Slf4j
class ApiPkg {

    /**
	 * Locates the given {@link Package} and returns the object (or null if not found) and the request status for further processing
	 * @param the field to look for the identifier, one of {id, globalUID, gokbId, ns:identifier}
	 * @param the identifier value
     * @return {@link ApiBox}(obj: Package | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
	 * @see ApiBox#validatePrecondition_1()
     */
    static ApiBox findPackageBy(String query, String value) {
		ApiBox result = ApiBox.get()

        switch(query) {
            case 'id':
				result.obj = Package.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
				result.obj = Package.findAllWhere(globalUID: value)
                break
            case 'gokbId':
				result.obj = Package.findAllWhere(gokbId: value)
                break
            case 'ns:identifier':
				result.obj = Identifier.lookupObjectsByIdentifierString(new Package(), value)
                break
            default:
				result.status = Constants.HTTP_BAD_REQUEST
                return result
                break
        }
		result.validatePrecondition_1()

		if (result.obj instanceof Package) {
			result.validateDeletedStatus_2('packageStatus', RDStore.PACKAGE_STATUS_DELETED)
		}
		result
    }

    /**
	 * Retrieves the given {@link Package} for the given institution
	 * @param pkg the {@link Package} to retrieve
	 * @param context the institution ({@link Org}) requesting the record
     * @return JSON
     */
    static getPackage(Package pkg, Org context) {
        Map<String, Object> result = [:]

        result = getPackageMap(pkg, context)

        return result ? new JSON(result) : null
    }

	/**
	 * Assembles the given package attributes into a {@link Map}. The schema of the map can be seen in
	 * schemas.gsp
	 * @param pkg the {@link Package} which should be output
	 * @param context the institution ({@link Org}) requesting
	 * @return Map<String, Object>
	 */
	static Map<String, Object> getPackageMap(Package pkg, Org context) {
		Map<String, Object> result = [:]

		pkg = GrailsHibernateUtil.unwrapIfProxy(pkg)

		result.globalUID        	= pkg.globalUID
		result.gokbId           	= pkg.gokbId
		result.name             	= pkg.name
		result.altnames     		= ApiCollectionReader.getAlternativeNameCollection(pkg.altnames)

		result.dateCreated      	= ApiToolkit.formatInternalDate(pkg.dateCreated)
		result.lastUpdated      	= ApiToolkit.formatInternalDate(pkg._getCalculatedLastUpdated())

		// RefdataValues

		result.contentType      	= pkg.contentType?.value
		result.scope     			= pkg.scope?.value
		result.file	 				= pkg.file?.value
		result.packageStatus    	= pkg.packageStatus?.value
		result.breakable        	= pkg.breakable?.value
		result.consistent       	= pkg.consistent?.value

		// References

		//result.documents        = ApiCollectionReader.retrieveDocumentCollection(pkg.documents) // de.laser.DocContext
		result.identifiers      = ApiCollectionReader.getIdentifierCollection(pkg.ids) // de.laser.Identifier
		//result.license          = ApiStubReader.requestLicenseStub(pkg.license, context) // com.k_int.kbplus.License
		result.nominalPlatform  = ApiUnsecuredMapReader.getPlatformMap(pkg.nominalPlatform, context) // com.k_int.kbplus.Platform
		result.organisations    = ApiCollectionReader.getOrgLinkCollection(pkg.orgs, ApiReader.IGNORE_PACKAGE, context) // de.laser.OrgRole
		//result.subscriptions    = ApiStubReader.retrieveSubscriptionPackageStubCollection(pkg.subscriptions, ApiCollectionReader.IGNORE_PACKAGE, context) // de.laser.SubscriptionPackage
		result.tipps            = ApiCollectionReader.getTippCollection(pkg.tipps, ApiReader.IGNORE_ALL, context) // de.laser.TitleInstancePackagePlatform

		ApiToolkit.cleanUp(result, true, true)
	}
}
