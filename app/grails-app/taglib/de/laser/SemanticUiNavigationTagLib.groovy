package de.laser

import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.web.servlet.support.RequestContextUtils

class SemanticUiNavigationTagLib {

    def springSecurityService
    def contextService
    def accessService

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "semui"


    // <semui:breadcrumbs>
    //     <semui:crumb controller="controller" action="action" params="params" text="${text}" message="local.string" />
    // <semui:breadcrumbs>

    def breadcrumbs = { attrs, body ->

        out <<   '<nav class="ui breadcrumb">'
        out <<     crumb([controller: 'home', text:'<i class="home icon"></i>'])
        out <<     body()
        out <<   '</nav>'
    }

    // text             = raw text
    // message          = translate via i18n
    // class="active"   = no link

    def crumb = { attrs, body ->

        def lbText    = attrs.text ? attrs.text : ''
        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage

        if (attrs.controller) {

            if (attrs.controller != 'home') {
                linkBody = linkBody.encodeAsHTML()
            }

            out << g.link(
                    linkBody,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params,
                    class: 'section' + (attrs.class ? " ${attrs.class}" : ''),
                    id: attrs.id
            )
        }
        else {
            out << linkBody.encodeAsHTML()
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

/**
        out << '//////////////////////////////////////'
        out << '<br>'
        out << 'total (Items gesamt): '
        out << total
        out << '<br>'
        out << 'max (Items pro Seite): '
        out << max
        out << '<br>'
        out << 'maxsteps (Steps eingestellt pro Seite ): '
        out << maxsteps
        out << '<br>'
        out << 'offset (Items, die vor dem aktuellen Step angezeigt werden ): '
        out << offset
        out << '<br>'
        out << 'laststep (Step, der als letztes erscheint): '
        out << laststep
        out << '<br>'
        out << 'steps (wahr wenn Steps eingestellt pro Seite größer als 0 ): '
        out << steps
        out << '<br>'
        out << 'currentstep (): '
        out << currentstep
        out << '<br>'
        out << '//////////////////////////////////////'
 */

        // steps im Falle alle notwendigen Steps können angezeigt werden
        if (maxsteps > laststep) {
            out << '<br>'
            out << 'es passen alle steps in die Paginieung'

        }
        out << '<div class="ui center aligned basic segment">'
        out << '<div class="ui pagination menu">'

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
        allLinkAttrs.class = "item"

        allLinkAttrs.params.remove('offset')
        allLinkAttrs.params.max = 100000
        allLinkAttrs += [title: messageSource.getMessage('default.paginate.all', null, 'Show all', locale)]

        out << link(allLinkAttrs, '<i class="list icon"></i>')

        out << '</div>'
        out << '</div><!--.pagination-->'
    }


    // <semui:mainNavItem controller="controller" action="action" params="params" text="${text}" message="local.string" affiliation="INST_EDITOR" />

    def mainNavItem = { attrs, body ->

        def lbText    = attrs.text ? attrs.text : ''
        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage

        out << g.link(linkBody,
                controller: attrs.controller,
                action: attrs.action,
                params: attrs.params,
                class: 'item' + (attrs.class ? " ${attrs.class}" : ''),
                id: attrs.id
        )
    }


    def securedMainNavItem = { attrs, body ->

        def lbText    = attrs.text ? attrs.text : ''
        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage

        boolean check = SpringSecurityUtils.ifAnyGranted(attrs.specRole ?: [])

        if(attrs.newAffiliationRequests) {
            linkBody = linkBody + "<div class='ui floating red circular label'>${attrs.newAffiliationRequests}</div>";
        }

        if (!check) {
            if (attrs.affiliation && attrs.orgPerm) {
                if (contextService.getUser()?.hasAffiliation(attrs.affiliation) && accessService.checkPerm(attrs.orgPerm)) {
                    check = true
                }
            }
            else if (attrs.affiliation && contextService.getUser()?.hasAffiliation(attrs.affiliation)) {
                check = true
            }
            else if (attrs.orgPerm && accessService.checkPerm(attrs.orgPerm)) {
                check = true
            }
        }

        if (check) {
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

    // introduced as of December 3rd, 2018 with ticket #793
    // <semui:securedMainNavItemDisabled controller="controller" action="action" params="params" text="${text}" message="local.string" affiliation="INST_EDITOR" />


    def securedMainNavItemDisabled = { attrs, body ->

        def lbText    = attrs.text ? attrs.text : ''
        def lbMessage = attrs.message ? "${message(code: attrs.message)}" : ''
        def linkBody  = (lbText && lbMessage) ? lbText + " - " + lbMessage : lbText + lbMessage

        out << '<div class="item"><div class="disabled" data-tooltip="Die Funktion \''+lbMessage+'\' ist zur Zeit nicht verfügbar!">' + linkBody + '</div></div>'
    }

}
