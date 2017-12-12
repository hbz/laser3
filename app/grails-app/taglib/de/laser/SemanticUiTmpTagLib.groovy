package de.laser

import org.springframework.web.servlet.support.RequestContextUtils

class SemanticUiTmpTagLib {

    def springSecurityService

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "semui"

    //<semui:paginate .. />
    // copied from twitter.bootstrap.scaffolding.PaginationTagLib

    def paginate = { attrs ->

        if (attrs.total == null) {
            throwTagError("Tag [paginate] is missing required attribute [total]")
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

        out << '<div class="pagination" style="text-align:center">' // TODO CSS
        out << '<div class="ui basic buttons">'

        // display previous link when not on firststep
        if (currentstep > firststep) {
            linkParams.offset = offset - max
            if (currentstep == firststep) {
                linkTagAttrs.class = "ui button disabled prevLink"
            } else {
                linkTagAttrs.class = "ui button prevLink"
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
                    linkTagAttrs.class = "ui button active"
                } else {
                    linkTagAttrs.class = "ui button"
                }
                out << link(linkTagAttrs.clone()) {i.toString()}
            }
        }

        // display next link when not on laststep
        if (currentstep < laststep) {
            linkParams.offset = offset + max
            if (currentstep == laststep) {
                linkTagAttrs.class = "ui button disabled nextLink"
            } else {
                linkTagAttrs.class = "ui button nextLink"
            }
            def nextLinkAttrs = linkTagAttrs.clone()

            nextLinkAttrs += [title: (attrs.next ? attrs.next : messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))]
            out << link(nextLinkAttrs, '<i class="angle double right icon"></i>')
        }

        out << '</div>'
        out << '</div><!--.pagination-->'
    }
}
