<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
  </head>

  <body>

    <div>
      <ul class="breadcrumb">
        <li> <g:link controller="home" action="index">${message(code:'default', default:'Home')}</g:link> <span class="divider">/</span> </li>
        <li> <g:link controller="myInstitution" action="currentSubscriptions">${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</g:link> <span class="divider">/</span> </li>
        <li> <g:link controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}">${message(code:'subscription.label', default:'Subscription')} ${subscriptionInstance.id} - ${message(code:'default.notes.label', default:'Notes')}</g:link> </li>
      </ul>
    </div>
    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>
    <h1 class="ui header">
      <semui:editableLabel editable="${editable}" />
      <g:inPlaceEdit domain="Subscription" pk="${subscriptionInstance.id}" field="name" id="name" class="newipe">${subscriptionInstance?.name}</g:inPlaceEdit>
    </h1>

    <g:render template="nav" contextPath="." />

      <g:link controller="subscriptionDetails"
                    action="launchRenewalsProcess" 
                    params="${[id:params.id]}">${message(code:'subscription.details.renewals.click_here', default:'Click Here')}</g:link> ${message(code:'subscription.details.renewals.note')}
    
  </body>
</html>
