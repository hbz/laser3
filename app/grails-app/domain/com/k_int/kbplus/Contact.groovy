package com.k_int.kbplus

import groovy.util.logging.Log4j
import org.apache.commons.logging.LogFactory
import groovy.util.logging.*

@Log4j
class Contact {
    
    String       mail
    String       phone
    RefdataValue type
    Person       prs
    Org          org
    
    static mapping = {
        id      column:'ct_id'
        version column:'ct_version'
        mail    column:'ct_mail'
        phone   column:'ct_phone'
        type    column:'ct_type_rv_fk'
        prs     column:'ct_prs_fk'
        org     column:'ct_org_fk'
    }
    
    static constraints = {
        mail   (nullable:true, blank:true)
        phone  (nullable:true, blank:true)
        type   (nullable:false)
        prs    (nullable:true)
        org    (nullable:true)
    }
    
    static getAllRefdataValues() {
        RefdataCategory.getAllRefdataValues('ContactType')
    }
    
    @Override
    String toString() {
        mail + ', ' + phone + ' (' + id + ')'
    }
    
    static def lookupOrCreate(mail, phone, type, person, organisation) {
        
        def result = null
        
        mail         = mail   ? mail : ''
        phone        = phone  ? phone : ''
        type         = type   ? type : null
        person       = person ? person : null
        organisation = organisation ? organisation : null
        
        def c = Contact.executeQuery(
            "from Contact c where lower(c.mail) = ? and lower(c.phone) = ?",
            [mail.toLowerCase(), phone.toLowerCase()]
            )
       
        if(!c && type){
            LogFactory.getLog(this).debug('trying to save new contact')
            
            result = new Contact(
                mail:  mail,
                phone: phone,
                type:  type,
                prs:   person,
                org:   organisation
                )
                
            if(!result.save()){
                result.errors.each{ println it }
            }
        }
        else {
            result = c
        }
        
        result
       
    }
}
