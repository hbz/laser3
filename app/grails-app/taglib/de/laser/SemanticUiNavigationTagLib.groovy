package de.laser

import org.springframework.web.servlet.support.RequestContextUtils

class SemanticUiNavigationTagLib {

    def springSecurityService

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "semui"


    // <semui:breadcrumbs>
    //     <semui:crumb controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // <semui:breadcrumbs>

    def breadcrumbs = { attrs, body ->

        out <<   '<div class="ui large breadcrumb">'
        out <<     crumb([controller: 'home', text:'<i class="home icon"></i>'])
        out <<     body()
        out <<   '</div>'
    }

    // text             = raw text
    // message          = translate via i18n
    // class="active"   = no link

    def crumb = { attrs, body ->

        def lbText    = attrs.text ? attrs.text : ''
        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage

        if (attrs.controller) {
            out << g.link(linkBody,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params,
                    class: 'section' + (attrs.class ? " ${attrs.class}" : ''),
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

    // <semui:crumbAsBadge message="default.editable" class="orange" />

    def crumbAsBadge = { attrs, body ->

        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''

        out << '<div class="ui horizontal label ' + attrs.class + '">' + lbMessage + '</div>'
    }

    // <semui:subNav actionName="${actionName}">
    //     <semui:subNavItem controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // </semui:subNav>

    def subNav = { attrs, body ->

        out << '<div class="ui secondary pointing menu">'
        out <<   body()
        out << '</div>'
    }

    def subNavItem = { attrs, body ->

        def text      = attrs.text ? attrs.text : ''
        def message   = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (text && message) ? text + " - " + message : text + message
        def aClass    = 'item'
        if (this.pageScope.variables?.actionName == attrs.action) {
            aClass = 'item active'
        }
        if (attrs.controller) {
            out << g.link(linkBody,
                    class: aClass,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params
            )
        }
        else {
            out << linkBody
        }
    }


    //<semui:paginate .. />
    // copied from twitter.bootstrap.scaffolding.PaginationTagLib

    def paginate = { attrs ->

        if (attrs.total == null) {
            log.debug("throwTagError(\"Tag [paginate] is missing required attribute [total]\")")
        }

        def messageSource = grailsAttributes.messageSource
        def locale = RequestContextUtils.getLocale(request)

        def total = attrs.int('total') ?: 0
        def action = (attrs.action ? attrs.action : (params.action ? params.action : "list"))

        def offset = params.int('offset') ?: 0
        if (! offset) offset = (attrs.int('offset') ?: 0)

        def max = params.int('max')
        if (! max) max = (attrs.int('max') ?: 10)

        def maxsteps = (attrs.int('maxsteps') ?: 10)

        if (total <= max) {
            return
        }

        def linkParams = [:]
        if (attrs.params) linkParams.putAll(attrs.params)
        linkParams.offset = offset - max
        linkParams.max = max
        if (params.sort) linkParams.sort = params.sort
        if (params.order) linkParams.order = params.order

        def linkTagAttrs = [action: action]
        if (attrs.controller) {
            linkTagAttrs.controller = attrs.controller
        }
        if (attrs.id != null) {
            linkTagAttrs.id = attrs.id
        }
        if (attrs.fragment != null) {
            linkTagAttrs.fragment = attrs.fragment
        }
        linkTagAttrs.params = linkParams

        // determine paging variables
        def steps = maxsteps > 0
        int currentstep = (offset / max) + 1
        int firststep = 1
        int laststep = Math.round(Math.ceil(total / max))

        out << '<div class="ui center aligned basic segment">'
        out << '<div class="ui pagination menu">'

        // display previous link when not on firststep
        if (currentstep > firststep) {
            linkParams.offset = offset - max
            if (currentstep == firststep) {
                linkTagAttrs.class = "item disabled prevLink"
            } else {
                linkTagAttrs.class = "item prevLink"
            }
            def prevLinkAttrs = linkTagAttrs.clone()
            prevLinkAttrs += [title: (attrs.prev ?: messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))]
            out << link(prevLinkAttrs, '<i class="angle double left icon"></i>')
        }

        // display steps when steps are enabled and laststep is not firststep
        if (steps && laststep > firststep) {

            // determine begin and endstep paging variables
            int beginstep = currentstep - Math.round(maxsteps / 2) + (maxsteps % 2)
            int endstep = currentstep + Math.round(maxsteps / 2) - 1

            if (beginstep < firststep) {
                beginstep = firststep
                endstep = maxsteps
            }
            if (endstep > laststep) {
                beginstep = laststep - maxsteps + 1
                if (beginstep < firststep) {
                    beginstep = firststep
                }
                endstep = laststep
            }

            // display paginate steps
            for (int i in beginstep..endstep) {
                linkParams.offset = (i - 1) * max
                if (currentstep == i) {
                    linkTagAttrs.class = "item active"
                } else {
                    linkTagAttrs.class = "item"
                }
                out << link(linkTagAttrs.clone()) {i.toString()}
            }
        }

        // display next link when not on laststep
        if (currentstep < laststep) {
            linkParams.offset = offset + max
            if (currentstep == laststep) {
                linkTagAttrs.class = "item disabled nextLink"
            } else {
                linkTagAttrs.class = "item nextLink"
            }
            def nextLinkAttrs = linkTagAttrs.clone()

            nextLinkAttrs += [title: (attrs.next ? attrs.next : messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))]
            out << link(nextLinkAttrs, '<i class="angle double right icon"></i>')
        }

        out << '</div>'
        out << '</div><!--.pagination-->'
    }


    // <semui:mainNavItem controller="controller" action="action" params="params" text="${text}" message="local.string" affiliation="INST_EDITOR" />


    def mainNavItem = { attrs, body ->

        def lbText    = attrs.text ? attrs.text : ''
        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage

        if (attrs.affiliation && springSecurityService.getCurrentUser()?.hasAffiliation(attrs.affiliation)) {
            out << g.link(linkBody,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params,
                    class: 'item' + (attrs.class ? " ${attrs.class}" : ''),
                    id: attrs.id
            )
        }
        else {
            out << '<div class="item disabled">' + linkBody + '</div>'
        }
    }
}
