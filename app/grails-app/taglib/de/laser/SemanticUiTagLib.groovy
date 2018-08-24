package de.laser

import com.k_int.kbplus.Subscription
import com.k_int.kbplus.auth.User
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.web.servlet.support.RequestContextUtils

// Semantic UI

class SemanticUiTagLib {

    def springSecurityService
    def yodaService

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "semui"

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

    // <semui:msg class="negative|positive|warning|.." header="${text}" text="${text}" message="18n.token" />

    def msg = { attrs, body ->

        out << '<div class="ui ' + attrs.class + ' message">'
        out <<   '<i class="close icon"></i>'
        out <<   '<div class="content">'

        if (attrs.header ) {
            out << '<div class="header">'
            out << attrs.header
            out << '</div>'
        }

        out <<     '<p>'
        if (attrs.text ) {
            out << attrs.text
        }
        if (attrs.message) {
            out << "${message(code: attrs.message)}"
        }
        out <<     '</p>'

        out <<   '</div>'
        out << '</div>'
    }

    // <semui:errors bean="${instanceOfObject}" />

    def errors = { attrs, body ->

        if (attrs.bean?.errors?.allErrors) {
            out << '<div class="ui negative message">'
            out <<   '<i class="close icon"></i>'
            out <<   '<ul class="list">'
            attrs.bean.errors.allErrors.each { e ->
                if (e in org.springframework.validation.FieldError) {
                    out << '<li data-field-id="${error.field}">'
                } else {
                    out << '<li>'
                }
                out << g.message(error: "${e}") + '</li>'
            }
            out <<   '</ul>'
            out << '</div>'
        }
    }

    // <semui:objectStatus object="${obj}" status="${status}"/>

    def objectStatus = { attrs, body ->

        if ('deleted'.equalsIgnoreCase(attrs.status?.value)) {

            out << '<div class="ui segment inverted red">'
            out << '<p><strong>' + message(code: 'default.object.isDeleted') + '</strong></p>'
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
        out << '<div class="content">'

        if (title) {
            //out << '<div class="content">'
            out <<   '<div class="header" style="display: inline">' + title + '</div>'
            if (attrs.editable && attrs.href) {
                out << '<button type="button" class="ui right floated  icon button editable-cancel" data-semui="modal" href="' + attrs.href + '" ><i class="plus icon"></i></button>'
            }
            //out << '</div>'
        }

        out <<     body()

        out << '</div>'
        out << '</div>'

    }

    //
    /*
    def editableLabel = { attrs, body ->

        if (attrs.editable) {
            out << '<div class="ui orange circular label" style="margin-left:0">'
            out << '<i class="write icon" style="margin-right:0"></i>'
            out << '</div>'
        }
    }
    */
    def debugInfo = { attrs, body ->

        if (yodaService.showDebugInfo()) {

            out << '<a href="#debugInfo" id="showDebugInfo" class="ui button icon" data-semui="modal">'
            out << '<i class="red bug icon"></i>'
            out << '</a>'

            out << '<div id="debugInfo" class="ui modal">'
            out <<     '<h4 class="ui red header"> <i class="bug icon"></i> DEBUG-INFORMATION</h4>'
            out <<     '<div class="scrolling content">'
            out <<          body()
            out <<          '<br />'
            out <<     '</div>'
            out <<     '<div class="actions">'
            out <<         '<a href="#" class="ui button" onclick="$(\'#debugInfo\').modal(\'hide\')">Schließen</a>'
            out <<     '</div>'
            out << '</div>'
        }
    }

    def headerIcon = { attrs, body ->

        out << '<div class="ui la-object circular label" style="margin-left:0; margin-right: 5px!important; ">'
        out << '<i class="icon"></i>'
        out << '</div>'
    }

    def headerTitleIcon = { attrs, body ->

        switch(attrs.type) {
            case 'Journal':
                out << '<div class="ui la-object-journal circular label" style="margin-left:0; margin-right: 5px!important; ">'
                break
            case 'Database':
                out << '<div class="ui la-object-database circular label" style="margin-left:0; margin-right: 5px!important; ">'
                break
            case 'EBook':
                out << '<div class="ui la-object-ebook circular label" style="margin-left:0; margin-right: 5px!important; ">'
                break
            default:
                out << '<div class="ui la-object circular label" style="margin-left:0; margin-right: 5px!important; ">'
                break
        }
        out << '<i class="icon"></i>'
        out << '</div>'
    }

    def listIcon = { attrs, body ->

        switch(attrs.type) {
            case 'Journal':
                out << '<div class="la-inline-flexbox" data-tooltip="' + message(code:'spotlight.journaltitle') + '" data-position="left center" data-variation="tiny">'
                out << '    <i class="icon newspaper outline la-list-icon"></i>'
                out << '</div>'
                break
            case 'Database':
                out << '<div class="la-inline-flexbox" data-tooltip="' + message(code:'spotlight.databasetitle') + '" data-position="left center" data-variation="tiny">'
                out << '    <i class="icon database la-list-icon"></i>'
                out << '</div>'
                break
            case 'EBook':
                out << '<div class="la-inline-flexbox" data-tooltip="' + message(code:'spotlight.ebooktitle') + '" data-position="left center" data-variation="tiny">'
                out << '    <i class="icon tablet alternate outline la-list-icon"></i>'
                out << '</div>'
                break
            default:
                out << '<div class="la-inline-flexbox" data-tooltip="' + message(code:'spotlight.title') + '" data-position="left center" data-variation="tiny">'
                out << '    <i class="icon book la-list-icon"></i>'
                out << '</div>'
                break
        }
    }

    def contactIcon = { attrs, body ->

        switch(attrs.type) {
            case 'E-Mail':
            case 'Mail': // Deprecated
                out << '<span  data-tooltip="' +message(code:'contact.icon.label.email') + '" data-position="left center" data-variation="tiny">'
                out << '    <i class="ui icon envelope outline la-list-icon"></i>'
                out << '</span>'
                break
            case 'Fax':
                out << '<span  data-tooltip="' +message(code:'contact.icon.label.fax') + '" data-position="left center" data-variation="tiny">'
                out << '    <i class="ui icon tty la-list-icon"></i>'
                out << '</span>'
                break
            case 'Phone':
                out << '<span  data-tooltip="' +message(code:'contact.icon.label.phone') + '" data-position="left center" data-variation="tiny">'
                out << '<i class="icon phone la-list-icon"></i>'
                out << '</span>'
                break
            case 'Url':
                out << '<span  data-tooltip="' +message(code:'contact.icon.label.url') + '" data-position="left center" data-variation="tiny">'
                out << '<i class="icon globe la-list-icon"></i>'
                out << '</span>'
                break
            default:
                out << '<span  data-tooltip="' +message(code:'contact.icon.label.contactinfo') + '" data-position="left center" data-variation="tiny">'
                out << '<i class="icon address book la-list-icon"></i>'
                out << '</span>'
                break
        }
    }

    def editableLabel = { attrs, body ->

        if (attrs.editable) {

            out << '<div class="ui green circular horizontal label"  style="margin-right:0; margin-left: 1rem;" data-tooltip="' + message(code:'statusbar.editable.tooltip') + '"  data-position="bottom right" data-variation="tiny">'
            out << '<i class="write  icon" style="margin-right:0"></i>'
            out << '</div>'
        }
    }
    // <semui:modeSwitch controller="controller" action="action" params="params" />


    def modeSwitch = { attrs, body ->

        //return;


        def mode = (attrs.params.mode=='basic') ? 'basic' : ((attrs.params.mode == 'advanced') ? 'advanced' : null)
        if (!mode) {
            def user = User.get(springSecurityService.principal.id)
            mode = (user.showSimpleViews?.value == 'No') ? 'advanced' : 'basic'

            // CAUTION: inject default mode
            attrs.params.mode = mode
        }


        /*

        out << '<div class="ui tiny buttons">'
        out << g.link( "${message(code:'profile.simpleView', default:'Basic')}",
                controller: attrs.controller,
                action: attrs.action,
                params: attrs.params + ['mode':'basic'],
                class: "ui mini button ${mode == 'basic' ? 'positive' : ''}"
        )

        //out << '<div class="or"></div>'

        out << g.link( "${message(code:'profile.advancedView', default:'Advanced')}",
                controller: attrs.controller,
                action: attrs.action,
                params: attrs.params + ['mode':'advanced'],
                class: "ui mini button ${mode == 'advanced' ? 'positive' : ''}"
        )
        out << '</div>'
        */
    }

    //<semui:meta> CONTENT <semui:meta>

    def meta = { attrs, body ->

        out << '<aside class="ui segment metaboxContent accordion">'
        out <<   '<div class="title"> <i class="dropdown icon la-dropdown-accordion"></i>Identifikatoren anzeigen</div>'
        out <<   '<div class="content">'
        out <<      body()
        out <<   '</div>'
        out << '</aside>'
        out << '<div class="metaboxContent-spacer"></div>'
    }

    //<semui:filter> CONTENT <semui:filter>

    def filter = { attrs, body ->

        out << '<div class="ui la-filter segment">'
        out <<   body()
        out << '</div>'
    }

    //<semui:form> CONTENT <semui:form>

    def form = { attrs, body ->

        out << '<div class="ui grey segment">'
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

    //<semui:modal id="myModalDialog" text="${text}" message="local.string" hideSubmitButton="true" > CONTENT <semui:modal>

    def modal = { attrs, body ->

        def id        = attrs.id ? ' id="' + attrs.id + '" ' : ''
        def text      = attrs.text ? attrs.text : ''
        def message   = attrs.message ? "${message(code: attrs.message)}" : ''
        def title     = (text && message) ? text + " - " + message : text + message
        def editmodal = attrs.editmodal

        def msgClose = "Schließen"
        def msgSave  = editmodal ? "Änderungen speichern" : "Anlegen"
        def msgDelete  = "Löschen"

        out << '<div class="ui modal"' + id + '>'
        out <<   '<div class="header">' + title + '</div>'
        out <<   '<div class="content">'
        out <<     body()
        out <<   '</div>'
        out <<   '<div class="actions">'
        out <<     '<a href="#" class="ui button '+ attrs.id +'" onclick="$(\'#' + attrs.id + '\').modal(\'hide\')">' + msgClose + '</a>'


        if (attrs.hideSubmitButton == null) {
            if (attrs.formID) {
                out << '<input type="submit" class="ui button green" name="save" value="' + msgSave + '" onclick="event.preventDefault(); $(\'#' + attrs.id + '\').find(\'#' + attrs.formID + '\').submit()"/>'
            }else {
                out << '<input type="submit" class="ui button green" name="save" value="' + msgSave + '" onclick="event.preventDefault(); $(\'#' + attrs.id + '\').find(\'form\').submit()"/>'
            }
        }
        if (attrs.deletebutton) {
            if (attrs.formdeleteID) {
                out <<   '<input type="submit" class="ui negative button" name="delete" value="' + msgDelete + '"onclick="'
                out <<   'return confirm(\'Sind Sie sicher, dass Sie dies löschen wollen?\')?$(\'#' + attrs.id + '\').find(\'#' + attrs.formdeleteID + '\').submit():$(\'#' + attrs.id + '\').modal(\'show\')'
                out <<   '"/>'
            }
        }
        out <<   '</div>'
        out << '</div>'
    }

    def confirmationModal = { attrs, body ->

        def id        = attrs.id ? ' id="' + attrs.id + '" ' : ''
        def text      = attrs.text ? attrs.text : ''
        def message   = attrs.message ? "${message(code: attrs.message)}" : ''
        def title     = (text && message) ? text + " - " + message : text + message
        def deletemodal = attrs.deletemodal

        def msgClose = "Schließen"
        def msgDelete  = deletemodal ? "Entgültig löschen" : "Entgültig ändern"
        def msgCancel  = "Abbrechen"

        out << '<div class="ui mini modal">'
        out <<   '<div class="header">' + title + '</div>'
        if (body) {
            out <<   '<div class="content">'
            out << body()
            out <<   '</div>'
        }
        out <<   '<div class="actions">'
        out <<     '<div class="ui deny button">'+ msgCancel+ '</div>'
        out <<     '<button class="ui positive right labeled icon button">'+ msgDelete
        out <<     '    <i class="trash alternate icon"></i>'
        out <<     '</button>'
        out <<   '</div>'
        out << '</div>'
    }



    //<semui:datepicker class="grid stuff here" label="" bean="${objInstance}" name="fieldname" value="" required="true" />

    def datepicker = { attrs, body ->

        def label       = attrs.label ? "${message(code: attrs.label)}" : '&nbsp'
        def name        = attrs.name ? "${message(code: attrs.name)}" : ''
        def placeholder = attrs.placeholder ? "${message(code: attrs.placeholder)}" : 'Date'
        def value       = attrs.value ?: ''
        def classes     = attrs.required ? 'field fieldcontain required' : 'field fieldcontain'
        def required    = attrs.required ? 'required="true"' : ''
        def hideLabel   = attrs.hideLabel ? false : true

        if (attrs.class) {
            classes += ' ' + attrs.class
        }
        // check for field errors
        if (attrs.bean && g.fieldError([bean:attrs.bean, field:"${name}"])) {
            classes += ' error'
        }

        out << '<div class="' + classes + '">'
        if (hideLabel) {
            out <<   '<label for="' + name + '">' + label + '</label>'
        }
        out <<   '<div class="ui calendar datepicker">'
        out <<      '<div class="ui input left icon">'
        out <<          '<i class="calendar icon"></i>'
        out <<          '<input name="' + name +'" type="text" placeholder="' + placeholder + '" value="' +  value + '"' + required +'>'
        out <<      '</div>'
        out <<   '</div>'
        out << '</div>'

    }

    public SemanticUiTagLib ( ) { } }
