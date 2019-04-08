package de.laser

import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication

@CompileStatic
class YodaService {

    GrailsApplication grailsApplication

    // gsp:
    // grailsApplication.mainContext.getBean("yodaService")
    // <g:set var="yodaService" bean="yodaService"/>

    boolean showDebugInfo() {
        //enhanced as of ERMS-829
        return ( SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA,ROLE_DATAMANAGER') || grailsApplication.config.showDebugInfo )
    }
}
