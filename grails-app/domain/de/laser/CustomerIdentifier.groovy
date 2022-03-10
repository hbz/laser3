package de.laser


import de.laser.annotations.RefdataAnnotation

/**
 * Represents a key/value pair for a customer {@link Org} which enables to load statistic data from a SUSHI server
 */
class CustomerIdentifier {

    @RefdataAnnotation(cat = 'CustomerIdentifierType')
    RefdataValue type
    String value
    String requestorKey
    String note

    Org customer        // target org
    Platform platform   // target platform

    Org owner           // owner
    boolean isPublic = false    // true = visible only for owner

    static transients = ['provider'] // mark read-only accessor methods

    static mapping = {
                id column:'cid_id'
           version column:'cid_version'
              type column:'cid_type_rv_fk'
             value column:'cid_value'
      requestorKey column:'cid_requestor_key', type: 'text'
             note  column:'cid_note', type: 'text'
          customer column:'cid_customer_fk'
          platform column:'cid_platform_fk'
             owner column:'cid_owner_fk'
          isPublic column:'cid_is_public'
    }

    static constraints = {
        value           (nullable: true)
        requestorKey    (nullable: true)
        note     (nullable:true, blank:true)
    }

    /**
     * Retrieves the provider associated to the platform (if the platform exists)
     * @return the {@link Org} linked to the {@link Platform}, null if not exists
     */
    Org getProvider() {
        return platform?.org
    }
}
