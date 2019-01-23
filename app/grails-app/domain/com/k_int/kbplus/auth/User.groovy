package com.k_int.kbplus.auth

import com.k_int.kbplus.UserSettings
import de.laser.interfaces.Permissions
import grails.plugin.springsecurity.SpringSecurityUtils

import javax.persistence.Transient
import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue

class User implements Permissions {

  transient springSecurityService
  def contextService
  def yodaService
  def grailsApplication

  String username
  String display
  String password
  String instname
  String instcode
  String email
  String shibbScope
  String apikey
  String apisecret
  boolean enabled
  boolean accountExpired
  boolean accountLocked
  boolean passwordExpired

    @Deprecated
    Long defaultPageSize = new Long(10); // will be removed

  SortedSet affiliations
  SortedSet roles

    @Deprecated
    Org defaultDash   // will be removed
  
  // TODO: move to new table
    @Deprecated
    RefdataValue showInfoIcon     // will be removed
    @Deprecated
    RefdataValue showSimpleViews  // will be removed

  static hasMany = [ affiliations: com.k_int.kbplus.auth.UserOrg, roles: com.k_int.kbplus.auth.UserRole, reminders: com.k_int.kbplus.Reminder ]
  static mappedBy = [ affiliations: 'user', roles: 'user' ]

  static constraints = {
    username blank: false, unique: true
    password blank: false
    instname blank: true, nullable: true
    display blank: true, nullable: true
    instcode blank: true, nullable: true
    email blank: true, nullable: true
    shibbScope blank: true, nullable: true
    defaultDash blank: true, nullable: true         // will be removed
    defaultPageSize blank: true, nullable: true     // will be removed
    apikey blank: true, nullable: true
    apisecret blank: true, nullable: true
    showInfoIcon blank:false, nullable:true         // will be removed
    showSimpleViews blank:false, nullable:true      // will be removed
  }

  static mapping = {
      table (name: '`user`')
      password column: '`password`'
  }

  Set<Role> getAuthorities() {
    UserRole.findAllByUser(this).collect { it.role } as Set
  }

  def beforeInsert() {
    encodePassword()
  }

  def beforeUpdate() {
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
  def getDisplayName() {
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
    affiliations.findAll { (it.status == UserOrg.STATUS_APPROVED) || (it.status==UserOrg.STATUS_AUTO_APPROVED) }
  }

  @Transient def getAuthorizedOrgs() {
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

  /**
   * This method lists all the principals that convey a particular permission on a user. For example
   * Institution UHI grants "EDIT" permission to anyone with the role "Member". This is the trivial case.
   * if "UHI" joins the "SHEDL" consortia, and the "Member" link between the two also carries the "EDIT" permission,
   * then the implication is that this user will be able to edit SHEDL resources. This method traverses the directed graph
   * of objects that grant the identified permission to a user. This simplifies searching the permissions space,
   * as we're then only looking for one of a finite set.
   */
  @Transient def listPrincipalsGrantingPermission(perm) {
    Set result= new HashSet();
    def perm_obj = Perm.findByCode(perm)
    if ( perm ) {
      def c = UserOrg.createCriteria()
      def results = c.list {
	      eq("user",this)
              formalRole {
                grantedPermissions {
                  eq("perm",perm_obj)
                }
              }
              or {
                eq("status",UserOrg.STATUS_APPROVED)
                eq("status",UserOrg.STATUS_AUTO_APPROVED)
              }
             
      }
      results.each { uo ->
        //log.debug("User has direct membership with ${uo.org.id}/${uo.org.name}")
        result.add("${uo.org.id}:${perm}")
        // We do a 1-hop addition - any outgoing combos carrying our identified permission - We should add those orgs too
        uo.org.outgoingCombos.each { oc ->
          //log.debug("Testing outgoing combo ${oc.toOrg.id}/${oc.toOrg.name} for perms of the given type")
          def has_perm = false;
          oc.type.sharedPermissions.each { sp ->
            if ( sp.perm.code==perm )
              has_perm=true;
          }
          if ( has_perm ) {
            if ( ! result.contains("${oc.toOrg.id}:${perm}") ) {
              result.add("${oc.toOrg.id}:${perm}")
            }
          }
        }
      }
    }

    log.debug("user granted ${perm} for ${result}")
    result
  }
  
  
  
  transient def getUserPreferences() {
    def userPrefs = [
      "showInfoIcon" : (getSettingsValue(UserSettings.KEYS.SHOW_INFO_ICON)?.value?.equalsIgnoreCase("Yes") ? true : false)
    ]
    
    // Return the prefs.
    userPrefs
  }

    def isEditableBy(user) {
        hasPerm('edit', user)
    }

    def isVisibleBy(user) {
        hasPerm('view', user)
    }

    def hasPerm(perm, user) {
        false
    }

    def isAdmin() {
        SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")
    }
    def isYoda() {
        SpringSecurityUtils.ifAnyGranted("ROLE_YODA")
    }

    /*
    def hasRole(roleName) {
      log.debug("USER.hasRole(): " + roleName) // TODO check roleHierarchy

        def result = false
        def role = Role.findByAuthority(roleName)
        if (role) {
            def ur = UserRole.findByUserAndRole(this, role)
            result = (ur && roles?.contains(ur))
        }
        result
    } */

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

    @Override
    String toString() {
        yodaService.showDebugInfo() ? display + ' (' + id + ')' : display
        //display
    }
}
