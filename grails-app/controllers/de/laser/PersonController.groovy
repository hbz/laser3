package de.laser

import de.laser.addressbook.Contact
import de.laser.annotations.DebugInfo
import de.laser.helper.Params
import de.laser.storage.RDStore
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages person-contact related calls
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class PersonController  {

    AddressbookService addressbookService
    ContextService contextService
    FormService formService
    GenericOIDService genericOIDService

    static allowedMethods = [editPerson: ['GET', 'POST']]

    /**
     * Redirects to the addressbook of the context institution
     * @return the list view of the context institution's contacts
     * @see MyInstitutionController#addressbook()
     */
    @Secured(['ROLE_USER'])
    def index() {
        redirect controller: 'myInstitution', action: 'addressbook'
    }

    /**
     * Takes the submitted parameters and updates the person contact based on the given parameter map
     * @return redirect to the referer -> the updated view of the person contact
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = [], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def editPerson() {

        Person.withTransaction {
            Org contextOrg = contextService.getOrg()
            Person personInstance = Person.get(params.id)
            String referer = request.getHeader('referer')
            if(!referer.contains('tab')) {
                if(referer.contains('?'))
                    referer += '&tab=contacts'
                else
                    referer += '?tab=contacts'
            }
            else referer = referer.replaceAll('tab=addresses', 'tab=contacts')

            if (!personInstance) {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label'), params.id]) as String
                redirect(url: request.getHeader('referer'))
                return
            }
            if (!addressbookService.isPersonEditable(personInstance, contextService.getUser())) {
                flash.error = message(code: 'default.notAutorized.message') as String
                redirect(url: request.getHeader('referer'))
                return
            }
            if (!params.functionType && !params.positionType) {
                flash.error = message(code: 'person.create.missing_function') as String
                redirect(url: request.getHeader('referer'))
                return
            }

            personInstance.properties = params

            if (!personInstance.save()) {
                log.info(personInstance.errors)
                flash.error = message(code: 'default.not.updated.message', args: [message(code: 'person.label'), personInstance.toString()]) as String
                redirect(url: request.getHeader('referer'))
                return
            }

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
                    RefdataValue functionType = RefdataValue.get(it)
                    Map<String, Object> configMap = [prs: personInstance, functionType: functionType]
                    if(personRoleOrg)
                        configMap.org = personRoleOrg
                    else if(personRoleProvider)
                        configMap.provider = personRoleProvider
                    else if(personRoleVendor)
                        configMap.vendor = personRoleVendor
                    PersonRole personRole = PersonRole.findWhere(configMap)

                    if (!personRole) {
                        personRole = new PersonRole(configMap)
                        if (personRole.save()) {
                            log.debug("adding PersonRole ${personRole}")
                        }
                        else {
                            log.error("problem saving new PersonRole ${personRole}")
                        }
                    }
                    else {
                        log.debug("ignore adding PersonRole because of existing duplicate")
                    }
                }
            }

            if(personRoleOrg) {
                personInstance.getPersonRoleByOrg(personRoleOrg).each { psr ->
                    if (psr.functionType && !(psr.functionType.id in Params.getLongList(params, 'functionType'))) {
                        personInstance.removeFromRoleLinks(psr)
                        psr.delete()
                    }
                    else if (psr.positionType && !(psr.positionType.id in Params.getLongList(params, 'positionType'))) {
                        personInstance.removeFromRoleLinks(psr)
                        psr.delete()
                    }
                }
            }
            else if(personRoleProvider) {
                personInstance.getPersonRoleByProvider(personRoleProvider).each { psr ->
                    if (psr.functionType && !(psr.functionType.id in Params.getLongList(params, 'functionType'))) {
                        personInstance.removeFromRoleLinks(psr)
                        psr.delete()
                    }
                    else if (psr.positionType && !(psr.positionType.id in Params.getLongList(params, 'positionType'))) {
                        personInstance.removeFromRoleLinks(psr)
                        psr.delete()
                    }
                }
            }
            else if(personRoleVendor) {
                personInstance.getPersonRoleByVendor(personRoleVendor).each { psr ->
                    if (psr.functionType && !(psr.functionType.id in Params.getLongList(params, 'functionType'))) {
                        personInstance.removeFromRoleLinks(psr)
                        psr.delete()
                    }
                    else if (psr.positionType && !(psr.positionType.id in Params.getLongList(params, 'positionType'))) {
                        personInstance.removeFromRoleLinks(psr)
                        psr.delete()
                    }
                }
            }

            if (params.positionType) {
                params.list('positionType').each {
                    RefdataValue positionType = RefdataValue.get(it)
                    Map<String, Object> configMap = [prs: personInstance, positionType: positionType]
                    if(personRoleOrg)
                        configMap.org = personRoleOrg
                    else if(personRoleProvider)
                        configMap.provider = personRoleProvider
                    else if(personRoleVendor)
                        configMap.vendor = personRoleVendor
                    PersonRole personRole = PersonRole.findWhere(configMap)

                    if (!personRole) {
                        personRole = new PersonRole(configMap)
                        if (personRole.save()) {
                            log.debug("adding PersonRole ${personRole}")
                        }
                        else {
                            log.error("problem saving new PersonRole ${personRole}")
                        }
                    }
                    else {
                        log.debug("ignore adding PersonRole because of existing duplicate")
                    }
                }
            }

            Set<Long> toDelete = []
            personInstance.contacts.each { contact ->
                if (!params.containsKey('contact'+contact.id)) {
                    toDelete << contact.id
                }
                else {
                    if (params."content${contact.id}") {
                        contact.content = params."content${contact.id}"
                        contact.save()
                    }
                    if (params."contactLang${contact.id}") {
                        contact.language = RefdataValue.get(params."contactLang${contact.id}")
                        contact.save()
                    }
                }
            }

            Contact.executeUpdate('delete from Contact c where c.id in (:ids)', [ids: toDelete])

            if (params.content) {
                params.list('content').eachWithIndex { content, i ->
                    if (content) {
                        RefdataValue rdvCT = RefdataValue.get(params.list('contentType.id')[i])
                        RefdataValue contactLang = params.list('contactLang.id')[i] ? RefdataValue.get(params.list('contactLang.id')[i]) : null
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

            flash.message = message(code: 'default.updated.message', args: [message(code: 'person.label'), personInstance.toString()]) as String
            redirect(url: referer)
        }
    }
}
