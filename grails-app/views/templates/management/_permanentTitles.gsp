<%@ page import="de.laser.finance.CostItem; de.laser.Person; de.laser.storage.RDStore; de.laser.FormService; de.laser.SubscriptionPackage; de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${filteredSubscriptions}">

    <g:set var="editableOld" value="${editable}"/>

    <h3 class="ui header">${message(code: 'subscriptionsManagement.info.package')}</h3>

        <div class="ui segment">

            <h3 class="ui header">
                <g:if test="${controllerName == "subscription"}">
                    ${message(code: 'subscriptionsManagement.subscriber')} <ui:totalNumber
                        total="${filteredSubscriptions.size()}"/>
                </g:if><g:else>
                    ${message(code: 'subscriptionsManagement.subscriptions')} <ui:totalNumber
                            total="${num_sub_rows}"/>
                </g:else>
            </h3>

            <table class="ui celled la-js-responsive-table la-table table">
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
                    <th>${message(code: 'default.startDate.label.shy')}</th>
                    <th>${message(code: 'default.endDate.label.shy')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th>${message(code: 'subscription.hasPerpetualAccess.label')}</th>
                    <th>${message(code: 'permanentTitle.label')}</th>
                    <th>${message(code: 'package.label')}</th>
                    <th class="la-no-uppercase">
                        <ui:multiYearIcon isConsortial="true" />
                    </th>
                    <th>${message(code:'default.actions.label')}</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${filteredSubscriptions}" status="i" var="zeile">
                    <g:set var="sub" value="${zeile instanceof Subscription ? zeile : zeile.sub}"/>
                    <g:set var="subscr" value="${zeile instanceof Subscription ? zeile.getSubscriber() : zeile.orgs}"/>
                    <tr>
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

                                <ui:customerTypeProIcon org="${subscr}" />
                            </td>
                        </g:if>
                        <g:if test="${controllerName == "myInstitution"}">
                            <td>${sub.name}</td>
                        </g:if>

                        <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                        <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                        <td>${sub.status.getI10n('value')}</td>
                        <td>
                            <ui:xEditableBoolean owner="${sub}" field="hasPerpetualAccess" overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'hasPerpetualAccess']"/>
                        </td>
                        <g:set var="countCurrentPermanentTitles" value="${subscriptionService.countCurrentPermanentTitles(sub)}"/>
                        <g:set var="countCurrentIssueEntitlements" value="${subscriptionService.countCurrentIssueEntitlements(sub)}"/>

                        <td class="center aligned ${(sub.hasPerpetualAccess && countCurrentPermanentTitles != countCurrentIssueEntitlements) || (!sub.hasPerpetualAccess && countCurrentPermanentTitles > 0 && countCurrentPermanentTitles != countCurrentIssueEntitlements) ? 'negative' : ''}">
                            ${countCurrentPermanentTitles}
                        </td>
                        <td class="center aligned ${(sub.hasPerpetualAccess && countCurrentPermanentTitles != countCurrentIssueEntitlements) || (!sub.hasPerpetualAccess && countCurrentPermanentTitles > 0 && countCurrentPermanentTitles != countCurrentIssueEntitlements) ? 'negative' : ''}">
                            ${countCurrentIssueEntitlements}
                        </td>
                        <td>
                            <g:if test="${sub.isMultiYear}">
                                <ui:multiYearIcon isConsortial="true" color="orange" />
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
        </div><!-- .segment -->
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br/><strong><g:message code="filter.result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br/><strong><g:message code="result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
    </g:else>
</g:else>



