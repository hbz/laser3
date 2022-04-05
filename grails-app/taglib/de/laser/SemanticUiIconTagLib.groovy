package de.laser

import de.laser.titles.BookInstance
import de.laser.titles.DatabaseInstance
import de.laser.titles.JournalInstance

class SemanticUiIconTagLib {
    static namespace = "semui"

    def headerIcon = { attrs, body ->

        out << '<i aria-hidden="true" class="circular icon la-object"></i> '
    }

    def headerTitleIcon = { attrs, body ->

        switch (attrs.type) {
            case 'Journal':
                out << '<i aria-hidden="true" class="circular icon la-object-journal"></i> '
                break
            case 'Database':
                out << '<i aria-hidden="true" class="circular icon la-object-database"></i> '
                break
            case 'EBook':
                out << '<i aria-hidden="true" class="circular icon la-object-ebook"></i> '
                break
            case 'Survey':
                out << '<i aria-hidden="true" class="circular icon inverted pink chart pie"></i> '
                break
            case 'Workflow':
                out << '<i aria-hidden="true" class="circular icon inverted brown icon tasks"></i> '
                break
            case 'Subscription':
                out << '<i aria-hidden="true" class="circular icon inverted orange icon clipboard"></i> '
                break
            default:
                out << '<i aria-hidden="true" class="circular icon la-object"></i> '
                break
        }
    }
    def childSubscriptionIcon = { attrs, body ->
        out << '<i class="icon circular orange child la-subscriptionIsChild"></i> '
    }

    def subHeaderTitleIcon = { attrs, body ->

        switch (attrs.type) {
            case 'Journal':
                out << '<i aria-hidden="true" class="circular icon la-object-journal"></i> '
                break
            case 'Database':
                out << '<i aria-hidden="true" class="circular icon la-object-database"></i> '
                break
            case 'EBook':
                out << '<i aria-hidden="true" class="circular icon la-object-ebook"></i> '
                break
            case 'Survey':
                out << '<i aria-hidden="true" class="circular icon inverted pink chart pie"></i> '
                break
            case 'Workflow':
                out << '<i aria-hidden="true" class="circular icon inverted brown icon tasks"></i> '
                break
            case 'Subscription':
                out << '<i aria-hidden="true" class="circular icon inverted orange icon clipboard outline"></i> '
                break
            default:
                out << '<i aria-hidden="true" class="circular icon la-object"></i> '
                break
        }
    }

    def propertyIcon = { attrs, body ->
        def object = attrs.object
        String aClass = (attrs.class ? ' ' + attrs.class : '')
        String defaultToolTippContent = object ? message(code: "${object.getClass().getSimpleName().toLowerCase()}.${attrs.propertyName}.label") : null
        String toolTippContent = attrs.toolTippContent ?: (defaultToolTippContent ?: '')

        boolean showToolTipp = attrs.showToolTipp ?: false

        if(showToolTipp) {
            out << '<span class="la-popup-tooltip la-delay" data-content="' + toolTippContent + '" data-position="left center" data-variation="tiny">'
        }

        switch (attrs.propertyName) {
            case 'status':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon ellipsis vertical"></i> '
                break
            case 'kind':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon image outline"></i> '
                break
            case 'form':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon dolly"></i> '
                break
            case 'resource':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon box"></i> '
                break
            case 'isPublicForApi':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon shipping fast"></i> '
                break
            case 'hasPerpetualAccess':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon flag outline"></i> '
                break
            case 'hasPublishComponent':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon comment"></i> '
                break
            case 'startDate':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon calendar alternate outline"></i> '
                break
            case 'endDate':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon calendar alternate outline"></i> '
                break
            case 'type':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon image outline"></i> '
                break
            case 'manualCancellationDate':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon calendar alternate outline"></i> '
                break
            case 'licenseUrl':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon cloud"></i> '
                break
            case 'licenseCategory':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon clipboard list"></i> '
                break
            case 'openEnded':
                out << '<i aria-hidden="true" class="'+aClass+ ' icon key"></i> '
                break

            default:
                out << '<i aria-hidden="true" class="'+aClass+ ' icon "></i> '
                break
        }

        if(showToolTipp) {
            out << '</span>'
        }
    }


    def listIcon = { attrs, body ->
        boolean hideTooltip = attrs.hideTooltip ? false : true

        String dc = message(code: 'spotlight.title')
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

        out << '<div class="la-inline-flexbox la-popup-tooltip la-delay"'
        if (hideTooltip) {
            out << ' data-content="' + dc + '" data-position="left center" data-variation="tiny"'
        }
        out << '><i aria-hidden="true" class="icon ' + icon + ' la-list-icon"></i>'
        out << '</div>'
    }

    def ieAcceptStatusIcon = { attrs, body ->
        boolean hideTooltip = attrs.hideTooltip ? false : true

        switch (attrs.status) {
            case 'Fixed':
                out << '<div class="la-inline-flexbox la-popup-tooltip la-delay" '
                if (hideTooltip) {
                    out << 'data-content="' + message(code: 'issueEntitlement.acceptStatus.fixed') + '" data-position="left center" data-variation="tiny"'
                }
                out << '><i aria-hidden="true" class="icon certificate green"></i>'
                out << '</div>'
                break
            case 'Under Negotiation':
                out << '<div class="la-inline-flexbox la-popup-tooltip la-delay" '
                if (hideTooltip) {
                    out << 'data-content="' + message(code: 'issueEntitlement.acceptStatus.underNegotiation') + '" data-position="left center" data-variation="tiny"'
                }
                out << '><i aria-hidden="true" class="icon hourglass end yellow"></i>'
                out << '</div>'
                break
            case 'Under Consideration':
                out << '<div class="la-inline-flexbox la-popup-tooltip la-delay" '
                if (hideTooltip) {
                    out << 'data-content="' + message(code: 'issueEntitlement.acceptStatus.underConsideration') + '" data-position="left center" data-variation="tiny"'
                }
                out << '><i aria-hidden="true" class="icon hourglass start red"></i>'
                out << '</div>'
                break
            default:
                out << ''
                break
        }
    }

    def contactIcon = { attrs, body ->

        switch (attrs.type) {
            case 'E-Mail':
            case 'Mail': // Deprecated
                out << '<span class="la-popup-tooltip la-delay" data-content="' + message(code: 'contact.icon.label.email') + '" data-position="left center" data-variation="tiny">'
                out << '    <i aria-hidden="true" class="ui icon envelope outline la-list-icon js-copyTrigger"></i>'
                out << '</span>'
                break
            case 'Fax':
                out << '<span  class="la-popup-tooltip la-delay" data-content="' + message(code: 'contact.icon.label.fax') + '" data-position="left center" data-variation="tiny">'
                out << '    <i aria-hidden="true" class="ui icon tty la-list-icon"></i>'
                out << '</span>'
                break
            case 'Phone':
                out << '<span class="la-popup-tooltip la-delay" data-content="' + message(code: 'contact.icon.label.phone') + '" data-position="left center" data-variation="tiny">'
                out << '<i aria-hidden="true" class="icon phone la-list-icon"></i>'
                out << '</span>'
                break
            case 'Url':
                out << '<span class="la-popup-tooltip la-delay" data-content="' + message(code: 'contact.icon.label.url') + '" data-position="left center" data-variation="tiny">'
                out << '<i aria-hidden="true" class="icon globe la-list-icon"></i>'
                out << '</span>'
                break
            default:
                out << '<span  class="la-popup-tooltip la-delay" data-content="' + message(code: 'contact.icon.label.contactinfo') + '" data-position="left center" data-variation="tiny">'
                out << '<i aria-hidden="true" class="icon address book la-list-icon"></i>'
                out << '</span>'
                break
        }
    }

    def linkIcon = { attrs, body ->
        out << ' <span class="la-popup-tooltip la-delay" style="bottom: -3px" data-position="top right" data-content="Diese URL aufrufen ..">'
        out << '&nbsp;<a href="' + attrs.href + '" aria-label="' + attrs.href + '" target="_blank" class="ui icon blue la-js-dont-hide-button">'
        out << '<i aria-hidden="true" class="share square icon"></i>'
        out << '</a>'
        out << '</span>'
    }
}
