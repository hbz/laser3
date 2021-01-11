package de.laser

import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.properties.LicenseProperty
import de.laser.properties.SubscriptionProperty
import grails.gorm.transactions.Transactional
import grails.plugins.orm.auditable.Auditable

import javax.persistence.Transient

//@CompileStatic
@Transactional
class AuditService {

    def changeNotificationService

    def getWatchedProperties(Auditable obj) {
        def result = []

        if (getAuditConfig(obj, AuditConfig.COMPLETE_OBJECT)) {
            obj.getLogIncluded().each { cp ->
                result << cp
            }
        }
        else {
            obj.getLogIncluded().each { cp ->
                if (getAuditConfig(obj, cp.toString())) {
                    result << cp
                }
            }
        }

        result
    }

    AuditConfig getAuditConfig(Auditable obj) {
        AuditConfig.getConfig(obj)
    }

    AuditConfig getAuditConfig(Auditable obj, String field) {
        AuditConfig.getConfig(obj, field)
    }

    Map<String,AuditConfig> getAllAuditConfigs(Auditable obj) {
        Map<String,AuditConfig> auditConfigMap = [:]
        List<AuditConfig> auditConfigs = AuditConfig.findAllByReferenceClassAndReferenceId(obj.class.name,obj.id)
        auditConfigs.each { AuditConfig ac ->
            auditConfigMap.put(ac.referenceField,ac)
        }
        auditConfigMap
    }

    @Transient
    def beforeDeleteHandler(Auditable obj) {

        obj.withNewSession {
            log.debug("beforeDeleteHandler() ${obj}")

            String oid = "${obj.class.name}:${obj.id}"

            if (obj instanceof SubscriptionProperty) {

                Map<String, Object> changeDoc = [
                        OID  : oid,
                        event: 'SubscriptionProperty.deleted',
                        prop : obj.type.name,
                        old  : "",
                        new  : "property removed",
                        name : obj.type.name
                ]
                changeNotificationService.fireEvent(changeDoc)
            }
            else if (obj instanceof LicenseProperty) {

                Map<String, Object> changeDoc = [ OID: oid,
                        event:'LicenseProperty.deleted',
                        prop: obj.type.name,
                        old: "",
                        new: "property removed",
                        name: obj.type.name
                ]
                changeNotificationService.fireEvent(changeDoc)
            }
        }
    }

    @Transient
    def beforeSaveHandler(Auditable obj) {

        obj.withNewSession {
            log.debug("beforeSaveHandler() ${obj}")
        }
    }

    @Transient
    def beforeUpdateHandler(Auditable obj, def oldMap, def newMap) {

        obj.withNewSession {
            log.debug("beforeUpdateHandler() ${obj} : ${oldMap} => ${newMap}")

            if (obj.instanceOf == null) {
                List<String> gwp = getWatchedProperties(obj)

                log.debug("found watched properties: ${gwp}")
                gwp.each { cp ->
                    if (oldMap[cp] != newMap[cp]) {

                        Map<String, Object> event = [:]
                        String clazz = obj."${cp}".getClass().getName()

                        log.debug("notifyChangeEvent() " + obj + " : " + clazz)

                        if (obj instanceof AbstractPropertyWithCalculatedLastUpdated && !obj.type.tenant && obj.isPublic == true) {

                            if (getAuditConfig(obj)) {

                                String old_oid
                                String new_oid
                                if (oldMap[cp] instanceof RefdataValue) {
                                    old_oid = oldMap[cp] ? "${oldMap[cp].class.name}:${oldMap[cp].id}" : null
                                    new_oid = newMap[cp] ? "${newMap[cp].class.name}:${newMap[cp].id}" : null
                                }

                                event = [
                                        OID     : "${obj.class.name}:${obj.id}",
                                        //OID        : "${obj.owner.class.name}:${obj.owner.id}",
                                        event   : "${obj.class.simpleName}.updated",
                                        prop    : cp,
                                        name    : obj.type.name,
                                        type    : obj."${cp}".class.name,
                                        old     : old_oid ?: oldMap[cp], // Backward Compatibility
                                        oldLabel: oldMap[cp] instanceof RefdataValue ? oldMap[cp].toString() : oldMap[cp],
                                        new     : new_oid ?: newMap[cp], // Backward Compatibility
                                        newLabel: newMap[cp] instanceof RefdataValue ? newMap[cp].toString() : newMap[cp],
                                        //propertyOID: "${obj.class.name}:${obj.id}"
                                ]
                            } else {
                                log.debug("ignored because no audit config")
                            }
                        } // CustomProperty
                        else {

                            boolean isSubOrLic = (obj instanceof Subscription || obj instanceof License)

                            if (!isSubOrLic || (isSubOrLic && getAuditConfig(obj, cp))) {

                                if (clazz.equals( RefdataValue.class.name )) {

                                    String old_oid = oldMap[cp] ? "${oldMap[cp].class.name}:${oldMap[cp].id}" : null
                                    String new_oid = newMap[cp] ? "${newMap[cp].class.name}:${newMap[cp].id}" : null

                                    event = [
                                            OID     : "${obj.class.name}:${obj.id}",
                                            event   : "${obj.class.simpleName}.updated",
                                            prop    : cp,
                                            type    : RefdataValue.class.name,
                                            old     : old_oid,
                                            oldLabel: oldMap[cp]?.toString(),
                                            new     : new_oid,
                                            newLabel: newMap[cp]?.toString()
                                    ]
                                } else {

                                    event = [
                                            OID  : "${obj.class.name}:${obj.id}",
                                            event: "${obj.class.simpleName}.updated",
                                            prop : cp,
                                            old  : oldMap[cp],
                                            new  : newMap[cp]
                                    ]
                                }
                            } // Subscription or License
                            else {
                                log.debug("ignored because no audit config")
                            }
                        }

                        log.debug( "event: " + event.toMapString() )

                        if (event) {
                            if (!changeNotificationService) {
                                log.error("changeNotificationService not implemented @ ${it}")
                            } else {
                                changeNotificationService.fireEvent(event)
                            }
                        }
                    }
                }
            }
        }
    }
}
