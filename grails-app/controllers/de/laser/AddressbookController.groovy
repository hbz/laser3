package de.laser
 
import de.laser.annotations.DebugInfo
import de.laser.survey.SurveyOrg
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
    GenericOIDService genericOIDService

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
            switch (request.method) {
                case 'GET':
                    [addressInstance: new Address(params)]
                    break
                case 'POST':
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
                    break
            }
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
            if (accessService.hasAccessToAddress(obj) || addressbookService.isAddressEditable(obj, contextService.getUser())) {
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

        Address.withTransaction {
            Address addressInstance = Address.get(params.id)
            if (!addressInstance) {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label'), params.id]) as String
                redirect(url: request.getHeader('referer'))
                return
            }
            if (!addressbookService.isAddressEditable(addressInstance, contextService.getUser())) {
                redirect(url: request.getHeader('referer'))
                return
            }
            if (params.version) {
                Long version = params.long('version')
                if (addressInstance.version > version) {
                    addressInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
                            [message(code: 'address.label')] as Object[],
                            "Another user has updated this Address while you were editing")
                    redirect(url: request.getHeader('referer'))
                    return
                }
            }

            addressInstance.properties = params

            List<RefdataValue> typesToRemove = []
            addressInstance.type.each {
                if (!(it.toString() in params.list('type.id'))) {
                    typesToRemove << it
                }
            }

            typesToRemove.each {
                addressInstance.removeFromType(it)
            }

            params.list('type.id').each {
                if (!(it in addressInstance.type)) {
                    addressInstance.addToType(RefdataValue.get(Long.parseLong(it)))
                }
            }

            String referer = request.getHeader('referer')
            if(!referer.contains('tab')) {
                if(referer.contains('?'))
                    referer += '&tab=addresses'
                else
                    referer += '?tab=addresses'
            }
            else referer = referer.replaceAll('tab=contacts', 'tab=addresses')
            if (!addressInstance.save()) {
                redirect(url: referer)
                return
            }

            flash.message = message(code: 'default.updated.message', args: [message(code: 'address.label'), (addressInstance.name ?: '')]) as String
            redirect(url: referer)
        }
    }
}
