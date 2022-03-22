package de.laser.helper

import grails.util.Environment
import grails.util.Holders
import grails.core.GrailsClass

class AppUtils {

    public static final PROD    = 'PROD'
    public static final QA      = 'QA'
    public static final DEV     = 'DEV'
    public static final LOCAL   = 'LOCAL'

    // -- Server

    static String getCurrentServer() {
        // laserSystemId mapping for runtime check; do not delete

        if (! Environment.isDevelopmentMode()) {

            switch (ConfigUtils.getLaserSystemId()) {
                case 'LAS:eR-Dev':
                case 'LAS:eR-Dev @ Grails3': // TODO - remove
                case 'LAS:eR-Dev @ Grails4': // TODO - remove
                    return DEV
                    break
                case 'LAS:eR-QA/Stage':
                case 'LAS:eR-QA/Stage @ Grails3': // TODO - remove
                case 'LAS:eR-QA/Stage @ Grails4': // TODO - remove
                    return QA
                    break
                case 'LAS:eR-Productive':
                    return PROD
                    break
            }
        }

        return LOCAL
    }

    // -- App

    static String getMeta(String token) {
        Holders.grailsApplication.metadata.get( token ) ?: token
    }
    static def getConfig(String token) {
        ConfigUtils.readConfig( token, false )
    }
    static def getPluginConfig(String token) {
        ConfigUtils.readConfig( 'grails.plugin.' + token, false )
    }

    // -- DC

    static GrailsClass getDomainClass(String qualifiedName) {
        // fallback
        String fallback = qualifiedName.replace("class ", "")
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
        Holders.grailsApplication.getArtefacts("Domain").toList()
    }
}
