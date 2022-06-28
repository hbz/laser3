package de.laser

import de.laser.titles.BookInstance
import de.laser.titles.DatabaseInstance
import de.laser.titles.JournalInstance

class SemuiIconTagLib {
    static namespace = "semui"

    def headerIcon = { attrs, body ->
        out << '<i aria-hidden="true" class="circular icon la-object"></i> '
    }

    def headerTitleIcon = { attrs, body ->
        String icon = 'la-object'

        switch (attrs.type) {
            case 'Journal':
                icon = 'la-object-journal'
                break
            case 'Database':
                icon = 'la-object-database'
                break
            case 'EBook':
                icon = 'la-object-ebook'
                break
            case 'Survey':
                icon = 'inverted pink chart pie'
                break
            case 'Workflow':
                icon = 'inverted brown tasks'
                break
            case 'Subscription':
                icon = 'inverted orange clipboard'
                break
        }
        out << '<i aria-hidden="true" class="circular icon ' + icon + '"></i> '
    }

    def childSubscriptionIcon = { attrs, body ->
        out << '<i class="icon circular orange child la-subscriptionIsChild"></i> '
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

        String msg = message(code: 'contact.icon.label.contactinfo')
        String icon = 'icon address book la-list-icon'

        switch (attrs.type) {
            case [ 'E-Mail', 'Mail' ]: // 'Mail' -> Deprecated
                msg = message(code: 'contact.icon.label.email')
                icon = 'ui icon envelope outline la-list-icon js-copyTrigger'
                break
            case 'Fax':
                msg = message(code: 'contact.icon.label.fax')
                icon = 'ui icon tty la-list-icon'
                break
            case 'Phone':
                msg = message(code: 'contact.icon.label.phone')
                icon = 'icon phone la-list-icon'
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

    def linkIcon = { attrs, body ->
        out << ' <span class="la-popup-tooltip la-delay" style="bottom: -3px" data-position="top right" data-content="Diese URL aufrufen ..">'
        out << '&nbsp;<a href="' + attrs.href + '" aria-label="' + attrs.href + '" target="_blank" class="ui icon blue la-js-dont-hide-button">'
        out << '<i aria-hidden="true" class="share square icon"></i>'
        out << '</a>'
        out << '</span>'
    }
}
