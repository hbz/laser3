
## Upgrade from Grails 3.3.11 to Grails 5.1.8

### SpringBootDeveloperTools

Previous versions of Grails used a reloading agent called SpringLoaded.
Since this library is no longer maintained and does not support Java 11 it has been removed.
As a replacement, SpringBootDeveloperTools is used.

Deploying code changes is configured to watch a [trigger file](../grails-app/conf/spring/restart.trigger).
Use the Gradle task **devtools.triggerRestart** to update this file and force a restart with the latest code changes.

### Apache Commons Lang

Two different versions are currently used simultaneously. New code should always use *org.apache.commons.lang3*, not the previous version *org.apache.commons.lang*.

### Configuration

Accessing configuration through dot notation *(config.a.b.c)* has been **deprecated**. The configuration should be accessed via *de.laser.helper.ConfigMapper* so that settings can be validated at any time.

### Database Migrations

A new naming scheme should be used. See [database-migration.md](./database-migration.md) for more information.

### DateUtils

*Date.parse()* is **deprecated**, *Date.format()* has been **removed** in Java 11. New code should use e.g. SimpleDateFormat to format dates.
To avoid confusion, the difference between localized and fixed usage in *de.laser.helder.Dateutils* is now reflected in a new naming scheme.

### Fallbacks

The following fallbacks have been set for faster migration. They can be treated later.

    grails.views.gsp.codecs.scriptlet: none
    hibernate.allow_update_outside_transaction: true

### HTTPBuilder

*org.codehaus.groovy.modules.http-builder:http-builder* is outdated.
A migration to *de.laser.http.BasicHttpClient* should take place.

### Localization

Localization dependent logic should be managed by *de.laser.helper.LocaleUtils* to ensure consistent behavior.

### Logging

*Static_logger* has been removed. New code should use **@Slf4j** as class annotation to enable logging in static contexts.

### Passwords

By default the Spring Security plugin uses the **bcrypt** algorithm to hash passwords.
Important: The password encoder still accepts legacy passwords, but encrypts them with bcrypt if they are changed.

### Quartz jobs

*de.laser.base.AbstractJob* offers new **start** and **stop** methods to simplify and unify job implementations. 
These should be used to wrap the execution logic.

### ReactiveX

*io.reactivex:rxjava* and *org.grails.plugins:rxjava* are outdated. Refactoring should take place.

### Service usage in domain classes

Autowiring of domain instances has been **disabled** because it represents a performance bottleneck.
Use BeanStore for static and non-static access to services and other beans.

    de.laser.storage.BeanStore

    static ContextService getContextService() {
        Holders.grailsApplication.mainContext.getBean('contextService') as ContextService
    }

### Remarks &rarr;

#### Apache Commons IO

*org.apache.commons.io* is only used in one file.

#### ~~CSV~~

~~*Opencsv* has been removed. New code should use *liquibase.util.csv*.~~

#### ExecutorGrailsPlugin !

*org.grails.plugins:grails-executor* is deprecated. Replacement should take place.

#### GPars

*org.codehaus.gpars:gpars* is only used in one file.

#### Juniversalchardet

*com.github.albfernandez:juniversalchardet* is only used in one file.



