package de.laser

import de.laser.ui.Btn
import de.laser.ui.Icon
import de.laser.remote.ApiSource

class LinkTagLib {

    ContextService contextService

    static namespace = 'ui'

    def linkWithIcon = { attrs, body ->
        String icon = attrs.icon ?: Icon.LNK.EXTERNAL // TODO erms-5784 doubles 'icon'

        out << '<span class="la-popup-tooltip la-delay" data-position="top right" data-content="' + message(code: 'tooltip.callUrl') + '" style="bottom:-3px">&nbsp;'
        out << '<a href="' + attrs.href + '" target="_blank" aria-label="' + attrs.href + '">'
        out << '<i class="' + icon + ' icon" aria-hidden="true"></i>'
        out << '</a>'
        out << '</span>'
    }

    // <wekbIconLink href="${target}" />
    def wekbIconLink = { attrs, body ->
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        String href = ''
        String label = 'Unbekannter Fehler'

        if (attrs.type == 'curatoryGroup') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'package.curatoryGroup.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'org') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'provider.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'package') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'package.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'platform') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'platform.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'provider') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'provider.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'source') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'package.source.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'tipp') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'title.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'vendor') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'vendor.label') + ' in der we:kb aufrufen'
        }
        out << '<span class="la-popup-tooltip la-delay" data-position="top right" data-content="' + label + '" >&nbsp;'
        out << '<a href="' + href + '" target="_blank" aria-label="' + label + '">'
        out << '<i class="' + Icon.WEKB + ' small" aria-hidden="true"></i>'
        out << '</a>'
        out << '</span>'
    }

    def wekbButtonLink = { attrs, body ->
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        String href = ''
        String label = ''

        if (attrs.type == 'org') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'provider.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'vendor') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'vendor.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'platform') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'platform.label') + ' in der we:kb aufrufen'
        }

        out << '<a href="' + href + '" target="_blank" aria-label="' + label + '" '
        out << 'class="' + Btn.MODERN.SIMPLE_ICON_TOOLTIP + '" '
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
