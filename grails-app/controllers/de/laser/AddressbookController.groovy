package de.laser
 
import de.laser.annotations.DebugInfo
import de.laser.storage.RDStore
import de.laser.survey.SurveyOrg
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller is responsible for contact address display and manipulation
 * @see Address
 * @see Contact
 * @see Person
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class AddressbookController {

    AccessService accessService
    AddressbookService addressbookService
    ContextService contextService
    FormService formService

    @DebugInfo(isInstUser_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
    })
    def index() {
        redirect controller: 'myInstitution', action: 'addressbook'
    }

    // --------------------------------- CREATE ---------------------------------

    /**
     * Creates a new address with the given parameters
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = [], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def createAddress() {
        // moved from AddressController.createAddress()

        Address.withTransaction {
            String referer = request.getHeader('referer')
            if(!referer.contains('tab')) {
                if(referer.contains('?'))
                    referer += '&tab=addresses'
                else
                    referer += '?tab=addresses'
            }
            else referer = referer.replaceAll('tab=contacts', 'tab=addresses')

            if (formService.validateToken(params)) {
                Address addressInstance = new Address()

                params.list('type.id').each {
                    if (!(it in addressInstance.type)) {
                        addressInstance.addToType(RefdataValue.get(Long.parseLong(it)))
                    }
                }

                params.remove('type.id')

                addressInstance.properties = params
                if (!addressInstance.save()) {
                    flash.error = message(code: 'default.save.error.general.message') as String
                    log.error('Adresse konnte nicht gespeichert werden. ' + addressInstance.errors)
                    redirect(url: referer)
                    return
                }

                flash.message = message(code: 'default.created.message', args: [message(code: 'address.label'), addressInstance.name]) as String
            }
            redirect(url: referer)
        }
    }

    /**
     * Takes submitted parameters and creates a new person contact instance based on the
     * given parameter map
     * @return redirect back to the referer -> an updated list of person contacts
     * @see Person
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = [], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def createPerson() {
        // moved from PersonController.createPerson()

        Person.withTransaction {
            Org contextOrg = contextService.getOrg()

            String referer = request.getHeader('referer')
            if(!referer.contains('tab')) {
                if(referer.contains('?'))
                    referer += '&tab=contacts'
                else
                    referer += '?tab=contacts'
            }
            else referer = referer.replaceAll('tab=addresses', 'tab=contacts')

            if (formService.validateToken(params)) {
                if(params.functionType || params.positionType)  {
                    Person personInstance = new Person(params)
                    if (!personInstance.save()) {
                        flash.error = message(code: 'default.not.created.message', args: [message(code: 'person.label')]) as String
                        log.debug("Person could not be created: " + personInstance.errors)
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    // processing dynamic form data
                    //addPersonRoles(personInstance)
                    Org personRoleOrg
                    Provider personRoleProvider
                    Vendor personRoleVendor
                    if (params.personRoleOrg) {
                        personRoleOrg = Org.get(params.personRoleOrg)
                    }
                    else if (params.personRoleProvider) {
                        personRoleProvider = Provider.get(params.personRoleProvider)
                    }
                    else if (params.personRoleVendor) {
                        personRoleVendor = Vendor.get(params.personRoleVendor)
                    }
                    else {
                        personRoleOrg = contextOrg
                    }

                    if (params.functionType) {
                        params.list('functionType').each {
                            PersonRole personRole
                            RefdataValue functionType = RefdataValue.get(it)
                            personRole = new PersonRole(prs: personInstance, functionType: functionType)
                            if(personRoleOrg)
                                personRole.org = personRoleOrg
                            else if(personRoleProvider)
                                personRole.provider = personRoleProvider
                            else if(personRoleVendor)
                                personRole.vendor = personRoleVendor

                            if (PersonRole.findWhere(prs: personInstance, org: personRoleOrg, vendor: personRoleVendor, provider: personRoleProvider, functionType: functionType)) {
                                log.debug("ignore adding PersonRole because of existing duplicate")
                            }
                            else if (personRole) {
                                if (personRole.save()) {
                                    log.debug("adding PersonRole ${personRole}")
                                }
                                else {
                                    log.error("problem saving new PersonRole ${personRole}")
                                }
                            }
                        }
                    }

                    if (params.positionType) {
                        params.list('positionType').each {
                            PersonRole personRole
                            RefdataValue positionType = RefdataValue.get(it)
                            personRole = new PersonRole(prs: personInstance, positionType: positionType)
                            if(personRoleOrg)
                                personRole.org = personRoleOrg
                            else if(personRoleProvider)
                                personRole.provider = personRoleProvider
                            else if(personRoleVendor)
                                personRole.vendor = personRoleVendor

                            if (PersonRole.findWhere(prs: personInstance, org: personRoleOrg, vendor: personRoleVendor, provider: personRoleProvider, positionType: positionType)) {
                                log.debug("ignore adding PersonRole because of existing duplicate")
                            }
                            else if (personRole) {
                                if (personRole.save()) {
                                    log.debug("adding PersonRole ${personRole}")
                                }
                                else {
                                    log.error("problem saving new PersonRole ${personRole}")
                                }
                            }
                        }
                    }

                    if (params.content) {
                        params.list('content').eachWithIndex { content, i ->
                            if (content) {
                                RefdataValue rdvCT = RefdataValue.get(params.list('contentType.id')[i])
                                RefdataValue contactLang = params['contactLang.id'] ? RefdataValue.get(params['contactLang.id']) : null
                                if (RDStore.CCT_EMAIL == rdvCT) {
                                    if (!formService.validateEmailAddress(content)) {
                                        flash.error = message(code: 'contact.create.email.error') as String
                                        return
                                    }
                                }
                                Contact contact = new Contact(prs: personInstance, contentType: rdvCT, language: contactLang, type: RDStore.CONTACT_TYPE_JOBRELATED, content: content)
                                contact.save()
                            }
                        }
                    }
                    flash.message = message(code: 'default.created.message', args: [message(code: 'person.label'), personInstance.toString()]) as String
                }
                else flash.error = message(code: 'person.create.missing_function') as String
            }
            redirect(url: referer)
        }
    }

    // --------------------------------- DELETE ---------------------------------

    @DebugInfo(isInstEditor_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def deleteAddress() {
        Address obj = Address.get(params.id)
        List args   = [message(code: 'address.label'), params.id]

        if (obj) {
            if (accessService.hasAccessToAddress(obj) || addressbookService.isAddressEditable(obj, contextService.getUser())) { // TODO
                Address.withTransaction {
                    try {
                        List changeList = SurveyOrg.findAllByAddress(obj)
                        changeList.each { tmp2 ->
                            tmp2.address = null
                            tmp2.save()
                        }
                        obj.delete() // TODO: check perms

                        flash.message = message(code: 'default.deleted.message', args: args)
                    }
                    catch (Exception e) {
                        log.debug(e.getMessage())
                        flash.error = message(code: 'default.not.deleted.message', args: args)
                    }
                }
            }
            else {
                flash.error = message(code: 'default.noPermissions')
            }
        }
        else {
            flash.error = message(code: 'default.not.found.message', args: args)
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugInfo(isInstEditor_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def deleteContact() {
        Contact obj = Contact.get(params.id)
        List args   = [message(code: 'contact.label'), params.id]

        if (accessService.hasAccessToContact(obj)) {
            try {
                obj.delete() // TODO: check perms
                flash.message = message(code: 'default.deleted.message', args: args)
            }
            catch (Exception e) {
                log.debug(e.getMessage())
                flash.error = message(code: 'default.not.deleted.message', args: args)
            }
        }
        else {
            flash.error = message(code: 'default.noPermissions')
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugInfo(isInstEditor_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def deletePerson() {
        Person obj = Person.get(params.id)
        List args  = [message(code: 'person.label'), params.id]

        if (obj) {
            if (accessService.hasAccessToPerson(obj) || addressbookService.isPersonEditable(obj, contextService.getUser())) { // TODO
                Person.withTransaction {
                    try {
                        List changeList = SurveyOrg.findAllByPerson(obj)
                        changeList.each { tmp2 ->
                            tmp2.person = null
                            tmp2.save()
                        }
                        obj.delete() // TODO: check perms

                        flash.message = message(code: 'default.deleted.message', args: args)
                    }
                    catch (Exception e) {
                        log.debug(e.getMessage())
                        flash.error = message(code: 'default.not.deleted.message', args: args)
                    }
                }
            }
            else {
                flash.error = message(code: 'default.noPermissions')
            }
        }
        else {
            flash.error = message(code: 'default.not.found.message', args: args)
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugInfo(isInstEditor_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def deletePersonRole() {
        PersonRole obj = PersonRole.get(params.id)
        if (obj) {
            try {
                obj.delete() // TODO: check perms
            }
            catch (Exception e) {
                log.debug(e.getMessage())
                flash.error = message(code: 'default.delete.error.general.message')
            }
        }
        else {
            flash.error = message(code: 'default.delete.error.general.message')
        }
        redirect(url: request.getHeader('referer'))
    }

    // --------------------------------- EDIT ---------------------------------

    /**
     * Updates the given address with the given updated data
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = [], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def editAddress() {
        // moved from AddressController.editAddress()
        Address obj     = Address.get(params.id)
        List args       = [message(code: 'address.label'), params.id]
        String referer  = request.getHeader('referer')

        if (obj) {
            if (accessService.hasAccessToAddress(obj) || addressbookService.isAddressEditable(obj, contextService.getUser())) { // TODO
                if (params.version) {
                    Long version = params.long('version')
                    if (obj.version > version) {
                        obj.errors.rejectValue('version', 'default.optimistic.locking.failure',
                                [message(code: 'address.label')] as Object[],
                                "Another user has updated this Address while you were editing")
                        redirect(url: referer)
                        return
                    }
                }

                Address.withTransaction {
                    try {
                        obj.properties = params

                        List<RefdataValue> typesToRemove = []
                        obj.type.each {
                            if (!(it.toString() in params.list('type.id'))) {
                                typesToRemove << it
                            }
                        }

                        typesToRemove.each {
                            obj.removeFromType(it)
                        }

                        params.list('type.id').each {
                            if (!(it in obj.type)) {
                                obj.addToType(RefdataValue.get(Long.parseLong(it)))
                            }
                        }

                        if (!referer.contains('tab')) {
                            if(referer.contains('?'))
                                referer += '&tab=addresses'
                            else
                                referer += '?tab=addresses'
                        }
                        else {
                            referer = referer.replaceAll('tab=contacts', 'tab=addresses')
                        }

                        if (obj.save()) {
                            flash.message = message(code: 'default.updated.message', args: args)
                        }
                        else {
                            flash.error = message(code: 'default.save.error.general.message')
                        }
                    }
                    catch (Exception e) {
                        log.debug(e.getMessage())
                        flash.error = message(code: 'default.save.error.general.message')
                    }
                }
            }
            else {
                flash.error = message(code: 'default.noPermissions')
            }
        }
        else {
            flash.error = message(code: 'default.not.found.message', args: args)
        }
        redirect(url: referer)
    }
}
