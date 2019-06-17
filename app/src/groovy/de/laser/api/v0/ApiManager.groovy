package de.laser.api.v0

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.User
import de.laser.api.v0.catalogue.ApiCatalogue
import de.laser.api.v0.entities.*
import de.laser.api.v0.special.ApiOA2020
import de.laser.api.v0.special.ApiStatistic
import de.laser.helper.Constants
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.http.HttpStatus
import org.apache.commons.lang.RandomStringUtils

import javax.servlet.http.HttpServletRequest

@Log4j
class ApiManager {

    static final VERSION = '0.54'
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
        def accessDueDatamanager = ApiToolkit.isDataManager(contextOrg)

        log.debug("API-READ (" + VERSION + "): ${obj} (${format}) -> ${query}:${value}")

        if ('costItem'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.costItem) {
                def costItem = ApiCostItem.findCostItemBy(query, value)
                if (costItem && !(costItem in failureCodes)) {
                    result = ApiCostItem.getCostItem((CostItem) costItem, contextOrg, accessDueDatamanager)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('costItemList'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.costItem) {

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
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('document'.equalsIgnoreCase(obj)) {
            //if (format in ApiReader.SUPPORTED_FORMATS.document) {
                result = ApiDoc.findDocumentBy(query, value)
                if (result && !(result in failureCodes)) {
                    result = ApiDoc.getDocument((Doc) result, contextOrg, accessDueDatamanager)
                }
            //}
        }
        else if (NOT_SUPPORTED && 'issueEntitlements'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.issueEntitlements) {
                def subPkg = ApiIssueEntitlement.findSubscriptionPackageBy(query, value)
                if (subPkg && !(subPkg in failureCodes) ) {
                    result = ApiIssueEntitlement.getIssueEntitlements(subPkg, contextOrg, accessDueDatamanager)

                    if (result && format == Constants.MIME_TEXT_PLAIN) {
                        def kbart = ApiKbartConverter.convertIssueEntitlements(result)
                        result = ApiKbartConverter.getAsDocument(kbart)
                    }
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('license'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.license) {
                result = ApiLicense.findLicenseBy(query, value)

                if (result && !(result in failureCodes)) {
                    result = ApiLicense.getLicense((License) result, contextOrg, accessDueDatamanager)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('licenseList'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.license) {
                result = ApiOrg.findOrganisationBy(query, value) // use of http status code
                if (result && !(result in failureCodes)) {
                    result = ApiLicense.getLicenseList(result, contextOrg, accessDueDatamanager)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('oa2020'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.oa2020) {
                result = ApiOrg.findOrganisationBy(query, value)

                if (result && !(result in failureCodes)) {
                    def ssa = OrgSettings.get(result, OrgSettings.KEYS.STATISTICS_SERVER_ACCESS)

                    if (ssa != OrgSettings.SETTING_NOT_FOUND && ssa.getValue()?.value == 'Yes') {
                        result = ApiOA2020.getDummy()
                    }
                    else {
                        result = Constants.HTTP_FORBIDDEN
                    }
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('oa2020List'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.oa2020) {
                result = ApiOA2020.getAllOrgs()
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if (NOT_SUPPORTED && 'onixpl'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.onixpl) {
                def lic = ApiLicense.findLicenseBy(query, value)

                if (lic && !(lic in failureCodes)) {
                    result = ApiDoc.getOnixPlDocument((License) lic, contextOrg, accessDueDatamanager)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('organisation'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.organisation) {
                result = ApiOrg.findOrganisationBy(query, value)

                if (result && !(result in failureCodes)) {
                    result = ApiOrg.getOrganisation((Org) result, contextOrg, accessDueDatamanager)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if (NOT_SUPPORTED && 'package'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.package) {
                result = ApiPkg.findPackageBy(query, value)

                if (result && !(result in failureCodes)) {
                    result = ApiPkg.getPackage((Package) result, contextOrg, accessDueDatamanager)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('refdataList'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.refdataList) {
                result = ApiCatalogue.getAllRefdatas()
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('statistic'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.statistic) {
                result = ApiPkg.findPackageBy(query, value) // use of http status code

                if (result && !(result in failureCodes)) {
                    // TODO: def ssa = OrgSettings.get(result, OrgSettings.KEYS.STATISTICS_SERVER_ACCESS)

                    //if (ssa != OrgSettings.SETTING_NOT_FOUND && ssa.getValue()?.value == 'Yes') {
                        result = ApiStatistic.getPackage(result)
                    //}
                    //else {
                    //    result = Constants.HTTP_FORBIDDEN
                    //}
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('statisticList'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.statistic) {
                //result = ApiStatistic.getAllOrgs()
                result = ApiStatistic.getAllPackages()
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('subscription'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.subscription) {
                result = ApiSubscription.findSubscriptionBy(query, value)

                if (result && !(result in failureCodes)) {
                    result = ApiSubscription.getSubscription((Subscription) result, contextOrg, accessDueDatamanager)
                }
            }
            else {
                return Constants.HTTP_NOT_ACCEPTABLE
            }
        }
        else if ('subscriptionList'.equalsIgnoreCase(obj)) {
            if (format in ApiReader.SUPPORTED_FORMATS.subscription) {
                result = ApiOrg.findOrganisationBy(query, value) // use of http status code
                if (result && !(result in failureCodes)) {
                    result = ApiSubscription.getSubscriptionList(result, contextOrg, accessDueDatamanager)
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

    @Deprecated
    static write(String obj, JSONObject data, Org contextOrg) {
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

    static buildResponse(HttpServletRequest request, String obj, String query, String value, String context, Org contextOrg, def result) {

        def response = []

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
                    response << new JSON( trimJson(["message": "resource successfully created", "debug": result['debug']]))
                    response << HttpStatus.CREATED.value()
                    break
                case Constants.HTTP_CONFLICT:
                    response << new JSON( trimJson(["message": "conflict with existing resource", "debug": result['debug']]))
                    response << HttpStatus.CONFLICT.value()
                    break
                case Constants.HTTP_INTERNAL_SERVER_ERROR:
                    response << new JSON( trimJson(["message": "resource not created", "debug": result['debug']]))
                    response << HttpStatus.INTERNAL_SERVER_ERROR.value()
                    break
            }
        }

        // GET

        else if (Constants.HTTP_FORBIDDEN == result) {
            if (contextOrg) {
                response << new JSON( trimJson(["message": "forbidden", "obj": obj, "q": query, "v": value, "context": contextOrg.shortcode]))
                response << HttpStatus.FORBIDDEN.value()
            }
            else {
                response << new JSON( trimJson(["message": "forbidden", "obj": obj, "context": context]))
                response << HttpStatus.FORBIDDEN.value()
            }
        }
        else if (Constants.HTTP_NOT_ACCEPTABLE == result) {
            response << new JSON( trimJson(["message": "requested format not supported", "method": request.method, "accept": request.getHeader('accept'), "obj": obj]))
            response << HttpStatus.NOT_ACCEPTABLE.value()
        }
        else if (Constants.HTTP_NOT_IMPLEMENTED == result) {
            response << new JSON( trimJson(["message": "requested method not implemented", "method": request.method, "obj": obj]))
            response << HttpStatus.NOT_IMPLEMENTED.value()
        }
        else if (Constants.HTTP_BAD_REQUEST == result) {
            response << new JSON( trimJson(["message": "invalid/missing identifier or post body", "obj": obj, "q": query, "context": context]))
            response << HttpStatus.BAD_REQUEST.value()
        }
        else if (Constants.HTTP_PRECONDITION_FAILED == result) {
            response << new JSON( trimJson(["message": "precondition failed; multiple matches", "obj": obj, "q": query, "context": context]))
            response << HttpStatus.PRECONDITION_FAILED.value()
        }

        if (! result) {
            response << new JSON( trimJson(["message": "result not found", "obj": obj, "q": query, "v": value, "context": context]))
            response << HttpStatus.NOT_FOUND.value()
        }
        else {
            if (result instanceof List) {
                response << new JSON(result)
            }
            else {
                response << result
            }
            response << HttpStatus.OK.value()
        }

        response
    }
}
