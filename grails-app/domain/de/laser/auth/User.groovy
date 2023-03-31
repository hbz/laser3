package de.laser.auth

import de.laser.Org
import de.laser.UserSetting
import de.laser.storage.BeanStore
import grails.plugin.springsecurity.SpringSecurityUtils

import javax.persistence.Transient

/**
 * A user in the system
 */
//@EqualsAndHashCode(includes='username')
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

    static hasMany      = [ affiliations: UserOrgRole, roles: UserRole ]
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
            'displayName', 'pageSizeOrDefault', 'affiliationOrgs', 'affiliationOrgsIdList', 'admin', 'yoda', 'lastInstAdmin'
    ] // mark read-only accessor methods

    static mapping = {
        cache           true
	    table           name: '`user`'
	    password        column: '`password`'

        id              column: 'usr_id'
        version         column: 'usr_version'

        accountExpired  column: 'usr_account_expired'
        accountLocked   column: 'usr_account_locked'
        display         column: 'usr_display'
        email           column: 'usr_email'
        enabled         column: 'usr_enabled'
        image           column: 'usr_image'
        password        column: 'usr_password'
        passwordExpired column: 'usr_password_expired'
        shibbScope      column: 'usr_shibb_scope'
        username        column: 'usr_username'
        
        lastUpdated     column: 'usr_last_updated'
        dateCreated     column: 'usr_date_created'
        
        affiliations    batchSize: 10
        roles           batchSize: 10
    }

    /**
     * Retrieves a {@link Set} of global {@link Role}s assigned to the user.
     * Note that they are not the affiliations (= {@link UserOrgRole}s) a user may have and which ensure institution-regulated permissions!
     * @return the user's {@link Role}s
     */
    Set<Role> getAuthorities() {
        (UserRole.findAllByUser(this) as List<UserRole>)*.role as Set<Role>
    }

    void beforeInsert() {
        _encodePassword()
    }

    void beforeUpdate() {
        if (isDirty('password')) {
            _encodePassword()
        }
    }

    /**
     * Encodes the submitted password
     */
    private void _encodePassword() {
        password = BeanStore.getSpringSecurityService().encodePassword(password)
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
    int getPageSizeOrDefault() {
        // create if no setting found
        UserSetting setting = getSetting(UserSetting.KEYS.PAGE_SIZE, 10)
        // if setting exists, but null value - fallback
        setting.getValue() ? setting.getValue() as int : 10
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
     * Gets a list of all {@link Org}s for which this user has affiliations
     * @return a {@link List} of authorised {@link Org}s
     */
    List<Org> getAffiliationOrgs() {
        this.affiliations.collect{ it.org }.unique()
    }

    /**
     * Same as {@link #getAffiliationOrgs}, but only the IDs of the {@link Org}s are being collected
     * @return a {@link List} of org IDs
     */
    List<Long> getAffiliationOrgsIdList() {
        getAffiliationOrgs().collect{ it.id }
    }

    /**
     * Checks whether the user is authorised for the given {@link Org}
     * @param org the {@link Org} to check the authority
     * @return is the user member of the given org?
     */
    boolean isMemberOf(Org org) {
        //used in user/global/edit.gsp
        ! Org.executeQuery(
                "select uo from UserOrgRole uo where uo.user = :user and uo.org = :org and uo.formalRole.roleType = 'user'",
                [user: this, org: org]
        ).isEmpty()
    }

    /**
     * Checks if the user is an INST_ADM of an {@link Org} linked by combo to the given {@link Org}
     * @param org the {@link Org} to check against
     * @return is the user INST_ADM of the org by a combo?
     */
    boolean isComboInstAdminOf(Org org) {
        //used in _membership_table.gsp
        List<Org> orgList = Org.executeQuery('select c.toOrg from Combo c where c.fromOrg = :org', [org: org])
        orgList.add(org)

        ! Org.executeQuery(
                "select uo from UserOrgRole uo where uo.user = :user and uo.org in (:orgList) and uo.formalRole = :instAdm",
                [user: this, orgList: orgList, instAdm: Role.findByAuthority('INST_ADM')]
        ).isEmpty()
    }

    /**
     * Checks if the given role is attributed to the user - can only be used with global (ROLE_) constants
     * @param roleName the role name to check for
     * @return does the user have this role granted?
     */
    @Deprecated
    boolean hasMinRole(String roleName) {
        SpringSecurityUtils.ifAnyGranted(roleName)
    }

    /**
     * Checks if the user has the given affiliation granted. To be used with one of the INST_ role constants
     * @param instUserRole the INST_-role to check for
     * @return does the user have the given INST_-role granted?
     */
    boolean hasCtxAffiliation_or_ROLEADMIN(String instUserRole) {
        BeanStore.getUserService().checkAffiliation_or_ROLEADMIN(this, BeanStore.getContextService().getOrg(), instUserRole)
    }

    /**
     * Checks if the user has the given affiliation for the foreign {@link Org} to check
     * @param instUserRole the INST_-role to check for
     * @param orgToCheck the {@link Org} to check whether the user is affiliated to
     * @return does the user have the given INST_-role for the given org?
     */
    boolean hasOrgAffiliation_or_ROLEADMIN(Org orgToCheck, String instUserRole) {
        BeanStore.getUserService().checkAffiliation_or_ROLEADMIN(this, orgToCheck, instUserRole)
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
