package com.k_int.kbplus.api.v0

import com.k_int.kbplus.Doc
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.api.v0.in.InService
import com.k_int.kbplus.auth.User
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.web.json.JSONObject

@Log4j
class MainService {

    static final CREATED                = "CREATED"         // 201
    static final BAD_REQUEST            = "BAD_REQUEST"     // 400
    static final FORBIDDEN              = "FORBIDDEN"       // 403
    static final CONFLICT               = "CONFLICT"        // 409
    static final INTERNAL_SERVER_ERROR  = "INTERNAL_SERVER_ERROR" // 500
    static final NOT_IMPLEMENTED        = "NOT_IMPLEMENTED" // 501

    InService inService

    DocService docService
    LicenseService licenseService
    OrgService orgService
    PkgService pkgService
    SubscriptionService subscriptionService

    def read(String obj, String query, String value, User user, Org contextOrg) {
        def result

        if ('document'.equalsIgnoreCase(obj)) {
            result = docService.findDocumentBy(query, value) // null or doc
            if (result && BAD_REQUEST != result) {
                result = docService.getDocument((Doc) result, user, contextOrg)
            }
        } else if ('license'.equalsIgnoreCase(obj)) {
            result = licenseService.findLicenseBy(query, value) // null or license
            if (result && BAD_REQUEST != result) {
                result = licenseService.getLicense((License) result, user, contextOrg)
            }
        } else if ('organisation'.equalsIgnoreCase(obj)) {
            result = orgService.findOrganisationBy(query, value) // null or organisation
            if (result && BAD_REQUEST != result) {
                result = orgService.getOrganisation((Org) result, user, contextOrg)
            }
        } else if ('package'.equalsIgnoreCase(obj)) {
            result = pkgService.findPackageBy(query, value) // null or package
            if (result && BAD_REQUEST != result) {
                result = pkgService.getPackage((Package) result, user, contextOrg)
            }
        } else if ('subscription'.equalsIgnoreCase(obj)) {
            result = subscriptionService.findSubscriptionBy(query, value) // null or subscription
            if (result && BAD_REQUEST != result) {
                result = subscriptionService.getSubscription((Subscription) result, user, contextOrg)
            }
        }
        else {
            result = NOT_IMPLEMENTED
        }

        result
    }

    def write(String obj, JSONObject data, User user, Org contextOrg) {
        def result

        if ('organisation'.equalsIgnoreCase(obj)) {

            // check existing resources
            def check = []
            def org
            data.identifiers?.each { orgIdent ->
                check << orgService.findOrganisationBy('identifier', orgIdent.namespace + ":" + orgIdent.value) // TODO returns null if multiple matches !!
            }
            check.removeAll([null, BAD_REQUEST])
            check.each { orgCandidate ->
                if (orgCandidate.name.equals(data.name.trim()))  {
                    org = orgCandidate
                }
            }
            if (org) {
                return ['result': CONFLICT, 'debug': check]
            }

            result = inService.importOrganisation(data, contextOrg)
        }
        else if ('license'.equalsIgnoreCase(obj)) {

            result = inService.importLicense(data, contextOrg)
        }
        else if ('subscription'.equalsIgnoreCase(obj)) {

            // TODO: check existing resources via ns:identifier
            def sub = subscriptionService.findSubscriptionBy('identifier', data.identifier)
            if (sub) {
                return ['result': CONFLICT, 'debug': sub]
            }

            result = inService.importSubscription(data, contextOrg)
        }
        else {
            result = NOT_IMPLEMENTED
        }
        result
    }

}
