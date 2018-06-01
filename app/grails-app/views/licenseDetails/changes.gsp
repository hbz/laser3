<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'license.label', default:'License')}</title>
</head>
<body>
    <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <h1 class="ui header"><semui:headerIcon />

    ${license.licensee?.name}
    ${message(code:'license.details.type', args:["${license.type?.getI10n('value')}"], default:'License')} :
    <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </h1>

    <g:render template="nav" />


      <table  class="ui celled la-table table">
          <thead>
            <tr>
              <th>${message(code:'license.history.todo.description', default:'ToDo Description')}</th>
              <th>${message(code:'default.outcome.label', default:'Outcome')}</th>
              <th>${message(code:'default.date.label', default:'Date')}</th>
            </tr>
          </thead>
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

        <semui:paginate  action="todoHistory" controller="licenseDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${todoHistoryLinesTotal}" />


</body>
</html>
