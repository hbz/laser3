<div class="ui negative message">
    <div class="header">
        <g:message code="myinst.message.attention" />:
        <g:message code="myinst.subscriptionDetails.message.ChildView" />
        <g:each in="${subscriptionInstance.getAllSubscribers()?.collect{itOrg -> itOrg.getDesignation()}}" var="subscr">
            <span class="ui label">${subscr}</span>.
        </g:each>
    </div>
    <p>
        <g:message code="myinst.subscriptionDetails.message.hereLink" />
        <g:link controller="subscription" action="members" id="${subscriptionInstance.instanceOf.id}">
            <g:message code="myinst.subscriptionDetails.message.backToMembers" />
        </g:link>
        <g:message code="myinst.subscriptionDetails.message.and" />
        <g:link controller="subscription" action="show" id="${subscriptionInstance.instanceOf.id}">
            <g:message code="myinst.subscriptionDetails.message.consortialLicence" />
        </g:link>.
    </p>
</div>