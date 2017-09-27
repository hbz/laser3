package com.k_int.kbplus.api.v0

import com.k_int.kbplus.Doc
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.api.v0.base.InService
import com.k_int.kbplus.auth.User
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.web.json.JSONObject

@Log4j
class MainService {

    static final CREATED                = "CREATED"         // 201
    static final BAD_REQUEST            = "BAD_REQUEST"     // 400
    static final FORBIDDEN              = "FORBIDDEN"       // 403
    static final CONFLICT               = "CONFLICT"        // 409
    static final PRECONDITION_FAILED    = "PRECONDITION_FAILED"   // 412
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
            result = docService.findDocumentBy(query, value)
            if (result && ! (result in [BAD_REQUEST, PRECONDITION_FAILED]) ) {
                result = docService.getDocument((Doc) result, user, contextOrg)
            }
        } else if ('license'.equalsIgnoreCase(obj)) {
            result = licenseService.findLicenseBy(query, value)
            if (result && ! (result in [BAD_REQUEST, PRECONDITION_FAILED]) ) {
                result = licenseService.getLicense((License) result, user, contextOrg)
            }
        }
        else if ('onixpl'.equalsIgnoreCase(obj)) {
            // TODO
            result = licenseService.findLicenseBy(query, value)
            if (result && ! (result in [BAD_REQUEST, PRECONDITION_FAILED]) ) {
                result = docService.getOnixPlDocument((License) result, user, contextOrg)
            }
        }
        else if ('organisation'.equalsIgnoreCase(obj)) {
            result = orgService.findOrganisationBy(query, value)
            if (result && ! (result in [BAD_REQUEST, PRECONDITION_FAILED]) ) {
                result = orgService.getOrganisation((Org) result, user, contextOrg)
            }
        } else if ('package'.equalsIgnoreCase(obj)) {
            result = pkgService.findPackageBy(query, value)
            if (result && ! (result in [BAD_REQUEST, PRECONDITION_FAILED]) ) {
                result = pkgService.getPackage((Package) result, user, contextOrg)
            }
        } else if ('subscription'.equalsIgnoreCase(obj)) {
            result = subscriptionService.findSubscriptionBy(query, value)
            if (result && ! (result in [BAD_REQUEST, PRECONDITION_FAILED]) ) {
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
            def conflict = false

            data.identifiers?.each { ident ->
                def hits = orgService.findOrganisationBy('ns:identifier', ident.namespace + ":" + ident.value)
                if (hits == PRECONDITION_FAILED || hits instanceof Org) {
                    conflict = true
                }
            }
            def hits = orgService.findOrganisationBy('name', data.name.trim())
            if (hits == PRECONDITION_FAILED || hits instanceof Org) {
                conflict = true
            }

            if (conflict) {
                return ['result': CONFLICT, 'debug': 'debug']
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
                if (hits == PRECONDITION_FAILED || hits instanceof Subscription) {
                    conflict = true
                }
            }
            def hits = subscriptionService.findSubscriptionBy('identifier', data.identifier)
            if (hits == PRECONDITION_FAILED || hits instanceof Subscription) {
                conflict = true
            }

            if (conflict) {
                return ['result': CONFLICT, 'debug': 'debug']
            }

            result = inService.importSubscription(data, contextOrg)
        }
        else {
            result = NOT_IMPLEMENTED
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
