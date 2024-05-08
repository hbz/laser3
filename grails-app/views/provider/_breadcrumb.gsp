<%@ page import="de.laser.storage.RDStore" %>
<laser:serviceInjection/>

<ui:breadcrumbs>
    <g:if test="${isMyVendor}">
        <ui:crumb message="menu.my.vendors" controller="myInstitution" action="currentProviders"/>
    </g:if>
    <g:else>
        <ui:crumb message="menu.public.all_vendors" controller="organisation" action="listProvider"/>
    </g:else>
    <ui:crumb text="${provider.sortname}" class="active"/>
</ui:breadcrumbs>