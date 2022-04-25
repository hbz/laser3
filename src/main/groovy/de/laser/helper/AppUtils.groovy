package de.laser.helper

import de.laser.ContextService
import de.laser.storage.BeanStorage
import grails.util.Environment
import grails.util.Holders
import grails.core.GrailsClass

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

            switch (ConfigUtils.getLaserSystemId()) {
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
    static def getConfig(String token) {
        ConfigUtils.readConfig( token, false )
    }
    static def getPluginConfig(String token) {
        ConfigUtils.readConfig( 'grails.plugin.' + token, false )
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
        ContextService contextService = BeanStorage.getContextService()
        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        sessionCache.get( AU_S_DEBUGMODE ) == 'on'
    }

    static void setDebugMode(String status) {
        ContextService contextService = BeanStorage.getContextService()
        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        if (status?.toLowerCase() in ['true', 'on']) {
            sessionCache.put( AU_S_DEBUGMODE, 'on' )
        }
        else if (status?.toLowerCase() in ['false', 'off']) {
            sessionCache.put( AU_S_DEBUGMODE, 'off' )
        }
    }

    // -- domain classes

    static GrailsClass getDomainClass(String qualifiedName) {
        // fallback
        String fallback = qualifiedName.replace('class ', '')
        GrailsClass dc = Holders.grailsApplication.getArtefact('Domain', fallback)

        if (! dc) {
            println "WARNING: AppUtils.getDomainClass( ${qualifiedName} ) found no result"
        }
        dc
    }

    static GrailsClass getDomainClassGeneric(String name) {
        GrailsClass dc
        List<String> namespaces = [ 'de.laser', 'com.k_int.kbplus' ]

        for (String ns : namespaces) {
            dc = Holders.grailsApplication.getArtefact('Domain', ns + '.' + name)
            if (dc) { break }
        }
        if (! dc) {
            println "WARNING: AppUtils.getDomainClassGeneric( ${name} ) found no result"
        }
        dc
    }

    static List<GrailsClass> getAllDomainClasses() {
        Holders.grailsApplication.getArtefacts('Domain').toList()
    }
}
