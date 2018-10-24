<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'license.label', default:'License')}</title>
</head>
<body>
    <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <h1 class="ui left aligned icon header"><semui:headerIcon />
        <g:if test="${license.type?.value == 'Template'}">${message(code:'license.label')} (${license.type.getI10n('value')}):</g:if>
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </h1>

    <g:render template="nav" />


      <table  class="ui celled la-table table">
          <thead>
            <tr>
              <th>${message(code:'license.history.todo.description', default:'ToDo Description')}</th>
              <th>${message(code:'default.status.label', default:'Status')}</th>
              <th>${message(code:'default.date.label', default:'Date')}</th>
            </tr>
          </thead>
        <g:if test="${todoHistoryLines}">
          <g:each in="${todoHistoryLines}" var="hl">
            <tr>
              <td>

                  <g:if test="${hl.msgToken}">
                      <g:message code="${hl.msgToken}" args="${hl.getParsedParams()}" default="${hl.desc}" />
                  </g:if>
                  <g:else>
                      <% print hl.desc; /* avoid auto encodeAsHTML() */ %>
                  </g:else>

              </td>
              <td>
                <g:if test="${hl.status}">
                    ${hl.status?.getI10n('value')}
                </g:if>
                <g:else>
                    Ausstehend
                </g:else>

                <g:if test="${hl.status?.value in ['Accepted', 'Rejected']}">
                    <%--${message(code:'subscription.details.todo_history.by_on', args:[(hl.user?.display ?: hl.user?.username)])}--%>
                    / <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${hl.actionDate}"/>
                </g:if>
              </td>
              <td>
                  <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${hl.ts}"/>
              </td>
            </tr>
          </g:each>
        </g:if>
      </table>

        <semui:paginate  action="todoHistory" controller="licenseDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${todoHistoryLinesTotal}" />


</body>
</html>
