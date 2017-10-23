<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'license.label', default:'License')}</title>
</head>

<body>

    <div class="container">
      <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>
    </div>

    <div class="container">
        <h1>${license.licensee?.name} ${license.type?.value} License : ${license.reference}</h1>

<g:render template="nav" />

    </div>

    <div class="container">
      <h3>${message(code:'license.nav.todo_history', default:'ToDo History')}</h3>
      <table  class="ui celled striped table">
        <tr>
          <th>${message(code:'license.history.todo.description', default:'ToDo Description')}</th>
          <th>${message(code:'default.outcome.label', default:'Outcome')}</th>
          <th>${message(code:'default.date.label', default:'Date')}</th>
        </tr>
        <g:if test="${todoHistoryLines}">
          <g:each in="${todoHistoryLines}" var="hl">
            <tr>
              <td>${hl.desc}</td>
              <td>${hl.status?.value?:'Pending'}
                <g:if test="${((hl.status?.value=='Accepted')||(hl.status?.value=='Rejected'))}">
                  ${message(code:'subscription.details.todo_history.by_on', args:[(hl.user?.display?:hl.user?.username)])} <g:formatDate formatName="default.date.format.notime" date="${hl.actionDate}"/>
                </g:if>
              </td>
              <td><g:formatDate formatName="default.date.format.notime" date="${hl.ts}"/></td>
            </tr>
          </g:each>
        </g:if>
      </table>
            <div class="pagination">
        <bootstrap:paginate  action="todo_history" controller="licenseDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${todoHistoryLinesTotal}" />
      </div>
    </div>

</body>
</html>
