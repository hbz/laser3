
// --- globals ---

// cache

grails {
    cache {
        enabled = true
        config = { // affects only cache-plugin caches
            cache {
                name 'message'
            }
        }
        ehcache {
            ehcacheXmlLocation = 'ehcache.xml'
        }
    }
}

// gorm

grails.gorm.default.mapping = {
    autowire true            // service dependency injection enabled
    id generator: 'identity' // postgresql sequences for primary keys
}

// database migration plugin

grails.plugin.databasemigration.updateOnStart = true

// audit logging plugin

grails {
    plugin {
        auditLog {
            auditDomainClassName = 'org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent'
            defaultActor = 'SYS'
        }
    }
}

// wkhtmltopdf plugin
// https://plugins.grails.org/plugin/rlovtangen/wkhtmltopdf

grails.plugin.wkhtmltopdf.binary = '/usr/bin/wkhtmltopdf'

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
//grails.plugin.springsecurity.gsp.layoutAuth                         = 'laser'

grails.plugin.springsecurity.roleHierarchy = '''
    ROLE_YODA > ROLE_ADMIN
    ROLE_ADMIN > ROLE_GLOBAL_DATA
    ROLE_GLOBAL_DATA > ROLE_USER
'''

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        [pattern: '/assets/**',      access: ['permitAll']],
        [pattern: '/static/**',      access: ['permitAll']],
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

//Mail
grails.mail.poolSize = 20 //default 5 emails at a time, then que based system (prereq = async true)
//grails.mail.port = 30//TODO: Diese Zeile nur für Lokal nutzen!!!

aggr_es_indices = [
        "DocContext": "laser_doc_contexts",
        "IssueEntitlement": "laser_issue_intitlements",
        "License": "laser_licenses",
        "LicenseProperty": "laser_license_propertys",
        "Org": "laser_orgs",
        "Package": "laser_packages",
        "Platform": "laser_platforms",
        "Subscription": "laser_subscriptions",
        "SubscriptionProperty": "laser_subscription_propertys",
        "SurveyConfig": "laser_survey_configs",
        "SurveyOrg": "laser_survey_orgs",
        "Task": "laser_tasks",
        "TitleInstancePackagePlatform": "laser_tipps",
]