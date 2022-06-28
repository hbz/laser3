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
}
