<%@ page import="de.laser.interfaces.CalculatedType; de.laser.interfaces.CalculatedType;" %>
<g:set var="checkCons" value="${contextOrg?.id == subscriptionInstance.getConsortia()?.id && subscriptionInstance.getCalculatedType() in [CalculatedType.CALCULATED_TYPE_PARTICIPATION, CalculatedType.CALCULATED_TYPE_PARTICIPATION_AS_COLLECTIVE]}" />
<g:set var="checkColl" value="${contextOrg?.id == subscriptionInstance.getCollective()?.id && subscriptionInstance.getCalculatedType() in [CalculatedType.CALCULATED_TYPE_PARTICIPATION]}" />

<g:if test="${checkCons || checkColl}">

<div class="ui negative message">
    <div class="header">
        <g:message code="myinst.message.attention" />:
        <g:message code="myinst.subscriptionDetails.message.ChildView" />
        <g:each in="${subscriptionInstance.getAllSubscribers()?.collect{itOrg -> itOrg.getDesignation()}}" var="subscr">
            <span class="ui label">${subscr}</span>.
        </g:each>
        <div class="la-float-right">
            <g:if test="${params.orgBasicMemberView}">
                <g:link controller="${controllerName}" action="${actionName}" id="${params.id}" params="[sub: subscriptionInstance.id]" data-content="${message(code: 'myinst.subscriptionDetails.message.changeToNormalView')}" class="ui icon button la-popup-tooltip la-delay red">
                    <i class="icon window restore "></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="${controllerName}" action="${actionName}" id="${params.id}" params="[orgBasicMemberView: true, sub: subscriptionInstance?.id]" data-content="${message(code: 'myinst.subscriptionDetails.message.changeToChildView')}" class="ui icon button la-popup-tooltip la-delay green">
                    <i class="icon window restore outline "></i>
                </g:link>
            </g:else>
        </div>
    </div>
    <p>
        <g:if test="${checkCons}">
            <g:message code="myinst.subscriptionDetails.message.hereLink" />
            <g:link controller="subscription" action="show" id="${subscriptionInstance.instanceOf.id}">
                <g:if test="${subscriptionInstance.administrative}">
                    <g:message code="myinst.subscriptionDetails.message.administrativeSubscription" />
                </g:if>
                <g:else>
                    <g:message code="myinst.subscriptionDetails.message.consortialSubscription" />
                </g:else>
            </g:link>
        </g:if>

        <g:elseif test="${checkColl}">
            <g:message code="myinst.subscriptionDetails.message.hereLink" />
            <g:link controller="subscription" action="show" id="${subscriptionInstance.instanceOf.id}">
                <g:message code="myinst.subscriptionDetails.message.collectiveSubscription" />
            </g:link>
        </g:elseif>

        <g:message code="myinst.subscriptionDetails.message.and" />

        <g:link controller="subscription" action="members" id="${subscriptionInstance.instanceOf.id}">
            <g:message code="myinst.subscriptionDetails.message.backToMembers" />
        </g:link>.
    </p>
</div>

</g:if>