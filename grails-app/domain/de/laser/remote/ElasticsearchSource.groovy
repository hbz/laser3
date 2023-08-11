package de.laser.remote

/**
 * A source pointing to a local ElasticSearch index, mirroring the LAS:eR data (see {@link de.laser.DataloadService})
 * Distinct it from {@link de.laser.remote.ApiSource}s and {@link de.laser.remote.GlobalRecordSource}s where a direct connection to the knowledge base is being established, fetching either data on the fly or synchronising them to LAS:eR.
 * Those are title, provider, package and platform data; this is done by bulk synchronisation and those API sources are being recorded in {@link de.laser.remote.GlobalRecordSource}s
 */
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
        port (nullable:true)
        index (nullable:true, blank:false)
        cluster (nullable:true, blank:false)
        url (nullable:true, blank:false)
        lastUpdated (nullable: true)
    }
}
