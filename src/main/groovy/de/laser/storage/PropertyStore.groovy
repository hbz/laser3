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

    public final static PropertyDefinition LIC_ACCESSIBILITY_COMPLIANCE = getPropertyDefinition('Accessibility compliance', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_ALUMNI_ACCESS    = getPropertyDefinition('Alumni Access', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_AUTHORIZED_USERS    = getPropertyDefinition('Authorized Users', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_ARCHIVAL_COPY_CONTENT      = getPropertyDefinition('Archival Copy Content', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_ARCHIVAL_COPY_COST      = getPropertyDefinition('Archival Copy: Cost', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_ARCHIVAL_COPY_PERMISSION      = getPropertyDefinition('Archival Copy: Permission', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_ARCHIVAL_COPY_TIME      = getPropertyDefinition('Archival Copy: Time', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_CHANGE_TO_LICENSED_MATERIAL    = getPropertyDefinition('Change to licensed material', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_CITATION_REQUIREMENT_DETAIL    = getPropertyDefinition('Citation requirement detail', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_COMPLETENESS_OF_CONTENT_CLAUSE    = getPropertyDefinition('Completeness of content clause', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_CONCURRENT_USERS    = getPropertyDefinition('Concurrent Users', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_CONCURRENCY_WITH_PRINT_VERSION    = getPropertyDefinition('Concurrency with print version', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_CONFORMITY_WITH_URHG    = getPropertyDefinition('Conformity with UrhG', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_CONTENT_WARRANTY    = getPropertyDefinition('Content warranty', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_CONT_ACCESS_PAYMENT_NOTE    = getPropertyDefinition('Continuing Access: Payment Note', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_CONT_ACCESS_RESTRICTIONS    = getPropertyDefinition('Continuing Access: Restrictions', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_CONT_ACCESS_TITLE_TRANSFER    = getPropertyDefinition('Continuing Access: Title Transfer', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_COURSE_PACK_ELECTRONIC    = getPropertyDefinition('Course pack electronic', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_COURSE_PACK_PRINT    = getPropertyDefinition('Course pack print', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_COURSE_PACK_TERM_NOTE    = getPropertyDefinition('Course pack term note', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_COURSE_RESERVE_ELECTRONIC    = getPropertyDefinition('Course reserve electronic/cached', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_COURSE_RESERVE_PRINT    = getPropertyDefinition('Course reserve print', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_COURSE_RESERVE_TERM_NOTE    = getPropertyDefinition('Course reserve term note', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_DIGITAL_COPY    = getPropertyDefinition('Digitial copy', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_DIGITAL_COPY_TERM_NOTE    = getPropertyDefinition('Digitial copy term note', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_DISTANCE_EDUCATION    = getPropertyDefinition('Distance Education', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_DOCUMENT_DELIVERY_SERVICE    = getPropertyDefinition('Document delivery service (commercial)', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_ELECTRONIC_LINK  = getPropertyDefinition('Electronic link', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_ELECTRONIC_LINK_TERM_NOTE  = getPropertyDefinition('Electronic link term note', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_FAIR_USE_CLAUSE_INDICATOR     = getPropertyDefinition('Fair use clause indicator', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_ILL_ELECTRONIC   = getPropertyDefinition('ILL electronic', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_ILL_PRINT_OR_FAX = getPropertyDefinition('ILL print or fax', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_ILL_RECORD_KEEPING_REQUIRED  = getPropertyDefinition('ILL record keeping required', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_ILL_SECURE_ELECTRONIC_TRANSMISSION   = getPropertyDefinition('ILL secure electronic transmission', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_ILL_TERM_NOTE    = getPropertyDefinition('ILL term note', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_LOCAL_AUTHORIZED_USER_DEFINITION = getPropertyDefinition('Local authorized user defintion', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_MAINTENANCE_WINDOW  = getPropertyDefinition('Maintenance window', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_METADATA_DELIVERY  = getPropertyDefinition('Metadata delivery', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_METADATA_RELATED_CONTRACTUAL_TERMS  = getPropertyDefinition('Metadata-related contractual terms', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_METHOD_OF_AUTHENTICATION  = getPropertyDefinition('Method of Authentication', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_OA_FIRST_DATE = getPropertyDefinition('OA First Date', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_OA_LAST_DATE    = getPropertyDefinition('OA Last Date', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_OA_NOTE    = getPropertyDefinition('OA Note', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_OPEN_ACCESS    = getPropertyDefinition('Open Access', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_OTHER_USE_RESTRICTION_NOTE    = getPropertyDefinition('Other Use Restriction Note', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_PARTNERS_ACCESS    = getPropertyDefinition('Partners Access', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_PERFORMANCE_WARRANTY    = getPropertyDefinition('Performance warranty', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_PERPETUAL_COVERAGE_FROM    = getPropertyDefinition('Perpetual coverage from', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_PERPETUAL_COVERAGE_NOTE    = getPropertyDefinition('Perpetual coverage note', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_PERPETUAL_COVERAGE_TO    = getPropertyDefinition('Perpetual coverage to', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_POST_CANCELLATION_ONLINE_ACCESS    = getPropertyDefinition('Post Cancellation Online Access', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_PRINT_COPY   = getPropertyDefinition('Print copy', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_PRINT_COPY_TERM_NOTE     = getPropertyDefinition('Print copy term note', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_REPOSITORY    = getPropertyDefinition('Repository', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_REMOTE_ACCESS    = getPropertyDefinition('Remote access', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_SCHOLARLY_SHARING    = getPropertyDefinition('Scholarly sharing', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_SCHOLARLY_SHARING_TERM_NOTE    = getPropertyDefinition('Scholarly sharing term note', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_SINGLE_USER_ACCESS    = getPropertyDefinition('SingleUserAccess', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_TDM  = getPropertyDefinition('Text- and Datamining', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_TDM_CHAR_COUNT  = getPropertyDefinition('Text- and Datamining Character Count', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_TDM_RESTRICTIONS  = getPropertyDefinition('Text- and Datamining Restrictions', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_UPTIME_GUARANTEE = getPropertyDefinition('Uptime guarantee', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_USAGE_STATISTICS_ADDRESSEE = getPropertyDefinition('Usage Statistics Addressee', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_USAGE_STATISTICS_AVAILABILITY_INDICATOR = getPropertyDefinition('Usage Statistics Availability Indicator', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_WALK_IN_ACCESS = getPropertyDefinition('Walk-In Access', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_WALK_IN_USER_TERM_NOTE = getPropertyDefinition('Walk-In User Term Note', PropertyDefinition.LIC_PROP)
    public final static PropertyDefinition LIC_WIFI_ACCESS = getPropertyDefinition('Wifi Access', PropertyDefinition.LIC_PROP)
/*
    public final static PropertyDefinition PLA_PROXY           = getPropertyDefinition('Proxy: Supported', PropertyDefinition.PLA_PROP)
    public final static PropertyDefinition PLA_SHIBBOLETH      = getPropertyDefinition('Shibboleth: Supported', PropertyDefinition.PLA_PROP)
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
