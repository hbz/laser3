package de.laser

import de.laser.addressbook.Contact
import de.laser.addressbook.Person
import de.laser.addressbook.PersonRole
import de.laser.annotations.Check404
import de.laser.annotations.DebugInfo
import de.laser.annotations.UnstableFeature
import de.laser.cache.EhcacheWrapper
import de.laser.ctrl.OrganisationControllerService
import de.laser.ctrl.UserControllerService
import de.laser.properties.OrgProperty
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.properties.PropertyDefinition
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.system.SystemEvent
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.utils.PdfUtils
import de.laser.utils.SwissKnife
import de.laser.wekb.Platform
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.web.servlet.mvc.GrailsParameterMap
import org.apache.http.HttpStatus
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.springframework.validation.FieldError

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat

/**
 * This controller manages calls related to organisations. An organisation - see the domain class - may be either
 * an academic institution or a commercial organisation like an editor, a provider or an agency. Former are referred
 * as institutions in the code, latter as organisations while organisation is also an umbrella term for both academic
 * institutions and commercial organisations. Check the definitions in the Org domain class for criteria to see which
 * organisation is of what kind
 * @see Org
 * @see OrgRole
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class OrganisationController  {

    AccessPointService accessPointService
    AddressbookService addressbookService
    ContextService contextService
    CustomerTypeService customerTypeService
    DeletionService deletionService
    DocstoreService docstoreService
    ExportClickMeService exportClickMeService
    FilterService filterService
    GenericOIDService genericOIDService
    IdentifierService identifierService
    InfoService infoService
    LinksGenerationService linksGenerationService
    OrganisationControllerService organisationControllerService
    OrganisationService organisationService
    TaskService taskService
    UserControllerService userControllerService
    UserService userService
    WorkflowService workflowService

    //-----

    /**
     * Map containing menu alternatives if an unexisting object has been called
     */
    public static final Map<String, String> CHECK404_ALTERNATIVES = [
            'list' : 'menu.public.all_orgs',                // todo: check perms
            'listConsortia' : 'menu.public.all_cons',       // todo: check perms
            'listInstitution' : 'menu.public.all_insts',    // todo: check perms
            'listProvider' : 'menu.public.all_providers'
    ]

    //-----

    /**
     * Redirects to {@link #list()}
     * @return the list view of organisations
     */
    @Secured(['ROLE_ADMIN'])
    def index() {
        redirect action: 'list', params: params
    }

    /**
     * Manages calls to the general organisation / institution settings page. The view is parametrised, thus different
     * returns are possible:
     * @return one of:
     * <ul>
     *     <li>api: API usage related settings such API level, key and password</li>
     *     <li>ezb: permissions to the Elektronische Zeitschriftenbibliothek (EZB) harvest access</li>
     *     <li>natstat: permissions to the Nationaler Statistikserver harvest access</li>
     *     <li>oamonitor: permissions to the Open Access Monitor harvest access</li>
     * </ul>
     */
    @DebugInfo(isInstAdm_denySupport_or_ROLEADMIN = ['FAKE,ORG_INST_BASIC,ORG_CONSORTIUM_BASIC'])
    @Secured(closure = {
        ctx.contextService.isInstAdm_denySupport_or_ROLEADMIN( 'FAKE,ORG_INST_BASIC,ORG_CONSORTIUM_BASIC' )
    })
    @Check404(domain=Org)
    def settings() {
        Map<String,Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        if (! result) {
            redirect controller: 'organisation', action: 'show', id: params.id
            return
        }
        if (! params.containsKey('tab')) {
            params.tab = result.orgInstance.isCustomerType_Pro() ? 'oamonitor' : 'natstat'
        }

        // TODO: erms-5467
        Boolean isComboRelated = Combo.findByFromOrgAndToOrg(result.orgInstance, contextService.getOrg())
        result.isComboRelated = isComboRelated

        Boolean hasAccess = (
                (result.inContextOrg && userService.hasFormalAffiliation(result.orgInstance, 'INST_ADM')) ||
                (isComboRelated && contextService.isInstAdm()) ||
                SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
        )

        // forbidden access
        if (! hasAccess) {
            redirect controller: 'organisation', action: 'show', id: result.orgInstance.id
            return
        }

        // adding default settings
        organisationService.initMandatorySettings(result.orgInstance)

        // collecting visible settings by customer type, role and/or combo
        List<OrgSetting> allSettings = OrgSetting.findAllByOrg(result.orgInstance)

        List<OrgSetting.KEYS> apiSet = [
                OrgSetting.KEYS.API_LEVEL,
                OrgSetting.KEYS.API_KEY,
                OrgSetting.KEYS.API_PASSWORD
        ]
        List<OrgSetting.KEYS> ezbSet = [
                OrgSetting.KEYS.EZB_SERVER_ACCESS
        ]
        List<OrgSetting.KEYS> oaMonitorSet = [
                OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS
        ]
        List<OrgSetting.KEYS> natstatSet = [
                OrgSetting.KEYS.NATSTAT_SERVER_API_KEY,
                OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID,
                OrgSetting.KEYS.NATSTAT_SERVER_ACCESS
        ]
        List<OrgSetting.KEYS> mailSet = [
                OrgSetting.KEYS.MAIL_REPLYTO_FOR_SURVEY,
                OrgSetting.KEYS.MAIL_SURVEY_FINISH_RESULT,
                OrgSetting.KEYS.MAIL_SURVEY_FINISH_RESULT_ONLY_BY_MANDATORY
        ]

        result.settings = []

        switch(params.tab) {
//            case 'general': result.settings.addAll(allSettings.findAll { OrgSetting os -> os.key in generalSet })
//                break
            case 'mail': result.settings.addAll(allSettings.findAll { OrgSetting os -> os.key in mailSet })
                break
            case 'api': result.settings.addAll(allSettings.findAll { OrgSetting os -> os.key in apiSet })
                break
            case 'ezb': result.settings.addAll(allSettings.findAll { OrgSetting os -> os.key in ezbSet })
                break
            case 'natstat': result.settings.addAll(allSettings.findAll { OrgSetting os -> os.key in natstatSet })
                break
            case 'oamonitor': result.settings.addAll(allSettings.findAll { OrgSetting os -> os.key in oaMonitorSet })
                break
        }
        result
    }

    /**
     * Call to list all institutions and commercial organisations. The list may be rendered as HTML list or exported as Excel worksheet or CSV file
     * @return the list of organisations, either as HTML page or as export
     */
    @Secured(['ROLE_ADMIN'])
    def list() {

        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.sort = params.sort ?: " LOWER(o.sortname), LOWER(o.name)"

        result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        FilterService.Result fsr = filterService.getOrgQuery(params)
        result.filterSet = params.filterSet ? true : false

        List<Org> orgListTotal  = Org.findAll(fsr.query, fsr.queryParams)
        result.orgList      = Org.executeQuery(fsr.query, fsr.queryParams + [max: result.max, offset: result.offset])
        result.orgListTotal = Org.executeQuery('select o.id ' + fsr.query, fsr.queryParams).size()

        SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
        String datetoday = sdf.format(new Date())
        String message = message(code: 'export.all.orgs') as String
        // Write the output to a file
        String file = message+"_${datetoday}"
        if ( params.exportXLS ) {

            try {
                SXSSFWorkbook wb = (SXSSFWorkbook) organisationService.exportOrg(orgListTotal, message, true,'xls')

                response.setHeader "Content-disposition", "attachment; filename=\"${file}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
                return

            }
            catch (Exception e) {
                log.error("Problem",e);
                response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                return
            }
        }
        else {
            withFormat {
                html {
                    result
                }
                csv {
                    response.setHeader("Content-disposition", "attachment; filename=\"${file}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { writer ->
                        writer.write((String) organisationService.exportOrg(orgListTotal,message,true,"csv"))
                    }
                    out.close()
                }
            }
        }
    }

    /**
     * Call to list the academic institutions without consortia
     * @return a list of institutions; basic consortia members or single users
     */
    @DebugInfo(isInstUser = [CustomerTypeService.ORG_CONSORTIUM_BASIC], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def listInstitution() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        params.customerType = customerTypeService.getOrgInstRoles().id

        if(!params.sort)
            params.sort = " LOWER(o.sortname)"

        FilterService.Result fsr = filterService.getOrgQuery(params)
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        List<Org> availableOrgs = Org.executeQuery(fsr.query, fsr.queryParams, [sort:params.sort])
        result.consortiaMemberIds = Combo.executeQuery('select cmb.fromOrg.id from Combo cmb where cmb.toOrg = :toOrg and cmb.type = :type',[toOrg: contextService.getOrg(), type: RDStore.COMBO_TYPE_CONSORTIUM])

        if (params.isMyX) {
            List<String> xFilter = params.list('isMyX')
            Set<Long> f1Result = []

            if (xFilter.contains('ismyx_exclusive')) {
                f1Result.addAll( availableOrgs.findAll { result.consortiaMemberIds.contains( it.id ) }.collect{ it.id } )
            }
            if (xFilter.contains('ismyx_not')) {
                f1Result.addAll( availableOrgs.findAll { ! result.consortiaMemberIds.contains( it.id ) }.collect{ it.id }  )
            }
            availableOrgs = availableOrgs.findAll { f1Result.contains(it.id) } as List<Org>

//            if (xFilter.contains('ismyx_exclusive')) {
//                availableOrgs = availableOrgs.findAll { result.consortiaMemberIds.contains( it.id ) }
//            }
//            else if (xFilter.contains('ismyx_not')) {
//                availableOrgs = availableOrgs.findAll { ! result.consortiaMemberIds.contains( it.id ) }
//            }
        }
        result.consortiaMemberTotal = availableOrgs.size()

        String message = message(code: 'menu.institutions') as String
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        String datetoday = sdf.format(new Date(System.currentTimeMillis()))
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
                    SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportOrgs(availableOrgs, selectedFields, 'institution', ExportClickMeService.FORMAT.XLS, contactSwitch)

                    response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                    response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    wb.write(response.outputStream)
                    response.outputStream.flush()
                    response.outputStream.close()
                    wb.dispose()
                    return //IntelliJ cannot know that the return prevents an obsolete redirect
                case 'csv':
                    response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { writer ->
                        writer.write((String) exportClickMeService.exportOrgs(availableOrgs, selectedFields, 'institution', ExportClickMeService.FORMAT.CSV, contactSwitch))
                    }
                    out.close()
                    return
                case 'pdf':
                    Map<String, Object> pdfOutput = exportClickMeService.exportOrgs(availableOrgs, selectedFields, 'institution', ExportClickMeService.FORMAT.PDF, contactSwitch)

                    byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                    response.setHeader('Content-disposition', 'attachment; filename="'+ filename +'.pdf"')
                    response.setContentType('application/pdf')
                    response.outputStream.withStream { it << pdf }
                    return
            }
        }
        else {
            result.totalOrgs = availableOrgs
            result.availableOrgs = availableOrgs.drop(result.offset).take(result.max)
            result
        }
    }

    /**
     * Inverse of listInstitution: lists for single users and basic members the consortia
     * @return a list of consortia institutions
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_INST_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.ORG_INST_BASIC)
    })
    Map listConsortia() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        params.customerType = [Role.findByAuthority('ORG_CONSORTIUM_PRO').id, Role.findByAuthority('ORG_CONSORTIUM_BASIC').id]
        if(!params.sort)
            params.sort = " LOWER(o.sortname)"
        FilterService.Result fsr = filterService.getOrgQuery(params)
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        List<Org> availableOrgs = Org.executeQuery(fsr.query, fsr.queryParams, [sort:params.sort])
        // TODO [ticket=2276]
        availableOrgs.removeAll(customerTypeService.getAllOrgsByCustomerType(CustomerTypeService.ORG_SUPPORT))

        String exportHeader = message(code: 'export.all.consortia')
        SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
        // Write the output to a file
        String file = "${sdf.format(new Date())}_"+exportHeader

        // ? --- copied from myInstitutionController.currentConsortia()
        GrailsParameterMap queryParams = params.clone() as GrailsParameterMap
        queryParams.clear()
        queryParams.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
        queryParams.subStatus = RDStore.SUBSCRIPTION_CURRENT.id
        queryParams.invertDirection = true
        FilterService.Result currentConsortiaFsr = filterService.getOrgComboQuery(queryParams, contextService.getOrg())
        if (currentConsortiaFsr.isFilterSet) { queryParams.filterSet = true }

        result.consortiaIds = Org.executeQuery(currentConsortiaFsr.query, currentConsortiaFsr.queryParams).collect{ it.id }
        // ? ---

        if (params.isMyX) {
            List<String> xFilter = params.list('isMyX')
            Set<Long> f1Result = []

            if (xFilter.contains('ismyx_exclusive')) {
                f1Result.addAll( availableOrgs.findAll { result.consortiaIds.contains( it.id ) }.collect{ it.id } )
            }
            if (xFilter.contains('ismyx_not')) {
                f1Result.addAll( availableOrgs.findAll { ! result.consortiaIds.contains( it.id ) }.collect{ it.id }  )
            }
            availableOrgs = availableOrgs.findAll { f1Result.contains(it.id) } as List<Org>
        }

        result.consortiaTotal = availableOrgs.size()
        result.availableOrgs  = availableOrgs.drop(result.offset).take(result.max)

        Set<String> contactSwitch = []
        Map<String, Object> selectedFields = [:]
        if(params.fileformat) {
            if (params.filename) {
                file = params.filename
            }
            contactSwitch.addAll(params.list("contactSwitch"))
            contactSwitch.addAll(params.list("addressSwitch"))
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            switch(params.fileformat) {
                case 'xlsx':
                    SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportOrgs(availableOrgs, selectedFields, 'consortium', ExportClickMeService.FORMAT.XLS, contactSwitch)

                    response.setHeader "Content-disposition", "attachment; filename=\"${file}.xlsx\""
                    response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    wb.write(response.outputStream)
                    response.outputStream.flush()
                    response.outputStream.close()
                    wb.dispose()
                    return
                case 'csv':
                    response.setHeader("Content-disposition", "attachment; filename=\"${file}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { writer ->
                        writer.write((String) exportClickMeService.exportOrgs(availableOrgs, selectedFields, 'consortium', ExportClickMeService.FORMAT.CSV, contactSwitch))
                    }
                    out.close()
                    return
                case 'pdf':
                    Map<String, Object> pdfOutput = exportClickMeService.exportOrgs(availableOrgs, selectedFields, 'consortium', ExportClickMeService.FORMAT.PDF, contactSwitch)

                    byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                    response.setHeader('Content-disposition', 'attachment; filename="'+ file +'.pdf"')
                    response.setContentType('application/pdf')
                    response.outputStream.withStream { it << pdf }
                    return
            }
        }
        else {
            result
        }
    }

    /**
     * Call to open the identifier creation modal; checks which namespaces are available for the given organisation
     * @return the identifier construction modal
     * @see IdentifierNamespace
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def createIdentifier(){
        log.debug("OrganisationController::createIdentifier ${params}")
        Org org   = params.id? Org.get(params.id) : null

        if (! org) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        //                List<IdentifierNamespace> nsList = IdentifierNamespace.where{(nsType == de.laser.Org.class.name || nsType == null)}
        /*
        List<IdentifierNamespace> nsList = IdentifierNamespace.where{(nsType == Org.class.name)}
                .list(sort: 'ns')
                .sort { a, b ->
            String aVal = a.getI10n('name') ?: a.ns
            String bVal = b.getI10n('name') ?: b.ns
            aVal.compareToIgnoreCase bVal
        }
        .collect{ it }
         */
        Set<String> primaryExcludes = [IdentifierNamespace.EZB_ANCHOR]
        List<IdentifierNamespace> nsList = IdentifierNamespace.executeQuery(
                'select idns from IdentifierNamespace idns where (idns.nsType = :org or idns.nsType = null) and idns.isFromLaser = true and idns.ns not in (:primaryExcludes) order by idns.name_' + LocaleUtils.getCurrentLang() + ', idns.ns',
                [org: Org.class.name, primaryExcludes: primaryExcludes])

        nsList = nsList - IdentifierNamespace.findAllByNsInList([IdentifierNamespace.CROSSREF_FUNDER_ID, IdentifierNamespace.DBPEDIA, IdentifierNamespace.LOC_ID, IdentifierNamespace.VIAF, IdentifierNamespace.WIKIDATA_ID])
        if(org.ids.find { Identifier id -> id.ns == IdentifierNamespace.findByNs(IdentifierNamespace.LEIT_ID) })
            nsList = nsList - IdentifierNamespace.findByNs(IdentifierNamespace.LEIT_ID)
        if(org.ids.find { Identifier id -> id.ns == IdentifierNamespace.findByNs(IdentifierNamespace.PEPPOL_RECEIVER_ID) })
            nsList = nsList - IdentifierNamespace.findByNs(IdentifierNamespace.PEPPOL_RECEIVER_ID)

        Map<String, Object> namespacesWithValidations = organisationService.getNamespacesWithValidations()

        render template: '/templates/identifier/modal_create', model: [orgInstance: org, nsList: nsList, namespacesWithValidations: namespacesWithValidations]
    }

    /**
     * Call to edit a given identifier; opens the creation modal with prefilled values
     * @return the identifier construction modal with prefilled values
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def editIdentifier(){
        log.debug("OrganisationController::editIdentifier ${params}")
        Identifier identifier = Identifier.get(params.identifier)
        Org org = identifier?.org

        Map<String, Object> namespacesWithValidations = organisationService.getNamespacesWithValidations()

        if (! identifier) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'default.search.identifier'), params.identifier]) as String
            redirect(url: request.getHeader('referer'))
            return
        }

        render template: '/templates/identifier/modal_create', model: [orgInstance: org, identifier: identifier, namespacesWithValidations: namespacesWithValidations]
    }

    /**
     * Processes the given identifier parameters and creates a new identifier record for the given organisation
     * @return the identifier list view
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def processCreateIdentifier(){
        log.debug("OrganisationController::processCreateIdentifier ${params}")
        Org org   = params.orgid ? Org.get(params.orgid) : null
        if ( ! (org && params.ns.id)){
            flash.error = message(code: 'menu.admin.error') as String
            redirect(url: request.getHeader('referer'))
            return
        }
        IdentifierNamespace namespace   = IdentifierNamespace.get(params.ns.id)
        if (!namespace){
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'identifier.namespace.label'), params.ns.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        if ( ! params.value){
            flash.error = message(code: 'identifier.create.err.missingvalue', args: [namespace.getI10n('name') ?: namespace.ns]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        String value = params.value.trim()
        String note = params.note?.trim()
        Identifier id = Identifier.construct([value: value, reference: org, namespace: namespace])
        id.note = note
        id.save()

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Processes the given customer identifier parameters and creates a new customer identifier record for the given organisation
     * @return the identifier list view, opened on tab customer identifiers
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def processCreateCustomerIdentifier(){
        log.debug("OrganisationController::processCreateCustomerIdentifier ${params}")

        Org org = params.orgid ? Org.get(params.orgid) : null
        if ( ! (org && params.addCIPlatform)){
            flash.error = message(code: 'menu.admin.error') as String
            redirect(url: request.getHeader('referer'))
            return
        }

        Platform plt = Platform.get(params.addCIPlatform)
        if (!plt){
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'default.provider.platform.label'), params.addCIPlatform]) as String
            redirect(url: request.getHeader('referer'))
            return
        }

        if ( ! params.value){
            String p = plt.org.name + (plt.org.sortname ? " (${plt.org.sortname})" : '') + ' : ' + plt.name
            flash.error = message(code: 'org.customerIdentifier.create.err.missingvalue', args: [p]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        if (plt) {
            CustomerIdentifier ci = new CustomerIdentifier(
                    customer: org,
                    platform: plt,
                    value: params.value?.trim(),
                    requestorKey: params.requestorKey?.trim(),
                    note: params.note?.trim(),
                    owner: contextService.getOrg(),
                    isPublic: true,
                    type: RDStore.CUSTOMER_IDENTIFIER_TYPE_DEFAULT
            )
            if(!ci.save())
                log.error("error on inserting customer identifier: ${ci.getErrors().getAllErrors().toListString()}")
        }

        redirect action: 'ids', id: params.orgid, params: [tab: 'customerIdentifiers']
    }

    /**
     * Takes the given parameters and updates the given identifier record.
     * Leitweg-IDs (the identifier necessary for the North-Rhine Westphalia invoicing system) are autogenerated; they
     * can get actual values only by editing. That is why pattern validation is taking place here
     * @return the identifier list view
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def processEditIdentifier(){
        log.debug("OrganisationController::processEditIdentifier ${params}")
        Identifier identifier   = Identifier.get(params.identifierId)
        if ( ! identifier){
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'default.search.identifier'), params.identifierId]) as String
            redirect(url: request.getHeader('referer'))
            return
        }

        if(identifier.ns.ns == IdentifierNamespace.LEIT_ID && params.leitID1 && params.leitID3){
            String leitID1
            String leitID2
            String leitID3

            if(params.leitID1 ==~ /[0-9]{2,12}/) {
                leitID1 = params.leitID1
            }else{
                flash.error = message(code: 'identifier.edit.err.leitID', args: [message(code: 'identifier.leitID.leitID1.info')]) as String
                redirect(url: request.getHeader('referer'))
                return
            }

            if(params.leitID2 ==~ /[a-zA-Z0-9]{0,30}/) {
                leitID2 = params.leitID2
            }else{
                flash.error = message(code: 'identifier.edit.err.leitID', args: [message(code: 'identifier.leitID.leitID2.info')]) as String
                redirect(url: request.getHeader('referer'))
                return
            }

            if(params.leitID3 ==~ /[0-9]{2,2}/) {
                leitID3 = params.leitID3
            }else{
                flash.error = message(code: 'identifier.edit.err.leitID', args: [message(code: 'identifier.leitID.leitID3.info')]) as String
                redirect(url: request.getHeader('referer'))
                return
            }

            params.value = leitID1 + '-' + (leitID2 ? leitID2 + '-' : '') + leitID3
        }

        if(identifier.ns.ns == IdentifierNamespace.PEPPOL_RECEIVER_ID && params.leitID){
            String leitID

            if(params.leitID) {
                leitID = params.leitID
            }else{
                flash.error = message(code: 'identifier.edit.err.peppolID', args: [message(code: 'identifier.leitID.leitID1.info')]) as String
                redirect(url: request.getHeader('referer'))
                return
            }

            params.value = '0204:'+leitID
        }

        if ( ! params.value){
            flash.error = message(code: 'identifier.edit.err.missingvalue', args: [identifier.ns?.getI10n('name') ?: identifier.ns?.ns]) as String
            redirect(url: request.getHeader('referer'))
            return
        }

        identifier.value = params.value.trim()
        identifier.note = params.note?.trim()
        identifier.save()
        identifier.errors.getFieldErrors().each { FieldError fe ->
            flash.error = message(code: 'identifier.edit.err.wrongValue', args: [fe.rejectedValue]) as String
        }

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Takes the given parameters and updates the given customer identifier record.
     * @return the identifier list view, opened on tab customer identifiers
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def processEditCustomerIdentifier(){
        log.debug("OrganisationController::processEditIdentifier ${params}")
        CustomerIdentifier customeridentifier   = CustomerIdentifier.get(params.customeridentifier)
        if ( ! customeridentifier){
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'default.search.identifier'), params.identifierId]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        if ( ! params.value){
            Platform plt = customeridentifier.platform
            String p = plt.org.name + (plt.org.sortname ? " (${plt.org.sortname})" : '') + ' : ' + plt.name
            flash.error = message(code: 'org.customerIdentifier.edit.err.missingvalue', args: [p]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        customeridentifier.value = params.value
        customeridentifier.requestorKey = params.requestorKey?.trim()
        customeridentifier.note = params.note?.trim()
        customeridentifier.save()

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Call to open the customer identifier creation modal; checks which platforms are available for the given organisation
     * @return the customer identifier construction modal
     * @see CustomerIdentifier
     * @see Platform
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = ['FAKE,ORG_INST_BASIC,ORG_CONSORTIUM_BASIC'])
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN( 'FAKE,ORG_INST_BASIC,ORG_CONSORTIUM_BASIC' )
    })
    def createCustomerIdentifier(){
        log.debug("OrganisationController::createCustomerIdentifier ${params}")
        Org org   = Org.get(params.id)

        if (! org) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        boolean isComboRelated = Combo.findByFromOrgAndToOrg(org, contextService.getOrg()) != null
        List<Platform> allPlatforms
        if(isComboRelated)
            allPlatforms = organisationService.getAllPlatformsForContextOrg(org)
        else allPlatforms = organisationService.getAllPlatformsForContextOrg()

        render template: '/templates/customerIdentifier/modal_create', model: [orgInstance: org, allPlatforms: allPlatforms]
    }

    /**
     * Call to open the customer identifier creation modal with prefilled values
     * @return the customer identifier construction modal with prefilled values
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = ['FAKE,ORG_INST_BASIC,ORG_CONSORTIUM_BASIC'])
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN( 'FAKE,ORG_INST_BASIC,ORG_CONSORTIUM_BASIC' )
    })
    def editCustomerIdentifier(){
        log.debug("OrganisationController::editCustomerIdentifier ${params}")
        CustomerIdentifier customeridentifier = CustomerIdentifier.get(params.customeridentifier)
        Org org = customeridentifier?.owner

        if (! customeridentifier) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.customerIdentifier'), params.customeridentifier]) as String
            redirect(url: request.getHeader('referer'))
            return
        }

        render template: '/templates/customerIdentifier/modal_create', model: [orgInstance: org, customeridentifier: customeridentifier]
    }

    /**
     * Call to create a new organisation: either as GET; then, the form to enter the new data is being rendered;
     * or as POST; then, the submitted form values will be processed and a new organisation record set up. The
     * new record is being shown on the organisation details page to which is being redirected after creating
     * the new record
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def create() {
        switch (request.method) {
            case 'POST':
                Org orgInstance = new Org(params)
                //if (params.name) {
                    if (orgInstance.save()) {
                        orgInstance.setDefaultCustomerType()

                        flash.message = message(code: 'default.created.message', args: [message(code: 'org.label'), orgInstance.name]) as String
                        redirect action: 'show', id: orgInstance.id
                        return
                    }
                //}

                render view: 'create', model: [orgInstance: orgInstance]
                break
        }
    }

    /**
     * Call to create a new member with the given parameter map
     * @return the details view of the new member in case of success, the creation page otherwise
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.ORG_CONSORTIUM_BASIC], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def createMember() {
        Map<String,Object> ctrlResult = organisationControllerService.createMember(this,params)
        if(ctrlResult.status == OrganisationControllerService.STATUS_ERROR) {
            redirect action:'findOrganisationMatches', params:params
            return
        }
        else {
            redirect action: 'show', id: ctrlResult.result.orgInstance.id
            return
        }
    }

    /**
     * Call to create a new consortium member; opens a form to check the new name against existing ones in order to exclude duplicates
     * @return the form with eventual name matches
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.ORG_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    Map findOrganisationMatches() {
        Map memberMap = [:]

        Combo.findAllByType(RDStore.COMBO_TYPE_CONSORTIUM).each { lObj ->
            Combo link = (Combo) lObj
            List members = memberMap.get(link.fromOrg.id)
            if(!members) {
                members = [link.toOrg.id]
            } else {
                members << link.toOrg.id
            }
            memberMap.put(link.fromOrg.id,members)
        }

        Map result = [institution:contextService.getOrg(), organisationMatches:[], members:memberMap, comboType:RDStore.COMBO_TYPE_CONSORTIUM]
        //searching members for consortium, i.e. the context org is a consortium
        if (params.proposedOrganisation) {
            result.organisationMatches.addAll(Org.executeQuery(
                    "select o from Org o, OrgSetting os where os.org = o and os.key = :ct and os.roleValue in (:roles) and (lower(o.name) like :searchName or lower(o.sortname) like :searchName or exists(select a from o.altnames a where genfunc_filter_matcher(a.name, :searchName) = true)) ",
                    [ct: OrgSetting.KEYS.CUSTOMER_TYPE, roles: customerTypeService.getOrgInstRoles(), searchName: "%${params.proposedOrganisation.toLowerCase()}%"])
            )
        }
        if (params.proposedOrganisationID) {
            result.organisationMatches.addAll(Org.executeQuery("select id.org from Identifier id where lower(id.value) like :identifier and lower(id.ns.ns) in (:namespaces) ",
                    [identifier: "%${params.proposedOrganisationID.toLowerCase()}%",namespaces:["isil","wibid"]]))
        }

        result
    }

    @UnstableFeature
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404(domain=Org)
    def dataviz() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        Map<String,Object> info = [:]
        String view = ''

        if (! result) {
            response.sendError(401); return
        }

        Org ctxOrg = contextService.getOrg()
        Org org    = result.orgInstance as Org

        if (! org.isInfoAccessible()) {
            response.sendError(401); return
        }
        else if (ctxOrg.isCustomerType_Consortium() && org.isCustomerType_Inst()) {
            info = infoService.getInfo_ConsAtInst(ctxOrg, org)
            view = 'dataviz/consAtInst'
        }
        else if (ctxOrg.isCustomerType_Inst() && ctxOrg == org) {
            info = infoService.getInfo_Inst(ctxOrg)
            view = 'dataviz/inst'
        }

        result.subscriptionMap          = info.subscriptionMap
        result.subscriptionTimelineMap  = info.subscriptionTimelineMap
        result.licenseMap               = info.licenseMap
        result.licenseTimelineMap       = info.licenseTimelineMap
        result.providerMap              = info.providerMap
        result.providerTimelineMap      = info.providerTimelineMap
        result.surveyMap                = info.surveyMap
        result.surveyTimelineMap        = info.surveyTimelineMap
        result.costs                    = info.costs

        render view: view, model: result
    }


    /**
     * Shows the details of the organisation to display
     * @return the details view of the given orgainsation
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404(domain=Org)
    def show() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        if (! result) {
            response.sendError(401)
            return
        }
        result.missing = [:]

        if (result.inContextOrg && contextService.getOrg().eInvoice) {
            Identifier leitID = contextService.getOrg().getLeitID()

            if (!contextService.getOrg().eInvoicePortal) {
                result.missing.eInvoicePortal = message(code: 'org.eInvoice.info.missing.eInvoicePortal')
            }
            if(!leitID || (leitID && (leitID.value == '' || leitID.value == null)))
                result.missing.leitID = message(code: 'org.eInvoice.info.missing.leitID')
        }

        result.tasks = taskService.getTasksByResponsibilityAndObject(result.user, result.orgInstance)

        result.formalOrg = result.user.formalOrg as Org

        // create mandatory OrgPrivateProperties if not existing

        List<PropertyDefinition> mandatories = PropertyDefinition.getAllByDescrAndMandatoryAndTenant(PropertyDefinition.ORG_PROP, true, contextService.getOrg())

        mandatories.each { PropertyDefinition pd ->
            if (!OrgProperty.findWhere(owner: result.orgInstance, type: pd)) {
                def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, result.orgInstance, pd, contextService.getOrg())


                if (newProp.hasErrors()) {
                    log.error(newProp.errors.toString())
                } else {
                    log.debug("New org private property created via mandatory: " + newProp.type.name)
                }
            }
        }

        result.orgInstance.createCoreIdentifiersIfNotExist()

        if (result.orgInstance.createdBy) {
			result.createdByOrgGeneralContacts = PersonRole.executeQuery(
					"select distinct(prs) from PersonRole pr join pr.prs prs join pr.org oo " +
							"where oo = :org and pr.functionType = :ft and prs.isPublic = true",
					[org: result.orgInstance.createdBy, ft: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]
			)
        }
		if (result.orgInstance.legallyObligedBy) {
			result.legallyObligedByOrgGeneralContacts = PersonRole.executeQuery(
					"select distinct(prs) from PersonRole pr join pr.prs prs join pr.org oo " +
							"where oo = :org and pr.functionType = :ft and prs.isPublic = true",
					[org: result.orgInstance.legallyObligedBy, ft: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]
			)
		}

        workflowService.executeCmdAndUpdateResult(result, params)
        result
    }

    /**
     * Displays either the identifiers or the customer identifiers attached to the organisation.
     * Only institutions may have customer identifiers; commercial organisations cannot have such
     * @return the table view of the records, either the identifiers or the customer number key pairs
     * @see Identifier
     * @see CustomerIdentifier
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404(domain=Org)
    def ids() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        if(!result) {
            response.sendError(401)
            return
        }
        result.editable_identifier = !result.orgInstance.gokbId && result.editable

        boolean userIsAdmin = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        //this is a flag to check whether the page has been called directly after creation
        result.fromCreate = params.fromCreate ? true : false
        if(!params.tab)
            params.tab = 'identifier'

        if (contextService.getOrg().isCustomerType_Consortium()) {
            List<Long> consortia = Combo.executeQuery('select c.id from Combo c where c.type = :type and c.fromOrg = :target and c.toOrg = :context',
                    [type:RDStore.COMBO_TYPE_CONSORTIUM, target:result.orgInstance, context:contextService.getOrg()])
            if (consortia.size() == 1 && contextService.isInstEditor())
                result.editable_identifier = true
        }
        else
            result.editable_identifier = userIsAdmin || userService.hasFormalAffiliation(result.orgInstance, 'INST_EDITOR')

        result.orgInstance.createCoreIdentifiersIfNotExist()

        Boolean inContextOrg = result.inContextOrg
        Boolean isComboRelated = Combo.findByFromOrgAndToOrg(result.orgInstance, contextService.getOrg())
        result.isComboRelated = isComboRelated

        result.hasAccessToCustomeridentifier = (userIsAdmin || (inContextOrg && contextService.isInstUser()) || (isComboRelated && contextService.isInstUser()))
                 && OrgSetting.get(result.orgInstance, OrgSetting.KEYS.CUSTOMER_TYPE) != OrgSetting.SETTING_NOT_FOUND

        // TODO: erms-5495

        if (result.hasAccessToCustomeridentifier) {

            result.editable_customeridentifier = userIsAdmin || (inContextOrg && contextService.isInstEditor()) || (isComboRelated && contextService.isInstEditor())

            // adding default settings
            organisationService.initMandatorySettings(result.orgInstance)
            if(params.tab == 'customerIdentifiers') {
                if(isComboRelated)
                    result.allPlatforms = organisationService.getAllPlatformsForContextOrg(result.orgInstance)
                else result.allPlatforms = organisationService.getAllPlatformsForContextOrg()
                Map<String, Object> queryParams = [customer: result.orgInstance]
                String validSubsQuery = "select oo.sub from OrgRole oo where oo.org = :customer", consortiaRelationFilter = "and exists(select ooo from OrgRole ooo where ooo.sub = oo.sub and ooo.org = :context)"
                Set<Subscription> validSubs
                if(result.institution.isCustomerType_Consortium()) {
                    validSubsQuery = "${validSubsQuery} ${consortiaRelationFilter}"
                    validSubs = Subscription.executeQuery(validSubsQuery, queryParams+[context: contextService.getOrg()])
                }
                else
                    validSubs = Subscription.executeQuery(validSubsQuery, queryParams)
                if(validSubs) {
                    String query = "select ci from CustomerIdentifier ci join ci.platform platform where ci.customer = :customer and platform in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription in (:validSubs))"
                    queryParams.validSubs = validSubs
                    if(params.customerIdentifier) {
                        query += " and ci.value like (:customerIdentifier)"
                        queryParams.customerIdentifier = "%${params.customerIdentifier.toLowerCase()}%"
                    }
                    if(params.requestorKey) {
                        query += " and ci.requestorKey like (:requestorKey)"
                        queryParams.requestorKey = "%${params.requestorKey.toLowerCase()}%"
                    }
                    if(params.ciPlatform) {
                        query += " and platform.id = :platform"
                        queryParams.platform = params.long("ciPlatform")
                    }
                    else {
                        query += " and platform in (:wekbPlatforms)"
                        queryParams.wekbPlatforms = result.allPlatforms
                    }
                    String sort = " order by platform.provider.name asc"
                    if(params.sort) {
                        sort = " order by ${params.sort} ${params.order}"
                    }
                    if (userIsAdmin) {
                        result.customerIdentifier = CustomerIdentifier.executeQuery(query+sort, queryParams)
                    }
                    else if (inContextOrg) {
                        if (contextService.getOrg().isCustomerType_Consortium()) {
                            result.customerIdentifier = CustomerIdentifier.executeQuery(query + sort, queryParams)
                        } else if (contextService.getOrg().isCustomerType_Inst()) {
                            result.customerIdentifier = CustomerIdentifier.executeQuery(query + sort, queryParams)
                        }
                    }
                    else if (isComboRelated) {
                        log.debug('settings for combo related org: consortia')
                        result.customerIdentifier = CustomerIdentifier.executeQuery(query+sort, queryParams)
                    }
                }
            }
        }
        result
    }

    /**
     * Shows the tasks attached to the given organisation. Displayed here are tasks which
     * are related to the given organisation (i.e. which have the given organisation as target)
     * and not such assigned to the given one! If the target organisation is an institution, users
     * affiliated to it do not know about those tasks since they are internal to that institution which
     * created them
     * @return the task table view
     * @see Task
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_PRO)
    })
    @Check404(domain=Org)
    def tasks() {
        Map<String,Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        if (!result) {
            response.sendError(401); return
        }
        SwissKnife.setPaginationParams(result, params, result.user as User)
        result.cmbTaskInstanceList = taskService.getTasks((User) result.user, (Org) result.orgInstance)['cmbTaskInstanceList']

        result
    }

    /**
     * Call to open the workflows related to the given organisation. If submitted, the workflow is being updated
     * @return the workflow checklist view
     * @see de.laser.workflow.WfChecklist
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_PRO)
    })
    @Check404()
    def workflows() {
        Map<String,Object> ctrlResult = organisationControllerService.workflows( this, params )
        render view: 'workflows', model: ctrlResult.result
    }

    /**
     * Shows the documents attached to the given organisation. Important:
     * Displayed here are those documents which have been *attached* to the given object.
     * If the target is an institution, the institution does not know about the documents unless they have been
     * shared with the institution!
     * @return the document table view
     * @see Doc
     * @see DocContext
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    @Check404(domain=Org)
    def documents() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
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
     * Opens the notes view for the given organisation. Beware that those notes are being shown
     * which are attached to the given organisation; the target (if it is an institution) does not
     * see the notes!
     * @return a {@link List} of notes ({@link Doc})
     * @see Doc
     * @see DocContext
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404(domain=Org)
    def notes() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        if(!result) {
            response.sendError(401)
            return
        }
        result
    }

    /**
     * Call to delete the given customer identifier
     * @return the customer identifier table view
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = ['FAKE,ORG_INST_BASIC,ORG_CONSORTIUM_BASIC'], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN( 'FAKE,ORG_INST_BASIC,ORG_CONSORTIUM_BASIC' )
    })
    def deleteCustomerIdentifier() {
        Map<String,Object> ctrlResult = organisationControllerService.deleteCustomerIdentifier(this,params)
        if(ctrlResult.status == OrganisationControllerService.STATUS_ERROR)
            flash.error = ctrlResult.result.error
        redirect action: 'ids', id: params.id, params: [tab: 'customerIdentifiers']
    }

    /**
     * Call to delete the given identifier
     * @return the identifier table view
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def deleteIdentifier() {
        identifierService.deleteIdentifier(params.owner,params.target)
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Shows all user accounts affiliated to (at least) the given institution
     * @return renders the user list template with the users affiliated to this institution
     * @see User
     */
    @DebugInfo(isInstAdm_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN()
    })
    @Check404(domain=Org)
    def users() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        result.editable = _checkIsEditable(result.orgInstance)

        if (! result.editable) {
            boolean instAdminExists = (result.orgInstance as Org).hasInstAdminEnabled()
            boolean comboCheck = userService.hasComboInstAdmPivileges(result.orgInstance as Org)

            result.editable = comboCheck && ! instAdminExists
        }

        if (! result.editable) {
            redirect controller: 'organisation', action: 'show', id: result.orgInstance.id
            return
        }

        GrailsParameterMap filterParams = params.clone() as GrailsParameterMap
        filterParams.org = (result.orgInstance as Org).id

        Map userData = userService.getUserMap(filterParams)
        result.total = userData.count
        result.users = userData.data
        result.titleMessage = "${result.orgInstance.name} - ${message(code:'org.nav.users')}"
        result.inContextOrg = false
        result.multipleAffiliationsWarning = true
        Set<Org> availableComboOrgs = Org.executeQuery(
                'select c.fromOrg from Combo c where c.toOrg = :ctxOrg and c.type = :type order by c.fromOrg.name asc',
                [ctxOrg: result.orgInstance, type: RDStore.COMBO_TYPE_CONSORTIUM]
        )
        availableComboOrgs.add(result.orgInstance)

        result.navConfig = [
                orgInstance: result.orgInstance, inContextOrg: result.inContextOrg
        ]
        result.filterConfig = [
                filterableRoles:Role.findAllByRoleType('user'), orgField: false
        ]
        result.tmplConfig = [
                editable: result.editable,
                editLink: 'editUser',
                deleteLink: 'deleteUser',
                users: result.users,
                showAllAffiliations: false,
                availableComboOrgs: availableComboOrgs
        ]
        render view: '/user/global/list', model: result
    }

    /**
     * Call to detach the given user from the given institution.
     * Data the given user may have authored will be reassigned to another user
     * @return the user deletion view where eventual conflicts are being listed
     */
    @DebugInfo(isInstAdm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def deleteUser() {
        Map<String, Object> result = userControllerService.getResultGenericsERMS3067(params)
        result.orgInstance = Org.get(params.id) // overwrite

        if (! result.editable) {
            redirect controller: 'organisation', action: 'users', params: params
            return
        }

        if (result.user) {
            if (result.user.formalOrg && ! result.editable) {
                flash.error = message(code: 'user.delete.error.foreignOrg') as String
                redirect action: 'editUser', params: [uoid: params.uoid, id: params.id]
                return
            }

            if (params.process && result.editable) {
                User userReplacement = User.get(params.userReplacement)

                result.delResult = deletionService.deleteUser(result.user as User, userReplacement, false)
            }
            else {
                result.delResult = deletionService.deleteUser(result.user as User, null, DeletionService.DRY_RUN)
            }

            result.substituteList = User.executeQuery(
                    'select distinct u from User u where u.formalOrg = :ctxOrg and u != :self',
                    [ctxOrg: result.orgInstance, self: result.user]
            )
        }

        render view: '/user/global/delete', model: result
    }

    /**
     * Call to edit the given user profile
     * @return the profile editing template
     */
    @DebugInfo(isInstAdm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def editUser() {
        Map result = [
                user: genericOIDService.resolveOID(params.uoid),
                orgInstance: Org.get(params.id),
                manipulateAffiliations: SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
        ]

        result.editable = result.manipulateAffiliations || userService.isUserEditableForInstAdm(result.user) // TODO: ERMS-6044
        result.availableOrgs = [ result.orgInstance ]

        render view: '/user/global/edit', model: result
    }

    /**
     * Call to create a new user profile
     * @return the profile creation template
     */
    @DebugInfo(isInstAdm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def createUser() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        result.availableOrgs = Org.get(params.id)

        render view: '/user/global/create', model: result
    }

    /**
     * Takes the submitted parameters and creates a new user record with the given parameters
     * @return the user editing template in case of success, redirects back to the creation page otherwise
     */
    @DebugInfo(isInstAdm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def processCreateUser() {
        def success = userService.addNewUser(params, flash)
        //despite IntelliJ's warnings, success may be an array other than the boolean true
        if(success instanceof User) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'user.label'), success.id]) as String
            redirect action: 'editUser', params: [uoid: genericOIDService.getOID(success), id: params.id]
            return
        }
        else if(success instanceof List) {
            flash.error = success.join('<br>')
            redirect action: 'createUser'
            return
        }
    }

    /**
     * Attaches the given user to the given institution
     * @return the user editing profile with the updated data
     */
    @DebugInfo(isInstAdm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def setAffiliation() {
        Map<String, Object> result = userControllerService.getResultGenericsERMS3067(params)
        result.orgInstance = Org.get(params.id) // overwrite

        if (! result.editable) {
            flash.error = message(code: 'default.noPermissions') as String
            redirect action: 'editUser', params: [id: params.id, uoid: params.uoid]
            return
        }

        userService.setAffiliation(result.user as User, params.org, params.formalRole, flash)
        redirect action: 'editUser', params: [id: params.id, uoid: params.uoid]
    }

    /**
     * Call to delete the given organisation, offering substitution candidates
     * @return the deletion view
     */
    @Secured(['ROLE_ADMIN'])
    def delete() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        if (result.orgInstance) {
            if (params.process  && result.editable) {
                result.delResult = deletionService.deleteOrganisation(result.orgInstance, null, false)
            }
            else {
                result.delResult = deletionService.deleteOrganisation(result.orgInstance, null, DeletionService.DRY_RUN)
            }

            if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
                result.substituteList = Org.executeQuery('select distinct o from Org o where o.archiveDate is null')
            }
            else {
                List<Org> orgList = [result.orgInstance]
                orgList.addAll(Org.executeQuery("select o from Combo cmb join cmb.fromOrg o where o.archiveDate is null and cmb.toOrg = :org", [org: result.orgInstance]))
                orgList.addAll(Org.executeQuery("select o from Combo cmb join cmb.toOrg o where o.archiveDate is null and cmb.fromOrg = :org", [org: result.orgInstance]))
                orgList.unique()

                result.substituteList = orgList
            }
        }

        render view: 'delete', model: result
    }

    @Secured(['ROLE_ADMIN'])
    def disableAllUsers() {
        List disabledAccounts = []

        Org org = Org.get(params.id)
        if (org) {
            User.executeQuery('select u from User u where u.formalOrg = :org and u.enabled = true', [org: org]).each { User usr ->
                usr.enabled = false
                usr.save()
                disabledAccounts.add([usr.id, usr.username])
            }
            SystemEvent.createEvent('UA_FLAG_DISABLED', [org: [org.id, org.name], disabled: disabledAccounts])
        }
        redirect action:'users', id:params.id, params:[disabledAccounts: disabledAccounts]
    }

    @Secured(['ROLE_ADMIN'])
    def markAsArchive() {
        Org org = Org.get(params.id)
        if (org) {
            Set<User> activeAffiliatedUsers = User.findAllByFormalOrgAndEnabled(org, true)
            if (org.isArchived()) {
                flash.error = "Die Einrichtung wurde bereits archiviert."
            }
            else if(activeAffiliatedUsers) {
                flash.error = "Es gibt noch aktive Nutzerkennungen. Bitte vorher alle Accounts deaktivieren."
            }
            else {
                Set<User> affiliatedUsers = User.findAllByFormalOrg(org)
                affiliatedUsers.each { User usr ->
                    deletionService.deleteUser(usr, null, false)
                }
                org.archiveDate = new Date()
                org.save()
                flash.message = "Die Einrichtung wurde archiviert."
            }
        }
        redirect action:'show', id:params.id
    }

    /**
     * Call to list the public contacts of the given organisation
     * @return a table view of public contacts
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    @Check404(domain=Org)
    def addressbook() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        if(!result) {
            response.sendError(401)
            return
        }

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.org = result.orgInstance
        params.sort = params.sort ?: 'p.last_name, p.first_name'
        params.tab = params.tab ?: 'contacts'

        EhcacheWrapper cache = contextService.getUserCache("/org/addressbook/${params.id}")
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
                if(p.roleLinks.org[0]) {
                    oid = genericOIDService.getOID(p.roleLinks.org[0])
                }
                else if(p.roleLinks.vendor[0]) {
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
     * Lists the current reader numbers of the given institution
     * @return a table view of the reader numbers, grouped by semesters on the one hand, due dates on the other
     * @see ReaderNumber
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404(domain=Org)
    def readerNumber() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        if(!result) {
            response.sendError(401)
            return
        }

        if(params.tableA) {
            params.sortA = params.sort
            params.orderA = params.order
        }
        else {
            params.sortA = 'semester'
            params.orderA = 'desc'
        }

        if(params.tableB) {
            params.sortB = params.sort
            params.orderB = params.order
        }
        else {
            params.sortB = 'dueDate'
            params.orderB = 'desc'
        }

        Map<String,Map<String,ReaderNumber>> numbersWithSemester = organisationService.groupReaderNumbersByProperty(ReaderNumber.findAllByOrgAndSemesterIsNotNull((Org) result.orgInstance,[sort:params.sortA,order:params.orderA]),"semester")
        Map<String,Map<String,ReaderNumber>> numbersWithYear = organisationService.groupReaderNumbersByProperty(ReaderNumber.findAllByOrgAndYearIsNotNull((Org) result.orgInstance,[sort:params.sortB,order:params.orderB]),"year")

        TreeSet<String> semesterCols = [], yearCols = []
        Map<String,BigDecimal> yearSums = [:]
        Map<String,Map<String,BigDecimal>> semesterSums = [:]
        numbersWithSemester.each { Map.Entry<String,Map<String,ReaderNumber>> semesters ->
            semesters.value.each { Map.Entry<String,ReaderNumber> row ->
                semesterCols << row.key
                ReaderNumber rn = row.value
                Map<String,BigDecimal> semesterSumRow = semesterSums.get(semesters.key)
                if(!semesterSumRow)
                    semesterSumRow = [:]
                if(rn.value) {
                    BigDecimal groupSum = semesterSumRow.get(rn.referenceGroup.getI10n("value"))
                    if(groupSum == null) {
                        groupSum = rn.value
                    }
                    else groupSum += rn.value
                    semesterSumRow.put(rn.referenceGroup.getI10n("value"),groupSum)
                }
                semesterSums.put(semesters.key,semesterSumRow)
            }
        }
        numbersWithYear.each { Map.Entry<String,Map<String,ReaderNumber>> years ->

            years.value.eachWithIndex { Map.Entry<String,ReaderNumber> row, int i ->
                yearCols << row.key
                ReaderNumber rn = row.value
                log.debug("at index ${i}: ${rn.referenceGroup.getI10n('value')}")
                BigDecimal yearSum = yearSums.get(years.key)
                if(rn.value) {
                    if((years.value.keySet().contains(RDStore.READER_NUMBER_FTE_TOTAL.getI10n('value')) && row.key != RDStore.READER_NUMBER_FTE.getI10n('value')) ||
                            !years.value.keySet().contains(RDStore.READER_NUMBER_FTE_TOTAL.getI10n('value'))) {
                        if(yearSum == null) {
                            yearSum = rn.value
                        }
                        else {
                            yearSum += rn.value
                            log.debug("${yearSum}")
                        }
                    }
                }
                yearSums.put(years.key,yearSum)
            }
        }

        result.numbersWithSemester = numbersWithSemester
        result.numbersWithYear = numbersWithYear
        result.semesterCols = semesterCols
        result.semesterSums = semesterSums
        result.yearCols = yearCols
        result.yearSums = yearSums

        result
    }

    /**
     * Lists the access point configurations of the given institution
     * @return a list view of access points
     * @see de.laser.oap.OrgAccessPoint
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404(domain=Org)
    def accessPoints() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        if(!result) {
            response.sendError(401)
            return
        }

        List orgAccessPointList = accessPointService.getOapListWithLinkCounts(result.orgInstance)
        result.orgAccessPointList = orgAccessPointList.groupBy {it.oap.accessMethod.value}.sort {it.key}

        result.activeTab = params.activeTab ?: 'ip'

        if (params.exportXLSX) {

            SXSSFWorkbook wb
            SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
            String datetoday = sdf.format(new Date())
            String filename = "${datetoday}_" + g.message(code: "org.accessPoints.export")
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb = (SXSSFWorkbook) accessPointService.exportAccessPoints(orgAccessPointList.collect {it.oap}, ExportClickMeService.FORMAT.XLS)
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }else {
            result
        }
    }

    /**
     * Assigns the given subject group to the given organisation
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def addSubjectGroup() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        if (!result.orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        RefdataValue newSubjectGroup = RefdataValue.get(params.subjectGroup)
        if (!newSubjectGroup) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.subjectGroup.label'), params.subjectGroup]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        if (result.orgInstance.getSubjectGroup().find { it.subjectGroupId == newSubjectGroup.id }) {
            flash.message = message(code: 'default.err.alreadyExist', args: [message(code: 'org.subjectGroup.label')]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        result.editable = _checkIsEditable(result.orgInstance)

        if (result.editable) {
            result.orgInstance.addToSubjectGroup(subjectGroup: RefdataValue.get(params.subjectGroup))
            result.orgInstance.save()
//            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label'), orgInstance.name])
        }

        redirect action: 'show', id: params.id
    }

    /**
     * Assigns the given discovery system to the given organisation
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def addDiscoverySystem() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        if (!result.orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        result.editable = _checkIsEditable(result.orgInstance)
        if (result.editable) {
            if(params.containsKey('frontend')) {
                RefdataValue newFrontend = RefdataValue.get(params.frontend)
                if (!newFrontend) {
                    flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.discoverySystems.frontend.label'), params.frontend]) as String
                    redirect(url: request.getHeader('referer'))
                    return
                }
                if (result.orgInstance.getDiscoverySystemFrontends().find { it.frontendId == newFrontend.id }) {
                    flash.message = message(code: 'default.err.alreadyExist', args: [message(code: 'org.discoverySystems.frontend.label')]) as String
                    redirect(url: request.getHeader('referer'))
                    return
                }
                result.orgInstance.addToDiscoverySystemFrontends(frontend: newFrontend)
            }
            else if(params.containsKey('index')) {
                RefdataValue newIndex = RefdataValue.get(params.index)
                if (!newIndex) {
                    flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.discoverySystems.index.label'), params.index]) as String
                    redirect(url: request.getHeader('referer'))
                    return
                }
                if (result.orgInstance.getDiscoverySystemIndices().find { it.indexId == newIndex.id }) {
                    flash.message = message(code: 'default.err.alreadyExist', args: [message(code: 'org.discoverySystems.index.label')]) as String
                    redirect(url: request.getHeader('referer'))
                    return
                }
                result.orgInstance.addToDiscoverySystemIndices(index: newIndex)
            }

            result.orgInstance.save()
//            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label'), orgInstance.name])
        }

        redirect action: 'show', id: params.id
    }

    /**
     * Removes the given subject group from the given organisation
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def deleteSubjectGroup() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        if (!result.orgInstance) {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        if (result.editable) {
            OrgSubjectGroup osg = OrgSubjectGroup.get(params.removeOrgSubjectGroup)
            result.orgInstance.removeFromSubjectGroup(osg)
            result.orgInstance.save()
            osg.delete()
//            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label'), orgInstance.name])
        }

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Removes the given discovery system from the given organisation
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def deleteDiscoverySystem() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        if (!result.orgInstance) {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        if (result.editable) {
            if (params.frontend) {
                DiscoverySystemFrontend ds = DiscoverySystemFrontend.get(params.long('frontend'))
                if (ds) {
                    result.orgInstance.removeFromDiscoverySystemFrontends(ds)
                    result.orgInstance.save()
                    ds.delete()
                }
            }
            else if (params.index) {
                DiscoverySystemIndex ds = DiscoverySystemIndex.get(params.long('index'))
                if (ds) {
                    result.orgInstance.removeFromDiscoverySystemIndices(ds)
                    result.orgInstance.save()
                    ds.delete()
                }
            }
//            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label'), orgInstance.name])
        }

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Call to toggle the consortium membership state between the given institution and the consortium
     * (adds or removes a combo link between the institution and the consortium)
     * @see Combo
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_BASIC], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def toggleCombo() {
        Map<String,Object> ctrlResult = organisationControllerService.toggleCombo(this,params)
        if(ctrlResult.status == OrganisationControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
            }
        }
        else {
            flash.message = ctrlResult.result.message
        }
        redirect action: 'listInstitution'
    }

    /**
     * Call to list the contacts the context institution has attached to the given organisation
     * @return a table view of the contacts
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404(domain=Org)
    def contacts() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        result.rdvAllPersonFunctions = [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, RDStore.PRS_FUNC_CONTACT_PRS, RDStore.PRS_FUNC_INVOICING_CONTACT, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_SURVEY_CONTACT]
        result.rdvAllPersonPositions = PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION) - [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS]
        result.tmplShowConfig = ['lineNumber', 'name', 'showContacts', 'function', 'position']

        if(!result.orgInstance.isCustomerType_Consortium() && !result.orgInstance.isCustomerType_Support())
            result.tmplShowConfig << 'preferredForSurvey'

        if ((contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) && result.orgInstance) {
            params.org = result.orgInstance
            result.rdvAllPersonFunctions << RDStore.PRS_FUNC_GASCO_CONTACT
        } else {
            params.org = contextService.getOrg()
        }

        params.sort = params.sort ?: 'p.last_name, p.first_name'
        Map adrParams = [:]
        adrParams.putAll(params)
        if(params.sort.contains('p.'))
            adrParams.remove('sort')

        List visiblePersons = addressbookService.getVisiblePersons("contacts", params)
        result.num_visiblePersons = visiblePersons.size()
        result.visiblePersons = visiblePersons.drop(result.offset).take(result.max)

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
                if(p.roleLinks.org[0]) {
                    oid = genericOIDService.getOID(p.roleLinks.org[0])
                }
                else if(p.roleLinks.vendor[0]) {
                    oid = genericOIDService.getOID(p.roleLinks.vendor[0])
                }
                else if(p.roleLinks.provider[0]) {
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

        params.tab = params.tab ?: 'contacts'

        result.addresses = addressbookService.getVisibleAddresses("contacts", adrParams)

        result
    }

    /**
     * Shows the details of the organisation to display
     * @return the details view of the given orgainsation
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404(domain=Org)
    def mailInfos() {
        Map<String, Object> result = organisationControllerService.mailInfos(params)
        if (! result) {
            response.sendError(401)
            return
        }

        result
    }


    /**
     * Helper method to determine the edit rights the current user has for the given organisation in the given view
     * @param org the target organisation
     * @return true if edit rights are granted to the given user/org/view context, false otherwise
     */
    private boolean _checkIsEditable(Org org) {
        boolean isEditable = false

        boolean inContextOrg          = org.id == contextService.getOrg().id
        boolean userIsYoda            = contextService.getUser().isYoda()
        boolean userIsAdmin           = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
        boolean userHasEditableRights = userIsAdmin || contextService.isInstEditor()

        switch(params.action){
            case 'delete':
                isEditable = userIsAdmin
                break
            case 'properties':
                isEditable = userIsAdmin || userService.hasFormalAffiliation(Org.get(params.id), 'INST_EDITOR')
                break
            case 'users':
                isEditable = userIsAdmin || userService.hasFormalAffiliation(Org.get(params.id), 'INST_ADM')
                break
            case 'contacts':
                if (inContextOrg) {
                    isEditable = userHasEditableRights
                }
                break
            case [ 'show', 'ids', 'addSubjectGroup', 'deleteSubjectGroup', 'addDiscoverySystem', 'deleteDiscoverySystem', 'readerNumber', 'accessPoints', 'addressbook' ]:
                if (inContextOrg || params.action == 'addressbook') {
                    isEditable = userHasEditableRights
                }
                else {
                    switch (contextService.getOrg().getCustomerType()){
                        case [ CustomerTypeService.ORG_INST_BASIC, CustomerTypeService.ORG_INST_PRO ] :
                            switch (org.getCustomerType()){
                                case CustomerTypeService.ORG_INST_BASIC:        isEditable = userIsYoda; break
                                case CustomerTypeService.ORG_INST_PRO:          isEditable = userIsYoda; break
                                case CustomerTypeService.ORG_CONSORTIUM_BASIC:  isEditable = userIsYoda; break
                                case CustomerTypeService.ORG_CONSORTIUM_PRO:    isEditable = userIsYoda; break
                                case CustomerTypeService.ORG_SUPPORT:           isEditable = userIsYoda; break
                            }
                            break
                        case [ CustomerTypeService.ORG_CONSORTIUM_BASIC, CustomerTypeService.ORG_CONSORTIUM_PRO, CustomerTypeService.ORG_SUPPORT ] :
                            switch (org.getCustomerType()){
                                case CustomerTypeService.ORG_INST_BASIC:        isEditable = userHasEditableRights; break
                                case CustomerTypeService.ORG_INST_PRO:          isEditable = userHasEditableRights; break
                                case CustomerTypeService.ORG_CONSORTIUM_BASIC:  isEditable = userIsYoda; break
                                case CustomerTypeService.ORG_CONSORTIUM_PRO:    isEditable = userIsYoda; break
                                case CustomerTypeService.ORG_SUPPORT:           isEditable = userIsYoda; break
                            }
                            break
                    }
                }
                break
            default:
                isEditable = userIsAdmin || userService.hasFormalAffiliation(org,'INST_EDITOR')
        }
        // println '>>> isEditable: ' + isEditable + ' >>> ' + params.action
        isEditable
    }
}
