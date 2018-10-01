package de.laser

import groovy.util.logging.Log4j

import javax.persistence.Transient

@Log4j
class AuditConfig {

    @Transient
    def grailsApplication

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
        referenceField  (nullable:true,  blank:false, maxSize:255)
    }

    static addConfig(Object obj, String field) {
        new AuditConfig (
            referenceId: obj.getId(),
            referenceClass: obj.getClass().name,
            referenceField: field
        ).save(flush: true)
    }

    static getConfig(Object obj) {
        def result = []
        try {
            result = AuditConfig.findAllWhere(
                referenceId: obj.getId(),
                referenceClass: obj.getClass().name,
                referenceField: null
            )
        } catch(Exception e) {
            log.error(e)
        }
        result
    }

    static getConfig(Object obj, String field) {
        def result = []
        try {
            result = AuditConfig.findAllWhere(
                referenceId: obj.getId(),
                referenceClass: obj.getClass().name,
                referenceField: field
            )
        } catch(Exception e) {
            log.error(e)
        }
        result
    }

    static removeConfig(Object obj) {
        AuditConfig.findAllWhere(
                referenceId: obj.getId(),
                referenceClass: obj.getClass().name,
                referenceField: null
        ).delete(flush:true)
    }

    static removeConfig(Object obj, String field) {
        AuditConfig.findAllWhere(
                referenceId: obj.getId(),
                referenceClass: obj.getClass().name,
                referenceField: field
        ).delete(flush:true)
    }
}
