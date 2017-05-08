package com.k_int.kbplus

class PersonRole {

    RefdataValue    functionType        // 'Person Function'; exclusive with responsibilityType
    RefdataValue    responsibilityType  // 'Person Responsibility'; exclusive with functionType
    License         lic
    Cluster         cluster
    Package         pkg
    Subscription    sub   
    TitleInstance   title
    Date            start_date 
    Date            end_date
    
    static belongsTo = [
        prs:        Person,
        org:        Org
    ]
    
    static mapping = {
        id          column:'pr_id'
        version     column:'pr_version'
        functionType        column:'pr_function_type_rv_fk'
        responsibilityType  column:'pr_responsibility_type_rv_fk'
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
        functionType        (nullable:true)
        responsibilityType  (nullable:true)
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
    
    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }
}
