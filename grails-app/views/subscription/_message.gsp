<%@ page import="de.laser.ui.Icon; de.laser.interfaces.CalculatedType;" %>
<laser:serviceInjection/>
<g:set var="checkCons" value="${contextService.getOrg().id == subscription.getConsortium()?.id && subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION}" />%{-- ERMS-6070 subFinancialData --}%

<g:if test="${checkCons}">

<div class="ui error message">
    <div class="header">
        <g:message code="myinst.message.attention" />
        <g:message code="myinst.subscriptionDetails.message.ChildView" />
        <g:if test="${subscription.getSubscriber()}">
            <span class="ui label">
                <ui:customerTypeIcon org="${subscription.getSubscriber()}" /><g:link controller="organisation" action="show" id="${subscription.getSubscriber().id}">${subscription.getSubscriber().getDesignation()}</g:link>
            </span>.
        </g:if>
    </div>
    <p>
        <g:if test="${checkCons}">
            <g:message code="myinst.subscriptionDetails.message.hereLink" />
            <g:link controller="subscription" action="show" id="${subscription.instanceOf.id}">
                <g:if test="${subscription.administrative}">
                    <g:message code="myinst.subscriptionDetails.message.administrativeSubscription" />
                </g:if>
                <g:else>
                    <g:message code="myinst.subscriptionDetails.message.consortialSubscription" />
                </g:else>
            </g:link>
        </g:if>

        <g:message code="myinst.subscriptionDetails.message.and" />

        <g:link controller="subscription" action="members" id="${subscription.instanceOf.id}">
            <g:message code="myinst.subscriptionDetails.message.backToMembers" />
        </g:link>.
    </p>
</div>

    <g:if test="${subscription.comment}">

        <ui:msg class="info" showIcon="true" hideClose="true" header="${message(code: 'subscription.details.internalComment')}">
            ${subscription.comment}
        </ui:msg>

    </g:if>

</g:if>