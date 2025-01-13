<%@ page import="de.laser.interfaces.CalculatedType;de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>
<laser:htmlStart message="menu.institutions.subscriptionsManagement" />

<ui:breadcrumbs>
    <ui:crumb controller="org" action="show" id="${contextService.getOrg().id}" text="${contextService.getOrg().getDesignation()}"/>
    <ui:crumb message="menu.institutions.subscriptionsManagement" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.institutions.subscriptionsManagement" total="${num_sub_rows}" />

<laser:render template="${customerTypeService.getNavSubscriptionManagementTemplatePath()}" model="${[args: args]}"/>

<ui:messages data="${flash}"/>

<ui:debugInfo>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
</ui:debugInfo>

<g:if test="${params.tab == 'properties'}">
    <laser:render template="/templates/management/properties"/>
</g:if><g:else>

    <laser:render template="${customerTypeService.getSubscriptionFilterTemplatePath()}"/>

    <g:if test="${params.tab == 'linkLicense'}">
        <laser:render template="/templates/management/linkLicense"/>
    </g:if>
    <g:elseif test="${params.tab == 'linkPackages' && !contextService.getOrg().isCustomerType_Support()}">
        <laser:render template="/templates/management/linkPackages"/>
    </g:elseif>
    <g:elseif test="${params.tab == 'permanentTitles' && !contextService.getOrg().isCustomerType_Support()}">
        <laser:render template="/templates/management/permanentTitles"/>
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

    <g:if test="${filteredSubscriptions}">
        <ui:paginate action="${actionName}" controller="${controllerName}" params="${params}" max="${max}" total="${num_sub_rows}"/>
    </g:if>
</g:else>

<laser:htmlEnd />
