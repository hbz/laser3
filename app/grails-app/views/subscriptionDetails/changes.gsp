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
    <h1 class="ui left aligned icon header"><semui:headerIcon />
        <semui:xEditable owner="${subscription}" field="name" />
        <semui:anualRings object="${subscription}" controller="subscriptionDetails" action="changes" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>
    </h1>

    <g:render template="nav" contextPath="." />

    <g:if test="${subscriptionInstance.instanceOf && (contextOrg?.id == subscriptionInstance.getConsortia()?.id)}">
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

        <semui:paginate  action="todoHistory" controller="subscriptionDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${todoHistoryLinesTotal}" />

</body>
</html>
