package de.laser

import de.laser.auth.User
import de.laser.helper.Params
import de.laser.properties.ProviderProperty
import de.laser.remote.ApiSource
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.traces.DeletedObject
import de.laser.utils.SwissKnife
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.hibernate.Session
import org.springframework.transaction.TransactionStatus

@Transactional
class VendorService {

    ContextService contextService
    DocstoreService docstoreService
    DeletionService deletionService
    GokbService gokbService
    TaskService taskService
    WorkflowService workflowService

    static String RESULT_BLOCKED            = 'RESULT_BLOCKED'
    static String RESULT_SUCCESS            = 'RESULT_SUCCESS'
    static String RESULT_ERROR              = 'RESULT_ERROR'

    /**
     * Gets the contact persons; optionally, a function type may be given as filter. Moreover, the request may be limited to public contacts only
     * @param vendor the {@link Vendor} for which the contacts should be retrieved
     * @param onlyPublic retrieve only public contacts?
     * @param functionType the function type of the contacts to be requested
     * @param exWekb should only contacts being retrieved which come from the provider itself (i.e. from we:kb)?
     * @return a {@link List} of {@link Person}s matching to the function type
     */
    List<Person> getContactPersonsByFunctionType(Vendor vendor, Org contextOrg, boolean onlyPublic, RefdataValue functionType = null, boolean exWekb = false) {
        Map<String, Object> queryParams = [vendor: vendor]
        String functionTypeFilter = ''
        if(functionType) {
            functionTypeFilter = 'and pr.functionType = :functionType'
            queryParams.functionType = functionType
        }
        if (onlyPublic) {
            if(exWekb) {
                Person.executeQuery(
                        'select distinct p from Person as p inner join p.roleLinks pr where pr.vendor = :vendor '+functionTypeFilter+' and p.tenant = null',
                        queryParams
                )
            }
            else {
                Person.executeQuery(
                        'select distinct p from Person as p inner join p.roleLinks pr where pr.vendor = :vendor and p.isPublic = true and p.tenant != null '+functionTypeFilter,
                        queryParams
                )
            }
        }
        else {
            queryParams.ctx = contextOrg
            Person.executeQuery(
                    'select distinct p from Person as p inner join p.roleLinks pr where pr.vendor = :vendor ' + functionTypeFilter +
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
    Map<String, Map> getWekbVendorRecords(GrailsParameterMap params, Map result) {
        Map<String, Map> records = [:], queryParams = [componentType: 'Vendor']

        if(params.containsKey('nameContains'))
            queryParams.q = params.nameContains

        if(params.containsKey('curatoryGroup'))
            queryParams.curatoryGroupExact = params.curatoryGroup.replaceAll('&','ampersand').replaceAll('\\+','%2B').replaceAll(' ','%20')


        if(params.containsKey('venStatus')) {
            queryParams.status = Params.getRefdataList(params, 'venStatus').value
        }
        else if(!params.containsKey('venStatus') && !params.containsKey('filterSet')) {
            queryParams.status = "Current"
            params.venStatus = RDStore.VENDOR_STATUS_CURRENT.id
        }

        Set<String> directMappings = ['curatoryGroupType', 'qp_supportedLibrarySystems', 'qp_electronicBillings', 'qp_invoiceDispatchs']
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
     * Changes agencies ({@link Org}s defined as such) into {@link Vendor}s
     */
    void migrateVendors() {
        Org.withTransaction { TransactionStatus ts ->
            Set<Combo> agencyCombos = Combo.executeQuery('select c from Combo c, Org o join o.orgType ot where (c.fromOrg = o or c.toOrg = o) and ot = :agency', [agency: RDStore.OT_AGENCY])
            agencyCombos.each { Combo ac ->
                VendorLink vl = new VendorLink(type: RDStore.PROVIDER_LINK_FOLLOWS)
                vl.from = Vendor.convertFromAgency(ac.fromOrg)
                vl.to = Vendor.convertFromAgency(ac.toOrg)
                vl.dateCreated = ac.dateCreated
                if(vl.save()) {
                    ac.delete()
                }
                else {
                    log.error(vl.getErrors().getAllErrors().toListString())
                }
            }
            ts.flush()
            Set<PersonRole> agencyContacts = PersonRole.executeQuery('select pr from PersonRole pr join pr.org o join o.orgType ot where ot = :agency', [agency: RDStore.OT_AGENCY])
            agencyContacts.each { PersonRole pr ->
                Provider p = Provider.findByGlobalUID(pr.org.globalUID.replace(Org.class.simpleName.toLowerCase(), Provider.class.simpleName.toLowerCase()))
                if (!p) {
                    p = Provider.convertFromOrg(pr.org)
                }
                pr.provider = p
                pr.org = null
                pr.save()
            }
            ts.flush()
            Set<DocContext> docOrgContexts = DocContext.executeQuery('select dc from DocContext dc where dc.org.orgType = :agency', [agency: RDStore.OT_AGENCY])
            docOrgContexts.each { DocContext dc ->
                Vendor v = Vendor.findByGlobalUID(dc.org.globalUID.replace(Org.class.simpleName.toLowerCase(), Vendor.class.simpleName.toLowerCase()))
                if (!v) {
                    v = Vendor.convertFromAgency(dc.org)
                }
                if(dc.targetOrg == dc.org)
                    dc.targetOrg = null
                dc.org = null
                dc.vendor = v
                dc.save()
            }
            ts.flush()
            Set<DocContext> docTargetOrgContexts = DocContext.executeQuery('select dc from DocContext dc where dc.targetOrg.orgType = :agency', [agency: RDStore.OT_AGENCY])
            docTargetOrgContexts.each { DocContext dc ->
                Vendor v = Vendor.findByGlobalUID(dc.org.globalUID.replace(Org.class.simpleName.toLowerCase(), Provider.class.simpleName.toLowerCase()))
                if (!v) {
                    v = Vendor.convertFromAgency(dc.org)
                }
                dc.targetOrg = null
                dc.org = null
                dc.vendor = v
                dc.save()
            }
            ts.flush()
            Set<OrgRole> agencyRelations = OrgRole.findAllByRoleType(RDStore.OR_AGENCY)
            Set<Long> toDelete = []
            agencyRelations.each { OrgRole ar ->
                Vendor v = Vendor.findByGlobalUID(ar.org.globalUID.replace(Org.class.simpleName.toLowerCase(), Vendor.class.simpleName.toLowerCase()))
                if (!v) {
                    v = Vendor.convertFromAgency(ar.org)
                }
                if (ar.sub || ar.lic) {
                    VendorRole vr = new VendorRole(vendor: v, isShared: ar.isShared)
                    if (ar.sub)
                        vr.subscription = ar.sub
                    if (ar.lic)
                        vr.license = ar.lic
                    if (vr.save()) {
                        if (ar.isShared) {
                            if (ar.sub) {
                                List<Subscription> newTargets = Subscription.findAllByInstanceOf(vr.subscription)
                                newTargets.each{ Subscription sub ->
                                    vr.addShareForTarget_trait(sub)
                                }
                            }
                            if (ar.lic) {
                                List<License> newTargets = License.findAllByInstanceOf(vr.license)
                                newTargets.each{ License lic ->
                                    vr.addShareForTarget_trait(lic)
                                }
                            }
                            //log.debug("${OrgRole.executeUpdate('delete from OrgRole oorr where oorr.sharedFrom = :sf', [sf: ar])} shares deleted")
                        }
                        log.debug("processed: ${vr.vendor}:${vr.subscription}:${vr.license} ex ${ar.org}:${ar.sub}:${ar.lic}")
                    } else log.error(vr.errors.getAllErrors().toListString())
                } else if (ar.pkg) {
                    PackageVendor pv = new PackageVendor(vendor: v, pkg: ar.pkg)
                    if (pv.save())
                        log.debug("processed: ${pv.vendor}:${pv.pkg} ex ${ar.org}:${ar.pkg}")
                    else log.error(pv.errors.getAllErrors().toListString())
                }
                ar.delete()
            }
            toDelete.collate(50000).eachWithIndex { subSet, int i ->
                log.debug("deleting records ${i * 50000}-${(i + 1) * 50000}")
                OrgRole.executeUpdate('delete from OrgRole ar where ar.sharedFrom.id in (:toDelete)', [toDelete: subSet])
                OrgRole.executeUpdate('delete from OrgRole ar where ar.id in (:toDelete)', [toDelete: subSet])
            }
            ts.flush()
        }
        Set<Org> agencies = Org.executeQuery('select o from Org o join o.orgType ot where ot = :agency', [agency: RDStore.OT_AGENCY])
        agencies.each { Org agency ->
            OrgRole.executeUpdate('delete from OrgRole oo where oo.org = :agency and oo.roleType not in (:toKeep)', [agency: agency, toKeep: [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER, RDStore.OR_LICENSOR, RDStore.OR_AGENCY]])
            Map<String, Object> delResult = deletionService.deleteOrganisation(agency, null, false)
            if(delResult.deletable == false) {
                log.info("${agency.name}:${agency.id} could not be deleted. Pending: ${delResult.info.findAll{ info -> info[1].size() > 0 && info[2] == DeletionService.FLAG_BLOCKER }.toListString()}")
                agency.removeFromOrgType(RDStore.OT_AGENCY)
                agency.save()
            }
        }
    }

    /**
     * Merges the given two vendors; displays eventual attached objects
     * @param vendor the vendor which should be merged
     * @param replacement the vendor to merge with
     * @param dryRun should the merge avoided and only information be fetched?
     * @return a map returning the information about the organisation
     */
    Map<String, Object> mergeVendors(Vendor vendor, Vendor replacement, boolean dryRun) {

        Map<String, Object> result = [:]

        // gathering references

        List vendorLinks       = VendorRole.findAllByVendor(vendor)

        List addresses      = new ArrayList(vendor.addresses)
        List contacts       = new ArrayList(vendor.contacts)

        List prsLinks       = VendorRole.findAllByVendor(vendor)
        List docContexts    = new ArrayList(vendor.documents)
        List tasks          = Task.findAllByVendor(vendor)
        List packages       = PackageVendor.findAllByVendor(vendor)

        List customProperties       = new ArrayList(vendor.propertySet.findAll { it.type.tenant == null })
        List privateProperties      = new ArrayList(vendor.propertySet.findAll { it.type.tenant != null })

        // collecting information

        result.info = []

        //result.info << ['Links: Orgs', links, FLAG_BLOCKER]

        //result.info << ['Identifikatoren', ids]
        result.info << ['VendorRoles', vendorLinks]

        result.info << ['Adressen', addresses]
        result.info << ['Kontaktdaten', contacts]
        result.info << ['Personen', prsLinks]
        result.info << ['Aufgaben', tasks]
        result.info << ['Dokumente', docContexts]
        result.info << ['Packages', packages]

        result.info << ['Allgemeine Merkmale', customProperties]
        result.info << ['Private Merkmale', privateProperties]


        // checking constraints and/or processing

        result.mergeable = true

        if (dryRun || ! result.mergeable) {
            return result
        }
        else {
            Vendor.withTransaction { status ->

                try {
                    Map<String, Object> genericParams = [source: vendor, target: replacement]
                    /* identifiers
                    vendor.ids.clear()
                    ids.each { Identifier id ->
                        id.provider = replacement
                        id.save()
                    }
                    */

                    vendorLinks.each { VendorRole vr ->
                        Map<String, Object> checkParams = [target: replacement]
                        String targetClause = ''
                        if(vr.subscription) {
                            targetClause = 'vr.subscription = :sub'
                            checkParams.sub = vr.subscription
                        }
                        else if(vr.license) {
                            targetClause = 'vr.license = :lic'
                            checkParams.lic = vr.license
                        }
                        List vendorRoleCheck = OrgRole.executeQuery('select vr from VendorRole vr where vr.provider = :target and '+targetClause, checkParams)
                        if(!vendorRoleCheck) {
                            vr.vendor = replacement
                            vr.save()
                        }
                        else {
                            vr.delete()
                        }
                    }

                    // addresses
                    vendor.addresses.clear()
                    log.debug("${Address.executeUpdate('update Address a set a.vendor = :target where a.org = :source', genericParams)} addresses updated")

                    // contacts
                    vendor.contacts.clear()
                    log.debug("${Contact.executeUpdate('update Contact c set c.vendor = :target where c.org = :source', genericParams)} contacts updated")

                    // custom properties
                    vendor.propertySet.clear()
                    log.debug("${ProviderProperty.executeUpdate('update VendorProperty vp set vp.owner = :target where vp.owner = :source', genericParams)} properties updated")

                    // documents
                    vendor.documents.clear()
                    log.debug("${DocContext.executeUpdate('update DocContext dc set dc.vendor = :target where dc.vendor = :source', genericParams)} document contexts updated")

                    // supported library systems
                    vendor.supportedLibrarySystems.clear()
                    log.debug("${DocContext.executeUpdate('update LibrarySystem ls set ls.vendor = :target where ls.vendor = :source', genericParams)} supported library systems updated")

                    // electronic billings
                    vendor.electronicBillings.clear()
                    log.debug("${ElectronicBilling.executeUpdate('update ElectronicBilling eb set eb.vendor = :target where eb.vendor = :source', genericParams)} electronic billings updated")

                    // invoice dispatchs
                    vendor.invoiceDispatchs.clear()
                    log.debug("${InvoiceDispatch.executeUpdate('update InvoiceDispatch idi set idi.vendor = :target where idi.vendor = :source', genericParams)} invoice dispatchs updated")

                    // electronic delivery delay notifications
                    vendor.electronicDeliveryDelays.clear()
                    log.debug("${ElectronicDeliveryDelayNotification.executeUpdate('update ElectronicDeliveryDelayNotification eddn set eddn.vendor = :target where eddn.vendor = :source', genericParams)} electronic delivery delay notifications updated")

                    // persons
                    log.debug("${Person.executeUpdate('update Person p set p.vendor = :target where p.vendor = :source', genericParams)} persons updated")

                    // tasks
                    log.debug("${Task.executeUpdate('update Task t set t.vendor = :target where t.vendor = :source', genericParams)} tasks updated")

                    // platforms
                    log.debug("${Platform.executeUpdate('update Platform p set p.vendor = :target where p.vendor = :source', genericParams)} platforms updated")

                    // alternative names
                    vendor.altnames.clear()
                    log.debug("${AlternativeName.executeUpdate('update AlternativeName alt set alt.vendor = :target where alt.vendor = :source', genericParams)} alternative names updated")
                    AlternativeName.construct([name: vendor.name, vendor: replacement])

                    vendor.delete()

                    DeletedObject.withTransaction {
                        DeletedObject.construct(vendor)
                    }
                    status.flush()

                    result.status = RESULT_SUCCESS
                }
                catch (Exception e) {
                    log.error 'error while merging provider ' + vendor.id + ' .. rollback: ' + e.message
                    e.printStackTrace()
                    status.setRollbackOnly()
                    result.status = RESULT_ERROR
                }
            }
        }

        result
    }

    boolean isMyVendor(Vendor vendor, Org contextOrg) {
        int count = VendorRole.executeQuery('select count(*) from OrgRole oo, VendorRole vr where (vr.subscription = oo.sub or vr.license = oo.lic) and oo.org = :context and vr.vendor = :vendor', [vendor: vendor, context: contextOrg])[0]
        count > 0
    }

    Set<Platform> getSubscribedPlatforms(Vendor vendor, Org contextOrg) {
        String instanceFilter = ''
        if(contextOrg.isCustomerType_Consortium())
            instanceFilter = 'and s.instanceOf = null'
        Platform.executeQuery('select pkg.nominalPlatform from PackageVendor pv, VendorRole vr, OrgRole oo join oo.sub s join pv.pkg pkg where pv.vendor = :vendor and pv.vendor = vr.vendor and vr.subscription = s and s.status = :current and oo.org = :contextOrg '+instanceFilter, [vendor: vendor, current: RDStore.SUBSCRIPTION_CURRENT, contextOrg: contextOrg])
    }

    Map<String, Object> getResultGenerics(GrailsParameterMap params) {
        Org contextOrg = contextService.getOrg()
        User contextUser = contextService.getUser()
        Map<String, Object> result = [user: contextUser,
                                      institution: contextOrg,
                                      contextOrg: contextOrg, //for templates
                                      wekbApi: ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)]
        if(params.id) {
            result.vendor = Vendor.get(params.id)
            int tc1 = taskService.getTasksByResponsiblesAndObject(result.user, result.institution, result.vendor).size()
            int tc2 = taskService.getTasksByCreatorAndObject(result.user, result.vendor).size()
            result.tasksCount = (tc1 || tc2) ? "${tc1}/${tc2}" : ''
            result.docsCount        = docstoreService.getDocsCount(result.vendor, result.institution)
            result.notesCount       = docstoreService.getNotesCount(result.vendor, result.institution)
            //result.checklistCount   = workflowService.getWorkflowCount(result.vendor, result.institution) TODO
        }

        SwissKnife.setPaginationParams(result, params, contextUser)
        result
    }

}
