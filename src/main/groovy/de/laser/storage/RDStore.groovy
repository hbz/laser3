package de.laser.storage

import de.laser.RefdataValue
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

@CompileStatic
@Slf4j
class RDStore {

    public final static RefdataValue GENERIC_NULL_VALUE             = getRefdataValue('generic.null.value','filter.fake.values')

    public final static RefdataValue ACCESS_POINT_TYPE_EZPROXY      = getRefdataValue('ezproxy', RDConstants.ACCESS_POINT_TYPE)
    public final static RefdataValue ACCESS_POINT_TYPE_IP           = getRefdataValue('ip', RDConstants.ACCESS_POINT_TYPE)
    public final static RefdataValue ACCESS_POINT_TYPE_MAIL_DOMAIN  = getRefdataValue('mailDomain', RDConstants.ACCESS_POINT_TYPE)
    public final static RefdataValue ACCESS_POINT_TYPE_OA           = getRefdataValue('oa', RDConstants.ACCESS_POINT_TYPE)
    public final static RefdataValue ACCESS_POINT_TYPE_PROXY        = getRefdataValue('proxy', RDConstants.ACCESS_POINT_TYPE)
    public final static RefdataValue ACCESS_POINT_TYPE_SHIBBOLETH   = getRefdataValue('shibboleth', RDConstants.ACCESS_POINT_TYPE)
    public final static RefdataValue ACCESS_POINT_TYPE_VPN          = getRefdataValue('vpn', RDConstants.ACCESS_POINT_TYPE)

    public final static RefdataValue ADDRESS_TYPE_POSTAL         = getRefdataValue('Postal address', RDConstants.ADDRESS_TYPE)
    public final static RefdataValue ADDRESS_TYPE_BILLING        = getRefdataValue('Billing address', RDConstants.ADDRESS_TYPE)
    public final static RefdataValue ADDRESS_TYPE_LEGAL_PATRON   = getRefdataValue('Legal patron address', RDConstants.ADDRESS_TYPE)
    public final static RefdataValue ADDRESS_TYPE_DELIVERY       = getRefdataValue('Delivery address', RDConstants.ADDRESS_TYPE)
    public final static RefdataValue ADDRESS_TYPE_LIBRARY        = getRefdataValue('Library address', RDConstants.ADDRESS_TYPE)

    public final static RefdataValue CCT_EMAIL  = getRefdataValue('E-Mail', RDConstants.CONTACT_CONTENT_TYPE)
    public final static RefdataValue CCT_PHONE  = getRefdataValue('Phone', RDConstants.CONTACT_CONTENT_TYPE)
    public final static RefdataValue CCT_FAX    = getRefdataValue('Fax', RDConstants.CONTACT_CONTENT_TYPE)
    public final static RefdataValue CCT_URL    = getRefdataValue('Url', RDConstants.CONTACT_CONTENT_TYPE)

    public final static RefdataValue CIEC_POSITIVE  = getRefdataValue('positive', RDConstants.COST_CONFIGURATION)
    public final static RefdataValue CIEC_NEGATIVE  = getRefdataValue('negative', RDConstants.COST_CONFIGURATION)
    public final static RefdataValue CIEC_NEUTRAL   = getRefdataValue('neutral', RDConstants.COST_CONFIGURATION)

    public final static RefdataValue COMBO_TYPE_CONSORTIUM      = getRefdataValue('Consortium', RDConstants.COMBO_TYPE)
    public final static RefdataValue COMBO_TYPE_FOLLOWS         = getRefdataValue('follows', RDConstants.COMBO_TYPE)

    public final static RefdataValue COMBO_STATUS_ACTIVE        = getRefdataValue('Active', RDConstants.COMBO_STATUS)
    public final static RefdataValue COMBO_STATUS_INACTIVE      = getRefdataValue('Inactive', RDConstants.COMBO_STATUS)

    public final static RefdataValue CONTACT_TYPE_JOBRELATED    = getRefdataValue('Job-related', RDConstants.CONTACT_TYPE)
    public final static RefdataValue CONTACT_TYPE_PERSONAL      = getRefdataValue('Personal', RDConstants.CONTACT_TYPE)

    public final static RefdataValue COUNTRY_DE     = getRefdataValue('DE', RDConstants.COUNTRY)
    public final static RefdataValue COUNTRY_AT     = getRefdataValue('AT', RDConstants.COUNTRY)
    public final static RefdataValue COUNTRY_CH     = getRefdataValue('CH', RDConstants.COUNTRY)

    public final static RefdataValue COST_ITEM_ACTUAL   = getRefdataValue('Actual', RDConstants.COST_ITEM_STATUS)
    public final static RefdataValue COST_ITEM_DELETED  = getRefdataValue('Deleted', RDConstants.COST_ITEM_STATUS)

    public final static RefdataValue COST_ITEM_ELEMENT_CONSORTIAL_PRICE = getRefdataValue('price: consortial price', RDConstants.COST_ITEM_ELEMENT)

    public final static RefdataValue CURRENCY_EUR   = getRefdataValue('EUR', RDConstants.CURRENCY)
    public final static RefdataValue CURRENCY_GBP   = getRefdataValue('GBP', RDConstants.CURRENCY)
    public final static RefdataValue CURRENCY_USD   = getRefdataValue('USD', RDConstants.CURRENCY)

    public final static RefdataValue DOC_CONF_PUBLIC            = getRefdataValue('public', RDConstants.DOCUMENT_CONFIDENTIALITY)
    public final static RefdataValue DOC_CONF_INTERNAL          = getRefdataValue('internal', RDConstants.DOCUMENT_CONFIDENTIALITY)
    public final static RefdataValue DOC_CONF_STRICTLY          = getRefdataValue('strictly_confidential', RDConstants.DOCUMENT_CONFIDENTIALITY)
    public final static RefdataValue DOC_CTX_STATUS_DELETED     = getRefdataValue('Deleted', RDConstants.DOCUMENT_CONTEXT_STATUS)
    public final static RefdataValue DOC_TYPE_ANNOUNCEMENT      = getRefdataValue('Announcement', RDConstants.DOCUMENT_TYPE)
    public final static RefdataValue DOC_TYPE_NOTE              = getRefdataValue('Note', RDConstants.DOCUMENT_TYPE)
    public final static RefdataValue DOC_TYPE_ONIXPL            = getRefdataValue('ONIX-PL License', RDConstants.DOCUMENT_TYPE)

    public final static RefdataValue IE_ACCESS_CURRENT                      = getRefdataValue('Current', RDConstants.IE_ACCESS_STATUS)
    public final static RefdataValue IE_ACCEPT_STATUS_FIXED                 = getRefdataValue('Fixed', RDConstants.IE_ACCEPT_STATUS)
    public final static RefdataValue IE_ACCEPT_STATUS_UNDER_NEGOTIATION     = getRefdataValue('Under Negotiation', RDConstants.IE_ACCEPT_STATUS)
    public final static RefdataValue IE_ACCEPT_STATUS_UNDER_CONSIDERATION   = getRefdataValue('Under Consideration', RDConstants.IE_ACCEPT_STATUS)

    public final static RefdataValue LANGUAGE_DE            = getRefdataValue('de', RDConstants.LANGUAGE)
    
    public final static RefdataValue LICENSE_TYPE_ACTUAL    = getRefdataValue('Actual', RDConstants.LICENSE_TYPE)

    public final static RefdataValue LICENSE_NO_STATUS      = getRefdataValue('Status not defined', RDConstants.LICENSE_STATUS)
    public final static RefdataValue LICENSE_CURRENT        = getRefdataValue('Current', RDConstants.LICENSE_STATUS)
    public final static RefdataValue LICENSE_INTENDED       = getRefdataValue('Intended', RDConstants.LICENSE_STATUS)
    //public final static RefdataValue LICENSE_IN_PROGRESS  = getRefdataValue('In Progress', RDConstants.LICENSE_STATUS)
    public final static RefdataValue LICENSE_EXPIRED        = getRefdataValue('Retired', RDConstants.LICENSE_STATUS)

    public final static RefdataValue LINKTYPE_FOLLOWS       = getRefdataValue('follows', RDConstants.LINK_TYPE)
    public final static RefdataValue LINKTYPE_LICENSE       = getRefdataValue('license', RDConstants.LINK_TYPE)

    public final static RefdataValue ORG_STATUS_CURRENT     = getRefdataValue('Current', RDConstants.ORG_STATUS)
    public final static RefdataValue ORG_STATUS_DELETED     = getRefdataValue('Deleted', RDConstants.ORG_STATUS)
    public final static RefdataValue ORG_STATUS_REMOVED     = getRefdataValue('Removed', RDConstants.ORG_STATUS)
    public final static RefdataValue ORG_STATUS_RETIRED     = getRefdataValue('Retired', RDConstants.ORG_STATUS)

    public final static RefdataValue OR_LICENSING_CONSORTIUM    = getRefdataValue('Licensing Consortium', RDConstants.ORGANISATIONAL_ROLE)
    public final static RefdataValue OR_LICENSEE                = getRefdataValue('Licensee', RDConstants.ORGANISATIONAL_ROLE)
    public final static RefdataValue OR_LICENSEE_CONS           = getRefdataValue('Licensee_Consortial', RDConstants.ORGANISATIONAL_ROLE)

    public final static RefdataValue OR_SUBSCRIPTION_CONSORTIA  = getRefdataValue('Subscription Consortia', RDConstants.ORGANISATIONAL_ROLE)
    public final static RefdataValue OR_SUBSCRIBER              = getRefdataValue('Subscriber', RDConstants.ORGANISATIONAL_ROLE)
    public final static RefdataValue OR_SUBSCRIBER_CONS         = getRefdataValue('Subscriber_Consortial', RDConstants.ORGANISATIONAL_ROLE)
    public final static RefdataValue OR_SUBSCRIBER_CONS_HIDDEN  = getRefdataValue('Subscriber_Consortial_Hidden', RDConstants.ORGANISATIONAL_ROLE)

    public final static RefdataValue OR_AGENCY                  = getRefdataValue('Agency', RDConstants.ORGANISATIONAL_ROLE)
    public final static RefdataValue OR_LICENSOR                = getRefdataValue('Licensor', RDConstants.ORGANISATIONAL_ROLE)
    public final static RefdataValue OR_PROVIDER                = getRefdataValue('Provider', RDConstants.ORGANISATIONAL_ROLE)
    public final static RefdataValue OR_PUBLISHER               = getRefdataValue('Publisher', RDConstants.ORGANISATIONAL_ROLE)
    public final static RefdataValue OR_CONTENT_PROVIDER        = getRefdataValue('Content Provider', RDConstants.ORGANISATIONAL_ROLE)
    public final static RefdataValue OR_PACKAGE_CONSORTIA       = getRefdataValue('Package Consortia', RDConstants.ORGANISATIONAL_ROLE)

    public final static RefdataValue O_SECTOR_HIGHER_EDU    = getRefdataValue('Higher Education', RDConstants.ORG_SECTOR)
    public final static RefdataValue O_SECTOR_PUBLISHER     = getRefdataValue('Publisher', RDConstants.ORG_SECTOR)

    public final static RefdataValue O_STATUS_CURRENT       = getRefdataValue('Current', RDConstants.ORG_STATUS)
    public final static RefdataValue O_STATUS_DELETED       = getRefdataValue('Deleted', RDConstants.ORG_STATUS)

    public final static RefdataValue OT_CONSORTIUM          = getRefdataValue('Consortium', RDConstants.ORG_TYPE)
    public final static RefdataValue OT_INSTITUTION         = getRefdataValue('Institution', RDConstants.ORG_TYPE)
    public final static RefdataValue OT_AGENCY              = getRefdataValue('Agency', RDConstants.ORG_TYPE)
    public final static RefdataValue OT_LICENSOR            = getRefdataValue('Licensor', RDConstants.ORG_TYPE)
    public final static RefdataValue OT_PROVIDER            = getRefdataValue('Provider', RDConstants.ORG_TYPE)
    public final static RefdataValue OT_PUBLISHER           = getRefdataValue('Publisher', RDConstants.ORG_TYPE)

    public final static RefdataValue PACKAGE_STATUS_DELETED     = getRefdataValue('Deleted', RDConstants.PACKAGE_STATUS)
    public final static RefdataValue PACKAGE_STATUS_REMOVED     = getRefdataValue('Removed', RDConstants.PACKAGE_STATUS)

    public final static RefdataValue PACKAGE_SCOPE_NATIONAL     = getRefdataValue('National', RDConstants.PACKAGE_SCOPE)

    public final static RefdataValue PENDING_CHANGE_PENDING     = getRefdataValue('Pending', RDConstants.PENDING_CHANGE_STATUS)
    public final static RefdataValue PENDING_CHANGE_ACCEPTED    = getRefdataValue('Accepted', RDConstants.PENDING_CHANGE_STATUS)
    public final static RefdataValue PENDING_CHANGE_SUPERSEDED  = getRefdataValue('Superseded', RDConstants.PENDING_CHANGE_STATUS)
    public final static RefdataValue PENDING_CHANGE_HISTORY     = getRefdataValue('History', RDConstants.PENDING_CHANGE_STATUS)
    public final static RefdataValue PENDING_CHANGE_REJECTED    = getRefdataValue('Rejected', RDConstants.PENDING_CHANGE_STATUS)

    public final static RefdataValue PENDING_CHANGE_CONFIG_ACCEPT = getRefdataValue('Accept', RDConstants.PENDING_CHANGE_CONFIG_SETTING)
    public final static RefdataValue PENDING_CHANGE_CONFIG_PROMPT = getRefdataValue('Prompt', RDConstants.PENDING_CHANGE_CONFIG_SETTING)
    public final static RefdataValue PENDING_CHANGE_CONFIG_REJECT = getRefdataValue('Reject', RDConstants.PENDING_CHANGE_CONFIG_SETTING)

    public final static RefdataValue PERM_PERM_EXPL              = getRefdataValue('Permitted (explicit)', RDConstants.PERMISSIONS)
    public final static RefdataValue PERM_PERM_INTERP            = getRefdataValue('Permitted (interpreted)',RDConstants.PERMISSIONS)
    public final static RefdataValue PERM_PROH_EXPL              = getRefdataValue('Prohibited (explicit)', RDConstants.PERMISSIONS)
    public final static RefdataValue PERM_PROH_INTERP            = getRefdataValue('Prohibited (interpreted)', RDConstants.PERMISSIONS)
    public final static RefdataValue PERM_SILENT                 = getRefdataValue('Silent', RDConstants.PERMISSIONS)
    public final static RefdataValue PERM_NOT_APPLICABLE         = getRefdataValue('Not applicable', RDConstants.PERMISSIONS)
    public final static RefdataValue PERM_UNKNOWN                = getRefdataValue('Unknown', RDConstants.PERMISSIONS)

    public final static RefdataValue PERSON_CONTACT_TYPE_PERSONAL       = getRefdataValue('Personal Contact', RDConstants.PERSON_CONTACT_TYPE)
    public final static RefdataValue PERSON_CONTACT_TYPE_FUNCTIONAL     = getRefdataValue('Functional Contact', RDConstants.PERSON_CONTACT_TYPE)

    public final static RefdataValue PLATFORM_STATUS_CURRENT    = getRefdataValue('Current', RDConstants.PLATFORM_STATUS)
    public final static RefdataValue PLATFORM_STATUS_DELETED    = getRefdataValue('Deleted', RDConstants.PLATFORM_STATUS)
    public final static RefdataValue PLATFORM_STATUS_REMOVED    = getRefdataValue('Removed', RDConstants.PLATFORM_STATUS)

    public final static RefdataValue PRS_FUNC_CONTACT_PRS               = getRefdataValue('Contact Person', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_FC_BILLING_ADDRESS        = getRefdataValue('Functional Contact Billing Adress', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_FC_DELIVERY_ADDRESS       = getRefdataValue('Functional Contact Delivery Address', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_FC_LEGAL_PATRON_ADDRESS   = getRefdataValue('Functional Contact Legal Patron Address', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_FC_LIBRARY_ADDRESS        = getRefdataValue('Functional Contact Library Address', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_FC_POSTAL_ADDRESS         = getRefdataValue('Functional Contact Postal Address', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_GASCO_CONTACT             = getRefdataValue('GASCO-Contact', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_GENERAL_CONTACT_PRS       = getRefdataValue('General contact person', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_RESPONSIBLE_ADMIN         = getRefdataValue('Responsible Admin', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_SERVICE_SUPPORT           = getRefdataValue('Service Support', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_TECHNICAL_SUPPORT         = getRefdataValue('Technical Support', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_METADATA                  = getRefdataValue('Metadata', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_OA_CONTACT                = getRefdataValue('OA contact', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_CUSTOMER_SERVICE          = getRefdataValue('Customer Service', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_SALES_MARKETING           = getRefdataValue('Sales and Marketing', RDConstants.PERSON_FUNCTION)
    public final static RefdataValue PRS_FUNC_TRAINING                  = getRefdataValue('Training', RDConstants.PERSON_FUNCTION)

    public final static RefdataValue PRS_POS_ACCOUNT        = getRefdataValue('Account Manager', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_BBL            = getRefdataValue('Bereichsbibliotheksleitung', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_DIREKTION      = getRefdataValue('Direktion', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_DIREKTION_ASS  = getRefdataValue('Direktionsassistenz', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_EA             = getRefdataValue('Erwerbungsabteilung', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_EL             = getRefdataValue('Erwerbungsleitung', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_FACHREFERAT    = getRefdataValue('Fachreferat', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_HEAD           = getRefdataValue('Head Access Services', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_LD             = getRefdataValue('Library Director', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_MB             = getRefdataValue('Medienbearbeitung', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_RB             = getRefdataValue('Rechnungsbearbeitung', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_SD             = getRefdataValue('Sales Director', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_SS             = getRefdataValue('Sales Support', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_TS             = getRefdataValue('Technical Support', RDConstants.PERSON_POSITION)
    public final static RefdataValue PRS_POS_ZA             = getRefdataValue('Zeitschriftenabteilung', RDConstants.PERSON_POSITION)

    public final static RefdataValue PRS_RESP_SPEC_LIC_EDITOR   = getRefdataValue('Specific license editor', RDConstants.PERSON_RESPONSIBILITY)
    public final static RefdataValue PRS_RESP_SPEC_PKG_EDITOR   = getRefdataValue('Specific package editor', RDConstants.PERSON_RESPONSIBILITY)
    public final static RefdataValue PRS_RESP_SPEC_SUB_EDITOR   = getRefdataValue('Specific subscription editor', RDConstants.PERSON_RESPONSIBILITY)

    public final static RefdataValue REPORTING_CONTACT_TYPE_CONTACTS    = getRefdataValue('contacts', RDConstants.REPORTING_CONTACT_TYPE)
    public final static RefdataValue REPORTING_CONTACT_TYPE_ADDRESSES   = getRefdataValue('addresses', RDConstants.REPORTING_CONTACT_TYPE)

    public final static RefdataValue READER_NUMBER_USER              = getRefdataValue('User', RDConstants.NUMBER_TYPE)
    public final static RefdataValue READER_NUMBER_PEOPLE            = getRefdataValue('Population', RDConstants.NUMBER_TYPE)
    public final static RefdataValue READER_NUMBER_SCIENTIFIC_STAFF  = getRefdataValue('Scientific staff', RDConstants.NUMBER_TYPE)
    public final static RefdataValue READER_NUMBER_STUDENTS          = getRefdataValue('Students', RDConstants.NUMBER_TYPE)
    public final static RefdataValue READER_NUMBER_FTE               = getRefdataValue('FTE', RDConstants.NUMBER_TYPE)

    public final static RefdataValue SHARE_CONF_ALL                     = getRefdataValue('everyone', RDConstants.SHARE_CONFIGURATION)
    public final static RefdataValue SHARE_CONF_UPLOADER_ORG            = getRefdataValue('only for author organisation', RDConstants.SHARE_CONFIGURATION) //maps to key, value is correct!
    public final static RefdataValue SHARE_CONF_UPLOADER_AND_TARGET     = getRefdataValue('only for author and target organisation', RDConstants.SHARE_CONFIGURATION) //maps to key, value is correct!
    public final static RefdataValue SHARE_CONF_CONSORTIUM              = getRefdataValue('only for consortia members', RDConstants.SHARE_CONFIGURATION)

    public final static RefdataValue SUBSCRIPTION_CURRENT           = getRefdataValue('Current', RDConstants.SUBSCRIPTION_STATUS)
    public final static RefdataValue SUBSCRIPTION_INTENDED          = getRefdataValue('Intended', RDConstants.SUBSCRIPTION_STATUS)
    public final static RefdataValue SUBSCRIPTION_EXPIRED           = getRefdataValue('Expired', RDConstants.SUBSCRIPTION_STATUS)
    public final static RefdataValue SUBSCRIPTION_ORDERED           = getRefdataValue('Ordered', RDConstants.SUBSCRIPTION_STATUS)
    public final static RefdataValue SUBSCRIPTION_NO_STATUS         = getRefdataValue('Status not defined', RDConstants.SUBSCRIPTION_STATUS)
    public final static RefdataValue SUBSCRIPTION_TEST_ACCESS       = getRefdataValue('Test Access', RDConstants.SUBSCRIPTION_STATUS)
    public final static RefdataValue SUBSCRIPTION_UNDER_PROCESS_OF_SELECTION     = getRefdataValue('Under Process Of Selection', RDConstants.SUBSCRIPTION_STATUS)

    public final static RefdataValue SURVEY_READY                   = getRefdataValue('Ready', RDConstants.SURVEY_STATUS)
    public final static RefdataValue SURVEY_IN_PROCESSING           = getRefdataValue('In Processing', RDConstants.SURVEY_STATUS)
    public final static RefdataValue SURVEY_IN_EVALUATION           = getRefdataValue('In Evaluation', RDConstants.SURVEY_STATUS)
    public final static RefdataValue SURVEY_COMPLETED               = getRefdataValue('Completed', RDConstants.SURVEY_STATUS)
    public final static RefdataValue SURVEY_SURVEY_STARTED          = getRefdataValue('Survey started', RDConstants.SURVEY_STATUS)
    public final static RefdataValue SURVEY_SURVEY_COMPLETED        = getRefdataValue('Survey completed', RDConstants.SURVEY_STATUS)

    public final static RefdataValue SURVEY_TYPE_RENEWAL            = getRefdataValue('renewal', RDConstants.SURVEY_TYPE)
    public final static RefdataValue SURVEY_TYPE_INTEREST           = getRefdataValue('interest', RDConstants.SURVEY_TYPE)
    public final static RefdataValue SURVEY_TYPE_TITLE_SELECTION    = getRefdataValue('selection', RDConstants.SURVEY_TYPE)
    public final static RefdataValue SURVEY_TYPE_SUBSCRIPTION       = getRefdataValue('subscription survey', RDConstants.SURVEY_TYPE)

    public final static RefdataValue SUBSCRIPTION_TYPE_LOCAL            = getRefdataValue('Local Subscription', RDConstants.SUBSCRIPTION_TYPE)
    public final static RefdataValue SUBSCRIPTION_TYPE_CONSORTIAL       = getRefdataValue('Consortial Subscription', RDConstants.SUBSCRIPTION_TYPE)
    public final static RefdataValue SUBSCRIPTION_TYPE_ADMINISTRATIVE   = getRefdataValue('Administrative Subscription', RDConstants.SUBSCRIPTION_TYPE)

    public final static RefdataValue SUBSCRIPTION_KIND_CONSORTIAL       = getRefdataValue('Consortial Subscription', RDConstants.SUBSCRIPTION_KIND)
    public final static RefdataValue SUBSCRIPTION_KIND_ALLIANCE   		= getRefdataValue('Alliance Subscription', RDConstants.SUBSCRIPTION_KIND)
    public final static RefdataValue SUBSCRIPTION_KIND_NATIONAL   		= getRefdataValue('National Subscription', RDConstants.SUBSCRIPTION_KIND)
    public final static RefdataValue SUBSCRIPTION_KIND_LOCAL            = getRefdataValue('Local Subscription', RDConstants.SUBSCRIPTION_KIND)

    public final static RefdataValue TASK_STATUS_OPEN            = getRefdataValue('Open', RDConstants.TASK_STATUS)
    public final static RefdataValue TASK_STATUS_DONE            = getRefdataValue('Done', RDConstants.TASK_STATUS)
    public final static RefdataValue TASK_STATUS_DEFERRED        = getRefdataValue('Deferred', RDConstants.TASK_STATUS)

    public final static RefdataValue TAX_TYPE_NOT_APPLICABLE    = getRefdataValue('not applicable', RDConstants.TAX_TYPE)
    public final static RefdataValue TAX_TYPE_NOT_TAXABLE       = getRefdataValue('not taxable', RDConstants.TAX_TYPE)
    public final static RefdataValue TAX_TYPE_REVERSE_CHARGE    = getRefdataValue('reverse charge', RDConstants.TAX_TYPE)
    public final static RefdataValue TAX_TYPE_TAXABLE           = getRefdataValue('taxable', RDConstants.TAX_TYPE)
    public final static RefdataValue TAX_TYPE_TAXABLE_EXEMPT    = getRefdataValue('taxable tax-exempt', RDConstants.TAX_TYPE)
    public final static RefdataValue TAX_TYPE_TAX_CONTAINED_7   = getRefdataValue('tax contained 7', RDConstants.TAX_TYPE)
    public final static RefdataValue TAX_TYPE_TAX_CONTAINED_19  = getRefdataValue('tax contained 19', RDConstants.TAX_TYPE)

    public final static RefdataValue TITLE_TYPE_EBOOK       = getRefdataValue('Book', RDConstants.TITLE_MEDIUM)
    public final static RefdataValue TITLE_TYPE_JOURNAL     = getRefdataValue('Journal', RDConstants.TITLE_MEDIUM)
    public final static RefdataValue TITLE_TYPE_DATABASE    = getRefdataValue('Database', RDConstants.TITLE_MEDIUM)

    public final static RefdataValue TITLE_STATUS_CURRENT   = getRefdataValue('Current', RDConstants.TITLE_STATUS)
    public final static RefdataValue TITLE_STATUS_RETIRED   = getRefdataValue('Retired', RDConstants.TITLE_STATUS)
    public final static RefdataValue TITLE_STATUS_DELETED   = getRefdataValue('Deleted', RDConstants.TITLE_STATUS)

    public final static RefdataValue TIPP_PAYMENT_PAID      = getRefdataValue('Paid', RDConstants.TIPP_ACCESS_TYPE)
    public final static RefdataValue TIPP_PAYMENT_FREE      = getRefdataValue('Free', RDConstants.TIPP_ACCESS_TYPE)

    public final static RefdataValue TIPP_STATUS_CURRENT            = getRefdataValue('Current', RDConstants.TIPP_STATUS)
    public final static RefdataValue TIPP_STATUS_RETIRED            = getRefdataValue('Retired', RDConstants.TIPP_STATUS)
    public final static RefdataValue TIPP_STATUS_EXPECTED           = getRefdataValue('Expected', RDConstants.TIPP_STATUS)
    public final static RefdataValue TIPP_STATUS_REMOVED            = getRefdataValue('Removed', RDConstants.TIPP_STATUS)
    public final static RefdataValue TIPP_STATUS_TRANSFERRED        = getRefdataValue('Transferred', RDConstants.TIPP_STATUS)
    public final static RefdataValue TIPP_STATUS_UNKNOWN            = getRefdataValue('Unknown', RDConstants.TIPP_STATUS)
    public final static RefdataValue TIPP_STATUS_DELETED            = getRefdataValue('Deleted',  RDConstants.TIPP_STATUS)

    public final static RefdataValue US_DASHBOARD_TAB_DUE_DATES = getRefdataValue('Due Dates', RDConstants.USER_SETTING_DASHBOARD_TAB)

    //public final static RefdataValue WF_CONDITION_STATUS_OPEN    = getRefdataValue('open', RDConstants.WF_CONDITION_STATUS)
    //public final static RefdataValue WF_CONDITION_STATUS_DONE    = getRefdataValue('done', RDConstants.WF_CONDITION_STATUS)

    public final static RefdataValue WF_TASK_PRIORITY_NORMAL     = getRefdataValue('normal', RDConstants.WF_TASK_PRIORITY)
    public final static RefdataValue WF_TASK_PRIORITY_IMPORTANT  = getRefdataValue('important', RDConstants.WF_TASK_PRIORITY)
    public final static RefdataValue WF_TASK_PRIORITY_OPTIONAL   = getRefdataValue('optional', RDConstants.WF_TASK_PRIORITY)

    public final static RefdataValue WF_TASK_STATUS_OPEN         = getRefdataValue('open', RDConstants.WF_TASK_STATUS)
    public final static RefdataValue WF_TASK_STATUS_CANCELED     = getRefdataValue('canceled', RDConstants.WF_TASK_STATUS)
    public final static RefdataValue WF_TASK_STATUS_DONE         = getRefdataValue('done', RDConstants.WF_TASK_STATUS)

    public final static RefdataValue WF_WORKFLOW_STATE_ACTIVE    = getRefdataValue('active', RDConstants.WF_WORKFLOW_STATE)
    public final static RefdataValue WF_WORKFLOW_STATE_TEST      = getRefdataValue('test', RDConstants.WF_WORKFLOW_STATE)

    public final static RefdataValue WF_WORKFLOW_TARGET_ROLE_ALL            = getRefdataValue('all', RDConstants.WF_WORKFLOW_TARGET_ROLE)
    public final static RefdataValue WF_WORKFLOW_TARGET_ROLE_CONSORTIUM     = getRefdataValue('consortium', RDConstants.WF_WORKFLOW_TARGET_ROLE)
    public final static RefdataValue WF_WORKFLOW_TARGET_ROLE_INSTITUTION    = getRefdataValue('institution', RDConstants.WF_WORKFLOW_TARGET_ROLE)

    public final static RefdataValue WF_WORKFLOW_TARGET_TYPE_AGENCY         = getRefdataValue('agency', RDConstants.WF_WORKFLOW_TARGET_TYPE)
    public final static RefdataValue WF_WORKFLOW_TARGET_TYPE_INSTITUTION    = getRefdataValue('institution', RDConstants.WF_WORKFLOW_TARGET_TYPE)
    public final static RefdataValue WF_WORKFLOW_TARGET_TYPE_LICENSE        = getRefdataValue('license', RDConstants.WF_WORKFLOW_TARGET_TYPE)
    public final static RefdataValue WF_WORKFLOW_TARGET_TYPE_OWNER          = getRefdataValue('owner', RDConstants.WF_WORKFLOW_TARGET_TYPE)
    public final static RefdataValue WF_WORKFLOW_TARGET_TYPE_PROVIDER       = getRefdataValue('provider', RDConstants.WF_WORKFLOW_TARGET_TYPE)
    public final static RefdataValue WF_WORKFLOW_TARGET_TYPE_SUBSCRIPTION   = getRefdataValue('subscription', RDConstants.WF_WORKFLOW_TARGET_TYPE)

    public final static RefdataValue WF_WORKFLOW_STATUS_OPEN     = getRefdataValue('open', RDConstants.WF_WORKFLOW_STATUS)
    public final static RefdataValue WF_WORKFLOW_STATUS_CANCELED = getRefdataValue('canceled', RDConstants.WF_WORKFLOW_STATUS)
    public final static RefdataValue WF_WORKFLOW_STATUS_DONE     = getRefdataValue('done', RDConstants.WF_WORKFLOW_STATUS)

    public final static RefdataValue YN_YES         = getRefdataValue('Yes', RDConstants.Y_N)
    public final static RefdataValue YN_NO          = getRefdataValue('No', RDConstants.Y_N)
    public final static RefdataValue YNO_YES        = getRefdataValue('Yes', RDConstants.Y_N_O)
    public final static RefdataValue YNO_NO         = getRefdataValue('No', RDConstants.Y_N_O)
    public final static RefdataValue YNO_OTHER      = getRefdataValue('Other', RDConstants.Y_N_O)
    public final static RefdataValue YNU_UNKNOWN    = getRefdataValue('Unknown', RDConstants.Y_N_U)

    // --

    static RefdataValue getRefdataValue(String value, String category) {
        RefdataValue result = RefdataValue.getByValueAndCategory(value, category)

        if (! result) {
            log.warn "No RefdataValue found for value:'${value}', category:'${category}'"
        }
        (RefdataValue) GrailsHibernateUtil.unwrapIfProxy( result)
    }
}
