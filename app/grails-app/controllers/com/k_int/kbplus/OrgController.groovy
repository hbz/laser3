package com.k_int.kbplus

import org.springframework.dao.DataIntegrityViolationException

import grails.plugins.springsecurity.Secured
import grails.converters.*

import org.elasticsearch.groovy.common.xcontent.*

import groovy.xml.MarkupBuilder
import grails.plugins.springsecurity.Secured

import com.k_int.kbplus.ajax.AjaxOrgRoleHandler
import com.k_int.kbplus.auth.*;

class OrgController extends AjaxOrgRoleHandler {

    def springSecurityService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        redirect action: 'list', params: params
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        def results = null;
        def count = null;
        if ( ( params.orgNameContains != null ) && ( params.orgNameContains.length() > 0 ) &&
             ( params.orgRole != null ) && ( params.orgRole.length() > 0 ) ) {
          def qry = "from Org o where o.name like ? and exists ( from o.links r where r.roleType.id = ? )"
          results = Org.findAll(qry, ["%${params.orgNameContains}%", Long.parseLong(params.orgRole)],params);
          count = Org.executeQuery("select count(o) ${qry}",["%${params.orgNameContains}%", Long.parseLong(params.orgRole)])[0]
        }
        else if ( ( params.orgNameContains != null ) && ( params.orgNameContains.length() > 0 ) ) {
          def qry = "from Org o where o.name like ?"
          results = Org.findAll(qry, ["%${params.orgNameContains}%"], params);
          count = Org.executeQuery("select count (o) ${qry}",["%${params.orgNameContains}%"])[0]
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

        [orgInstanceList: results, orgInstanceTotal: count]
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def create() {
		switch (request.method) {
		case 'GET':
		    if (!params.name && !params.sector) {
				params.sector = 'Higher Education'
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
        def orgInstance = Org.get(params.id)
        if (!orgInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
            redirect action: 'list'
            return
        }

        [orgInstance: orgInstance]
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit() {
		switch (request.method) {
		case 'GET':
	        def orgInstance = Org.get(params.id)
	        if (!orgInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'org.label', default: 'Org'), params.id])
	            redirect action: 'list'
	            return
	        }

	        [orgInstance: orgInstance]
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

	        orgInstance.properties = params

	        if (!orgInstance.save(flush: true)) {
	            render view: 'edit', model: [orgInstance: orgInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'org.label', default: 'Org'), orgInstance.id])
	        redirect action: 'show', id: orgInstance.id
			break
		}
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
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

    @Override
    def ajax() {
        // TODO: check permissions for operation
        
        switch(params.op){
            case 'add':
                ajaxOrgRoleAdd()
                return
            break;
            case 'delete':
                ajaxOrgRoleDelete()
                return
            break;
            default:
                ajaxOrgRoleList()
                return
            break;
        }
    }
    @Override
    def ajaxOrgRoleList() {
        def orgInstance = Org.get(params.id)
        def cluster = Cluster.getAll()
        def roles = RefdataValue.findAllByOwner(com.k_int.kbplus.RefdataCategory.findByDesc('Cluster Role'))
        
        render view: 'ajax/orgRoleList', model: [
            orgInstance: orgInstance,
            cluster: cluster,
            roles: roles
            ]
        return
    }
    @Override
    def private ajaxOrgRoleDelete() {
        
        def orgRole = OrgRole.get(params.orgRole)
        // TODO: switch to resolveOID/resolveOID2 ?
        
        //def orgRole = AjaxController.resolveOID(params.orgRole[0])
        if(orgRole) {
            log.debug("deleting OrgRole ${orgRole}")
            orgRole.delete(flush:true);
        }
        ajaxOrgRoleList()
    }
    @Override
    def private ajaxOrgRoleAdd() {
        
        def org     = Org.get(params.id)
        def cluster = Cluster.get(params.cluster)
        def role    = RefdataValue.get(params.role)
                
        def newOrgRole = new OrgRole(org:org, roleType:role, cluster: cluster)
        if ( newOrgRole.save(flush:true) ) {
            log.debug("adding OrgRole ${newOrgRole}")
        } else {
            log.error("problem saving new OrgRole ${newOrgRole}")
            newOrgRole.errors.each { e ->
                log.error(e)
            }
        }
        
        ajaxOrgRoleList()
    }
}
