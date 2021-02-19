<%@ page import="de.laser.Subscription;de.laser.License;de.laser.finance.CostItem;de.laser.PendingChange; de.laser.IssueEntitlement;" %>
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
<semui:totalNumber total="${num_pkgHistory_rows}"/>
</h1>
<semui:anualRings object="${subscription}" controller="subscription" action="entitlementChanges"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<g:render template="nav"/>

<g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
    <g:render template="message"/>
</g:if>

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
<table class="ui celled la-table table">
    <thead>
    <tr>
        <th>${message(code: 'sidewide.number')}</th>
        <th><g:message code="profile.dashboard.changes.object"/></th>
        <th><g:message code="profile.dashboard.changes.event"/></th>
        <th><g:message code="profile.dashboard.changes.action"/></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${packageHistory}" var="entry">
        <tr>
            <td>
                ${counter++}
            </td>
            <td>
                <g:set var="ie" value="${IssueEntitlement.findByTipp(entry.tipp)}"/>
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
            </td>
            <td>
                <g:if test="${!accepted.contains(entry.id)}">
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

<semui:paginate offset="${offset}" max="${max}" total="${num_pkgHistory_rows}"/>

</body>
</html>
