package de.laser.api.v0


import de.laser.License
import de.laser.Org
import de.laser.Person
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.helper.Constants
import de.laser.helper.RDConstants
import groovy.util.logging.Slf4j
import org.grails.web.json.JSONObject
import org.springframework.transaction.TransactionStatus

@Deprecated
@Slf4j
class ApiWriter {

    /**
     *
     * @param JSONObject data
     * @param com.k_int.kbplus.Org context
     * @return
     */
    @Deprecated
    static importLicense(JSONObject data, Org context) {
        def result = []

        License.withTransaction { TransactionStatus status ->

            try {
                def license = new License(
                        // removed - contact:            data.contact,
                        licenseUrl:         data.licenseUrl,
                        // removed - licensorRef:        data.licensorRef,
                        // removed - licenseeRef:        data.licenseeRef,
                        //licenseType:        data.licenseType,
                        licenseStatus:      data.licenseStatus,
                        noticePeriod:       data.noticePeriod,
                        reference:          data.reference,
                        sortableReference:  data.normReference,
                )

                license.startDate   = ApiWriterHelper.getValidDateFormat(data.startDate)
                license.endDate     = ApiWriterHelper.getValidDateFormat(data.endDate)
                // todo: license.lastmod     = data.lastmod // long ????

                ['Yes','yes']
                // RefdataValues
                license.licenseCategory  = RefdataValue.getByValueAndCategory(data.licenseCategory, RDConstants.LICENSE_CATEGORY)
                license.status           = RefdataValue.getByValueAndCategory(data.status, RDConstants.LICENSE_STATUS)
                license.ids              = ApiWriterHelper.getIdentifiers(data.identifiers, license) // implicit creation of identifier and namespace

                // References
                def properties            = ApiWriterHelper.getProperties(data.properties, license, context)
                license.propertySet  = properties['custom']
                //license.privateProperties = properties['private']

                // not supported: license.documents
                // not supported: license.onixplLicense

                // TO CHECK: save license before saving orgRelations
                license.save()

                license.orgRelations = ApiWriterHelper.getOrgRelations(data.organisations, license, context)

                // TODO: set subscription.owner = license
                //def subscriptions = inHelperService.getSubscriptions(data.subscriptions)

                license.save()
                result = ['result': Constants.HTTP_CREATED, 'debug': license]
            }
            catch (Exception e) {
                log.error("Error while importing LICENSE via API; rollback forced")
                log.error( e.toString() )
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
    @Deprecated
    static importOrganisation(JSONObject data, Org context) {
        def result = []

        Org.withTransaction { TransactionStatus status ->

            try {
                Org org = new Org(
                        name: data.name,
                        comment: data.comment,
                        scope: data.scope
                )

                // RefdataValues
                org.sector  = RefdataValue.getByValueAndCategory(data.sector, RDConstants.ORG_SECTOR)
                org.status  = RefdataValue.getByValueAndCategory(data.status, RDConstants.ORG_STATUS)
                //org.orgType = ApiWriterHelper.getRefdataValue(data.type, "OrgType")

                // References
                org.addresses = ApiWriterHelper.getAddresses(data.addresses, org, null)
                org.contacts  = ApiWriterHelper.getContacts(data.contacts, org, null)
                org.ids       = ApiWriterHelper.getIdentifiers(data.identifiers, org) // implicit creation of identifier and namespace

                def properties        = ApiWriterHelper.getProperties(data.properties, org, context)
                org.propertySet  = properties['custom']
                org.privateProperties = properties['private']

                // MUST: save org before saving persons and prsLinks
                org.save()

                def personsAndRoles = ApiWriterHelper.getPersonsAndRoles(data.persons, org, context)
                personsAndRoles['persons'].each { p ->
                    (Person) p.save() // MUST: save persons before saving prsLinks
                }

                org.prsLinks        = personsAndRoles['personRoles']

                org.save()
                result = ['result': Constants.HTTP_CREATED, 'debug': org]
            }
            catch (Exception e) {
                log.error("Error while importing ORG via API; rollback forced")
                log.error( e.toString() )
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
    @Deprecated
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
                sub.isSlaved  =  data.isSlaved in ['Yes','yes']
                //sub.isPublic  = ApiWriterHelper.getRefdataValue(data.isPublic, RDConstants.Y_N) // legacy
                //sub.status    = ApiWriterHelper.getRefdataValue(data.isSlaved, RDConstants.SUBSCRIPTION_STATUS)
                //sub.type      = ApiWriterHelper.getRefdataValue(data.isSlaved, "Organisational Role")

                // References
                def properties       = ApiWriterHelper.getProperties(data.properties, sub, context)
                sub.propertySet = properties['custom']
                sub.ids              = ApiWriterHelper.getIdentifiers(data.identifiers, sub) // implicit creation of identifier and namespace

                // TO CHECK: save subscriptions before saving orgRelations
                sub.save()

                sub.orgRelations     = ApiWriterHelper.getOrgRelations(data.organisations, sub, context)

                // not supported: documents
                // not supported: derivedSubscriptions
                // not supported: instanceOf
                // not supported: license
                // not supported: packages

                sub.save()
                result = ['result': Constants.HTTP_CREATED, 'debug': sub]
            }
            catch (Exception e) {
                log.error("Error while importing SUBSCRIPTION via API; rollback forced")
                log.error( e.toString() )

                status.setRollbackOnly()
                result = ['result': Constants.HTTP_INTERNAL_SERVER_ERROR, 'debug': e]
            }
        }
        result
    }
}
