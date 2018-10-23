
## General Specs for LAS:eR Development

#### Current Context

Use **de.laser.ContextService** to access current user, 
context organisation and authorized memberships.

#### GlobalUID

Extend **de.laser.domain.AbstractBaseDomain** to use the globalUID attribute 
in your domain class.

#### Translation via i10n

Extend **de.laser.domain.AbstractI10nTranslatable** to support
db based translation mechanism in your domain class.

#### Database Migration Scripts

Additionally execute *changelog-xyz-post.sql* scripts after
running *dbm-update* (for *changelog-xyz.groovy*) to clean up or migrate existing data.

#### Securing controller

Use **com.k_int.kbplus.auth.User.hasAffiliation('MIN_INST_ROLE')** to secure actions.

    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })

Use **DebugAnnotation** for dynamic documentation.

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    
#### Service Injection

Mainly used services are available in GSPs due the following custom tag.

    <laser:serviceInjection />
    
#### Using Caches

Access *user depending ehcache* due **de.laser.ContextService**.
For more functionality use **de.laser.CacheService** directly.

    def cache = contextService.getCache()
    def cacheKeyPrefix = 'ProfileController/properties/'
    cacheService.put(cache, cacheKeyPrefix + 'myObj', myObj)
    cacheService.get(cache, cacheKeyPrefix + 'myObj')
    
