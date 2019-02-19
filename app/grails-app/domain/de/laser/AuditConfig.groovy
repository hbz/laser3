package de.laser

import groovy.util.logging.Log4j
import javax.persistence.Transient

@Log4j
class AuditConfig {

    @Transient
    def grailsApplication

    final static COMPLETE_OBJECT = 'COMPLETE_OBJECT'

    Long   referenceId
    String referenceClass

    String referenceField

    static mapping = {
        id              column: 'auc_id'
        version         column: 'auc_version'
        referenceId     column: 'auc_reference_id'
        referenceClass  column: 'auc_reference_class'
        referenceField  column: 'auc_reference_field'
    }

    static constraints = {
        referenceId     (nullable:false, blank:false)
        referenceClass  (nullable:false, blank:false, maxSize:255)
        referenceField  (nullable:false, blank:false, maxSize:255)
    }

    static addConfig(Object obj) {
        addConfig(obj, AuditConfig.COMPLETE_OBJECT)
    }
    static AuditConfig getConfig(Object obj) {
        getConfig(obj, AuditConfig.COMPLETE_OBJECT)
    }
    static void removeConfig(Object obj) {
        removeConfig(obj, AuditConfig.COMPLETE_OBJECT)
    }

    static void addConfig(Object obj, String field) {
        if (obj) {
            new AuditConfig(
                    referenceId: obj.getId(),
                    referenceClass: obj.getClass().name,
                    referenceField: field
            ).save(flush: true)
        }
    }

    static AuditConfig getConfig(Object obj, String field) {
        if (! obj)
            return null

        AuditConfig.findWhere(
            referenceId: obj.getId(),
            referenceClass: obj.getClass().name,
            referenceField: field
        )
    }

    static void removeConfig(Object obj, String field) {
        if (obj) {
            AuditConfig.findAllWhere(
                    referenceId: obj.getId(),
                    referenceClass: obj.getClass().name,
                    referenceField: field
            ).each { it.delete() }
        }
    }

    static void removeAllConfigs(Object obj) {
        if (obj) {
            AuditConfig.findAllWhere(
                    referenceId: obj.getId(),
                    referenceClass: obj.getClass().name
            ).each { it.delete() }
        }
    }
}
