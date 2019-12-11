package de.laser.api.v0

import com.k_int.kbplus.*
import de.laser.api.v0.catalogue.ApiCatalogue
import de.laser.api.v0.entities.*
import de.laser.api.v0.special.ApiOAMonitor
import de.laser.api.v0.special.ApiStatistic
import de.laser.helper.Constants
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletRequest

@Log4j
class ApiManager {

    static final VERSION = '0.74'
    static final NOT_SUPPORTED = false

    /**
     * @return Object
     * @return BAD_REQUEST: if invalid/missing (unsupported) identifier
     * @return PRECONDITION_FAILED: if multiple matches(objects) are found
     * @return NOT_ACCEPTABLE: if requested format(response) is not supported
     * @return NOT_IMPLEMENTED: if requested method(object type) is not supported
     */
    static read(String obj, String query, String value, Org contextOrg, String format) {
        def result

        def failureCodes  = [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED]
        boolean accessDueDatamanager = ApiToolkit.isDataManager(contextOrg)

        log.debug("API-READ (" + VERSION + "): ${obj} (${format}) -> ${query}:${value}")

        def resolve = { endpoint, supportedFormats ->
            if (! endpoint.equalsIgnoreCase(obj)) {
                return Constants.HTTP_NOT_IMPLEMENTED
            }
            else if (! format in ApiReader.SUPPORTED_FORMATS){
                return Constants.HTTP_NOT_ACCEPTABLE
            }
            return Constants.VALID_REQUEST
        }

        if (resolve('costItem', ApiReader.SUPPORTED_FORMATS.costItem) == Constants.VALID_REQUEST) {

            def costItem = ApiCostItem.findCostItemBy(query, value)

            if (costItem && !(costItem in failureCodes)) {
                result = ApiCostItem.getCostItem((CostItem) costItem, contextOrg, accessDueDatamanager)
            }
        }
        else if (resolve('costItemList', ApiReader.SUPPORTED_FORMATS.costItem) == Constants.VALID_REQUEST) {

            def identifierAndTimestamp = ApiToolkit.parseTimeLimitedQuery( query, value )

            if (identifierAndTimestamp == Constants.HTTP_BAD_REQUEST) {
                return Constants.HTTP_BAD_REQUEST
            }

            result = ApiOrg.findOrganisationBy(identifierAndTimestamp[0].key, identifierAndTimestamp[0].value) // use of http status code

            if (result && !(result in failureCodes)) {

                if(identifierAndTimestamp[1].key == 'timestamp'){
                    result = ApiCostItem.getCostItemListWithTimeStamp(result, contextOrg, accessDueDatamanager, identifierAndTimestamp[1].value)
                }
                else {
                    result = ApiCostItem.getCostItemList(result, contextOrg, accessDueDatamanager)
                }
            }
        }
        else if ('document'.equalsIgnoreCase(obj)) {

            result = ApiDoc.findDocumentBy(query, value)

            if (result && !(result in failureCodes)) {
                result = ApiDoc.getDocument((Doc) result, contextOrg, accessDueDatamanager)
            }
        }
        else if (NOT_SUPPORTED && 'issueEntitlements'.equalsIgnoreCase(obj)) {

            def subPkg = ApiIssueEntitlement.findSubscriptionPackageBy(query, value)

            if (subPkg && !(subPkg in failureCodes) ) {
                result = ApiIssueEntitlement.getIssueEntitlements(subPkg, contextOrg, accessDueDatamanager)

                if (result && format == Constants.MIME_TEXT_PLAIN) {
                    def kbart = ApiKbartConverter.convertIssueEntitlements(result)
                    result = ApiKbartConverter.getAsDocument(kbart)
                }
            }
        }
        else if (resolve('license', ApiReader.SUPPORTED_FORMATS.license) == Constants.VALID_REQUEST) {

            result = ApiLicense.findLicenseBy(query, value)

            if (result && !(result in failureCodes)) {
                result = ApiLicense.getLicense((License) result, contextOrg, accessDueDatamanager)
            }
        }
        else if (resolve('licenseList', ApiReader.SUPPORTED_FORMATS.license) == Constants.VALID_REQUEST) {

            result = ApiOrg.findOrganisationBy(query, value)

            if (result && !(result in failureCodes)) {
                result = ApiLicense.getLicenseList(result, contextOrg, accessDueDatamanager)
            }
        }
        else if (resolve('oaMonitor', ApiReader.SUPPORTED_FORMATS.oaMonitor) == Constants.VALID_REQUEST) {

            if (! accessDueDatamanager) {
                return Constants.HTTP_FORBIDDEN
            }
            result = ApiOrg.findOrganisationBy(query, value)

            if (result && !(result in failureCodes)) {
                result = ApiOAMonitor.getOrganisation(result, contextOrg, accessDueDatamanager)
            }
        }
        else if (resolve('oaMonitorList', ApiReader.SUPPORTED_FORMATS.oaMonitorList) == Constants.VALID_REQUEST) {

            result = ApiOAMonitor.getAllOrgs(accessDueDatamanager)
        }
        else if (NOT_SUPPORTED && 'onixpl'.equalsIgnoreCase(obj)) {

            def lic = ApiLicense.findLicenseBy(query, value)

            if (lic && !(lic in failureCodes)) {
                result = ApiDoc.getOnixPlDocument((License) lic, contextOrg, accessDueDatamanager)
            }
        }
        else if (resolve('organisation', ApiReader.SUPPORTED_FORMATS.organisation) == Constants.VALID_REQUEST) {

            result = ApiOrg.findOrganisationBy(query, value)

            if (result && !(result in failureCodes)) {
                result = ApiOrg.getOrganisation((Org) result, contextOrg, accessDueDatamanager)
            }
        }
        else if (resolve('package', ApiReader.SUPPORTED_FORMATS.package) == Constants.VALID_REQUEST) {

            result = ApiPkg.findPackageBy(query, value)

            if (result && !(result in failureCodes)) {
                result = ApiPkg.getPackage((Package) result, contextOrg, accessDueDatamanager)
            }
        }
        else if (resolve('propertyList', ApiReader.SUPPORTED_FORMATS.propertyList) == Constants.VALID_REQUEST) {

			result = ApiCatalogue.getAllProperties(contextOrg)
        }
        else if (resolve('refdataList', ApiReader.SUPPORTED_FORMATS.refdataList) == Constants.VALID_REQUEST) {

            result = ApiCatalogue.getAllRefdatas()
        }
        else if (resolve('statistic', ApiReader.SUPPORTED_FORMATS.statistic) == Constants.VALID_REQUEST) {

            if (! accessDueDatamanager) {
                return Constants.HTTP_FORBIDDEN
            }
            result = ApiPkg.findPackageBy(query, value)

            if (result && !(result in failureCodes)) {
                result = ApiStatistic.getPackage(result, contextOrg, accessDueDatamanager)
            }
        }
        else if (resolve('statisticList', ApiReader.SUPPORTED_FORMATS.statistic) == Constants.VALID_REQUEST) {

            result = ApiStatistic.getAllPackages(accessDueDatamanager)
        }
        else if (resolve('subscription', ApiReader.SUPPORTED_FORMATS.subscription) == Constants.VALID_REQUEST) {

            result = ApiSubscription.findSubscriptionBy(query, value)

            if (result && !(result in failureCodes)) {
                result = ApiSubscription.getSubscription((Subscription) result, contextOrg, accessDueDatamanager)
            }
        }
        else if (resolve('subscriptionList', ApiReader.SUPPORTED_FORMATS.subscription) == Constants.VALID_REQUEST) {

            result = ApiOrg.findOrganisationBy(query, value)

            if (result && !(result in failureCodes)) {
                result = ApiSubscription.getSubscriptionList(result, contextOrg, accessDueDatamanager)
            }
        }
        else {
            result = Constants.HTTP_NOT_IMPLEMENTED
        }

        result
    }

    @Deprecated
    static write(String obj, JSONObject data, Org contextOrg) {

        return // closed ..

        def result

        // TODO check isDataManager, etc for contextOrg

        // check existing resources
        def conflict = false

        if (NOT_SUPPORTED && 'organisation'.equalsIgnoreCase(obj)) {

            data.identifiers?.each { ident ->
                def hits = ApiOrg.findOrganisationBy('ns:identifier', ident.namespace + ":" + ident.value)
                if (hits == Constants.HTTP_PRECONDITION_FAILED || hits instanceof Org) {
                    conflict = true
                }
            }
            def hits = ApiOrg.findOrganisationBy('name', data.name.trim())
            if (hits == Constants.HTTP_PRECONDITION_FAILED || hits instanceof Org) {
                conflict = true
            }

            if (conflict) {
                return ['result': Constants.HTTP_CONFLICT, 'debug': 'debug']
            }

            result = ApiWriter.importOrganisation(data, contextOrg)
        }
        else if (NOT_SUPPORTED && 'license'.equalsIgnoreCase(obj)) {

            result = ApiWriter.importLicense(data, contextOrg)
        }
        else if (NOT_SUPPORTED && 'subscription'.equalsIgnoreCase(obj)) {

            data.identifiers?.each { ident ->
                def hits = ApiSubscription.findSubscriptionBy('ns:identifier', ident.namespace + ":" + ident.value)
                if (hits == Constants.HTTP_PRECONDITION_FAILED || hits instanceof Subscription) {
                    conflict = true
                }
            }
            def hits = ApiSubscription.findSubscriptionBy('identifier', data.identifier)
            if (hits == Constants.HTTP_PRECONDITION_FAILED || hits instanceof Subscription) {
                conflict = true
            }

            if (conflict) {
                return ['result': Constants.HTTP_CONFLICT, 'debug': 'debug']
            }

            result = ApiWriter.importSubscription(data, contextOrg)
        }
        else {
            result = Constants.HTTP_NOT_IMPLEMENTED
        }
        result
    }

    static Map<String, Object> buildResponse(HttpServletRequest request, String obj, String query, String value, String context, Org contextOrg, def result) {

        def response = [:]

        def trimJson = { map ->
            Map<String, Object> rm = [:]
            map.each{ key, val ->
                if (key && val) {
                    rm.put(key, val)
                }
            }
            rm
        }

        // POST

        if (result instanceof HashMap) {

            switch(result['result']) {
                case Constants.HTTP_CREATED:
                    response.json = new JSON( trimJson(["message": "resource successfully created",
                                                    "debug": result['debug'],
                                                    "status": HttpStatus.CREATED.value()]))
                    response.status = HttpStatus.CREATED.value()
                    break
                case Constants.HTTP_CONFLICT:
                    response.json = new JSON( trimJson(["message": "conflict with existing resource",
                                                    "debug": result['debug'],
                                                    "status": HttpStatus.CONFLICT.value()]))
                    response.status = HttpStatus.CONFLICT.value()
                    break
                case Constants.HTTP_INTERNAL_SERVER_ERROR:
                    response.json = new JSON( trimJson(["message": "resource not created",
                                                    "debug": result['debug'],
                                                    "status": HttpStatus.INTERNAL_SERVER_ERROR.value()]))
                    response.status = HttpStatus.INTERNAL_SERVER_ERROR.value()
                    break
            }
        }

        // GET

        else if (Constants.HTTP_FORBIDDEN == result) {
            if (contextOrg) {
                response.json = new JSON( trimJson(["message": "forbidden",
                                                "obj": obj, "q": query, "v": value, "context": contextOrg.shortcode,
                                                "status": HttpStatus.FORBIDDEN.value()]))
                response.status = HttpStatus.FORBIDDEN.value()
            }
            else {
                response.json = new JSON( trimJson(["message": "forbidden",
                                                "obj": obj, "context": context,
                                                "status": HttpStatus.FORBIDDEN.value()]))
                response.status = HttpStatus.FORBIDDEN.value()
            }
        }
        else if (Constants.HTTP_NOT_ACCEPTABLE == result) {
            response.json = new JSON( trimJson(["message": "requested format not supported",
                                            "method": request.method, "accept": request.getHeader('accept'), "obj": obj,
                                            "status": HttpStatus.NOT_ACCEPTABLE.value()]))
            response.status = HttpStatus.NOT_ACCEPTABLE.value()
        }
        else if (Constants.HTTP_NOT_IMPLEMENTED == result) {
            response.json = new JSON( trimJson(["message": "requested method not implemented",
                                            "method": request.method, "obj": obj,
                                            "status": HttpStatus.NOT_IMPLEMENTED.value()]))
            response.status = HttpStatus.NOT_IMPLEMENTED.value()
        }
        else if (Constants.HTTP_BAD_REQUEST == result) {
            response.json = new JSON( trimJson(["message": "invalid/missing identifier or post body",
                                            "obj": obj, "q": query, "context": context,
                                            "status": HttpStatus.BAD_REQUEST.value()]))
            response.status = HttpStatus.BAD_REQUEST.value()
        }
        else if (Constants.HTTP_PRECONDITION_FAILED == result) {
            response.json = new JSON( trimJson(["message": "precondition failed; multiple matches",
                                            "obj": obj, "q": query, "context": context,
                                            "status": HttpStatus.PRECONDITION_FAILED.value()]))
            response.status = HttpStatus.PRECONDITION_FAILED.value()
        }

        if (! result) {
            response.json = new JSON( trimJson(["message": "result not found",
                                            "obj": obj, "q": query, "v": value, "context": context,
                                            "status": HttpStatus.NOT_FOUND.value()]))
            response.status = HttpStatus.NOT_FOUND.value()
        }
        else {
            if (result instanceof List) {
                response.json = new JSON(result)
            }
            else {
                response.json = result
            }
            response.status = HttpStatus.OK.value()
        }

        response
    }
}
