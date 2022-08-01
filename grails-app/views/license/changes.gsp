<laser:htmlStart message="license.nav.todo_history" />

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <ui:h1HeaderWithIcon>
        <ui:xEditable owner="${license}" field="reference" id="reference"/>
        <ui:totalNumber total="${todoHistoryLinesTotal}"/>
    </ui:h1HeaderWithIcon>

    <ui:anualRings object="${license}" controller="license" action="changes" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

    <laser:render template="nav" />


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

        <ui:paginate action="todoHistory" controller="license" params="${params}" max="${max}" total="${todoHistoryLinesTotal}" />


<laser:htmlEnd />
