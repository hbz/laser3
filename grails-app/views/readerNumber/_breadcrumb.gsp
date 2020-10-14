<%@ page import="de.laser.helper.RDStore" %>
<semui:breadcrumbs>
    <g:if test="${(RDStore.OT_PROVIDER.id in orgInstance.getAllOrgTypeIds())}">
        <semui:crumb controller="organisation" action="show" id="${orgInstance.id}" text="${orgInstance.name}" />
        <semui:crumb message="" class="active"/>
    </g:if>
    <g:else>
        <semui:crumb controller="organisation" action="show" id="${orgInstance.id}" text="${orgInstance.name}" />
        <semui:crumb message="menu.institutions.addressbook" class="active"/>
    </g:else>
</semui:breadcrumbs>