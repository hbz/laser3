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

@Slf4j
class ApiPkg {

    /**
     * @return ApiBox(obj: Package | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
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
     * @return JSON
     */
    static getPackage(Package pkg, Org context) {
        Map<String, Object> result = [:]

        result = getPackageMap(pkg, context)

        return result ? new JSON(result) : null
    }

	/**
	 * @return Map<String, Object>
	 */
	static Map<String, Object> getPackageMap(Package pkg, Org context) {
		Map<String, Object> result = [:]

		pkg = GrailsHibernateUtil.unwrapIfProxy(pkg)

		result.globalUID        	= pkg.globalUID
		result.gokbId           	= pkg.gokbId
		result.name             	= pkg.name
		result.sortName         	= pkg.sortName

		result.autoAccept       	= pkg.autoAccept ? 'Yes' : 'No'
		result.cancellationAllowances = pkg.cancellationAllowances
		result.dateCreated      	= ApiToolkit.formatInternalDate(pkg.dateCreated)
		result.endDate          	= ApiToolkit.formatInternalDate(pkg.endDate)
		result.lastUpdated      	= ApiToolkit.formatInternalDate(pkg._getCalculatedLastUpdated())
		result.vendorURL        	= pkg.vendorURL
		result.startDate        	= ApiToolkit.formatInternalDate(pkg.startDate)
		//result.listVerifiedDate     = ApiToolkit.formatInternalDate(pkg.listVerifiedDate)

		// RefdataValues

		result.contentType      	= pkg.contentType?.value
		result.scope     			= pkg.scope?.value
		result.file	 				= pkg.file?.value
		result.packageStatus    	= pkg.packageStatus?.value
		result.breakable        	= pkg.breakable?.value
		result.consistent       	= pkg.consistent?.value
		result.isPublic         	= pkg.isPublic ? 'Yes' : 'No'

		// References

		//result.documents        = ApiCollectionReader.retrieveDocumentCollection(pkg.documents) // de.laser.DocContext
		result.identifiers      = ApiCollectionReader.getIdentifierCollection(pkg.ids) // de.laser.Identifier
		//result.license          = ApiStubReader.requestLicenseStub(pkg.license, context) // com.k_int.kbplus.License
		result.nominalPlatform  = ApiUnsecuredMapReader.getPlatformMap(pkg.nominalPlatform, context) // com.k_int.kbplus.Platform
		result.organisations    = ApiCollectionReader.getOrgLinkCollection(pkg.orgs, ApiReader.IGNORE_PACKAGE, context) // de.laser.OrgRole
		//result.subscriptions    = ApiStubReader.retrieveSubscriptionPackageStubCollection(pkg.subscriptions, ApiCollectionReader.IGNORE_PACKAGE, context) // de.laser.SubscriptionPackage
		result.tipps            = ApiCollectionReader.getTippCollection(pkg.tipps, ApiReader.IGNORE_ALL, context) // de.laser.TitleInstancePackagePlatform

		// Ignored
		/*
		result.persons          = exportHelperService.resolvePrsLinks(
				pkg.prsLinks, exportHelperService.NO_CONSTRAINT, exportHelperService.NO_CONSTRAINT, context
		) // de.laser.PersonRole
		*/
		ApiToolkit.cleanUp(result, true, true)
	}
}
