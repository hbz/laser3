package spring

import de.laser.custom.CustomMigrationCallbacks
import de.laser.custom.CustomPasswordEncoderFactories
import de.laser.custom.CustomUserDetailsService
import de.laser.custom.CustomAuthSuccessHandler
import de.laser.custom.CustomAuditRequestResolver
import de.laser.custom.CustomWebSocketMessageBrokerConfig
import de.laser.custom.CustomWkhtmltoxService
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider
import org.springframework.security.web.context.SecurityContextPersistenceFilter

import grails.plugin.springsecurity.SpringSecurityUtils

beans = {

    // [ audit logging ..
    auditRequestResolver( CustomAuditRequestResolver ) {
        springSecurityService = ref('springSecurityService')
    }
    // .. ]

    // [ database migration plugin ..
    migrationCallbacks( CustomMigrationCallbacks ) {
        grailsApplication = ref('grailsApplication')
    }

    springConfig.addAlias('sessionFactory_dataSource_storage', 'sessionFactory_storage')
    springConfig.addAlias('transactionManager_dataSource_storage', 'transactionManager_storage')
    // .. ]

    // [ password migration/fallback ..
    passwordEncoder( CustomPasswordEncoderFactories ) { bean ->
        bean.factoryMethod = "createDelegatingPasswordEncoder"
    }
    // .. ]

    // [ websockets ..
    //webSocketConfig( CustomWebSocketConfigurer )
    webSocketConfig( CustomWebSocketMessageBrokerConfig )
    // .. ]

    // reloadableResourceBundleMessageSource ( CustomReloadableResourceBundleMessageSource )
    // pluginAwareResourceBundleMessageSource ( CustomPluginAwareResourceBundleMessageSource )
    // messageSource ( CustomPluginAwareResourceBundleMessageSource )

    // [ wkhtmltopdf ..
    wkhtmltoxService( CustomWkhtmltoxService ) {
        grailsApplication           = ref('grailsApplication')
        mailMessageContentRenderer  = ref('mailMessageContentRenderer')
    }
    // .. ]

    // [ supporting initMandatorySettings for users ..
    authenticationSuccessHandler( CustomAuthSuccessHandler ) {
        ConfigObject conf = SpringSecurityUtils.securityConfig

        springSecurityService       = ref('springSecurityService')
        userService                 = ref('userService')
        contextService              = ref('contextService')

        requestCache                = ref('requestCache')
        redirectStrategy            = ref('redirectStrategy')
        defaultTargetUrl            = conf.successHandler.defaultTargetUrl
        alwaysUseDefaultTargetUrl   = conf.successHandler.alwaysUseDefault
        targetUrlParameter          = conf.successHandler.targetUrlParameter
        ajaxSuccessUrl              = conf.successHandler.ajaxSuccessUrl
        useReferer                  = conf.successHandler.useReferer
    }
    // .. ]

    // [ spring ..
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
    // .. ]

}
