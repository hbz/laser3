package de.laser

import com.k_int.kbplus.Org
import org.codehaus.groovy.grails.web.util.WebUtils

class ContextService {

    def springSecurityService

    static final SERVER_LOCAL = 'SERVER_LOCAL'
    static final SERVER_DEV   = 'SERVER_DEV'
    static final SERVER_QA    = 'SERVER_QA'
    static final SERVER_PROD  = 'SERVER_PROD'

    def setOrg(Org context) {
        def session = WebUtils.retrieveGrailsWebRequest().getSession()
        session.setAttribute('contextOrg', context)
    }

    def getOrg() {
        def session = WebUtils.retrieveGrailsWebRequest().getSession()
        def context = session.getAttribute('contextOrg') ?: Org.findByShortcode(getUser()?.defaultDash?.shortcode)
        context?.refresh()
    }

    def getUser() {
        springSecurityService.getCurrentUser()
    }

    def getMemberships() {
        getUser()?.authorizedOrgs
    }
}
