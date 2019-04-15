package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.dao.DataIntegrityViolationException
import com.k_int.kbplus.auth.User
import com.k_int.properties.*

@Secured(['IS_AUTHENTICATED_FULLY'])
class PersonController extends AbstractDebugController {

    def springSecurityService
    def addressbookService
    def genericOIDService
    def contextService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER'])
    def index() {
        redirect controller: 'myInstitution', action: 'addressbook'
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
        def userMemberships = User.get(springSecurityService.principal.id).authorizedOrgs
        
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
            redirect(url: request.getHeader('referer'))
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
        else if(personInstance && personInstance.isPublic.equals(RDStore.YN_NO)) {
            if(contextService.org != personInstance.tenant && !SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
                redirect(url: request.getHeader('referer'))
                return
            }
        }

        def result = [
                personInstance: personInstance,
                editable: addressbookService.isPersonEditable(personInstance, springSecurityService.getCurrentUser())
        ]

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
        redirect controller: 'person', action: 'show', params: params
        return // ----- deprecated

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
            //redirect action: 'list'
            redirect(url: request.getHeader('referer'))
            return
        }
        
        def user = User.get(springSecurityService.principal.id)
        def editable = SpringSecurityUtils.ifAllGranted('ROLE_ADMIN')

        // create mandatory PersonPrivateProperties if not existing

        def org = contextService.getOrg()
        def mandatories = PropertyDefinition.findAllByDescrAndMandatoryAndTenant("Person Property", true, org)

        mandatories.each{ pd ->
            if (! PersonPrivateProperty.findWhere(owner: personInstance, type: pd)) {
                def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, personInstance, pd)

                if (newProp.hasErrors()) {
                    log.error(newProp.errors)
                } else {
                    log.debug("New person private property created via mandatory: " + newProp.type.name)
                }
            }
        }

        [personInstance: personInstance, editable: editable]
    }

    @Deprecated
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

    @Deprecated
    private deletePersonRoles(Person prs){

        params?.personRoleDeleteIds?.each{ key, value ->
             def prsRole = PersonRole.get(value)
             if(prsRole) {
                 log.debug("deleting PersonRole ${prsRole}")
                 prsRole.delete(flush:true);
             }
        }
    }


    def addPersonRole() {
        def result
        def prs = Person.get(params.id)

        if (addressbookService.isPersonEditable(prs, springSecurityService.getCurrentUser())) {

            if (params.newPrsRoleOrg && params.newPrsRoleType) {
                def org = Org.get(params.newPrsRoleOrg)
                def rdv = RefdataValue.get(params.newPrsRoleType)

                def prAttr = params.roleType ?: PersonRole.TYPE_FUNCTION

                if (prAttr in [PersonRole.TYPE_FUNCTION, PersonRole.TYPE_POSITION]) {

                    if (PersonRole.find("from PersonRole as PR where PR.prs = ${prs.id} and PR.org = ${org.id} and PR.${prAttr} = ${rdv.id}")) {
                        log.debug("ignore adding PersonRole because of existing duplicate")
                    }
                    else {
                        result = new PersonRole(prs: prs, org: org)
                        result."${prAttr}" = rdv

                        if (result.save(flush: true)) {
                            log.debug("adding PersonRole ${result}")
                        }
                        else {
                            log.error("problem saving new PersonRole ${result}")
                        }
                    }
                }
            }
        }
        redirect action: 'show', id: params.id
    }

    def deletePersonRole() {
        def prs = Person.get(params.id)

        if (addressbookService.isPersonEditable(prs, springSecurityService.getCurrentUser())) {

            if (params.oid) {
                def pr = genericOIDService.resolveOID(params.oid)

                if (pr && (pr.prs.id == prs.id) && pr.delete()) {
                    log.debug("deleted PersonRole ${pr}")
                } else {
                    log.debug("problem deleting PersonRole ${pr}")
                }
            }
        }
        redirect action: 'show', id: params.id
    }

    @Deprecated
    private addPersonRoles(Person prs){

        if (params.functionType) {
            def result

            def functionRdv = RefdataValue.get(params.functionType) ?: RefdataValue.getByValueAndCategory('General contact person', 'Person Function')
            def functionOrg = Org.get(params.functionOrg)

            if (functionRdv && functionOrg) {
                result = new PersonRole(prs: prs, functionType: functionRdv, org: functionOrg)

                if (PersonRole.find("from PersonRole as PR where PR.prs = ${prs.id} and PR.org = ${functionOrg.id} and PR.functionType = ${functionRdv.id}")) {
                    log.debug("ignore adding PersonRole because of existing duplicate")
                }
                else if (result) {
                    if (result.save(flush: true)) {
                        log.debug("adding PersonRole ${result}")
                    }
                    else {
                        log.error("problem saving new PersonRole ${result}")
                    }
                }
            }

            def positionRdv = params.positionType ? RefdataValue.get(params.positionType) : null
            def positionOrg = Org.get(params.positionOrg)

            if (positionRdv && positionOrg) {
                result = new PersonRole(prs: prs, positionType: positionRdv, org: positionOrg)

                if (PersonRole.find("from PersonRole as PR where PR.prs = ${prs.id} and PR.org = ${positionOrg.id} and PR.positionType = ${positionRdv.id}")) {
                    log.debug("ignore adding PersonRole because of existing duplicate")
                }
                else if (result) {
                    if (result.save(flush: true)) {
                        log.debug("adding PersonRole ${result}")
                    }
                    else {
                        log.error("problem saving new PersonRole ${result}")
                    }
                }
            }
        }

        //@Deprecated
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
