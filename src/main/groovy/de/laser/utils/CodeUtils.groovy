package de.laser.utils


import grails.core.GrailsClass
import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.model.PersistentEntity

/**
 * Util class for determining domain classes
 */
@CompileStatic
@Slf4j
class CodeUtils {

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

    static List<GrailsClass> getAllControllerClasses() {
        Holders.grailsApplication.getArtefacts('Controller').toList()
    }

    static PersistentEntity getPersistentEntity(String qualifiedName) {
        // fallback
        String fallback = qualifiedName.replace('class ', '')
        PersistentEntity pe = Holders.grailsApplication.mappingContext.getPersistentEntity( fallback )

        if (! pe) {
            log.warn "Found no result - getPersistentEntity( ${qualifiedName} )"
        }
        pe
    }
}
