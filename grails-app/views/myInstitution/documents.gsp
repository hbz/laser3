<%@page import="de.laser.Org" %>
<laser:htmlStart message="menu.my.documents" />

    <semui:breadcrumbs>
      <semui:crumb message="menu.my.documents" class="active"/>
    </semui:breadcrumbs>

    <semui:controlButtons>
      <laser:render template="actions" />
    </semui:controlButtons>
    <semui:messages data="${flash}" />

    <semui:h1HeaderWithIcon message="menu.my.documents" />

    <%-- does not work as it is mapped upon a DomainClass attribute <laser:render template="/templates/documents/filter" model="${[availableUsers:availableUsers]}"/>--%>

    <laser:render template="/templates/documents/table" model="${[instance: Org.get(institution.id), inContextOrg: true, context:'documents', redirect:'documents', owntp: 'org']}"/>

    <semui:paginate action="documents" params="${params}" total="${totalSize}"/>
  <laser:htmlEnd />
