package com.k_int.kbplus

class Person {

    enum Gender {
        M ("M"), W ("W")
        String name
        Gender (String name) {
            this.name = name
        }
    }
    
    String first_name
    String middle_name
    String last_name
    Gender gender // TODO mapping in db
    Org    org
    
    static mapping = {
        id          column:'prs_id'
        version     column:'version'
        first_name  column:'prs_first_name'
        middle_name column:'prs_middle_name'
        last_name   column:'prs_last_name'
        gender      column:'prs_gender'
        org         column:'prs_org_fk'
    }
    
    static constraints = {
        first_name  (nullable:false, blank:false)
        middle_name (nullable:true,  blank:true)
        last_name   (nullable:false, blank:false)
        gender      (nullable:true)
        org         (nullable:true)
    }
}
