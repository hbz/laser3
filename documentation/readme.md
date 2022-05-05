
## Upgrade to Grails 4.0.13

### SpringBootDeveloperTools

Previous versions of Grails used a reloading agent called SpringLoaded. 
Since this library is no longer maintained and does not support Java 11 it has been removed. 
As a replacement, SpringBootDeveloperTools is used.

Deploying code changes is configured to watch a [trigger file](../grails-app/conf/spring/restart.trigger). 
Use the Gradle task **devtools.triggerRestart** to update this file and force a restart with the latest code changes.

### Fallbacks

The following fallbacks have been set for faster migration. They can be treated later.

    grails.views.gsp.codecs.scriptlet: none
    hibernate.allow_update_outside_transaction: true

### Passwords

By default the Spring Security plugin uses the **bcrypt** algorithm to hash passwords.
Important: The password encoder still accepts legacy passwords, but encrypts them with bcrypt if they are changed.

### Configuration

The configuration should be accessed via *de.laser.helper.ConfigMapper* so that settings can be validated at any time.

### Localization

Localization dependent logic should be managed by *de.laser.helper.LocaleHelper* to ensure consistent behavior.

### Service usage in domain classes

Autowiring of domain instances has been **disabled** because it represents a performance bottleneck.
Use BeanStore for static and non-static access to services and other beans.

    de.laser.storage.BeanStore

    static ContextService getContextService() {
        Holders.grailsApplication.mainContext.getBean('contextService') as ContextService
    }

### HTTPBuilder

*org.codehaus.groovy.modules.http-builder:http-builder* is outdated.
A migration to *de.laser.http.BasicHttpClient* should take place.

### CSV

*Opencsv* has been removed. New code should use *liquibase.util.csv*.

### Apache Commons Lang

Two different versions are currently used simultaneously. New code should always use *org.apache.commons.lang3*, not the previous version *org.apache.commons.lang*.

### .. todo?

#### ExecutorGrailsPlugin

*org.grails.plugins:grails-executor* is deprecated. Replacement should take place.

#### Apache Commons IO

*org.apache.commons.io* is only used in one file.

#### Juniversalchardet

*com.github.albfernandez:juniversalchardet* is only used in one file.

#### GPars

*org.codehaus.gpars:gpars* is only used in one file.
