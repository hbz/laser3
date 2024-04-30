package de.laser

import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class ProviderService {

    DeletionService deletionService
    GokbService gokbService

    /**
     * Gets a (filtered) map of provider records from the we:kb
     * @param params the request parameters
     * @param result a result generics map, containing also configuration params for the request
     * @return a {@link Map} of structure [providerUUID: providerRecord] containing the request results
     */
    Map<String, Map> getWekbProviderRecords(GrailsParameterMap params, Map result) {
        Map<String, Map> records = [:], queryParams = [componentType: 'Org']
        if (params.curatoryGroup || params.providerRole) {
            if (params.curatoryGroup)
                queryParams.curatoryGroupExact = params.curatoryGroup.replaceAll('&', 'ampersand').replaceAll('\\+', '%2B').replaceAll(' ', '%20')
            if (params.providerRole)
                queryParams.role = RefdataValue.get(params.providerRole).value.replaceAll(' ', '%20')
        }
        Map<String, Object> wekbResult = gokbService.doQuery(result, [max: 10000, offset: 0], queryParams)
        if(wekbResult.recordsCount > 0)
            records.putAll(wekbResult.records.collectEntries { Map wekbRecord -> [wekbRecord.uuid, wekbRecord] })
        records
    }

    /**
     * should be a batch process, triggered by DBM change script, but should be triggerable for Yodas as well
     * Changes provider orgs ({@link Org}s defined as such) into {@link Provider}s
     */
    void migrateProviders() {
        Org.withTransaction { ts ->
            Set<Org> providers = Org.executeQuery('select o from Org o join o.orgType ot where ot = :provider', [provider: RDStore.OT_PROVIDER])
            providers.each { Org provider ->
                Provider.convertFromOrg(provider)
                provider.orgType.remove(RDStore.OT_PROVIDER)
                provider.save()
            }
            Platform.findAllByOrgIsNotNull().each { Platform plat ->
                plat.provider = Provider.convertFromOrg(plat.org)
                plat.org = null
                plat.save()
            }
            Set<OrgRole> providerRelations = OrgRole.findAllByRoleTypeInListAndIsShared([RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER], false)
            providerRelations.each { OrgRole or ->
                Provider p = Provider.findByGlobalUID(or.org.globalUID.replace(Org.class.simpleName.toLowerCase(), Provider.class.simpleName.toLowerCase()))
                if(!p) {
                    p = Provider.convertFromOrg(or.org)
                    or.org.orgType.remove(RDStore.OT_PROVIDER)
                    or.org.save()
                }
                if(or.sub && !ProviderRole.findByProviderAndSubscription(p, or.sub)) {
                    ProviderRole pr = new ProviderRole(provider: p, subscription: or.sub)
                    if(pr.save()) {
                        if(or.isShared) {
                            pr.addShareForTarget_trait(or.sub)
                        }
                        log.debug("processed: ${pr.provider}:${pr.subscription} ex ${or.org}:${or.sub}")
                    }
                    else log.error(pr.errors.getAllErrors().toListString())
                }
                else if(or.lic && !ProviderRole.findByProviderAndLicense(p, or.lic)) {
                    ProviderRole pr = new ProviderRole(provider: p, license: or.lic)
                    if(pr.save()) {
                        if(or.isShared) {
                            pr.addShareForTarget_trait(or.lic)
                        }
                        log.debug("processed: ${pr.provider}:${pr.license} ex ${or.org}:${or.lic}")
                    }
                    else log.error(pr.errors.getAllErrors().toListString())
                }
                else if(or.pkg) {
                    Package pkg = or.pkg
                    pkg.provider = p
                    if(pkg.save())
                        log.debug("processed: ${pkg.provider}:${pkg} ex ${or.org}:${or.pkg}")
                    else log.error(pkg.errors.getAllErrors().toListString())
                }
                or.delete()
            }
            ts.flush()
            providers.each { Org provider ->
                Map<String, Object> delResult = deletionService.deleteOrganisation(provider, null, false)
                if(delResult.deletable == false)
                    log.info("objects pending; ${provider.name}:${provider.id} could not be deleted")
            }
        }
    }

}
