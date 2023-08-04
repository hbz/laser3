
## Roles and Permissions and more ..

2023-08-04

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



### Permission Checks (formal)

#### de.laser.ContextService

    boolean isInstUser_or_ROLEADMIN ()
    boolean isInstEditor_or_ROLEADMIN ()
    boolean isInstAdm_or_ROLEADMIN ()

    boolean hasPermAsInstUser_or_ROLEADMIN (String orgPerms)
    boolean hasPermAsInstEditor_or_ROLEADMIN (String orgPerms)
    boolean hasPermAsInstAdm_or_ROLEADMIN (String orgPerms)



### Affiliation Checks

#### de.laser.UserService

    boolean hasAffiliation_or_ROLEADMIN (User userToCheck, Org orgToCheck, String instUserRole)

    boolean hasFormalAffiliation (User userToCheck, Org orgToCheck, String instUserRole)
    boolean hasFormalAffiliation_or_ROLEADMIN (User userToCheck, Org orgToCheck, String instUserRole)



### Various

#### de.laser.auth.User

    Org formalOrg
    Role formalRole

    boolean isFormal (Role role)
    boolean isFormal (Org org)
    boolean isFormal (Role role, Org org)

    boolean isLastInstAdminOf (Org org)
    boolean isAdmin ()
    boolean isYoda ()

#### de.laser.Org

    boolean hasInstAdmin ()
    boolean hasInstAdminEnabled ()

#### de.laser.ContextService

    User getUser ()
    Org getOrg ()

#### de.laser.UserService

    void setAffiliation (User user, Serializable formalOrgId, Serializable formalRoleId, FlashScope flash)
