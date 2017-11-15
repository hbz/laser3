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
        <g:if test="${params.shortcode}">
          <li> <g:link controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:params.shortcode]}"> ${params.shortcode} ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</g:link> <span class="divider">/</span> </li>
        </g:if>
        <li> <g:link controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}">${message(code:'subscription.label', default:'Subscription')} ${subscriptionInstance.id} - ${message(code:'default.notes.label', default:'Notes')}</g:link> </li>
        <g:if test="${editable}">
          <li class="pull-right"><span class="badge badge-warning">${message(code:'default.editable', default:'Editable')}</span>&nbsp;</li>
        </g:if>
      </ul>
    </div>

   <div>
       <h1>${subscriptionInstance?.name}</h1>
       <g:render template="nav" contextPath="." />
    </div>

    <div>
      <g:link controller="subscriptionDetails"
                    action="launchRenewalsProcess" 
                    params="${[id:params.id]}">${message(code:'subscription.details.renewals.click_here', default:'Click Here')}</g:link> ${message(code:'subscription.details.renewals.note')}
    </div>
    
  </body>
</html>
