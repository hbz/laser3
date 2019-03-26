<%@page import="com.k_int.kbplus.*" %>
<laser:serviceInjection/>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'default.documents.label')}</title>
  </head>

  <body>
    <semui:breadcrumbs>
      <semui:crumb controller="myInstitution" action="dashboard" text="${contextService.org.getDesignation()}" />
      <semui:crumb message="default.documents.label" class="active"/>
    </semui:breadcrumbs>

    <semui:controlButtons>
      <g:render template="actions" />
    </semui:controlButtons>
    <semui:messages data="${flash}" />

    <h1 class="ui left aligned icon header"><semui:headerIcon />${contextService.org.name}</h1>

    <g:render template="/templates/documents/filter" model="${[availableUsers:availableUsers]}"/>

    <g:render template="/templates/documents/table" model="${[instance:[documents:documents], context:'documents', redirect:'documents', owntp: 'org']}"/>
  </body>
</html>
