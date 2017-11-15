package de.laser

// Semantic UI

class SemanticUiTagLib {
    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "semui"

    // <semui:breadcrumbs>
    //     <semui:crumb controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // <semui:breadcrumbs>

    def breadcrumbs = { attrs, body ->

        out << '<div>'
        out <<   '<div class="ui large breadcrumb">'
        out <<     crumb([controller: 'home', message:'default.home.label'])
        out <<     body()
        out <<   '</div>'
        out << '</div>'
    }

    // text             = raw text
    // message          = translate via i18n
    // class="active"   = no link

    def crumb = { attrs, body ->

        def lbText    = attrs.text ? attrs.text : ''
        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage

        if (attrs.controller) {
            out << g.link(linkBody,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params,
                    class: 'section ' + attrs.class,
                    id: attrs.id
            )
        }
        else {
            out << linkBody
        }
        if (! "active".equalsIgnoreCase(attrs.class.toString())) {
            out << ' <div class="divider">/</div> '
        }
    }

    def crumbAsBadge = { attrs, body ->

        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''

        out << '<div class="ui horizontal label ' + attrs.class + '">' + lbMessage + '</div>'
    }

    // <semui:subNav actionName="${actionName}">
    //     <semui:subNavItem controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // <semui:subNav>

    def subNav = { attrs, body ->

        out << '<div class="ui secondary pointing menu">'
        out <<   body()
        out << '</div>'
    }

    def subNavItem = { attrs, body ->

        def text      = attrs.text ? attrs.text : ''
        def message   = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (text && message) ? text + " - " + message : text + message
        def aClass    = 'item'
        if (this.pageScope.variables?.actionName == attrs.action) {
            aClass = 'item active'
        }
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

    // <semui:messages data="${flash}" />

    def messages = { attrs, body ->

        def flash = attrs.data

        if (flash && flash.message) {
            out << '<div class="ui success message">'
            out <<   '<i class="close icon"></i>'
            out <<   '<p>'
            out <<     flash.message
            out <<   '</p>'
            out << '</div>'
        }

        if (flash && flash.error) {
            out << '<div class="ui negative message">'
            out <<   '<i class="close icon"></i>'
            out <<   '<p>'
            out <<     flash.error
            out <<   '</p>'
            out << '</div>'
        }
    }

    // <semui:card title="" class="some_css_class">
    //
    // <semui:card>

    def card = { attrs, body ->
        def title = attrs.title ? "${message(code: attrs.title)}" : ''

        out << '<div class="ui card ' + attrs.class + '">'
        out <<   '<div class="content">'
        if (title) {
            out << '<div class="header">' + title + '</div>'
        }
        out <<     body()
        out <<   '</div>'
        out << '</div>'
    }
}
