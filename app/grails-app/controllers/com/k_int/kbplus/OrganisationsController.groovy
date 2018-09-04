package com.k_int.kbplus

import de.laser.helper.DebugAnnotation
import grails.converters.JSON
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

        result.editable = accessService.checkMinUserOrgRole(result.user, orgInstance, 'INST_ADM') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

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
        params.max = params.max ?: result.user?.getDefaultPageSize()

        def fsq = filterService.getOrgQuery(params)

        result.orgList  = Org.findAll(fsq.query, fsq.queryParams, params)
        result.orgListTotal = Org.executeQuery("select count (o) ${fsq.query}", fsq.queryParams)[0]

        result
    }

    @Secured(['ROLE_USER'])
    def listProvider() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        params.max = params.max ?: result.user?.getDefaultPageSize()


        params.orgSector = RefdataValue.getByValueAndCategory('Publisher','OrgSector').id.toString()
        params.orgType = RefdataValue.getByValueAndCategory('Provider','OrgType').id.toString()

        def fsq = filterService.getOrgQuery(params)

        result.orgList  = Org.findAll(fsq.query, fsq.queryParams, params)
        result.orgListTotal = Org.executeQuery("select count (o) ${fsq.query}", fsq.queryParams)[0]

        result
    }

    @Secured(['ROLE_ADMIN','ROLE_ORG_EDITOR'])
    def create() {
        switch (request.method) {
            case 'GET':
                if (!params.name && !params.sector) {
                    params.sector = RefdataValue.findByValue('Higher Education')
                }
                if (!params.name && !params.orgType) {
                    params.orgType = RefdataValue.findByValue('Institution')
                }
                [orgInstance: new Org(params)]
                break
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
                def orgType = RefdataValue.getByValueAndCategory('Provider','OrgType')
                def orgInstance = new Org(name: params.provider, orgType: orgType.id, sector: orgSector.id)

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

            result.providerMatches= Org.findAllByNameIlikeAndOrgType("%${params.proposedProvider}%", RefdataValue.getByValueAndCategory('Provider','OrgType'))
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
        def orgType = RefdataValue.getByValueAndCategory('Provider','OrgType')

        //IF ORG is a Provider
        if(orgInstance.sector == orgSector || orgType == orgInstance.orgType)
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

        result.orgInstance = contextService.getOrg()
        result.numbersInstanceList = Numbers.findAllByOrg(contextService.getOrg(), [sort: 'type'])

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
}
