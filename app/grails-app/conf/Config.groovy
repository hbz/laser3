// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

import grails.plugin.springsecurity.SpringSecurityUtils
import org.apache.log4j.RollingFileAppender


grails.project.groupId  = appName // change this to alter the default package name and Maven publishing destination
grails.config.locations = ["file:${userHome}/.grails/${appName}-config.groovy"]

System.out.println("!  local config override: ${grails.config.locations}")

//
// --- database migration plugin ---
//

grails.plugin.databasemigration.updateOnStart = false
grails.plugin.databasemigration.updateOnStartFileNames = [ 'changelog.groovy' ]
grails.plugin.databasemigration.changelogLocation = "./migrations"

//
// --- audit logging plugin ---
//

auditLog {
    logFullClassName = true
    stampEnabled = false // 1.1.3
    stampAlways = false // 1.1.3
    cacheDisabled = true // 1.1.3

    actorClosure = { request, session ->

        if (request.applicationContext.springSecurityService.principal instanceof java.lang.String) {
            return request.applicationContext.springSecurityService.principal
        }

        String username = request.applicationContext.springSecurityService.principal?.username

        if (SpringSecurityUtils.isSwitched()){
            username = SpringSecurityUtils.switchedUserOriginalUsername+" AS "+username
        }

        return (username in [null, 'anonymousUser', 'system']) ? username : 'anonymised'
    }
}

//
// --- spring security core plugin
//

grails.gsp.tldScanPattern                                      = 'classpath*:/META-INF/*.tld,/WEB-INF/tld/*.tld'
grails.plugin.springsecurity.userLookup.userDomainClassName    = 'com.k_int.kbplus.auth.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.k_int.kbplus.auth.UserRole'
grails.plugin.springsecurity.userLookup.usernamePropertyName   = 'username'
grails.plugin.springsecurity.authority.className               = 'com.k_int.kbplus.auth.Role'
grails.plugin.springsecurity.securityConfigType                = "Annotation"
grails.plugin.springsecurity.successHandler.alwaysUseDefault   = false //Change on 09.08.18 from true to false MD
grails.plugin.springsecurity.successHandler.defaultTargetUrl   = '/home/index'
grails.plugin.springsecurity.password.algorithm                = 'SHA-256' // default: 'bcrypt'
grails.plugin.springsecurity.password.hash.iterations          = 1

//grails.plugin.springsecurity.useSessionFixationPrevention      = false // 2.0

grails.plugin.springsecurity.providerNames = [
        'preAuthenticatedAuthenticationProvider',
        'daoAuthenticationProvider' //,
        // 'anonymousAuthenticationProvider' //,
        // 'rememberMeAuthenticationProvider'
]

grails.plugin.springsecurity.roleHierarchy = '''
    ROLE_YODA > ROLE_ADMIN
    ROLE_ADMIN > ROLE_GLOBAL_DATA
    ROLE_GLOBAL_DATA > ROLE_USER
'''

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        [pattern: '/monitoring',                access: ['ROLE_YODA']],
        [pattern: '/swagger/v0/laser.yaml.gsp', access: ['permitAll']]
]

//
// --- custom ---
//

laserSystemId = 'local'
documentStorageLocation = '/tmp/laser' // for uploaded documents
deployBackupLocation = documentStorageLocation + '/laserDeployBackups' // for database backups in context of deploys

basicDataFileName = 'basicDataDump.xml'
orgDumpFileNamePattern = 'orgDump_'
orgDumpFileExtension = '.xml'

systemEmail = 'laser@hbz-nrw.de'
notifications.email.from = 'laser@hbz-nrw.de'
notifications.email.replyTo = 'laser@hbz-nrw.de'
notifications.email.genericTemplate = true // if enabled, no customisation in email i.e. Reminder inst info, User info... Else, Customised template will be sent to user

financials.currency = "EUR|GBP|USD|CHF" // list in priority of order

isUpdateDashboardTableInDatabase = true
isSendEmailsForDueDatesOfAllUsers = true
//notificationsJobActive = true
//activateTestJob = true

quartzHeartbeat = 'Never'
//showDebugInfo = false

/*globalDataSync = [
  "replaceLocalImpIds": [
    "Org": true,
    "TitleInstance": true,
    "Platform": true,
    "Package": true,
    "TIPP": true
  ]
]*/

appDefaultPrefs {
    globalDatepickerFormat    = 'yyyy-mm-dd'
    globalDateFormat          = 'yyyy-MM-dd'
    globalDateFormatSQL       = '%Y-%m-%d'
}

// @NotificationsJob
// - enable notification
// - enable reminder

//
// ---  cache, gorm & database ---
//

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
    id generator: 'identity' // postgresql sequences for primary keys
}
//grails.gorm.default.constraints = {
//    '*'(nullable: false)  <- default!
//}


//
// --- grails ---
//

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
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

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
//rails.resources.resourceLocatorEnabled = true // upgrade to 1.2.14
//grails.resources.uriToUrlCacheTimeout = 0  // upgrade to 1.2.14
//grails.resources.processing.startup = "delayed" // upgrade to 1.2.14
grails.resources.adhoc.patterns = [
        '/images/*', '/css/*', '/js/*', '/plugins/*', '/semantic/*', '/semantic-restoration/*', '/vendor/*']
grails.resources.adhoc.includes = [
        '/images/**', '/css/**', '/js/**', '/plugins/**', '/semantic/**', '/semantic-restoration/**', '/vendor/**']

// The default codec used to encode data with ${}
grails.views.default.codec = "html" // none, html, base64
grails.views.gsp.encoding  = "UTF-8"
grails.converters.encoding = "UTF-8"

// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

grails.project.dependency.resolver = "maven"

grails.plugins.remotepagination.enableBootstrap = true // Finance

//grails.mail.default.from = "server@yourhost.com" //override system wide
grails.mail.disabled = false //System wide
grails.mail.poolSize = 20 //default 5 emails at a time, then que based system (prereq = async true)
//grails.mail.overrideAddress="ryan@k-int.com" //Test env only, overrides to and from address
//grails.mail.port = 30//TODO: Diese Zeile fürs Deploy entfernen!!!

// grails.databinding.dateFormats = ['MMddyyyy', 'yyyy-MM-dd HH:mm:ss.S', "yyyy-MM-dd'T'hh:mm:ss'Z'"]

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
        grails.serverURL = "http://localhost:8080/laser"
        environment_dev = true
        logWatchFile = new File("logs/${appName}-${appVersion}.log")
        globalDataSyncLogWatchFile = new File ("logs/globalDataSyncLog-${appVersion}.log")
    }
    production {
        grails.logging.jul.usebridge = false
        grails.serverURL = "http://localhost:8080/laser" // override in local config (laser-config.groovy)
        logWatchFile = new File ("${base}/logs/catalina.out")
        globalDataSyncLogWatchFile = new File ("${base}/logs/globalDataSyncLog-${appVersion}.log")
    }
}


// Log file variable.
def logFile = logWatchFile.canonicalPath
def globalDataSyncFile = globalDataSyncLogWatchFile.canonicalPath

//System.out.println("~ using log file location: ${logFile}")

grails {
    fileViewer {
        locations = ["${logFile}"]
        linesCount = 250
        areDoubleDotsAllowedInFilePath = false
    }
}

// log4j configuration
log4j = {
  // Example of changing the log pattern for the default console
  // appender:
  //
  //appenders {
  //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
  //}
  //trace 'org.hibernate.type'
  //debug 'org.hibernate.SQL'

  appenders {
    console name: "stdout", threshold: org.apache.log4j.Level.ALL
    if (environment_dev) {
      appender new RollingFileAppender(
          name: 'dailyAppender',
          fileName: (logFile),
          layout: pattern(conversionPattern:'%d [%t] %-5p %c{2} %x - %m%n')
      )
    }
      appender new RollingFileAppender(
              name: 'globalDataSyncAppender',
              fileName: (globalDataSyncFile),
              layout: pattern(conversionPattern: '%d [%t] %-5p %c{2} %x - %m%n')
      )
  }
  root {
    if (environment_dev) {
      error 'stdout', 'dailyAppender'
    } else {
      error 'stdout'
    }
  }

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


  debug  'grails.app.controllers',
      'grails.app.services',
      'grails.app.domain',
      'grails.app.conf',
      'grails.app.jobs',
      'grails.app.conf.BootStrap',
      //'edu.umn.shibboleth.sp',
      'com.k_int',
  // 'org.springframework.security'
      'grails.app.taglib.InplaceTagLib'

  // info   'com.linkedin.grails'
}
