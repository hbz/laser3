
## Roles and Permissions and more ..

2023-06-27

### Customer Types

#### de.laser.Org

    String getCustomerType ()
    String getCustomerTypeI10n ()

    boolean isCustomerType_Basic ()
    boolean isCustomerType_Pro ()

    boolean isCustomerType_Inst ()
    boolean isCustomerType_Consortium ()

    boolean isCustomerType_Inst_Basic ()
    boolean isCustomerType_Inst_Pro ()
    boolean isCustomerType_Consortium_Basic ()
    boolean isCustomerType_Consortium_Pro ()

#### de.laser.CustomerTypeService

    static final String ORG_INST_BASIC           = 'ORG_INST_BASIC'
    static final String ORG_INST_PRO             = 'ORG_INST_PRO'
    static final String ORG_CONSORTIUM_BASIC     = 'ORG_CONSORTIUM_BASIC'
    static final String ORG_CONSORTIUM_PRO       = 'ORG_CONSORTIUM_PRO'

    static final String PERMS_BASIC              = 'ORG_INST_BASIC,ORG_CONSORTIUM_BASIC'
    static final String PERMS_PRO                = 'ORG_INST_PRO,ORG_CONSORTIUM_PRO'

    static final String PERMS_INST_BASIC_CONSORTIUM_PRO  = 'ORG_INST_BASIC,ORG_CONSORTIUM_PRO'
    static final String PERMS_INST_PRO_CONSORTIUM_BASIC  = 'ORG_INST_PRO,ORG_CONSORTIUM_BASIC'

    boolean isConsortium (String customerType)


### Various

#### de.laser.Org

    boolean hasInstAdmin ()
    boolean hasInstAdminEnabled ()
