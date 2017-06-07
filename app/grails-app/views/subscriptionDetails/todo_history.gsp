<!doctype html>
<html>
    <head>
        <meta name="layout" content="mmbootstrap"/>
        <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
</head>

<body>

    <div class="container">
        <ul class="breadcrumb">
            <li> <g:link controller="home" action="index">${message(code:'default', default:'Home')}</g:link> <span class="divider">/</span> </li>
            <li>${message(code:'subscription.plural', default:'Subscriptions')}</li>
        </ul>
    </div>

    <div class="container">
        <h1>${subscription.name}</h1>
        <g:render template="nav" contextPath="." />
    </div>

    <div class="container">

      <h3>${message(code:'subscription.details.todo_history.label', default:'ToDo History')}</h3>

      <table  class="table table-striped table-bordered">
        <tr>
          <th>${message(code:'subscription.details.todo_history.descr', default:'ToDo Description')}</th>
          <th>${message(code:'subscription.details.todo_history.outcome', default:'Outcome')}</th>
          <th>${message(code:'default.date.label', default:'Date')}</th>
        </tr>
        <g:if test="${todoHistoryLines}">
          <g:each in="${todoHistoryLines}" var="hl">
            <tr>
              <td>${hl.desc}</td>
              <td>${hl.status?.value?:'Pending'}
                <g:if test="${((hl.status?.value=='Accepted')||(hl.status?.value=='Rejected'))}">
                  ${message(code:'subscription.details.todo_history.by_on', args:[hl.user?.display?:hl.user?.username])} <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${hl.actionDate}"/>
                </g:if>
              </td>
              <td><g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${hl.ts}"/></td>
            </tr>
          </g:each>
        </g:if>
      </table>
       <div class="pagination">
        <bootstrap:paginate  action="todo_history" controller="subscriptionDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${todoHistoryLinesTotal}" />
      </div>
</body>
</html>
