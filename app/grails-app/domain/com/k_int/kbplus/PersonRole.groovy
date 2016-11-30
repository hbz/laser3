package com.k_int.kbplus

class PersonRole {

    RefdataValue    roleType
    License         lic
    Org             org
    Package         pkg
    Subscription    sub   
    TitleInstance   title
    def             start_date // TODO
    def             end_date   // TODO
    
    static belongsTo = [
        prs:        Person,
    ]
    
    static mapping = {
        id          column:'pr_id'
        version     column:'pr_version'
        roleType    column:'pr_roletype_rv_fk'
        prs         column:'pr_prs_fk'
        lic         column:'pr_lic_fk'
        org         column:'pr_org_fk'
        pkg         column:'pr_pkg_fk'
        sub         column:'pr_sub_fk'
        title       column:'pr_title_fk'
        start_date  column:'pr_startdate'
        end_date    column:'pr_enddate'
    }
    
    static constraints = {
        roleType    (nullable:false)
        prs         (nullable:false)
        lic         (nullable:true)
        org         (nullable:true)
        pkg         (nullable:true)
        sub         (nullable:true)
        title       (nullable:true)
        start_date  (nullable:true)
        end_date    (nullable:true)
    }
    
    static getAllRefdataValues() {
        RefdataCategory.getAllRefdataValues('Person Role')
    }
}
