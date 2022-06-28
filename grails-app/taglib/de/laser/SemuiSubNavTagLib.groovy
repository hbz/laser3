package de.laser


import de.laser.helper.SwissKnife

class SemuiSubNavTagLib {

    AccessService accessService
    ContextService contextService

    static namespace = "semui"

    // <semui:subNav actionName="${actionName}">
    //     <semui:subNavItem controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // </semui:subNav>

    def subNav = { attrs, body ->

        out << '<nav class="ui ' + (attrs.showInTabular ? 'tabular ' : 'secondary pointing ')
        out << 'stackable menu la-clear-before" role="tablist">'
        out <<   body()
        out << '</nav>'
    }

    def complexSubNavItem = { attrs, body ->

        String aClass = ((this.pageScope.variables?.workFlowPart == attrs.workFlowPart) ? 'item active' : 'item') + (attrs.class ? ' ' + attrs.class : '')

        if (attrs.controller) {
            if(attrs.disabled == true) {
                out << '<div class="item disabled">' +body()+ '</div>'
            }
            else {
                out << g.link(body(),
                        class: aClass,
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
        String aClass    = ((this.pageScope.variables?.actionName == attrs.action && (attrs.tab == params.tab || attrs.tab == params[attrs.subTab])) ? 'item active' : 'item') + (attrs.class ? ' ' + attrs.class : '')

        String tooltip = attrs.tooltip ?: ""
        Integer counts = attrs.counts ? attrs.counts as Integer : null

        if(tooltip != "") {
            linkBody = '<div class="la-popup-tooltip la-delay" data-content="' + tooltip + '">' + linkBody + '</div>'
        }

        if (counts) {
            linkBody = linkBody + '<div class="ui floating blue circular label">'+counts+'</div>'
        }

        if (attrs.disabled) {
            out << '<div class="item disabled">' + linkBody + '</div>'
        }
        else if (attrs.controller) {
            out << g.link(linkBody,
                    class: aClass,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params,
                    role: "Tab"
            )
        }
        else {
            out << '<a href="" class="' + aClass + '">' + linkBody + '</a>'
        }
    }

    // affiliation="INST_EDITOR" affiliationOrg="${orgToShow}"

    def securedSubNavItem = { attrs, body ->

        def (lbText, lbMessage) = SwissKnife.getTextAndMessage(attrs)
        String linkBody  = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage
        String aClass = ((this.pageScope.variables?.actionName == attrs.action) ? 'item active' : 'item') + (attrs.class ? ' ' + attrs.class : '')

        String tooltip = attrs.tooltip ?: ""
        Integer counts = attrs.counts ? attrs.counts as Integer : null

        boolean check = SwissKnife.checkAndCacheNavPerms(attrs, request)

        if (tooltip != "") {
            linkBody = '<div data-tooltip="' + tooltip + '" data-position="bottom center">' + linkBody + '</div>'
        }

        if (counts) {
            linkBody = linkBody + '<div class="ui floating blue circular label">'+counts+'</div>'
        }

        if (check) {
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
            if (attrs.affiliation && contextService.getUser().hasAffiliation(attrs.affiliation)) {
                out << '<div class="item disabled la-popup-tooltip la-delay" data-position="left center" data-content="' + message(code:'tooltip.onlyFullMembership') + '" role="menuitem">' + linkBody + '</div>'
            }
            else out << '<div class="item disabled la-popup-tooltip la-delay" data-position="left center" role="menuitem">' + linkBody + '</div>'
        }
    }
}
