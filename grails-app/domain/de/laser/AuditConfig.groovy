package de.laser

import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * This domain class keeps track of the inherited properties. Consortial objects may inherit some of their properties to the member objects, those are:
 * <ul>
 *     <li>{@link Subscription}</li>
 *     <li>{@link License}</li>
 * </ul>
 * The object reference is being stored in {@link #referenceClass} and {@link #referenceId}. The property which should be inherited is stored in {@link #referenceField}. When the reference field in the reference object is being
 * changed, the beforeUpdate() handler triggers the propagation to the member objects (i.e. those objects whose instanceOf is pointing to the triggering object). Subscription/license identifiers or properties or pending change
 * configurations may be inherited as whole; in such case, the reference field is the COMPLETE_OBJECT (stored as a constant as whole) and any property triggers propagation
 */
@Slf4j
class AuditConfig {

    public static final String COMPLETE_OBJECT = 'COMPLETE_OBJECT'

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

    /**
     * Activates the inheritance for the given object. Applies to
     * <ul>
     *     <li>{@link de.laser.properties.SubscriptionProperty}</li>
     *     <li>{@link de.laser.properties.LicenseProperty}</li>
     *     <li>{@link de.laser.Identifier}</li>
     * </ul>
     * @param obj the object which should propagate its values
     */
    static addConfig(Object obj) {
        addConfig(obj, AuditConfig.COMPLETE_OBJECT)
    }

    /**
     * Gets the current audit config for the given object
     * @param obj the object whose audit config should be retrieved
     */
    static AuditConfig getConfig(Object obj) {
        getConfig(obj, AuditConfig.COMPLETE_OBJECT)
    }

    /**
     * Disables auditing for the given object; deactivates the propagation trigger
     * @param obj the object whose audit config should be removed
     */
    static void removeConfig(Object obj) {
        removeConfig(obj, AuditConfig.COMPLETE_OBJECT)
    }

    /**
     * Activates the inheritance for a given field of the given object. Any change on the reference object's field is now passed onto the members who are instance of the triggering object
     * @param obj the object whose inheritance should be activated
     * @param field the field (or COMPLETE_OBJECT if the object as whole is being tracked) which should be inherited to child objects
     */
    static void addConfig(Object obj, String field) {
        AuditConfig.withTransaction {
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

    /**
     * Gets the current audit config to a given field of the given object
     * @param obj the object whose configuration should be checked
     * @param field the field which should be checked
     * @return the audit config if exists, null otherwise
     */
    static AuditConfig getConfig(Object obj, String field) {
        if (! obj)
            return null
        obj = GrailsHibernateUtil.unwrapIfProxy(obj)
        AuditConfig ret = AuditConfig.findByReferenceIdAndReferenceClassAndReferenceField(
                obj.getId(),
                obj.getClass().name,
                field
        )
        ret
    }

    /**
     * Lists all inherited settings of the given object
     * @param obj the object whose fields should be retrieved
     * @return a {@link List} of current audit configs
     */
    static List<AuditConfig> getConfigs(Object obj) {
        if (! obj)
            return null

        List<AuditConfig> configs = []

        obj.getLogIncluded().each{ prop ->
            AuditConfig config = getConfig(obj, prop)
            if(config){
                configs << config
            }
        }
        return configs
    }

    /**
     * Disables the inheritance for the given object's given field
     * @param obj the object from which the inheritance should be disabled
     * @param field the field to be deactivated
     */
    static void removeConfig(Object obj, String field) {
        AuditConfig.withTransaction {
            if (obj) {
                AuditConfig.findAllWhere(
                        referenceId: obj.getId(),
                        referenceClass: obj.getClass().name,
                        referenceField: field
                ).each { it.delete() }
            }
        }
    }

    /**
     * Deactivates every inherited fields from the given object
     * @param obj the object which should lose all settings
     */
    static void removeAllConfigs(Object obj) {
        AuditConfig.withTransaction {
            if (obj) {
                AuditConfig.executeUpdate(
                    'delete AuditConfig ac where ac.referenceId = :id and ac.referenceClass = :cls',
                    [ id: obj.getId(), cls: obj.getClass().name ]
                )
            }
        }
    }
}
