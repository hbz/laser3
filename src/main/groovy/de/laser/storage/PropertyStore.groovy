package de.laser.storage

import de.laser.properties.PropertyDefinition
import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Slf4j
class PropertyStore {

    public static final PLA_COUNTER_REGISTRY_URL    = getPropertyDefinition('COUNTER Registry URL', PropertyDefinition.PLA_PROP)

    public static final PLA_COUNTER_R3_REPORTS      = getPropertyDefinition('COUNTER R3: Reports supported', PropertyDefinition.PLA_PROP)
    public static final PLA_COUNTER_R4_REPORTS      = getPropertyDefinition('COUNTER R4: Reports supported', PropertyDefinition.PLA_PROP)
    public static final PLA_COUNTER_R5_REPORTS      = getPropertyDefinition('COUNTER R5: Reports supported', PropertyDefinition.PLA_PROP)
    public static final PLA_COUNTER_R4_STATS_URL    = getPropertyDefinition('COUNTER R4: Usage Statistics URL', PropertyDefinition.PLA_PROP)
    public static final PLA_COUNTER_R5_STATS_URL    = getPropertyDefinition('COUNTER R5: Usage Statistics URL', PropertyDefinition.PLA_PROP)
    public static final PLA_COUNTER_R4_SUSHI_API    = getPropertyDefinition('COUNTER R4: COUNTER_SUSHI API supported', PropertyDefinition.PLA_PROP)
    public static final PLA_COUNTER_R5_SUSHI_API    = getPropertyDefinition('COUNTER R5: COUNTER_SUSHI API supported', PropertyDefinition.PLA_PROP)
    public static final PLA_COUNTER_R4_SUSHI_URL    = getPropertyDefinition('COUNTER R4: SUSHI Server URL', PropertyDefinition.PLA_PROP)
    public static final PLA_COUNTER_R5_SUSHI_URL    = getPropertyDefinition('COUNTER R5: SUSHI Server URL', PropertyDefinition.PLA_PROP)

    public static final PLA_IPV4    = getPropertyDefinition('IPv4: Supported', PropertyDefinition.PLA_PROP)
    public static final PLA_IPV6    = getPropertyDefinition('IPv6: Supported', PropertyDefinition.PLA_PROP)

    public static final PLA_NATSTAT_SID     = getPropertyDefinition('NatStat Supplier ID', PropertyDefinition.PLA_PROP)
    public static final PLA_PROXY           = getPropertyDefinition('Proxy: Supported', PropertyDefinition.PLA_PROP)

    public static final PLA_SHIBBOLETH      = getPropertyDefinition('Shibboleth: Supported', PropertyDefinition.PLA_PROP)
    public static final PLA_SHIBBOLETH_EID  = getPropertyDefinition('Shibboleth: SP entityID', PropertyDefinition.PLA_PROP)

    static PropertyDefinition getPropertyDefinition(String name, String descr) {
        PropertyDefinition result = PropertyDefinition.getByNameAndDescr(name, descr)

        if (! result) {
            log.warn "No PropertyDefinition found for name:'${name}', descr:'${descr}'"
        }
        (PropertyDefinition) GrailsHibernateUtil.unwrapIfProxy( result)
    }
}
