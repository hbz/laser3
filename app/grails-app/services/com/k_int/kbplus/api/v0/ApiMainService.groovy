package com.k_int.kbplus.api.v0

import com.k_int.kbplus.Doc
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.api.v0.converter.ApiKbartService
import com.k_int.kbplus.auth.User
import de.laser.api.v0.ApiWriter
import de.laser.api.v0.entities.ApiDoc
import de.laser.api.v0.entities.ApiIssueEntitlement
import de.laser.api.v0.entities.ApiLicense
import de.laser.api.v0.entities.ApiOrg
import de.laser.api.v0.entities.ApiPkg
import de.laser.api.v0.entities.ApiSubscription
import de.laser.domain.Constants
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletRequest

@Log4j
class ApiMainService {

    ApiKbartService apiKbartService

    /**
     * @return Object | BAD_REQUEST | PRECONDITION_FAILED | NOT_ACCEPTABLE
     */
    def read(String obj, String query, String value, User user, Org contextOrg, String format) {
        def result
        log.debug("API-READ: ${obj} (${format}) @ ${query}:${value}")

        if ('document'.equalsIgnoreCase(obj)) {
            //if (format in [ContentType.ALL]) {
                result = ApiDoc.findDocumentBy(query, value)
                if (result && !(result in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED])) {
                    result = ApiDoc.getDocument((Doc) result, user, contextOrg)
                }
            //}
        }
        else if ('issueEntitlements'.equalsIgnoreCase(obj)) {
            if (format in [Constants.MIME_TEXT_PLAIN, Constants.MIME_APPLICATION_JSON]) {
                def subPkg = ApiIssueEntitlement.findSubscriptionPackageBy(query, value)
                if (subPkg && ! (subPkg in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED]) ) {
                    result = ApiIssueEntitlement.getIssueEntitlements(subPkg, user, contextOrg)

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
                result = ApiLicense.findLicenseBy(query, value)
                if (result && !(result in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED])) {
                    result = ApiLicense.getLicense((License) result, user, contextOrg)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('onixpl'.equalsIgnoreCase(obj)) {
            if (format in [Constants.MIME_APPLICATION_XML]) {
                def lic = ApiLicense.findLicenseBy(query, value)
                if (lic && !(lic in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED])) {
                    result = ApiDoc.getOnixPlDocument((License) lic, user, contextOrg)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('organisation'.equalsIgnoreCase(obj)) {
            if (format in [Constants.MIME_APPLICATION_JSON]) {
                result = ApiOrg.findOrganisationBy(query, value)
                if (result && !(result in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED])) {
                    result = ApiOrg.getOrganisation((Org) result, user, contextOrg)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('package'.equalsIgnoreCase(obj)) {
            if (format in [Constants.MIME_APPLICATION_JSON]) {
                result = ApiPkg.findPackageBy(query, value)
                if (result && !(result in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED])) {
                    result = ApiPkg.getPackage((Package) result, user, contextOrg)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('subscription'.equalsIgnoreCase(obj)) {
            if (format in [Constants.MIME_APPLICATION_JSON]) {
                result = ApiSubscription.findSubscriptionBy(query, value)
                if (result && !(result in [Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED])) {
                    result = ApiSubscription.getSubscription((Subscription) result, user, contextOrg)
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
        else if ('license'.equalsIgnoreCase(obj)) {

            result = ApiWriter.importLicense(data, contextOrg)
        }
        else if ('subscription'.equalsIgnoreCase(obj)) {

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

    def buildResponse(HttpServletRequest request, def obj, def query, def value, def context, def contextOrg, def result) {

        def response = []

        // POST

        if (result instanceof HashMap) {

            switch(result['result']) {
                case Constants.HTTP_CREATED:
                    response << new JSON(["message": "resource successfully created", "debug": result['debug']])
                    response << HttpStatus.CREATED.value()
                    break
                case Constants.HTTP_CONFLICT:
                    response << new JSON(["message": "conflict with existing resource", "debug": result['debug']])
                    response << HttpStatus.CONFLICT.value()
                    break
                case Constants.HTTP_INTERNAL_SERVER_ERROR:
                    response << new JSON(["message": "resource not created", "debug": result['debug']])
                    response << HttpStatus.INTERNAL_SERVER_ERROR.value()
                    break
            }
        }

        // GET

        else if (Constants.HTTP_FORBIDDEN == result) {
            if (contextOrg) {
                response << new JSON(["message": "forbidden", "obj": obj, "q": query, "v": value, "context": contextOrg.shortcode])
                response << HttpStatus.FORBIDDEN.value()
            }
            else {
                response << new JSON(["message": "forbidden", "obj": obj, "context": context])
                response << HttpStatus.FORBIDDEN.value()
            }
        }
        else if (Constants.HTTP_NOT_ACCEPTABLE == result) {
            response << new JSON(["message": "requested format not supported", "method": request.method, "accept": request.getHeader('accept'), "obj": obj])
            response << HttpStatus.NOT_ACCEPTABLE.value()
        }
        else if (Constants.HTTP_NOT_IMPLEMENTED == result) {
            response << new JSON(["message": "requested method not implemented", "method": request.method, "obj": obj])
            response << HttpStatus.NOT_IMPLEMENTED.value()
        }
        else if (Constants.HTTP_BAD_REQUEST == result) {
            response << new JSON(["message": "invalid/missing identifier or post body", "obj": obj, "q": query, "context": context])
            response << HttpStatus.BAD_REQUEST.value()
        }
        else if (Constants.HTTP_PRECONDITION_FAILED == result) {
            response << new JSON(["message": "precondition failed; multiple matches", "obj": obj, "q": query, "context": context])
            response << HttpStatus.PRECONDITION_FAILED.value()
        }

        if (! result) {
            response << new JSON(["message": "object not found", "obj": obj, "q": query, "v": value, "context": context])
            response << HttpStatus.NOT_FOUND.value()
        }
        else {
            response << result
            response << HttpStatus.OK.value()
        }

        response
    }
}
