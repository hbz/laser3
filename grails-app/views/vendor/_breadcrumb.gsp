<%@ page import="de.laser.storage.RDStore" %>
<laser:serviceInjection/>

<ui:breadcrumbs>
    <g:if test="${isMyVendor}">
        <ui:crumb message="menu.my.vendors" controller="myInstitution" action="currentVendors"/>
    </g:if>
    <g:else>
        <ui:crumb message="menu.public.all_vendors" controller="vendor" action="list"/>
    </g:else>
    <ui:crumb text="${vendor.dropdownNamingConvention()}" class="active"/>
</ui:breadcrumbs>