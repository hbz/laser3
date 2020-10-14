package de.laser

import groovy.util.logging.Slf4j

import javax.persistence.Transient

@Slf4j
class AuditConfig {

    def grailsApplication

    final static COMPLETE_OBJECT = 'COMPLETE_OBJECT'

    Long   referenceId
    String referenceClass

    String referenceField

    Date dateCreated
    Date lastUpdated

    static mapping = {
        id              column: 'auc_id'
        version         column: 'auc_version'
        referenceId     column: 'auc_reference_id',    index:'auc_ref_idx'
        referenceClass  column: 'auc_reference_class', index:'auc_ref_idx'
        referenceField  column: 'auc_reference_field'
        lastUpdated     column: 'auc_last_updated'
        dateCreated     column: 'auc_date_created'
    }

    static constraints = {
        referenceClass  (blank:false, maxSize:255)
        referenceField  (blank:false, maxSize:255)
        lastUpdated     (nullable: true)
        dateCreated     (nullable: true)
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
        withTransaction {
            if (obj) {
                AuditConfig config = new AuditConfig(
                        referenceId: obj.getId(),
                        referenceClass: obj.getClass().name,
                        referenceField: field
                )
                if (! config.save())
                    log.error(config.errors.toString())
            }
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

    static List<AuditConfig> getConfigs(Object obj) {
        if (! obj)
            return null

        List<AuditConfig> configs = []

        obj.getLogIncluded().each{ prop ->
            def config = getConfig(obj, prop)
            if(config){
                configs << config
            }
        }
        return configs
    }

    static void removeConfig(Object obj, String field) {
        withTransaction {
            if (obj) {
                AuditConfig.findAllWhere(
                        referenceId: obj.getId(),
                        referenceClass: obj.getClass().name,
                        referenceField: field
                ).each { it.delete() }
            }
        }
    }

    static void removeAllConfigs(Object obj) {
        withTransaction {
            if (obj) {
                AuditConfig.findAllWhere(
                        referenceId: obj.getId(),
                        referenceClass: obj.getClass().name
                ).each { it.delete() }
            }
        }
    }
}
