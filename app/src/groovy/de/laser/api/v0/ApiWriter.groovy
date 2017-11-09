package de.laser.api.v0

import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Person
import com.k_int.kbplus.Subscription
import de.laser.domain.Constants
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.transaction.TransactionStatus

@Log4j
class ApiWriter {

    /**
     *
     * @param JSONObject data
     * @param com.k_int.kbplus.Org context
     * @return
     */
    static importLicense(JSONObject data, Org context) {
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

                license.startDate   = ApiWriterHelper.getValidDateFormat(data.startDate)
                license.endDate     = ApiWriterHelper.getValidDateFormat(data.endDate)
                // todo: license.lastmod     = data.lastmod // long ????

                // RefdataValues
                license.isPublic         = ApiWriterHelper.getRefdataValue(data.isPublic, "YN")
                license.licenseCategory  = ApiWriterHelper.getRefdataValue(data.licenseCategory, "LicenseCategory")
                license.status           = ApiWriterHelper.getRefdataValue(data.status, "License Status")
                license.type             = ApiWriterHelper.getRefdataValue(data.type, "License Type")
                license.ids              = ApiWriterHelper.getIdentifiers(data.identifiers, license) // implicit creation of identifier and namespace

                // References
                def properties            = ApiWriterHelper.getProperties(data.properties, license, context)
                license.customProperties  = properties['custom']
                license.privateProperties = properties['private']

                // not supported: license.documents
                // not supported: license.onixplLicense

                // TO CHECK: save license before saving orgLinks
                license.save()

                license.orgLinks = ApiWriterHelper.getOrgLinks(data.organisations, license, context)

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
    static importOrganisation(JSONObject data, Org context) {
        def result = []

        Org.withTransaction { TransactionStatus status ->

            try {
                def org = new Org(
                        name: data.name,
                        comment: data.comment,
                        scope: data.scope
                )

                // RefdataValues
                org.sector  = ApiWriterHelper.getRefdataValue(data.sector, "OrgSector")
                org.status  = ApiWriterHelper.getRefdataValue(data.status, "OrgStatus") // TODO unknown catagory !!!
                org.orgType = ApiWriterHelper.getRefdataValue(data.type, "OrgType")

                // References
                org.addresses = ApiWriterHelper.getAddresses(data.addresses, org, null)
                org.contacts  = ApiWriterHelper.getContacts(data.contacts, org, null)
                org.ids       = ApiWriterHelper.getIdentifiers(data.identifiers, org) // implicit creation of identifier and namespace

                def properties        = ApiWriterHelper.getProperties(data.properties, org, context)
                org.customProperties  = properties['custom']
                org.privateProperties = properties['private']

                // MUST: save org before saving persons and prsLinks
                org.save()

                def personsAndRoles = ApiWriterHelper.getPersonsAndRoles(data.persons, org, context)
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
    static importSubscription(JSONObject data, Org context) {
        def result = []

        Subscription.withTransaction { TransactionStatus status ->

            try {
                def sub = new Subscription(
                        name:                   data.name,
                        cancellationAllowances: data.cancellationAllowances,
                        identifier:             data.identifier,
                )
                sub.startDate   = ApiWriterHelper.getValidDateFormat(data.startDate)
                sub.endDate     = ApiWriterHelper.getValidDateFormat(data.endDate)
                sub.manualRenewalDate = ApiWriterHelper.getValidDateFormat(data.manualRenewalDate)

                // RefdataValues
                sub.isSlaved  = ApiWriterHelper.getRefdataValue(data.isSlaved, "YN")
                sub.isPublic  = ApiWriterHelper.getRefdataValue(data.isPublic, "YN")
                sub.status    = ApiWriterHelper.getRefdataValue(data.isSlaved, "Subscription Status")
                sub.type      = ApiWriterHelper.getRefdataValue(data.isSlaved, "Organisational Role")

                // References
                def properties       = ApiWriterHelper.getProperties(data.properties, sub, context)
                sub.customProperties = properties['custom']
                sub.ids              = ApiWriterHelper.getIdentifiers(data.identifiers, sub) // implicit creation of identifier and namespace

                // TO CHECK: save subscriptions before saving orgRelations
                sub.save()

                sub.orgRelations     = ApiWriterHelper.getOrgLinks(data.organisations, sub, context)

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
