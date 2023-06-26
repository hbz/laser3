package de.laser
 
import de.laser.annotations.DebugInfo
import de.laser.storage.RDStore
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

/**
 * This controller is responsible for contact address display and manipulation
 * @see Address
 * @see Contact
 * @see Person
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class AddressController  {

    AddressbookService addressbookService
    ContextService contextService
    FormService formService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    /**
     * Index call
     * @return the addressbook of the context institution
     */
    @Secured(['ROLE_USER'])
    def index() {
        redirect controller: 'myInstitution', action: 'addressbook'
    }

    /**
     * Creates a new address with the given parameters
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = true, wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def create() {
        Address.withTransaction {
            switch (request.method) {
                case 'GET':
                    [addressInstance: new Address(params)]
                    break
                case 'POST':
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
                            redirect(url: request.getHeader('referer'), params: params)
                            return
                        }

                        flash.message = message(code: 'default.created.message', args: [message(code: 'address.label'), addressInstance.name]) as String
                    }

                    redirect(url: request.getHeader('referer'), params: params)
                    break
            }
        }
    }

    /**
     * Shows the given address details
     * @return a modal containing the address details
     */
    @Secured(['ROLE_USER'])
    def show() {
        Address addressInstance = Address.get(params.id)
        if (! addressInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label'), params.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        String messageCode
        switch (addressInstance.type){
            case RDStore.ADDRESS_TYPE_POSTAL:
                messageCode = "addressFormModalPostalAddress"
                break
            case RDStore.ADDRESS_TYPE_BILLING:
                messageCode = "addressFormModalBillingAddress"
                break
            case RDStore.ADDRESS_TYPE_LEGAL_PATRON:
                messageCode = "addressFormModalLegalPatronAddress"
                break
            case RDStore.ADDRESS_TYPE_DELIVERY:
                messageCode = "addressFormModalDeliveryAddress"
                break
            case RDStore.ADDRESS_TYPE_LIBRARY:
                messageCode = "addressFormModalLibraryAddress"
                break
        }
        Map model = [
            addressInstance: addressInstance,
            orgId: addressInstance.org?.id,
            prsId: addressInstance.prs?.id,
            typeId: addressInstance.type?.id,
            modalText: messageCode?
                    message(code: 'default.edit.label', args: [message(code: messageCode)]) :
                    message(code: 'default.new.label', args: [message(code: 'person.address.label')]),
            editable: addressbookService.isAddressEditable(addressInstance, contextService.getUser()),
            redirect: '.',
            hideType: true
        ]
        render template: "/templates/cpa/addressFormModal", model: model
    }

    /**
     * Updates the given address with the given updated data
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = true, wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def edit() {
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

            if (!addressInstance.save()) {
                redirect(url: request.getHeader('referer'))
                return
            }

            flash.message = message(code: 'default.updated.message', args: [message(code: 'address.label'), (addressInstance.name ?: '')]) as String
            redirect(url: request.getHeader('referer'))
        }
    }

    /**
     * Deletes the given address
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = true, wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def delete() {
        Address.withTransaction {
            Address addressInstance = Address.get(params.id)
            if (!addressInstance) {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label'), params.id]) as String
                redirect action: 'list'
                return
            }
            if (!addressbookService.isAddressEditable(addressInstance, contextService.getUser())) {
                redirect action: 'show', id: params.id
                return
            }

            try {
                addressInstance.delete()
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'address.label'), params.id]) as String
                redirect action: 'list'
                return
            }
            catch (DataIntegrityViolationException e) {
                flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'address.label'), params.id]) as String
                redirect action: 'show', id: params.id
                return
            }
        }
    }
}
