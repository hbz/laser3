package de.laser

/*  WORK IN PROGRESS */

import grails.test.mixin.TestFor
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
@TestFor(CacheService)
class CacheServiceTest extends Specification {

    def ehcacheManager
    def plugincacheManager

    def "GetCacheManager"() {

        when:

        ehcacheManager = service.getCacheManager(CacheService.EHCACHE)
        //plugincacheManager = service.getCacheManager(CacheService.PLUGINCACHE)

        then:

        assertNotNull(ehcacheManager)
        ehcacheManager.class.name == 'net.sf.ehcache.CacheManager'
        //plugincacheManager.class.name == 'grails.plugin.cache.GrailsConcurrentMapCacheManager'
        assertNull(service.getCacheManager(null))
    }

    def "GetCache"() {

        when:

        ehcacheManager = service.getCacheManager(CacheService.EHCACHE)
        //plugincacheManager = service.getCacheManager(CacheService.PLUGINCACHE)

        then:

        assertNotNull(ehcacheManager)
        assertNotNull(service.getCache(ehcacheManager, 'cache-abc'))
        //service.getCache(plugincacheManager, 'cache-xyz')

    }

    /*
    def "GetTTL300Cache"() {
    }

    def "GetTTL1800Cache"() {
    }

    def "Put"() {
    }

    def "Get"() {
    }

    def "Clear"() {
    }
    */
}
