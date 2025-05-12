package de.laser

import de.laser.annotations.FixedFeature_DoNotModify
import de.laser.auth.User
import de.laser.cache.SessionCacheWrapper
import de.laser.ui.Btn
import de.laser.ui.Icon
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.utils.SwissKnife
import org.apache.groovy.io.StringBuilderWriter
import org.grails.encoder.CodecLookup
import org.grails.encoder.Encoder
import org.grails.taglib.TagLibraryLookup
import org.grails.taglib.TagOutput
import org.grails.taglib.encoder.OutputContextLookupHelper
import org.grails.web.servlet.GrailsFlashScope
import org.springframework.context.MessageSource
import org.springframework.web.servlet.support.RequestContextUtils

import java.text.SimpleDateFormat

class UiTagLib {

    AuditService auditService
    CodecLookup codecLookup
    ContextService contextService
    FormService formService
    GenericOIDService genericOIDService
    TagLibraryLookup gspTagLibraryLookup

    static namespace = 'ui'

    // <ui:h1HeaderWithIcon text="${text}" message="18n.token" args="[key:value]" type="${headerTitleIconType}" total="${totalNumber}" floated="true">
    //    content
    // </ui:headerWithIcon>

    def h1HeaderWithIcon = { attrs, body ->

        if (attrs.floated && attrs.floated != 'false') {
            out << '<h1 class="ui icon header la-clear-before left floated aligned la-positionRelative">'
        } else {
            out << '<h1 class="ui icon header la-clear-before la-noMargin-top">'
        }

        if (attrs.visibleProviders && attrs.visibleProviders instanceof Collection) {
            attrs.visibleProviders = attrs.visibleProviders.collect{ it.provider.name }.join(' – ')
        }
        if (attrs.visibleVendors && attrs.visibleVendors instanceof Collection) {
            attrs.visibleVendors = attrs.visibleVendors.collect{ it.vendor.name }.join(' – ')
        }
        if ( (attrs.referenceYear) || (attrs.visibleProviders) || (attrs.visibleVendors) ){
            out << '<div class="la-subPlusYear">'
        }
        if (attrs.type) {
            out << ui.headerTitleIcon([type: attrs.type])
        } else {
            out << ui.headerIcon()
        }
        if ( (attrs.referenceYear) || (attrs.visibleProviders) || (attrs.visibleVendors) ) {
            out << '<div class="la-subPlusYear-texts">'
        }
        if (attrs.text) {
            out << attrs.text
        }

        if (attrs.message) {
            SwissKnife.checkMessageKey(attrs.message as String)
            out << "${message(code: attrs.message, args: attrs.args)}"
        }

        if (attrs.total) {
            out << ui.totalNumber([total: attrs.total])
        }
        if ( body ) {
            out << body()
        }
        if ( (attrs.referenceYear)|| (attrs.visibleProviders) ) {
            out << '<span class="la-subPlusYear-year">'
            out << attrs.referenceYear
            if(attrs.visibleProviders?.size() > 0) {
                if (attrs.referenceYear) {
                    out << ' – '
                }
                out << '<span class="la-orgRelations">'
                out << attrs.visibleProviders
                out << '</span>'
            }
            out << '</span>'
            out << '</div>'
            out << '</div>'
        }
        out << '</h1>'
    }

    // <ui:messages data="${flash}" />

    def cardLabelAdminOnly= { attrs, body ->
        String text = message(code: 'default.adminsOnly.label')

        if (attrs.text) {
            text = attrs.text
        }
        if (attrs.message) {
            SwissKnife.checkMessageKey(attrs.message as String)
            text = "${message(code: attrs.message)}"
        }
        out << '  <div class="ui top right attached label">'
        out << '    <i class="' + Icon.AUTH.ROLE_ADMIN + ' orange"></i>'
        out << '    <span class="content">' + text + '</span>'
        out << '  </div>'
    }

    def cardLabelConsortiumOnly = { attrs, body ->
        String text = message(code: 'default.consortiumOnly.label')

        if (attrs.text) {
            text = attrs.text
        }
        if (attrs.message) {
            SwissKnife.checkMessageKey(attrs.message as String)
            text = "${message(code: attrs.message)}"
        }
        out << '  <div class="ui top right attached label">'
        out << '    <i class="' + Icon.AUTH.ORG_CONSORTIUM + ' teal"></i>'
        out << '    <span class="content">' + text + '</span>'
        out << '  </div>'
    }

    def cardLabelDeprecated = { attrs, body ->
        String text = 'DEPRECATED: Wird zukünftig entfernt'

        if (attrs.text) {
            text = attrs.text
        }
        if (attrs.message) {
            SwissKnife.checkMessageKey(attrs.message as String)
            text = "${message(code: attrs.message)}"
        }
        out << '  <div class="ui top right attached label">'
        out << '    <i class="' + Icon.UI.WARNING + ' red"></i>'
        out << '    <span class="content">' + text + '</span>'
        out << '  </div>'
    }

    @FixedFeature_DoNotModify
    def messages = { attrs, body ->

        def flash = attrs.data
        if (flash) {
            if (flash instanceof GrailsFlashScope) {
                if (flash.message) {
                    out << ui.msg(class: 'success', text: flash.message)
                }
                if (flash.error) {
                    out << ui.msg(class: 'error', text: flash.error)
                }
                if (flash.invalidToken) {
                    out << ui.msg(class: 'error', text: flash.invalidToken)
                }
            }
            else {
                log.warn 'INVALID USAGE: <ui:messages/> only accepts GrailsFlashScope: ' + flash.getClass()
            }
        }
    }

    // <ui:msg class="error|info|success|warning" header="${text}" text="${text}" message="18n.token" showIcon="true" hideClose="true" />

    @FixedFeature_DoNotModify
    def msg = { attrs, body ->

        String clss = ''
        String icon = ''

        if (attrs.class) {
            clss = attrs.class.toString()

            if (attrs.showIcon) {
                if (clss.toLowerCase() in ['error', 'info', 'success', 'warning']) {
                    icon = Icon.UI[clss.toUpperCase()]
                }
                else {
                    icon = 'poo icon'
                }
            }
        }
//        println '> ' + clss + ', ' + attrs.showIcon + ' = ' +  icon

        out << '<div class="ui ' + clss + ' message ' + (icon ? 'icon ' : '') + 'la-clear-before">'

        if (! attrs.hideClose) {
            out << '<i aria-hidden="true" class="close icon"></i>'
        }
        if (icon) {
            out << '<i class="' + icon + '"></i>'
            out << '<div class="content">'
        }
        if (attrs.header) {
            out << '<div class="header">' + attrs.header + '</div>'
        }

        out << '<p> ' // only phrasing content allowed

        if (attrs.text) {
            out << attrs.text
        }
        if (attrs.message) {
            SwissKnife.checkMessageKey(attrs.message as String)
            out << message(code: attrs.message, args: attrs.args)
        }
        if (body) {
            String content = body()
            out << content

            boolean phraseCheck = true
            for (String bad : ['</p>, </div>', '<input', '</li>']) {
                if (phraseCheck && content.contains(bad)) {
                    phraseCheck = false
                    break
                }
            }
            if (!phraseCheck) {
                log.warn 'INVALID USAGE: <ui:msg/> only accepts phrasing content -> ' + content
            }
        }
        out << ' </p>'

        if (icon) {
            out << '</div>' //.content
        }
        out << '</div>'
    }

    // <ui:errors bean="${instanceOfObject}" />

    def errors = { attrs, body ->

        if (attrs.bean?.errors?.allErrors) {
            out << '<div class="ui error message">'
            out << '<i aria-hidden="true" class="close icon"></i>'
//            out << '<i class="exclamation triangle icon" aria-hidden="true"></i>'
            out << '<ul class="list">'
            attrs.bean.errors.allErrors.each { e ->
                if (e in org.springframework.validation.FieldError) {
                    out << '<li data-field-id="${error.field}">'
                    out << BeanStore.getMessageSource().getMessage(e, LocaleUtils.getCurrentLocale())
                    out << '</li>'
                } else {
                    out << '<li>' + message(error: "${e}") + '</li>'
                }
            }
            out << '</ul>'
            out << '</div>'
        }
    }

    // <ui:objectStatus object="${obj}" />

    def objectStatus = { attrs, body ->
        String status

        if (attrs.object) {
                 if (attrs.object.hasProperty('status'))            { status = attrs.object.status?.value }
            else if (attrs.object.hasProperty('costItemStatus'))    { status = attrs.object.costItemStatus?.value }
            else if (attrs.object.hasProperty('packageStatus'))     { status = attrs.object.packageStatus?.value }
        }

        if ('deleted'.equalsIgnoreCase(status)) {
            out << '<div class="ui segment inverted red">'
            out << '<p><strong>' + message(code: 'default.object.isDeleted') + '</strong></p>'
            out << '</div>'
        }
        if (attrs.object && attrs.object instanceof Org && attrs.object.isArchived()) {
            out << '<div class="ui segment inverted red">'
            out << '<p><strong>' + message(code: 'default.object.isArchived') + '</strong></p>'
            out << '</div>'
        }
    }

    // <ui:card text="${text}" message="local.string" class="some_css_class">
    // </ui:card>

    def card = { attrs, body ->
        def (text, message) = SwissKnife.getTextAndMessage(attrs)
        String title = (text && message) ? text + " - " + message : text + message

        out << '<div class="ui card' + (attrs.class ? ' ' + attrs.class : '') + '">'
        out << '    <div class="content">'

        if (title) {
            out << '    <div class="ui header la-flexbox la-justifyContent-spaceBetween">'
            out << '        <h2>'+ title + '</h2>'
            if (attrs.editable && attrs.href) {
                out << '        <div class="right aligned four wide column">'
                out << '            <button type="button" class="' + Btn.MODERN.SIMPLE + '" data-ui="modal" data-href="' + attrs.href + '" ><i aria-hidden="true" class="' + Icon.CMD.ADD + '"></i></button>'
                out << '        </div>'
            }
            out << '   </div>'

        }
        out << body()

        out << '    </div>'
        out << '</div>'
    }

    def auditButton = { attrs, body ->

        if (attrs.auditable) {
            try {
                def obj = attrs.auditable[0]
                def objAttr = attrs.auditable[1]
                boolean hasAuditConfig

                if (obj?.getLogIncluded()?.contains(objAttr)) {

                    // inherited (to)
                    if (obj.instanceOf) {
                        if(attrs.auditConfigs)
                            hasAuditConfig = attrs.auditConfigs[objAttr]
                        else hasAuditConfig = auditService.getAuditConfig(obj.instanceOf, objAttr)
                        if (hasAuditConfig) {
                            out << ui.auditIcon(type: 'auto')
                        }
                    }
                    // inherit (from)
                    else if (obj?.showUIShareButton()) {
                        String oid = genericOIDService.getOID(obj)
                        if(attrs.auditConfigs)
                            hasAuditConfig = attrs.auditConfigs[objAttr]
                        else hasAuditConfig = auditService.getAuditConfig(obj, objAttr)

                        if (hasAuditConfig) {
                            if(attrs.withoutOptions) {
                                out << '<a role="button" data-content="Wert wird vererbt" class="ui icon green button la-modern-button ' + attrs.class + ' la-audit-button la-popup-tooltip" href="'
                                out << g.createLink(
                                        controller: 'ajax',
                                        action: 'toggleAudit',
                                        params: ['owner': oid, 'property': [objAttr], keep: true],
                                )
                                out << '">'
                                out << '<i aria-hidden="true" class="' + Icon.SIG.INHERITANCE + '"></i>'
                                out << '</a>'
                            }
                            else {
                                out << '<div class="ui simple dropdown icon green button la-modern-button ' + attrs.class + ' la-audit-button" data-content="Wert wird vererbt">'
                                out   << '<i aria-hidden="true" class="' + Icon.SIG.INHERITANCE + '"></i>'
                                out   << '<div class="menu">'
                                out << g.link( 'Vererbung deaktivieren. Wert für Einrichtung <strong>löschen</strong>',
                                        controller: 'ajax',
                                        action: 'toggleAudit',
                                        params: ['owner': oid, 'property': [objAttr]],
                                        class: 'item'
                                )
                                out << g.link( 'Vererbung deaktivieren. Wert für Einrichtung <strong>erhalten</strong>',
                                        controller: 'ajax',
                                        action: 'toggleAudit',
                                        params: ['owner': oid, 'property': [objAttr], keep: true],
                                        class: 'item'
                                )
                                out   << '</div>'
                                out << '</div>'
                            }
                        }
                        else {
                            out << '<a role="button" data-content="Wert wird nicht vererbt" class="ui icon button la-modern-button ' + attrs.class + ' la-audit-button la-popup-tooltip" href="'
                            out << g.createLink(
                                    controller: 'ajax',
                                    action: 'toggleAudit',
                                    params: ['owner': oid, 'property': [objAttr]],
                            )
                            out << '">'
                            out << '<i aria-hidden="true" class="' + Icon.SIG.INHERITANCE_OFF + '"></i>'
                            out << '</a>'
                        }
                    }
                }

            } catch (Exception e) {
            }
        }
    }

    def auditInfo = { attrs, body ->

        if (attrs.auditable) {
            try {
                def obj = attrs.auditable[0]
                def objAttr = attrs.auditable[1]

                if (obj?.getLogIncluded()?.contains(objAttr)) {

                    // inherited (to)
                    if (obj.instanceOf) {
                        if (auditService.getAuditConfig(obj.instanceOf, objAttr)) {
                            out << ui.auditIcon(type: 'auto')
                        }
                    }
                    // inherit (from)
                    else if (obj?.showUIShareButton()) {
                        if (auditService.getAuditConfig(obj, objAttr)) {
                            out << ui.auditIcon(type: 'default')
                        }
                        else {
                            out << '<span class="la-popup-tooltip" data-content="Wert wird nicht vererbt" data-position="top right">'
                            out << '<i aria-hidden="true" class="' + Icon.SIG.INHERITANCE_OFF + '"></i>'
                            out << '</span>'
                        }
                    }
                }

            } catch (Exception e) {
            }
        }
    }

    // <ui:auditIcon type="default|auto" />

    def auditIcon = { attrs, body ->  // TODO - in progress

        if (! ['default', 'auto'].contains(attrs.type)) {
            out << "[auditIconWithTooltip: missing/faulty attribute 'type']"
        }
        else {
            if (attrs.type == 'default') {
                // Wert wird geerbt
                out << '<span class="la-popup-tooltip" data-content="' + message(code:'property.audit.target.inherit') + '" data-position="top right">'
                out << '<i class="' + Icon.SIG.INHERITANCE + ' grey"></i>'
                out << '</span>'
            }
            else if (attrs.type == 'auto') {
                // Wert wird automatisch geerbt
                out << '<span class="la-popup-tooltip" data-content="' + message(code: 'property.audit.target.inherit.auto') + '" data-position="top right">'
                out << '<i class="' + Icon.SIG.INHERITANCE_AUTO + '"></i>'
                out << '</span>'
            }
        }
    }

    // <ui:archiveIcon org="${org}" />

    def archiveIcon = { attrs, body ->  // TODO - in progress

        if (attrs.org && attrs.org.isArchived()) {
            String ad = formatDate(format: message(code: 'default.date.format.notime'), date: attrs.org.archiveDate)
            out << '<span class="la-popup-tooltip" data-position="top right" data-content="' + message(code:'org.archiveDate.label') + ': ' + ad + '">'
            out << '  <i class="exclamation triangle icon red"></i>'
            out << '</span>'
        }
    }

    //<ui:filter simple="true|false" extended="true|false"> CONTENT </ui:filter>

    def filter = { attrs, body ->

        boolean simpleFilter = attrs.simple?.toLowerCase() == 'true'
        boolean extended = true

        if (! simpleFilter) {

			// overwrite due attribute
            if (attrs.extended) {
                if (attrs.extended.toLowerCase() == 'false') {
                    extended = false
                }
            }
            else {
				// overwrite due session
                SessionCacheWrapper sessionCache = contextService.getSessionCache()
                def cacheEntry = sessionCache.get("${UserSetting.KEYS.SHOW_EXTENDED_FILTER.toString()}/${controllerName}/${actionName}")

                if (cacheEntry) {
                    if (cacheEntry.toLowerCase() == 'false') {
                        extended = false
                    }
                }
				// default profile setting
                else {
                    User currentUser = contextService.getUser()
                    String settingValue = currentUser.getSettingsValue(UserSetting.KEYS.SHOW_EXTENDED_FILTER, RDStore.YN_YES).value

                    if (settingValue.toLowerCase() == 'no') {
                        extended = false
                    }
                }
            }
        }
        // for WCAG
        out << '<section class="la-clearfix" aria-label="filter">'

            if (! simpleFilter) {
                out << '<button aria-expanded="' + (extended ? 'true':'false') + '" class="ui right floated button la-inline-labeled la-js-filterButton la-clearfix' + (extended ? '':' blue') + '">'
                out << '    Filter'
                out << '    <i aria-hidden="true" class="' + Icon.LNK.FILTERED + '"></i>'
                out << '   <span class="ui circular label la-js-filter-total hidden">0</span>'
                out << '</button>'

                out << render(template: '/templates/filter/js', model: [filterAjaxUri: "${controllerName}/${actionName}"])
            }

            out << '<div class="ui la-filter segment la-clear-before"' + (extended ?'':' style="display: none;"') + '>'
            out << body()
            out << '</div>'

        out << '</section>'
    }

    def searchSegment = { attrs, body ->

        String method = attrs.method ?: 'GET'
        String controller = attrs.controller ?: ''
        String action = attrs.action ?: ''

        out << '<div class="ui la-search segment">'
        out << '<form class="ui form" controller="' + controller + '" action="' + action + '" method="' + method + '">'
        out << body()
        out << '</form>'
        out << '</div>'
    }

    def greySegment = { attrs, body ->

        out << '<div class="ui grey segment la-clear-before">'
        out << body()
        out << '</div>'
    }

    def flagDeprecated = { attrs, body ->

        out << ui.msg(class: 'error', showIcon: 'true', text: 'Diese Funktionalität wird demnächst entfernt.<br/>Bitte nicht mehr verwenden und ggfs. Daten migrieren.')
    }

    /**
     * @attr hideWrapper Renders only the &lt;form&gt; without a &lt;div&gt; wrapper
     * @attr action The name of the action to use in the link, if not specified the default action will be linked
     * @attr controller The name of the controller to use in the link, if not specified the current controller will be linked
     * @attr method The form method to use, either 'POST' or 'GET'; defaults to 'POST'
     * @attr autocomplete Defaults to 'off'
     * @attr id The id to use in the link
     * @attr class Additional css classes for the &lt;form&gt;
     * @attr params Additional parameters to use in the link
     */
    def form = { attrs, body ->

        Map<String, Object> formAttrs = [
                controller  : attrs.controller ?: null,
                action      : attrs.action ?: null,
                method      : attrs.method ?: 'POST',
                autocomplete: attrs.autocomplete ?: 'off',
                id          : attrs.id ?: null,
                class       : 'ui form' + (attrs.class ? ' ' + attrs.class : ''),
                params      : attrs.params,
        ]

        (attrs.keySet() as Set<String>).findAll { it.startsWith('data-') }.each { formAttrs.put(it, attrs.getAt(it)) }

        Writer bodyWriter = new StringBuilderWriter()
        bodyWriter << '<input type="hidden" name="' + FormService.FORM_SERVICE_TOKEN + '" value="' + formService.getNewToken() + '"/>'
        bodyWriter << body()

        if ('true'.equalsIgnoreCase(attrs.hideWrapper as String)) {
            out << g.form(formAttrs, bodyWriter.toString())
        }
        else {
            out << '<div class="ui grey segment la-clear-before">'
            out << g.form(formAttrs, bodyWriter.toString())
            out << '</div>'
        }
    }

    //<ui:datepicker class="grid stuff here" type="" label="" bean="${objInstance}" name="fieldname" value="" required="" modifiers="" />

    def datepicker = { attrs, body ->

        String inputCssClass = attrs.inputCssClass ?: ''
        String label = attrs.label ? "${message(code: attrs.label)}" : '&nbsp'
        String name = attrs.name ? "${message(code: attrs.name)}" : ''
        String id = attrs.id ? "${message(code: attrs.id)}" : ''
        String placeholder = attrs.placeholder ? "${message(code: attrs.placeholder)}" : "${message(code: 'default.date.label')}"

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        String value = ''
        try {
            value = attrs.value ? sdf.format(attrs.value) : value
        }
        catch (Exception e) {
            value = attrs.value
        }

        String classes    = attrs.containsKey('required') ? 'field required' : 'field'
        String required   = attrs.containsKey('required') ? 'required=""' : ''
        String mandatoryField   = attrs.containsKey('required') ? "${message(code: 'messageRequiredField')}" :""

        boolean hideLabel = attrs.hideLabel ? false : true

        if (attrs.class) {
            classes += ' ' + attrs.class
        }
        // check for field errors
        if (attrs.bean && g.fieldError([bean: attrs.bean, field: "${name}"])) {
            classes += ' error'
        }

        // reporting -->
        if (attrs.modifiers) {
            String modName = name + '_modifier'
            String modValue = params.get(modName) ?: attrs.defaultModifier
            String modIconClass = Icon.MATH.EQUAL + ' small'

            switch (modValue) {
                case 'less':
                    modIconClass = Icon.MATH.LESS + ' small'
                    break
                case 'greater':
                    modIconClass = Icon.MATH.GREATER + ' small'
                    break
                case 'equals':
                    modIconClass = Icon.MATH.EQUAL + ' small'
                    break
                case 'less-equal':
                    modIconClass = Icon.MATH.LESS_OR_EQUAL + ' small'
                    break
                case 'greater-equal':
                    modIconClass = Icon.MATH.GREATER_OR_EQUAL + ' small'
                    break
            }
            out << '<div class="field la-combi-input-left">'
            out <<   '<label for="dateBeforeVal">&nbsp;</label>'
            out <<   '<div class="ui compact selection dropdown la-not-clearable">'
            out <<     '<input type="hidden" name="' + modName + '" value="' + modValue + '">'
            out <<     '<i class="dropdown icon"></i>'
            out <<      '<div class="text"><i class="' + modIconClass + '"></i></div>'
            out <<      '<div class="menu">'
            out <<        '<div class="item' + ( modValue == 'less' ? ' active' : '' ) + '" data-value="less"><i class="' + modIconClass + '"></i></div>'
            out <<        '<div class="item' + ( modValue == 'greater' ? ' active' : '' ) + '" data-value="greater"><i class="' + modIconClass + '"></i></div>'
            out <<        '<div class="item' + ( modValue == 'equals' ? ' active' : '' ) + '" data-value="equals"><i class="' + modIconClass + '"></i></div>'
            out <<        '<div class="item' + ( modValue == 'less-equal' ? ' active' : '' ) + '" data-value="less-equal"><i class="' + modIconClass + '"></i></div>'
            out <<        '<div class="item' + ( modValue == 'greater-equal' ? ' active' : '' ) + '" data-value="greater-equal"><i class="' +  + modIconClass + '"></i></div>'
            out <<     '</div>'
            out <<   '</div>'
            out << '</div>'
        }

        String modClass = attrs.modifiers ? ' la-combi-input-right' : ''

        out << '<div class="' + classes + modClass +'">'
        if (hideLabel) {
            out << '<label for="' + id + '">' + label + ' ' + mandatoryField + '</label>'
        }
        String type = attrs.type == 'year' ? 'yearpicker' : 'datepicker'
        out <<   '<div class="ui calendar '+type+'">'
        out <<     '<div class="ui input left icon">'
        out <<       '<i aria-hidden="true" class="' + Icon.SYM.DATE + '"></i>'
        out <<       '<input class="' + inputCssClass + '" name="' + name +  '" id="' + id +'" type="text" placeholder="' + placeholder + '" value="' + value + '" ' + required + '>'
        out <<     '</div>'
        out <<   '</div>'

        out << '</div>'
    }

    def anualRings = { attrs, body ->

        String ddf_notime = message(code: 'default.date.format.notime')

        def object = attrs.object
        def prev = attrs.navPrev
        def next = attrs.navNext
        String color = ''
        String tooltip = message(code: 'subscription.details.statusNotSet')
        String startDate = ''
        String endDate = ''
        String dash = ''

        String prevStartDate
        String prevEndDate

        String nextStartDate
        String nextEndDate

        if (object.status) {
            tooltip = object.status.getI10n('value')
            switch (object.status) {
                case 'Current': color = 'la-status-active'
                    break
                case 'Expired': color = 'la-status-inactive'
                    break
                default: color = 'la-status-else'
                    break
            }
        }
        out << '<div class="ui large label la-annual-rings">'

        if (object.hasProperty('startDate') && object.hasProperty('endDate')) {
            if (object.startDate) {
                startDate = g.formatDate(date: object.startDate, format: ddf_notime)
            }
            if (object.endDate) {
                dash = '–'
                endDate = g.formatDate(date: object.endDate, format: ddf_notime)
            }
        }
        else if(object.hasProperty('retirementDate'))
            startDate = g.formatDate(date: object.retirementDate, format: ddf_notime)
        Map params = [:]
        if(attrs.tab)
            params.tab = attrs.tab
        if (prev) {
            if (prev.size() == 1) {
                prev.each { p ->
                    if (attrs.mapping) {
                        params.sub = p.id
                        out << g.link("<i class='arrow left icon'></i>", controller: attrs.controller, action: attrs.action, class: "item", params: params, mapping: attrs.mapping)

                    } else {
                        out << g.link("<i class='arrow left icon'></i>", controller: attrs.controller, action: attrs.action, class: "item", params: params, id: p.id)
                    }
                }
            } else {

                out << '<div class="ui right pointing dropdown"> <i class="arrow left icon"></i> <div class="menu">'

                prev.each { p ->
                    if (p.hasProperty('startDate') && p.hasProperty('endDate')) {
                        if (p.startDate) {
                            prevStartDate = g.formatDate(date: p.startDate, format: ddf_notime)
                        }
                        if (p.endDate) {
                            prevEndDate = g.formatDate(date: p.endDate, format: ddf_notime)
                        }
                    }
                    if (attrs.mapping) {
                        params.sub = p.id
                        out << g.link("<strong>${p instanceof License ? p.reference : p.name}:</strong> " + "${prevStartDate}" + "${dash}" + "${prevEndDate}", controller: attrs.controller, action: attrs.action, class: "item", params: params, mapping: attrs.mapping)
                    } else {
                        out << g.link("<strong>${p instanceof License ? p.reference : p.name}:</strong> " + "${prevStartDate}" + "${dash}" + "${prevEndDate}", controller: attrs.controller, action: attrs.action, class: "item", params: params, id: p.id)
                    }
                }
                out << '</div> </div>'
            }
        } else if (object.hasProperty('retirementDate')){}
        else out << '<i aria-hidden="true" class="arrow left icon disabled"></i>'
        if ( (startDate) || (endDate) ) {
            out << '<span class="la-annual-rings-text">' + startDate + dash + endDate + '</span>'
        }
        out << "<a class='ui ${color} circular tiny label la-popup-tooltip'  data-variation='tiny' data-content='Status: ${tooltip}'>"
        out << '       &nbsp;'
        out << '</a>'

        if (next) {
            if (next.size() == 1) {
                next.each { n ->
                    if (attrs.mapping) {
                        params.sub = n.id
                        out << g.link("<i class='arrow right icon'></i>", controller: attrs.controller, action: attrs.action, class: "item", params: params, mapping: attrs.mapping)

                    } else {
                        out << g.link("<i class='arrow right icon'></i>", controller: attrs.controller, action: attrs.action, class: "item", params: params, id: n.id)
                    }
                }
            } else {
                out << '<div class="ui left pointing dropdown"> <i class="arrow right icon"></i> <div class="menu">'

                next.each { n ->
                    if (n.hasProperty('startDate') && n.hasProperty('endDate')) {
                        if (n.startDate) {
                            nextStartDate = g.formatDate(date: n.startDate, format: ddf_notime)
                        }
                        if (n.endDate) {
                            nextEndDate = g.formatDate(date: n.endDate, format: ddf_notime)
                        }
                    }
                    if (attrs.mapping) {
                        params.sub = n.id
                        out << g.link("<strong>${n instanceof License ? n.reference : n.name}:</strong> " + "${nextStartDate}" + "${dash}" + "${nextEndDate}", controller: attrs.controller, action: attrs.action, class: "item", params: params, mapping: attrs.mapping)
                    } else {
                        out << g.link("<strong>${n instanceof License ? n.reference : n.name}:</strong> " + "${nextStartDate}" + "${dash}" + "${nextEndDate}", controller: attrs.controller, action: attrs.action, class: "item", params: params, id: n.id)
                    }
                }
                out << '</div> </div>'
            }

        } else if (object.hasProperty('retirementDate')){}
        else out << '<i aria-hidden="true" class="arrow right icon disabled"></i>'
        out << '</div>'
    }

/*    def anualRingsModern = { attrs, body ->
        String ddf_notime = message(code: 'default.date.format.notime')

        def object = attrs.object
        def prev = attrs.navPrev
        def next = attrs.navNext
        String color = ''
        String tooltip = message(code: 'subscription.details.statusNotSet')
        String startDate = ''
        String endDate = ''
        String dash = ''

        String prevStartDate
        String prevEndDate

        String nextStartDate
        String nextEndDate

        if (object.status) {
            tooltip = object.status.getI10n('value')
            switch (object.status) {
                case 'Current': color = 'la-status-active'
                    break
                case 'Expired': color = 'la-status-inactive'
                    break
                default: color = 'la-status-else'
                    break
            }
        }
        out << '<div class="ui large label la-annual-rings-modern">'

        if (object.startDate) {
            startDate = g.formatDate(date: object.startDate, format: ddf_notime)
        }
        if (object.endDate) {
            dash = '–'
            endDate = g.formatDate(date: object.endDate, format: ddf_notime)
        }
        if (prev) {
            if (prev.size() == 1) {
                prev.each { p ->
                    if (attrs.mapping) {
                        out << g.link("<i class='arrow left icon'></i>", controller: attrs.controller, action: attrs.action, class: "item", params: [sub: p.id], mapping: attrs.mapping)

                    } else {
                        out << g.link("<i class='arrow left icon'></i>", controller: attrs.controller, action: attrs.action, class: "item", id: p.id)
                    }
                }
            } else {
                out << '<div class="ui right pointing dropdown"> <i class="arrow left icon"></i> <div class="menu">'

                prev.each { p ->
                    if (p.startDate) {
                        prevStartDate = g.formatDate(date: p.startDate, format: ddf_notime)
                    }
                    if (p.endDate) {
                        prevEndDate = g.formatDate(date: p.endDate, format: ddf_notime)
                    }
                    if (attrs.mapping) {
                        out << g.link("<strong>${p instanceof License ? p.reference : p.name}:</strong> " + "${prevStartDate}" + "${dash}" + "${prevEndDate}", controller: attrs.controller, action: attrs.action, class: "item", params: [sub: p.id], mapping: attrs.mapping)
                    } else {
                        out << g.link("<strong>${p instanceof License ? p.reference : p.name}:</strong> " + "${prevStartDate}" + "${dash}" + "${prevEndDate}", controller: attrs.controller, action: attrs.action, class: "item", id: p.id)
                    }
                }
                out << '</div> </div>'
            }
        } else {
            out << '<i aria-hidden="true" class="arrow left icon disabled"></i>'
        }
        out << '<span class="la-annual-rings-text">' + startDate + dash + endDate + '</span>'

        out << "<a class='ui ${color} circular tiny label la-popup-tooltip'  data-variation='tiny' data-content='Status: ${tooltip}'>"
        out << '       &nbsp;'
        out << '</a>'

        if (next) {
            if (next.size() == 1) {
                next.each { n ->
                    if (attrs.mapping) {
                        out << g.link("<i class='arrow right icon'></i>", controller: attrs.controller, action: attrs.action, class: "item", params: [sub: n.id], mapping: attrs.mapping)

                    } else {
                        out << g.link("<i class='arrow right icon'></i>", controller: attrs.controller, action: attrs.action, class: "item", id: n.id)
                    }
                }
            } else {
                out << '<div class="ui left pointing dropdown"> <i class="arrow right icon"></i> <div class="menu">'

                next.each { n ->
                    if (n.startDate) {
                        nextStartDate = g.formatDate(date: n.startDate, format: ddf_notime)
                    }
                    if (n.endDate) {
                        nextEndDate = g.formatDate(date: n.endDate, format: ddf_notime)
                    }
                    if (attrs.mapping) {
                        out << g.link("<strong>${n instanceof License ? n.reference : n.name}:</strong> " + "${nextStartDate}" + "${dash}" + "${nextEndDate}", controller: attrs.controller, action: attrs.action, class: "item", params: [sub: n.id], mapping: attrs.mapping)
                    } else {
                        out << g.link("<strong>${n instanceof License ? n.reference : n.name}:</strong> " + "${nextStartDate}" + "${dash}" + "${nextEndDate}", controller: attrs.controller, action: attrs.action, class: "item", id: n.id)
                    }
                }
                out << '</div> </div>'
            }
        } else {
            out << '<i aria-hidden="true" class="arrow right icon disabled"></i>'
        }
        out << '</div>'
    }*/

    def totalNumber = { attrs, body ->
        def total = attrs.total ?: 0
        def newClass = attrs.class ?: ''

        out << '<span class="ui circular ' + newClass + ' label">' + total + '</span>'
    }

    def dateDevider = { attrs, body ->

        out << '<span class="ui grey horizontal divider la-date-devider">' + message(code:'default.to') + '</span>'
    }

    def tabs = { attrs, body ->
        out << '<div class="ui top attached tabular stackable menu">'
        out << body()
        out << '</div>'
    }

    def tabsItem = { attrs, body ->

        def (text, message) = SwissKnife.getTextAndMessage(attrs)
        String linkBody = (text && message) ? text + " - " + message : text + message
        String cssClass = ((this.pageScope.variables?.actionName == attrs.action && (attrs.tab == params.tab || attrs.tab == params.subTab)) ? 'item active' : 'item') + (attrs.class ? ' ' + attrs.class : '')

        String counts = (attrs.counts >= 0) ? '<span class="ui circular label">' + attrs.counts + '</span>' : null

        linkBody = counts ? linkBody + counts : linkBody

        if (attrs.controller) {
            out << g.link(linkBody,
                    class: cssClass,
                    controller: attrs.controller,
                    action: attrs.action,
                    params: attrs.params
            )
        } else {
            out << linkBody
        }
    }

    @Deprecated
    Closure sortableColumn = { attrs, body ->
        def writer = out
        if (!attrs.property) {
            throwTagError("Tag [sortableColumn] is missing required attribute [property]")
        }

        if (!attrs.title && !attrs.titleKey) {
            throwTagError("Tag [sortableColumn] is missing required attribute [title] or [titleKey]")
        }

        def property = attrs.remove("property")
        def action = attrs.action ? attrs.remove("action") : (actionName ?: "list")
        def namespace = attrs.namespace ? attrs.remove("namespace") : ""

        def defaultOrder = attrs.remove("defaultOrder")
        if (defaultOrder != "desc") defaultOrder = "asc"

        // current sorting property and order
        def sort = params.sort
        def order = params.order

        // add sorting property and params to link params
        Map linkParams = [:]
        if (params.id) linkParams.put("id", params.id)
        def paramsAttr = attrs.remove("params")
        if (paramsAttr instanceof Map) linkParams.putAll(paramsAttr)
        linkParams.sort = property

        // propagate "max" and "offset" standard params
        if (params.max) linkParams.max = params.max
        if (params.offset) linkParams.offset = params.offset

        // determine and add sorting order for this column to link params
        attrs['class'] = (attrs['class'] ? "${attrs['class']} " : "")
        if (property == sort) {
            if (order == "asc") {
                linkParams.order = "desc"
                attrs['class'] = (attrs['class'] as String) + " sorted ascending "
            }
            else {
                linkParams.order = "asc"
                attrs['class'] = (attrs['class'] as String) + " sorted descending "
            }
        }
        else {
            linkParams.order = defaultOrder
            attrs['class'] = (attrs['class'] as String) + " sortable "
        }

        // determine column title
        String title = attrs.remove("title") as String
        String titleKey = attrs.remove("titleKey") as String
        Object mapping = attrs.remove('mapping')
        if (titleKey) {
            if (!title) title = titleKey
            MessageSource messageSource = BeanStore.getMessageSource()
            Locale locale = RequestContextUtils.getLocale(request)
            title = messageSource.getMessage(titleKey, null, title, locale)
        }

        writer << "<th "
        // process remaining attributes
        Encoder htmlEncoder = codecLookup.lookupEncoder('HTML')
        attrs.each { k, v ->
            writer << k
            writer << "=\""
            writer << htmlEncoder.encode(v)
            writer << "\" "
        }
        writer << '>'
        Map linkAttrs = [:]
        linkAttrs.params = linkParams
        if (mapping) {
            linkAttrs.mapping = mapping
        }

        linkAttrs.action = action
        linkAttrs.namespace = namespace

        writer << _callLink((Map)linkAttrs) {
            title
        }

        if(body)
        {
            writer << body()
        }

        writer << '</th>'
    }

    private Object _callLink(Map attrs, Object body) {
        TagOutput.captureTagOutput(gspTagLibraryLookup, 'g', 'link', attrs, body, OutputContextLookupHelper.lookupOutputContext())
    }

    def showPropertyValue = { attrs, body ->
        def property = attrs.property

        if(property instanceof Date) {
            out << g.formatDate(date: property, format: message(code: 'default.date.format.notime'))
        }
        else if(property instanceof RefdataValue) {
            out << property?.getI10n('value')
        }
        else if(property instanceof Boolean) {
            out << (property ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
        }else {
            out << (property ?: '')
        }
    }

    // <ui:select optionValue="field" />  ==> <ui:select optionValue="field_(de|en|fr)" />

    def select = { attrs, body ->
        attrs.optionValue = attrs.optionValue + "_" + LocaleUtils.getCurrentLang()
        out << g.select(attrs)
    }

    def statsLink = {attrs, body ->
        if (attrs.module) {
            attrs.base = attrs.base ? attrs.base+"/${attrs.module}" : "/${attrs.module}"
            attrs.remove('module')
        }
        if (!attrs.params.packages){
            attrs.params.remove('packages')
        }
        String cleanLink = g.link(attrs, body)
        out << cleanLink.replaceAll("(?<!(http:|https:))[//]+", "/")
    }

    def showMoreCloseButton = { attrs, body ->
        out << '<button class="' + Btn.SIMPLE +' la-js-closeAll-showMore right floated">'
        out << message(code: "accordion.button.closeAll")
        out << '</button>'
    }

    // <ui:bubble count="${list.size()}" grey="true" float="true" />

    def bubble = { attrs, body ->
        String color = attrs.grey ? '' : ' blue'

        if (attrs.float) {
            out << '<span class="ui circular label floating' + color + '">'
            out << (attrs.count ?: '0')
            out << '</span>'
        }
        else {
            out << '<span class="ui circular label' + color + '">'
            out << (attrs.count ?: '0')
            out << '</span>'
        }
    }

    def costSign = { attrs, body ->
        if(attrs.ci) {
            de.laser.finance.CostItem ci = attrs.ci
            switch(ci.costItemElementConfiguration) {
                case RDStore.CIEC_POSITIVE: out << '<span class="la-popup-tooltip" data-position="right center" data-content="'+message(code:'financials.costItemConfiguration.positive')+'"><i class="'+Icon.FNC.COST_POSITIVE+'"></i></span>'
                    break
                case RDStore.CIEC_NEGATIVE: out << '<span class="la-popup-tooltip" data-position="right center" data-content="'+message(code:'financials.costItemConfiguration.negative')+'"><i class="'+Icon.FNC.COST_NEGATIVE+'"></i></span>'
                    break
                case RDStore.CIEC_NEUTRAL: out << '<span class="la-popup-tooltip" data-position="right center" data-content="'+message(code:'financials.costItemConfiguration.neutral')+'"><i class="'+Icon.FNC.COST_NEUTRAL+'"></i></span>'
                    break
                default: out << '<span class="la-popup-tooltip" data-position="right center" data-content="'+ message(code:'financials.costItemConfiguration.notSet') +'"><i class="'+Icon.FNC.COST_NOT_SET+'"></i></span>'
                    break
            }
        }
    }

    // moved from Package.getPackageSize()
    def pkgSize = { attrs, body ->

        if (attrs.pkg) {
            de.laser.wekb.Package pkg = attrs.pkg as de.laser.wekb.Package
            def c1 = de.laser.wekb.TitleInstancePackagePlatform.executeQuery('select count(*) from TitleInstancePackagePlatform tipp where tipp.pkg = :ctx and tipp.status = :current', [ctx: pkg, current:RDStore.TIPP_STATUS_CURRENT])[0]
            out << '('
            out << '<span data-tooltip="Titel im Paket"><i class="' + Icon.TIPP + '"></i> ' + c1 + '</span>'
            out << ')'
        }
        else {
            out << '[ ERROR @ ui:pkgSize ]'
        }
    }

    // moved from SubscriptionPackage.getIEandPackageSize() // TODO: icons
    def ieAndPkgSize = { attrs, body ->

        if (attrs.sp) {
            SubscriptionPackage sp = attrs.sp as SubscriptionPackage
            def c1 = SubscriptionPackage.executeQuery('select count(*) from IssueEntitlement ie where ie.subscription = :sub and ie.tipp.pkg = :pkg and ie.status = :current', [sub: sp.subscription, pkg: sp.pkg, current: RDStore.TIPP_STATUS_CURRENT])[0]
            def c2 = SubscriptionPackage.executeQuery('select count(*) from TitleInstancePackagePlatform tipp join tipp.pkg pkg where pkg = :ctx and tipp.status = :current', [ctx: sp.pkg, current:RDStore.TIPP_STATUS_CURRENT])[0]

            out << '('
            out << '<span data-tooltip="Titel in der Lizenz"><i class="icon archive"></i> ' + c1 + '</span>'
            out << ' / '
            out << '<span data-tooltip="Titel im Paket"><i class="' + Icon.TIPP + '"></i> ' + c2 + '</span>'
            out << ')'
        }
        else {
            out << '[ ERROR @ ui:ieAndPkgSize ]'
        }
    }
}
