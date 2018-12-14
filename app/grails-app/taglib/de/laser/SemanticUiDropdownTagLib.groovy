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


        out << '<div class="ui icon buttons la-ctrls la-float-right">'
        out <<   body()
        out << '</div>'
    }

    def exportDropdown = { attrs, body ->


        out << '<div class="ui simple dropdown button">'
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
        out << '</div>'
    }

    //<semui:exportDropdownItem> LINK <semui:exportDropdownItem>

    def exportDropdownItem = { attrs, body ->


        out <<   body()

    }

    // <semui:actionsDropdown params="${params}"  />

    def actionsDropdown = { attrs, body ->

        out << '<div class="ui simple dropdown button">'
        out <<  '<i class="magic icon"></i>'
        out <<  '<div class="menu">'

        out <<          body()

        out <<  '</div>'
        out << '</div>'
    }

    def actionsDropdownItem = { attrs, body ->

        def text      = attrs.text ? attrs.text : ''
        def message   = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (text && message) ? text + " - " + message : text + message
        def aClass    = 'item'
        def href      = attrs.href ? attrs.href : '#'

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
            out << '<a href="' + href + '" class="item"'
            if (attrs.id) { // e.g. binding js events
                out << ' id="' + attrs.id + '">'
            }
            if (attrs.'data-semui') { // e.g. binding modals
                out << ' data-semui="' + attrs.'data-semui' + '">'
            }
            out << linkBody + '</a>'
        }
    }

    def actionsDropdownItemDisabled = { attrs, body ->

        def message   = attrs.message ? "${message(code: attrs.message)}" : ''

        out << '<a href="#" class="item"><div class="disabled" data-tooltip="Die Funktion \''+message+'\' ist zur Zeit nicht verfügbar!">'+message+'</div></a>'

    }
}

