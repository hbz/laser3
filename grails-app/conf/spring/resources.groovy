//import de.laser.dbm.MigrationCallbacks

import grails.plugin.springsecurity.SpringSecurityUtils

// # https://docs.spring.io/spring-security/site/migrate/current/3-to-4/html5/migrate-3-to-4-xml.html

beans = {

    //migrationCallbacks(MigrationCallbacks) {
    //    grailsApplication = ref('grailsApplication')
    //}

    // [ user counter ..
    sessionRegistry(org.springframework.security.core.session.SessionRegistryImpl)

    // .. ]

    // [ supporting initMandatorySettings for users ..
    authenticationSuccessHandler(de.laser.web.AuthSuccessHandler) {
        // Reusing the security configuration
        def conf = SpringSecurityUtils.securityConfig
        // Configuring the bean ..
        requestCache = ref('requestCache')
        redirectStrategy = ref('redirectStrategy')
        defaultTargetUrl = conf.successHandler.defaultTargetUrl
        alwaysUseDefaultTargetUrl = conf.successHandler.alwaysUseDefault
        targetUrlParameter = conf.successHandler.targetUrlParameter
        ajaxSuccessUrl = conf.successHandler.ajaxSuccessUrl
        useReferer = conf.successHandler.useReferer
    }
    // .. ]

    userDetailsService(de.laser.userdetails.CustomUserDetailsService) {
        grailsApplication = ref('grailsApplication')
    }

//    userDetailsService(GormUserDetailsService) {
//        grailsApplication = ref('grailsApplication')
//    }

    userDetailsByNameServiceWrapper(org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper) {
        userDetailsService = ref('userDetailsService')
    }

    preAuthenticatedAuthenticationProvider(org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider) {
        preAuthenticatedUserDetailsService = ref('userDetailsByNameServiceWrapper')
    }

    securityContextPersistenceFilter(org.springframework.security.web.context.SecurityContextPersistenceFilter){}

    // [ controls api access via hmac ..
    apiFilter(de.laser.web.ApiFilter){}
    // .. ]

}
