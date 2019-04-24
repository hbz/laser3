package de.laser

import org.springframework.web.servlet.support.RequestContextUtils
import grails.plugin.springsecurity.SpringSecurityUtils

class SemanticUiSubNavTagLib {

    def springSecurityService
    def contextService
    def accessService

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "semui"

    // <semui:subNav actionName="${actionName}">
    //     <semui:subNavItem controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // </semui:subNav>

    def subNav = { attrs, body ->

        out << '<nav class="ui secondary pointing  stackable  menu">'
        out <<   body()
        out << '</nav>'
    }

    def complexSubNavItem = { attrs, body ->

        def text      = attrs.text ? attrs.text : ''
        def message   = attrs.message ? "${message(code: attrs.message)}" : ''
        def aClass    = ((this.pageScope.variables?.workFlowPart == attrs.workFlowPart) ? 'item active' : 'item') + (attrs.class ? ' ' + attrs.class : '')

        if (attrs.controller) {
            out << g.link(body(),
                    class: aClass,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params
            )
        }
        else {
            out << body()
        }
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

        boolean check = SpringSecurityUtils.ifAnyGranted(attrs.specRole ?: [])

        if (!check) {

            if (attrs.affiliation && attrs.affiliationOrg) {
                if (contextService.getUser()?.hasAffiliationForOrg(attrs.affiliation, attrs.affiliationOrg)) {
                    check = true
                }
            }
            else {
                if (attrs.affiliation && attrs.orgPerm) {
                    if (contextService.getUser()?.hasAffiliation(attrs.affiliation) && accessService.checkPerm(attrs.orgPerm)) {
                        check = true
                    }
                }
                else if (attrs.affiliation && contextService.getUser()?.hasAffiliation(attrs.affiliation)) {
                    check = true
                }
                else if (attrs.orgPerm && accessService.checkPerm(attrs.orgPerm)) {
                    check = true
                }
            }
        }

        if (check) {
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
