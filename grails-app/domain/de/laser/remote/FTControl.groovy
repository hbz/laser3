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
        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }
}
