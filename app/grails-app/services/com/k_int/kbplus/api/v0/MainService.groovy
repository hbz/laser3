package com.k_int.kbplus.api.v0

import com.k_int.kbplus.Doc
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.api.v0.base.InService
import com.k_int.kbplus.api.v0.converter.KbartService
import com.k_int.kbplus.auth.User
import de.laser.domain.Constants
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.web.json.JSONObject

@Log4j
class MainService {

    InService inService

    DocService docService
    IssueEntitlementService issueEntitlementService
    LicenseService licenseService
    OrgService orgService
    PkgService pkgService
    SubscriptionService subscriptionService

    KbartService kbartService

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
                        def kbart = kbartService.convertIssueEntitlements(result)
                        result = kbartService.getAsDocument(kbart)
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

        if ('organisation'.equalsIgnoreCase(obj)) {

            // check existing resources
            def conflict = false

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

            result = inService.importOrganisation(data, contextOrg)
        }
        else if ('license'.equalsIgnoreCase(obj)) {

            result = inService.importLicense(data, contextOrg)
        }
        else if ('subscription'.equalsIgnoreCase(obj)) {

            // check existing resources
            def conflict = false

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

            result = inService.importSubscription(data, contextOrg)
        }
        else {
            result = Constants.HTTP_NOT_IMPLEMENTED
        }
        result
    }

    /*
    def hasAffiliation(User user, Org org, Org context) {
        def hit = false

        user.authorizedAffiliations.each { uo -> //  com.k_int.kbplus.auth.UserOrg
            def affOrg = Org.findWhere(id: uo.org.id)
            if (affOrg == context) {
                hit = true
            }
        }
        hit
    }*/
}
