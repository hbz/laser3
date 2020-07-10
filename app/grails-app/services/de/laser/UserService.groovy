package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.UserSettings
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.kbplus.auth.UserRole
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import grails.transaction.Transactional
import org.codehaus.groovy.grails.web.servlet.FlashScope
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.FieldError

@Transactional
class UserService {

    def instAdmService
    def contextService
    def messageSource
    def grailsApplication
    Locale locale = LocaleContextHolder.getLocale()

    // called after every successful login
    void initMandatorySettings(User user) {
        log.debug('initMandatorySettings for user #' + user.id)

        def uss = UserSettings.get(user, UserSettings.KEYS.DASHBOARD)

        def userOrgMatches = user.getAuthorizedOrgsIds()
        if (userOrgMatches.size() > 0) {
            if (uss == UserSettings.SETTING_NOT_FOUND) {
                user.getSetting(UserSettings.KEYS.DASHBOARD, Org.findById(userOrgMatches.first()))
            }
            else if (! uss.getValue()) {
                uss.setValue(Org.findById(userOrgMatches.first()))
            }
        }

        user.getSetting(UserSettings.KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_YES)
        user.getSetting(UserSettings.KEYS.IS_REMIND_FOR_SURVEYS_MANDATORY_ENDDATE, RDStore.YN_YES)
        user.getSetting(UserSettings.KEYS.REMIND_PERIOD_FOR_SURVEYS_MANDATORY_ENDDATE, UserSettings.DEFAULT_REMINDER_PERIOD)

        user.getSetting(UserSettings.KEYS.IS_NOTIFICATION_BY_EMAIL, RDStore.YN_YES)
        user.getSetting(UserSettings.KEYS.IS_NOTIFICATION_FOR_SURVEYS_START, RDStore.YN_YES)
        user.getSetting(UserSettings.KEYS.IS_NOTIFICATION_FOR_SYSTEM_MESSAGES, RDStore.YN_YES)
    }

    Set<User> getUserSet(Map params) {
        // only context org depending
        List baseQuery = ['select distinct u from User u']
        List whereQuery = []
        Map queryParams = [:]
        if (params.org) {
            baseQuery << 'UserOrg uo'
            whereQuery << 'uo.user = u and uo.org = :org'
            queryParams.org = params.org
        }

        if (params.status) {
            whereQuery << 'uo.status = :status'
            queryParams.status = params.status
        }

        if (params.authority) {
            baseQuery.add('UserRole ur')
            whereQuery.add('ur.user = u and ur.role = :role')
            queryParams.put('role', Role.get(params.authority.toLong()))
        }

        if (params.name && params.name != '' ) {
            whereQuery.add('(genfunc_filter_matcher(u.username, :name) = true or genfunc_filter_matcher(u.display, :name) = true)')
            queryParams.put('name', "%${params.name.toLowerCase()}%")
        }
        User.executeQuery(baseQuery.join(', ') + (whereQuery ? ' where ' + whereQuery.join(' and ') : '') , queryParams /*,params */)
    }

    LinkedHashMap setResultGenerics(Map params) {
        def result = [orgInstance:contextService.org]
        result.editor = contextService.user

        if (params.get('id')) {
            result.user = User.get(params.id)
            result.editable = result.editor.hasRole('ROLE_ADMIN') || instAdmService.isUserEditableForInstAdm(result.user, result.editor)

            //result.editable = instAdmService.isUserEditableForInstAdm(result.user, result.editor, contextService.getOrg())
        }
        else {
            result.editable = result.editor.hasRole('ROLE_ADMIN') || result.editor.hasAffiliation('INST_ADM')
        }

        result
    }

    User addNewUser(Map params, FlashScope flash) {
        User user = new User(params)
        user.enabled = true

        if (! user.save()) {
            Set errMess = []
            Object[] withArticle = new Object[messageSource.getMessage('user.withArticle.label',null,locale)]
            user.errors.fieldErrors.each { FieldError e ->
                if(e.field == 'username' && e.code == 'unique')
                    errMess.add(messageSource.getMessage('user.not.created.message',null,locale))
                else errMess.add(messageSource.getMessage('default.not.created.message',withArticle,locale))
            }
            errMess
        }

        log.debug("created new user: " + user)

        UserRole defaultRole = new UserRole(user: user, role: Role.findByAuthority('ROLE_USER'))
        defaultRole.save()

        if (params.org && params.formalRole) {
            Org org = Org.get(params.org)
            Role formalRole = Role.get(params.formalRole)

            if (org && formalRole) {
                def existingUserOrgs = UserOrg.findAllByOrgAndFormalRole(org, formalRole).size()

                instAdmService.createAffiliation(user, org, formalRole, UserOrg.STATUS_APPROVED, flash)

                if (formalRole.authority == 'INST_ADM' && existingUserOrgs == 0 && ! org.legallyObligedBy) { // only if new instAdm
                    if (UserOrg.findByOrgAndUserAndFormalRole(org, user, formalRole)) { // only on success
                        org.legallyObligedBy = contextService.getOrg()
                        org.save()
                        log.debug("set legallyObligedBy for ${org} -> ${contextService.getOrg()}")
                    }
                }

                user.getSetting(UserSettings.KEYS.DASHBOARD, org)
                user.getSetting(UserSettings.KEYS.DASHBOARD_TAB, RefdataValue.getByValueAndCategory('Due Dates', RDConstants.USER_SETTING_DASHBOARD_TAB))
            }
        }

        user
    }

    def addAffiliation(user, orgId, formalRoleId, flash) {
        Org org = Org.get(orgId)
        Role formalRole = Role.get(formalRoleId)

        if (user && org && formalRole) {
            instAdmService.createAffiliation(user, org, formalRole, UserOrg.STATUS_APPROVED, flash)
        }
    }

    void setupAdminAccounts(Map<String,Org> orgs) {
        List adminUsers = grailsApplication.config.adminUsers
        List<String> customerTypes = ['konsorte','institut','singlenutzer','kollektivnutzer','konsortium']
        //the Aninas, Rahels and Violas ... if my women get chased from online test environments, I feel permitted to keep them internally ... for more women in IT branch!!!
        Map<String,Role> userRights = ['benutzer':Role.findByAuthority('INST_USER'), //internal 'Anina'
                                       'redakteur':Role.findByAuthority('INST_EDITOR'), //internal 'Rahel'
                                       'admin':Role.findByAuthority('INST_ADM')] //internal 'Viola'

        adminUsers.each { adminUser ->
            customerTypes.each { String customerKey ->
                userRights.each { String rightKey, Role userRole ->
                    String username = "${adminUser.name}_${customerKey}_${rightKey}"
                    User user = User.findByUsername(username)

                    if(! user) {
                        log.debug("trying to create new user: ${username}")
                        user = addNewUser([username: username, password: "${adminUser.pass}", display: username, email: "${adminUser.email}", enabled: true, org: orgs[customerKey]],null)

                        if (user && orgs[customerKey]) {
                            if (! user.hasAffiliationForForeignOrg(rightKey, orgs[customerKey])) {

                                instAdmService.createAffiliation(user, orgs[customerKey], userRole, UserOrg.STATUS_APPROVED, null)
                                user.getSetting(UserSettings.KEYS.DASHBOARD, orgs[customerKey])
                            }
                        }
                    }
                }
            }
        }
    }
}
