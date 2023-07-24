package de.laser

import de.laser.auth.Role
import de.laser.storage.RDStore
import de.laser.titles.BookInstance
import de.laser.titles.DatabaseInstance
import de.laser.titles.JournalInstance

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
                icon = 'bordered inverted teal address book la-object-extended'
                break
            case 'admin':
                icon = 'la-object trophy'
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
            case 'datamanager':
                icon = 'la-object hdd'
                break
            case 'ebook':
                icon = 'bordered la-object-ebook'
                break
            case 'file':
                icon = 'bordered inverted blue file alternate la-object-extended'
                break
            case 'finance':
                icon = 'bordered inverted teal euro sign la-object-extended'
                break
            case 'help':
                icon = 'question circle bordered inverted grey la-object-extended'
                break
            case 'journal':
                icon = 'bordered la-object-journal'
                break
            case 'search':
                icon = 'search'
                break
            case 'reporting':
                icon = 'bordered inverted teal chartline icon la-object-extended'
                break
            case 'subscription':
                icon = 'bordered inverted orange clipboard la-object-extended'
                break
            case 'survey':
                icon = 'bordered inverted pink chart pie la-object-extended'
                break
            case 'task':
                icon = 'bordered inverted green calendar check outline la-object-extended'
                break
            case 'user':
                icon = 'user bordered inverted grey la-object-extended'
                break
            case 'workflow':
                icon = 'bordered inverted brown tasks la-object-extended'
                break
            case 'yoda':
                icon = 'la-object tools'
                break
        }
        }
        out << '<i aria-hidden="true" class="icon ' + icon + '"></i> '
    }

    def childSubscriptionIcon = { attrs, body ->
        out << '<i class="icon circular orange child la-subscriptionIsChild"></i> '
    }
    def objectMineIcon = { attrs, body ->
        out << '<div class="la-additionalIcon"><i class="icon circular star la-objectIsMine"></i></div> '
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
                icon = 'shipping fast'
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
                icon = 'calendar alternate outline'
                break
            case 'endDate':
                icon = 'calendar alternate outline'
                break
            case 'type':
                icon = 'image outline'
                break
            case 'manualCancellationDate':
                icon = 'calendar alternate outline'
                break
            case 'referenceYear':
                icon = 'calendar alternate outline'
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

        out << '<i aria-hidden="true" class="' + cssClass + ' icon ' + icon + '"></i> '

        if(showToolTipp) {
            out << '</span>'
        }
    }

    def listIcon = { attrs, body ->
        boolean hideTooltip = attrs.hideTooltip ? false : true

        boolean hideSurroundingMarkup = attrs.hideSurroundingMarkup ? false : true


        String dc = message(code: 'default.title.label')
        String icon = 'question'

        switch (attrs.type) {
            case [ 'Journal', JournalInstance.class.name ]:
                dc = message(code: 'spotlight.journaltitle')
                icon = 'newspaper outline'
                break
            case [ 'Database', DatabaseInstance.class.name ]:
                dc = message(code: 'spotlight.databasetitle')
                icon = 'database'
                break
            case [ 'Book', 'EBook', BookInstance.class.name ]:
                dc = message(code: 'spotlight.ebooktitle')
                icon = 'tablet alternate'
                break
            case 'Other':
                dc = message(code: 'spotlight.othertitle')
                icon = 'film'
                break
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
        String icon = 'icon address book la-list-icon'

        switch (attrs.type) {
            case [ 'E-Mail', 'Mail' ]: // 'Mail' -> Deprecated
                msg = message(code: 'contact.icon.label.email')
                icon = 'ui icon envelope outline la-list-icon  la-js-copyTriggerIcon'
                break
            case 'Fax':
                msg = message(code: 'contact.icon.label.fax')
                icon = 'ui icon tty la-list-icon'
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

    def customerTypeIcon = { attrs, body ->
        if (attrs.org && attrs.org.isCustomerType_Inst_Pro()) {
            out << '<span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center" data-content="' + attrs.org.getCustomerTypeI10n() + '">'
            if (attrs.cssClass) {
                out << '<i class="icon grey trophy ' + attrs.cssClass + '"></i>'
            } else {
                out << '<i class="icon grey trophy"></i>'
            }
            out << '</span>'
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
                    markup = markup + ' style="padding:3px;">'
                }

                switch (doc.confidentiality) {
                    case RDStore.DOC_CONF_PUBLIC:
                        markup = markup + '<i class="ui icon circle olive"></i> '
                        break;
                    case RDStore.DOC_CONF_INTERNAL:
                        markup = markup + '<i class="ui icon lock yellow"></i> '
                        break;
                    case RDStore.DOC_CONF_STRICTLY:
                        markup = markup + '<i class="ui icon lock orange"></i> '
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
}
