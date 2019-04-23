<%@ page import="de.laser.helper.RDStore" %>
<semui:breadcrumbs>
    <g:if test="${(RDStore.OT_PROVIDER.id in orgInstance.getallOrgTypeIds())}">
        <semui:crumb message="menu.public.all_provider" controller="organisation" action="listProvider"/>
        <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
    </g:if>
    <g:elseif test="${RDStore.OT_INSTITUTION.id in orgInstance.getallOrgTypeIds()}">
        <semui:crumb message="menu.public.all_orgs" controller="organisation" action="index"/>
        <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
    </g:elseif>
    <g:elseif test="${RDStore.OT_DEPARTMENT.id in orgInstance.getallOrgTypeIds()}">
        <semui:crumb message="menu.my.departments" controller="myInstitution" action="manageMembers" params="[comboType:'Department']"/>
        <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
    </g:elseif>
</semui:breadcrumbs>