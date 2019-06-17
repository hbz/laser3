package de.laser.helper

import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.RefdataValue
import de.laser.interfaces.Permissions
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@CompileStatic
class RDStore {

    static final GENERIC_NULL_VALUE         = getRefdataValue('generic.null.value','filter.fake.values')

    static final OR_LICENSING_CONSORTIUM    = getRefdataValue('Licensing Consortium', 'Organisational Role')
    static final OR_LICENSEE                = getRefdataValue('Licensee','Organisational Role')
    static final OR_LICENSEE_CONS           = getRefdataValue('Licensee_Consortial','Organisational Role')

    static final OR_SUBSCRIPTION_CONSORTIA  = getRefdataValue('Subscription Consortia','Organisational Role')
    static final OR_SUBSCRIBER              = getRefdataValue('Subscriber','Organisational Role')
    static final OR_SUBSCRIBER_CONS         = getRefdataValue('Subscriber_Consortial','Organisational Role')
    static final OR_SUBSCRIBER_CONS_HIDDEN  = getRefdataValue('Subscriber_Consortial_Hidden','Organisational Role')

    static final OR_AGENCY                  = getRefdataValue('Agency', 'Organisational Role')
    static final OR_LICENSOR                = getRefdataValue('Licensor','Organisational Role')
    static final OR_PROVIDER                = getRefdataValue('Provider', 'Organisational Role')
    static final OR_CONTENT_PROVIDER        = getRefdataValue('Content Provider', 'Organisational Role')

    static final OT_CONSORTIUM              = getRefdataValue('Consortium', 'OrgRoleType')
    static final OT_INSTITUTION             = getRefdataValue('Institution', 'OrgRoleType')
    static final OT_AGENCY                  = getRefdataValue('Agency', 'OrgRoleType')
    static final OT_LICENSOR                = getRefdataValue('Licensor', 'OrgRoleType')
    static final OT_PROVIDER                = getRefdataValue('Provider', 'OrgRoleType')
    static final OT_DEPARTMENT              = getRefdataValue('Department','OrgRoleType')

    static final O_SECTOR_HIGHER_EDU        = getRefdataValue('Higher Education', 'OrgSector')
    static final O_SECTOR_PUBLISHER         = getRefdataValue('Publisher', 'OrgSector')

    static final O_STATUS_CURRENT           = getRefdataValue('Current','OrgStatus')
    static final O_STATUS_DELETED           = getRefdataValue('Deleted','OrgStatus')

    static final DOC_DELETED                = getRefdataValue('Deleted', 'Document Context Status')
    static final IE_DELETED                 = getRefdataValue('Deleted', 'Entitlement Issue Status')
    static final LICENSE_DELETED            = getRefdataValue('Deleted', 'License Status')
    static final ORG_DELETED                = getRefdataValue('Deleted', 'OrgStatus')
    static final PACKAGE_DELETED            = getRefdataValue('Deleted', 'Package Status')
    static final PLATFORM_DELETED           = getRefdataValue('Deleted', 'Platform Status')
    static final TIPP_DELETED               = getRefdataValue('Deleted', 'TIPP Status')

    static final SUBSCRIPTION_DELETED       = getRefdataValue('Deleted', 'Subscription Status')
    static final SUBSCRIPTION_CURRENT       = getRefdataValue('Current', 'Subscription Status')
    static final SUBSCRIPTION_INTENDED      = getRefdataValue('Intended', 'Subscription Status')
    static final SUBSCRIPTION_EXPIRED       = getRefdataValue('Expired', 'Subscription Status')
    static final SUBSCRIPTION_NO_STATUS     = getRefdataValue('subscription.status.no.status.set.but.null','filter.fake.values')

    static final SURVEY_READY               = getRefdataValue('Ready', 'Survey Status')
    static final SURVEY_IN_PROCESSING       = getRefdataValue('In Processing', 'Survey Status')
    static final SURVEY_IN_EVALUATION       = getRefdataValue('In Evaluation', 'Survey Status')
    static final SURVEY_COMPLETED           = getRefdataValue('Completed', 'Survey Status')
    static final SURVEY_SURVEY_STARTED      = getRefdataValue('Survey started', 'Survey Status')
    static final SURVEY_SURVEY_COMPLETED    = getRefdataValue('Survey completed', 'Survey Status')

    static final LICENSE_CURRENT            = getRefdataValue('Current','License Status')

    static final COST_ITEM_ACTUAL           = getRefdataValue('Actual','CostItemStatus')
    static final COST_ITEM_DELETED          = getRefdataValue('Deleted','CostItemStatus')

    static final SUBSCRIPTION_TYPE_LOCAL = getRefdataValue('Local Licence', 'Subscription Type')
    static final SUBSCRIPTION_TYPE_CONSORTIAL = getRefdataValue('Consortial Licence', 'Subscription Type')
    static final SUBSCRIPTION_TYPE_ADMINISTRATIVE = getRefdataValue('Administrative Subscription','Subscription Type')

    static final LICENSE_TYPE_TEMPLATE      = getRefdataValue('Template', 'License Type')

    static final LINKTYPE_FOLLOWS           = getRefdataValue('follows','Link Type')

    static final YN_YES                     = getRefdataValue('Yes','YN')
    static final YNO_YES                    = getRefdataValue('Yes','YNO')
    static final YN_NO                      = getRefdataValue('No','YN')
    static final YNO_NO                     = getRefdataValue('No','YNO')
    static final YNO_OTHER                  = getRefdataValue('Other','YNO')

    static final CIEC_POSITIVE              = getRefdataValue('positive','Cost configuration')
    static final CIEC_NEGATIVE              = getRefdataValue('negative','Cost configuration')
    static final CIEC_NEUTRAL               = getRefdataValue('neutral','Cost configuration')

    static final PERM_PERM_EXPL             = getRefdataValue('Permitted (explicit)', 'Permissions')
    static final PERM_PERM_INTERP           = getRefdataValue('Permitted (interpreted)','Permissions')
    static final PERM_PROH_EXPL             = getRefdataValue('Prohibited (explicit)','Permissions')
    static final PERM_PROH_INTERP           = getRefdataValue('Prohibited (interpreted)','Permissions')
    static final PERM_SILENT                = getRefdataValue('Silent','Permissions')
    static final PERM_NOT_APPLICABLE        = getRefdataValue('Not applicable','Permissions')
    static final PERM_UNKNOWN               = getRefdataValue('Unknown','Permissions')

    static final TITLE_TYPE_EBOOK           = getRefdataValue('EBook','Title Type')
    static final TITLE_TYPE_JOURNAL         = getRefdataValue('Journal','Title Type')
    static final TITLE_TYPE_DATABASE        = getRefdataValue('Database','Title Type')

    static final COMBO_TYPE_CONSORTIUM      = getRefdataValue('Consortium','Combo Type')
    static final COMBO_TYPE_DEPARTMENT      = getRefdataValue('Department','Combo Type')

    static final CONTACT_TYPE_PERSONAL      = getRefdataValue('Personal Contact','Person Contact Type')
    static final CONTACT_TYPE_FUNCTIONAL    = getRefdataValue('Functional Contact','Person Contact Type')

    static final TIPP_PAYMENT_COMPLIMENTARY     = getRefdataValue('Complimentary','TitleInstancePackagePlatform.PaymentType')
    static final TIPP_PAYMENT_LIMITED_PROMOTION = getRefdataValue('Limited Promotion','TitleInstancePackagePlatform.PaymentType')
    static final TIPP_PAYMENT_PAID              = getRefdataValue('Paid','TitleInstancePackagePlatform.PaymentType')
    static final TIPP_PAYMENT_OA                = getRefdataValue('OA','TitleInstancePackagePlatform.PaymentType')
    static final TIPP_PAYMENT_OPT_OUT_PROMOTION = getRefdataValue('Opt Out Promotion','TitleInstancePackagePlatform.PaymentType')
    static final TIPP_PAYMENT_UNCHARGED         = getRefdataValue('Uncharged','TitleInstancePackagePlatform.PaymentType')
    static final TIPP_PAYMENT_UNKNOWN           = getRefdataValue('Unknown','TitleInstancePackagePlatform.PaymentType')

    static final TIPP_STATUS_CURRENT            = getRefdataValue('Current', 'TIPP Status')
    static final TIPP_STATUS_EXPECTED           = getRefdataValue('Expected','TIPP Status')
    static final TIPP_STATUS_DELETED            = getRefdataValue('Deleted', 'TIPP Status')
    static final TIPP_STATUS_TRANSFERRED        = getRefdataValue('Transferred', 'TIPP Status')
    static final TIPP_STATUS_UNKNOWN            = getRefdataValue('Unknown', 'TIPP Status')

    static final PRS_FUNC_GENERAL_CONTACT_PRS = getRefdataValue('General contact person', 'Person Function')
    static final CCT_EMAIL                  = getRefdataValue('E-Mail','ContactContentType')
    static final CCT_PHONE                  = getRefdataValue('Phone','ContactContentType')
    static final PRS_RESP_SPEC_SUB_EDITOR   = getRefdataValue('Specific subscription editor', 'Person Responsibility')

    static final PENDING_CHANGE_STATUS      = getRefdataValue('Pending', 'PendingChangeStatus')

    static final TASK_STATUS_DONE           = getRefdataValue('Done', 'Task Status')

    static final SHARE_CONF_ALL                 = getRefdataValue('everyone','Share Configuration')
    static final SHARE_CONF_CREATOR             = getRefdataValue('only for creator','Share Configuration')
    static final SHARE_CONF_UPLOADER_ORG        = getRefdataValue('only for author organisation','Share Configuration') //maps to key, value is correct!
    static final SHARE_CONF_UPLOADER_AND_TARGET = getRefdataValue('only for author and target organisation','Share Configuration') //maps to key, value is correct!
    static final SHARE_CONF_CONSORTIUM          = getRefdataValue('only for consortia members','Share Configuration')

    static RefdataValue getRefdataValue(String value, String category) {
        (RefdataValue) GrailsHibernateUtil.unwrapIfProxy( RefdataValue.getByValueAndCategory(value, category))
    }
}
