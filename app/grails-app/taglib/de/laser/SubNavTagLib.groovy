package de.laser

// Bootstrap 2

class SubNavTagLib {
    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "laser"

    // <laser:subNav actionName="${actionName}">
    //     <laser:subNavItem controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // <laser:subNav>

    def subNav = { attrs, body ->

        out << '<ul class="nav nav-pills">'
        out <<   body()
        out << '</ul>'
    }

    def subNavItem = { attrs, body ->

        def text    = attrs.text ? attrs.text : ''
        def message = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (text && message) ? text + " - " + message : text + message

        if (this.pageScope.variables?.actionName == attrs.action) {
            out << '<li class="active">'
        }
        else {
            out << '<li>'
        }
        if (attrs.controller) {
            out << g.link(linkBody,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params
            )
        }
        else {
            out << linkBody
        }
        out << '</li>'
    }
}
