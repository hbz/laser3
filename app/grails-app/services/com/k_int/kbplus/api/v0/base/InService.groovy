package com.k_int.kbplus.api.v0.base

import com.k_int.kbplus.*
import com.k_int.kbplus.api.v0.MainService
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.transaction.TransactionStatus

@Log4j
class InService {

    InHelperService inHelperService

    /**
     *
     * @param JSONObject data
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def importLicense(JSONObject data, Org context) {
        def result = []

        License.withTransaction { TransactionStatus status ->

            try {
                def license = new License(
                        contact:            data.contact,
                        licenseUrl:         data.licenseUrl,
                        licensorRef:        data.licensorRef,
                        licenseeRef:        data.licenseeRef,
                        licenseType:        data.licenseType,
                        licenseStatus:      data.licenseStatus,
                        noticePeriod:       data.noticePeriod,
                        reference:          data.reference,
                        sortableReference:  data.sortableReference,
                )

                license.startDate   = inHelperService.getValidDateFormat(data.startDate)
                license.endDate     = inHelperService.getValidDateFormat(data.endDate)
                // todo: license.lastmod     = data.lastmod // long ????

                // RefdataValues
                license.isPublic         = inHelperService.getRefdataValue(data.isPublic, "YN")
                license.licenseCategory  = inHelperService.getRefdataValue(data.licenseCategory, "LicenseCategory")
                license.status           = inHelperService.getRefdataValue(data.status, "License Status")
                license.type             = inHelperService.getRefdataValue(data.type, "License Type")

                // References
                def properties           = inHelperService.getProperties(data.properties, license, context)
                license.customProperties = properties['custom']

                // not supported: license.documents
                // not supported: license.onixplLicense

                // TO CHECK: save license before saving orgLinks
                license.save()

                license.orgLinks = inHelperService.getOrgLinks(data.organisations, license, context)

                // TODO: set subscription.owner = license
                //def subscriptions = inHelperService.getSubscriptions(data.subscriptions)

                license.save(flush: true)
                result = ['result': MainService.CREATED, 'debug': license]
            }
            catch (Exception e) {
                log.error("Error while importing LICENSE via API; rollback forced")
                log.error(e)
                status.setRollbackOnly()
                result = ['result': MainService.INTERNAL_SERVER_ERROR, 'debug': e]
            }
        }
        result
    }

    /**
     *
     * @param JSONObject data
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def importOrganisation(JSONObject data, Org context) {
        def result = []

        Org.withTransaction { TransactionStatus status ->

            try {
                def org = new Org(
                        name: data.name,
                        comment: data.comment,
                        scope: data.scope
                )

                // RefdataValues
                org.sector  = inHelperService.getRefdataValue(data.sector, "OrgSector")
                org.status  = inHelperService.getRefdataValue(data.status, "OrgStatus") // TODO unknown catagory !!!
                org.orgType = inHelperService.getRefdataValue(data.type, "OrgType")

                // References
                org.addresses = inHelperService.getAddresses(data.addresses, org, null)
                org.contacts  = inHelperService.getContacts(data.contacts, org, null)
                org.ids       = inHelperService.getIdentifiers(data.identifiers, org) // implicit creation of identifier and namespace

                def properties        = inHelperService.getProperties(data.properties, org, context)
                org.customProperties  = properties['custom']
                org.privateProperties = properties['private']

                // MUST: save org before saving persons and prsLinks
                org.save()

                def personsAndRoles = inHelperService.getPersonsAndRoles(data.persons, org, context)
                personsAndRoles['persons'].each { p ->
                    (Person) p.save() // MUST: save persons before saving prsLinks
                }

                org.prsLinks        = personsAndRoles['personRoles']

                org.save(flush: true)
                result = ['result': MainService.CREATED, 'debug': org]
            }
            catch (Exception e) {
                log.error("Error while importing ORG via API; rollback forced")
                log.error(e)
                status.setRollbackOnly()
                result = ['result': MainService.INTERNAL_SERVER_ERROR, 'debug': e]
            }
        }
        result
    }

    /**
     *
     * @param JSONObject data
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def importSubscription(JSONObject data, Org context) {
        def result = []

        Subscription.withTransaction { TransactionStatus status ->

            try {
                def sub = new Subscription(
                        name:                   data.name,
                        cancellationAllowances: data.cancellationAllowances,
                        identifier:             data.identifier,
                )
                sub.startDate   = inHelperService.getValidDateFormat(data.startDate)
                sub.endDate     = inHelperService.getValidDateFormat(data.endDate)
                sub.manualRenewalDate = inHelperService.getValidDateFormat(data.manualRenewalDate)

                // RefdataValues
                sub.isSlaved  = inHelperService.getRefdataValue(data.isSlaved, "YN")
                sub.isPublic  = inHelperService.getRefdataValue(data.isPublic, "YN")
                sub.status    = inHelperService.getRefdataValue(data.isSlaved, "Subscription Status")
                sub.type      = inHelperService.getRefdataValue(data.isSlaved, "Organisational Role")

                // References
                def properties       = inHelperService.getProperties(data.properties, sub, context)
                sub.customProperties = properties['custom']
                sub.ids              = inHelperService.getIdentifiers(data.identifiers, sub) // implicit creation of identifier and namespace

                // TO CHECK: save subscriptions before saving orgRelations
                sub.save()

                sub.orgRelations     = inHelperService.getOrgLinks(data.organisations, sub, context)

                // not supported: documents
                // not supported: derivedSubscriptions
                // not supported: instanceOf
                // not supported: license
                // not supported: packages

                sub.save(flush: true)
                result = ['result': MainService.CREATED, 'debug': sub]
            }
            catch (Exception e) {
                log.error("Error while importing SUBSCRIPTION via API; rollback forced")
                log.error(e)

                status.setRollbackOnly()
                result = ['result': MainService.INTERNAL_SERVER_ERROR, 'debug': e]
            }
        }
        result
    }
}
