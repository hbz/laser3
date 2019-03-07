package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication

//@CompileStatic
class UserService {

    GrailsApplication grailsApplication

    def createAffiliationRequest(){

        /*
        def user        = User.get(springSecurityService.principal.id)
        def org         = Org.get(params.org)
        def formal_role = Role.get(params.formalRole)

        try {
            if ( (org != null) && (formal_role != null) ) {
                def existingRel = UserOrg.find( { org==org && user==user && formalRole==formal_role } )

                if (existingRel && existingRel.status == UserOrg.STATUS_CANCELLED) {
                    existingRel.delete()
                    existingRel = null
                }

                if(existingRel) {
                    log.debug("existing rel");
                    flash.error= message(code:'profile.processJoinRequest.error', default:"You already have a relation with the requested organisation.")
                }
                else {
                    log.debug("Create new user_org entry....");
                    def p = new UserOrg(dateRequested:System.currentTimeMillis(),
                            status:UserOrg.STATUS_PENDING,
                            org:org,
                            user:user,
                            formalRole:formal_role)
                    p.save(flush:true, failOnError:true)
                }
            }
            else {
                log.error("Unable to locate org or role");
            }
        }
        catch ( Exception e ) {
            log.error("Problem requesting affiliation",e);
        }
        */
    }
}
