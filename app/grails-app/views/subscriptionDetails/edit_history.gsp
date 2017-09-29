<!doctype html>
<html>
    <head>
        <meta name="layout" content="mmbootstrap"/>
        <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
</head>

<body>

    <div class="container">
        <ul class="breadcrumb">
            <li> <g:link controller="home" action="index">${message(code:'default.home', default:'Home')}</g:link> <span class="divider">/</span> </li>
            <g:if test="${params.shortcode}">
              <li> <g:link controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:params.shortcode]}"> ${params.shortcode} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</g:link> <span class="divider">/</span> </li>
            </g:if>
            <li>${message(code:'subscription.label', default:'Subscriptions')} - ${message(code:'license.nav.edit_history')}</li>
        </ul>
    </div>

    <div class="container">
        <h1>${subscription.name}</h1>
        <g:render template="nav" contextPath="." />
    </div>

    <div class="container">
      <h3>${message(code:'subscription.details.edit_history.label', default:'Subscription history')}</h3>
      <table  class="table table-striped table-bordered">
        <tr>
          <th>${message(code:'default.eventID.label', default:'Event ID')}</th>
          <th>${message(code:'default.person.label', default:'Person')}</th>
          <th>${message(code:'default.date.label', default:'Date')}</th>
          <th>${message(code:'default.event.label', default:'Event')}</th>
          <th>${message(code:'default.field.label', default:'Field')}</th>
          <th>${message(code:'default.oldValue.label', default:'Old Value')}</th>
          <th>${message(code:'default.newValue.label', default:'New Value')}</th>
        </tr>
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
       <div class="pagination">
        <bootstrap:paginate  action="edit_history" controller="subscriptionDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${historyLinesTotal}" />
      </div>
    </div>

</body>
</html>
