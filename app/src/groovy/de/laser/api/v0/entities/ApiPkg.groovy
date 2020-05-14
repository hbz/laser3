package de.laser.api.v0.entities

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import de.laser.api.v0.ApiBox
import de.laser.api.v0.ApiCollectionReader
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiToolkit
import de.laser.api.v0.ApiUnsecuredMapReader
import de.laser.helper.Constants
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
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
     * @return JSON | FORBIDDEN
     */
    static requestPackage(Package pkg, Org context) {
        Map<String, Object> result = [:]

        result = getPackageMap(pkg, context)

        return new JSON(result)
    }

	/**
	 * @return Map<String, Object>
	 */
	static Map<String, Object> getPackageMap(com.k_int.kbplus.Package pkg, Org context) {
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
		result.lastUpdated      	= ApiToolkit.formatInternalDate(pkg.getCalculatedLastUpdated())
		result.vendorURL        	= pkg.vendorURL
		result.startDate        	= ApiToolkit.formatInternalDate(pkg.startDate)
		result.listVerifiedDate     = ApiToolkit.formatInternalDate(pkg.listVerifiedDate)

		// RefdataValues

		result.packageListStatus 	= pkg.packageListStatus?.value
		result.contentType      	= pkg.contentType?.value
		result.packageScope     	= pkg.packageScope?.value
		result.packageStatus    	= pkg.packageStatus?.value
		result.breakable        	= pkg.breakable?.value
		result.consistent       	= pkg.consistent?.value
		result.fixed            	= pkg.fixed?.value
		result.isPublic         	= pkg.isPublic ? 'Yes' : 'No'

		// References

		//result.documents        = ApiCollectionReader.retrieveDocumentCollection(pkg.documents) // com.k_int.kbplus.DocContext
		result.identifiers      = ApiCollectionReader.getIdentifierCollection(pkg.ids) // com.k_int.kbplus.Identifier
		//result.license          = ApiStubReader.requestLicenseStub(pkg.license, context) // com.k_int.kbplus.License
		result.nominalPlatform  = ApiUnsecuredMapReader.getPlatformMap(pkg.nominalPlatform, context) // com.k_int.kbplus.Platform
		result.organisations    = ApiCollectionReader.getOrgLinkCollection(pkg.orgs, ApiReader.IGNORE_PACKAGE, context) // com.k_int.kbplus.OrgRole
		//result.subscriptions    = ApiStubReader.retrieveSubscriptionPackageStubCollection(pkg.subscriptions, ApiCollectionReader.IGNORE_PACKAGE, context) // com.k_int.kbplus.SubscriptionPackage
		result.tipps            = ApiCollectionReader.getTippCollection(pkg.tipps, ApiReader.IGNORE_ALL, context) // com.k_int.kbplus.TitleInstancePackagePlatform

		// Ignored
		/*
		result.persons          = exportHelperService.resolvePrsLinks(
				pkg.prsLinks, exportHelperService.NO_CONSTRAINT, exportHelperService.NO_CONSTRAINT, context
		) // com.k_int.kbplus.PersonRole
		*/
		ApiToolkit.cleanUp(result, true, true)
	}
}
