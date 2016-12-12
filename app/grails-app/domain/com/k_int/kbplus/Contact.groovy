package com.k_int.kbplus

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
}
