package com.k_int.kbplus

import org.springframework.dao.DataIntegrityViolationException

import com.k_int.kbplus.ajax.AjaxHandler

class PersonController extends AjaxHandler {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [personInstanceList: Person.list(params), personInstanceTotal: Person.count()]
    }

    def create() {
		switch (request.method) {
		case 'GET':
        	[personInstance: new Person(params)]
			break
		case 'POST':
	        def personInstance = new Person(params)
	        if (!personInstance.save(flush: true)) {
	            render view: 'create', model: [personInstance: personInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'person.label', default: 'Person'), personInstance.id])
	        redirect action: 'show', id: personInstance.id
			break
		}
    }

    def show() {
        def personInstance = Person.get(params.id)
        if (!personInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])
            redirect action: 'list'
            return
        }

        [personInstance: personInstance]
    }

    def edit() {
		switch (request.method) {
		case 'GET':
	        def personInstance = Person.get(params.id)
	        if (!personInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])
	            redirect action: 'list'
	            return
	        }

	        [personInstance: personInstance]
			break
		case 'POST':
	        def personInstance = Person.get(params.id)
	        if (!personInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])
	            redirect action: 'list'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (personInstance.version > version) {
	                personInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'person.label', default: 'Person')] as Object[],
	                          "Another user has updated this Person while you were editing")
	                render view: 'edit', model: [personInstance: personInstance]
	                return
	            }
	        }

	        personInstance.properties = params

	        if (!personInstance.save(flush: true)) {
	            render view: 'edit', model: [personInstance: personInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'person.label', default: 'Person'), personInstance.id])
	        redirect action: 'show', id: personInstance.id
			break
		}
    }

    def delete() {
        def personInstance = Person.get(params.id)
        if (!personInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])
            redirect action: 'list'
            return
        }

        try {
            personInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'person.label', default: 'Person'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'person.label', default: 'Person'), params.id])
            redirect action: 'show', id: params.id
        }
    }
    
    @Override
    def ajax() {
        // TODO: check permissions for operation
        
        switch(params.op){
            case 'add':
                ajaxAdd()
                return
            break;
            case 'delete':
                ajaxDelete()
                return
            break;
            default:
                ajaxList()
                return
            break;
        }
    }
    
    @Override
    def private ajaxList() {
        def valid               = true
        def person              = Person.get(params.id)
        def org                 = Org.get(params.org)
        def orgs                = Org.getAll()
        def type                = params.type
        def prsLinks            // only person depeding entities
        def targets             //
        def roles               = RefdataValue.findAllByOwner(com.k_int.kbplus.RefdataCategory.findByDesc('Person Role'))
        def targetOptionValue   = "name"
        def linkController
        def linkAction          // TODO
                
        def hqlPart = "from PersonRole as GOR where GOR.prs = ${person.id}"
        
        if(type == "cluster") {
            targets             = Cluster.getAll()
            linkController      = "cluster"
            prsLinks            = PersonRole.findAll(hqlPart + " and GOR.cluster is not NULL")
        }
        else if(type == "lic") {
            targets             = License.getAll()
            targetOptionValue   = "reference"
            linkController      = "license"
            prsLinks            = PersonRole.findAll(hqlPart + " and GOR.lic is not NULL")
        }
        else if(type == "pkg") {
            targets             = Package.getAll()
            linkController      = "package"
            prsLinks            = PersonRole.findAll(hqlPart + " and GOR.pkg is not NULL")
        }
        else if(type == "sub") {
            targets             = Subscription.getAll()
            linkController      = "subscription"
            prsLinks            = PersonRole.findAll(hqlPart + " and GOR.sub is not NULL")
        }
        else if(type == "title") {
            targets             = TitleInstance.getAll()
            targetOptionValue   = "normTitle"
            linkController      = "titleInstance"
            prsLinks            = PersonRole.findAll(hqlPart + " and GOR.title is not NULL")
        }
        else {
            valid = false
        }

        render view: 'ajax/genericOrgRoleList', model: [
            personInstance:     person,
            targets:            targets,
            orgs:               orgs,
            prsLinks:           prsLinks,
            type:               type,
            roles:              roles,
            targetOptionValue:  targetOptionValue,
            linkController:     linkController
            ]
        return
    }

    @Override
    def private ajaxDelete() {

        log.debug(params)
        
        def prsRole = PersonRole.get(params.prsRole)
        // TODO: switch to resolveOID/resolveOID2 ?
        
        //def prsRole = AjaxController.resolveOID(params.prsRole[0])
        if(prsRole) {
            log.debug("deleting PersonRole ${prsRole}")
            prsRole.delete(flush:true);
        }
        ajaxList()
    }

    @Override
    def private ajaxAdd() {
        
        log.debug(params)
     
        def prs         = Person.get(params.id)
        def org         = Org.get(params.org)
        def prsRole     = RefdataValue.get(params.role)
        def type        = params.type
        def newPrsRole
        def target
        
        switch(type) {
            case "cluster":
                target     = Cluster.get(params.target)
                newPrsRole = new PersonRole(prs:prs, roleType:prsRole, org:org, cluster:target)
            break;
            case"lic":
                target     = License.get(params.target)
                newPrsRole = new PersonRole(prs:prs, roleType:prsRole, org:org, lic:target)
            break;
            case "pkg":
                target     = Package.get(params.target)
                newPrsRole = new PersonRole(prs:prs, roleType:prsRole, org:org, pkg:target)
            break;
            case "sub":
                target     = Subscription.get(params.target)
                newPrsRole = new PersonRole(prs:prs, roleType:prsRole, org:org, sub:target)
            break;
            case "title":
                target     = TitleInstance.get(params.target)
                newPrsRole = new PersonRole(prs:prs, roleType:prsRole, org:org, title:target)
            break;
        }
        
        if(PersonRole.find("from PersonRole as GOR where GOR.prs = ${prs.id} and GOR.org = ${org.id} and GOR.roleType = ${prsRole.id} and GOR.${type} = ${target.id}")) {
            log.debug("ignoring to add PersonRole because of existing duplicate")
        }
        else if(newPrsRole) {
            if(newPrsRole.save(flush:true)) {
                log.debug("adding PersonRole ${newPrsRole}")
            } else {
                log.error("problem saving new PersonRole ${newPrsRole}")
                if(newPrsRole)
                    newPrsRole.errors.each { e ->
                        log.error(e)
                    }
            }
        }
        else {
            log.debug("problem saving new PersonRole")
        }
        
        ajaxList()
    }
}
