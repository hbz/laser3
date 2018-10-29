<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.label', default:'Subscription')}</title>
  </head>

  <body>

    <g:render template="breadcrumb" model="${[ params:params ]}"/>
    <semui:controlButtons>
      <g:render template="actions" />
    </semui:controlButtons>
    <semui:messages data="${flash}" />

      <h1 class="ui left aligned icon header"><semui:headerIcon />

          <semui:xEditable owner="${subscriptionInstance}" field="name" />

          <span class="la-forward-back">
            <g:if test="${navPrevSubscription}">
              <g:link controller="subscriptionDetails" action="documents" params="[id:navPrevSubscription.id]"><i class="chevron left icon"></i></g:link>
            </g:if>
            <g:else>
              <i class="chevron left icon disabled"></i>
            </g:else>

            <g:formatDate date="${subscriptionInstance.startDate}" format="${message(code: 'default.date.format.notime')}"/>
            ${subscriptionInstance.endDate ?  "- "+g.formatDate(date: subscriptionInstance.endDate, format: message(code: 'default.date.format.notime')) : ''}

            <g:if test="${navNextSubscription}">
              <g:link controller="subscriptionDetails" action="documents" params="[id:navNextSubscription.id]"><i class="chevron right icon"></i></g:link>
            </g:if>
            <g:else>
              <i class="chevron right icon disabled"></i>
            </g:else>
          </span>
      </h1>
      <semui:anualRings object="${subscriptionInstance}" controller="subscriptionDetails" action="documents" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}">
      </semui:anualRings>
    <g:render template="nav" />

    <g:if test="${subscriptionInstance.instanceOf && (contextOrg == subscriptionInstance.getConsortia())}">
      <div class="ui negative message">
        <div class="header"><g:message code="myinst.message.attention" /></div>
        <p>
          <g:message code="myinst.subscriptionDetails.message.ChildView" />
          <span class="ui label">${subscriptionInstance.getAllSubscribers()?.collect{itOrg -> itOrg.name}.join(',')}</span>.
        <g:message code="myinst.subscriptionDetails.message.ConsortialView" />
        <g:link controller="subscriptionDetails" action="show" id="${subscriptionInstance.instanceOf.id}"><g:message code="myinst.subscriptionDetails.message.here" /></g:link>.
        </p>
      </div>
    </g:if>

    <semui:messages data="${flash}" />

    <g:render template="/templates/documents/table" model="${[instance:subscriptionInstance, context:'documents', redirect:'documents']}"/>

  </body>
</html>
