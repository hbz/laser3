<%@ page import="de.laser.License; de.laser.Person; de.laser.storage.RDStore; de.laser.FormService" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'subscriptionsManagement.subscriptions.members')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <semui:crumb controller="subscription" action="show" id="${subscription.id}"
                 text="${subscription.name}"/>
    <semui:crumb class="active"
                 text="${message(code: 'subscriptionsManagement.subscriptions.members')}"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${subscription.name}</h1>

<semui:anualRings object="${subscription}" controller="subscription" action="${actionName}"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


<h2>
    ${message(code: 'subscriptionsManagement.subscriptions.members')}
</h2>

<laser:render template="/templates/management/navSubscriptionManagement" model="${[args: args]}"/>

<semui:messages data="${flash}"/>

<h4 class="ui header">
    <g:message code="subscription"/>: <g:link
        controller="subscription" action="show"
        id="${subscription.id}">${subscription.name}</g:link><br /><br />

    <g:if test="${params.tab == 'linkLicense' && parentLicense}">
        <g:message code="subscriptionsManagement.license" args="${args.superOrgType}"/>: <g:link
            controller="license"
            action="show"
            id="${parentLicense.id}">${parentLicense.reference}</g:link>
    </g:if>
</h4>



<g:if test="${params.tab == 'linkLicense'}">
    <laser:render template="/templates/management/linkLicense"/>
</g:if>
<g:elseif test="${params.tab == 'linkPackages'}">
    <laser:render template="/templates/management/linkPackages"/>
</g:elseif>
<g:elseif test="${params.tab == 'properties'}">
    <laser:render template="/templates/management/properties"/>
</g:elseif>
<g:elseif test="${params.tab == 'generalProperties'}">
    <laser:render template="/templates/management/generalProperties"/>
</g:elseif>
<g:elseif test="${params.tab == 'providerAgency'}">
    <laser:render template="/templates/management/providerAgency"/>
</g:elseif>
<g:elseif test="${params.tab == 'multiYear'}">
    <laser:render template="/templates/management/multiYear"/>
</g:elseif>
<g:elseif test="${params.tab == 'notes'}">
    <laser:render template="/templates/management/notes"/>
</g:elseif>
<g:elseif test="${params.tab == 'documents'}">
    <laser:render template="/templates/management/documents"/>
</g:elseif>
<g:elseif test="${params.tab == 'customerIdentifiers'}">
    <laser:render template="/templates/management/customerIdentifiers"/>
</g:elseif>


</body>
</html>

