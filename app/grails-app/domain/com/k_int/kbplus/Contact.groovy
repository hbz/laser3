package com.k_int.kbplus

class Contact {
    
    String       content
    RefdataValue contentType    // 'ContactContentType'
    RefdataValue type           // 'ContactType'
    Person       prs
    Org          org
    
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
}
