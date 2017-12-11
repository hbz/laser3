package de.laser

import com.k_int.kbplus.auth.User

// Semantic UI

class SemanticUiTagLib {

    def springSecurityService

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "semui"

    // <semui:breadcrumbs>
    //     <semui:crumb controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // <semui:breadcrumbs>

    def breadcrumbs = { attrs, body ->

        out << '<div>'
        out <<   '<div class="ui large breadcrumb">'
        out <<     crumb([controller: 'home', text:'<i class="home icon"></i>'])
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

    // <semui:crumbAsBadge message="default.editable" class="orange" />

    def crumbAsBadge = { attrs, body ->

        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''

        out << '<div class="ui horizontal label ' + attrs.class + '">' + lbMessage + '</div>'
    }

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

    // <semui:card text="${text}" message="local.string" class="some_css_class">
    //
    // <semui:card>

    def card = { attrs, body ->
        def text      = attrs.text ? attrs.text : ''
        def message   = attrs.message ? "${message(code: attrs.message)}" : ''
        def title  = (text && message) ? text + " - " + message : text + message

        out << '<div class="ui card ' + attrs.class + '">'
        out <<   '<div class="content">'
        if (title) {
            out << '<div class="header">' + title + '</div>'
        }
        out <<     body()
        out <<   '</div>'
        out << '</div>'
    }

    // <semui:modeSwitch controller="controller" action="action" params="params" />

    def modeSwitch = { attrs, body ->

        def mode = (attrs.params.mode=='basic') ? 'basic' : ((attrs.params.mode == 'advanced') ? 'advanced' : null)
        if (!mode) {
            def user = User.get(springSecurityService.principal.id)
            mode = (user.showSimpleViews?.value == 'No') ? 'advanced' : 'basic'

            // CAUTION: inject default mode
            attrs.params.mode = mode
        }

        out << '<div class="ui buttons">'
        out << g.link( "${message(code:'profile.simpleView', default:'Basic')}",
                controller: attrs.controller,
                action: attrs.action,
                params: attrs.params + ['mode':'basic'],
                class: "ui mini button ${mode == 'basic' ? 'positive' : ''}"
        )

        out << '<div class="or"></div>'

        out << g.link( "${message(code:'profile.advancedView', default:'Advanced')}",
                controller: attrs.controller,
                action: attrs.action,
                params: attrs.params + ['mode':'advanced'],
                class: "ui mini button ${mode == 'advanced' ? 'positive' : ''}"
        )
        out << '</div>'
    }

    // <semui:exportDropdown params="${params}" transforms="${transforms}" />

    def exportDropdown = { attrs, body ->

        out << '<div class="ui right floated compact menu">'
        out <<   '<div class="ui simple dropdown item">'
        out <<     'Export'
        out <<     '<i class="dropdown icon"></i>'
        out <<     '<div class="menu">'

        out <<       body()
        /*
        out <<       '<div class="item">'
        out <<         g.link("JSON Export", action:"show", params:"${params+[format:'json']}")
        out <<       '</div>'
        out <<       '<div class="item">'
        out <<         g.link("XML Export", action:"show", params:"${params+[format:'xml']}")
        out <<       '</div>'

        attrs.transforms?.each{key, val ->
            out <<       '<div class="item">'
            out <<         g.link("${val.name}", action:"show", id:"${attrs.params.id}", params:"${[format:'xml', transformId:key, mode:attrs.params.mode]}")
            out <<       '</div>'
        }
        */

        out <<     '</div>'
        out <<   '</div>'
        out << '</div>'
    }

    //<semui:exportDropdownItem> LINK <semui:exportDropdownItem>

    def exportDropdownItem = { attrs, body ->

        out << '<div class="item">'
        out <<   body()
        out << '</div>'
    }

    //<semui:filter> CONTENT <semui:filter>

    def filter = { attrs, body ->

        out << '<div class="ui yellow segment">'
        out <<   body()
        out << '</div>'
    }

    //<semui:form> CONTENT <semui:form>

    def form = { attrs, body ->

        out << '<div class="ui blue segment">'
        out <<   body()
        out << '</div>'
    }

    //<semui:form> CONTENT <semui:form>

    def simpleForm = { attrs, body ->

        def method      = attrs.method ?: 'GET'
        def controller  = attrs.controller ?: ''
        def action      = attrs.action ?: ''
        def text        = attrs.text ? attrs.text : ''
        def message     = attrs.message ? "${message(code: attrs.message)}" : ''
        def title       = (text && message) ? text + " - " + message : text + message

        out << '<div class="ui segment">'
        out <<   '<form class="ui form" controller="' + controller + '" action="' + action + '" method="' + method + '">'
        out <<     '<label>' + title + '</label>'
        out <<     body()
        out <<   '</form>'
        out << '</div>'
    }

    //<semui:modal id="myModalDialog" text="${text}" message="local.string"> CONTENT <semui:form>

    def modal = { attrs, body ->

        def id        = attrs.id ? ' id="' + attrs.id + '" ' : ''
        def text      = attrs.text ? attrs.text : ''
        def message   = attrs.message ? "${message(code: attrs.message)}" : ''
        def title     = (text && message) ? text + " - " + message : text + message

        def msgClose = "Schließen"
        def msgSave  = "Änderungen speichern"

        out << '<div class="ui modal"' + id + '>'
        out <<   '<div class="header">' + title + '</div>'
        out <<   '<div class="scrolling content">'
        out <<     body()
        out <<   '</div>'
        out <<   '<div class="actions">'
        out <<     '<a href="#" class="ui button" onclick="$(\'#' + attrs.id + '\').modal(\'hide\')">' + msgClose + '</a>'
        out <<     '<input type="submit" class="ui positive button" name="save" value="' + msgSave + '" onclick="$(\'#' + attrs.id + '\').find(\'form\').submit()"/>'
        out <<   '</div>'
        out << '</div>'
    }

}
