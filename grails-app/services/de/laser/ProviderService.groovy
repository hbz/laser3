package de.laser

import de.laser.addressbook.Address
import de.laser.addressbook.Person
import de.laser.addressbook.PersonRole
import de.laser.auth.User
import de.laser.convenience.Marker
import de.laser.helper.Params
import de.laser.properties.ProviderProperty
import de.laser.storage.RDStore
import de.laser.survey.SurveyInfo
import de.laser.traces.DeletedObject
import de.laser.wekb.ElectronicBilling
import de.laser.wekb.InvoiceDispatch
import de.laser.wekb.InvoicingVendor
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.ProviderRole
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class ProviderService {

    ContextService contextService
    DocstoreService docstoreService
    GokbService gokbService
    TaskService taskService
    WorkflowService workflowService

    static String RESULT_BLOCKED            = 'RESULT_BLOCKED'
    static String RESULT_SUCCESS            = 'RESULT_SUCCESS'
    static String RESULT_ERROR              = 'RESULT_ERROR'

    /**
     * Gets the contact persons; optionally, a function type may be given as filter. Moreover, the request may be limited to public contacts only
     * @param provider the {@link de.laser.wekb.Provider} for which the contacts should be retrieved
     * @param onlyPublic retrieve only public contacts?
     * @param functionType the function type of the contacts to be requested
     * @param exWekb should only contacts being retrieved which come from the provider itself (i.e. from we:kb)?
     * @return a {@link List} of {@link de.laser.addressbook.Person}s matching to the function type
     */
    List<Person> getContactPersonsByFunctionType(Provider provider, boolean onlyPublic, RefdataValue functionType = null) {
        Map<String, Object> queryParams = [provider: provider]
        String functionTypeFilter = ''
        if(functionType) {
            functionTypeFilter = 'and pr.functionType = :functionType'
            queryParams.functionType = functionType
        }
        if (onlyPublic) {
            Person.executeQuery(
                    'select distinct p from Person as p inner join p.roleLinks pr where pr.provider = :provider and p.isPublic = true '+functionTypeFilter,
                    queryParams
            )
        }
        else {
            queryParams.ctx = contextService.getOrg()
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

        List providerLinks  = new ArrayList(provider.links)

        List addresses      = new ArrayList(provider.addresses)

        List prsLinks       = new ArrayList(provider.prsLinks)
        List docContexts    = new ArrayList(provider.documents)
        List tasks          = Task.findAllByProvider(provider)
        List platforms      = new ArrayList(provider.packages)
        List packages       = new ArrayList(provider.platforms)
        List surveys        = SurveyInfo.findAllByProvider(provider)

        List customProperties       = new ArrayList(provider.propertySet.findAll { it.type.tenant == null })
        List privateProperties      = new ArrayList(provider.propertySet.findAll { it.type.tenant != null })

        List markers        = Marker.findAllByProv(provider)

        // collecting information

        result.info = []

        //result.info << ['Links: Orgs', links, FLAG_BLOCKER]

        result.info << ['Identifikatoren', ids]
        result.info << ['ProviderRoles', providerLinks]

        result.info << ['Adressen', addresses]
        result.info << ['Personen', prsLinks]
        result.info << ['Aufgaben', tasks]
        result.info << ['Dokumente', docContexts]
        result.info << ['Plattformen', platforms]
        result.info << ['Pakete', packages]
        result.info << ['Umfragen', surveys]

        result.info << ['Allgemeine Merkmale', customProperties]
        result.info << ['Private Merkmale', privateProperties]

        result.info << ['Marker', markers]

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

                    int updateCount = 0, deleteCount = 0
                    provider.links.clear()
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
                        List providerRoleCheck = ProviderRole.executeQuery('select pvr from ProviderRole pvr where pvr.provider = :target and '+targetClause, checkParams)
                        if(!providerRoleCheck) {
                            pvr.provider = replacement
                            pvr.save()
                            updateCount++
                        }
                        else {
                            pvr.delete()
                            deleteCount++
                        }
                    }
                    log.debug("${updateCount} provider roles updated, ${deleteCount} provider roles deleted because already existent")

                    // addresses
                    provider.addresses.clear()
                    log.debug("${Address.executeUpdate('update Address a set a.provider = :target where a.provider = :source', genericParams)} addresses updated")

                    // custom properties
                    provider.propertySet.clear()
                    log.debug("${ProviderProperty.executeUpdate('update ProviderProperty pp set pp.owner = :target where pp.owner = :source', genericParams)} properties updated")

                    // documents
                    // executeUpdate does not trigger properly the cascade - would result in EntityNotFoundException
                    updateCount = 0
                    provider.documents.clear()
                    docContexts.each { DocContext dc ->
                        dc.provider = replacement
                        if(!dc.save())
                            log.error(dc.errors.getAllErrors().toListString())
                        else
                            updateCount++
                    }
                    log.debug("${updateCount} doc contexts updated")

                    // electronic billings
                    provider.electronicBillings.clear()
                    log.debug("${ElectronicBilling.executeUpdate('update ElectronicBilling eb set eb.provider = :target where eb.provider = :source', genericParams)} electronic billings updated")

                    // invoice dispatchs
                    provider.invoiceDispatchs.clear()
                    log.debug("${InvoiceDispatch.executeUpdate('update InvoiceDispatch idi set idi.provider = :target where idi.provider = :source', genericParams)} invoice dispatchs updated")

                    // invoicing vendors
                    provider.invoicingVendors.clear()
                    log.debug("${InvoicingVendor.executeUpdate('update InvoicingVendor iv set iv.provider = :target where iv.provider = :source', genericParams)} invoicing vendors updated")

                    markers.each { Marker mkr ->
                        mkr.prov = replacement
                        mkr.save()
                    }

                    // persons
                    List<Person> targetPersons = Person.executeQuery('select pr.prs from PersonRole pr where pr.provider = :target', [target: replacement])
                    updateCount = 0
                    deleteCount = 0
                    provider.prsLinks.clear()
                    prsLinks.each { PersonRole pr ->
                        Person equivalent = targetPersons.find { Person pT -> pT.last_name == pr.prs.last_name && pT.tenant == pT.tenant }
                        if(!equivalent) {
                            pr.provider = replacement
                            //ERMS-5775
                            if(replacement.gokbId && pr.prs.isPublic) {
                                pr.prs.isPublic = false
                                pr.prs.save()
                            }
                            if(!pr.save())
                                log.error(pr.errors.getAllErrors().toListString())
                            updateCount++
                        }
                        else {
                            pr.delete()
                            deleteCount++
                        }
                    }
                    log.debug("${updateCount} contacts updated, ${deleteCount} contacts deleted because already existent")

                    // tasks
                    // executeUpdate does not trigger properly the cascade - would result in EntityNotFoundException
                    updateCount = 0
                    tasks.each { Task t ->
                        t.provider = replacement
                        if(!t.save())
                            log.error(t.errors.getAllErrors().toListString())
                        else
                            updateCount++
                    }
                    log.debug("${updateCount} tasks updated")

                    // platforms
                    log.debug("${Platform.executeUpdate('update Platform p set p.provider = :target where p.provider = :source', genericParams)} platforms updated")

                    // packages
                    log.debug("${Package.executeUpdate('update Package pkg set pkg.provider = :target where pkg.provider = :source', genericParams)} packages updated")

                    // surveys
                    // executeUpdate does not trigger properly the cascade - would result in EntityNotFoundException
                    updateCount = 0
                    surveys.each { SurveyInfo surin ->
                        surin.provider = replacement
                        if(!surin.save())
                            log.error(surin.errors.getAllErrors().toListString())
                        else
                            updateCount++
                    }
                    log.debug("${updateCount} survey infos updated")

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

    boolean isMyProvider(Provider provider) {
        int count = ProviderRole.executeQuery(
                'select count(*) from OrgRole oo, ProviderRole pvr where (pvr.subscription = oo.sub or pvr.license = oo.lic) and oo.org = :context and pvr.provider = :provider',
                [provider: provider, context: contextService.getOrg()]
        )[0]
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

        if (params.id) {
            result.provider = Provider.get(params.id)
            result.editable = contextService.isInstEditor()
            result.isAdmin = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
            //set isMyOrg-flag for relations context -> provider
            int relationCheck = OrgRole.executeQuery('select count(oo) from ProviderRole pvr join pvr.subscription sub, OrgRole oo where pvr.subscription = oo.org and oo.org = :context and sub.status = :current', [context: org, current: RDStore.SUBSCRIPTION_CURRENT])[0]
            result.isMyProvider = relationCheck > 0

            int tc1 = taskService.getTasksByResponsibilityAndObject(result.user, result.provider).size()
            int tc2 = taskService.getTasksByCreatorAndObject(result.user, result.provider).size()
            result.tasksCount = (tc1 || tc2) ? "${tc1}/${tc2}" : ''
            result.docsCount        = docstoreService.getDocsCount(result.provider, contextService.getOrg())
            result.notesCount       = docstoreService.getNotesCount(result.provider, contextService.getOrg())
            result.checklistCount   = workflowService.getWorkflowCount(result.provider, contextService.getOrg())
        }

        //result.links = linksGenerationService.getProviderLinks(result.orgInstance)
        //Map<String, List> nav = (linksGenerationService.generateNavigation(result.orgInstance, true))
        //result.navPrevProvider = nav.prevLink
        //result.navNextProvider = nav.nextLink
        result
    }

    Set<Long> getCurrentProviderIds(Org context) {
        Set<Long> result = ProviderRole.executeQuery("select p.id from ProviderRole pr join pr.provider as p where pr.subscription in (select sub from OrgRole where org = :context and roleType in (:roleTypes))",
                [context: context, roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIUM, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]])
        result
    }

    Set<Provider> getCurrentProviders(Org context) {
        Set<Provider> result = ProviderRole.executeQuery("select p from ProviderRole pr join pr.provider as p where (pr.subscription in (select sub from OrgRole where org = :context and roleType in (:subRoleTypes)) or pr.license in (select lic from OrgRole where org = :context and roleType in (:licRoleTypes))) order by p.name",
                [context: context,
                 subRoleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIUM,RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER],
                 licRoleTypes:[RDStore.OR_LICENSING_CONSORTIUM,RDStore.OR_LICENSEE_CONS,RDStore.OR_LICENSEE]])
        result
    }
}
