package de.laser

import grails.gorm.transactions.Transactional

@Transactional
class CustomerTypeService {

    // used to declare customer types or check granted permissions

    public static final String ORG_INST_BASIC           = 'ORG_INST_BASIC'
    public static final String ORG_INST_PRO             = 'ORG_INST_PRO'
    public static final String ORG_CONSORTIUM_BASIC     = 'ORG_CONSORTIUM_BASIC'
    public static final String ORG_CONSORTIUM_PRO       = 'ORG_CONSORTIUM_PRO'

    public static final String PERMS_PRO                        = 'ORG_INST_PRO,ORG_CONSORTIUM_PRO'
    public static final String PERMS_INST_PRO_CONSORTIUM_BASIC  = 'ORG_INST_PRO,ORG_CONSORTIUM_BASIC'

    // -- string parsing --

    boolean isConsortium(String customerType) {
        customerType == ORG_CONSORTIUM_BASIC || customerType == ORG_CONSORTIUM_PRO
    }
}
