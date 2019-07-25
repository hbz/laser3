package de.laser


import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import org.codehaus.groovy.grails.commons.GrailsApplication

//@CompileStatic
class YodaService {

    GrailsApplication grailsApplication
    def sessionRegistry = Holders.grailsApplication.mainContext.getBean('sessionRegistry')
    def contextService = Holders.grailsApplication.mainContext.getBean('contextService')

    // gsp:
    // grailsApplication.mainContext.getBean("yodaService")
    // <g:set var="yodaService" bean="yodaService"/>

    boolean showDebugInfo() {
        //enhanced as of ERMS-829
        return ( SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA') || grailsApplication.config.showDebugInfo )
    }

    int getNumberOfActiveUsers() {
        int count = 0

        sessionRegistry.getAllPrincipals().each { user ->
            List lastAccessTimes = []

            sessionRegistry.getAllSessions(user, false).each { userSession ->
                if (user.username == contextService.getUser()?.username) {
                    userSession.refreshLastRequest()
                }
                lastAccessTimes << userSession.getLastRequest().getTime()
            }
            if (lastAccessTimes.max() > System.currentTimeMillis() - (1000 * 600)) { // 10 minutes
                count++
            }
        }
        count
    }
}
