package de.laser

import com.k_int.kbplus.Org
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.codehaus.groovy.grails.web.util.WebUtils

class ContextService {

    def springSecurityService

    def setOrg(Org context) {
        def session = WebUtils.retrieveGrailsWebRequest().getSession()
        session.setAttribute('contextOrg', GrailsHibernateUtil.unwrapIfProxy(context))
    }

    def getOrg() {
        def session = WebUtils.retrieveGrailsWebRequest().getSession()
        def context = session.getAttribute('contextOrg') ?: Org.findByShortcode(getUser()?.defaultDash?.shortcode)
        context
    }

    def getUser() {
        springSecurityService.getCurrentUser()
    }

    def getMemberships() {
        getUser()?.authorizedOrgs
    }
}
