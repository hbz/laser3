
<g:if test="${processingpc}">
    <ui:msg class="negative"  message="pendingchange.inprogress" />
</g:if>

<g:if test="${editable && pendingChanges?.size() > 0}">

    <g:if test="${! tmplSimpleView}">
        <div class="ui segment">
            <h3 class="ui header">
                ${message(code:'template.pendingChanges')}
            </h3>
    </g:if>

        <g:if test="${! tmplSimpleView}">
            <g:if test="${! processingpc}">
                <g:link controller="pendingChange" action="acceptAll" params="[OID: model.class.name + ':' + model.id]"
                        class="ui positive button">
                  ${message(code:'template.pendingChanges.accept_all')}
              </g:link>
                <g:link controller="pendingChange" action="rejectAll" params="[OID: model.class.name + ':' + model.id]"
                        class="ui negative button">
                  ${message(code:'template.pendingChanges.reject_all')}
              </g:link>
            </g:if>

            <br />
        </g:if>


        <table class="ui celled la-js-responsive-table la-table compact table">
          <thead>
            <tr>
                <th>${pendingChanges.size()? "1-"+pendingChanges.size() : 0}</th>
              <th>${message(code:'default.info.label')}</th>
                <th>${message(code: 'event.timestamp')}</th>
              <th>${message(code:'default.actions.label')}</th>
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
                    <g:link controller="pendingChange" action="accept" id="${pc.id}" class="ui icon positive button la-modern-button">
                      <i class="checkmark icon"></i>
                      <!--${message(code:'default.button.accept.label')}-->
                    </g:link>
                    <g:link controller="pendingChange" action="reject" id="${pc.id}" class="ui icon negative button la-modern-button">
                      <i class="times icon"></i>
                      <!--${message(code:'default.button.reject.label')}-->
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
