
## Upgrade to Grails 4.0.13

### SpringBootDeveloperTools

Previous versions of Grails used a reloading agent called SpringLoaded. 
Since this library is no longer maintained and does not support Java 11 it has been removed. 
As a replacement, SpringBootDeveloperTools is used.

Deploying code changes is configured now via trigger file. Use the Gradle task **devtools.triggerRestart** to update this file and force Devtools to restart the app with current code changes.

### Passwords

By default the Spring Security plugin uses the **bcrypt** algorithm to hash passwords.
Important: The password encoder still accepts legacy passwords, but encrypts them with bcrypt if they are changed.

### Service usage in domain classes

Autowiring of domain instances has been **disabled** because it represents a performance bottleneck.
Use BeanStorage for static and non-static access to services and other beans.

    de.laser.storage.BeanStorage

    static ContextService getContextService() {
        Holders.grailsApplication.mainContext.getBean('contextService') as ContextService
    }

### Fallbacks

The following fallbacks have been set for faster migration. They can be treated later.

    grails.views.gsp.codecs.scriptlet: none
    hibernate.allow_update_outside_transaction: true

### HTTPBuilder

*org.codehaus.groovy.modules.http-builder:http-builder* is outdated. A migration to *io.micronaut:micronaut-http-client* (or alternative) should take place.
