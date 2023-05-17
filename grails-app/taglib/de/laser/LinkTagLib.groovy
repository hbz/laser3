package de.laser

import de.laser.remote.ApiSource

class LinkTagLib {

    ContextService contextService

    static namespace = 'ui'

    // <externalIconLink href="${target}" tooltip="icon tooltip" />
    def externalIconLink = {attrs, body ->
        out << '<a href="' + attrs.href + '" target="_blank" aria-label="' + attrs.href + '">'
        out << '<i class="external alternate icon" aria-hidden="true"' + (attrs.tooltip ? ' title="' + attrs.tooltip + '"' : '') + '></i>'
        out << '</a>'
    }

    def wekbIconLink = { attrs, body ->
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

        if (attrs.type == 'org') {
            String href = '' + apiSource.baseUrl + '/public/orgContent/' + attrs.gokbId

            out << '<a href="' + href+ '" target="_blank" aria-label="' + href + '">'
            out << '<i class="icon external alternate" aria-hidden="true"></i>'
            out << '</a>'
        }
        else if (attrs.type == 'platform') {
            String href = '' + apiSource.baseUrl + '/public/platformContent/' + attrs.gokbId

            out << '<a href="' + href + '" target="_blank" aria-label="' + href + '">'
            out << '<i class="icon external alternate" aria-hidden="true"></i>'
            out << '</a>'
        }
    }

    def wekbButtonLink = { attrs, body ->
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

        out << '<a class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay" '
        out << 'data-content="' + message(code: 'org.isWekbCurated.header.label') + '" '
        out << 'aria-label="' + message(code: 'org.isWekbCurated.header.label') + '" '
        out << 'role="button" '

        if (attrs.type == 'org') {
            String href = '' + apiSource.baseUrl + '/public/orgContent/' + attrs.gokbId
            out << 'href="' + href + '" target="_blank" aria-label="' + href + '">'
        }
        else if (attrs.type == 'platform') {
            String href = '' + apiSource.baseUrl + '/public/platformContent/' + attrs.gokbId
            out << 'href="' + href + '" target="_blank" aria-label="' + href + '">'
        }

        out << '<i class="la-gokb icon" aria-hidden="true"></i>'
        out << '</a>'
    }
}
