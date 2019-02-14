package de.laser.helper

import com.k_int.kbplus.RefdataValue
import de.laser.interfaces.Permissions
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@CompileStatic
class RDStore {

    static final OR_LICENSING_CONSORTIUM    = getRefdataValue('Licensing Consortium', 'Organisational Role')
    static final OR_LICENSEE                = getRefdataValue('Licensee','Organisational Role')
    static final OR_LICENSEE_CONS           = getRefdataValue('Licensee_Consortial','Organisational Role')
    static final OR_SUBSCRIPTION_CONSORTIA  = getRefdataValue('Subscription Consortia','Organisational Role')
    static final OR_SUBSCRIBER              = getRefdataValue('Subscriber','Organisational Role')
    static final OR_SUBSCRIBER_CONS         = getRefdataValue('Subscriber_Consortial','Organisational Role')
    static final OR_PROVIDER                = getRefdataValue('Provider', 'Organisational Role')
    static final OR_AGENCY                  = getRefdataValue('Agency', 'Organisational Role')

    static final OR_TYPE_CONSORTIUM         = getRefdataValue('Consortium', 'OrgRoleType')
    static final OR_TYPE_INSTITUTION        = getRefdataValue('Institution', 'OrgRoleType')
    static final OR_TYPE_PROVIDER           = getRefdataValue('Provider', 'OrgRoleType')
    static final OR_TYPE_AGENCY             = getRefdataValue('Agency', 'OrgRoleType')

    static final O_SECTOR_HIGHER_EDU        = getRefdataValue('Higher Education', 'OrgSector')
    static final O_SECTOR_PUBLISHER         = getRefdataValue('Publisher', 'OrgSector')

    static final O_TYPE_CONSORTIUM          = getRefdataValue('Consortium','OrgType')
    static final O_TYPE_INSTITUTION         = getRefdataValue('Institution','OrgType')
    static final O_TYPE_PUBLISHER           = getRefdataValue('Publisher','OrgType')
    static final O_TYPE_PROVIDER            = getRefdataValue('Provider', 'OrgType')
    static final O_TYPE_OTHER               = getRefdataValue('Other', 'OrgType')

    static final LICENSE_DELETED            = getRefdataValue('Deleted', 'License Status')
    static final ORG_DELETED                = getRefdataValue('Deleted', 'OrgStatus')
    static final PACKAGE_DELETED            = getRefdataValue('Deleted', 'Package Status')
    static final IE_DELETED                 = getRefdataValue('Deleted', 'Entitlement Issue Status')

    static final SUBSCRIPTION_DELETED       = getRefdataValue('Deleted', 'Subscription Status')
    static final SUBSCRIPTION_CURRENT       = getRefdataValue('Current', 'Subscription Status')
    static final SUBSCRIPTION_INTENDED      = getRefdataValue('Intended', 'Subscription Status')
    static final SUBSCRIPTION_EXPIRED       = getRefdataValue('Expired', 'Subscription Status')

    static final SUBSCRIPTION_TYPE_LOCAL_LICENSE      = getRefdataValue('Local Licence', 'Subscription Type')
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

    static final PRS_FUNC_GENERAL_CONTACT_PRS = getRefdataValue('General contact person', 'Person Function')
    static final CCT_EMAIL                  = getRefdataValue('E-Mail','ContactContentType')
    static final PRS_RESP_SPEC_SUB_EDITOR   = getRefdataValue('Specific subscription editor', 'Person Responsibility')

    static final PENDING_CHANGE_STATUS      = getRefdataValue('Pending', 'PendingChangeStatus')

    static final TASK_STATUS_DONE           =  getRefdataValue('Done', 'Task Status')


    static RefdataValue getRefdataValue(String value, String category) {
        (RefdataValue) GrailsHibernateUtil.unwrapIfProxy( RefdataValue.getByValueAndCategory(value, category))
    }
}
