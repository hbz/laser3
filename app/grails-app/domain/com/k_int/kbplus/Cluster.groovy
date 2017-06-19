package com.k_int.kbplus

class Cluster {

    String       name
    String       definition
    RefdataValue type           // RefdataCategory 'ClusterType'
    
    static hasMany = [
        orgs:     OrgRole,
        prsLinks: PersonRole,
    ]
    
    static mappedBy = [
        orgs:     'cluster',
        prsLinks: 'cluster'
    ]

    static mapping = {
        id         column:'cl_id'
        version    column:'cl_version'
        name       column:'cl_name'
        definition column:'cl_definition'
        type       column:'cl_type_rv_fk'
    }

    static getAllRefdataValues() {
        RefdataCategory.getAllRefdataValues('ClusterType')
    }
    
    @Override
    String toString() {
        name + ', ' + definition + ' (' + id + ')'
    }
}
