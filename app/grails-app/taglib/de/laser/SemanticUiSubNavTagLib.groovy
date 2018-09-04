package de.laser

import org.springframework.web.servlet.support.RequestContextUtils
import grails.plugin.springsecurity.SpringSecurityUtils

class SemanticUiSubNavTagLib {

    def springSecurityService

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "semui"

    // <semui:subNav actionName="${actionName}">
    //     <semui:subNavItem controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // </semui:subNav>

    def subNav = { attrs, body ->

        out << '<div class="ui secondary pointing menu">'
        out <<   body()
        out << '</div>'
    }

    def subNavItem = { attrs, body ->

        def text      = attrs.text ? attrs.text : ''
        def message   = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (text && message) ? text + " - " + message : text + message
        def aClass    = ((this.pageScope.variables?.actionName == attrs.action) ? 'item active' : 'item') + (attrs.class ? ' ' + attrs.class : '')

        if (attrs.controller) {
            out << g.link(linkBody,
                    class: aClass,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params
            )
        }
        else {
            out << linkBody
        }
    }

    // affiliation="INST_EDITOR" affiliationOrg="${orgToShow}"

    def securedSubNavItem = { attrs, body ->

        def text      = attrs.text ? attrs.text : ''
        def message   = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (text && message) ? text + " - " + message : text + message
        def specrole      = []
        specrole          = attrs.specRoleCheck

        def secureCheck = false

        secureCheck = specrole ? SpringSecurityUtils.ifAnyGranted(specrole) : false

        if (!secureCheck || attrs.affiliation) {
            if (attrs.affiliationOrg) {
                if (springSecurityService.getCurrentUser()?.hasAffiliationForOrg(attrs.affiliation, attrs.affiliationOrg)) {
                    secureCheck = true
                }
            } else {
                if (springSecurityService.getCurrentUser()?.hasAffiliation(attrs.affiliation)) {
                    secureCheck = true
                }
            }
        }

        if (secureCheck) {
            def aClass = (this.pageScope.variables?.actionName == attrs.action) ? 'item active' : 'item'

            if (attrs.controller) {
                out << g.link(linkBody,
                        class: aClass,
                        controller: attrs.controller,
                        action: attrs.action,
                        params: attrs.params
                )
            }
            else {
                out << linkBody
            }
        }
        else {
            out << '<div class="item disabled">' + linkBody + '</div>'
        }
    }
}
