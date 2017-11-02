package com.k_int.kbplus.api.v0

import com.k_int.kbplus.*
import de.laser.domain.Constants
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.transaction.TransactionStatus

@Log4j
class ApiWriteService {

    ApiWriteHelperService apiWriteHelperService

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

                license.startDate   = apiWriteHelperService.getValidDateFormat(data.startDate)
                license.endDate     = apiWriteHelperService.getValidDateFormat(data.endDate)
                // todo: license.lastmod     = data.lastmod // long ????

                // RefdataValues
                license.isPublic         = apiWriteHelperService.getRefdataValue(data.isPublic, "YN")
                license.licenseCategory  = apiWriteHelperService.getRefdataValue(data.licenseCategory, "LicenseCategory")
                license.status           = apiWriteHelperService.getRefdataValue(data.status, "License Status")
                license.type             = apiWriteHelperService.getRefdataValue(data.type, "License Type")
                license.ids              = apiWriteHelperService.getIdentifiers(data.identifiers, license) // implicit creation of identifier and namespace

                // References
                def properties            = apiWriteHelperService.getProperties(data.properties, license, context)
                license.customProperties  = properties['custom']
                license.privateProperties = properties['private']

                // not supported: license.documents
                // not supported: license.onixplLicense

                // TO CHECK: save license before saving orgLinks
                license.save()

                license.orgLinks = apiWriteHelperService.getOrgLinks(data.organisations, license, context)

                // TODO: set subscription.owner = license
                //def subscriptions = inHelperService.getSubscriptions(data.subscriptions)

                license.save(flush: true)
                result = ['result': Constants.HTTP_CREATED, 'debug': license]
            }
            catch (Exception e) {
                log.error("Error while importing LICENSE via API; rollback forced")
                log.error(e)
                status.setRollbackOnly()
                result = ['result': Constants.HTTP_INTERNAL_SERVER_ERROR, 'debug': e]
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
                org.sector  = apiWriteHelperService.getRefdataValue(data.sector, "OrgSector")
                org.status  = apiWriteHelperService.getRefdataValue(data.status, "OrgStatus") // TODO unknown catagory !!!
                org.orgType = apiWriteHelperService.getRefdataValue(data.type, "OrgType")

                // References
                org.addresses = apiWriteHelperService.getAddresses(data.addresses, org, null)
                org.contacts  = apiWriteHelperService.getContacts(data.contacts, org, null)
                org.ids       = apiWriteHelperService.getIdentifiers(data.identifiers, org) // implicit creation of identifier and namespace

                def properties        = apiWriteHelperService.getProperties(data.properties, org, context)
                org.customProperties  = properties['custom']
                org.privateProperties = properties['private']

                // MUST: save org before saving persons and prsLinks
                org.save()

                def personsAndRoles = apiWriteHelperService.getPersonsAndRoles(data.persons, org, context)
                personsAndRoles['persons'].each { p ->
                    (Person) p.save() // MUST: save persons before saving prsLinks
                }

                org.prsLinks        = personsAndRoles['personRoles']

                org.save(flush: true)
                result = ['result': Constants.HTTP_CREATED, 'debug': org]
            }
            catch (Exception e) {
                log.error("Error while importing ORG via API; rollback forced")
                log.error(e)
                status.setRollbackOnly()
                result = ['result': Constants.HTTP_INTERNAL_SERVER_ERROR, 'debug': e]
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
                sub.startDate   = apiWriteHelperService.getValidDateFormat(data.startDate)
                sub.endDate     = apiWriteHelperService.getValidDateFormat(data.endDate)
                sub.manualRenewalDate = apiWriteHelperService.getValidDateFormat(data.manualRenewalDate)

                // RefdataValues
                sub.isSlaved  = apiWriteHelperService.getRefdataValue(data.isSlaved, "YN")
                sub.isPublic  = apiWriteHelperService.getRefdataValue(data.isPublic, "YN")
                sub.status    = apiWriteHelperService.getRefdataValue(data.isSlaved, "Subscription Status")
                sub.type      = apiWriteHelperService.getRefdataValue(data.isSlaved, "Organisational Role")

                // References
                def properties       = apiWriteHelperService.getProperties(data.properties, sub, context)
                sub.customProperties = properties['custom']
                sub.ids              = apiWriteHelperService.getIdentifiers(data.identifiers, sub) // implicit creation of identifier and namespace

                // TO CHECK: save subscriptions before saving orgRelations
                sub.save()

                sub.orgRelations     = apiWriteHelperService.getOrgLinks(data.organisations, sub, context)

                // not supported: documents
                // not supported: derivedSubscriptions
                // not supported: instanceOf
                // not supported: license
                // not supported: packages

                sub.save(flush: true)
                result = ['result': Constants.HTTP_CREATED, 'debug': sub]
            }
            catch (Exception e) {
                log.error("Error while importing SUBSCRIPTION via API; rollback forced")
                log.error(e)

                status.setRollbackOnly()
                result = ['result': Constants.HTTP_INTERNAL_SERVER_ERROR, 'debug': e]
            }
        }
        result
    }
}
