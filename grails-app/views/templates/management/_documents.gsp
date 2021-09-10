<%@ page import="de.laser.Subscription;" %>
<g:if test="${filteredSubscriptions}">

    <g:if test="${controllerName == "subscription"}">
        <div class="ui segment ">
            <h3 class="ui header"><g:message code="subscriptionsManagement.subscription"
                                             args="${args.superOrgType}"/></h3>

            <g:render template="/templates/documents/table"
                      model="${[instance: subscription, context: 'documents', redirect: actionName, owntp: 'subscription']}"/>
        </div>
    </g:if>

    <div class="ui segment">
        <h3 class="ui header">
            <g:if test="${controllerName == "subscription"}">
                ${message(code: 'subscriptionsManagement.subscriber')} <semui:totalNumber
                    total="${filteredSubscriptions.size()}"/>
            </g:if><g:else>
                ${message(code: 'subscriptionsManagement.subscriptions')} <semui:totalNumber
                        total="${filteredSubscriptions.size()}/${num_sub_rows}"/>
            </g:else>
        </h3>
        <table class="ui celled la-table table">
            <thead>
            <tr>
                <th>${message(code: 'sidewide.number')}</th>
                <g:if test="${controllerName == "subscription"}">
                    <th>${message(code: 'default.sortname.label')}</th>
                    <th>${message(code: 'subscriptionDetails.members.members')}</th>
                </g:if>
                <g:if test="${controllerName == "myInstitution"}">
                    <th>${message(code: 'default.subscription.label')}</th>
                </g:if>
                <th>${message(code: 'default.documents.label')}</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${filteredSubscriptions}" status="i" var="zeile">
                <g:set var="sub" value="${zeile instanceof Subscription ? zeile : zeile.sub}"/>
                <g:set var="subscr" value="${zeile instanceof Subscription ? zeile.getSubscriber() : zeile.orgs}"/>
                <tr>
                    <td>${i + 1}</td>
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
                                    <i class="thumbtack blue icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${subscr.getCustomerType() == 'ORG_INST'}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
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
                        <g:render template="/templates/documents/table"
                                  model="${[instance: sub, context: 'documents', redirect: actionName, owntp: 'subscription']}"/>

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

</laser:script>