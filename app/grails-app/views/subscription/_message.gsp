<%@ page import="de.laser.interfaces.TemplateSupport;" %>
<g:set var="checkCons" value="${contextOrg?.id == subscriptionInstance.getConsortia()?.id && subscriptionInstance.getCalculatedType() in [TemplateSupport.CALCULATED_TYPE_PARTICIPATION, TemplateSupport.CALCULATED_TYPE_PARTICIPATION_AS_COLLECTIVE]}" />
<g:set var="checkColl" value="${contextOrg?.id == subscriptionInstance.getCollective()?.id && subscriptionInstance.getCalculatedType() in [TemplateSupport.CALCULATED_TYPE_PARTICIPATION]}" />

<g:if test="${checkCons || checkColl}">

<div class="ui negative message">
    <div class="header">
        <g:message code="myinst.message.attention" />:
        <g:message code="myinst.subscriptionDetails.message.ChildView" />
        <g:each in="${subscriptionInstance.getAllSubscribers()?.collect{itOrg -> itOrg.getDesignation()}}" var="subscr">
            <span class="ui label">${subscr}</span>.
        </g:each>
    </div>
    <p>
        <g:if test="${checkCons}">
            <g:message code="myinst.subscriptionDetails.message.hereLink" />
            <g:link controller="subscription" action="show" id="${subscriptionInstance.instanceOf.id}">
                <g:message code="myinst.subscriptionDetails.message.consortialLicence" />
            </g:link>
        </g:if>

        <g:elseif test="${checkColl}">
            <g:message code="myinst.subscriptionDetails.message.hereLink" />
            <g:link controller="subscription" action="show" id="${subscriptionInstance.instanceOf.id}">
                <g:message code="myinst.subscriptionDetails.message.collectiveLicence" />
            </g:link>
        </g:elseif>

        <g:message code="myinst.subscriptionDetails.message.and" />

        <g:link controller="subscription" action="members" id="${subscriptionInstance.instanceOf.id}">
            <g:message code="myinst.subscriptionDetails.message.backToMembers" />
        </g:link>.
    </p>
</div>

</g:if>