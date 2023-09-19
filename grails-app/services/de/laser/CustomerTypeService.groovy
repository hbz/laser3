package de.laser

import grails.gorm.transactions.Transactional

/**
 * This service contains the currently available customer types.
 * Only institutions can have a customer types; that distinguishes them from providers while both are organisations.
 * The hierarchy is as follows:
 * <ul>
 *     <li>basic customers with limited functionality</li>
 *     <li>pro customers with limited functionality</li>
 * </ul>
 * then
 * <ul>
 *     <li>(single) institutions</li>
 *     <li>consortia</li>
 * </ul>
 * Institutions have another range of functionality than consortia who need to manage bulk subscriptions and the communication with many institutions at once
 * @see OrgSetting
 * @see Org
 */
@Transactional
class CustomerTypeService {

    ContextService contextService

    // used to declare customer types or check granted permissions

    public static final String ORG_INST_BASIC           = 'ORG_INST_BASIC'
    public static final String ORG_INST_PRO             = 'ORG_INST_PRO'
    public static final String ORG_CONSORTIUM_BASIC     = 'ORG_CONSORTIUM_BASIC'
    public static final String ORG_CONSORTIUM_PRO       = 'ORG_CONSORTIUM_PRO'

    public static final String ORG_SUPPORT              = 'ORG_SUPPORT'

    // perm lists

    public static final String PERMS_PRO                        = 'ORG_INST_PRO,ORG_CONSORTIUM_PRO'
    public static final String PERMS_INST_PRO_CONSORTIUM_BASIC  = 'ORG_INST_PRO,ORG_CONSORTIUM_BASIC'

    // + hbz

    public static final String PERMS_PRO_SUPPORT                = 'ORG_INST_PRO,ORG_CONSORTIUM_PRO,ORG_SUPPORT'

    // -- string parsing --

    /**
     * Checks if the given customer type belongs to the consortium types
     * @param customerType the customer type string to check
     * @return true if the given type is one of {@link #ORG_CONSORTIUM_BASIC} or {@link #ORG_CONSORTIUM_PRO}
     */
    boolean isConsortium(String customerType) {
        customerType == ORG_CONSORTIUM_BASIC || customerType == ORG_CONSORTIUM_PRO
    }

    //

    String getActionsTemplatePath() {
        return contextService.getOrg().isCustomerType_Support() ? 'actions_support' : 'actions'
    }

    String getNavTemplatePath() {
        return contextService.getOrg().isCustomerType_Support() ? 'nav_support' : 'nav'
    }
}
