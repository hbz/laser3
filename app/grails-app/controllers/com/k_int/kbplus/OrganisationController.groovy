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

    @Secured(['ROLE_USER'])
    def index() {
        redirect action: 'list', params: params
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
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

        result.settings = OrgSettings.findAllByOrg(result.orgInstance)

        result
    }

    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR'])
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

                response.setHeader "Content-disposition", "attachment; filename=\"${file}\".xlsx"
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

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") }) //preliminarily, until the new roleTypes are there
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
        result.editable    = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR,ROLE_ORG_COM_EDITOR')

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

    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR','ROLE_ORG_COM_EDITOR'])
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
    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR','ROLE_ORG_COM_EDITOR'])
    def findProviderMatches() {

        def result=[:]
        if ( params.proposedProvider ) {

            result.providerMatches= Org.executeQuery("from Org as o where exists (select roletype from o.orgType as roletype where roletype = :provider ) and (lower(o.name) like :searchName or lower(o.shortname) like :searchName or lower(o.sortname) like :searchName ) ",
                    [provider: RDStore.OT_PROVIDER, searchName: "%${params.proposedProvider.toLowerCase()}%"])
        }
        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_ADM")') //TODO temporary, to be changed as soon as ERMS-1078 is decided!
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def createInstitution() {
        Org contextOrg = contextService.org
        RefdataValue orgSector = RefdataValue.getByValueAndCategory('Higher Education','OrgSector')
        //RefdataValue orgType = RDStore.OT_INSTITUTION
        Org orgInstance = new Org(name: params.institution, sector: orgSector)
        orgInstance.addToOrgType(RefdataValue.getByValueAndCategory('Institution','OrgRoleType'))

        try {
            orgInstance.save(flush:true)
            if(RDStore.OT_CONSORTIUM.id in contextOrg.getallOrgTypeIds()) {
                Combo newMember = new Combo(fromOrg:orgInstance,toOrg:contextOrg,type:RefdataValue.getByValueAndCategory('Consortium','Combo Type'))
                newMember.save(flush:true)
            }
            orgInstance.setDefaultCustomerType()

            flash.message = message(code: 'default.created.message', args: [message(code: 'org.label', default: 'Org'), orgInstance.id])
            redirect action: 'show', id: orgInstance.id, params: [institutionalView: true]
        }
        catch (Exception e) {
            log.error("Problem creating title: ${orgInstance.errors}")
            log.error(e.printStackTrace())
            flash.message = message(code: "org.error.createInstitutionError",args:[orgInstance.errors])
            redirect ( action:'findInstitutionMatches' )
        }
    }

    @DebugAnnotation(test='hasAffiliation("INST_ADM")') //TODO temporary, to be changed as soon as ERMS-1078 is decided!
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    Map findInstitutionMatches() {
        Map consortiaMap = [:]
        Combo.findAllByType(RefdataValue.getByValueAndCategory('Consortium','Combo Type')).each { lObj ->
            Combo link = (Combo) lObj
            List consortia = consortiaMap.get(link.fromOrg.id)
            if(!consortia)
                consortia = [link.toOrg.id]
            else consortia << link.toOrg.id
            consortiaMap.put(link.fromOrg.id,consortia)
        }
        Map result=[institutionMatches:[],consortia:consortiaMap]
        if ( params.proposedInstitution ) {
            result.institutionMatches.addAll(Org.executeQuery("select o from Org as o where exists (select roletype from o.orgType as roletype where roletype = :institution ) and (lower(o.name) like :searchName or lower(o.shortname) like :searchName or lower(o.sortname) like :searchName ) ",
                    [institution: RDStore.OT_INSTITUTION, searchName: "%${params.proposedInstitution.toLowerCase()}%"]))
        }
        if (params.proposedInstitutionID) {
            result.institutionMatches.addAll(Org.executeQuery("select id.org from IdentifierOccurrence id where lower(id.identifier.value) like :identifier and lower(id.identifier.ns.ns) in (:namespaces) ",
                    [identifier: "%${params.proposedInstitutionID.toLowerCase()}%",namespaces:["isil","wibid"]]))
        }
        result
    }

    @Secured(['ROLE_USER'])
    def show() {

        def result = [:]

        //this is a flag to check whether the page has been called by a context org without full reading/writing permissions, to be extended as soon as the new orgTypes are defined
        if(params.institutionalView)
            result.institutionalView = params.institutionalView

        DebugUtil du = new DebugUtil()
        du.setBenchMark('this-n-that')

        def orgInstance = Org.get(params.id)
        def user = contextService.getUser()
        def org = contextService.getOrg()

        def link_vals = RefdataCategory.getAllRefdataValues("Organisational Role")
        def sorted_links = [:]
        def offsets = [:]

        du.setBenchMark('orgRoles')

        // TODO: experimental asynchronous task
        //def task_orgRoles = task {

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
        //}

        if (params.ajax) {
            render template: '/templates/links/orgRoleContainer', model: [listOfLinks: sorted_links, orgInstance: orgInstance]
            return
        }

        du.setBenchMark('editable')

        result.sorted_links = sorted_links

        result.user = user
        result.orgInstance = orgInstance

        def orgSector = RefdataValue.getByValueAndCategory('Publisher','OrgSector')
        def orgType = RefdataValue.getByValueAndCategory('Provider','OrgRoleType')

        //IF ORG is a Provider
        if(orgInstance.sector == orgSector || orgType?.id in orgInstance?.getallOrgTypeIds())
        {
            du.setBenchMark('editable2')
            result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_COM_EDITOR,ROLE_ORG_EDITOR')
        }else {
            du.setBenchMark('editable2')
            List<Long> consortia = Combo.findAllByTypeAndFromOrg(RefdataValue.getByValueAndCategory('Consortium','Combo Type'),orgInstance).collect { it ->
                it.toOrg.id
            }
            if(RDStore.OT_CONSORTIUM.id in org.getallOrgTypeIds() && consortia.size() == 1 && consortia.contains(org.id) && accessService.checkMinUserOrgRole(result.user,org,'INST_ADM'))
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

        result
    }

    @Secured(['ROLE_USER'])
    def documents() {
        Map result = setResultGenerics()
        result.org = Org.get(params.id)
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
        def ctxlist = []

        log.debug("deleteDocuments ${params}");

        docstoreService.unifiedDeleteDocuments(params)

        redirect controller: 'organisation', action: 'documents' /*, fragment: 'docstab' */
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
        result.users = UserOrg.findAllByStatusAndOrg(UserOrg.STATUS_APPROVED, orgInstance, [sort:'user.username', order: 'asc'])

        result.orgInstance = orgInstance
        result
    }

    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR','ROLE_ORG_COM_EDITOR'])
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
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
    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def readerNumber() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.editable = accessService.checkMinUserOrgRole(result.user, contextService.getOrg(), 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')

        result.orgInstance = Org.get(params.id)
        result.numbersInstanceList = ReaderNumber.findAllByOrg(result.orgInstance, [sort: 'referenceGroup', order: 'asc'])

        result
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def accessPoints() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        def orgInstance = Org.get(params.id)

        if ( SpringSecurityUtils.ifAllGranted('ROLE_ADMIN') ) {
          result.editable = true
        }
        else {
          result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_ADM')
        }

        if (!orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
            redirect action: 'list'
            return
        }

        def orgAccessPointList = OrgAccessPoint.findAllByOrg(orgInstance,  [sort: ["name": 'asc', "accessMethod" : 'asc']])
        result.orgAccessPointList = orgAccessPointList
        result.orgInstance = orgInstance

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

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
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
