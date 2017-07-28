package de.laser

class BreadCrumbsTagLib {
    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "laser"

    // <laser:breadcrumbs>
    //     <laser:crumb controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // <laser:breadcrumbs>

    def breadcrumbs = { attrs, body ->

        out << '<div class="container">'
        out <<   '<ul class="breadcrumb">'
        out <<     crumb([controller: 'home', message:'default.home.label'])
        out <<     body()
        out <<   '</ul>'
        out << '</div>'
    }

    // text             = raw text
    // message          = translate via i18n
    // class="active"   = no link

    def crumb = { attrs, body ->

        def lbText    = attrs.text ? attrs.text : ''
        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage

        out << '<li>'
        if (attrs.controller) {
            out << g.link(linkBody,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params,
                    class: attrs.class,
                    id: attrs.id
            )
        }
        else {
            out << linkBody
        }
        if (! "active".equalsIgnoreCase(attrs.class.toString())) {
            out << ' <span class="divider">/</span> '
        }
        out << '</li>'
    }

    def crumbAsBadge = { attrs, body ->

        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''

        out << '<li class="pull-right">'
        out << '<span class="badge ' + attrs.class + '">' + lbMessage + '</span>'
        out << '</li>'
    }
}
