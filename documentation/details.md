
## Details: Upgrade from [Grails 6.1.2](./grails6/details.md) to Grails 6.2.3

### Database Migration Plugin

 * Dependency *org.grails:grails-shell:6.1.2* added -> TODO: remove workaround
 * Database Migration Plugin will be merged into **gorm-hibernate5**  (9.0.0-M3)

### Asset Pipeline Plugin

Upgrade to 5.x -> Switch back to usual asset include for swagger-ui

### Grails Mail Plugin

Configuration *grails.mail.port* in *laser3-config.groovy* no longer works in version 4.x -> For local tests, the configuration must be set in *application.yml*

### ExecutorGrailsPlugin

*org.grails.plugins:grails-executor* is deprecated as Grails already has similar functionality build in -> TODO: replacement

### HTTP Status Codes

    401 Unauthorized            - authentication/login is required (only)
    403 Forbidden               - missing permissions   
    500 Internal Server Error   - processing failed due unexpected conditions

### Localization

Localization dependent logic should be managed by *de.laser.utils.LocaleUtils* to ensure consistent behavior.

### Naming Convention
  * **Private methods** in domain classes, controllers, and services should be named with a leading underscore to avoid confusion. 
    In general, some of the currently found constructs should be reconsidered!
  * **Helper and Utils** - Helper classes should be defined in affected packages or in *de.laser.helper* but named according to the context. Global utilities should be defined in *de.laser.utils*

### Passwords

By default the Spring Security plugin uses the **bcrypt** algorithm to hash passwords.
Important: The password encoder still accepts legacy passwords, but encrypts them with bcrypt if they are changed.

### Quartz jobs

*de.laser.base.AbstractJob* offers new **start** and **stop** methods to simplify and standardize job implementations. 
These should be used to wrap the execution logic.

### RandomUtils

*de.laser.utils.RandomUtils* offers various methods for generating random IDs, UUIDs, passwords and more.

### Service usage in Domain Classes

Autowiring of domain instances has been **disabled** because it represents a performance bottleneck.
Use BeanStore for static and non-static access to services and other beans.

    de.laser.storage.BeanStore

    static ContextService getContextService() {
        Holders.grailsApplication.mainContext.getBean('contextService') as ContextService
    }

### Websockets

Todo: Websocket communication is broken after upgrading from Grails 4 to Grails 5.

***  

### Remarks

* #### Apache Commons IO

    *org.apache.commons.io* is only used in one file (Api).

* #### GPars

    *org.codehaus.gpars:gpars* is outdated and only used in ~~one file~~ two files (StatsSync, Yoda)

* #### Juniversalchardet

    *com.github.albfernandez:juniversalchardet* is ~~only used in one file.~~ used in three files  (AjaxHtml, MyInstitution, Survey).



