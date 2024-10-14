package de.laser
 
import de.laser.annotations.DebugInfo
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

    @DebugInfo(isInstEditor_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def deleteAddress() {
        Address obj = Address.get(params.id)
        if (accessService.hasAccessToAddress(obj)) {
            obj.delete() // TODO: check perms
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'address.label'), params.id])
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
    def deleteContact() {
        Contact obj = Contact.get(params.id)
        if (accessService.hasAccessToContact(obj)) {
            obj.delete() // TODO: check perms
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'contact.label'), params.id])
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
    def deletePersonRole() {
        PersonRole obj = PersonRole.get(params.id)
        if (obj) {
            obj.delete() // TODO: check perms
        }
        else {
            flash.error = message(code: 'default.noPermissions')
        }
        redirect(url: request.getHeader('referer'))
    }
}
