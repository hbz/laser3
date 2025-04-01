<%@ page import="de.laser.ui.Icon; de.laser.OrgSetting; de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.Org; de.laser.auth.Role; de.laser.storage.RDStore; de.laser.storage.RDConstants" %>
<%@ page import="de.laser.CustomerTypeService; grails.plugin.springsecurity.SpringSecurityUtils" %>

<laser:htmlStart message="org.nav.dataTransfer" />

        <laser:render template="breadcrumb"
              model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView]}"/>

        <ui:h1HeaderWithIcon text="${orgInstance.name}" type="${orgInstance.getCustomerType()}">
            <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
        </ui:h1HeaderWithIcon>

        <ui:controlButtons>
            <laser:render template="actions" />
        </ui:controlButtons>

        <ui:objectStatus object="${orgInstance}" />

        <laser:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

        <ui:messages data="${flash}" />

        <ui:tabs actionName="settings">
            <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') || contextService.getOrg().isCustomerType_Consortium()}">
                <ui:tabsItem controller="org" action="settings" params="[id: orgInstance.id, tab: 'mail']" tab="mail" text="${message(code: 'org.setting.tab.mail')}"/>
            </g:if>
            <%--<ui:tabsItem controller="org" action="settings" params="[id: orgInstance.id, tab: 'general']" tab="general" text="${message(code: 'org.setting.tab.general')}"/>--%>
            <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') || (OrgSetting.get(contextService.getOrg(), OrgSetting.KEYS.API_LEVEL) != OrgSetting.SETTING_NOT_FOUND && OrgSetting.get(contextService.getOrg(), OrgSetting.KEYS.API_LEVEL).getValue() in ['API_LEVEL_READ', 'API_LEVEL_INVOICETOOL', 'API_LEVEL_EZB', 'API_LEVEL_OAMONITOR']) || contextService._hasPerm('FAKE')}">
                <ui:tabsItem controller="org" action="settings" params="[id: orgInstance.id, tab: 'api']" tab="api" text="${message(code: 'org.setting.tab.api')}"/>
            </g:if>
            <%-- deactivated U.F.N. as of [ticket=5385], November 8th, 2023 --%>
            <%--<ui:tabsItem controller="org" action="settings" params="[id: orgInstance.id, tab: 'ezb']" tab="ezb" text="${message(code: 'org.setting.tab.ezb')}"/>--%>
            <ui:tabsItem controller="org" action="settings" params="[id: orgInstance.id, tab: 'natstat']" tab="natstat" text="${message(code: 'org.setting.tab.natstat')}"/>
            <g:if test="${orgInstance.isCustomerType_Pro()}">
                <ui:tabsItem controller="org" action="settings" params="[id: orgInstance.id, tab: 'oamonitor']" tab="oamonitor" text="${message(code: 'org.setting.tab.oamonitor')}"/>
            </g:if>
        </ui:tabs>

        <div class="ui bottom attached tab active segment">
                <div class="la-inline-lists">

                    <g:if test="${params.tab != 'general'}">
                    <div class="ui la-dl-no-table">
                        <div class="content">

                            <table class="ui la-js-responsive-table la-table table">
                                <thead>
                                <tr>
                                    <th>Merkmal</th>
                                    <th>${message(code:'default.value.label')}</th>
                                </tr>
                                </thead>
                                <tbody>
                                <%-- Extra Call from editable cause valiation needed only in Case of Selection "Ja" --%>
                                <laser:script file="${this.getGroovyPageFileName()}">

                                    $('body #natstat_server_access').editable('destroy').editable({
                                        tpl: '<select class="ui dropdown clearable"></select>'
                                    }).on('shown', function() {
                                        r2d2.initDynamicUiStuff('body');
                                        $(".table").trigger('reflow');
                                        $('.ui.dropdown').dropdown({
                                            clearable: true
                                        });
                                    }).on('hidden', function() {
                                        $(".table").trigger('reflow')
                                    });

                                    $('body #oamonitor_server_access').editable('destroy').editable({
                                        tpl: '<select class="ui dropdown clearable"></select>'
                                    }).on('shown', function() {
                                        r2d2.initDynamicUiStuff('body');
                                        $(".table").trigger('reflow');
                                        $('.ui.dropdown').dropdown({
                                            clearable: true
                                        });
                                    }).on('hidden', function() {
                                        $(".table").trigger('reflow')
                                    });

                                    $('body #ezb_server_access').editable('destroy').editable({
                                        tpl: '<select class="ui dropdown clearable"></select>'
                                    }).on('shown', function() {
                                        r2d2.initDynamicUiStuff('body');
                                        $(".table").trigger('reflow');
                                        $('.ui.dropdown').dropdown({
                                            clearable: true
                                        });
                                    }).on('hidden', function() {
                                        $(".table").trigger('reflow')
                                    });
                                </laser:script>

                                <g:each in="${settings}" var="os">
                                    <tr>
                                        <td>
                                            ${message(code:"org.setting.${os.key}", default: "${os.key}")}
                                            <g:if test="${OrgSetting.KEYS.NATSTAT_SERVER_ACCESS == os.key}">
                                                <span class="la-popup-tooltip" data-content="${message(code:'org.setting.NATSTAT_SERVER_ACCESS.tooltip')}">
                                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                                </span>
                                            </g:if>
                                            <g:elseif test="${OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS == os.key}">
                                                <span class="la-popup-tooltip" data-content="${message(code:'org.setting.OAMONITOR_SERVER_ACCESS.tooltip')}">
                                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                                </span>
                                            </g:elseif>
                                            <g:elseif test="${OrgSetting.KEYS.EZB_SERVER_ACCESS == os.key}">
                                                <span class="la-popup-tooltip" data-content="${message(code:'org.setting.EZB.tooltip')}">
                                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                                </span>
                                            </g:elseif>
                                        </td>
                                        <td>
                                            <g:if test="${editable && os.key in OrgSetting.getEditableSettings()}">
                                                <g:if test="${OrgSetting.KEYS.NATSTAT_SERVER_ACCESS == os.key}">
                                                    <ui:xEditableRefData owner="${os}"
                                                                            field="rdValue"
                                                                            id="natstat_server_access"
                                                                            data_confirm_tokenMsg="${message(code: 'org.setting.NATSTAT_SERVER_ACCESS.confirm')}"
                                                                            data_confirm_term_how="ok"
                                                                            class="js-open-confirm-modal-xEditableRefData"
                                                                            data_confirm_value="${RefdataValue.class.name}:${RDStore.YN_YES.id}"
                                                                            config="${os.key.rdc}" />
                                                </g:if>
                                                <g:elseif test="${OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS == os.key}">
                                                    <ui:xEditableRefData owner="${os}"
                                                                            field="rdValue"
                                                                            id="oamonitor_server_access"
                                                                            data_confirm_tokenMsg="${message(code: 'org.setting.OAMONITOR_SERVER_ACCESS.confirm')}"
                                                                            data_confirm_term_how="ok"
                                                                            class="js-open-confirm-modal-xEditableRefData"
                                                                            data_confirm_value="${RefdataValue.class.name}:${RDStore.YN_YES.id}"
                                                                            config="${os.key.rdc}" />
                                                </g:elseif>
                                                <g:elseif test="${OrgSetting.KEYS.EZB_SERVER_ACCESS == os.key}">
                                                    <ui:xEditableRefData owner="${os}"
                                                                            field="rdValue"
                                                                            id="ezb_server_access"
                                                                            data_confirm_tokenMsg="${message(code: 'org.setting.EZB.confirm')}"
                                                                            data_confirm_term_how="ok"
                                                                            class="js-open-confirm-modal-xEditableRefData"
                                                                            data_confirm_value="${RefdataValue.class.name}:${RDStore.YN_YES.id}"
                                                                            config="${os.key.rdc}" />
                                                </g:elseif>
                                                <g:elseif test="${OrgSetting.KEYS.MAIL_REPLYTO_FOR_SURVEY == os.key}">
                                                    <ui:xEditable owner="${os}" field="strValue" validation="email"/>
                                                </g:elseif>
                                                <g:elseif test="${OrgSetting.KEYS.MAIL_SURVEY_FINISH_RESULT == os.key}">
                                                    <ui:xEditable owner="${os}" field="strValue" validation="email"/>
                                                </g:elseif>
                                                <g:elseif test="${OrgSetting.KEYS.MAIL_SURVEY_FINISH_RESULT_ONLY_BY_MANDATORY == os.key}">
                                                    <ui:xEditableRefData owner="${os}" field="rdValue" config="${os.key.rdc}" />
                                                </g:elseif>
                                                <g:elseif test="${os.key.type == RefdataValue}">
                                                    <ui:xEditableRefData owner="${os}" field="rdValue" config="${os.key.rdc}" />
                                                </g:elseif>
                                                <g:elseif test="${os.key.type == Role}">
                                                    ${os.getValue()?.getI10n('authority')} (Editierfunktion deaktiviert) <%-- TODO --%>
                                                </g:elseif>
                                                <g:else>
                                                    <ui:xEditable owner="${os}" field="strValue" />
                                                </g:else>

                                            </g:if>
                                            <g:else>

                                                <g:if test="${os.key.type == RefdataValue}">
                                                    ${os.getValue()?.getI10n('value')}
                                                </g:if>
                                                <g:elseif test="${os.key.type == Role}">
                                                    ${os.getValue()?.getI10n('authority')}
                                                </g:elseif>
                                                <g:else>
                                                    ${os.getValue()}
                                                </g:else>

                                            </g:else>
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                        </table>
                        </div><!-- .content -->
                    </div>
                    </g:if>


                </div><!-- .la-inline-lists -->

        </div><!-- .grid -->

<laser:htmlEnd />
