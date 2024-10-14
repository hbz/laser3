package de.laser

import de.laser.annotations.DebugInfo
import de.laser.helper.Params
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyOrg
import de.laser.wekb.Package
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

/**
 * This controller manages person-contact related calls
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class PersonController  {

    AddressbookService addressbookService
    ContextService contextService
    FormService formService
    GenericOIDService genericOIDService

    static allowedMethods = [createPerson: ['GET', 'POST'], editPerson: ['GET', 'POST'], deletePerson: 'POST']

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
        Person.withTransaction {
            Org contextOrg = contextService.getOrg()
            List userMemberships = contextService.getUser().formalOrg ? [contextService.getUser().formalOrg] : []

            switch (request.method) {
                case 'GET':
                    Person personInstance = new Person(params)
                    // processing dynamic form data
                    _addPersonRoles(personInstance)

                    [personInstance: personInstance, userMemberships: userMemberships]
                    break
                case 'POST':
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

                            if (params.multipleAddresses) {
                                params.list('multipleAddresses').eachWithIndex { name, i ->
                                    if(params.type) {
                                        Address addressInstance = new Address(
                                                name: (1 == params.list('name').size()) ? params.name : params.name[i],
                                                additionFirst: (1 == params.list('additionFirst').size()) ? params.additionFirst : params.additionFirst[i],
                                                additionSecond: (1 == params.list('additionSecond').size()) ? params.additionSecond : params.additionSecond[i],
                                                street_1: (1 == params.list('street_1').size()) ? params.street_1 : params.street_1[i],
                                                street_2: (1 == params.list('street_2').size()) ? params.street_2 : params.street_2[i],
                                                zipcode: (1 == params.list('zipcode').size()) ? params.zipcode : params.zipcode[i],
                                                city: (1 == params.list('city').size()) ? params.city : params.city[i],
                                                region: (1 == params.list('region').size()) ? params.region : params.region[i],
                                                country: (1 == params.list('country').size()) ? params.country : params.country[i],
                                                pob: (1 == params.list('pob').size()) ? params.pob : params.pob[i],
                                                pobZipcode: (1 == params.list('pobZipcode').size()) ? params.pobZipcode : params.pobZipcode[i],
                                                pobCity: (1 == params.list('pobCity').size()) ? params.pobCity : params.pobCity[i],
                                                prs: personInstance)

                                        params.list('type').each {
                                            if (!(it in addressInstance.type)) {
                                                addressInstance.addToType(RefdataValue.get(Long.parseLong(it)))
                                            }
                                        }
                                        if (!addressInstance.save()) {
                                            flash.error = message(code: 'default.save.error.general.message') as String
                                            log.error('Adresse konnte nicht gespeichert werden. ' + addressInstance.errors)
                                            redirect(url: referer)
                                            return
                                        }
                                    }
                                }
                            }

                            flash.message = message(code: 'default.created.message', args: [message(code: 'person.label'), personInstance.toString()]) as String
                        }
                        else flash.error = message(code: 'person.create.missing_function') as String
                    }
                    redirect(url: referer)
                    break
            }
        }
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

            if (params.multipleAddresses) {
                params.list('multipleAddresses').eachWithIndex { name, i ->
                    if(params.type) {
                        Address addressInstance = new Address(
                            name: (1 == params.list('name').size()) ? params.name : params.name[i],
                            additionFirst: (1 == params.list('additionFirst').size()) ? params.additionFirst : params.additionFirst[i],
                            additionSecond: (1 == params.list('additionSecond').size()) ? params.additionSecond : params.additionSecond[i],
                            street_1: (1 == params.list('street_1').size()) ? params.street_1 : params.street_1[i],
                            street_2: (1 == params.list('street_2').size()) ? params.street_2 : params.street_2[i],
                            zipcode: (1 == params.list('zipcode').size()) ? params.zipcode : params.zipcode[i],
                            city: (1 == params.list('city').size()) ? params.city : params.city[i],
                            region: (1 == params.list('region').size()) ? params.region : params.region[i],
                            country: (1 == params.list('country').size()) ? params.country : params.country[i],
                            pob: (1 == params.list('pob').size()) ? params.pob : params.pob[i],
                            pobZipcode: (1 == params.list('pobZipcode').size()) ? params.pobZipcode : params.pobZipcode[i],
                            pobCity: (1 == params.list('pobCity').size()) ? params.pobCity : params.pobCity[i],
                            prs: personInstance)

                        params.list('type').each {
                            if (!(it in addressInstance.type)) {
                                addressInstance.addToType(RefdataValue.get(Long.parseLong(it)))
                            }
                        }
                        if (!addressInstance.save()) {
                            flash.error = message(code: 'default.save.error.general.message') as String
                            log.error('Adresse konnte nicht gespeichert werden. ' + addressInstance.errors)
                            redirect(url: referer)
                            return
                        }
                    }
                }
            }

            flash.message = message(code: 'default.updated.message', args: [message(code: 'person.label'), personInstance.toString()]) as String
            redirect(url: referer)
        }
    }

    /**
     * Lists all possible tenants of the given person contact
     * @return a JSON map containing all organisations / institutions linked to the given person contact
     */
    @Secured(['ROLE_USER'])
    def getPossibleTenantsAsJson() {
        def result = []

        Person person = (Person) genericOIDService.resolveOID(params.oid)

        List<Org> orgs = person.roleLinks?.collect{ it.org }
        orgs.add(person.tenant)
        orgs.add(contextService.getOrg())

        orgs.unique().each { o ->
            result.add([value: "${o.class.name}:${o.id}", text: "${o.toString()}"])
        }

        render result as JSON
    }

    /**
     * Removes an assignal from the given person contact
     * @return the person details view
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def deletePersonRole() {
        Person prs = Person.get(params.id)

        if (addressbookService.isPersonEditable(prs, contextService.getUser())) {

            if (params.oid) {
                PersonRole pr = (PersonRole) genericOIDService.resolveOID(params.oid)

                if (pr && (pr.prs.id == prs.id) && pr.delete()) {
                    log.debug("deleted PersonRole ${pr}")
                }
                else {
                    log.debug("problem deleting PersonRole ${pr}")
                }
            }
        }
        redirect(url: request.getHeader('referer'))
    }

    @Deprecated
    @Transactional
    private void _addPersonRoles(Person prs){

        if (params.functionType) {
            PersonRole result

            RefdataValue functionRdv = RefdataValue.get(params.functionType) ?: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS
            Org functionOrg = Org.get(params.functionOrg)

            if (functionRdv && functionOrg) {
                result = new PersonRole(prs: prs, functionType: functionRdv, org: functionOrg)

                String query = "from PersonRole as PR where PR.prs = ${prs.id} and PR.org = ${functionOrg.id} and PR.functionType = ${functionRdv.id}"
                if (PersonRole.find(query)) {
                    log.debug("ignore adding PersonRole because of existing duplicate")
                }
                else if (result) {
                    if (result.save()) {
                        log.debug("adding PersonRole ${result}")
                    }
                    else {
                        log.error("problem saving new PersonRole ${result}")
                    }
                }
            }

            RefdataValue positionRdv = params.positionType ? RefdataValue.get(params.positionType) : null
            Org positionOrg = Org.get(params.positionOrg)

            if (positionRdv && positionOrg) {
                result = new PersonRole(prs: prs, positionType: positionRdv, org: positionOrg)

                String query = "from PersonRole as PR where PR.prs = ${prs.id} and PR.org = ${positionOrg.id} and PR.positionType = ${positionRdv.id}"
                if (PersonRole.find(query)) {
                    log.debug("ignore adding PersonRole because of existing duplicate")
                }
                else if (result) {
                    if (result.save()) {
                        log.debug("adding PersonRole ${result}")
                    }
                    else {
                        log.error("problem saving new PersonRole ${result}")
                    }
                }
            }
        }

        //@Deprecated
        params?.responsibilityType?.each { key, value ->
            PersonRole result

            RefdataValue roleRdv = RefdataValue.get(params.responsibilityType[key])
            Org org = Org.get(params.org[key])

            if (roleRdv && org) {
                def subject      // dynamic
                def subjectType = params.subjectType[key]

                switch (subjectType) {
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
                }
            }
            if (result) {
                if (result.save()) {
                    log.debug("adding PersonRole ${result}")
                }
                else {
                    log.error("problem saving new PersonRole ${result}")
                }
            }
        }
    }
}
