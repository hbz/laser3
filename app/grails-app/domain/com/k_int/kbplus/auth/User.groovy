package com.k_int.kbplus.auth

import com.k_int.kbplus.UserSettings
import de.laser.interfaces.Permissions
import grails.plugin.springsecurity.SpringSecurityUtils

import javax.persistence.Transient
import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue

import java.nio.charset.Charset

class User {

  transient springSecurityService
  def contextService
  def yodaService
  def grailsApplication

  String username
  String display
  String password
  String email
  String shibbScope
  String apikey
  String apisecret
  boolean enabled
  boolean accountExpired
  boolean accountLocked
  boolean passwordExpired

  SortedSet affiliations
  SortedSet roles

  static hasMany = [ affiliations: com.k_int.kbplus.auth.UserOrg, roles: com.k_int.kbplus.auth.UserRole, reminders: com.k_int.kbplus.Reminder ]
  static mappedBy = [ affiliations: 'user', roles: 'user' ]

  static constraints = {
    username blank: false, unique: true
    password blank: false
    display blank: true, nullable: true
    email blank: true, nullable: true
    shibbScope blank: true, nullable: true
    apikey blank: true, nullable: true
    apisecret blank: true, nullable: true
  }

  static mapping = {
      table (name: '`user`')
      password column: '`password`'
  }

  Set<Role> getAuthorities() {
    UserRole.findAllByUser(this).collect { it.role } as Set
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
    def getSetting(UserSettings.KEYS key, def defaultValue) {
        def us = UserSettings.get(this, key)
        (us == UserSettings.SETTING_NOT_FOUND) ? UserSettings.add(this, key, defaultValue) : us
    }

    /*
        gets VALUE of UserSettings
        creating new UserSettings (with value) if not existing
     */
    def getSettingsValue(UserSettings.KEYS key, def defaultValue) {
        def setting = getSetting(key, defaultValue)
        setting.getValue()
    }

    /*
        gets VALUE of UserSettings
        creating new UserSettings if not existing
     */
    def getSettingsValue(UserSettings.KEYS key) {
        getSettingsValue(key, null)
    }

    // refactoring -- tmp changes

    def getDefaultPageSizeTMP() {
        //defaultPageSize
        def setting = getSetting(UserSettings.KEYS.PAGE_SIZE, 10)
        setting.getValue()
    }

    // refactoring -- tmp changes

  @Transient
  String getDisplayName() {
    def result = null;
    if ( display ) {
      result = display
    }
    else {
      result = username
    }
    result
  }

  protected void encodePassword() {
    password = springSecurityService.encodePassword(password)
  }

  @Transient def getAuthorizedAffiliations() {
    affiliations.findAll { it.status == UserOrg.STATUS_APPROVED }
  }

  @Transient List<Org> getAuthorizedOrgs() {
    // def result = Org.find(
    def qry = "select o from Org as o where exists ( select uo from UserOrg as uo where uo.org = o and uo.user = ? and ( uo.status=1 or uo.status=3)) order by o.name"
    def o = Org.executeQuery(qry, [this]);
    o
  }
  @Transient def getAuthorizedOrgsIds() {
    // def result = Org.find(
    def qry = "select o.id from Org as o where exists ( select uo from UserOrg as uo where uo.org = o and uo.user = ? and ( uo.status=1 or uo.status=3)) order by o.name"
    def o = Org.executeQuery(qry, [this]);
    o
  }

    def hasRole(String roleName) {
        //println SpringSecurityUtils.ifAnyGranted(roleName)
        SpringSecurityUtils.ifAnyGranted(roleName)
    }

    def isAdmin() {
        SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")
    }
    def isYoda() {
        SpringSecurityUtils.ifAnyGranted("ROLE_YODA")
    }

    def hasAffiliation(userRoleName) {
        hasAffiliationAND(userRoleName, 'ROLE_USER')
    }

    def hasAffiliationAND(userRoleName, globalRoleName) {
        affiliationCheck(userRoleName, globalRoleName, 'AND', contextService.getOrg())
    }
    def hasAffiliationOR(userRoleName, globalRoleName) {
        affiliationCheck(userRoleName, globalRoleName, 'OR', contextService.getOrg())
    }

    def hasAffiliationForOrg(userRoleName, orgToCheck) {
        affiliationCheck(userRoleName, 'ROLE_USER', 'AND', orgToCheck)
    }

    private def affiliationCheck(userRoleName, globalRoleName, mode, orgToCheck) {
        def result = false
        def rolesToCheck = [userRoleName]

        //log.debug("USER.hasAffiliation(): ${userRoleName}, ${globalRoleName}, ${mode} @ ${orgToCheck}")

        // TODO:

        if (SpringSecurityUtils.ifAnyGranted("ROLE_YODA")) {
            return true // may the force be with you
        }
        if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
            return true // may the force be with you
        }

        if (mode == 'AND') {
            if (! SpringSecurityUtils.ifAnyGranted(globalRoleName)) {
                return false // min restriction fail
            }
        }
        else if (mode == 'OR') {
            if (SpringSecurityUtils.ifAnyGranted(globalRoleName)) {
                return true // min level granted
            }
        }

        // TODO:

        // sym. role hierarchy
        if (userRoleName == "INST_USER") {
            rolesToCheck << "INST_EDITOR"
            rolesToCheck << "INST_ADM"
        }
        else if (userRoleName == "INST_EDITOR") {
            rolesToCheck << "INST_ADM"
        }

        rolesToCheck.each{ rot ->
            def role = Role.findByAuthority(rot)
            if (role) {
                def uo = UserOrg.findByUserAndOrgAndFormalRole(this, orgToCheck, role)
                result = result || (uo && getAuthorizedAffiliations()?.contains(uo))
            }
        }
        result
    }

    static String generateRandomPassword() {
        org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(24)
    }

    @Override
    String toString() {
        yodaService.showDebugInfo() ? display + ' (' + id + ')' : display
        //display
    }
}
