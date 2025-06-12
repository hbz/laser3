package de.laser


import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants
import de.laser.wekb.Platform
import de.laser.wekb.Provider

/**
 * Represents a key/value pair for a customer {@link Org} which enables to load statistic data from a SUSHI server
 */
class CustomerIdentifier {

    @RefdataInfo(cat = RDConstants.CUSTOMER_IDENTIFIER_TYPE)
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
          customer column:'cid_customer_fk',    index: 'cid_customer_idx'
          platform column:'cid_platform_fk',    index: 'cid_platform_idx'
             owner column:'cid_owner_fk',       index: 'cid_owner_idx'
          isPublic column:'cid_is_public'
    }

    static constraints = {
        value           (nullable: true)
        requestorKey    (nullable: true)
        note     (nullable:true, blank:true)
        platform(unique: ['customer'])
    }

    /**
     * Retrieves the provider associated to the platform (if the platform exists)
     * @return the {@link Org} linked to the {@link Platform}, null if not exists
     */
    Provider getProvider() {
        return platform?.provider
    }
}
