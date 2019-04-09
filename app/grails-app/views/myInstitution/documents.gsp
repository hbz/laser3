<%@page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'default.documents.label')}</title>
  </head>

  <body>
    <semui:breadcrumbs>
      <semui:crumb controller="myInstitution" action="dashboard" text="${institution.getDesignation()}" />
      <semui:crumb message="menu.my.documents" class="active"/>
    </semui:breadcrumbs>

    <semui:controlButtons>
      <g:render template="actions" />
    </semui:controlButtons>
    <semui:messages data="${flash}" />

    <h1 class="ui left aligned icon header"><semui:headerIcon />${institution.name}</h1>

    <g:render template="/templates/documents/filter" model="${[availableUsers:availableUsers]}"/>

    <g:render template="/templates/documents/table" model="${[instance: Org.get(institution.id), context:'documents', redirect:'documents', owntp: 'org']}"/>

    <semui:paginate action="documents" params="${params}" total="${totalSize}"/>
  </body>
</html>
