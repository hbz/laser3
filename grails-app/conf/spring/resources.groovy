//import de.laser.dbm.MigrationCallbacks
import de.laser.web.ApiFilter
import de.laser.web.AuthSuccessHandler
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider
import org.springframework.security.web.context.SecurityContextPersistenceFilter
import grails.plugin.springsecurity.userdetails.GormUserDetailsService
import org.springframework.security.web.session.ConcurrentSessionFilter

// # https://docs.spring.io/spring-security/site/migrate/current/3-to-4/html5/migrate-3-to-4-xml.html

beans = {

    //migrationCallbacks(MigrationCallbacks) {
    //    grailsApplication = ref('grailsApplication')
    //}

    // [ user counter ..
    sessionRegistry(SessionRegistryImpl)

    // .. ]

    // [ supporting initMandatorySettings for users ..
    authenticationSuccessHandler(AuthSuccessHandler) {
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

    userDetailsService(GormUserDetailsService) {
        grailsApplication = ref('grailsApplication')
    }

    userDetailsByNameServiceWrapper(UserDetailsByNameServiceWrapper) {
        userDetailsService = ref('userDetailsService')
    }

    preAuthenticatedAuthenticationProvider(PreAuthenticatedAuthenticationProvider) {
        preAuthenticatedUserDetailsService = ref('userDetailsByNameServiceWrapper')
    }

    securityContextPersistenceFilter(SecurityContextPersistenceFilter){}

    // [ controls api access via hmac ..
    apiFilter(ApiFilter){}
    // .. ]

}
