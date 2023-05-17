package de.laser

import de.laser.remote.ApiSource

class LinkTagLib {

    ContextService contextService

    static namespace = 'ui'

    // <externalIconLink href="${target}" tooltip="icon tooltip" />
    def externalIconLink = {attrs, body ->
        out << '<a href="' + attrs.href + '" target="_blank">' + (attrs.aBody ?: '') + ' '
        out << '<i class="external alternate icon"' + (attrs.tooltip ? ' title="' + attrs.tooltip + '"' : '') + '></i>'
        out << '</a>'
    }

    def wekbIconLink = { attrs, body ->
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

        if (attrs.type == 'org') {
            out << '<a href="' + apiSource.baseUrl + '/public/orgContent/' + attrs.gokbId + '" target="_blank"> <i class="icon external alternate"></i></a>'
        }
        else if (attrs.type == 'platform') {
            out << '<a href="' + apiSource.baseUrl + '/public/platformContent/' + attrs.gokbId + '" target="_blank"> <i class="icon external alternate"></i></a>'
        }
    }

    def wekbButtonLink = { attrs, body ->
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

        out << '<a class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay" '
        out << 'data-content="' + message(code: 'org.isWekbCurated.header.label') + '" '
        out << 'aria-label="' + message(code: 'org.isWekbCurated.header.label') + '" '
        out << 'role="button" '

        if (attrs.type == 'org') {
            out << 'href="' + apiSource.baseUrl + '/public/orgContent/' + attrs.gokbId + '" target="_blank">'
        }
        else if (attrs.type == 'platform') {
            out << 'href="' + apiSource.baseUrl + '/public/platformContent/' + attrs.gokbId + '" target="_blank">'
        }

        out << '<i class="la-gokb icon" aria-hidden="true"></i>'
        out << '</a>'
    }
}
