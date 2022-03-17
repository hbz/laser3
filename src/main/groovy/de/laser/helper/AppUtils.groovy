package de.laser.helper

import grails.util.Holders
import grails.core.GrailsClass

class AppUtils {

    // --

    static String getMeta(String token) {
        Holders.grailsApplication.metadata[ token ] ?: token
    }
    static def getConfig(String token) {
        Holders.grailsApplication.config[ token ] ?: null
    }
    static def getPluginConfig(String token) {
        Holders.grailsApplication.config[ 'grails.plugin.' + token ] ?: null
    }

    // --

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
