package de.laser

// Semantic UI

class SemanticUiTagLib {
    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "semui"

    // <laser:breadcrumbs>
    //     <laser:crumb controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // <laser:breadcrumbs>

    def breadcrumbs = { attrs, body ->

        out << '<div class="ui container">'
        out <<   '<div class="ui large breadcrumb">'
        out <<     crumb([controller: 'home', message:'default.home.label'])
        out <<     body()
        out <<   '</div>'
        out << '</div>'
    }

    // text             = raw text
    // message          = translate via i18n
    // class="active"   = no link

    // TODO: adopt for semantic ui
    def crumb = { attrs, body ->

        def lbText    = attrs.text ? attrs.text : ''
        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage

        if (attrs.controller) {
            out << g.link(linkBody,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params,
                    class: 'section ' + attrs.class,
                    id: attrs.id
            )
        }
        else {
            out << linkBody
        }
        if (! "active".equalsIgnoreCase(attrs.class.toString())) {
            out << ' <div class="divider">/</div> '
        }
    }

    def crumbAsBadge = { attrs, body ->

        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''

        out << '<li class="pull-right">'
        out << '<span class="badge ' + attrs.class + '">' + lbMessage + '</span>'
        out << '</li>'
    }
}
