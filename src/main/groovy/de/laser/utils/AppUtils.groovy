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

    public static final String PROD    = 'PROD'
    public static final String QA      = 'QA'
    public static final String DEV     = 'DEV'
    public static final String LOCAL   = 'LOCAL'

    public static final String AU_S_DEBUGMODE = 'AppUtils/Session/DebugMode'

    // -- server

    /**
     * Returns the currently valid server environment
     * @return one of the currently defined environment constants:
     * <ul>
     *     <li>{@link #LOCAL}</li>
     *     <li>{@link #DEV}</li>
     *     <li>{@link #QA}</li>
     *     <li>{@link #PROD}</li>
     * </ul>
     */
    static String getCurrentServer() {
        // laserSystemId mapping for runtime check; do not delete

        if (! Environment.isDevelopmentMode()) {

            switch (ConfigMapper.getLaserSystemId()) {
                case { it.startsWithIgnoreCase('LAS:eR-DEV') }:
                    return DEV
                    break
                case { it.startsWithIgnoreCase('LAS:eR-QA') }:
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

    /**
     * Gets metadata about the app
     * @param token the metadata token to be returned
     * @return the value defined for the given token
     */
    static String getMeta(String token) {
        Holders.grailsApplication.metadata.get( token ) ?: token
    }

    // -- debug mode

    /**
     * Returns whether debug mode is currently activated
     * @return true if debug mode is activated, false otherwise
     */
    static boolean isDebugMode() {
        ContextService contextService = BeanStore.getContextService()
        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        sessionCache.get( AU_S_DEBUGMODE ) == 'on'
    }

    /**
     * Sets the debug mode flag to the given setting
     * @param status on or true to activate it, off or false to deactivate
     */
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

    /**
     * Returns the current filesystem document storage information
     * @return path, size and count of files currently in the datastore in a {@link Map}
     */
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
