package de.laser.ajax

import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Subscription
import de.laser.Address
import de.laser.Doc
import de.laser.Person
import de.laser.Task
import de.laser.helper.RDStore
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll'])
class AjaxHtmlController {

    /**
     * only template rendering here ..
     * no object manipulation
     *
     */

    def addressbookService
    def contextService
    def genericOIDService
    def taskService

    def test() {
        String result = '<p data-status="ok">OK'
        if (params.id) {
            result += ', ID:' + params.id
        }
        result += '</p>'
        render result
    }

    @Secured(['ROLE_USER'])
    def addressFields() {
        render template: "/templates/cpa/addressFields"
    }

    @Secured(['ROLE_USER'])
    def contactFields() {
        render template: "/templates/cpa/contactFields"
    }

    @Secured(['ROLE_USER'])
    def getLicensePropertiesForSubscription() {
        License loadFor = (License) genericOIDService.resolveOID(params.loadFor)
        if (loadFor) {
            Map<String,Object> derivedPropDefGroups = loadFor._getCalculatedPropDefGroups(contextService.org)
            render view: '/subscription/_licProp', model: [license: loadFor, derivedPropDefGroups: derivedPropDefGroups, linkId: params.linkId]
        }
    }

    @Secured(['ROLE_USER'])
    def createAddress() {
        Map<String, Object> result = [:]
        result.orgId = params.orgId
        result.prsId = params.prsId
        result.redirect = params.redirect
        result.typeId = Long.valueOf(params.typeId)
        result.hideType = params.hideType

        if (result.orgId && result.typeId) {
            String messageCode = 'addressFormModalLibraryAddress'

            if (result.typeId == RDStore.ADRESS_TYPE_LEGAL_PATRON.id)  {
                messageCode = 'addressFormModalLegalPatronAddress'
            }
            else if (result.typeId == RDStore.ADRESS_TYPE_BILLING.id)  {
                messageCode = 'addressFormModalBillingAddress'
            }
            else if (result.typeId == RDStore.ADRESS_TYPE_POSTAL.id)   {
                messageCode = 'addressFormModalPostalAddress'
            }
            else if (result.typeId == RDStore.ADRESS_TYPE_DELIVERY.id) {
                messageCode = 'addressFormModalDeliveryAddress'
            }
            else if (result.typeId == RDStore.ADRESS_TYPE_LIBRARY.id)  {
                messageCode = 'addressFormModalLibraryAddress'
            }

            result.modalText = message(code: 'default.create.label', args: [message(code: messageCode)])
        } else {
            result.modalText = message(code: 'default.new.label', args: [message(code: 'person.address.label')])
        }
        result.modalMsgSave = message(code: 'default.button.create.label')
        result.url = [controller: 'address', action: 'create']

        render template: "/templates/cpa/addressFormModal", model: result
    }

    @Secured(['ROLE_USER'])
    def editAddress() {
        Map<String, Object> result = [:]
        result.addressInstance = Address.get(params.id)
        if (result.addressInstance){
            result.modalId = 'addressFormModal'

            String messageCode = 'person.address.label'
            switch (result.addressInstance.type){
                case RDStore.ADRESS_TYPE_LEGAL_PATRON:
                    messageCode = 'addressFormModalLegalPatronAddress'
                    break
                case RDStore.ADRESS_TYPE_BILLING:
                    messageCode = 'addressFormModalBillingAddress'
                    break
                case RDStore.ADRESS_TYPE_POSTAL:
                    messageCode = 'addressFormModalPostalAddress'
                    break
                case RDStore.ADRESS_TYPE_DELIVERY:
                    messageCode = 'addressFormModalDeliveryAddress'
                    break
                case RDStore.ADRESS_TYPE_LIBRARY:
                    messageCode = 'addressFormModalLibraryAddress'
                    break
            }

            result.typeId = result.addressInstance.type.id
            result.modalText = message(code: 'default.edit.label', args: [message(code: messageCode)])
            result.modalMsgSave = message(code: 'default.button.save_changes')
            result.url = [controller: 'address', action: 'edit']

            render template: "/templates/cpa/addressFormModal", model: result
        }
    }

    @Secured(['ROLE_USER'])
    def editNote() {
        Map<String, Object> result = [:]
        result.params = params
        result.noteInstance = Doc.get(params.id)

        render template: "/templates/notes/modal_edit", model: result
    }

    @Secured(['ROLE_USER'])
    def readNote() {
        Map<String, Object> result = [:]
        result.params = params
        result.noteInstance = Doc.get(params.id)

        render template: "/templates/notes/modal_read", model: result
    }

    @Secured(['ROLE_USER'])
    def editPerson() {
        Map<String, Object> result = [:]

        result.personInstance = Person.get(params.id)
        if (result.personInstance){
            result.modalId = 'personEditModal'
            result.modalText = message(code: 'default.edit.label', args: [message(code: 'person.label')])
            result.modalMsgSave = message(code: 'default.button.save_changes')
            result.showContacts = params.showContacts == "true" ? true : ''
            result.addContacts = params.showContacts == "true" ? true : ''
            result.showAddresses = params.showAddresses == "true" ? true : ''
            result.addAddresses = params.showAddresses == "true" ? true : ''
            result.editable = addressbookService.isPersonEditable(result.personInstance, contextService.getUser())
            result.url = [controller: 'person', action: 'edit', id: result.personInstance.id]
            result.contextOrg = contextService.getOrg()

            render template: "/templates/cpa/personFormModal", model: result
        }
    }

    @Secured(['ROLE_USER'])
    def createTask() {
        long backendStart = System.currentTimeMillis()
        Org contextOrg = contextService.getOrg()
        Map<String, Object> result = taskService.getPreconditions(contextOrg)

        result.backendStart = backendStart

        render template: "/templates/tasks/modal_create", model: result
    }

    @Secured(['ROLE_USER'])
    def editTask() {
        Org contextOrg = contextService.getOrg()
        Map<String, Object> result = taskService.getPreconditionsWithoutTargets(contextOrg)

        result.params = params
        result.taskInstance = Task.get(params.id)

        if (result.taskInstance){
            render template: "/templates/tasks/modal_edit", model: result
//        } else {
//            flash.error = "Diese Aufgabe existiert nicht (mehr)."
//            redirect(url: request.getHeader('referer'))
        }
    }
}