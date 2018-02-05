<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
    </head>
    <body>
        <g:render template="breadcrumb" model="${[ params:params ]}"/>

        <g:render template="actions" />

        <h1 class="ui header">
            <semui:editableLabel editable="${editable}" />
            <semui:xEditable owner="${subscriptionInstance}" field="name" />
        </h1>

        <g:render template="nav" />

        <g:render template="/templates/notes/table" model="${[instance: subscriptionInstance, redirect: 'notes']}"/>

        <g:render template="/templates/notes/modal" model="${[doclist: subscriptionInstance.documents, ownobj: subscriptionInstance, owntp: 'subscription']}"/>
    
  </body>
</html>
