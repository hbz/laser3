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
    String url

    Date dateCreated
    Date lastUpdated

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
        url column: 'ess_url'
        dateCreated column: 'ess_date_created'
        lastUpdated column: 'ess_last_updated'
    }

    static constraints = {
        identifier(nullable:true, blank:false)
        name(nullable:true, blank:false, maxSize:2048)
        host(nullable:true, blank:false)
        active      (nullable:false, blank:false)
        port (nullable:true, blank:false)
        index (nullable:true, blank:false)
        cluster (nullable:true, blank:false)
        laser_es    (nullable:false, blank:false)
        gokb_es     (nullable:false, blank:false)
        url (nullable:true, blank:false)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }
}
