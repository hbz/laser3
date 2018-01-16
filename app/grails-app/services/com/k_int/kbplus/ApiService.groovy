package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import groovy.util.logging.Log4j
import groovy.util.slurpersupport.GPathResult

@Log4j
class ApiService {

    /**
     *
     * @param xml
     * @return
     */
    GPathResult importOrg(GPathResult xml){

        def rdcYN = RefdataCategory.findByDesc('YN')
        def rdcContactContentType = RefdataCategory.findByDesc('ContactContentType')

        def overrideTenant
        def overrideIsPublic

        if(xml.override?.tenant?.text()){
            overrideTenant   = Org.findByShortcode(xml.override.tenant.text())
            overrideIsPublic = RefdataValue.findByOwnerAndValue(rdcYN, 'No')

            log.info("OVERRIDING TENANT: ${overrideTenant}")
        }

        xml.institution.each{ inst ->

            def identifiers = [:]
            def org         = null

            def title = normString(inst.title.text())
            def subscriperGroup = inst.subscriper_group ? normString(inst.subscriper_group.text()) : ''

            log.info("importing org: ${title}")

            def firstName, middleName, lastName
            def email, telephone
            def street1, street2

            // store identifiers
            def tmp = [:]

            if(inst.uid){
                tmp.put("uid", normString(inst.uid.text()))
            }
            if(inst.sigel){
                tmp.put("isil", normString(inst.sigel.text()))
            }
            if(inst.user_name){
                tmp.put("wib", normString(inst.user_name.text()))
            }
            tmp.each{ k, v ->
                if(v != '')
                    identifiers.put(k, v)
            }
            
            // find org by identifier or name (Org.lookupOrCreate)
            org = Org.lookupOrCreate(title, subscriperGroup, null, identifiers, null)
            if(org){

                // adding new identifiers
                identifiers.each{ k,v ->
                    def ns   = IdentifierNamespace.findByNsIlike(k)
                    if(null == Identifier.findByNsAndValue(ns, v)){
                        new IdentifierOccurrence(org:org, identifier:Identifier.lookupOrCreateCanonicalIdentifier(k, v)).save()
                    }
                }

                // adding address
                if(inst.zip && inst.city){
                    def stParts = normString(inst.street.text())
                    stParts = stParts.replaceAll("(Str\\.|Strasse)", "Straße").replaceAll("stra(ss|ß)e", "str.").trim().split(" ")
                    
                    if(stParts.size() > 1){
                        street1 = stParts[0..stParts.size() - 2].join(" ")
                        street2 = stParts[stParts.size() - 1]
                    }
                    else {
                        street1 = stParts[0]
                        street2 = ""
                    }
                    
                    // create if no match found
                    Address.lookupOrCreate(
                        "${street1}",
                        "${street2}",
                        null,
                        normString(inst.zip?.text()),
                        normString(inst.city?.text()),
                        normString(inst.county?.text()),
                        normString(inst.country?.text()),
                        RefdataValue.findByValue("Postal address"),
                        null,
                        org
                        )
                }
                
                // adding contact persons
                def person
                
                // overrides
                def tenant   = org
                def isPublic = RefdataValue.findByOwnerAndValue(rdcYN, 'Yes')
                
                if(overrideTenant){
                    tenant = overrideTenant
                }
                if(overrideIsPublic){
                    isPublic = overrideIsPublic
                }
                
                def cpList    = flattenToken(inst.contactperson)
                def mailList  = flattenToken(inst.email)
                def phoneList = flattenToken(inst.telephone)
                def faxList   = flattenToken(inst.fax)
                         
                cpList.eachWithIndex{ token, i ->

                    // adding person
                    def cpText = cpList[i] ? cpList[i].text() : ''
                    def cpParts = normString(cpText).replaceAll("(Herr|Frau)", "").trim().split(" ")
  
                    firstName  = cpParts[0].trim()
                    middleName = (cpParts.size() > 2) ? (cpParts[1..cpParts.size() - 2].join(" ")) : ""
                    lastName   = cpParts[cpParts.size() - 1].trim()

                    // create if no match found
                    if(firstName != '' && lastName != ''){
                        person = Person.lookupOrCreateWithPersonRole(
                            firstName,
                            middleName,
                            lastName,
                            null /* gender */,
                            tenant,
                            isPublic,
                            org, /* needed for person_role */
                            RefdataValue.findByValue("General contact person")
                            )

                        // adding contacts
                            
                        def mailText  = mailList[i]  ? mailList[i].text()  : ''
                        def phoneText = phoneList[i] ? phoneList[i].text() : ''
                        def faxText   = faxList[i]   ? faxList[i].text()   : ''

                        if(mailText != '' ){
                            Contact.lookupOrCreate(
                                normString(mailText),
                                RefdataValue.findByOwnerAndValue(rdcContactContentType, 'Mail'),
                                RefdataValue.findByValue("Job-related"),
                                person,
                                null /* person contact only */
                                )
                        }
                        if(phoneText != ''){
                            Contact.lookupOrCreate(
                                normString(phoneText),
                                RefdataValue.findByOwnerAndValue(rdcContactContentType, 'Phone'),
                                RefdataValue.findByValue("Job-related"),
                                person,
                                null /* person contact only */
                                )
                        }
                        if(faxText != ''){
                            Contact.lookupOrCreate(
                                normString(faxText),
                                RefdataValue.findByOwnerAndValue(rdcContactContentType, 'Fax'),
                                RefdataValue.findByValue("Job-related"),
                                person,
                                null /* person contact only */
                                )
                        }
                    }     
                    
                }
            } // if(org)
        }
        
        return xml
    }
    
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
    GPathResult newOrgImport(GPathResult xml){

        def count = xml.institution.size()

        xml.institution.each { inst ->

            // handle identifiers

            def identifiers = [:]

            inst.identifier.children().each { ident ->
                def idValue = normString(ident.text())
                if (idValue) {
                    identifiers.put("${ident.name()}", idValue)
                }
            }

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
               tenant = Org.findByShortcode('Hochschulbibliothekszentrum_des_Landes_NRW')
               isPublic = RefdataValue.getByValueAndCategory('No','YN')
            }
            else if (inst.source == 'nationallizenzen.de') {
            //    tenant = Org.findByShortcode('NL')
            }
            if (! tenant) {
                tenant = org
                isPublic = RefdataValue.getByValueAndCategory('Yes','YN')
            }

            // simple attributes

            def now = new Date()
            org.importSource    = inst.source?.text() ?: org.importSource
            org.sortname        = inst.sortname?.text() ?: org.sortname
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

            identifiers.each{ k,v ->
                def ns = IdentifierNamespace.findByNsIlike(k)
                if(null == Identifier.findByNsAndValue(ns, v)){
                    new IdentifierOccurrence(org:org, identifier:Identifier.lookupOrCreateCanonicalIdentifier(k, v)).save()
                }
            }

            // postal address

            def street1 = '', street2 = '', stParts = normString(inst.street.text())
            stParts = stParts.replaceAll("(Str\\.|Strasse)", "Straße").replaceAll("stra(ss|ß)e", "str.").trim().split(" ")

            if (stParts.size() > 1) {
                street1 = stParts[0..stParts.size() - 2].join(" ")
                street2 = stParts[stParts.size() - 1]
            }
            else {
                street1 = stParts[0]
            }

            Address.lookupOrCreate( // street1, street2, postbox, zipcode, city, state, country, type, person, organisation
                    normString("${street1}"),
                    normString("${street2}"),
                    normString("${inst.pobox}"),
                    normString("${inst.zip}"),
                    normString("${inst.city}"),
                    RefdataValue.getByCategoryDescAndI10nValueDe('Federal State', inst.county?.text()),
                    RefdataValue.getByCategoryDescAndI10nValueDe('Country', inst.country?.text()),
                    RefdataValue.getByValueAndCategory('Postal address', 'AddressType'),
                    null,
                    org
            )

            // billing address

            if (inst.billing_address?.text()) {
                street2 = ''
                stParts = normString(inst.billing_address.street?.text())
                stParts = stParts.replaceAll("(Str\\.|Strasse)", "Straße").replaceAll("stra(ss|ß)e", "str.").trim().split(" ")

                if (stParts.size() > 1) {
                    street1 = stParts[0..stParts.size() - 2].join(" ")
                    street2 = stParts[stParts.size() - 1]
                }
                else {
                    street1 = stParts[0]
                }

                Address.lookupOrCreate( // street1, street2, postbox, zipcode, city, state, country, type, person, organisation
                        normString("${street1}"),
                        normString("${street2}"),
                        null,
                        normString("${inst.billing_address.zip}"),
                        normString("${inst.billing_address.city}"),
                        null,
                        null,
                        RefdataValue.getByValueAndCategory('Billing address', 'AddressType'),
                        null,
                        org
                )
            }

            // private properties
            // TODO custom prop when tenant = org

            inst.private_property.children().each { pp ->
                def name = pp.name().capitalize()
                def type = PropertyDefinition.findWhere(
                        name: name,
                        refdataCategory: name,
                        type: 'class ' + RefdataValue.class.name,
                        descr:  PropertyDefinition.ORG_PROP,
                        tenant: tenant
                )

                if (type) {
                    def check = "adding private property: " + name + " : " + pp.text()

                    // create on-the-fly
                    def rdv = RefdataValue.getByCategoryDescAndI10nValueDe(name, pp.text())
                    if (! rdv) {
                        rdv = new RefdataValue(value: pp.text(), owner: RefdataCategory.getByI10nDesc(name))
                        rdv.save()
                    }
                    def opp = OrgPrivateProperty.findWhere(type: type, owner: org, refValue: rdv)
                    if (! opp) {
                        opp = new OrgPrivateProperty(type: type, owner: org, refValue: rdv)
                    }

                    if (opp.save()) {
                        check += " > OK"
                    }
                    else {
                        check += " > FAIL"
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
                def person = Person.lookup(
                        normString("${p.first_name}"),
                        normString("${p.last_name}"),
                        tenant,
                        isPublic,
                        org,
                        rdvContactPerson
                )
                if (! person) {
                    log.debug("creating new person: ${p.first_name} ${p.last_name}")

                    person = new Person(
                            first_name: normString("${p.first_name}"),
                            last_name: normString("${p.last_name}"),
                            tenant: tenant,
                            isPublic: isPublic
                    )
                    person.gender = RefdataValue.getByCategoryDescAndI10nValueDe('Gender', p.gender?.text()) ?: person.gender

                    if (person.save()) {
                        log.debug("creating new personRole: ${person} ${rdvContactPerson} ${org}")

                        // role

                        def pr = new PersonRole(
                                prs: person,
                                org: org,
                                functionType: rdvContactPerson
                        )
                        pr.save()

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

        return xml
    }

}
