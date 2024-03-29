package de.laser.remote

/**
 * This is a record for a domain class which should be indexed in the local ElasticSearch index.
 * Recorded are the count of elements in database, the count of elements in index and when the last index refresh was done.
 * It may be deactivated by setting the active flag to false; then, the ElasticSearch update does not update the objects of the domain class concerned
 */
class FTControl {

    String domainClassName
    String activity
    Long lastTimestamp
    Integer esElements
    Integer dbElements

    /**
     * This flag controls the ElasticSearch index update behavior; set it to false to save performance on local development
     */
    boolean active = true

    Date dateCreated
    Date lastUpdated

    static constraints = {
        lastUpdated (nullable: true)
    }

    static mapping = {
        id              column: 'ftc_id'
        version         column: 'ftc_version'
        active          column: 'ftc_active'
        activity        column: 'ftc_activity'
        domainClassName column: 'ftc_domain_class_name'
        dbElements      column: 'ftc_db_elements'
        esElements      column: 'ftc_es_elements'
        dateCreated     column: 'ftc_date_created'
        lastUpdated     column: 'ftc_last_updated'
    }
}
