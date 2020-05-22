package de.laser.interfaces

interface AuditableSupport {

    static auditable            = [ ignore: ['version', 'lastUpdated'] ]

    static controlledProperties = []
}
