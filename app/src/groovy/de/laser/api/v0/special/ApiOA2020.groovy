package de.laser.api.v0.special

import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.OrgSettings
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.SubscriptionPackage
import de.laser.api.v0.ApiReaderHelper
import de.laser.api.v0.ApiToolkit
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class ApiOA2020 {

    static private List<Org> getAccessibleOrgs() {

        List<Org> orgs = OrgSettings.executeQuery(
                "select o from OrgSettings os join os.org o where os.key = :key and os.rdValue = :rdValue " +
                        "and (o.status is null or o.status != :deleted)", [
                key    : OrgSettings.KEYS.OA2020_SERVER_ACCESS,
                rdValue: RefdataValue.getByValueAndCategory('Yes', 'YN'),
                deleted: RefdataValue.getByValueAndCategory('Deleted', 'OrgStatus')
        ])

        orgs
    }

    /**
     * @return JSON
     */
    static JSON getAllOrgs() {
        Collection<Object> result = []

        // if (requestingOrghasNoAccessDueSpecialFlag?) { return Constants.HTTP_FORBIDDEN }

        List<Org> orgs = getAccessibleOrgs()

        orgs.each{ o ->
            result << ApiReaderHelper.retrieveOrganisationStubMap(o, o)
        }

        return (result ? new JSON(result) : null)
    }

    /**
     * @return JSON
     */
    // TODO
    // TODO
    // TODO
    static JSON getOrganisation(Org org) {
        def result = [:]

        org = GrailsHibernateUtil.unwrapIfProxy(org)

        //def context = org // TODO

        result.globalUID    = org.globalUID
        result.gokbId       = org.gokbId
        result.comment      = org.comment
        result.name         = org.name
        result.scope        = org.scope
        result.shortname    = org.shortname
        result.sortname     = org.sortname
        result.federalState = org.federalState?.value
        result.country      = org.country?.value
        result.libraryType  = org.libraryType?.value

        //result.fteStudents  = org.fteStudents // TODO dc/table readerNumber
        //result.fteStaff     = org.fteStaff // TODO dc/table readerNumber

        // RefdataValues

        result.sector       = org.sector?.value
        result.type         = org.orgType?.collect{ it -> it.value }
        result.status       = org.status?.value

        // References

        //result.addresses    = ApiReaderHelper.retrieveAddressCollection(org.addresses, ApiReaderHelper.NO_CONSTRAINT) // com.k_int.kbplus.Address
        //result.contacts     = ApiReaderHelper.retrieveContactCollection(org.contacts, ApiReaderHelper.NO_CONSTRAINT) // com.k_int.kbplus.Contact
        result.identifiers  = ApiReaderHelper.retrieveIdentifierCollection(org.ids) // com.k_int.kbplus.IdentifierOccurrence
        //result.persons      = ApiReaderHelper.retrievePrsLinkCollection(
        //        org.prsLinks, ApiReaderHelper.NO_CONSTRAINT, ApiReaderHelper.NO_CONSTRAINT, context
        //) // com.k_int.kbplus.PersonRole

        //result.properties   = ApiReaderHelper.retrievePropertyCollection(org, context) // com.k_int.kbplus.(OrgCustomProperty, OrgPrivateProperty)

        result.subscriptions = retrieveSubscriptionCollection(org)

        result = ApiToolkit.cleanUp(result, true, true)

        return (result ? new JSON(result) : null)
    }

    static Collection<Object> retrieveSubscriptionCollection(Org org) {
        if (!org ) {
            return null
        }
        if (org.status?.value == 'Deleted') {
            return []
        }

        Collection<Object> result = []

        OrgRole.executeQuery(
                'select distinct(oo.sub) from OrgRole oo where oo.roleType in (:roleTypes)',
                [roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
        ).each { sub ->

            def subStub = ApiReaderHelper.requestSubscriptionStub(sub, org, true)
            if (subStub) {
                result.add(ApiToolkit.cleanUp(subStub, true, true))
            }
        }

        result = ApiToolkit.cleanUp(result, true, true)

        result
    }
}
