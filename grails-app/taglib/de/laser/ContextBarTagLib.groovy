package de.laser

import de.laser.auth.Role
import de.laser.auth.User
import de.laser.convenience.Marker
import de.laser.helper.Icons
import de.laser.interfaces.MarkerSupport

class ContextBarTagLib {

    ContextService contextService
    GenericOIDService genericOIDService

    static namespace = 'ui'

    // <ui:cbItemCustomerType org="${contextService.getOrg()}" />

    def cbItemCustomerType = {attrs, body ->
        String icon  = 'question icon grey'
        String text  = '?'
        Org org = attrs.org as Org

        if (!org) {
            icon  = Icons.ERROR + ' red'
            text  = message(code: 'profile.membership.error1')
        }
        else if (org.isCustomerType_Consortium_Pro()) {
            icon  = Icons.Auth.ORG_CONSORTIUM_PRO
            text  = Role.findByAuthority(CustomerTypeService.ORG_CONSORTIUM_PRO).getI10n('authority')
        }
        else if (org.isCustomerType_Consortium_Basic()) {
            icon  = Icons.Auth.ORG_CONSORTIUM_BASIC
            text  = Role.findByAuthority(CustomerTypeService.ORG_CONSORTIUM_BASIC).getI10n('authority')
        }
        else if (org.isCustomerType_Inst_Pro()) {
            icon  = Icons.Auth.ORG_INST_PRO
            text  = Role.findByAuthority(CustomerTypeService.ORG_INST_PRO).getI10n('authority')
        }
        else if (org.isCustomerType_Inst()) {
            icon  = Icons.Auth.ORG_INST_BASIC
            text  = Role.findByAuthority(CustomerTypeService.ORG_INST_BASIC).getI10n('authority')
        }
        else if (org.isCustomerType_Support()) {
            icon = Icons.Auth.ORG_SUPPORT
            text  = Role.findByAuthority(CustomerTypeService.ORG_SUPPORT).getI10n('authority')
        }

        out << '<div class="item la-cb-context">'
        out <<     '<span class="ui label" data-display="' + text + '">'
        out <<         '<i class="' + icon + '"></i>'
        out <<     '</span>'
        out << '</div>'
    }

    // <ui:cbItemUserAffiliation user="${contextService.getUser()}" showGlobalRole="true|false" />

    def cbItemUserAffiliation = {attrs, body ->
        String icon     = 'user slash icon'
        String color    = 'grey'
        String text     = '?'

        User user = attrs.user as User
        Role fr = user.formalRole

        if (fr) {
            if (fr.authority == Role.INST_USER) {
                icon = Icons.Auth.INST_USER
                text = message(code: 'cv.roles.INST_USER')
            }
            else if (fr.authority == Role.INST_EDITOR) {
                icon = Icons.Auth.INST_EDITOR
                text = message(code: 'cv.roles.INST_EDITOR')
            }
            else if (fr.authority == Role.INST_ADM) {
                icon = Icons.Auth.INST_ADM
                text = message(code: 'cv.roles.INST_ADM')
            }
        }
        else {
            icon  = Icons.ERROR
            color = 'red'
            text  = message(code: 'profile.membership.error2')
        }

        out << '<div class="item la-cb-context">'
        out <<     '<span class="ui label" data-display="' + text + '">'
        out <<         '<i class="' + icon + ' ' + color + '"></i>'
        out <<     '</span>'
        out << '</div>'
    }

    // <ui:cbItemUserSysRole user="${contextService.getUser()}" showGlobalRole="true|false" />

    def cbItemUserSysRole = {attrs, body ->
        String icon = 'question icon'
        String color = 'grey'
        String text = '?'

        User user = attrs.user as User

        if (user.isYoda()) {
            text = 'Systemberechtigung: YODA'
            icon = Icons.Auth.ROLE_YODA
        }
        else if (user.isAdmin()) {
            text = 'Systemberechtigung: ADMIN'
            icon = Icons.Auth.ROLE_ADMIN
        }

        if (icon) {
            out << '<div class="item la-cb-context">'
            out <<     '<span class="ui label" data-display="' + text + '">'
            out <<         '<i class="' + icon + ' ' + color + '"></i>'
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
        out <<         '<i class="icon ' + (attrs.icon ? attrs.icon + ' ' : '') + (attrs.color ? attrs.color + ' ' : '') + '"></i>' // TODO erms-5784 doubles 'icon'
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

        MarkerSupport obj   = (attrs.org ?: attrs.package ?: attrs.platform ?: attrs.provider ?: attrs.vendor ?: attrs.tipp) as MarkerSupport
        Marker.TYPE mType   = attrs.type ? Marker.TYPE.get(attrs.type as String) : Marker.TYPE.UNKOWN // TODO
        boolean isMarked    = obj.isMarked(contextService.getUser(), mType)
        String tt           = ''
        String tt_list      = message(code: 'marker.' + mType.value)

        if (attrs.org) {
            tt = isMarked ? 'Das Objekt' : 'das Objekt'
        }
        else if (attrs.package) {
            tt = isMarked ? 'Das Paket' : 'das Paket'
        }
        else if (attrs.platform) {
            tt = isMarked ? 'Die Plattform' : 'die Plattform'
        }
        else if (attrs.provider) {
            tt = isMarked ? 'Der Anbieter' : 'den Anbieter'
        }
        else if (attrs.vendor) {
            tt = isMarked ? 'Der Lieferant' : 'den Lieferanten'
        }
        else if (attrs.tipp) {
            tt = isMarked ? 'Der Titel' : 'den Titel'
        }

        if (tt) {
            tt = isMarked   ? tt + ' ist auf der Beobachtungsliste (' + tt_list + '). Anklicken, um zu entfernen.'
                            : 'Anklicken, um ' + tt + ' auf die Beobachtungsliste (' + tt_list + ') zu setzen.'
        }
        else {
            tt = '???'
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
                out <<              '<i class="' + Icons.MARKER + ' purple' + (isMarked ? '' : ' outline') + '"></i>'
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
                out <<              '<i class="' + (isMarked ? Icons.MARKER : 'la-bookmark slash icon' ) + '"></i>'
                out <<      '</div>'

                if (! attrs.ajax) {
                    out << '</div>'
                }
            }
        }
    }

}
