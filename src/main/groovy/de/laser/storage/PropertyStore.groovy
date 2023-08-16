package de.laser.storage

import de.laser.properties.PropertyDefinition
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

@CompileStatic
@Slf4j
class PropertyStore {

    // -- Platform Properties

    /*
    public final static PropertyDefinition PLA_COUNTER_REGISTRY_URL    = getPropertyDefinition('COUNTER Registry URL', PropertyDefinition.PLA_PROP)

    public final static PropertyDefinition PLA_COUNTER_R3_REPORTS      = getPropertyDefinition('COUNTER R3: Reports supported', PropertyDefinition.PLA_PROP)
    public final static PropertyDefinition PLA_COUNTER_R4_REPORTS      = getPropertyDefinition('COUNTER R4: Reports supported', PropertyDefinition.PLA_PROP)
    public final static PropertyDefinition PLA_COUNTER_R5_REPORTS      = getPropertyDefinition('COUNTER R5: Reports supported', PropertyDefinition.PLA_PROP)
    public final static PropertyDefinition PLA_COUNTER_R4_STATS_URL    = getPropertyDefinition('COUNTER R4: Usage Statistics URL', PropertyDefinition.PLA_PROP)
    public final static PropertyDefinition PLA_COUNTER_R5_STATS_URL    = getPropertyDefinition('COUNTER R5: Usage Statistics URL', PropertyDefinition.PLA_PROP)
    public final static PropertyDefinition PLA_COUNTER_R4_SUSHI_API    = getPropertyDefinition('COUNTER R4: COUNTER_SUSHI API supported', PropertyDefinition.PLA_PROP)
    public final static PropertyDefinition PLA_COUNTER_R5_SUSHI_API    = getPropertyDefinition('COUNTER R5: COUNTER_SUSHI API supported', PropertyDefinition.PLA_PROP)
    public final static PropertyDefinition PLA_COUNTER_R4_SUSHI_URL    = getPropertyDefinition('COUNTER R4: SUSHI Server URL', PropertyDefinition.PLA_PROP)
    public final static PropertyDefinition PLA_COUNTER_R5_SUSHI_URL    = getPropertyDefinition('COUNTER R5: SUSHI Server URL', PropertyDefinition.PLA_PROP)

    public final static PropertyDefinition PLA_IPV4    = getPropertyDefinition('IPv4: Supported', PropertyDefinition.PLA_PROP)
    public final static PropertyDefinition PLA_IPV6    = getPropertyDefinition('IPv6: Supported', PropertyDefinition.PLA_PROP)
    */
    public final static PropertyDefinition PLA_NATSTAT_SID     = getPropertyDefinition('NatStat Supplier ID', PropertyDefinition.PLA_PROP)
    /*
    public final static PropertyDefinition PLA_PROXY           = getPropertyDefinition('Proxy: Supported', PropertyDefinition.PLA_PROP)

    public final static PropertyDefinition PLA_SHIBBOLETH      = getPropertyDefinition('Shibboleth: Supported', PropertyDefinition.PLA_PROP)
    public final static PropertyDefinition PLA_SHIBBOLETH_EID  = getPropertyDefinition('Shibboleth: SP entityID', PropertyDefinition.PLA_PROP)
    */

    // -- Subscription Properties

    public final static PropertyDefinition SUB_PROP_GASCO_DISPLAY_NAME      = getPropertyDefinition('GASCO display name', PropertyDefinition.SUB_PROP)
    public final static PropertyDefinition SUB_PROP_GASCO_ENTRY             = getPropertyDefinition('GASCO Entry', PropertyDefinition.SUB_PROP)
    public final static PropertyDefinition SUB_PROP_GASCO_INFORMATION_LINK  = getPropertyDefinition('GASCO information link', PropertyDefinition.SUB_PROP)
    public final static PropertyDefinition SUB_PROP_GASCO_NEGOTIATOR_NAME   = getPropertyDefinition('GASCO negotiator name', PropertyDefinition.SUB_PROP)

    // --

    static PropertyDefinition getPropertyDefinition(String name, String descr) {
        PropertyDefinition result = PropertyDefinition.getByNameAndDescr(name, descr)

        if (! result) {
            log.warn "No PropertyDefinition found for name:'${name}', descr:'${descr}'"
        }
        (PropertyDefinition) GrailsHibernateUtil.unwrapIfProxy( result)
    }

    // -- Survey Properties

    public final static PropertyDefinition SURVEY_PROPERTY_PARTICIPATION   = getSurveyProperty('Participation')
    public final static PropertyDefinition SURVEY_PROPERTY_ORDER_NUMBER    = getSurveyProperty('Order number')
    public final static PropertyDefinition SURVEY_PROPERTY_MULTI_YEAR_5    = getSurveyProperty('Multi-year term 5 years')
    public final static PropertyDefinition SURVEY_PROPERTY_MULTI_YEAR_4    = getSurveyProperty('Multi-year term 4 years')
    public final static PropertyDefinition SURVEY_PROPERTY_MULTI_YEAR_3    = getSurveyProperty('Multi-year term 3 years')
    public final static PropertyDefinition SURVEY_PROPERTY_MULTI_YEAR_2    = getSurveyProperty('Multi-year term 2 years')
    public final static PropertyDefinition SURVEY_PROPERTY_SUBSCRIPTION_FORM    = getSurveyProperty('Subscription Form')
    public final static PropertyDefinition SURVEY_PROPERTY_PUBLISHING_COMPONENT    = getSurveyProperty('Publishing Component')


    static PropertyDefinition getSurveyProperty(String name) {
        PropertyDefinition result = PropertyDefinition.getByNameAndDescrAndTenant(name, PropertyDefinition.SVY_PROP, null)

        if (! result) {
            log.warn "No PropertyDefinition found for name:'${name}', descr:'${PropertyDefinition.SVY_PROP}'"
        }
        (PropertyDefinition) GrailsHibernateUtil.unwrapIfProxy(result)
    }
}
