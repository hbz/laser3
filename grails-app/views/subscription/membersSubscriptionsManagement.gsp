<%@ page import="de.laser.License; de.laser.Person; de.laser.storage.RDStore; de.laser.FormService" %>
<laser:htmlStart message="subscriptionsManagement.subscriptions.members" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="show" id="${subscription.id}" text="${subscription.name}"/>
    <ui:crumb class="active" text="${message(code: 'subscriptionsManagement.subscriptions.members')}"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}" text="${subscription.name}" />

<ui:anualRings object="${subscription}" controller="subscription" action="${actionName}"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<h2 class="ui left aligned icon header la-clear-before">
    ${message(code: 'subscriptionsManagement.subscriptions.members')}
</h2>

<laser:render template="${customerTypeService.getNavSubscriptionManagementTemplatePath()}" model="${[args: args]}"/>

<ui:messages data="${flash}"/>

<g:if test="${params.tab == 'linkLicense'}">
    <laser:render template="/templates/management/linkLicense"/>
</g:if>
<g:elseif test="${params.tab == 'linkPackages' && !contextService.getOrg().isCustomerType_Support()}">
    <laser:render template="/templates/management/linkPackages"/>
</g:elseif>
<g:elseif test="${params.tab == 'properties'}">
    <laser:render template="/templates/management/properties"/>
</g:elseif>
<g:elseif test="${params.tab == 'generalProperties'}">
    <laser:render template="/templates/management/generalProperties"/>
</g:elseif>
<g:elseif test="${params.tab == 'providerAgency' && !contextService.getOrg().isCustomerType_Support()}">
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


<laser:htmlEnd />

