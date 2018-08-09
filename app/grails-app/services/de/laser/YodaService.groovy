package de.laser

import grails.plugin.springsecurity.SpringSecurityUtils

class YodaService {

    def grailsApplication
    def springSecurityService

    // gsp:
    // grailsApplication.mainContext.getBean("yodaService")
    // <g:set var="yodaService" bean="yodaService"/>

    def showDebugInfo() {

        return ( SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA') || grailsApplication.config.showDebugInfo )
    }
}
