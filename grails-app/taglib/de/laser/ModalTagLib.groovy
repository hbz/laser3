package de.laser

import de.laser.ContextService
import de.laser.SystemService
import de.laser.YodaService
import de.laser.ui.Btn
import de.laser.ui.Icon
import de.laser.utils.SwissKnife

class ModalTagLib {

    ContextService contextService
    SystemService systemService
    YodaService yodaService

    static namespace = 'ui'

    def debugInfo = { attrs, body ->

        if (yodaService.showDebugInfo()) {

            out << '<a href="#debugInfo" id="showDebugInfo" role="button" aria-label="Debug-Information" class="' + Btn.ICON.SIMPLE + ' grey la-debugInfos" data-ui="modal">'
            out << '<i aria-hidden="true" class="bug icon"></i>'
            out << '</a>'

            out << '<div id="debugInfo" class="ui modal">'
            out << '<h4 class="ui red header"> <i aria-hidden="true" class="bug icon"></i> DEBUG-INFORMATION</h4>'
            out << '<div class="scrolling content">'
            out << body()
            out << '<br />'
            out << '</div>'
            out << '<div class="actions">'
            out << '<a href="#" class="' + Btn.SIMPLE + '" onclick="$(\'#debugInfo\').modal(\'hide\')">Schließen</a>'
            out << '</div>'
            out << '</div>'
        }
    }
    //<ui:modalContact id="myModalDialog" text="${text}" message="local.string" hideSubmitButton="true" modalSize="large/small/tiny/mini" >
    // CONTENT
    // </ui:modalContact>

    def modalAddress = { attrs, body ->

        def (text, message) = SwissKnife.getTextAndMessage(attrs)
        String id           = attrs.id ? ' id="' + attrs.id + '" ' : ''
        String modalSize    = attrs.modalSize ? attrs.modalSize  : ''
        String refreshModal    = attrs.refreshModal ? 'longer'  : ''
        String title        = (text && message) ? text + " - " + message : text + message
        String isEditModal  = attrs.isEditModal

        String msgClose    = attrs.msgClose  ?: "${g.message(code:'default.button.close.label')}"
        String msgSave     = attrs.msgSave   ?: (isEditModal ? "${g.message(code:'default.button.save_changes')}" : "${g.message(code:'default.button.create.label')}")
        String msgDelete   = attrs.msgDelete ?: "${g.message(code:'default.button.delete.label')}"

        out << '<div role="dialog" class="ui ' + refreshModal + ' modal ' + modalSize + '"' + id + ' aria-label="Modal">'
        out << '<div class="header">' + title + '</div>'

        if (attrs.contentClass) {
            out << '<div class="content ' + attrs.contentClass + '">'
        } else {
            out << '<div class="content">'
        }

        out << body()
        out << '</div>'
        out << '<div class="actions">'
        out << '<button class="' + Btn.SIMPLE + ' deny" >' + msgClose + '</button>'

        if (attrs.showDeleteButton) {

            out << '<input type="submit" class="' + Btn.NEGATIVE + '" name="delete" value="' + msgDelete + '" onclick="'
            out << "return confirm('${g.message(code:'default.button.delete.confirmDeletion.message')}')?"
            out << '$(\'#' + attrs.id + '\').find(\'#' + attrs.deleteFormID + '\').submit():null'
            out << '"/>'
        }

        if (attrs.hideSubmitButton == true || attrs.hideSubmitButton == 'true') {
        }
        else {
            // todo: Btn.POSITIVE = JS-Trigger
            out << '<input type="submit" class="ui button green"" name="save" form="' + attrs.form + '" value="' + msgSave + '" />'
        }

        out << '</div>'
        out << '</div>'
    }

    //<ui:modal id="myModalDialog" text="${text}" message="local.string" hideSubmitButton="true" modalSize="large/small/tiny/mini" >
    // CONTENT
    // </ui:modal>

    def modal = { attrs, body ->

        def (text, message) = SwissKnife.getTextAndMessage(attrs)
        String id           = attrs.id ? ' id="' + attrs.id + '" ' : ''
        String modalSize    = attrs.modalSize ? attrs.modalSize  : ''
        String refreshModal    = attrs.refreshModal ? 'longer'  : ''
        String title        = (text && message) ? text + " - " + message : text + message
        String isEditModal  = attrs.isEditModal

        String msgClose    = attrs.msgClose  ?: "${g.message(code:'default.button.close.label')}"
        String msgSave     = attrs.msgSave   ?: (isEditModal ? "${g.message(code:'default.button.save_changes')}" : "${g.message(code:'default.button.create.label')}")
        String msgDelete   = attrs.msgDelete ?: "${g.message(code:'default.button.delete.label')}"

        out << '<div role="dialog" class="ui ' + refreshModal + ' modal ' + modalSize + '"' + id + ' aria-label="Modal">'
        if (title) {
            out << '<div class="header">' + title + '</div>'
        }

        if (attrs.contentClass) {
            out << '<div class="content ' + attrs.contentClass + '">'
        } else {
            out << '<div class="content">'
        }

        out << body()
        out << '</div>'
        out << '<div class="actions">'
        out << '<button class="' + Btn.SIMPLE + ' deny" >' + msgClose + '</button>'

        if (attrs.showDeleteButton) {

            out << '<input type="submit" class="' + Btn.NEGATIVE + '" name="delete" value="' + msgDelete + '" onclick="'
            out << "return confirm('${g.message(code:'default.button.delete.confirmDeletion.message')}')?"
            out << '$(\'#' + attrs.id + '\').find(\'#' + attrs.deleteFormID + '\').submit():null'
            out << '"/>'
        }

        if (attrs.hideSubmitButton == true || attrs.hideSubmitButton == 'true') {
        }
        else {
            if (attrs.formID) {
                // todo: Btn.POSITIVE = JS-Trigger
                out << '<input type="submit" class="ui button green" name="save" value="' + msgSave + '" onclick="event.preventDefault(); $(\'#' + attrs.id + '\').find(\'#' + attrs.formID + '\').submit();"/>'
            } else {
                // todo: Btn.POSITIVE = JS-Trigger
                out << '<input type="submit" class="ui button green" name="save" value="' + msgSave + '" onclick="event.preventDefault(); $(\'#' + attrs.id + '\').find(\'form\').submit()"/>'
            }
        }

        out << '</div>'
        out << '</div>'
    }

    //  <ui:infoModal> ${content} <ui:infoModal />

    def infoModal = { attrs, body ->

        String id        = attrs.id ? ' id="' + attrs.id + '" ' : ''
        String modalSize = attrs.modalSize ? attrs.modalSize  : ''
        String msgClose  = attrs.msgClose  ?: "${g.message(code:'default.button.merci.label')}"

        out << '<div role="dialog" class="ui modal ' + modalSize + '"' + id + ' aria-label="Modal">'
        out <<    '<div class="content ui items">'
        out <<       '<div class="item">'
        out <<          '<div class="image"><i class="' + Icon.UI.HELP + ' circular huge"></i></div>'
        out <<          '<div class="content">'
        out << body()
        out <<          '</div>'
        out <<       '</div>'
        out <<    '</div>'
        out <<    '<div class="actions">'
        out <<       '<button class="' + Btn.SIMPLE + ' ' + attrs.id + '" onclick="$(\'#' + attrs.id + '\').modal(\'hide\')">' + msgClose + '</button>'
        out <<    '</div>'
        out << '</div>'
    }

    //  <ui:confirmationModal  />
    // global included at semanticUI.gsp
    // called by the specific delete button
    //  - to send a form oridden
    //        <g:form data-confirm-id="${person?.id.toString()+ '_form'}">
    //        <div class="....... js-open-confirm-modal" data-confirm-term-what="diese Person" data-confirm-id="${person?.id}" >
    //  - to call a link
    //        <g:link class="..... js-open-confirm-modal" data-confirm-term-what="diese Kontaktdresse" ...... >

    def confirmationModal = { attrs, body ->
        String msgDelete = "Endgültig löschen"
        String msgCancel = "Abbrechen"

        out << '<div id="js-modal" class="ui tiny modal" role="dialog" aria-modal="true" tabindex="-1" aria-label="'+ "${message(code: 'wcag.label.confirmationModal')}" +'" >'
        out << '<div class="header">'
        out <<     '<span class="confirmation-term" id="js-confirmation-term"></span>'
        out << '</div>'

        out << '<div class="content confirmation-content" id="js-confirmation-content-term">'
        out << '</div>'

        out << '<div class="actions">'
        out << '<button class="' + Btn.SIMPLE + ' deny">' + msgCancel + '</button>'
        out << '<button id="js-confirmation-button" class="ui positive right labeled icon button">' + msgDelete
        out << '    <i aria-hidden="true" class="' + Icon.CMD.DELETE + '"></i>'
        out << '</button>'
        out << '</div>'
        out << '</div>'
    }
}
