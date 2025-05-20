<%@ page import="de.laser.License; de.laser.addressbook.Person; de.laser.storage.RDStore; de.laser.FormService" %>
<laser:htmlStart message="subscriptionsManagement.subscriptions.members" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="show" id="${subscription.id}" text="${subscription.name}"/>
    <ui:crumb class="active" text="${message(code: 'subscriptionsManagement.subscriptions.members')}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}" text="${subscription.name}" />

<ui:anualRings object="${subscription}" controller="subscription" action="${actionName}"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}" tab="${params.tab}"/>

<h2 class="ui left aligned icon header la-clear-before">
    ${message(code: 'subscriptionsManagement.subscriptions.members')}
    <g:if test="${filteredSubscriptions}">
        <ui:totalNumber total="${filteredSubscriptions.size()}"/>
    </g:if>
</h2>

<g:if test="${params.tab != 'customerIdentifiers'}">
    <g:form action="${actionName}" params="[tab: params.tab, id: params.id, propertiesFilterPropDef: propertiesFilterPropDef]">
        <div class="ui toggle checkbox">
            <input type="checkbox" onchange="this.form.submit()" name="showMembersSubWithMultiYear" ${params.showMembersSubWithMultiYear ? 'checked' : ''}>
            <label><g:message code="subscriptionsManagement.showMembersSubWithMultiYear"/></label>
        </div>
    </g:form>
</g:if>


<laser:render template="${customerTypeService.getNavSubscriptionManagementTemplatePath()}" model="${[args: args]}"/>

<ui:messages data="${flash}"/>

<g:if test="${params.tab != 'customerIdentifiers'}">
    <ui:filter>
        <g:form action="membersSubscriptionsManagement" controller="subscription"
                params="${[id: params.id, showMembersSubWithMultiYear: params.showMembersSubWithMultiYear, propertiesFilterPropDef: propertiesFilterPropDef]}"
                method="get" class="ui form">
            <input type="hidden" name="tab" value="${params.tab}"/>
            <laser:render template="/templates/filter/orgFilter"
                          model="[
                                  tmplConfigShow      : [['name', 'identifier', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['discoverySystemsFrontend', 'discoverySystemsIndex'], ['subRunTimeMultiYear']],
                                  tmplConfigFormFilter: true
                          ]"/>
        </g:form>
    </ui:filter>
</g:if>

<g:if test="${params.tab == 'linkLicense'}">
    <laser:render template="/templates/management/linkLicense"/>
</g:if>
<g:elseif test="${params.tab == 'linkPackages' && !contextService.getOrg().isCustomerType_Support()}">
    <laser:render template="/templates/management/linkPackages"/>
</g:elseif>
<g:elseif test="${params.tab == 'permanentTitles' && !contextService.getOrg().isCustomerType_Support()}">
    <laser:render template="/templates/management/permanentTitles"/>
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

