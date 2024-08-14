package de.laser.storage

import de.laser.properties.PropertyDefinition
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * Container class for frequently used properties
 */
@CompileStatic
@Slf4j
class PropertyStore {

    // -- License Properties

    public final static PropertyDefinition LIC_AUTHORIZED_USERS    = getPropertyDefinition('Authorized Users', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_ARCHIVAL_COPY_TIME      = getPropertyDefinition('Archival Copy: Time', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_CITATION_REQUIREMENT_DETAIL    = getPropertyDefinition('Citation requirement detail', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_LOCAL_AUTHORIZED_USER_DEFINITION = getPropertyDefinition('Local authorized user defintion', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_OA_FIRST_DATE = getPropertyDefinition('OA First Date', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_OA_LAST_DATE    = getPropertyDefinition('OA Last Date', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_OTHER_USE_RESTRICTION_NOTE    = getPropertyDefinition('Other Use Restriction Note', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_PERPETUAL_COVERAGE_FROM    = getPropertyDefinition('Perpetual coverage from', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_PERPETUAL_COVERAGE_TO    = getPropertyDefinition('Perpetual coverage to', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_REPOSITORY    = getPropertyDefinition('Repository', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_USAGE_STATISTICS_ADDRESSEE = getPropertyDefinition('Usage Statistics Addressee', PropertyDefinition.LIC_PROP)
    /*

    public final static PropertyDefinition PLA_IPV4    = getPropertyDefinition('IPv4: Supported', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition PLA_IPV6    = getPropertyDefinition('IPv6: Supported', PropertyDefinition.LIC_PROP)

    public final static PropertyDefinition PLA_NATSTAT_SID     = getPropertyDefinition('NatStat Supplier ID', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition PLA_PROXY           = getPropertyDefinition('Proxy: Supported', PropertyDefinition.LIC_PROP)

    public final static PropertyDefinition PLA_SHIBBOLETH      = getPropertyDefinition('Shibboleth: Supported', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition PLA_SHIBBOLETH_EID  = getPropertyDefinition('Shibboleth: SP entityID', PropertyDefinition.LIC_PROP)
    */

    // -- Subscription Properties

    public final static PropertyDefinition SUB_PROP_GASCO_DISPLAY_NAME      = getPropertyDefinition('GASCO display name', PropertyDefinition.SUB_PROP)
    public final static PropertyDefinition SUB_PROP_GASCO_ENTRY             = getPropertyDefinition('GASCO Entry', PropertyDefinition.SUB_PROP)
    public final static PropertyDefinition SUB_PROP_GASCO_INFORMATION_LINK  = getPropertyDefinition('GASCO information link', PropertyDefinition.SUB_PROP)
    public final static PropertyDefinition SUB_PROP_GASCO_NEGOTIATOR_NAME   = getPropertyDefinition('GASCO negotiator name', PropertyDefinition.SUB_PROP)

    // --

    /**
     * Preloads the given property definition by name and object type (description)
     * @param name the name of the property definition
     * @param descr the owner object type
     * @return the {@link PropertyDefinition} matching the given name and object type
     */
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

    /**
     * Preloads the given survey property definition by name and object type (description)
     * @param name the name of the property definition
     * @return the {@link PropertyDefinition} matching the given name and object type
     */
    static PropertyDefinition getSurveyProperty(String name) {
        PropertyDefinition result = PropertyDefinition.getByNameAndDescrAndTenant(name, PropertyDefinition.SVY_PROP, null)

        if (! result) {
            log.warn "No PropertyDefinition found for name:'${name}', descr:'${PropertyDefinition.SVY_PROP}'"
        }
        (PropertyDefinition) GrailsHibernateUtil.unwrapIfProxy(result)
    }
}
