package de.laser.interfaces

interface CalculatedLastUpdate {

    Date cascadingLastUpdated

    def afterInsert()
    def afterUpdate()
    def afterDelete()

    Date getCalculatedLastUpdate()
}
