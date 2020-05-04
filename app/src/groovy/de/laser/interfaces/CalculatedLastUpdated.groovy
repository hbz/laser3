package de.laser.interfaces

interface CalculatedLastUpdated {

    Date lastUpdatedCascading

    def afterInsert()
    def afterUpdate()
    def afterDelete()

    Date getCalculatedLastUpdated()
}
