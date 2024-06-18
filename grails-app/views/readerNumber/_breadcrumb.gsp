<%@ page import="de.laser.storage.RDStore" %>
<ui:breadcrumbs>
    <ui:crumb controller="organisation" action="show" id="${orgInstance.id}" text="${orgInstance.name}" />
    <ui:crumb message="menu.institutions.addressbook" class="active"/>
</ui:breadcrumbs>