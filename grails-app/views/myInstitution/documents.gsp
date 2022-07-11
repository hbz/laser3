<%@page import="de.laser.Org" %>
<laser:htmlStart message="menu.my.documents" />

    <ui:breadcrumbs>
      <ui:crumb message="menu.my.documents" class="active"/>
    </ui:breadcrumbs>

    <ui:controlButtons>
      <laser:render template="actions" />
    </ui:controlButtons>
    <ui:messages data="${flash}" />

    <ui:h1HeaderWithIcon message="menu.my.documents" />

    <%-- does not work as it is mapped upon a DomainClass attribute <laser:render template="/templates/documents/filter" model="${[availableUsers:availableUsers]}"/>--%>

    <laser:render template="/templates/documents/table" model="${[instance: Org.get(institution.id), inContextOrg: true, context:'documents', redirect:'documents', owntp: 'org']}"/>

    <ui:paginate action="documents" params="${params}" total="${totalSize}"/>
  <laser:htmlEnd />
