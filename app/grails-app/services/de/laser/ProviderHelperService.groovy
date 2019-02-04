package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Subscription
import de.laser.helper.RDStore

class ProviderHelperService {

    def contextService

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

    def getCurrentProviders(Org context) {
        return OrgRole.findAll("from OrgRole where sub in (:subscriptions) and roleType = :provider",
                [subscriptions: getCurrentSubscriptions(context),
                 provider: RDStore.OR_PROVIDER]
        )
    }

    def getCurrentAgencies(Org context) {
        return OrgRole.findAll("from OrgRole where sub in (:subscriptions) and roleType = :agency",
                [subscriptions: getCurrentSubscriptions(context),
                 agency: RDStore.OR_AGENCY]
        )
    }
}
