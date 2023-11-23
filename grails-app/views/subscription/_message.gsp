<%@ page import="de.laser.interfaces.CalculatedType;" %>
<g:set var="checkCons" value="${contextOrg.id == subscription.getConsortia()?.id && subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION}" />

<g:if test="${checkCons}">

<div class="ui negative message">
    <div class="header">
        <g:message code="myinst.message.attention" />
        <g:message code="myinst.subscriptionDetails.message.ChildView" />
        <g:each in="${subscription.getAllSubscribers()}" var="subscr">
            <span class="ui label"><g:link controller="organisation" action="show" id="${subscr.id}">${subscr.getDesignation()}</g:link></span>.
        </g:each>
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

</g:if>