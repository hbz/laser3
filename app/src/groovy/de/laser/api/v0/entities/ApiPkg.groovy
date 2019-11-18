package de.laser.api.v0.entities

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiReaderHelper
import de.laser.api.v0.ApiToolkit
import de.laser.helper.Constants
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class ApiPkg {

    /**
     * @return Package | BAD_REQUEST | PRECONDITION_FAILED
     */
    static findPackageBy(String query, String value) {
        def result

        switch(query) {
            case 'id':
                result = Package.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result = Package.findAllWhere(globalUID: value)
                break
            case 'impId':
                result = Package.findAllWhere(impId: value)
                break
            case 'gokbId':
                result = Package.findAllWhere(gokbId: value)
                break
            case 'ns:identifier':
                result = Identifier.lookupObjectsByIdentifierString(new Package(), value)
                break
            default:
                return Constants.HTTP_BAD_REQUEST
                break
        }

        ApiToolkit.checkPreconditionFailed(result)
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static getPackage(Package pkg, Org context, boolean hasAccess) {
        Map<String, Object> result = [:]

		// TODO check hasAccess
        result = retrievePackageMap(pkg, context)

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

	/**
	 * @return Map<String, Object>
	 */
	static Map<String, Object> retrievePackageMap(com.k_int.kbplus.Package pkg, Org context) {
		def result = [:]

		pkg = GrailsHibernateUtil.unwrapIfProxy(pkg)

		result.globalUID        	= pkg.globalUID
		result.gokbId           	= pkg.gokbId
		result.impId            	= pkg.impId
		result.name             	= pkg.name
		result.sortName         	= pkg.sortName

		result.autoAccept       	= pkg.autoAccept ? 'Yes' : 'No'
		result.cancellationAllowances = pkg.cancellationAllowances
		result.dateCreated      	= pkg.dateCreated
		result.endDate          	= pkg.endDate
		//result.forumId          	= pkg.forumId
		//result.identifier       = pkg.identifier - TODO refactoring legacy

		result.lastUpdated      	= pkg.lastUpdated
		result.vendorURL        	= pkg.vendorURL
		result.startDate        	= pkg.startDate

		// RefdataValues

		result.packageListStatus 	= pkg.packageListStatus?.value
		result.packageType      	= pkg.packageType?.value
		result.packageScope     	= pkg.packageScope?.value
		result.packageStatus    	= pkg.packageStatus?.value
		result.breakable        	= pkg.breakable?.value
		result.consistent       	= pkg.consistent?.value
		result.fixed            	= pkg.fixed?.value
		result.isPublic         	= pkg.isPublic ? 'Yes' : 'No'

		// References

		//result.documents        = ApiReaderHelper.retrieveDocumentCollection(pkg.documents) // com.k_int.kbplus.DocContext
		result.identifiers      = ApiReaderHelper.retrieveIdentifierCollection(pkg.ids) // com.k_int.kbplus.Identifier
		//result.license          = ApiReaderHelper.requestLicenseStub(pkg.license, context) // com.k_int.kbplus.License
		result.nominalPlatform  = ApiReaderHelper.retrievePlatformMap(pkg.nominalPlatform) // com.k_int.kbplus.Platform
		result.organisations    = ApiReaderHelper.retrieveOrgLinkCollection(pkg.orgs, ApiReaderHelper.IGNORE_PACKAGE, context) // com.k_int.kbplus.OrgRole
		//result.subscriptions    = ApiReaderHelper.retrieveSubscriptionPackageStubCollection(pkg.subscriptions, ApiReaderHelper.IGNORE_PACKAGE, context) // com.k_int.kbplus.SubscriptionPackage
		result.tipps            = ApiReaderHelper.retrieveTippCollection(pkg.tipps, ApiReaderHelper.IGNORE_ALL, context) // com.k_int.kbplus.TitleInstancePackagePlatform

		// Ignored
		/*
		result.persons          = exportHelperService.resolvePrsLinks(
				pkg.prsLinks, exportHelperService.NO_CONSTRAINT, exportHelperService.NO_CONSTRAINT, context
		) // com.k_int.kbplus.PersonRole
		*/
		return ApiToolkit.cleanUp(result, true, true)
	}
}
