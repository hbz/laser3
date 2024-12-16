package de.laser

import de.laser.ui.Btn
import de.laser.ui.Icon
import de.laser.remote.Wekb

class LinkTagLib {

    ContextService contextService

    static namespace = 'ui'

    def linkWithIcon = { attrs, body ->
        out << '<span class="la-popup-tooltip" data-position="top right" data-content="' + message(code: 'tooltip.callUrl') + '" style="bottom:-3px">&nbsp;'
        out << '<a href="' + attrs.href + '" target="_blank" aria-label="' + attrs.href + '">'
        out << '<i class="' + (attrs.icon ?: Icon.LNK.EXTERNAL) + '" aria-hidden="true"></i>'
        out << '</a>'
        out << '</span>'
    }
    def buttonWithIcon = { attrs, body ->
        out << '<a class="ui button icon la-modern-button la-popup-tooltip" data-position="top right" '
        out <<        ' data-content="' + attrs.message + '" '
        out <<        'href="' + attrs.href + '" '
        out <<        'style="' +attrs.style + '" '
        out <<        'target="_blank" '
        out <<        'data-variation"' + attrs.variation + '" '
        out <<        'aria-label="' + attrs.href + '">'
        out << '<i class="' + attrs.icon  + '" aria-hidden="true"></i>'
        out << '</a>'
    }

    // <wekbIconLink href="${target}" />
    def wekbIconLink = { attrs, body ->
        String resourceUrl  = Wekb.getResourceShowURL() + '/' + attrs.gokbId
        String href         = ''
        String label        = 'Unbekannter Fehler'

        if (attrs.type == 'curatoryGroup') {
            href  = resourceUrl
            label = message(code: 'package.curatoryGroup.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'org') {
            href  = resourceUrl
            label = message(code: 'provider.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'package') {
            href  = resourceUrl
            label = message(code: 'package.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'platform') {
            href  = resourceUrl
            label = message(code: 'platform.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'provider') {
            href  = resourceUrl
            label = message(code: 'provider.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'source') {
            href  = resourceUrl
            label = message(code: 'package.source.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'tipp') {
            href  = resourceUrl
            label = message(code: 'title.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'vendor') {
            href  = resourceUrl
            label = message(code: 'vendor.label') + ' in der we:kb aufrufen'
        }
        out << '<span class="la-popup-tooltip" data-position="top right" data-content="' + label + '" >&nbsp;'
        out << '<a href="' + href + '" target="_blank" aria-label="' + label + '">'
        out << '<i class="' + Icon.WEKB + ' small" aria-hidden="true"></i>'
        out << '</a>'
        out << '</span>'
    }

    def wekbButtonLink = { attrs, body ->
        String resourceUrl  = Wekb.getResourceShowURL() + '/' + attrs.gokbId
        String href         = ''
        String label        = ''

        if (attrs.type == 'org') {
            href  = resourceUrl
            label = message(code: 'provider.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'vendor') {
            href  = resourceUrl
            label = message(code: 'vendor.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'platform') {
            href  = resourceUrl
            label = message(code: 'platform.label') + ' in der we:kb aufrufen'
        }

        out << '<a href="' + href + '" target="_blank" aria-label="' + label + '" '
        out << 'class="' + Btn.MODERN.SIMPLE_TOOLTIP + '" '
        out << 'data-position="top right" data-content="' + label + '" '
        out << 'role="button">'

        out << '<i class="' + Icon.WEKB + '" aria-hidden="true"></i>'
        out << '</a>'
    }

    def skipLink  = { attrs, body ->
//        out << '<!-- skip to main content / for screenreader --!>'
        out << '<nav class="la-skipLink" role="navigation" aria-label="' + message(code:'accessibility.jumpLink') + '">'
        out << '<p><a href="#main" class="la-screenReaderText">"' + message(code:'accessibility.jumpToMain') + '"</a></p>'
        out << '</nav>'
    }
}
