package de.laser

import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Subscription
import de.laser.helper.RDStore

class OrgTypeService {

    def contextService

    /**
     * @return List<Org> with orgType 'Agency'; generic
     */
    List<Org> getOrgsForTypeAgency() {
        List<Org> result = Org.executeQuery(
                "select o from Org o join o.orgType as rt where rt.value = 'Agency' order by lower(o.sortname), o.name"
        )
        result.unique()
    }

    /**
     * @return List<Org> with orgType 'Provider'; generic
     */
    List<Org> getOrgsForTypeProvider() {
        List<Org> result = Org.executeQuery(
                "select o from Org o join o.orgType as rt where rt.value = 'Provider' order by lower(o.sortname), o.name"
        )
        result.unique()
    }

    /**
     * @return List<Org> with orgType in ('Agency, Broker, Content Provider, Provider, Vendor'); generic
     */
    List<Org> getOrgsForTypeLicensor() {
        List<Org> result = Org.executeQuery(
                "select o from Org o join o.orgType as rt where rt.value in ('Agency', 'Broker', 'Content Provider', 'Provider', 'Vendor') order by lower(o.sortname), o.name"
        )
        result.unique()
    }

    /**
     * @return List<License> with accessible (my) licenses
     */
    List<License> getCurrentLicenses(Org context) {
        return License.executeQuery( """
            select l from License as l join l.orgLinks as ogr where
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
    List<Subscription> getCurrentSubscriptions(Org context) {
        String query = "select s from Subscription as s join s.orgRelations as ogr where ( s = ogr.sub and ogr.org = :subOrg ) and ( ogr.roleType in (:roleTypes) )"

        return Subscription.executeQuery( query, [
                subOrg: context,
                roleTypes: [
                    RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_COLLECTIVE,
                    RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIPTION_COLLECTIVE
            ]]
        )
    }

    /**
     * @return List<Long> with accessible (my) subscription ids
     */
    List<Long> getCurrentSubscriptionIds(Org context) {
        getCurrentSubscriptions(context).collect{ it.id }
    }

    /**
     * @return List<Org> with OrgRole relations (type 'Licensor') depending on current (my) subscriptions
     */
    List<Org> getCurrentLicensors(Org context) {
        List<Org> result = []
        List<License> current = getCurrentLicenses(context)

        if (current) {
            result = OrgRole.findAll("from OrgRole where lic in (:licenses) and roleType = :licensor",
                    [licenses: current,
                     licensor: RDStore.OR_LICENSOR]
            ).collect { it.org }
        }

        result.unique()
    }

    /**
     * @Deprecated If possible, use getCurrentOrgsOfProvidersAndAgencies or getCurrentOrgIdsOfProvidersAndAgencies instead,
     * because these new methods are significantly faster.
     * @return List<Org> with OrgRole relations (type 'Agency') depending on current (my) subscriptions
     */
    @Deprecated
    List<Org> getCurrentAgencies(Org context) {
        List<Org> result = []
        List<Subscription> current = getCurrentSubscriptions(context)

        if (current) {
            result = OrgRole.findAll("from OrgRole where sub in (:subscriptions) and roleType = :agency",
                    [subscriptions: current,
                     agency       : RDStore.OR_AGENCY]
            ).collect { it.org }
        }

        result.unique()
    }

    /**
     * @Deprecated If possible, use getCurrentOrgsOfProvidersAndAgencies or getCurrentOrgIdsOfProvidersAndAgencies instead
     * because these new methods are significantly faster.
     * @return List<Org> with OrgRole relations (type 'Provider') depending on current (my) subscriptions
     */
    @Deprecated
    List<Org> getCurrentProviders(Org context) {
        List<Org> result = []
        List<Subscription> current = getCurrentSubscriptions(context)

        if (current) {
            result = OrgRole.findAll("from OrgRole where sub in (:subscriptions) and roleType = :provider",
                    [subscriptions: current,
                     provider     : RDStore.OR_PROVIDER]
            ).collect { it.org }
        }

        result.unique()
    }

    List<Org> getCurrentOrgsOfProvidersAndAgencies(Org context) {
        List<Org> result = []
        List<Subscription> current = getCurrentSubscriptions(context)

        if (current) {
            result = OrgRole.findAll(
                    "from OrgRole where sub in (:subscriptions) and roleType in (:provider, :agency)",
                    [subscriptions: current,
                     provider     : RDStore.OR_PROVIDER,
                     agency       : RDStore.OR_AGENCY]
            ).collect { it.org }
        }

        result.unique()
    }

    List<Long> getCurrentOrgIdsOfProvidersAndAgencies(Org context) {
        List<Long> result = []
        List<Subscription> current = getCurrentSubscriptions(context)

        if (current) {
            result = OrgRole.executeQuery(""" 
                     select distinct(o.id) from OrgRole orgr 
                     left join orgr.org as o 
                         where orgr.sub in (:subscriptions) 
                         and orgr.roleType in (:provider, :agency)""",
                    [subscriptions: current,
                     provider     : RDStore.OR_PROVIDER,
                     agency       : RDStore.OR_AGENCY]
            )
        }
        result
    }

}
