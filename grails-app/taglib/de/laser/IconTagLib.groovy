package de.laser

import de.laser.auth.Role
import de.laser.helper.Icons
import de.laser.storage.RDStore

class IconTagLib {

    ContextService contextService

    static namespace = 'ui'

    def headerIcon = { attrs, body ->
        out << '<i aria-hidden="true" class="icon la-object"></i> '
    }

    def headerTitleIcon = { attrs, body ->
        String icon = 'la-object'

        if (attrs.type) {
        switch (attrs.type.toLowerCase()) {
            case 'addressbook':
                icon = Icons.ADDRESS_PUBLIC + ' bordered inverted teal la-object-extended'
                break
            case 'admin':
                icon = 'la-object tools'
                break
            case 'affiliation':
                Role fr = contextService.getUser().formalRole
                if (fr) {
                    if (fr.authority == Role.INST_USER)   { icon = 'user bordered inverted grey la-object-extended' }
                    if (fr.authority == Role.INST_EDITOR) { icon = 'user edit bordered inverted grey la-object-extended' }
                    if (fr.authority == Role.INST_ADM)    { icon = 'user shield bordered inverted grey la-object-extended' }
                } else {
                    icon = 'user slash bordered inverted grey la-object-extended'
                }
                break
            case 'database':
                icon = 'bordered la-object-database'
                break
            case 'ebook':
            case 'monograph':
                icon = 'bordered la-object-ebook'
                break
            case 'file':
                icon = Icons.DOCUMENT + ' bordered inverted blue la-object-extended'
                break
            case 'finance':
                icon = 'bordered inverted teal euro sign la-object-extended'
                break
            case 'help':
                icon = Icons.HELP + ' bordered inverted grey la-object-extended'
                break
            case 'journal':
            case 'serial':
                icon = 'bordered la-object-journal'
                break
            case 'gasco':
                icon = Icons.GASCO +' bordered inverted grey la-object-extended'
                break
            case 'marker':
                icon = Icons.MARKER + ' bordered inverted purple la-object-extended'
                break
            case 'package':
                icon = 'bordered la-package la-object-extended'
                break
            case 'search':
                icon = 'search'
                break
            case 'reporting':
                icon = Icons.REPORTING + ' bordered inverted teal la-object-extended'
                break
            case 'subscription':
                icon = Icons.SUBSCRIPTION + ' bordered inverted orange la-object-extended'
                break
            case 'survey':
                icon = Icons.SURVEY + ' bordered inverted pink la-object-extended'
                break
            case 'task':
                icon = Icons.TASK + ' bordered inverted green la-object-extended'
                break
            case 'user':
                icon = 'user bordered inverted grey la-object-extended'
                break
            case 'workflow':
                icon = Icons.WORKFLOW + ' bordered inverted brown la-object-extended'
                break
            case 'yoda':
                icon = 'la-object star of life'
                break
        }
        }
        out << '<i aria-hidden="true" class="icon ' + icon + '"></i> ' // TODO erms-5784 doubles 'icon'
    }

    def propertyIcon = { attrs, body ->
        def object = attrs.object
        String cssClass = (attrs.class ?: '')
        String defaultToolTippContent = object ? message(code: "${object.getClass().getSimpleName().toLowerCase()}.${attrs.propertyName}.label") : null
        String toolTippContent = attrs.toolTippContent ?: (defaultToolTippContent ?: '')

        boolean showToolTipp = attrs.showToolTipp ?: false
        String icon = ''

        if(showToolTipp) {
            out << '<span class="la-popup-tooltip la-delay" data-content="' + toolTippContent + '" data-position="left center" data-variation="tiny">'
        }

        switch (attrs.propertyName) {
            case 'status':
                icon = 'ellipsis vertical'
                break
            case 'kind':
                icon = 'image outline'
                break
            case 'form':
                icon = 'dolly'
                break
            case 'resource':
                icon = 'box'
                break
            case 'isPublicForApi':
                icon = 'lock open'
                break
            case 'hasPerpetualAccess':
                icon = 'flag outline'
                break
            case 'hasPublishComponent':
                icon = 'comment'
                break
            case 'holdingSelection':
                icon = 'pizza slice'
                break
            case 'startDate':
                icon = Icons.DATE
                break
            case 'endDate':
                icon = Icons.DATE
                break
            case 'type':
                icon = 'image outline'
                break
            case 'manualCancellationDate':
                icon = Icons.DATE
                break
            case 'referenceYear':
                icon = Icons.DATE
                break
            case 'licenseUrl':
                icon = 'cloud'
                break
            case 'licenseCategory':
                icon = 'clipboard list'
                break
            case 'openEnded':
                icon = 'key'
                break
        }

        out << '<i aria-hidden="true" class="' + cssClass + ' icon ' + icon + '"></i> ' // TODO erms-5784 doubles 'icon'

        if(showToolTipp) {
            out << '</span>'
        }
    }

    def listIcon = { attrs, body ->
        boolean hideTooltip = attrs.hideTooltip ? false : true

        boolean hideSurroundingMarkup = attrs.hideSurroundingMarkup ? false : true


        String dc = message(code: 'default.title.label')
        String icon = 'question'

        if (attrs.type) {

            switch (attrs.type.toLowerCase()) {
                case ['journal', 'serial']:
                    dc = attrs.type
                    icon = 'newspaper outline'
                    break
                case ['database']:
                    dc = attrs.type
                    icon = 'database'
                    break
                case ['book', 'ebook', 'monograph']:
                    dc = attrs.type
                    icon = 'tablet alternate'
                    break
                case 'other':
                    dc = attrs.type
                    icon = 'film'
                    break
            }
        }
        if (hideSurroundingMarkup) {
            out << '<div class="la-inline-flexbox la-popup-tooltip la-delay"'
        }
        if ( (hideTooltip) &&  (hideSurroundingMarkup) ){
            out << ' data-content="' + dc + '" data-position="left center" data-variation="tiny"'
        }
        if (hideSurroundingMarkup) {
            out << '>'
        }
        out << '<i aria-hidden="true" class="icon ' + icon + ' la-list-icon"></i>'
        if (hideSurroundingMarkup) {
            out << '</div>'
        }
    }

    def contactIcon = { attrs, body ->

        String msg = message(code: 'contact.icon.label.contactinfo')
        String icon = Icons.ADDRESS_PUBLIC + ' la-list-icon'

        switch (attrs.type) {
            case [ 'E-Mail', 'Mail' ]: // 'Mail' -> Deprecated
                msg = message(code: 'contact.icon.label.email')
                icon = Icons.EMAIL + ' la-list-icon la-js-copyTriggerIcon'
                break
            case 'Fax':
                msg = message(code: 'contact.icon.label.fax')
                icon = 'icon tty la-list-icon'
                break
            case 'Phone':
                msg = message(code: 'contact.icon.label.phone')
                icon = 'icon la-phone la-list-icon la-js-copyTriggerIcon'
                break
            case 'Url':
                msg = message(code: 'contact.icon.label.url')
                icon = 'icon globe la-list-icon'
                break
        }

        out << '<span class="la-popup-tooltip la-delay" data-content="' + msg + '" data-position="left center" data-variation="tiny">'
        out << '<i aria-hidden="true" class="' + icon + '"></i>'
        out << '</span>'
    }

    def customerTypeProIcon = { attrs, body ->
        if (attrs.org) {
//            if (attrs.org.isCustomerType_Inst_Pro()) {
            if (attrs.org.isCustomerType_Pro()) {
                out << '<span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center" data-content="' + attrs.org.getCustomerTypeI10n() + '">'

                String color = attrs.org.isCustomerType_Consortium() ? 'teal' : 'grey'
                if (attrs.cssClass) {
                    out << '<i class="icon ' + color + ' trophy ' + attrs.cssClass + '"></i>'
                } else {
                    out << '<i class="icon ' + color + ' trophy"></i>'
                }
                out << '</span>'
            }
        }
    }

    def documentIcon = { attrs, body ->
        if (attrs.doc) {
            Doc doc = attrs.doc as Doc

            if (doc && doc.confidentiality) {
                String markup = ''
                String conf = doc.confidentiality.getI10n('value')
                boolean showTooltip = 'true'.equalsIgnoreCase(attrs.showTooltip as String)
                boolean showText = 'true'.equalsIgnoreCase(attrs.showText as String)

                if (showTooltip) {
                    markup = '<span class="la-popup-tooltip la-delay" data-content="' + message(code: 'template.addDocument.confidentiality') + ': ' + conf + '"'
                    markup = markup + '>'
                }

                switch (doc.confidentiality) {
                    case RDStore.DOC_CONF_PUBLIC:
                        markup = markup + '<i class="icon lock open olive" style="margin-right: 0px"></i> '
                        break;
                    case RDStore.DOC_CONF_INTERNAL:
                        markup = markup + '<i class="icon lock yellow"  style="margin-right: 0px"></i> '
                        break;
                    case RDStore.DOC_CONF_STRICTLY:
                        markup = markup + '<i class="icon lock orange"  style="margin-right: 0px"></i> '
                        break;
                    default:
                        markup = markup + ''
                }

                if (showTooltip) { markup = markup + '</span>' }
                if (showText)    { markup = markup + conf }

                out << markup
            }
        }
    }

    def documentShareConfigIcon = { attrs, body ->
        if (attrs.docctx) {
            DocContext docctx = attrs.docctx as DocContext

            if (docctx.shareConf && !(docctx.sharedFrom && docctx.isShared)) {
                String markup = ''

                switch(docctx.shareConf) {
                    case RDStore.SHARE_CONF_UPLOADER_ORG:
                        markup = '<span class="ui icon la-popup-tooltip la-delay" data-content="'+ message(code:'org.docs.table.shareConf') + ': ' + RDStore.SHARE_CONF_UPLOADER_ORG.getI10n('value') + '" style="margin-left:1em"> <i class="icon eye slash blue"></i></span>'
                        break
                    case RDStore.SHARE_CONF_UPLOADER_AND_TARGET:
                        markup = '<span class="ui icon la-popup-tooltip la-delay" data-content="'+ message(code:'org.docs.table.shareConf') + ': ' + RDStore.SHARE_CONF_UPLOADER_AND_TARGET.getI10n('value') + '" style="margin-left:1em"> <i class="icon eye slash red"></i></span>'
                        break
                    case RDStore.SHARE_CONF_ALL:
                        markup = '<span class="ui icon la-popup-tooltip la-delay" data-content="'+ message(code:'org.docs.table.shareConf') + ': ' + RDStore.SHARE_CONF_ALL.getI10n('value') + '" style="margin-left:1em"> <i class="icon eye blue"></i></span>'
                        break
                }

                out << markup
            }
        }
    }

    def multiYearIcon = { attrs, body ->
        String tt = (attrs.isConsortial && attrs.isConsortial == 'true') ? message(code: 'subscription.isMultiYear.consortial.label') : message(code: 'subscription.isMultiYear.label')
//        String color = attrs.color ? ' ' + attrs.color : ''
        String color = attrs.color ? 'grey' : '' // tmp override

        out << '<span class="la-long-tooltip la-popup-tooltip la-delay"'
        out << ' data-position="bottom center" data-content="' + tt +'">'
        out << '<i class="' + Icons.SUBSCRIPTION_IS_MULTIYEAR + ' ' + color + '"></i>'
        out << '</span>'
    }

    def markerIcon = { attrs, body ->
        String tooltip = attrs.type ? message(code: 'marker.' + attrs.type ) : message(code: 'marker.label')

        out << '<span class="la-popup-tooltip la-delay" data-content="' + tooltip + '">'
        out << '<i class="' + Icons.MARKER + (attrs.color ? ' ' + attrs.color : '') + '"></i>'
        out << '</span>'
    }

    // <ui:myXIcon tooltip="optional" color="optional" />

    def myXIcon = { attrs, body ->

        if (attrs.tooltip) {
            out << '<span class="la-popup-tooltip la-delay" data-content="' + attrs.tooltip + '">'
        } else {
            out << '<span>'
        }
        out << '<i class="icon ' + (attrs.color ? attrs.color + ' ' : '') + 'star"></i>'
        out << '</span>'
    }

    def usageIcon = { attrs, body ->
        String tt = message(code: 'default.usage.label')
        String color = attrs.color ? ' grey' : '' // tmp override

        out << '<span class="la-long-tooltip la-popup-tooltip la-delay"'
        out << ' data-position="bottom center" data-content="' + tt +'">'
        out << '<i class="chart bar icon' + color + '"></i>'
        out << '</span>'
    }

    def booleanIcon = { attrs, body ->
        String icon = 'question circle yellow'

        if (attrs.value === true) {
            icon = 'check circle green'
        }
        else if(attrs.value === false) {
            icon = 'minus circle red'
        }
        else if (attrs.value === null) {
            'minus circle orange'
        }
        out << '<i class="icon ' + icon + '"></i>'
    }
}
