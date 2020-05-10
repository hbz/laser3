package de.laser

import de.laser.interfaces.AuditableSupport
import org.codehaus.groovy.grails.commons.GrailsApplication

//@CompileStatic
class AuditService {

    GrailsApplication grailsApplication


    def getWatchedProperties(AuditableSupport auditable) {
        def result = []

        if (getAuditConfig(auditable, AuditConfig.COMPLETE_OBJECT)) {
            auditable.controlledProperties.each { cp ->
                result << cp
            }
        }
        else {
            auditable.controlledProperties.each { cp ->
                if (getAuditConfig(auditable, cp)) {
                    result << cp
                }
            }
        }

        result
    }

    AuditConfig getAuditConfig(AuditableSupport auditable) {
        AuditConfig.getConfig(auditable)
    }

    AuditConfig getAuditConfig(AuditableSupport auditable, String field) {
        AuditConfig.getConfig(auditable, field)
    }
}
