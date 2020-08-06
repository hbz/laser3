package de.laser

import com.k_int.kbplus.License
import com.k_int.kbplus.LicenseProperty
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.SubscriptionProperty
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.interfaces.AuditableSupport
import grails.transaction.Transactional
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.persistence.Transient

//@CompileStatic
@Transactional
class AuditService {

    GrailsApplication grailsApplication

    def changeNotificationService

    def getWatchedProperties(AuditableSupport obj) {
        def result = []

        if (getAuditConfig(obj, AuditConfig.COMPLETE_OBJECT)) {
            obj.controlledProperties.each { cp ->
                result << cp
            }
        }
        else {
            obj.controlledProperties.each { cp ->
                if (getAuditConfig(obj, cp.toString())) {
                    result << cp
                }
            }
        }

        result
    }

    AuditConfig getAuditConfig(AuditableSupport obj) {
        AuditConfig.getConfig(obj)
    }

    AuditConfig getAuditConfig(AuditableSupport obj, String field) {
        AuditConfig.getConfig(obj, field)
    }

    @Transient
    def beforeDeleteHandler(AuditableSupport obj) {

        obj.withNewSession {
            log.debug("beforeDeleteHandler() ${obj}")

            String oid = "${obj.class.name}:${obj.id}"

            if (obj instanceof SubscriptionProperty) {

                Map<String, Object> changeDoc = [
                        OID  : oid,
                        event: 'SubscriptionCustomProperty.deleted',
                        prop : obj.type.name,
                        old  : "",
                        new  : "property removed",
                        name : obj.type.name
                ]
                changeNotificationService.fireEvent(changeDoc)
            }
            else if (obj instanceof LicenseProperty) {

                Map<String, Object> changeDoc = [ OID: oid,
                        event:'LicenseCustomProperty.deleted',
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
    def beforeSaveHandler(AuditableSupport obj) {

        obj.withNewSession {
            log.debug("beforeSaveHandler() ${obj}")
        }
    }

    @Transient
    def beforeUpdateHandler(AuditableSupport obj, def oldMap, def newMap) {

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
                                        type    : obj."${cp}".getClass().toString(),
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

                                if (clazz.equals("com.k_int.kbplus.RefdataValue")) {

                                    String old_oid = oldMap[cp] ? "${oldMap[cp].class.name}:${oldMap[cp].id}" : null
                                    String new_oid = newMap[cp] ? "${newMap[cp].class.name}:${newMap[cp].id}" : null

                                    event = [
                                            OID     : "${obj.class.name}:${obj.id}",
                                            event   : "${obj.class.simpleName}.updated",
                                            prop    : cp,
                                            type    : RefdataValue.toString(),
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

                        log.debug( event.toMapString() )

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
