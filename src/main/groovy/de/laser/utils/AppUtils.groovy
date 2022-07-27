package de.laser.utils

import de.laser.ContextService
import de.laser.cache.SessionCacheWrapper
import de.laser.config.ConfigDefaults
import de.laser.config.ConfigMapper
import de.laser.storage.BeanStore
import grails.util.Environment
import grails.util.Holders
import groovy.util.logging.Slf4j

import java.nio.file.Files
import java.nio.file.Paths

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
                case { it.startsWithIgnoreCase('LAS:eR-DEV') }:
                    return DEV
                    break
                case { it.startsWithIgnoreCase('LAS:eR-QA/Stage') }:
                    return QA
                    break
                case { it.equalsIgnoreCase('LAS:eR-Productive') }:
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
            FileReader fr = new FileReader( ConfigDefaults.DEVTOOLS_TRIGGER_FILE )
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

    // --

    static Map<String, Object> getDocumentStorageInfo() {
        Map<String, Object> info = [
                folderPath : ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK,
                folderSize : '?',
                filesCount : '?'
        ]
        try {
            File folder = new File( info.folderPath as String )
            if (folder.exists()) {
                info.folderSize = (folder.directorySize() / 1024 / 1024).round(2)
                info.filesCount = Files.walk(Paths.get( info.folderPath as String )).filter(Files::isRegularFile).toArray().size()
            }
        }
        catch (Exception e) {}
        info
    }
}
