<laser:htmlStart message="license.nav.todo_history" />

    <laser:render template="breadcrumb" model="${[ subscription: subscription, params:params ]}"/>
    <ui:controlButtons>
        <laser:render template="actions" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon>
        <g:if test="${subscription.instanceOf && (contextOrg.id == subscription.getConsortia()?.id)}">
            <laser:render template="iconSubscriptionIsChild"/>
        </g:if>
        <ui:xEditable owner="${subscription}" field="name" />
        <ui:totalNumber total="${todoHistoryLinesTotal}"/>
    </ui:h1HeaderWithIcon>
    <ui:anualRings object="${subscription}" controller="subscription" action="changes" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <laser:render template="nav" />

    <g:if test="${subscription.instanceOf && (contextOrg.id == subscription.getConsortia()?.id)}">
        <laser:render template="message" />
    </g:if>

    <ui:msg class="info" noClose="true">
            Hier sehen Sie:
            <ul>
                <li>Änderungen am Bestand einer Lizenz</li>
                <li>Änderung an der Lizenz durch die Konsortialstelle (nur vererbte Änderungen)</li>
            </ul>
    </ui:msg>

    <table class="ui celled la-js-responsive-table la-table table">
          <thead>
            <tr>
              <th>${message(code:'subscription.details.todo_history.descr')}</th>
              <th>${message(code:'default.status.label')}</th>
              <th>${message(code:'default.date.label')}</th>
            </tr>
          </thead>
        <g:if test="${todoHistoryLines}">
          <g:each in="${todoHistoryLines}" var="hl">
            <tr>
              <td>
                  <g:if test="${hl.msgToken && hl.msgParams}">
                      <g:message code="${hl.msgToken}" args="${hl.getParsedParams()}" default="${hl.desc}" />
                  </g:if>
                  <g:if test="${hl.msgToken}">
                      ${message(code: 'subscription.packages.' + hl.msgToken)}
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
                    / <g:formatDate format="${message(code:'default.date.format.notime')}" date="${hl.actionDate}"/>
                </g:if>
              </td>
              <td>
                  <g:formatDate format="${message(code:'default.date.format.noZ')}" date="${hl.ts}"/>
              </td>
            </tr>
          </g:each>
        </g:if>
      </table>

        <ui:paginate action="todoHistory" controller="subscription" params="${params}" max="${max}" total="${todoHistoryLinesTotal}" />

<laser:htmlEnd />
