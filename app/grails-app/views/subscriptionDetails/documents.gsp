<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
  </head>

  <body>

    <g:render template="breadcrumb" model="${[ params:params ]}"/>

    <g:render template="actions" />

    <semui:messages data="${flash}" />

      <h1 class="ui header">
          <semui:editableLabel editable="${editable}" />
          <semui:xEditable owner="${subscriptionInstance}" field="name" />
      </h1>

    <g:render template="nav" />

    <g:render template="/templates/documents/table" model="${[instance:subscriptionInstance, context:'documents', redirect:'documents']}"/>

    <g:render template="/templates/documents/modal" model="${[doclist:subscriptionInstance.documents, ownobj:subscriptionInstance, owntp:'subscription']}"/>

  </body>
</html>
