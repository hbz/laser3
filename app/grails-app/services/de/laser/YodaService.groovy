package de.laser

import grails.plugin.springsecurity.SpringSecurityUtils

class YodaService {

    def grailsApplication
    def springSecurityService

    // gsp:
    // grailsApplication.mainContext.getBean("yodaService")
    // <g:set var="yodaService" bean="yodaService"/>

    def showDebugInfo() {
        //enhanced as of ERMS-829
        return ( SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA,ROLE_DATAMANAGER') || grailsApplication.config.showDebugInfo )
    }
}
