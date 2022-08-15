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

    static Class getDomainClass(String qualifiedName) {
        getDomainArtefact(qualifiedName)?.clazz
    }

    static Class getDomainClassBySimpleName(String simpleName) {
        getDomainArtefactBySimpleName(simpleName)?.clazz
    }

    static List<Class> getAllDomainClasses() {
        getAllDomainArtefacts().collect{ it.clazz }
    }

    static List<Class> getAllControllerClasses() {
        getAllControllerArtefacts().collect{ it.clazz }
    }

    // --

    static PersistentEntity getPersistentEntity(String qualifiedName) {
        String fallback = qualifiedName.replace('class ', '')
        PersistentEntity pe = Holders.grailsApplication.mappingContext.getPersistentEntity( fallback )

        if (! pe) {
            log.warn "Found no result - getPersistentEntity( ${qualifiedName} )"
        }
        pe
    }

    // --

    static GrailsClass getDomainArtefact(String qualifiedName) {
        String fallback = qualifiedName.replace('class ', '')
        GrailsClass dc = Holders.grailsApplication.getArtefact('Domain', fallback)

        if (! dc) {
            log.warn "Found no result - getDomainArtefact( ${qualifiedName} )"
        }
        dc
    }

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

    static List<GrailsClass> getAllDomainArtefacts() {
        // it.class = class org.grails.core.DefaultGrailsDomainClass
        // it.clazz = class de.laser.<XY>
        Holders.grailsApplication.getArtefacts('Domain').toList().sort{ it.clazz.simpleName }
    }

    static List<GrailsClass> getAllControllerArtefacts() {
        // it.class = class org.grails.core.DefaultGrailsControllerClass
        // it.clazz = class de.laser.<XY>Controller
        Holders.grailsApplication.getArtefacts('Controller').toList().sort{ it.clazz.simpleName }
    }

}
