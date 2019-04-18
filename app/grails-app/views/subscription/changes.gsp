<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.label', default:'Subscription')}</title>
</head>

<body>

    <g:render template="breadcrumb" model="${[ subscriptionInstance: subscription, params:params ]}"/>
    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>
    <h1 class="ui icon header"><semui:headerIcon />
        <semui:xEditable owner="${subscription}" field="name" />
    </h1>
    <semui:anualRings object="${subscription}" controller="subscription" action="changes" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


    <g:render template="nav" contextPath="." />

    <g:if test="${subscriptionInstance.instanceOf && (contextOrg?.id == subscriptionInstance.getConsortia()?.id)}">
        <g:render template="message" />
    </g:if>

    <div class="ui info message">
        <div class="header"></div>
        <p>
            Hier sehen Sie:
            <ul>
                <li>Änderungen am Bestand einer Lizenz</li>
                <li>Änderung an der Lizenz durch die Konsortialstelle (nur vererbte Änderungen)</li>
            </ul>
        </p>
    </div>

    <table class="ui celled la-table table">
          <thead>
            <tr>
              <th>${message(code:'subscription.details.todo_history.descr', default:'ToDo Description')}</th>
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
                    <%--${message(code:'subscription.details.todo_history.by_on', args:[hl.user?.display?:hl.user?.username])}--%>
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

        <semui:paginate  action="todoHistory" controller="subscription" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${todoHistoryLinesTotal}" />

</body>
</html>
