package de.laser.interfaces

/**
 * This abstract container keeps track that the last update timestamp is being set
 */
interface CalculatedLastUpdated {

    def afterInsert()
    def afterUpdate()
    def afterDelete()

    Date _getCalculatedLastUpdated()
}
