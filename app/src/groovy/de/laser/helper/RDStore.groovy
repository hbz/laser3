package de.laser.helper


import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.SurveyProperty
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

    static final DOC_TYPE_ANNOUNCEMENT      = getRefdataValue('Announcement', RDConstants.DOCUMENT_TYPE)

    static final DOC_DELETED                = getRefdataValue('Deleted', RDConstants.DOCUMENT_CONTEXT_STATUS)
    @Deprecated
    static final IE_DELETED                 = TIPP_DELETED
    static final ORG_DELETED                = getRefdataValue('Deleted', RDConstants.ORG_STATUS)
    static final PACKAGE_DELETED            = getRefdataValue('Deleted', RDConstants.PACKAGE_STATUS)
    static final PLATFORM_DELETED           = getRefdataValue('Deleted', RDConstants.PLATFORM_STATUS)
    static final TIPP_DELETED               = getRefdataValue('Deleted',  RDConstants.TIPP_STATUS)

//DO NOT USE THIS STATUS. Subs have no longer a deleted flag. They ARE deleted!
    @Deprecated
    static final SUBSCRIPTION_DELETED       = getRefdataValue('Deleted', RDConstants.SUBSCRIPTION_STATUS)
    static final SUBSCRIPTION_CURRENT       = getRefdataValue('Current', RDConstants.SUBSCRIPTION_STATUS)
    static final SUBSCRIPTION_INTENDED      = getRefdataValue('Intended', RDConstants.SUBSCRIPTION_STATUS)
    static final SUBSCRIPTION_EXPIRED       = getRefdataValue('Expired', RDConstants.SUBSCRIPTION_STATUS)
    static final SUBSCRIPTION_NO_STATUS     = getRefdataValue('Status not defined', RDConstants.SUBSCRIPTION_STATUS)

    static final SUBSCRIPTION_INTENDED_PERENNIAL = getRefdataValue('IntendedPerennial', RDConstants.SUBSCRIPTION_STATUS)
    static final SUBSCRIPTION_EXPIRED_PERENNIAL = getRefdataValue('ExpiredPerennial', RDConstants.SUBSCRIPTION_STATUS)

    static final SURVEY_READY               = getRefdataValue('Ready', RDConstants.SURVEY_STATUS)
    static final SURVEY_IN_PROCESSING       = getRefdataValue('In Processing', RDConstants.SURVEY_STATUS)
    static final SURVEY_IN_EVALUATION       = getRefdataValue('In Evaluation', RDConstants.SURVEY_STATUS)
    static final SURVEY_COMPLETED           = getRefdataValue('Completed', RDConstants.SURVEY_STATUS)
    static final SURVEY_SURVEY_STARTED      = getRefdataValue('Survey started', RDConstants.SURVEY_STATUS)
    static final SURVEY_SURVEY_COMPLETED    = getRefdataValue('Survey completed', RDConstants.SURVEY_STATUS)

    static final SURVEY_TYPE_RENEWAL        = getRefdataValue('renewal', RDConstants.SURVEY_TYPE)
    static final SURVEY_TYPE_INTEREST       = getRefdataValue('interest', RDConstants.SURVEY_TYPE)
    static final SURVEY_TYPE_TITLE_SELECTION= getRefdataValue('selection', RDConstants.SURVEY_TYPE)


    static final COST_ITEM_ACTUAL           = getRefdataValue('Actual', RDConstants.COST_ITEM_STATUS)
    static final COST_ITEM_DELETED          = getRefdataValue('Deleted', RDConstants.COST_ITEM_STATUS)

    static final SUBSCRIPTION_TYPE_LOCAL            = getRefdataValue('Local Licence', RDConstants.SUBSCRIPTION_TYPE)
    static final SUBSCRIPTION_TYPE_CONSORTIAL       = getRefdataValue('Consortial Licence', RDConstants.SUBSCRIPTION_TYPE)
    static final SUBSCRIPTION_TYPE_ADMINISTRATIVE   = getRefdataValue('Administrative Subscription', RDConstants.SUBSCRIPTION_TYPE)
    static final SUBSCRIPTION_TYPE_ALLIANCE   		= getRefdataValue('Alliance Licence', RDConstants.SUBSCRIPTION_TYPE)
    static final SUBSCRIPTION_TYPE_NATIONAL   		= getRefdataValue('National Licence', RDConstants.SUBSCRIPTION_TYPE)
    //static final SUBSCRIPTION_TYPE_COLLECTIVE   	= getRefdataValue('Collective Subscription', RDConstants.SUBSCRIPTION_TYPE)

    static final LICENSE_TYPE_TEMPLATE      = getRefdataValue('Template', RDConstants.LICENSE_TYPE)
    static final LICENSE_TYPE_ACTUAL        = getRefdataValue('Actual', RDConstants.LICENSE_TYPE)

    static final LICENSE_NO_STATUS          = getRefdataValue('Status not defined', RDConstants.LICENSE_STATUS)
    static final LICENSE_CURRENT            = getRefdataValue('Current', RDConstants.LICENSE_STATUS)
    static final LICENSE_INTENDED           = getRefdataValue('Intended', RDConstants.LICENSE_STATUS)
    static final LICENSE_IN_PROGRESS        = getRefdataValue('In Progress', RDConstants.LICENSE_STATUS)

    static final LINKTYPE_FOLLOWS           = getRefdataValue('follows', RDConstants.LINK_TYPE)

    static final YN_YES                     = getRefdataValue('Yes', RDConstants.Y_N)
    static final YN_NO                      = getRefdataValue('No', RDConstants.Y_N)
    static final YNO_YES                    = getRefdataValue('Yes', RDConstants.Y_N_O)
    static final YNO_NO                     = getRefdataValue('No', RDConstants.Y_N_O)
    static final YNO_OTHER                  = getRefdataValue('Other', RDConstants.Y_N_O)

    static final CIEC_POSITIVE              = getRefdataValue('positive', RDConstants.COST_CONFIGURATION)
    static final CIEC_NEGATIVE              = getRefdataValue('negative', RDConstants.COST_CONFIGURATION)
    static final CIEC_NEUTRAL               = getRefdataValue('neutral', RDConstants.COST_CONFIGURATION)

    static final CURRENCY_EUR               = getRefdataValue('EUR', RDConstants.CURRENCY)

    static final PERM_PERM_EXPL             = getRefdataValue('Permitted (explicit)', RDConstants.PERMISSIONS)
    static final PERM_PERM_INTERP           = getRefdataValue('Permitted (interpreted)',RDConstants.PERMISSIONS)
    static final PERM_PROH_EXPL             = getRefdataValue('Prohibited (explicit)', RDConstants.PERMISSIONS)
    static final PERM_PROH_INTERP           = getRefdataValue('Prohibited (interpreted)', RDConstants.PERMISSIONS)
    static final PERM_SILENT                = getRefdataValue('Silent', RDConstants.PERMISSIONS)
    static final PERM_NOT_APPLICABLE        = getRefdataValue('Not applicable', RDConstants.PERMISSIONS)
    static final PERM_UNKNOWN               = getRefdataValue('Unknown', RDConstants.PERMISSIONS)

    static final TITLE_TYPE_EBOOK           = getRefdataValue('EBook', RDConstants.TITLE_MEDIUM)
    static final TITLE_TYPE_JOURNAL         = getRefdataValue('Journal', RDConstants.TITLE_MEDIUM)
    static final TITLE_TYPE_DATABASE        = getRefdataValue('Database', RDConstants.TITLE_MEDIUM)

    static final TITLE_STATUS_CURRENT       = getRefdataValue('Current', RDConstants.TITLE_STATUS)
    static final TITLE_STATUS_RETIRED       = getRefdataValue('Retired', RDConstants.TITLE_STATUS)
    static final TITLE_STATUS_DELETED       = getRefdataValue('Deleted', RDConstants.TITLE_STATUS)

    static final TAX_REVERSE_CHARGE         = getRefdataValue('reverse charge', RDConstants.TAX_TYPE)

    static final COMBO_TYPE_CONSORTIUM      = getRefdataValue('Consortium', RDConstants.COMBO_TYPE)
    static final COMBO_TYPE_DEPARTMENT      = getRefdataValue('Department', RDConstants.COMBO_TYPE)

    static final COMBO_STATUS_ACTIVE        = getRefdataValue('Active', RDConstants.COMBO_STATUS)
    static final COMBO_STATUS_INACTIVE      = getRefdataValue('Inactive', RDConstants.COMBO_STATUS)

    static final CONTACT_TYPE_PERSONAL      = getRefdataValue('Personal Contact', RDConstants.PERSON_CONTACT_TYPE)
    static final CONTACT_TYPE_FUNCTIONAL    = getRefdataValue('Functional Contact', RDConstants.PERSON_CONTACT_TYPE)

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
    //TIPP_STATUS_DELETED is defined above as TIPP_DELETED
    
    static final IE_ACCEPT_STATUS_FIXED            = getRefdataValue('Fixed', RDConstants.IE_ACCEPT_STATUS)
    static final IE_ACCEPT_STATUS_UNDER_NEGOTIATION = getRefdataValue('Under Negotiation', RDConstants.IE_ACCEPT_STATUS)
    static final IE_ACCEPT_STATUS_UNDER_CONSIDERATION = getRefdataValue('Under Consideration', RDConstants.IE_ACCEPT_STATUS)

    static final PRS_FUNC_GENERAL_CONTACT_PRS   = getRefdataValue('General contact person', RDConstants.PERSON_FUNCTION)
    static final PRS_FUNC_GASCO_CONTACT         = getRefdataValue('GASCO-Contact', RDConstants.PERSON_FUNCTION)
    static final CCT_EMAIL                      = getRefdataValue('E-Mail', RDConstants.CONTACT_CONTENT_TYPE)
    static final CCT_PHONE                      = getRefdataValue('Phone', RDConstants.CONTACT_CONTENT_TYPE)
    static final CCT_URL                        = getRefdataValue('Url', RDConstants.CONTACT_CONTENT_TYPE)
    static final PRS_RESP_SPEC_SUB_EDITOR       = getRefdataValue('Specific subscription editor', RDConstants.PERSON_RESPONSIBILITY)
    static final PRS_RESP_SPEC_LIC_EDITOR       = getRefdataValue('Specific license editor', RDConstants.PERSON_RESPONSIBILITY)

    static final PENDING_CHANGE_STATUS          = getRefdataValue('Pending', RDConstants.PENDING_CHANGE_STATUS)

    static final TASK_STATUS_OPEN               = getRefdataValue('Open', RDConstants.TASK_STATUS)
    static final TASK_STATUS_DONE               = getRefdataValue('Done', RDConstants.TASK_STATUS)

    static final SHARE_CONF_ALL                 = getRefdataValue('everyone', RDConstants.SHARE_CONFIGURATION)
    static final SHARE_CONF_CREATOR             = getRefdataValue('only for creator', RDConstants.SHARE_CONFIGURATION)
    static final SHARE_CONF_UPLOADER_ORG        = getRefdataValue('only for author organisation', RDConstants.SHARE_CONFIGURATION) //maps to key, value is correct!
    static final SHARE_CONF_UPLOADER_AND_TARGET = getRefdataValue('only for author and target organisation', RDConstants.SHARE_CONFIGURATION) //maps to key, value is correct!
    static final SHARE_CONF_CONSORTIUM          = getRefdataValue('only for consortia members', RDConstants.SHARE_CONFIGURATION)

    //Properties

    static final SURVEY_PARTICIPATION_PROPERTY = getSurveyProperty('Participation')

    static RefdataValue getRefdataValue(String value, String category) {
        RefdataValue result = RefdataValue.getByValueAndCategory(value, category)

        if (! result) {
            println "WARNING: No RefdataValue found by RDStore for value:'${value}', category:'${category}'"
        }
        (RefdataValue) GrailsHibernateUtil.unwrapIfProxy( result)
    }

    static SurveyProperty getSurveyProperty(String name) {
        (SurveyProperty) GrailsHibernateUtil.unwrapIfProxy( SurveyProperty.getByName(name))
    }
}
