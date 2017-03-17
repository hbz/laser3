package com.k_int.kbplus

import groovy.util.logging.Log4j
import org.apache.commons.logging.LogFactory
import org.hibernate.Query
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

        def hqlMail  = Contact.hqlHelper(mail)  
        def hqlPhone = Contact.hqlHelper(phone)
        def hqlType  = Contact.hqlHelper(type)
        def hqlPrs   = Contact.hqlHelper(person)
        def hqlOrg   = Contact.hqlHelper(organisation)
          
        def query = "from Contact c where lower(c.mail) ${hqlMail[1]} and c.phone ${hqlPhone[1]} and c.type ${hqlType[1]} and c.prs ${hqlPrs[1]} and c.org ${hqlOrg[1]}" 
        
        def queryParams = [hqlMail[0].toLowerCase(), hqlPhone[0], hqlType[0], hqlPrs[0], hqlOrg[0]]
        queryParams.removeAll([null, ''])
        
        log.debug(query)
        log.debug('@ ' + queryParams)
        
        def c = Contact.executeQuery(query, queryParams)
   
        if(!c && type){
            LogFactory.getLog(this).debug("trying to save new contact: ${mail} ${phone} ${type}")
            
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
            // TODO catch multiple results
            if(c.size() > 0){
                result = c[0]
            }
        }
        
        result  
    }
    
    /**
     *
     * @param obj
     * @return list with two elements for building hql query
     */
    static List hqlHelper(obj){
        
        def result = []
        result.add(obj ? obj : '')
        result.add(obj ? '= ?' : 'is null')
        
        result
    }
}
