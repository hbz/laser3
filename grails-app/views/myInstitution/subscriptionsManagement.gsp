<%@ page import="de.laser.interfaces.CalculatedType;de.laser.helper.RDStore; de.laser.helper.RDConstants; de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>
<laser:serviceInjection/>
<!doctype html>

<html>
<head>
    <meta name="layout" content="laser"/>
    <title>${message(code: 'laser')} : ${message(code: 'menu.my.subscriptionsManagement')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.my.subscriptionsManagement" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
</semui:controlButtons>

<h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon/>${message(code: 'menu.my.subscriptionsManagement')}
<semui:totalNumber total="${num_sub_rows}"/>
</h1>

<g:render template="/templates/management/navSubscriptionManagement" model="${[args: args]}"/>

<semui:messages data="${flash}"/>



<g:if test="${params.tab == 'properties'}">
    <g:render template="/templates/management/properties"/>
</g:if><g:else>

    <g:render template="/templates/subscription/subscriptionFilter"/>

    <g:if test="${params.tab == 'linkLicense'}">
        <g:render template="/templates/management/linkLicense"/>
    </g:if>
    <g:elseif test="${params.tab == 'linkPackages'}">
        <g:render template="/templates/management/linkPackages"/>
    </g:elseif>
    <g:elseif test="${params.tab == 'generalProperties'}">
        <g:render template="/templates/management/generalProperties"/>
    </g:elseif>
    <g:elseif test="${params.tab == 'providerAgency'}">
        <g:render template="/templates/management/providerAgency"/>
    </g:elseif>
    <g:elseif test="${params.tab == 'multiYear'}">
        <g:render template="/templates/management/multiYear"/>
    </g:elseif>
    <g:elseif test="${params.tab == 'notes'}">
        <g:render template="/templates/management/notes"/>
    </g:elseif>
    <g:elseif test="${params.tab == 'documents'}">
        <g:render template="/templates/management/documents"/>
    </g:elseif>

    <g:if test="${filteredSubscriptions}">
        <semui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                        next="${message(code: 'default.paginate.next')}"
                        prev="${message(code: 'default.paginate.prev')}" max="${max}"
                        total="${num_sub_rows}"/>
    </g:if>
</g:else>

</body>
</html>
