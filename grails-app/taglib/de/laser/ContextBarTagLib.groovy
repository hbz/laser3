package de.laser

import de.laser.auth.Role
import de.laser.auth.User
import de.laser.convenience.Marker
import de.laser.ui.Btn
import de.laser.ui.Icon
import de.laser.interfaces.MarkerSupport

class ContextBarTagLib {

    ContextService contextService
    GenericOIDService genericOIDService

    static namespace = 'ui'

    // <ui:cbItemCustomerType org="${contextService.getOrg()}" />

    def cbItemCustomerType = {attrs, body ->
        String icon  = Icon.SYM.UNKOWN + ' grey'
        String text  = '?'
        Org org = attrs.org as Org

        if (!org) {
            icon  = Icon.UI.ERROR + ' red'
            text  = message(code: 'profile.membership.error1')
        }
        else {
            Map ctm = org.getCustomerTypeInfo()
            if (ctm.icon && ctm.text) {
                icon = ctm.icon
                text = ctm.text
            }
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
                icon = Icon.AUTH.INST_USER
                text = message(code: 'cv.roles.INST_USER')
            }
            else if (fr.authority == Role.INST_EDITOR) {
                icon = Icon.AUTH.INST_EDITOR
                text = message(code: 'cv.roles.INST_EDITOR')
            }
            else if (fr.authority == Role.INST_ADM) {
                icon = Icon.AUTH.INST_ADM
                text = message(code: 'cv.roles.INST_ADM')
            }
        }
        else {
            icon  = Icon.UI.ERROR
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
        String icon     = Icon.SYM.UNKOWN
        String color    = 'grey'
        String text     = '?'

        User user = attrs.user as User

        if (user.isYoda()) {
            text = 'Systemberechtigung: YODA'
            icon = Icon.AUTH.ROLE_YODA
        }
        else if (user.isAdmin()) {
            text = 'Systemberechtigung: ADMIN'
            icon = Icon.AUTH.ROLE_ADMIN
        }

        if (icon != Icon.SYM.UNKOWN) {
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
            tt = isMarked ? 'Der Library Supplier' : 'den Library Supplier'
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

                out <<      '<a class="ui icon label la-popup-tooltip la-long-tooltip" onclick="' + onClick + '" '
                out <<          'data-content="' + tt + '" data-position="top right">'
                out <<              '<i class="' + Icon.MARKER + ' purple' + (isMarked ? '' : ' outline') + '"></i>'
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

                out <<      '<div class="' + Btn.ICON.SIMPLE_TOOLTIP + ' purple la-long-tooltip' + (isMarked ? ' active' : ' inactive') + '" onclick="' + onClick + '" '
                out <<          'data-content="' + tt + '" data-position="top right">'
                out <<              '<i class="' + (isMarked ? Icon.MARKER : 'la-bookmark slash icon' ) + '"></i>'
                out <<      '</div>'

                if (! attrs.ajax) {
                    out << '</div>'
                }
            }
        }
    }

}
