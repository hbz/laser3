<%@ page import="de.laser.PendingChangeConfiguration; de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
<laser:serviceInjection/>

<g:set var="user" value="${contextService.getUser()}"/>
<g:set var="org" value="${contextService.getOrg()}"/>

<ui:actionsDropdown>
%{--    <g:if test="${(editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')) && ! ['list'].contains(actionName)}">
        <ui:actionsDropdownItem message="task.create.new" data-semui="modal" href="#modalCreateTask" />
        <ui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument" />
    </g:if>
    <g:if test="${accessService.checkMinUserOrgRole(user,org,'INST_EDITOR') && ! ['list'].contains(actionName)}">
        <ui:actionsDropdownItem message="template.addNote" data-semui="modal" href="#modalCreateNote" />
    </g:if>
    <g:if test="${(editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')) && ! ['list'].contains(actionName)}">
        <div class="divider"></div>
    </g:if>--}%

    <g:if test="${(editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM', 'INST_EDITOR')) && !['list'].contains(actionName) && packageInstance}">
        <ui:actionsDropdownItem message="package.show.linkToSub" data-semui="modal" href="#linkToSubModal"/>
        <div class="divider"></div>
    </g:if>

    <ui:actionsDropdownItemDisabled controller="package" action="compare" message="menu.public.comp_pkg"/>

</ui:actionsDropdown>

<g:if test="${(editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM', 'INST_EDITOR')) && !['list'].contains(actionName) && packageInstance}">
    <ui:modal id="linkToSubModal" contentClass="scrolling" message="package.show.linkToSub" msgSave="${message(code: 'default.button.link.label')}">

        <g:form class="ui form" url="[controller: 'package', action: 'processLinkToSub', id: params.id]">

            <div class="field">
                <label>${message(code: 'filter.status')}</label>
                <ui:select class="ui dropdown" name="status" id="status"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}"
                              optionKey="id"
                              optionValue="value"
                              multiple="true"
                              value="${RDStore.SUBSCRIPTION_CURRENT.id}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"
                              onchange="JSPC.app.adjustDropdown()"/>
            </div>
            <br id="element-vor-target-dropdown"/>
            <br>
            <br>
            <br>

            <div class="grouped required fields">
                <label for="With">${message(code: 'subscription.details.linkPackage.label')}</label>

                <div class="field">
                    <div class="ui radio checkbox">
                        <input type="radio" name="addType" id="With" value="With" tabindex="0" class="hidden">
                        <label>${message(code: 'subscription.details.link.with_ents')}</label>
                    </div>
                </div>

                <div class="field">
                    <div class="ui radio checkbox">
                        <input type="radio" name="addType" id="Without" value="Without" tabindex="0" class="hidden">
                        <label>${message(code: 'subscription.details.link.no_ents')}</label>
                    </div>
                </div>
            </div>

            <br>
            <br>

            <div class="field">
                <h3 class="ui dividing header">
                    <g:message code="subscription.packages.config.label" args="${[packageInstance.name]}"/>
                </h3>

                <table class="ui table compact">
                    <tr>
                        <th class="control-label"><g:message code="subscription.packages.changeType.label"/></th>
                        <th class="control-label">
                            <g:message code="subscription.packages.setting.label"/>
                        </th>
                        <th class="control-label la-popup-tooltip la-delay"
                            data-content="${message(code: "subscription.packages.notification.label")}">
                            <i class="ui large icon bullhorn"></i>
                        </th>
                        <g:if test="${contextCustomerType == 'ORG_CONSORTIUM'}">
                            <th class="control-label la-popup-tooltip la-delay"
                                data-content="${message(code: 'subscription.packages.auditable')}">
                                <i class="ui large icon thumbtack"></i>
                            </th>
                        </g:if>
                    </tr>
                    <g:set var="excludes"
                           value="${[PendingChangeConfiguration.PACKAGE_PROP,
                                     PendingChangeConfiguration.PACKAGE_DELETED,
                                     PendingChangeConfiguration.NEW_PRICE,
                                     PendingChangeConfiguration.PRICE_UPDATED,
                                     PendingChangeConfiguration.PRICE_DELETED]}"/>
                    <g:each in="${PendingChangeConfiguration.SETTING_KEYS}" var="settingKey">
                        <tr>
                            <td class="control-label">
                                <g:message code="subscription.packages.${settingKey}"/>
                            </td>
                            <td>
                                <g:if test="${!(settingKey in excludes)}">
                                    <g:if test="${true}">
                                        <ui:select class="ui dropdown"
                                                      name="${settingKey}!§!setting"
                                                      from="${RefdataCategory.getAllRefdataValues(RDConstants.PENDING_CHANGE_CONFIG_SETTING)}"
                                                      optionKey="id" optionValue="value"
                                                      value="${RDStore.PENDING_CHANGE_CONFIG_PROMPT.id}"/>
                                    </g:if>
                                    <g:else>
                                        ${RDStore.PENDING_CHANGE_CONFIG_PROMPT.getI10n("value")}
                                    </g:else>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${true}">
                                    <g:checkBox class="ui checkbox" name="${settingKey}!§!notification"
                                                checked="${false}"/>
                                </g:if>
                                <g:else>
                                    ${RDStore.YN_NO.getI10n("value")}
                                </g:else>
                            </td>
                            <g:if test="${contextCustomerType == 'ORG_CONSORTIUM'}">
                                <td>
                                    <g:if test="${!(settingKey in excludes)}">
                                        <g:if test="${true}">
                                            <g:checkBox class="ui checkbox" name="${settingKey}!§!auditable"
                                                        checked="${false}"/>
                                        </g:if>
                                        <g:else>
                                            ${RDStore.YN_NO.getI10n("value")}
                                        </g:else>
                                    </g:if>
                                </td>
                            </g:if>
                        </tr>
                    </g:each>
                </table>
            </div>
        </g:form>

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.app.adjustDropdown = function () {

                var showSubscriber = $("input[name='show.subscriber'").prop('checked');
                var showConnectedObjs = $("input[name='show.connectedObjects'").prop('checked');
                var url = '<g:createLink controller="ajaxJson"
                                         action="adjustSubscriptionList"/>'

                url = url + '?valueAsOID=true&showSubscriber=' + showSubscriber + '&showConnectedObjs=' + showConnectedObjs

                var status = $("select#status").serialize()
                if (status) {
                    url = url + '&' + status
                }

                $.ajax({
                    url: url,
                    success: function (data) {
                        var select = '';
                        for (var index = 0; index < data.length; index++) {
                            var option = data[index];
                            var optionText = option.text;
                            var optionValue = option.value;
                            var count = index + 1
                            // console.log(optionValue +'-'+optionText)

                            select += '<div class="item" data-value="' + optionValue + '">'+ count + ': ' + optionText + '</div>';
                        }

                        select = '<label>${message(code: 'default.select2.label', args: [message(code: "subscription.label")])}:</label> <div class="ui fluid search selection dropdown la-filterProp">' +
                '   <input type="hidden" id="targetObjectId" name="targetObjectId">' +
                '   <i class="dropdown icon"></i>' +
                '   <div class="default text">${message(code: 'default.select.choose.label')}</div>' +
                '   <div class="menu">'
                + select +
                '</div>' +
                '</div>';

                        $('#element-vor-target-dropdown').next().replaceWith(select);

                        $('.la-filterProp').dropdown({
                            duration: 150,
                            transition: 'fade',
                            clearable: true,
                            forceSelection: false,
                            selectOnKeydown: false,
                            onChange: function (value, text, $selectedItem) {
                                value !== '' ? $(this).addClass("la-filter-selected") : $(this).removeClass("la-filter-selected");
                            }
                        });
                    }, async: false
                });
            }

            JSPC.app.adjustDropdown()
        </laser:script>

    </ui:modal>
</g:if>

%{--
<g:if test="${(editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')) && ! ['list'].contains(actionName)}">
    <laser:render template="/templates/documents/modal" model="${[ownobj: packageInstance, institution: contextService.getOrg(), owntp: 'pkg']}"/>
    <laser:render template="/templates/tasks/modal_create" model="${[ownobj:packageInstance, owntp:'pkg']}"/>
</g:if>
<g:if test="${accessService.checkMinUserOrgRole(user,org,'INST_EDITOR') && ! ['list'].contains(actionName)}">
    <laser:render template="/templates/notes/modal_create" model="${[ownobj: packageInstance, owntp: 'pkg']}"/>
</g:if>--}%
