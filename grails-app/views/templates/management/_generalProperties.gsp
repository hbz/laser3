<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.FormService; de.laser.Subscription; de.laser.interfaces.CalculatedType;" %>
<laser:serviceInjection/>

<g:if test="${filteredSubscriptions}">

    <g:set var="editableOld" value="${editable}"/>

    <g:if test="${controllerName == "subscription"}">
        <div class="ui segment">
            <h3 class="ui header"><g:message code="consortium.superSubscriptionType"/></h3>
            <table class="ui celled  monitor stackable la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th>${message(code: 'subscription')}</th>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <th>${message(code: 'subscription.referenceYear.label')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th>${message(code: 'subscription.kind.label')}</th>
                    <th>${message(code: 'subscription.form.label')}</th>
                    <th>${message(code: 'subscription.resource.label')}</th>
                    <th>${message(code: 'subscription.isPublicForApi.label')}</th>
                    <th>${message(code: 'subscription.hasPerpetualAccess.label')}</th>
                    <th>${message(code: 'subscription.hasPublishComponent.label')}</th>
                    <th>${message(code: 'default.actions.label')}</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>${subscription.name}</td>

                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${subscription.startDate}"/>
                        <ui:auditButton auditable="[subscription, 'startDate']"/>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${subscription.endDate}"/>
                        <ui:auditButton auditable="[subscription, 'endDate']"/>
                    </td>
                    <td>
                        ${subscription.referenceYear}
                        <ui:auditButton auditable="[subscription, 'referenceYear']"/>
                    </td>
                    <td>
                        ${subscription.status.getI10n('value')}
                        <ui:auditButton auditable="[subscription, 'status']"/>
                    </td>
                    <td>
                        ${subscription.kind?.getI10n('value')}
                        <ui:auditButton auditable="[subscription, 'kind']"/>
                    </td>
                    <td>
                        ${subscription.form?.getI10n('value')}
                        <ui:auditButton auditable="[subscription, 'form']"/>
                    </td>
                    <td>
                        ${subscription.resource?.getI10n('value')}
                        <ui:auditButton auditable="[subscription, 'resource']"/>
                    </td>
                    <td>
                        ${subscription.isPublicForApi ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                        <ui:auditButton auditable="[subscription, 'isPublicForApi']"/>
                    </td>
                    <td>
                        ${subscription.hasPerpetualAccess ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                        <ui:auditButton auditable="[subscription, 'hasPerpetualAccess']"/>
                    </td>
                    <td>
                        ${subscription.hasPublishComponent ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                        <ui:auditButton auditable="[subscription, 'hasPublishComponent']"/>
                    </td>

                    <td class="x">
                        <g:link controller="subscription" action="show" id="${subscription.id}"
                                class="ui icon button blue la-modern-button"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.edit.universal')}">
                            <i aria-hidden="true" class="write icon"></i></g:link>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="divider"></div>

    </g:if>

    <div class="ui segment">
        <g:form action="${actionName}" controller="${controllerName}" params="[tab: 'generalProperties']" method="post"
                class="ui form propertiesSubscription">
            <g:hiddenField id="pspm_id_${params.id}" name="id" value="${params.id}"/>
            <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

            <h4 class="ui header">${message(code: 'subscriptionsManagement.info.subscriptionProperty')}</h4>

            <div class="three fields">
                <ui:datepicker label="subscription.startDate.label" id="valid_from" name="valid_from"/>

                <ui:datepicker label="subscription.endDate.label" id="valid_to" name="valid_to"/>

                <ui:datepicker label="subscription.referenceYear.label" id="reference_year" name="reference_year" type="year"/>
            </div>

            <div class="four fields">
                <div class="field">
                    <label>${message(code: 'default.status.label')}</label>
                    <%
                        def fakeList = []
                        fakeList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS))
                        fakeList.remove(RefdataValue.getByValueAndCategory('Deleted', RDConstants.SUBSCRIPTION_STATUS))
                    %>
                    <ui:select name="process_status" from="${fakeList}" optionKey="id" optionValue="value"
                                  noSelection="${['': '']}"
                                  value="${['': '']}"/>
                </div>

                <div class="field">
                    <label>${message(code: 'subscription.kind.label')}</label>
                    <ui:select name="process_kind"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)}"
                                  optionKey="id" optionValue="value" noSelection="${['': '']}"
                                  value="${['': '']}"/>
                </div>

                <div class="field">
                    <label>${message(code: 'subscription.form.label')}</label>
                    <ui:select name="process_form"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}"
                                  optionKey="id" optionValue="value" noSelection="${['': '']}"
                                  value="${['': '']}"/>
                </div>

                <div class="field">
                    <label>${message(code: 'subscription.resource.label')}</label>
                    <ui:select name="process_resource"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}"
                                  optionKey="id" optionValue="value" noSelection="${['': '']}"
                                  value="${['': '']}"/>
                </div>
            </div>

            <div class="four fields">

                <div class="field">
                    <label>${message(code: 'subscription.isPublicForApi.label')}</label>
                    <ui:select name="process_isPublicForApi"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                  optionKey="id" optionValue="value" noSelection="${['': '']}"
                                  value="${['': '']}"/>
                </div>

                <div class="field">
                    <label>${message(code: 'subscription.hasPerpetualAccess.label')}</label>
                    <ui:select name="process_hasPerpetualAccess"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                  optionKey="id" optionValue="value" noSelection="${['': '']}"
                                  value="${['': '']}"/>
                </div>

                <div class="field">
                    <label>${message(code: 'subscription.hasPublishComponent.label')}</label>
                    <ui:select name="process_hasPublishComponent"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                  optionKey="id" optionValue="value" noSelection="${['': '']}"
                                  value="${['': '']}"/>
                </div>

                <g:if test="${accessService.checkPerm('ORG_PRO')}">
                    <div class="field">
                        <label>${message(code: 'subscription.isAutomaticRenewAnnually.label')}</label>
                        <ui:select name="process_isAutomaticRenewAnnually"
                                      from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                      optionKey="id" optionValue="value" noSelection="${['': '']}"
                                      value="${['': '']}"/>
                    </div>
                </g:if>

            </div>

            <button class="ui button" ${!editable ? 'disabled="disabled"' : ''} type="submit" name="processOption"
                    value="changeProperties">${message(code: 'default.button.save_changes')}</button>


            <h3 class="ui header">
                <g:if test="${controllerName == "subscription"}">
                    ${message(code: 'subscriptionsManagement.subscriber')} <ui:totalNumber
                        total="${filteredSubscriptions.size()}"/>
                </g:if><g:else>
                    ${message(code: 'subscriptionsManagement.subscriptions')} <ui:totalNumber
                            total="${filteredSubscriptions.size()}/${num_sub_rows}"/>
                </g:else>
            </h3>
            <table class="ui celled monitor stackable la-js-responsive-table la-table la-compact table">
                <thead>
                <tr>
                    <g:if test="${editable}">
                        <th>
                            <g:checkBox name="membersListToggler" id="membersListToggler" checked="false"/>
                        </th>
                    </g:if>
                    <th>${message(code: 'sidewide.number')}</th>
                    <g:if test="${controllerName == "subscription"}">
                        <th>${message(code: 'default.sortname.label')}</th>
                        <th>${message(code: 'subscriptionDetails.members.members')}</th>
                    </g:if>
                    <g:if test="${controllerName == "myInstitution"}">
                        <th>${message(code: 'default.subscription.label')}</th>
                    </g:if>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <th>${message(code: 'subscription.referenceYear.label')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th>${message(code: 'subscription.kind.label')}</th>
                    <th>${message(code: 'subscription.form.label')}</th>
                    <th>${message(code: 'subscription.resource.label')}</th>
                    <th>${message(code: 'subscription.isPublicForApi.label')}</th>
                    <th>${message(code: 'subscription.hasPerpetualAccess.label')}</th>
                    <th>${message(code: 'subscription.hasPublishComponent.label')}</th>
                    <g:if test="${accessService.checkPerm('ORG_PRO')}">
                        <th>${message(code: 'subscription.isAutomaticRenewAnnually.label')}</th>
                    </g:if>
                    <th class="la-no-uppercase">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                              data-content="${message(code: 'subscription.isMultiYear.label')}">
                            <i class="map orange icon"></i>
                        </span>
                    </th>
                    <th>${message(code:'default.actions.label')}</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${filteredSubscriptions}" status="i" var="zeile">
                    <g:set var="sub" value="${zeile instanceof Subscription ? zeile : zeile.sub}"/>
                    <g:set var="subscr" value="${zeile instanceof Subscription ? zeile.getSubscriber() : zeile.orgs}"/>
                    <tr>
                        <g:if test="${editable}">
                            <td>
                                <%-- This whole construct is necessary for that the form validation works!!! --%>
                                <div class="field">
                                    <div class="ui checkbox">
                                        <g:checkBox id="selectedSubs_${sub.id}" name="selectedSubs" value="${sub.id}"
                                                    checked="false"/>
                                    </div>
                                </div>
                            </td>
                        </g:if>
                        <td>${(offset ?: 0) + i + 1}</td>
                        <g:if test="${controllerName == "subscription"}">
                            <td>
                                ${subscr.sortname}
                            </td>
                            <td>
                                <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>

                                <g:if test="${sub.isSlaved}">
                                    <span data-position="top right"
                                          class="la-popup-tooltip la-delay"
                                          data-content="${message(code: 'license.details.isSlaved.tooltip')}">
                                        <i class="grey la-thumbtack-regular icon"></i>
                                    </span>
                                </g:if>

                                <g:if test="${subscr.isCustomerType_Inst_Pro()}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay"
                                          data-position="bottom center"
                                          data-content="${subscr.getCustomerTypeI10n()}">
                                        <i class="chess rook grey icon"></i>
                                    </span>
                                </g:if>
                            </td>
                        </g:if>
                        <g:if test="${controllerName == "myInstitution"}">
                            <td>${sub.name}</td>
                        </g:if>

                        <td>
                            <ui:xEditable owner="${sub}" field="startDate" type="date"
                                             overwriteEditable="${editableOld}" validation="datesCheck"/>
                            <ui:auditButton auditable="[sub, 'startDate']"/>
                        </td>
                        <td><ui:xEditable owner="${sub}" field="endDate" type="date"
                                             overwriteEditable="${editableOld}" validation="datesCheck"/>
                        <ui:auditButton auditable="[sub, 'endDate']"/>
                        </td>
                        <td>
                            <ui:xEditable owner="${sub}" field="referenceYear" type="year"
                                          overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'referenceYear']"/>
                        </td>
                        <td>
                            <ui:xEditableRefData owner="${sub}" field="status"
                                                    config="${RDConstants.SUBSCRIPTION_STATUS}"
                                                    constraint="removeValue_deleted"
                                                    overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'status']"/>
                        </td>
                        <td>
                            <ui:xEditableRefData owner="${sub}" field="kind"
                                                    config="${RDConstants.SUBSCRIPTION_KIND}"
                                                    overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'kind']"/>
                        </td>
                        <td>
                            <ui:xEditableRefData owner="${sub}" field="form"
                                                    config="${RDConstants.SUBSCRIPTION_FORM}"
                                                    overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'form']"/>
                        </td>
                        <td>
                            <ui:xEditableRefData owner="${sub}" field="resource"
                                                    config="${RDConstants.SUBSCRIPTION_RESOURCE}"
                                                    overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'resource']"/>
                        </td>
                        <td>
                            <ui:xEditableBoolean owner="${sub}" field="isPublicForApi"
                                                    overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'isPublicForApi']"/>
                        </td>
                        <td>
                            <ui:xEditableBoolean owner="${sub}" field="hasPerpetualAccess"
                                                    overwriteEditable="${editableOld}"/>
                            <%--<ui:xEditableRefData owner="${sub}" field="hasPerpetualAccess"
                                                    config="${RDConstants.Y_N}"
                                                    overwriteEditable="${editableOld}"/>--%>
                            <ui:auditButton auditable="[sub, 'hasPerpetualAccess']"/>
                        </td>
                        <td>
                            <ui:xEditableBoolean owner="${sub}" field="hasPublishComponent"
                                                    overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'hasPublishComponent']"/>
                        </td>
                        <g:if test="${accessService.checkPerm('ORG_PRO')}">
                            <td>
                                <g:if test="${(sub.type == RDStore.SUBSCRIPTION_TYPE_LOCAL && sub._getCalculatedType() == CalculatedType.TYPE_LOCAL)}">
                                    <ui:xEditableBoolean owner="${sub}" field="isAutomaticRenewAnnually" overwriteEditable="${editable && sub.isAllowToAutomaticRenewAnnually()}"/>
                                </g:if>
                            </td>
                        </g:if>
                        <td>
                            <g:if test="${sub.isMultiYear}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay"
                                      data-position="bottom center"
                                      data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                    <i class="map orange icon"></i>
                                </span>
                            </g:if>
                        </td>
                        <td class="x">
                            <g:link controller="subscription" action="show" id="${sub.id}"
                                    class="ui icon button blue la-modern-button"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </g:link>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:form>
    </div>
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br/><strong><g:message code="filter.result.empty.object"
                                args="${[message(code: "subscription.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br/><strong><g:message code="result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
    </g:else>
</g:else>

<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">

    $('#membersListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
        }
    });

    $.fn.form.settings.rules.endDateNotBeforeStartDate = function() {
        if($("#valid_from").val() !== '' && $("#valid_to").val() !== '') {
            var startDate = Date.parse(JSPC.helper.formatDate($("#valid_from").val()));
            var endDate = Date.parse(JSPC.helper.formatDate($("#valid_to").val()));
            return (startDate < endDate);
        }
        else return true;
    };

    $('.propertiesSubscription').form({
        on: 'blur',
        inline: true,
        fields: {
            valid_from: {
                identifier: 'valid_from',
                rules: [
                    {
                        type: 'endDateNotBeforeStartDate',
                        prompt: '<g:message code="validation.startDateAfterEndDate"/>'
                    }
                ]
            },
            valid_to: {
                identifier: 'valid_to',
                rules: [
                    {
                        type: 'endDateNotBeforeStartDate',
                        prompt: '<g:message code="validation.endDateBeforeStartDate"/>'
                    }
                ]
            },
            noSubscription: {
                identifier: 'selectedSubs',
                rules: [
                    {
                        type: 'checked',
                        prompt: '<g:message code="subscriptionsManagement.noSelectedSubscriptions.table"/>'
                    }
                ]
            }
        }
    });
</laser:script>


