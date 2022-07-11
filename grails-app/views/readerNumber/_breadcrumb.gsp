<%@ page import="de.laser.storage.RDStore" %>
<ui:breadcrumbs>
    <g:if test="${(RDStore.OT_PROVIDER.id in orgInstance.getAllOrgTypeIds())}">
        <ui:crumb controller="organisation" action="show" id="${orgInstance.id}" text="${orgInstance.name}" />
        <ui:crumb message="" class="active"/>
    </g:if>
    <g:else>
        <ui:crumb controller="organisation" action="show" id="${orgInstance.id}" text="${orgInstance.name}" />
        <ui:crumb message="menu.institutions.addressbook" class="active"/>
    </g:else>
</ui:breadcrumbs>