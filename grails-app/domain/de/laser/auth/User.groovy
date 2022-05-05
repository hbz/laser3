package de.laser.auth

import de.laser.Org
import de.laser.UserSetting
import de.laser.storage.BeanStore
import grails.plugin.springsecurity.SpringSecurityUtils

import javax.persistence.Transient

/**
 * A user in the system
 */
//@GrailsCompileStatic
//@EqualsAndHashCode(includes='username')
//@ToString(includes='username', includeNames=true, includePackage=false)
class User {

    String username
    String display
    String password
    String email
    String image

    String shibbScope
    Date dateCreated
    Date lastUpdated

    boolean enabled         = false
    boolean accountExpired  = false
    boolean accountLocked   = false
    boolean passwordExpired = false

    static hasMany      = [ affiliations: UserOrg, roles: UserRole ]
    static mappedBy     = [ affiliations: 'user',  roles: 'user' ]

    static constraints = {
        username    blank: false, unique: true
        password    blank: false, password: true
        display     blank: true, nullable: true
        email       blank: true, nullable: true
        image       blank: true, nullable: true
        shibbScope  blank: true, nullable: true
    }

    static transients = [
            'displayName', 'defaultPageSize', 'defaultPageSizeAsInteger',
            'authorizedAffiliations', 'authorizedOrgs', 'authorizedOrgsIds',
            'admin', 'yoda', 'lastInstAdmin'
    ] // mark read-only accessor methods

    static mapping = {
        cache           true
	    table           name: '`user`'
	    password        column: '`password`'

        affiliations    batchSize: 10
        roles           batchSize: 10
    }

    /**
     * Retrieves a {@link Set} of global {@link Role}s assigned to the user.
     * Note that they are not the affiliations (= {@link UserOrg}s) a user may have and which ensure institution-regulated permissions!
     * @return the user's {@link Role}s
     */
    Set<Role> getAuthorities() {
        (UserRole.findAllByUser(this) as List<UserRole>)*.role as Set<Role>
    }

    void beforeInsert() {
        encodePassword()
    }

    void beforeUpdate() {
        if (isDirty('password')) {
            encodePassword()
        }
    }

    /**
     * Gets a {@link UserSetting} with the given key (of the enum {@link UserSetting.KEYS}), creating new one (with value) if not existing
     * @param key the user setting key to look for
     * @param defaultValue the default value to set for the setting if it is not existing
     * @return the user's {@link UserSetting} responding to the given key (created or retrieved)
     */
    UserSetting getSetting(UserSetting.KEYS key, def defaultValue) {
        def us = UserSetting.get(this, key)
        (us == UserSetting.SETTING_NOT_FOUND) ? UserSetting.add(this, key, defaultValue) : (UserSetting) us
    }

    /**
     * Gets VALUE of {@link UserSetting} for the given key (of enum {@link UserSetting.KEYS}), creating new {@link UserSetting} (with value) if not existing
     * @param key the user setting key to look for
     * @param defaultValue the default value to set for the setting if it is not existing
     * @return the value of the {@link UserSetting.KEYS} setting key
     */
    def getSettingsValue(UserSetting.KEYS key, def defaultValue) {
        UserSetting setting = getSetting(key, defaultValue)
        setting.getValue()
    }

    /**
     * Gets VALUE of {@link UserSetting} for the given key (of enum {@link UserSetting.KEYS}), creating new {@link UserSetting} without value if not existing
     * @param key the user setting key to look for
     * @return the value of the {@link UserSetting.KEYS} setting key
     */
    def getSettingsValue(UserSetting.KEYS key) {
        getSettingsValue(key, null)
    }

    /**
     * Gets the default count of entries per page for the given user
     * @return the number of entries per page
     */
    long getDefaultPageSize() {
        // create if no setting found
        UserSetting setting = getSetting(UserSetting.KEYS.PAGE_SIZE, 10)
        // if setting exists, but null value - fallback
        long value = setting.getValue() ?: 10
        return value
    }

    /**
     * Same as {@link #getDefaultPageSize}, but converts the result to an integer. This is needed for certain list operations
     * @return the number of entries per page as int
     */
    int getDefaultPageSizeAsInteger() {
        long value = getDefaultPageSize()
        return value.intValue()
    }

    /**
     * Gets the user's name to display
     * @return display, username if display is null
     */
    @Transient
    String getDisplayName() {
        display ? display : username
    }

    /**
     * Encodes the submitted password
     */
    protected void encodePassword() {
        password = BeanStore.getSpringSecurityService().encodePassword(password)
    }

    // TODO -> rename to getAffiliations() -> remove
    /**
     * Gets all affiliations the user is authorised to
     * @return unlike {@link #getAuthorizedOrgs}, this method delivers a {@link List} of {@link UserOrg} entries
     */
    List<UserOrg> getAuthorizedAffiliations() {
        UserOrg.findAllByUser(this)
    }

    /**
     * Gets a list of all {@link Org}s for which this user has affiliations
     * @return a {@link List} of authorised {@link Org}s
     */
    // TODO -> rename to getAffiliationOrgs()
    List<Org> getAuthorizedOrgs() {
        String qry = "select distinct(o) from Org as o where exists ( select uo from UserOrg as uo where uo.org = o and uo.user = :user ) order by o.name"
        Org.executeQuery(qry, [user: this])
    }

    /**
     * Same as {@link #getAuthorizedOrgs}, but only the IDs of the {@link Org}s are being collected
     * @return a {@link List} of org IDs
     */
    // TODO -> rename to getAffiliationOrgIds()
    List<Long> getAuthorizedOrgsIds() {
        getAuthorizedOrgs().collect{ it.id }
    }

    /**
     * Checks whether the user is authorised for the given {@link Org}
     * @param org the {@link Org} to check the authority
     * @return is the user member of the given org?
     */
    boolean isAuthorizedInstMember(Org org) {
        ! Org.executeQuery(
                "select uo from UserOrg uo where uo.user = :user and uo.org = :org and uo.formalRole.roleType = 'user'",
                [user: this, org: org]
        ).isEmpty()
    }

    /**
     * Checks if the user is an INST_ADM of an {@link Org} linked by combo to the given {@link Org}
     * @param org the {@link Org} to check against
     * @return is the user INST_ADM of the org by a combo?
     */
    boolean isAuthorizedComboInstAdmin(Org org) {
        //used in _membership_table.gsp
        List<Org> orgList = Org.executeQuery('select c.toOrg from Combo c where c.fromOrg = :org', [org: org])
        orgList.add(org)

        ! Org.executeQuery(
                "select uo from UserOrg uo where uo.user = :user and uo.org in (:orgList) and uo.formalRole = :instAdm",
                [user: this, orgList: orgList, instAdm: Role.findByAuthority('INST_ADM')]
        ).isEmpty()
    }

    /**
     * Checks if the given role is attributed to the user - can only be used with global (ROLE_) constants
     * @param roleName the role name to check for
     * @return does the user have this role granted?
     */
    boolean hasRole(String roleName) {
        SpringSecurityUtils.ifAnyGranted(roleName)
    }

    /**
     * Checks if one of the given role names is attributed to the user - can only be used with global (ROLE_) constants
     * @param roleNames the list of role names to check for
     * @return does the user have any of the roles granted?
     */
    boolean hasRole(List<String> roleNames) {
        SpringSecurityUtils.ifAnyGranted(roleNames?.join(','))
    }

    /**
     * Checks for the ROLE_ADMIN status of the user
     * @return is the user a global admin?
     */
    boolean isAdmin() {
        SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")
    }

    /**
     * Checks for the ROLE_YODA status of the user
     * @return is the user a yoda superuser?
     */
    boolean isYoda() {
        SpringSecurityUtils.ifAnyGranted("ROLE_YODA")
    }

    /**
     * Checks if the user has the given affiliation granted. To be used with one of the INST_ role constants
     * @param userRoleName the INST_-role to check for
     * @return does the user have the given INST_-role granted?
     */
    boolean hasAffiliation(String userRoleName) {
        hasAffiliationAND(userRoleName, 'ROLE_USER')
    }

    /**
     * Checks if the user has the given institution affiliation and the given global role granted
     * @param userRoleName the INST_-role to check for
     * @param globalRoleName the global ROLE constant to check for
     * @return does the user have the given INST-role AND ROLE constant granted?
     */
    boolean hasAffiliationAND(String userRoleName, String globalRoleName) {
        BeanStore.getUserService().checkAffiliation(this, userRoleName, globalRoleName, 'AND', BeanStore.getContextService().getOrg())
    }

    /**
     * Checks if the user has the given affiliation for the foreign {@link Org} to check
     * @param userRoleName the INST_-role to check for
     * @param orgToCheck the {@link Org} to check whether the user is affiliated to
     * @return does the user have the given INST_-role for the given org?
     */
    boolean hasAffiliationForForeignOrg(String userRoleName, Org orgToCheck) {
        BeanStore.getUserService().checkAffiliation(this, userRoleName, 'ROLE_USER', 'AND', orgToCheck)
    }

    /**
     * Checks if the user is the last INST_ADM of the ${@link Org}s affiliated to
     * @return is the user the last institution admin?
     */
    boolean isLastInstAdmin() {
        boolean lia = false

        affiliations.each { aff ->
            if (BeanStore.getInstAdmService().isUserLastInstAdminForOrg(this, aff.org)) {
                lia = true
            }
        }
        lia
    }

    /**
     * short-hand for getDisplayName() but with the difference that for YODAs, the ID is displayed for debugging purposes
     * @return
     */
    @Override
    String toString() {
        BeanStore.getYodaService().showDebugInfo() ? display + ' (' + id + ')' : display
    }
}
