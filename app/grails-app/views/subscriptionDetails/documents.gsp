<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
  </head>

  <body>

  <g:render template="/templates/addDocument" model="${[doclist:subscriptionInstance.documents, ownobj:subscriptionInstance, owntp:'subscription']}" />

    <div>
      <ul class="breadcrumb">
        <li> <g:link controller="home" action="index">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span> </li>
        <g:if test="${params.shortcode}">
          <li> <g:link controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:params.shortcode]}"> ${params.shortcode} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</g:link> <span class="divider">/</span> </li>
        </g:if>
        <li> <g:link controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}">${message(code:'subscription.label', default:'Subscription')} ${subscriptionInstance.id} - ${message(code:'default.details.label', default:'Details')}</g:link> </li>
      </ul>
    </div>

    <g:render template="actions" />

    <semui:messages data="${flash}" />

      <h1 class="ui header">
          <semui:editableLabel editable="${editable}" />
          <semui:xEditable owner="${subscriptionInstance}" field="name" />
      </h1>

    <g:render template="nav" />

    <g:render template="/templates/documents_table"
                  model="${[instance:subscriptionInstance,context:'documents',redirect:'documents']}" />

  </body>
</html>
