<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap"/>
    <title>KB+ ${message(code:'subscription.label', default:'Subscription')}</title>
  </head>

  <body>

  <g:render template="/templates/addDocument" model="${[doclist:subscriptionInstance.documents, ownobj:subscriptionInstance, owntp:'subscription']}" />

    <div class="container">
      <ul class="breadcrumb">
        <li> <g:link controller="home" action="index">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span> </li>
        <g:if test="${subscriptionInstance.subscriber}">
          <li> <g:link controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:subscriptionInstance.subscriber.shortcode]}"> ${subscriptionInstance.subscriber.name} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</g:link> <span class="divider">/</span> </li>
        </g:if>
        <li> <g:link controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}">${message(code:'subscription.label', default:'Subscription')} ${subscriptionInstance.id} - ${message(code:'default.details.label', default:'Details')}</g:link> </li>
        <g:if test="${editable}">
          <li class="pull-right"><span class="badge badge-warning">${message(code:'default.editable', default:'Editable')}</span>&nbsp;</li>
        </g:if>
      </ul>
    </div>

    <g:if test="${flash.message}">
      <div class="container"><bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert></div>
    </g:if>

    <g:if test="${flash.error}">
      <div class="container"><bootstrap:alert class="alert-error">${flash.error}</bootstrap:alert></div>
    </g:if>

    <div class="container">

       <h1>${subscriptionInstance?.name}</h1>

       <g:render template="nav" />

    </div>


    <div class="container">

        <g:render template="/templates/documents_table"
                  model="${[instance:subscriptionInstance,context:'documents',redirect:'documents']}" />
    </div>

  </body>
</html>
