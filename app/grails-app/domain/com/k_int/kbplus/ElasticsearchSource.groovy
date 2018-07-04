package com.k_int.kbplus

class ElasticsearchSource {

    String identifier
    String name
    String host
    Boolean active = false
    Integer port = 9200
    String index
    String cluster
    Boolean laser_es = false
    Boolean gokb_es = false

    static mapping = {
        id column:'ess_id'
        version column:'ess_version'
        identifier column:'ess_identifier'
        name column:'ess_name', type:'text'
        host column:'ess_host'
        active column:'ess_active'
        port column: 'ess_port'
        index column: 'ess_index'
        cluster column: 'ess_cluster'
        laser_es column: 'ess_laser_es'
        gokb_es column: 'ess_gokb_es'
    }

    static constraints = {
        identifier(nullable:true, blank:false)
        name(nullable:true, blank:false, maxSize:2048)
        host(nullable:true, blank:false)
        active(nullable:true, blank:false)
        port (nullable:true, blank:false)
        index (nullable:true, blank:false)
        cluster (nullable:true, blank:false)
        laser_es (nullable:true, blank:false)
        gokb_es (nullable:true, blank:false)
    }
}
