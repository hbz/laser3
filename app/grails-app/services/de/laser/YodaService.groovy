package de.laser

import grails.plugin.springsecurity.SpringSecurityUtils

class YodaService {

    def grailsApplication
    def springSecurityService

    // gsp:
    // grailsApplication.mainContext.getBean("yodaService")
    // <g:set var="yodaService" bean="yodaService"/>

    def showDebugInfo() {
        //ROLE_ADMIN,
        return ( SpringSecurityUtils.ifAnyGranted('ROLE_YODA') || grailsApplication.config.showDebugInfo )
    }
}
