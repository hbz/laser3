package de.laser

import de.laser.addressbook.Address
import de.laser.addressbook.Person
import de.laser.addressbook.PersonRole
import de.laser.auth.User
import de.laser.convenience.Marker
import de.laser.helper.Params
import de.laser.properties.VendorProperty
import de.laser.remote.Wekb
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfigVendor
import de.laser.survey.SurveyVendorResult
import de.laser.traces.DeletedObject
import de.laser.utils.LocaleUtils
import de.laser.utils.SwissKnife
import de.laser.wekb.ElectronicBilling
import de.laser.wekb.ElectronicDeliveryDelayNotification
import de.laser.wekb.InvoiceDispatch
import de.laser.wekb.InvoicingVendor
import de.laser.wekb.LibrarySystem
import de.laser.wekb.Package
import de.laser.wekb.PackageVendor
import de.laser.wekb.Platform
import de.laser.wekb.Vendor
import de.laser.wekb.VendorRole
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

@Transactional
class VendorService {

    ContextService contextService
    DocstoreService docstoreService
    GokbService gokbService
    MessageSource messageSource
    TaskService taskService
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
     * @return a {@link List} of {@link de.laser.addressbook.Person}s matching to the function type
     */
    List<Person> getContactPersonsByFunctionType(Vendor vendor, boolean onlyPublic, RefdataValue functionType = null) {
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
            queryParams.ctx = contextService.getOrg()
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
        List surveyResults   = SurveyVendorResult.findAllByVendor(vendor)
        List electronicBillings = new ArrayList(vendor.electronicBillings)
        List invoiceDispatchs = new ArrayList(vendor.invoiceDispatchs)
        List supportedLibrarySystems = new ArrayList(vendor.supportedLibrarySystems)
        List electronicDeliveryDelays = new ArrayList(vendor.electronicDeliveryDelays)
        List invoicingFor   = InvoicingVendor.findAllByVendor(vendor)

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
        result.info << ['Umfrage-Ergebnisse', surveyResults]
        result.info << ['Rechnungsstellung', invoicingFor]

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
                        mkr.save()
                    }

                    // persons
                    List<Person> targetPersons = Person.executeQuery('select pr.prs from PersonRole pr where pr.vendor = :target', [target: replacement])
                    updateCount = 0
                    deleteCount = 0
                    vendor.prsLinks.clear()
                    prsLinks.each { PersonRole pr ->
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
                    invoicingFor.each { InvoicingVendor iv ->
                        if(!InvoicingVendor.findAllByVendorAndProvider(replacement, iv.provider)) {
                            iv.vendor = replacement
                            iv.save()
                        }
                        else iv.delete()
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
                    surveyResults.each { SurveyVendorResult svr ->
                        if(!SurveyVendorResult.findByVendorAndParticipantAndSurveyConfig(replacement, svr.participant, svr.surveyConfig)) {
                            svr.vendor = replacement
                            svr.save()
                        }
                        else svr.delete()
                    }

                    vendor.delete()

                    DeletedObject.withTransaction {
                        DeletedObject.construct(vendor)
                    }
                    status.flush()

                    result.status = RESULT_SUCCESS
                }
                catch (Exception e) {
                    log.error('error while merging vendor ' + vendor.id + ' .. rollback: ' + e.message)
                    e.printStackTrace()
                    status.setRollbackOnly()
                    result.status = RESULT_ERROR
                }
            }
        }

        result
    }

    boolean isMyVendor(Vendor vendor) {
        int count = VendorRole.executeQuery(
                'select count(*) from OrgRole oo, VendorRole vr where (vr.subscription = oo.sub or vr.license = oo.lic) and oo.org = :context and vr.vendor = :vendor',
                [vendor: vendor, context: contextService.getOrg()]
        )[0]
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
                                      contextCustomerType:contextOrg.getCustomerType()]
        if(params.id) {
            result.vendor = Vendor.get(params.id)
            result.editable = contextService.isInstEditor()
            result.isAdmin = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
            int tc1 = taskService.getTasksByResponsibilityAndObject(result.user, result.vendor).size()
            int tc2 = taskService.getTasksByCreatorAndObject(result.user, result.vendor).size()
            result.tasksCount = (tc1 || tc2) ? "${tc1}/${tc2}" : ''
            result.docsCount        = docstoreService.getDocsCount(result.vendor, contextService.getOrg())
            result.notesCount       = docstoreService.getNotesCount(result.vendor, contextService.getOrg())
            result.checklistCount   = workflowService.getWorkflowCount(result.vendor, contextService.getOrg())
        }

        SwissKnife.setPaginationParams(result, params, contextUser)
        result
    }

    Map<String, Map> getWekbVendors(GrailsParameterMap params) {
        Map<String, Object> result = [:], queryParams = [:]
        User contextUser = contextService.getUser()
        SwissKnife.setPaginationParams(result, params, contextUser)
        Locale locale = LocaleUtils.getCurrentLocale()

        result.flagContentGokb = true // vendorService.getWekbVendorRecords()
        Map queryCuratoryGroups = gokbService.executeQuery(Wekb.getGroupsURL(), [:])
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
            vendorQuery += " order by v.name "

        Set<Vendor> vendorsTotal = Vendor.executeQuery(vendorQuery, queryParams)

        result.vendorListTotal = vendorsTotal.size()
        result.vendorList = vendorsTotal.drop(result.offset).take(result.max)
        result.vendorTotal = vendorsTotal
        result

    }

    Set<Vendor> getCurrentVendors(Org context) {
        Set<Vendor> result = VendorRole.executeQuery("select v from VendorRole vr join vr.vendor as v where (vr.subscription in (select sub from OrgRole where org = :context and roleType in (:subRoleTypes)) or vr.license in (select lic from OrgRole where org = :context and roleType in (:licRoleTypes))) order by v.name",
                [context:context,
                 subRoleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIUM,RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER],
                 licRoleTypes:[RDStore.OR_LICENSING_CONSORTIUM,RDStore.OR_LICENSEE_CONS,RDStore.OR_LICENSEE]])
        result
    }

}
