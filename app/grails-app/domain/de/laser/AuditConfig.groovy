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
        addConfig(obj, COMPLETE_OBJECT)
    }
    static getConfig(Object obj) {
        getConfig(obj, COMPLETE_OBJECT)
    }
    static removeConfig(Object obj) {
        removeConfig(obj, COMPLETE_OBJECT)
    }

    static addConfig(Object obj, String field) {
        new AuditConfig (
            referenceId: obj.getId(),
            referenceClass: obj.getClass().name,
            referenceField: field
        ).save(flush: true)
    }

    static getConfig(Object obj, String field) {
        AuditConfig.findAllWhere(
            referenceId: obj.getId(),
            referenceClass: obj.getClass().name,
            referenceField: field
        )
    }

    static removeConfig(Object obj, String field) {
        AuditConfig.findAllWhere(
                referenceId: obj.getId(),
                referenceClass: obj.getClass().name,
                referenceField: field
        ).each{ it.delete() }
    }

    static removeAllConfigs(Object obj) {
        AuditConfig.findAllWhere(
                referenceId: obj.getId(),
                referenceClass: obj.getClass().name
        ).each{ it.delete() }
    }
}
