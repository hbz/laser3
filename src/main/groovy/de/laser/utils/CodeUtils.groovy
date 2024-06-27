package de.laser.utils


import grails.core.GrailsClass
import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.model.PersistentEntity

/**
 * Util class for determining domain classes
 */
@Slf4j
class CodeUtils {

    /**
     * Returns the domain class by the given full name
     * @param qualifiedName the full name of the class to resolve
     * @return the resolved {@link Class}
     */
    static Class getDomainClass(String qualifiedName) {
        getDomainArtefact(qualifiedName)?.clazz
    }

    /**
     * Returns the domain class by the given simple name
     * @param simpleName the simple name of the class to resolve
     * @return
     */
    static Class getDomainClassBySimpleName(String simpleName) {
        getDomainArtefactBySimpleName(simpleName)?.clazz
    }

    /**
     * Returns all currently defined domain classes
     * @return a {@link List} of domain {@link Class}es currently defined in the system
     */
    static List<Class> getAllDomainClasses() {
        getAllDomainArtefacts().collect{ it.clazz }
    }

    /**
     * Returns all currently defined controller classes
     * @return a {@link List} of controller {@link Class}es currently defined in the system
     */
    static List<Class> getAllControllerClasses() {
        getAllControllerArtefacts().collect{ it.clazz }
    }

    // --

    /**
     * Returns the persistent entity behind the given domain class name
     * @param qualifiedName the full name of the domain to retrieve
     * @return the {@link PersistentEntity} defined for the given domain class
     */
    static PersistentEntity getPersistentEntity(String qualifiedName) {
        String fallback = qualifiedName.replace('class ', '')
        PersistentEntity pe = Holders.grailsApplication.mappingContext.getPersistentEntity( fallback )

        if (! pe) {
            log.warn "Found no result - getPersistentEntity( ${qualifiedName} )"
        }
        pe
    }

    // --

    /**
     * Retrieves the domain artefact by the given full domain class name
     * @param qualifiedName the full name of the class to resolve
     * @return the {@link GrailsClass} reflecting the domain artefact
     */
    static GrailsClass getDomainArtefact(String qualifiedName) {
        String fallback = qualifiedName.replace('class ', '')
        GrailsClass dc = Holders.grailsApplication.getArtefact('Domain', fallback)

        if (! dc) {
            log.warn "Found no result - getDomainArtefact( ${qualifiedName} )"
        }
        dc
    }

    /**
     * Retrieves the domain artefact by the given simple domain class name
     * @param simpleName the simple name of the class to resolve
     * @return the {@link GrailsClass} reflecting the domain artefact
     */
    static GrailsClass getDomainArtefactBySimpleName(String simpleName) {
        GrailsClass dc
        List<String> namespaces = getAllDomainClasses().packageName.unique()

        for (String ns : namespaces) {
            dc = Holders.grailsApplication.getArtefact('Domain', ns + '.' + simpleName)
            if (dc) { break }
        }
        if (! dc) {
            log.warn "Found no result - getDomainArtefactBySimpleName( ${simpleName} )"
        }
        dc
    }

    /**
     * Returns all currently defined domain artefacts
     * @return a {@link List} of currently defined {@link GrailsClass} domain artefacts
     */
    static List<GrailsClass> getAllDomainArtefacts() {
        // it.class = class org.grails.core.DefaultGrailsDomainClass
        // it.clazz = class de.laser.<XY>
        Holders.grailsApplication.getArtefacts('Domain').toList().sort{ it.clazz.simpleName }
    }

    /**
     * Returns all currently defined controller artefacts
     * @return a {@link List} of currently defined {@link GrailsClass} controller artefacts
     */
    static List<GrailsClass> getAllControllerArtefacts() {
        // it.class = class org.grails.core.DefaultGrailsControllerClass
        // it.clazz = class de.laser.<XY>Controller
        Holders.grailsApplication.getArtefacts('Controller').toList().sort{ it.clazz.simpleName }
    }

}
