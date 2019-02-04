package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Subscription
import de.laser.helper.RDStore

class ProviderHelperService {

    def contextService

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
     * @return List<Org> with OrgRole relations (type 'Provider') depending on current (my) subscriptions
     */
    def getCurrentProviders(Org context) {
        List<Org> result = OrgRole.findAll("from OrgRole where sub in (:subscriptions) and roleType = :provider",
                [subscriptions: getCurrentSubscriptions(context),
                 provider: RDStore.OR_PROVIDER]
        ).collect{ it -> it.org }

        result.unique()
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
     * @return List<Org> with orgRoleType 'Provider'; generic
     */
    def getAllWithTypeProvider() {
        Org.executeQuery(
                "select o from Org o join o.orgRoleType as rt where rt.value = 'Provider' order by lower(o.sortname), o.name"
        )
    }

    /**
     * @return List<Org> with orgRoleType 'Agency'; generic
     */
    def getAllWithTypeAgency() {
        Org.executeQuery(
                "select o from Org o join o.orgRoleType as rt where rt.value = 'Agency' order by lower(o.sortname), o.name"
        )
    }
}
