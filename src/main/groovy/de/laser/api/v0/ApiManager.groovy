package de.laser.api.v0


import de.laser.Doc
import de.laser.License
import de.laser.Org
import de.laser.Package
import de.laser.Platform
import de.laser.Subscription
import de.laser.api.v0.special.ApiEZB
import de.laser.finance.CostItem
import de.laser.oap.OrgAccessPoint
import de.laser.api.v0.catalogue.ApiCatalogue
import de.laser.api.v0.entities.*
import de.laser.api.v0.special.ApiOAMonitor
import de.laser.api.v0.special.ApiStatistic
import de.laser.storage.Constants
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.apache.tools.ant.util.DateUtils
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletRequest

/**
 * Manager class to handle calls; reads the data queried and builds the response object from the query result. It contains
 * only two generic methods - read() to perform the actual query and buildResponse() to deliver the output
 */
@Slf4j
class ApiManager {

    /**
     * The current version of the API. To be updated on every change which affects the output
     */
    static final VERSION = '1.5'

    /**
     * Checks if the request is valid and if, whether the permissions are granted for the context institution making
     * the request. If the requests are granted, the object and eventual stubs are being returned. See the return list
     * which cases may hold
     * @param obj the object type or list being requested
     * @param query the field used to identify the object
     * @param value the identifier value being requested
     * @param contextOrg the institution doing the request
     * @param format the format in which the response should be returned
     * @return one of:
     * <ul>
     *     <li>Object</li>
     *     <li>BAD_REQUEST: if invalid/missing (unsupported) identifier</li>
     *     <li>PRECONDITION_FAILED: if multiple matches(objects) are found</li>
     *     <li>NOT_ACCEPTABLE: if requested format(response) is not supported</li>
     *     <li>NOT_IMPLEMENTED: if requested method(object type) is not supported</li>
     * </ul>
     */
    static read(String obj, String query, String value, Org contextOrg, String format, Date changedFrom = null) {
        def result

        boolean isDatamanager = ApiToolkit.hasApiLevel(contextOrg, ApiToolkit.API_LEVEL_DATAMANAGER)
        boolean isEZB         = ApiToolkit.hasApiLevel(contextOrg, ApiToolkit.API_LEVEL_EZB)
        boolean isOAMonitor   = ApiToolkit.hasApiLevel(contextOrg, ApiToolkit.API_LEVEL_OAMONITOR)
        boolean isNatStat     = ApiToolkit.hasApiLevel(contextOrg, ApiToolkit.API_LEVEL_NATSTAT)
        boolean isInvoiceTool = ApiToolkit.hasApiLevel(contextOrg, ApiToolkit.API_LEVEL_INVOICETOOL)

        log.debug("API-READ (" + VERSION + "): ${obj} (${format}) -> ${query}:${value} ${changedFrom ? changedFrom.format(DateUtils.ISO8601_DATE_PATTERN) : ''}")

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

        /*  ---
            Naming convention
            ---

            ApiObject.findByX()         returning the matching result from db (implicit checking delete status)

            ApiObject.requestY()        returning object if access is granted (implicit checked)

            ApiObject.getZ()            returning object without access check

            ---
         */

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
        else if (checkValidRequest('ezb/license/illIndicators')) {

            if (! (isEZB || isDatamanager)) {
                return Constants.HTTP_FORBIDDEN
            }

            ApiBox tmp = ApiLicense.findLicenseBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiEZB.requestIllIndicators((License) tmp.obj)
            }
        }
        else if (checkValidRequest('ezb/subscription')) {

            if (! (isEZB || isDatamanager)) {
                return Constants.HTTP_FORBIDDEN
            }

            ApiBox tmp = ApiSubscription.findSubscriptionBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiEZB.requestSubscription((Subscription) tmp.obj, changedFrom)
            }
        }
        else if (checkValidRequest('ezb/subscription/list')) {

            if (! (isEZB || isDatamanager)) {
                return Constants.HTTP_FORBIDDEN
            }

            result = ApiEZB.getAllSubscriptions(changedFrom, contextOrg)
        }
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
        else if (checkValidRequest('oamonitor/organisations/list')) {

            if (! (isOAMonitor || isDatamanager)) {
                return Constants.HTTP_FORBIDDEN
            }

            result = ApiOAMonitor.getAllOrgs()
        }
        else if (checkValidRequest('oamonitor/organisations')) {

            if (! (isOAMonitor || isDatamanager)) {
                return Constants.HTTP_FORBIDDEN
            }
            ApiBox tmp = ApiOrg.findOrganisationBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiOAMonitor.requestOrganisation((Org) tmp.obj, contextOrg)
            }
        }
        else if (checkValidRequest('oamonitor/subscriptions')) {

            if (! (isOAMonitor || isDatamanager)) {
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
        }else if (checkValidRequest('orgAccessPoint')) {

            ApiBox tmp = ApiOrgAccessPoint.findAccessPointBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiOrgAccessPoint.requestOrgAccessPoint((OrgAccessPoint) tmp.obj, contextOrg)
            }
        }
        else if (checkValidRequest('package')) {

            ApiBox tmp = ApiPkg.findPackageBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiPkg.getPackage((Package) tmp.obj, contextOrg)
            }
        }
        else if (checkValidRequest('platform')) {

            ApiBox tmp = ApiPlatform.findPlatformBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiPlatform.getPlatform((Platform) tmp.obj, contextOrg)
            }
        }
        else if (checkValidRequest('platformList')) {

            result = ApiPlatform.getPlatformList()
        }
        else if (checkValidRequest('propertyList')) {

			result = ApiCatalogue.getAllProperties(contextOrg)
        }
        else if (checkValidRequest('refdataList')) {

            result = ApiCatalogue.getAllRefdatas()
        }
        else if (checkValidRequest('statistic/packages/list')) {

            if (! (isNatStat || isDatamanager)) {
                return Constants.HTTP_FORBIDDEN
            }

            result = ApiStatistic.getAllPackages()
        }
        else if (checkValidRequest('statistic/packages')) {

            if (! (isNatStat || isDatamanager)) {
                return Constants.HTTP_FORBIDDEN
            }
            ApiBox tmp = ApiPkg.findPackageBy(query, value)
            result = (tmp.status != Constants.OBJECT_NOT_FOUND) ? tmp.status : null // TODO: compatibility fallback; remove

            if (tmp.checkFailureCodes_3()) {
                result = ApiStatistic.requestPackage((Package) tmp.obj)
            }
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

    /**
     * Builds the response depending in the request and the requested object parameters
     * @param request the {@link HttpServletRequest} request object
     * @param obj the object type or list being requested
     * @param query the field used to identify the object requested
     * @param value the identifier value
     * @param context the request context
     * @param contextOrg the institution performing the request
     * @param result the result {@link Map} of the query
     * @return a {@link Map} containing the response status and data
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
