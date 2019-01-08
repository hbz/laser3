package de.laser.helper

import com.k_int.kbplus.RefdataValue

class RDStore {

    static final OR_LICENSING_CONSORTIUM = RefdataValue.getByValueAndCategory('Licensing Consortium', 'Organisational Role')
    static final OR_LICENSEE = RefdataValue.getByValueAndCategory('Licensee','Organisational Role')
    static final OR_LICENSEE_CONS = RefdataValue.getByValueAndCategory('Licensee_Consortial','Organisational Role')
    static final OR_SUBSCRIPTION_CONSORTIA = RefdataValue.getByValueAndCategory('Subscription Consortia','Organisational Role')
    static final OR_SUBSCRIBER = RefdataValue.getByValueAndCategory('Subscriber','Organisational Role')
    static final OR_SUBSCRIBER_CONS = RefdataValue.getByValueAndCategory('Subscriber_Consortial','Organisational Role')
    static final OR_TYPE_CONSORTIUM = RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')

    static final ORT_PROVIDER = RefdataValue.getByValueAndCategory('Provider', 'OrgRoleType')
    static final ORT_AGENCY = RefdataValue.getByValueAndCategory('Agency', 'OrgRoleType')

    static final SUBSCRIPTION_DELETED = RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status')
    static final SUBSCRIPTION_CURRENT = RefdataValue.getByValueAndCategory('Current', 'Subscription Status')
    static final SUBSCRIPTION_INTENDED = RefdataValue.getByValueAndCategory('Intended','Subscription Status')
    static final SUBSCRIPTION_EXPIRED =  RefdataValue.getByValueAndCategory('Expired','Subscription Status')
    static final LICENSE_DELETED = RefdataValue.getByValueAndCategory('Deleted', 'License Status')

    static final YN_YES = RefdataValue.getByValueAndCategory('Yes','YN')
    static final YN_NO = RefdataValue.getByValueAndCategory('No','YN')

    static final CIEC_POSITIVE = RefdataValue.getByValueAndCategory('positive','Cost configuration')
    static final CIEC_NEGATIVE = RefdataValue.getByValueAndCategory('negative','Cost congifuration')
    static final CIEC_NEUTRAL = RefdataValue.getByValueAndCategory('neutral','Cost configuration')

}
