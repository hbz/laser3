package de.laser.auth

import de.laser.Org
import de.laser.UserSetting
import grails.plugin.springsecurity.SpringSecurityUtils
import org.apache.commons.lang.RandomStringUtils

import javax.persistence.Transient

//@GrailsCompileStatic
//@EqualsAndHashCode(includes='username')
//@ToString(includes='username', includeNames=true, includePackage=false)
class User {

    def contextService
    def instAdmService
    def springSecurityService
    def userService
    def yodaService

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

    /*
        gets UserSetting
        creating new one (with value) if not existing
     */
    UserSetting getSetting(UserSetting.KEYS key, def defaultValue) {
        def us = UserSetting.get(this, key)
        (us == UserSetting.SETTING_NOT_FOUND) ? UserSetting.add(this, key, defaultValue) : (UserSetting) us
    }

    /*
        gets VALUE of UserSetting
        creating new UserSetting (with value) if not existing
     */
    def getSettingsValue(UserSetting.KEYS key, def defaultValue) {
        UserSetting setting = getSetting(key, defaultValue)
        setting.getValue()
    }

    /*
        gets VALUE of UserSetting
        creating new UserSetting if not existing
     */
    def getSettingsValue(UserSetting.KEYS key) {
        getSettingsValue(key, null)
    }

    long getDefaultPageSize() {
        // create if no setting found
        UserSetting setting = getSetting(UserSetting.KEYS.PAGE_SIZE, 10)
        // if setting exists, but null value
        long value = setting.getValue() ?: 10
        return value
    }

    int getDefaultPageSizeAsInteger() {
        long value = getDefaultPageSize()
        return value.intValue()
    }

    @Transient
    String getDisplayName() {
        display ? display : username
    }

    protected void encodePassword() {
        password = springSecurityService.encodePassword(password)
    }

    List<UserOrg> getAuthorizedAffiliations() {
        UserOrg.findAllByUserAndStatus(this, UserOrg.STATUS_APPROVED)
    }
    List<Org> getAuthorizedOrgs() {
        String qry = "select distinct(o) from Org as o where exists ( select uo from UserOrg as uo where uo.org = o and uo.user = :user and uo.status = :approved ) order by o.name"
        Org.executeQuery(qry, [user: this, approved: UserOrg.STATUS_APPROVED])
    }
    List<Long> getAuthorizedOrgsIds() {
        getAuthorizedOrgs().collect{ it.id }
    }

    boolean hasRole(String roleName) {
        SpringSecurityUtils.ifAnyGranted(roleName)
    }
    boolean hasRole(List<String> roleNames) {
        SpringSecurityUtils.ifAnyGranted(roleNames?.join(','))
    }

    boolean isAdmin() {
        SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")
    }
    boolean isYoda() {
        SpringSecurityUtils.ifAnyGranted("ROLE_YODA")
    }

    boolean hasAffiliation(String userRoleName) {
        hasAffiliationAND(userRoleName, 'ROLE_USER')
    }

    boolean hasAffiliationAND(String userRoleName, String globalRoleName) {
        userService.checkAffiliation(this, userRoleName, globalRoleName, 'AND', contextService.getOrg())
    }
    boolean hasAffiliationOR(String userRoleName, String globalRoleName) {
        userService.checkAffiliation(this, userRoleName, globalRoleName, 'OR', contextService.getOrg())
    }

    boolean hasAffiliationForForeignOrg(String userRoleName, Org orgToCheck) {
        userService.checkAffiliation(this, userRoleName, 'ROLE_USER', 'AND', orgToCheck)
    }

    boolean isLastInstAdmin() {
        boolean lia = false

        affiliations.each { aff ->
            if (instAdmService.isUserLastInstAdminForOrg(this, aff.org)) {
                lia = true
            }
        }
        lia
    }

    static String generateRandomPassword() {
        RandomStringUtils.randomAlphanumeric(24)
    }

    @Override
    String toString() {
        yodaService.showDebugInfo() ? display + ' (' + id + ')' : display
    }
}
