package de.laser

import de.laser.annotations.DebugAnnotation
import de.laser.ctrl.OrganisationControllerService
import de.laser.ctrl.UserControllerService
import de.laser.properties.OrgProperty
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.properties.PropertyDefinition
 
import de.laser.helper.*
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.springframework.context.i18n.LocaleContextHolder

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

    def accessPointService
    def accessService
    def addressbookService
    def contextService
    def deletionService
    def docstoreService
    def instAdmService
    def filterService
    def genericOIDService
    def organisationService
    def propertyService
    def userService
    IdentifierService identifierService
    OrganisationControllerService organisationControllerService
    TaskService taskService
    UserControllerService userControllerService
    ExportClickMeService exportClickMeService

    /**
     * Redirects to {@link #list()}
     * @return the list view of organisations
     */
    @Secured(['ROLE_ORG_EDITOR','ROLE_ADMIN'])
    def index() {
        redirect action: 'list', params: params
    }

    /**
     * Manages calls to the general organisation / institution settings page. The view is parametrised, thus different
     * returns are possible:
     * @return one of:
     * <ul>
     *     <li>general: general settings such as GASCO display, customer type or properties</li>
     *     <li>api: API usage related settings such API level, key and password</li>
     *     <li>natstat: permissions to the Nationaler Statistikserver harvest access</li>
     *     <li>oamonitor: permissions to the Open Access harvest access</li>
     * </ul>
     */
    @DebugAnnotation(perm="FAKE,ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_ADM", specRole="ROLE_ADMIN,ROLE_ORG_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("FAKE,ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_ADM", "ROLE_ADMIN,ROLE_ORG_EDITOR")
    })
    def settings() {
        Map<String,Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        if (! result.orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect action: 'list'
            return
        }
        if(!params.containsKey("tab"))
            params.tab = "general"
        Boolean isComboRelated = Combo.findByFromOrgAndToOrg(result.orgInstance, result.institution)
        result.isComboRelated = isComboRelated
        result.contextOrg = result.institution //for the properties template

        Boolean hasAccess = (result.inContextOrg && accessService.checkMinUserOrgRole(result.user, result.orgInstance, 'INST_ADM')) ||
                (isComboRelated && accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')) ||
                SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')

        // forbidden access
        if (! hasAccess) {
            redirect controller: 'organisation', action: 'show', id: org.id
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
        List<OrgSetting.KEYS> generalSet = [
                OrgSetting.KEYS.CUSTOMER_TYPE,
                OrgSetting.KEYS.GASCO_ENTRY
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

        result.settings = []

        switch(params.tab) {
            case 'general': result.settings.addAll(allSettings.findAll { OrgSetting os -> os.key in generalSet })
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
        /* kept for case of reference, permission check should be handed over to tab display
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')) {
            result.settings.addAll(allSettings.findAll { it.key in ownerSet })
            result.settings.addAll(allSettings.findAll { it.key in accessSet })
            result.settings.addAll(allSettings.findAll { it.key in credentialsSet })
        }
        else if (result.inContextOrg) {
            log.debug( 'settings for own org')
            result.settings.addAll(allSettings.findAll { it.key in ownerSet })

            if (result.institution.hasPerm('ORG_CONSORTIUM,ORG_INST')) {
                result.settings.addAll(allSettings.findAll { it.key in accessSet })
                result.settings.addAll(allSettings.findAll { it.key in credentialsSet })
            }
            else if (['ORG_BASIC_MEMBER'].contains(result.institution.getCustomerType())) {
                result.settings.addAll(allSettings.findAll { it.key == OrgSetting.KEYS.NATSTAT_SERVER_ACCESS })
            }
            else if (['FAKE'].contains(result.institution.getCustomerType())) {
                result.settings.addAll(allSettings.findAll { it.key == OrgSetting.KEYS.NATSTAT_SERVER_ACCESS })
            }
        }
        */

//        result.allPlatforms = Platform.executeQuery('select p from Platform p join p.org o where p.org is not null order by o.name, o.sortname, p.name')
        result
    }

    /**
     * Call to list all institutions and commercial organisations. The list may be rendered as HTML list or exported as Excel worksheet or CSV file
     * @return the list of organisations, either as HTML page or as export
     */
    @Secured(['ROLE_ORG_EDITOR','ROLE_ADMIN'])
    def list() {

        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.sort = params.sort ?: " LOWER(o.shortname), LOWER(o.name)"

        result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')

        def fsq = filterService.getOrgQuery(params)
        result.filterSet = params.filterSet ? true : false

        List orgListTotal  = Org.findAll(fsq.query, fsq.queryParams)
        result.orgListTotal = orgListTotal.size()
        result.orgList = orgListTotal.drop((int) result.offset).take((int) result.max)

        SimpleDateFormat sdf = DateUtils.getSDF_NoTimeNoPoint()
        String datetoday = sdf.format(new Date())
        def message = message(code: 'export.all.orgs')
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

            }
            catch (Exception e) {
                log.error("Problem",e);
                response.sendError(500)
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
    @DebugAnnotation(perm="ORG_CONSORTIUM", type="Consortium", affil="INST_USER", specRole="ROLE_ADMIN, ROLE_ORG_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermTypeAffiliationX("ORG_CONSORTIUM", "Consortium", "INST_USER", "ROLE_ADMIN, ROLE_ORG_EDITOR")
    })
    Map listInstitution() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        params.orgType   = RDStore.OT_INSTITUTION.id.toString()
        params.orgSector = RDStore.O_SECTOR_HIGHER_EDU.id.toString()
        if(!params.sort)
            params.sort = " LOWER(o.sortname)"
        def fsq = filterService.getOrgQuery(params)
        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeAsInteger()
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0
        List<Org> availableOrgs = Org.executeQuery(fsq.query, fsq.queryParams, [sort:params.sort])
        result.consortiaMemberIds = Combo.executeQuery('select cmb.fromOrg.id from Combo cmb where cmb.toOrg = :toOrg and cmb.type = :type',[toOrg: result.institution, type: RDStore.COMBO_TYPE_CONSORTIUM])
        result.consortiaMemberTotal = availableOrgs.size()


        def message = g.message(code: 'menu.institutions')
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        String datetoday = sdf.format(new Date(System.currentTimeMillis()))
        String filename = message+"_${datetoday}"
        if(params.exportClickMeExcel) {
            if (params.filename) {
                filename =params.filename
            }

            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            Map<String, Object> selectedFields = [:]
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }

            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportOrgs(availableOrgs, selectedFields, 'institution')

            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return //IntelliJ cannot know that the return prevents an obsolete redirect
        }
        result.availableOrgs = availableOrgs.drop(result.offset).take(result.max)
        result
    }

    /**
     * Inverse of listInstitution: lists for single users and basic members the consortia
     * @return a list of consortia institutions
     */
    @Secured(['ROLE_USER'])
    Map listConsortia() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        params.customerType   = Role.findByAuthority('ORG_CONSORTIUM').id.toString()
        if(!params.sort)
            params.sort = " LOWER(o.sortname)"
        def fsq = filterService.getOrgQuery(params)
        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeAsInteger()
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0
        List<Org> availableOrgs = Org.executeQuery(fsq.query, fsq.queryParams, [sort:params.sort])
        availableOrgs.remove(Org.findByName("LAS:eR Backoffice"))
        result.consortiaTotal = availableOrgs.size()
        result.availableOrgs = availableOrgs.drop(result.offset).take(result.max)

        result
    }

    /**
     * Call to list non-academic institutions such as providers. The list may be rendered
     * as HTML or a configurable Excel worksheet or CSV file. The export contains more fields
     * than the HTML table due to reasons of space in the HTML page
     * @return a list of provider organisations, either as HTML table or as Excel/CSV export
     * @see OrganisationService#exportOrg(java.util.List, java.lang.Object, boolean, java.lang.String)
     * @see ExportClickMeService#getExportOrgFields(java.lang.String)
     */
    @Secured(['ROLE_USER'])
    def listProvider() {
        Map<String, Object> result = [:]
        result.propList    = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())
        result.user        = contextService.getUser()
        result.editable    = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR') || accessService.checkConstraint_ORG_COM_EDITOR()

        params.orgSector    = RDStore.O_SECTOR_PUBLISHER?.id?.toString()
        params.orgType      = RDStore.OT_PROVIDER?.id?.toString()
        params.sort        = params.sort ?: " LOWER(o.shortname), LOWER(o.name)"

        def fsq            = filterService.getOrgQuery(params)
        result.filterSet = params.filterSet ? true : false

        if (params.filterPropDef) {
            def orgIdList = Org.executeQuery("select o.id ${fsq.query}", fsq.queryParams)
            params.constraint_orgIds = orgIdList
            fsq = filterService.getOrgQuery(params)
            fsq = propertyService.evalFilterQuery(params, fsq.query, 'o', fsq.queryParams)
        }
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        List orgListTotal   = Org.findAll(fsq.query, fsq.queryParams)
        result.orgListTotal = orgListTotal.size()
        result.orgList      = orgListTotal.drop((int) result.offset).take((int) result.max)

        def message = g.message(code: 'export.all.providers')
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        String datetoday = sdf.format(new Date())
        String filename = message+"_${datetoday}"

        if ( params.exportXLS) {
            params.remove('max')
            try {
                SXSSFWorkbook wb = (SXSSFWorkbook) organisationService.exportOrg(orgListTotal, message, false, "xls")
                // Write the output to a file

                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()

                return
            }
            catch (Exception e) {
                log.error("Problem",e);
                response.sendError(500)
                return
            }
        }
        else if(params.exportClickMeExcel) {
            if (params.filename) {
                filename =params.filename
            }

            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            Map<String, Object> selectedFields = [:]
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }

            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportOrgs(orgListTotal, selectedFields, 'provider')

            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return //IntelliJ cannot know that the return prevents an obsolete redirect
        }
        withFormat {
            html {
                result
            }
            csv {
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                response.contentType = "text/csv"
                ServletOutputStream out = response.outputStream
                out.withWriter { writer ->
                    writer.write((String) organisationService.exportOrg(orgListTotal,message,true,"csv"))
                }
                out.close()
            }
        }
    }

    /**
     * Call to open the identifier creation modal; checks which namespaces are available for the given organisation
     * @return the identifier construction modal
     * @see IdentifierNamespace
     */
    def createIdentifier(){
        log.debug("OrganisationController::createIdentifier ${params}")
        Org org   = params.id? Org.get(params.id) : null

        if (! org) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
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
        List<IdentifierNamespace> nsList = IdentifierNamespace.executeQuery('select idns from IdentifierNamespace idns where (idns.nsType = :org or idns.nsType = null) and idns.isFromLaser = true and idns.ns not in (:primaryExcludes) order by idns.name_'+I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())+', idns.ns', [org: Org.class.name, primaryExcludes: primaryExcludes])
        if((RDStore.OT_PROVIDER.id in org.getAllOrgTypeIds()) || (RDStore.OT_AGENCY.id in org.getAllOrgTypeIds())) {
            nsList = nsList - IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS)
        }
        else {
            if(org.ids.find { Identifier id -> id.ns == IdentifierNamespace.findByNs(IdentifierNamespace.LEIT_ID) })
                nsList = nsList - IdentifierNamespace.findByNs(IdentifierNamespace.LEIT_ID)
            if(org.ids.find { Identifier id -> id.ns == IdentifierNamespace.findByNs(IdentifierNamespace.LEIT_KR) })
                nsList = nsList - IdentifierNamespace.findByNs(IdentifierNamespace.LEIT_KR)
        }
        render template: '/templates/identifier/modal_create', model: [orgInstance: org, nsList: nsList]
    }

    /**
     * Call to edit a given identifier; opens the creation modal with prefilled values
     * @return the identifier construction modal with prefilled values
     */
    def editIdentifier(){
        log.debug("OrganisationController::editIdentifier ${params}")
        Identifier identifier = Identifier.get(params.identifier)
        Org org = identifier?.org

        if (! identifier) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'default.search.identifier'), params.identifier])
            redirect(url: request.getHeader('referer'))
            return
        }

        render template: '/templates/identifier/modal_create', model: [orgInstance: org, identifier: identifier]
    }

    /**
     * Processes the given identifier parameters and creates a new identifier record for the given organisation
     * @return the identifier list view
     */
    @Transactional
    def processCreateIdentifier(){
        log.debug("OrganisationController::processCreateIdentifier ${params}")
        Org org   = params.orgid ? Org.get(params.orgid) : null
        if ( ! (org && params.ns.id)){
            flash.error = message(code: 'menu.admin.error')
            redirect(url: request.getHeader('referer'))
            return
        }
        IdentifierNamespace namespace   = IdentifierNamespace.get(params.ns.id)
        if (!namespace){
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'identifier.namespace.label'), params.ns.id])
            redirect(url: request.getHeader('referer'))
            return
        }
        if ( ! params.value){
            flash.error = message(code: 'identifier.create.err.missingvalue', args: [namespace.getI10n('name') ?: namespace.ns])
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
    def processCreateCustomerIdentifier(){
        log.debug("OrganisationController::processCreateCustomerIdentifier ${params}")

        Org org = params.orgid ? Org.get(params.orgid) : null
        if ( ! (org && params.addCIPlatform)){
            flash.error = message(code: 'menu.admin.error')
            redirect(url: request.getHeader('referer'))
            return
        }

        Platform plt = Platform.get(params.addCIPlatform)
        if (!plt){
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'default.provider.platform.label'), params.addCIPlatform])
            redirect(url: request.getHeader('referer'))
            return
        }

        if ( ! params.value){
            String p = plt.org.name + (plt.org.sortname ? " (${plt.org.sortname})" : '') + ' : ' + plt.name
            flash.error = message(code: 'org.customerIdentifier.create.err.missingvalue', args: [p])
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
                    type: RefdataValue.getByValueAndCategory('Default', RDConstants.CUSTOMER_IDENTIFIER_TYPE)
            )
            if(!ci.save())
                log.error("error on inserting customer identifier: ${ci.getErrors().getAllErrors().toListString()}")
        }

        redirect action: 'ids', id: params.orgid, params: [tab: 'customerIdentifiers']
    }

    /**
     * Takes the given parameters and updates the given identifier record.
     * Leitweg-IDs (the identifier necessary for the North-Rhine Westphalia billing system) are autogenerated; they
     * can get actual values only by editing. That is why pattern validation is taking place here
     * @return the identifier list view
     */
    @Transactional
    def processEditIdentifier(){
        log.debug("OrganisationController::processEditIdentifier ${params}")
        Identifier identifier   = Identifier.get(params.identifierId)
        if ( ! identifier){
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'default.search.identifier'), params.identifierId])
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
                flash.error = message(code: 'identifier.edit.err.leitID', args: [message(code: 'identifier.leitID.leitID1.info')])
                redirect(url: request.getHeader('referer'))
                return
            }

            if(params.leitID2 ==~ /[a-zA-Z0-9]{0,30}/) {
                leitID2 = params.leitID2
            }else{
                flash.error = message(code: 'identifier.edit.err.leitID', args: [message(code: 'identifier.leitID.leitID2.info')])
                redirect(url: request.getHeader('referer'))
                return
            }

            if(params.leitID3 ==~ /[0-9]{2,2}/) {
                leitID3 = params.leitID3
            }else{
                flash.error = message(code: 'identifier.edit.err.leitID', args: [message(code: 'identifier.leitID.leitID3.info')])
                redirect(url: request.getHeader('referer'))
                return
            }

            params.value = leitID1 + '-' + (leitID2 ? leitID2 + '-' : '') + leitID3
        }

        if ( ! params.value){
            flash.error = message(code: 'identifier.edit.err.missingvalue', args: [identifier.ns?.getI10n('name') ?: identifier.ns?.ns])
            redirect(url: request.getHeader('referer'))
            return
        }

        identifier.value = params.value.trim()
        identifier.note = params.note?.trim()
        identifier.save()

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Takes the given parameters and updates the given customer identifier record.
     * @return the identifier list view, opened on tab customer identifiers
     */
    @Transactional
    def processEditCustomerIdentifier(){
        log.debug("OrganisationController::processEditIdentifier ${params}")
        CustomerIdentifier customeridentifier   = CustomerIdentifier.get(params.customeridentifier)
        if ( ! customeridentifier){
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'default.search.identifier'), params.identifierId])
            redirect(url: request.getHeader('referer'))
            return
        }
        if ( ! params.value){
            Platform plt = customeridentifier.platform
            String p = plt.org.name + (plt.org.sortname ? " (${plt.org.sortname})" : '') + ' : ' + plt.name
            flash.error = message(code: 'org.customerIdentifier.edit.err.missingvalue', args: [p])
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
    def createCustomerIdentifier(){
        log.debug("OrganisationController::createCustomerIdentifier ${params}")
        Org org   = Org.get(params.id)

        if (! org) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect(url: request.getHeader('referer'))
            return
        }
        List<Platform> allPlatforms = organisationService.getAllPlatforms()

        render template: '/templates/customerIdentifier/modal_create', model: [orgInstance: org, allPlatforms: allPlatforms]
    }

    /**
     * Call to open the customer identifier creation modal with prefilled values
     * @return the customer identifier construction modal with prefilled values
     */
    def editCustomerIdentifier(){
        log.debug("OrganisationController::editCustomerIdentifier ${params}")
        CustomerIdentifier customeridentifier = CustomerIdentifier.get(params.customeridentifier)
        Org org = customeridentifier?.owner

        if (! customeridentifier) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.customerIdentifier'), params.customeridentifier])
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
    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR'])
    @Transactional
    def create() {
        switch (request.method) {
            case 'POST':
                Org orgInstance = new Org(params)
                orgInstance.status = RDStore.O_STATUS_CURRENT

                //if (params.name) {
                    if (orgInstance.save()) {
                        orgInstance.setDefaultCustomerType()

                        flash.message = message(code: 'default.created.message', args: [message(code: 'org.label'), orgInstance.name])
                        redirect action: 'show', id: orgInstance.id
                        return
                    }
                //}

                render view: 'create', model: [orgInstance: orgInstance]
                break
        }
    }

    /**
     * Creates a new provider organisation with the given parameters
     * @return the details view of the provider or the creation view in case of an error
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN,ROLE_ORG_EDITOR", wtc = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN,ROLE_ORG_EDITOR")
    })
    def createProvider() {
        Org.withTransaction {

            Org orgInstance = new Org(name: params.provider, sector: RDStore.O_SECTOR_PUBLISHER, status: RDStore.O_STATUS_CURRENT)
            if (orgInstance.save()) {

                orgInstance.addToOrgType(RDStore.OT_PROVIDER)
                orgInstance.save()

                flash.message = message(code: 'default.created.message', args: [message(code: 'org.label'), orgInstance.name])
                redirect action: 'show', id: orgInstance.id
                return
            }
            else {
                log.error("Problem creating org: ${orgInstance.errors}");
                flash.message = message(code: 'org.error.createProviderError', args: [orgInstance.errors])
                redirect(action: 'findProviderMatches')
                return
            }
        }
    }

    /**
     * Call to create a new provider; offers first a query for the new name to insert in order to exclude duplicates
     * @return the empty form (with a submit to proceed with the new organisation) or a list of eventual name matches
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN,ROLE_ORG_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN,ROLE_ORG_EDITOR")
    })
    def findProviderMatches() {

        Map<String, Object> result = [:]
        if ( params.proposedProvider ) {

            result.providerMatches= Org.executeQuery("from Org as o where exists (select roletype from o.orgType as roletype where roletype = :provider ) and (lower(o.name) like :searchName or lower(o.shortname) like :searchName or lower(o.sortname) like :searchName ) ",
                    [provider: RDStore.OT_PROVIDER, searchName: "%${params.proposedProvider.toLowerCase()}%"])
        }
        result
    }

    /**
     * Call to create a new member with the given parameter map
     * @return the details view of the new member in case of success, the creation page otherwise
     */
    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_EDTIOR",specRole="ROLE_ADMIN, ROLE_ORG_EDITOR", ctrlService = 2)
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM","INST_EDITOR","ROLE_ADMIN, ROLE_ORG_EDITOR") })
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
    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_EDITOR",specRole="ROLE_ADMIN, ROLE_ORG_EDITOR")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM","INST_EDITOR","ROLE_ADMIN, ROLE_ORG_EDITOR") })
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
            result.organisationMatches.addAll(Org.executeQuery("select o from Org as o where exists (select roletype from o.orgType as roletype where roletype = :institution ) and (lower(o.name) like :searchName or lower(o.shortname) like :searchName or lower(o.sortname) like :searchName) ",
                    [institution: RDStore.OT_INSTITUTION, searchName: "%${params.proposedOrganisation.toLowerCase()}%"]))
        }
        if (params.proposedOrganisationID) {
            result.organisationMatches.addAll(Org.executeQuery("select id.org from Identifier id where lower(id.value) like :identifier and lower(id.ns.ns) in (:namespaces) ",
                    [identifier: "%${params.proposedOrganisationID.toLowerCase()}%",namespaces:["isil","wibid"]]))
        }

        result
    }

    /**
     * Shows the details of the organisation to display
     * @return the details view of the given orgainsation
     */
    @Secured(['ROLE_USER'])
    def show() {

        ProfilerUtils pu = new ProfilerUtils()
        pu.setBenchmark('this-n-that')

        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        if (! result) {
            response.sendError(401)
            return
        }
        if (! result.orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect action: 'list'
            return
        }

        result.availableOrgTypes = RefdataCategory.getAllRefdataValues(RDConstants.ORG_TYPE)-RDStore.OT_CONSORTIUM
        result.missing = [:]
        if(result.error)
            flash.error = result.error //to display we:kb's eventual 404

        if(result.inContextOrg && result.institution.eInvoice) {
            Identifier leitID = result.institution.getLeitID()

            if(!result.institution.eInvoicePortal)
                result.missing.eInvoicePortal = message(code: 'org.eInvoice.info.missing.eInvoicePortal')
            if(!leitID || (leitID && (leitID.value == '' || leitID.value == null)))
                result.missing.leitID = message(code: 'org.eInvoice.info.missing.leitID')
        }

        pu.setBenchmark('orgRoles & editable')

        pu.setBenchmark('tasks')

        result.tasks = taskService.getTasksByResponsiblesAndObject(result.user,result.institution,result.orgInstance)
        Map<String,Object> preCon = taskService.getPreconditionsWithoutTargets(result.institution)
        result << preCon

        pu.setBenchmark('properties')

        result.authorizedOrgs = result.user?.authorizedOrgs

        // create mandatory OrgPrivateProperties if not existing

        List<PropertyDefinition> mandatories = PropertyDefinition.getAllByDescrAndMandatoryAndTenant(PropertyDefinition.ORG_PROP, true, result.institution)

        mandatories.each { PropertyDefinition pd ->
            if (!OrgProperty.findWhere(owner: result.orgInstance, type: pd)) {
                def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, result.orgInstance, pd, result.institution)


                if (newProp.hasErrors()) {
                    log.error(newProp.errors.toString())
                } else {
                    log.debug("New org private property created via mandatory: " + newProp.type.name)
                }
            }
        }

        pu.setBenchmark('identifier')

        if(!result.isProviderOrAgency){
            result.orgInstance.createCoreIdentifiersIfNotExist()
        }

        pu.setBenchmark('createdBy and legallyObligedBy')

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
        List bm = pu.stopBenchmark()
        result.benchMark = bm

        result
    }

    /**
     * Displays either the identifiers or the customer identifiers attached to the organisation.
     * Only institutions may have customer identifiers; commercial organisations cannot have such
     * @return the table view of the records, either the identifiers or the customer number key pairs
     * @see Identifier
     * @see CustomerIdentifier
     */
    @Secured(['ROLE_USER'])
    def ids() {

        ProfilerUtils pu = new ProfilerUtils()
        pu.setBenchmark('this-n-that')

        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        if(!result) {
            response.sendError(401)
            return
        }
        result.editable_identifier = result.editable

        //this is a flag to check whether the page has been called directly after creation
        result.fromCreate = params.fromCreate ? true : false
        if(!params.tab)
            params.tab = 'identifier'

        pu.setBenchmark('editable_identifier')

        //IF ORG is a Provider
        if(result.orgInstance.sector == RDStore.O_SECTOR_PUBLISHER || RDStore.OT_PROVIDER.id in result.allOrgTypeIds) {
            pu.setBenchmark('editable_identifier2')
            result.editable_identifier = accessService.checkMinUserOrgRole(result.user, result.orgInstance, 'INST_EDITOR') ||
                    accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN,ROLE_ORG_EDITOR")
        }
        else {
            pu.setBenchmark('editable_identifier2')
            if(accessService.checkPerm("ORG_CONSORTIUM")) {
                List<Long> consortia = Combo.executeQuery('select c.id from Combo c where c.type = :type and c.fromOrg = :target and c.toOrg = :context',[type:RDStore.COMBO_TYPE_CONSORTIUM,target:result.orgInstance,context:result.institution])
                if(consortia.size() == 1 && accessService.checkMinUserOrgRole(result.user,result.institution,'INST_EDITOR'))
                    result.editable_identifier = true
            }
            else
                result.editable_identifier = accessService.checkMinUserOrgRole(result.user, result.orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
        }

          if (!result.orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect action: 'list'
            return
          }

        pu.setBenchmark('create Identifiers if necessary')

        // TODO: experimental asynchronous task
        //waitAll(task_orgRoles, task_properties)

        if(!(RDStore.OT_PROVIDER.id in result.allOrgTypeIds)){
            result.orgInstance.createCoreIdentifiersIfNotExist()
        }

        pu.setBenchmark('orgsettings')
        Boolean inContextOrg = result.inContextOrg
        Boolean isComboRelated = Combo.findByFromOrgAndToOrg(result.orgInstance, result.institution)
        result.isComboRelated = isComboRelated

        result.hasAccessToCustomeridentifier = ((inContextOrg && accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_USER')) ||
                (isComboRelated && accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_USER')) ||
                SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')) && OrgSetting.get(result.orgInstance, OrgSetting.KEYS.CUSTOMER_TYPE) != OrgSetting.SETTING_NOT_FOUND

        if (result.hasAccessToCustomeridentifier) {

            result.editable_customeridentifier = (inContextOrg && accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')) ||
                    (isComboRelated && accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')) ||
                    SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')

            // adding default settings
            organisationService.initMandatorySettings(result.orgInstance)
            if(params.tab == 'customerIdentifiers') {
                result.allPlatforms = organisationService.getAllPlatforms()
                Map<String, Object> queryParams = [customer: result.orgInstance]
                String query = "select ci from CustomerIdentifier ci join ci.platform platform where ci.customer = :customer"
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
                String sort = " order by platform.org.name asc"
                if(params.sort) {
                    sort = " order by ${params.sort} ${params.order}"
                }
                if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')) {
                    result.customerIdentifier = CustomerIdentifier.executeQuery(query+sort, queryParams)
                } else if (inContextOrg) {

                    if (result.institution.hasPerm('ORG_CONSORTIUM,ORG_INST')) {
                        result.customerIdentifier = CustomerIdentifier.executeQuery(query+sort, queryParams)
                    } else if (['ORG_BASIC_MEMBER'].contains(result.institution.getCustomerType())) {
                        result.customerIdentifier = CustomerIdentifier.executeQuery(query+sort, queryParams)
                    }
                } else if (isComboRelated) {
                    log.debug('settings for combo related org: consortia')
                    result.customerIdentifier = CustomerIdentifier.executeQuery(query+sort, queryParams)
                }
            }

        }
        List bm = pu.stopBenchmark()
        result.benchMark = bm
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
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER") })
    def tasks() {
        Map<String,Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        if (!result) {
            response.sendError(401); return
        }

        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        result.putAll(taskService.getTasks(offset, (User) result.user, (Org) result.institution, result.orgInstance))

        result
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
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def documents() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        if(!result) {
            response.sendError(401)
            return
        }
        result
    }

    /**
     * Call to edit the given document. Beware: edited are the relations between the document and the object
     * it has been attached to; content editing of an uploaded document is not possible in this app!
     * @return the modal to edit the document parameters
     */
    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def editDocument() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        if(!result) {
            response.sendError(401)
            return
        }
        result.ownobj = result.institution
        result.owntp = 'organisation'
        if(params.id) {
            result.docctx = DocContext.get(params.id)
            result.doc = result.docctx.owner
        }

        render template: "/templates/documents/modal", model: result
    }

    /**
     * Call to delete a given document
     * @return the document table view ({@link #documents()})
     * @see com.k_int.kbplus.DocstoreService#unifiedDeleteDocuments()
     */
    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def deleteDocuments() {
        log.debug("deleteDocuments ${params}");

        docstoreService.unifiedDeleteDocuments(params)

        redirect controller: 'organisation', action:params.redirectAction, id:params.instanceId /*, fragment: 'docstab' */
    }

    /**
     * Opens the notes view for the given organisation. Beware that those notes are being shown
     * which are attached to the given organisation; the target (if it is an institution) does not
     * see the notes!
     * @return a {@link List} of notes ({@link Doc})
     * @see Doc
     * @see DocContext
     */
    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = {
        ctx.contextService.getUser()?.hasAffiliation("INST_USER")
    })
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
    @DebugAnnotation(perm="FAKE,ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN,ROLE_ORG_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("FAKE,ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN,ROLE_ORG_EDITOR")
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
    @DebugAnnotation(ctrlService = 2)
    @Secured(['ROLE_USER'])
    def deleteIdentifier() {
        identifierService.deleteIdentifier(params.owner,params.target)
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Shows all user accounts affiliated to (at least) the given institution
     * @return renders the user list template with the users affiliated to this institution
     * @see UserOrg
     * @see User
     */
    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_ADM") })
    def users() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        if (! result.orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id]) as String
            redirect action: 'list'
            return
        }

        result.editable = checkIsEditable(result.user, result.orgInstance)

        if (! result.editable) {
            boolean instAdminExists = result.orgInstance.getAllValidInstAdmins().size() > 0
            boolean comboCheck = instAdmService.hasInstAdmPivileges(result.user, result.orgInstance, [RDStore.COMBO_TYPE_CONSORTIUM])

            result.editable = comboCheck && ! instAdminExists
        }

        if (! result.editable) {
            redirect controller: 'organisation', action: 'show', id: result.orgInstance.id
            return
        }

        Map filterParams = params
        filterParams.org = genericOIDService.getOID(result.orgInstance)

        result.users = userService.getUserSet(filterParams)
        result.titleMessage = "${result.orgInstance.name} - ${message(code:'org.nav.users')}"
        result.inContextOrg = false
        result.multipleAffiliationsWarning = true
        Set<Org> availableComboOrgs = Org.executeQuery('select c.fromOrg from Combo c where c.toOrg = :ctxOrg order by c.fromOrg.name asc', [ctxOrg:result.orgInstance])
        availableComboOrgs.add(result.orgInstance)

        result.navConfig = [
                orgInstance: result.orgInstance, inContextOrg: result.inContextOrg
        ]
        result.filterConfig = [
                filterableRoles:Role.findAllByRoleType('user'), orgField: false
        ]
        result.tmplConfig = [
                editable: result.editable,
                editor: result.user,
                editLink: 'editUser',
                deleteLink: 'deleteUser',
                users: result.users,
                showAllAffiliations: false,
                modifyAccountEnability: SpringSecurityUtils.ifAllGranted('ROLE_YODA'),
                availableComboOrgs: availableComboOrgs
        ]
        result.total = result.users.size()
        render view: '/user/global/list', model: result
    }

    /**
     * Call to detach the given user from the given institution.
     * Data the given user may have authored will be reassigned to another user
     * @return the user deletion view where eventual conflicts are being listed
     */
    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM", "INST_ADM", "ROLE_ADMIN") })
    def deleteUser() {
        Map<String, Object> result = userControllerService.getResultGenericsERMS3067(params)
        result.orgInstance = Org.get(params.id) // overwrite

        if (! result.editable) {
            redirect controller: 'organisation', action: 'users', params: params
            return
        }

        if (result.user) {
            List<Org> affils = Org.executeQuery('select distinct uo.org from UserOrg uo where uo.user = :user', [user: result.user])

            if (affils.size() > 1) {
                flash.error = message(code: 'user.delete.error.multiAffils') as String
                redirect action: 'editUser', params: [uoid: params.uoid, id: params.id]
                return
            }
            else if (affils.size() == 1 && ! result.editable) {
                flash.error = message(code: 'user.delete.error.foreignOrg') as String
                redirect action: 'editUser', params: [uoid: params.uoid, id: params.id]
                return
            }

            if (params.process && result.editable) {
                User userReplacement = (User) genericOIDService.resolveOID(params.userReplacement)

                result.delResult = deletionService.deleteUser(result.user, userReplacement, false)
            }
            else {
                result.delResult = deletionService.deleteUser(result.user, null, DeletionService.DRY_RUN)
            }

            result.substituteList = User.executeQuery(
                    'select distinct u from User u join u.affiliations ua where ua.org = :ctxOrg and u != :self',
                    [ctxOrg: result.orgInstance, self: result.user]
            )
        }

        render view: '/user/global/delete', model: result
    }

    /**
     * Call to edit the given user profile
     * @return the profile editing template
     */
    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM", "INST_ADM", "ROLE_ADMIN") })
    def editUser() {
        Map result = [
                user: genericOIDService.resolveOID(params.uoid),
                orgInstance: Org.get(params.id),
                manipulateAffiliations: contextService.getUser().hasRole(['ROLE_ADMIN', 'ROLE_YODA'])
        ]
        result.editable = checkIsEditable(result.user, contextService.getOrg())
        result.availableOrgs = [ result.orgInstance ]

        render view: '/user/global/edit', model: result
    }

    /**
     * Call to create a new user profile
     * @return the profile creation template
     */
    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM", "INST_ADM", "ROLE_ADMIN") })
    def createUser() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        result.availableOrgs = Org.get(params.id)
        result.editor = result.user

        render view: '/user/global/create', model: result
    }

    /**
     * Takes the submitted parameters and creates a new user record with the given parameters
     * @return the user editing template in case of success, redirects back to the creation page otherwise
     */
    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM","INST_ADM","ROLE_ADMIN") })
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
    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM","INST_ADM","ROLE_ADMIN") })
    def addAffiliation() {
        Map<String, Object> result = userControllerService.getResultGenericsERMS3067(params)
        result.orgInstance = Org.get(params.id) // overwrite

        if (! result.editable) {
            flash.error = message(code: 'default.noPermissions') as String
            redirect action: 'editUser', params: [id: params.id, uoid: params.uoid]
            return
        }
        userService.addAffiliation(result.user,params.org,params.formalRole,flash)
        redirect action: 'editUser', params: [id: params.id, uoid: params.uoid]
    }

    @Deprecated
    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR'])
    def edit() {
        redirect controller: 'organisation', action: 'show', params: params
        return
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

            if (contextService.getUser().isAdmin()) {
                result.substituteList = Org.executeQuery("select distinct o from Org o where o.status != :delState", [delState: RDStore.O_STATUS_DELETED])
            }
            else {
                List<Org> orgList = [result.orgInstance]
                orgList.addAll(Org.executeQuery("select o from Combo cmb join cmb.fromOrg o where o.status != :delState and cmb.toOrg = :org", [delState: RDStore.O_STATUS_DELETED, org: result.orgInstance]))
                orgList.addAll(Org.executeQuery("select o from Combo cmb join cmb.toOrg o where o.status != :delState and cmb.fromOrg = :org", [delState: RDStore.O_STATUS_DELETED, org: result.orgInstance]))
                orgList.unique()

                result.substituteList = orgList
            }
        }

        render view: 'delete', model: result
    }

    /**
     * Was used to link two organisations by combo
     * @deprecated Use {@link #linkOrgs()} instead
     */
    @Deprecated
    @Secured(['ROLE_USER'])
    @Transactional
    def addOrgCombo(Org fromOrg, Org toOrg) {
        RefdataValue comboType = RefdataValue.get(params.comboTypeTo)
        log.debug("Processing combo creation between ${fromOrg} AND ${toOrg} with type ${comboType}")
        def dupe = Combo.executeQuery("from Combo as c where c.fromOrg = :fromOrg and c.toOrg = :toOrg", [fromOrg: fromOrg, toOrg: toOrg])
      
      if (! dupe) {
          Combo consLink = new Combo(fromOrg:fromOrg,
                                 toOrg:toOrg,
                                 status:null,
                                 type:comboType)
          consLink.save()
      }
      else {
        flash.message = "This Combo already exists!"
      }
    }

    /**
     * Call to list the public contacts of the given organisation
     * @return a table view of public contacts
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def addressbook() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        if(!result) {
            response.sendError(401)
            return
        }

        if (! result.institution) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect action: 'list'
            return
        }

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.org = result.orgInstance
        params.sort = params.sort ?: 'p.last_name, p.first_name'

        List visiblePersons = addressbookService.getVisiblePersons("addressbook",params)

        result.propList =
                PropertyDefinition.findAllWhere(
                        descr: PropertyDefinition.PRS_PROP,
                        tenant: contextService.getOrg() // private properties
                )

        result.num_visiblePersons = visiblePersons.size()
        result.visiblePersons = visiblePersons.drop(result.offset).take(result.max)

        if (visiblePersons){
            result.emailAddresses = Contact.executeQuery("select c.content from Contact c where c.prs in (:persons) and c.contentType = :contentType",
                    [persons: visiblePersons, contentType: RDStore.CCT_EMAIL])
        }

        result
    }

    /**
     * Lists the current reader numbers of the given institution
     * @return a table view of the reader numbers, grouped by semesters on the one hand, due dates on the other
     * @see ReaderNumber
     */
    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_USER") })
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
        Map<String,Map<String,ReaderNumber>> numbersWithDueDate = organisationService.groupReaderNumbersByProperty(ReaderNumber.findAllByOrgAndDueDateIsNotNull((Org) result.orgInstance,[sort:params.sortB,order:params.orderB]),"dueDate")

        TreeSet<String> semesterCols = [], dueDateCols = []
        Map<String,BigDecimal> dueDateSums = [:]
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
        numbersWithDueDate.each { Map.Entry<String,Map<String,ReaderNumber>> dueDates ->
            dueDates.value.each { Map.Entry<String,ReaderNumber> row ->
                dueDateCols << row.key
                ReaderNumber rn = row.value
                BigDecimal dueDateSum = dueDateSums.get(dueDates.key)
                if(rn.value) {
                    if(dueDateSum == null) {
                        dueDateSum = rn.value
                    }
                    else dueDateSum += rn.value
                }
                dueDateSums.put(dueDates.key,dueDateSum)
            }
        }

        result.numbersWithSemester = numbersWithSemester
        result.numbersWithDueDate = numbersWithDueDate
        result.semesterCols = semesterCols
        result.semesterSums = semesterSums
        result.dueDateCols = dueDateCols
        result.dueDateSums = dueDateSums

        result
    }

    /**
     * Lists the access point configurations of the given institution
     * @return a list view of access points
     * @see de.laser.oap.OrgAccessPoint
     */
    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_USER") })
    def accessPoints() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)
        if(!result) {
            response.sendError(401)
            return
        }

        if (! result.orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect action: 'list'
            return
        }

        List orgAccessPointList = accessPointService.getOapListWithLinkCounts(result.orgInstance)
        result.orgAccessPointList = orgAccessPointList.groupBy {it.oap.accessMethod.value}.sort {it.key}

        if (params.exportXLSX) {

            SXSSFWorkbook wb
            SimpleDateFormat sdf = DateUtils.getSDF_NoTimeNoPoint()
            String datetoday = sdf.format(new Date())
            String filename = "${datetoday}_" + g.message(code: "org.nav.accessPoints")
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb = (SXSSFWorkbook) accessPointService.exportAccessPoints(orgAccessPointList.collect {it.oap}, result.institution)
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
     * Links two organisations with the given params
     */
    def linkOrgs() {
        organisationControllerService.linkOrgs(params)
        redirect action: 'show', id: params.context
    }

    /**
     * Removes the given link between two organisations
     */
    def unlinkOrg() {
        organisationControllerService.unlinkOrg(params)
        redirect action: 'show', id: params.id
    }

    /**
     * Assigns the given organisation type to the given organisation
     */
    @Transactional
    def addOrgType() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        Org orgInstance = Org.get(params.org)

        if (!orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id]) as String
            redirect action: 'list'
            return
        }
        result.editable = checkIsEditable(result.user, orgInstance)

        if (result.editable) {
            orgInstance.addToOrgType(RefdataValue.get(params.orgType))
            orgInstance.save()
//            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label'), orgInstance.name])
        }

        redirect action: 'show', id: orgInstance.id
    }

    /**
     * Removes the given organisation type from the given organisation
     */
    @Transactional
    def deleteOrgType() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        Org orgInstance = Org.get(params.org)

        if (!orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id]) as String
            redirect action: 'list'
            return
        }

        result.editable = checkIsEditable(result.user, orgInstance)

        if (result.editable) {
            orgInstance.removeFromOrgType(RefdataValue.get(params.removeOrgType))
            orgInstance.save()
//            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label'), orgInstance.name])
        }

        redirect action: 'show', id: orgInstance.id
    }

    /**
     * Assigns the given subject group to the given organisation
     */
    @Transactional
    def addSubjectGroup() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        if (!result.orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect(url: request.getHeader('referer'))
            return
        }
        RefdataValue newSubjectGroup = RefdataValue.get(params.subjectGroup)
        if (!newSubjectGroup) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.subjectGroup.label'), params.subjectGroup])
            redirect(url: request.getHeader('referer'))
            return
        }
        if (result.orgInstance.getSubjectGroup().find { it.subjectGroupId == newSubjectGroup.id }) {
            flash.message = message(code: 'default.err.alreadyExist', args: [message(code: 'org.subjectGroup.label')])
            redirect(url: request.getHeader('referer'))
            return
        }
        result.editable = checkIsEditable(result.user, result.orgInstance)

        if (result.editable) {
            result.orgInstance.addToSubjectGroup(subjectGroup: RefdataValue.get(params.subjectGroup))
            result.orgInstance.save()
//            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label'), orgInstance.name])
        }

        redirect action: 'show', id: params.id
    }

    /**
     * Removes the given subject group from the given organisation
     */
    @Transactional
    def deleteSubjectGroup() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        if (!result.orgInstance) {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
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
     * Call to toggle the consortium membership state between the given institution and the consortium
     * (adds or removes a combo link between the institution and the consortium)
     * @see Combo
     */
    @DebugAnnotation(perm="ORG_CONSORTIUM", type="Consortium", affil="INST_EDITOR", specRole="ROLE_ADMIN, ROLE_ORG_EDITOR", ctrlService = 2)
    @Secured(closure = { ctx.accessService.checkPermTypeAffiliationX("ORG_CONSORTIUM", "Consortium", "INST_EDITOR", "ROLE_ADMIN,ROLE_ORG_EDITOR") })
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
    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def myPublicContacts() {
        Map<String, Object> result = organisationControllerService.getResultGenericsAndCheckAccess(this, params)

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        result.rdvAllPersonFunctions = [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, RDStore.PRS_FUNC_CONTACT_PRS, RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN]
        result.rdvAllPersonPositions = PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION) - [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS]

        if(result.institution.getCustomerType() == 'ORG_CONSORTIUM' && result.orgInstance)
        {
            params.org = result.orgInstance
            result.rdvAllPersonFunctions << RDStore.PRS_FUNC_GASCO_CONTACT
        }else{
            params.org = result.institution
        }

        List allOrgTypeIds = result.orgInstance.getAllOrgTypeIds()
        if(RDStore.OT_PROVIDER.id in allOrgTypeIds || RDStore.OT_AGENCY.id in allOrgTypeIds){
            result.rdvAllPersonFunctions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FUNC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FUNC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FUNC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS, RDStore.PRS_FUNC_FUNC_DELIVERY_ADDRESS]
            result.rdvAllPersonPositions = [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_DIREKTION, RDStore.PRS_POS_DIREKTION_ASS, RDStore.PRS_POS_RB, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS, RDStore.PRS_POS_TS]

        }

        params.sort = params.sort ?: 'p.last_name, p.first_name'

        List visiblePersons = addressbookService.getVisiblePersons("myPublicContacts",params)
        result.num_visiblePersons = visiblePersons.size()
        result.visiblePersons = visiblePersons.drop(result.offset).take(result.max)

        if (visiblePersons){
            result.emailAddresses = Contact.executeQuery("select c.content from Contact c where c.prs in (:persons) and c.contentType = :contentType",
                    [persons: visiblePersons, contentType: RDStore.CCT_EMAIL])
        }

        params.tab = params.tab ?: 'contacts'

        result.addresses = Address.findAllByOrg(params.org)

        result
    }

    /**
     * Helper method to determine the edit rights the given user has for the given organisation in the given view
     * @param user the user whose rights should be checked
     * @param org the target organisation
     * @return true if edit rights are granted to the given user/org/view context, false otherwise
     */
    boolean checkIsEditable(User user, Org org) {
        boolean isEditable
        Org contextOrg = contextService.getOrg()
        Org orgInstance = org
        boolean inContextOrg =  orgInstance?.id == contextOrg.id
        boolean userHasEditableRights = user.hasRole('ROLE_ADMIN') ||user.hasRole('ROLE_ORG_EDITOR') || user.hasAffiliation('INST_EDITOR')
        switch(params.action){
            case 'editUser':
                isEditable = true
                break
            case 'delete':
                isEditable = SpringSecurityUtils.ifAnyGranted('ROLE_ORG_EDITOR,ROLE_ADMIN')
                break
            case 'properties':
                isEditable = accessService.checkMinUserOrgRole(user, Org.get(params.id), 'INST_EDITOR') || SpringSecurityUtils.ifAllGranted('ROLE_ADMIN')
                break
            case 'users':
                isEditable = accessService.checkMinUserOrgRole(user, Org.get(params.id), 'INST_ADM') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
                break
            case 'addOrgType':
            case 'deleteOrgType':
                isEditable = accessService.checkMinUserOrgRole(user, Org.get(params.org), 'INST_ADM') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
                break
            case 'myPublicContacts':
                if (inContextOrg) {
                    isEditable = userHasEditableRights
                }else{
                    isEditable = user.hasRole('ROLE_YODA')
                }
                break
            case 'show':
            case 'ids':
            case 'readerNumber':
            case 'accessPoints':
            case 'addSubjectGroup':
            case 'deleteSubjectGroup':
            case 'addressbook':
                if (inContextOrg) {
                    isEditable = userHasEditableRights
                } else {
                    switch (contextOrg.getCustomerType()){
                        case 'ORG_BASIC_MEMBER':
                            switch (orgInstance.getCustomerType()){
                                case 'ORG_BASIC_MEMBER':    isEditable = user.hasRole('ROLE_YODA'); break
                                case 'ORG_INST':            isEditable = user.hasRole('ROLE_YODA'); break
                                case 'ORG_CONSORTIUM':      isEditable = user.hasRole('ROLE_YODA'); break
                                default:                    isEditable = user.hasRole('ROLE_YODA'); break
                            }
                            break
                        case 'ORG_INST':
                            switch (orgInstance.getCustomerType()){
                                case 'ORG_BASIC_MEMBER':    isEditable = user.hasRole('ROLE_YODA'); break
                                case 'ORG_INST':            isEditable = user.hasRole('ROLE_YODA'); break
                                case 'ORG_CONSORTIUM':      isEditable = user.hasRole('ROLE_YODA'); break
                                default:                    isEditable = userHasEditableRights; break //means providers and agencies
                            }
                            break
                        case 'ORG_CONSORTIUM':
                            switch (orgInstance.getCustomerType()){
                                case 'ORG_BASIC_MEMBER':    isEditable = userHasEditableRights; break
                                case 'ORG_INST':            isEditable = userHasEditableRights; break
                                case 'ORG_CONSORTIUM':      isEditable = user.hasRole('ROLE_YODA'); break
                                default:                    isEditable = userHasEditableRights; break //means providers and agencies
                            }
                            break
                    }
                }
                break
            default:
                isEditable = accessService.checkMinUserOrgRole(user, org,'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
        }
        isEditable
    }
}
