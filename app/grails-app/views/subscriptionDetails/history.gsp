<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.label', default:'Subscription')}</title>
</head>
<body>

    <g:render template="breadcrumb" model="${[ subscriptionInstance:subscription, params:params ]}"/>

    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>

    <h1 class="ui left aligned icon header"><semui:headerIcon />

        <semui:xEditable owner="${subscription}" field="name" />
        <span class="la-forward-back">
            <g:if test="${navPrevSubscription}">
                <g:link controller="subscriptionDetails" action="history" params="[id:navPrevSubscription.id]"><i class="chevron left icon"></i></g:link>
            </g:if>
            <g:else>
                <i class="chevron left icon disabled"></i>
            </g:else>

            <g:formatDate date="${subscription.startDate}" format="${message(code: 'default.date.format.notime')}"/>
            ${subscription.endDate ?  "- "+g.formatDate(date: subscription.endDate, format: message(code: 'default.date.format.notime')) : ''}

            <g:if test="${navNextSubscription}">
                <g:link controller="subscriptionDetails" action="history" params="[id:navNextSubscription.id]"><i class="chevron right icon"></i></g:link>
            </g:if>
            <g:else>
                <i class="chevron right icon disabled"></i>
            </g:else>
        </span>
    </h1>
    <semui:anualRings object="${subscription}" controller="subscriptionDetails" action="history" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}">
    </semui:anualRings>
    <g:render template="nav" contextPath="." />

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

      <table  class="ui celled la-table table">
        <thead>
            <tr>
              <th>${message(code:'default.eventID.label', default:'Event ID')}</th>
              <th>${message(code:'default.person.label', default:'Person')}</th>
              <th>${message(code:'default.date.label', default:'Date')}</th>
              <th>${message(code:'default.event.label', default:'Event')}</th>
              <th>${message(code:'default.field.label', default:'Field')}</th>
              <th>${message(code:'default.oldValue.label', default:'Old Value')}</th>
              <th>${message(code:'default.newValue.label', default:'New Value')}</th>
            </tr>
        </thead>
        <g:if test="${historyLines}">
          <g:each in="${historyLines}" var="hl">
            <tr>
              <td>${hl.id}</td>
              <td style="white-space:nowrap;">${hl.actor}</td>
              <td style="white-space:nowrap;">${hl.dateCreated}</td>
              <td style="white-space:nowrap;">${hl.eventName}</td>
              <td style="white-space:nowrap;">${hl.propertyName}</td>
              <td>${hl.oldValue}</td>
              <td>${hl.newValue}</td>
            </tr>
          </g:each>
        </g:if>
      </table>

    <semui:paginate  action="editHistory" controller="subscriptionDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${historyLinesTotal}" />


</body>
</html>
