package de.laser.helper

import grails.util.Holders
import org.codehaus.groovy.grails.commons.GrailsClass

class AppUtils {

    static GrailsClass getDomainClass(String qualifiedName) {
        Holders.grailsApplication.getArtefact('Domain', qualifiedName)
    }

    static GrailsClass getDomainClassGeneric(String name) {
        GrailsClass dc
        List<String> namespaces = [ 'de.laser', 'com.k_int.kbplus' ]

        for (String ns : namespaces) {
            dc = Holders.grailsApplication.getArtefact('Domain', ns + '.' + name)
            if (dc) { break }
        }
        // println dc?.getClazz()
        dc
    }
}
