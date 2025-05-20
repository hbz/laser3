package de.laser

import de.laser.auth.Role
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

    //

    List<Role> getOrgInstRoles() {
        [Role.findByAuthority(ORG_INST_BASIC), Role.findByAuthority(ORG_INST_PRO)]
    }

    /**
     * Determines whether the view to be rendered is the normal version or the version for support type customers
     * @param view the view to render
     * @return the view path, suffixed with "_support" if the context institution is a support type customer
     * @see #ORG_SUPPORT
     */
    String getCustomerTypeDependingView(String view) {
        contextService.getOrg().isCustomerType_Support() ? view + '_support' : view
    }

    /**
     * Gets all institutions matching the given customer type
     * @param customerType the customer type (see the constants defined in this service) to be retrieved
     * @return a {@link List} of matching institutions ({@link Org})
     */
    List<Org> getAllOrgsByCustomerType(String customerType) {
        Role role = Role.findByAuthority(customerType)
        if (role) {
            OrgSetting.executeQuery("select os.org from OrgSetting as os where os.key = :key and os.roleValue = :role order by os.org.sortname, os.org.name",
                [key: OrgSetting.KEYS.CUSTOMER_TYPE, role: role]
            )
        }
        else {
            []
        }
    }

    //

    /**
     * Gets the template path for the actions fragment, depending on the context institution's customer type
     * @return the appropriate actions template path
     */
    String getActionsTemplatePath() {
        getCustomerTypeDependingView('actions')
    }

    /**
     * Gets the template path for the navigation fragment, depending on the context institution's customer type
     * @return the appropriate navigation template path
     */
    String getNavTemplatePath() {
        getCustomerTypeDependingView('nav')
    }

    /**
     * Gets the template path for the license filter fragment, depending on the context institution's customer type
     * @return the appropriate license filter path
     */
    String getLicenseFilterTemplatePath() {
        getCustomerTypeDependingView('/templates/license/licenseFilter')
    }

    /**
     * Gets the template path for the subscription filter fragment, depending on the context institution's customer type
     * @return the appropriate subscription filter path
     */
    String getSubscriptionFilterTemplatePath() {
        getCustomerTypeDependingView('/templates/subscription/subscriptionFilter')
    }

    /**
     * Gets the template path for the consortium subscription filter fragment, depending on the context consortium's customer type
     * @return the appropriate consortium subscription filter path
     */
    String getConsortiaSubscriptionFilterTemplatePath() {
        getCustomerTypeDependingView('/templates/subscription/consortiaSubscriptionFilter')
    }

    /**
     * Gets the template path for the subscription management fragment, depending on the context institution's customer type
     * @return the appropriate subscription management path
     */
    String getNavSubscriptionManagementTemplatePath() {
        getCustomerTypeDependingView('/templates/management/navSubscriptionManagement')
    }
}
