package de.laser.api.v0.entities

import de.laser.Identifier
import de.laser.Org
import de.laser.wekb.Package
import de.laser.api.v0.*
import de.laser.storage.Constants
import de.laser.storage.RDStore
import de.laser.wekb.TitleInstancePackagePlatform
import grails.converters.JSON
import groovy.sql.Sql
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * An API representation of a {@link Package}
 */
class ApiPkg {

    /**
	 * Locates the given {@link Package} and returns the object (or null if not found) and the request status for further processing
	 * @param the field to look for the identifier, one of {id, globalUID, gokbId, ns:identifier}
	 * @param the identifier value
     * @return {@link ApiBox}(obj: Package | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
	 * @see ApiBox#validatePrecondition_1()
     */
    static ApiBox findPackageBy(String query, String value, int max) {
		ApiBox result = ApiBox.get()

		if(max <= 20000) {
			switch(query) {
				case 'id':
					result.obj = Package.get(value)
					break
				case 'globalUID':
					result.obj = Package.findByGlobalUID(value)
					break
				case 'wekbId':
					result.obj = Package.findByGokbId(value)
					break
				case 'ns:identifier':
					result.obj = Identifier.lookupObjectsByIdentifierString(Package.class.getSimpleName(), value)
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
		}
		else {
			result.status = Constants.HTTP_BAD_REQUEST
		}
		result
    }

    /**
	 * Retrieves the given {@link Package} for the given institution
	 * @param pkg the {@link Package} to retrieve
	 * @param context the institution ({@link Org}) requesting the record
     * @return JSON
     */
    static getPackage(Sql sql, Package pkg, Org context, int max, int offset) {
        Map<String, Object> result = [:]

        result = getPackageMap(sql, pkg, context, max, offset)

        return result ? new JSON(result) : null
    }

	/**
	 * Assembles the given package attributes into a {@link Map}. The schema of the map can be seen in
	 * schemas.gsp
	 * @param pkg the {@link Package} which should be output
	 * @param context the institution ({@link Org}) requesting
	 * @return Map<String, Object>
	 */
	static Map<String, Object> getPackageMap(Sql sql, Package pkg, Org context, int max, int offset) {
		Map<String, Object> result = [:]

		pkg = GrailsHibernateUtil.unwrapIfProxy(pkg)

		result.globalUID        	= pkg.globalUID
		result.wekbId           	= pkg.gokbId
		result.name             	= pkg.name
		result.altnames     		= ApiCollectionReader.getAlternativeNameCollection(pkg.altnames)

		result.dateCreated      	= ApiToolkit.formatInternalDate(pkg.dateCreated)
		result.lastUpdated      	= ApiToolkit.formatInternalDate(pkg._getCalculatedLastUpdated())

		// RefdataValues

		result.contentType      	= pkg.contentType?.value
		result.scope     			= pkg.scope?.value
		result.file	 				= pkg.file?.value
		result.status				= pkg.packageStatus?.value
		result.breakable        	= pkg.breakable?.value
		result.consistent       	= pkg.consistent?.value

		// References

		//result.documents        = ApiCollectionReader.retrieveDocumentCollection(pkg.documents) // de.laser.DocContext
		result.identifiers      = ApiCollectionReader.getIdentifierCollection(pkg.ids) // de.laser.Identifier
		//result.license          = ApiStubReader.requestLicenseStub(pkg.license, context) // de.laser.License
		result.nominalPlatform  = ApiUnsecuredMapReader.getPlatformMap(pkg.nominalPlatform, context) // de.laser.wekb.Platform
		result.provider    		= ApiUnsecuredMapReader.getProviderStubMap(pkg.provider) // de.laser.wekb.Provider
		result.librarySuppliers	= ApiCollectionReader.getLibrarySuppliers(pkg.vendors?.vendor) //de.laser.wekb.Vendor
		//result.subscriptions    = ApiStubReader.retrieveSubscriptionPackageStubCollection(pkg.subscriptions, ApiCollectionReader.IGNORE_PACKAGE, context) // de.laser.SubscriptionPackage
		Set<Long> tippIDs = TitleInstancePackagePlatform.executeQuery('select tipp.id from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg order by tipp.sortname', [pkg: pkg, max: max, offset: offset])
		int total = TitleInstancePackagePlatform.countByPkg(pkg)
		//move to native sql
		result.tipps            = ApiCollectionReader.getTippCollectionWithSQL(sql, tippIDs, ApiReader.IGNORE_ALL) // de.laser.wekb.TitleInstancePackagePlatform
		result.recordTotalCount = total
		result.recordCount = tippIDs ? tippIDs.size() : 0
		result.offset = offset
		result.max = max
		result.currentPage = (offset/max)+1
		result.totalPage = Math.ceil(total/max)

		ApiToolkit.cleanUp(result, true, true)
	}
}
