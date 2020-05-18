package de.laser.helper

import com.k_int.kbplus.RefdataValue
import com.k_int.properties.PropertyDefinition
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

//@CompileStatic
class RDStore {

    static final GENERIC_NULL_VALUE         = getRefdataValue('generic.null.value','filter.fake.values')

    static final OR_LICENSING_CONSORTIUM    = getRefdataValue('Licensing Consortium', RDConstants.ORGANISATIONAL_ROLE)
    static final OR_LICENSEE                = getRefdataValue('Licensee', RDConstants.ORGANISATIONAL_ROLE)
    static final OR_LICENSEE_CONS           = getRefdataValue('Licensee_Consortial', RDConstants.ORGANISATIONAL_ROLE)
    static final OR_LICENSEE_COLL           = getRefdataValue('Licensee_Collective', RDConstants.ORGANISATIONAL_ROLE)

    static final OR_SUBSCRIPTION_CONSORTIA  = getRefdataValue('Subscription Consortia', RDConstants.ORGANISATIONAL_ROLE)
    static final OR_SUBSCRIBER              = getRefdataValue('Subscriber', RDConstants.ORGANISATIONAL_ROLE)
    static final OR_SUBSCRIBER_CONS         = getRefdataValue('Subscriber_Consortial', RDConstants.ORGANISATIONAL_ROLE)
    static final OR_SUBSCRIBER_CONS_HIDDEN  = getRefdataValue('Subscriber_Consortial_Hidden', RDConstants.ORGANISATIONAL_ROLE)
    static final OR_SUBSCRIPTION_COLLECTIVE = getRefdataValue('Subscription Collective', RDConstants.ORGANISATIONAL_ROLE)
    static final OR_SUBSCRIBER_COLLECTIVE   = getRefdataValue('Subscriber_Collective', RDConstants.ORGANISATIONAL_ROLE)

    static final OR_AGENCY                  = getRefdataValue('Agency', RDConstants.ORGANISATIONAL_ROLE)
    static final OR_LICENSOR                = getRefdataValue('Licensor', RDConstants.ORGANISATIONAL_ROLE)
    static final OR_PROVIDER                = getRefdataValue('Provider', RDConstants.ORGANISATIONAL_ROLE)
    static final OR_PUBLISHER               = getRefdataValue('Publisher', RDConstants.ORGANISATIONAL_ROLE)
    static final OR_CONTENT_PROVIDER        = getRefdataValue('Content Provider', RDConstants.ORGANISATIONAL_ROLE)
    static final OR_PACKAGE_CONSORTIA       = getRefdataValue('Package Consortia', RDConstants.ORGANISATIONAL_ROLE)

    static final OT_CONSORTIUM              = getRefdataValue('Consortium', RDConstants.ORG_TYPE)
    static final OT_INSTITUTION             = getRefdataValue('Institution', RDConstants.ORG_TYPE)
    static final OT_AGENCY                  = getRefdataValue('Agency', RDConstants.ORG_TYPE)
    static final OT_LICENSOR                = getRefdataValue('Licensor', RDConstants.ORG_TYPE)
    static final OT_PROVIDER                = getRefdataValue('Provider', RDConstants.ORG_TYPE)
    static final OT_DEPARTMENT              = getRefdataValue('Department', RDConstants.ORG_TYPE)

    static final O_SECTOR_HIGHER_EDU        = getRefdataValue('Higher Education', RDConstants.ORG_SECTOR)
    static final O_SECTOR_PUBLISHER         = getRefdataValue('Publisher', RDConstants.ORG_SECTOR)

    static final O_STATUS_CURRENT           = getRefdataValue('Current', RDConstants.ORG_STATUS)
    static final O_STATUS_DELETED           = getRefdataValue('Deleted', RDConstants.ORG_STATUS)

    static final CCT_EMAIL          = getRefdataValue('E-Mail', RDConstants.CONTACT_CONTENT_TYPE)
    static final CCT_PHONE          = getRefdataValue('Phone', RDConstants.CONTACT_CONTENT_TYPE)
    static final CCT_URL            = getRefdataValue('Url', RDConstants.CONTACT_CONTENT_TYPE)

    static final CIEC_POSITIVE          = getRefdataValue('positive', RDConstants.COST_CONFIGURATION)
    static final CIEC_NEGATIVE          = getRefdataValue('negative', RDConstants.COST_CONFIGURATION)
    static final CIEC_NEUTRAL           = getRefdataValue('neutral', RDConstants.COST_CONFIGURATION)

    static final COMBO_TYPE_CONSORTIUM      = getRefdataValue('Consortium', RDConstants.COMBO_TYPE)
    static final COMBO_TYPE_DEPARTMENT      = getRefdataValue('Department', RDConstants.COMBO_TYPE)

    static final COMBO_STATUS_ACTIVE        = getRefdataValue('Active', RDConstants.COMBO_STATUS)
    static final COMBO_STATUS_INACTIVE      = getRefdataValue('Inactive', RDConstants.COMBO_STATUS)

    static final CONTACT_TYPE_PERSONAL      = getRefdataValue('Personal Contact', RDConstants.PERSON_CONTACT_TYPE)
    static final CONTACT_TYPE_FUNCTIONAL    = getRefdataValue('Functional Contact', RDConstants.PERSON_CONTACT_TYPE)

    static final COUNTRY_DE                 = getRefdataValue('DE', RDConstants.COUNTRY)
    static final COUNTRY_AT                 = getRefdataValue('AT', RDConstants.COUNTRY)
    static final COUNTRY_CH                 = getRefdataValue('CH', RDConstants.COUNTRY)

    static final COST_ITEM_ACTUAL           = getRefdataValue('Actual', RDConstants.COST_ITEM_STATUS)
    static final COST_ITEM_DELETED          = getRefdataValue('Deleted', RDConstants.COST_ITEM_STATUS)
    static final COST_ITEM_ELEMENT_CONSORTIAL_PRICE          = getRefdataValue('price: consortial price', RDConstants.COST_ITEM_ELEMENT)

    static final CURRENCY_EUR               = getRefdataValue('EUR', RDConstants.CURRENCY)

    static final DOC_CTX_STATUS_DELETED     = getRefdataValue('Deleted', RDConstants.DOCUMENT_CONTEXT_STATUS)
    static final DOC_TYPE_ANNOUNCEMENT      = getRefdataValue('Announcement', RDConstants.DOCUMENT_TYPE)

    static final IE_ACCESS_CURRENT          = getRefdataValue('Current', RDConstants.IE_ACCESS_STATUS)

    static final IE_ACCEPT_STATUS_FIXED                 = getRefdataValue('Fixed', RDConstants.IE_ACCEPT_STATUS)
    static final IE_ACCEPT_STATUS_UNDER_NEGOTIATION     = getRefdataValue('Under Negotiation', RDConstants.IE_ACCEPT_STATUS)
    static final IE_ACCEPT_STATUS_UNDER_CONSIDERATION   = getRefdataValue('Under Consideration', RDConstants.IE_ACCEPT_STATUS)

    static final LICENSE_TYPE_ACTUAL        = getRefdataValue('Actual', RDConstants.LICENSE_TYPE)

    static final LINKTYPE_FOLLOWS           = getRefdataValue('follows', RDConstants.LINK_TYPE)
    static final LINKTYPE_LICENSE           = getRefdataValue('license', RDConstants.LINK_TYPE)

    static final ORG_STATUS_DELETED         = getRefdataValue('Deleted', RDConstants.ORG_STATUS)

    static final PACKAGE_STATUS_DELETED         = getRefdataValue('Deleted', RDConstants.PACKAGE_STATUS)
    static final PLATFORM_STATUS_DELETED        = getRefdataValue('Deleted', RDConstants.PLATFORM_STATUS)

    static final PENDING_CHANGE_PENDING = getRefdataValue('Pending', RDConstants.PENDING_CHANGE_STATUS)
    static final PENDING_CHANGE_ACCEPTED = getRefdataValue('Accepted', RDConstants.PENDING_CHANGE_STATUS)
    static final PENDING_CHANGE_SUPERSEDED = getRefdataValue('Superseded', RDConstants.PENDING_CHANGE_STATUS)
    static final PENDING_CHANGE_REJECTED = getRefdataValue('Rejected', RDConstants.PENDING_CHANGE_STATUS)

    static final PENDING_CHANGE_CONFIG_ACCEPT = getRefdataValue('Accept', RDConstants.PENDING_CHANGE_CONFIG_SETTING)
    static final PENDING_CHANGE_CONFIG_PROMPT = getRefdataValue('Prompt', RDConstants.PENDING_CHANGE_CONFIG_SETTING)
    static final PENDING_CHANGE_CONFIG_REJECT = getRefdataValue('Reject', RDConstants.PENDING_CHANGE_CONFIG_SETTING)

    static final PERM_PERM_EXPL             = getRefdataValue('Permitted (explicit)', RDConstants.PERMISSIONS)
    static final PERM_PERM_INTERP           = getRefdataValue('Permitted (interpreted)',RDConstants.PERMISSIONS)
    static final PERM_PROH_EXPL             = getRefdataValue('Prohibited (explicit)', RDConstants.PERMISSIONS)
    static final PERM_PROH_INTERP           = getRefdataValue('Prohibited (interpreted)', RDConstants.PERMISSIONS)
    static final PERM_SILENT                = getRefdataValue('Silent', RDConstants.PERMISSIONS)
    static final PERM_NOT_APPLICABLE        = getRefdataValue('Not applicable', RDConstants.PERMISSIONS)
    static final PERM_UNKNOWN               = getRefdataValue('Unknown', RDConstants.PERMISSIONS)

    static final PRS_FUNC_GENERAL_CONTACT_PRS   = getRefdataValue('General contact person', RDConstants.PERSON_FUNCTION)
    static final PRS_FUNC_GASCO_CONTACT         = getRefdataValue('GASCO-Contact', RDConstants.PERSON_FUNCTION)

    static final PRS_RESP_SPEC_SUB_EDITOR       = getRefdataValue('Specific subscription editor', RDConstants.PERSON_RESPONSIBILITY)
    static final PRS_RESP_SPEC_LIC_EDITOR       = getRefdataValue('Specific license editor', RDConstants.PERSON_RESPONSIBILITY)

    static final SHARE_CONF_ALL                 = getRefdataValue('everyone', RDConstants.SHARE_CONFIGURATION)
    static final SHARE_CONF_CREATOR             = getRefdataValue('only for creator', RDConstants.SHARE_CONFIGURATION)
    static final SHARE_CONF_UPLOADER_ORG        = getRefdataValue('only for author organisation', RDConstants.SHARE_CONFIGURATION) //maps to key, value is correct!
    static final SHARE_CONF_UPLOADER_AND_TARGET = getRefdataValue('only for author and target organisation', RDConstants.SHARE_CONFIGURATION) //maps to key, value is correct!
    static final SHARE_CONF_CONSORTIUM          = getRefdataValue('only for consortia members', RDConstants.SHARE_CONFIGURATION)

//DO NOT USE THIS STATUS. Subs have no longer a deleted flag. They ARE deleted!
    @Deprecated
    static final SUBSCRIPTION_DELETED       = getRefdataValue('Deleted', RDConstants.SUBSCRIPTION_STATUS)
    static final SUBSCRIPTION_CURRENT       = getRefdataValue('Current', RDConstants.SUBSCRIPTION_STATUS)
    static final SUBSCRIPTION_INTENDED      = getRefdataValue('Intended', RDConstants.SUBSCRIPTION_STATUS)
    static final SUBSCRIPTION_EXPIRED       = getRefdataValue('Expired', RDConstants.SUBSCRIPTION_STATUS)
    static final SUBSCRIPTION_NO_STATUS     = getRefdataValue('Status not defined', RDConstants.SUBSCRIPTION_STATUS)
    static final SUBSCRIPTION_UNDER_PROCESS_OF_SELECTION     = getRefdataValue('Under Process Of Selection', RDConstants.SUBSCRIPTION_STATUS)

    static final SURVEY_READY                   = getRefdataValue('Ready', RDConstants.SURVEY_STATUS)
    static final SURVEY_IN_PROCESSING           = getRefdataValue('In Processing', RDConstants.SURVEY_STATUS)
    static final SURVEY_IN_EVALUATION           = getRefdataValue('In Evaluation', RDConstants.SURVEY_STATUS)
    static final SURVEY_COMPLETED               = getRefdataValue('Completed', RDConstants.SURVEY_STATUS)
    static final SURVEY_SURVEY_STARTED          = getRefdataValue('Survey started', RDConstants.SURVEY_STATUS)
    static final SURVEY_SURVEY_COMPLETED        = getRefdataValue('Survey completed', RDConstants.SURVEY_STATUS)

    static final SURVEY_TYPE_RENEWAL                = getRefdataValue('renewal', RDConstants.SURVEY_TYPE)
    static final SURVEY_TYPE_INTEREST               = getRefdataValue('interest', RDConstants.SURVEY_TYPE)
    static final SURVEY_TYPE_TITLE_SELECTION        = getRefdataValue('selection', RDConstants.SURVEY_TYPE)
    static final SURVEY_TYPE_SUBSCRIPTION        = getRefdataValue('subscription survey', RDConstants.SURVEY_TYPE)

    static final SUBSCRIPTION_TYPE_LOCAL            = getRefdataValue('Local Licence', RDConstants.SUBSCRIPTION_TYPE)
    static final SUBSCRIPTION_TYPE_CONSORTIAL       = getRefdataValue('Consortial Licence', RDConstants.SUBSCRIPTION_TYPE)
    static final SUBSCRIPTION_TYPE_ADMINISTRATIVE   = getRefdataValue('Administrative Subscription', RDConstants.SUBSCRIPTION_TYPE)

    //static final SUBSCRIPTION_TYPE_COLLECTIVE   	= getRefdataValue('Collective Subscription', RDConstants.SUBSCRIPTION_TYPE)

    static final SUBSCRIPTION_KIND_CONSORTIAL       = getRefdataValue('Consortial Licence', RDConstants.SUBSCRIPTION_KIND)
    static final SUBSCRIPTION_KIND_ALLIANCE   		= getRefdataValue('Alliance Licence', RDConstants.SUBSCRIPTION_KIND)
    static final SUBSCRIPTION_KIND_NATIONAL   		= getRefdataValue('National Licence', RDConstants.SUBSCRIPTION_KIND)
    static final SUBSCRIPTION_KIND_LOCAL            = getRefdataValue('Local Licence', RDConstants.SUBSCRIPTION_KIND)

    static final TASK_STATUS_OPEN       = getRefdataValue('Open', RDConstants.TASK_STATUS)
    static final TASK_STATUS_DONE       = getRefdataValue('Done', RDConstants.TASK_STATUS)

    static final TAX_REVERSE_CHARGE         = getRefdataValue('reverse charge', RDConstants.TAX_TYPE)

    static final TITLE_TYPE_EBOOK           = getRefdataValue('Book', RDConstants.TITLE_MEDIUM)
    static final TITLE_TYPE_JOURNAL         = getRefdataValue('Journal', RDConstants.TITLE_MEDIUM)
    static final TITLE_TYPE_DATABASE        = getRefdataValue('Database', RDConstants.TITLE_MEDIUM)

    static final TITLE_STATUS_CURRENT       = getRefdataValue('Current', RDConstants.TITLE_STATUS)
    static final TITLE_STATUS_RETIRED       = getRefdataValue('Retired', RDConstants.TITLE_STATUS)
    static final TITLE_STATUS_DELETED       = getRefdataValue('Deleted', RDConstants.TITLE_STATUS)

    static final TIPP_PAYMENT_COMPLIMENTARY     = getRefdataValue('Complimentary', RDConstants.TIPP_PAYMENT_TYPE)
    static final TIPP_PAYMENT_LIMITED_PROMOTION = getRefdataValue('Limited Promotion', RDConstants.TIPP_PAYMENT_TYPE)
    static final TIPP_PAYMENT_PAID              = getRefdataValue('Paid', RDConstants.TIPP_PAYMENT_TYPE)
    static final TIPP_PAYMENT_OA                = getRefdataValue('OA', RDConstants.TIPP_PAYMENT_TYPE)
    static final TIPP_PAYMENT_OPT_OUT_PROMOTION = getRefdataValue('Opt Out Promotion', RDConstants.TIPP_PAYMENT_TYPE)
    static final TIPP_PAYMENT_UNCHARGED         = getRefdataValue('Uncharged', RDConstants.TIPP_PAYMENT_TYPE)
    static final TIPP_PAYMENT_UNKNOWN           = getRefdataValue('Unknown', RDConstants.TIPP_PAYMENT_TYPE)

    static final TIPP_STATUS_CURRENT            = getRefdataValue('Current', RDConstants.TIPP_STATUS)
    static final TIPP_STATUS_RETIRED            = getRefdataValue('Retired', RDConstants.TIPP_STATUS)
    static final TIPP_STATUS_EXPECTED           = getRefdataValue('Expected', RDConstants.TIPP_STATUS)
    static final TIPP_STATUS_TRANSFERRED        = getRefdataValue('Transferred', RDConstants.TIPP_STATUS)
    static final TIPP_STATUS_UNKNOWN            = getRefdataValue('Unknown', RDConstants.TIPP_STATUS)
    static final TIPP_STATUS_DELETED            = getRefdataValue('Deleted',  RDConstants.TIPP_STATUS)

    static final PRS_FUNC_RESPONSIBLE_ADMIN     = getRefdataValue('Responsible Admin', RDConstants.PERSON_FUNCTION)
    static final PRS_FUNC_FUNC_BILLING_ADDRESS  = getRefdataValue('Functional Contact Billing Adress', RDConstants.PERSON_FUNCTION)
    static final PRS_FUNC_TECHNICAL_SUPPORT     = getRefdataValue('Technichal Support', RDConstants.PERSON_FUNCTION)

    static final YN_YES         = getRefdataValue('Yes', RDConstants.Y_N)
    static final YN_NO          = getRefdataValue('No', RDConstants.Y_N)
    static final YNO_YES        = getRefdataValue('Yes', RDConstants.Y_N_O)
    static final YNO_NO         = getRefdataValue('No', RDConstants.Y_N_O)
    static final YNO_OTHER      = getRefdataValue('Other', RDConstants.Y_N_O)

    static final LANGUAGE_DE      = getRefdataValue('de', RDConstants.LANGUAGE)

    //Properties

    static final SURVEY_PROPERTY_PARTICIPATION = getSurveyProperty('Participation')
    static final SURVEY_PROPERTY_MULTI_YEAR_3 = getSurveyProperty('Multi-year term 3 years')
    static final SURVEY_PROPERTY_MULTI_YEAR_2 = getSurveyProperty('Multi-year term 2 years')


    static RefdataValue getRefdataValue(String value, String category) {
        RefdataValue result = RefdataValue.getByValueAndCategory(value, category)

        if (! result) {
            println "WARNING: No RefdataValue found by RDStore for value:'${value}', category:'${category}'"
        }
        (RefdataValue) GrailsHibernateUtil.unwrapIfProxy( result)
    }

    static PropertyDefinition getSurveyProperty(String name) {
        PropertyDefinition result = PropertyDefinition.getByNameAndDescrAndTenant(name, PropertyDefinition.SUR_PROP, null)


        if (! result) {
            println "WARNING: No PropertyDefinition found by RDStore for name:'${name}', descr:'${PropertyDefinition.SUR_PROP}'"
        }

        (PropertyDefinition) GrailsHibernateUtil.unwrapIfProxy(result)
    }
}
