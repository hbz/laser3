package de.laser

import de.laser.remote.ApiSource

class LinkTagLib {

    ContextService contextService

    static namespace = 'ui'

    def linkWithIcon = { attrs, body ->
        out << '<span class="la-popup-tooltip la-delay" data-position="top right" data-content="' + message(code: 'tooltip.callUrl') + '" style="bottom:-3px">&nbsp;'
        out << '<a href="' + attrs.href + '" target="_blank" aria-label="' + attrs.href + '" class="la-js-dont-hide-button">'
        out << '<i class="icon external alternate" aria-hidden="true"></i>'
        out << '</a>'
        out << '</span>'
    }

    // <externalIconLink href="${target}" tooltip="icon tooltip" />
    def externalIconLink = {attrs, body ->
        out << '<a href="' + attrs.href + '" target="_blank" aria-label="' + attrs.href + '">'
        out << '<i class="icon external alternate" aria-hidden="true"' + (attrs.tooltip ? ' title="' + attrs.tooltip + '"' : '') + '></i>'
        out << '</a>'
    }

    // <wekbIconLink href="${target}" />
    def wekbIconLink = { attrs, body ->
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        String href = ''
        String label = ''

        if (attrs.type == 'curatoryGroup') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'package.curatoryGroup.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'org') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'default.provider.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'package') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'package.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'platform') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'platform.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'source') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'package.source.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'tipp') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'title.label') + ' in der we:kb aufrufen'
        }
        out << '<span class="la-popup-tooltip la-delay" data-position="top right" data-content="' + label + '" style="bottom: -3px">&nbsp;'
        out << '<a href="' + href + '" target="_blank" aria-label="' + label + '">'
        out << '<i class="icon la-gokb" aria-hidden="true"></i>'
        out << '</a>'
        out << '</span>'
    }

    def wekbButtonLink = { attrs, body ->
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        String href = ''
        String label = ''

        if (attrs.type == 'org') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'default.provider.label') + ' in der we:kb aufrufen'
        }
        else if (attrs.type == 'platform') {
            href = '' + apiSource.baseUrl + '/resource/show/' + attrs.gokbId
            label = message(code: 'platform.label') + ' in der we:kb aufrufen'
        }

        out << '<a href="' + href + '" target="_blank" aria-label="' + label + '" '
        out << 'class="ui icon blue button la-modern-button la-js-dont-hide-button la-popup-tooltip la-delay" '
        out << 'data-position="top right" data-content="' + label + '" '
//        out << 'data-content="' + message(code: 'org.isWekbCurated.header.label') + '" '
//        out << 'aria-label="' + message(code: 'org.isWekbCurated.header.label') + '" '
        out << 'role="button">'

        out << '<i class="icon la-gokb" aria-hidden="true"></i>'
        out << '</a>'
    }
}
