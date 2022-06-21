package de.laser.utils

import de.laser.ContextService
import de.laser.cache.SessionCacheWrapper
import de.laser.storage.BeanStore
import grails.util.Environment
import grails.util.Holders
import grails.core.GrailsClass
import groovy.util.logging.Slf4j

/**
 * Util class for determining domain classes
 */
@Slf4j
class AppUtils {

    public static final PROD    = 'PROD'
    public static final QA      = 'QA'
    public static final DEV     = 'DEV'
    public static final LOCAL   = 'LOCAL'

    final static String AU_S_DEBUGMODE = 'AppUtils/Session/DebugMode'

    // -- server

    static String getCurrentServer() {
        // laserSystemId mapping for runtime check; do not delete

        if (! Environment.isDevelopmentMode()) {

            switch (ConfigMapper.getLaserSystemId()) {
                case { it.startsWith('LAS:eR-Dev') }:
                    return DEV
                    break
                case { it.startsWith('LAS:eR-QA/Stage') }:
                    return QA
                    break
                case 'LAS:eR-Productive':
                    return PROD
                    break
            }
        }

        return LOCAL
    }

    // -- app

    static String getMeta(String token) {
        Holders.grailsApplication.metadata.get( token ) ?: token
    }

    // -- devtools

    static boolean isRestartedByDevtools() {
        try {
            FileReader fr = new FileReader('./grails-app/conf/spring/restart.trigger')
            if (fr) {
                Long ts = Long.parseLong(fr.readLine())
                if (30000 > (System.currentTimeMillis() - ts)) {
                    return true
                }
            }
        } catch (Exception e) {}

        false
    }

    // -- debug mode

    static boolean isDebugMode() {
        ContextService contextService = BeanStore.getContextService()
        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        sessionCache.get( AU_S_DEBUGMODE ) == 'on'
    }

    static void setDebugMode(String status) {
        ContextService contextService = BeanStore.getContextService()
        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        if (status?.toLowerCase() in ['true', 'on']) {
            sessionCache.put( AU_S_DEBUGMODE, 'on' )
        }
        else if (status?.toLowerCase() in ['false', 'off']) {
            sessionCache.put( AU_S_DEBUGMODE, 'off' )
        }
    }

    // -- domain classes

    /**
     * Gets the domain class entity for the given class name. The name has to be complete
     * @param qualifiedName the fully qualified name of the class to retrieve
     * @return the {@link GrailsClass} instance
     */
    static GrailsClass getDomainClass(String qualifiedName) {
        // fallback
        String fallback = qualifiedName.replace('class ', '')
        GrailsClass dc = Holders.grailsApplication.getArtefact('Domain', fallback)

        if (! dc) {
            log.warn "Found no result - getDomainClass( ${qualifiedName} )"
        }
        dc
    }

    /**
     * Gets the domain class entity for the given class name. The name can be completed, i.e. does not need to be fully qualified
     * @param qualifiedName the name of the class to retrieve
     * @return the {@link GrailsClass} instance
     */
    static GrailsClass getDomainClassGeneric(String name) {
        GrailsClass dc
        List<String> namespaces = [ 'de.laser' ]

        for (String ns : namespaces) {
            dc = Holders.grailsApplication.getArtefact('Domain', ns + '.' + name)
            if (dc) { break }
        }
        if (! dc) {
            log.warn "Found no result - getDomainClassGeneric( ${name} )"
        }
        dc
    }

    /**
     * Returns a complete list of currently registered domain classes
     * @return a list of {@link GrailsClass} entitles
     */
    static List<GrailsClass> getAllDomainClasses() {
        Holders.grailsApplication.getArtefacts('Domain').toList()
    }
}
