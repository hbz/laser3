package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.PrivateProperty
import de.laser.helper.DebugAnnotation
import grails.converters.JSON
import org.apache.poi.hssf.usermodel.HSSFRichTextString
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.springframework.dao.DataIntegrityViolationException
import grails.plugin.springsecurity.annotation.Secured
import com.k_int.kbplus.auth.*;
import grails.plugin.springsecurity.SpringSecurityUtils
import com.k_int.properties.*

@Secured(['IS_AUTHENTICATED_FULLY'])
class OrganisationsController {

    def springSecurityService
    def accessService
    def contextService
    def addressbookService
    def filterService
    def genericOIDService
    def propertyService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER'])
    def index() {
        redirect action: 'list', params: params
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def config() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        def orgInstance = Org.get(params.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_ADM') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')

        // TODO: deactived
      /*
      if(! orgInstance.customProperties){
        grails.util.Holders.config.customProperties.org.each{ 
          def entry = it.getValue()
          def type = PropertyDefinition.lookupOrCreate(
                  entry.name,
                  entry.class,
                  PropertyDefinition.ORG_CONF,
                  PropertyDefinition.FALSE,
                  PropertyDefinition.FALSE,
                  null
          )
          def prop = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, orgInstance, type)
          prop.note = entry.note
          prop.save()
        }
      }
      */

      if (! orgInstance) {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
        redirect action: 'list'
        return
      }

      result.orgInstance = orgInstance
      result
    }

    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR'])
    def list() {

        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        params.max  = params.max ?: result.user?.getDefaultPageSizeTMP()
        params.sort = params.sort ?: " LOWER(o.shortname), LOWER(o.name)"

        def fsq = filterService.getOrgQuery(params)

        result.orgList  = Org.findAll(fsq.query, fsq.queryParams, params)
        result.orgListTotal = Org.executeQuery("select count (o) ${fsq.query}", fsq.queryParams)[0]

        if ( params.exportXLS=='yes' ) {

            params.remove('max')

            def orgs = Org.findAll(fsq.query, fsq.queryParams, params)

            def message = g.message(code: 'menu.institutions.all_orgs')

            exportOrg(orgs, message, true)
            return
        }


        result
    }

    @Secured(['ROLE_USER'])
    def listProvider() {
        def result = [:]
        result.propList    = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())
        result.user        = User.get(springSecurityService.principal.id)

        params.orgSector   = RefdataValue.getByValueAndCategory('Publisher','OrgSector')?.id?.toString()
        params.orgRoleType = RefdataValue.getByValueAndCategory('Provider','OrgRoleType')?.id?.toString()
        params.sort        = params.sort ?: " LOWER(o.shortname), LOWER(o.name)"

        def fsq            = filterService.getOrgQuery(params)
        def orgListTotal   = Org.findAll(fsq.query, fsq.queryParams)
        params.max         = params.max ?: result.user?.getDefaultPageSizeTMP()
        def fsq2           = null

        if (params.filterPropDef) {
            def tmpQuery
            def tmpQueryParams
            fsq2 = filterService.getOrgQuery([constraint_orgIds: orgListTotal.collect{ it2 -> it2.id }] << params)
            (tmpQuery, tmpQueryParams) = propertyService.evalFilterQuery(params, fsq2.query, 'o', [:])
            def tmpQueryParams2 = fsq2.queryParams << tmpQueryParams
            result.orgList      = Org.findAll(tmpQuery, tmpQueryParams2, params)
            result.orgListTotal = Org.executeQuery("select count (o) ${tmpQuery}", tmpQueryParams2)[0]
            fsq.query = tmpQuery
            fsq.queryParams  = tmpQueryParams2

        } else {

            result.orgList      = Org.findAll(fsq.query, fsq.queryParams, params)
            result.orgListTotal = Org.executeQuery("select count (o) ${fsq.query}", fsq.queryParams)[0]
        }

        if ( params.exportXLS=='yes' ) {

            params.remove('max')

            def orgs = Org.findAll(fsq.query, fsq.queryParams, params)

            def message = g.message(code: 'menu.institutions.all_provider')

            exportOrg(orgs, message, false)
            return
        }

        result
    }

    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR'])
    def create() {
        switch (request.method) {
            case 'POST':
                def orgInstance = new Org(params)

                if (params.name) {
                    if (orgInstance.save(flush: true)) {
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
                def orgRoleType = RefdataValue.getByValueAndCategory('Provider','OrgRoleType')
                def orgInstance = new Org(name: params.provider, sector: orgSector.id)
                orgInstance.addToOrgRoleType(orgRoleType)

                if ( orgInstance.save(flush:true) ) {
                    flash.message = message(code: 'default.created.message', args: [message(code: 'org.label', default: 'Org'), orgInstance.id])
                    redirect action: 'show', id: orgInstance.id
                    return
                }
                else {
                    log.error("Problem creating title: ${orgInstance.errors}");
                    flash.message = "Problem creating Provider: ${orgInstance.errors}"
                    redirect ( action:'findProviderMatchesMatches' )
                }
    }
    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR','ROLE_ORG_COM_EDITOR'])
    def findProviderMatches() {

        def result=[:]
        if ( params.proposedProvider ) {

            result.providerMatches= Org.executeQuery("from Org as o where exists (select roletype from o.orgRoleType as roletype where roletype = :provider ) and (lower(o.name) like :searchName or lower(o.shortname) like :searchName or lower(o.sortname) like :searchName ) ",
                    [provider: RefdataValue.getByValueAndCategory('Provider', 'OrgRoleType'), searchName: "%${params.proposedProvider.toLowerCase()}%"])
        }
        result
    }

    @Secured(['ROLE_USER'])
    def show() {
        def result = [:]

        def orgInstance = Org.get(params.id)

        def link_vals = RefdataCategory.getAllRefdataValues("Organisational Role")
        def sorted_links = [:]
        def offsets = [:]

        link_vals.each { lv ->
            def param_offset = 0

            if(lv.id){
                def cur_param = "rdvl_${String.valueOf(lv.id)}"

                if(params[cur_param]){
                    param_offset = params[cur_param]
                    result[cur_param] = param_offset
                }

                def links = OrgRole.findAll {
                            org == orgInstance &&
                            roleType == lv
                }
                links = links.findAll{ it -> it.ownerStatus?.value != 'Deleted' }

                def link_type_results = links.drop(param_offset.toInteger()).take(10) // drop from head, take 10

                if(link_type_results){
                    sorted_links["${String.valueOf(lv.id)}"] = [rdv: lv, rdvl: cur_param, links: link_type_results, total: links.size()]
                }
            }else{
                log.debug("Could not read Refdata: ${lv}")
            }
        }

        if (params.ajax) {
            render template: '/templates/links/orgRoleContainer', model: [listOfLinks: sorted_links, orgInstance: orgInstance]
            return
        }

        result.sorted_links = sorted_links

        result.user = User.get(springSecurityService.principal.id)
        result.orgInstance = orgInstance

        def orgSector = RefdataValue.getByValueAndCategory('Publisher','OrgSector')
        def orgRoleType = RefdataValue.getByValueAndCategory('Provider','OrgRoleType')

        //IF ORG is a Provider
        if(orgInstance.sector == orgSector || orgRoleType in orgInstance.orgRoleType)
        {
            result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_COM_EDITOR,ROLE_ORG_EDITOR')
        }else {
            result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
        }
        
      if (! orgInstance) {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
        redirect action: 'list'
        return
      }

        // -- private properties

        result.authorizedOrgs = result.user?.authorizedOrgs
        result.contextOrg     = contextService.getOrg()

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

        // -- private properties
      
      result
    }

    def renderGroupedProperties_Ajax() {
        // JUST FOR TESTING
        // JUST FOR TESTING
        // JUST FOR TESTING

        def user            = User.get(springSecurityService.principal.id)
        def authorizedOrgs  = user?.authorizedOrgs
        def orgInstance     = Org.get(params.id)
        def editable        = false

        // copied from show()
        def orgSector = RefdataValue.getByValueAndCategory('Publisher','OrgSector')
        def orgRoleType = RefdataValue.getByValueAndCategory('Provider','OrgRoleType')

        //IF ORG is a Provider
        if(orgInstance.sector == orgSector || orgRoleType in orgInstance.orgRoleType) {
            editable = accessService.checkMinUserOrgRole(user, orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_COM_EDITOR,ROLE_ORG_EDITOR')
        }
        else {
            editable = accessService.checkMinUserOrgRole(user, orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
        }

        render template: "properties", model: [ orgInstance: orgInstance, authorizedOrgs: authorizedOrgs, editable: editable ]
    }

    @Secured(['ROLE_USER'])
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def users() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        def orgInstance = Org.get(params.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_ADM') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

      def tracked_roles = ["ROLE_ADMIN":"KB+ Administrator"]

      if (!orgInstance) {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
        redirect action: 'list'
        return
      }
      result.users = orgInstance.affiliations.collect{ userOrg ->
        def admin_roles = []
        userOrg.user.roles.each{ 
          if (tracked_roles.keySet().contains(it.role.authority)){
            def role_match = tracked_roles.get(it.role.authority)+" (${it.role.authority})"
            admin_roles += role_match
          }
        }
        // log.debug("Found roles: ${admin_roles} for user ${userOrg.user.displayName}")

        return [userOrg,admin_roles?:null]

      }
      // log.debug(result.users)
      result.orgInstance = orgInstance
      result
    }

    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR','ROLE_ORG_COM_EDITOR'])
    def edit() {
        redirect controller: 'organisations', action: 'show', params: params
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
    def revokeRole() {
      def result = [:]
      result.user = User.get(springSecurityService.principal.id)
      UserOrg uo = UserOrg.get(params.grant)
      if (accessService.checkMinUserOrgRole(result.user, uo.org, 'INST_ADM') ) {
        uo.status = UserOrg.STATUS_REJECTED
        uo.save()
      }
      redirect action: 'users', id: params.id
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def enableRole() {
      def result = [:]
      result.user = User.get(springSecurityService.principal.id)
      UserOrg uo = UserOrg.get(params.grant)
      if ( accessService.checkMinUserOrgRole(result.user, uo.org, 'INST_ADM') ) {
        uo.status = UserOrg.STATUS_APPROVED
        uo.save();
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

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def deleteRole() {
      def result = [:]
      result.user = User.get(springSecurityService.principal.id)
      UserOrg uo = UserOrg.get(params.grant)
      if ( accessService.checkMinUserOrgRole(result.user, uo.org, 'INST_ADM') ) {
        uo.delete();
      }
      redirect action: 'users', id: params.id
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
    def numbers() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.editable = accessService.checkMinUserOrgRole(result.user, contextService.getOrg(), 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')

        result.orgInstance = Org.get(params.id)
        result.numbersInstanceList = Numbers.findAllByOrg(Org.get(params.id), [sort: 'type'])

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
    def addOrgRoleType()
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
            orgInstance.addToOrgRoleType(RefdataValue.get(params.orgRoleType))
            orgInstance.save(flush: true)
            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label', default: 'Org'), orgInstance.name])
            redirect action: 'show', id: orgInstance.id
        }
    }
    def deleteOrgRoleType()
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
            orgInstance.removeFromOrgRoleType(RefdataValue.get(params.removeOrgRoleType))
            orgInstance.save(flush: true)
            flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label', default: 'Org'), orgInstance.name])
            redirect action: 'show', id: orgInstance.id
        }
    }

    private def exportOrg(orgs, message, addHigherEducationTitles) {
        try {
            def titles = [
                    'Name', 'Kurzname', 'Sortiername']

            def orgSector = RefdataValue.getByValueAndCategory('Higher Education','OrgSector')
            def orgRoleType = RefdataValue.getByValueAndCategory('Provider','OrgRoleType')


            if(addHigherEducationTitles)
            {
                titles.add('Bibliothekstyp')
                titles.add('Verbundszugehörigkeit')
                titles.add('Trägerschaft')
                titles.add('Bundesland')
                titles.add('Land')
            }

            def propList =
                    PropertyDefinition.findAll( "from PropertyDefinition as pd where pd.descr in :defList and pd.tenant is null", [
                            defList: [PropertyDefinition.ORG_PROP],
                    ] // public properties
                    ) +
                            PropertyDefinition.findAll( "from PropertyDefinition as pd where pd.descr in :defList and pd.tenant = :tenant", [
                                    defList: [PropertyDefinition.ORG_PROP],
                                    tenant: contextService.getOrg()
                            ]// private properties
                            )

            propList.sort { a, b -> a.name.compareToIgnoreCase b.name}

            propList.each {
                titles.add(it.name)
            }

            def sdf = new java.text.SimpleDateFormat(g.message(code:'default.date.format.notime', default:'yyyy-MM-dd'));
            def datetoday = sdf.format(new Date(System.currentTimeMillis()))

            HSSFWorkbook wb = new HSSFWorkbook();

            HSSFSheet sheet = wb.createSheet(message);

            //the following three statements are required only for HSSF
            sheet.setAutobreaks(true);

            //the header row: centered text in 48pt font
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(16.75f);
            titles.eachWithIndex { titlesName, index ->
                Cell cell = headerRow.createCell(index);
                cell.setCellValue(titlesName);
            }

            //freeze the first row
            sheet.createFreezePane(0, 1);

            Row row;
            Cell cell;
            int rownum = 1;

            orgs.sort{it.name}
            orgs.each{  org ->
                int cellnum = 0;
                row = sheet.createRow(rownum);

                //Name
                cell = row.createCell(cellnum++);
                cell.setCellValue(new HSSFRichTextString(org.name));

                //Shortname
                cell = row.createCell(cellnum++);
                cell.setCellValue(new HSSFRichTextString(org.shortname));

                //Sortname
                cell = row.createCell(cellnum++);
                cell.setCellValue(new HSSFRichTextString(org.sortname));


                if(addHigherEducationTitles) {

                    //libraryType
                    cell = row.createCell(cellnum++);
                    cell.setCellValue(new HSSFRichTextString(org.libraryType?.getI10n('value') ?: ' '));

                    //libraryNetwork
                    cell = row.createCell(cellnum++);
                    cell.setCellValue(new HSSFRichTextString(org.libraryNetwork?.getI10n('value') ?: ' '));

                    //funderType
                    cell = row.createCell(cellnum++);
                    cell.setCellValue(new HSSFRichTextString(org.funderType?.getI10n('value') ?: ' '));

                    //federalState
                    cell = row.createCell(cellnum++);
                    cell.setCellValue(new HSSFRichTextString(org.federalState?.getI10n('value') ?: ' '));

                    //country
                    cell = row.createCell(cellnum++);
                    cell.setCellValue(new HSSFRichTextString(org.country?.getI10n('value') ?: ' '));
                }

                propList.each { pd ->
                    def value = ''
                    org.customProperties.each{ prop ->
                        if(prop.type.descr == pd.descr && prop.type == pd)
                        {
                            if(prop.type.type == Integer.toString()){
                                value = prop.intValue.toString()
                            }
                            else if (prop.type.type == String.toString()){
                                value = prop.stringValue
                            }
                            else if (prop.type.type == BigDecimal.toString()){
                                value = prop.decValue.toString()
                            }
                            else if (prop.type.type == Date.toString()){
                                value = prop.dateValue.toString()
                            }
                            else if (prop.type.type == RefdataValue.toString()) {
                                value = prop.refValue?.getI10n('value') ?: ''
                            }

                        }
                    }

                    org.privateProperties.each{ prop ->
                           if(prop.type.descr == pd.descr && prop.type == pd)
                           {
                               if(prop.type.type == Integer.toString()){
                                   value = prop.intValue.toString()
                               }
                               else if (prop.type.type == String.toString()){
                                   value = prop.stringValue
                               }
                               else if (prop.type.type == BigDecimal.toString()){
                                   value = prop.decValue.toString()
                               }
                               else if (prop.type.type == Date.toString()){
                                       value = prop.dateValue.toString()
                                   }
                               else if (prop.type.type == RefdataValue.toString()) {
                                   value = prop.refValue?.getI10n('value') ?: ''
                               }

                           }
                   }
                    cell = row.createCell(cellnum++);
                    cell.setCellValue(new HSSFRichTextString(value));
                }

                rownum++
            }

            for (int i = 0; i < 22; i++) {
                sheet.autoSizeColumn(i);
            }
            // Write the output to a file
            String file = message+"_${datetoday}.xls";
            //if(wb instanceof XSSFWorkbook) file += "x";

            response.setHeader "Content-disposition", "attachment; filename=\"${file}\""
            // response.contentType = 'application/xls'
            response.contentType = 'application/vnd.ms-excel'
            wb.write(response.outputStream)
            response.outputStream.flush()

        }
        catch ( Exception e ) {
            log.error("Problem",e);
            response.sendError(500)
        }
    }
}
