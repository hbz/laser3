package com.k_int.kbplus

import com.k_int.kbplus.auth.*
import grails.converters.JSON
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
        if(!str)
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

    def getSubscriptionAsJson(Subscription sub){
        def result = [:]

        result.cancellationAllowances   = sub.cancellationAllowances
        result.customProperties         = resolveCustomProperties(sub.customProperties)
        result.dateCreated              = sub.dateCreated
        result.documents                = resolveDocuments(sub.documents)
        result.endDate                  = sub.endDate
        result.id                       = sub.id
        result.identifier               = sub.identifier
        result.ids                      = resolveIdentifiers(sub.ids)

        // If a subscription is slaved then any changes to instanceOf will automatically be applied to this subscription
        result.instanceOf               = sub.instanceOf // ?
        result.isSlaved                 = sub.isSlaved?.value // ?

        result.isPublic                 = sub.isPublic?.value

        result.issueEntitlements        = sub.issueEntitlements // ?

        result.lastUpdated              = sub.lastUpdated
        result.manualRenewalDate        = sub.manualRenewalDate
        result.noticePeriod             = sub.noticePeriod
        result.name                     = sub.name
        result.owner                    = sub.owner // ?

        result.prsLinks                 = resolvePrsLinks(sub.prsLinks)

        result.startDate            = sub.startDate
        result.status               = sub.status?.value
        result.type                 = sub.type?.value

        // TODO
        result.costItems            = sub.costItems

        result.derivedSubscriptions = sub.derivedSubscriptions
        result.orgRelations         = resolveOrgRelations(sub.orgRelations)

        result.packages = sub.packages
        result.pendingChanges = sub.pendingChanges


        def json = new JSON(result)
        return json.toString(true)
    }

    def getOrganisationAsJson(Org org){
        def result = [:]

        result.comment          = org.comment
        result.id               = org.id
        result.name             = org.name
        result.membership       = org.membership?.value
        result.orgType          = org.orgType?.value
        result.scope            = org.scope
        result.sector           = org.sector?.value
        result.shortcode        = org.shortcode
        result.status           = org.status?.value
        result.addresses        = resolveAddresses(org.addresses)
        result.affiliations     = org.affiliations
        result.contacts         = resolveContacts(org.contacts)
        result.customProperties = resolveCustomProperties(org.customProperties)
        result.ids              = resolveIdentifiers(org.ids)
        result.incomingCombos   = org.incomingCombos
        result.links            = org.links
        result.outgoingCombos   = org.outgoingCombos
        result.privateProperties    = org.privateProperties
        result.prsLinks         = resolvePrsLinks(org.prsLinks)

        def json = new JSON(result)
        return json.toString(true)
    }
    def getLicenseAsJson(License lic){
        def result = [:]

        def json = new JSON(lic)
        return json.toString(true)
    }

    def resolveAddresses(list) {
        def result = []

        list.each { it ->
            def tmp = [:]
            tmp.id          = it.id
            tmp.street1     = it.street_1
            tmp.street2     = it.street_2
            tmp.pob         = it.pob
            tmp.zipcode     = it.zipcode
            tmp.city        = it.city
            tmp.state       = it.state
            tmp.country     = it.country
            tmp.type        = it.type?.value
            result << tmp
        }
        result
    }

    def resolveContacts(list) {
        def result = []

        list.each { it ->
            def tmp = [:]
            tmp.id              = it.id
            tmp.content         = it.content
            tmp.contentType     = it.contentType?.value
            tmp.type            = it.type?.value
            result << tmp
        }
        result
    }

    def resolveCustomProperties(list) {
        def result = []

        list.each { it ->
            def tmp = [:]
            tmp.id              = it.id
            tmp.value           = it.type?.name
            tmp.descr           = it.type?.descr
            tmp.stringValue     = it.stringValue
            tmp.intValue        = it.intValue
            tmp.decValue        = it.decValue
            tmp.refdataValue    = it.refValue?.value
            tmp.note            = it.note
            result << tmp
        }
        result
    }

    def resolveDocuments(list) {
        def result = []

        list.each { it ->
            def tmp = [:]
            tmp.id          = it.id
            tmp.doctype     = it.doctype?.value

            // nested owner
            tmp.owner           = [:]
            tmp.owner.title     = it.owner?.title
            tmp.owner.type      = it.owner?.type?.value
            tmp.owner.content   = it.owner?.content
            tmp.owner.uuid      = it.owner?.uuid
            tmp.owner.filename  = it.owner?.filename

            result << tmp
        }
        result
    }

    def resolveIdentifiers(list) {
        def result = []

        list.each { it ->
            def tmp = [:]
            tmp.value      = it.identifier?.value
            tmp.namespace  = it.identifier?.ns?.ns
            result << tmp
        }
        result
    }

    def resolveOrgRelations(list) {
        def result = []

        list.each { it ->
            def tmp = [:]
            tmp.endDate     = it.endDate
            tmp.roleType    = it.roleType?.value
            tmp.startDate   = it.startDate
            tmp.title       = it.title

            // nested org
            tmp.org             = [:]
            tmp.org.id          = it.org?.id
            tmp.org.name        = it.org?.name
            tmp.org.shortcode   = it.org?.shortcode
            tmp.org.ids         = resolveIdentifiers(it.org?.ids)

            result << tmp
        }
        result
    }

    def resolvePrsLinks(list) {
        def result = []

        list.each { it ->
            def tmp = [:]
            tmp.startDate       = it.start_date
            tmp.endDate         = it.end_date
            tmp.functionType    = it.functionType?.value

            // nested person
            tmp.prs                 = [:]
            tmp.prs.id              = it.prs?.id
            tmp.prs.firstName       = it.prs?.first_name
            tmp.prs.middleName      = it.prs?.middle_name
            tmp.prs.lastName        = it.prs?.last_name
            tmp.prs.gender          = it.prs?.gender?.value
            tmp.prs.isPublic        = it.prs?.isPublic?.value

            // nested person contacts and addresses
            tmp.prs.contacts            = resolveContacts(it.prs?.contacts)
            tmp.prs.addresses           = resolveAddresses(it.prs?.addresses)

            result << tmp
        }
        result
    }
}
