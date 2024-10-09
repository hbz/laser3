package de.laser

import de.laser.auth.User
import de.laser.convenience.Marker
import de.laser.helper.Params
import de.laser.properties.PropertyDefinition
import de.laser.properties.VendorProperty
import de.laser.remote.ApiSource
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfigVendor
import de.laser.traces.DeletedObject
import de.laser.utils.LocaleUtils
import de.laser.utils.SwissKnife
import de.laser.wekb.ElectronicBilling
import de.laser.wekb.ElectronicDeliveryDelayNotification
import de.laser.wekb.InvoiceDispatch
import de.laser.wekb.Package
import de.laser.wekb.PackageVendor
import de.laser.wekb.Platform
import de.laser.wekb.Vendor
import de.laser.wekb.VendorLink
import de.laser.wekb.VendorRole
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.transaction.TransactionStatus

@Transactional
class VendorService {

    ContextService contextService
    DocstoreService docstoreService
    DeletionService deletionService
    GokbService gokbService
    MessageSource messageSource
    TaskService taskService
    UserService userService
    WorkflowService workflowService

    static String RESULT_BLOCKED            = 'RESULT_BLOCKED'
    static String RESULT_SUCCESS            = 'RESULT_SUCCESS'
    static String RESULT_ERROR              = 'RESULT_ERROR'

    /**
     * Gets the contact persons; optionally, a function type may be given as filter. Moreover, the request may be limited to public contacts only
     * @param vendor the {@link de.laser.wekb.Vendor} for which the contacts should be retrieved
     * @param onlyPublic retrieve only public contacts?
     * @param functionType the function type of the contacts to be requested
     * @param exWekb should only contacts being retrieved which come from the provider itself (i.e. from we:kb)?
     * @return a {@link List} of {@link Person}s matching to the function type
     */
    List<Person> getContactPersonsByFunctionType(Vendor vendor, Org contextOrg, boolean onlyPublic, RefdataValue functionType = null) {
        Map<String, Object> queryParams = [vendor: vendor]
        String functionTypeFilter = ''
        if(functionType) {
            functionTypeFilter = 'and pr.functionType = :functionType'
            queryParams.functionType = functionType
        }
        if (onlyPublic) {
            Person.executeQuery(
                    'select distinct p from Person as p inner join p.roleLinks pr where pr.vendor = :vendor and p.isPublic = true '+functionTypeFilter,
                    queryParams
            )
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

        if(params.uuids)
            queryParams.uuids = params.uuids

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
        PropertyDefinition.findAllByDescr(PropertyDefinition.ORG_PROP).each { PropertyDefinition orgPropDef ->
            if(!PropertyDefinition.findByNameAndDescrAndTenant(orgPropDef.name, PropertyDefinition.VEN_PROP, orgPropDef.tenant)) {
                PropertyDefinition venPropDef = new PropertyDefinition(
                        name: orgPropDef.name,
                        name_de: orgPropDef.name_de,
                        name_en: orgPropDef.name_en,
                        expl_de: orgPropDef.expl_de,
                        expl_en: orgPropDef.expl_en,
                        descr: PropertyDefinition.VEN_PROP,
                        type: orgPropDef.type,
                        refdataCategory: orgPropDef.refdataCategory,
                        multipleOccurrence: orgPropDef.multipleOccurrence,
                        mandatory: orgPropDef.mandatory,
                        isUsedForLogic: orgPropDef.isUsedForLogic,
                        isHardData: orgPropDef.isHardData,
                        tenant: orgPropDef.tenant
                )
                venPropDef.save()
            }
        }
        Org.withTransaction { TransactionStatus ts ->
            Set<Combo> agencyCombos = Combo.executeQuery('select c from Combo c, Org o where (c.fromOrg = o or c.toOrg = o) and o.orgType_new = :agency', [agency: RDStore.OT_AGENCY])
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
            Set<PersonRole> agencyContacts = PersonRole.executeQuery('select pr from PersonRole pr join pr.org o where o.orgType_new = :agency', [agency: RDStore.OT_AGENCY])
            agencyContacts.each { PersonRole pr ->
                Vendor v = Vendor.findByGlobalUID(pr.org.globalUID.replace(Org.class.simpleName.toLowerCase(), Vendor.class.simpleName.toLowerCase()))
                if (!v) {
                    v = Vendor.convertFromAgency(pr.org)
                }
                if(pr.prs.tenant == pr.org) {
                    List<Contact> contacts = new ArrayList(pr.prs.contacts)
                    contacts.each { Contact tmp ->
                        tmp.delete()
                    }
                    pr.prs.contacts.clear()
                    pr.prs.delete()
                    pr.delete()
                }
                else {
                    pr.vendor = v
                    pr.org = null
                    pr.save()
                }
            }
            ts.flush()
            Set<DocContext> docOrgContexts = DocContext.executeQuery('select dc from DocContext dc join dc.org o where o.orgType_new = :agency', [agency: RDStore.OT_AGENCY])
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
            Set<DocContext> docTargetOrgContexts = DocContext.executeQuery('select dc from DocContext dc join dc.targetOrg o where o.orgType_new = :agency', [agency: RDStore.OT_AGENCY])
            docTargetOrgContexts.each { DocContext dc ->
                Vendor v = Vendor.findByGlobalUID(dc.targetOrg.globalUID.replace(Org.class.simpleName.toLowerCase(), Vendor.class.simpleName.toLowerCase()))
                if (!v) {
                    v = Vendor.convertFromAgency(dc.targetOrg)
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
                if(!ar.org.getCustomerType()) {
                    Vendor v = Vendor.findByGlobalUID(ar.org.globalUID.replace(Org.class.simpleName.toLowerCase(), Vendor.class.simpleName.toLowerCase()))
                    if (!v) {
                        v = Vendor.convertFromAgency(ar.org)
                    }
                    if (ar.sub && !VendorRole.findByVendorAndSubscription(v, ar.sub)) {
                        VendorRole vr = new VendorRole(vendor: v, subscription: ar.sub, isShared: ar.isShared)
                        if (vr.save()) {
                            if (ar.isShared) {
                                List<Subscription> newTargets = Subscription.findAllByInstanceOf(vr.subscription)
                                newTargets.each{ Subscription sub ->
                                    vr.addShareForTarget_trait(sub)
                                }
                                //log.debug("${OrgRole.executeUpdate('delete from OrgRole oorr where oorr.sharedFrom = :sf', [sf: ar])} shares deleted")
                            }
                            log.debug("processed: ${vr.vendor}:${vr.subscription} ex ${ar.org}:${ar.sub}")
                        }
                        else log.error(vr.errors.getAllErrors().toListString())
                    }
                    else if(ar.lic && !VendorRole.findByVendorAndLicense(v, ar.lic)) {
                        VendorRole vr = new VendorRole(vendor: v, license: ar.lic, isShared: ar.isShared)
                        if (vr.save()) {
                            if (ar.isShared) {
                                List<License> newTargets = License.findAllByInstanceOf(vr.license)
                                newTargets.each{ License lic ->
                                    vr.addShareForTarget_trait(lic)
                                }
                                //log.debug("${OrgRole.executeUpdate('delete from OrgRole oorr where oorr.sharedFrom = :sf', [sf: ar])} shares deleted")
                            }
                            log.debug("processed: ${vr.vendor}:${vr.license} ex ${ar.org}:${ar.lic}")
                        }
                        else log.error(vr.errors.getAllErrors().toListString())
                    }
                    else if (ar.pkg) {
                        if(!PackageVendor.findByVendorAndPkg(v, ar.pkg)) {
                            PackageVendor pv = new PackageVendor(vendor: v, pkg: ar.pkg)
                            if (pv.save())
                                log.debug("processed: ${pv.vendor}:${pv.pkg} ex ${ar.org}:${ar.pkg}")
                            else log.error(pv.errors.getAllErrors().toListString())
                        }
                    }
                }
                ar.delete()
            }
            ts.flush()
            toDelete.collate(50000).eachWithIndex { subSet, int i ->
                log.debug("deleting records ${i * 50000}-${(i + 1) * 50000}")
                OrgRole.executeUpdate('delete from OrgRole ar where ar.sharedFrom.id in (:toDelete)', [toDelete: subSet])
                OrgRole.executeUpdate('delete from OrgRole ar where ar.id in (:toDelete)', [toDelete: subSet])
            }
            ts.flush()
        }
        Set<Org> agencies = Org.executeQuery('select o from Org o where o.orgType_new in (:agency)', [agency: [RDStore.OT_PROVIDER, RDStore.OT_AGENCY]])
        agencies.each { Org agency ->
            OrgRole.executeUpdate('delete from OrgRole oo where oo.org = :agency and oo.roleType not in (:toKeep)', [agency: agency, toKeep: [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER, RDStore.OR_LICENSOR, RDStore.OR_AGENCY]])
            List<Person> oldPersons = Person.executeQuery('select p from Person p where p.tenant = :agency and p.isPublic = true',[agency: agency])
            oldPersons.each { Person old ->
                PersonRole.executeUpdate('delete from PersonRole pr where pr.prs = :oldPerson', [oldPerson: old])
                Contact.executeUpdate('delete from Contact c where c.prs = :oldPerson', [oldPerson: old])
                Person.executeUpdate('delete from Person p where p = :oldPerson', [oldPerson: old])
            }
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
        List ids            = new ArrayList(vendor.ids)
        List vendorLinks    = new ArrayList(vendor.links)

        List addresses      = new ArrayList(vendor.addresses)

        List prsLinks       = new ArrayList(vendor.prsLinks)
        List docContexts    = new ArrayList(vendor.documents)
        List tasks          = Task.findAllByVendor(vendor)
        List packages       = new ArrayList(vendor.packages)
        List surveys        = new ArrayList(vendor.surveys)
        List electronicBillings = new ArrayList(vendor.electronicBillings)
        List invoiceDispatchs = new ArrayList(vendor.invoiceDispatchs)
        List supportedLibrarySystems = new ArrayList(vendor.supportedLibrarySystems)
        List electronicDeliveryDelays = new ArrayList(vendor.electronicDeliveryDelays)

        List customProperties       = new ArrayList(vendor.propertySet.findAll { it.type.tenant == null })
        List privateProperties      = new ArrayList(vendor.propertySet.findAll { it.type.tenant != null })

        List markers        = Marker.findAllByVen(vendor)

        // collecting information

        result.info = []

        //result.info << ['Links: Orgs', links, FLAG_BLOCKER]

        result.info << ['Identifikatoren', ids]
        result.info << ['VendorRoles', vendorLinks]

        result.info << ['Adressen', addresses]
        result.info << ['Personen', prsLinks]
        result.info << ['Aufgaben', tasks]
        result.info << ['Dokumente', docContexts]
        result.info << ['Packages', packages]
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
            Vendor.withTransaction { status ->

                try {
                    Map<String, Object> genericParams = [source: vendor, target: replacement]
                    /* identifiers */
                    vendor.ids.clear()
                    ids.each { Identifier id ->
                        id.vendor = replacement
                        id.save()
                    }

                    int updateCount = 0, deleteCount = 0
                    vendor.links.clear()
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
                        List vendorRoleCheck = VendorRole.executeQuery('select vr from VendorRole vr where vr.vendor = :target and '+targetClause, checkParams)
                        if(!vendorRoleCheck) {
                            vr.vendor = replacement
                            vr.save()
                            updateCount++
                        }
                        else {
                            vr.delete()
                            deleteCount++
                        }
                    }
                    log.debug("${updateCount} vendor roles updated, ${deleteCount} vendor roles deleted because already existent")

                    // addresses
                    vendor.addresses.clear()
                    log.debug("${Address.executeUpdate('update Address a set a.vendor = :target where a.vendor = :source', genericParams)} addresses updated")

                    // custom properties
                    vendor.propertySet.clear()
                    log.debug("${VendorProperty.executeUpdate('update VendorProperty vp set vp.owner = :target where vp.owner = :source', genericParams)} properties updated")

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

                    markers.each { Marker mkr ->
                        mkr.ven = replacement
                    }

                    // persons
                    List<Person> targetPersons = Person.executeQuery('select pr.prs from PersonRole pr where pr.vendor = :target', [target: replacement])
                    updateCount = 0
                    deleteCount = 0
                    PersonRole.findAllByVendor(vendor).each { PersonRole pr ->
                        Person equivalent = targetPersons.find { Person pT -> pT.last_name == pr.prs.last_name && pT.tenant == pT.tenant }
                        if(!equivalent) {
                            pr.vendor = replacement
                            //ERMS-5775
                            if(replacement.gokbId && pr.prs.isPublic) {
                                pr.prs.isPublic = false
                                pr.prs.save()
                            }
                            pr.save()
                            updateCount++
                        }
                        else {
                            pr.delete()
                            deleteCount++
                        }
                    }
                    log.debug("${updateCount} contacts updated, ${deleteCount} contacts deleted because already existent")

                    // tasks
                    log.debug("${Task.executeUpdate('update Task t set t.vendor = :target where t.vendor = :source', genericParams)} tasks updated")

                    // platforms
                    Set<PackageVendor> targetPackages = replacement.packages
                    vendor.packages.clear()
                    packages.each { PackageVendor pv ->
                        if(!targetPackages.find { PackageVendor pvB -> pvB.pkg == pv.pkg }) {
                            pv.vendor = replacement
                            pv.save()
                        }
                        else pv.delete()
                    }

                    // alternative names
                    vendor.altnames.clear()
                    log.debug("${AlternativeName.executeUpdate('update AlternativeName alt set alt.vendor = :target where alt.vendor = :source', genericParams)} alternative names updated")
                    AlternativeName.construct([name: vendor.name, vendor: replacement])

                    // supported library systems
                    Set<LibrarySystem> targetLibrarySystems = replacement.supportedLibrarySystems
                    vendor.supportedLibrarySystems.clear()
                    supportedLibrarySystems.each { LibrarySystem ls ->
                        if(!targetLibrarySystems.find { LibrarySystem lsT -> lsT.librarySystem == ls.librarySystem }) {
                            ls.vendor = replacement
                            ls.save()
                        }
                        else ls.delete()
                    }
                    Set<ElectronicDeliveryDelayNotification> targetElectronicDeliveryDelays = replacement.electronicDeliveryDelays
                    vendor.electronicDeliveryDelays.clear()
                    electronicDeliveryDelays.each { ElectronicDeliveryDelayNotification eddn ->
                        if(!targetElectronicDeliveryDelays.find { ElectronicDeliveryDelayNotification eddnT -> eddnT.delayNotification == eddn.delayNotification }) {
                            eddn.vendor = replacement
                            eddn.save()
                        }
                        else eddn.delete()
                    }
                    Set<InvoiceDispatch> targetInvoiceDispatchs = replacement.invoiceDispatchs
                    vendor.invoiceDispatchs.clear()
                    invoiceDispatchs.each { InvoiceDispatch idi ->
                        if(!targetInvoiceDispatchs.find { InvoiceDispatch idiT -> idiT.invoiceDispatch == idi.invoiceDispatch }) {
                            idi.vendor = replacement
                            idi.save()
                        }
                        else idi.delete()
                    }
                    Set<ElectronicBilling> targetElectronicBillings = replacement.electronicBillings
                    vendor.electronicBillings.clear()
                    electronicBillings.each { ElectronicBilling eb ->
                        if(!targetElectronicBillings.find { ElectronicBilling ebT -> ebT.invoicingFormat == eb.invoicingFormat }) {
                            eb.vendor = replacement
                            eb.save()
                        }
                        else eb.delete()
                    }
                    Set<SurveyConfigVendor> targetSurveyConfigs = replacement.surveys
                    vendor.surveys.clear()
                    surveys.each { SurveyConfigVendor scv ->
                        if(!targetSurveyConfigs.find { SurveyConfigVendor scvT -> scvT.surveyConfig == scv.surveyConfig }) {
                            scv.vendor = replacement
                            scv.save()
                        }
                        else scv.delete()
                    }

                    vendor.delete()

                    DeletedObject.withTransaction {
                        DeletedObject.construct(vendor)
                    }
                    status.flush()

                    result.status = RESULT_SUCCESS
                }
                catch (Exception e) {
                    log.error('error while merging provider ' + vendor.id + ' .. rollback: ' + e.message)
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
        /*
        may cause overload; for hbz data, the filter must be excluded
        String instanceFilter = ''
        if(contextOrg.isCustomerType_Consortium())
            instanceFilter = 'and s.instanceOf = null'
        */
        Set<Package> subscribedPackages = Package.executeQuery('select pkg from SubscriptionPackage sp join sp.pkg pkg, OrgRole oo join oo.sub s where sp.subscription = oo.sub and s.status = :current and oo.org = :contextOrg and pkg in (select pv.pkg from PackageVendor pv where pv.vendor = :vendor)', [vendor: vendor, current: RDStore.SUBSCRIPTION_CURRENT, contextOrg: contextOrg])
        subscribedPackages.nominalPlatform
    }

    Map<String, Object> getResultGenerics(GrailsParameterMap params) {
        Org contextOrg = contextService.getOrg()
        User contextUser = contextService.getUser()
        Map<String, Object> result = [user: contextUser,
                                      institution: contextOrg,
                                      contextOrg: contextOrg, //for templates
                                      contextCustomerType:contextOrg.getCustomerType(),
                                      wekbApi: ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)]
        if(params.id) {
            result.vendor = Vendor.get(params.id)
            result.editable = userService.hasFormalAffiliation_or_ROLEADMIN(contextUser, contextOrg, 'INST_EDITOR')
            int tc1 = taskService.getTasksByResponsibilityAndObject(result.user, result.vendor).size()
            int tc2 = taskService.getTasksByCreatorAndObject(result.user, result.vendor).size()
            result.tasksCount = (tc1 || tc2) ? "${tc1}/${tc2}" : ''
            result.docsCount        = docstoreService.getDocsCount(result.vendor, result.institution)
            result.notesCount       = docstoreService.getNotesCount(result.vendor, result.institution)
            result.checklistCount   = workflowService.getWorkflowCount(result.vendor, result.institution)
        }

        SwissKnife.setPaginationParams(result, params, contextUser)
        result
    }

    Map<String, Map> getWekbVendors(GrailsParameterMap params) {
        Map<String, Object> result = [:], queryParams = [:]
        User contextUser = contextService.getUser()
        SwissKnife.setPaginationParams(result, params, contextUser)
        Locale locale = LocaleUtils.getCurrentLocale()
        result.wekbApi = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

        result.flagContentGokb = true // vendorService.getWekbVendorRecords()
        Map queryCuratoryGroups = gokbService.executeQuery(result.wekbApi.baseUrl + result.wekbApi.fixToken + '/groups', [:])
        if (queryCuratoryGroups.code == 404) {
            result.error = message(code: 'wekb.error.' + queryCuratoryGroups.error) as String
        } else {
            if (queryCuratoryGroups) {
                List recordsCuratoryGroups = queryCuratoryGroups.result
                result.curatoryGroups = recordsCuratoryGroups?.findAll { it.status == "Current" }
            }
            result.wekbRecords = getWekbVendorRecords(params, result)
        }
        result.curatoryGroupTypes = [
                [value: 'Provider', name: messageSource.getMessage('package.curatoryGroup.provider', null, locale)],
                [value: 'Vendor', name: messageSource.getMessage('package.curatoryGroup.vendor', null, locale)],
                [value: 'Other', name: messageSource.getMessage('package.curatoryGroup.other', null, locale)]
        ]
        List<String> queryArgs = []
        if (params.containsKey('nameContains')) {
            queryArgs << "(genfunc_filter_matcher(v.name, :name) = true or genfunc_filter_matcher(v.sortname, :name) = true)"
            queryParams.name = params.nameContains
        }
        if (params.containsKey('venStatus')) {
            queryArgs << "v.status in (:status)"
            queryParams.status = Params.getRefdataList(params, 'venStatus')
        } else if (!params.containsKey('venStatus') && !params.containsKey('filterSet')) {
            queryArgs << "v.status = :status"
            queryParams.status = "Current"
            params.venStatus = RDStore.VENDOR_STATUS_CURRENT.id
        }

        if (params.containsKey('qp_supportedLibrarySystems')) {
            queryArgs << "exists (select ls from v.supportedLibrarySystems ls where ls.librarySystem in (:librarySystems))"
            queryParams.put('librarySystems', Params.getRefdataList(params, 'qp_supportedLibrarySystems'))
        }

        if (params.containsKey('qp_electronicBillings')) {
            queryArgs << "exists (select eb from v.electronicBillings eb where eb.invoicingFormat in (:electronicBillings))"
            queryParams.put('electronicBillings', Params.getRefdataList(params, 'qp_electronicBillings'))
        }

        if (params.containsKey('qp_invoiceDispatchs')) {
            queryArgs << "exists (select idi from v.invoiceDispatchs idi where idi.invoiceDispatch in (:invoiceDispatchs))"
            queryParams.put('invoiceDispatchs', Params.getRefdataList(params, 'qp_invoiceDispatchs'))
        }

        if (params.containsKey('qp_providers')) {
            queryArgs << "exists (select pv from PackageVendor pv where pv.vendor = v and pv.pkg.provider.id in (:providers))"
            queryParams.providers = Params.getLongList(params, 'qp_providers')
        }

        if (params.containsKey('curatoryGroup') || params.containsKey('curatoryGroupType')) {
            queryArgs << "v.gokbId in (:wekbIds)"
            queryParams.wekbIds = result.wekbRecords.keySet()
        }

        if (params.containsKey('uuids')) {
            queryArgs << "v.gokbId in (:wekbIds)"
            queryParams.wekbIds = result.wekbRecords.keySet()
        }

        if (params.containsKey('ids')) {
            queryArgs << "v.id in (:ids)"
            queryParams.ids = Params.getLongList(params, 'ids')
        }

        String vendorQuery = 'select v from Vendor v'
        if (queryArgs) {
            vendorQuery += ' where ' + queryArgs.join(' and ')
        }
        if (params.containsKey('sort')) {
            vendorQuery += " order by ${params.sort} ${params.order ?: 'asc'}, v.name ${params.order ?: 'asc'} "
        } else
            vendorQuery += " order by v.sortname "

        Set<Vendor> vendorsTotal = Vendor.executeQuery(vendorQuery, queryParams)

        result.vendorListTotal = vendorsTotal.size()
        result.vendorList = vendorsTotal.drop(result.offset).take(result.max)
        result

    }

    Set<Long> getCurrentVendorIds(Org context) {
        Set<Long> result = VendorRole.executeQuery("select vr.id from VendorRole vr join vr.vendor as v where vr.subscription in (select sub from OrgRole where org = :context and roleType in (:roleTypes))",
                [context:context,roleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIUM,RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER]])
        result
    }

}
