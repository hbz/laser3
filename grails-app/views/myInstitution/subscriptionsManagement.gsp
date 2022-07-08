<%@ page import="de.laser.interfaces.CalculatedType;de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>
<laser:htmlStart message="menu.my.subscriptionsManagement" serviceInjection="true"/>

<semui:breadcrumbs>
    <semui:crumb message="menu.my.subscriptionsManagement" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
</semui:controlButtons>

<semui:h1HeaderWithIcon message="menu.my.subscriptionsManagement" total="${num_sub_rows}" />

<laser:render template="/templates/management/navSubscriptionManagement" model="${[args: args]}"/>

<semui:messages data="${flash}"/>



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
        <semui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                        next="${message(code: 'default.paginate.next')}"
                        prev="${message(code: 'default.paginate.prev')}" max="${max}"
                        total="${num_sub_rows}"/>
    </g:if>
</g:else>

<laser:htmlEnd />
