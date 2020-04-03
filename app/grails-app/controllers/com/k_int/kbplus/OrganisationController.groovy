package com.k_int.kbplus

import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.properties.PropertyDefinition
import de.laser.DeletionService
import de.laser.controller.AbstractDebugController
import de.laser.helper.*
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.xssf.streaming.SXSSFWorkbook

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat

import static de.laser.helper.RDStore.*

@Secured(['IS_AUTHENTICATED_FULLY'])
class OrganisationController extends AbstractDebugController {

    def springSecurityService
    def accessService
    def contextService
    def addressbookService
    def filterService
    def genericOIDService
    def propertyService
    def docstoreService
    def instAdmService
    def organisationService
    def deletionService
    def userService
    def accessPointService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_ORG_EDITOR','ROLE_ADMIN'])
    def index() {
        redirect action: 'list', params: params
    }

    @DebugAnnotation(perm="FAKE,ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_ADM", specRole="ROLE_ADMIN,ROLE_ORG_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("FAKE,ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_ADM", "ROLE_ADMIN,ROLE_ORG_EDITOR")
    })
    def settings() {

        User user = User.get(springSecurityService.principal.id)
        Org org   = Org.get(params.id)

        if (! org) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect action: 'list'
            return
        }

        Boolean inContextOrg = contextService.getOrg().id == org.id
        Boolean isComboRelated = Combo.findByFromOrgAndToOrg(org, contextService.getOrg())

        Boolean hasAccess = (inContextOrg && accessService.checkMinUserOrgRole(user, org, 'INST_ADM')) ||
                (isComboRelated && accessService.checkMinUserOrgRole(user, contextService.getOrg(), 'INST_ADM')) ||
                SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')

        // forbidden access
        if (! hasAccess) {
            redirect controller: 'organisation', action: 'show', id: org.id
        }

        Map result = [
                user:           user,
                orgInstance:    org//,
//                editable:   	SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR'),
//                inContextOrg:   inContextOrg
        ]
//        result.editable = result.editable || (inContextOrg && accessService.checkMinUserOrgRole(user, org, 'INST_ADM'))
//        result.isComboRelated = isComboRelated

//		if (params.deleteCI) {
//			CustomerIdentifier ci = genericOIDService.resolveOID(params.deleteCI)
//			if (ci && ci.owner == org) {
//				ci.delete()
//			}
//		}
//        if (params.addCIPlatform) {
//            Platform plt = genericOIDService.resolveOID(params.addCIPlatform)
//            if (plt) {
//                CustomerIdentifier ci = new CustomerIdentifier(
//                        customer: org,
//                        platform: plt,
//                        value: params.addCIValue?.trim(),
//                        note: params.addCINote?.trim(),
//                        owner: contextService.getOrg(),
//                        isPublic: true,
//                        type: RefdataValue.getByValueAndCategory('Default', RDConstants.CUSTOMER_IDENTIFIER_TYPE)
//                )
//                ci.save()
//            }
//        }

        // adding default settings
        organisationService.initMandatorySettings(org)

        // collecting visible settings by customer type, role and/or combo
        List<OrgSettings> allSettings = OrgSettings.findAllByOrg(org)

        List<OrgSettings.KEYS> ownerSet = [
                OrgSettings.KEYS.API_LEVEL,
                OrgSettings.KEYS.API_KEY,
                OrgSettings.KEYS.API_PASSWORD,
                OrgSettings.KEYS.CUSTOMER_TYPE,
                OrgSettings.KEYS.GASCO_ENTRY
        ]
        List<OrgSettings.KEYS> accessSet = [
                OrgSettings.KEYS.OAMONITOR_SERVER_ACCESS,
                OrgSettings.KEYS.NATSTAT_SERVER_ACCESS
        ]
        List<OrgSettings.KEYS> credentialsSet = [
                OrgSettings.KEYS.NATSTAT_SERVER_API_KEY,
                OrgSettings.KEYS.NATSTAT_SERVER_REQUESTOR_ID
        ]

        result.settings = []

        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')) {
            result.settings.addAll(allSettings.findAll { it.key in ownerSet })
            result.settings.addAll(allSettings.findAll { it.key in accessSet })
            result.settings.addAll(allSettings.findAll { it.key in credentialsSet })
            result.customerIdentifier = CustomerIdentifier.findAllByCustomer(org)
        }
        else if (inContextOrg) {
            log.debug( 'settings for own org')
            result.settings.addAll(allSettings.findAll { it.key in ownerSet })

            if (org.hasPerm('ORG_CONSORTIUM,ORG_INST')) {
                result.settings.addAll(allSettings.findAll { it.key in accessSet })
                result.settings.addAll(allSettings.findAll { it.key in credentialsSet })
                result.customerIdentifier = CustomerIdentifier.findAllByCustomer(org)
            }
            else if (['ORG_BASIC_MEMBER'].contains(org.getCustomerType())) {
                result.settings.addAll(allSettings.findAll { it.key == OrgSettings.KEYS.NATSTAT_SERVER_ACCESS })
                result.customerIdentifier = CustomerIdentifier.findAllByCustomer(org)
            }
            else if (['FAKE'].contains(org.getCustomerType())) {
                result.settings.addAll(allSettings.findAll { it.key == OrgSettings.KEYS.NATSTAT_SERVER_ACCESS })
            }
        }
        else if (isComboRelated){
            log.debug( 'settings for combo related org: consortia or collective')
            result.customerIdentifier = CustomerIdentifier.findAllByCustomer(org)
        }

//        result.allPlatforms = Platform.executeQuery('select p from Platform p join p.org o where p.org is not null order by o.name, o.sortname, p.name')
        result
    }

    @Secured(['ROLE_ORG_EDITOR','ROLE_ADMIN'])
    def list() {

        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.max  = params.max ? Long.parseLong(params.max) : result.user?.getDefaultPageSizeTMP()
        result.offset = params.offset ? Long.parseLong(params.offset) : 0
        params.sort = params.sort ?: " LOWER(o.shortname), LOWER(o.name)"

        result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')

        def fsq = filterService.getOrgQuery(params)
        result.filterSet = params.filterSet ? true : false

        List orgListTotal  = Org.findAll(fsq.query, fsq.queryParams)
        result.orgListTotal = orgListTotal.size()
        result.orgList = orgListTotal.drop((int) result.offset).take((int) result.max)

        SimpleDateFormat sdf = DateUtil.getSDF_NoTimeNoPoint()
        String datetoday = sdf.format(new Date(System.currentTimeMillis()))
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

    @DebugAnnotation(perm="ORG_CONSORTIUM", type="Consortium", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermTypeAffiliation("ORG_CONSORTIUM", "Consortium", "INST_USER")
    })
    Map listInstitution() {
        Map result = setResultGenericsAndCheckAccess(params)
        params.orgType   = OT_INSTITUTION.id.toString()
        params.orgSector = O_SECTOR_HIGHER_EDU.id.toString()
        if(!params.sort)
            params.sort = " LOWER(o.sortname)"
        def fsq = filterService.getOrgQuery(params)
        result.availableOrgs = Org.executeQuery(fsq.query, fsq.queryParams, params)
        result.consortiaMemberIds = []
        Combo.findAllWhere(
                toOrg: result.institution,
                type:    RefdataValue.getByValueAndCategory('Consortium', RDConstants.COMBO_TYPE)
        ).each { cmb ->
            result.consortiaMemberIds << cmb.fromOrg.id
        }

        result.consortiaMemberTotal = result.consortiaMemberIds.size()

        result
    }

    @Secured(['ROLE_USER'])
    def listProvider() {
        Map<String, Object> result = [:]
        result.propList    = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())
        result.user        = User.get(springSecurityService.principal.id)
        result.editable    = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR') || accessService.checkConstraint_ORG_COM_EDITOR()

        params.orgSector   = O_SECTOR_PUBLISHER?.id?.toString()
        params.orgType = OT_PROVIDER?.id?.toString()
        params.sort        = params.sort ?: " LOWER(o.shortname), LOWER(o.name)"

        def fsq            = filterService.getOrgQuery(params)
        result.filterSet = params.filterSet ? true : false

        if (params.filterPropDef) {
            def orgIdList = Org.executeQuery("select o.id ${fsq.query}", fsq.queryParams)
            fsq = filterService.getOrgQuery([constraint_orgIds: orgIdList] << params)
            fsq = propertyService.evalFilterQuery(params, fsq.query, 'o', fsq.queryParams)
        }
        result.max          = params.max ? Integer.parseInt(params.max) : result.user?.getDefaultPageSizeTMP()
        result.offset       = params.offset ? Integer.parseInt(params.offset) : 0
        List orgListTotal   = Org.findAll(fsq.query, fsq.queryParams)
        result.orgListTotal = orgListTotal.size()
        result.orgList      = orgListTotal.drop((int) result.offset).take((int) result.max)

        def message = g.message(code: 'export.all.providers')
        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
        String datetoday = sdf.format(new Date(System.currentTimeMillis()))
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
            }
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
    def createIdentifier(){
        log.debug("OrganisationController::createIdentifier ${params}")
        Org org   = params.id? Org.get(params.id) : null

        if (! org) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect(url: request.getHeader('referer'))
            return
        }
        //                List<IdentifierNamespace> nsList = IdentifierNamespace.where{(nsType == com.k_int.kbplus.Org.class.name || nsType == null)}
        List<IdentifierNamespace> nsList = IdentifierNamespace.where{(nsType == com.k_int.kbplus.Org.class.name)}
                .list(sort: 'ns')
                .sort { a, b ->
            String aVal = a.getI10n('name') ?: a.ns
            String bVal = b.getI10n('name') ?: b.ns
            aVal.compareToIgnoreCase bVal
        }
        .collect{ it }

        render template: '/templates/identifier/modal_create', model: [orgInstance: org, nsList: nsList]
    }

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
    def processCreateCustomerIdentifier(){
        log.debug("OrganisationController::processCreateCustomerIdentifier ${params}")
        Org org   = params.orgid ? Org.get(params.orgid) : null
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
                    value: params.value.trim(),
                    note: params.note?.trim(),
                    owner: contextService.getOrg(),
                    isPublic: true,
                    type: RefdataValue.getByValueAndCategory('Default', RDConstants.CUSTOMER_IDENTIFIER_TYPE)
            )
            ci.save()
        }

        redirect(url: request.getHeader('referer'))
    }

    def processEditIdentifier(){
        log.debug("OrganisationController::processEditIdentifier ${params}")
        Identifier identifier   = Identifier.get(params.identifierId)
        if ( ! identifier){
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'default.search.identifier'), params.identifierId])
            redirect(url: request.getHeader('referer'))
            return
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
        customeridentifier.note = params.note?.trim()
        customeridentifier.save()

        redirect(url: request.getHeader('referer'))
    }

    def createCustomerIdentifier(){
        log.debug("OrganisationController::createCustomerIdentifier ${params}")
        Org org   = Org.get(params.id)

        if (! org) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect(url: request.getHeader('referer'))
            return
        }
        List allPlatforms = Platform.executeQuery('select p from Platform p join p.org o where p.org is not null order by o.name, o.sortname, p.name')

        render template: '/templates/customerIdentifier/modal_create', model: [orgInstance: org, allPlatforms: allPlatforms]
    }

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

    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR'])
    def create() {
        switch (request.method) {
            case 'POST':
                Org orgInstance = new Org(params)
                orgInstance.status = O_STATUS_CURRENT

                //if (params.name) {
                    if (orgInstance.save(flush: true)) {
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

    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR'])
    def setupBasicTestData() {
        Org targetOrg = Org.get(params.id)
        if(organisationService.setupBasicTestData(targetOrg)) {
            flash.message = message(code:'org.setup.success')
        }
        else {
            flash.error = message(code:'org.setup.error',args: [organisationService.dumpErrors()])
        }
        redirect action: 'show', id: params.id
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN,ROLE_ORG_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN,ROLE_ORG_EDITOR")
    })
    def createProvider() {

        RefdataValue orgSector = RefdataValue.getByValueAndCategory('Publisher', RDConstants.ORG_SECTOR)
        RefdataValue orgType = RefdataValue.getByValueAndCategory('Provider', RDConstants.ORG_TYPE)
        RefdataValue orgType2 = RefdataValue.getByValueAndCategory('Agency', RDConstants.ORG_TYPE)
        Org orgInstance = new Org(name: params.provider, sector: orgSector.id)

        if ( orgInstance.save(flush:true) ) {

            orgInstance.addToOrgType(orgType)
            orgInstance.addToOrgType(orgType2)
            orgInstance.save(flush:true)

            flash.message = message(code: 'default.created.message', args: [message(code: 'org.label'), orgInstance.name])
            redirect action: 'show', id: orgInstance.id
        }
        else {
            log.error("Problem creating title: ${orgInstance.errors}");
            flash.message = message(code:'org.error.createProviderError',args:[orgInstance.errors])
            redirect ( action:'findProviderMatches' )
        }
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN,ROLE_ORG_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN,ROLE_ORG_EDITOR")
    })
    def findProviderMatches() {

        Map<String, Object> result = [:]
        if ( params.proposedProvider ) {

            result.providerMatches= Org.executeQuery("from Org as o where exists (select roletype from o.orgType as roletype where roletype = :provider ) and (lower(o.name) like :searchName or lower(o.shortname) like :searchName or lower(o.sortname) like :searchName ) ",
                    [provider: OT_PROVIDER, searchName: "%${params.proposedProvider.toLowerCase()}%"])
        }
        result
    }

    @DebugAnnotation(perm="ORG_INST_COLLECTIVE, ORG_CONSORTIUM", affil="INST_ADM",specRole="ROLE_ADMIN, ROLE_ORG_EDITOR")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_INST_COLLECTIVE, ORG_CONSORTIUM","INST_ADM","ROLE_ADMIN, ROLE_ORG_EDITOR") })
    def createMember() {
        Org contextOrg = contextService.org
        //new institution = consortia member, implies combo type consortium
        RefdataValue orgSector = O_SECTOR_HIGHER_EDU
        if(params.institution) {
            Org orgInstance

            try {
                if(accessService.checkPerm("ORG_CONSORTIUM")) {
                    // createdBy will set by Org.beforeInsert()
                    orgInstance = new Org(name: params.institution, sector: orgSector, status: O_STATUS_CURRENT)
                    orgInstance.save()

                    Combo newMember = new Combo(fromOrg:orgInstance,toOrg:contextOrg,type:COMBO_TYPE_CONSORTIUM)
                    newMember.save()

                    orgInstance.setDefaultCustomerType()
                    orgInstance.addToOrgType(RefdataValue.getByValueAndCategory('Institution', RDConstants.ORG_TYPE)) //RDStore adding causes a DuplicateKeyException
                }

                flash.message = message(code: 'default.created.message', args: [message(code: 'org.institution.label'), orgInstance.name])
                redirect action: 'show', id: orgInstance.id, params: [fromCreate: true]
            }
            catch (Exception e) {
                log.error("Problem creating institution")
                log.error(e.printStackTrace())

                flash.message = message(code: "org.error.createInstitutionError", args:[orgInstance ? orgInstance.errors : 'unbekannt'])

                redirect ( action:'findOrganisationMatches', params: params )
            }
        }
        //new department = institution member, implies combo type department
        else if(params.department) {
            Org deptInstance

            try {
                if(accessService.checkPerm("ORG_INST_COLLECTIVE")) {
                    // createdBy will set by Org.beforeInsert()
                    deptInstance = new Org(name: params.department, sector: orgSector, status: O_STATUS_CURRENT)
                    deptInstance.save()

                    Combo newMember = new Combo(fromOrg:deptInstance,toOrg:contextOrg,type:COMBO_TYPE_DEPARTMENT)
                    newMember.save()

                    deptInstance.setDefaultCustomerType()
                    deptInstance.addToOrgType(RefdataValue.getByValueAndCategory('Department', RDConstants.ORG_TYPE))
                }

                flash.message = message(code: 'default.created.message', args: [message(code: 'org.department.label'), deptInstance.name])
                redirect action: 'show', id: deptInstance.id, params: [fromCreate: true]
            }
            catch (Exception e) {
                log.error("Problem creating department")
                log.error(e.printStackTrace())

                flash.error = message(code: "org.error.createInstitutionError", args:[deptInstance ? deptInstance.errors : 'unbekannt'])

                redirect ( action:'findOrganisationMatches', params: params )
            }
        }
    }

    @DebugAnnotation(perm="ORG_INST_COLLECTIVE, ORG_CONSORTIUM", affil="INST_ADM",specRole="ROLE_ADMIN, ROLE_ORG_EDITOR")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_INST_COLLECTIVE, ORG_CONSORTIUM","INST_ADM","ROLE_ADMIN, ROLE_ORG_EDITOR") })
    Map findOrganisationMatches() {
        Map memberMap = [:]
        RefdataValue comboType

        if (accessService.checkPerm('ORG_CONSORTIUM')) {
            comboType = COMBO_TYPE_CONSORTIUM
        }
        else if (accessService.checkPerm('ORG_INST_COLLECTIVE')) {
            comboType = COMBO_TYPE_DEPARTMENT
        }

        Combo.findAllByType(comboType).each { lObj ->
            Combo link = (Combo) lObj
            List members = memberMap.get(link.fromOrg.id)
            if(!members) {
                members = [link.toOrg.id]
            } else {
                members << link.toOrg.id
            }
            memberMap.put(link.fromOrg.id,members)
        }

        Map result = [institution:contextService.org,organisationMatches:[],members:memberMap,comboType:comboType]
        //searching members for consortium, i.e. the context org is a consortium
        if (comboType == COMBO_TYPE_CONSORTIUM) {
            if (params.proposedOrganisation) {
                result.organisationMatches.addAll(Org.executeQuery("select o from Org as o where exists (select roletype from o.orgType as roletype where roletype = :institution ) and (lower(o.name) like :searchName or lower(o.shortname) like :searchName or lower(o.sortname) like :searchName) ",
                        [institution: OT_INSTITUTION, searchName: "%${params.proposedOrganisation.toLowerCase()}%"]))
            }
            if (params.proposedOrganisationID) {
                result.organisationMatches.addAll(Org.executeQuery("select id.org from Identifier id where lower(id.value) like :identifier and lower(id.ns.ns) in (:namespaces) ",
                        [identifier: "%${params.proposedOrganisationID.toLowerCase()}%",namespaces:["isil","wibid"]]))
            }
        }
        //searching departments of the institution, i.e. the context org is an institution
        else if (comboType == COMBO_TYPE_DEPARTMENT) {
            if (params.proposedOrganisation) {
                result.organisationMatches.addAll(Org.executeQuery("select c.fromOrg from Combo c join c.fromOrg o where c.toOrg = :contextOrg and c.type = :department and (lower(o.name) like :searchName or lower(o.shortname) like :searchName or lower(o.sortname) like :searchName)",
                        [department: comboType, contextOrg: contextService.org, searchName: "%${params.proposedOrganisation.toLowerCase()}%"]))
            }
        }

        result
    }

    @Secured(['ROLE_USER'])
    def show() {

        DebugUtil du = new DebugUtil()
        du.setBenchmark('this-n-that')

        Map result = setResultGenericsAndCheckAccess(params)
        if(!result) {
            response.sendError(401)
            return
        }

        //this is a flag to check whether the page has been called directly after creation
        result.fromCreate = params.fromCreate ? true : false

        du.setBenchmark('orgRoles')

        du.setBenchmark('editable')

        def orgSector = O_SECTOR_PUBLISHER
        def orgType = OT_PROVIDER

        //IF ORG is a Provider
        if(result.orgInstance.sector == orgSector || orgType?.id in result.orgInstance?.getallOrgTypeIds()) {
            du.setBenchmark('editable2')
            result.editable = accessService.checkMinUserOrgRole(result.user, result.orgInstance, 'INST_EDITOR') ||
                    accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN,ROLE_ORG_EDITOR")
        }
        else {
            du.setBenchmark('editable2')
            if(accessService.checkPerm("ORG_CONSORTIUM")) {
                List<Long> consortia = Combo.findAllByTypeAndFromOrg(COMBO_TYPE_CONSORTIUM,result.orgInstance).collect { it ->
                    it.toOrg.id
                }
                if(consortia.size() == 1 && consortia.contains(result.institution.id) && accessService.checkMinUserOrgRole(result.user,result.institution,'INST_EDITOR'))
                    result.editable = true
            }
            else if(accessService.checkPerm("ORG_INST_COLLECTIVE")) {
                List<Long> department = Combo.findAllByTypeAndFromOrg(COMBO_TYPE_DEPARTMENT,result.orgInstance).collect { it ->
                    it.toOrg.id
                }
                if (department.contains(result.institution.id) && accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR'))
                    result.editable = true
            }
            else
                result.editable = accessService.checkMinUserOrgRole(result.user, result.orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
        }
        
      if (!result.orgInstance) {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
        redirect action: 'list'
        return
      }

        du.setBenchmark('properties')

        result.authorizedOrgs = result.user?.authorizedOrgs

        // create mandatory OrgPrivateProperties if not existing

        List<PropertyDefinition> mandatories = PropertyDefinition.getAllByDescrAndMandatoryAndTenant(PropertyDefinition.ORG_PROP, true, result.institution)

        mandatories.each { pd ->
            if (!OrgPrivateProperty.findWhere(owner: result.orgInstance, type: pd)) {
                def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, result.orgInstance, pd)


                if (newProp.hasErrors()) {
                    log.error(newProp.errors)
                } else {
                    log.debug("New org private property created via mandatory: " + newProp.type.name)
                }
            }
        }

        du.setBenchmark('identifier')

        if(!Combo.findByFromOrgAndType(result.orgInstance,COMBO_TYPE_DEPARTMENT) && !(OT_PROVIDER.id in result.orgInstance.getallOrgTypeIds())){
            result.orgInstance.createCoreIdentifiersIfNotExist()
        }

        du.setBenchmark('createdBy and legallyObligedBy')

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
        List bm = du.stopBenchmark()
        result.benchMark = bm

        result
    }

    @Secured(['ROLE_USER'])
    def ids() {

        User user = User.get(springSecurityService.principal.id)
        Org org   = Org.get(params.id)
        DebugUtil du = new DebugUtil()
        du.setBenchmark('this-n-that')

        Map result = setResultGenericsAndCheckAccess(params)
        if(!result) {
            response.sendError(401)
            return
        }
        result.editable_identifier = result.editable

        //this is a flag to check whether the page has been called directly after creation
        result.fromCreate = params.fromCreate ? true : false

        du.setBenchmark('editable_identifier')


        def orgSector = O_SECTOR_PUBLISHER
        def orgType = OT_PROVIDER

        //IF ORG is a Provider
        if(result.orgInstance?.sector == orgSector || orgType?.id in result.orgInstance?.getallOrgTypeIds()) {
            du.setBenchmark('editable_identifier2')
            result.editable_identifier = accessService.checkMinUserOrgRole(result.user, result.orgInstance, 'INST_EDITOR') ||
                    accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN,ROLE_ORG_EDITOR")
        }
        else {
            du.setBenchmark('editable_identifier2')
            if(accessService.checkPerm("ORG_CONSORTIUM")) {
                List<Long> consortia = Combo.findAllByTypeAndFromOrg(COMBO_TYPE_CONSORTIUM,result.orgInstance).collect { it ->
                    it.toOrg.id
                }
                if(consortia.size() == 1 && consortia.contains(result.institution.id) && accessService.checkMinUserOrgRole(result.user,result.institution,'INST_EDITOR'))
                    result.editable_identifier = true
            }
            else if(accessService.checkPerm("ORG_INST_COLLECTIVE")) {
                List<Long> department = Combo.findAllByTypeAndFromOrg(COMBO_TYPE_DEPARTMENT,result.orgInstance).collect { it ->
                    it.toOrg.id
                }
                if (department.contains(result.institution.id) && accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR'))
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

        du.setBenchmark('create Identifiers if necessary')

        // TODO: experimental asynchronous task
        //waitAll(task_orgRoles, task_properties)

        if(!Combo.findByFromOrgAndType(result.orgInstance,COMBO_TYPE_DEPARTMENT) && !(OT_PROVIDER.id in result.orgInstance.getallOrgTypeIds())){
            result.orgInstance.createCoreIdentifiersIfNotExist()
        }

//------------------------orgSettings --------------------
        du.setBenchmark('orgsettings')
        Boolean inContextOrg = contextService.getOrg().id == org.id
        Boolean isComboRelated = Combo.findByFromOrgAndToOrg(org, contextService.getOrg())

        result.hasAccessToCustomeridentifier = (inContextOrg && accessService.checkMinUserOrgRole(user, org, 'INST_ADM')) ||
                (isComboRelated && accessService.checkMinUserOrgRole(user, contextService.getOrg(), 'INST_ADM')) ||
                SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')

        if (! result.hasAccessToCustomeridentifier) {

            result.user = user
            result.orgInstance = org

            result.inContextOrg = inContextOrg
            result.editable_customeridentifier =
                    SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR') ||
                            (inContextOrg && accessService.checkMinUserOrgRole(user, org, 'INST_ADM'))
            result.isComboRelated = isComboRelated

            // adding default settings
            organisationService.initMandatorySettings(org)

            // collecting visible settings by customer type, role and/or combo
            List<OrgSettings> allSettings = OrgSettings.findAllByOrg(org)

            List<OrgSettings.KEYS> ownerSet = [
                    OrgSettings.KEYS.API_LEVEL,
                    OrgSettings.KEYS.API_KEY,
                    OrgSettings.KEYS.API_PASSWORD,
                    OrgSettings.KEYS.CUSTOMER_TYPE,
                    OrgSettings.KEYS.GASCO_ENTRY
            ]
            List<OrgSettings.KEYS> accessSet = [
                    OrgSettings.KEYS.OAMONITOR_SERVER_ACCESS,
                    OrgSettings.KEYS.NATSTAT_SERVER_ACCESS
            ]
            List<OrgSettings.KEYS> credentialsSet = [
                    OrgSettings.KEYS.NATSTAT_SERVER_API_KEY,
                    OrgSettings.KEYS.NATSTAT_SERVER_REQUESTOR_ID
            ]

            result.settings = []

            if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')) {
                result.settings.addAll(allSettings.findAll { it.key in ownerSet })
                result.settings.addAll(allSettings.findAll { it.key in accessSet })
                result.settings.addAll(allSettings.findAll { it.key in credentialsSet })
                result.customerIdentifier = CustomerIdentifier.findAllByCustomer(org, [sort: 'platform'])
            } else if (inContextOrg) {
                log.debug('settings for own org')
                result.settings.addAll(allSettings.findAll { it.key in ownerSet })

                if (org.hasPerm('ORG_CONSORTIUM,ORG_INST')) {
                    result.settings.addAll(allSettings.findAll { it.key in accessSet })
                    result.settings.addAll(allSettings.findAll { it.key in credentialsSet })
                    result.customerIdentifier = CustomerIdentifier.findAllByCustomer(org, [sort: 'platform'])
                } else if (['ORG_BASIC_MEMBER'].contains(org.getCustomerType())) {
                    result.settings.addAll(allSettings.findAll { it.key == OrgSettings.KEYS.NATSTAT_SERVER_ACCESS })
                    result.customerIdentifier = CustomerIdentifier.findAllByCustomer(org, [sort: 'platform'])
                } else if (['FAKE'].contains(org.getCustomerType())) {
                    result.settings.addAll(allSettings.findAll { it.key == OrgSettings.KEYS.NATSTAT_SERVER_ACCESS })
                }
            } else if (isComboRelated) {
                log.debug('settings for combo related org: consortia or collective')
                result.customerIdentifier = CustomerIdentifier.findAllByCustomer(org, [sort: 'platform'])
            }
        }
        List bm = du.stopBenchmark()
        result.benchMark = bm
        result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def documents() {
        Map result = setResultGenericsAndCheckAccess(params)
        if(!result) {
            response.sendError(401)
            return
        }
        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def editDocument() {
        Map result = setResultGenericsAndCheckAccess(params)
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

    @DebugAnnotation(perm="FAKE,ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_ADM", specRole="ROLE_ADMIN,ROLE_ORG_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("FAKE,ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_ADM", "ROLE_ADMIN,ROLE_ORG_EDITOR")
    })
    def deleteCustomerIdentifier() {
        log.debug("OrganisationController::deleteIdentifier ${params}");
        CustomerIdentifier ci = genericOIDService.resolveOID(params.deleteCI)
        if (ci && ci.owner == contextService.org) {
            ci.delete()
        }
        redirect action: 'ids', id: contextService.org.id
        return
    }

    @Secured(['ROLE_USER'])
    def deleteIdentifier() {
        log.debug("OrganisationController::deleteIdentifier ${params}")
        def owner = genericOIDService.resolveOID(params.owner)
        def target = genericOIDService.resolveOID(params.target)

        log.debug('owner: ' + owner)
        log.debug('target: ' + target)

        if (owner && target) {
            if (target."${Identifier.getAttributeName(owner)}"?.id == owner.id) {
                log.debug("Identifier deleted: ${params}")
                target.delete()
            }
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def deleteDocuments() {
        log.debug("deleteDocuments ${params}");

        docstoreService.unifiedDeleteDocuments(params)

        redirect controller: 'organisation', action: 'documents', id: params.instanceId /*, fragment: 'docstab' */
    }

    @Deprecated
    @Secured(['ROLE_ADMIN'])
    def properties() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        Org orgInstance = Org.get(params.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        if (!orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect action: 'list'
            return
        }

        // create mandatory OrgPrivateProperties if not existing

        def mandatories = []
        result.user?.authorizedOrgs?.each{ org ->
            List<PropertyDefinition> ppd = PropertyDefinition.getAllByDescrAndMandatoryAndTenant(PropertyDefinition.ORG_PROP, true, org)
            if(ppd){
                mandatories << ppd
            }
        }
        mandatories.flatten().each{ pd ->
            if (! OrgPrivateProperty.findWhere(owner: orgInstance, type: pd)) {
                def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, orgInstance, pd)

                if (newProp.hasErrors()) {
                    log.error(newProp.errors)
                } else {
                    log.debug("New org private property created via mandatory: " + newProp.type.name)
                }
            }
        }

        result.orgInstance = orgInstance
        result.authorizedOrgs = result.user?.authorizedOrgs
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def users() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        Org orgInstance = Org.get(params.id)

        if (! orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect action: 'list'
            return
        }

        result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') || accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_ADM')

        if (! result.editable) {
            boolean instAdminExists = orgInstance.getAllValidInstAdmins().size() > 0
            boolean comboCheck = instAdmService.hasInstAdmPivileges(result.user, orgInstance, [COMBO_TYPE_DEPARTMENT, COMBO_TYPE_CONSORTIUM])

            result.editable = comboCheck && ! instAdminExists
        }

        if (! result.editable) {
            redirect controller: 'organisation', action: 'show', id: orgInstance.id
        }

        result.pendingRequests = UserOrg.findAllByStatusAndOrg(UserOrg.STATUS_PENDING, orgInstance, [sort:'dateRequested', order:'desc'])
        result.orgInstance = orgInstance

        Map filterParams = params
        filterParams.status = UserOrg.STATUS_APPROVED
        filterParams.org = orgInstance

        result.users = userService.getUserSet(filterParams)
        result.breadcrumb = 'breadcrumb'
        result.titleMessage = "${orgInstance.name} - ${message(code:'org.nav.users')}"
        result.inContextOrg = false
        result.navPath = "nav"
        result.navConfiguration = [orgInstance: orgInstance, inContextOrg: false]
        result.multipleAffiliationsWarning = true
        Set<Org> availableComboOrgs = Org.executeQuery('select c.fromOrg from Combo c where c.toOrg = :ctxOrg order by c.fromOrg.name asc', [ctxOrg:orgInstance])
        availableComboOrgs.add(orgInstance)
        result.filterConfig = [filterableRoles:Role.findAllByRoleType('user'), orgField: false]
        result.tableConfig = [editable: result.editable, editor: result.user, editLink: 'userEdit', users: result.users, showAllAffiliations: false, modifyAccountEnability: SpringSecurityUtils.ifAllGranted('ROLE_YODA'), availableComboOrgs: availableComboOrgs]
        result.total = result.users.size()
        render view: '/templates/user/_list', model: result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_ADM", "ROLE_ADMIN") })
    def userEdit() {
        Map result = [user: User.get(params.id), editor: contextService.user, editable: true, orgInstance: contextService.org, manipulateAffiliations: false]

        render view: '/templates/user/_edit', model: result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_ADM", "ROLE_ADMIN") })
    def userCreate() {
        Map result = setResultGenericsAndCheckAccess(params)
        result.availableOrgs = Org.get(params.id)
        result.availableOrgRoles = Role.findAllByRoleType('user')
        result.editor = result.user
        result.breadcrumb = 'breadcrumb'

        render view: '/templates/user/_create', model: result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_INST_COLLECTIVE,ORG_CONSORTIUM","INST_ADM","ROLE_ADMIN") })
    def processUserCreate() {
        def success = userService.addNewUser(params, flash)
        //despite IntelliJ's warnings, success may be an array other than the boolean true
        if(success instanceof User) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'user.label'), success.id])
            redirect action: 'userEdit', id: success.id
        }
        else if(success instanceof List) {
            flash.error = success.join('<br>')
            redirect action: 'userCreate'
        }
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_INST_COLLECTIVE,ORG_CONSORTIUM","INST_ADM","ROLE_ADMIN") })
    def addAffiliation() {
        Map result = userService.setResultGenerics(params)
        if (! result.editable) {
            flash.error = message(code: 'default.noPermissions')
            redirect action: 'userEdit', id: params.id
            return
        }
        userService.addAffiliation(result.user,params.org,params.formalRole,flash)
        redirect action: 'userEdit', id: params.id
    }

    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR'])
    def edit() {
        redirect controller: 'organisation', action: 'show', params: params
        return
    }

    @Secured(['ROLE_ADMIN'])
    def _delete() {
        Map<String, Object> result = [:]

        result.editable = SpringSecurityUtils.ifAnyGranted("ROLE_ORG_EDITOR,ROLE_ADMIN")
        result.orgInstance = Org.get(params.id)

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

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def processAffiliation() {
      Map<String, Object> result = [:]
      result.user = User.get(springSecurityService.principal.id)
      UserOrg uo = UserOrg.get(params.assoc)

      if (instAdmService.hasInstAdmPivileges(result.user, Org.get(params.id), [COMBO_TYPE_DEPARTMENT, COMBO_TYPE_CONSORTIUM])) {

          if (params.cmd == 'approve') {
              uo.status = UserOrg.STATUS_APPROVED
              uo.dateActioned = System.currentTimeMillis()
              uo.save(flush: true)
          }
          else if (params.cmd == 'reject') {
              uo.status = UserOrg.STATUS_REJECTED
              uo.dateActioned = System.currentTimeMillis()
              uo.save(flush: true)
          }
          else if (params.cmd == 'delete') {
              uo.delete(flush: true)
          }
      }
      redirect action: 'users', id: params.id
    }

    @Secured(['ROLE_USER'])
    def addOrgCombo(Org fromOrg, Org toOrg) {
      //def comboType = RefdataCategory.lookupOrCreate(RDConstants.ORGANISATIONAL_ROLE, 'Package Consortia')
        RefdataValue comboType = RefdataValue.get(params.comboTypeTo)
      log.debug("Processing combo creation between ${fromOrg} AND ${toOrg} with type ${comboType}")
      def dupe = Combo.executeQuery("from Combo as c where c.fromOrg = ? and c.toOrg = ?", [fromOrg, toOrg])
      
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

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def addressbook() {
        Map result = setResultGenericsAndCheckAccess(params)
        if(!result) {
            response.sendError(401)
            return
        }

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        if (! result.institution) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect action: 'list'
            return
        }

        List visiblePersons = addressbookService.getAllVisiblePersons(result.user, result.orgInstance)

        result.propList =
                PropertyDefinition.findAllWhere(
                        descr: PropertyDefinition.PRS_PROP,
                        tenant: contextService.getOrg() // private properties
                )


        result.visiblePersons = visiblePersons

        result
    }
    @DebugAnnotation(test = 'checkForeignOrgComboPermAffiliation()')
    @Secured(closure = {
        ctx.accessService.checkForeignOrgComboPermAffiliationX([
                org: Org.get(request.getRequestURI().split('/').last()),
                affiliation: "INST_USER",
                comboPerm: "ORG_CONSORTIUM",
                comboAffiliation: "INST_EDITOR",
                specRoles: "ROLE_ADMIN,ROLE_ORG_EDITOR"
                ])
    })
    def readerNumber() {
        Map result = setResultGenericsAndCheckAccess(params)
        if(!result) {
            response.sendError(401)
            return
        }
        result.editable = accessService.checkMinUserOrgRole(result.user, result.orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')

        params.sort = params.sort ?: 'dueDate'
        params.order = params.order ?: 'desc'

        result.numbersInstanceList = ReaderNumber.findAllByOrg(result.orgInstance, params)

        result
    }

    @DebugAnnotation(test = 'checkForeignOrgComboPermAffiliation()')
    @Secured(closure = {
        ctx.accessService.checkForeignOrgComboPermAffiliationX([
                org: Org.get(request.getRequestURI().split('/').last()),
                affiliation: "INST_USER",
                comboPerm: "ORG_CONSORTIUM",
                comboAffiliation: "INST_EDITOR",
                specRoles: "ROLE_ADMIN"
        ])
    })
    def accessPoints() {
        Map result = setResultGenericsAndCheckAccess(params)
        if(!result) {
            response.sendError(401)
            return
        }
        result.editable = accessService.checkMinUserOrgRole(result.user, result.orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAllGranted('ROLE_ADMIN')

        if (! result.orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect action: 'list'
            return
        }

        def orgAccessPointList = accessPointService.getOapListWithLinkCounts(result.orgInstance)
        result.orgAccessPointList = orgAccessPointList

        result
    }

    def addOrgType()
    {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        Org orgInstance = Org.get(params.org)

        if (!orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect action: 'list'
            return
        }

        if ( SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR') ) {
            result.editable = true
        }
        else {
            result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_ADM')
        }

        if(result.editable)
        {
            orgInstance.addToOrgType(RefdataValue.get(params.orgType))
            orgInstance.save(flush: true)
//            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label'), orgInstance.name])
            redirect action: 'show', id: orgInstance.id
        }
    }
    def deleteOrgType()
    {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        Org orgInstance = Org.get(params.org)

        if (!orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect action: 'list'
            return
        }

        if ( SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR') ) {
            result.editable = true
        }
        else {
            result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_ADM')
        }

        if(result.editable)
        {
            orgInstance.removeFromOrgType(RefdataValue.get(params.removeOrgType))
            orgInstance.save(flush: true)
//            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label'), orgInstance.name])
            redirect action: 'show', id: orgInstance.id
        }
    }
    def addSubjectGroup() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        Org orgInstance = Org.get(params.org)

        if (!orgInstance) {
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
        if (orgInstance.getSubjectGroup().find { it.subjectGroupId == newSubjectGroup.id }) {
            flash.message = message(code: 'default.err.alreadyExist', args: [message(code: 'org.subjectGroup.label')])
            redirect(url: request.getHeader('referer'))
            return
        }
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')) {
            result.editable = true
        } else {
            result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_ADM')
        }

        if (result.editable){
            orgInstance.addToSubjectGroup(subjectGroup: RefdataValue.get(params.subjectGroup))
            orgInstance.save(flush: true)
//            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label'), orgInstance.name])
            redirect action: 'show', id: orgInstance.id
        }
    }

    def deleteSubjectGroup() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        Org orgInstance = Org.get(params.org)

        if (!orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label'), params.id])
            redirect(url: request.getHeader('referer'))
            return
        }
        if ( SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR') ) {
            result.editable = true
        } else {
            result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_ADM')
        }

        if(result.editable) {
            def osg = OrgSubjectGroup.get(params.removeOrgSubjectGroup)
            orgInstance.removeFromSubjectGroup(osg)
            orgInstance.save()
            osg.delete()
//            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label'), orgInstance.name])
            redirect(url: request.getHeader('referer'))
        }
    }

    private Map setResultGenericsAndCheckAccess(params) {
        User user = User.get(springSecurityService.principal.id)
        Org org = contextService.org
        Map result = [user:user,institution:org,editable:accessService.checkMinUserOrgRole(user,org,'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ORG_EDITOR,ROLE_ADMIN'),inContextOrg:true,institutionalView:false,departmentalView:false]
        if(params.id) {
            result.orgInstance = Org.get(params.id)
            result.inContextOrg = result.orgInstance?.id == org.id
            //this is a flag to check whether the page has been called for a consortia or inner-organisation member
            Combo checkCombo = Combo.findByFromOrgAndToOrg(result.orgInstance,org)
            if(checkCombo) {
                if(checkCombo.type == COMBO_TYPE_CONSORTIUM)
                    result.institutionalView = true
                else if(checkCombo.type == COMBO_TYPE_DEPARTMENT)
                    result.departmentalView = true
            }
            //restrictions hold if viewed org is not the context org
            if(!result.inContextOrg && !accessService.checkPerm("ORG_CONSORTIUM") && !SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_ORG_EDITOR")) {
                //restrictions further concern only single users, not consortia
                if(accessService.checkPerm("ORG_INST") && !result.departmentalView) {
                    if(result.orgInstance.hasPerm("ORG_INST,ORG_INST_COLLECTIVE")) {
                        return null
                    }
                    else if(accessService.checkPerm("ORG_INST_COLLECTIVE") && Combo.findByFromOrgAndType(result.orgInstance,COMBO_TYPE_DEPARTMENT)) {
                        return null
                    }
                }
            }
        }
        result
    }

    @DebugAnnotation(perm="ORG_CONSORTIUM", type="Consortium", affil="INST_EDITOR", specRole="ROLE_ORG_EDITOR")
    @Secured(closure = { ctx.accessService.checkPermTypeAffiliationX("ORG_CONSORTIUM", "Consortium", "INST_EDITOR", "ROLE_ORG_EDITOR") })
    def toggleCombo() {
        Map result = setResultGenericsAndCheckAccess(params)
        if(!result) {
            response.sendError(401)
            return
        }
        if(!params.direction) {
            flash.error(message(code:'org.error.noToggleDirection'))
            response.sendError(404)
            return
        }
        switch(params.direction) {
            case 'add':
                Map map = [toOrg: result.institution,
                        fromOrg: Org.get(params.fromOrg),
                        type: RefdataValue.getByValueAndCategory('Consortium', RDConstants.COMBO_TYPE)]
                if (! Combo.findWhere(map)) {
                    Combo cmb = new Combo(map)
                    cmb.save()
                }
                break
            case 'remove':

                def subs = Subscription.executeQuery("from Subscription as s where exists ( select o from s.orgRelations as o where o.org in (:orgs) )", [orgs: [result.institution, Org.get(params.fromOrg)]])
                if(subs){
                    flash.error = message(code:'org.consortiaToggle.remove.notPossible.sub')
                }
                def lics = License.executeQuery("from License as l where exists ( select o from l.orgLinks as o where o.org in (:orgs) )", [orgs: [result.institution, Org.get(params.fromOrg)]])
                if(lics){
                    flash.error = message(code:'org.consortiaToggle.remove.notPossible.sub')
                }

                if(!subs && !lics) {

                    Combo cmb = Combo.findWhere(toOrg: result.institution,
                            fromOrg: Org.get(params.fromOrg),
                            type: RefdataValue.getByValueAndCategory('Consortium', RDConstants.COMBO_TYPE))
                    cmb.delete()
                }
                break
        }
        redirect action: 'listInstitution'
    }
}
