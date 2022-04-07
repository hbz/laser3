package de.laser


import de.laser.helper.SwissKnife

// Semantic UI

class SemanticUiDropdownTagLib {

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "semui"

    def controlButtons = { attrs, body ->

        out << '<nav class="ui icon buttons la-ctrls la-js-dont-hide-button" aria-label=' + message(code: 'wcag.label.actionMenu')  +'>'
        out <<   body()
        out << '</nav>'
    }

    def exportDropdown = { attrs, body ->

        out << '<div class="ui simple dropdown button la-js-dont-hide-button">'
        out <<   '<i class="download icon"></i>'
        out <<   '<div class="menu">'

        out <<       body()

        out <<  '</div>'
        out << '</div>'
    }

    // <semui:exportDropdownItem> LINK <semui:exportDropdownItem>

    def exportDropdownItem = { attrs, body ->

        out << body()
    }

    // <semui:actionsDropdown params="${params}"  />

    def actionsDropdown = { attrs, body ->

        out << '<div class="ui simple dropdown button la-js-dont-hide-button">'
        out <<  '<i class="magic icon"></i>'
        out <<  '<div class="menu" style="left: auto; right: 0">'

        out <<          body()

        out <<  '</div>'
        out << '</div>'
    }

    def actionsDropdownItem = { attrs, body ->

        def (text, message) = SwissKnife.getTextAndMessage(attrs)
        String linkBody  = (text && message) ? text + " - " + message : text + message
        String aClass    = attrs.class ? attrs.class + ' item' : 'item'
        String href      = attrs.href ? attrs.href : '#'

        if (attrs.tooltip && attrs.tooltip != '') {
            linkBody = '<div data-tooltip="' + attrs.tooltip +'" data-position="bottom center">' + linkBody + '</div>'
        }
        if (this.pageScope.variables?.actionName == attrs.action && !attrs.notActive) {
            aClass = aClass + ' active'
        }

        Map linkParams = [
                class: aClass,
                controller: attrs.controller,
                action: attrs.action,
                params: attrs.params
        ]
        if (attrs.onclick) {
            linkParams.onclick = attrs.onclick
        }

        if (attrs.controller) {
            out << g.link(linkParams, linkBody)
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
        def (text, message) = SwissKnife.getTextAndMessage(attrs)
        def tooltip = attrs.tooltip ?: "Die Funktion \'"+message+"\' ist zur Zeit nicht verfügbar!"

        out << '<a href="#" class="item"><div class="disabled" data-tooltip="'+tooltip+'" data-position="bottom center">'+message+'</div></a>'
    }

    def dropdownWithI18nExplanations = { attrs, body ->
        if (!attrs.name) {
            throwTagError("Tag [semui:dropdownWithI18nExplanations] is missing required attribute [name]")
        }
        if (!attrs.containsKey('from')) {
            throwTagError("Tag [semui:dropdownWithI18nExplanations] is missing required attribute [from]")
        }
        if (!attrs.containsKey('optionExpl')) {
            throwTagError("Tag [semui:dropdownWithI18nExplanations] is missing required attribute [optionExpl]")
        }
        def optionKey = attrs.remove('optionKey')
        def optionValue = attrs.remove('optionValue')
        def optionExpl = attrs.remove('optionExpl')

        out << "<div class='ui dropdown selection ${attrs.class}' id='${attrs.id}'>"
        out << "<input type='hidden' name='${attrs.name}' "
        if(attrs.value)
            out << "value='${attrs.value}'"
        out << ">"
        out << '<i class="dropdown icon"></i>'
        out << "<div class='default text'>${attrs.noSelection}</div>"
        out << '<div class="menu">'
        attrs.from?.each { el ->
            out << '<div class="item" data-value="'
            if(optionKey) {
                if(optionKey instanceof Closure)
                    out << optionKey(el)
                else out << el[optionKey]
            }
            out << '">'
            if(optionExpl instanceof Closure)
                out << '<span class="description">'+optionExpl(el)+'</span>'
            else out << '<span class="description">'+el[optionExpl]+'</span>'
            if(optionValue instanceof Closure)
                out << '<span class="text">'+optionValue(el).toString().encodeAsHTML()+'</span>'
            else out << '<span class="text">'+el[optionValue].toString().encodeAsHTML()+'</span>'
            out << '</div>'
        }
        out << '</div>'
        out << '</div>'

    }

    def menuDropdownItems = { attrs, body ->
        def (text, message) = SwissKnife.getTextAndMessage(attrs)
        String textMessage     = (text && message) ? text + " - " + message : text + message
        //def aClass = ((this.pageScope.variables?.actionName == attrs.actionName) ? 'item active' : 'item') + (attrs.class ? ' ' + attrs.class : '')

        out << '<div class="ui pointing dropdown link item '
        //out << aClass
        out << '">'
        out << textMessage
        out << '<i class="dropdown icon"></i> '
        out <<  '<div class="menu">'

        out <<          body()

        out << '</div>'
        out << '</div>'
    }

    def menuDropdownItem = { attrs, body ->
        def (text, message) = SwissKnife.getTextAndMessage(attrs)
        String linkBody  = (text && message) ? text + " - " + message : text + message
        String aClass    = ('item') + (attrs.class ? ' ' + attrs.class : '')

        if (attrs.disabled) {
            out << '<div class="item disabled">' + linkBody + '</div>'
        }
        else if (attrs.controller) {
            out << g.link(linkBody,
                    class: aClass,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params
            )
        }
        else {
            out << '<div class="item">'
            out << linkBody
            out << '</div>'
        }

        out << '<div class="divider"></div>'
    }

    def dropdown = { attrs, body ->
        if (!attrs.name) {
            throwTagError("Tag [semui:dropdown] is missing required attribute [name]")
        }
        if (!attrs.containsKey('from')) {
            throwTagError("Tag [semui:dropdown] is missing required attribute [from]")
        }

        def name = attrs.name
        def id = attrs.id
        def cssClass = attrs.class
        def from = attrs.from
        def optionKey = attrs.optionKey
        def optionValue = attrs.optionValue
        def iconWhich = attrs.iconWhich
        def requestParam = attrs.requestParam
        def display = attrs.display

        def noSelection = attrs.noSelection

        out << "<div class='ui fluid search selection dropdown ${cssClass}' data-requestParam='"+requestParam+"' data-display='"+display+"'>"

        out << "<input type='hidden' name='${name}'>"
        out << ' <i aria-hidden="true" class="dropdown icon"></i>'
        out << "<input class='search' id='${id}'>"
        out << ' <div class="default text">'
        out << "${noSelection}"

        out << '</div>'
        out << ' <div class="menu">'

        from.eachWithIndex { el, i ->
            out << '<div class="item" data-value="'
            //out <<    el.toString().encodeAsHTML()
            if (optionKey) {
                out << optionKey(el)
            }
            out <<  '">'
            out <<  optionValue(el).toString().encodeAsHTML()

            def tenant = el.hasProperty('tenant') ? el.tenant : null
            def owner  = el.hasProperty('owner') ? el.owner : null

            if (tenant != null || owner != null){
                out <<  " <i class='${iconWhich} icon'></i>"
            }
            out <<  '</div>'
        }
        // close <div class="menu">
        out <<  '</div>'

        // close <div class="ui fluid search selection dropdown">
        out << '</div>'

    }
}

