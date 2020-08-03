package de.laser.interfaces

interface CalculatedLastUpdated {

    def afterInsert()
    def afterUpdate()
    def afterDelete()

    Date _getCalculatedLastUpdated()
}
