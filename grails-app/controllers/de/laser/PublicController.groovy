package de.laser

import de.laser.api.v0.ApiManager
import de.laser.properties.SubscriptionProperty
import de.laser.config.ConfigMapper
import de.laser.storage.PropertyStore
import de.laser.storage.RDStore
import de.laser.utils.AppUtils
import de.laser.wekb.Package
import de.laser.wekb.TitleInstancePackagePlatform
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.plugins.mail.MailService

/**
 * This controller contains all pages which are accessible without a user account, i.e. public access pages
 */
@Secured(['permitAll'])
class PublicController {

    ContextService contextService
    EscapeService escapeService
    GenericOIDService genericOIDService
    MailService mailService
    SpringSecurityService springSecurityService

    /**
     * Redirects to dashboard or login
     */
    @Secured(['permitAll'])
    def index() {
        if (springSecurityService.isLoggedIn()) {
            redirect( uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl )
        }
        else {
            redirect( uri: SpringSecurityUtils.securityConfig.auth.loginFormUrl ) // , params: params
        }
    }

   /**
    * Displays the robots.txt preventing crawler access to instances other than the productive one
    */
    @Secured(['permitAll'])
    def robots() {
        String text = "User-agent: *\n"

        if (AppUtils.getCurrentServer() == AppUtils.PROD) {
            text += "Disallow: /gasco/data/ \n"
            text += "Disallow: /gasco/details/ \n"
            text += "Disallow: /login/ \n"              // ERMS-6180
            text += "Disallow: /public/api/ \n"         // ERMS-6180
            text += "Disallow: /public/dsgvo/ \n"
            text += "Disallow: /public/faq/ \n"
            text += "Disallow: /public/help/ \n"
            text += "Disallow: /public/manual/ \n"
            text += "Disallow: /public/releases/ \n"
            text += "Disallow: /public/wcagFeedbackForm/ \n"
            text += "Disallow: /public/wcagStatement/ \n"
            text += "Disallow: /public/wcagTest/ \n"
        }
        else {
            text += "Disallow: / \n"
        }
        render(text: text, contentType: "text/plain", encoding: "UTF-8")
    }

    /**
     * Displays the WCAG statement
     */
    @Secured(['ROLE_ADMIN'])
    @Deprecated
    def wcagStatement() {
    }

    /**
     * Displays the WCAG feedback form
     */
    @Secured(['ROLE_USER'])
    def wcagFeedbackForm() {
    }

    /**
     * Takes the submitted message and sends a barrier-free feedback mail to an address responsible for
     * disability matters
     */
    @Secured(['ROLE_USER'])
    def sendFeedbackForm() {

        try {
            mailService.sendMail {
                to 'barrierefreiheitsbelange@hbz-nrw.de'
                from ConfigMapper.getNotificationsEmailFrom()
                subject ConfigMapper.getLaserSystemId() + ' - Feedback-Mechanismus Barrierefreiheit'
                body (view: '/mailTemplates/text/wcagFeedback', model: [name:params.name, email:params.email,url:params.url, comment:escapeService.replaceUmlaute(params.comment)])
            }
        }
        catch (Exception e) {
            log.error "Unable to perform email due to exception ${e.message}"
        }
    }

    /**
     * Displays the site content in easy language
     */
    @Secured(['permitAll'])
    def wcagEasyLanguage() {
    }

    /**
     * Test page for check compatibility
     */
    @Secured(['ROLE_ADMIN'])
    @Deprecated
    def wcagTest() {
    }

    /**
     * Call to open the GDPR statement page
     */
    @Secured(['ROLE_USER'])
    def dsgvo() {
        Map<String, Object> result = [:]
        result
    }

    /**
     * @return the frontend view with sample area for frontend developing and showcase
     */
    @Secured(['permitAll'])
    def licensingModel() {
        Map<String, Object> result = [:]
        result.mappingColsBasic = ["asService", "accessRights", "community", "wekb"]
        result.mappingColsPro = ["management", "organisation", "reporting", "api"]
        result
    }

    @Secured(['ROLE_USER'])
    def help() {
    }

    @Secured(['ROLE_USER'])
    def api() {
        Map<String, Object> result = [
                history : ApiManager.HISTORY
        ]

        String[] amv = ApiManager.VERSION.split('\\.')
        if (params.id) {
            amv = params.id.toString().split('\\.')
            result.version = (amv.length >= 1) ? amv[0] : 'failed'
            result
        }
        else {
            render view: 'apiIndex', model: result
        }
    }

    @Secured(['ROLE_USER'])
    def manual() {
        Map<String, Object> result = [
                content : [
                        'various'                       : ['Allgemein', 'General'],
                        'subscriptionInformationSheet'  : ['Lizenzinformationsblatt', 'Licence Information Sheet'],
                        'fillingOutSurveys'             : ['Umfragen und Kontakte ausfüllen', 'Complete surveys and contacts'],
                        'fileImport'                    : ['Anleitung zum Hochladen', 'Upload instructions']
                ], // todo
                topic   : 'various'
        ]
        if (params.id) {
            result.topic = params.id
        }
        result
    }

    @Secured(['ROLE_USER'])
    def faq() {
        Map<String, Object> result = [
                content : [
                        'various'               : ['Allgemein', 'General'],
                        'notifications'         : ['Benachrichtigungen', 'Notifications'],
                        'propertyDefinitions'   : ['Merkmale', 'Properties'],
                        'userManagement'        : ['Benutzer-Accounts', 'User accounts'],
                ], // todo
                topic   : 'various'
        ]
        if (params.id) {
            result.topic = params.id
        }
        result
    }

    @Secured(['ROLE_USER'])
    def releases() {
        Map<String, Object> result = [
                history : ['3.2', '3.3', '3.4', '3.5'] // todo
        ]

        String[] iap = AppUtils.getMeta('info.app.version').split('\\.')
        if (params.id) {
            iap = params.id.toString().split('\\.')
        }
        result.version = (iap.length >= 2) ? (iap[0] + '.' + iap[1]) : 'failed'
        result
    }
}
