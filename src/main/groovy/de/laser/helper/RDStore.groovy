package de.laser.helper

import de.laser.RefdataValue
import de.laser.properties.PropertyDefinition
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

//@CompileStatic
class RDStore {

    public static final GENERIC_NULL_VALUE         = getRefdataValue('generic.null.value','filter.fake.values')

    public static final ACCESS_POINT_TYPE_EZPROXY      = getRefdataValue('ezproxy', RDConstants.ACCESS_POINT_TYPE)
    public static final ACCESS_POINT_TYPE_IP           = getRefdataValue('ip', RDConstants.ACCESS_POINT_TYPE)
    public static final ACCESS_POINT_TYPE_OA           = getRefdataValue('oa', RDConstants.ACCESS_POINT_TYPE)
    public static final ACCESS_POINT_TYPE_PROXY        = getRefdataValue('proxy', RDConstants.ACCESS_POINT_TYPE)
    public static final ACCESS_POINT_TYPE_SHIBBOLETH   = getRefdataValue('shibboleth', RDConstants.ACCESS_POINT_TYPE)
    public static final ACCESS_POINT_TYPE_VPN          = getRefdataValue('vpn', RDConstants.ACCESS_POINT_TYPE)

    public static final ADRESS_TYPE_POSTAL             = getRefdataValue('Postal address', RDConstants.ADDRESS_TYPE)
    public static final ADRESS_TYPE_BILLING            = getRefdataValue('Billing address', RDConstants.ADDRESS_TYPE)
    public static final ADRESS_TYPE_LEGAL_PATRON       = getRefdataValue('Legal patron address', RDConstants.ADDRESS_TYPE)
    public static final ADRESS_TYPE_DELIVERY           = getRefdataValue('Delivery address', RDConstants.ADDRESS_TYPE)
    public static final ADRESS_TYPE_LIBRARY            = getRefdataValue('Library address', RDConstants.ADDRESS_TYPE)

    public static final CCT_EMAIL                  = getRefdataValue('E-Mail', RDConstants.CONTACT_CONTENT_TYPE)
    public static final CCT_PHONE                  = getRefdataValue('Phone', RDConstants.CONTACT_CONTENT_TYPE)
    public static final CCT_FAX                    = getRefdataValue('Fax', RDConstants.CONTACT_CONTENT_TYPE)
    public static final CCT_URL                    = getRefdataValue('Url', RDConstants.CONTACT_CONTENT_TYPE)

    public static final CIEC_POSITIVE              = getRefdataValue('positive', RDConstants.COST_CONFIGURATION)
    public static final CIEC_NEGATIVE              = getRefdataValue('negative', RDConstants.COST_CONFIGURATION)
    public static final CIEC_NEUTRAL               = getRefdataValue('neutral', RDConstants.COST_CONFIGURATION)

    public static final COMBO_TYPE_CONSORTIUM      = getRefdataValue('Consortium', RDConstants.COMBO_TYPE)
    public static final COMBO_TYPE_FOLLOWS         = getRefdataValue('follows', RDConstants.COMBO_TYPE)

    public static final COMBO_STATUS_ACTIVE        = getRefdataValue('Active', RDConstants.COMBO_STATUS)
    public static final COMBO_STATUS_INACTIVE      = getRefdataValue('Inactive', RDConstants.COMBO_STATUS)

    public static final CONTACT_TYPE_JOBRELATED    = getRefdataValue('Job-related', RDConstants.CONTACT_TYPE)
    public static final CONTACT_TYPE_PERSONAL      = getRefdataValue('Personal', RDConstants.CONTACT_TYPE)

    public static final COUNTRY_DE                 = getRefdataValue('DE', RDConstants.COUNTRY)
    public static final COUNTRY_AT                 = getRefdataValue('AT', RDConstants.COUNTRY)
    public static final COUNTRY_CH                 = getRefdataValue('CH', RDConstants.COUNTRY)

    public static final COST_ITEM_ACTUAL           = getRefdataValue('Actual', RDConstants.COST_ITEM_STATUS)
    public static final COST_ITEM_DELETED          = getRefdataValue('Deleted', RDConstants.COST_ITEM_STATUS)

    public static final COST_ITEM_ELEMENT_CONSORTIAL_PRICE          = getRefdataValue('price: consortial price', RDConstants.COST_ITEM_ELEMENT)

    public static final CURRENCY_EUR               = getRefdataValue('EUR', RDConstants.CURRENCY)
    public static final CURRENCY_GBP               = getRefdataValue('GBP', RDConstants.CURRENCY)
    public static final CURRENCY_USD               = getRefdataValue('USD', RDConstants.CURRENCY)

    public static final DOC_CTX_STATUS_DELETED     = getRefdataValue('Deleted', RDConstants.DOCUMENT_CONTEXT_STATUS)
    public static final DOC_TYPE_ANNOUNCEMENT      = getRefdataValue('Announcement', RDConstants.DOCUMENT_TYPE)
    public static final DOC_TYPE_NOTE              = getRefdataValue('Note', RDConstants.DOCUMENT_TYPE)
    public static final DOC_TYPE_ONIXPL            = getRefdataValue('ONIX-PL License', RDConstants.DOCUMENT_TYPE)

    public static final IE_ACCESS_CURRENT          = getRefdataValue('Current', RDConstants.IE_ACCESS_STATUS)

    public static final IE_ACCEPT_STATUS_FIXED                 = getRefdataValue('Fixed', RDConstants.IE_ACCEPT_STATUS)
    public static final IE_ACCEPT_STATUS_UNDER_NEGOTIATION     = getRefdataValue('Under Negotiation', RDConstants.IE_ACCEPT_STATUS)
    public static final IE_ACCEPT_STATUS_UNDER_CONSIDERATION   = getRefdataValue('Under Consideration', RDConstants.IE_ACCEPT_STATUS)

    public static final LANGUAGE_DE         = getRefdataValue('de', RDConstants.LANGUAGE)
    
    public static final LICENSE_TYPE_ACTUAL        = getRefdataValue('Actual', RDConstants.LICENSE_TYPE)

    public static final LICENSE_NO_STATUS          = getRefdataValue('Status not defined', RDConstants.LICENSE_STATUS)
    public static final LICENSE_CURRENT            = getRefdataValue('Current', RDConstants.LICENSE_STATUS)
    public static final LICENSE_INTENDED           = getRefdataValue('Intended', RDConstants.LICENSE_STATUS)
    //public static final LICENSE_IN_PROGRESS        = getRefdataValue('In Progress', RDConstants.LICENSE_STATUS)
    public static final LICENSE_EXPIRED            = getRefdataValue('Retired', RDConstants.LICENSE_STATUS)

    public static final LINKTYPE_FOLLOWS           = getRefdataValue('follows', RDConstants.LINK_TYPE)
    public static final LINKTYPE_LICENSE           = getRefdataValue('license', RDConstants.LINK_TYPE)

    public static final ORG_STATUS_CURRENT         = getRefdataValue('Current', RDConstants.ORG_STATUS)
    public static final ORG_STATUS_DELETED         = getRefdataValue('Deleted', RDConstants.ORG_STATUS)
    public static final ORG_STATUS_RETIRED         = getRefdataValue('Retired', RDConstants.ORG_STATUS)

    public static final OR_LICENSING_CONSORTIUM    = getRefdataValue('Licensing Consortium', RDConstants.ORGANISATIONAL_ROLE)
    public static final OR_LICENSEE                = getRefdataValue('Licensee', RDConstants.ORGANISATIONAL_ROLE)
    public static final OR_LICENSEE_CONS           = getRefdataValue('Licensee_Consortial', RDConstants.ORGANISATIONAL_ROLE)

    public static final OR_SUBSCRIPTION_CONSORTIA  = getRefdataValue('Subscription Consortia', RDConstants.ORGANISATIONAL_ROLE)
    public static final OR_SUBSCRIBER              = getRefdataValue('Subscriber', RDConstants.ORGANISATIONAL_ROLE)
    public static final OR_SUBSCRIBER_CONS         = getRefdataValue('Subscriber_Consortial', RDConstants.ORGANISATIONAL_ROLE)
    public static final OR_SUBSCRIBER_CONS_HIDDEN  = getRefdataValue('Subscriber_Consortial_Hidden', RDConstants.ORGANISATIONAL_ROLE)

    public static final OR_AGENCY                  = getRefdataValue('Agency', RDConstants.ORGANISATIONAL_ROLE)
    public static final OR_LICENSOR                = getRefdataValue('Licensor', RDConstants.ORGANISATIONAL_ROLE)
    public static final OR_PROVIDER                = getRefdataValue('Provider', RDConstants.ORGANISATIONAL_ROLE)
    public static final OR_PUBLISHER               = getRefdataValue('Publisher', RDConstants.ORGANISATIONAL_ROLE)
    public static final OR_CONTENT_PROVIDER        = getRefdataValue('Content Provider', RDConstants.ORGANISATIONAL_ROLE)
    public static final OR_PACKAGE_CONSORTIA       = getRefdataValue('Package Consortia', RDConstants.ORGANISATIONAL_ROLE)

    public static final O_SECTOR_HIGHER_EDU        = getRefdataValue('Higher Education', RDConstants.ORG_SECTOR)
    public static final O_SECTOR_PUBLISHER         = getRefdataValue('Publisher', RDConstants.ORG_SECTOR)

    public static final O_STATUS_CURRENT           = getRefdataValue('Current', RDConstants.ORG_STATUS)
    public static final O_STATUS_DELETED           = getRefdataValue('Deleted', RDConstants.ORG_STATUS)

    public static final OT_CONSORTIUM              = getRefdataValue('Consortium', RDConstants.ORG_TYPE)
    public static final OT_INSTITUTION             = getRefdataValue('Institution', RDConstants.ORG_TYPE)
    public static final OT_AGENCY                  = getRefdataValue('Agency', RDConstants.ORG_TYPE)
    public static final OT_LICENSOR                = getRefdataValue('Licensor', RDConstants.ORG_TYPE)
    public static final OT_PROVIDER                = getRefdataValue('Provider', RDConstants.ORG_TYPE)
    public static final OT_PUBLISHER               = getRefdataValue('Publisher', RDConstants.ORG_TYPE)

    public static final PACKAGE_STATUS_DELETED      = getRefdataValue('Deleted', RDConstants.PACKAGE_STATUS)

    public static final PACKAGE_SCOPE_NATIONAL      = getRefdataValue('National', RDConstants.PACKAGE_SCOPE)

    public static final PENDING_CHANGE_PENDING    = getRefdataValue('Pending', RDConstants.PENDING_CHANGE_STATUS)
    public static final PENDING_CHANGE_ACCEPTED   = getRefdataValue('Accepted', RDConstants.PENDING_CHANGE_STATUS)
    public static final PENDING_CHANGE_SUPERSEDED = getRefdataValue('Superseded', RDConstants.PENDING_CHANGE_STATUS)
    public static final PENDING_CHANGE_HISTORY    = getRefdataValue('History', RDConstants.PENDING_CHANGE_STATUS)
    public static final PENDING_CHANGE_REJECTED   = getRefdataValue('Rejected', RDConstants.PENDING_CHANGE_STATUS)

    public static final PENDING_CHANGE_CONFIG_ACCEPT = getRefdataValue('Accept', RDConstants.PENDING_CHANGE_CONFIG_SETTING)
    public static final PENDING_CHANGE_CONFIG_PROMPT = getRefdataValue('Prompt', RDConstants.PENDING_CHANGE_CONFIG_SETTING)
    public static final PENDING_CHANGE_CONFIG_REJECT = getRefdataValue('Reject', RDConstants.PENDING_CHANGE_CONFIG_SETTING)

    public static final PERM_PERM_EXPL              = getRefdataValue('Permitted (explicit)', RDConstants.PERMISSIONS)
    public static final PERM_PERM_INTERP            = getRefdataValue('Permitted (interpreted)',RDConstants.PERMISSIONS)
    public static final PERM_PROH_EXPL              = getRefdataValue('Prohibited (explicit)', RDConstants.PERMISSIONS)
    public static final PERM_PROH_INTERP            = getRefdataValue('Prohibited (interpreted)', RDConstants.PERMISSIONS)
    public static final PERM_SILENT                 = getRefdataValue('Silent', RDConstants.PERMISSIONS)
    public static final PERM_NOT_APPLICABLE         = getRefdataValue('Not applicable', RDConstants.PERMISSIONS)
    public static final PERM_UNKNOWN                = getRefdataValue('Unknown', RDConstants.PERMISSIONS)

    public static final PERSON_CONTACT_TYPE_PERSONAL      = getRefdataValue('Personal Contact', RDConstants.PERSON_CONTACT_TYPE)
    public static final PERSON_CONTACT_TYPE_FUNCTIONAL    = getRefdataValue('Functional Contact', RDConstants.PERSON_CONTACT_TYPE)

    public static final PLATFORM_STATUS_CURRENT        = getRefdataValue('Current', RDConstants.PLATFORM_STATUS)
    public static final PLATFORM_STATUS_DELETED        = getRefdataValue('Deleted', RDConstants.PLATFORM_STATUS)

    public static final PRS_RESP_SPEC_SUB_EDITOR       = getRefdataValue('Specific subscription editor', RDConstants.PERSON_RESPONSIBILITY)
    public static final PRS_RESP_SPEC_LIC_EDITOR       = getRefdataValue('Specific license editor', RDConstants.PERSON_RESPONSIBILITY)

    public static final REPORTING_CONTACT_TYPE_CONTACTS     = getRefdataValue('contacts', RDConstants.REPORTING_CONTACT_TYPE)
    public static final REPORTING_CONTACT_TYPE_ADDRESSES    = getRefdataValue('addresses', RDConstants.REPORTING_CONTACT_TYPE)

    public static final READER_NUMBER_USER              = getRefdataValue('User', RDConstants.NUMBER_TYPE)
    public static final READER_NUMBER_PEOPLE            = getRefdataValue('Population', RDConstants.NUMBER_TYPE)
    public static final READER_NUMBER_SCIENTIFIC_STAFF  = getRefdataValue('Scientific staff', RDConstants.NUMBER_TYPE)
    public static final READER_NUMBER_STUDENTS          = getRefdataValue('Students', RDConstants.NUMBER_TYPE)
    public static final READER_NUMBER_FTE               = getRefdataValue('FTE', RDConstants.NUMBER_TYPE)

    public static final SHARE_CONF_ALL                 = getRefdataValue('everyone', RDConstants.SHARE_CONFIGURATION)
    public static final SHARE_CONF_UPLOADER_ORG        = getRefdataValue('only for author organisation', RDConstants.SHARE_CONFIGURATION) //maps to key, value is correct!
    public static final SHARE_CONF_UPLOADER_AND_TARGET = getRefdataValue('only for author and target organisation', RDConstants.SHARE_CONFIGURATION) //maps to key, value is correct!
    public static final SHARE_CONF_CONSORTIUM          = getRefdataValue('only for consortia members', RDConstants.SHARE_CONFIGURATION)

    //DO NOT USE THIS STATUS. Subs have no longer a deleted flag. They ARE deleted!
    //@Deprecated
    //public static final SUBSCRIPTION_DELETED       = getRefdataValue('Deleted', RDConstants.SUBSCRIPTION_STATUS)
    public static final SUBSCRIPTION_CURRENT        = getRefdataValue('Current', RDConstants.SUBSCRIPTION_STATUS)
    public static final SUBSCRIPTION_INTENDED       = getRefdataValue('Intended', RDConstants.SUBSCRIPTION_STATUS)
    public static final SUBSCRIPTION_EXPIRED        = getRefdataValue('Expired', RDConstants.SUBSCRIPTION_STATUS)
    public static final SUBSCRIPTION_NO_STATUS      = getRefdataValue('Status not defined', RDConstants.SUBSCRIPTION_STATUS)
    public static final SUBSCRIPTION_UNDER_PROCESS_OF_SELECTION     = getRefdataValue('Under Process Of Selection', RDConstants.SUBSCRIPTION_STATUS)
    public static final SUBSCRIPTION_ORDERED     = getRefdataValue('Ordered', RDConstants.SUBSCRIPTION_STATUS)

    public static final SURVEY_READY                   = getRefdataValue('Ready', RDConstants.SURVEY_STATUS)
    public static final SURVEY_IN_PROCESSING           = getRefdataValue('In Processing', RDConstants.SURVEY_STATUS)
    public static final SURVEY_IN_EVALUATION           = getRefdataValue('In Evaluation', RDConstants.SURVEY_STATUS)
    public static final SURVEY_COMPLETED               = getRefdataValue('Completed', RDConstants.SURVEY_STATUS)
    public static final SURVEY_SURVEY_STARTED          = getRefdataValue('Survey started', RDConstants.SURVEY_STATUS)
    public static final SURVEY_SURVEY_COMPLETED        = getRefdataValue('Survey completed', RDConstants.SURVEY_STATUS)

    public static final SURVEY_TYPE_RENEWAL                 = getRefdataValue('renewal', RDConstants.SURVEY_TYPE)
    public static final SURVEY_TYPE_INTEREST                = getRefdataValue('interest', RDConstants.SURVEY_TYPE)
    public static final SURVEY_TYPE_TITLE_SELECTION         = getRefdataValue('selection', RDConstants.SURVEY_TYPE)
    public static final SURVEY_TYPE_SUBSCRIPTION            = getRefdataValue('subscription survey', RDConstants.SURVEY_TYPE)

    public static final SUBSCRIPTION_TYPE_LOCAL            = getRefdataValue('Local Licence', RDConstants.SUBSCRIPTION_TYPE)
    public static final SUBSCRIPTION_TYPE_CONSORTIAL       = getRefdataValue('Consortial Licence', RDConstants.SUBSCRIPTION_TYPE)
    public static final SUBSCRIPTION_TYPE_ADMINISTRATIVE   = getRefdataValue('Administrative Subscription', RDConstants.SUBSCRIPTION_TYPE)

    public static final SUBSCRIPTION_KIND_CONSORTIAL       = getRefdataValue('Consortial Licence', RDConstants.SUBSCRIPTION_KIND)
    public static final SUBSCRIPTION_KIND_ALLIANCE   		= getRefdataValue('Alliance Licence', RDConstants.SUBSCRIPTION_KIND)
    public static final SUBSCRIPTION_KIND_NATIONAL   		= getRefdataValue('National Licence', RDConstants.SUBSCRIPTION_KIND)
    public static final SUBSCRIPTION_KIND_LOCAL            = getRefdataValue('Local Licence', RDConstants.SUBSCRIPTION_KIND)

    public static final TASK_STATUS_OPEN            = getRefdataValue('Open', RDConstants.TASK_STATUS)
    public static final TASK_STATUS_DONE            = getRefdataValue('Done', RDConstants.TASK_STATUS)
    public static final TASK_STATUS_DEFERRED        = getRefdataValue('Deferred', RDConstants.TASK_STATUS)

    public static final TAX_REVERSE_CHARGE          = getRefdataValue('reverse charge', RDConstants.TAX_TYPE)

    public static final TITLE_TYPE_EBOOK            = getRefdataValue('Book', RDConstants.TITLE_MEDIUM)
    public static final TITLE_TYPE_JOURNAL          = getRefdataValue('Journal', RDConstants.TITLE_MEDIUM)
    public static final TITLE_TYPE_DATABASE         = getRefdataValue('Database', RDConstants.TITLE_MEDIUM)

    public static final TITLE_STATUS_CURRENT        = getRefdataValue('Current', RDConstants.TITLE_STATUS)
    public static final TITLE_STATUS_RETIRED        = getRefdataValue('Retired', RDConstants.TITLE_STATUS)
    public static final TITLE_STATUS_DELETED        = getRefdataValue('Deleted', RDConstants.TITLE_STATUS)

    public static final TIPP_PAYMENT_PAID           = getRefdataValue('Paid', RDConstants.TIPP_ACCESS_TYPE)
    public static final TIPP_PAYMENT_FREE           = getRefdataValue('Free', RDConstants.TIPP_ACCESS_TYPE)

    public static final TIPP_STATUS_CURRENT            = getRefdataValue('Current', RDConstants.TIPP_STATUS)
    public static final TIPP_STATUS_RETIRED            = getRefdataValue('Retired', RDConstants.TIPP_STATUS)
    public static final TIPP_STATUS_EXPECTED           = getRefdataValue('Expected', RDConstants.TIPP_STATUS)
    public static final TIPP_STATUS_TRANSFERRED        = getRefdataValue('Transferred', RDConstants.TIPP_STATUS)
    public static final TIPP_STATUS_UNKNOWN            = getRefdataValue('Unknown', RDConstants.TIPP_STATUS)
    public static final TIPP_STATUS_DELETED            = getRefdataValue('Deleted',  RDConstants.TIPP_STATUS)


    public static final PRS_FUNC_CONTACT_PRS           = getRefdataValue('Contact Person', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_FUNC_BILLING_ADDRESS  = getRefdataValue('Functional Contact Billing Adress', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_FUNC_DELIVERY_ADDRESS = getRefdataValue('Functional Contact Delivery Address', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_FUNC_LEGAL_PATRON_ADDRESS     = getRefdataValue('Functional Contact Legal Patron Address', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_FUNC_LIBRARY_ADDRESS     = getRefdataValue('Functional Contact Library Address', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_FUNC_POSTAL_ADDRESS     = getRefdataValue('Functional Contact Postal Address', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_GASCO_CONTACT         = getRefdataValue('GASCO-Contact', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_GENERAL_CONTACT_PRS   = getRefdataValue('General contact person', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_RESPONSIBLE_ADMIN     = getRefdataValue('Responsible Admin', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_SERVICE_SUPPORT     = getRefdataValue('Service Support', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_TECHNICAL_SUPPORT     = getRefdataValue('Technical Support', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_METADATA     = getRefdataValue('Metadata', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_CUSTOMER_SERVICE     = getRefdataValue('Customer Service', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_SALES_MARKETING     = getRefdataValue('Sales and Marketing', RDConstants.PERSON_FUNCTION)
    public static final PRS_FUNC_TRAINING     = getRefdataValue('Training', RDConstants.PERSON_FUNCTION)

    public static final PRS_POS_ACCOUNT     = getRefdataValue('Account Manager', RDConstants.PERSON_POSITION)
    public static final PRS_POS_BBL     = getRefdataValue('Bereichsbibliotheksleitung', RDConstants.PERSON_POSITION)
    public static final PRS_POS_DIREKTION     = getRefdataValue('Direktion', RDConstants.PERSON_POSITION)
    public static final PRS_POS_DIREKTION_ASS     = getRefdataValue('Direktionsassistenz', RDConstants.PERSON_POSITION)
    public static final PRS_POS_EA     = getRefdataValue('Erwerbungsabteilung', RDConstants.PERSON_POSITION)
    public static final PRS_POS_EL     = getRefdataValue('Erwerbungsleitung', RDConstants.PERSON_POSITION)
    public static final PRS_POS_FACHREFERAT     = getRefdataValue('Fachreferat', RDConstants.PERSON_POSITION)
    public static final PRS_POS_HEAD     = getRefdataValue('Head Access Services', RDConstants.PERSON_POSITION)
    public static final PRS_POS_LD     = getRefdataValue('Library Director', RDConstants.PERSON_POSITION)
    public static final PRS_POS_MB    = getRefdataValue('Medienbearbeitung', RDConstants.PERSON_POSITION)
    public static final PRS_POS_RB    = getRefdataValue('Rechnungsbearbeitung', RDConstants.PERSON_POSITION)
    public static final PRS_POS_SD     = getRefdataValue('Sales Director', RDConstants.PERSON_POSITION)
    public static final PRS_POS_SS     = getRefdataValue('Sales Support', RDConstants.PERSON_POSITION)
    public static final PRS_POS_TS     = getRefdataValue('Technical Support', RDConstants.PERSON_POSITION)
    public static final PRS_POS_ZA     = getRefdataValue('Zeitschriftenabteilung', RDConstants.PERSON_POSITION)

    //public static final WF_CONDITION_STATUS_OPEN    = getRefdataValue('open', RDConstants.WF_CONDITION_STATUS)
    //public static final WF_CONDITION_STATUS_DONE    = getRefdataValue('done', RDConstants.WF_CONDITION_STATUS)

    public static final WF_TASK_PRIORITY_NORMAL     = getRefdataValue('normal', RDConstants.WF_TASK_PRIORITY)
    public static final WF_TASK_PRIORITY_IMPORTANT  = getRefdataValue('important', RDConstants.WF_TASK_PRIORITY)
    public static final WF_TASK_PRIORITY_OPTIONAL   = getRefdataValue('optional', RDConstants.WF_TASK_PRIORITY)

    public static final WF_TASK_STATUS_OPEN         = getRefdataValue('open', RDConstants.WF_TASK_STATUS)
    public static final WF_TASK_STATUS_CANCELED     = getRefdataValue('canceled', RDConstants.WF_TASK_STATUS)
    public static final WF_TASK_STATUS_DONE         = getRefdataValue('done', RDConstants.WF_TASK_STATUS)

    public static final WF_WORKFLOW_STATE_ACTIVE    = getRefdataValue('active', RDConstants.WF_WORKFLOW_STATE)
    public static final WF_WORKFLOW_STATUS_OPEN     = getRefdataValue('open', RDConstants.WF_WORKFLOW_STATUS)
    public static final WF_WORKFLOW_STATUS_CANCELED = getRefdataValue('canceled', RDConstants.WF_WORKFLOW_STATUS)
    public static final WF_WORKFLOW_STATUS_DONE     = getRefdataValue('done', RDConstants.WF_WORKFLOW_STATUS)

    public static final YN_YES              = getRefdataValue('Yes', RDConstants.Y_N)
    public static final YN_NO               = getRefdataValue('No', RDConstants.Y_N)
    public static final YNO_YES             = getRefdataValue('Yes', RDConstants.Y_N_O)
    public static final YNO_NO              = getRefdataValue('No', RDConstants.Y_N_O)
    public static final YNO_OTHER           = getRefdataValue('Other', RDConstants.Y_N_O)
    public static final YNU_UNKNOWN         = getRefdataValue('Unknown', RDConstants.Y_N_U)

    //Properties

    public static final SURVEY_PROPERTY_PARTICIPATION   = getSurveyProperty('Participation')
    public static final SURVEY_PROPERTY_ORDER_NUMBER    = getSurveyProperty('Order number')
    public static final SURVEY_PROPERTY_MULTI_YEAR_3    = getSurveyProperty('Multi-year term 3 years')
    public static final SURVEY_PROPERTY_MULTI_YEAR_2    = getSurveyProperty('Multi-year term 2 years')


    static RefdataValue getRefdataValue(String value, String category) {
        RefdataValue result = RefdataValue.getByValueAndCategory(value, category)

        if (! result) {
            println "WARNING: No RefdataValue found by RDStore for value:'${value}', category:'${category}'"
        }
        (RefdataValue) GrailsHibernateUtil.unwrapIfProxy( result)
    }

    static PropertyDefinition getSurveyProperty(String name) {
        PropertyDefinition result = PropertyDefinition.getByNameAndDescrAndTenant(name, PropertyDefinition.SVY_PROP, null)

        if (! result) {
            println "WARNING: No PropertyDefinition found by RDStore for name:'${name}', descr:'${PropertyDefinition.SVY_PROP}'"
        }

        (PropertyDefinition) GrailsHibernateUtil.unwrapIfProxy(result)
    }
}
