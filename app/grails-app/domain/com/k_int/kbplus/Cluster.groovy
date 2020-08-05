package com.k_int.kbplus

import de.laser.helper.RDConstants
import de.laser.helper.RefdataAnnotation

class Cluster {

    String       name
    String       definition

    Date dateCreated
    Date lastUpdated

    @RefdataAnnotation(cat = RDConstants.CLUSTER_TYPE)
    RefdataValue type
    
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

        dateCreated column: 'cl_date_created'
        lastUpdated column: 'cl_last_updated'

        orgs        batchSize: 10
        prsLinks    batchSize: 10
    }
    static constraints = {
        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }

    static List<RefdataValue> getAllRefdataValues() {
        RefdataCategory.getAllRefdataValues('ClusterType')
    }
    
    @Override
    String toString() {
        name + ', ' + definition + ' (' + id + ')'
    }
}
