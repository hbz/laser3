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

    static final OR_TYPE_CONSORTIUM         = getRefdataValue('Consortium', 'OrgRoleType')
    static final OR_TYPE_INSTITUTION         = getRefdataValue('Institution', 'OrgRoleType')
    static final OR_TYPE_PROVIDER   = getRefdataValue('Provider', 'OrgRoleType')
    static final OR_TYPE_AGENCY     = getRefdataValue('Agency', 'OrgRoleType')

    static final SUBSCRIPTION_DELETED  = getRefdataValue('Deleted', 'Subscription Status')
    static final SUBSCRIPTION_CURRENT  = getRefdataValue('Current', 'Subscription Status')
    static final SUBSCRIPTION_INTENDED = getRefdataValue('Intended', 'Subscription Status')
    static final SUBSCRIPTION_EXPIRED  = getRefdataValue('Expired', 'Subscription Status')
  
    static final LICENSE_DELETED = getRefdataValue('Deleted', 'License Status')

    static final LINKTYPE_FOLLOWS = getRefdataValue('follows','Link Type')

    static final YN_YES = getRefdataValue('Yes','YN')
    static final YNO_YES = getRefdataValue('Yes','YNO')
    static final YN_NO  = getRefdataValue('No','YN')
    static final YNO_NO = getRefdataValue('No','YNO')
    static final YNO_OTHER = getRefdataValue('Other','YNO')

    static final CIEC_POSITIVE  = getRefdataValue('positive','Cost configuration')
    static final CIEC_NEGATIVE  = getRefdataValue('negative','Cost configuration')
    static final CIEC_NEUTRAL   = getRefdataValue('neutral','Cost configuration')

    static final PERM_PERM_EXPL = getRefdataValue('Permitted (explicit)', 'Permissions')
    static final PERM_PERM_INTERP = getRefdataValue('Permitted (interpreted)','Permissions')
    static final PERM_PROH_EXPL = getRefdataValue('Prohibited (explicit)','Permissions')
    static final PERM_PROH_INTERP = getRefdataValue('Prohibited (interpreted)','Permissions')
    static final PERM_SILENT = getRefdataValue('Silent','Permissions')
    static final PERM_NOT_APPLICABLE = getRefdataValue('Not applicable','Permissions')
    static final PERM_UNKNOWN = getRefdataValue('Unknown','Permissions')

    static RefdataValue getRefdataValue(String value, String category) {
        (RefdataValue) GrailsHibernateUtil.unwrapIfProxy( RefdataValue.getByValueAndCategory(value, category))
    }
}
