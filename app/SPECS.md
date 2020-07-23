
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

Access *user depending ehcache* with key prefix due **de.laser.ContextService**.
For more functionality use **de.laser.CacheService** directly.

    def ctxCache = contextService.getCache('ProfileController/properties/')
    ctxCache.put('myObj', myObj)
    ctxCache.get('myObj')

Or use *global caches* with *cacheKeyPrefix* like this:
  
    def globalCache30m = cacheService.getTTL1800Cache('ProfileController/properties/')
    def globalCache5m = cacheService.getTTL300Cache('ProfileController/properties/')
    
## Naming Conventions

#### Derived Attributes

Attributes, that are obtained from other objects if not set. 

    ObjA.getDerivedAttribute() {
        attribute ?: ObjB.attribute
    }

#### Calculated Attributes

Non-persistent Attributes, that are calculated realtime.

    ObjA._getCalculatedAttribute() {
        attributeC * attributeD * 0.19
    }