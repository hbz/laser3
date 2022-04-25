<%@page import="de.laser.Org" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.my.documents')}</title>
  </head>

  <body>
    <semui:breadcrumbs>
      <semui:crumb message="menu.my.documents" class="active"/>
    </semui:breadcrumbs>

    <semui:controlButtons>
      <laser:render template="actions" />
    </semui:controlButtons>
    <semui:messages data="${flash}" />

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.my.documents')}</h1>

    <%-- does not work as it is mapped upon a DomainClass attribute <laser:render template="/templates/documents/filter" model="${[availableUsers:availableUsers]}"/>--%>

    <laser:render template="/templates/documents/table" model="${[instance: Org.get(institution.id), inContextOrg: true, context:'documents', redirect:'documents', owntp: 'org']}"/>

    <semui:paginate action="documents" params="${params}" total="${totalSize}"/>
  </body>
</html>
