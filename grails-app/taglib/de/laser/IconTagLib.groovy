package de.laser

import de.laser.annotations.FixedFeature_DoNotModify
import de.laser.auth.Role
import de.laser.ui.Icon
import de.laser.storage.RDStore

class IconTagLib {

    ContextService contextService

    static namespace = 'ui'

    def headerIcon = { attrs, body ->
        out << '<i aria-hidden="true" class="icon la-object"></i> '
    }

    def headerTitleIcon = { attrs, body ->
        String icon = 'icon la-object'

        if (attrs.type) {
        switch (attrs.type.toLowerCase()) {
            case 'addressbook':
                icon = Icon.ACP_PRIVATE + ' bordered inverted teal la-object-extended'
                break
            case 'admin':
                icon = Icon.AUTH.ROLE_ADMIN + ' la-object'
                break
            case 'affiliation':
                Role fr = contextService.getUser().formalRole
                if (fr) {
                    icon = Icon.AUTH[fr.authority] + ' bordered inverted grey la-object-extended'
                } else {
                    icon = 'icon user slash bordered inverted grey la-object-extended'
                }
                break
            case 'consortium':
                icon = Icon.AUTH.ORG_CONSORTIUM_PRO + ' bordered inverted la-object-extended'
                break
            case [ CustomerTypeService.ORG_CONSORTIUM_BASIC.toLowerCase(),
                   CustomerTypeService.ORG_CONSORTIUM_PRO.toLowerCase(),
                   CustomerTypeService.ORG_INST_BASIC.toLowerCase(),
                   CustomerTypeService.ORG_INST_PRO.toLowerCase(),
                   CustomerTypeService.ORG_SUPPORT.toLowerCase() ]:
                icon = Icon.AUTH[attrs.type.toUpperCase()] + ' bordered inverted la-object-extended'
                break
            case 'database':
                icon = 'icon bordered la-object-database'
                break
            case 'dev':
                icon = 'icon code bordered inverted teal la-object-extended'
                break
            case [ 'ebook', 'monograph' ]:
                icon = 'icon bordered la-object-ebook'
                break
            case 'file':
                icon = Icon.DOCUMENT + ' bordered inverted blue la-object-extended'
                break
            case 'finance':
                icon = Icon.FINANCE + ' bordered inverted teal la-object-extended'
                break
            case 'help':
                icon = Icon.UI.HELP + ' bordered inverted grey la-object-extended'
                break
            case 'institution':
                icon = Icon.AUTH.ORG_INST_PRO + ' bordered inverted la-object-extended'
                break
            case [ 'journal', 'serial' ]:
                icon = 'icon bordered la-object-journal'
                break
            case 'gasco':
                icon = Icon.GASCO +' bordered inverted grey la-object-extended'
                break
            case 'marker':
                icon = Icon.MARKER + ' bordered inverted purple la-object-extended'
                break
            case 'package':
                icon = 'icon bordered la-package la-object-extended'
                break
            case 'search':
                icon = Icon.SYM.SEARCH
                break
            case 'reporting':
                icon = Icon.REPORTING + ' bordered inverted teal la-object-extended'
                break
            case 'subscription':
                icon = Icon.SUBSCRIPTION + ' bordered inverted orange la-object-extended'
                break
            case 'support':
                icon = Icon.AUTH.ORG_SUPPORT + ' bordered inverted la-object-extended'
                break
            case 'survey':
                icon = Icon.SURVEY + ' bordered inverted pink la-object-extended'
                break
            case 'task':
                icon = Icon.TASK + ' bordered inverted green la-object-extended'
                break
            case 'user':
                icon = Icon.AUTH.ROLE_USER + ' bordered inverted grey la-object-extended'
                break
            case 'workflow':
                icon = Icon.WORKFLOW + ' bordered inverted brown la-object-extended'
                break
            case 'yoda':
                icon = Icon.AUTH.ROLE_YODA + ' la-object'
                break
        }
        }
        out << '<i aria-hidden="true" class="' + icon + '"></i> '
    }

    def propertyIcon = { attrs, body ->
        def object = attrs.object
        String cssClass = (attrs.class ?: '')
        String defaultToolTippContent = object ? message(code: "${object.getClass().getSimpleName().toLowerCase()}.${attrs.propertyName}.label") : null
        String toolTippContent = attrs.toolTippContent ?: (defaultToolTippContent ?: '')

        boolean showToolTipp = attrs.showToolTipp ?: false
        String icon = ''

        if (showToolTipp) {
            out << '<span class="la-popup-tooltip" data-content="' + toolTippContent + '" data-position="left center" data-variation="tiny">'
        }

        switch (attrs.propertyName) {
            case 'status':
                icon = Icon.SYM.STATUS
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
                icon = Icon.SYM.IS_PUBLIC
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
                icon = Icon.SYM.DATE
                break
            case 'endDate':
                icon = Icon.SYM.DATE
                break
            case 'type':
                icon = 'image outline'
                break
            case 'manualCancellationDate':
                icon = Icon.SYM.DATE
                break
            case 'referenceYear':
                icon = Icon.SYM.DATE
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

        if (showToolTipp) {
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
            out << '<div class="la-inline-flexbox la-popup-tooltip"'
            if (hideTooltip){
                out << ' data-content="' + dc + '" data-position="left center" data-variation="tiny"'
            }
            out << '>'
        }
        out << '<i aria-hidden="true" class="icon ' + icon + ' la-list-icon"></i>'
        if (hideSurroundingMarkup) {
            out << '</div>'
        }
    }

    def contactIcon = { attrs, body ->

        String msg = message(code: 'contact.icon.label.contactinfo')
        String icon = Icon.ACP_PUBLIC

        switch (attrs.type) {
            case [ 'E-Mail', 'Mail' ]: // 'Mail' -> Deprecated
                msg = message(code: 'contact.icon.label.email')
                icon = Icon.SYM.EMAIL + ' la-js-copyTriggerIcon'
                break
            case 'Fax':
                msg = message(code: 'contact.icon.label.fax')
                icon = Icon.SYM.FAX
                break
            case 'Phone':
                msg = message(code: 'contact.icon.label.phone')
                icon = 'icon la-phone la-js-copyTriggerIcon'
                break
            case 'Url':
                msg = message(code: 'contact.icon.label.url')
                icon = Icon.SYM.URL
                break
        }

        out << '<span style="align-self:center" class="la-popup-tooltip" data-content="' + msg + '" data-position="left center" data-variation="tiny">'
        out << '<i aria-hidden="true" class="' + icon + ' la-list-icon"></i>'
        out << '</span>'
    }

    // <ui:customerTypeIcon org="${org}" option="text|tooltip" />

    def customerTypeIcon = { attrs, body ->
        if (attrs.org) {
            Org org = attrs.org as Org
            Map ctm = org.getCustomerTypeInfo()

            if (attrs.option == 'text') {
                out << '<span>'
                out << '<i class="' + ctm.icon + ' la-list-icon"></i>' + ctm.text
                out << '</span>'
            }
            else if (attrs.option == 'tooltip') {
                out << '<span class="la-long-tooltip la-popup-tooltip" data-position="top right" data-content="' + ctm.text + '">'
                out << '<i class="' + ctm.icon + '"></i>'
                out << '</span>'
            }
            else {
                out << '<i class="' + ctm.icon + ' la-list-icon"></i>'
            }
        }
    }

    // <ui:customerTypeOnlyProIcon org="${org}" cssClass="${css}" />

    def customerTypeOnlyProIcon = { attrs, body ->
        if (attrs.org) {
            if (attrs.org.isCustomerType_Pro()) {
                out << '<span class="la-long-tooltip la-popup-tooltip" data-position="bottom center" data-content="' + attrs.org.getCustomerTypeI10n() + '">'

                String icon = attrs.org.isCustomerType_Consortium() ? Icon.AUTH.ORG_CONSORTIUM_PRO : Icon.AUTH.ORG_INST_PRO
                if (attrs.cssClass) {
                    out << '<i class="' + icon + ' ' + attrs.cssClass + '"></i>'
                } else {
                    out << '<i class="' + icon + '"></i>'
                }
//                if (attrs.cssClass) {
//                    out << '<i class="' + Icon.AUTH.ORG_PRO + ' ' + attrs.cssClass + '"></i>'
//                } else {
//                    out << '<i class="' + Icon.AUTH.ORG_PRO + '"></i>'
//                }
                out << '</span>'
            }
        }
    }

    def documentIcon = { attrs, body ->
        if (attrs.doc) {
            Doc doc = attrs.doc as Doc

            if (doc && doc.confidentiality) {
                String markup = ''
                String tooltip = ''
                String conf = doc.confidentiality.getI10n('value')
                boolean showTooltip = 'true'.equalsIgnoreCase(attrs.showTooltip as String)
                boolean showText = 'true'.equalsIgnoreCase(attrs.showText as String)

                if (showTooltip) {
                    tooltip = 'data-content="' + message(code: 'template.addDocument.confidentiality') + ': ' + conf + '"'
                }

                switch (doc.confidentiality) {
                    case RDStore.DOC_CONF_PUBLIC:
                        markup = markup + '<i class="'  + (attrs.showTooltip ? 'corner la-popup-tooltip ' : '') + Icon.ATTR.DOCUMENT_CONFIDENTIALITY + ' olive" style="margin-right: 0px"' + tooltip +'></i> '
                        break;
                    case RDStore.DOC_CONF_INTERNAL:
                        markup = markup + '<i class="' + (attrs.showTooltip ? 'corner la-popup-tooltip ' : '') + Icon.ATTR.DOCUMENT_CONFIDENTIALITY + ' yellow" style="margin-right: 0px"' + tooltip +'></i> '
                        break;
                    case RDStore.DOC_CONF_STRICTLY:
                        markup = markup + '<i class="' + (attrs.showTooltip ? 'corner la-popup-tooltip ' : '') + Icon.ATTR.DOCUMENT_CONFIDENTIALITY + ' orange" style="margin-right: 0px"' + tooltip + '></i> '
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
                        markup = '<span class="la-popup-tooltip" data-content="' + message(code:'org.docs.table.shareConf') + ': ' + RDStore.SHARE_CONF_UPLOADER_ORG.getI10n('value') + '" style="margin-left:1em"> <i class="icon eye slash blue"></i></span>'
                        break
                    case RDStore.SHARE_CONF_UPLOADER_AND_TARGET:
                        markup = '<span class="la-popup-tooltip" data-content="' + message(code:'org.docs.table.shareConf') + ': ' + RDStore.SHARE_CONF_UPLOADER_AND_TARGET.getI10n('value') + '" style="margin-left:1em"> <i class="icon eye slash red"></i></span>'
                        break
                    case RDStore.SHARE_CONF_ALL:
                        markup = '<span class="la-popup-tooltip" data-content="' + message(code:'org.docs.table.shareConf') + ': ' + RDStore.SHARE_CONF_ALL.getI10n('value') + '" style="margin-left:1em"> <i class="icon eye blue"></i></span>'
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

        out << '<span class="la-long-tooltip la-popup-tooltip" data-position="bottom center" data-content="' + tt +'">'
        out << '<i class="' + Icon.ATTR.SUBSCRIPTION_IS_MULTIYEAR + ' ' + color + '"></i>'
        out << '</span>'
    }

    def markerIcon = { attrs, body ->
        String tooltip = attrs.type ? message(code: 'marker.' + attrs.type ) : message(code: 'marker.label')

        out << '<span class="la-popup-tooltip" data-content="' + tooltip + '">'
        out << '<i class="' + Icon.MARKER + (attrs.color ? ' ' + attrs.color : '') + '"></i>'
        out << '</span>'
    }

    // <ui:myXIcon tooltip="optional" color="optional" />

    def myXIcon = { attrs, body ->

        if (attrs.tooltip) {
            out << '<span class="la-popup-tooltip" data-content="' + attrs.tooltip + '">'
        } else {
            out << '<span>'
        }
        out << '<i class="icon ' + (attrs.color ? attrs.color + ' ' : '') + 'star"></i>'
        out << '</span>'
    }

    def usageIcon = { attrs, body ->
        String tt = message(code: 'default.usage.label')
        String color = attrs.color ? ' grey' : '' // tmp override

        out << '<span class="la-long-tooltip la-popup-tooltip" data-position="bottom center" data-content="' + tt +'">'
        out << '<i class="' + Icon.STATS + ' ' + color + '"></i>'
        out << '</span>'
    }

    def optionsIcon = { attrs, body ->
        out << '<span class="la-popup-tooltip" data-content="' + message(code:'default.actions.label') + '">'
        out << '  <i class="' + Icon.SYM.OPTIONS + '"></i>'
        out << '</span>'
    }

    @FixedFeature_DoNotModify
    def booleanIcon = { attrs, body ->
        String icon = Icon.SYM.UNKOWN

        if (attrs.value === true) {
            icon = Icon.SYM.CIRCLE + ' green'
        }
        else if(attrs.value === false) {
            icon = Icon.SYM.CIRCLE + ' red'
        }
        else if (attrs.value === null) {
            icon = Icon.SYM.CIRCLE + ' grey'
        }
        out << '<i class="' + icon + '"></i>'
    }
}
