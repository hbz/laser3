package de.laser.api.v0.special

import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.OrgSettings
import com.k_int.kbplus.RefdataValue
import de.laser.api.v0.ApiCollectionReader
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiToolkit
import de.laser.api.v0.ApiStubReader
import de.laser.helper.Constants
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class ApiOAMonitor {

    static boolean calculateAccess(Org result, Org context, boolean hasAccess) {

        // context is ignored due hasAccess = accessDueDatamanager
        // maybe changed later into a lesser accessRole like API_LEVEL_OA2020

        if (! hasAccess) {
            def resultSetting = OrgSettings.get(result, OrgSettings.KEYS.OAMONITOR_SERVER_ACCESS)

            if (resultSetting != OrgSettings.SETTING_NOT_FOUND && resultSetting.getValue()?.value == 'Yes') {
                hasAccess = true
            }
            else {
                hasAccess = false
            }
        }

        hasAccess
    }

    static private List<Org> getAccessibleOrgs() {

        List<Org> orgs = OrgSettings.executeQuery(
                "select o from OrgSettings os join os.org o where os.key = :key and os.rdValue = :rdValue " +
                        "and (o.status is null or o.status != :deleted)", [
                key    : OrgSettings.KEYS.OAMONITOR_SERVER_ACCESS,
                rdValue: RefdataValue.getByValueAndCategory('Yes', 'YN'),
                deleted: RefdataValue.getByValueAndCategory('Deleted', 'OrgStatus')
        ])

        orgs
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static getAllOrgs(boolean hasAccess) {
        Collection<Object> result = []

        if (hasAccess) {
            List<Org> orgs = getAccessibleOrgs()

            orgs.each { o ->
                result << ApiStubReader.retrieveOrganisationStubMap(o, o)
            }
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static getOrganisation(Org org, Org context, boolean hasAccess) {
        Map<String, Object> result = [:]
        hasAccess = calculateAccess(org, context, hasAccess)

        if (hasAccess) {

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
            result.lastUpdated  = org.lastUpdated

            //result.fteStudents  = org.fteStudents // TODO dc/table readerNumber
            //result.fteStaff     = org.fteStaff // TODO dc/table readerNumber

            // RefdataValues

            result.sector       = org.sector?.value
            result.type         = org.orgType?.collect{ it.value }
            result.status       = org.status?.value

            // References

            //result.addresses    = ApiCollectionReader.retrieveAddressCollection(org.addresses, ApiReader.NO_CONSTRAINT) // com.k_int.kbplus.Address
            //result.contacts     = ApiCollectionReader.retrieveContactCollection(org.contacts, ApiReader.NO_CONSTRAINT) // com.k_int.kbplus.Contact
            result.identifiers  = ApiCollectionReader.retrieveIdentifierCollection(org.ids) // com.k_int.kbplus.Identifier
            //result.persons      = ApiCollectionReader.retrievePrsLinkCollection(
            //        org.prsLinks, ApiCollectionReader.NO_CONSTRAINT, ApiCollectionReader.NO_CONSTRAINT, context
            //) // com.k_int.kbplus.PersonRole

            result.properties    = ApiCollectionReader.retrievePropertyCollection(org, context, ApiReader.IGNORE_PRIVATE_PROPERTIES) // com.k_int.kbplus.(OrgCustomProperty, OrgPrivateProperty)
            result.subscriptions = retrieveSubscriptionCollection(org)

            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    static private Collection<Object> retrieveSubscriptionCollection(Org org) {
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
            result.add(ApiStubReader.requestSubscriptionStub(sub, org, true))
        }

        result = ApiToolkit.cleanUp(result, true, true)

        result
    }
}
