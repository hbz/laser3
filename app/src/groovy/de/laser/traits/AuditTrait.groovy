package de.laser.traits

import com.k_int.kbplus.License
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import de.laser.AuditConfig

import javax.persistence.Transient

trait AuditTrait {

    /**
     * IMPORTANT:
     *
     * Declare auditable and controlledProperties in implementing classes.
     *
     * Overwrite onChange() and/or notifyDependencies() if needed ..
     *
     */

    // def changeNotificationService

    // static auditable = [ ignore: ['version', 'lastUpdated', 'pendingChanges'] ]

    // static controlledProperties = ['name', 'date', 'etc']

    @Transient
    def onDelete = { oldMap ->
        log?.debug("onDelete() ${this}")
    }

    @Transient
    def onSave = {
        log?.debug("onSave() ${this}")
    }

    @Transient
    def onChange = { oldMap, newMap ->

        log?.debug("onChange(${this.id}): ${oldMap} => ${newMap}")

        getWatchedProperties()?.each { cp ->
            if (oldMap[cp] != newMap[cp]) {
                def event
                def clazz = this."${cp}".getClass().getName()

                log?.debug("notifyChangeEvent() " + this + " : " + clazz)

                if (this instanceof CustomProperty) {

                    if (AuditConfig.getConfig(this)) {

                        event = [
                                OID        : "${this.class.name}:${this.id}",
                                //OID        : "${this.owner.class.name}:${this.owner.id}",
                                event      : "${this.class.simpleName}.updated",
                                prop       : cp,
                                name       : type.name,
                                type       : this."${cp}".getClass().toString(),
                                old        : oldMap[cp] instanceof RefdataValue ? oldMap[cp].toString() : oldMap[cp],
                                new        : newMap[cp] instanceof RefdataValue ? newMap[cp].toString() : newMap[cp],
                                //propertyOID: "${this.class.name}:${this.id}"
                        ]
                    }
                    else {
                        log?.debug("ignored because no audit config")
                    }
                } // CustomProperty
                else {

                    def isSubOrLic = (this instanceof Subscription || this instanceof License)

                    if ( ! isSubOrLic || (isSubOrLic && AuditConfig.getConfig(this, cp)) ) {

                        if (clazz.equals("com.k_int.kbplus.RefdataValue")) {

                            def old_oid = oldMap[cp] ? "${oldMap[cp].class.name}:${oldMap[cp].id}" : null
                            def new_oid = newMap[cp] ? "${newMap[cp].class.name}:${newMap[cp].id}" : null

                            event = [
                                    OID     : "${this.class.name}:${this.id}",
                                    event   : "${this.class.simpleName}.updated",
                                    prop    : cp,
                                    old     : old_oid,
                                    oldLabel: oldMap[cp]?.toString(),
                                    new     : new_oid,
                                    newLabel: newMap[cp]?.toString()
                            ]
                        } else {

                            event = [
                                    OID  : "${this.class.name}:${this.id}",
                                    event: "${this.class.simpleName}.updated",
                                    prop : cp,
                                    old  : oldMap[cp],
                                    new  : newMap[cp]
                            ]
                        }
                    } // Subscription or License
                    else {
                        log?.debug("ignored because no audit config")
                    }
                }

                if (event) {
                    if (! changeNotificationService) {
                        log?.error("changeNotificationService not implemented @ ${it}")
                    } else {
                        changeNotificationService.fireEvent(event)
                    }
                }
            }
        }
    }

    @Transient
    def notifyDependencies(changeDocument) {
        log?.debug("notifyDependencies() not implemented => ${changeDocument}")
    }

    def getWatchedProperties() {
        def result = []

        if (this instanceof CustomProperty) {
            def cfg = getAuditConfig()
            result = cfg ? cfg.referenceField : result
        }
        else {
            this.controlledProperties.each { cp ->
                def cfg = getAuditConfig(cp)

                if (getAuditConfig(cp)) {
                    result << cp
                }
            }
        }

        result
    }

    @Transient
    def getAuditConfig() {
        AuditConfig.getConfig(this)
    }

    @Transient
    def getAuditConfig(String field) {
        AuditConfig.getConfig(this, field)
    }
}
