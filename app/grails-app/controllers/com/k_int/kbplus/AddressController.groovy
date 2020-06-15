package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

@Secured(['IS_AUTHENTICATED_FULLY'])
class AddressController extends AbstractDebugController {

	def springSecurityService
	def addressbookService
    def contextService
    def formService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER'])
    def index() {
        redirect action: 'list', params: params
    }

    @Secured(['ROLE_ADMIN'])
    def list() {
		params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSizeTMP()
        [addressInstanceList: Address.list(params), addressInstanceTotal: Address.count()]
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
		switch (request.method) {
			case 'GET':
				[addressInstance: new Address(params)]
				break
			case 'POST':
                if (formService.validateToken(params)) {

                    Address addressInstance = new Address(params)
                    if (!addressInstance.save(flush: true)) {
                        flash.error = message(code: 'default.save.error.message', args: [message(code: 'address.label')
                                                                                                 + addressInstance.id])
                        redirect(url: request.getHeader('referer'), params: params)
                        return
                    }

                    flash.message = message(code: 'default.created.message', args: [message(code: 'address.label'), addressInstance.id])
                }
                redirect(url: request.getHeader('referer'), params: params)
			break
		}
    }

    @Secured(['ROLE_USER'])
    def show() {
        Address addressInstance = Address.get(params.id)
        if (! addressInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label'), params.id])
            redirect(url: request.getHeader('referer'))
            return
        }
        String modalId = ''
        switch (addressInstance.type){
            case RDStore.ADRESS_TYPE_POSTAL:
                modalId = "addressFormModalPostalAddress"
                break
            case RDStore.ADRESS_TYPE_BILLING:
                modalId = "addressFormModalBillingAddress"
                break
            case RDStore.ADRESS_TYPE_LEGAL_PATRON:
                modalId = "addressFormModalLegalPatronAddress"
                break
            case RDStore.ADRESS_TYPE_DELIVERY:
                modalId = "addressFormModalDeliveryAddress"
                break
            case RDStore.ADRESS_TYPE_LIBRARY:
                modalId = "addressFormModalLibraryAddress"
                break
        }
        Map model = [
            addressInstance: addressInstance,
            orgId: addressInstance.org?.id,
            prsId: addressInstance.prs?.id,
            modalId: modalId,
            editable: addressbookService.isAddressEditable(addressInstance, springSecurityService.getCurrentUser()),
            redirect: '.',
            hideType: true
        ]
        render template: "/address/formModal", model: model
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
        Address addressInstance = Address.get(params.id)
        if (! addressInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label'), params.id])
            redirect(url: request.getHeader('referer'))
            return
        }
        if (! addressbookService.isAddressEditable(addressInstance, springSecurityService.getCurrentUser())) {
            redirect(url: request.getHeader('referer'))
            return
        }
        if (params.version) {
            def version = params.version.toLong()
            if (addressInstance.version > version) {
                addressInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
                          [message(code: 'address.label')] as Object[],
                          "Another user has updated this Address while you were editing")
                redirect(url: request.getHeader('referer'))
                return
            }
        }

        addressInstance.properties = params

        if (! addressInstance.save(flush: true)) {
            redirect(url: request.getHeader('referer'))
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'address.label'), addressInstance.id])
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        Address addressInstance = Address.get(params.id)
        if (! addressInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label'), params.id])
            redirect action: 'list'
            return
        }
        if (! addressbookService.isAddressEditable(addressInstance, springSecurityService.getCurrentUser())) {
            redirect action: 'show', id: params.id
            return
        }

        try {
            addressInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'address.label'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'address.label'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
