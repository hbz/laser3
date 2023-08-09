package de.laser

import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.properties.LicenseProperty
import de.laser.properties.SubscriptionProperty
import grails.gorm.transactions.Transactional
import grails.plugins.orm.auditable.Auditable

import javax.persistence.Transient

/**
 * This service manages inheritance triggering, i.e. if a property has been changed which is inherited to member objects,
 * the change is being processed to the member objects
 */
@Transactional
class AuditService {

    /**
     * Retrieves the list of properties which trigger inheritance for the given object
     * @param obj the object upon which a change has been performed
     * @return a list of properties which trigger inheritance
     */
    List<String> getWatchedProperties(Auditable obj) {
        List<String> result = []

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

    /**
     * Substitution call for {@link AuditConfig#getConfig(java.lang.Object)}
     * @param obj the object whose inheritance should be checked
     * @return the result of {@link AuditConfig#getConfig(java.lang.Object)}
     */
    AuditConfig getAuditConfig(Auditable obj) {
        AuditConfig.getConfig(obj)
    }

    /**
     * Substitution call for {@link AuditConfig#getConfig(java.lang.Object, java.lang.String)}
     * @param obj the object whose inheritance should be checked
     * @param field the field whose inheritance flag should be checked
     * @return the result of {@link AuditConfig#getConfig(java.lang.Object, java.lang.String)}
     */
    AuditConfig getAuditConfig(Auditable obj, String field) {
        AuditConfig.getConfig(obj, field)
    }

    /**
     * Retrieves all inheritance settings for the given object
     * @param obj the object whose settings should be retrieved
     * @return a map of structure [field: setting] reflecting the inheritance configuration of the given object
     */
    Map<String,AuditConfig> getAllAuditConfigs(Auditable obj) {
        Map<String,AuditConfig> auditConfigMap = [:]
        List<AuditConfig> auditConfigs = AuditConfig.findAllByReferenceClassAndReferenceId(obj.class.name,obj.id)
        auditConfigs.each { AuditConfig ac ->
            auditConfigMap.put(ac.referenceField,ac)
        }
        auditConfigMap
    }

    /**
     * Propagates the deletion of a public subscription or license property to member objects
     * @param obj the deleted property object
     * @see LicenseProperty
     * @see SubscriptionProperty
     */
    @Transient
    def beforeDeleteHandler(Auditable obj) {

        log.debug("beforeDeleteHandler() ${obj}")
        Set depending = (new GroovyClassLoader()).loadClass(obj.class.name).findAllByInstanceOf(obj)
        depending.each { dependingObj ->
            dependingObj.delete()
        }
    }

    /**
     * Propagates a change on the given object; a pending change object is being generated which
     * tracks the change from old to new for that it may be decided whether the change should be applied on a
     * given member object or not
     * @param obj the object on which a change has been performed
     * @param oldMap the map of old values
     * @param newMap the map of new values
     * @see PendingChange
     */
    @Transient
    def beforeUpdateHandler(Auditable obj, def oldMap, def newMap) {

        //from withNewTransaction to withTransaction and as of ERMS-5207, back to withNewTransaction; it may be that it will conflict with other tickets! Keep an eye on this closure!
        obj.withNewTransaction {
            log.debug("beforeUpdateHandler() ${obj} : ${oldMap} => ${newMap}")

            if (obj.instanceOf == null) {
                List<String> gwp = getWatchedProperties(obj)

                log.debug("found watched properties: ${gwp}")
                gwp.each { cp ->
                    if (oldMap[cp] != newMap[cp]) {

                        Map<String, Object> event = [:]
                        String clsName = obj."${cp}".getClass().getName()

                        log.debug("trigger inheritance: " + obj + " : " + clsName)
                        // CustomProperty or Identifier
                        if ((obj instanceof AbstractPropertyWithCalculatedLastUpdated && !obj.type.tenant && obj.isPublic == true) || obj instanceof Identifier) {
                            if (getAuditConfig(obj)) {
                                Set depending = (new GroovyClassLoader()).loadClass(obj.class.name).findAllByInstanceOf(obj)
                                depending.each { dependingObj ->
                                    dependingObj[cp] = newMap[cp]
                                    //it would be way more elegant to execute it by query but it needs to trigger the API increment, so I need to trigger the beforeUpdate() handlers of each object!
                                    dependingObj.save()
                                }
                            } else {
                                log.debug("ignored because no audit config")
                            }
                        }
                        // Subscription or License
                        else {

                            boolean isSubOrLic = (obj instanceof Subscription || obj instanceof License)

                            if (!isSubOrLic || (isSubOrLic && getAuditConfig(obj, cp))) {
                                Set depending = (new GroovyClassLoader()).loadClass(obj.class.name).findAllByInstanceOf(obj)
                                depending.each { dependingObj ->
                                    dependingObj[cp] = newMap[cp]
                                    //it would be way more elegant to execute it by query but it needs to trigger the API increment, so I need to trigger the beforeUpdate() handlers of each object!
                                    dependingObj.save()
                                }
                            } else {
                                log.debug("ignored because no audit config")
                            }
                        }
                    }
                }
            }
        }
    }
}
