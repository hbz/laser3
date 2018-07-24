package com.k_int.kbplus

import de.laser.helper.DebugAnnotation
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.dao.DataIntegrityViolationException
import com.k_int.kbplus.auth.User
import com.k_int.properties.*

@Secured(['IS_AUTHENTICATED_FULLY'])
class PersonController {

    def springSecurityService
    def addressbookService
    def genericOIDService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER'])
    def index() {
        redirect action: 'list', params: params
    }

    @Secured(['ROLE_ADMIN'])
    def list() {
        params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSize()
        [personInstanceList: Person.list(params), personInstanceTotal: Person.count()]
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
        def userMemberships = User.get(springSecurityService.principal.id).authorizedOrgs

        // TODO remove this fallback !!!!
        if(userMemberships.size() == 0){
            userMemberships = Org.list()
        }
        
        switch (request.method) {
		case 'GET':
            def personInstance = new Person(params)
            // processing dynamic form data
            addPersonRoles(personInstance)
            
        	[personInstance: personInstance, userMemberships: userMemberships]
			break
		case 'POST':
	        def personInstance = new Person(params)
	        if (!personInstance.save(flush: true)) {
                flash.error = message(code: 'default.not.created.message', args: [message(code: 'person.label', default: 'Person')])
                redirect(url: request.getHeader('referer'))
	            //render view: 'create', model: [personInstance: personInstance, userMemberships: userMemberships]
	            return
	        }
            // processing dynamic form data
            addPersonRoles(personInstance)
            
			flash.message = message(code: 'default.created.message', args: [message(code: 'person.label', default: 'Person'), personInstance.toString()])
            redirect action: 'show', id: personInstance.id
			break
		}
    }

    @Secured(['ROLE_USER'])
    def show() {
        def personInstance = Person.get(params.id)
        if (! personInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])
            //redirect action: 'list'
            redirect(url: request.getHeader('referer'))
            return
        }

        def result = [
                personInstance: personInstance,
                editable: addressbookService.isPersonEditable(personInstance, springSecurityService.getCurrentUser())
        ]

        if (result.editable) {
            if (params.cmd?.equalsIgnoreCase('deleteAddress')) {
                def obj = genericOIDService.resolveOID(params.oid)
                if (obj) {
                    obj.delete()
                }
                redirect(url: request.getHeader('referer'))
            }
            if (params.cmd?.equalsIgnoreCase('deleteContact')) {
                def obj = genericOIDService.resolveOID(params.oid)
                if (obj) {
                    obj.delete()
                }
                redirect(url: request.getHeader('referer'))
            }
        }

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
        def userMemberships = User.get(springSecurityService.principal.id).authorizedOrgs
        def personInstance = Person.get(params.id)

        if (! personInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])
            //redirect action: 'list'
            redirect(url: request.getHeader('referer'))
            return
        }
        if (! addressbookService.isPersonEditable(personInstance, springSecurityService.getCurrentUser())) {
            redirect action: 'show', id: params.id
            return
        }

		switch (request.method) {
		case 'GET':
            // processing dynamic form data
            addPersonRoles(personInstance)
            deletePersonRoles(personInstance)
            
            // current tenant must be present
            if (personInstance.tenant){
                userMemberships << personInstance.tenant 
            }
            
	        [personInstance: personInstance, userMemberships: userMemberships]
			break
		case 'POST':
	        if (params.version) {
	            def version = params.version.toLong()
	            if (personInstance.version > version) {
	                personInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'person.label', default: 'Person')] as Object[],
	                          "Another user has updated this Person while you were editing")
	                render view: 'show', model: [personInstance: personInstance, userMemberships: userMemberships]
	                return
	            }
	        }

	        personInstance.properties = params

	        if (! personInstance.save(flush: true)) {
	            render view: 'show', model: [personInstance: personInstance, userMemberships: userMemberships]
	            return
	        }
            // processing dynamic form data
            addPersonRoles(personInstance)
            deletePersonRoles(personInstance)
            
            // current tenant must be present
            if (personInstance.tenant){
                userMemberships << personInstance.tenant 
            }
            
			flash.message = message(code: 'default.updated.message', args: [message(code: 'person.label', default: 'Person'), personInstance.toString()])
	        redirect action: 'show', id: personInstance.id
			break
		}
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        def personInstance = Person.get(params.id)
        if (! personInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])
            //redirect action: 'list'
            redirect(url: request.getHeader('referer'))
            return
        }
        if (! addressbookService.isPersonEditable(personInstance, springSecurityService.getCurrentUser())) {
            redirect action: 'show', id: params.id
            return
        }

        try {
            personInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'person.label', default: 'Person'), params.id])
            //redirect action: 'list'
            redirect(url: request.getHeader('referer'))
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'person.label', default: 'Person'), params.id])
            redirect action: 'show', id: params.id
        }
    }
    
    @Secured(['ROLE_USER'])
    def properties() {
        def personInstance = Person.get(params.id)
        if (!personInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])
            redirect action: 'list'
            return
        }
        
        def user = User.get(springSecurityService.principal.id)
        def editable = SpringSecurityUtils.ifAllGranted('ROLE_ADMIN')

        // create mandatory PersonPrivateProperties if not existing

        def mandatories = []
        user?.authorizedOrgs?.each{ org ->
            def ppd = PropertyDefinition.findAllByDescrAndMandatoryAndTenant("Person Property", true, org)
            if(ppd){
                mandatories << ppd
            }
        }
        mandatories.flatten().each{ pd ->
            if (! PersonPrivateProperty.findWhere(owner: personInstance, type: pd)) {
                def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, personInstance, pd)

                if (newProp.hasErrors()) {
                    log.error(newProp.errors)
                } else {
                    log.debug("New person private property created via mandatory: " + newProp.type.name)
                }
            }
        }

        [personInstance: personInstance, authorizedOrgs: user?.authorizedOrgs, editable: editable]
    }
    
    @Secured(['ROLE_USER'])
    def ajax() {        
        def person                  = Person.get(params.id)
        def existingPrsLinks
        
        def allSubjects             // all subjects of given type
        def subjectType             // type of subject
        def subjectFormOptionValue
        
        def cmd      = params.cmd
        def roleType = params.roleType
        
        // requesting form for deleting existing personRoles
        if('list' == cmd){ 
            
            if(person){
                if('func' == roleType){
                    def rdc = RefdataCategory.findByDesc('Person Function')
                    def hqlPart = "from PersonRole as PR where PR.prs = ${person?.id} and PR.functionType.owner = ${rdc.id}"  
                    existingPrsLinks = PersonRole.findAll(hqlPart) 
                }
                else if('resp' == roleType){
                    def rdc = RefdataCategory.findByDesc('Person Responsibility')
                    def hqlPart = "from PersonRole as PR where PR.prs = ${person?.id} and PR.responsibilityType.owner = ${rdc.id}"  
                    existingPrsLinks = PersonRole.findAll(hqlPart)
                }

                render view: 'ajax/listPersonRoles', model: [
                    existingPrsLinks: existingPrsLinks
                ]
                return
            }
            else {
                render "No Data found."
                return
            }
        }
        
        // requesting form for adding new personRoles
        else if('add' == cmd){ 
            
            def roleRdv = RefdataValue.get(params.roleTypeId)

            if('func' == roleType){
                
                // only one rdv of person function
            }
            else if('resp' == roleType){
                
                if(roleRdv?.value == "Specific cluster editor") {
                    allSubjects             = Cluster.getAll()
                    subjectType             = "cluster"
                    subjectFormOptionValue  = "name"
                }
                else if(roleRdv?.value == "Specific license editor") {
                    allSubjects             = License.getAll()
                    subjectType             = "license"
                    subjectFormOptionValue  = "reference"
                }
                else if(roleRdv?.value == "Specific package editor") {
                    allSubjects             = Package.getAll()
                    subjectType             = "package"
                    subjectFormOptionValue  = "name"
                }
                else if(roleRdv?.value == "Specific subscription editor") {
                    allSubjects             = Subscription.getAll()
                    subjectType             = "subscription"
                    subjectFormOptionValue  = "name"
                }
                else if(roleRdv?.value == "Specific title editor") {
                    allSubjects             = TitleInstance.getAll()
                    subjectType             = "titleInstance"
                    subjectFormOptionValue  = "normTitle"
                }
            }
            
            render view: 'ajax/addPersonRole', model: [
                personInstance:     person,
                allOrgs:            Org.getAll(),
                allSubjects:        allSubjects,
                subjectType:        subjectType,
                subjectOptionValue: subjectFormOptionValue,
                existingPrsLinks:   existingPrsLinks,
                roleType:           roleType,
                roleRdv:            roleRdv,
                org:                Org.get(params.org),        // through passing for g:select value
                timestamp:          System.currentTimeMillis()
                ]
            return
        }
    }

    private deletePersonRoles(Person prs){

        params?.personRoleDeleteIds?.each{ key, value ->
             def prsRole = PersonRole.get(value)
             if(prsRole) {
                 log.debug("deleting PersonRole ${prsRole}")
                 prsRole.delete(flush:true);
             }
        }
    }

    private addPersonRoles(Person prs){

        //IF functionType not seleced
        if(!params?.functionType && params.org_id)
        {
            def result

            def roleRdv = RefdataValue.getByValueAndCategory('General contact person', 'Person Function')
            def org     = Org.get(params.org_id)

            if (roleRdv && org) {
                result = new PersonRole(prs: prs, functionType: roleRdv, org: org)

                if (PersonRole.find("from PersonRole as PR where PR.prs = ${prs.id} and PR.org = ${org.id} and PR.functionType = ${roleRdv.id}")) {
                    log.debug("ignore adding PersonRole because of existing duplicate")
                }
                else if (result) {
                    if (result.save(flush:true)) {
                        log.debug("adding PersonRole ${result}")
                    }
                    else {
                        log.error("problem saving new PersonRole ${result}")
                    }
                }
            }
        }

        params?.functionType?.each{ key, value ->
            def result
            
            def roleRdv = RefdataValue.get(params.functionType[key])
            def org     = Org.get(params.org[key])

            if (roleRdv && org) {
                result = new PersonRole(prs: prs, functionType: roleRdv, org: org)

                if (PersonRole.find("from PersonRole as PR where PR.prs = ${prs.id} and PR.org = ${org.id} and PR.functionType = ${roleRdv.id}")) {
                    log.debug("ignore adding PersonRole because of existing duplicate")
                }
                else if (result) {
                    if (result.save(flush:true)) {
                        log.debug("adding PersonRole ${result}")
                    }
                    else {
                        log.error("problem saving new PersonRole ${result}")
                    }
                }
            }
       }
        
       params?.responsibilityType?.each{ key, value ->
           def result

           def roleRdv      = RefdataValue.get(params.responsibilityType[key])
           def org          = Org.get(params.org[key])

           if (roleRdv && org) {
               def subject      // dynamic
               def subjectType = params.subjectType[key]

               switch (subjectType) {
                   case "cluster":
                       if (params.cluster) {
                           subject = Cluster.get(params.cluster[key])
                           result = new PersonRole(prs: prs, responsibilityType: roleRdv, org: org, cluster: subject)
                       }
                       break;
                   case "license":
                       if (params.license) {
                           subject = License.get(params.license[key])
                           result = new PersonRole(prs: prs, responsibilityType: roleRdv, org: org, lic: subject)
                       }
                       break;
                   case "package":
                       if (params.package) {
                           subject = Package.get(params.package[key])
                           result = new PersonRole(prs: prs, responsibilityType: roleRdv, org: org, pkg: subject)
                       }
                       break;
                   case "subscription":
                       if (params.subscription) {
                           subject = Subscription.get(params.subscription[key])
                           result = new PersonRole(prs: prs, responsibilityType: roleRdv, org: org, sub: subject)
                       }
                       break;
                   case "titleInstance":
                       if (params.titleInstance) {
                           subject = TitleInstance.get(params.titleInstance[key])
                           result = new PersonRole(prs: prs, responsibilityType: roleRdv, org: org, title: subject)
                       }
                       break;
               }
           }

           // TODO duplicate check
           /* if(PersonRole.find("from PersonRole as PR where PR.prs = ${prs.id} and PR.org = ${org.id} and PR.responsibilityType = ${roleRdv.id} and PR.${typeTODOHERE} = ${subject.id}")) {
               log.debug("ignore adding PersonRole because of existing duplicate")
           }
           else */ if (result) {
               if (result.save(flush:true)) {
                   log.debug("adding PersonRole ${result}")
               }
               else {
                   log.error("problem saving new PersonRole ${result}")
               }
           }
       }
    }
}
