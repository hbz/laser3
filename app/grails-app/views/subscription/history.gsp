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

    <h1 class="ui icon header"><semui:headerIcon />
        <semui:xEditable owner="${subscription}" field="name" />
    </h1>
    <semui:anualRings object="${subscription}" controller="subscription" action="history" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


    <g:render template="nav" contextPath="." />

    <g:if test="${subscriptionInstance.instanceOf && (contextOrg?.id == subscriptionInstance.getConsortia()?.id)}">
        <g:render template="message" />
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

    <semui:paginate  action="history" controller="subscription" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${historyLinesTotal}" />


</body>
</html>
