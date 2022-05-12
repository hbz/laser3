package de.laser

import de.laser.helper.SwissKnife
import org.springframework.web.servlet.support.RequestContextUtils

class SemanticUiNavigationTagLib {

    def contextService
    def accessService

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "semui"


    // <semui:breadcrumbs>
    //     <semui:crumb controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // <semui:breadcrumbs>

    def breadcrumbs = { attrs, body ->

        out <<   '<nav class="ui tiny breadcrumb" aria-label="Sie sind hier:">'
        out <<      '<ul>'
        out <<      crumb([controller: 'home', ariaLabel:'Home', text:'<i class="home icon"></i>'])
        out <<          body()
        out <<      '</ul>'
        out <<   '</nav>'
    }

    // text             = raw text
    // message          = translate via i18n
    // class="active"   = no link

    def crumb = { attrs, body ->

        def (lbText, lbMessage) = SwissKnife.getTextAndMessage(attrs)
        String linkBody  = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage

        if (attrs.controller) {
            if (attrs.controller != 'home') {
                linkBody = linkBody.encodeAsHTML()
            }

            out << '<li>'
            out << g.link(
                    linkBody,
                    controller: attrs.controller,
                    "aria-label": attrs.ariaLabel,
                    action: attrs.action,
                    params: attrs.params,
                    class: 'section' + (attrs.class ? " ${attrs.class}" : ''),
                    id: attrs.id
            )
            if (! "active".equalsIgnoreCase(attrs.class.toString())) {
                out << '<span aria-hidden="true"> </span><i class="right angle icon divider"></i><span aria-hidden="true"> </span>'
            }
            out << '</li>'
        }
        else {
            out << '<li class="active section" aria-current="page">' << linkBody.encodeAsHTML() << '</li>'
        }

    }

    // <semui:crumbAsBadge message="default.editable" class="orange" />

    def crumbAsBadge = { attrs, body ->

        def (lbText, lbMessage) = SwissKnife.getTextAndMessage(attrs)
        out << '<div class="ui horizontal label ' + attrs.class + '">' + lbMessage + '</div>'
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

        def offset = attrs.int('offset') ?: 0
        if (! offset) offset = (params.int('offset') ?: 0)

        def max = attrs.int('max')
        if (! max) max = (params.int('max') ?: 10)

        def maxsteps = (attrs.int('maxsteps') ?: 6)

        if (total <= max) {
            return
        }

        def linkParams = [:]
        if (attrs.params) {
            linkParams.putAll(attrs.params)
        }

        linkParams.offset = offset - max
        linkParams.max = max


        if (params.sort) {
            linkParams.sort = params.sort
        }
        if (params.order) {
            linkParams.order = params.order
        }

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

        Map prevMap = [title: (attrs.prev ?: messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))]
        Map nextMap = [title: (attrs.next ?: messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))]

        // determine paging variables
        def steps = maxsteps > 0
        int currentstep = Math.round(Math.ceil(offset / max)) + 1
        int firststep = 1
        int laststep = Math.round(Math.ceil(total / max))

        out << '<div class="ui center aligned basic segment">'
        out << '<nav class="ui pagination menu" aria-label=' + message(code:'wcag.label.pagination') + '>'

        if (steps && laststep > firststep) {
            if (maxsteps > laststep) { // | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | > |
                // prev-buttons
                if (currentstep > firststep) {
                    // <
                    linkParams.offset = offset - max
                    linkTagAttrs.class = (currentstep == firststep) ? "item disabled prevLink" : "item prevLink"

                    def prevLinkAttrs2 = linkTagAttrs.clone()
                    out << link((prevLinkAttrs2 += prevMap), '<i class="angle left icon"></i>')
                }
                // steps
                for (int i in currentstep..(currentstep + maxsteps)) {
                    if (((i - 1) * max) < total) {
                        linkParams.offset = (i - 1) * max
                        if (currentstep == i) {
                            linkTagAttrs.class = "item active"
                        } else {
                            linkTagAttrs.class = "item"
                        }
                        out << link(linkTagAttrs.clone()) { i.toString() }
                    }
                }
                // next-buttons
                if (currentstep < laststep) {
                    // <
                    linkParams.offset = offset + max
                    linkTagAttrs.class = (currentstep == laststep) ? "item disabled nextLink" : "item nextLink"

                    def nextLinkAttrs1 = linkTagAttrs.clone()
                    out << link((nextLinkAttrs1 += nextMap), '<i class="angle right icon"></i>')
                }
            }
            else { // | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | ... | 8121 | > | >> |

                // | << | < |
                if (currentstep > firststep) {
                    // | << |
                    int tmp = (offset - (max * (maxsteps +1)))
                    linkParams.offset = tmp > 0 ? tmp : 0
                    linkTagAttrs.class = (currentstep == firststep) ? "item disabled prevLink" : "item prevLink"

                    def prevLinkAttrs1 = linkTagAttrs.clone()
                    out << link((prevLinkAttrs1 += prevMap), '<i class="double angle left icon"></i>')

                    // | < |
                    linkParams.offset = offset - max
                    linkTagAttrs.class = (currentstep == firststep) ? "item disabled prevLink" : "item prevLink"

                    def prevLinkAttrs2 = linkTagAttrs.clone()
                    out << link((prevLinkAttrs2 += prevMap), '<i class="angle left icon"></i>')
                }
                // steps | 1 | 2 | 3 | 4 |
                for (int i in currentstep..(currentstep + maxsteps)) {
                    if (((i) * max) < total) {
                        linkParams.offset = (i - 1) * max
                        if (currentstep == i) {
                            linkTagAttrs.class = "item active"
                        } else {
                            linkTagAttrs.class = "item"
                        }
                        out << link(linkTagAttrs.clone()) { i.toString() }
                    }
                }
                // | ... |
                if (currentstep < laststep-maxsteps-1) {
                    out << '  <div class="disabled item">\n' +
                            '    ...\n' +
                            '  </div>'
                }
                // laststep | 1154 |
                if (currentstep == laststep) {
                    linkTagAttrs.class = "item active"
                } else {
                    linkTagAttrs.class = "item"
                }

                def lastLinkAttrs = linkTagAttrs.clone()

                linkParams.offset = total - max
                out << link(lastLinkAttrs) {laststep.toString() }

                // | > | >> |
                if (currentstep < laststep) {
                    // | > |
                    linkParams.offset = offset + max
                    linkTagAttrs.class = (currentstep == laststep) ? "item disabled nextLink" : "item nextLink"

                    def nextLinkAttrs1 = linkTagAttrs.clone()
                    out << link((nextLinkAttrs1 += nextMap), '<i class="angle right icon"></i>')
                    if (currentstep < laststep-maxsteps-1) {
                        // | >> |
                        int tmp = linkParams.offset + (max * maxsteps)
                        linkParams.offset = tmp < total ? tmp : ((laststep - 1) * max)
                        linkTagAttrs.class = (currentstep == laststep) ? "item disabled nextLink" : "item nextLink"

                        def nextLinkAttrs2 = linkTagAttrs.clone()
                        out << link((nextLinkAttrs2 += nextMap), '<i class="double angle right icon"></i>')
                    }
                }
            }
        }
        // all button
        def allLinkAttrs = linkTagAttrs.clone()
        allLinkAttrs += [title: messageSource.getMessage('default.paginate.all', null, locale)]
        if(total <= 200) {
            allLinkAttrs.class = "item"
            allLinkAttrs.params.remove('offset')
            allLinkAttrs.params.max = 200
            out << link(allLinkAttrs, '<i class="list icon"></i>')
        }
        else {
            out << '<div class="disabled item la-popup-tooltip" data-content="'+messageSource.getMessage('default.paginate.listTooLong',null,locale)+'"><i class="list icon"></i></div>'
        }
        // Custom Input
        out << '<div class="item la-pagination-custom-input">'
        out << '    <div class="ui mini form">'
        out << '            <div class="field">'
        out << '                <input maxlength="8" placeholder="Seite:" type="text">'
        out << '                <i class="large chevron circle right link icon la-popup-tooltip" data-content="Gehe zur Seite"></i>'
        out << '            </div>'
        out << '    </div>'
        out << '</div>'
        out << '</nav>'
        out << '</div><!--.pagination-->'
    }


    // <semui:mainNavItem controller="controller" action="action" params="params" text="${text}" message="local.string" affiliation="INST_EDITOR" />

    def mainNavItem = { attrs, body ->

        def (lbText, lbMessage) = SwissKnife.getTextAndMessage(attrs)
        String linkBody  = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage

        if (attrs.generateElementId) {
            attrs.elementId = generateElementId(attrs)
        }

        out << g.link(linkBody,
                controller: attrs.controller,
                action: attrs.action,
                params: attrs.params,
                class: 'item' + (attrs.class ? " ${attrs.class}" : ''),
                elementId: attrs.elementId,
                role: attrs.role
        )
    }


    def securedMainNavItem = { attrs, body ->

        def (lbText, lbMessage) = SwissKnife.getTextAndMessage(attrs)
        String linkBody  = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage

        boolean check = SwissKnife.checkAndCacheNavPerms(attrs, request)

        if(attrs.newAffiliationRequests) {
            linkBody = linkBody + "<div class='ui floating red circular label'>${attrs.newAffiliationRequests}</div>";
        }

        if (attrs.generateElementId) {
            attrs.elementId = generateElementId(attrs)
        }

        if (check) {
            out << g.link(linkBody,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params,
                    class: 'item' + (attrs.class ? " ${attrs.class}" : ''),
                    elementId: attrs.elementId,
                    role: attrs.role
            )
        }
        else {
            if (attrs.affiliation && contextService.getUser().hasAffiliation(attrs.affiliation)) {
                out << '<div class="item disabled la-popup-tooltip la-delay" data-position="left center" data-content="' + message(code:'tooltip.onlyFullMembership') + '" role="menuitem">' + linkBody + '</div>'
            }
            else out << '<div class="item disabled la-popup-tooltip la-delay" data-position="left center" role="menuitem">' + linkBody + '</div>'
        }
    }

    def link = { attrs, body ->

        Map<Object, Object> filteredAttrs = attrs.findAll{ it ->
            ! (it.key in ['generateElementId', 'class'])
        }
        String css = attrs.class ? (attrs.class != 'item' ? attrs.class + ' item' : attrs.class) : 'item'
        filteredAttrs.put('class', css)

        if (attrs.generateElementId) {
            filteredAttrs.put('elementId', generateElementId(attrs))
        }

        out << g.link(filteredAttrs, body)
    }

    private String generateElementId(Map<String, Object> attrs) {

        // IMPORTANT: cache only for current request
        if (! request.getAttribute('laser_navigation_ids')) {
            request.setAttribute('laser_navigation_ids', [])
        }
        String elementId = attrs.controller + (attrs.action ? '-' + attrs.action : '')
        int counter

        while (((List) request.getAttribute('laser_navigation_ids')).contains(elementId)) {
            if (counter) {
                elementId = elementId.substring(0, elementId.lastIndexOf('-'))
            }
            else {
                counter = 1 // first index needed: 2
            }
            elementId = elementId + '-' + (++counter)
        }
        ((List) request.getAttribute('laser_navigation_ids')).add(elementId)

        elementId
    }
}
