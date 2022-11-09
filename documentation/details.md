
## Upgrade from Grails 3.3.11 to Grails 5.2.4

### SpringBootDeveloperTools

Previous versions of Grails used a reloading agent called SpringLoaded.
Since this library is no longer maintained and does not support Java 11 it has been removed.
As a replacement, SpringBootDeveloperTools is used.

~~Deploying code changes is configured to watch a [trigger file](../grails-app/conf/spring/restart.trigger).~~
~~Use the Gradle task **devtools.triggerRestart** to update this file and force a restart with the latest code changes.~~

### Apache Commons Lang

New code should always use *org.apache.commons.lang3*, 
not the previous version *org.apache.commons.lang* (which is still a dependency of _org.grails.plugins:gsp_)

### Configuration

Accessing configuration through dot notation *(config.a.b.c)* has been **deprecated**. 
The configuration should be accessed via *de.laser.config.ConfigMapper* so that settings can be maintained and validated at any time.
Fallbacks and default values should be stored in *de.laser.config.ConfigDefaults*

### Custom Tags

Default namespace for custom tags has been changed from **semui** to **ui**. System related namespace is still **laser**. 
Domain specific namespaces have been added, e.g. _uiReporting_

### Database Migrations

A new naming scheme should be used. See [database-migration.md](./database-migration.md) for more information.

### DateUtils

*Date.parse()* is **deprecated**, *Date.format()* has been **removed** in Java 11. New code should use e.g. SimpleDateFormat to format dates.
To avoid confusion, *de.laser.utils.DateUtils* reflects the difference between localized and fixed pattern/output in a new naming scheme.

### ExecutorGrailsPlugin

*org.grails.plugins:grails-executor* is deprecated as Grails already has similar functionality build in. Replacement should take place.

### ~~Fallbacks~~

~~The following fallbacks have been set for faster migration. They can be treated later.~~

* ~~hibernate.allow_update_outside_transaction: true~~

### Git

The produktive branch has been renamed from *master* to *main*.

### ~~HTTPBuilder~~

~~*org.codehaus.groovy.modules.http-builder:http-builder* is outdated.
A migration to *de.laser.http.BasicHttpClient* should take place.~~

### HTTP Status Codes

    401 Unauthorized            - authentication/login is required (only)
    403 Forbidden               - missing permissions   
    500 Internal Server Error   - processing failed due unexpected conditions

### Localization

Localization dependent logic should be managed by *de.laser.utils.LocaleUtils* to ensure consistent behavior.

### Logging

*Static_logger* has been removed. New code has to use **@Slf4j** as class annotation to enable logging in static contexts.

### Naming Convention
  * **Private methods** in domain classes, controllers, and services should be named with a leading underscore to avoid confusion. 
    In general, some of the currently found constructs should be reconsidered!
  * **Helper and Utils** - Helper classes should be defined in affected packages or in *de.laser.helper* but named according to the context. Global utilities should be defined in *de.laser.utils*

### Opencsv

*com.opencsv.** has been removed. New code has to use *liquibase.repackaged.com.opencsv.**

### Passwords

By default the Spring Security plugin uses the **bcrypt** algorithm to hash passwords.
Important: The password encoder still accepts legacy passwords, but encrypts them with bcrypt if they are changed.

### Quartz jobs

*de.laser.base.AbstractJob* offers new **start** and **stop** methods to simplify and unify job implementations. 
These should be used to wrap the execution logic.

### ~~ReactiveX~~

~~*io.reactivex:rxjava* and *org.grails.plugins:rxjava* are outdated. Refactoring should take place.~~

### Refdata & Constants

Holders of refdata and constants have been moved to *de.laser.storage*

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

    *org.apache.commons.io* is only used in one file.

* #### GPars

    *org.codehaus.gpars:gpars* is outdated and only used in one file.

* #### Juniversalchardet

    *com.github.albfernandez:juniversalchardet* is ~~only used in one file.~~ used in two files.



