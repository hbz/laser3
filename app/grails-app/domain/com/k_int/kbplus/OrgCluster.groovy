package com.k_int.kbplus

class OrgCluster {
    
    static belongsTo = [
        org:     Org,
        cluster: Cluster
      ]

    static mapping = {
        id       column:'oc_id'
        version  column:'oc_version'
        org      column:'oc_org_fk'
        cluster  column:'oc_cluster_fk'
    }
    
    static constraints = {
        org      (nullable:false)
        cluster  (nullable:false)
    }
}
