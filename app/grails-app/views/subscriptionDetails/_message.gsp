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
        <g:link controller="subscriptionDetails" action="members" id="${subscriptionInstance.instanceOf.id}">
            <g:message code="myinst.subscriptionDetails.message.backToMembers" />
        </g:link>
        <g:message code="myinst.subscriptionDetails.message.and" />
        <g:link controller="subscriptionDetails" action="show" id="${subscriptionInstance.instanceOf.id}">
            <g:message code="myinst.subscriptionDetails.message.consotialLicence" />
        </g:link>.
    </p>
</div>