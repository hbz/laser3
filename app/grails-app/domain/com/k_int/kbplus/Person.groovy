package com.k_int.kbplus

class Person {

    String       first_name
    String       middle_name
    String       last_name
    RefdataValue gender     // 'Gender'
    Org          owner
    RefdataValue isPublic   // 'YN'
    
    static mapping = {
        id          column:'prs_id'
        version     column:'prs_version'
        first_name  column:'prs_first_name'
        middle_name column:'prs_middle_name'
        last_name   column:'prs_last_name'
        gender      column:'prs_gender_rv_fk'
        owner       column:'prs_owner_fk'
        isPublic    column:'prs_is_public_rv_fk'
    }
    
    static mappedBy = [
        roleLinks: 'prs',
        addresses: 'prs',
        contacts:  'prs'
    ]
  
    static hasMany = [
        roleLinks: PersonRole,
        addresses: Address,
        contacts:  Contact
    ]
    
    static constraints = {
        first_name  (nullable:false, blank:false)
        middle_name (nullable:true,  blank:true)
        last_name   (nullable:false, blank:false)
        gender      (nullable:true)
        owner       (nullable:true)
        isPublic    (nullable:true)
    }
    
    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }
    
    @Override
    String toString() {
        last_name + ', ' + first_name + ' ' + middle_name + ' (' + id + ')'
    }
}
