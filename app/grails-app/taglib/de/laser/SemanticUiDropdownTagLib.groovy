package de.laser

import com.k_int.kbplus.auth.User

// Semantic UI

class SemanticUiDropdownTagLib {

    def springSecurityService

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "semui"

    // <semui:exportDropdown params="${params}" transforms="${transforms}" />

    def controlButtons = { attrs, body ->


        out << '<div class="ui icon buttons la-float-right">'
        out <<       body()
        out <<       '</div>'
    }

    def exportDropdown = { attrs, body ->


        out << '<button class="ui button dropdown">'
        out <<   '<i class="download icon"></i>'
        out <<   '<div class="menu">'

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

        out <<  '</div>'
        out << '</button>'
    }

    //<semui:exportDropdownItem> LINK <semui:exportDropdownItem>

    def exportDropdownItem = { attrs, body ->

        out << '<div class="item">'
        out <<   body()
        out << '</div>'
    }

    // <semui:actionsDropdown params="${params}"  />

    def actionsDropdown = { attrs, body ->

        out << '<button class="ui button dropdown">'
        out <<  '<i class="setting icon"></i>'
        out <<  '<div class="menu">'

        out <<          body()

        out <<  '</div>'
        out << '</button>'
    }

    def actionsDropdownItem = { attrs, body ->

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
}

