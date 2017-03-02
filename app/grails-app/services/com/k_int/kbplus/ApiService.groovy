package com.k_int.kbplus

import com.k_int.kbplus.auth.*
import org.apache.commons.lang3.StringUtils
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
        
        xml.institution.each{ inst ->

            def identifiers = [:]
            def org         = null
            def person      = null
            
            def title = normString(inst.title.text())
            title     = StringUtils.left(title, 128) // TODO: HOTFIX
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
                        normString(inst.country?.text()),
                        normString(inst.country?.text()),
                        RefdataValue.findByValue("Postal address"),
                        null,
                        org
                        )
                }
                
                // adding contact persons
                def cpList    = flattenToken(inst.contactperson)
                def mailList  = flattenToken(inst.email)
                def phoneList = flattenToken(inst.telephone)
                         
                cpList.eachWithIndex{ token, i ->
                    
                    def cpText    = cpList[i]    ? cpList[i].text()    : ''
                    def mailText  = mailList[i]  ? mailList[i].text()  : ''
                    def phoneText = phoneList[i] ? phoneList[i].text() : ''
                    
                    // adding person
                    def cpParts = normString(cpText).replaceAll("(Herr|Frau)", "").trim().split(" ")
  
                    firstName  = cpParts[0].trim()
                    middleName = cpParts.size() > 2 ? cpParts[1..cpParts.size() - 2].join(" ") : ''
                    lastName   = cpParts[cpParts.size() - 1].trim()
  
                    // create if no match found
                    if(firstName != '' && lastName != ''){
                        person = Person.lookupOrCreate(
                            firstName,
                            middleName,
                            lastName,
                            null /* gender */,
                            org,
                            RefdataValue.findByValue("General contact person")
                            )
                            
                        // adding contact
                        email     = normString(mailText)
                        telephone = phoneText.replaceAll("(/|-|\\(|\\))", "").replaceAll("\\s+", "")
    
                        if(email != '' || telephone != ''){
                            
                            // create if no match found
                            Contact.lookupOrCreate(
                                email,
                                telephone,
                                RefdataValue.findByValue("Job-related"),
                                person,
                                org
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
