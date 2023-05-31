package de.laser.auth

import de.laser.Org
import de.laser.UserSetting
import de.laser.storage.BeanStore

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

    Org formalOrg
    Role formalRole

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
        formalOrg   blank: false, nullable: true
        formalRole  blank: false, nullable: true
    }

    static transients = [
            'displayName', 'pageSizeOrDefault', 'affiliationOrgs', 'admin', 'yoda', 'lastInstAdmin'
    ] // mark read-only accessor methods

    static mapping = {
        cache           true
	    table           name: '`user`'
	    password        column: '`password`'

        id              column: 'usr_id'
        version         column: 'usr_version'

        username        column: 'usr_username'
        password        column: 'usr_password'
        passwordExpired column: 'usr_password_expired'
        accountExpired  column: 'usr_account_expired'
        accountLocked   column: 'usr_account_locked'
        enabled         column: 'usr_enabled'
        display         column: 'usr_display'
        email           column: 'usr_email'
        image           column: 'usr_image'
        formalOrg       column: 'usr_formal_org_fk'
        formalRole      column: 'usr_formal_role_fk'

        lastUpdated     column: 'usr_last_updated'
        dateCreated     column: 'usr_date_created'
        
        affiliations    batchSize: 10
        roles           batchSize: 10
    }

    /**
     * Retrieves a {@link Set} of global {@link Role}s assigned to the user.
     * Note that they are not the affiliations a user may have and which ensure institution-regulated permissions!
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
        formalOrg ? [formalOrg] : []
    }

    boolean isFormal() {
        formalOrg?.id == BeanStore.getContextService().getOrg().id
    }
    boolean isFormal(Role role) {
        (formalOrg?.id == BeanStore.getContextService().getOrg().id) && (formalRole?.id == role.id)
    }

    /**
     * Checks whether the user is authorised for the given {@link Org}
     * @param org the {@link Org} to check the authority
     * @return is the user member of the given org?
     */
    boolean isMemberOf(Org org) {
        //used in user/global/edit.gsp
        (formalOrg?.id == org.id) && (formalRole?.roleType == 'user')
    }

    /**
     * Checks if the user is an INST_ADM of an {@link Org} linked by combo to the given {@link Org}
     * @param org the {@link Org} to check against
     * @return is the user INST_ADM of the org by a combo?
     */
    boolean isComboInstAdminOf(Org org) {
        //used in _membership_table.gsp
        List<Long> orgIdList = Org.executeQuery('select c.toOrg.id from Combo c where c.fromOrg = :org', [org: org])
        orgIdList.add(org.id)

        (formalOrg?.id in orgIdList) && (formalRole?.id == Role.findByAuthority('INST_ADM').id)
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

    boolean isYoda() {
        getAuthorities().authority.contains('ROLE_YODA')
    }

    /**
     * Checks if the user is the last INST_ADM of the ${@link Org}s affiliated to
     * @return is the user the last institution admin?
     */
    boolean isLastInstAdmin() {
        formalOrg ? BeanStore.getInstAdmService().isUserLastInstAdminForOrg(this, formalOrg) : false
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
