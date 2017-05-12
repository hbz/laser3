package com.k_int.kbplus

import com.k_int.kbplus.auth.*
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
}
