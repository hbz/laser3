package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.kbplus.auth.UserRole
import de.laser.helper.RDStore
import org.codehaus.groovy.grails.commons.GrailsApplication
import com.k_int.kbplus.UserSettings
import org.codehaus.groovy.grails.web.servlet.FlashScope
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.FieldError

class UserService {

    def instAdmService
    def contextService
    def messageSource
    Locale locale = LocaleContextHolder.getLocale()

    void initMandatorySettings(User user) {
        log.debug('initMandatorySettings for user #' + user.id)


        user.getSetting(UserSettings.KEYS.IS_NOTIFICATION_BY_EMAIL, RDStore.YN_YES)
        user.getSetting(UserSettings.KEYS.IS_NOTIFICATION_FOR_SURVEYS_START, RDStore.YN_YES)

    // called after
    // new User.save()
    // or
    // every successful login
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

    def addNewUser(Map params, FlashScope flash) {
        User user = new User(params)
        user.enabled = true

        if (! user.save()) {
            Set errMess = []
            user.errors.fieldErrors.each { FieldError e ->
                if(e.field == 'username' && e.code == 'unique')
                    errMess.add(messageSource.getMessage('user.not.created.message',null,locale))
                else errMess.add(messageSource.getMessage('default.not.created.message',[messageSource.getMessage('user.withArticle.label',null,locale)],locale))
            }
            errMess
        }

        log.debug("created new user: " + user)

        def defaultRole = new UserRole(user: user, role: Role.findByAuthority('ROLE_USER'))
        defaultRole.save()

        if (params.org && params.formalRole) {
            Org org = Org.get(params.org)
            Role formalRole = Role.get(params.formalRole)
            if (org && formalRole) {
                instAdmService.createAffiliation(user, org, formalRole, UserOrg.STATUS_APPROVED, flash)
                user.getSetting(UserSettings.KEYS.DASHBOARD, org)
                user.getSetting(UserSettings.KEYS.DASHBOARD_TAB, RefdataValue.getByValueAndCategory('Due Dates', 'User.Settings.Dashboard.Tab'))
            }
        }

        user
    }

}
