package de.laser

import de.laser.ui.Icon
import de.laser.utils.SwissKnife

class DropdownTagLib {

    static namespace = 'ui'

    def controlButtons = { attrs, body ->

        out << '<nav class="ui icon buttons la-js-ctrls" aria-label="' + message(code: 'wcag.label.actionMenu') + '">'
        out <<   body()
        out << '</nav>'
    }

    def exportDropdown = { attrs, body ->

        out << '<div class="ui simple dropdown button">'
        out <<     '<i class="' + Icon.CMD.DOWNLOAD + '"></i>'
        out <<     '<div class="menu">'
        out <<         body()
        out <<     '</div>'
        out << '</div>'
    }

    // <ui:exportDropdownItem> LINK </ui:exportDropdownItem>

    def exportDropdownItem = { attrs, body ->

        out << body()
    }

    // <ui:actionsDropdown params="${params}"  />

    def actionsDropdown = { attrs, body ->

        out << '<div class="ui simple dropdown button">'
        out <<     '<i class="magic icon"></i>'
        out <<     '<div class="menu" style="left:auto; right:0">'
        out <<         body()
        out <<     '</div>'
        out << '</div>'
    }

    def actionsDropdownItem = { attrs, body ->

        def (text, message) = SwissKnife.getTextAndMessage(attrs)
        String linkBody  = (text && message) ? text + " - " + message : text + message
        String cssClass  = attrs.class ? attrs.class + ' item' : 'item'
        String href      = attrs.href ? attrs.href : '#'

        if (attrs.tooltip && attrs.tooltip != '') {
            linkBody = '<div class="la-popup-tooltip" data-content="' + attrs.tooltip + '">' + linkBody + '</div>'
        }
        if (this.pageScope.variables?.actionName == attrs.action && !attrs.notActive) {
            cssClass = cssClass + ' active'
        }

        Map linkParams = [
                class: cssClass,
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
                out << ' id="' + attrs.id + '"'
            }
            if (attrs.'data-ui') { // e.g. binding modals
                out << ' data-ui="' + attrs.'data-ui' + '"'
            }
            out << '>'
            out << linkBody + '</a>'
        }
    }

    def actionsDropdownItemDisabled = { attrs, body ->
        def (text, message) = SwissKnife.getTextAndMessage(attrs)
        String tooltip = attrs.tooltip ?: "Die Funktion '${message}' ist zur Zeit nicht verfügbar!"

        out << '<a href="#" class="item disabled la-popup-tooltip la-delay" data-content="' + tooltip + '">' + message + '</a>'
    }

    def dropdownWithI18nExplanations = { attrs, body ->
        if (!attrs.name) {
            throwTagError("Tag [ui:dropdownWithI18nExplanations] is missing required attribute [name]")
        }
        if (!attrs.containsKey('from')) {
            throwTagError("Tag [ui:dropdownWithI18nExplanations] is missing required attribute [from]")
        }
        if (!attrs.containsKey('optionExpl')) {
            throwTagError("Tag [ui:dropdownWithI18nExplanations] is missing required attribute [optionExpl]")
        }

        String id           = attrs.id ?: ''
        String cssClass     = attrs.class ?: ''
        String noSelection  = attrs.noSelection ?: ''

        def optionKey = attrs.remove('optionKey')
        def optionValue = attrs.remove('optionValue')
        def optionExpl = attrs.remove('optionExpl')

        out << '<div class="ui dropdown selection ' + cssClass + '" id="' + id + '">'
        out <<     '<input type="hidden" name="' + attrs.name + '"' + (attrs.value ? ' value="' + attrs.value + '">' : '>')
        out <<     '<i class="dropdown icon"></i>'
        out <<     '<div class="default text">' + noSelection + '</div>'
        out <<     '<div class="menu">'

        attrs.from.each { el ->
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
        out <<     '</div>'
        out << '</div>'
    }

    def menuDropdownItems = { attrs, body ->
        def (text, message) = SwissKnife.getTextAndMessage(attrs)
        String textMessage     = (text && message) ? text + " - " + message : text + message

        out << '<div class="ui pointing dropdown link item">'
        out <<     textMessage
        out <<     '<i class="dropdown icon"></i>'
        out <<     '<div class="menu">'
        out <<         body()
        out <<     '</div>'
        out << '</div>'
    }

    def menuDropdownItem = { attrs, body ->
        def (text, message) = SwissKnife.getTextAndMessage(attrs)
        String linkBody  = (text && message) ? text + " - " + message : text + message
        String cssClass  = ('item') + (attrs.class ? ' ' + attrs.class : '')

        if (attrs.disabled) {
            out << '<div class="item disabled">' + linkBody + '</div>'
        }
        else if (attrs.controller) {
            out << g.link(linkBody,
                    class: cssClass,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params
            )
        }
        else {
            out << '<div class="item">' + linkBody + '</div>'
        }
        out << '<div class="divider"></div>'
    }

    def sortingDropdown = { attrs, body ->
        if (!attrs.containsKey('from')) {
            throwTagError("Tag [ui:sortingDropdown] is missing required attribute [from]")
        }
        String cssClass     = attrs.class ?: ''
        String noSelection  = attrs.noSelection ?: ''

        out << '<label>' + message(code: 'form.label.sort') + '</label>'
        out << '<select class="ui  selection dropdown la-js-sorting la-not-clearable ' + cssClass + '">'
        out <<     '<option value="">' + noSelection + '</option>'
        attrs.from.eachWithIndex { sortKey, sortValue, i ->
            String selectedAsc = attrs.sort == sortKey && attrs.order == 'asc' ? 'selected' : ''
            String selectedDesc = attrs.sort == sortKey && attrs.order == 'desc' ? 'selected' : ''
            out << '<option data-value="' + sortKey + '" data-order="asc" '+selectedAsc+'>'
            out <<    sortValue + ' (aufsteigend)'
            out << '</option>'
            out << '<option  data-value="' + sortKey + '" data-order="desc" '+selectedDesc+'>'
            out <<    sortValue + ' (absteigend)'
            out << '</option>'
        }
        out << '</select>'

    }

    def dropdown = { attrs, body ->
        if (!attrs.name) {
            throwTagError("Tag [ui:dropdown] is missing required attribute [name]")
        }
        if (!attrs.containsKey('from')) {
            throwTagError("Tag [ui:dropdown] is missing required attribute [from]")
        }

        String id           = attrs.id ?: ''
        String cssClass     = attrs.class ?: ''
        String noSelection  = attrs.noSelection ?: ''
        String iconWhich    = attrs.iconWhich ?: 'question'

        def optionKey = attrs.optionKey
        def optionValue = attrs.optionValue

        out << '<div class="ui fluid search selection dropdown ' + cssClass + '">'

        out <<     '<input type="hidden" name="' + attrs.name + '" />'
        out <<     '<i aria-hidden="true" class="dropdown icon"></i>'
        //out <<     '<input class="search" id="' + id + '" />'
        // TODO: Checking for accessibility
        out <<     '<div class="default text">' + noSelection + '</div>'
        out <<     '<div class="menu">'

        attrs.from.eachWithIndex { el, i ->
            out << '<div class="item" data-value="' + (optionKey ? optionKey(el) : '') + '">'
            out <<  optionValue(el).toString().encodeAsHTML()

            def tenant = el.hasProperty('tenant') ? el.tenant : null
            def owner  = el.hasProperty('owner') ? el.owner : null

            if (tenant != null || owner != null){
                out <<  '<i class="' + iconWhich + ' icon"></i>'
            }
            out <<     '</div>'
        }
        out <<     '</div>'
        out << '</div>'
    }
}
