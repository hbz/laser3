<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.label', default:'Subscription')}</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb controller="myInstitution" action="dashboard" text="${org.getDesignation()}" />
      <semui:crumb message="default.documents.label" class="active"/>
    </semui:breadcrumbs>

    <semui:controlButtons>
      <g:render template="actions" model="[org:org]" />
    </semui:controlButtons>
    <semui:messages data="${flash}" />

    <h1 class="ui left aligned icon header"><semui:headerIcon />${org.name}</h1>

    <semui:messages data="${flash}" />

    <g:render template="/templates/documents/table" model="${[instance:org, context:'documents', redirect:'documents']}"/>

  </body>
</html>
