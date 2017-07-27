package com.k_int.kbplus.api.v0

import com.k_int.kbplus.Doc
import com.k_int.kbplus.DocContext
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.api.v0.out.*
import com.k_int.kbplus.auth.User
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.web.json.JSONObject

@Log4j
class MainService {

    static final CREATED            = "CREATED"         // 201
    static final BAD_REQUEST        = "BAD_REQUEST"     // 400
    static final FORBIDDEN          = "FORBIDDEN"       // 403
    static final CONFLICT           = "CONFLICT"        // 409
    static final INTERNAL_SERVER_ERROR  = "INTERNAL_SERVER_ERROR" // 500
    static final NOT_IMPLEMENTED        = "NOT_IMPLEMENTED" // 501

    LicenseService licenseService
    OrgService orgService
    PkgService pkgService
    SubscriptionService subscriptionService

    def read(String obj, String query, String value, User user, Org contextOrg) {
        def result

        if ('document'.equalsIgnoreCase(obj)) {
            result = findDocumentBy(query, value) // null or doc
            if (result && BAD_REQUEST != result) {
                result = getDocument((Doc) result, user, contextOrg)
            }
        } else if ('license'.equalsIgnoreCase(obj)) {
            result = findLicenseBy(query, value) // null or license
            if (result && BAD_REQUEST != result) {
                result = getLicense((License) result, user, contextOrg)
            }
        } else if ('organisation'.equalsIgnoreCase(obj)) {
            result = findOrganisationBy(query, value) // null or organisation
            if (result && BAD_REQUEST != result) {
                result = getOrganisation((Org) result, user, contextOrg)
            }
        } else if ('package'.equalsIgnoreCase(obj)) {
            result = findPackageBy(query, value) // null or package
            if (result && BAD_REQUEST != result) {
                result = getPackage((Package) result, user, contextOrg)
            }
        } else if ('subscription'.equalsIgnoreCase(obj)) {
            result = findSubscriptionBy(query, value) // null or subscription
            if (result && BAD_REQUEST != result) {
                result = getSubscription((Subscription) result, user, contextOrg)
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
            check << findOrganisationBy('shortcode', Org.generateShortcodeFunction(data.name.trim()))
            data.identifiers?.each{ it ->
                check << findOrganisationBy('identifier', it.namespace + ":" + it.value)
            }
            check.removeAll([null, BAD_REQUEST])

            if (! check.isEmpty()) {
                return ['result': CONFLICT, 'debug': check]
            }

            result = orgService.postOrganisation(data, contextOrg)
        }
        else {
            result = NOT_IMPLEMENTED
        }
        result
    }

    /**
     * @return Doc | FORBIDDEN
     */
    def findDocumentBy(String query, String value) {
        def obj
        if('id'.equalsIgnoreCase(query)) {
            obj = Doc.findWhere(id: Long.parseLong(value))
        }
        else if('uuid'.equalsIgnoreCase(query)) {
            obj = Doc.findWhere(uuid: value)
        }
        else {
            obj = BAD_REQUEST
        }
        obj
    }

    /**
     * @return License | FORBIDDEN
     */
    def findLicenseBy(String query, String value) {
        def obj
        if('id'.equalsIgnoreCase(query)) {
            obj = License.findWhere(id: Long.parseLong(value))
        }
        else if('impId'.equalsIgnoreCase(query)) {
            obj = License.findWhere(impId: value)
        }
        else {
            obj = BAD_REQUEST
        }
        obj
    }

    /**
     * @return Org | FORBIDDEN
     */
    def findOrganisationBy(String query, String value) {
        def obj
        if('id'.equalsIgnoreCase(query)) {
            obj = Org.findWhere(id: Long.parseLong(value))
        }
        else if('impId'.equalsIgnoreCase(query)) {
            obj = Org.findWhere(impId: value)
        }
        else if('identifier'.equalsIgnoreCase(query)) {
            obj = Org.lookupByIdentifierString(value)
        }
        else if('shortcode'.equalsIgnoreCase(query)) {
            obj = Org.findWhere(shortcode: value)
        }
        else {
            obj = BAD_REQUEST
        }
        obj
    }

    /**
     * @return Package | FORBIDDEN
     */
    def findPackageBy(String query, String value) {
        def obj
        if('id'.equalsIgnoreCase(query)) {
            obj = Package.findWhere(id: Long.parseLong(value))
        }
        else if('identifier'.equalsIgnoreCase(query)) { // != identifiers
            obj = Package.findWhere(identifier: value)
        }
        else if('impId'.equalsIgnoreCase(query)) {
            obj = Package.findWhere(impId: value)
        }
        else {
            obj = BAD_REQUEST
        }
        obj
    }

    /**
     * @return Subscription | FORBIDDEN
     */
    def findSubscriptionBy(String query, String value) {
        def obj
        if('id'.equalsIgnoreCase(query)) {
            obj = Subscription.findWhere(id: Long.parseLong(value))
        }
        else if('identifier'.equalsIgnoreCase(query)) { // != identifiers
            obj = Subscription.findWhere(identifier: value)
        }
        else if('impId'.equalsIgnoreCase(query)) {
            obj = Subscription.findWhere(impId: value)
        }
        else {
            obj = BAD_REQUEST
        }
        obj
    }

    /**
     * @return Doc | FORBIDDEN
     */
    def getDocument(Doc doc, User user, Org context){
        def hasAccess = false

        DocContext.findAllByOwner(doc).each{ dc ->
            if(dc.license) {
                dc.getLicense().getOrgLinks().each { orgRole ->
                    // TODO check orgRole.roleType
                    if(orgRole.getOrg().id == context?.id) {
                        hasAccess = true
                    }
                }
            }
            if(dc.pkg) {
                dc.getPkg().getOrgs().each { orgRole ->
                    // TODO check orgRole.roleType
                    if(orgRole.getOrg().id == context?.id) {
                        hasAccess = true
                    }
                }
            }
            if(dc.subscription) {
                dc.getSubscription().getOrgRelations().each { orgRole ->
                    // TODO check orgRole.roleType
                    if(orgRole.getOrg().id == context?.id) {
                        hasAccess = true
                    }
                }
            }
        }
        return (hasAccess ? doc : FORBIDDEN)
    }

    /**
     * @return grails.converters.JSON | FORBIDDEN
     */
    def getLicense(License lic, User user, Org context){
        def hasAccess = false

        lic.orgLinks.each{ orgRole ->
            if(orgRole.getOrg().id == context?.id) {
                hasAccess = true
            }
        }

        def result = []
        if(hasAccess) {
            result = licenseService.resolveLicense(lic, ExportHelperService.IGNORE_NONE, context) // TODO check orgRole.roleType
        }

        return (hasAccess ? new JSON(result) : FORBIDDEN)
    }

    /**
     * @return grails.converters.JSON | FORBIDDEN
     */
    def getOrganisation(Org org, User user, Org context) {
        def hasAccess = true
        def result = orgService.getOrganisation(org, context)

        return (hasAccess ? new JSON(result) : FORBIDDEN)
    }

    /**
     * @return grails.converters.JSON | FORBIDDEN
     */
    def getPackage(Package pkg, User user, Org context) {
        def hasAccess = true

        //pkg.orgs.each{ orgRole ->
        //    if(orgRole.getOrg().id == context?.id) {
        //        hasAccess = true
        //    }
        //}

        def result = []
        if (hasAccess) {
            result = pkgService.resolvePackage(pkg, context) // TODO check orgRole.roleType
        }

        return (hasAccess ? new JSON(result) : FORBIDDEN)
    }

    /**
     * @return grails.converters.JSON | FORBIDDEN
     */
    def getSubscription(Subscription sub, User user, Org context){
        def hasAccess = false

        sub.orgRelations.each{ orgRole ->
            if(orgRole.getOrg().id == context?.id) {
                hasAccess = true
            }
        }

        def result = []
        if (hasAccess) {
            result = subscriptionService.resolveSubscription(sub, context) // TODO check orgRole.roleType
        }

        return (hasAccess ? new JSON(result) : FORBIDDEN)
    }
}
