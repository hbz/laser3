package de.laser.interfaces

interface AuditableSupport {

    static auditable            = [ ignore: ['version', 'lastUpdated'] ]

    static controlledProperties = []

    Collection<String> getLogIncluded()     // grails 3

    Collection<String> getLogExcluded()     // grails 3
}
