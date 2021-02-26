<%@ page import="de.laser.Subscription;de.laser.License;de.laser.finance.CostItem;de.laser.PendingChange; de.laser.IssueEntitlement; de.laser.helper.RDStore; de.laser.RefdataValue;" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'myinst.pendingChanges.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="package" action="index" text="${message(code: 'package.show.all')}"/>
    <semui:crumb text="${packageInstance.name}" id="${packageInstance.id}" class="active"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-noMargin-top"><semui:headerIcon/>
<semui:xEditable owner="${packageInstance}" field="name"/>
</h1>


<g:render template="nav"/>



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
