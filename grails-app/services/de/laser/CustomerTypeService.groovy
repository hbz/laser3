package de.laser

import grails.gorm.transactions.Transactional

@Transactional
class CustomerTypeService {

    public static final String ORG_INST_BASIC           = 'ORG_INST_BASIC'
    public static final String ORG_INST_PRO             = 'ORG_INST_PRO'
    public static final String ORG_CONSORTIUM_BASIC     = 'ORG_CONSORTIUM_BASIC'
    public static final String ORG_CONSORTIUM_PRO       = 'ORG_CONSORTIUM_PRO'

    public static final String PERMS_BASIC              = 'ORG_INST_BASIC,ORG_CONSORTIUM_BASIC'
    public static final String PERMS_PRO                = 'ORG_INST_PRO,ORG_CONSORTIUM_PRO'

    public static final String PERMS_INST_BASIC_CONSORTIUM_PRO  = 'ORG_INST_BASIC,ORG_CONSORTIUM_PRO'
    public static final String PERMS_INST_PRO_CONSORTIUM_BASIC  = 'ORG_INST_PRO,ORG_CONSORTIUM_BASIC'

    // -- string parsing --

    boolean isConsortium(String customerType) {
        customerType == ORG_CONSORTIUM_BASIC || customerType == ORG_CONSORTIUM_PRO
    }
//    boolean isInstBasicOrConsortiumPro(String customerType) {
//        customerType == ORG_INST_BASIC || customerType == ORG_CONSORTIUM_PRO
//    }
//    boolean isInstProOrConsortiumBasic(String customerType) {
//        customerType == ORG_INST_PRO || customerType == ORG_CONSORTIUM_BASIC
//    }

    // -- oss customer type --

//    boolean isCustomerType_Basic(Org org) {
//        org.getCustomerType() in [ ORG_INST_BASIC, ORG_CONSORTIUM_BASIC ]
//    }
//    boolean isCustomerType_Pro(Org org) {
//        org.getCustomerType() in [ ORG_INST_PRO, ORG_CONSORTIUM_PRO ]
//    }
//    boolean isCustomerType_Inst(Org org) {
//        org.getCustomerType() in [ ORG_INST_BASIC, ORG_INST_PRO ]
//    }
//    boolean isCustomerType_Consortium(Org org) {
//        org.getCustomerType() in [ ORG_CONSORTIUM_BASIC, ORG_CONSORTIUM_PRO ]
//    }
}
