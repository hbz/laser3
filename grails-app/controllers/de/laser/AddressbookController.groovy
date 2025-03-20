package de.laser

import de.laser.addressbook.Address
import de.laser.addressbook.Contact
import de.laser.addressbook.Person
import de.laser.addressbook.PersonRole
import de.laser.annotations.DebugInfo
import de.laser.auth.Role
import de.laser.helper.Params
import de.laser.storage.RDStore
import de.laser.survey.SurveyOrg
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.transaction.TransactionStatus

/**
 * This controller is responsible for contact address display and manipulation
 * @see de.laser.addressbook.Address
 * @see de.laser.addressbook.Contact
 * @see de.laser.addressbook.Person
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class AddressbookController {

    AccessService accessService
    ContextService contextService
    FormService formService
    GenericOIDService genericOIDService

    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def index() {
        redirect controller: 'myInstitution', action: 'addressbook'
    }

    // --------------------------------- CREATE ---------------------------------

    /**
     * Creates a new address with the given parameters
     */
    @DebugInfo(isInstEditor = [], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
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
     * @see de.laser.addressbook.Person
     */
    @DebugInfo(isInstEditor = [], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def createPerson() {
        // moved from PersonController.createPerson()

        Person.withTransaction {
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
                        personRoleOrg = contextService.getOrg()
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

    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def createPersonRole() {
        // moved from AjaxController.addPrsRole()

        def owner           = genericOIDService.resolveOID(params.ownObj)
        def parent          = genericOIDService.resolveOID(params.parent)
        Person person       = Person.get(params.long('person'))
        RefdataValue role   = RefdataValue.get(params.long('role'))

        if (accessService.hasAccessToPerson(person, AccessService.WRITE)) {
            PersonRole newPrsRole
            List<PersonRole> existingPrsRole

            if (owner && role) {
                newPrsRole = new PersonRole(prs: person)

                if (owner instanceof Org)           { newPrsRole.org = owner }
                else if(owner instanceof Provider)  { newPrsRole.provider = owner }
                else if(owner instanceof Vendor)    { newPrsRole.vendor = owner }

                if (parent) {
                    newPrsRole.responsibilityType = role
                    newPrsRole.setReference(parent)

                    String[] ref = newPrsRole.getReference().split(":")
                    String query = "select pr from PersonRole pr where pr.prs = :prs and (pr.org = :owner or pr.provider = :owner or pr.vendor = :owner) and pr.responsibilityType = :responsibilityType and ${ref[0]} = :parent"
                    existingPrsRole = PersonRole.executeQuery(query, [prs:person, owner: owner, responsibilityType: role, parent: parent])
                }
                else {
                    newPrsRole.functionType = role
                    existingPrsRole = PersonRole.executeQuery('select pr from PersonRole pr where pr.prs = :prs and (pr.org = :owner or pr.provider = :owner or pr.vendor = :owner) and pr.functionType = :functionType', [prs:person, owner: owner, functionType: role])
                }
            }

            if (! existingPrsRole && newPrsRole && newPrsRole.save()) {
                //flash.message = message(code: 'default.success')
            }
            else {
                log.error("Problem saving new person role ..")
                flash.error = message(code: 'default.error')
            }
        }
        else {
            flash.error = message(code: 'default.noPermissions')
        }
        redirect(url: request.getHeader('referer'))
    }

    // --------------------------------- DELETE ---------------------------------

    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def deleteAddress() {
        Address obj = Address.get(params.id)
        List args   = [message(code: 'address.label'), params.id]

        if (obj) {
            if (accessService.hasAccessToAddress(obj, AccessService.WRITE)) {
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

    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def deleteContact() {
        Contact obj = Contact.get(params.id)
        Long prsId = obj.prs.id
        List args   = [message(code: 'contact.label'), params.id]

        if (accessService.hasAccessToContact(obj, AccessService.WRITE)) {
            Contact.withTransaction { TransactionStatus ts ->
                obj.delete()
                ts.flush()
                Person prs = Person.get(prsId)
                int contactCount = Contact.countByPrs(prs)
                try {
                    if(contactCount == 0) {
                        List changeList = SurveyOrg.findAllByPerson(prs)
                        changeList.each { tmp2 ->
                            tmp2.person = null
                            tmp2.save()
                        }
                        prs.delete()
                    }
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
        redirect(url: request.getHeader('referer'))
    }

    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def deletePerson() {
        Person obj = Person.get(params.id)
        List args  = [message(code: 'person.label'), params.id]

        if (obj) {
            if (accessService.hasAccessToPerson(obj, AccessService.WRITE)) {
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

    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def deletePersonRole() {
        PersonRole obj = PersonRole.get(params.id)
        if (obj) {
            if (accessService.hasAccessToPerson(obj.prs, AccessService.WRITE)) {
                try {
                    obj.delete()
                }
                catch (Exception e) {
                    log.debug(e.getMessage())
                    flash.error = message(code: 'default.delete.error.general.message')
                }
            }
            else {
                flash.error = message(code: 'default.noPermissions')
            }
        }
        redirect(url: request.getHeader('referer'))
    }

    // --------------------------------- EDIT ---------------------------------

    /**
     * Updates the given address with the given updated data
     */
    @DebugInfo(isInstEditor = [], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def editAddress() {
        // moved from AddressController.editAddress()
        Address obj     = Address.get(params.id)
        List args       = [message(code: 'address.label'), params.id]
        String referer  = request.getHeader('referer')

        if (obj) {
            if (accessService.hasAccessToAddress(obj, AccessService.WRITE)) {
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

                        if(!params.containsKey('region'))
                            obj.region = null

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

                        if(obj.preferredForSurvey && !(RDStore.ADDRESS_TYPE_BILLING in obj.type)){
                            obj.preferredForSurvey = false
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

    /**
     * Takes the submitted parameters and updates the person contact based on the given parameter map
     * @return redirect to the referer -> the updated view of the person contact
     */
    @DebugInfo(isInstEditor = [], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def editPerson() {
        // moved from PersonController.editPerson()
        Person obj      = Person.get(params.id)
        List args       = [message(code: 'person.label'), params.id]
        String referer  = request.getHeader('referer')

        if (obj) {
            Person.withTransaction {
                if (accessService.hasAccessToPerson(obj, AccessService.WRITE)) {

                    if (!params.functionType && !params.positionType) {
                        flash.error = message(code: 'person.create.missing_function') as String
                        redirect(url: referer)
                        return
                    }

                    obj.properties = params

                    if(obj.preferredBillingPerson && !PersonRole.findByFunctionTypeAndPrsAndOrg(RDStore.PRS_FUNC_INVOICING_CONTACT, obj, contextService.getOrg())){
                        obj.preferredBillingPerson = false
                    }

                    if(obj.preferredSurveyPerson && !PersonRole.findByFunctionTypeAndPrsAndOrg(RDStore.PRS_FUNC_SURVEY_CONTACT, obj, contextService.getOrg())){
                        obj.preferredSurveyPerson = false
                    }

                    if (!obj.save()) {
                        log.info(obj.errors)
                        flash.error = message(code: 'default.not.updated.message', args: args)
                        redirect(url: referer)
                        return
                    }

                    if (!referer.contains('tab')) {
                        if (referer.contains('?'))
                            referer += '&tab=contacts'
                        else
                            referer += '?tab=contacts'
                    } else referer = referer.replaceAll('tab=addresses', 'tab=contacts')

                    Org personRoleOrg
                    Provider personRoleProvider
                    Vendor personRoleVendor
                    if (params.personRoleOrg)           { personRoleOrg = Org.get(params.personRoleOrg) }
                    else if (params.personRoleProvider) { personRoleProvider = Provider.get(params.personRoleProvider) }
                    else if (params.personRoleVendor)   { personRoleVendor = Vendor.get(params.personRoleVendor) }
                    else                                { personRoleOrg = contextService.getOrg() }

                    if (params.functionType) {
                        params.list('functionType').each {
                            Map<String, Object> findCfg = [ prs: obj, functionType: RefdataValue.get(it) ]

                            if (personRoleOrg)           { findCfg.org = personRoleOrg }
                            else if (personRoleProvider) { findCfg.provider = personRoleProvider }
                            else if (personRoleVendor)   { findCfg.vendor = personRoleVendor }

                            PersonRole personRole = PersonRole.findWhere(findCfg)

                            if (!personRole) {
                                personRole = new PersonRole(findCfg)
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

                    if (personRoleOrg) {
                        obj.getPersonRoleByOrg(personRoleOrg).each { psr ->
                            if (psr.functionType && !(psr.functionType.id in Params.getLongList(params, 'functionType'))) {
                                obj.removeFromRoleLinks(psr)
                                psr.delete()
                            }
                            else if (psr.positionType && !(psr.positionType.id in Params.getLongList(params, 'positionType'))) {
                                obj.removeFromRoleLinks(psr)
                                psr.delete()
                            }
                        }
                    }
                    else if (personRoleProvider) {
                        obj.getPersonRoleByProvider(personRoleProvider).each { psr ->
                            if (psr.functionType && !(psr.functionType.id in Params.getLongList(params, 'functionType'))) {
                                obj.removeFromRoleLinks(psr)
                                psr.delete()
                            }
                            else if (psr.positionType && !(psr.positionType.id in Params.getLongList(params, 'positionType'))) {
                                obj.removeFromRoleLinks(psr)
                                psr.delete()
                            }
                        }
                    }
                    else if (personRoleVendor) {
                        obj.getPersonRoleByVendor(personRoleVendor).each { psr ->
                            if (psr.functionType && !(psr.functionType.id in Params.getLongList(params, 'functionType'))) {
                                obj.removeFromRoleLinks(psr)
                                psr.delete()
                            }
                            else if (psr.positionType && !(psr.positionType.id in Params.getLongList(params, 'positionType'))) {
                                obj.removeFromRoleLinks(psr)
                                psr.delete()
                            }
                        }
                    }

                    if (params.positionType) {
                        params.list('positionType').each {
                            Map<String, Object> findCfg = [ prs: obj, positionType: RefdataValue.get(it) ]

                            if (personRoleOrg)           { findCfg.org = personRoleOrg }
                            else if (personRoleProvider) { findCfg.provider = personRoleProvider }
                            else if (personRoleVendor)   { findCfg.vendor = personRoleVendor }

                            PersonRole personRole = PersonRole.findWhere(findCfg)

                            if (!personRole) {
                                personRole = new PersonRole(findCfg)
                                if (personRole.save()) {
                                    log.debug("adding PersonRole ${personRole}")
                                }
                                else {
                                    log.error("problem saving new PersonRole ${personRole}")
                                }
                            } else {
                                log.debug("ignore adding PersonRole because of existing duplicate")
                            }
                        }
                    }

                    Set<Long> toDelete = []
                    obj.contacts.each { contact ->
                        if (!params.containsKey('contact' + contact.id)) {
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
                                Contact contact = new Contact(prs: obj, contentType: rdvCT, language: contactLang, type: RDStore.CONTACT_TYPE_JOBRELATED, content: content)
                                contact.save()
                            }
                        }
                    }

                    flash.message = message(code: 'default.updated.message', args: [message(code: 'person.label'), obj.toString()]) as String
                }
                else {
                    flash.error = message(code: 'default.noPermissions')
                }
            }
        }
        else {
            flash.error = message(code: 'default.not.found.message', args: args)
        }
        redirect(url: referer)
    }
}
