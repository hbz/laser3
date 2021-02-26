<%@ page import="de.laser.Subscription;de.laser.License;de.laser.finance.CostItem;de.laser.PendingChange; de.laser.IssueEntitlement; de.laser.helper.RDStore; de.laser.RefdataValue;" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'myinst.pendingChanges.label')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>

<h1 class="ui icon header la-noMargin-top"><semui:headerIcon/>
<semui:xEditable owner="${subscription}" field="name"/>
</h1>
<semui:anualRings object="${subscription}" controller="subscription" action="entitlementChanges"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<g:render template="nav"/>

<g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
    <g:render template="message"/>
</g:if>


<div class="ui top attached tabular menu">
    <g:link controller="subscription" action="entitlementChanges" id="${subscription.id}" params="[tab: 'changes']"
            class="item ${params.tab == "changes" ? 'active' : '' }">
        <g:message code="myinst.pendingChanges.label"/>
        <span class="ui circular label">
            ${countPendingChanges}
        </span>
    </g:link>

    <g:link controller="subscription" action="entitlementChanges" id="${subscription.id}" params="[tab: 'acceptedChanges']"
            class="item ${params.tab == "acceptedChanges" ? 'active' : '' }">
        <g:message code="myinst.acceptedChanges.label"/>
        <span class="ui circular label">
            ${countAcceptedChanges}
        </span>
    </g:link>

</div>

<div class="ui bottom attached tab active segment">
<g:if test="${packages}">
    <g:form controller="pendingChange" action="processAll">
        <g:select from="${packages}" noSelection="${['': message(code: 'default.select.choose.label')]}"
                  name="acceptChangesForPackages" class="ui select search multiple dropdown" optionKey="${{ it.id }}"
                  optionValue="${{ it.pkg.name }}"/>
        <g:submitButton class="ui button positive" name="acceptAll" value="${message(code: 'pendingChange.takeAll')}"/>
        <g:submitButton class="ui button negative" name="rejectAll"
                        value="${message(code: 'pendingChange.rejectAll')}"/>
    </g:form>
</g:if>


<g:set var="counter" value="${offset + 1}"/>
<table class="ui celled la-table table sortable">
    <thead>
    <tr>
        <th>${message(code: 'sidewide.number')}</th>
        <th><g:message code="profile.dashboard.changes.object"/></th>
        <g:sortableColumn property="msgToken" title="${message(code: 'profile.dashboard.changes.event')}" params="[tab: params.tab]"/>
        <g:sortableColumn property="ts" title="${message(code: 'default.date.label')}" params="[tab: params.tab]"/>
        <th><g:message code="profile.dashboard.changes.action"/></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${changes}" var="entry">
        <tr>
            <td>
                ${counter++}
            </td>
            <td>
                <g:set var="ie" value="${IssueEntitlement.findByTippAndSubscription(entry.tipp, subscription)}"/>
                <g:if test="${entry.tipp}">
                    <g:if test="${ie}">
                        <g:link controller="issueEntitlement" action="show" id="${ie.id}">${entry.tipp.name}</g:link>
                    </g:if>
                    <g:else>
                        ${entry.tipp.name}
                    </g:else>
                </g:if>
                <g:elseif test="${entry.tippCoverage}">
                    <g:if test="${ie}">
                        <g:link controller="issueEntitlement" action="show"
                                id="${ie.id}">${entry.tippCoverage.tipp.name}</g:link>
                    </g:if>
                    <g:else>
                        ${entry.tippCoverage.tipp.name}
                    </g:else>

                </g:elseif>
                <g:elseif test="${entry.priceItem}">
                    <g:if test="${ie}">
                        <g:link controller="issueEntitlement" action="show"
                                id="${ie.id}">${entry.priceItem.tipp.name}</g:link>
                    </g:if>
                    <g:else>
                        ${entry.priceItem.tipp.name}
                    </g:else>
                </g:elseif>
            </td>
            <td>
                ${message(code: 'subscription.packages.' + entry.msgToken)}

                <g:if test="${entry.targetProperty in PendingChange.DATE_FIELDS && entry.oldValue != null && entry.newValue != null}">
                    <g:set var="oldValue" value="${formatDate(format: "${message(code:'default.date.format.notime')}", date:entry.oldValue)}"/>
                    <g:set var="newValue" value="${formatDate(format: "${message(code:'default.date.format.notime')}", date:entry.newValue)}"/>
                </g:if>
                <g:elseif test ="${entry.targetProperty in PendingChange.REFDATA_FIELDS}">
                    <g:set var="oldValue" value="${RefdataValue.get(entry.oldValue)?.getI10n('value')}"/>
                    <g:set var="newValue" value="${RefdataValue.get(entry.newValue)?.getI10n('value')}"/>
                </g:elseif>
                <g:else>
                    <g:set var="oldValue" value="${entry.oldValue}"/>
                    <g:set var="newValue" value="${entry.newValue}"/>
                </g:else>

                <g:if test="${oldValue != null && newValue != null}">
                    <i class="grey question circle icon la-popup-tooltip la-delay"
                       data-content="${(message(code: 'tipp.'+entry.targetProperty) ?: '')+': '+message(code: 'pendingChange.change', args: [oldValue, newValue])}"></i>
                </g:if>
                <g:elseif test ="${entry.targetProperty}">
                    <i class="grey question circle icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.'+entry.targetProperty)}"></i>
                </g:elseif>

            </td>
            <td>
                <g:formatDate format="${message(code:'default.date.format.noZ')}" date="${entry.ts}"/>
            </td>
            <td>
                <g:if test="${!(entry.status in [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_HISTORY, RDStore.PENDING_CHANGE_REJECTED])}">
                    <div class="ui buttons">
                        <g:link class="ui positive button" controller="pendingChange" action="accept" id="${entry.id}"
                                params="[subId: subscription.id]"><g:message
                                code="default.button.accept.label"/></g:link>
                        <%--
                        <div class="or" data-text="${message(code:'default.or')}"></div>
                        <g:link class="ui negative button" controller="pendingChange" action="reject" id="${entry.id}"><g:message code="default.button.reject.label"/></g:link>
                        --%>
                    </div>
                </g:if>
            </td>
        </tr>

    </g:each>
    </tbody>
</table>

<semui:paginate offset="${offset}" max="${max}" total="${num_change_rows}" params="${params}"/>

</div>
</body>
</html>
