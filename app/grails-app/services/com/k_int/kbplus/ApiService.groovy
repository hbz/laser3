package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import groovy.util.logging.Log4j
import groovy.util.slurpersupport.GPathResult

@Log4j
class ApiService {

    /**
     * 
     * @param s
     * @return trimmed string with multiple whitespaces removed
     */
    private String normString(String str){
        if(! str)
            return ""
            
        str = str.replaceAll("\\s+", " ").trim()
        str
    }

    /**
     * 
     * @param obj
     * @return list with children of given object or object if no children
     */
    private List flattenToken(Object obj){
        def result = []

        obj?.token?.each{ token ->
            result << token
        }
        if(result.size() == 0 && obj){
            result << obj
        }
        
        result
    }

    /**
     * Use for INITIAL import ONLY !!!
     *
     * @param xml
     * @return
     */
    GPathResult makeshiftOrgImport(GPathResult xml){

        def count = xml.institution.size()

        // helper

        def resolveStreet = { street ->

            def street1 = '', street2 = '', stParts = normString(street)
            stParts = stParts.replaceAll("(Str\\.|Strasse)", "Straße").replaceAll("stra(ss|ß)e", "str.").trim().split(" ")

            if (stParts.size() > 1) {
                street1 = stParts[0..stParts.size() - 2].join(" ")
                street2 = stParts[stParts.size() - 1]
            }
            else {
                street1 = stParts[0]
            }

            ["street1": street1, "street2": street2]
        }

        def doublets = [:]

        xml.institution.each { inst ->

            // handle identifiers

            def identifiers = []

            inst.identifier.children().each { ident ->
                def idValue = normString(ident.text())
                if (idValue) {
                    identifiers << ["${ident.name()}": idValue]

                    doublets.put("${ident.name()}:${idValue}", (doublets.get("${ident.name()}:${idValue}") ?: 0) + 1)
                }
            }
//return;
            // find org by identifier or name (Org.lookupOrCreate)

            def org = Org.lookup(inst.name?.text(), identifiers)
            if (! org) {
                org = Org.lookupOrCreate(inst.name?.text(), null, null, identifiers, null)
                org.sector = RefdataValue.getByValueAndCategory('Higher Education','OrgSector')

                log.debug("#${(count--)} -- creating new org: " + inst.name?.text() + " / " + identifiers)
            }
            else {
                log.debug("#${(count--)} -- updating org: (" + org.id + ") " + org.name)
            }

            // set tenant

            def tenant, isPublic

            if (inst.source == 'edb des hbz') {
               tenant = Org.findByShortnameIlike('hbz')
               isPublic = RefdataValue.getByValueAndCategory('No','YN')
            }
            //else if (inst.source == 'nationallizenzen.de') {
            //    tenant = Org.findByShortcode('NL')
            //}
            if (! tenant) {
                tenant = org
                isPublic = RefdataValue.getByValueAndCategory('Yes','YN')
            }

            // simple attributes

            def now = new Date()
            org.importSource    = inst.source?.text() ?: org.importSource
            org.sortname        = inst.sortname?.text() ?: org.sortname
            org.shortname       = inst.shortname?.text() ?: org.shortname
            org.lastImportDate  = now

            org.fteStudents     = inst.fte_students?.text() ? Long.valueOf(inst.fte_students.text()) : org.fteStudents
            org.fteStaff        = inst.fte_staff?.text() ? Long.valueOf(inst.fte_staff.text()) : org.fteStaff
            org.url             = inst.url?.text() ?: org.url

            // refdatas

            org.libraryType     = RefdataValue.getByCategoryDescAndI10nValueDe('Library Type', inst.library_type?.text()) ?: org.libraryType
            org.libraryNetwork  = RefdataValue.getByCategoryDescAndI10nValueDe('Library Network', inst.library_network?.text()) ?: org.libraryNetwork
            org.funderType      = RefdataValue.getByCategoryDescAndI10nValueDe('Funder Type', inst.funder_type?.text()) ?: org.funderType

            org.save(flush: true)

            // adding new identifiers

            identifiers.each{ it ->
                it.each { k, v ->
                    def ns = IdentifierNamespace.findByNsIlike(k)
                    if (null == Identifier.findByNsAndValue(ns, v)) {
                        new IdentifierOccurrence(org: org, identifier: Identifier.lookupOrCreateCanonicalIdentifier(k, v)).save()
                    }
                }
            }

            // postal address

            def tmp1 = resolveStreet(inst.street.text())

            Address.lookupOrCreate( // name, street1, street2, zipcode, city, state, country, postbox, pobZipcode, pobCity, type, person, organisation
                    normString("${inst.name}"),
                    normString("${tmp1.street1}"),
                    normString("${tmp1.street2}"),
                    normString("${inst.zip}"),
                    normString("${inst.city}"),
                    RefdataValue.getByCategoryDescAndI10nValueDe('Federal State', inst.county?.text()),
                    RefdataValue.getByCategoryDescAndI10nValueDe('Country', inst.country?.text()),
                    normString("${inst.pob}"),
                    normString("${inst.pobZipcode}"),
                    normString("${inst.pobCity}"),
                    RefdataValue.getByValueAndCategory('Postal address', 'AddressType'),
                    null,
                    org
            )

            // billing address

            inst.billing_address.each{ billing_address ->

                def tmp2 = resolveStreet(billing_address.street?.text())

                def billingAddress = Address.lookupOrCreate( // name, street1, street2, zipcode, city, state, country, postbox, pobZipcode, pobCity, type, person, organisation
                        inst.billing_address.name.text() ? normString("${billing_address.name}") : normString("${inst.name}"),
                        normString("${tmp2.street1}"),
                        normString("${tmp2.street2}"),
                        normString("${billing_address.zip}"),
                        normString("${billing_address.city}"),
                        null,
                        null,
                        billing_address.pob ? normString("${billing_address.pob}") : null,
                        billing_address.pobZipcode ? normString("${billing_address.pobZipcode}") : null,
                        billing_address.pobCity ? normString("${billing_address.pobCity}") : null,
                        RefdataValue.getByValueAndCategory('Billing address', 'AddressType'),
                        null,
                        org
                )
                if (billing_address.addition_first) {
                    billingAddress.setAdditionFirst( normString("${billing_address.addition_first}"))
                    billingAddress.save()
                }
                if (billing_address.addition_second) {
                    billingAddress.setAdditionSecond(normString("${billing_address.addition_second}"))
                    billingAddress.save()
                }
            }

            // legal address

            inst.legal_address.each{ legal_address ->

                def tmp3 = resolveStreet(legal_address.street?.text())

                def legalAddress = Address.lookupOrCreate( // name, street1, street2, zipcode, city, state, country, postbox, pobZipcode, pobCity, type, person, organisation
                        legal_address.name.text() ? normString("${legal_address.name}") : normString("${inst.name}"),
                        normString("${tmp3.street1}"),
                        normString("${tmp3.street2}"),
                        normString("${legal_address.zip}"),
                        normString("${legal_address.city}"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        RefdataValue.getByValueAndCategory('Legal patron address', 'AddressType'),
                        null,
                        org
                )
                if (legal_address.url.text()) {
                    log.debug("setting urlGov to: " + legal_address.url)
                    org.setUrlGov(legal_address.url?.text())
                    org.save(flush: true)
                }
            }

            inst.private_property.children().each { pp ->
                def name = pp.name().replace('_', ' ').toLowerCase()

                def type = PropertyDefinition.findWhere(
                        name: name,
                        type: 'class ' + RefdataValue.class.name,
                        descr:  PropertyDefinition.ORG_PROP,
                        tenant: tenant
                )
                if (! type) {
                    type = PropertyDefinition.findWhere(
                            name: name,
                            type: 'class ' + String.class.name,
                            descr:  PropertyDefinition.ORG_PROP,
                            tenant: tenant
                    )
                }

                if (type) {
                    def opp
                    def check = "private property: " + name + " : " + pp.text()

                    if (type.refdataCategory) {
                        def rdv = RefdataValue.getByCategoryDescAndI10nValueDe(type.refdataCategory, pp.text())
                        if (rdv) {
                            opp = OrgPrivateProperty.findWhere(type: type, owner: org, refValue: rdv)
                            if (! opp) {
                                opp = new OrgPrivateProperty(type: type, owner: org, refValue: rdv)
                                opp.save() ? (check += " > OK") : (check += " > FAIL")
                            } else {
                                check = "ignored existing " + check
                            }
                        }
                    }
                    else {
                        opp = OrgPrivateProperty.findWhere(type: type, owner: org, stringValue: pp.text())
                        if (! opp) {
                            opp = new OrgPrivateProperty(type: type, owner: org, stringValue: pp.text())
                            opp.save() ? (check += " > OK") : (check += " > FAIL")
                        } else {
                            check = "ignored existing " + check
                        }
                    }

                    log.debug(check)
                }
                else {
                    log.debug("ignoring private property due mismatch: " + name + " : " + pp.text())
                }
            }

            // persons

            def rdvContactPerson = RefdataValue.getByValueAndCategory('General contact person', 'Person Function')
            def rdvJobRelated    = RefdataValue.getByValueAndCategory('Job-related','ContactType')

            inst.person?.children().each { p ->

                def rdvContactType   = RefdataValue.getByValueAndCategory('Personal contact', 'Person Contact Type')

                if( ! p.first_name.text() ) {
                    rdvContactType = RefdataValue.getByValueAndCategory('Functional contact', 'Person Contact Type')
                }
                def person = Person.lookup( // firstName, lastName, tenant, isPublic, contactType, org, functionType
                        p.first_name.text() ? normString("${p.first_name}") : null,
                        p.last_name.text() ? normString("${p.last_name}") : null,
                        tenant,
                        isPublic,
                        rdvContactType,
                        org,
                        rdvContactPerson,
                )

                // do NOT change existing persons !!!
                if (! person) {
                    log.debug("creating new person: ${p.first_name} ${p.last_name}")

                    person = new Person(
                            first_name: p.first_name.text() ? normString("${p.first_name}") : null,
                            last_name: normString("${p.last_name}"),
                            tenant: tenant,
                            isPublic: isPublic,
                            contactType: rdvContactType,
                            gender: RefdataValue.getByCategoryDescAndI10nValueDe('Gender', p.gender?.text())
                    )

                    if (person.save()) {
                        log.debug("creating new personRole: ${person} ${rdvContactPerson} ${org}")

                        // role

                        def pr = new PersonRole(
                                prs: person,
                                org: org,
                                functionType: rdvContactPerson
                        )
                        pr.save()

                        p.function.children().each { func ->
                            def rdv = RefdataValue.getByCategoryDescAndI10nValueDe('Person Function', func.text())
                            if (rdv) {
                                def pf = new PersonRole(
                                        prs: person,
                                        org: org,
                                        functionType: rdv
                                )
                                pf.save()
                            }
                        }

                        // contacts

                        if (p.email) {
                            Contact.lookupOrCreate(
                                    normString("${p.email}"),
                                    RefdataValue.getByValueAndCategory('E-Mail','ContactContentType'),
                                    rdvJobRelated,
                                    person,
                                    null
                            )
                        }
                        if (p.fax) {
                            Contact.lookupOrCreate(
                                    normString("${p.fax}"),
                                    RefdataValue.getByValueAndCategory('Fax','ContactContentType'),
                                    rdvJobRelated,
                                    person,
                                    null
                            )
                        }
                        if (p.telephone) {
                            Contact.lookupOrCreate(
                                    normString("${p.telephone}"),
                                    RefdataValue.getByValueAndCategory('Phone','ContactContentType'),
                                    rdvJobRelated,
                                    person,
                                    null
                            )
                        }
                    }
                }
                else {
                    log.debug("ignoring existing person [${rdvContactPerson}]: " + person)
                }
            }
        }

        log.debug("WARNING: doublets @ " + doublets.findAll{ it.value > 1})

        return xml
    }

}
