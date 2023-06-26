<%@ page import="de.laser.interfaces.CalculatedType;de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>
<laser:htmlStart message="menu.institutions.subscriptionsManagement" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="org" action="show" id="${institution.id}" text="${institution.getDesignation()}"/>
    <ui:crumb message="menu.institutions.subscriptionsManagement" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.institutions.subscriptionsManagement" total="${num_sub_rows}" />

<laser:render template="/templates/management/navSubscriptionManagement" model="${[args: args]}"/>

<ui:messages data="${flash}"/>

<g:if test="${params.tab == 'properties'}">
    <laser:render template="/templates/management/properties"/>
</g:if><g:else>

    <laser:render template="/templates/subscription/subscriptionFilter"/>

    <g:if test="${params.tab == 'linkLicense'}">
        <laser:render template="/templates/management/linkLicense"/>
    </g:if>
    <g:elseif test="${params.tab == 'linkPackages'}">
        <laser:render template="/templates/management/linkPackages"/>
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

    <g:if test="${filteredSubscriptions}">
        <ui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                        max="${max}" total="${num_sub_rows}"/>
    </g:if>
</g:else>

<laser:htmlEnd />
