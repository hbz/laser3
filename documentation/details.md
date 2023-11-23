
## Details: Upgrade from [Grails 5.3.3](./grails5/details.md) to Grails 6.0.0

### ExecutorGrailsPlugin

*org.grails.plugins:grails-executor* is deprecated as Grails already has similar functionality build in. Replacement should take place.

### ~~Fallbacks~~

~~The following fallbacks have been set for faster migration. They can be treated later.~~

* ~~hibernate.allow_update_outside_transaction: true~~

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

*de.laser.base.AbstractJob* offers new **start** and **stop** methods to simplify and unify job implementations. 
These should be used to wrap the execution logic.

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

    *org.codehaus.gpars:gpars* is outdated and only used in ~~one file~~ two files.

* #### Juniversalchardet

    *com.github.albfernandez:juniversalchardet* is ~~only used in one file.~~ used in two files.



