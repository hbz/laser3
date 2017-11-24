<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
  </head>

  <body>

      <semui:breadcrumbs>
          <g:if test="${params.shortcode}">
              <semui:crumb controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:params.shortcode]}" text="${params.shortcode} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}" />
          </g:if>
          <semui:crumb controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}"  text="${subscriptionInstance.name}" />
          <semui:crumb class="active" text="${message(code:'default.notes.label', default:'Notes')}" />
          <g:if test="${editable}">
              <li class="pull-right"><span class="badge badge-warning">${message(code:'default.editable', default:'Editable')}</span>&nbsp;</li>
          </g:if>
      </semui:breadcrumbs>

   <div>

       <h1 class="ui header">${subscriptionInstance?.name}</h1>

       <g:render template="nav" />

    </div>

    <div>
        <g:render template="/templates/notes_table" model="${[instance: subscriptionInstance, redirect: 'notes']}"/>
    </div>
  <g:render template="/templates/addNote"
            model="${[doclist: subscriptionInstance.documents, ownobj: subscriptionInstance, owntp: 'subscription']}"/>
    
  </body>
</html>
