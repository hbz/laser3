package de.laser


import de.laser.helper.RDStore
import grails.gorm.transactions.Transactional

/**
 * This service is specifically for organisation type calls
 */
@Transactional
class OrgTypeService {

    ContextService contextService

    /**
     * @return List<Org> with orgType 'Agency'; generic
     */
    List<Org> getOrgsForTypeAgency() {
        List<Org> result = Org.executeQuery(
                "select o from Org o join o.orgType as rt where rt.value = 'Agency' order by lower(o.shortname), o.name"
        )
        result.unique()
    }

    /**
     * @return List<Org> with orgType 'Provider'; generic
     */
    List<Org> getOrgsForTypeProvider() {
        List<Org> result = Org.executeQuery(
                "select o from Org o join o.orgType as rt where rt.value = 'Provider' order by lower(o.shortname), o.name"
        )
        result.unique()
    }

    /**
     * @return List<Org> with orgType in ('Agency, Broker, Content Provider, Provider, Vendor'); generic
     */
    Collection<Org> getOrgsForTypeLicensor() {
        Set<Org> result = Org.executeQuery("select o from Org o join o.orgType as rt where rt.value in ('Agency', 'Broker', 'Content Provider', 'Provider', 'Vendor') order by o.name, lower(o.sortname)")
        result
    }

    /**
     * @return List<License> with accessible (my) licenses
     */
    @Deprecated
    List<License> getCurrentLicenses(Org context) {
        return License.executeQuery( """
            select l from License as l join l.orgRelations as ogr where
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
        return Subscription.executeQuery("select oo.sub from OrgRole oo where oo.org = :subOrg and oo.roleType in (:roleTypes)", [subOrg: context, roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA]])
    }

    /**
     * @return List<Long> with accessible (my) subscription ids
     */
    List<Long> getCurrentSubscriptionIds(Org context) {
        return Subscription.executeQuery("select oo.sub.id from OrgRole oo where oo.org = :subOrg and oo.roleType in (:roleTypes)", [subOrg: context, roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA]])
    }

    /**
     * @return List<Org> with OrgRole relations (type 'Licensor') depending on current (my) subscriptions
     */
    @Deprecated
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
     * @deprecated If possible, use getCurrentOrgsOfProvidersAndAgencies or getCurrentOrgIdsOfProvidersAndAgencies instead,
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
     * @deprecated If possible, use getCurrentOrgsOfProvidersAndAgencies or getCurrentOrgIdsOfProvidersAndAgencies instead
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

    /**
     * Gets organisations which are linked either as providers or as agencies to subscriptions
     * @param context the institution whose subscriptions should be considered
     * @return a result of full {@link Org} objects
     */
    Set<Org> getCurrentOrgsOfProvidersAndAgencies(Org context) {
        Set<Org> result = Org.executeQuery("select oo.org from OrgRole oo where oo.sub in (select sub from OrgRole where org = :context and roleType in (:roleTypes)) and oo.roleType in (:providerTypes) order by oo.org.name asc",
                    [context:context,roleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER],providerTypes: [RDStore.OR_PROVIDER,RDStore.OR_AGENCY]])
        result
    }

    /**
     * Gets organisation IDs which are linked either as providers or as agencies to subscriptions.
     * This is the more performant duplicate of {@link #getCurrentOrgsOfProvidersAndAgencies(de.laser.Org)}
     * @param context the institution whose subscriptions should be considered
     * @return a result of {@link Org} IDs
     */
    Set<Long> getCurrentOrgIdsOfProvidersAndAgencies(Org context) {
        Set<Long> result = OrgRole.executeQuery("select o.id from OrgRole oo join oo.org as o where oo.sub in (select sub from OrgRole where org = :context and roleType in (:roleTypes)) and oo.roleType in (:providerTypes)",
                    [context:context,roleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER],providerTypes: [RDStore.OR_PROVIDER,RDStore.OR_AGENCY]])
        result
    }

}
