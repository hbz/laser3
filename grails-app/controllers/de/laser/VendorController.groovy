package de.laser

import de.laser.addressbook.Contact
import de.laser.addressbook.Person
import de.laser.addressbook.PersonRole
import de.laser.annotations.Check404
import de.laser.annotations.DebugInfo
import de.laser.auth.User
import de.laser.cache.EhcacheWrapper
import de.laser.helper.Params
import de.laser.helper.Profiler
import de.laser.properties.PropertyDefinition
import de.laser.remote.Wekb
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.utils.PdfUtils
import de.laser.utils.SwissKnife
import de.laser.wekb.ElectronicBilling
import de.laser.wekb.ElectronicDeliveryDelayNotification
import de.laser.wekb.InvoiceDispatch
import de.laser.wekb.LibrarySystem
import de.laser.wekb.Package
import de.laser.wekb.PackageVendor
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import de.laser.wekb.VendorLink
import de.laser.wekb.VendorRole
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.xssf.streaming.SXSSFWorkbook

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat

@Secured(['IS_AUTHENTICATED_FULLY'])
class VendorController {

    AddressbookService addressbookService
    ContextService contextService
    DocstoreService docstoreService
    ExportClickMeService exportClickMeService
    GenericOIDService genericOIDService
    GokbService gokbService
    LinksGenerationService linksGenerationService
    PropertyService propertyService
    TaskService taskService
    VendorService vendorService
    WorkflowService workflowService

    public static final Map<String, String> CHECK404_ALTERNATIVES = [
            'list' : 'menu.public.all_vendors'
    ]

    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def index() {
        redirect action: 'list'
    }

    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def list() {
        Profiler prf = new Profiler()
        prf.startSimpleBench()
        Map<String, Object> result = vendorService.getResultGenerics(params), queryParams = [:]
        result.flagContentGokb = true // vendorService.getWekbVendorRecords()
        result.propList    = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.VEN_PROP], contextService.getOrg())
        prf.setBenchmark("get curatory groups")
        Map queryCuratoryGroups = gokbService.executeQuery(Wekb.getGroupsURL(), [:])
        prf.setBenchmark("get we:kb vendors")
        if(queryCuratoryGroups.code == 404) {
            result.error = message(code: 'wekb.error.'+queryCuratoryGroups.error) as String
        }
        else {
            if (queryCuratoryGroups) {
                List recordsCuratoryGroups = queryCuratoryGroups.result
                result.curatoryGroups = recordsCuratoryGroups?.findAll { it.status == "Current" }
            }
            result.wekbRecords = vendorService.getWekbVendorRecords(params, result)
        }
        result.curatoryGroupTypes = [
                [value: 'Provider', name: message(code: 'package.curatoryGroup.provider')],
                [value: 'Vendor', name: message(code: 'package.curatoryGroup.vendor')],
                [value: 'Other', name: message(code: 'package.curatoryGroup.other')]
        ]
        List<String> queryArgs = []
        if(params.containsKey('nameContains')) {
            queryArgs << "(genfunc_filter_matcher(v.name, :name) = true or genfunc_filter_matcher(v.sortname, :name) = true or exists(select a from v.altnames a where genfunc_filter_matcher(a.name, :name) = true))"
            queryParams.name = params.nameContains
        }
        if(params.containsKey('venStatus')) {
            queryArgs << "v.status in (:status)"
            queryParams.status = Params.getRefdataList(params, 'venStatus')
        }
        else if(params.containsKey('filterSet')) {
            queryArgs << "v.status != :status"
            queryParams.status = RDStore.VENDOR_STATUS_REMOVED
        }
        else if(!params.containsKey('venStatus') && !params.containsKey('filterSet')) {
            queryArgs << "v.status = :status"
            queryParams.status = RDStore.VENDOR_STATUS_CURRENT
            params.venStatus = RDStore.VENDOR_STATUS_CURRENT.id
        }

        if(params.containsKey('qp_supportedLibrarySystems')) {
            queryArgs << "exists (select ls from v.supportedLibrarySystems ls where ls.librarySystem in (:librarySystems))"
            queryParams.put('librarySystems', Params.getRefdataList(params, 'qp_supportedLibrarySystems'))
        }

        if(params.containsKey('qp_electronicBillings')) {
            queryArgs << "exists (select eb from v.electronicBillings eb where eb.invoicingFormat in (:electronicBillings))"
            queryParams.put('electronicBillings', Params.getRefdataList(params, 'qp_electronicBillings'))
        }

        if(params.containsKey('qp_invoiceDispatchs')) {
            queryArgs << "exists (select idi from v.invoiceDispatchs idi where idi.invoiceDispatch in (:invoiceDispatchs))"
            queryParams.put('invoiceDispatchs', Params.getRefdataList(params, 'qp_invoiceDispatchs'))
        }

        if(params.containsKey('curatoryGroup') || params.containsKey('curatoryGroupType')) {
            queryArgs << "v.gokbId in (:wekbIds)"
            queryParams.wekbIds = result.wekbRecords.keySet()
        }

        String vendorQuery = 'select v from Vendor v'
        if(queryArgs) {
            vendorQuery += ' where '+queryArgs.join(' and ')
        }

        if (params.filterPropDef) {
            Map<String, Object> efq = propertyService.evalFilterQuery(params, vendorQuery, 'v', queryParams)
            vendorQuery = efq.query
            queryParams = efq.queryParams as Map<String, Object>
        }

        if(params.containsKey('sort')) {
            vendorQuery += " order by ${params.sort} ${params.order ?: 'asc'}, v.name ${params.order ?: 'asc'} "
        }
        else
            vendorQuery += " order by v.name "
        prf.setBenchmark("get total vendors")
        Set<Vendor> vendorsTotal = Vendor.executeQuery(vendorQuery, queryParams)
        prf.setBenchmark("get subscribed vendors")
        result.currentVendorIdList = Vendor.executeQuery('select vr.vendor.id from VendorRole vr, OrgRole oo join oo.sub s where s = vr.subscription and oo.org = :context and s.status = :current', [current: RDStore.SUBSCRIPTION_CURRENT, context: contextService.getOrg()])
        if (params.isMyX) {
            List<String> xFilter = params.list('isMyX')
            Set<Long> f1Result = [], f2Result = []
            boolean   f1Set = false, f2Set = false

            if (xFilter.contains('ismyx_exclusive')) {
                f1Result.addAll( vendorsTotal.findAll { result.currentVendorIdList.contains( it.id ) }.collect{ it.id } )
                f1Set = true
            }
            if (xFilter.contains('ismyx_not')) {
                f1Result.addAll( vendorsTotal.findAll { ! result.currentVendorIdList.contains( it.id ) }.collect{ it.id }  )
                f1Set = true
            }
            if (xFilter.contains('wekb_exclusive')) {
                f2Result.addAll( vendorsTotal.findAll { it.gokbId != null && it.gokbId in result.wekbRecords.keySet() }.collect{ it.id } )
                f2Set = true
            }
            if (xFilter.contains('wekb_not')) {
                f2Result.addAll( vendorsTotal.findAll { it.gokbId == null }.collect{ it.id }  )
                f2Set = true
            }

            if (f1Set) { vendorsTotal = vendorsTotal.findAll { f1Result.contains(it.id) } }
            if (f2Set) { vendorsTotal = vendorsTotal.findAll { f2Result.contains(it.id) } }
        }
        String message = message(code: 'export.all.vendors') as String
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        String datetoday = sdf.format(new Date())
        String filename = message+"_${datetoday}"
        Map<String, Object> selectedFields = [:]
        Set<String> contactSwitch = []
        if(params.fileformat) {
            if (params.filename) {
                filename =params.filename
            }
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            contactSwitch.addAll(params.list("contactSwitch"))
            contactSwitch.addAll(params.list("addressSwitch"))
            switch(params.fileformat) {
                case 'xlsx':
                    SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportVendors(vendorsTotal, selectedFields, ExportClickMeService.FORMAT.XLS, contactSwitch)
                    response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                    response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    wb.write(response.outputStream)
                    response.outputStream.flush()
                    response.outputStream.close()
                    wb.dispose()
                    return
                case 'csv':
                    response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { writer ->
                        writer.write((String) exportClickMeService.exportVendors(vendorsTotal, selectedFields, ExportClickMeService.FORMAT.CSV, contactSwitch))
                    }
                    out.close()
                    return
                case 'pdf':
                    Map<String, Object> pdfOutput = exportClickMeService.exportVendors(vendorsTotal, selectedFields, ExportClickMeService.FORMAT.PDF, contactSwitch)

                    byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                    response.setHeader('Content-disposition', 'attachment; filename="'+ filename +'.pdf"')
                    response.setContentType('application/pdf')
                    response.outputStream.withStream { it << pdf }
                    return
            }
        }
        else {
            prf.setBenchmark("end loading")
            result.vendorListTotal = vendorsTotal.size()
            result.vendorList = vendorsTotal.drop(result.offset).take(result.max)
            result.benchMark = prf.stopBenchmark()
            result
        }
    }

    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404(domain=Vendor)
    def show() {
        Map<String, Object> result = vendorService.getResultGenerics(params)
        if(params.containsKey('id')) {
            Vendor vendor = Vendor.get(params.id)
            result.vendor = vendor
            result.editable = vendor.gokbId ? false : contextService.isInstEditor()
            result.subEditable = contextService.isInstEditor()
            result.isMyVendor = vendorService.isMyVendor(vendor)
            String subscriptionConsortiumFilter = '', licenseConsortiumFilter = ''
            /*
            currently deactivated as of ERMS-6504
            if(contextService.getOrg().isCustomerType_Consortium()) {
                subscriptionConsortiumFilter = 'and s.instanceOf = null'
                licenseConsortiumFilter = 'and l.instanceOf = null'
            }
            */
            Set<Package> allPackages = vendor.packages.findAll { PackageVendor pv -> pv.pkg.packageStatus != RDStore.PACKAGE_STATUS_REMOVED }.pkg.sort { Package pkg -> pkg.name }
            Set<Package> allMyPackages = Package.executeQuery('select sp.pkg from SubscriptionPackage sp, OrgRole oo join oo.sub s where sp.subscription = s and s.status = :current and oo.org = :context '+subscriptionConsortiumFilter, [current: RDStore.SUBSCRIPTION_CURRENT, context: contextService.getOrg()]) as Set<Package>
            Set<Package> myPackages = Package.executeQuery('select pkg from PackageVendor pv join pv.pkg pkg, VendorRole vr where pkg in (:myPkgs) and vr.subscription in (select s from OrgRole oo join oo.sub s where s.status = :current and oo.org = :context '+subscriptionConsortiumFilter+') order by pkg.name', [myPkgs: allMyPackages, current: RDStore.SUBSCRIPTION_CURRENT, context: contextService.getOrg()])
            result.allPackages = allPackages
            result.packages = myPackages
            result.providers = Provider.executeQuery('select p from Package pkg join pkg.provider p where pkg in (:allPackages) order by p.name', [allPackages: allPackages]).toSet()
            result.myProviders = Provider.executeQuery('select p from Package pkg join pkg.provider p where pkg in (:myPkgs) order by p.name', [myPkgs: myPackages]).toSet()
            result.tasks = taskService.getTasksByResponsibilityAndObject(result.user, vendor)
            result.links = VendorLink.executeQuery('select vl from VendorLink vl where vl.from = :vendor or vl.to = :vendor', [vendor: vendor])
            result.currentSubscriptionsCount = VendorRole.executeQuery('select count(*) from VendorRole vr join vr.subscription s join s.orgRelations oo where vr.vendor = :vendor and s.status = :current and oo.org = :context '+subscriptionConsortiumFilter, [vendor: vendor, current: RDStore.SUBSCRIPTION_CURRENT, context: contextService.getOrg()])[0]
            result.currentLicensesCount  = VendorRole.executeQuery('select count(*) from VendorRole vr join vr.license l join l.orgRelations oo where vr.vendor = :vendor and l.status = :current and oo.org = :context '+licenseConsortiumFilter, [vendor: vendor, current: RDStore.LICENSE_CURRENT, context: contextService.getOrg()])[0]
            result.subLinks = VendorRole.executeQuery('select count(*) from VendorRole vr join vr.subscription s join s.orgRelations oo where vr.vendor = :vendor and oo.org = :context '+subscriptionConsortiumFilter, [vendor: vendor, context: contextService.getOrg()])[0]
            result.licLinks = VendorRole.executeQuery('select count(*) from VendorRole vr join vr.license l join l.orgRelations oo where vr.vendor = :vendor and oo.org = :context '+licenseConsortiumFilter, [vendor: vendor, context: contextService.getOrg()])[0]

            workflowService.executeCmdAndUpdateResult(result, params)
            if (result.vendor.createdBy) {
                result.createdByOrgGeneralContacts = PersonRole.executeQuery(
                        "select distinct(prs) from PersonRole pr join pr.prs prs join pr.org oo " +
                                "where oo = :org and pr.functionType = :ft and prs.isPublic = true",
                        [org: result.vendor.createdBy, ft: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]
                )
            }
            if (result.vendor.legallyObligedBy) {
                result.legallyObligedByOrgGeneralContacts = PersonRole.executeQuery(
                        "select distinct(prs) from PersonRole pr join pr.prs prs join pr.org oo " +
                                "where oo = :org and pr.functionType = :ft and prs.isPublic = true",
                        [org: result.vendor.legallyObligedBy, ft: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]
                )
            }
            result
        }
        else {
            response.sendError(404)
            return
        }
    }

    /**
     * Creates a new provider organisation with the given parameters
     * @return the details view of the provider or the creation view in case of an error
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def createVendor() {
        Vendor.withTransaction {

            Vendor vendor = new Vendor(name: params.vendor, status: RDStore.VENDOR_STATUS_CURRENT, createdBy: contextService.getOrg())
            vendor.setGlobalUID()
            if (vendor.save()) {
                flash.message = message(code: 'default.created.message', args: [message(code: 'vendor.label'), vendor.name]) as String
                redirect action: 'show', id: vendor.id
                return
            }
            else {
                log.error("Problem creating vendor: ${vendor.errors}");
                flash.message = message(code: 'org.error.createVendorError', args: [vendor.errors]) as String
                redirect(action: 'findVendorMatches')
                return
            }
        }
    }

    /**
     * Call to create a new provider; offers first a query for the new name to insert in order to exclude duplicates
     * @return the empty form (with a submit to proceed with the new organisation) or a list of eventual name matches
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def findVendorMatches() {

        Map<String, Object> result = [:]
        if ( params.proposedVendor ) {

            result.vendorMatches= Vendor.executeQuery("from Vendor as v where (genfunc_filter_matcher(v.name, :searchName) = true or genfunc_filter_matcher(v.sortname, :searchName) = true or exists(select a from v.altnames a where genfunc_filter_matcher(a.name, :searchName) = true)) ",
                    [searchName: params.proposedVendor])
        }
        result
    }

    /**
     * Links two vendors with the given params
     */
    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def link() {
        linksGenerationService.linkProviderVendor(params, VendorLink.class.name)
        redirect action: 'show', id: params.context.split(':')[1]
    }

    /**
     * Removes the given link between two vendors
     */
    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def unlink() {
        linksGenerationService.unlinkProviderVendor(params)
        redirect action: 'show', id: params.id
    }

    /**
     * Call to list the public contacts of the given provider
     * @return a table view of public contacts
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    @Check404(domain=Vendor)
    def addressbook() {
        Map<String, Object> result = vendorService.getResultGenerics(params)
        if(!result) {
            response.sendError(401)
            return
        }

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.vendor = result.vendor
        params.sort = params.sort ?: 'p.last_name, p.first_name'
        params.tab = params.tab ?: 'contacts'

        EhcacheWrapper cache = contextService.getUserCache("/vendor/addressbook/${params.id}")
        switch(params.tab) {
            case 'contacts':
                result.personOffset = result.offset
                result.addressOffset = cache.get('addressOffset') ?: 0
                break
            case 'addresses':
                result.addressOffset = result.offset
                result.personOffset = cache.get('personOffset') ?: 0
                break
        }
        cache.put('personOffset', result.personOffset)
        cache.put('addressOffset', result.addressOffset)

        Map<String, Object> configMap = params.clone()

        List visiblePersons = addressbookService.getVisiblePersons("addressbook", configMap+[offset: result.personOffset]),
             visibleAddresses = addressbookService.getVisibleAddresses("addressbook", configMap+[offset: result.addressOffset])

        result.propList =
                PropertyDefinition.findAllWhere(
                        descr: PropertyDefinition.PRS_PROP,
                        tenant: contextService.getOrg() // private properties
                )

        result.num_visiblePersons = visiblePersons.size()
        result.visiblePersons = visiblePersons.drop(result.personOffset).take(result.max)
        result.num_visibleAddresses = visibleAddresses.size()
        result.addresses = visibleAddresses.drop(result.addressOffset).take(result.max)

        Map<Org, String> emailAddresses = [:]
        visiblePersons.each { Person p ->
            Contact mail = Contact.findByPrsAndContentType(p, RDStore.CCT_EMAIL)
            if(mail) {
                String oid
                if(p.roleLinks.vendor[0]) {
                    oid = genericOIDService.getOID(p.roleLinks.vendor[0])
                }
                if(oid) {
                    Set<String> mails = emailAddresses.get(oid)
                    if(!mails)
                        mails = []
                    mails << mail.content
                    emailAddresses.put(oid, mails)
                }
            }
        }
        result.emailAddresses = emailAddresses

        result
    }

    /**
     * Shows the tasks attached to the given provider. Displayed here are tasks which
     * are related to the given provider (i.e. which have the given provider as target)
     * and not such assigned to the given one!
     * @return the task table view
     * @see Task
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_PRO)
    })
    @Check404(domain=Vendor)
    def tasks() {
        Map<String,Object> result = vendorService.getResultGenerics(params)
        if (!result) {
            response.sendError(401); return
        }
        SwissKnife.setPaginationParams(result, params, result.user as User)
        result.cmbTaskInstanceList = taskService.getTasks((User) result.user, (Vendor) result.vendor)['cmbTaskInstanceList']

        result
    }

    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_PRO)
    })
    @Check404()
    def workflows() {
        Map<String, Object> result = vendorService.getResultGenerics(params)

        workflowService.executeCmdAndUpdateResult(result, params)
        result
    }

    /**
     * Opens the notes view for the given provider
     * @return a {@link List} of notes ({@link Doc})
     * @see Doc
     * @see DocContext
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404(domain=Vendor)
    def notes() {
        Map<String, Object> result = vendorService.getResultGenerics(params)
        if(!result) {
            response.sendError(401)
            return
        }
        result
    }

    /**
     * Shows the documents attached to the given provider
     * @return the document table view
     * @see Doc
     * @see DocContext
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    @Check404(domain=Vendor)
    def documents() {
        Map<String, Object> result = vendorService.getResultGenerics(params)
        if(!result) {
            response.sendError(401)
            return
        }

        if (params.bulk_op) {
            docstoreService.bulkDocOperation(params, result, flash)
        }
        result
    }

    /**
     * Assigns the given subject group to the given organisation
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def addAttribute() {
        Map<String, Object> result = vendorService.getResultGenerics(params)

        if (!result.vendor) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'vendor'), params.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        def newAttr = genericOIDService.resolveOID(params.get(params.field))
        if (result.editable) {
            switch(params.field) {
                case 'librarySystem':
                    if (!newAttr) {
                        flash.message = message(code: 'default.not.found.message', args: [message(code: 'vendor.ordering.supportedLibrarySystems.label'), params.get(params.field)]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    if (result.vendor.supportedLibrarySystems.find { LibrarySystem ls -> ls.librarySystem.id == newAttr.id }) {
                        flash.message = message(code: 'default.err.alreadyExist', args: [message(code: 'vendor.ordering.supportedLibrarySystems.label')]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    result.vendor.addToSupportedLibrarySystems(librarySystem: newAttr)
                    break
                case 'delayNotification':
                    if (!newAttr) {
                        flash.message = message(code: 'default.not.found.message', args: [message(code: 'vendor.ordering.electronicDeliveryDelayNotifications.label'), params.get(params.field)]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    if (result.vendor.electronicDeliveryDelays.find { ElectronicDeliveryDelayNotification eddn -> eddn.delayNotification.id == newAttr.id }) {
                        flash.message = message(code: 'default.err.alreadyExist', args: [message(code: 'vendor.ordering.electronicDeliveryDelayNotifications.label')]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    result.vendor.addToElectronicDeliveryDelays(delayNotification: newAttr)
                    break
                case 'invoicingFormat':
                    if (!newAttr) {
                        flash.message = message(code: 'default.not.found.message', args: [message(code: 'vendor.invoicing.formats.label'), params.get(params.field)]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    if (result.vendor.electronicBillings.find { ElectronicBilling eb -> eb.invoicingFormat.id == newAttr.id }) {
                        flash.message = message(code: 'default.err.alreadyExist', args: [message(code: 'vendor.invoicing.formats.label')]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    result.vendor.addToElectronicBillings(invoicingFormat: newAttr)
                    break
                case 'invoiceDispatch':
                    if (!newAttr) {
                        flash.message = message(code: 'default.not.found.message', args: [message(code: 'vendor.invoicing.dispatch.label'), params.get(params.field)]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    if (result.vendor.invoiceDispatchs.find { InvoiceDispatch idi -> idi.invoiceDispatch.id == newAttr.id }) {
                        flash.message = message(code: 'default.err.alreadyExist', args: [message(code: 'vendor.invoicing.dispatch.label')]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    result.vendor.addToInvoiceDispatchs(invoiceDispatch: newAttr)
                    break
            }
            result.vendor.save()
        }

        redirect action: 'show', id: params.id
    }

    /**
     * Removes the given subject group from the given organisation
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def deleteAttribute() {
        Map<String, Object> result = vendorService.getResultGenerics(params)

        if (!result.vendor) {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'vendor'), params.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        if (result.editable) {
            def attr = genericOIDService.resolveOID(params.removeObjectOID)
            switch(params.field) {
                case 'invoicingFormat': result.vendor.removeFromElectronicBillings(attr)
                    break
                case 'invoiceDispatch': result.vendor.removeFromInvoiceDispatchs(attr)
                    break
            }
            result.vendor.save()
            attr.delete()
        }

        redirect(url: request.getHeader('referer'))
    }
}
