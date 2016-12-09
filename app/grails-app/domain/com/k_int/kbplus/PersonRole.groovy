package com.k_int.kbplus

class PersonRole {

    RefdataValue    roleType
    License         lic
    Org             org
    Cluster         cluster
    Package         pkg
    Subscription    sub   
    TitleInstance   title
    Date            start_date 
    Date            end_date
    
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
        cluster     column:'pr_cluster_fk'
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
        cluster     (nullable:true)
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
