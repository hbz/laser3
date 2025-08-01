<%@ page import="de.laser.storage.RDStore" %>
<laser:serviceInjection/>

<ui:breadcrumbs>
    <g:if test="${isMyProvider}">
        <ui:crumb message="menu.my.providers" controller="myInstitution" action="currentProviders"/>
    </g:if>
    <g:else>
        <ui:crumb message="menu.public.all_providers" controller="provider" action="list"/>
    </g:else>
    <ui:crumb text="${provider.name}" class="active"/>
</ui:breadcrumbs>