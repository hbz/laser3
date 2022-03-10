// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

import grails.plugin.springsecurity.SpringSecurityUtils
import org.apache.log4j.RollingFileAppender

//done - grails.project.groupId  = appName // change this to alter the default package name and Maven publishing destination
//done - grails.config.locations = ["file:${userHome}/.grails/${appName}-config.groovy"]

//done - System.out.println("!  local config override: ${grails.config.locations}")

//
// --- database migration plugin ---
//

//done - grails.plugin.databasemigration.updateOnStart = false
//done - grails.plugin.databasemigration.updateOnStartFileNames = [ 'changelog.groovy' ]
//done - grails.plugin.databasemigration.changelogLocation = "./migrations"

//
// --- audit logging plugin ---
//

//done - auditLog {
//done -     logFullClassName = true
    stampEnabled = false // 1.1.3
    stampAlways = false // 1.1.3
    cacheDisabled = true // 1.1.3
//done -     actorClosure = { request, session ->
//done -         if (request.applicationContext.springSecurityService.principal instanceof java.lang.String) {
//done - return request.applicationContext.springSecurityService.principal
//done -         }
//done -         String username = request.applicationContext.springSecurityService.principal?.username
//done -         if (SpringSecurityUtils.isSwitched()){
//done -             username = SpringSecurityUtils.switchedUserOriginalUsername+" AS "+username
//done -         }
//done -         return (username in [null, 'anonymousUser', 'system']) ? username : 'anonymised'
//done -     }
//done - }

//
// --- spring security core plugin
//

grails.gsp.tldScanPattern                                      = 'classpath*:/META-INF/*.tld,/WEB-INF/tld/*.tld'
//done - grails.plugin.springsecurity.userLookup.userDomainClassName    = 'com.k_int.kbplus.auth.User'
//done - grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.k_int.kbplus.auth.UserRole'
//done - grails.plugin.springsecurity.userLookup.usernamePropertyName   = 'username'
//done - grails.plugin.springsecurity.authority.className               = 'com.k_int.kbplus.auth.Role'
grails.plugin.springsecurity.securityConfigType                = "Annotation"
//done - grails.plugin.springsecurity.successHandler.alwaysUseDefault   = false //Change on 09.08.18 from true to false MD
//done - grails.plugin.springsecurity.successHandler.defaultTargetUrl   = '/home/index'
//done - grails.plugin.springsecurity.password.algorithm                = 'SHA-256' // default: 'bcrypt'
//done - grails.plugin.springsecurity.password.hash.iterations          = 1

//grails.plugin.springsecurity.useSessionFixationPrevention      = false // 2.0

grails.plugin.springsecurity.providerNames = [
        'preAuthenticatedAuthenticationProvider',
        'daoAuthenticationProvider' //,
        // 'anonymousAuthenticationProvider' //,
        // 'rememberMeAuthenticationProvider'
]

//done - grails.plugin.springsecurity.roleHierarchy = '''
//done - ROLE_YODA > ROLE_ADMIN
//done - ROLE_ADMIN > ROLE_GLOBAL_DATA
//done - ROLE_GLOBAL_DATA > ROLE_USER
//done - '''

//done - grails.plugin.springsecurity.controllerAnnotations.staticRules = [
//done - [pattern: '/monitoring',                access: ['ROLE_YODA']],
//done - [pattern: '/swagger/v0/laser.yaml.gsp', access: ['permitAll']]
//done - ]

//
// --- custom ---
//

//done@config - laserSystemId = 'local'
//done@config - documentStorageLocation = '/tmp/laser' // for uploaded documents
//done@config - deployBackupLocation = documentStorageLocation + '/laserDeployBackups' // for database backups in context of deploys

//done@config - basicDataFileName = 'basicDataDump.xml'
//done@config - orgDumpFileNamePattern = 'orgDump_'
//done@config - orgDumpFileExtension = '.xml'

//done@config - systemEmail = 'laser@hbz-nrw.de'
//done@config - notifications.email.from = 'laser@hbz-nrw.de'
//done@config - notifications.email.replyTo = 'laser@hbz-nrw.de'
//done@config - notifications.email.genericTemplate = true // if enabled, no customisation in email i.e. Reminder inst info, User info... Else, Customised template will be sent to user

//done@config - financials.currency = "EUR|GBP|USD|CHF" // list in priority of order

//done@config - isUpdateDashboardTableInDatabase = true
//done@config - isSendEmailsForDueDatesOfAllUsers = true
//done@config - //notificationsJobActive = true
//done@config - //activateTestJob = true

//done@config - quartzHeartbeat = 'Never'
//done@config - //showDebugInfo = false

//done@config - appDefaultPrefs {
//done@config - globalDatepickerFormat    = 'yyyy-mm-dd'
//done@config -     globalDateFormat          = 'yyyy-MM-dd'
//done@config -     globalDateFormatSQL       = '%Y-%m-%d'
//done@config - }

//
// ---  cache, gorm & database ---
//

//done - grails.cache.enabled = true

grails.cache.config = {
    // affects only cache-plugin caches
    cache {
        name = 'laser_static_pages'
    }
//done -     cache {
//done -         name 'message'
//done -    }
}

//done - grails.gorm.default.mapping = {
//done -     id generator: 'identity' // postgresql sequences for primary keys
//done - }
//grails.gorm.default.constraints = {
//    '*'(nullable: false)  <- default!
//}


//
// --- grails ---
//

// done:default - grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
        html:   ['text/html','application/xhtml+xml'],
        xml:    ['text/xml', 'application/xml'],
        text:   'text/plain',
        js:     'text/javascript',
        rss:    'application/rss+xml',
        atom:   'application/atom+xml',
        css:    'text/css',
        csv:    'text/csv',
        all:    '*/*',
        json:   ['application/json','text/json'],
        form:   'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data'
]

// What URL patterns should be processed by the resources plugin
// done - grails.resources.adhoc.patterns = [
// done -         '/images/*', '/css/*', '/js/*', '/plugins/*', '/semantic/*', '/semantic-restoration/*', '/vendor/*']
// done - grails.resources.adhoc.includes = [
// done -         '/images/**', '/css/**', '/js/**', '/plugins/**', '/semantic/**', '/semantic-restoration/**', '/vendor/**']

// The default codec used to encode data with ${}
grails.views.default.codec = "html" // none, html, base64
// done:default - grails.views.gsp.encoding  = "UTF-8"
grails.converters.encoding = "UTF-8"

// enable Sitemesh preprocessing of GSP pages
// done:default - grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
// done:default - grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
//done - grails.exceptionresolver.params.exclude = ['password']

grails.project.dependency.resolver = "maven"

grails.plugins.remotepagination.enableBootstrap = true // Finance

//grails.mail.default.from = "server@yourhost.com" //override system wide
grails.mail.disabled = false //System wide
grails.mail.poolSize = 20 //default 5 emails at a time, then que based system (prereq = async true)
//grails.mail.port = 30//TODO: Diese Zeile fürs Deploy entfernen!!!

//
// --- logging ---
//

// Log directory/created in current working dir if tomcat var not found.
File logWatchFile
// GlobalDataSyncLog directory/created in current working dir if tomcat var not found.
File globalDataSyncLogWatchFile

String base = System.getProperty("catalina.base")
boolean environment_dev = false
// set per-environment serverURL stem for creating absolute links
environments {
    development {
        grails.logging.jul.usebridge = true
//done -         grails.serverURL = "http://localhost:8080/laser"
        environment_dev = true
        logWatchFile = new File("logs/${appName}-${appVersion}.log")
        globalDataSyncLogWatchFile = new File ("logs/globalDataSyncLog-${appVersion}.log")
    }
    production {
        grails.logging.jul.usebridge = false
//done -         grails.serverURL = "http://localhost:8080/laser" // override in local config (laser-config.groovy)
        logWatchFile = new File ("${base}/logs/catalina.out")
        globalDataSyncLogWatchFile = new File ("${base}/logs/globalDataSyncLog-${appVersion}.log")
    }
}


// Log file variable.
def logFile = logWatchFile.canonicalPath
def globalDataSyncFile = globalDataSyncLogWatchFile.canonicalPath

// log4j configuration
log4j = {
  appenders {
    console name: "stdout", threshold: org.apache.log4j.Level.ALL
//done -     if (environment_dev) {
//done -       appender new RollingFileAppender(
//done -           name: 'dailyAppender',
//done -           fileName: (logFile),
//done -           layout: pattern(conversionPattern:'%d [%t] %-5p %c{2} %x - %m%n')
//done -       )
    }
      appender new RollingFileAppender(
              name: 'globalDataSyncAppender',
              fileName: (globalDataSyncFile),
              layout: pattern(conversionPattern: '%d [%t] %-5p %c{2} %x - %m%n')
      )
  }
//done -   root {
//done -     if (environment_dev) {
//done -       error 'stdout', 'dailyAppender'
//done -     } else {
//done -       error 'stdout'
//done -     }
//done -   }

  debug globalDataSyncAppender: "grails.app.services.com.k_int.kbplus.GlobalSourceSyncService"
  //    // Enable Hibernate SQL logging with param values
  //    trace 'org.hibernate.type'
  // debug 'org.hibernate.SQL'
  off    'grails.plugin.formfields'

  error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
      'org.codehaus.groovy.grails.web.pages', //  GSP
      'org.codehaus.groovy.grails.web.sitemesh', //  layouts
      'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
      'org.codehaus.groovy.grails.web.mapping', // URL mapping
      'org.codehaus.groovy.grails.commons', // core / classloading
      'org.codehaus.groovy.grails.plugins', // plugins
      'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
      'org.springframework',
      'org.hibernate',
      'org.hibernate.cache.ehcache',
      'formfields',
      'com.k_int.kbplus.filter',
      // 'org.codehaus.groovy.grails.plugins.springsecurity'
      'grails.plugin.springsecurity' // 2.0


//done -   debug  'grails.app.controllers',
//done -       'grails.app.services',
//done -       'grails.app.domain',
//done -       'grails.app.conf',
//done -       'grails.app.jobs',
//done -       'grails.app.conf.BootStrap',
//done -       //'edu.umn.shibboleth.sp',
//done -       'com.k_int',
//done -   // 'org.springframework.security'
//done -       'grails.app.taglib.InplaceTagLib'
}
