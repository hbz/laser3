<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.FormService; de.laser.Subscription;" %>
<laser:serviceInjection/>
<g:if test="${filteredSubscriptions}">

    <g:if test="${controllerName == "subscription"}">
        <div class="ui segment">
            <h3 class="ui header"><g:message code="subscriptionsManagement.subscription" args="${args.superOrgType}"/></h3>

            <laser:render template="/templates/notes/table" model="${[instance: subscription, redirect: actionName]}"/>
        </div>
    </g:if>

    <h3 class="ui header">${message(code: 'subscriptionsManagement.note.info.newNote')}</h3>

    <g:form action="${actionName}" controller="${controllerName}" params="[tab: 'notes']" method="post"
            class="ui form notes">
        <g:hiddenField id="pspm_id_${params.id}" name="id" value="${params.id}"/>
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

        <div class="ui segments">
        <div class="ui segment">

        <div class="field">
            <label for="noteTitle">${message(code:'default.title.label')}:</label>
            <input type="text" id="noteTitle" name="noteTitle" />
        </div>
        <div class="field">
            <label for="noteContent">${message(code:'default.note.label')}:</label>
            <textarea class="la-textarea-resize-vertical" id="noteContent" name="noteContent"></textarea>
        </div>

        <button class="ui button" ${!editable ? 'disabled="disabled"' : ''} type="submit" name="processOption"
                value="newNote">${message(code: 'default.button.create.label')}</button>

        </div><!-- .segment -->
        <div class="ui segment">

        <h3 class="ui header">
            <g:if test="${controllerName == "subscription"}">
                ${message(code: 'subscriptionsManagement.subscriber')} <ui:totalNumber total="${filteredSubscriptions.size()}"/>
            </g:if><g:else>
                ${message(code: 'subscriptionsManagement.subscriptions')} <ui:totalNumber total="${num_sub_rows}"/>
            </g:else>
        </h3>
        <table class="ui celled la-js-responsive-table la-table table">
            <thead>
            <tr>
                <g:if test="${editable}">
                    <th class="center aligned">
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
                <th>${message(code:'default.notes.label')}</th>
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
                                    <g:checkBox id="selectedSubs_${sub.id}" name="selectedSubs" value="${sub.id}" checked="false"/>
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

                            <ui:customerTypeIcon org="${subscr}" />
                        </td>
                    </g:if>
                    <g:if test="${controllerName == "myInstitution"}">
                        <td>${sub.name} <span class="${sub.type == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL ? 'sc_blue' : ''}"> (${sub.type.getI10n('value')}) </span></td>
                    </g:if>
                    <td>
                        <laser:render template="/templates/notes/table" model="${[instance: sub, redirect: actionName]}"/>
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
        </div><!-- .segment -->
        </div><!-- .segments -->
    </g:form>

</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br/><strong><g:message code="filter.result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
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

    $('.notes').form({
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