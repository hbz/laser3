package de.laser

import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Subscription
import de.laser.helper.RDStore

class OrgTypeService {

    def contextService

    /**
     * @return List<Org> with orgRoleType 'Agency'; generic
     */
    def getOrgsForTypeAgency() {
        Org.executeQuery(
                "select o from Org o join o.orgRoleType as rt where rt.value = 'Agency' order by lower(o.sortname), o.name"
        )
    }

    /**
     * @return List<Org> with orgRoleType 'Provider'; generic
     */
    def getOrgsForTypeProvider() {
        Org.executeQuery(
                "select o from Org o join o.orgRoleType as rt where rt.value = 'Provider' order by lower(o.sortname), o.name"
        )
    }

    /**
     * @return List<Org> with orgRoleType in ('Agency, Broker, Content Provider, Provider, Vendor'); generic
     */
    def getOrgsForTypeLicensor() {
        Org.executeQuery(
                "select o from Org o join o.orgRoleType as rt where rt.value in ('Agency', 'Broker', 'Content Provider', 'Provider', 'Vendor') order by lower(o.sortname), o.name"
        )
    }

    /**
     * @return List<License> with accessible (my) licenses
     */
    def getCurrentLicenses(Org context) {
        return License.executeQuery( """
            select l from License as l join l.orgLinks as ogr where
                ( l.status.value != 'Deleted' ) and
                ( l = ogr.lic and ogr.org = :licOrg ) and
                ( ogr.roleType = (:roleLic) or ogr.roleType = (:roleLicCons) or ogr.roleType = (:roleLicConsortia) )
        """, [licOrg: context,
              roleLic: RDStore.OR_LICENSEE,
              roleLicCons: RDStore.OR_LICENSEE_CONS,
              roleLicConsortia: RDStore.OR_LICENSING_CONSORTIUM]
        )
    }

    /**
     * @return List<Subscription> with accessible (my) subscriptions
     */
    def getCurrentSubscriptions(Org context) {
        return Subscription.executeQuery( """
            select s from Subscription as s join s.orgRelations as ogr where
                ( s.status.value != 'Deleted' ) and
                ( s = ogr.sub and ogr.org = :subOrg ) and
                ( ogr.roleType = (:roleSub) or ogr.roleType = (:roleSubCons) or ogr.roleType = (:roleSubConsortia) )
        """, [subOrg: context,
              roleSub: RDStore.OR_SUBSCRIBER,
              roleSubCons: RDStore.OR_SUBSCRIBER_CONS,
              roleSubConsortia: RDStore.OR_SUBSCRIPTION_CONSORTIA]
        )
    }

    /**
     * @return List<Org> with OrgRole relations (type 'Agency') depending on current (my) subscriptions
     */
    def getCurrentAgencies(Org context) {
        List<Org> result = OrgRole.findAll("from OrgRole where sub in (:subscriptions) and roleType = :agency",
                [subscriptions: getCurrentSubscriptions(context),
                 agency: RDStore.OR_AGENCY]
        ).collect{ it -> it.org }

        result.unique()
    }

    /**
     * @return List<Org> with OrgRole relations (type 'Licensor') depending on current (my) subscriptions
     */
    def getCurrentLicensors(Org context) {
        List<Org> result = OrgRole.findAll("from OrgRole where lic in (:licenses) and roleType = :licensor",
                [licenses: getCurrentLicenses(context),
                 licensor: RDStore.OR_LICENSOR]
        ).collect{ it -> it.org }

        result.unique()
    }

    /**
     * @return List<Org> with OrgRole relations (type 'Provider') depending on current (my) subscriptions
     */
    def getCurrentProviders(Org context) {
        List<Org> result = OrgRole.findAll("from OrgRole where sub in (:subscriptions) and roleType = :provider",
                [subscriptions: getCurrentSubscriptions(context),
                 provider: RDStore.OR_PROVIDER]
        ).collect{ it -> it.org }

        result.unique()
    }
}
