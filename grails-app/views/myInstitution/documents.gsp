<%@page import="de.laser.Org" %>
<laser:htmlStart message="menu.my.documents" />

    <ui:breadcrumbs>
      <ui:crumb message="menu.my.documents" class="active"/>
    </ui:breadcrumbs>

    <ui:controlButtons>
      <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon message="menu.my.documents" type="file" />

    <ui:messages data="${flash}" />

    <%-- does not work as it is mapped upon a DomainClass attribute <laser:render template="/templates/documents/filter" model="${[availableUsers:availableUsers]}"/>--%>

    <laser:render template="/templates/documents/table" model="${[instance: contextService.getOrg(), inContextOrg: true, context:'documents', redirect:'documents', owntp: 'org']}"/>

    <ui:paginate action="documents" params="${params}" total="${totalSize}"/>
<laser:htmlEnd />
