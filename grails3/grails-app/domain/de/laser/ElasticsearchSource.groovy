package de.laser

/**
 * A source pointing to an ElasticSearch index.
 * IMPORTANT: LAS:eR is fed by two indices: the local ElasticSearch index whose sources are being kept here. These ES indices mirror the LAS:eR data (see {@link com.k_int.kbplus.DataloadService})
 * Then, there are external ElasticSearch indices serving as data sources; package and platform data are being loaded on-the-fly from we:kb instances. Those sources are being recorded in {@link ApiSource}.
 * Moreover, title data is being mirrored from we:kb instances as well; this is done by bulk synchronisation and those API sources are being recorded in {@link GlobalRecordSource}s
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

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }
}
