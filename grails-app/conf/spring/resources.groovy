import de.laser.dbm.MigrationCallbacks
import de.laser.userdetails.CustomUserDetailsService
import de.laser.web.AuthSuccessHandler

import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy
import org.springframework.security.web.context.SecurityContextPersistenceFilter

import grails.plugin.springsecurity.SpringSecurityUtils


// # https://docs.spring.io/spring-security/site/migrate/current/3-to-4/html5/migrate-3-to-4-xml.html

beans = {

    migrationCallbacks( MigrationCallbacks ) {
        grailsApplication = ref('grailsApplication')
    }

    // [ user counter ..
    sessionRegistry( SessionRegistryImpl )

    registerSessionAuthenticationStrategy( RegisterSessionAuthenticationStrategy, ref('sessionRegistry') )

    sessionFixationProtectionStrategy( SessionFixationProtectionStrategy )

    concurrentSessionControlAuthenticationStrategy( ConcurrentSessionControlAuthenticationStrategy, ref('sessionRegistry') ){
        maximumSessions = -1
        // exceptionIfMaximumExceeded = true
    }
//    compositeSessionAuthenticationStrategy( CompositeSessionAuthenticationStrategy, [
//            ref('registerSessionAuthenticationStrategy'),
//            ref('sessionFixationProtectionStrategy'),
//            ref('concurrentSessionControlAuthenticationStrategy')
//    ])

    sessionAuthenticationStrategy( CompositeSessionAuthenticationStrategy, [
            ref('concurrentSessionControlAuthenticationStrategy'),
            ref('sessionFixationProtectionStrategy'),
            ref('registerSessionAuthenticationStrategy')
    ])
    // .. ]

    // [ supporting initMandatorySettings for users ..
    authenticationSuccessHandler( AuthSuccessHandler ) {
        ConfigObject conf = SpringSecurityUtils.securityConfig

        requestCache                = ref('requestCache')
        redirectStrategy            = ref('redirectStrategy')
        defaultTargetUrl            = conf.successHandler.defaultTargetUrl
        alwaysUseDefaultTargetUrl   = conf.successHandler.alwaysUseDefault
        targetUrlParameter          = conf.successHandler.targetUrlParameter
        ajaxSuccessUrl              = conf.successHandler.ajaxSuccessUrl
        useReferer                  = conf.successHandler.useReferer
    }
    // .. ]

    userDetailsService( CustomUserDetailsService ) {
        grailsApplication = ref('grailsApplication')
    }

    userDetailsByNameServiceWrapper( UserDetailsByNameServiceWrapper ) {
        userDetailsService = ref('userDetailsService')
    }

    preAuthenticatedAuthenticationProvider( PreAuthenticatedAuthenticationProvider ) {
        preAuthenticatedUserDetailsService = ref('userDetailsByNameServiceWrapper')
    }

    securityContextPersistenceFilter( SecurityContextPersistenceFilter )
}
