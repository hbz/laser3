package de.laser

import grails.gorm.transactions.Transactional

@Transactional
class CustomerTypeService {

    public static final String ORG_BASIC                = 'ORG_BASIC'
    public static final String ORG_PRO                  = 'ORG_PRO'
    public static final String ORG_CONSORTIUM_BASIC     = 'ORG_CONSORTIUM_BASIC'
    public static final String ORG_CONSORTIUM_PRO       = 'ORG_CONSORTIUM_PRO'

    public static final String PERMS_BASIC                      = 'ORG_BASIC,ORG_CONSORTIUM_BASIC'
    public static final String PERMS_PRO                        = 'ORG_PRO,ORG_CONSORTIUM_PRO'

    public static final String PERMS_ORG_PRO_CONSORTIUM_BASIC   = 'ORG_PRO,ORG_CONSORTIUM_BASIC'

//    boolean isCustomerType_Basic(Org org) {
//        org.getCustomerType() in [ ORG_BASIC, ORG_CONSORTIUM_BASIC ]
//    }
//    boolean isCustomerType_Pro(Org org) {
//        org.getCustomerType() in [ ORG_PRO, ORG_CONSORTIUM_PRO ]
//    }
//    boolean isCustomerType_Inst(Org org) {
//        org.getCustomerType() in [ ORG_BASIC, ORG_PRO ]
//    }
//    boolean isCustomerType_Consortium(Org org) {
//        org.getCustomerType() in [ ORG_CONSORTIUM_BASIC, ORG_CONSORTIUM_PRO ]
//    }
}
