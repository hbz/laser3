// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

import de.laser.ContextService
import grails.plugin.springsecurity.SpringSecurityUtils
import org.apache.log4j.DailyRollingFileAppender
import org.apache.log4j.RollingFileAppender

grails.project.groupId  = appName // change this to alter the default package name and Maven publishing destination
grails.config.locations = ["file:${userHome}/.grails/${appName}-config.groovy"]

laserSystemId = 'local'
documentStorageLocation = '/tmp/laser'
featureSurvey = false

globalDataSync = [
  "replaceLocalImpIds": [
    "Org": true,
    "TitleInstance": true,
    "Platform": true,
    "Package": true,
    "TIPP": true
  ]
]

//localauth = true
//showDebugInfo = false

// @NotificationsJob
// - enable notification
// - enable reminder
//hbzMaster = true
isUpdateDashboardTableInDatabase = true
isSendEmailsForDueDatesOfAllUsers = true


// Database Migration Plugin
grails.plugin.databasemigration.updateOnStart = false
grails.plugin.databasemigration.updateOnStartFileNames = [ 'changelog.groovy' ]

System.out.println("\n")
System.out.println("~ local config override: ${grails.config.locations}")
System.out.println("~ database migration plugin updateOnStart: ${grails.plugin.databasemigration.updateOnStart}")

getCurrentServer = {
    // laserSystemId mapping for runtime check; do not delete
    switch (grailsApplication.config.laserSystemId) {
        case 'LAS:eR-Dev':
            return ContextService.SERVER_DEV
            break
        case 'LAS:eR-QA/Stage':
            return ContextService.SERVER_QA
            break
        case 'LAS:eR-Productive':
            return ContextService.SERVER_PROD
            break
        default:
            return ContextService.SERVER_LOCAL
            break
    }
}

customProperties =[
    "org":[
        "journalAccess":[
            "name"  : "Public Journal Access",
            "class" : String.toString(),
            "note"  : "Set the required rights for accessing the public Journals page. For example 'Staff,Student,Public' or leave empty/delete for no public access."
        ]
    ]
]

onix = [
  "codelist" : "ONIX_PublicationsLicense_CodeLists.xsd",
  "comparisonPoints" : [
    'template' : '$value$',
    'values' : [
      '_:PublicationsLicenseExpression' : [
        'text' : 'All',
        'children' : [
          'template' : '_:$value$',
          'values' : [
            'Definitions' : [
              'processor': ({ List<Map> data ->

                def new_data = []
                data.each { Map item ->
                  switch (item."_name") {
                    case "AgentRelatedAgent" :
                      // Add a new row for each related agent.
                      (0..(item."RelatedAgent"?.size() - 1)).each { int idx ->
                        def entry = [:]

                        // Copy the whole of the data.
                        entry << item

                        // Replace the related agent with a list of just 1.
                        entry."RelatedAgent" = [item["RelatedAgent"][idx]]

                        new_data += entry
                      }
                      break

                    default :
                      // Just add the item.
                      new_data += item
                      break
                  }
                }
                if (new_data.size() > 0) {
                  // Because we want to edit the referenced data we can not create a new list,
                  // we must instead empty the old and repopulate with the new.
                  data.clear()
                  data.addAll(new_data)
                }

                // Return the data.
                data
              }),
              'text' : 'Authorised Users',
              'children': [
                'template' : "_:AgentDefinition[normalize-space(_:AgentLabel/text())='\$value\$']/_:AgentRelatedAgent",
                'values' : [
                  'AuthorizedUser' : ['text': 'Authorized User'],
                ]
              ]
            ],
            'LicenseGrant' : [
              'text' : 'License Grants'
            ],
            'UsageTerms' : [
              'processor': ({ List<Map> data ->
                def new_data = []
                def users = data.getAt(0)['User']
                def deepcopy = { orig ->
                  def bos;
                  def oos;
                  def bin;
                  def ois;
                  try{
                   bos = new ByteArrayOutputStream()
                   oos = new ObjectOutputStream(bos)
                   oos.writeObject(orig); oos.flush()
                   bin = new ByteArrayInputStream(bos.toByteArray())
                   ois = new ObjectInputStream(bin)
                   return ois.readObject()
                  }finally{
                    bos?.close()
                    oos?.close()
                    bin?.close()
                    ois?.close()
                  }
                }
                def refresh_data = {
                  if (new_data.size() > 0) {
                  // Because we want to edit the referenced data we can not create a new list,
                  // we must instead empty the old and repopulate with the new.
                  data.clear()
                  data.addAll(new_data)
                  new_data.clear()
                  }
                }
                if(users?.size() > 1){
                  users.each{ item ->
                    def copy = [:]
                    //Copy the data
                    copy << data.getAt(0)
                    //Grab the single user and add him to an array
                    def temp = [item]
                    //Then replace the data User(s) with the single User
                    copy['User'] = temp

                    new_data += copy
                  }
                }

                 refresh_data();
                //Create several rows for comparison points that need to be split
                def replicate_row = {usage,type ->
                    usage?."${type}"?.each{ method ->
                      def copy = [:]
                      copy << usage
                      def temp = [method]
                      copy[type] = temp
                      new_data += copy
                    }
                }

                def replicate_nested_row = {usage,parent,child ->
                    usage."${parent}"?."${child}"?.getAt(0)?.each{ place ->
                      def copy = [:]
                      def entry = [:]
                      copy = deepcopy(usage)
                      entry = place.clone()
                      copy."${parent}"[0]."${child}"= [entry]
                      new_data.addAll(copy)
                    }
                }
                //Need to loop we might have multiple data here, genetrated from above
                data.each{ usage ->
                  def usageType = usage."UsageType"?.getAt(0)?."_content"
                  switch (usageType){
                    case "onixPL:Access":
                      replicate_row(usage,'UsageMethod');
                      break;
                    case "onixPL:Copy":
                      replicate_row(usage,'UsagePurpose');
                      break;
                    case "onixPL:DepositInPerpetuity":
                      replicate_nested_row(usage,'UsageRelatedPlace','RelatedPlace');
                      break;
                    case "onixPL:Include":
                      if(usage.'UsageRelatedResource'?.'UsageResourceRelator'?.'_content'?.contains(['onixPL:TargetResource'])){
                        replicate_nested_row(usage,'UsageRelatedResource','RelatedResource');
                      }
                      break;
                    case "onixPL:MakeAvailable":
                      if(usage.'UsageRelatedAgent'?.'UsageAgentRelator'?.'_content'?.contains(['onixPL:ReceivingAgent'])){
                        replicate_nested_row(usage,'UsageRelatedAgent','RelatedAgent');
                      }
                      break;
                    case "onixPL:SupplyCopy":
                      if(usage.'UsageRelatedAgent'?.'UsageAgentRelator'?.'_content'?.contains(['onixPL:ReceivingAgent'])){
                        replicate_nested_row(usage,'UsageRelatedAgent','RelatedAgent');
                      }
                      break;
                    case "onixPL:Use":
                      replicate_row(usage,'UsagePurpose');
                      break;
                    case "onixPL:UseForDataMining":
                      replicate_row(usage,'UsagePurpose');
                      break;
                    default:
                        new_data += usage
                      break;
                  }

                }
                refresh_data();

                //Return the data.
                data
              }),
              'text' : 'Usage Terms',
              'children' : [
                'template' : "_:Usage[normalize-space(_:UsageType/text())='\$value\$']",
                'values' : [
                  'onixPL:Access' : ['text' :  'Access'],
                  'onixPL:Copy' : ['text' : 'Copy'],
                  'onixPL:DepositInPerpetuity' : ['text' :  'Deposit In Perpetuity'],
                  'onixPL:Include': ['text': 'Include'],
                  'onixPL:MakeAvailable': ['text': 'Make Available'],
                  'onixPL:MakeDigitalCopy' : ['text' :  'Make Digital Copy'],
                  'onixPL:Modify' : ['text' :  'Modify'],
                  'onixPL:PrintCopy' : ['text': 'PrintCopy'],
                  'onixPL:ProvideIntegratedAccess' : ['text' :  'Provide Integrated Access'],
                  'onixPL:ProvideIntegratedIndex' : ['text' :  'Provide Integrated Index'],
                  'onixPL:RemoveObscureOrModify' : ['text' :  'Remove Obscure Or Modify'],
                  'onixPL:Sell' : ['text' :  'Sell'],
                  'onixPL:SupplyCopy' : ['text' : 'Supply Copy'],
                  'onixPL:Use' : ['text': 'Use'],
                  'onixPL:UseForDataMining' : ['text':'Use For Data Mining'],


                ]
              ]
            ],
            'SupplyTerms' : [
              'text' : 'Supply Terms',
              'children' : [
                'template' : "_:SupplyTerm[normalize-space(_:SupplyTermType/text())='\$value\$']",
                'values' : [
                  'onixPL:ChangeOfOwnershipOfLicensedResource' : ['text': 'Change Of Ownership Of Licensed Resource'],
                  'onixPL:ChangesToLicensedContent' : ['text': 'Changes To Licensed Content'],
                  'onixPL:CompletenessOfContent' : ['text': 'Completeness Of Content'],
                  'onixPL:ComplianceWithAccessibilityStandards' : ['text': 'Compliance With Accessibility Standards'],
                  'onixPL:ComplianceWithONIX' : ['text': 'Compliance With ONIX'],
                  'onixPL:ComplianceWithOpenURLStandard' : ['text': 'Compliance With OpenURL Standard'],
                  'onixPL:ComplianceWithProjectTransferCode' : ['text': 'Compliance With Project Transfer Code'],
                  'onixPL:ComplianceWithStandardsAndBestPractices' : ['text': 'Compliance With Standards And Best Practices'],
                  'onixPL:ConcurrencyWithPrintVersion' : ['text': 'Concurrency With Print Version'],
                  'onixPL:ContentDelivery' : ['text': 'Content Delivery'],
                  'onixPL:ContentWarranty' : ['text': 'Content Warranty'],
                  'onixPL:LicenseeOpenAccessContent' : ['text': 'Licensee OpenAccess Content'],
                  'onixPL:MediaWarranty' : ['text': 'Licensee OpenAccess Content'],
                  'onixPL:MetadataSupply' : ['text': 'Metadata Supply'],
                  'onixPL:NetworkAccess' : ['text': 'Network Access'],
                  'onixPL:OpenAccessContent' : ['text': 'OpenAccess Content'],
                  'onixPL:ProductDocumentation' : ['text': 'Product Documentation'],
                  'onixPL:PublicationSchedule' : ['text': 'Publication Schedule'],
                  'onixPL:ServicePerformance' : ['text': 'Service Performance'],
                  'onixPL:ServicePerformanceGuarantee' : ['text': 'Service Performance Guarantee'],
                  'onixPL:StartOfService' : ['text': 'Start Of Service'],
                  'onixPL:UsageStatistics' : ['text': 'Usage Statistics'],
                  'onixPL:UserRegistration' : ['text': 'User Registration'],
                  'onixPL:UserSupport' : ['text': 'UserSupport']
                ]
              ]
            ],
            'ContinuingAccessTerms' : [
              'processor': ({ List<Map> data ->

                def new_data = []
                def deepcopy = { orig ->
                  def bos;
                  def oos;
                  def bin;
                  def ois;
                  try{
                   bos = new ByteArrayOutputStream()
                   oos = new ObjectOutputStream(bos)
                   oos.writeObject(orig); oos.flush()
                   bin = new ByteArrayInputStream(bos.toByteArray())
                   ois = new ObjectInputStream(bin)
                   return ois.readObject()
                  }finally{
                    bos?.close()
                    oos?.close()
                    bin?.close()
                    ois?.close()
                  }
                }
                data.each{access ->
                  access."ContinuingAccessTermRelatedAgent"?."RelatedAgent"?.getAt(0)?.each{ agent ->
                    def copy = [:]
                    def entry = [:]
                    copy = deepcopy(access)
                    entry = agent.clone()
                    copy."ContinuingAccessTermRelatedAgent"."RelatedAgent"[0].clear()
                    copy."ContinuingAccessTermRelatedAgent"."RelatedAgent"[0].addAll(entry)
                    new_data.addAll(copy)
                  }
                }
                if (new_data.size() > 0) {
                  // Because we want to edit the referenced data we can not create a new list,
                  // we must instead empty the old and repopulate with the new.
                  data.clear()
                  data.addAll(new_data)
                }

                data
              }),
              'text' : 'Continuing Access Terms',
              'children' : [
                'template' : "_:ContinuingAccessTerm[normalize-space(_:ContinuingAccessTermType/text())='\$value\$']",
                'values' : [
                  'onixPL:ContinuingAccess' : ['text' :  'Continuing Access' ],
                  'onixPL:ArchiveCopy' : ['text' :  'Archive Copy' ],
                  'onixPL:PostCancellationFileSupply': ['text': 'Post Cancellation File Supply'],
                  'onixPL:PostCancellationOnlineAccess': ['text': 'Post Cancellation Online Access'],
                  'onixPL:NotificationOfDarkArchive': ['text': 'Notification Of Dark Archive'],
                  'onixPL:PreservationInDarkArchive': ['text': 'Preservation In Dark Archive']
                ]
              ]
            ],
            'PaymentTerms/_:PaymentTerm' : [
              'text' : 'Payment Terms'
            ],
            'GeneralTerms/_:GeneralTerm' : [
              'text' : 'General Terms'
            ]
          ]
        ]
      ]
    ]
  ]
]

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
        html: ['text/html','application/xhtml+xml'],
        xml: ['text/xml', 'application/xml'],
        text: 'text/plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        all: '*/*',
        json: ['application/json','text/json'],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data'
]

grails {
    cache {
        enabled = true
    }
}

grails.cache.config = {
    // affects only cache-plugin caches
    cache {
        name = 'laser_static_pages'
    }
    cache {
        name 'message'
    }
}

// postgresql sequences for primary keys
grails.gorm.default.mapping = {
   id generator: 'identity'
}

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

// set per-environment serverURL stem for creating absolute links
environments {
    development {
        grails.logging.jul.usebridge = true
        grails.serverURL = "http://localhost:8080/laser"
    }
    production {
        grails.logging.jul.usebridge = false
        grails.serverURL = "http://localhost:8080/laser" // override in local config (laser-config.groovy)
    }
}

basicDataPath = documentStorageLocation+'/basic_data_dumps/'
basicDataFileName = 'basicDataDump.xml'

subscriptionTransforms = [
    'oclc':[name:'OCLC Resolver', xsl:'oclc.xslt', returnFileExtention:'txt', returnMime:'text/plain'],
    'ss':[name:'Serials Solutions Resolver', xsl:'serialssolutions.xslt', returnFileExtention:'txt', returnMime:'text/plain'],
    'sfx':[name:'SFX Resolver', xsl:'SFX.xslt', returnFileExtention:'txt', returnMime:'text/plain'],
    //'kbplus':[name:'KBPlus (CSV)', xsl:'kbplusimp.xsl', returnFileExtention:'txt', returnMime:'text/plain'],
    //'kbart2':[name:'KBART II', xsl:'kbartii.xsl', returnFileExtention:'tsv', returnMime:'text/tab-separated-values']
]

// KBPlus import not available in titlelist because we need sub id and it's possible for multiple IEs to appear
// per title, which isn't valid inside a KB+ package file
titlelistTransforms = [
    'oclc':[name:'OCLC Resolver', xsl:'oclc.xslt', returnFileExtention:'txt', returnMime:'text/plain'],
    'ss':[name:'Serials Solutions Resolver', xsl:'serialssolutions.xslt', returnFileExtention:'txt', returnMime:'text/plain'],
    'sfx':[name:'SFX Resolver', xsl:'SFX.xslt', returnFileExtention:'txt', returnMime:'text/plain'],
]

packageTransforms = [
    //'kbplus':[name:'KBPlus(CSV)', xsl:'kbplusimp.xsl', returnFileExtention:'csv', returnMime:'text/csv'],
    //'kbart2':[name:'KBART II', xsl:'kbartii.xsl', returnFileExtention:'tsv', returnMime:'text/tab-separated-values']
]
licenseTransforms = [
    'sub_ie':[name:'Licensed Issue Entitlements (CSV)', xsl:'licensed_titles.xsl', returnFileExtention:'csv', returnMime:'text/csv'],
    'sub_pkg':[name:'Licensed Subscriptions/Packages (CSV)', xsl:'licensed_subscriptions_packages.xsl', returnFileExtention:'csv', returnMime:'text/csv']
]


// Log directory/created in current working dir if tomcat var not found.
def logWatchFile

// First lets see if we have a log file present.
def base = System.getProperty("catalina.base")
if (base) {
    logWatchFile = new File ("${base}/logs/catalina.out")

   if (!logWatchFile.exists()) {
        // Need to create one in current context.
        base = false;
   }
}

if (!base) {
    logWatchFile = new File("logs/${appName}-${appVersion}.log")
}

// Log file variable.
def logFile = logWatchFile.canonicalPath

System.out.println("~ using log file location: ${logFile}")

// Also add it as config value too.
log_location = logFile

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
    if (!base) {
      appender new RollingFileAppender(
          name: 'dailyAppender',
          fileName: (logFile),
          layout: pattern(conversionPattern:'%d [%t] %-5p %c{2} %x - %m%n')
      )
    }
  }
  root {
    if (!base) {
      error 'stdout', 'dailyAppender'
    } else {
      error 'stdout'
    }
  }
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
      'grails.app.service',
      'grails.app.services',
      'grails.app.domain',
      'grails.app.conf',
      'grails.app.jobs',
      'grails.app.conf.BootStrap',
      'grails.app.controllers.OrganisationController',
      //'edu.umn.shibboleth.sp',
      'com.k_int',
  // 'org.springframework.security'
      'grails.app.taglib.InplaceTagLib'

  // info   'com.linkedin.grails'
}

// Added by the Spring Security Core plugin:
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

//grails.plugin.springsecurity.
//grails.plugin.springsecurity.useSessionFixationPrevention      = false // 2.0

grails.plugin.springsecurity.providerNames = [
        'preAuthenticatedAuthenticationProvider',
        'daoAuthenticationProvider' //,
        // 'anonymousAuthenticationProvider' //,
        // 'rememberMeAuthenticationProvider'
]

grails.plugin.springsecurity.roleHierarchy = '''
    ROLE_YODA > ROLE_ADMIN
    ROLE_ADMIN > ROLE_DATAMANAGER
    ROLE_DATAMANAGER > ROLE_GLOBAL_DATA
    ROLE_GLOBAL_DATA > ROLE_USER
'''

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        [pattern: '/monitoring',                access: ['ROLE_YODA']],
        [pattern: '/swagger/v0/laser.yaml.gsp', access: ['permitAll']]
]

auditLog {
    logFullClassName = true
    stampEnabled = false // 1.1.3
    stampAlways = false // 1.1.3
    cacheDisabled = true // 1.1.3

  actorClosure = { request, session ->

    if (request.applicationContext.springSecurityService.principal instanceof java.lang.String) {
      return request.applicationContext.springSecurityService.principal
    }

    def username = request.applicationContext.springSecurityService.principal?.username

    if (SpringSecurityUtils.isSwitched()){
      username = SpringSecurityUtils.switchedUserOriginalUsername+" AS "+username
    }

      return (username in [null, 'anonymousUser', 'system']) ? username : 'anonymised'
  }
}


appDefaultPrefs {
    globalDatepickerFormat    = 'yyyy-mm-dd'
    globalDateFormat          = 'yyyy-MM-dd'
    globalDateFormatSQL       = '%Y-%m-%d'
}

// The following 2 entries make the app use basic auth by default
// grails.plugins.springsecurity.useBasicAuth = true
// grails.plugins.springsecurity.basic.realmName = "KBPlus"

// II : This doesn't work because we are calling registerFilter to install the ediauth filter.. need to find a different solution, which is annoying
// See http://jira.grails.org/browse/GPSPRINGSECURITYCORE-210
// This stanza then says everything should use form apart from /api
// More info: http://stackoverflow.com/questions/7065089/how-to-configure-grails-spring-authentication-scheme-per-url
// grails.plugins.springsecurity.filterChain.chainMap = [
//    '/api/**': 'JOINED_FILTERS,-exceptionTranslationFilter',
//    '/**': 'JOINED_FILTERS,-basicAuthenticationFilter,-basicExceptionTranslationFilter'
// ]

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line
 // GSP settings
 grails {
 views {
 gsp {
 encoding = 'UTF-8'
 htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
 codecs {
 expression = 'html' // escapes values inside null
 scriptlet = 'none' // escapes output from scriptlets in GSPs
 taglib = 'none' // escapes output from taglibs
 staticparts = 'none' // escapes output from static template parts
 }
 }
 // escapes all not-encoded output at final stage of outputting
 filteringCodecForContentType {
 //'text/html' = 'html'
 }
 }
 }
 remove this line */

quartzHeartbeat = 'Never'
// grails.databinding.dateFormats = ['MMddyyyy', 'yyyy-MM-dd HH:mm:ss.S', "yyyy-MM-dd'T'hh:mm:ss'Z'"]

//grails.mail.default.from = "server@yourhost.com" //override system wide
grails.mail.disabled = false //System wide
grails.mail.poolSize = 20 //default 5 emails at a time, then que based system (prereq = async true)
//grails.mail.overrideAddress="ryan@k-int.com" //Test env only, overrides to and from address
//grails.mail.port = 30//TODO: Diese Zeile fürs Deploy entfernen!!!
notifications.email.from = 'laser_support@hbz-nrw.de'
notifications.email.replyTo = 'laser_support@hbz-nrw.de'
notifications.email.genericTemplate = true //If enabled, no customisation in email i.e. Reminder inst info, User info... Else, Customised template will be sent to user
systemEmail = 'laser_support@hbz-nrw.de'

//Finance
grails.plugins.remotepagination.enableBootstrap = true
financials.currency = "EUR|GBP|USD|CHF" //List in priority of order

defaultOaiConfig = [
  serverName: 'K-Int generic Grails OAI Module :: KBPlus.ac.uk',
  lastModified:'lastUpdated',
  serverEmail:'laser@laser.laser',
  schemas:[
    'oai_dc':[
      type:'method',
      methodName:'toOaiDcXml',
      schema:'http://www.openarchives.org/OAI/2.0/oai_dc.xsd',
      metadataNamespaces: [
        '_default_' : 'http://www.openarchives.org/OAI/2.0/oai_dc/',
        'dc'        : "http://purl.org/dc/elements/1.1/"
      ]],
    'kbplus':[
      type:'method',
      methodName:'toKBPlus',
      schema:'http://www.kbplus.ac.uk/schemas/oai_metadata.xsd',
      metadataNamespaces: [
        '_default_': 'http://www.kbplus.ac.uk/oai_metadata/'
      ]],
  ]
]

