package de.laser.auth

import de.laser.Org
import de.laser.UserSetting
import de.laser.storage.BeanStore
import de.laser.storage.RDStore

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

    Date lastLogin
    Integer invalidLoginAttempts = 0

    boolean enabled         = false     // administrative
    boolean accountExpired  = false     // inactivity
    boolean accountLocked   = false     // too many login attempts
    boolean passwordExpired = false     // not used

    static hasMany      = [ roles: UserRole ]
    static mappedBy     = [ roles: 'user' ]

    static constraints = {
        username    blank: false, unique: true
        password    blank: false, password: true
        display     blank: true, nullable: true
        image       blank: true, nullable: true
        formalOrg                nullable: true
        formalRole               nullable: true
        lastLogin                nullable: true
        invalidLoginAttempts     nullable: true
    }

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
        formalOrg       column: 'usr_formal_org_fk', lazy: false
        formalRole      column: 'usr_formal_role_fk', lazy: false

        dateCreated     column: 'usr_date_created'
        lastUpdated     column: 'usr_last_updated'

        lastLogin               column: 'usr_last_login'
        invalidLoginAttempts    column: 'usr_invalid_login_attempts'

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

    /**
     * Call to encode the password before inserting the new user into the database
     */
    void beforeInsert() {
        _encodePassword()
    }

    /**
     * if the password has changed, enocde it before updating the database
     */
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
     * Checks if the user has the given role in the context institution
     * @param role the {@link Role} to be verified
     * @return does the user has the given role at the context institution?
     */
    boolean isFormal(Role role) {
        isFormal(role, BeanStore.getContextService().getOrg())
    }

    /**
     * Checks if the given user belongs to the given institution
     * @param org the institution ({@link Org}) to be verified
     * @return is the given user affiliated to the given institution?
     */
    boolean isFormal(Org org) {
        //used in user/global/edit.gsp
        (formalOrg?.id == org.id) && (formalRole?.roleType == 'user')
    }

    /**
     * Checks if the given user has at the given institution the given role
     * @param role the {@link Role} which should be verified
     * @param org the institution ({@link Org}) to be verified
     * @return is the given user affiliated to the given institution and does it has the given role?
     */
    boolean isFormal(Role role, Org org) {
        (formalRole?.id == role.id) && (formalOrg?.id == org.id)
    }

    /**
     * Checks if the user is an INST_ADM of an {@link Org} linked by combo to the given {@link Org}
     * @param org the {@link Org} to check against
     * @return is the user INST_ADM of the org by a combo?
     */
    boolean isComboInstAdminOf(Org org) {
        //used in _membership_table.gsp
        List<Long> orgIdList = Org.executeQuery(
                'select c.toOrg.id from Combo c where c.fromOrg = :org and c.type = :type', [org: org, type: RDStore.COMBO_TYPE_CONSORTIUM]
        )
        orgIdList.add(org.id)

        (formalOrg?.id in orgIdList) && (formalRole?.id == Role.findByAuthority('INST_ADM').id)
    }

    /**
     * Checks if the user is the last INST_ADM of the ${@link Org}s affiliated to
     * @return is the user the last institution admin?
     */
    boolean isLastInstAdminOf(Org org) {
        boolean lastInstAdmin = false

        if (org) {
            List<User> users = executeQuery('select u from User u where u.formalOrg = :fo and u.formalRole = :fr', [fo: org, fr: Role.findByAuthority('INST_ADM')])
            lastInstAdmin = (users.size() == 1 && users[0] == this)
        }
        lastInstAdmin
    }

    /**
     * Checks if the given user is a global system administrator
     * @return has the user ADMIN rights?
     */
    boolean isAdmin() {
        getAuthorities().authority.contains('ROLE_ADMIN')
    }

    /**
     * Checks if the given user has YODA rights, i.e. is superadmin
     * @return has the user YODA rights?
     */
    boolean isYoda() {
        getAuthorities().authority.contains('ROLE_YODA')
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
