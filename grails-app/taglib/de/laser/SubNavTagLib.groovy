package de.laser

import de.laser.auth.Role
import de.laser.utils.SwissKnife

class SubNavTagLib {

    AccessService accessService
    ContextService contextService

    static namespace = 'ui'

    // <ui:subNav actionName="${actionName}">
    //     <ui:subNavItem controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // </ui:subNav>

    def subNav = { attrs, body ->

        out << '<nav class="ui ' + (attrs.showInTabular ? 'tabular ' : 'secondary pointing ')
        out << 'stackable menu la-clear-before" role="tablist">'
        out <<   body()
        out << '</nav>'
    }

    def complexSubNavItem = { attrs, body ->

        String cssClass = ((this.pageScope.variables?.workFlowPart == attrs.workFlowPart) ? 'item active' : 'item') + (attrs.class ? ' ' + attrs.class : '')

        if (attrs.controller) {
            if(attrs.disabled == true) {
                out << '<div class="item disabled">' +body()+ '</div>'
            }
            else {
                out << g.link(body(),
                        class: cssClass,
                        controller: attrs.controller,
                        action: attrs.action,
                        params: attrs.params
                )
            }
        }
        else {
            out << body()
        }
    }

    def subNavItem = { attrs, body ->

        def (text, message) = SwissKnife.getTextAndMessage(attrs)
        String linkBody  = (text && message) ? text + " - " + message : text + message
        String cssClass    = ((this.pageScope.variables?.actionName == attrs.action && (attrs.tab == params.tab || attrs.tab == params[attrs.subTab])) ? 'item active' : 'item') + (attrs.class ? ' ' + attrs.class : '')

        String tooltip = attrs.tooltip ?: ""

        if (tooltip != "") {
            linkBody = '<div class="la-popup-tooltip la-delay" data-content="' + tooltip + '">' + linkBody + '</div>'
        }

        if (attrs.counts) {
            linkBody = linkBody + '<div class="ui floating blue circular label">' + attrs.counts + '</div>'
        }

        if (attrs.disabled) {
            out << '<div class="item disabled">' + linkBody + '</div>'
        }
        else if (attrs.controller) {
            out << g.link(linkBody,
                    class: cssClass,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params,
                    role: 'tab'
            )
        }
        else {
            out << '<a href="" class="' + cssClass + '">' + linkBody + '</a>'
        }
    }

    // affiliationOrg="${orgToShow}"

    def securedSubNavItem = { attrs, body ->

        def (lbText, lbMessage) = SwissKnife.getTextAndMessage(attrs)
        String linkBody = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage
        String cssClass = ((this.pageScope.variables?.actionName == attrs.action) ? 'item active' : 'item') + (attrs.class ? ' ' + attrs.class : '')
        String tooltip = attrs.tooltip ?: ""

        if (!attrs.instRole) {
            attrs.instRole = Role.INST_USER // new default
        }

        boolean check = SwissKnife.checkAndCacheNavPermsForCurrentRequest(attrs, request)

        if (tooltip != "") {
            linkBody = '<div data-tooltip="' + tooltip + '" data-position="bottom center">' + linkBody + '</div>'
        }

        if (check) {
            if (attrs.counts) {
                linkBody = linkBody + '<div class="ui floating blue circular label">' + attrs.counts + '</div>'
            }

            if (attrs.controller) {
                out << g.link(linkBody,
                        class: cssClass,
                        controller: attrs.controller,
                        action: attrs.action,
                        params: attrs.params,
                        role: 'tab'
                )
            }
            else {
                out << linkBody
            }
        }
        else {
            if (attrs.instRole && contextService.getUser().hasCtxAffiliation_or_ROLEADMIN(attrs.instRole)) {
                out << '<div class="item disabled la-popup-tooltip la-delay" data-position="left center" data-content="' + message(code:'tooltip.onlyFullMembership') + '" role="tab">' + linkBody + '</div>'
            }
//            else out << '<div class="item disabled la-popup-tooltip la-delay" data-position="left center" role="tab">' + linkBody + '</div>'
        }
    }
}
