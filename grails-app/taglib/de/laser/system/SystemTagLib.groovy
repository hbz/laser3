package de.laser.system

import de.laser.ui.Btn
import de.laser.utils.SwissKnife

class SystemTagLib {

    static namespace = 'laser'

    // <laser:serviceInjection/>

    def serviceInjection = { attrs, body ->

        g.set( var:'accessService',                 bean:'accessService' )
        g.set( var:'addressbookService',            bean:'addressbookService' )
        g.set( var:'auditService',                  bean:'auditService' )
        g.set( var:'cacheService',                  bean:'cacheService' )
        g.set( var:'compareService',                bean:'compareService' )
        g.set( var:'contextService',                bean:'contextService' )
        g.set( var:'controlledListService',         bean:'controlledListService' )
        g.set( var:'customerTypeService',           bean:'customerTypeService' )
        g.set( var:'dashboardService',              bean:'dashboardService')
        g.set( var:'deletionService',               bean:'deletionService')
        g.set( var:'docstoreService',               bean:'docstoreService' )
        g.set( var:'escapeService',                 bean:'escapeService')
        g.set( var:'exportClickMeService',          bean:'exportClickMeService')
        g.set( var:'exportService',                 bean:'exportService')
        g.set( var:'financeService',                bean:'financeService')
        g.set( var:'fileCryptService',              bean:'fileCryptService' )
        g.set( var:'filterService',                 bean:'filterService' )
        g.set( var:'formService',                   bean:'formService' )
        g.set( var:'genericOIDService',             bean:'genericOIDService' )
        g.set( var:'gokbService',                   bean:'gokbService' )
        g.set( var:'helpService',                   bean:'helpService' )
        g.set( var:'identifierService',             bean:'identifierService' )
        g.set( var:'issueEntitlementService',       bean:'issueEntitlementService' )
        g.set( var:'licenseService',                bean:'licenseService' )
        g.set( var:'linksGenerationService',        bean:'linksGenerationService' )
        g.set( var:'markerService',                 bean:'markerService' )
        g.set( var:'managementService',             bean:'managementService' )
        g.set( var:'packageService',                bean:'packageService')
        g.set( var:'pendingChangeService',          bean:'pendingChangeService')
        g.set( var:'propertyService',               bean:'propertyService')
        g.set( var:'providerService',               bean:'providerService' )
        g.set( var:'subscriptionsQueryService',     bean:'subscriptionsQueryService' )
        g.set( var:'subscriptionControllerService', bean:'subscriptionControllerService' )
        g.set( var:'subscriptionService',           bean:'subscriptionService' )
        g.set( var:'surveyService',                 bean:'surveyService' )
        g.set( var:'systemService',                 bean:'systemService' )
        g.set( var:'taskService',                   bean:'taskService' )
        g.set( var:'userService',                   bean:'userService' )
        g.set( var:'workflowService',               bean:'workflowService' )
        g.set( var:'vendorService',                 bean:'vendorService' )
        g.set( var:'yodaService',                   bean:'yodaService' )
    }

    // DO NOT use for templates, pdf or email generation

    // <laser:htmlStart text="" message="" description="" />

    def htmlStart = { attrs, body ->

        String title = message(code: 'laser')

        if (attrs.text) {
            title = title + ' : ' + attrs.text
        }
        if (attrs.message) {
            SwissKnife.checkMessageKey(attrs.message as String)
            title = title + ' : ' + message(code: attrs.message, args: attrs.args)
        }

        laser.serviceInjection()

        out << '<!doctype html><html><head>'
        out << '<meta name="layout" content="laser">'

        if (attrs.description) {
            out << '<meta name="description" content="' + attrs.description + '">'
        }
        out << '<title>' + title + '</title>'
        out << body()
        out << '</head>'
        out << '<body>'
    }

    def htmlEnd = { attrs, body ->
        out << '</body></html>'
    }

    def serverCodeMessage = { attrs, body ->
        out << '<div class="ui segment piled">'
        out << '  <div class="content">'
        out << '    <div> <span class="ui orange label huge">' + attrs.status + '</span> </div>'
        out << '    <h2 class="ui header"> ' + attrs.header + ' </h2>'
        out << '    <div>'
        if (attrs.subheader) {
            out << '  <p> ' + attrs.subheader + ' </p>'
        }
        out << body()
        out << '      <br />'
        out << '      <p> <button class="' + Btn.SIMPLE + '" onclick="JSPC.helper.goBack()">' + message(code: 'default.button.back') + '</button> </p>'
        out << '    </div>'
        out << '  </div>'
        out << '</div>'
    }

}
