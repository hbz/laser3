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

    static final VERSION = '0.97'
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

        boolean isDatamanager = ApiToolkit.isDataManager(contextOrg)
        boolean isInvoiceTool = ApiToolkit.isInvoiceTool(contextOrg)

        log.debug("API-READ (" + VERSION + "): ${obj} (${format}) -> ${query}:${value}")

        Closure checkValidRequest = { endpoint ->
            if (! endpoint.equalsIgnoreCase(obj)) {
                return false
            }
            result = Constants.VALID_REQUEST
            return true
        }

        if (! (ApiReader.SUPPORTED_FORMATS.containsKey(obj))){
            return Constants.HTTP_NOT_IMPLEMENTED
        }
        else if (! (format in ApiReader.SUPPORTED_FORMATS[obj])){
            return Constants.HTTP_NOT_ACCEPTABLE
        }

        if (checkValidRequest('costItem')) {

            ApiBox tmp = ApiCostItem.findCostItemBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiCostItem.requestCostItem((CostItem) tmp.obj, contextOrg, isInvoiceTool)
            }
        }
        else if (checkValidRequest('costItemList')) {

            def identifierAndTimestamp = ApiToolkit.parseTimeLimitedQuery( query, value )

            if (identifierAndTimestamp == Constants.HTTP_BAD_REQUEST) {
                return Constants.HTTP_BAD_REQUEST
            }
            ApiBox tmp = ApiOrg.findOrganisationBy(identifierAndTimestamp[0].key, identifierAndTimestamp[0].value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                if(identifierAndTimestamp[1].key == 'timestamp'){
                    result = ApiCostItem.requestCostItemListWithTimeStamp((Org) tmp.obj, contextOrg, identifierAndTimestamp[1].value, isInvoiceTool)
                }
                else {
                    result = ApiCostItem.requestCostItemList((Org) tmp.obj, contextOrg, isInvoiceTool)
                }
            }
        }
        else if (checkValidRequest('document')) {

            ApiBox tmp = ApiDoc.findDocumentBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiDoc.requestDocument((Doc) tmp.obj, contextOrg)
            }
        }
        /* else if (NOT_SUPPORTED && 'issueEntitlements'.equalsIgnoreCase(obj)) {

            def subPkg = ApiIssueEntitlement.findSubscriptionPackageBy(query, value)

            if (subPkg && !(subPkg in failureCodes) ) {
                result = ApiIssueEntitlement.getIssueEntitlements(subPkg, contextOrg)

                if (result && format == Constants.MIME_TEXT_PLAIN) {
                    def kbart = ApiKbartConverter.convertIssueEntitlements(result)
                    result = ApiKbartConverter.getAsDocument(kbart)
                }
            }
        } */
        else if (checkValidRequest('license')) {

            ApiBox tmp = ApiLicense.findLicenseBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiLicense.requestLicense((License) tmp.obj, contextOrg)
            }
        }
        else if (checkValidRequest('licenseList')) {

            ApiBox tmp = ApiOrg.findOrganisationBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiLicense.getLicenseList((Org) tmp.obj, contextOrg)
            }
        }
        else if (checkValidRequest('oaMonitor')) {

            if (! isDatamanager) {
                return Constants.HTTP_FORBIDDEN
            }
            ApiBox tmp = ApiOrg.findOrganisationBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiOAMonitor.requestOrganisation((Org) tmp.obj, contextOrg)
            }
        }
        else if (checkValidRequest('oaMonitorList')) {

            if (! isDatamanager) {
                return Constants.HTTP_FORBIDDEN
            }

            result = ApiOAMonitor.getAllOrgs()
        }
        else if (checkValidRequest('oaMonitorSubscription')) {

            if (! isDatamanager) {
                return Constants.HTTP_FORBIDDEN
            }

            ApiBox tmp = ApiSubscription.findSubscriptionBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiOAMonitor.requestSubscription((Subscription) tmp.obj, contextOrg)
            }
        }
        else if (checkValidRequest('organisation')) {

            ApiBox tmp = ApiOrg.findOrganisationBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiOrg.requestOrganisation((Org) tmp.obj, contextOrg, isInvoiceTool)
            }
        }
        else if (checkValidRequest('package')) {

            ApiBox tmp = ApiPkg.findPackageBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiPkg.requestPackage((Package) tmp.obj, contextOrg)
            }
        }
        else if (checkValidRequest('propertyList')) {

			result = ApiCatalogue.getAllProperties(contextOrg)
        }
        else if (checkValidRequest('refdataList')) {

            result = ApiCatalogue.getAllRefdatas()
        }
        else if (checkValidRequest('statistic')) {

            if (! isDatamanager) {
                return Constants.HTTP_FORBIDDEN
            }
            ApiBox tmp = ApiPkg.findPackageBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiStatistic.requestPackage((Package) tmp.obj)
            }
        }
        else if (checkValidRequest('statisticList')) {

            if (! isDatamanager) {
                return Constants.HTTP_FORBIDDEN
            }

            result = ApiStatistic.getAllPackages()
        }
        else if (checkValidRequest('subscription')) {

            ApiBox tmp = ApiSubscription.findSubscriptionBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiSubscription.requestSubscription((Subscription) tmp.obj, contextOrg, isInvoiceTool)
            }
        }
        else if (checkValidRequest('subscriptionList')) {

            ApiBox tmp = ApiOrg.findOrganisationBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiSubscription.getSubscriptionList((Org) tmp.obj, contextOrg)
            }
        }
        else {
            result = Constants.HTTP_NOT_IMPLEMENTED
        }

        result
    }

    //@Deprecated
    /*
    static write(String obj, JSONObject data, Org contextOrg) {

        return // closed ..

        def result

        // TODO check isDataManager, etc for contextOrg

        // check existing resources
        boolean conflict = false

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
*/

    static Map<String, Object> buildResponse(HttpServletRequest request, String obj, String query, String value, String context, Org contextOrg, def result) {

        Map<String, Object> response = [:]

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
        else if (Constants.OBJECT_STATUS_DELETED == result) {
            response.json = new JSON( trimJson(["message": "result not found",
                                                "obj": obj, "q": query, "v": value, "context": context,
                                                "status": HttpStatus.NOT_FOUND.value()]))
            response.status = HttpStatus.NOT_FOUND.value()
        }
        else if (! result) {
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
