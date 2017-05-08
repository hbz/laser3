package com.k_int.kbplus

import groovy.util.logging.Log4j
import org.apache.commons.logging.LogFactory
import org.hibernate.Query
import groovy.util.logging.*

@Log4j
class Contact {
    
    String       content
    RefdataValue contentType    // RefdataCategory 'ContactContentType'
    RefdataValue type           // RefdataCategory 'ContactType'
    Person       prs            // person related contact
    Org          org            // org related contact
                                // if prs AND org set,
                                // this contact belongs to a person in context of an org
    
    static mapping = {
        id          column:'ct_id'
        version     column:'ct_version'
        content     column:'ct_content'
        contentType column:'ct_content_type_rv_fk'
        type        column:'ct_type_rv_fk'
        prs         column:'ct_prs_fk'
        org         column:'ct_org_fk'
    }
    
    static constraints = {
        content     (nullable:true, blank:true)
        contentType (nullable:true, blank:true)
        type        (nullable:false)
        prs         (nullable:true)
        org         (nullable:true)
    }
    
    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }
    
    @Override
    String toString() {
        contentType?.value + ', ' + content + ' (' + id + '); ' + type?.value
    }
    
    // TODO implement existing check (lookup)
    static def customCreate(content, contentType, type, person, organisation) {
        
        LogFactory.getLog(this).debug("trying to save new contact: ${content} ${contentType} ${type}")
        def result = null

        if(person && organisation){
            type = RefdataValue.findByValue("Job-related")
        }
        
        result = new Contact(
            content:     content,
            contentType: contentType,
            type:        type,
            prs:         person,
            org:         organisation
            )
            
        if(!result.save()){
            result.errors.each{ println it }
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
