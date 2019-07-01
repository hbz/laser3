package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import de.laser.helper.DebugUtil
import de.laser.helper.RDStore
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.springframework.dao.DataIntegrityViolationException
import grails.plugin.springsecurity.annotation.Secured
import com.k_int.kbplus.auth.*;
import grails.plugin.springsecurity.SpringSecurityUtils
import com.k_int.properties.*

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat

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

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_ORG_EDITOR','ROLE_ADMIN'])
    def index() {
        redirect action: 'list', params: params
    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_ADM", specRole="ROLE_ADMIN,ROLE_ORG_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_ADM", "ROLE_ADMIN,ROLE_ORG_EDITOR")
    })
    def settings() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.orgInstance = Org.get(params.id)

        if (! result.orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
            redirect action: 'list'
            return
        }

        result.editable = accessService.checkMinUserOrgRole(result.user, result.orgInstance, 'INST_ADM') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')

        // forbidden access
        if (! result.editable && result.orgInstance.id != contextService.getOrg().id) {
            redirect controller: 'organisation', action: 'show', id: result.orgInstance.id

        }

        // adding default settings
        if (OrgSettings.get(result.orgInstance, OrgSettings.KEYS.STATISTICS_SERVER_ACCESS) == OrgSettings.SETTING_NOT_FOUND) {
            OrgSettings.add(
                    result.orgInstance,
                    OrgSettings.KEYS.STATISTICS_SERVER_ACCESS,
                    RefdataValue.getByValueAndCategory('No', 'YN')
            )
        }
        if (OrgSettings.get(result.orgInstance, OrgSettings.KEYS.OA2020_SERVER_ACCESS) == OrgSettings.SETTING_NOT_FOUND) {
            OrgSettings.add(
                    result.orgInstance,
                    OrgSettings.KEYS.OA2020_SERVER_ACCESS,
                    RefdataValue.getByValueAndCategory('No', 'YN')
            )
        }

        result.settings = OrgSettings.findAllByOrg(result.orgInstance)

        result
    }

    @Secured(['ROLE_ORG_EDITOR','ROLE_ADMIN'])
    def list() {

        def result = [:]
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

        SimpleDateFormat sdf = new SimpleDateFormat(g.message(code:'default.date.format.notimenopoint'))
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

    @DebugAnnotation(perm="ORG_CONSORTIUM", type="Consortium", affil="INST_USER", specRole="ROLE_ADMIN,ROLE_ORG_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermTypeAffiliationX("ORG_CONSORTIUM", "Consortium", "INST_USER", "ROLE_ADMIN,ROLE_ORG_EDITOR")
    })
    Map listInstitution() {
        Map result = setResultGenerics()
        if(!result.institution.getallOrgTypeIds().contains(RDStore.OT_CONSORTIUM.id)) {
            flash.error = message(code:'org.error.noConsortium')
            response.sendError(401)
            return
        }
        params.orgType   = RDStore.OT_INSTITUTION.id.toString()
        params.orgSector = RDStore.O_SECTOR_HIGHER_EDU.id.toString()
        if(!params.sort)
            params.sort = " LOWER(o.sortname)"
        def fsq = filterService.getOrgQuery(params)
        result.availableOrgs = Org.executeQuery(fsq.query, fsq.queryParams, params)
        result.consortiaMemberIds = []
        Combo.findAllWhere(
                toOrg: result.institution,
                type:    RefdataValue.getByValueAndCategory('Consortium','Combo Type')
        ).each { cmb ->
            result.consortiaMemberIds << cmb.fromOrg.id
        }
        result
    }

    @Secured(['ROLE_USER'])
    def listProvider() {
        def result = [:]
        result.propList    = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())
        result.user        = User.get(springSecurityService.principal.id)
        result.editable    = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR') || accessService.checkConstraint_ORG_COM_EDITOR()

        params.orgSector   = RDStore.O_SECTOR_PUBLISHER?.id?.toString()
        params.orgType = RDStore.OT_PROVIDER?.id?.toString()
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
        SimpleDateFormat sdf = new SimpleDateFormat(g.message(code:'default.date.format.notime', default:'yyyy-MM-dd'))
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

    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR'])
    def create() {
        switch (request.method) {
            case 'POST':
                def orgInstance = new Org(params)

                if (params.name) {
                    if (orgInstance.save(flush: true)) {
                        orgInstance.setDefaultCustomerType()

                        flash.message = message(code: 'default.created.message', args: [message(code: 'org.label', default: 'Org'), orgInstance.id])
                        redirect action: 'show', id: orgInstance.id
                        return
                    }
                }

                render view: 'create', model: [orgInstance: orgInstance]
                break
        }
    }

    @Secured(['ROLE_DATAMANAGER','ROLE_ORG_EDITOR'])
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

        def orgSector = RefdataValue.getByValueAndCategory('Publisher','OrgSector')
        def orgType = RefdataValue.getByValueAndCategory('Provider','OrgRoleType')
        def orgType2 = RefdataValue.getByValueAndCategory('Agency','OrgRoleType')
        def orgInstance = new Org(name: params.provider, sector: orgSector.id)

        if ( orgInstance.save(flush:true) ) {

            orgInstance.addToOrgType(orgType)
            orgInstance.addToOrgType(orgType2)
            orgInstance.save(flush:true)

            flash.message = message(code: 'default.created.message', args: [message(code: 'org.label', default: 'Org'), orgInstance.id])
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

        def result=[:]
        if ( params.proposedProvider ) {

            result.providerMatches= Org.executeQuery("from Org as o where exists (select roletype from o.orgType as roletype where roletype = :provider ) and (lower(o.name) like :searchName or lower(o.shortname) like :searchName or lower(o.sortname) like :searchName ) ",
                    [provider: RDStore.OT_PROVIDER, searchName: "%${params.proposedProvider.toLowerCase()}%"])
        }
        result
    }

    @DebugAnnotation(perm="ORG_INST_COLLECTIVE, ORG_CONSORTIUM", affil="INST_ADM",specRole="ROLE_ADMIN, ROLE_ORG_EDITOR")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_INST_COLLECTIVE, ORG_CONSORTIUM","INST_ADM","ROLE_ADMIN, ROLE_ORG_EDITOR") })
    def createMember() {
        Org contextOrg = contextService.org
        //new institution = consortia member, implies combo type consortium
        if(params.institution) {
            RefdataValue orgSector = RefdataValue.getByValueAndCategory('Higher Education','OrgSector')
            Org orgInstance = new Org(name: params.institution, sector: orgSector)
            orgInstance.addToOrgType(RefdataValue.getByValueAndCategory('Institution','OrgRoleType'))
            try {
                orgInstance.save(flush:true)
                if(RDStore.OT_CONSORTIUM.id in contextOrg.getallOrgTypeIds()) {
                    Combo newMember = new Combo(fromOrg:orgInstance,toOrg:contextOrg,type:RDStore.COMBO_TYPE_CONSORTIUM)
                    newMember.save(flush:true)
                }
                orgInstance.setDefaultCustomerType()

                flash.message = message(code: 'default.created.message', args: [message(code: 'org.institution.label'), orgInstance.id])
                redirect action: 'show', id: orgInstance.id, params: [fromCreate: true]
            }
            catch (Exception e) {
                log.error("Problem creating institution: ${orgInstance.errors}")
                log.error(e.printStackTrace())
                flash.message = message(code: "org.error.createInstitutionError",args:[orgInstance.errors])
                redirect ( action:'findOrganisationMatches', params: params )
            }
        }
        //new department = institution member, implies combo type department
        else if(params.department) {
            Org deptInstance = new Org(name: params.department)
            deptInstance.addToOrgType(RefdataValue.getByValueAndCategory('Department','OrgRoleType'))
            try {
                deptInstance.save(flush:true)
                if(RDStore.OT_INSTITUTION.id in (contextOrg.getallOrgTypeIds())) {
                    Combo newMember = new Combo(fromOrg:deptInstance,toOrg:contextOrg,type:RDStore.COMBO_TYPE_DEPARTMENT)
                    newMember.save()
                }

                flash.message = message(code: 'default.created.message', args: [message(code: 'org.department.label'), deptInstance.id])
                redirect action: 'show', id: deptInstance.id, params: [fromCreate: true]
            }
            catch (Exception e) {
                log.error("Problem creating department: ${deptInstance.errors}")
                log.error(e.printStackTrace())
                flash.error = message(code: "org.error.createInstitutionError",args:[deptInstance.errors])
                redirect ( action:'findOrganisationMatches', params: params )
            }
        }
    }

    @DebugAnnotation(perm="ORG_INST_COLLECTIVE, ORG_CONSORTIUM", affil="INST_ADM",specRole="ROLE_ADMIN, ROLE_ORG_EDITOR")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_INST_COLLECTIVE, ORG_CONSORTIUM","INST_ADM","ROLE_ADMIN, ROLE_ORG_EDITOR") })
    Map findOrganisationMatches() {
        Map memberMap = [:]
        RefdataValue comboType
        if(accessService.checkPerm('ORG_CONSORTIUM'))
            comboType = RDStore.COMBO_TYPE_CONSORTIUM
        else if(accessService.checkPerm('ORG_INST_COLLECTIVE'))
            comboType = RDStore.COMBO_TYPE_DEPARTMENT
        Combo.findAllByType(comboType).each { lObj ->
            Combo link = (Combo) lObj
            List members = memberMap.get(link.fromOrg.id)
            if(!members)
                members = [link.toOrg.id]
            else members << link.toOrg.id
            memberMap.put(link.fromOrg.id,members)
        }
        Map result=[organisationMatches:[],members:memberMap,comboType:comboType]
        //searching members for consortium, i.e. the context org is a consortium
        if(comboType == RDStore.COMBO_TYPE_CONSORTIUM) {
            if ( params.proposedOrganisation ) {
                result.organisationMatches.addAll(Org.executeQuery("select o from Org as o where exists (select roletype from o.orgType as roletype where roletype = :institution ) and (lower(o.name) like :searchName or lower(o.shortname) like :searchName or lower(o.sortname) like :searchName) ",
                        [institution: RDStore.OT_INSTITUTION, searchName: "%${params.proposedOrganisation.toLowerCase()}%"]))
            }
            if (params.proposedOrganisationID) {
                result.organisationMatches.addAll(Org.executeQuery("select id.org from IdentifierOccurrence id where lower(id.identifier.value) like :identifier and lower(id.identifier.ns.ns) in (:namespaces) ",
                        [identifier: "%${params.proposedOrganisationID.toLowerCase()}%",namespaces:["isil","wibid"]]))
            }
        }
        //searching departments of the institution, i.e. the context org is an institution
        else if(comboType == RDStore.COMBO_TYPE_DEPARTMENT) {
            if(params.proposedOrganisation) {
                result.organisationMatches.addAll(Org.executeQuery("select c.fromOrg from Combo c join c.fromOrg o where c.toOrg = :contextOrg and c.type = :department and (lower(o.name) like :searchName or lower(o.shortname) like :searchName or lower(o.sortname) like :searchName)",
                        [department: comboType, contextOrg: contextService.org, searchName: "%${params.proposedOrganisation.toLowerCase()}%"]))
            }
        }
        result
    }

    @Secured(['ROLE_USER'])
    def show() {

        def result = [:]

        DebugUtil du = new DebugUtil()
        du.setBenchMark('this-n-that')

        def orgInstance = Org.get(params.id)
        def user = contextService.getUser()
        def org = contextService.getOrg()

        //this is a flag to check whether the page has been called for a consortia or inner-organisation member
        Combo checkCombo = Combo.findByFromOrgAndToOrg(orgInstance,org)
        if(checkCombo) {
            if(checkCombo.type == RDStore.COMBO_TYPE_CONSORTIUM)
                result.institutionalView = true
            else if(checkCombo.type == RDStore.COMBO_TYPE_DEPARTMENT)
                result.departmentalView = true
        }
        //this is a flag to check whether the page has been called directly after creation
        result.fromCreate = params.fromCreate ? true : false

        //def link_vals = RefdataCategory.getAllRefdataValues("Organisational Role")
        //def sorted_links = [:]
        //def offsets = [:]

        du.setBenchMark('orgRoles')

        // TODO: experimental asynchronous task
        /*def task_orgRoles = task {

            if (SpringSecurityUtils.ifAnyGranted("ROLE_YODA") ||
                    (orgInstance.id == org.id && user.hasAffiliation('INST_ADM'))
            ) {

                link_vals.each { lv ->
                    def param_offset = 0

                    if (lv.id) {
                        def cur_param = "rdvl_${String.valueOf(lv.id)}"

                        if (params[cur_param]) {
                            param_offset = params[cur_param]
                            result[cur_param] = param_offset
                        }

                        def links = OrgRole.findAll {
                            org == orgInstance && roleType == lv
                        }
                        links = links.findAll { it2 -> it2.ownerStatus?.value != 'Deleted' }

                        def link_type_results = links.drop(param_offset.toInteger()).take(10) // drop from head, take 10

                        if (link_type_results) {
                            sorted_links["${String.valueOf(lv.id)}"] = [rdv: lv, rdvl: cur_param, links: link_type_results, total: links.size()]
                        }
                    } else {
                        log.debug("Could not read Refdata: ${lv}")
                    }
                }
            }
        }*/

        /*if (params.ajax) {
            render template: '/templates/links/orgRoleContainer', model: [listOfLinks: sorted_links, orgInstance: orgInstance]
            return
        }*/

        du.setBenchMark('editable')

        //result.sorted_links = sorted_links

        result.user = user
        result.orgInstance = orgInstance
        result.contextOrg = org

        def orgSector = RefdataValue.getByValueAndCategory('Publisher','OrgSector')
        def orgType = RefdataValue.getByValueAndCategory('Provider','OrgRoleType')

        //IF ORG is a Provider
        if(orgInstance.sector == orgSector || orgType?.id in orgInstance?.getallOrgTypeIds())
        {
            du.setBenchMark('editable2')
            result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_EDITOR') ||
                    accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN,ROLE_ORG_EDITOR")
        }
        else {
            du.setBenchMark('editable2')
            List<Long> consortia = Combo.findAllByTypeAndFromOrg(RefdataValue.getByValueAndCategory('Consortium','Combo Type'),orgInstance).collect { it ->
                it.toOrg.id
            }
            if(RDStore.OT_CONSORTIUM.id in org.getallOrgTypeIds() && consortia.size() == 1 && consortia.contains(org.id) && accessService.checkMinUserOrgRole(result.user,org,'INST_EDITOR'))
                result.editable = true
            else
                result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
        }
        
      if (! orgInstance) {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
        redirect action: 'list'
        return
      }

        du.setBenchMark('properties')

        // TODO: experimental asynchronous task
        //def task_properties = task {

            // -- private properties

            result.authorizedOrgs = result.user?.authorizedOrgs
            result.contextOrg = contextService.getOrg()

            // create mandatory OrgPrivateProperties if not existing

            def mandatories = PropertyDefinition.findAllByDescrAndMandatoryAndTenant("Organisation Property", true, result.contextOrg)

            mandatories.each { pd ->
                if (!OrgPrivateProperty.findWhere(owner: orgInstance, type: pd)) {
                    def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, orgInstance, pd)


                    if (newProp.hasErrors()) {
                        log.error(newProp.errors)
                    } else {
                        log.debug("New org private property created via mandatory: " + newProp.type.name)
                    }
                }
            }

            // -- private properties
       //}

        //documents
        //du.setBenchMark('documents')

        List bm = du.stopBenchMark()
        result.benchMark = bm

        // TODO: experimental asynchronous task
        //waitAll(task_orgRoles, task_properties)

        if(RDStore.OT_CONSORTIUM.id in orgInstance.getallOrgTypeIds() || RDStore.OT_INSTITUTION.id in orgInstance.getallOrgTypeIds()){

            def foundIsil = false
            def foundWibid = false
            def foundEZB = false

            orgInstance.ids.each {io ->
                if(io?.identifier?.ns?.ns == 'ISIL')
                {
                    foundIsil = true
                }
                if(io?.identifier?.ns?.ns == 'wibid')
                {
                    foundWibid = true
                }
                if(io?.identifier?.ns?.ns == 'ezb')
                {
                    foundEZB = true
                }
            }

            if(!foundIsil) {
                orgInstance.addOnlySpecialIdentifiers('ISIL', 'Unknown')


            }
            if(!foundWibid) {
                orgInstance.addOnlySpecialIdentifiers('wibid', 'Unknown')


            }
            if(!foundEZB) {
                orgInstance.addOnlySpecialIdentifiers('ezb', 'Unknown')


            }

            result.orgInstance = Org.get(orgInstance.id).refresh()

        }


        result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def documents() {
        Map result = setResultGenerics()
        result.orgInstance = Org.get(params.id)
        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def editDocument() {
        Map result = setResultGenerics()
        result.ownobj = result.institution
        result.owntp = 'organisation'
        if(params.id) {
            result.docctx = DocContext.get(params.id)
            result.doc = result.docctx.owner
        }

        render template: "/templates/documents/modal", model: result
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
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        def orgInstance = Org.get(params.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        if (!orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
            redirect action: 'list'
            return
        }

        // create mandatory OrgPrivateProperties if not existing

        def mandatories = []
        result.user?.authorizedOrgs?.each{ org ->
            def ppd = PropertyDefinition.findAllByDescrAndMandatoryAndTenant("Organisation Property", true, org)
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
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        def orgInstance = Org.get(params.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_ADM') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
        result.editable = result.editable || instAdmService.hasInstAdmPivileges(result.user, orgInstance)

        // forbidden access
        if (! result.editable && orgInstance.id != contextService.getOrg().id) {
            redirect controller: 'organisation', action: 'show', id: orgInstance.id

        }

      if (!orgInstance) {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
        redirect action: 'list'
        return
      }

        result.pendingRequests = UserOrg.findAllByStatusAndOrg(UserOrg.STATUS_PENDING, orgInstance, [sort:'dateRequested', order:'desc'])
        //result.users = UserOrg.findAllByStatusAndOrg(UserOrg.STATUS_APPROVED, orgInstance, [sort:'user.username', order: 'asc'])

        List baseQuery  = ['select distinct uo from UserOrg uo, User u']
        List whereQuery = ['uo.user = u and uo.org = :org']
        Map queryParams = [org: orgInstance]

        whereQuery.add('uo.status = :status')
        queryParams.put('status', UserOrg.STATUS_APPROVED)

        if (params.authority) {
            whereQuery.add('uo.formalRole = :role')
            queryParams.put('role', Role.get(params.authority.toLong()))
        }

        if (params.name && params.name != '' ) {
            whereQuery.add('(lower(u.username) like :name or lower(u.display) like :name)')
            queryParams.put('name', "%${params.name.toLowerCase()}%")
        }

        result.users = User.executeQuery(
                baseQuery.join(', ') + (whereQuery ? ' where ' + whereQuery.join(' and ') : '') ,
                queryParams,
                [sort:'user.username', order: 'asc']
        )

        result.orgInstance = orgInstance
        result
    }

    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR'])
    def edit() {
        redirect controller: 'organisation', action: 'show', params: params
        return
    }

    @Secured(['ROLE_ADMIN'])
    def delete() {
        def orgInstance = Org.get(params.id)
        if (!orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
            redirect action: 'list'
            return
        }

        try {
            orgInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'org.label', default: 'Org'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'org.label', default: 'Org'), params.id])
            redirect action: 'show', id: params.id
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def processAffiliation() {
      def result = [:]
      result.user = User.get(springSecurityService.principal.id)
      UserOrg uo = UserOrg.get(params.assoc)

      if (instAdmService.hasInstAdmPivileges(result.user, Org.get(params.id))) {

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
      //def comboType = RefdataCategory.lookupOrCreate('Organisational Role', 'Package Consortia')
      def comboType = RefdataValue.get(params.comboTypeTo)
      log.debug("Processing combo creation between ${fromOrg} AND ${toOrg} with type ${comboType}")
      def dupe = Combo.executeQuery("from Combo as c where c.fromOrg = ? and c.toOrg = ?", [fromOrg, toOrg])
      
      if (! dupe) {
        def consLink = new Combo(fromOrg:fromOrg,
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
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.editable = accessService.checkMinUserOrgRole(result.user, contextService.getOrg(), 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        def orgInstance = Org.get(params.id)
        if (! orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
            redirect action: 'list'
            return
        }

        result.orgInstance = orgInstance
        result.visiblePersons = addressbookService.getAllVisiblePersons(result.user, orgInstance)

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
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.orgInstance = Org.get(params.id)
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
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.orgInstance = Org.get(params.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAllGranted('ROLE_ADMIN')

        if (! result.orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
            redirect action: 'list'
            return
        }

        def orgAccessPointList = OrgAccessPoint.findAllByOrg(result.orgInstance,  [sort: ["name": 'asc', "accessMethod" : 'asc']])
        result.orgAccessPointList = orgAccessPointList

        result
    }

    def addOrgType()
    {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        def orgInstance = Org.get(params.org)

        if (!orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
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
            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label', default: 'Org'), orgInstance.name])
            redirect action: 'show', id: orgInstance.id
        }
    }
    def deleteOrgType()
    {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        def orgInstance = Org.get(params.org)

        if (!orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
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
            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label', default: 'Org'), orgInstance.name])
            redirect action: 'show', id: orgInstance.id
        }
    }

    private Map setResultGenerics() {
        User user = User.get(springSecurityService.principal.id)
        Org org = contextService.org
        return [user:user,institution:org,editable:accessService.checkMinUserOrgRole(user,org,'INST_EDITOR')]
    }

    @DebugAnnotation(perm="ORG_CONSORTIUM", type="Consortium", affil="INST_ADM", specRole="ROLE_ORG_EDITOR")
    @Secured(closure = { ctx.accessService.checkPermTypeAffiliationX("ORG_CONSORTIUM", "Consortium", "INST_ADM", "ROLE_ORG_EDITOR") })
    def toggleCombo() {
        Map result = setResultGenerics()
        if(!params.direction) {
            flash.error(message(code:'org.error.noToggleDirection'))
            response.sendError(404)
            return
        }
        switch(params.direction) {
            case 'add':
                Map map = [toOrg: result.institution,
                        fromOrg: Org.get(params.fromOrg),
                        type: RefdataValue.getByValueAndCategory('Consortium','Combo Type')]
                if (! Combo.findWhere(map)) {
                    def cmb = new Combo(map)
                    cmb.save()
                }
                break
            case 'remove':
                Combo cmb = Combo.findWhere(toOrg: result.institution,
                    fromOrg: Org.get(params.fromOrg),
                    type: RefdataValue.getByValueAndCategory('Consortium','Combo Type'))
                cmb.delete()
                break
        }
        redirect action: 'listInstitution'
    }
}
