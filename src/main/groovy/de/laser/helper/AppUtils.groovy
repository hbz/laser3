package de.laser.helper

import grails.util.Holders
import grails.core.GrailsClass

/**
 * Util class for determining domain classes
 */
class AppUtils {

    /**
     * Gets the domain class entity for the given class name. The name has to be complete
     * @param qualifiedName the fully qualified name of the class to retrieve
     * @return the {@link GrailsClass} instance
     */
    static GrailsClass getDomainClass(String qualifiedName) {
        // fallback
        String fallback = qualifiedName.replace("class ", "")
        GrailsClass dc = Holders.grailsApplication.getArtefact('Domain', fallback)

        if (! dc) {
            println "WARNING: AppUtils.getDomainClass( ${qualifiedName} ) found no result"
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

    /**
     * Returns a complete list of currently registered domain classes
     * @return a list of {@link GrailsClass} entitles
     */
    static List<GrailsClass> getAllDomainClasses() {
        Holders.grailsApplication.getArtefacts("Domain").toList()
    }
}
