package com.k_int.kbplus

import org.springframework.dao.DataIntegrityViolationException
import grails.plugins.springsecurity.Secured
import com.k_int.kbplus.auth.*;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import com.k_int.properties.*

class OrganisationsController {

    def springSecurityService
    def permissionHelperService
    def contextService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        redirect action: 'list', params: params
    }
    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def config() {
      def result = [:]
      result.user = User.get(springSecurityService.principal.id)
      def orgInstance = Org.get(params.id)

      if ( SpringSecurityUtils.ifAllGranted('ROLE_ADMIN') ) {
        result.editable = true
      }
      else {
        result.editable = permissionHelperService.hasUserWithRole(result.user, orgInstance, 'INST_ADM')
      }
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

      if (!orgInstance) {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
        redirect action: 'list'
        return
      }

      result.orgInstance = orgInstance
      result
    }
    @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
    def list() {

        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        params.max = params.max ?: result.user?.getDefaultPageSize()

      def results = null;
      def count = null;
      if ( ( params.orgNameContains != null ) && ( params.orgNameContains.length() > 0 ) &&
           ( params.orgRole != null ) && ( params.orgRole.length() > 0 ) ) {
        def qry = "from Org o where lower(o.name) like ? and exists ( from o.links r where r.roleType.id = ? )"
        results = Org.findAll(qry, ["%${params.orgNameContains.toLowerCase()}%", Long.parseLong(params.orgRole)],params);
        count = Org.executeQuery("select count(o) ${qry}",["%${params.orgNameContains.toLowerCase()}%", Long.parseLong(params.orgRole)])[0]
      }
      else if ( ( params.orgNameContains != null ) && ( params.orgNameContains.length() > 0 ) ) {
        def qry = "from Org o where lower(o.name) like ?"
        results = Org.findAll(qry, ["%${params.orgNameContains.toLowerCase()}%"], params);
        count = Org.executeQuery("select count (o) ${qry}",["%${params.orgNameContains.toLowerCase()}%"])[0]
      }
      else if ( ( params.orgRole != null ) && ( params.orgRole.length() > 0 ) ) {
        def qry = "from Org o where exists ( select r from o.links r where r.roleType.id = ? )"
        results = Org.findAll(qry, [Long.parseLong(params.orgRole)],params);
        count = Org.executeQuery("select count(o) ${qry}", [Long.parseLong(params.orgRole)])[0]
      }
      else { 
        results = Org.list(params)
        count = Org.count()
      }

      result.orgInstanceList = results
      result.orgInstanceTotal = count

      result
    }

    @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
    def create() {
    switch (request.method) {
    case 'GET':
        if (!params.name && !params.sector) {
            params.sector = RefdataValue.findByValue('Higher Education')
        }
          [orgInstance: new Org(params)]
      break
    case 'POST':
          def orgInstance = new Org(params)
          if (!orgInstance.save(flush: true)) {
              render view: 'create', model: [orgInstance: orgInstance]
              return
          }

      flash.message = message(code: 'default.created.message', args: [message(code: 'org.label', default: 'Org'), orgInstance.id])
          redirect action: 'show', id: orgInstance.id
      break
    }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def show() {
      def result = [:]
      result.user = User.get(springSecurityService.principal.id)
      def orgInstance = Org.get(params.id)

      if ( SpringSecurityUtils.ifAllGranted('ROLE_ADMIN') ) {
        result.editable = true
      }
      else {
        result.editable = permissionHelperService.hasUserWithRole(result.user, orgInstance, 'INST_ADM')
      }


      if (!orgInstance) {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
        redirect action: 'list'
        return
      }

      result.orgInstance = orgInstance
      
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
          
          def link_type_count = OrgRole.executeQuery("select count(*) from OrgRole as orgr where orgr.org = :oi and orgr.roleType.id = :lid",[oi: orgInstance, lid: lv.id])
          def link_type_results = OrgRole.executeQuery("select orgr from OrgRole as orgr where orgr.org = :oi and orgr.roleType.id = :lid",[oi: orgInstance, lid: lv.id],[offset:param_offset,max:10])
          
          if(link_type_results){
            sorted_links["${String.valueOf(lv.id)}"] = [rdv: lv, rdvl: cur_param, links: link_type_results, total: link_type_count[0]]
          }
        }else{
          log.debug("Could not read Refdata: ${lv}")
        }
      }
      
      result.sorted_links = sorted_links


        // -- private properties

        result.authorizedOrgs = result.user?.authorizedOrgs
        result.contextOrg     = contextService.getOrg(result.user)

        // create mandatory OrgPrivateProperties if not existing

        def mandatories = []
        result.user?.authorizedOrgs?.each{ org ->
            def ppd = PropertyDefinition.findAllByDescrAndMandatoryAndTenant("Org Property", true, org)
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


    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def properties() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        def orgInstance = Org.get(params.id)

        if (SpringSecurityUtils.ifAllGranted('ROLE_ADMIN')) {
            result.editable = true
        }
        else {
            result.editable = permissionHelperService.hasUserWithRole((User)result.user, orgInstance, 'INST_ADM')
        }

        if (!orgInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
            redirect action: 'list'
            return
        }

        // create mandatory OrgPrivateProperties if not existing

        def mandatories = []
        result.user?.authorizedOrgs?.each{ org ->
            def ppd = PropertyDefinition.findAllByDescrAndMandatoryAndTenant("Org Property", true, org)
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
    
    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def users() {
      def result = [:]
      result.user = User.get(springSecurityService.principal.id)
      def orgInstance = Org.get(params.id)
	  
      
      if ( permissionHelperService.hasUserWithRole(result.user, orgInstance, 'INST_ADM') ) {
        result.editable = true
      }
      def tracked_roles = ["KBPLUS_EDITOR":"KB+ Editor","ROLE_ADMIN":"KB+ Administrator"]

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


    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def info() {

      def result = [:]

      result.user = User.get(springSecurityService.principal.id)

      def orgInstance = Org.get(params.id)
      if (!orgInstance) {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
        redirect action: 'info'
        return
      }

      result.orgInstance=orgInstance
      
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
          
          def link_type_count = OrgRole.executeQuery("select count(*) from OrgRole as orgr where orgr.org = :oi and orgr.roleType.id = :lid",[oi: orgInstance, lid: lv.id])
          def link_type_results = OrgRole.executeQuery("select orgr from OrgRole as orgr where orgr.org = :oi and orgr.roleType.id = :lid",[oi: orgInstance, lid: lv.id],[offset:param_offset,max:10])
          
          if(link_type_results){
            sorted_links["${String.valueOf(lv.id)}"] = [rdv: lv, rdvl: cur_param, links: link_type_results, total: link_type_count[0]]
          }
        }else{
          log.debug("Could not read Refdata: ${lv}")
        }
      }
      
      result.sorted_links = sorted_links
      
      result
    }


    @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
    def edit() {
    switch (request.method) {
    case 'GET':
          def orgInstance = Org.get(params.id)
          if (!orgInstance) {
              flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
              redirect action: 'list'
              return
          }

          [orgInstance: orgInstance, editable:true]
      break
    case 'POST':
          def orgInstance = Org.get(params.id)
          if (!orgInstance) {
              flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
              redirect action: 'list'
              return
          }

          if (params.version) {
              def version = params.version.toLong()
              if (orgInstance.version > version) {
                  orgInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
                            [message(code: 'org.label', default: 'Org')] as Object[],
                            "Another user has updated this Org while you were editing")
                  render view: 'edit', model: [orgInstance: orgInstance]
                  return
              }
          }
          
          if (params.fromOrg){
            addOrgCombo(Org.get(params.fromOrg), Org.get(params.toOrg))
            render view: 'edit', model: [orgInstance: orgInstance]
            return
          }

          orgInstance.properties = params

          if (!orgInstance.save(flush: true)) {
              render view: 'edit', model: [orgInstance: orgInstance, editable:true]
              return
          }

          flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label', default: 'Org'), orgInstance.id])
          redirect action: 'show', id: orgInstance.id
      break
    }
    }

    @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
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

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def revokeRole() {
      def result = [:]
      result.user = User.get(springSecurityService.principal.id)
      UserOrg uo = UserOrg.get(params.grant)
      if (permissionHelperService.hasUserWithRole(result.user, uo.org, 'INST_ADM') ) {
        uo.status = UserOrg.STATUS_REJECTED
        uo.save()
      }
      redirect action: 'users', id: params.id
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def enableRole() {
      def result = [:]
      result.user = User.get(springSecurityService.principal.id)
      UserOrg uo = UserOrg.get(params.grant)
      if ( permissionHelperService.hasUserWithRole(result.user, uo.org, 'INST_ADM') ) {
        uo.status = UserOrg.STATUS_APPROVED
        uo.save();
      }
      redirect action: 'users', id: params.id
    }
    
    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
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


    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def deleteRole() {
      def result = [:]
      result.user = User.get(springSecurityService.principal.id)
      UserOrg uo = UserOrg.get(params.grant)
      if ( permissionHelperService.hasUserWithRole(result.user, uo.org, 'INST_ADM') ) {
        uo.delete();
      }
      redirect action: 'users', id: params.id
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def addressbook() {
      def result = [:]
      result.user = User.get(springSecurityService.principal.id)
      
      def orgInstance = Org.get(params.id)
      if (!orgInstance) {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
        redirect action: 'list'
        return
      }    
      
      def membershipOrgIds = []
      result.user?.authorizedOrgs?.each{ org ->
          membershipOrgIds << org.id
      }
      
      def visiblePersons = []
      orgInstance?.prsLinks.each { pl ->
          if(pl.prs?.isPublic?.value == 'No'){
              if(pl.prs?.tenant?.id && membershipOrgIds.contains(pl.prs?.tenant?.id)){
                  if(!visiblePersons.contains(pl.prs)){
                      visiblePersons << pl.prs
                  }
              }
          }
      }
      result.visiblePersons = visiblePersons
      
      result.orgInstance = orgInstance
      result
    }
}
