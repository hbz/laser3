package spring

import de.laser.custom.CustomMigrationCallbacks
import de.laser.custom.CustomPasswordEncoderFactories
import de.laser.custom.CustomUserDetailsService
import de.laser.custom.CustomAuthSuccessHandler
import de.laser.custom.CustomAuditRequestResolver
import de.laser.custom.CustomWkhtmltoxService
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy
import org.springframework.security.web.context.SecurityContextPersistenceFilter

import grails.plugin.springsecurity.SpringSecurityUtils

beans = {

    migrationCallbacks( CustomMigrationCallbacks ) {
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

    sessionAuthenticationStrategy( CompositeSessionAuthenticationStrategy, [
            ref('concurrentSessionControlAuthenticationStrategy'),
            ref('sessionFixationProtectionStrategy'),
            ref('registerSessionAuthenticationStrategy')
    ])
    // .. ]

    // [ supporting initMandatorySettings for users ..
    authenticationSuccessHandler( CustomAuthSuccessHandler ) {
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

    // [ audit logging ..
    auditRequestResolver( CustomAuditRequestResolver ) {
        springSecurityService = ref('springSecurityService')
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

    // [ password fallback ..
    passwordEncoder(CustomPasswordEncoderFactories) { bean ->
        bean.factoryMethod = "createDelegatingPasswordEncoder"
    }
    // .. ]

    // [ wkhtmltopdf ..
    wkhtmltoxService( CustomWkhtmltoxService ) {
        grailsApplication = ref('grailsApplication')
        mailMessageContentRenderer = ref('mailMessageContentRenderer')
    }
    // .. ]
}
