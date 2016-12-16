package com.k_int.kbplus

class Person {

    String       first_name
    String       middle_name
    String       last_name
    RefdataValue gender
    
    static mapping = {
        id          column:'prs_id'
        version     column:'prs_version'
        first_name  column:'prs_first_name'
        middle_name column:'prs_middle_name'
        last_name   column:'prs_last_name'
        gender      column:'prs_gender'
    }
    
    static mappedBy = [
        roleLinks: 'prs',
        contacts:  'prs'
    ]
  
    static hasMany = [
        roleLinks: PersonRole,
        contacts:  Contact
    ]
    
    static constraints = {
        first_name  (nullable:false, blank:false)
        middle_name (nullable:true,  blank:true)
        last_name   (nullable:false, blank:false)
        gender      (nullable:true)
    }
    
    static getAllRefdataValues() {
        RefdataCategory.getAllRefdataValues('Gender')
    }
    
    @Override
    String toString() {
        last_name + ', ' + first_name + ' ' + middle_name + ' (' + id + ')'
    }
}
