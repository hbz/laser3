// globals

grails.cache.enabled = true
grails.cache.config = {
    // affects only cache-plugin caches
    cache {
        name = 'laser_static_pages'
    }
    cache {
        name 'message'
    }
}


grails.gorm.default.mapping = {
    autowire true
    id generator: 'identity' // postgresql sequences for primary keys
}

// database migration plugin

grails.plugin.databasemigration.updateOnStart                       = false
grails.plugin.databasemigration.updateOnStartFileNames              = [ 'changelog.groovy' ]
grails.plugin.databasemigration.changelogLocation                   = "./migrations"

// spring security plugin

grails.plugin.springsecurity.userLookup.userDomainClassName 	    = 'de.laser.auth.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName 	    = 'de.laser.auth.UserRole'
grails.plugin.springsecurity.authority.className 				    = 'de.laser.auth.Role'
grails.plugin.springsecurity.userLookup.usernamePropertyName        = 'username'

grails.plugin.springsecurity.password.algorithm                     = 'SHA-256'
grails.plugin.springsecurity.password.hash.iterations               = 1

//grails.plugin.springsecurity.securityConfigType                     = "Annotation"
grails.plugin.springsecurity.successHandler.alwaysUseDefault        = false
grails.plugin.springsecurity.successHandler.defaultTargetUrl        = '/home/index'
grails.plugin.springsecurity.successHandler.logout.afterLogoutUrl   = '/'
grails.plugin.springsecurity.logout.postOnly                        = false
//grails.plugin.springsecurity.gsp.layoutAuth                         = 'semanticUI'

grails.plugin.springsecurity.roleHierarchy = '''
    ROLE_YODA > ROLE_ADMIN
    ROLE_ADMIN > ROLE_GLOBAL_DATA
    ROLE_GLOBAL_DATA > ROLE_USER
'''

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        [pattern: '/monitoring',                access: ['ROLE_YODA']],
        [pattern: '/swagger/v0/laser.yaml.gsp', access: ['permitAll']],
        [pattern: '/',               access: ['permitAll']],
        [pattern: '/error',          access: ['permitAll']],
        [pattern: '/index',          access: ['permitAll']],
        [pattern: '/index.gsp',      access: ['permitAll']],
        [pattern: '/shutdown',       access: ['permitAll']],
        [pattern: '/assets/**',      access: ['permitAll']],
        [pattern: '/**/js/**',       access: ['permitAll']],
        [pattern: '/**/css/**',      access: ['permitAll']],
        [pattern: '/**/images/**',   access: ['permitAll']],
        [pattern: '/**/favicon.ico', access: ['permitAll']]
]

grails.plugin.springsecurity.filterChain.chainMap = [
        [pattern: '/assets/**',      filters: 'none'],
        [pattern: '/**/js/**',       filters: 'none'],
        [pattern: '/**/css/**',      filters: 'none'],
        [pattern: '/**/images/**',   filters: 'none'],
        [pattern: '/**/favicon.ico', filters: 'none'],
        [pattern: '/**',             filters: 'JOINED_FILTERS']
]

// audit-logging-plugin

//grails.plugin.auditLog.auditDomainClassName = 'org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent'