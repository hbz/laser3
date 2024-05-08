package de.laser

import de.laser.auth.Role
import de.laser.auth.User
import de.laser.convenience.Marker
import de.laser.interfaces.MarkerSupport

class ContextBarTagLib {

    ContextService contextService
    GenericOIDService genericOIDService

    static namespace = 'ui'

    // <ui:cbItemCustomerType org="${contextService.getOrg()}" />

    def cbItemCustomerType = {attrs, body ->
        String icon  = 'question'
        String color = 'grey'
        String text  = '?'
        Org org = attrs.org as Org

        if (!org) {
            icon  = 'exclamation circle'
            color = 'red'
            text  = message(code: 'profile.membership.error1')
        }
        else if (org.isCustomerType_Consortium_Pro()) {
            icon  = 'trophy'
            color = 'teal'
            text  = Role.findByAuthority(CustomerTypeService.ORG_CONSORTIUM_PRO).getI10n('authority')
        }
        else if (org.isCustomerType_Consortium_Basic()) {
            icon  = 'user circle'
            color = 'teal'
            text  = Role.findByAuthority(CustomerTypeService.ORG_CONSORTIUM_BASIC).getI10n('authority')
        }
        else if (org.isCustomerType_Inst_Pro()) {
            icon  = 'trophy'
            color = 'grey'
            text  = Role.findByAuthority(CustomerTypeService.ORG_INST_PRO).getI10n('authority')
        }
        else if (org.isCustomerType_Inst()) {
            icon  = 'user circle'
            color = 'grey'
            text  = Role.findByAuthority(CustomerTypeService.ORG_INST_BASIC).getI10n('authority')
        }
        else if (org.isCustomerType_Support()) {
            icon = 'theater masks'
            color = 'red'
            text  = Role.findByAuthority(CustomerTypeService.ORG_SUPPORT).getI10n('authority')
        }

        out << '<div class="item la-cb-context">'
        out <<     '<span class="ui label" data-display="' + text + '">'
        out <<         '<i class="icon ' + icon + ' ' + color + '"></i>'
        out <<     '</span>'
        out << '</div>'
    }

    // <ui:cbItemUserAffiliation user="${contextService.getUser()}" showGlobalRole="true|false" />

    def cbItemUserAffiliation = {attrs, body ->
        String icon = 'user slash'
        String color = 'grey'
        String text = '?'

        User user = attrs.user as User
        Role fr = user.formalRole

        if (fr) {
            if (fr.authority == Role.INST_USER) {
                icon = 'user'
                text = message(code: 'cv.roles.INST_USER')
            }
            else if (fr.authority == Role.INST_EDITOR) {
                icon = 'user edit'
                text = message(code: 'cv.roles.INST_EDITOR')
            }
            else if (fr.authority == Role.INST_ADM) {
                icon = 'user shield'
                text = message(code: 'cv.roles.INST_ADM')
            }
        }
        else {
            icon  = 'exclamation circle'
            color = 'red'
            text  = message(code: 'profile.membership.error2')
        }

        out << '<div class="item la-cb-context">'
        out <<     '<span class="ui label" data-display="' + text + '">'
        out <<         '<i class="icon ' + icon + ' ' + color + '"></i>'
        out <<     '</span>'
        out << '</div>'
    }

    // <ui:cbItemUserSysRole user="${contextService.getUser()}" showGlobalRole="true|false" />

    def cbItemUserSysRole = {attrs, body ->
        String icon = ''
        String color = 'grey'
        String text = '?'

        User user = attrs.user as User

        if (user.isYoda()) {
            text = 'Systemberechtigung: YODA'
            icon = 'star of life'
        }
        else if (user.isAdmin()) {
            text = 'Systemberechtigung: ADMIN'
            icon = 'tools'
        }

        if (icon) {
            out << '<div class="item la-cb-context">'
            out <<     '<span class="ui label" data-display="' + text + '">'
            out <<         '<i class="icon ' + icon + ' ' + color + '"></i>'
            out <<     '</span>'
            out << '</div>'
        }
    }

    // <ui:cbItemInfo icon="icon" display="optional" color="optional" />

    def cbItemInfo = { attrs, body ->

        String openSpan = '<span class="ui label">'
        if (attrs.display) {
            openSpan = '<span class="ui label" data-display="' + attrs.display + '">'
        }

        out << '<div class="item la-cb-info">'
        out <<     openSpan
        out <<         '<i class="icon ' + (attrs.icon ? attrs.icon + ' ' : '') + (attrs.color ? attrs.color + ' ' : '') + '"></i>'
        out <<     '</span>'
        out << '</div>'
    }

    // <cbItemToggleAction status="" tooltip="" icon="" />
    def cbItemToggleAction = { attrs, body ->

        String status = attrs.status ?: ''
        String tooltip = attrs.tooltip ?: ''
        String icon = attrs.icon ?: ''

        out << '<div class="item la-cb-action">'
        if (attrs.id) {
            out << '<button id="' + attrs.id + '" class="ui icon button ' + status + ' toggle la-toggle-green-red la-popup-tooltip la-delay" '
            // toggle -> JS
        } else {
            out << '<button class="ui icon button ' + status + ' toggle la-toggle-green-red la-popup-tooltip la-delay" ' // toggle -> JS
        }
        if (attrs.reload) {
            out <<      'data-reload="' + attrs.reload + '" '
        }
        out <<          'data-content="' + tooltip + '" data-position="bottom left">'
        out <<              '<i class="icon ' + icon + '"></i>'
        out <<     '</button>'
        out << '</div>'
    }

    // <ui:cbItemMarkerAction org="optional" package="optional" platform="optional" simple="true|false" />

    def cbItemMarkerAction = { attrs, body ->

        MarkerSupport obj   = (attrs.org ?: attrs.package ?: attrs.platform ?: attrs.vendor ?: attrs.tipp) as MarkerSupport
        Marker.TYPE mType   = attrs.type ? Marker.TYPE.get(attrs.type as String) : Marker.TYPE.UNKOWN // TODO
        boolean isMarked    = obj.isMarked(contextService.getUser(), mType)
        String tt           = '?'
        String tt_list      = message(code: 'marker.' + mType.value)

        if (attrs.org) {
            tt = isMarked ? 'Der Anbieter ist auf der ' + tt_list + '. Anklicken, um zu entfernen.'
                    : 'Anklicken, um den Anbieter auf die ' + tt_list + ' zu setzen.'
        }
        else if (attrs.package) {
            tt = isMarked ? 'Das Paket ist auf der ' + tt_list + '. Anklicken, um zu entfernen.'
                    : 'Anklicken, um das Paket auf die ' + tt_list + ' zu setzen.'
        }
        else if (attrs.platform) {
            tt = isMarked ? 'Der Plattform ist auf der ' + tt_list + '. Anklicken, um zu entfernen.'
                    : 'Anklicken, um die Plattform auf die ' + tt_list + ' zu setzen.'
        }
        else if (attrs.vendor) {
            tt = isMarked ? 'Der Lieferant ist auf der ' + tt_list + '. Anklicken, um zu entfernen.'
                    : 'Anklicken, um den Lieferanten auf die ' + tt_list + ' zu setzen.'
        }
        else if (attrs.tipp) {
            tt = isMarked ? 'Der Titel ist auf der ' + tt_list + '. Anklicken, um zu entfernen.'
                    : 'Anklicken, um den Titel auf die ' + tt_list + ' zu setzen.'
        }

        if (obj) {
            Map<String, Object> jsMap = [
                    controller:     'ajax',
                    action:         'toggleMarker',
                    data:           '{oid:\'' + genericOIDService.getOID(obj) + '\', type:\'' + mType + '\'}',
                    update:         '#marker-' + obj.id,
                    successFunc:    'tooltip.init(\'#marker-' + obj.id + '\')'
            ]

            if (attrs.simple) {
                jsMap.data = '{oid:\'' + genericOIDService.getOID(obj) + '\', type:\'' + mType + '\', simple: true}'
                String onClick = ui.remoteJsToggler(jsMap)

                if (! attrs.ajax) {
                    out << '<span id="marker-' + obj.id + '">'
                }

                out <<      '<a class="ui icon label la-popup-tooltip la-long-tooltip la-delay" onclick="' + onClick + '" '
                out <<          'data-content="' + tt + '" data-position="top right">'
                out <<              '<i class="icon purple bookmark' + (isMarked ? '' : ' outline') + '"></i>'
                out <<      '</a>'

                if (! attrs.ajax) {
                    out << '</span>'
                }
            }
            else {
                String onClick = ui.remoteJsToggler(jsMap)

                if (! attrs.ajax) {
                    out << '<div class="item la-cb-action" id="marker-' + obj.id + '">'
                }

                out <<      '<div class="ui icon button purple ' + (isMarked ? 'active' : ' inactive ') + ' la-popup-tooltip la-long-tooltip la-delay" onclick="' + onClick + '" '
                out <<          'data-content="' + tt + '" data-position="top right">'
                out <<              '<i class="icon ' + (isMarked ? 'bookmark' : ' la-bookmark slash' ) + '"></i>'
                out <<      '</div>'

                if (! attrs.ajax) {
                    out << '</div>'
                }
            }
        }
    }

}
