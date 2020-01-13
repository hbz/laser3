package com.k_int.kbplus

import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.UserOrg
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.commons.GrailsApplication

// import org.springframework.security.web.authentication.AbstractProcessingFilter

@Deprecated
class ProcessLoginController {

  GrailsApplication grailsApplication
  def ediAuthTokenMap

  @Deprecated
  def index() {
    return

      com.k_int.kbplus.ProcessLoginController.log.debug("ProcessLoginController::index() - session = ${request.session.id}");

    def ctx = grailsApplication.mainContext

    // Check that request comes from 127.0.0.1
    com.k_int.kbplus.ProcessLoginController.log.debug("Auth request from ${request.getRemoteAddr()}");

    if ( request.getRemoteAddr() == '127.0.0.1' ) {
    }
    else {
      return
    }
    // request.getHeader("X-Forwarded-For")
    // request.getHeader("Client-IP")

    if ( request.getRemoteAddr() == '127.0.0.1' ) {
    }
    else {
      return
    }


      com.k_int.kbplus.ProcessLoginController.log.debug("remote institution appears to be : ${params.ea_edinaOrgId}");
      com.k_int.kbplus.ProcessLoginController.log.debug("remote user appears to be : ${params.ea_edinaUserId}");

    def response_str = 'NO EA_EXTRA FOUND';

    if ( params.ea_extra ) {
      def map = params.ea_extra.split('&').inject([:]) { map, kv -> def (key, value) = 
        kv.split('=').toList(); map[key] = value != null ? URLDecoder.decode(value) : null; map 
      }

        com.k_int.kbplus.ProcessLoginController.log.debug("Auth inst = ${map.authInstitutionName}");
        com.k_int.kbplus.ProcessLoginController.log.debug("UserId = ${map.eduPersonTargetedID}");
        com.k_int.kbplus.ProcessLoginController.log.debug("email = ${map.mail}");
        com.k_int.kbplus.ProcessLoginController.log.debug("Inst Addr = ${map.authInstitutionAddress}");

      if ( ( map.eduPersonTargetedID != null ) && ( map.eduPersonTargetedID.length() > 0 ) ) {
  
        def user = com.k_int.kbplus.auth.User.findByUsername(map.eduPersonTargetedID)
        if ( !user ) {
          com.k_int.kbplus.ProcessLoginController.log.debug("Creating user");
          user = new com.k_int.kbplus.auth.User(username:map.eduPersonTargetedID,
                                                password:'**',
                                                enabled:true,
                                                accountExpired:false,
                                                accountLocked:false, 
                                                passwordExpired:false,
                                                shibbScope:map.shibbScope,
                                                email:map.mail)
  
          if ( user.save(flush:true) ) {
            com.k_int.kbplus.ProcessLoginController.log.debug("Created user, allocating user role");
            def userRole = com.k_int.kbplus.auth.Role.findByAuthority('ROLE_USER')
  
            if ( userRole ) {
              com.k_int.kbplus.ProcessLoginController.log.debug("looked up user role: ${userRole}");
              def new_role_allocation = new com.k_int.kbplus.auth.UserRole(user:user,role:userRole);
    
              if ( new_role_allocation.save(flush:true) ) {
                com.k_int.kbplus.ProcessLoginController.log.debug("New role created...");
              }
              else {
                new_role_allocation.errors.each { e ->
                    com.k_int.kbplus.ProcessLoginController.log.error(e);
                }
              }
            }
            else {
              com.k_int.kbplus.ProcessLoginController.log.error("Unable to look up ROLE_USER");
            }

            // See if we can find the org this user is attached to
            if ( grailsApplication.config.autoAffiliate ) {
              createUserOrgLink(user, map.authInstitutionName, map.shibbScope);
            }

              com.k_int.kbplus.ProcessLoginController.log.debug("Done creating user");
          }
        }
        else {
          com.k_int.kbplus.ProcessLoginController.log.error("Problem creating user......");
          user.errors.each { err ->
              com.k_int.kbplus.ProcessLoginController.log.error(err);
          }
        }
      
        // securityContext.authentication = new PreAuthenticatedAuthenticationToken(map.eduPersonTargetedID, map, roles)
        // securityContext.authentication.setDetails(user)
        // log.debug("Auth set, isAuthenticated = ${securityContext.authentication.isAuthenticated()}, name=${securityContext.authentication.getName()}");
        // log.debug("ea_context=${map.ea_context}");
  
        def tok = java.util.UUID.randomUUID().toString()
        ediAuthTokenMap[tok] = map.eduPersonTargetedID

          com.k_int.kbplus.ProcessLoginController.log.debug("Setting entry in ediAuthTokenMap to ${tok} = ${map.eduPersonTargetedID}");
          com.k_int.kbplus.ProcessLoginController.log.debug(ediAuthTokenMap)
  
        if ( ( params.ea_context ) && ( params.ea_context.trim().length() > 0 ) ) {
          if ( params.ea_context.indexOf('?') > 0 ) {
            response_str="${params.ea_context.replaceAll('ediauthToken','_oldeat_')}&ediauthToken=${tok}"
          }
          else {
            response_str="${params.ea_context}?ediauthToken=${tok}"
          }
        }
        else {
          response_str="http://knowplus.edina.ac.uk/kbplus/?ediauthToken=${tok}"
        }
      }
      else {
        response.sendError(401);
        return
      }
    }

      com.k_int.kbplus.ProcessLoginController.log.debug("Rendering processLoginController response, URL will be ${response_str}");

    // redirect(controller:'home');
    render "${response_str}"
  }

  @Deprecated
  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def createUserOrgLink(user, authInstitutionName, shibbScope) {
    return

    if ( ( authInstitutionName ) && ( authInstitutionName.length() > 0 ) ) {
      def candidate = authInstitutionName.trim().replaceAll(" ","_")
      Org org = com.k_int.kbplus.Org.findByScope(shibbScope)

      // If we didn't match by scope, try matching by normalised name
      if ( ! org )
        org = com.k_int.kbplus.Org.findByShortcode(candidate)

      if ( org ) {
        //boolean auto_approve = false;

        //def auto_approve_setting = Setting.findByName('AutoApproveMemberships');

        //if ( auto_approve_setting?.value == 'true' )
        //  auto_approve = true;

        Role userRole = com.k_int.kbplus.auth.Role.findByAuthority('INST_USER')
        UserOrg user_org_link = new com.k_int.kbplus.auth.UserOrg(user:user,
                                                              org:org, 
                                                              formalRole:userRole,
                                                              status: com.k_int.kbplus.auth.UserOrg.STATUS_PENDING,
                                                              dateRequested:System.currentTimeMillis(), 
                                                              dateActioned:System.currentTimeMillis())
        if ( !user_org_link.save(flush:true) ) {
          com.k_int.kbplus.ProcessLoginController.log.error("Problem saving user org link");
          user_org_link.errors.each { e ->
              com.k_int.kbplus.ProcessLoginController.log.error(e);
          }

        }
        else {
          com.k_int.kbplus.ProcessLoginController.log.debug("Linked user with org ${org.id} based on name ${authInstitutionName}");
        }
      }
    }
  }

}
