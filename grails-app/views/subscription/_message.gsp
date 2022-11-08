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
        <div class="la-float-right">
            <g:if test="${params.orgBasicMemberView}">
                <g:link controller="${controllerName}" action="${actionName}" id="${params.id}" params="[sub: subscription.id]" data-content="${message(code: 'myinst.subscriptionDetails.message.changeToNormalView')}" class="ui icon button la-popup-tooltip la-delay red">
                    <i class="icon window restore "></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="${controllerName}" action="${actionName}" id="${params.id}" params="[orgBasicMemberView: true, sub: subscription.id]" data-content="${message(code: 'myinst.subscriptionDetails.message.changeToChildView')}" class="ui icon button la-popup-tooltip la-delay green">
                    <i class="icon window restore outline "></i>
                </g:link>
            </g:else>
        </div>
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