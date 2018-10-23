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

        <span class="la-forward-back">
            <g:if test="${navPrevSubscription}">
                <g:link controller="subscriptionDetails" action="changes" params="[id:navPrevSubscription.id]"><i class="chevron left icon"></i></g:link>
            </g:if>
            <g:else>
                <i class="chevron left icon disabled"></i>
            </g:else>

            <g:formatDate date="${subscription.startDate}" format="${message(code: 'default.date.format.notime')}"/>
            ${subscription.endDate ?  "- "+g.formatDate(date: subscription.endDate, format: message(code: 'default.date.format.notime')) : ''}

            <g:if test="${navNextSubscription}">
                <g:link controller="subscriptionDetails" action="changes" params="[id:navNextSubscription.id]"><i class="chevron right icon"></i></g:link>
            </g:if>
            <g:else>
                <i class="chevron right icon disabled"></i>
            </g:else>
        </span>

    </h1>

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
              <th>${message(code:'subscription.details.todo_history.descr', default:'ToDo Description')}</th>
              <th>${message(code:'subscription.details.todo_history.outcome', default:'Outcome')}</th>
              <th>${message(code:'default.date.label', default:'Date')}</th>
            </tr>
          </thead>
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

        <semui:paginate  action="todoHistory" controller="subscriptionDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${todoHistoryLinesTotal}" />

</body>
</html>
