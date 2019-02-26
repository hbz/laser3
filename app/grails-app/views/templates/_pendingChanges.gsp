
<g:if test="${processingpc}">
     <bootstrap:alert class="alert-warning"><g:message code="pendingchange.inprogress"/></bootstrap:alert>
</g:if>

<g:if test="${editable && pendingChanges?.size() > 0}">

    <g:if test="${! tmplSimpleView}">
        <div class="ui segment">

            <h3 class="ui header">
                ${message(code:'template.pendingChanges', default:'There are pending change notifications')}
            </h3>
    </g:if>

        <g:if test="${! tmplSimpleView}">
            <g:if test="${! processingpc}">
                <g:link controller="pendingChange" action="acceptAll" params="[OID: model.class.name + ':' + model.id]"
                        class="ui positive button">
                  ${message(code:'template.pendingChanges.accept_all', default:'Accept All')}
              </g:link>
                <g:link controller="pendingChange" action="rejectAll" params="[OID: model.class.name + ':' + model.id]"
                        class="ui negative button">
                  ${message(code:'template.pendingChanges.reject_all', default:'Reject All')}
              </g:link>
            </g:if>

            <br />
        </g:if>


        <table class="ui celled la-table la-table-small table">
          <thead>
            <tr>
                <th></th>
              <th>${message(code:'default.info.label', default:'Info')}</th>
                <th>${message(code: 'event.timestamp', default: 'Timestamp')}</th>
              <th>${message(code:'default.actions.label', default:'Action')}</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${pendingChanges}" var="pc" status="i">
              <tr>
                <td>${i+1}</td>
                <td>
                    <g:if test="${pc.msgToken}">
                        <g:message code="${pc.msgToken}" args="${pc.getParsedParams()}" default="${pc.desc}" />
                        <%
                            //print pc.msgToken; /* avoid auto encodeAsHTML() */
                            //print pc.msgParams; /* avoid auto encodeAsHTML() */
                        %>
                    </g:if>
                    <g:else>
                        <% print pc.desc; /* avoid auto encodeAsHTML() */ %>
                    </g:else>
                </td>
                <td><g:formatDate format="${message(code: 'default.date.format')}" date="${pc.ts}"/></td>
                <td class="x">
                  <g:if test="${! processingpc}">
                    <g:link controller="pendingChange" action="accept" id="${pc.id}" class="ui icon positive button">
                      <i class="checkmark icon"></i>
                      <!--${message(code:'default.button.accept.label', default:'Accept')}-->
                    </g:link>
                    <g:link controller="pendingChange" action="reject" id="${pc.id}" class="ui icon negative button">
                      <i class="times icon"></i>
                      <!--${message(code:'default.button.reject.label', default:'Reject')}-->
                    </g:link>
                  </g:if>
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>

    <g:if test="${! tmplSimpleView}">
        </div><!-- .segment -->
    </g:if>

    <br />
</g:if>
