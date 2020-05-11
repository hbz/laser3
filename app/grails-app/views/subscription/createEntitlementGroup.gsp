<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.Subscription;" %>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.addEntitlements.label')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <semui:crumb controller="subscription" action="index" id="${subscriptionInstance.id}"
                 text="${subscriptionInstance.name}"/>
    <semui:crumb class="active"
                 text="${message(code: 'subscription.details.createEntitlementGroup.label')}"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon/>
<g:inPlaceEdit domain="${Subscription.class.name}" pk="${subscriptionInstance.id}" field="name" id="name" class="newipe">${subscriptionInstance?.name}</g:inPlaceEdit>
</h1>
<h2 class="ui left aligned icon header la-clear-before">${message(code: 'subscription.details.createEntitlementGroup.label')}</h2>
<semui:messages data="${flash}"/>

<semui:form>
    <g:form action="processCreateEntitlementGroup" controller="subscription" params="[id: params.id]" method="post" class="ui form">

        <div class="field required ">
            <label>${message(code: 'issueEntitlementGroup.name.label')}</label>
            <input type="text" name="name" placeholder="" value="${params.name}" required/>
        </div>

        <div class="field ">
            <label>${message(code: 'issueEntitlementGroup.description.label')}</label>

            <textarea name="description">${params.description}</textarea>
        </div>

        <br/>

        <input type="submit" class="ui button"
               value="${message(code: 'default.button.create.label')}"/>

    </g:form>
</semui:form>

</body>
</html>
