<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.helper.RDStore; de.laser.helper.RDConstants; de.laser.FormService; de.laser.Subscription;" %>
<laser:serviceInjection/>

<g:if test="${filteredSubscriptions}">
    <g:if test="${controllerName == "subscription"}">
        <div class="ui segment ">
            <h3 class="ui header"><g:message code="subscriptionsManagement.subscription"
                                             args="${args.superOrgType}"/></h3>

            <table class="ui celled la-table table">
                <thead>
                <tr>
                    <th>${message(code: 'subscription')}</th>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>${subscription.name}</td>

                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${subscription.startDate}"/>
                        <semui:auditButton auditable="[subscription, 'startDate']"/>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${subscription.endDate}"/>
                        <semui:auditButton auditable="[subscription, 'endDate']"/>
                    </td>
                    <td>
                        ${subscription.status.getI10n('value')}
                        <semui:auditButton auditable="[subscription, 'status']"/>
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
    </g:if>

    <div class="ui segment">
    <g:form action="${actionName}" controller="${controllerName}" params="[tab: 'multiYear']" method="post"
            class="ui form propertiesSubscription">
        <g:hiddenField id="pspm_id_${params.id}" name="id" value="${params.id}"/>
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

        <h4 class="ui header">${message(code: 'subscriptionsManagement.info.subscriptionProperty')}</h4>

        <div class="two fields">
            <semui:datepicker label="subscription.startDate.label" id="valid_from" name="valid_from"/>

            <semui:datepicker label="subscription.endDate.label" id="valid_to" name="valid_to"/>
        </div>

        <div class="two fields">
            <div class="field">
                <label>${message(code: 'default.status.label')}</label>
                <%
                    def fakeList = []
                    fakeList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS))
                    fakeList.remove(RefdataValue.getByValueAndCategory('Deleted', RDConstants.SUBSCRIPTION_STATUS))
                %>
                <laser:select name="process_status" from="${fakeList}" optionKey="id" optionValue="value"
                              noSelection="${['': '']}"
                              value="${['': '']}"/>
            </div>
            <div class="field">
                <label>${message(code: 'subscription.isMultiYear.label')}</label>
                <laser:select name="process_isMultiYear"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id" optionValue="value" noSelection="${['': '']}"
                              value="${['': '']}"/>
            </div>
        </div>


        <button class="ui button" ${!editable ? 'disabled="disabled"' : ''} type="submit" name="processOption"
                value="changeProperties">${message(code: 'default.button.save_changes')}</button>


        <h3 class="ui header">
            <g:if test="${controllerName == "subscription"}">
                ${message(code: 'subscriptionsManagement.subscriber')} <semui:totalNumber
                    total="${filteredSubscriptions.size()}"/>
            </g:if><g:else>
                ${message(code: 'subscriptionsManagement.subscriptions')} <semui:totalNumber
                        total="${filteredSubscriptions.size()}/${num_sub_rows}"/>
            </g:else>
        </h3>
        <table class="ui celled la-js-responsive-table la-table table">
            <thead>
            <tr>
                <g:if test="${editable}">
                    <th data-label="${message(code:'responsive.table.selectElement')}">
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
                <th>${message(code: 'default.status.label')}</th>
                <th>${message(code: 'subscription.isMultiYear.label')}</th>
                <th></th>
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

                            <g:if test="${subscr.getCustomerType() == 'ORG_INST'}">
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
                        <semui:xEditable owner="${sub}" field="startDate" type="date" validation="datesCheck"/>
                        <semui:auditButton auditable="[sub, 'startDate']"/>
                    </td>
                    <td><semui:xEditable owner="${sub}" field="endDate" type="date" validation="datesCheck"/>
                    <semui:auditButton auditable="[sub, 'endDate']"/>
                    </td>
                    <td>
                        <semui:xEditableRefData owner="${sub}" field="status"
                                                config="${RDConstants.SUBSCRIPTION_STATUS}"
                                                constraint="removeValue_deleted"/>
                        <semui:auditButton auditable="[sub, 'status']"/>
                    </td>
                    <td>
                        <semui:xEditableBoolean owner="${sub}" field="isMultiYear"/>
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
        <br/><strong><g:message code="result.empty.object"
                                args="${[message(code: "subscription.plural")]}"/></strong>
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

    $('.propertiesSubscription').form({
        on: 'blur',
        inline: true,
        fields: {
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


