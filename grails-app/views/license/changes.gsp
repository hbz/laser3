<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'license.nav.todo_history')}</title>
</head>
<body>
    <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
        <semui:totalNumber total="${todoHistoryLinesTotal}"/>
    </h1>

    <semui:anualRings object="${license}" controller="license" action="show" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

    <g:render template="nav" />


      <table  class="ui celled la-js-responsive-table la-table table">
          <thead>
            <tr>
              <th>${message(code:'license.history.todo.description')}</th>
              <th>${message(code:'default.status.label')}</th>
              <th>${message(code:'default.date.label')}</th>
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
                    / <g:formatDate format="${message(code:'default.date.format.notime')}" date="${hl.actionDate}"/>
                </g:if>
              </td>
              <td>
                  <g:formatDate format="${message(code:'default.date.format.notime')}" date="${hl.ts}"/>
              </td>
            </tr>
          </g:each>
        </g:if>
      </table>

        <semui:paginate  action="todoHistory" controller="license" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" max="${max}" total="${todoHistoryLinesTotal}" />


</body>
</html>
