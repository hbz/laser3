<%@page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'default.documents.label')}</title>
  </head>

  <body>
    <%
      List notAvailable = [RefdataValue.getByValueAndCategory('ONIX-PL License','Document Type'),
                           RefdataValue.getByValueAndCategory('Note','Document Type'),
                           RefdataValue.getByValueAndCategory('Announcement','Document Type')]
      List documentTypes = RefdataCategory.getAllRefdataValues("Document Type")-notAvailable
    %>
    <semui:breadcrumbs>
      <semui:crumb controller="myInstitution" action="dashboard" text="${org.getDesignation()}" />
      <semui:crumb message="default.documents.label" class="active"/>
    </semui:breadcrumbs>

    <semui:controlButtons>
      <g:render template="actions" model="[org:org]" />
    </semui:controlButtons>
    <semui:messages data="${flash}" />

    <h1 class="ui left aligned icon header"><semui:headerIcon />${org.name}</h1>

    <g:render template="/templates/documents/table" model="${[instance:org, context:'documents', redirect:'documents', owntp: 'org']}"/>
  </body>
</html>
