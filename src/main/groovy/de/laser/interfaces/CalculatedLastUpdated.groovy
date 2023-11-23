package de.laser.interfaces

/**
 * This abstract container keeps track that the last update timestamp is being set
 */
interface CalculatedLastUpdated {

    def afterInsert()
    def afterUpdate()
    def afterDelete()

    /**
     * Gets the date of the last update of the given object
     * @return the lastUpdated or lastUpdatedCascading property
     */
    Date _getCalculatedLastUpdated()
}
