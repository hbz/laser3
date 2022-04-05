package de.laser.api.v0


import de.laser.Identifier
import de.laser.License
import de.laser.Org
import de.laser.OrgRole
import de.laser.PersonRole
import de.laser.RefdataValue
import de.laser.properties.LicenseProperty
import de.laser.properties.OrgProperty
import de.laser.properties.PersonProperty
import de.laser.properties.PropertyDefinition
import de.laser.Address
import de.laser.Contact
import de.laser.Person
import de.laser.api.v0.entities.ApiOrg
import de.laser.helper.Constants
import de.laser.storage.RDConstants
import groovy.util.logging.Slf4j

@Deprecated
@Slf4j
class ApiWriterHelper {

    // ##### HELPER #####

    @Deprecated
    static getValidDateFormat(def value) {
        // TODO: check and format date

        def date = new Date()
        date = date.parse("yyyy-MM-dd HH:mm:ss", value)
        date
    }

    // #####

    @Deprecated
    static getAddresses(def data, Org ownerOrg, Person ownerPerson) {
        def addresses = []

        data?.each { it ->
            def address = new Address(
                    street_1: it.street1,
                    street_2: it.street2,
                    pob: it.pob,
                    zipcode: it.zipcode,
                    city: it.city,
                    region: it.region,
                    country: it.country
            )

            // RefdataValues
            address.type = RefdataValue.getByValueAndCategory(it.type?.value, RDConstants.ADDRESS_TYPE)

            // References
            address.org = ownerOrg
            address.prs = ownerPerson

            addresses << address
        }
        addresses
    }

    @Deprecated
    static getContacts(def data, Org ownerOrg, Person ownerPerson) {
        def contacts = []

        data?.each { it ->
            def contact = new Contact(
                    content: it.content
            )

            // RefdataValues
            contact.type        = RefdataValue.getByValueAndCategory(it.type?.value, RDConstants.CONTACT_TYPE)
            contact.contentType = RefdataValue.getByValueAndCategory(it.category?.value, RDConstants.CONTACT_CONTENT_TYPE)

            // References
            contact.org = ownerOrg
            contact.prs = ownerPerson

            contacts << contact
        }
        contacts
    }

    @Deprecated
    static getPersonsAndRoles(def data, Org owner, Org contextOrg) {
        def result = [
                'persons'    : [],
                'personRoles': []
        ]

        data?.each { it ->
            def person = new Person(
                    first_name:  it.firstName,
                    middle_name: it.middleName,
                    last_name:   it.lastName
            )

            // RefdataValues
            person.gender   = RefdataValue.getByValueAndCategory(it.gender?.value, RDConstants.GENDER)
            person.isPublic = it.isPublic in ['Yes', 'yes']

            // References
            person.tenant = person.isPublic ? owner : contextOrg

            person.addresses = getAddresses(it.addresses, null, person)
            person.contacts  = getContacts(it.contacts, null, person)

            def properties = getProperties(it.properties, person, contextOrg)
            person.propertySet = properties['private']

            // PersonRoles
            it.roles?.each { it2 ->
                if (it2.functionType) {
                    def personRole = new PersonRole(
                            org: owner,
                            prs: person
                    )

                    // RefdataValues
                    personRole.functionType = RefdataValue.getByValueAndCategory(it2.functionType?.value, RDConstants.PERSON_FUNCTION)
                    if (personRole.functionType) {
                        result['persons'] << person
                        result['personRoles'] << personRole
                    }

                    // TODO: responsibilityType
                    //def rdvResponsibilityType = getRefdataValue(it2.functionType?.value, RDConstants.PERSON_RESPONSIBILITY)
                }
            }
        }
        result
    }

    @Deprecated
    static getIdentifiers(HashMap data, def owner) {
        List<Identifier> idenfifiers = []

        data?.each { it ->
            idenfifiers << Identifier.construct([value:it.value, reference:owner, namespace:it.key])
        }

        idenfifiers
    }

    @Deprecated
    static getOrgRelations(def data, def owner, Org context) {
        def result = []

        data?.each { it ->   // de.laser.OrgRole

            // check existing resources
            def check = []
            it.organisation?.identifiers?.each { orgIdent ->
                check << ApiOrg.findOrganisationBy('identifier', orgIdent.namespace + ":" + orgIdent.value)
            }
            check.removeAll([null, [], Constants.HTTP_BAD_REQUEST, Constants.HTTP_PRECONDITION_FAILED])
            check = check.flatten()

            def candidates = []
            check.each { orgCandidate ->
                if (orgCandidate.name.equals(it.organisation?.name)?.trim()) {
                    candidates << orgCandidate
                }
            }
            if (candidates.size() == 1) {
                log.debug("create new orgRole")
                def org = candidates.get(0)

                OrgRole orgRole = new OrgRole(
                        org:        org,
                        endDate:    getValidDateFormat(it.endDate),
                        startDate:  getValidDateFormat(it.startDate),
                        roleType:   RefdataValue.getByValueAndCategory(it.roleType, RDConstants.ORGANISATIONAL_ROLE)
                )
                orgRole.setReference(owner)

                // check permission to use org for orgRole
                // currently only if org equals context
                if (org.id == context.id) {
                    if (orgRole.roleType) {
                        result << orgRole
                    }
                }
                else {
                    log.debug("IGNORED: create new orgRole due lack of permission")
                }
            }
            else {
                log.debug("IGNORED: create new orgRole")
            }
        }
        // TODO: check permissions for org
        result
    }

    @Deprecated
    static getProperties(def data, def owner, Org contextOrg) {
        def properties = [
                'custom': [],
                'private': []
        ]

        data?.each { it ->
            def property

            // Private Property
            if (it.tenant == contextOrg) {
                if (owner instanceof Org) {
                    property = new OrgProperty(
                            owner:  owner,
                            tenant: contextOrg,
                            note:   it.note,
                            isPublic: it.isPublic
                    )
                }
                else if (owner instanceof Person) {
                    property = new PersonProperty(
                            owner:  owner,
                            tenant: contextOrg,
                            note:   it.note
                    )
                }
                else if (owner instanceof License) {
                    property = new LicenseProperty(
                            owner:     owner,
                            tenant:    contextOrg,
                            paragraph: it.paragraph,
                            note:      it.note,
                            isPublic: it.isPublic
                    )
                }

                if (property) {
                    def propertyDefinition = PropertyDefinition.getByNameAndDescrAndTenant(data.name, data.description, contextOrg)
                    property.type = propertyDefinition
                    property.setValue(it.value, propertyDefinition.type, propertyDefinition.refdataCategory)

                    properties['private'] << property
                }
                else {
                    log.debug('private property not supported: ' + owner + ' < '+ data)
                }

            }
            // Custom Property
            else if(!it.tenant) {
                if (owner instanceof Org) {
                    property = new OrgProperty(
                            owner: owner,
                            note:  it.note,
                            tenant: it.tenant,
                            isPublic: it.isPublic
                    )
                }
                else if (owner instanceof License) {
                    property = new LicenseProperty(
                            owner:     owner,
                            note:      it.note,
                            paragraph: it.paragraph,
                            tenant: it.tenant,
                            isPublic: it.isPublic
                    )
                }

                if (property) {
                    def propertyDefinition = PropertyDefinition.getByNameAndDescr(data.name, data.description)
                    property.type = propertyDefinition
                    property.setValue(it.value, propertyDefinition.type, propertyDefinition.refdataCategory)

                    properties['custom'] << property
                }
                else {
                    log.debug('property not supported: ' + owner + ' < '+ data)
                }

            }
        }
        properties
    }
}