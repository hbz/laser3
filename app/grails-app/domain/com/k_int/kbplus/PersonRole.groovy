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

    /**
     * Generic setter
     */
    def setReference(def owner) {
        org     = owner instanceof Org ? owner : org

        lic     = owner instanceof License ? owner : lic
        cluster = owner instanceof Cluster ? owner : cluster
        pkg     = owner instanceof Package ? owner : pkg
        sub     = owner instanceof Subscription ? owner : sub
        title   = owner instanceof TitleInstance ? owner : title
    }

    def getReference() {

        if (lic)        return 'lic:' + lic.id
        if (cluster)    return 'cluster:' + cluster.id
        if (pkg)        return 'pkg:' + pkg.id
        if (sub)        return 'sub:' + sub.id
        if (title)      return 'title:' + title.id
    }

    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }

    static def lookup(prs, lic, org, cluster, pkg, sub, title, start_date, end_date, functionType) {

        def personRole
        def p = PersonRole.findAllWhere(
                prs:        prs,
                lic:        lic,
                org:        org,
                cluster:    cluster,
                pkg:        pkg,
                sub:        sub,
                title:      title,
                start_date: start_date,
                end_date:   end_date,
                functionType:   functionType
        ).sort({id: 'asc'})

        if ( p.size() > 0 ) {
            personRole = p[0]
        }

        personRole
    }

    static def getByPersonAndOrgAndRespValue(Person prs, Org org, def resp) {

        def result = PersonRole.findAllWhere(
            prs: prs,
            org: org,
            responsibilityType: RefdataValue.getByValueAndCategory(resp, 'Person Responsibility')
        )

        result.first()
    }
}
