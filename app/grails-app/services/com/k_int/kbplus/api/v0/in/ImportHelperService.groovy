package com.k_int.kbplus.api.v0.in

import com.k_int.kbplus.*
import com.k_int.kbplus.api.v0.MainService
import com.k_int.kbplus.api.v0.out.LicenseService
import com.k_int.properties.PropertyDefinition
import groovy.util.logging.Log4j

@Log4j
class ImportHelperService {

    // ##### HELPER #####

    def getRefdataValue(def value, String category) {
        // TODO
        if (value && category) {
            def rdCategory  = RefdataCategory.findByDesc(category)
            def rdValue     = RefdataValue.findByOwnerAndValue(rdCategory, value.toString())
            return rdValue
        }
        null
    }

    // #####

    def getAddresses(def data, Org ownerOrg, Person ownerPerson) {
        def addresses = []

        data.each{ it ->
            def address = new Address(
                    street_1:   it.street1,
                    street_2:   it.street2,
                    pob:        it.pob,
                    zipcode:    it.zipcode,
                    city:       it.city,
                    state:      it.state,
                    country:    it.country
            )

            // RefdataValues
            address.type = getRefdataValue(it.type?.value,"AddressType")

            // References
            address.org = ownerOrg
            address.prs = ownerPerson

            addresses << address
        }
        addresses
    }

    def getContacts(def data, Org ownerOrg, Person ownerPerson) {
        def contacts = []

        data.each{ it ->
            def contact = new Contact(
                    content: it.content
            )

            // RefdataValues
            contact.type        = getRefdataValue(it.type?.value,"ContactType")
            contact.contentType = getRefdataValue(it.category?.value,"ContactContentType")

            // References
            contact.org = ownerOrg
            contact.prs = ownerPerson

            contacts << contact
        }
        contacts
    }

    def getPersonsAndRoles(def data, Org owner, Org contextOrg) {
        def result = [
                'persons': [],
                'personRoles': []
        ]

        data.each { it ->
            def person = new Person(
                    first_name:     it.firstName,
                    middle_name:    it.middleName,
                    last_name:      it.lastName
            )

            // RefdataValues
            person.gender   = getRefdataValue(it.gender?.value,"Gender")
            person.isPublic = getRefdataValue(it.isPublic?.value,"YN")

            // References
            person.tenant    = "No".equalsIgnoreCase(person.isPublic?.value) ? contextOrg : owner

            person.addresses = getAddresses(it.addresses, null, person)
            person.contacts  = getContacts(it.contacts, null, person)

            def properties = getProperties(it.properties, null, person, contextOrg)
            person.privateProperties = properties['private']

            // PersonRoles
            it.roles?.each { it2 ->
                if(it2.functionType) {
                    def personRole = new PersonRole(
                            org: owner,
                            prs: person
                    )

                    // RefdataValues
                    personRole.functionType = getRefdataValue(it2.functionType?.value,"Person Function")
                    if (personRole.functionType) {
                        result['persons'] << person
                        result['personRoles'] << personRole
                    }

                    // TODO: responsibilityType
                    //def rdvResponsibilityType = getRefdataValue(it2.functionType?.value,"Person Responsibility")
                }
            }
        }
        result
    }

    def getIdentifiers(def data, Org ownerOrg) {
        def idenfifierOccurences = []

        data.each { it ->
            def identifier = Identifier.lookupOrCreateCanonicalIdentifier(it.namespace, it.value)
            def idenfifierOccurence = new IdentifierOccurrence(
                    identifier: identifier,
                    org: ownerOrg
            )
            idenfifierOccurences << idenfifierOccurence
        }

        idenfifierOccurences
    }

    def getProperties(def data, Org ownerOrg, Person ownerPerson, Org contextOrg) {
        def properties = [
                'custom': [],
                'private': []
        ]

        data.each { it ->
            def property

            // Custom or Private?
            def isPublic = getRefdataValue(it.isPublic?.value,"YN")
            if ("No".equalsIgnoreCase(isPublic?.value)) {
                if (ownerOrg) {
                    property = new OrgPrivateProperty(
                            owner: ownerOrg,
                            tenant: contextOrg,
                            note: it.note
                    )
                    properties['private'] << property
                }
                else if (ownerPerson) {
                    property = new PersonPrivateProperty(
                            owner: ownerPerson,
                            tenant: contextOrg,
                            note: it.note
                    )
                    properties['private'] << property
                }
            }
            else {
                if (ownerOrg) {
                    property = new OrgCustomProperty(
                            owner: ownerOrg,
                            note: it.note
                    )
                    properties['custom'] << property
                }
            }

            if (property) {
                def propertyDefinition = PropertyDefinition.findByDescrAndName(data.description, data.name)
                property.type = propertyDefinition
                property.setValue(it.value, propertyDefinition.type, propertyDefinition.refdataCategory)
            }
        }
        properties
    }
}
