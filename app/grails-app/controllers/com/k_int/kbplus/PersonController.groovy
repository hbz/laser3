package com.k_int.kbplus

import grails.plugins.springsecurity.Secured
import org.springframework.dao.DataIntegrityViolationException

class PersonController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        redirect action: 'list', params: params
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [personInstanceList: Person.list(params), personInstanceTotal: Person.count()]
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def create() {
        switch (request.method) {
		case 'GET':
            def personInstance = new Person(params)
            addPersonRoles(personInstance)
            
        	[personInstance: personInstance]
			break
		case 'POST':
	        def personInstance = new Person(params)
	        if (!personInstance.save(flush: true)) {
	            render view: 'create', model: [personInstance: personInstance]
	            return
	        }
            addPersonRoles(personInstance)
            
			flash.message = message(code: 'default.created.message', args: [message(code: 'person.label', default: 'Person'), personInstance.id])
	        redirect action: 'show', id: personInstance.id
			break
		}
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def show() {
        def personInstance = Person.get(params.id)
        if (!personInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])
            redirect action: 'list'
            return
        }

        [personInstance: personInstance]
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit() {
		switch (request.method) {
		case 'GET':
	        def personInstance = Person.get(params.id)
	        if (!personInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])
	            redirect action: 'list'
	            return
	        }
            addPersonRoles(personInstance)
            deletePersonRoles(personInstance)
            
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
            addPersonRoles(personInstance)
            deletePersonRoles(personInstance)
            
			flash.message = message(code: 'default.updated.message', args: [message(code: 'person.label', default: 'Person'), personInstance.id])
	        redirect action: 'show', id: personInstance.id
			break
		}
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
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
    
    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def ajax() {        
        def person              = Person.get(params.id)         // current person
        def org                 = Org.get(params.org)           // referenced org for setting g:select value
        def personRoleRdv       = PersonRole.getAllRefdataValues()
        def prsLinks                                            // person depending person roles
        def allOrgs             = Org.getAll()
        def allSubjects                                         // all subjects of given type
        def subjectType                                         // type of subject
        def subjectOptionValue
        def timestamp           = System.currentTimeMillis()
        
        def cmd  = params.cmd
        def type = params.type
        
        if(cmd == 'list'){ 
        // lists existing personRoles with checkbox to delete
            
            if(person){
                def hqlPart = "from PersonRole as GOR where GOR.prs = ${person?.id}"  
                prsLinks    = PersonRole.findAll(hqlPart)

                render view: 'ajax/listPersonRoles', model: [
                    prsLinks: prsLinks
                ]
                return
            }
            else {
                render "No Data found."
                return
            }
        }
        
        else if(cmd == 'add'){
        // requesting form for adding new personRoles
            
            if(type == "org") {
                subjectType  = "personRole"
            }
            else if(type == "cluster") {
                allSubjects         = Cluster.getAll()
                subjectType         = "cluster"
                subjectOptionValue  = "name"
            }
            else if(type == "lic") {
                allSubjects         = License.getAll()
                subjectType         = "license"
                subjectOptionValue  = "reference"
            }
            else if(type == "pkg") {
                allSubjects         = Package.getAll()
                subjectType         = "package"
                subjectOptionValue  = "name"
            }
            else if(type == "sub") {
                allSubjects         = Subscription.getAll()
                subjectType         = "subscription"
                subjectOptionValue  = "name"
            }
            else if(type == "title") {
                allSubjects          = TitleInstance.getAll()
                subjectType          = "titleInstance"
                subjectOptionValue   = "normTitle"
            }
            
            render view: 'ajax/addPersonRole', model: [
                personInstance:     person,
                allOrgs:            allOrgs,
                allSubjects:        allSubjects,
                subjectType:        subjectType,
                subjectOptionValue: subjectOptionValue,
                prsLinks:           prsLinks,
                type:               type,
                roleTypes:          personRoleRdv,
                org:                org,
                timestamp:          timestamp
                ]
            return
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    private deletePersonRoles(Person prs){

        params?.personRoleDeleteIds?.each{ key, value ->
             def prsRole = PersonRole.get(value)
             if(prsRole) {
                 log.debug("deleting PersonRole ${prsRole}")
                 prsRole.delete(flush:true);
             }
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    private addPersonRoles(Person prs){
    
       params?.roleType?.each{ key, value ->     
           def result
           def prsRole  = RefdataValue.get(params.roleType[key])
           def org      = Org.get(params.org[key])
           def subject  // dynamic
           def type     = params.type[key]
           
           switch(type) {
               case "org":
                   result = new PersonRole(prs:prs, roleType:prsRole, org:org)
               break;
               case "cluster":
                   subject = Cluster.get(params.cluster[key])
                   result = new PersonRole(prs:prs, roleType:prsRole, org:org, cluster:subject)
               break;
               case"lic":
                   subject = License.get(params.lic[key])
                   result = new PersonRole(prs:prs, roleType:prsRole, org:org, lic:subject)
               break;
               case "pkg":
                   subject = Package.get(params.pkg[key])
                   result = new PersonRole(prs:prs, roleType:prsRole, org:org, pkg:subject)
               break;
               case "sub":
                   subject = Subscription.get(params.sub[key])
                   result = new PersonRole(prs:prs, roleType:prsRole, org:org, sub:subject)
               break;
               case "title":
                   subject = TitleInstance.get(params.title[key])
                   result = new PersonRole(prs:prs, roleType:prsRole, org:org, title:subject)
               break;
           }
           
           /*if(PersonRole.find("from PersonRole as GOR where GOR.prs = ${prs.id} and GOR.org = ${org.id} and GOR.roleType = ${prsRole.id} and GOR.${type} = ${target.id}")) {
               log.debug("ignore adding PersonRole because of existing duplicate")
           }
           else*/ if(result){
               if(result.save(flush:true)) {
                   log.debug("adding PersonRole ${result}")
               } else {
                   log.error("problem saving new PersonRole ${result}")
               }
           }
       }
    }
}
