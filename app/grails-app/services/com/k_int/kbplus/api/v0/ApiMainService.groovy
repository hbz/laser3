package com.k_int.kbplus.api.v0

import com.k_int.kbplus.Doc
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.api.v0.converter.ApiKbartService
import com.k_int.kbplus.api.v0.entities.DocService
import com.k_int.kbplus.api.v0.entities.IssueEntitlementService
import com.k_int.kbplus.api.v0.entities.LicenseService
import com.k_int.kbplus.api.v0.entities.OrgService
import com.k_int.kbplus.api.v0.entities.PkgService
import com.k_int.kbplus.api.v0.entities.SubscriptionService
import com.k_int.kbplus.auth.User
import de.laser.domain.Constants
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletRequest

@Log4j
class ApiMainService {

    ApiWriteService apiWriteService

    DocService docService
    IssueEntitlementService issueEntitlementService
    LicenseService licenseService
    OrgService orgService
    PkgService pkgService
    SubscriptionService subscriptionService

    ApiKbartService apiKbartService

    /**
     * @return Object | BAD_REQUEST | PRECONDITION_FAILED | NOT_ACCEPTABLE
     */
    def read(String obj, String query, String value, User user, Org contextOrg, String format) {
        def result
        log.debug("API-READ: ${obj} (${format}) @ ${query}:${value}")

        if ('document'.equalsIgnoreCase(obj)) {
            //if (format in [ContentType.ALL]) {
                result = docService.findDocumentBy(query, value)
                if (result && !(result in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED])) {
                    result = docService.getDocument((Doc) result, user, contextOrg)
                }
            //}
        }
        else if ('issueEntitlements'.equalsIgnoreCase(obj)) {
            if (format in [Constants.MIME_TEXT_PLAIN, Constants.MIME_APPLICATION_JSON]) {
                def subPkg = issueEntitlementService.findSubscriptionPackageBy(query, value)
                if (subPkg && ! (subPkg in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED]) ) {
                    result = issueEntitlementService.getIssueEntitlements(subPkg, user, contextOrg)

                    if (format == Constants.MIME_TEXT_PLAIN) {
                        def kbart = apiKbartService.convertIssueEntitlements(result)
                        result = apiKbartService.getAsDocument(kbart)
                    }
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('license'.equalsIgnoreCase(obj)) {
            if (format in [Constants.MIME_APPLICATION_JSON]) {
                result = licenseService.findLicenseBy(query, value)
                if (result && !(result in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED])) {
                    result = licenseService.getLicense((License) result, user, contextOrg)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('onixpl'.equalsIgnoreCase(obj)) {
            if (format in [Constants.MIME_APPLICATION_XML]) {
                def lic = licenseService.findLicenseBy(query, value)
                if (lic && !(lic in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED])) {
                    result = docService.getOnixPlDocument((License) lic, user, contextOrg)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('organisation'.equalsIgnoreCase(obj)) {
            if (format in [Constants.MIME_APPLICATION_JSON]) {
                result = orgService.findOrganisationBy(query, value)
                if (result && !(result in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED])) {
                    result = orgService.getOrganisation((Org) result, user, contextOrg)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('package'.equalsIgnoreCase(obj)) {
            if (format in [Constants.MIME_APPLICATION_JSON]) {
                result = pkgService.findPackageBy(query, value)
                if (result && !(result in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED])) {
                    result = pkgService.getPackage((Package) result, user, contextOrg)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('subscription'.equalsIgnoreCase(obj)) {
            if (format in [Constants.MIME_APPLICATION_JSON]) {
                result = subscriptionService.findSubscriptionBy(query, value)
                if (result && !(result in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED])) {
                    result = subscriptionService.getSubscription((Subscription) result, user, contextOrg)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else {
            result = Constants.HTTP_NOT_IMPLEMENTED
        }

        result
    }

    def write(String obj, JSONObject data, User user, Org contextOrg) {
        def result

        // check existing resources
        def conflict = false

        if ('organisation'.equalsIgnoreCase(obj)) {

            data.identifiers?.each { ident ->
                def hits = orgService.findOrganisationBy('ns:identifier', ident.namespace + ":" + ident.value)
                if (hits == Constants.HTTP_PRECONDITION_FAILED || hits instanceof Org) {
                    conflict = true
                }
            }
            def hits = orgService.findOrganisationBy('name', data.name.trim())
            if (hits == Constants.HTTP_PRECONDITION_FAILED || hits instanceof Org) {
                conflict = true
            }

            if (conflict) {
                return ['result': Constants.HTTP_CONFLICT, 'debug': 'debug']
            }

            result = apiWriteService.importOrganisation(data, contextOrg)
        }
        else if ('license'.equalsIgnoreCase(obj)) {

            result = apiWriteService.importLicense(data, contextOrg)
        }
        else if ('subscription'.equalsIgnoreCase(obj)) {

            data.identifiers?.each { ident ->
                def hits = subscriptionService.findSubscriptionBy('ns:identifier', ident.namespace + ":" + ident.value)
                if (hits == Constants.HTTP_PRECONDITION_FAILED || hits instanceof Subscription) {
                    conflict = true
                }
            }
            def hits = subscriptionService.findSubscriptionBy('identifier', data.identifier)
            if (hits == Constants.HTTP_PRECONDITION_FAILED || hits instanceof Subscription) {
                conflict = true
            }

            if (conflict) {
                return ['result': Constants.HTTP_CONFLICT, 'debug': 'debug']
            }

            result = apiWriteService.importSubscription(data, contextOrg)
        }
        else {
            result = Constants.HTTP_NOT_IMPLEMENTED
        }
        result
    }

    def buildResponseBody(HttpServletRequest request, def obj, def query, def value, def context, def contextOrg, def result) {

        // POST

        if (result instanceof HashMap) {

            switch(result['result']) {
                case Constants.HTTP_CREATED:
                    result = new JSON(["message": "resource successfully created", "debug": result['debug'], "_httpStatus": HttpStatus.CREATED.value()])
                    break
                case Constants.HTTP_CONFLICT:
                    result = new JSON(["message": "conflict with existing resource", "debug": result['debug'], "_httpStatus": HttpStatus.CONFLICT.value()])
                    break
                case Constants.HTTP_INTERNAL_SERVER_ERROR:
                    result = new JSON(["message": "resource not created", "debug": result['debug'], "_httpStatus": HttpStatus.INTERNAL_SERVER_ERROR.value()])
                    break
            }
        }

        // GET

        else if (Constants.HTTP_FORBIDDEN == result) {
            if (contextOrg) {
                result = new JSON(["message": "forbidden", "obj": obj, "q": query, "v": value, "context": contextOrg.shortcode, "_httpStatus": HttpStatus.FORBIDDEN.value()])
            }
            else {
                result = new JSON(["message": "forbidden", "obj": obj, "context": context, "_httpStatus": HttpStatus.FORBIDDEN.value()])
            }
        }
        else if (Constants.HTTP_NOT_ACCEPTABLE == result) {
            result = new JSON(["message": "requested format not supported", "method": request.method, "accept": request.getHeader('accept'), "obj": obj, "_httpStatus": HttpStatus.NOT_ACCEPTABLE.value()])
        }
        else if (Constants.HTTP_NOT_IMPLEMENTED == result) {
            result = new JSON(["message": "requested method not implemented", "method": request.method, "obj": obj, "_httpStatus": HttpStatus.NOT_IMPLEMENTED.value()])
        }
        else if (Constants.HTTP_BAD_REQUEST == result) {
            result = new JSON(["message": "invalid/missing identifier or post body", "obj": obj, "q": query, "context": context, "_httpStatus": HttpStatus.BAD_REQUEST.value()])
        }
        else if (Constants.HTTP_PRECONDITION_FAILED == result) {
            result = new JSON(["message": "precondition failed; multiple matches", "obj": obj, "q": query, "context": context, "_httpStatus": HttpStatus.PRECONDITION_FAILED.value()])
        }

        if (! result) {
            result = new JSON(["message": "object not found", "obj": obj, "q": query, "v": value, "context": context, "_httpStatus": HttpStatus.NOT_FOUND.value()])
        }
        else {
            result.target.put("_httpStatus", HttpStatus.OK.value()) // TODO
        }

        result
    }
}
