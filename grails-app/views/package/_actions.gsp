<%@ page import="de.laser.CustomerTypeService; de.laser.PendingChangeConfiguration; de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
<laser:serviceInjection/>

<g:set var="user" value="${contextService.getUser()}"/>
<g:set var="org" value="${contextService.getOrg()}"/>

<ui:actionsDropdown>
%{--    <g:if test="${(editable || contextService.hasAffiliation(CustomerTypeService.PERMS_ORG_PRO_CONSORTIUM_BASIC, 'INST_EDITOR')) && ! ['list'].contains(actionName)}">
        <ui:actionsDropdownItem message="task.create.new" data-ui="modal" href="#modalCreateTask" />
        <ui:actionsDropdownItem message="template.documents.add" data-ui="modal" href="#modalCreateDocument" />
    </g:if>
    <g:if test="${userService.checkAffiliationAndCtxOrg(user,org,'INST_EDITOR') && ! ['list'].contains(actionName)}">
        <ui:actionsDropdownItem message="template.addNote" data-ui="modal" href="#modalCreateNote" />
    </g:if>
    <g:if test="${(editable || contextService.hasAffiliation(CustomerTypeService.PERMS_ORG_PRO_CONSORTIUM_BASIC, 'INST_EDITOR')) && ! ['list'].contains(actionName)}">
        <div class="divider"></div>
    </g:if>--}%

    <g:if test="${(editable || contextService.hasAffiliation(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC, 'INST_EDITOR')) && !['list'].contains(actionName) && packageInstance}">
        <ui:actionsDropdownItem message="package.show.linkToSub" data-ui="modal" href="#linkToSubModal"/>
    </g:if>

%{--    <ui:actionsDropdownItemDisabled controller="package" action="compare" message="menu.public.comp_pkg"/>--}%

</ui:actionsDropdown>

<g:if test="${(editable || contextService.hasAffiliation(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC, 'INST_EDITOR')) && !['list'].contains(actionName) && packageInstance}">
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
            <div id="dropdownWrapper"></div>
            <br/>
            <br/>
            <br/>
            <div class="field holdingSelection">
                <label for="holdingSelection">${message(code: 'subscription.holdingSelection.label')} <span class="la-long-tooltip la-popup-tooltip la-delay" data-content="${message(code: "subscription.holdingSelection.explanation")}"><i class="question circle icon la-popup"></i></span></label>
            </div>
            <div class="two fields holdingSelection">
                <div class="field">
                    <ui:select class="ui dropdown search selection" id="holdingSelection" name="holdingSelection" from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_HOLDING)}" optionKey="id" optionValue="value"/>
                </div>
                <g:if test="${org.isCustomerType_Consortium()}">
                    <g:hiddenField name="subOID" value="null"/>
                    <div class="field">
                        <button id="inheritHoldingSelection" data-content="${message(code: 'subscription.holdingSelection.inherit')}" class="ui icon blue button la-modern-button la-audit-button la-popup-tooltip la-delay" data-inherited="false">
                            <i aria-hidden="true" class="icon la-js-editmode-icon la-thumbtack slash"></i>
                        </button>
                    </div>
                </g:if>
            </div>
        </g:form>
            <%--
            <br/>
            <br/>
            <br/>

            <div class="grouped required fields">
                <label for="With">${message(code: 'subscription.details.linkPackage.label')}</label>

                <div class="field">
                    <div class="ui radio checkbox">
                        <input type="radio" name="addType" id="With" value="With" tabindex="0" class="hidden">
                        <label for="With">${message(code: 'subscription.details.link.with_ents')}</label>
                    </div>
                </div>

                <div class="field">
                    <div class="ui radio checkbox">
                        <input type="radio" name="addType" id="Without" value="Without" tabindex="0" class="hidden">
                        <label for="Without">${message(code: 'subscription.details.link.no_ents')}</label>
                    </div>
                </div>
            </div>

            <br/>
            <br/>

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
                        <g:if test="${customerTypeService.isConsortium( contextCustomerType )}">
                            <th class="control-label la-popup-tooltip la-delay"
                                data-content="${message(code: 'subscription.packages.auditable')}">
                                <i class="ui large icon thumbtack"></i>
                            </th>
                        </g:if>
                    </tr>
                    <g:set var="excludes"
                           value="${[PendingChangeConfiguration.PACKAGE_PROP,
                                     PendingChangeConfiguration.PACKAGE_DELETED]}"/>
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
                            <g:if test="${customerTypeService.isConsortium( contextCustomerType )}">
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
        </g:form>--%>

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.app.adjustDropdown = function () {

                var showSubscriber = $("input[name='show.subscriber']").prop('checked');
                var showConnectedObjs = $("input[name='show.connectedObjects']").prop('checked');
                var url = '<g:createLink controller="ajaxJson"
                                         action="adjustSubscriptionList"/>'

                url = url + '?valueAsOID=true&showSubscriber=' + showSubscriber + '&showConnectedObjs=' + showConnectedObjs

                var status = $("select#status").serialize()
                if (status) {
                    url = url + '&' + status
                }

                var lookup = {
                    '${RDStore.SUBSCRIPTION_HOLDING_ENTIRE.id}': '${RDStore.SUBSCRIPTION_HOLDING_ENTIRE.getI10n('value')}',
                    '${RDStore.SUBSCRIPTION_HOLDING_PARTIAL.id}': '${RDStore.SUBSCRIPTION_HOLDING_PARTIAL.getI10n('value')}',
                };

                $.ajax({
                    url: url,
                    success: function (data) {
                        var select = '';
                        for (var index = 0; index < data.length; index++) {
                            var option = data[index];
                            var optionText = option.text;
                            var optionValue = option.value;
                            var holdingSelection = null;
                            if(option.holdingSelection !== null)
                                holdingSelection = lookup[option.holdingSelection];
                            var count = index + 1
                            // console.log(optionValue +'-'+optionText)
                            if(holdingSelection !== null)
                                optionText += ' (' + holdingSelection + ')'
                            select += '<div class="item" data-value="' + optionValue + '" data-holdingSelection="' + holdingSelection + '">'+ count + ': ' + optionText + '</div>';
                        }

                        select = '<label>${message(code: 'default.select2.label', args: [message(code: "subscription.label")])}:</label> <div class="ui fluid search selection dropdown la-filterProp">' +
                '   <input type="hidden" id="targetObjectId" name="targetObjectId">' +
                '   <i class="dropdown icon"></i>' +
                '   <div class="default text">${message(code: 'default.select.choose.label')}</div>' +
                '   <div class="menu">'
                + select +
                '</div>' +
                '</div>';

                        $('#dropdownWrapper').html(select);

                        $('.la-filterProp').dropdown({
                            duration: 150,
                            transition: 'fade',
                            clearable: true,
                            forceSelection: false,
                            selectOnKeydown: false,
                            onChange: function (value, text, $selectedItem) {
                                value !== '' ? $(this).addClass("la-filter-selected") : $(this).removeClass("la-filter-selected");
                                $("#subOID").val($selectedItem.attr('data-value'));
                                if($selectedItem.attr('data-holdingSelection') === 'null') {
                                    $(".holdingSelection").show();
                                }
                                else $(".holdingSelection").hide();
                            }
                        });
                    }, async: false
                });
            }

            JSPC.app.adjustDropdown();
            $(".holdingSelection").hide();

            $("#inheritHoldingSelection").click(function(e) {
                e.preventDefault();
                let isInherited = $(this).attr('data-inherited') === 'true';
                let button = $(this);
                let icon = $(this).find('i');
                $.ajax({
                    url: '<g:createLink controller="ajax" action="toggleAudit" />',
                    data: {
                        owner: $('#subOID').val(),
                        property: 'holdingSelection',
                        returnSuccessAsJSON: true,
                    }
                }).done(function(response) {
                    button.toggleClass('blue').toggleClass('green');
                    if(isInherited) {
                        icon.addClass('la-thumbtack slash').removeClass('thumbtack');
                        button.attr('data-inherited', 'false');
                    }
                    else {
                        icon.removeClass('la-thumbtack slash').addClass('thumbtack');
                        button.attr('data-inherited', 'true');
                    }
                }).fail(function () {
                    console.log("AJAX error! Please check logs!");
                });
            });
        </laser:script>

    </ui:modal>
</g:if>

%{--
<g:if test="${(editable && contextService.hasAffiliation(CustomerTypeService.PERMS_PRO, 'INST_EDITOR')) && ! ['list'].contains(actionName)}">
    <laser:render template="/templates/documents/modal" model="${[ownobj: packageInstance, institution: contextService.getOrg(), owntp: 'pkg']}"/>
    <laser:render template="/templates/tasks/modal_create" model="${[ownobj:packageInstance, owntp:'pkg']}"/>
</g:if>
<g:if test="${userService.checkAffiliationAndCtxOrg(user,org,'INST_EDITOR') && ! ['list'].contains(actionName)}">
    <laser:render template="/templates/notes/modal_create" model="${[ownobj: packageInstance, owntp: 'pkg']}"/>
</g:if>--}%
