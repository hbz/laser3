package de.laser

import de.laser.addressbook.Contact
import de.laser.addressbook.Person
import de.laser.addressbook.PersonRole
import de.laser.annotations.Check404
import de.laser.annotations.DebugInfo
import de.laser.auth.User
import de.laser.cache.EhcacheWrapper
import de.laser.helper.Params
import de.laser.properties.PropertyDefinition
import de.laser.remote.Wekb
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.utils.PdfUtils
import de.laser.utils.SwissKnife
import de.laser.wekb.ElectronicBilling
import de.laser.wekb.InvoiceDispatch
import de.laser.wekb.InvoicingVendor
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.ProviderLink
import de.laser.wekb.ProviderRole
import de.laser.wekb.VendorLink
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.xssf.streaming.SXSSFWorkbook

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat

@Secured(['IS_AUTHENTICATED_FULLY'])
class ProviderController {

    AddressbookService addressbookService
    ContextService contextService
    ExportClickMeService exportClickMeService
    GenericOIDService genericOIDService
    GokbService gokbService
    LinksGenerationService linksGenerationService
    ProviderService providerService
    PropertyService propertyService
    TaskService taskService
    DocstoreService docstoreService
    WorkflowService workflowService

    public static final Map<String, String> CHECK404_ALTERNATIVES = [
            'list' : 'menu.public.all_providers'
    ]

    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def index() {
        redirect action: 'list'
    }

    /**
     * Call to list non-academic institutions such as providers. The list may be rendered
     * as HTML or a configurable Excel worksheet or CSV file. The export contains more fields
     * than the HTML table due to reasons of space in the HTML page
     * @return a list of provider organisations, either as HTML table or as Excel/CSV export
     * @see OrganisationService#exportOrg(java.util.List, java.lang.Object, boolean, java.lang.String)
     * @see ExportClickMeService#getExportOrgFields(java.lang.String)
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def list() {
        Map<String, Object> result = [institution: contextService.getOrg(), user: contextService.getUser()], queryParams = [:]
        result.propList    = PropertyDefinition.findAll( "from PropertyDefinition as pd where pd.descr = :def and (pd.tenant is null or pd.tenant = :tenant) order by pd.name_de asc", [
                def: PropertyDefinition.PRV_PROP,
                tenant: contextService.getOrg()
        ])

        Map queryCuratoryGroups = gokbService.executeQuery(Wekb.getGroupsURL(), [:])
        if(queryCuratoryGroups.error == 404) {
            result.error = message(code:'wekb.error.'+queryCuratoryGroups.error) as String
        }
        else {
            if (queryCuratoryGroups) {
                List recordsCuratoryGroups = queryCuratoryGroups.result
                result.curatoryGroups = recordsCuratoryGroups?.findAll { it.status == "Current" }
            }
            else result.curatoryGroups = []
        }
        result.curatoryGroupTypes = [
                [value: 'Provider', name: message(code: 'package.curatoryGroup.provider')],
                [value: 'Other', name: message(code: 'package.curatoryGroup.other')]
        ]
        List<String> queryArgs = []
        result.wekbRecords = providerService.getWekbProviderRecords(params, result)
        if(params.containsKey('nameContains')) {
            queryArgs << "(genfunc_filter_matcher(p.name, :name) = true or genfunc_filter_matcher(p.sortname, :name) = true)"
            queryParams.name = params.nameContains
        }
        if(params.containsKey('provStatus')) {
            queryArgs << "p.status in (:status)"
            queryParams.status = Params.getRefdataList(params, 'provStatus')
        }
        else if(!params.containsKey('provStatus') && !params.containsKey('filterSet')) {
            queryArgs << "p.status = :status"
            queryParams.status = "Current"
            params.provStatus = RDStore.PROVIDER_STATUS_CURRENT.id
        }

        if(params.containsKey('inhouseInvoicing')) {
            boolean inhouseInvoicing = params.inhouseInvoicing == 'on'
            if(inhouseInvoicing)
                queryArgs << "p.inhouseInvoicing = true"
            else queryArgs << "p.inhouseInvoicing = false"
        }

        if(params.containsKey('qp_invoicingVendors')) {
            queryArgs << "exists (select iv from p.invoicingVendors iv where iv.vendor.id in (:vendors))"
            queryParams.put('vendors', Params.getLongList(params, 'qp_invoicingVendors'))
        }

        if(params.containsKey('qp_electronicBillings')) {
            queryArgs << "exists (select eb from p.electronicBillings eb where eb.invoicingFormat in (:electronicBillings))"
            queryParams.put('electronicBillings', Params.getRefdataList(params, 'qp_electronicBillings'))
        }

        if(params.containsKey('qp_invoiceDispatchs')) {
            queryArgs << "exists (select idi from p.invoiceDispatchs idi where idi.invoiceDispatch in (:invoiceDispatchs))"
            queryParams.put('invoiceDispatchs', Params.getRefdataList(params, 'qp_invoiceDispatchs'))
        }

        if(params.containsKey('curatoryGroup') || params.containsKey('curatoryGroupType')) {
            queryArgs << "p.gokbId in (:wekbIds)"
            queryParams.wekbIds = result.wekbRecords.keySet()
        }
        String providerQuery = 'select p from Provider p'
        if(queryArgs) {
            providerQuery += ' where '+queryArgs.join(' and ')
        }
        if (params.filterPropDef) {
            Map<String, Object> efq = propertyService.evalFilterQuery(params, providerQuery, 'p', queryParams)
            providerQuery = efq.query
            queryParams = efq.queryParams as Map<String, Object>
        }
        if(params.containsKey('sort')) {
            providerQuery += " order by ${params.sort} ${params.order ?: 'asc'}, p.name ${params.order ?: 'asc'} "
        }
        else
            providerQuery += " order by p.name "
        Set<Provider> providersTotal = Provider.executeQuery(providerQuery, queryParams)
        result.currentProviderIdList = Provider.executeQuery('select pvr.provider.id from ProviderRole pvr, OrgRole oo join oo.sub s where s = pvr.subscription and oo.org = :context and s.status = :current', [current: RDStore.SUBSCRIPTION_CURRENT, context: contextService.getOrg()])

        result.filterSet = params.filterSet ? true : false

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        if (params.isMyX) {
            List<String> xFilter = params.list('isMyX')
            Set<Long> f1Result = [], f2Result = []
            boolean   f1Set = false, f2Set = false

            if (xFilter.contains('ismyx_exclusive')) {
                f1Result.addAll( providersTotal.findAll { result.currentProviderIdList.contains( it.id ) }.collect{ it.id } )
                f1Set = true
            }
            if (xFilter.contains('ismyx_not')) {
                f1Result.addAll( providersTotal.findAll { ! result.currentProviderIdList.contains( it.id ) }.collect{ it.id }  )
                f1Set = true
            }
            if (xFilter.contains('wekb_exclusive')) {
                f2Result.addAll( providersTotal.findAll { it.gokbId != null }.collect{ it.id } )
                f2Set = true
            }
            if (xFilter.contains('wekb_not')) {
                f2Result.addAll( providersTotal.findAll { it.gokbId == null }.collect{ it.id }  )
                f2Set = true
            }

            if (f1Set) { providersTotal = providersTotal.findAll { f1Result.contains(it.id) } }
            if (f2Set) { providersTotal = providersTotal.findAll { f2Result.contains(it.id) } }
        }

        result.providersTotal = providersTotal.size()
        result.providerList   = providersTotal.drop((int) result.offset).take((int) result.max)

        String message = message(code: 'export.all.providers') as String
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
                    SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportProviders(providersTotal, selectedFields, ExportClickMeService.FORMAT.XLS, contactSwitch)
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
                        writer.write((String) exportClickMeService.exportProviders(providersTotal, selectedFields, ExportClickMeService.FORMAT.CSV, contactSwitch))
                    }
                    out.close()
                    return
                case 'pdf':
                    Map<String, Object> pdfOutput = exportClickMeService.exportProviders(providersTotal, selectedFields, ExportClickMeService.FORMAT.PDF, contactSwitch)

                    byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                    response.setHeader('Content-disposition', 'attachment; filename="'+ filename +'.pdf"')
                    response.setContentType('application/pdf')
                    response.outputStream.withStream { it << pdf }
                    return
            }
        }
        else {
            result
        }
    }

    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def show() {
        Map<String, Object> result = providerService.getResultGenericsAndCheckAccess(params)

        if (! result) {
            response.sendError(401)
            return
        }
        if(result.error)
            flash.error = result.error //to display we:kb's eventual 404
        if(params.containsKey('id')) {
            Provider provider = Provider.get(params.id)
            result.provider = provider
            result.editable = provider.gokbId ? false : contextService.isInstEditor()
            result.subEditable = contextService.isInstEditor()
            result.isMyProvider = providerService.isMyProvider(provider)
            String subscriptionConsortiumFilter = '', licenseConsortiumFilter = ''
            if(contextService.getOrg().isCustomerType_Consortium()) {
                subscriptionConsortiumFilter = 'and s.instanceOf = null'
                licenseConsortiumFilter = 'and l.instanceOf = null'
            }
            result.tasks = taskService.getTasksByResponsibilityAndObject(result.user, provider)
            Set<Package> allPackages = provider.packages
            result.allPackages = allPackages
            result.allPlatforms = allPackages.findAll { Package pkg -> pkg.nominalPlatform != null}.nominalPlatform.toSet()
            result.packages = Package.executeQuery('select pkg from SubscriptionPackage sp join sp.pkg pkg, OrgRole oo join oo.sub s where pkg.provider = :provider and s = sp.subscription and s.status = :current and oo.org = :context '+subscriptionConsortiumFilter, [provider: provider, current: RDStore.SUBSCRIPTION_CURRENT, context: contextService.getOrg()]) as Set<Package>
            result.platforms = Platform.executeQuery('select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg, OrgRole oo join oo.sub s where pkg.provider = :provider and oo.sub = sp.subscription and s.status = :current and oo.org = :context '+subscriptionConsortiumFilter, [provider: provider, current: RDStore.SUBSCRIPTION_CURRENT, context: contextService.getOrg()]) as Set<Platform>
            result.links = ProviderLink.executeQuery('select pl from ProviderLink pl where pl.from = :provider or pl.to = :provider', [provider: provider])
            result.currentSubscriptionsCount = ProviderRole.executeQuery('select count(*) from ProviderRole pvr join pvr.subscription s join s.orgRelations oo where pvr.provider = :provider and s.status = :current and oo.org = :context '+subscriptionConsortiumFilter, [provider: provider, current: RDStore.SUBSCRIPTION_CURRENT, context: contextService.getOrg()])[0]
            result.currentLicensesCount = ProviderRole.executeQuery('select count(*) from ProviderRole pvr join pvr.license l join l.orgRelations oo where pvr.provider = :provider and l.status = :current and oo.org = :context '+licenseConsortiumFilter, [provider: provider, current: RDStore.LICENSE_CURRENT, context: contextService.getOrg()])[0]
            result.subLinks = ProviderRole.executeQuery('select count(*) from ProviderRole pvr join pvr.subscription s join s.orgRelations oo where pvr.provider = :provider and oo.org = :context '+subscriptionConsortiumFilter, [provider: provider, context: contextService.getOrg()])[0]
            result.licLinks = ProviderRole.executeQuery('select count(*) from ProviderRole pvr join pvr.license l join l.orgRelations oo where pvr.provider = :provider and oo.org = :context '+licenseConsortiumFilter, [provider: provider, context: contextService.getOrg()])[0]

            workflowService.executeCmdAndUpdateResult(result, params)
            if (result.provider.createdBy) {
                result.createdByOrgGeneralContacts = PersonRole.executeQuery(
                        "select distinct(prs) from PersonRole pr join pr.prs prs join pr.org oo " +
                                "where oo = :org and pr.functionType = :ft and prs.isPublic = true",
                        [org: result.provider.createdBy, ft: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]
                )
            }
            if (result.provider.legallyObligedBy) {
                result.legallyObligedByOrgGeneralContacts = PersonRole.executeQuery(
                        "select distinct(prs) from PersonRole pr join pr.prs prs join pr.org oo " +
                                "where oo = :org and pr.functionType = :ft and prs.isPublic = true",
                        [org: result.provider.legallyObligedBy, ft: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]
                )
            }
            result
        }
        result
    }

    /**
     * Creates a new provider organisation with the given parameters
     * @return the details view of the provider or the creation view in case of an error
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def createProvider() {
        Provider.withTransaction {

            Provider provider = new Provider(name: params.provider, status: RDStore.PROVIDER_STATUS_CURRENT, createdBy: contextService.getOrg())
            provider.setGlobalUID()
            if (provider.save()) {
                flash.message = message(code: 'default.created.message', args: [message(code: 'provider.label'), provider.name]) as String
                redirect action: 'show', id: provider.id
                return
            }
            else {
                log.error("Problem creating org: ${provider.errors}");
                flash.message = message(code: 'org.error.createProviderError', args: [provider.errors]) as String
                redirect(action: 'findProviderMatches')
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
    def findProviderMatches() {

        Map<String, Object> result = [:]
        if ( params.proposedProvider ) {

            result.providerMatches= Provider.executeQuery("from Provider as p where (genfunc_filter_matcher(p.name, :searchName) = true or genfunc_filter_matcher(p.sortname, :searchName) = true) ",
                    [searchName: params.proposedProvider])
        }
        result
    }

    /**
     * Links two providers with the given params
     */
    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def link() {
        linksGenerationService.linkProviderVendor(params, ProviderLink.class.name)
        redirect action: 'show', id: params.context.split(':')[1]
    }

    /**
     * Removes the given link between two providers
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
    @Check404(domain=Provider)
    def addressbook() {
        Map<String, Object> result = providerService.getResultGenericsAndCheckAccess(params)
        if(!result) {
            response.sendError(401)
            return
        }

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.provider = result.provider
        params.sort = params.sort ?: 'p.last_name, p.first_name'
        params.tab = params.tab ?: 'contacts'

        EhcacheWrapper cache = contextService.getUserCache("/provider/addressbook/${params.id}")
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

        /*
        if (visiblePersons){
            result.emailAddresses = Contact.executeQuery("select new map(c.prs as person, c.content as mail) from Contact c join c.prs p join p.roleLinks pr join pr.org o where p in (:persons) and c.contentType = :contentType order by o.sortname",
                    [persons: visiblePersons, contentType: RDStore.CCT_EMAIL])
        }
        */
        Map<Org, String> emailAddresses = [:]
        visiblePersons.each { Person p ->
            Contact mail = Contact.findByPrsAndContentType(p, RDStore.CCT_EMAIL)
            if(mail) {
                String oid
                if(p.roleLinks.provider[0]) {
                    oid = genericOIDService.getOID(p.roleLinks.provider[0])
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
    @Check404(domain=Provider)
    def tasks() {
        Map<String,Object> result = providerService.getResultGenericsAndCheckAccess(params)
        if (!result) {
            response.sendError(401); return
        }
        SwissKnife.setPaginationParams(result, params, result.user as User)
        result.cmbTaskInstanceList = taskService.getTasks((User) result.user, (Provider) result.provider)['cmbTaskInstanceList']

        result
    }

    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_PRO)
    })
    @Check404(domain=Provider)
    def workflows() {
        Map<String, Object> result = providerService.getResultGenericsAndCheckAccess(params)

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
    @Check404(domain=Provider)
    def notes() {
        Map<String, Object> result = providerService.getResultGenericsAndCheckAccess(params)
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
    @Check404(domain=Provider)
    def documents() {
        Map<String, Object> result = providerService.getResultGenericsAndCheckAccess(params)
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
     * Assigns the given discovery system to the given organisation
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def addAttribute() {
        Map<String, Object> result = providerService.getResultGenericsAndCheckAccess(params)

        if (!result.provider) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'provider.label'), params.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        def newAttr = genericOIDService.resolveOID(params.get(params.field))
        if (result.editable) {
            switch(params.field) {
                case 'invoicingFormat':
                    if (!newAttr) {
                        flash.message = message(code: 'default.not.found.message', args: [message(code: 'vendor.invoicing.formats.label'), params.frontend]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    if (result.provider.getElectronicBillings().find { ElectronicBilling eb -> eb.invoicingFormat.id == newAttr.id }) {
                        flash.message = message(code: 'default.err.alreadyExist', args: [message(code: 'vendor.invoicing.formats.label')]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    result.provider.addToElectronicBillings(invoicingFormat: newAttr)
                break
                case 'invoiceDispatch':
                    if (!newAttr) {
                        flash.message = message(code: 'default.not.found.message', args: [message(code: 'vendor.invoicing.dispatch.label'), params.index]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    if (result.provider.getInvoiceDispatchs().find { InvoiceDispatch idi -> idi.invoiceDispatch.id == newAttr.id }) {
                        flash.message = message(code: 'default.err.alreadyExist', args: [message(code: 'vendor.invoicing.dispatch.label')]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    result.provider.addToInvoiceDispatchs(invoiceDispatch: newAttr)
                break
                case 'invoicingVendor':
                    if (!newAttr) {
                        flash.message = message(code: 'default.not.found.message', args: [message(code: 'vendor.invoicing.vendors.label'), params.index]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    if (result.provider.getInvoicingVendors().find { InvoicingVendor iv -> iv.vendor.id == newAttr.id }) {
                        flash.message = message(code: 'default.err.alreadyExist', args: [message(code: 'vendor.invoicing.vendors.label')]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    result.provider.addToInvoicingVendors(vendor: newAttr)
                break
            }
            result.provider.save()
        }

        redirect action: 'show', id: params.id
    }

    /**
     * Removes the given discovery system from the given organisation
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def deleteAttribute() {
        Map<String, Object> result = providerService.getResultGenericsAndCheckAccess(params)

        if (!result.provider) {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'provider.label'), params.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        if (result.editable) {
            def attr = genericOIDService.resolveOID(params.removeObjectOID)
            switch(params.field) {
                case 'invoicingFormat': result.provider.removeFromElectronicBillings(attr)
                    break
                case 'invoiceDispatch': result.provider.removeFromInvoiceDispatchs(attr)
                    break
                case 'invoicingVendor': result.provider.removeFromInvoicingVendors(attr)
                    break
            }
            result.provider.save()
            attr.delete()
        }

        redirect(url: request.getHeader('referer'))
    }
}
