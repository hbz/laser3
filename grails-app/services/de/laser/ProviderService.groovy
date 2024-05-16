package de.laser

import de.laser.auth.User
import de.laser.helper.Params
import de.laser.properties.ProviderProperty
import de.laser.remote.ApiSource
import de.laser.storage.RDStore
import de.laser.traces.DeletedObject
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.transaction.TransactionStatus

@Transactional
class ProviderService {

    ContextService contextService
    DeletionService deletionService
    DocstoreService docstoreService
    GokbService gokbService
    TaskService taskService
    UserService userService
    WorkflowService workflowService

    MessageSource messageSource

    static String RESULT_BLOCKED            = 'RESULT_BLOCKED'
    static String RESULT_SUCCESS            = 'RESULT_SUCCESS'
    static String RESULT_ERROR              = 'RESULT_ERROR'

    /**
     * Gets the contact persons; optionally, a function type may be given as filter. Moreover, the request may be limited to public contacts only
     * @param provider the {@link Provider} for which the contacts should be retrieved
     * @param onlyPublic retrieve only public contacts?
     * @param functionType the function type of the contacts to be requested
     * @param exWekb should only contacts being retrieved which come from the provider itself (i.e. from we:kb)?
     * @return a {@link List} of {@link Person}s matching to the function type
     */
    List<Person> getContactPersonsByFunctionType(Provider provider, Org contextOrg, boolean onlyPublic, RefdataValue functionType = null, boolean exWekb = false) {
        Map<String, Object> queryParams = [provider: provider]
        String functionTypeFilter = ''
        if(functionType) {
            functionTypeFilter = 'and pr.functionType = :functionType'
            queryParams.functionType = functionType
        }
        if (onlyPublic) {
            if(exWekb) {
                Person.executeQuery(
                        'select distinct p from Person as p inner join p.roleLinks pr where pr.provider = :provider '+functionTypeFilter+' and p.tenant = null',
                        queryParams
                )
            }
            else {
                Person.executeQuery(
                        'select distinct p from Person as p inner join p.roleLinks pr where pr.provider = :provider and p.isPublic = true and p.tenant != null '+functionTypeFilter,
                        queryParams
                )
            }
        }
        else {
            queryParams.ctx = contextOrg
            Person.executeQuery(
                    'select distinct p from Person as p inner join p.roleLinks pr where pr.provider = :provider ' + functionTypeFilter +
                            ' and ( (p.isPublic = false and p.tenant = :ctx) or (p.isPublic = true) )',
                    queryParams
            )
        }
    }

    /**
     * Gets a (filtered) map of provider records from the we:kb
     * @param params the request parameters
     * @param result a result generics map, containing also configuration params for the request
     * @return a {@link Map} of structure [providerUUID: providerRecord] containing the request results
     */
    Map<String, Map> getWekbProviderRecords(GrailsParameterMap params, Map result) {
        Map<String, Map> records = [:], queryParams = [componentType: 'Org']
        if(params.containsKey('nameContains'))
            queryParams.q = params.nameContains

        if (params.curatoryGroup || params.providerRole) {
            if (params.curatoryGroup)
                queryParams.curatoryGroupExact = params.curatoryGroup.replaceAll('&', 'ampersand').replaceAll('\\+', '%2B').replaceAll(' ', '%20')
            if (params.providerRole)
                queryParams.role = RefdataValue.get(params.providerRole).value.replaceAll(' ', '%20')
        }
        if(params.containsKey('provStatus')) {
            queryParams.status = Params.getRefdataList(params, 'provStatus').value
        }
        else if(!params.containsKey('provStatus') && !params.containsKey('filterSet')) {
            queryParams.status = "Current"
            params.provStatus = RDStore.PROVIDER_STATUS_CURRENT.id
        }

        Set<String> directMappings = ['curatoryGroupType', 'qp_invoicingVendors', 'qp_electronicBillings', 'qp_invoiceDispatchs']
        directMappings.each { String mapping ->
            if(params.containsKey(mapping))
                queryParams.put(mapping,params.get(mapping))
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
        Org.withTransaction { TransactionStatus ts ->
            Platform.findAllByOrgIsNotNull().each { Platform plat ->
                plat.provider = Provider.convertFromOrg(plat.org)
                plat.org = null
                plat.save()
            }
            Set<Combo> providerCombos = Combo.executeQuery('select c from Combo c, Org o join o.orgType ot where (c.fromOrg = o or c.toOrg = o) and ot in (:provider)', [provider: [RDStore.OT_PROVIDER, RDStore.OT_LICENSOR]])
            providerCombos.each { Combo pc ->
                ProviderLink pl = new ProviderLink(type: RDStore.PROVIDER_LINK_FOLLOWS)
                pl.from = Provider.convertFromOrg(pc.fromOrg)
                pl.to = Provider.convertFromOrg(pc.toOrg)
                pl.dateCreated = pc.dateCreated
                if(pl.save()) {
                    pc.delete()
                }
                else {
                    log.error(pl.getErrors().getAllErrors().toListString())
                }
            }
            ts.flush()
            Set<PersonRole> providerContacts = PersonRole.executeQuery('select pr from PersonRole pr join pr.org o join o.orgType ot where ot in (:provider)', [provider: [RDStore.OT_PROVIDER, RDStore.OT_LICENSOR]])
            providerContacts.each { PersonRole pr ->
                Provider p = Provider.findByGlobalUID(pr.org.globalUID.replace(Org.class.simpleName.toLowerCase(), Provider.class.simpleName.toLowerCase()))
                if (!p) {
                    p = Provider.convertFromOrg(pr.org)
                }
                pr.provider = p
                pr.org = null
                pr.save()
            }
            ts.flush()
            Set<DocContext> docOrgContexts = DocContext.executeQuery('select dc from DocContext dc join dc.org o join o.orgType ot where ot in (:provider)', [provider: [RDStore.OT_PROVIDER, RDStore.OT_LICENSOR]])
            docOrgContexts.each { DocContext dc ->
                Provider p = Provider.findByGlobalUID(dc.org.globalUID.replace(Org.class.simpleName.toLowerCase(), Provider.class.simpleName.toLowerCase()))
                if (!p) {
                    p = Provider.convertFromOrg(dc.org)
                }
                if(dc.targetOrg == dc.org)
                    dc.targetOrg = null
                dc.org = null
                dc.provider = p
                dc.save()
            }
            ts.flush()
            Set<DocContext> docTargetOrgContexts = DocContext.executeQuery('select dc from DocContext dc join dc.targetOrg o join o.orgType ot where ot in (:provider)', [provider: [RDStore.OT_PROVIDER, RDStore.OT_LICENSOR]])
            docTargetOrgContexts.each { DocContext dc ->
                Provider p = Provider.findByGlobalUID(dc.targetOrg.globalUID.replace(Org.class.simpleName.toLowerCase(), Provider.class.simpleName.toLowerCase()))
                if (!p) {
                    p = Provider.convertFromOrg(dc.targetOrg)
                }
                dc.targetOrg = null
                dc.org = null
                dc.provider = p
                dc.save()
            }
            ts.flush()
            Set<OrgRole> providerRelations = OrgRole.findAllByRoleTypeInList([RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER, RDStore.OR_LICENSOR])
            Set<Long> toDelete = []
            providerRelations.each { OrgRole or ->
                Provider p = Provider.findByGlobalUID(or.org.globalUID.replace(Org.class.simpleName.toLowerCase(), Provider.class.simpleName.toLowerCase()))
                if (!p) {
                    p = Provider.convertFromOrg(or.org)
                }
                if (or.sub && !ProviderRole.findByProviderAndSubscription(p, or.sub)) {
                    if (!or.sharedFrom) {
                        ProviderRole pr = new ProviderRole(provider: p, subscription: or.sub, isShared: or.isShared)
                        if (pr.save()) {
                            if(pr.isShared) {
                                List<Subscription> newTargets = Subscription.findAllByInstanceOf(pr.subscription)
                                newTargets.each{ Subscription sub ->
                                    pr.addShareForTarget_trait(sub)
                                }
                            }
                            log.debug("processed: ${pr.provider}:${pr.subscription} ex ${or.org}:${or.sub}")
                        }
                        else log.error(pr.errors.getAllErrors().toListString())
                    }
                } else if (or.lic && !ProviderRole.findByProviderAndLicense(p, or.lic)) {
                    if (!or.sharedFrom) {
                        ProviderRole pr = new ProviderRole(provider: p, license: or.lic, isShared: or.isShared)
                        if (pr.save()) {
                            if(pr.isShared) {
                                List<License> newTargets = License.findAllByInstanceOf(pr.license)
                                newTargets.each{ License lic ->
                                    pr.addShareForTarget_trait(lic)
                                }
                            }
                            log.debug("processed: ${pr.provider}:${pr.license} ex ${or.org}:${or.lic}")
                        }
                        else log.error(pr.errors.getAllErrors().toListString())
                    }
                } else if (or.pkg) {
                    Package pkg = or.pkg
                    pkg.provider = p
                    if (pkg.save())
                        log.debug("processed: ${pkg.provider}:${pkg} ex ${or.org}:${or.pkg}")
                    else log.error(pkg.errors.getAllErrors().toListString())
                }
                toDelete << or.id
            }
            toDelete.collate(50000).eachWithIndex { subSet, int i ->
                log.debug("deleting records ${i * 50000}-${(i + 1) * 50000}")
                OrgRole.executeUpdate('delete from OrgRole oo where oo.sharedFrom.id in (:toDelete)', [toDelete: subSet])
                OrgRole.executeUpdate('delete from OrgRole oo where oo.id in (:toDelete)', [toDelete: subSet])
            }
            ts.flush()
        }
        Set<Org> providers = Org.executeQuery('select o from Org o join o.orgType ot where ot in (:provider)', [provider: [RDStore.OT_PROVIDER, RDStore.OT_LICENSOR]])
        providers.each { Org provider ->
            OrgRole.executeUpdate('delete from OrgRole oo where oo.org = :provider and oo.roleType not in (:toKeep)', [provider: provider, toKeep: [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER, RDStore.OR_LICENSOR, RDStore.OR_AGENCY]])
            Map<String, Object> delResult = deletionService.deleteOrganisation(provider, null, false)
            if (delResult.deletable == false) {
                log.info("${provider.name}:${provider.id} could not be deleted. Pending: ${delResult.info.findAll { info -> info[1].size() > 0 && info[2] == DeletionService.FLAG_BLOCKER }.toListString()}")
                provider.removeFromOrgType(RDStore.OT_PROVIDER)
                provider.removeFromOrgType(RDStore.OT_LICENSOR)
                provider.save()
            }
        }
    }


    /**
     * Merges the given two providers; displays eventual attached objects
     * @param provider the provider which should be merged
     * @param replacement the provider to merge with
     * @param dryRun should the merge avoided and only information be fetched?
     * @return a map returning the information about the organisation
     */
    Map<String, Object> mergeProviders(Provider provider, Provider replacement, boolean dryRun) {

        Map<String, Object> result = [:]

        // gathering references

        List ids            = new ArrayList(provider.ids)

        List providerLinks       = ProviderRole.findAllByProvider(provider)

        List addresses      = new ArrayList(provider.addresses)
        List contacts       = new ArrayList(provider.contacts)

        List prsLinks       = PersonRole.findAllByProvider(provider)
        List docContexts    = new ArrayList(provider.documents)
        List tasks          = Task.findAllByProvider(provider)
        List platforms      = new ArrayList(provider.packages)
        List packages       = new ArrayList(provider.platforms)

        List customProperties       = new ArrayList(provider.propertySet.findAll { it.type.tenant == null })
        List privateProperties      = new ArrayList(provider.propertySet.findAll { it.type.tenant != null })

        // collecting information

        result.info = []

        //result.info << ['Links: Orgs', links, FLAG_BLOCKER]

        result.info << ['Identifikatoren', ids]
        result.info << ['ProviderRoles', providerLinks]

        result.info << ['Adressen', addresses]
        result.info << ['Kontaktdaten', contacts]
        result.info << ['Personen', prsLinks]
        result.info << ['Aufgaben', tasks]
        result.info << ['Dokumente', docContexts]
        result.info << ['Plattformen', platforms]
        result.info << ['Pakete', packages]

        result.info << ['Allgemeine Merkmale', customProperties]
        result.info << ['Private Merkmale', privateProperties]


        // checking constraints and/or processing

        result.mergeable = true

        if (dryRun || ! result.mergeable) {
            return result
        }
        else {
            Provider.withTransaction { status ->

                try {
                    Map<String, Object> genericParams = [source: provider, target: replacement]
                    // identifiers
                    provider.ids.clear()
                    ids.each { Identifier id ->
                        id.provider = replacement
                        id.save()
                    }

                    providerLinks.each { ProviderRole pvr ->
                        Map<String, Object> checkParams = [target: replacement]
                        String targetClause = ''
                        if(pvr.subscription) {
                            targetClause = 'pvr.subscription = :sub'
                            checkParams.sub = pvr.subscription
                        }
                        else if(pvr.license) {
                            targetClause = 'pvr.license = :lic'
                            checkParams.lic = pvr.license
                        }
                        List providerRoleCheck = OrgRole.executeQuery('select pvr from ProviderRole pvr where pvr.provider = :target and '+targetClause, checkParams)
                        if(!providerRoleCheck) {
                            pvr.provider = replacement
                            pvr.save()
                        }
                        else {
                            pvr.delete()
                        }
                    }

                    // addresses
                    provider.addresses.clear()
                    log.debug("${Address.executeUpdate('update Address a set a.provider = :target where a.org = :source', genericParams)} addresses updated")

                    // contacts
                    provider.contacts.clear()
                    log.debug("${Contact.executeUpdate('update Contact c set c.provider = :target where c.org = :source', genericParams)} contacts updated")

                    // custom properties
                    provider.propertySet.clear()
                    log.debug("${ProviderProperty.executeUpdate('update ProviderProperty pp set pp.owner = :target where pp.owner = :source', genericParams)} properties updated")

                    // documents
                    provider.documents.clear()
                    log.debug("${DocContext.executeUpdate('update DocContext dc set dc.provider = :target where dc.provider = :source', genericParams)} document contexts updated")

                    // electronic billings
                    provider.electronicBillings.clear()
                    log.debug("${ElectronicBilling.executeUpdate('update ElectronicBilling eb set eb.provider = :target where eb.provider = :source', genericParams)} electronic billings updated")

                    // invoice dispatchs
                    provider.invoiceDispatchs.clear()
                    log.debug("${InvoiceDispatch.executeUpdate('update InvoiceDispatch idi set idi.provider = :target where idi.provider = :source', genericParams)} invoice dispatchs updated")

                    // invoicing vendors
                    provider.invoicingVendors.clear()
                    log.debug("${InvoicingVendor.executeUpdate('update InvoicingVendor iv set iv.provider = :target where iv.provider = :source', genericParams)} invoicing vendors updated")

                    // persons
                    log.debug("${Person.executeUpdate('update Person p set p.provider = :target where p.provider = :source', genericParams)} persons updated")

                    // tasks
                    log.debug("${Task.executeUpdate('update Task t set t.provider = :target where t.provider = :source', genericParams)} tasks updated")

                    // platforms
                    log.debug("${Platform.executeUpdate('update Platform p set p.provider = :target where p.provider = :source', genericParams)} platforms updated")

                    // packages
                    log.debug("${Package.executeUpdate('update Package pkg set pkg.provider = :target where pkg.provider = :source', genericParams)} packages updated")

                    // alternative names
                    provider.altnames.clear()
                    log.debug("${AlternativeName.executeUpdate('update AlternativeName alt set alt.provider = :target where alt.provider = :source', genericParams)} alternative names updated")
                    AlternativeName.construct([name: provider.name, provider: replacement])


                    provider.delete()

                    DeletedObject.withTransaction {
                        DeletedObject.construct(provider)
                    }
                    status.flush()

                    result.status = RESULT_SUCCESS
                }
                catch (Exception e) {
                    log.error 'error while merging provider ' + provider.id + ' .. rollback: ' + e.message
                    e.printStackTrace()
                    status.setRollbackOnly()
                    result.status = RESULT_ERROR
                }
            }
        }

        result
    }

    boolean isMyProvider(Provider provider, Org contextOrg) {
        int count = ProviderRole.executeQuery('select count(*) from OrgRole oo, ProviderRole pvr where (pvr.subscription = oo.sub or pvr.license = oo.lic) and oo.org = :context and pvr.provider = :provider', [provider: provider, context: contextOrg])[0]
        count > 0
    }

    Map<String, Object> getResultGenericsAndCheckAccess(GrailsParameterMap params) {
        User user = contextService.getUser()
        Org org = contextService.getOrg()
        Map<String, Object> result = [user:user,
                                      institution:org,
                                      contextOrg: org, //for templates
                                      isMyOrg:false,
                                      contextCustomerType:org.getCustomerType()]
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        result.wekbApi = apiSource
        if (params.id) {
            result.provider = Provider.get(params.id)
            result.editable = userService.hasFormalAffiliation_or_ROLEADMIN(user, org,'INST_EDITOR')
            //set isMyOrg-flag for relations context -> provider
            int relationCheck = OrgRole.executeQuery('select count(oo) from ProviderRole pvr join pvr.subscription sub, OrgRole oo where pvr.subscription = oo.org and oo.org = :context and sub.status = :current', [context: org, current: RDStore.SUBSCRIPTION_CURRENT])[0]
            result.isMyOrg = relationCheck > 0

            int tc1 = taskService.getTasksByResponsiblesAndObject(result.user, result.institution, result.provider).size()
            int tc2 = taskService.getTasksByCreatorAndObject(result.user, result.provider).size()
            result.tasksCount = (tc1 || tc2) ? "${tc1}/${tc2}" : ''
            result.docsCount        = docstoreService.getDocsCount(result.provider, result.institution)
            result.notesCount       = docstoreService.getNotesCount(result.provider, result.institution)
            //result.checklistCount   = workflowService.getWorkflowCount(result.provider, result.institution) TODO
        }

        //result.links = linksGenerationService.getProviderLinks(result.orgInstance)
        //Map<String, List> nav = (linksGenerationService.generateNavigation(result.orgInstance, true))
        //result.navPrevProvider = nav.prevLink
        //result.navNextProvider = nav.nextLink
        result
    }
}
