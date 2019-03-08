package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.i18n.LocaleContextHolder

//@CompileStatic
class UserService {

    GrailsApplication grailsApplication

    def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')

    def createAffiliation(User user, Org org, Role formalRole, def uoStatus, def flash){

        try {
            def check = UserOrg.findByOrgAndUserAndFormalRole(org, user, formalRole)

            if (check && check.status == UserOrg.STATUS_CANCELLED) {
                check.delete()
                check = null
            }

            if (check) {
                flash?.error = messageSource.getMessage('profile.processJoinRequest.error', null, LocaleContextHolder.getLocale())
            }
            else {
                log.debug("Create new user_org entry....");
                def uo = new UserOrg(
                        dateRequested:System.currentTimeMillis(),
                        status: uoStatus,
                        org: org,
                        user: user,
                        formalRole: formalRole)

                if (uoStatus in [UserOrg.STATUS_APPROVED, UserOrg.STATUS_AUTO_APPROVED]) {
                    uo.dateActioned = uo.dateRequested
                }
                if (uo.save(flush:true)) {
                    flash?.message = "OK"
                }
                else {
                    flash?.error = "Problem requesting affiliation"
                }
            }
        }
        catch (Exception e) {
            flash?.error = "Problem requesting affiliation"
        }
    }
}
